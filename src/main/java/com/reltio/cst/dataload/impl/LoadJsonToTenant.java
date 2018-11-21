package com.reltio.cst.dataload.impl;

import com.google.gson.reflect.TypeToken;
import com.reltio.cst.dataload.DataloadConstants;
import com.reltio.cst.dataload.domain.DataloaderInput;
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
import com.reltio.file.ReltioFileReader;
import com.reltio.file.ReltioFileWriter;
import com.reltio.file.ReltioFlatFileReader;
import com.reltio.file.ReltioFlatFileWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static com.reltio.cst.dataload.DataloadConstants.DEFAULT_ERROR_CODE;
import static com.reltio.cst.dataload.DataloadConstants.GSON;
import static com.reltio.cst.dataload.DataloadConstants.JSON_FILE_TYPE_ARRAY;
import static com.reltio.cst.dataload.DataloadConstants.JSON_FILE_TYPE_PIPE;
import static com.reltio.cst.dataload.DataloadConstants.MAX_FAILURE_COUNT;
import static com.reltio.cst.dataload.util.DataloadFunctions.checkNull;
import static com.reltio.cst.dataload.util.DataloadFunctions.printDataloadPerformance;
import static com.reltio.cst.dataload.util.DataloadFunctions.sendHcps;
import static com.reltio.cst.dataload.util.DataloadFunctions.waitForQueue;
import static com.reltio.cst.dataload.util.DataloadFunctions.waitForTasksReady;

public class LoadJsonToTenant {

    public static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private static final Logger logger = LogManager.getLogger(LoadJsonToTenant.class.getName());

