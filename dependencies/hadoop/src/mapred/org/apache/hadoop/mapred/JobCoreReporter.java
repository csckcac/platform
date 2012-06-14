package org.apache.hadoop.mapred;

import org.apache.hadoop.conf.Configuration;

//WSO2 Fix:

public abstract class JobCoreReporter {

	private String jobUser = null;
	private String jobId = null;
	private String jobName = null;
	private Counters counters = null;
	//private long launchTime = 0;
	private long startTime = 0;
	//private long finishTime = 0;
	private String status = "UNKNOWN";
	private float mapProgress = -1;
	private float reduceProgress = -1;
	private String failureInfo = null;
	private String schedInfo = null;

	public abstract void init(Configuration conf);

	public void setJobUser(String user) {
		jobUser = user;
	}
	public String getJobUser() {
		return jobUser;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getJobId() {
		return jobId;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getJobName() {
		return jobName;
	}

	public void setCounters(Counters counters) {
		this.counters = counters;
	}
	public Counters getCounters() {
		return counters;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getStartTime() {
		return startTime;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatus() {
		return this.status;
	}

	public void setMapProgress(float progress) {
		this.mapProgress = progress;
	}
	public float getMapProgress() {
		return mapProgress;
	}

	public void setReduceProgress(float progress) {
		this.reduceProgress = progress;
	}
	public float getReduceProgress() {
		return reduceProgress;
	}

	public void setFailureInfo(String info) {
		this.failureInfo = info;
	}
	public String getFailureInfo() {
		return failureInfo;
	}

	public void setSchedInfo(String info) {
		this.schedInfo = info;
	}
	public String getSchedInfo() {
		return schedInfo;
	}
}
