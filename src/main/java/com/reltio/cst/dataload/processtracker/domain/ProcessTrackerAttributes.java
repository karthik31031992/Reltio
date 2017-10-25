package com.reltio.cst.dataload.processtracker.domain;

import java.util.List;

import com.reltio.cst.domain.Attribute;

public class ProcessTrackerAttributes {

	public List<Attribute> Type;
	public List<Attribute> DataloadType;

	public List<Attribute> TypeOfData;
	public List<Attribute> StartTime;
	public List<Attribute> UpdateTime;

	public List<Attribute> EndTime;
	public List<Attribute> Status;

	public List<Attribute> APIServerUsed;
	public List<Attribute> InitiatedBy;
	public List<Attribute> Log;
	public List<Attribute> TotalNumberOfRecords;
	public List<Attribute> FailedRecordsCount;
	public List<Attribute> SuccessRecordsCount;
	public List<Attribute> TotalTimeTaken;
	public List<Attribute> RecordsPerPost;
	public List<Attribute> NumberofThreads;
	public List<Attribute> UserComment;
	
	
	//Nested Attributes
	public List<Metrics> Metrics;
	public List<FailedRecords> FailedRecords;
	public List<ErrorDetails> ErrorDetails;

}
