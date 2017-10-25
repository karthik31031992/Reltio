package com.reltio.cst.dataload.impl.helper;

import static com.reltio.cst.dataload.DataloadConstants.GSON;
import static com.reltio.cst.dataload.util.DataloadFunctions.getAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.reflect.TypeToken;
import com.reltio.cst.dataload.DataloadConstants;
import com.reltio.cst.dataload.domain.DataloaderInput;
import com.reltio.cst.dataload.domain.ReltioDataloadErrors;
import com.reltio.cst.dataload.domain.ReltioDataloadResponse;
import com.reltio.cst.dataload.impl.LoadJsonToTenant;
import com.reltio.cst.dataload.processtracker.domain.ErrorDetails;
import com.reltio.cst.dataload.processtracker.domain.ErrorDetailsAttributes;
import com.reltio.cst.dataload.processtracker.domain.FailedRecords;
import com.reltio.cst.dataload.processtracker.domain.FailedRecordsAttributes;
import com.reltio.cst.dataload.processtracker.domain.Metrics;
import com.reltio.cst.dataload.processtracker.domain.MetricsAttributes;
import com.reltio.cst.dataload.processtracker.domain.ProcessTrackerAttributes;
import com.reltio.cst.dataload.processtracker.domain.ProcessTrackerObject;
import com.reltio.cst.dataload.util.mail.SendMail;
import com.reltio.cst.exception.handler.GenericException;
import com.reltio.cst.exception.handler.ReltioAPICallFailureException;
import com.reltio.cst.service.ReltioAPIService;
import com.reltio.file.ReltioCSVFileWriter;
import com.reltio.file.ReltioFileWriter;

public class ProcessTrackerService {

	private ProcessTrackerObject processTrackerObject;
	private Metrics metrics;
	private DataloaderInput dataloaderInput;
	private ReltioAPIService reltioAPIService;
	private String pcURI;
	private SendMail sendMail;

	public ProcessTrackerService(DataloaderInput dataloaderInput,
			ReltioAPIService reltioAPIService) throws GenericException,
			ReltioAPICallFailureException {

		this.dataloaderInput = dataloaderInput;
		this.processTrackerObject = new ProcessTrackerObject();
		this.reltioAPIService = reltioAPIService;
		this.sendMail = new SendMail();
		if (dataloaderInput.isProcessTrackerCreated()) {
			createInitialProcessTracker();
		}
	}

	private void createInitialProcessTracker() throws GenericException,
			ReltioAPICallFailureException {

		if (dataloaderInput.isProcessTrackerCreated()) {
			processTrackerObject.attributes = new ProcessTrackerAttributes();
			processTrackerObject.attributes.Type = getAttribute(
					processTrackerObject.attributes.Type, "Dataload");
			processTrackerObject.attributes.StartTime = getAttribute(
					processTrackerObject.attributes.StartTime,
					LoadJsonToTenant.sdf.format(dataloaderInput
							.getProgramStartTime()));
			processTrackerObject.attributes.DataloadType = getAttribute(
					processTrackerObject.attributes.DataloadType,
					dataloaderInput.getDataloadType());
			processTrackerObject.attributes.TypeOfData = getAttribute(
					processTrackerObject.attributes.TypeOfData,
					dataloaderInput.getDataType());
			processTrackerObject.attributes.Status = getAttribute(
					processTrackerObject.attributes.Status,
					dataloaderInput.getStatus());
			processTrackerObject.attributes.InitiatedBy = getAttribute(
					processTrackerObject.attributes.InitiatedBy,
					dataloaderInput.getUsername());
			processTrackerObject.attributes.APIServerUsed = getAttribute(
					processTrackerObject.attributes.APIServerUsed,
					dataloaderInput.getServerHostName());
			processTrackerObject.attributes.UpdateTime = getAttribute(
					processTrackerObject.attributes.UpdateTime,
					dataloaderInput.getLastUpdateTime());
			processTrackerObject.attributes.EndTime = getAttribute(
					processTrackerObject.attributes.EndTime,
					dataloaderInput.getProgramEndTime());
			processTrackerObject.attributes.UserComment = getAttribute(
					processTrackerObject.attributes.UserComment,
					dataloaderInput.getUserComments());
			processTrackerObject.attributes.Metrics = new ArrayList<Metrics>();
			metrics = new Metrics();
			MetricsAttributes metricsAttributes = new MetricsAttributes();

			metricsAttributes.NumberofThreads = getAttribute(
					metricsAttributes.NumberofThreads,
					dataloaderInput.getThreadCount());
			metricsAttributes.RecordsPerPost = getAttribute(
					metricsAttributes.RecordsPerPost,
					dataloaderInput.getGroupsCount());
			metricsAttributes.QueueSize = getAttribute(
					metricsAttributes.QueueSize,
					dataloaderInput.getQueueThreshold());

			metrics.value = metricsAttributes;
			processTrackerObject.attributes.Metrics.add(metrics);

			String responseEntity = reltioAPIService.post(
					dataloaderInput.getBaseDataloadURL()
							+ "/entities?returnUriOnly=true",
					"[" + DataloadConstants.GSON.toJson(processTrackerObject)
							+ "]");
			extractPCURI(responseEntity);
		}
	}

