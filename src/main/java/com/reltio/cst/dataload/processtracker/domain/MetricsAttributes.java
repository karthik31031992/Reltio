package com.reltio.cst.dataload.processtracker.domain;

import java.util.List;

import com.reltio.cst.domain.Attribute;

public class MetricsAttributes {
	public List<Attribute> RecordsPerPost;
	public List<Attribute> NumberofThreads;
	public List<Attribute> FailedRecordsCount;
	public List<Attribute> SuccessRecordsCount;
	public List<Attribute> TotalTimeTaken;
	public List<Attribute> QueueSize;
	public List<Attribute> TotalRecordsCount;
	
	public List<Attribute> TotalQueueWaitingTime;
	public List<Attribute> TotalOPS;
	public List<Attribute> TotalOPSWithoutQueue;
}
