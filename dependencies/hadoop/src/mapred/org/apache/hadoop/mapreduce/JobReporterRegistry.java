package org.apache.hadoop.mapreduce;

import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * WSO2 Fix:
 */
public class JobReporterRegistry {
	
	private static HashMap<Long, JobReporter> reporterRegistry;
	private static Logger log = Logger.getLogger(JobReporterRegistry.class);
	
	static {
		reporterRegistry = new HashMap<Long, JobReporter>();
	}
	
	public static void setReporter(Long threadId, JobReporter jr) {
		reporterRegistry.put(threadId, jr);
	}
	
	public static JobReporter getReporter(Long threadId) {
		return reporterRegistry.remove(threadId);
	}
	
}
