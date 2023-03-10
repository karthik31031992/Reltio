package com.reltio.cst.dataload.domain;

import com.reltio.cst.dataload.impl.LoadJsonToTenant;
import com.reltio.cst.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static com.reltio.cst.dataload.DataloadConstants.DEFAULT_TIMEOUT_IN_MINUTES;
import static com.reltio.cst.dataload.DataloadConstants.JSON_FILE_TYPE_PIPE;
import static com.reltio.cst.dataload.DataloadConstants.RECORDS_PER_POST;
import static com.reltio.cst.dataload.DataloadConstants.THREAD_COUNT;
import static com.reltio.cst.dataload.util.DataloadFunctions.checkNull;

/***
 * This class file holds all the input data provided by the user to execute the
 * dataload process
 */
public class DataloaderInput implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5831979169383717032L;
	private static final Logger logger = LogManager.getLogger(LoadJsonToTenant.class.getName());
	private final Map<Integer, List<ReltioDataloadErrors>> dataloadErrorsMap = new HashMap<>();
	private String fileName;
	private String serverHostName;
	private String tenantId;
	private String dataloadType;
	private String dataType;
	private String username;
	private String password;
	private String smpt_host;
	private String smtp_username;
	private String smtp_password;
	private String authURL;
	private String userComments;
	private Integer threadCount;
	private Integer groupsCount;
	private String failedRecordsFileName;
	private String jsonFileType; // PIPE_ARRAY,ARRAY,OBJECT
	private List<String> emailsToSendUpdate = null;
	private boolean isMaxObjectsUpdatePresent;
	//private Boolean maxObjectsToUpdateDefault = false;
	private Integer maxObjectsToUpdate;
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
	private Long totalThrottlingWaitTime = 0l;
	private AtomicInteger totalThrottledRequests = new AtomicInteger(0);

	private String log;
	private String baseDataloadURL;
	private boolean sendMailFlag = false;

	private String failedLogFileName;

	private Boolean isPartialOverride = false;
	private Boolean isUpdateAttributeUpdateDates = false;
	private Boolean isExecuteLCA = true;
	private Boolean isAlwaysCreateDCR = false;

	private String requestsLogFilePath = null;

	private Boolean returnFullBody = false;

	private Boolean isURIrequired = false;

	private String uriFilePath;


	private List<String> requiredProps = Arrays.asList(
			"FAILED_RECORD_FILE",
			"RECORDS_PER_POST",
			"ENVIRONMENT_URL",
			"TENANT_ID",
			"DATALOAD_TYPE",
			"TYPE_OF_DATA",
			"AUTH_URL",
			"JSON_FILE");


	/**
	 * This constructor reads the data from properties file and stores in the
	 * variable
	 *
	 * @param properties
	 */
	public DataloaderInput(Properties properties) {
		Map<List<String>, List<String>> mutualExclusiveProps = new HashMap<>();

		mutualExclusiveProps.put(Arrays.asList("PASSWORD","USERNAME"), Arrays.asList("CLIENT_CREDENTIALS"));

		List<String> missingProps = Util.listMissingProperties(properties, requiredProps, mutualExclusiveProps);

		if (missingProps != null && missingProps.size() > 0) {
			logger.error("Process Aborted due to insufficient input properties... Below are the list of missing properties");
			logger.error(missingProps);

			System.exit(-1);
		}

		if (!checkNull(properties.getProperty("RECORDS_PER_POST"))) {
			groupsCount = RECORDS_PER_POST;
		} else {
			groupsCount = Integer.parseInt(properties
					.getProperty("RECORDS_PER_POST"));
		}
		if (!checkNull(properties.getProperty("THREAD_COUNT"))) {
			threadCount = THREAD_COUNT;
		} else {
			threadCount = Integer.parseInt(properties
					.getProperty("THREAD_COUNT"));
		}

		fileName = properties.getProperty("JSON_FILE");
		jsonFileType = properties.getProperty("JSON_FILE_TYPE");
		if (jsonFileType == null || jsonFileType.trim().isEmpty()) {
			jsonFileType = JSON_FILE_TYPE_PIPE;
		}

		failedRecordsFileName = properties
				.getProperty("FAILED_RECORD_FILE");

		serverHostName = properties.getProperty("ENVIRONMENT_URL");
		tenantId = properties.getProperty("TENANT_ID");
		setDataloadType(properties.getProperty("DATALOAD_TYPE")); // Entities/Relations

		dataType = properties.getProperty("TYPE_OF_DATA");
		username = properties.getProperty("USERNAME");
		password = properties.getProperty("PASSWORD");
		smtp_username = properties.getProperty("SMTP_USERNAME");
		smtp_password = properties.getProperty("SMTP_PASSWORD");
		smpt_host = properties.getProperty("MAIL_SMTP_HOST", "email-smtp.us-east-1.amazonaws.com");


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
		if (checkNull(alwaysCreateDCR) && alwaysCreateDCR.equalsIgnoreCase("TRUE")) {
			isAlwaysCreateDCR = true;

			/*
			 * Setting group count to 1 if isAlwaysCreateDCR=true . Since change request should be created per entity.
			 */
			groupsCount = 1;
		}

		requestsLogFilePath = properties.getProperty("REQUESTS_LOG_FILE");

		String returnFB = properties.getProperty("RETURN_FULL_BODY");
		if (checkNull(returnFB)) {
			if (returnFB.equalsIgnoreCase("TRUE")) {
				returnFullBody = true;
			}
		}

		if (checkNull(properties.getProperty("MAX_OBJECTS_TO_UPDATE"))) {
			isMaxObjectsUpdatePresent = true;
			maxObjectsToUpdate = Integer.parseInt(properties
					.getProperty("MAX_OBJECTS_TO_UPDATE"));
		}

		isURIrequired = Boolean.valueOf(properties.getProperty("IS_CREATED_REQUIRED","false"));
		uriFilePath = (properties.getProperty("URI_FILE","uris.csv"));

		// Initialize throttling support properties:

		waitForThrottlingEnabled = getBooleanProperty(properties, "QL_SUPPORT_ENABLED", true);
		maxThrottlingWaitTime = getLongProperty(properties, "QL_MAX_WAIT_TIME_FOR_ONE_DATALOAD_CYCLE", 300000l);
		creditsThresholdPercentage = (int)getLongProperty(properties, "QL_QUOTA_THRESHOLD_BEFORE_PAUSE", 20l);
		baseThrottlingWaitPeriod = getLongProperty(properties, "QL_BASE_TIME_FOR_EXPONENTIAL_BACKOFF", 1000l);
		maxCreditRequestsFailures = (int)getLongProperty(properties, "QL_MAX_CREDIT_REQUESTS_FAILURES", 100l);;
		totalMaxThrottlingWaitTime = getLongProperty(properties, "QL_TOTAL_WAIT_TIME_BEFORE_PROCESS_TERMINATION", (long)(1000 * 60 * 60));
	}

	static boolean getBooleanProperty(Properties properties, String name, Boolean defaultValue) {
		boolean result = defaultValue;
		if (checkNull(properties.getProperty(name))) {
			try {
				result = Boolean.parseBoolean(properties
						.getProperty(name));
			} catch (Exception e) {
				logger.error("Failed to parse property " + name + ", continue with default value " + defaultValue);
			}
		}
		return result;
	}

	static long getLongProperty(Properties properties, String name, Long defaultValue) {
		long result = defaultValue;
		if (checkNull(properties.getProperty(name))) {
			try {
				result = Long.parseLong(properties
						.getProperty(name));
			} catch (Exception e) {
				logger.error("Failed to parse property " + name + ", continue with default value " + defaultValue);
			}
		}
		return result;
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

	public String getSmtp_username() {
		return smtp_username;
	}

	public void setSmtp_username(String smtp_username) {
		this.smtp_username = smtp_username;
	}

	public String getSmpt_host() {
		return smpt_host;
	}

	public void setSmpt_host(String smpt_host) {
		this.smpt_host = smpt_host;
	}

	public String getSmtp_password() {
		return smtp_password;
	}

	public void setSmtp_password(String smtp_password) {
		this.smtp_password = smtp_password;
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
	 * @param status the status to set
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
	 * @param lastUpdateTime the lastUpdateTime to set
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
	 * @param programEndTime the programEndTime to set
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
	 * @param totalRecordsCount the totalRecordsCount to set
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
	 * @param l the totalTimeTaken to set
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
	 * @param baseDataloadURL the baseDataloadURL to set
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
	 * @param log the log to set
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
	 * @param failedRecordsFileName the failedRecordsFileName to set
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
	 * @param jsonFileType the jsonFileType to set
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
	 * @param totalQueueWaitingTime the totalQueueWaitingTime to set
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
	 * @param isProcessTrackerCreated the isProcessTrackerCreated to set
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
	 * @param emailsToSendUpdate the emailsToSendUpdate to set
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
	 * @param failedLogFileName the failedLogFileName to set
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
	 * @param isPartialOverride the isPartialOverride to set
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

	/**
	 * @return the isMaxObjectsUpdatePresent
	 */
	public boolean isMaxObjectsUpdatePresent() {
		return isMaxObjectsUpdatePresent;
	}

	/**
	 * @param isMaxObjectsUpdatePresent the isMaxObjectsUpdatePresent to set
	 */
	public void setMaxObjectsUpdatePresent(boolean isMaxObjectsUpdatePresent) {
		this.isMaxObjectsUpdatePresent = isMaxObjectsUpdatePresent;
	}

	public Boolean getURIrequired() {
		return isURIrequired;
	}

	public String getUriFilePath() {
		return uriFilePath;
	}

	public void setUriFilePath(String uriFilePath) {
		this.uriFilePath = uriFilePath;
	}

	/**
	 * Throttling support properties
	 */
	private boolean waitForThrottlingEnabled;
	private long maxThrottlingWaitTime;
	private int creditsThresholdPercentage;
	private long baseThrottlingWaitPeriod;
	private int maxCreditRequestsFailures;
	private int creditsRequestFailedNumber = 0;
	private long totalMaxThrottlingWaitTime;

	public boolean isWaitForThrottlingEnabled() {
		return waitForThrottlingEnabled;
	}

	public void setWaitForThrottlingEnabled(boolean waitForThrottlingEnabled) {
		this.waitForThrottlingEnabled = waitForThrottlingEnabled;
	}

	public long getMaxThrottlingWaitTime() {
		return maxThrottlingWaitTime;
	}

	public int getCreditsThresholdPercentage() {
		return creditsThresholdPercentage;
	}

	public long getBaseThrottlingWaitPeriod() {
		return baseThrottlingWaitPeriod;
	}

	public int creditsRequestFailed() {
		return ++creditsRequestFailedNumber;
	}

	public int getMaxCreditRequestsFailures() {
		return maxCreditRequestsFailures;
	}

	public void addThrottledRequest() {
		totalThrottledRequests.incrementAndGet();
	}

	public int getTotalThrottledRequests() {
		return totalThrottledRequests.get();
	}

	public void addThrottlingWaitTime(long time) {
		totalThrottlingWaitTime += time;
	}

	public long getThrottlingWaitTime() {
		return totalThrottlingWaitTime;
	}

	public long getTotalMaxThrottlingWaitTime() {
		return totalMaxThrottlingWaitTime;
	}


	/*
        if (!dataloaderInput.isWaitForThrottlingEnabled()) {
		return 0l;
	}

	long time = System.currentTimeMillis();
        try {
		int tryNumber = 1;
		while (System.currentTimeMillis() - time < dataloaderInput.getMaxThrottlingWaitTime()) {
			CreditsBalance tenantCreditsBalance =
					requestTenantCredits(dataloaderInput.getBaseDataloadURL() + "/", reltioAPIService, dataloaderInput.getTenantId());
			if (tenantCreditsBalance.getPrimaryBalance() != null && tenantCreditsBalance.getPrimaryBalance().getStandardSyncCredits() != null &&
					tenantCreditsBalance.getPrimaryBalance().getStandardAsyncCredits() > dataloaderInput.getCreditsThresholdPercentage()) {
				try {
					Thread.sleep((2 >> tryNumber) * dataloaderInput.getBaseThrottlingWaitPeriod());
				} catch (InterruptedException ie) {
					break;
				}
				tryNumber++;
				continue;
			}
			break;
		}
	} catch (Exception e) {
		int failures = dataloaderInput.creditsRequestFailed();
		logger.warn("Failed to get tenant credits for " + failures + " number of times...", e);
		if (failures > dataloaderInput.maxCreditRequestsFailures()) {
			logger.error("Failed to get tenant credits for " + failures + " number of times. Disable waiting for throttling...", e);
			dataloaderInput.setWaitForThrottlingEnabled(false);
		}
	}
	*/


}
