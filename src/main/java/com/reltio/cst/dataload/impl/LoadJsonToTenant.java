package com.reltio.cst.dataload.impl;

import static com.reltio.cst.dataload.DataloadConstants.DEFAULT_ERROR_CODE;
import static com.reltio.cst.dataload.DataloadConstants.GSON;
import static com.reltio.cst.dataload.DataloadConstants.JSON_FILE_TYPE_ARRAY;
import static com.reltio.cst.dataload.DataloadConstants.JSON_FILE_TYPE_PIPE;
import static com.reltio.cst.dataload.DataloadConstants.MAX_FAILURE_COUNT;
import static com.reltio.cst.dataload.util.DataloadFunctions.printDataloadPerformance;
import static com.reltio.cst.dataload.util.DataloadFunctions.sendEntities;
import static com.reltio.cst.dataload.util.DataloadFunctions.waitForTasksReady;
import static com.reltio.cst.dataload.util.DataloadFunctions.waitForTenantStatus;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.reflect.TypeToken;
import com.reltio.cst.dataload.DataloadConstants;
import com.reltio.cst.dataload.domain.Crosswalk;
import com.reltio.cst.dataload.domain.DataloaderInput;
import com.reltio.cst.dataload.domain.EntityRequest;
import com.reltio.cst.dataload.domain.EntityResponse;
import com.reltio.cst.dataload.domain.ReltioCrosswalkObject;
import com.reltio.cst.dataload.domain.ReltioDataloadErrors;
import com.reltio.cst.dataload.domain.ReltioDataloadResponse;
import com.reltio.cst.dataload.impl.helper.ProcessTrackerService;
import com.reltio.cst.dataload.util.DataloadFunctions;
import com.reltio.cst.exception.handler.APICallFailureException;
import com.reltio.cst.exception.handler.GenericException;
import com.reltio.cst.exception.handler.ReltioAPICallFailureException;
import com.reltio.cst.service.ReltioAPIService;
import com.reltio.cst.service.TokenGeneratorService;
import com.reltio.cst.service.impl.SimpleReltioAPIServiceImpl;
import com.reltio.cst.service.impl.SimpleRestAPIServiceImpl;
import com.reltio.cst.service.impl.TokenGeneratorServiceImpl;
import com.reltio.cst.util.Util;
import com.reltio.file.ReltioCSVFileWriter;
import com.reltio.file.ReltioFileReader;
import com.reltio.file.ReltioFileWriter;
import com.reltio.file.ReltioFlatFileReader;
import com.reltio.file.ReltioFlatFileWriter;

public class LoadJsonToTenant {

	public static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	private static final Logger logger = LogManager.getLogger(LoadJsonToTenant.class.getName());

