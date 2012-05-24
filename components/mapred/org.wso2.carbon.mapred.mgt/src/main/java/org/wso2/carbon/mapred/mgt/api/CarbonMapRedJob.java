package org.wso2.carbon.mapred.mgt.api;

import org.apache.hadoop.conf.Configuration;

public interface CarbonMapRedJob {
	
	/**
	 * This method is invoked by an HadoopJobRunnerThread object which is responsible for executing 
	 * a Hadoop MapReduce job submitted to carbon MapReduce infrastructure.
	 * @param conf should be assigned to a org.apache.hadoop.conf.Configuration typed object
	 * declared in the implementing class.
	 */
	public void setConfiguration(Configuration conf);
	
	/**
	 * MapReduce job defined in the implementing class should use the Configuration object returned 
	 * by this method only.
	 * @return Configuration object set by the HadoopJobRunnerThread object.
	 */
	public Configuration getConfiguration();
	
	
	/**
	 * This is the entry point to the MapReduce job, this method is called by an HadoopJobRunnerThread 
	 * object to start the MapReduce job.
	 * @param args Arguments to the Hadoop MapReduce job.
	 */
	public void run(String[] args);
}