	public void sendProcessTrackerUpdate() throws GenericException,
			ReltioAPICallFailureException {
		if (dataloaderInput.isProcessTrackerCreated()) {
			updateAttribute();

			processTrackerObject.attributes.Log = getAttribute(
					processTrackerObject.attributes.Log,
					dataloaderInput.getLog());

			processTrackerObject.attributes.Status = getAttribute(
					processTrackerObject.attributes.Status,
					dataloaderInput.getStatus());

			processTrackerObject.attributes.UpdateTime = getAttribute(
					processTrackerObject.attributes.UpdateTime,
					dataloaderInput.getLastUpdateTime());

			processTrackerObject.attributes.EndTime = getAttribute(
					processTrackerObject.attributes.EndTime,
					dataloaderInput.getProgramEndTime());

			List<FailedRecords> failedRecords = new ArrayList<FailedRecords>();
			List<ErrorDetails> errorDetailsList = new ArrayList<ErrorDetails>();
			Set<String> uniqueErrorMessage = new HashSet<>();

			if (dataloaderInput.getDataloadErrorsMap() != null
					&& !dataloaderInput.getDataloadErrorsMap().isEmpty()) {
				for (Entry<Integer, List<ReltioDataloadErrors>> failedRecErrors : dataloaderInput
						.getDataloadErrorsMap().entrySet()) {
					uniqueErrorMessage.clear();
					ErrorDetails errorDetails = new ErrorDetails();
					ErrorDetailsAttributes errDetailAttributes = new ErrorDetailsAttributes();
					errDetailAttributes.ErrorCode = getAttribute(
							errDetailAttributes.ErrorCode,
							failedRecErrors.getKey());
					errDetailAttributes.RepeatCount = getAttribute(
							errDetailAttributes.RepeatCount, failedRecErrors
									.getValue().size());
					int count = 0;

					for (ReltioDataloadErrors reltioDataloadErrors : failedRecErrors
							.getValue()) {
						count++;
						FailedRecords failedRec = new FailedRecords();
						FailedRecordsAttributes failedRecAttr = new FailedRecordsAttributes();
						failedRecAttr.Crosswalk = getAttribute(
								failedRecAttr.Crosswalk,
								reltioDataloadErrors.getCrosswalkValue());
						failedRecAttr.SourceSystem = getAttribute(
								failedRecAttr.SourceSystem,
								reltioDataloadErrors.getCrosswalkType());

						failedRecAttr.ErrorCode = getAttribute(
								failedRecAttr.ErrorCode,
								reltioDataloadErrors.getErrorCode());
						failedRecAttr.ErrorMessage = getAttribute(
								failedRecAttr.ErrorMessage,
								reltioDataloadErrors.getErrorMessage());

						failedRecAttr.ErrorDetailMessage = getAttribute(
								failedRecAttr.ErrorDetailMessage,
								reltioDataloadErrors.getErrorDetailMessage());
						if (uniqueErrorMessage.add(reltioDataloadErrors
								.getErrorMessage())) {
							errDetailAttributes.ErrorDescription = getAttribute(
									errDetailAttributes.ErrorDescription,
									reltioDataloadErrors.getErrorMessage());
						}
						failedRec.value = failedRecAttr;
						failedRecords.add(failedRec);
						// Allow only limited crosswalk details @ tenant level
						if (count >= DataloadConstants.MAX_FAILURE_PER_ERROR_CODE) {
							break;
						}
					}

					errorDetails.value = errDetailAttributes;
					errorDetailsList.add(errorDetails);

				}
			}

			processTrackerObject.attributes.FailedRecords = failedRecords;

			processTrackerObject.attributes.ErrorDetails = errorDetailsList;

			metrics.value.FailedRecordsCount = getAttribute(
					metrics.value.FailedRecordsCount,
					dataloaderInput.getFailedRecordsCount());
			metrics.value.SuccessRecordsCount = getAttribute(
					metrics.value.SuccessRecordsCount,
					dataloaderInput.getSuccessRecordsCount());
			metrics.value.TotalRecordsCount = getAttribute(
					metrics.value.TotalRecordsCount,
					dataloaderInput.getTotalRecordsCount());
			metrics.value.TotalTimeTaken = getAttribute(
					metrics.value.TotalTimeTaken,
					dataloaderInput.getTotalTimeTaken() / 1000);
			if (dataloaderInput.getTotalQueueWaitingTime() != null) {
				metrics.value.TotalQueueWaitingTime = getAttribute(
						metrics.value.TotalQueueWaitingTime,
						dataloaderInput.getTotalQueueWaitingTime() / 1000);

				metrics.value.TotalOPS = getAttribute(metrics.value.TotalOPS,
						dataloaderInput.getTotalOPS());

				metrics.value.TotalOPSWithoutQueue = getAttribute(
						metrics.value.TotalOPSWithoutQueue,
						dataloaderInput.getTotalOPSWithoutQueue());
			}

			String responseEntity = reltioAPIService.post(
					dataloaderInput.getBaseDataloadURL()
							+ "/entities?returnUriOnly=true",
					"[" + DataloadConstants.GSON.toJson(processTrackerObject)
							+ "]");
			if (pcURI == null) {

				extractPCURI(responseEntity);
			}
		}
	}

