package org.wso2.carbon.mapred.mgt;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.Counters;
import org.apache.hadoop.mapreduce.JobReporter;
import org.apache.log4j.Logger;

public class CarbonJobReporter extends JobReporter {
	
	private final Logger log = Logger.getLogger(CarbonJobReporter.class);
	private String jobId = null;
	private String jobName = null;
	private Counters counters = null;
	private boolean isComplete = false;
	private boolean isSuccessful = false;
	private float mapProgress;
	private float redProgress;
	@Override
	public void run() {
		RunningJob runningJob = getRunningJob();
		try {
			jobId = runningJob.getID().getJtIdentifier(); 
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Job ID: "+jobId);
			jobName = runningJob.getJobName();
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Job Name: "+jobName);
			counters = runningJob.getCounters();
			synchronized (this) {
				this.notify();
			}
			while (!runningJob.isComplete()) {
				mapProgress = runningJob.mapProgress();
				redProgress = runningJob.reduceProgress();
				Thread.sleep(1000);
			}
			isComplete = runningJob.isComplete();
			isSuccessful = runningJob.isSuccessful();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public float getMapProgress() {
		return this.mapProgress;
	}
	
	public float getReduceProgress() {
		return this.redProgress;
	}
	
	public String getJobId() {
		return this.jobId;
	}
	
	public String getJobName() {
		return this.jobName;
	}
	
	public long getCounter(Enum key) {
		return this.counters.getCounter(key);
	}
	
	public boolean isJobComplete() {
		return this.isComplete;
	}
	
	public boolean isJobSuccessful() {
		return this.isSuccessful;
	}
	
	public static class CarbonJobReporterMap {
		private static HashMap<UUID, CarbonJobReporter> reporterMap;
		static {
			reporterMap = new HashMap<UUID, CarbonJobReporter>();
		}
		
		public static void putCarbonHadoopJobReporter(UUID uuid, CarbonJobReporter reporter) {
			reporterMap.put(uuid, reporter);
		}
		
		public static CarbonJobReporter getCarbonHadoopJobReporter(UUID uuid) {
			return reporterMap.get(uuid);
		}
	}
}