    public static void main(String[] args) throws Exception {

        final long[] pStartTime = new long[1];
        final boolean[] flag = new boolean[1];

        flag[0] = true;

        Properties properties = new Properties();

        try {
            String propertyFilePath = args[0];
            try (FileReader fileReader = new FileReader(propertyFilePath)) {
                properties.load(fileReader);
            }
        } catch (Exception e) {
            logger.error("Failed to Read the Properties File :: ");
            //System.out.println("Failed to Read the Properties File :: ");
            //e.printStackTrace();
            logger.debug(e);
            logger.error(e.getMessage());
        }

        // Read Input data From the Properties File
        final DataloaderInput dataloaderInput = new DataloaderInput(properties);

        if (args != null && args.length > 1) {
            dataloaderInput.setFileName(args[1]);
            dataloaderInput.setFailedRecordsFileName(args[2]);
            dataloaderInput.setDataloadType(args[3]);
            dataloaderInput.setDataType(args[4]);
        }

        // Validate the Input data provided
        if (!checkNull(dataloaderInput.getFileName())
                || !checkNull(dataloaderInput.getBaseDataloadURL())
                || !checkNull(dataloaderInput.getDataloadType())
                || !checkNull(dataloaderInput.getAuthURL())
                || !checkNull(dataloaderInput.getPassword())
                || !checkNull(dataloaderInput.getUsername())
                || !checkNull(dataloaderInput.getServerHostName())
                || !checkNull(dataloaderInput.getTenantId())
                || !checkNull(dataloaderInput.getFailedRecordsFileName())
                || !checkNull(String.valueOf(dataloaderInput.getSendMailFileName()))
                || !checkNull(String.valueOf(dataloaderInput.getSendMailFlag()))) {
//            System.out
//                    .println("One or more required Job configuration properties are missing... Please Verify and update the Job configuration file...");
//            System.out
//                    .println("Process Aborted due to insuficient input properties...");

            logger.error("One or more required Job configuration properties are missing... Please Verify and update the Job configuration file...");
            logger.error("Process Aborted due to insufficient input properties...");
            System.exit(-1);
        }
        final int MAX_QUEUE_SIZE_MULTIPLICATOR = 10;

        // Validate the Input Data Provided
        if (!dataloaderInput.getDataloadType().equalsIgnoreCase("Entities")
                && !dataloaderInput.getDataloadType().equalsIgnoreCase(
                "Relations")
                && !dataloaderInput.getDataloadType().equalsIgnoreCase(
                "Interactions")
                && !dataloaderInput.getDataloadType()
                .equalsIgnoreCase("Groups")) {
//            System.out
//                    .println("Invalid DATALOAD_TYPE provided. It Should be (Entities/Relations/Interactions/Groups) .... ");

            logger.error("Invalid DATALOAD_TYPE provided. It Should be (Entities/Relations/Interactions/Groups)");
            System.exit(-1);
        }


        SimpleRestAPIServiceImpl.setupRequestsLogger(dataloaderInput.getRequestsLogFilePath());
        try {
            final ReltioFileWriter reltioFileWriter = new ReltioFlatFileWriter(
                    dataloaderInput.getFailedRecordsFileName());

            StringBuilder apiUriBuilder = new StringBuilder()
                    .append(dataloaderInput.getBaseDataloadURL())
                    .append('/')
                    .append(dataloaderInput.getDataloadType().toLowerCase())
                    .append('?');
            apiUriBuilder.append("maxObjectsToUpdate=" + dataloaderInput.getMaxObjectsToUpdate());
            if (!dataloaderInput.getReturnFullBody()) {
                apiUriBuilder.append("&returnUriOnly=true");
            }


            if (dataloaderInput.getIsPartialOverride() && dataloaderInput.getIsUpdateAttributeUpdateDates()) {
                apiUriBuilder.append("&options=partialOverride,updateAttributeUpdateDates");
            } else if (dataloaderInput.getIsPartialOverride() && !dataloaderInput.getIsUpdateAttributeUpdateDates()) {
                apiUriBuilder.append("&options=partialOverride");
            } else if (!dataloaderInput.getIsPartialOverride() && dataloaderInput.getIsUpdateAttributeUpdateDates()) {
                apiUriBuilder.append("&options=updateAttributeUpdateDates");
            }
            //to stop LCA execution
            if (!dataloaderInput.getIsExecuteLCA()) {
                apiUriBuilder.append("&executeLCA=false");
            }
            //to initiate DCR
            /*if (dataloaderInput.getIsAlwaysCreateDCR()) {
                apiUriBuilder.append("&alwaysCreateDCR=true");
			}*/


            final String apiUrl = apiUriBuilder.toString();

            ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors
                    .newFixedThreadPool(dataloaderInput.getThreadCount());

            ReltioFileReader fileReader;
            if (dataloaderInput.getJsonFileType().equalsIgnoreCase(
                    JSON_FILE_TYPE_PIPE)) {
                fileReader = new ReltioFlatFileReader(
                        dataloaderInput.getFileName(), "|", StandardCharsets.UTF_8.name());
            } else {
                fileReader = new ReltioFlatFileReader(dataloaderInput.getFileName(), null, StandardCharsets.UTF_8.name());
            }

            // Create Token Generator Service
            TokenGeneratorService tokenGeneratorService = null;
            try {
                tokenGeneratorService = new TokenGeneratorServiceImpl(
                        dataloaderInput.getUsername(),
                        dataloaderInput.getPassword(), dataloaderInput.getAuthURL());
            } catch (APICallFailureException | GenericException e2) {
                logger.debug(e2);
                logger.error("Token Generation Process Failed. Please verify username/password and restart the process again...");
                System.exit(-1);
            }
            tokenGeneratorService.startBackgroundTokenGenerator();

            logger.info("DataLoad started for Tenant :: " + apiUrl);
            //System.out.println("DataLoad started for Tenant :: " + apiUrl);

            final ReltioAPIService reltioAPIService = new SimpleReltioAPIServiceImpl(
                    tokenGeneratorService, dataloaderInput.getTimeoutInMinutes());
            final ProcessTrackerService processTrackerService = new ProcessTrackerService(
                    dataloaderInput, reltioAPIService);
            int count = 0;

            pStartTime[0] = dataloaderInput.getProgramStartTime();

            long totalQueueWaitingTime = 0L;
            long totalFuturesExecutionTime = 0L;

            boolean eof = false;
            ArrayList<Future<Long>> futures = new ArrayList<>();
            while (!eof) {

                try {
                    totalQueueWaitingTime += waitForQueue(apiUrl,
                            dataloaderInput.getQueueThreshold(), executorService, reltioAPIService, dataloaderInput.getTenantId());
                } catch (GenericException | InterruptedException e2) {
                    logger.debug(e2);
                    dataloaderInput.setLastUpdateTime(sdf.format(System
                            .currentTimeMillis()));
                    dataloaderInput.setStatus("Aborted");
                    dataloaderInput.setLog(dataloaderInput.getLog()
                            + System.lineSeparator() + " Failed to get the Queue Size...");
                    try {
                        processTrackerService.sendProcessTrackerUpdate(true);
                    } catch (GenericException | ReltioAPICallFailureException e) {
                        logger.error("Aborting process to failure on getting the Queue details.. Also recent update not sent to Process tracker....");
                        logger.error(e.getMessage());
                        logger.debug(e);
                    }
                    System.exit(-1);
                }

                List<Object> inputRecords = new ArrayList<>();
                boolean isArray;
                int jsonIndex = 0;
                if (dataloaderInput.getJsonFileType().equalsIgnoreCase(
                        JSON_FILE_TYPE_PIPE)) {
                    jsonIndex = 1;
                    isArray = true;
                } else isArray = dataloaderInput.getJsonFileType().equalsIgnoreCase(
                        JSON_FILE_TYPE_ARRAY);
                // Initially we would put 10 times more requests than threads number
                // in executor service. It will get us
                // non-empty queue on waiting for tasks steps. When we wait for
                // tasks, we need to be sure that we not waste
                // time when part of tasks are done and part are still pending...
                for (int threadNum = futures.size(); threadNum < dataloaderInput
                        .getThreadCount() * MAX_QUEUE_SIZE_MULTIPLICATOR; threadNum++) {
                    inputRecords.clear();
                    for (int k = 0; k < dataloaderInput.getGroupsCount(); k++) {
                        String[] nextHcp = null;

                        try {
                            nextHcp = fileReader.readLine();
                        } catch (Exception e) {
                            logger.error(e.getMessage());
                            logger.debug(e);
                            nextHcp = fileReader.readLine();
                        }
                        if (nextHcp == null) {
                            eof = true;
                            break;
                        }

                        if (nextHcp.length == (jsonIndex + 1)) {
                            if (isArray) {
                                try {
                                    List<Object> recordsInLine = GSON.fromJson(
                                            nextHcp[jsonIndex],
                                            new TypeToken<List<Object>>() {
                                            }.getType());
                                    inputRecords.addAll(recordsInLine);
                                    count = count + recordsInLine.size();
                                } catch (Exception e) {
                                    count = count + 1;
                                    DataloadFunctions.invalidJSonError(
                                            "Invalid JSON|" + nextHcp[jsonIndex],
                                            dataloaderInput, reltioFileWriter);
                                }
                            } else {
                                try {

                                    Object recordInLine = GSON.fromJson(
                                            nextHcp[jsonIndex], Object.class);
                                    inputRecords.add(recordInLine);
                                    count = count + 1;

                                } catch (Exception e) {
                                    count = count + 1;
                                    DataloadFunctions.invalidJSonError(
                                            "Invalid JSON|" + nextHcp[jsonIndex],
                                            dataloaderInput, reltioFileWriter);
                                }

                            }
                        } else {

                            String line = nextHcp[jsonIndex];
                            for (int i = jsonIndex + 1; i < nextHcp.length; i++) {
                                line += "|" + nextHcp[i];
                            }
                            try {
                                List<Object> recordsInLine = GSON.fromJson(line,
                                        new TypeToken<List<Object>>() {
                                        }.getType());
                                inputRecords.addAll(recordsInLine);
                                count = count + recordsInLine.size();
                            } catch (Exception e) {
                                count = count + 1;
                                DataloadFunctions.invalidJSonError("Invalid JSON|"
                                                + nextHcp[jsonIndex], dataloaderInput,
                                        reltioFileWriter);
                            }

                            // String line = "";
                            //
                            // for (String val : nextHcp) {
                            // if (!line.isEmpty()) {
                            // line += "|";
                            // }
                            // line += val;
                            // }
                            // DataloadFunctions.invalidJSonError(
                            // "Invalid JSON Formatted Line:" + line,
                            // dataloaderInput, reltioFileWriter);
                            // count = count + 1;
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

                                    String result = sendHcps(apiUrl,
                                            GSON.toJson(totalRecordsSent),
                                            reltioAPIService);

                                    List<ReltioDataloadResponse> dataloadResponses = GSON
                                            .fromJson(
                                                    result,
                                                    new TypeToken<List<ReltioDataloadResponse>>() {
                                                    }.getType());
                                    List<Object> failedRecords = new ArrayList<Object>();

                                    int sucCount = 0;
                                    int failCcount = 0;
                                    for (ReltioDataloadResponse reltioDataloadResponse : dataloadResponses) {
                                        if (reltioDataloadResponse.getSuccessful()) {
                                            sucCount++;
                                        } else {
                                            failCcount++;

                                            Object failedRec = totalRecordsSent
                                                    .get(reltioDataloadResponse
                                                            .getIndex());
                                            failedRecords.add(failedRec);
                                            ReltioDataloadErrors dataloadErrors = reltioDataloadResponse
                                                    .getErrors();
                                            ReltioCrosswalkObject crosswalkObject = GSON.fromJson(
                                                    GSON.toJson(failedRec),
                                                    ReltioCrosswalkObject.class);

                                            if (crosswalkObject != null
                                                    && crosswalkObject
                                                    .getCrosswalks() != null
                                                    && !crosswalkObject
                                                    .getCrosswalks()
                                                    .isEmpty()) {
                                                dataloadErrors
                                                        .setCrosswalkType(crosswalkObject
                                                                .getCrosswalks()
                                                                .get(0).getType());
                                                dataloadErrors
                                                        .setCrosswalkValue(crosswalkObject
                                                                .getCrosswalks()
                                                                .get(0).getValue()
                                                                + "");
                                            }
                                            List<ReltioDataloadErrors> reltioDataloadErrors = dataloaderInput
                                                    .getDataloadErrorsMap()
                                                    .get(dataloadErrors
                                                            .getErrorCode());
                                            if (reltioDataloadErrors == null) {
                                                reltioDataloadErrors = new ArrayList<>();
                                            }
                                            reltioDataloadErrors
                                                    .add(dataloadErrors);
                                            dataloaderInput.getDataloadErrorsMap()
                                                    .put(dataloadErrors
                                                                    .getErrorCode(),
                                                            reltioDataloadErrors);
                                        }
                                    }

                                    dataloaderInput.addFailureCount(failCcount);
                                    dataloaderInput.addSuccessCount(sucCount);

                                    if (failCcount > 0) {
                                        logger.info("Success entities="
                                                + sucCount
                                                + "|Total Sent entities="
                                                + currentCount
                                                + "| Failed Entity Count="
                                                + failCcount + "|" + "ERROR:"
                                                + result);
                                        logger.info("Failure Count: "
                                                + failCcount
                                                + "|"
                                                + GSON.toJson(failedRecords));
                                        reltioFileWriter.writeToFile("Failure Count: "
                                                + failCcount
                                                + "|"
                                                + GSON.toJson(failedRecords));

                                        for (Entry<Integer, List<ReltioDataloadErrors>> errorMap : dataloaderInput
                                                .getDataloadErrorsMap().entrySet()) {
                                            if (errorMap.getValue().size() > MAX_FAILURE_COUNT) {
                                                dataloaderInput.setLastUpdateTime(sdf.format(System
                                                        .currentTimeMillis()));
                                                dataloaderInput
                                                        .setStatus("Aborted");
                                                processTrackerService
                                                        .sendProcessTrackerUpdate(true);
                                                logger.error("Killing process as there are lot of failures while loading the data. Please verify the JSON and relaod again. More details can be found in the Process Tracker Enitity on the tenant: ");
                                                System.exit(-1);
                                            }
                                        }

                                    } else {
                                        logger.info("Success entities="
                                                + sucCount
                                                + "|Total Sent entities="
                                                + currentCount);
                                    }

                                    long updateTime = System.currentTimeMillis()
                                            - pStartTime[0];
                                    int minutes = (int) ((updateTime / (1000 * 60)) % 60);

                                    if (minutes == 1) {
                                        if (flag[0]) {
                                            pStartTime[0] = System
                                                    .currentTimeMillis();
                                            flag[0] = false;

                                            dataloaderInput.setLastUpdateTime(sdf
                                                    .format(System
                                                            .currentTimeMillis()));
                                            processTrackerService
                                                    .sendProcessTrackerUpdate();
                                        }
                                    } else {
                                        flag[0] = true;
                                    }
                                } catch (GenericException e) {
                                    List<ReltioCrosswalkObject> crosswalkObjects = GSON
                                            .fromJson(
                                                    stringToSend,
                                                    new TypeToken<List<ReltioCrosswalkObject>>() {
                                                    }.getType());
                                    dataloaderInput
                                            .addFailureCount(crosswalkObjects
                                                    .size());
                                    List<ReltioDataloadErrors> dataloadErrors = dataloaderInput
                                            .getDataloadErrorsMap().get(
                                                    DEFAULT_ERROR_CODE);
                                    ReltioDataloadErrors reltioDataloadError = new ReltioDataloadErrors();

                                    if (dataloadErrors == null) {
                                        dataloadErrors = new ArrayList<>();
                                    }
                                    for (ReltioCrosswalkObject crosswalkObject : crosswalkObjects) {
                                        reltioDataloadError = new ReltioDataloadErrors();
                                        reltioDataloadError
                                                .setErrorCode(DEFAULT_ERROR_CODE);
                                        reltioDataloadError.setErrorMessage(e
                                                .getExceptionMessage());
                                        reltioDataloadError
                                                .setCrosswalkType(crosswalkObject
                                                        .getCrosswalks().get(0)
                                                        .getType());
                                        reltioDataloadError
                                                .setCrosswalkValue((String) crosswalkObject
                                                        .getCrosswalks().get(0)
                                                        .getValue());
                                        dataloadErrors.add(reltioDataloadError);
                                    }
                                    dataloaderInput.getDataloadErrorsMap().put(
                                            DEFAULT_ERROR_CODE, dataloadErrors);
                                    for (Entry<Integer, List<ReltioDataloadErrors>> errorMap : dataloaderInput
                                            .getDataloadErrorsMap().entrySet()) {
                                        if (errorMap.getValue().size() > MAX_FAILURE_COUNT) {
                                            dataloaderInput.setLastUpdateTime(sdf
                                                    .format(System
                                                            .currentTimeMillis()));
                                            dataloaderInput.setStatus("Aborted");
                                            try {
                                                processTrackerService
                                                        .sendProcessTrackerUpdate(true);
                                            } catch (GenericException e1) {
                                                logger.debug(e1);
                                                logger.error(e1.getExceptionMessage());
                                            } catch (ReltioAPICallFailureException e1) {
                                                logger.debug(e1);
                                                logger.error(e1.getErrorResponse());
                                            } catch (IOException e1) {
                                                logger.debug(e1);
                                                logger.error(e1.getMessage());
                                            }
                                            logger.error("Killing process as there are lot of failures while loading the data. Please verify the JSON and relaod again. More details can be found in the Process Tracker Enitity on the tenant: ");
                                            System.exit(-1);
                                        }
                                    }

                                    try {
                                        reltioFileWriter
                                                .writeToFile(DataloadConstants.FAILURE_LOG_KEY
                                                        + stringToSend);
                                    } catch (IOException e1) {
                                        logger.error(e1.getMessage());
                                        logger.debug(e);
                                    }

                                } catch (ReltioAPICallFailureException e) {

                                    List<ReltioCrosswalkObject> crosswalkObjects = GSON
                                            .fromJson(
                                                    stringToSend,
                                                    new TypeToken<List<ReltioCrosswalkObject>>() {
                                                    }.getType());
                                    dataloaderInput
                                            .addFailureCount(crosswalkObjects
                                                    .size());

                                    List<ReltioDataloadErrors> dataloadErrors = dataloaderInput
                                            .getDataloadErrorsMap().get(
                                                    e.getErrorCode());
                                    ReltioDataloadErrors reltioDataloadError = new ReltioDataloadErrors();

                                    if (dataloadErrors == null) {
                                        dataloadErrors = new ArrayList<>();
                                    }
                                    for (ReltioCrosswalkObject crosswalkObject : crosswalkObjects) {
                                        reltioDataloadError.setErrorCode(e
                                                .getErrorCode());
                                        reltioDataloadError.setErrorMessage(e
                                                .getErrorResponse());
                                        reltioDataloadError
                                                .setCrosswalkType(crosswalkObject
                                                        .getCrosswalks().get(0)
                                                        .getType());
                                        reltioDataloadError
                                                .setCrosswalkValue((String) crosswalkObject
                                                        .getCrosswalks().get(0)
                                                        .getValue());
                                        dataloadErrors.add(reltioDataloadError);
                                    }
                                    dataloaderInput.getDataloadErrorsMap().put(
                                            e.getErrorCode(), dataloadErrors);
                                    for (Entry<Integer, List<ReltioDataloadErrors>> errorMap : dataloaderInput
                                            .getDataloadErrorsMap().entrySet()) {
                                        if (errorMap.getValue().size() > MAX_FAILURE_COUNT) {
                                            dataloaderInput.setLastUpdateTime(sdf
                                                    .format(System
                                                            .currentTimeMillis()));
                                            dataloaderInput.setStatus("Aborted");
                                            try {
                                                processTrackerService
                                                        .sendProcessTrackerUpdate(true);
                                            } catch (GenericException e1) {
                                                logger.error(e1.getExceptionMessage());
                                                logger.debug(e1);
                                            } catch (ReltioAPICallFailureException e1) {
                                                logger.error(e1.getErrorCode() + " " + e1.getErrorResponse());
                                                logger.debug(e1);
                                            } catch (IOException e1) {
                                                logger.error(e1.getMessage());
                                                logger.debug(e1);
                                            }
                                            logger.error("Killing process as there are lot of failures while loading the data. Please verify the JSON and relaod again. More details can be found in the Process Tracker Enitity on the tenant: ");
                                            System.exit(-1);
                                        }
                                    }

                                    try {
                                        reltioFileWriter
                                                .writeToFile(DataloadConstants.FAILURE_LOG_KEY
                                                        + stringToSend);
                                    } catch (IOException e1) {
                                        logger.debug(e1);
                                        logger.error(e1.getMessage());
                                    }

                                } catch (IOException e) {

                                    logger.debug(e);
                                    logger.error(e.getMessage());
                                }
                                requestExecutionTime = System.currentTimeMillis()
                                        - startTime; // one
                                // request
                                // execution
                                // time....
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
                        dataloaderInput.getThreadCount()
                                * (MAX_QUEUE_SIZE_MULTIPLICATOR / 2));

                printDataloadPerformance(executorService.getCompletedTaskCount()
                                * dataloaderInput.getGroupsCount(),
                        totalFuturesExecutionTime, totalQueueWaitingTime,
                        dataloaderInput.getProgramStartTime(),
                        dataloaderInput.getThreadCount());
            }
            totalFuturesExecutionTime += waitForTasksReady(futures, 0);
            dataloaderInput.setTotalRecordsCount(dataloaderInput
                    .getSuccessRecordsCount()
                    + dataloaderInput.getFailedRecordsCount());

            // dataloaderInput.setStatus("Waiting for Queue");
            //
            // printDataloadPerformance(dataloaderInput.getTotalRecordsCount(),
            // totalFuturesExecutionTime, totalQueueWaitingTime,
            // dataloaderInput.getProgramStartTime(),
            // dataloaderInput.getThreadCount());
            // try {
            // processTrackerService.sendProcessTrackerUpdate();
            // } catch (GenericException | ReltioAPICallFailureException e) {
            // e.printStackTrace();
            // }
            //
            // System.out
            // .println("All data send to API. Program will wait for empty API queue and finish, you can safely break program execution at any time by just killing the process...");
            //
            // try {
            // totalFuturesExecutionTime += waitForQueue(apiUrl, 0,
            // executorService);
            // } catch (GenericException | InterruptedException e) {
            // e.printStackTrace();
            // dataloaderInput
            // .setLog(dataloaderInput.getLog()
            // +
            // "\n All the data sent to server. But could not able to check the queue in the end...");
            // }

            long endTime = System.currentTimeMillis();

            long finalTime = endTime - dataloaderInput.getProgramStartTime();
            dataloaderInput.setProgramEndTime(sdf.format(endTime));
            dataloaderInput.setLastUpdateTime(sdf.format(endTime));
            dataloaderInput.setTotalTimeTaken(finalTime);
            logger.info("All data send to API. Program will not wait for Queue to get Empty.");

            // System.out.println("Queues are empty. Printing final results");
            printDataloadPerformance(dataloaderInput.getTotalRecordsCount(),
                    totalFuturesExecutionTime, totalQueueWaitingTime,
                    dataloaderInput.getProgramStartTime(),
                    dataloaderInput.getThreadCount());

            String status = "";
            if (dataloaderInput.getFailedRecordsCount() > 0
                    && dataloaderInput.getSuccessRecordsCount() > 0) {
                status = "Completed with some failed records";
            } else if (dataloaderInput.getFailedRecordsCount() == 0) {
                status = "Completed";
            } else if (dataloaderInput.getSuccessRecordsCount() == 0) {
                status = "Failed";
            }

            dataloaderInput.setStatus(status);
            dataloaderInput.setTotalQueueWaitingTime(totalQueueWaitingTime);
            executorService.shutdown();
            fileReader.close();
            tokenGeneratorService.stopBackgroundTokenGenerator();
            reltioFileWriter.close();
            try {
                processTrackerService.sendProcessTrackerUpdate(true);
            } catch (GenericException | ReltioAPICallFailureException e) {
                logger.debug(e);
                logger.error(e.getMessage());
                logger.error("Dataload process completed.... But final update of process tracket not sent to tenant...");
            }
        } finally {
            SimpleRestAPIServiceImpl.shutdownRequestsLogger();
        }

    }

}
