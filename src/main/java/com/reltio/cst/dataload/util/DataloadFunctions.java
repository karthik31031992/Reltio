package com.reltio.cst.dataload.util;

import com.google.gson.Gson;
import com.reltio.cst.dataload.DataloadConstants;
import com.reltio.cst.dataload.domain.DataloaderInput;
import com.reltio.cst.dataload.domain.ReltioDataloadErrors;
import com.reltio.cst.dataload.domain.StatusResponse;
import com.reltio.cst.domain.Attribute;
import com.reltio.cst.exception.handler.GenericException;
import com.reltio.cst.exception.handler.ReltioAPICallFailureException;
import com.reltio.cst.service.ReltioAPIService;
import com.reltio.file.ReltioFileWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static com.reltio.cst.dataload.DataloadConstants.GREEN_STATUS;
import static com.reltio.cst.dataload.DataloadConstants.YELLOW_STATUS;


public class DataloadFunctions {

    private static final Logger logger = LogManager.getLogger(DataloadFunctions.class.getName());
    private static final Logger logPerformance = LogManager.getLogger("performance-log");
    private static Gson gson = new Gson();

    public static void invalidJSonError(String json,
                                        DataloaderInput dataloaderInput, ReltioFileWriter reltioFileWriter)
            throws IOException {
        logger.debug(json);
        logger.error("Invalid JSON..");
        reltioFileWriter.writeToFile(json);
        List<ReltioDataloadErrors> dataloadErrors = dataloaderInput
                .getDataloadErrorsMap()
                .get(DataloadConstants.INVALID_JSON_FILE);
        ReltioDataloadErrors reltioDataloadError = new ReltioDataloadErrors();
        if (dataloadErrors == null) {
            dataloadErrors = new ArrayList<>();
        }

        reltioDataloadError.setErrorCode(DataloadConstants.INVALID_JSON_FILE);
        reltioDataloadError
                .setErrorDetailMessage(DataloadConstants.INVALID_JSON_FILE_MESSAGE);
        dataloadErrors.add(reltioDataloadError);
        dataloaderInput.getDataloadErrorsMap().put(DataloadConstants.INVALID_JSON_FILE, dataloadErrors);
        dataloaderInput.addFailureCount(1);

    }

    public static List<Attribute> getAttribute(List<Attribute> attributes,
                                               Object obj) {

        String value = getStringValue(obj);

        if (checkNull(value)) {
            if (attributes == null) {
                attributes = new ArrayList<Attribute>();
            }
            Attribute attribute = new Attribute();
            attribute.setValue(value.trim());
            attributes.add(attribute);
        }

        return attributes;
    }

    public static List<Attribute> getAttribute(List<Attribute> attributes,
                                               String value) {
        if (checkNull(value)) {
            if (attributes == null) {
                attributes = new ArrayList<Attribute>();
            }
            Attribute attribute = new Attribute();
            attribute.setValue(value.trim());
            attributes.add(attribute);
        }

        return attributes;
    }

    public static List<Attribute> getAttribute(List<Attribute> attributes,
                                               String value, String type) {
        if (checkNull(value)) {
            if (attributes == null) {
                attributes = new ArrayList<Attribute>();
            }
            Attribute attribute = new Attribute();
            attribute.setValue(value);
            attribute.setType(type);
            attributes.add(attribute);
        }

        return attributes;
    }

    public static List<Attribute> getAttribute(List<Attribute> attributes,
                                               String value, String type, Boolean dataProvider) {
        if (checkNull(value)) {
            if (attributes == null) {
                attributes = new ArrayList<Attribute>();
            }
            Attribute attribute = new Attribute();
            attribute.setValue(value);
            attribute.setType(type);
            attribute.setDataProvider(dataProvider);
            attributes.add(attribute);
        }

        return attributes;
    }

    public static List<Attribute> getAttribute(List<Attribute> attributes,
                                               String value, String type, String updateDate) {
        if (checkNull(value)) {
            if (attributes == null) {
                attributes = new ArrayList<Attribute>();
            }
            Attribute attribute = new Attribute();
            attribute.setValue(value);
            attribute.setType(type);
            attribute.setUpdateDate(updateDate);
            attributes.add(attribute);
        }

        return attributes;
    }

