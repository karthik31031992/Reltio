/**
 * 
 */
package com.reltio.cst.dataload.domain;

import java.util.List;

import com.reltio.cst.domain.Attribute;

/**
 *
 *
 */
public class ReltioCrosswalkObject {

	private List<Attribute> crosswalks;

	/**
	 * @return the crosswalks
	 */
	public List<Attribute> getCrosswalks() {
		return crosswalks;
	}

	/**
	 * @param crosswalks
	 *            the crosswalks to set
	 */
	public void setCrosswalks(List<Attribute> crosswalks) {
		this.crosswalks = crosswalks;
	}

}