	public static void main(String[] args) throws Exception {

		final long[] pStartTime = new long[1];
		
		final boolean[] flag = new boolean[1];
		if (args.length == 0) {
			logger.error("Validation failed as no argument passed");
			return;
		}

		flag[0] = true;

		Properties properties = new Properties();

		try {
			String propertyFilePath = args[0];
			if (!new File(propertyFilePath).exists()) {
				logger.error("Validation failed as configuration file passed does not exist = " + propertyFilePath);
				return;
			}
			properties = Util.getProperties(propertyFilePath, "PASSWORD");
		} catch (Exception e) {
			logger.error("Failed to Read the Properties File :: ", e.getMessage());
			return;
		}

		final DataloaderInput dataloaderInput = new DataloaderInput(properties);

		if (args != null && args.length > 1) {
			dataloaderInput.setFileName(args[1]);
			dataloaderInput.setFailedRecordsFileName(args[2]);
			dataloaderInput.setDataloadType(args[3]);
			dataloaderInput.setDataType(args[4]);
		}

		final int MAX_QUEUE_SIZE_MULTIPLICATOR = 10;

		// Validate the Input Data Provided
		List<String> availableDataloadTypes = Arrays.asList("entities", "relations", "interactions", "groups");

		if (!availableDataloadTypes.contains(dataloaderInput.getDataloadType().toLowerCase())) {
			logger.error(
					"Validation failed as Invalid DATALOAD_TYPE provided. It Should be (Entities/Relations/Interactions/Groups)",
					dataloaderInput.getDataloadType());
			return;
		}

		SimpleRestAPIServiceImpl.setupRequestsLogger(dataloaderInput.getRequestsLogFilePath());

		try {
			final ReltioFileWriter reltioFileWriter = new ReltioFlatFileWriter(
					dataloaderInput.getFailedRecordsFileName());

			Map<String, String> params = new HashMap<>();

			StringBuilder apiUriBuilder = new StringBuilder().append(dataloaderInput.getBaseDataloadURL()).append('/')
					.append(dataloaderInput.getDataloadType().toLowerCase()).append('?');

			if (dataloaderInput.isMaxObjectsUpdatePresent()) {
				params.put("maxObjectsToUpdate", dataloaderInput.getMaxObjectsToUpdate().toString());
			}

			if (!dataloaderInput.getReturnFullBody()) {
				params.put("returnUriOnly", "true");
			}

			if (dataloaderInput.getIsPartialOverride() && dataloaderInput.getIsUpdateAttributeUpdateDates()) {
				params.put("options", "partialOverride,updateAttributeUpdateDates");
			} else if (dataloaderInput.getIsPartialOverride() && !dataloaderInput.getIsUpdateAttributeUpdateDates()) {
				params.put("options", "partialOverride");
			} else if (!dataloaderInput.getIsPartialOverride() && dataloaderInput.getIsUpdateAttributeUpdateDates()) {
				params.put("options", "updateAttributeUpdateDates");
			}
			// to stop LCA execution
			if (!dataloaderInput.getIsExecuteLCA()) {
				params.put("executeLCA", "false");
			}

			params.forEach((k, v) -> {
				apiUriBuilder.append("&" + k + "=" + v);
			});

			final String apiUrl = apiUriBuilder.toString().replaceFirst("&", "");

			ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors
					.newFixedThreadPool(dataloaderInput.getThreadCount());

			ReltioFileReader fileReader;
			if (dataloaderInput.getJsonFileType().equalsIgnoreCase(JSON_FILE_TYPE_PIPE)) {
				fileReader = new ReltioFlatFileReader(dataloaderInput.getFileName(), "|",
						StandardCharsets.UTF_8.name());
			} else {
				fileReader = new ReltioFlatFileReader(dataloaderInput.getFileName(), null,
						StandardCharsets.UTF_8.name());
			}

			// Create Token Generator Service
			TokenGeneratorService tokenGeneratorService = null;
			try {
				tokenGeneratorService = new TokenGeneratorServiceImpl(dataloaderInput.getUsername(),
						dataloaderInput.getPassword(), dataloaderInput.getAuthURL());
			} catch (APICallFailureException | GenericException e2) {
				logger.debug(e2);
				logger.error(
						"Token Generation Process Failed. Please verify username/password and restart the process again...");
				System.exit(-1);
			}
			tokenGeneratorService.startBackgroundTokenGenerator();

			logger.info("DataLoad started for Tenant :: " + apiUrl);
			// System.out.println("DataLoad started for Tenant :: " + apiUrl);

			final ReltioAPIService reltioAPIService = new SimpleReltioAPIServiceImpl(tokenGeneratorService,
					dataloaderInput.getTimeoutInMinutes());
			final ProcessTrackerService processTrackerService = new ProcessTrackerService(dataloaderInput,
					reltioAPIService);
			int count = 0;

			pStartTime[0] = dataloaderInput.getProgramStartTime();

			long totalQueueWaitingTime = 0L;
			long totalFuturesExecutionTime = 0L;

			boolean eof = false;

			List<Future<Long>> futures = new ArrayList<>();

			ReltioCSVFileWriter uriWriter = new ReltioCSVFileWriter(dataloaderInput.getUriFilePath());

			while (!eof) {

				try {
					totalQueueWaitingTime += waitForTenantStatus(apiUrl, executorService, reltioAPIService,
							dataloaderInput.getTenantId());
				} catch (GenericException | InterruptedException e2) {
					logger.debug(e2);
					dataloaderInput.setLastUpdateTime(sdf.format(System.currentTimeMillis()));
					dataloaderInput.setStatus("Aborted");
					dataloaderInput.setLog(
							dataloaderInput.getLog() + System.lineSeparator() + " Failed to get the Queue Size...");
					try {
						processTrackerService.sendProcessTrackerUpdate(true);
					} catch (GenericException | ReltioAPICallFailureException e) {
						logger.error(
								"Aborting process to failure on getting the Queue details.. Also recent update not sent to Process tracker....");
						logger.error(e.getMessage());
						logger.debug(e);
					}
					System.exit(-1);
				}

				List<Object> inputRecords = new ArrayList<>();
				boolean isArray;
				int jsonIndex = 0;
				if (dataloaderInput.getJsonFileType().equalsIgnoreCase(JSON_FILE_TYPE_PIPE)) {
					jsonIndex = 1;
					isArray = true;
				} else
					isArray = dataloaderInput.getJsonFileType().equalsIgnoreCase(JSON_FILE_TYPE_ARRAY);
				// Initially we would put 10 times more requests than threads number
				// in executor service. It will get us
				// non-empty queue on waiting for tasks steps. When we wait for
				// tasks, we need to be sure that we not waste
				// time when part of tasks are done and part are still pending...
				for (int threadNum = futures.size(); threadNum < dataloaderInput.getThreadCount()
						* MAX_QUEUE_SIZE_MULTIPLICATOR; threadNum++) {
					inputRecords.clear();
					for (int k = 0; k < dataloaderInput.getGroupsCount(); k++) {
						String[] nextEntity = null;

						try {
							nextEntity = fileReader.readLine();
						} catch (Exception e) {
							logger.error(e.getMessage());
							logger.debug(e);
							nextEntity = fileReader.readLine();
						}
						if (nextEntity == null) {
							eof = true;
							break;
						}

						if (nextEntity.length == (jsonIndex + 1)) {
							if (isArray) {
								try {
									List<Object> recordsInLine = GSON.fromJson(nextEntity[jsonIndex],
											new TypeToken<List<Object>>() {
											}.getType());
									inputRecords.addAll(recordsInLine);
									count = count + recordsInLine.size();
								} catch (Exception e) {
									count = count + 1;
									DataloadFunctions.invalidJSonError("Invalid JSON|" + nextEntity[jsonIndex],
											dataloaderInput, reltioFileWriter);
								}
							} else {
								try {

									Object recordInLine = GSON.fromJson(nextEntity[jsonIndex], Object.class);
									inputRecords.add(recordInLine);
									count = count + 1;

								} catch (Exception e) {
									count = count + 1;
									DataloadFunctions.invalidJSonError("Invalid JSON|" + nextEntity[jsonIndex],
											dataloaderInput, reltioFileWriter);
								}

							}
						} else {

							String line = nextEntity[jsonIndex];
							for (int i = jsonIndex + 1; i < nextEntity.length; i++) {
								line += "|" + nextEntity[i];
							}
							try {
								List<Object> recordsInLine = GSON.fromJson(line, new TypeToken<List<Object>>() {
								}.getType());
								inputRecords.addAll(recordsInLine);
								count = count + recordsInLine.size();
							} catch (Exception e) {
								count = count + 1;
								DataloadFunctions.invalidJSonError("Invalid JSON|" + nextEntity[jsonIndex],
										dataloaderInput, reltioFileWriter);
							}

						}
					}

					if (inputRecords.size() > 0) {
						final List<Object> totalRecordsSent = new ArrayList<>();
						totalRecordsSent.addAll(inputRecords);
						final String stringToSend = GSON.toJson(totalRecordsSent);
						final int currentCount = count;

						futures.add(executorService.submit(new Callable<Long>() {
							@Override
							public Long call() {
								long requestExecutionTime = 0L;
								long startTime = System.currentTimeMillis();
								try {

									String result = sendEntities(apiUrl, GSON.toJson(totalRecordsSent),
											reltioAPIService);

									List<ReltioDataloadResponse> dataloadResponses = GSON.fromJson(result,
											new TypeToken<List<ReltioDataloadResponse>>() {
											}.getType());
									List<Object> failedRecords = new ArrayList<Object>();

									int sucCount = 0;
									int failCcount = 0;

									if (dataloaderInput.getURIrequired()) {
										writeUris(uriWriter, stringToSend, result);
									}

									for (ReltioDataloadResponse reltioDataloadResponse : dataloadResponses) {
										if (reltioDataloadResponse.getSuccessful()) {

											sucCount++;
										} else {
											failCcount++;

											Object failedRec = totalRecordsSent.get(reltioDataloadResponse.getIndex());
											failedRecords.add(failedRec);
											ReltioDataloadErrors dataloadErrors = reltioDataloadResponse.getErrors();
											ReltioCrosswalkObject crosswalkObject = GSON
													.fromJson(GSON.toJson(failedRec), ReltioCrosswalkObject.class);

											if (crosswalkObject != null && crosswalkObject.getCrosswalks() != null
													&& !crosswalkObject.getCrosswalks().isEmpty()) {
												dataloadErrors.setCrosswalkType(
														crosswalkObject.getCrosswalks().get(0).getType());
												dataloadErrors.setCrosswalkValue(
														crosswalkObject.getCrosswalks().get(0).getValue() + "");
											}
											List<ReltioDataloadErrors> reltioDataloadErrors = dataloaderInput
													.getDataloadErrorsMap().get(dataloadErrors.getErrorCode());
											if (reltioDataloadErrors == null) {
												reltioDataloadErrors = new ArrayList<>();
											}
											reltioDataloadErrors.add(dataloadErrors);
											dataloaderInput.getDataloadErrorsMap().put(dataloadErrors.getErrorCode(),
													reltioDataloadErrors);
										}
									}

									dataloaderInput.addFailureCount(failCcount);
									dataloaderInput.addSuccessCount(sucCount);

									if (failCcount > 0) {
										logger.info("Success entities=" + sucCount + "|Total Sent entities="
												+ currentCount + "| Failed Entity Count=" + failCcount + "|" + "ERROR:"
												+ result + "\nFailure Count: " + failCcount + "|"
												+ GSON.toJson(failedRecords));
										reltioFileWriter.writeToFile(
												"Failure Count: " + failCcount + "|" + GSON.toJson(failedRecords));

										for (Entry<Integer, List<ReltioDataloadErrors>> errorMap : dataloaderInput
												.getDataloadErrorsMap().entrySet()) {
											if (errorMap.getValue().size() > MAX_FAILURE_COUNT) {
												dataloaderInput
														.setLastUpdateTime(sdf.format(System.currentTimeMillis()));
												dataloaderInput.setStatus("Aborted");
												processTrackerService.sendProcessTrackerUpdate(true);
												logger.error(
														"Killing process as there are lot of failures while loading the data. Please verify the JSON and relaod again. More details can be found in the Process Tracker Enitity on the tenant: ");
												System.exit(-1);
											}
										}

									} else {
										logger.info("Success entities=" + sucCount + "|Total Sent entities="
												+ currentCount);
									}

									long updateTime = System.currentTimeMillis() - pStartTime[0];
									int minutes = (int) ((updateTime / (1000 * 60)) % 60);

									if (minutes == 1) {
										if (flag[0]) {
											pStartTime[0] = System.currentTimeMillis();
											flag[0] = false;

											dataloaderInput.setLastUpdateTime(sdf.format(System.currentTimeMillis()));
											processTrackerService.sendProcessTrackerUpdate();
										}
									} else {
										flag[0] = true;
									}
								} catch (GenericException e) {
									List<ReltioCrosswalkObject> crosswalkObjects = GSON.fromJson(stringToSend,
											new TypeToken<List<ReltioCrosswalkObject>>() {
											}.getType());
									dataloaderInput.addFailureCount(crosswalkObjects.size());
									List<ReltioDataloadErrors> dataloadErrors = dataloaderInput.getDataloadErrorsMap()
											.get(DEFAULT_ERROR_CODE);
									ReltioDataloadErrors reltioDataloadError = new ReltioDataloadErrors();

									if (dataloadErrors == null) {
										dataloadErrors = new ArrayList<>();
									}
									for (ReltioCrosswalkObject crosswalkObject : crosswalkObjects) {
										reltioDataloadError = new ReltioDataloadErrors();
										reltioDataloadError.setErrorCode(DEFAULT_ERROR_CODE);
										reltioDataloadError.setErrorMessage(e.getExceptionMessage());
										reltioDataloadError
												.setCrosswalkType(crosswalkObject.getCrosswalks().get(0).getType());
										reltioDataloadError.setCrosswalkValue(
												(String) crosswalkObject.getCrosswalks().get(0).getValue());
										dataloadErrors.add(reltioDataloadError);
									}
									dataloaderInput.getDataloadErrorsMap().put(DEFAULT_ERROR_CODE, dataloadErrors);
									for (Entry<Integer, List<ReltioDataloadErrors>> errorMap : dataloaderInput
											.getDataloadErrorsMap().entrySet()) {
										if (errorMap.getValue().size() > MAX_FAILURE_COUNT) {
											dataloaderInput.setLastUpdateTime(sdf.format(System.currentTimeMillis()));
											dataloaderInput.setStatus("Aborted");
											try {
												processTrackerService.sendProcessTrackerUpdate(true);
											} catch (GenericException e1) {
												logger.error(e1.getExceptionMessage());
											} catch (ReltioAPICallFailureException e1) {
												logger.error(e1.getErrorResponse());
											} catch (IOException e1) {
												logger.error(e1.getMessage());
											}
											logger.error(
													"Killing process as there are lot of failures while loading the data. Please verify the JSON and relaod again. More details can be found in the Process Tracker Enitity on the tenant: ");
											System.exit(-1);
										}
									}

									try {
										reltioFileWriter.writeToFile(DataloadConstants.FAILURE_LOG_KEY + stringToSend);
									} catch (IOException e1) {
										logger.error(e1.getMessage());
									}

								} catch (ReltioAPICallFailureException e) {

									List<ReltioCrosswalkObject> crosswalkObjects = GSON.fromJson(stringToSend,
											new TypeToken<List<ReltioCrosswalkObject>>() {
											}.getType());
									dataloaderInput.addFailureCount(crosswalkObjects.size());

									List<ReltioDataloadErrors> dataloadErrors = dataloaderInput.getDataloadErrorsMap()
											.get(e.getErrorCode());
									ReltioDataloadErrors reltioDataloadError = new ReltioDataloadErrors();

									if (dataloadErrors == null) {
										dataloadErrors = new ArrayList<>();
									}
									for (ReltioCrosswalkObject crosswalkObject : crosswalkObjects) {
										reltioDataloadError.setErrorCode(e.getErrorCode());
										reltioDataloadError.setErrorMessage(e.getErrorResponse());
										reltioDataloadError
												.setCrosswalkType(crosswalkObject.getCrosswalks().get(0).getType());
										reltioDataloadError.setCrosswalkValue(
												(String) crosswalkObject.getCrosswalks().get(0).getValue());
										dataloadErrors.add(reltioDataloadError);
									}
									dataloaderInput.getDataloadErrorsMap().put(e.getErrorCode(), dataloadErrors);
									for (Entry<Integer, List<ReltioDataloadErrors>> errorMap : dataloaderInput
											.getDataloadErrorsMap().entrySet()) {
										if (errorMap.getValue().size() > MAX_FAILURE_COUNT) {
											dataloaderInput.setLastUpdateTime(sdf.format(System.currentTimeMillis()));
											dataloaderInput.setStatus("Aborted");
											try {
												processTrackerService.sendProcessTrackerUpdate(true);
											} catch (IOException | GenericException
													| ReltioAPICallFailureException e1) {
												logger.error("failed to update Process tracker", e1.getMessage());

											}
											logger.error(
													"Killing process as there are lot of failures while loading the data. Please verify the JSON and relaod again. More details can be found in the Process Tracker Enitity on the tenant: ");
											System.exit(-1);
										}
									}

									try {
										reltioFileWriter.writeToFile(DataloadConstants.FAILURE_LOG_KEY + stringToSend);
									} catch (IOException e1) {
										logger.error(e1.getMessage());
									}

								} catch (IOException e) {
									logger.error(e.getMessage());
								}
								requestExecutionTime = System.currentTimeMillis() - startTime; // one

								return requestExecutionTime;
							}
						}));
					}
				}

				// We would not wait till the end - when all tasks are done, we need
				// to wait just for part of tasks to make sure we don't have large
				// queue in executor. After waiting for some low number of tasks
				// left in executor queue, we can continue with loading
				totalFuturesExecutionTime += waitForTasksReady(futures,
						dataloaderInput.getThreadCount() * (MAX_QUEUE_SIZE_MULTIPLICATOR / 2));

				printDataloadPerformance(executorService.getCompletedTaskCount() * dataloaderInput.getGroupsCount(),
						totalFuturesExecutionTime, totalQueueWaitingTime, dataloaderInput.getProgramStartTime(),
						dataloaderInput.getThreadCount());
			}
			totalFuturesExecutionTime += waitForTasksReady(futures, 0);
			dataloaderInput.setTotalRecordsCount(
					dataloaderInput.getSuccessRecordsCount() + dataloaderInput.getFailedRecordsCount());

			long endTime = System.currentTimeMillis();

			long finalTime = endTime - dataloaderInput.getProgramStartTime();
			dataloaderInput.setProgramEndTime(sdf.format(endTime));
			dataloaderInput.setLastUpdateTime(sdf.format(endTime));
			dataloaderInput.setTotalTimeTaken(finalTime);
			logger.info("All data send to API. Program will not wait for Queue to get Empty.");

			printDataloadPerformance(dataloaderInput.getTotalRecordsCount(), totalFuturesExecutionTime,
					totalQueueWaitingTime, dataloaderInput.getProgramStartTime(), dataloaderInput.getThreadCount());

			String status = "";
			if (dataloaderInput.getFailedRecordsCount() > 0 && dataloaderInput.getSuccessRecordsCount() > 0) {
				status = "Completed with some failed records";
			} else if (dataloaderInput.getFailedRecordsCount() == 0) {
				status = "Completed";
			} else if (dataloaderInput.getSuccessRecordsCount() == 0) {
				status = "Failed";
			}

			dataloaderInput.setStatus(status);
			dataloaderInput.setTotalQueueWaitingTime(totalQueueWaitingTime);
			executorService.shutdown();

			Util.close(uriWriter, fileReader, reltioFileWriter);

			tokenGeneratorService.stopBackgroundTokenGenerator();
			try {
				processTrackerService.sendProcessTrackerUpdate(true);
			} catch (GenericException | ReltioAPICallFailureException e) {

			 
				logger.error(
						"Dataload process completed.... But final update of process tracket not sent to tenant...",e.getMessage());
			}
		} finally {
			SimpleRestAPIServiceImpl.shutdownRequestsLogger();
		}

	}

	/**
	 * This will write uris created in tenant with the cross walks info passed
	 */
	public static void writeUris(ReltioCSVFileWriter writer, String apiRequest, String apiResponse) {

		EntityRequest[] entityRequest = GSON.fromJson(apiRequest, EntityRequest[].class);
		EntityResponse[] entityResponse = GSON.fromJson(apiResponse, EntityResponse[].class);
		
		List<String[]> writeLines = new ArrayList<>();

		for (int i = 0; i < entityRequest.length; i++) {

			EntityRequest request = entityRequest[i];
			List<Crosswalk> requestCrosswalks = request.getCrosswalks();
			EntityResponse response = entityResponse[i];

			if (i != response.getIndex()) {
				logger.error("index did not match " + i + ", = " + response.getIndex());
				continue;
			}

			List<String> data = new ArrayList<>();

			data.add(response.getUri());

			for (Crosswalk xwalk : requestCrosswalks) {

				data.add(xwalk.getType());
				data.add(xwalk.getValue());
				data.add(xwalk.getSourceTable());
				data.add(String.valueOf(xwalk.isDataProvider() ? "true" : ""));
			}

			writeLines.add(data.stream().toArray(String[]::new));
		}

		writer.writeToFile(writeLines);
	}
}