    public static List<Attribute> getAttribute(List<Attribute> attributes,
                                               String value, String type, String createDate, String updateDate) {
        if (checkNull(value)) {
            if (attributes == null) {
                attributes = new ArrayList<Attribute>();
            }
            Attribute attribute = new Attribute();
            attribute.setValue(value);
            attribute.setType(type);
            attribute.setCreateDate(createDate);
            attribute.setUpdateDate(updateDate);
            attributes.add(attribute);
        }

        return attributes;
    }

    public static List<Attribute> getAttribute(List<Attribute> attributes,
                                               String value, String type, String createDate, String updateDate,
                                               String deleteDate) {
        if (checkNull(value)) {
            if (attributes == null) {
                attributes = new ArrayList<Attribute>();
            }
            Attribute attribute = new Attribute();
            attribute.setValue(value);
            attribute.setType(type);
            attribute.setCreateDate(createDate);
            attribute.setUpdateDate(updateDate);
            attribute.setDeleteDate(deleteDate);
            attributes.add(attribute);
        }

        return attributes;
    }

    // Function to ignore null values

    public static boolean checkNull(String value) {
        return value != null && !value.trim().equals("")
                && !value.trim().equals("UNKNOWN")
                && !value.trim().equals("<blank>")
                && !value.trim().equals("<UNAVAIL>")
                && !value.trim().equals("#")
                && !value.toLowerCase().trim().equals("null")
                && !value.toLowerCase().trim().equals("\"");
    }

    public static String getStringValue(Object obj) {
        if (obj != null) {
            return obj + "";
        }

        return null;
    }

    public static void printDataloadPerformance(long totalTasksExecuted,
                                                long totalTasksExecutionTime, long totalQueueWaitTime,
                                                long programStartTime, long numberOfThreads) {
        logger.info("[Performance]: ============= Current performance status ("
                + new Date().toString() + ") =============");
        long finalTime = System.currentTimeMillis() - programStartTime;
        logger.info("[Performance]:  Total processing time : "
                + finalTime);
        logger.info("[Performance]:  Total queue waiting time : "
                + totalQueueWaitTime);
        logger.info("[Performance]:  Entities sent: "
                + totalTasksExecuted);
        logger.info("[Performance]:  Total OPS (Entities sent / Time spent from program start): "
                + (totalTasksExecuted / (finalTime / 1000f)));
        logger.info("[Performance]:  Total OPS without waiting for queue (Entities sent / (Time spent from program start - Time spent in waiting for API queue)): "
                + (totalTasksExecuted / ((finalTime - totalQueueWaitTime) / 1000f)));
        logger.info("[Performance]:  API Server data load requests OPS (Entities sent / (Sum of time spend by API requests / Threads count)): "
                + (totalTasksExecuted / ((totalTasksExecutionTime / numberOfThreads) / 1000f)));
        logger.info("[Performance]: ===============================================================================================================");

        //log performance only in separate logs
        logPerformance.info("[Performance]: ============= Current performance status ("
                + new Date().toString() + ") =============");
        logPerformance.info("[Performance]:  Total processing time : "
                + finalTime);
        logPerformance.info("[Performance]:  Total queue waiting time : "
                + totalQueueWaitTime);
        logPerformance.info("[Performance]:  Entities sent: "
                + totalTasksExecuted);
        logPerformance.info("[Performance]:  Total OPS (Entities sent / Time spent from program start): "
                + (totalTasksExecuted / (finalTime / 1000f)));
        logPerformance.info("[Performance]:  Total OPS without waiting for queue (Entities sent / (Time spent from program start - Time spent in waiting for API queue)): "
                + (totalTasksExecuted / ((finalTime - totalQueueWaitTime) / 1000f)));
        logPerformance.info("[Performance]:  API Server data load requests OPS (Entities sent / (Sum of time spend by API requests / Threads count)): "
                + (totalTasksExecuted / ((totalTasksExecutionTime / numberOfThreads) / 1000f)));
        logPerformance.info("[Performance]: ===============================================================================================================");
    }

