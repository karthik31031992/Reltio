package com.reltio.cst.dataload.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.reltio.cst.dataload.DataloadConstants.*;
import static com.reltio.cst.dataload.util.DataloadFunctions.*;

/***
 * This class file holds all the input data provided by the user to execute the
 * dataload process
 */
public class DataloaderInput implements Serializable {

	/**
     *
     */
	private static final long serialVersionUID = 5831979169383717032L;

	private String fileName;
	private String serverHostName;
	private String tenantId;
	private String dataloadType;
	private String dataType;
	private String username;
	private String password;
	private String authURL;
	private String userComments;
	private Integer queueThreshold;
	private Integer threadCount;
	private Integer groupsCount;
	private String failedRecordsFileName;
	private String jsonFileType; // PIPE_ARRAY,ARRAY,OBJECT
	private List<String> emailsToSendUpdate = null;
	private Integer maxObjectsToUpdate;
	//private Boolean maxObjectsToUpdateDefault = false;

	private boolean isProcessTrackerCreated = false;

	private Long programStartTime = System.currentTimeMillis();

	// Additional Attributes;
	private String status = "InProgress";
	private String lastUpdateTime;
	private String programEndTime;
	private volatile Integer failedRecordsCount = 0;
	private volatile Integer successRecordsCount = 0;
	private volatile Integer totalRecordsCount = 0;
	private long totalTimeTaken;
	private Long totalQueueWaitingTime;
	private Integer totalOPS;
	private Integer totalOPSWithoutQueue;
	private Integer timeoutInMinutes;

	private String log;

	private final Map<Integer, List<ReltioDataloadErrors>> dataloadErrorsMap = new HashMap<>();

	private String baseDataloadURL;
	private boolean sendMailFlag = false;

	private String failedLogFileName;

	private Boolean isPartialOverride = false;
	private Boolean isUpdateAttributeUpdateDates = false;
	private Boolean isExecuteLCA = true;
	private Boolean isAlwaysCreateDCR = false;

	private String requestsLogFilePath = null;
	private Boolean returnFullBody = false;

