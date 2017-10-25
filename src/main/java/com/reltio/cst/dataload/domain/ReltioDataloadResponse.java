package com.reltio.cst.dataload.domain;


public class ReltioDataloadResponse {

	private Integer index;
	private Boolean successful;
	private String uri;
	private ReltioDataloadErrors errors;

	/**
	 * @return the index
	 */
	public Integer getIndex() {
		return index;
	}

	/**
	 * @param index
	 *            the index to set
	 */
	public void setIndex(Integer index) {
		this.index = index;
	}

	/**
	 * @return the successful
	 */
	public Boolean getSuccessful() {
		return successful;
	}

	/**
	 * @param successful
	 *            the successful to set
	 */
	public void setSuccessful(Boolean successful) {
		this.successful = successful;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @param uri
	 *            the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * @return the errors
	 */
	public ReltioDataloadErrors getErrors() {
		return errors;
	}

	/**
	 * @param errors
	 *            the errors to set
	 */
	public void setErrors(ReltioDataloadErrors errors) {
		this.errors = errors;
	}

}
