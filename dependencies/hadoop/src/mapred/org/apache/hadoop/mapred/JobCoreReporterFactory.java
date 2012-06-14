package org.apache.hadoop.mapred;

import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hadoop.security.UserGroupInformation;

//WSO2 Fix:
public class JobCoreReporterFactory {
	private static final Log log = LogFactory.getLog(JobCoreReporterFactory.class);
	
	public static JobCoreReporter getjobCoreReporter(JobProfile jobProfile,
			JobStatus jobStatus, Counters counters, Configuration conf) {
		JobCoreReporter coreReporter = ReflectionUtils.newInstance(
				conf.getClass("hadoop.mapred.reporter", null,
						JobCoreReporter.class), conf);
		coreReporter.setCounters(counters);
		coreReporter.setJobId(jobProfile.getJobID().getJtIdentifier());
		coreReporter.setJobName(jobProfile.getJobName());
		coreReporter.setJobUser(jobStatus.getOriginalUser());
		coreReporter.setStatus(JobStatus.getJobRunState(jobStatus.getRunState()));
		coreReporter.setStartTime(jobStatus.getStartTime());
		coreReporter.setSchedInfo(jobStatus.getSchedulingInfo());
		coreReporter.setSchedInfo(jobStatus.getFailureInfo());
		coreReporter.setMapProgress(jobStatus.mapProgress());
		coreReporter.setReduceProgress(jobStatus.reduceProgress());
		return coreReporter;
	}
}
