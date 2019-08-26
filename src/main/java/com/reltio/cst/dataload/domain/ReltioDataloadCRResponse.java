package com.reltio.cst.dataload.domain;

import java.util.HashMap;

public class ReltioDataloadCRResponse {

	private String uri;
	private String state;
	
	private HashMap<String, Object> changes;
	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}
	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}
	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}
	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}
	/**
	 * @return the changes
	 */
	public HashMap<String, Object> getChanges() {
		return changes;
	}
	/**
	 * @param changes the changes to set
	 */
	public void setChanges(HashMap<String, Object> changes) {
		this.changes = changes;
	}


}
