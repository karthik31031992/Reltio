package com.reltio.cst.dataload.processtracker.domain;

import java.util.List;

import com.reltio.cst.domain.Attribute;

public class ProcessTrackerObject {

	public String type = "configuration/entityTypes/ProcessTracker";
	public ProcessTrackerAttributes attributes;
	public List<Attribute> crosswalks;
}
