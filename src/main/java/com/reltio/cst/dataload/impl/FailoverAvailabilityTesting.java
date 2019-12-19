package com.reltio.cst.dataload.impl;

import com.google.gson.reflect.TypeToken;
import com.reltio.cst.dataload.DataloadConstants;
import com.reltio.cst.dataload.domain.*;
import com.reltio.cst.dataload.impl.helper.ProcessTrackerService;
import com.reltio.cst.dataload.util.DataloadFunctions;
import com.reltio.cst.exception.handler.GenericException;
import com.reltio.cst.exception.handler.ReltioAPICallFailureException;
import com.reltio.cst.service.ReltioAPIService;
import com.reltio.cst.service.TokenGeneratorService;
import com.reltio.cst.service.impl.SimpleReltioAPIServiceImpl;
import com.reltio.cst.service.impl.TokenGeneratorServiceImpl;
import com.reltio.cst.util.Util;
import com.reltio.file.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static com.reltio.cst.dataload.DataloadConstants.*;
import static com.reltio.cst.dataload.util.DataloadFunctions.*;

public class FailoverAvailabilityTesting {

    public static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private static final Logger logger = LogManager.getLogger(FailoverAvailabilityTesting.class.getName());
    private static final String GET_ENTITY_URI = "https://etalon-tst-01-failover.reltio.com/api/v1/tenants/ms2/entities/HCP/13DRQpXz";
    private static final String POST_ENTITY_URI="https://etalon-tst-01-failover.reltio.com/api/v1/tenants/ms2/request/";
    private static final String POST_ENTITY_REQUEST ="{\"url\": \"https://tst-01.reltio.com/reltio/api/ms2/entities\",\"method\": \"POST\",\"headers\": {},\"payload\": [{\"attributes\": {\"Name\": [{\"value\": \":REPLACE_NAME\"}]},\"type\": \"configuration/entityTypes/HCP\",\"roles\": []}]}";

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            logger.error("Validation failed as no argument passed");
            return;
        }

        Properties properties = new Properties();

        try {
            String propertyFilePath = args[0];
            if (!new File(propertyFilePath).exists()) {
                logger.error("Validation failed as configuration file passed does not exist = " + propertyFilePath);
                return;
            }
            properties.load(new FileReader(propertyFilePath));
        } catch (Exception e) {
            logger.error("Failed to Read the Properties File :: ", e.getMessage());
            return;
        }

        final DataloaderInput dataloaderInput = new DataloaderInput(properties);

        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors
                .newFixedThreadPool(100);

        TokenGeneratorService service = null;
        String authUrl =dataloaderInput.getAuthURL();
        String username = dataloaderInput.getUsername();
        String password = dataloaderInput.getPassword();
        service = new TokenGeneratorServiceImpl(username, password, authUrl);

        final ReltioAPIService reltioAPIService = new SimpleReltioAPIServiceImpl(service);
        List<Future<Long>> futures = new ArrayList<>();

        for (int threadNum = 1; threadNum <= 10000; threadNum++) {

            final String JSON = POST_ENTITY_REQUEST.replace(":REPLACE_NAME", "AutoTestName_AvailabilityTest_"+threadNum);
            futures.add(executorService.submit(new Callable<Long>() {
                @Override
                public Long call() {
                    long requestExecutionTime = 0L;
                    long startTime = System.currentTimeMillis();
                    try {

                        String getRes = reltioAPIService.get(GET_ENTITY_URI);
                        String postRes = reltioAPIService.post(POST_ENTITY_URI, JSON);
                        System.out.println(getRes);
                        System.out.println(postRes);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    requestExecutionTime = System.currentTimeMillis() - startTime; // one

                    return requestExecutionTime;
                }
            }));
        }

        waitForTasksReady(futures, 0);
        executorService.shutdown();

    }

    /**
     * Process response based on api response
     *
     * @param result
     * @param dataloaderInput
     * @param stringToSend
     * @param uriWriter
     * @param totalRecordsSent
     * @param currentCount
     * @param reltioFileWriter
     * @param processTrackerService
     * @throws GenericException
     * @throws ReltioAPICallFailureException
     * @throws IOException
     */
    private static void processResponse(String result, DataloaderInput dataloaderInput, String stringToSend, ReltioCSVFileWriter uriWriter,
                                        List<Object> totalRecordsSent, int currentCount, ReltioFileWriter reltioFileWriter, ProcessTrackerService processTrackerService) throws GenericException, ReltioAPICallFailureException, IOException {

        List<Object> failedRecords = new ArrayList<Object>();
        List<ReltioDataloadResponse> dataloadResponses = new ArrayList<>();
        int sucCount = 0;
        int failCcount = 0;

        if (dataloaderInput.getIsAlwaysCreateDCR()) {
            if (!result.contains("\"uri\":\"changeRequests")) {
                /*
                 * Error creating Chagerequest
                 */
                dataloadResponses = GSON.fromJson(result,
                        new TypeToken<List<ReltioDataloadResponse>>() {
                        }.getType());


                failedRecords.add(dataloadResponses.get(0));
                failCcount++;
                dataloaderInput.addFailureCount(failCcount);

            } else {
                sucCount++;
                dataloaderInput.addSuccessCount(sucCount);

                if (dataloaderInput.getURIrequired()) {
                    writeChangeRequestUris(uriWriter, stringToSend, result);
                }
            }
        } else {
            if (result.contains("\"uri\":\"changeRequests")) {

                Util.close(uriWriter, reltioFileWriter);

                /*
                 * Throw Error because the user has INITIATE_CHANGE_REQUST permission only. So stopping process execution
                 */

                logger.fatal("Error!!!  System is exiting.................");
                logger.fatal("The user does not permission to create/update entities, but has permission to InitiateChangeRequests. ");
                logger.fatal("Please give the user Create/Update permission or use ALWAYS_CREATE_DCR=true option to create changerequests only.");
                System.exit(-1);

            } else {


                dataloadResponses = GSON.fromJson(result,
                        new TypeToken<List<ReltioDataloadResponse>>() {
                        }.getType());

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

            }
        }


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


    }

    /**
     * This will write uris created in tenant with the cross walks info passed
     */
    public static void writeChangeRequestUris(ReltioCSVFileWriter writer, String apiRequest, String apiResponse) {

        EntityRequest[] entityRequest = GSON.fromJson(apiRequest, EntityRequest[].class);
        ReltioDataloadCRResponse crResponse = GSON.fromJson(apiResponse, ReltioDataloadCRResponse.class);

        List<String[]> writeLines = new ArrayList<>();


        EntityRequest request = entityRequest[0];
        List<Crosswalk> requestCrosswalks = request.getCrosswalks();
        ReltioDataloadCRResponse response = crResponse;

        List<String> data = new ArrayList<>();

        if (crResponse.getChanges() != null && crResponse.getChanges().size() > 0) {
            data.add(crResponse.getChanges().entrySet().iterator().next().getKey());
        }

        data.add(response.getUri());

        for (Crosswalk xwalk : requestCrosswalks) {

            data.add(xwalk.getType());
            data.add(xwalk.getValue());
            data.add(xwalk.getSourceTable());
            data.add(String.valueOf(xwalk.isDataProvider() ? "true" : ""));
        }

        writeLines.add(data.stream().toArray(String[]::new));

        writer.writeToFile(writeLines);
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

            if (!response.getSuccessful()) {
                logger.error("Operation is not successful");
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