	/**
	 * This constructor reads the data from properties file and stores in the
	 * variable
	 *
	 * @param properties
	 */
	public DataloaderInput(Properties properties) {

		if (!checkNull(properties.getProperty("RECORDS_PER_POST"))) {
			groupsCount = RECORDS_PER_POST;
		} else {
			groupsCount = Integer.parseInt(properties
					.getProperty("RECORDS_PER_POST"));
		}
		if (!checkNull(properties.getProperty("MAIL_TRANSPORT_PROTOCOL")))
			if (!checkNull(properties.getProperty("THREAD_COUNT"))) {
				threadCount = THREAD_COUNT;
			} else {
				threadCount = Integer.parseInt(properties
						.getProperty("THREAD_COUNT"));
			}

		fileName = properties.getProperty("JSON_FILE_PATH");
		jsonFileType = properties.getProperty("JSON_FILE_TYPE");
		if (jsonFileType == null || jsonFileType.trim().isEmpty()) {
			jsonFileType = JSON_FILE_TYPE_PIPE;
		}

		failedRecordsFileName = properties
				.getProperty("FAILED_RECORD_FILE_PATH");

		serverHostName = properties.getProperty("DATALOAD_SERVER_HOST");
		tenantId = properties.getProperty("TENANT_ID");
		setDataloadType(properties.getProperty("DATALOAD_TYPE")); // Entities/Relations

		dataType = properties.getProperty("TYPE_OF_DATA");
		username = properties.getProperty("USERNAME");
		password = properties.getProperty("PASSWORD");
		if (!checkNull(properties.getProperty("MAX_QUEUE_SIZE"))) {
			setQueueThreshold(MAX_QUEUE_SIZE);
		} else {
			setQueueThreshold(Integer.parseInt(properties
					.getProperty("MAX_QUEUE_SIZE")));
		}

		if (!checkNull(properties.getProperty("TIMEOUT_IN_MINUTES"))) {
			setTimeoutInMinutes(DEFAULT_TIMEOUT_IN_MINUTES);
		} else {
			setTimeoutInMinutes(Integer.parseInt(properties
					.getProperty("TIMEOUT_IN_MINUTES")));
		}

		authURL = properties.getProperty("AUTH_URL");
		userComments = properties.getProperty("USER_COMMENTS");

		if (checkNull(serverHostName) && checkNull(tenantId)) {
			baseDataloadURL = "https://" + serverHostName + "/reltio/api/"
					+ tenantId;
		}

		String skipProcessTracker = properties
				.getProperty("SKIP_PROCESS_TRACKER");

		if (checkNull(skipProcessTracker)) {
			if (skipProcessTracker.equalsIgnoreCase("TRUE")) {
				isProcessTrackerCreated = false;
			} else if (skipProcessTracker.equalsIgnoreCase("FALSE")) {
				isProcessTrackerCreated = true;
			}
		}

		String emailIds = properties.getProperty("EMAIL_IDS_TO_SEND_UPDATE");
		if (checkNull(emailIds)) {
			emailsToSendUpdate = new ArrayList<>();
			String[] emailList = emailIds.split(",", -1);
			emailsToSendUpdate.addAll(Arrays.asList(emailList));
		}

		String partialOverride = properties.getProperty("IS_PARTIAL_OVERRIDE");
		if (checkNull(partialOverride)) {
			if (partialOverride.equalsIgnoreCase("TRUE")) {
				isPartialOverride = true;
			}
		}
		String updateAttributeUpdateDates = properties.getProperty("IS_UPDATE_ATTRIBUTE_UPDATEDATE");
		if (checkNull(updateAttributeUpdateDates)) {
			if (updateAttributeUpdateDates.equalsIgnoreCase("TRUE")) {
				isUpdateAttributeUpdateDates = true;
			}
		}
		
		String executeLCA = properties.getProperty("EXECUTE_LCA");
		if (checkNull(executeLCA)) {
			if (executeLCA.equalsIgnoreCase("FALSE")) {
				isExecuteLCA = false;
			}
		}
		String alwaysCreateDCR = properties.getProperty("ALWAYS_CREATE_DCR");
		if (checkNull(alwaysCreateDCR)) {
			if (alwaysCreateDCR.equalsIgnoreCase("TRUE")) {
				isAlwaysCreateDCR = true;
			}
		}

		requestsLogFilePath = properties.getProperty("REQUESTS_LOG_FILE_PATH");

		String returnFB = properties.getProperty("RETURN_FULL_BODY");
		if (checkNull(returnFB)) {
			if (returnFB.equalsIgnoreCase("TRUE")) {
				returnFullBody = true;
			}
		}

        if (!checkNull(properties.getProperty("MAX_OBJECTS_TO_UPDATE"))) {
            maxObjectsToUpdate = MAX_OBJECTS_TO_UPDATE;
            //maxObjectsToUpdateDefault = true;
        } else {
            maxObjectsToUpdate = Integer.parseInt(properties
                    .getProperty("MAX_OBJECTS_TO_UPDATE"));
        }
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getServerHostName() {
		return serverHostName;
	}

	public void setServerHostName(String serverHostName) {
		this.serverHostName = serverHostName;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getDataloadType() {
		return dataloadType;
	}

	public void setDataloadType(String dataloadtype) {

		this.dataloadType = dataloadtype;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAuthURL() {
		return authURL;
	}

	public void setAuthURL(String authURL) {
		this.authURL = authURL;
	}

	public String getUserComments() {
		return userComments;
	}

	public void setUserComments(String userComments) {
		this.userComments = userComments;
	}

	public Integer getQueueThreshold() {
		return queueThreshold;
	}

	public void setQueueThreshold(Integer queueThreshold) {
		if (queueThreshold == null || queueThreshold > MAX_QUEUE_SIZE) {
			this.queueThreshold = MAX_QUEUE_SIZE;
		} else {
			this.queueThreshold = queueThreshold;
		}
	}

	public Integer getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(Integer threadCount) {
		this.threadCount = threadCount;
	}

	public Integer getGroupsCount() {
		return groupsCount;
	}

	public void setGroupsCount(Integer groupsCount) {
		this.groupsCount = groupsCount;
	}

	public Integer getMaxObjectsToUpdate() {
		return maxObjectsToUpdate;
	}

	public void setMaxObjectsToUpdate(Integer maxObjectsToUpdate) {
		this.maxObjectsToUpdate = maxObjectsToUpdate;
	}


	public Long getProgramStartTime() {
		return programStartTime;
	}

	public void setProgramStartTime(Long programStartTime) {
		this.programStartTime = programStartTime;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the lastUpdateTime
	 */
	public String getLastUpdateTime() {
		return lastUpdateTime;
	}

	/**
	 * @param lastUpdateTime
	 *            the lastUpdateTime to set
	 */
	public void setLastUpdateTime(String lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	/**
	 * @return the programEndTime
	 */
	public String getProgramEndTime() {
		return programEndTime;
	}

	/**
	 * @param programEndTime
	 *            the programEndTime to set
	 */
	public void setProgramEndTime(String programEndTime) {
		this.programEndTime = programEndTime;
	}

	/**
	 * @return the failedRecordsCount
	 */
	public Integer getFailedRecordsCount() {
		return failedRecordsCount;
	}

	/**
	 * @return the successRecordsCount
	 */
	public Integer getSuccessRecordsCount() {
		return successRecordsCount;
	}

	/**
	 * @return the totalRecordsCount
	 */
	public Integer getTotalRecordsCount() {
		return totalRecordsCount;
	}

	/**
	 * @param totalRecordsCount
	 *            the totalRecordsCount to set
	 */
	public void setTotalRecordsCount(Integer totalRecordsCount) {
		this.totalRecordsCount = totalRecordsCount;
	}

	/**
	 * @return the totalTimeTaken
	 */
	public long getTotalTimeTaken() {
		return totalTimeTaken;
	}

	/**
	 * @param l
	 *            the totalTimeTaken to set
	 */
	public void setTotalTimeTaken(long l) {
		this.totalTimeTaken = l;
	}

	/**
	 * @return the baseDataloadURL
	 */
	public String getBaseDataloadURL() {
		return baseDataloadURL;
	}

	/**
	 * @param baseDataloadURL
	 *            the baseDataloadURL to set
	 */
	public void setBaseDataloadURL(String baseDataloadURL) {
		this.baseDataloadURL = baseDataloadURL;
	}

	/**
	 * @return the dataloadErrorsMap
	 */
	public Map<Integer, List<ReltioDataloadErrors>> getDataloadErrorsMap() {
		return dataloadErrorsMap;
	}

	/**
	 * @return the log
	 */
	public String getLog() {
		if (log == null) {
			return "";
		}
		return log;
	}

	/**
	 * @param log
	 *            the log to set
	 */
	public void setLog(String log) {
		this.log = log;
	}

	/**
	 * @return the failedRecordsFileName
	 */
	public String getFailedRecordsFileName() {
		return failedRecordsFileName;
	}

	/**
	 * @param failedRecordsFileName
	 *            the failedRecordsFileName to set
	 */
	public void setFailedRecordsFileName(String failedRecordsFileName) {
		this.failedRecordsFileName = failedRecordsFileName;
	}

	public synchronized void addSuccessCount(Integer successCount) {
		this.successRecordsCount += successCount;
	}

	public synchronized void addFailureCount(Integer failCount) {
		this.failedRecordsCount += failCount;
	}

	public synchronized void addTotalCount(Integer totalCount) {
		this.totalRecordsCount += totalCount;
	}

	/**
	 * @return the jsonFileType
	 */
	public String getJsonFileType() {
		return jsonFileType;
	}

	/**
	 * @param jsonFileType
	 *            the jsonFileType to set
	 */
	public void setJsonFileType(String jsonFileType) {
		this.jsonFileType = jsonFileType;
	}

	/**
	 * @return the totalQueueWaitingTime
	 */
	public Long getTotalQueueWaitingTime() {
		return totalQueueWaitingTime;
	}

	/**
	 * @param totalQueueWaitingTime
	 *            the totalQueueWaitingTime to set
	 */
	public void setTotalQueueWaitingTime(Long totalQueueWaitingTime) {
		this.totalQueueWaitingTime = totalQueueWaitingTime;
		this.totalOPS = (int) (totalRecordsCount / (totalTimeTaken / 1000f));
		this.totalOPSWithoutQueue = (int) (totalRecordsCount / ((totalTimeTaken - totalQueueWaitingTime) / 1000f));
	}

	/**
	 * @return the totalOPS
	 */
	public double getTotalOPS() {
		return totalOPS;
	}

	/**
	 * @return the totalOPSWithoutQueue
	 */
	public double getTotalOPSWithoutQueue() {
		return totalOPSWithoutQueue;
	}

	/**
	 * @return the isProcessTrackerCreated
	 */
	public boolean isProcessTrackerCreated() {
		return isProcessTrackerCreated;
	}

	/**
	 * @param isProcessTrackerCreated
	 *            the isProcessTrackerCreated to set
	 */
	public void setProcessTrackerCreated(boolean isProcessTrackerCreated) {
		this.isProcessTrackerCreated = isProcessTrackerCreated;
	}

	public boolean getSendMailFlag() {

		return sendMailFlag;
	}

	public boolean getSendMailFileName() {

		return false;
	}

	/**
	 * @return the emailsToSendUpdate
	 */
	public List<String> getEmailsToSendUpdate() {
		return emailsToSendUpdate;
	}

	/**
	 * @param emailsToSendUpdate
	 *            the emailsToSendUpdate to set
	 */
	public void setEmailsToSendUpdate(List<String> emailsToSendUpdate) {
		this.emailsToSendUpdate = emailsToSendUpdate;
	}

	/**
	 * @return the failedLogFileName
	 */
	public String getFailedLogFileName() {
		return failedLogFileName;
	}

	/**
	 * @param failedLogFileName
	 *            the failedLogFileName to set
	 */
	public void setFailedLogFileName(String failedLogFileName) {
		this.failedLogFileName = failedLogFileName;
	}

	/**
	 * @return the isPartialOverride
	 */
	public Boolean getIsPartialOverride() {
		return isPartialOverride;
	}

	/**
	 * @param isPartialOverride
	 *            the isPartialOverride to set
	 */
	public void setIsPartialOverride(Boolean isPartialOverride) {
		this.isPartialOverride = isPartialOverride;
	}

	public Boolean getReturnFullBody() {
		return returnFullBody;
	}

	public String getRequestsLogFilePath() {
		return requestsLogFilePath;
	}
	
	public Boolean getIsUpdateAttributeUpdateDates() {
		return isUpdateAttributeUpdateDates;
	}

	public void setIsUpdateAttributeUpdateDates(Boolean isUpdateAttributeUpdateDates) {
		this.isUpdateAttributeUpdateDates = isUpdateAttributeUpdateDates;
	}

	public Boolean getIsExecuteLCA() {
		return isExecuteLCA;
	}

	public void setIsExecuteLCA(Boolean isExecuteLCA) {
		this.isExecuteLCA = isExecuteLCA;
	}

	public Boolean getIsAlwaysCreateDCR() {
		return isAlwaysCreateDCR;
	}

	public void setIsAlwaysCreateDCR(Boolean isAlwaysCreateDCR) {
		this.isAlwaysCreateDCR = isAlwaysCreateDCR;
	}

	public Integer getTimeoutInMinutes() {
		return timeoutInMinutes;
	}

	public void setTimeoutInMinutes(Integer timeoutInMinutes) {
		this.timeoutInMinutes = timeoutInMinutes;
	}
}