    /**
     * Waits for futures (load tasks list put to executor) are partially ready.
     * <code>maxNumberInList</code> parameters specifies how much tasks could be
     * uncompleted.
     *
     * @param futures         - futures to wait for.
     * @param maxNumberInList - maximum number of futures could be left in "undone" state.
     * @return sum of executed futures execution time.
     */
    public static long waitForTasksReady(Collection<Future<Long>> futures,
                                         int maxNumberInList) {
        long totalResult = 0l;
        while (futures.size() > maxNumberInList) {
            try {
                Thread.sleep(20);
            } catch (Exception e) {
                // ignore it...
            }
            for (Future<Long> future : new ArrayList<Future<Long>>(futures)) {
                if (future.isDone()) {
                    try {
                        totalResult += future.get();
                        futures.remove(future);
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                        logger.debug(e);
                    }
                }
            }
        }
        return totalResult;
    }

    /**
     * Waits for Reltio queues having not color other that <code>Yellow or Green</code>
     * messages
     *
     * @param srcUrl             - API url
     * @param threadPoolExecutor - executor service that processed uploading
     * @return total waiting for queue time. Waiting for queue is period when
     * executor executed all jobs and API just working on queue
     * @throws GenericException
     * @throws InterruptedException
     * @throws Exception
     */
    public static long waitForTenantStatus(String srcUrl,
                                    ThreadPoolExecutor threadPoolExecutor, ReltioAPIService reltioAPIService, String tenantId) throws GenericException,
            InterruptedException {

//        String queueStatus = "";
        long startWaitTime = threadPoolExecutor.getActiveCount() > 0 ? -1
                : System.currentTimeMillis();
        for (; ; ) {

            StatusResponse queueStatus = null;
            try {
                queueStatus = getQueuesSize(srcUrl, reltioAPIService, tenantId);

            } catch (Exception e) {
                logger.error("Error getting queues size");
                logger.error(e.getMessage());
                logger.debug(e);
            }
            if (queueStatus == null || queueStatus.getEventsQueueSize() == null
                    || queueStatus.getMatchingQueueSize() == null) {
                throw new GenericException("Can't get queue sizes");
            }
            logger.info("[Queues]: Main events queue size = "
                    + queueStatus.getEventsQueueSize() + ", Matching queue size = "
                    + queueStatus.getMatchingQueueSize() + ", Status ="
                    + queueStatus.getStatus());

            if (queueStatus.getStatus().equals(GREEN_STATUS)
                    || queueStatus.getStatus().equals(YELLOW_STATUS)) {
                break;
            }
            // for longer time....
            for (int k = 0; k < 10; k++) {
                if (startWaitTime < 0) {
                    logger.info("[Queues]: Executor is empty, data load program start just waiting for queue...");
                    startWaitTime = threadPoolExecutor.getActiveCount() > 0 ? -1
                            : System.currentTimeMillis();
                }
                Thread.sleep(1000);
            }
        }
        if (startWaitTime > 0) {
            return System.currentTimeMillis() - startWaitTime;
        }
        return 0l;
    }

    public static String sendEntities(String srcUrl, String stringToSend,
                                      ReltioAPIService reltioAPIService) throws GenericException,
            ReltioAPICallFailureException {
        String response = null;
        response = reltioAPIService.post(srcUrl, stringToSend);

        if (response == null) {
            throw new GenericException("Empty Response Received...");
        }
        return response;

    }

    public static StatusResponse getQueuesSize(String srcUrl, ReltioAPIService reltioAPIService, String tenantID) throws Exception {

//		String url = srcUrl.substring(0, srcUrl.indexOf("/api/")) + "/status";
        String url = srcUrl.substring(0, srcUrl.indexOf("/api/")) + "/status/tenant/" + tenantID;

        String responseStr = reltioAPIService.get(url);
        return gson.fromJson(responseStr, StatusResponse.class);

    }

}