	private void updateAttribute() {

		processTrackerObject.attributes.Status = null;
		processTrackerObject.attributes.Log = null;

		processTrackerObject.attributes.UpdateTime = null;

		processTrackerObject.attributes.EndTime = null;

		metrics.value.FailedRecordsCount = null;
		metrics.value.SuccessRecordsCount = null;
		metrics.value.TotalRecordsCount = null;
		metrics.value.TotalTimeTaken = null;
	}

	public void sendProcessTrackerUpdate(boolean isLastUpdate)
			throws GenericException, ReltioAPICallFailureException, IOException {

		if (isLastUpdate) {
			if (!dataloaderInput.getDataloadErrorsMap().isEmpty()) {
				String failedLogFileName = dataloaderInput
						.getFailedRecordsFileName() + "_failurelog.csv";
				ReltioFileWriter failureLog = new ReltioCSVFileWriter(failedLogFileName);
				createFailureLogFile(failureLog, dataloaderInput);
				dataloaderInput.setFailedLogFileName(failedLogFileName);
			}

			sendMail.send(dataloaderInput);
		}
		sendProcessTrackerUpdate();
	}

	private void extractPCURI(String responseEntity) {
		List<ReltioDataloadResponse> recordsInLine = GSON.fromJson(
				responseEntity, new TypeToken<List<ReltioDataloadResponse>>() {
				}.getType());
		ReltioDataloadResponse dataloadResponse = recordsInLine.get(0);
		System.out.println(dataloadResponse.getUri());
		pcURI = dataloadResponse.getUri().replaceAll("entities/", "");

		processTrackerObject.crosswalks = getAttribute(
				processTrackerObject.crosswalks, pcURI,
				DataloadConstants.PC_SOURCE_SYSTEM, null, null);

	}

	private void createFailureLogFile(ReltioFileWriter failureLog,
			DataloaderInput dataloaderInput) throws IOException {
		failureLog.writeToFile(DataloadConstants.FAILED_LOG_FILE_HEADER);
		List<String[]> failedRecords = new ArrayList<>();
		String[] line;
		for (Entry<Integer, List<ReltioDataloadErrors>> failedRecErrors : dataloaderInput
				.getDataloadErrorsMap().entrySet()) {

			int count = 0;

			for (ReltioDataloadErrors reltioDataloadErrors : failedRecErrors
					.getValue()) {
				count++;
				line = new String[5];
				line[0] = reltioDataloadErrors.getCrosswalkType();
				line[1] = reltioDataloadErrors.getCrosswalkValue();
				if (reltioDataloadErrors.getErrorCode() != null)
					line[2] = reltioDataloadErrors.getErrorCode() + "";
				line[3] = reltioDataloadErrors.getErrorMessage();
				line[4] = reltioDataloadErrors.getErrorDetailMessage();
				failedRecords.add(line);
				if (count % 100 == 0) {
					failureLog.writeToFile(failedRecords);
					failedRecords.clear();
				}

			}

		}

		failureLog.writeToFile(failedRecords);
		failureLog.close();

	}
}
