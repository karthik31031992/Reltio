package com.reltio.cst.dataload.domain;

import java.util.List;

public class ReltioDataloadErrors {

	private String severity;
	private String errorMessage;
	private String errorDetailMessage;
	private Integer errorCode;

	private List<ReltioDataloadErrors> foundErrors;

	private String crosswalkType;
	private String crosswalkValue;

	/**
	 * @return the severity
	 */
	public String getSeverity() {
		return severity;
	}

	/**
	 * @param severity
	 *            the severity to set
	 */
	public void setSeverity(String severity) {
		this.severity = severity;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage
	 *            the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * @return the errorDetailMessage
	 */
	public String getErrorDetailMessage() {
		return errorDetailMessage;
	}

	/**
	 * @param errorDetailMessage
	 *            the errorDetailMessage to set
	 */
	public void setErrorDetailMessage(String errorDetailMessage) {
		this.errorDetailMessage = errorDetailMessage;
	}

	/**
	 * @return the errorCode
	 */
	public Integer getErrorCode() {
		return errorCode;
	}

	/**
	 * @param errorCode
	 *            the errorCode to set
	 */
	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * @return the crosswalkType
	 */
	public String getCrosswalkType() {
		return crosswalkType;
	}

	/**
	 * @param crosswalkType
	 *            the crosswalkType to set
	 */
	public void setCrosswalkType(String crosswalkType) {
		this.crosswalkType = crosswalkType;
	}

	/**
	 * @return the crosswalkValue
	 */
	public String getCrosswalkValue() {
		return crosswalkValue;
	}

	/**
	 * @param crosswalkValue
	 *            the crosswalkValue to set
	 */
	public void setCrosswalkValue(String crosswalkValue) {
		this.crosswalkValue = crosswalkValue;
	}

	public List<ReltioDataloadErrors> getFoundErrors() {
		return foundErrors;
	}

	public void setFoundErrors(List<ReltioDataloadErrors> foundErrors) {
		this.foundErrors = foundErrors;
	}
}

