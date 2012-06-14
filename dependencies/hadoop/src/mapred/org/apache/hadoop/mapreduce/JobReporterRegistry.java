package org.apache.hadoop.mapreduce;

import org.apache.log4j.Logger;

/**
 * WSO2 Fix:
 */
public class JobReporterRegistry {
	
	private static final ThreadLocal<JobReporter> jobReporterTL = new ThreadLocal<JobReporter>();
	private static final Logger log = Logger.getLogger(JobReporterRegistry.class);
	
	public static void setReporter(JobReporter jr) {
		jobReporterTL.set(jr);
	}
	
        /**
         * Calling this method will remove the object from the thread's local variable map.
         */
	public static JobReporter getReporter() {
		JobReporter reporter = jobReporterTL.get();
                jobReporterTL.remove();
                return reporter;
	}
	
}
