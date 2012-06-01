package org.wso2.carbon.mapred.mgt.api;

import org.apache.hadoop.conf.Configuration;

public abstract class CarbonMapRedJob {
	
	/**
	 * This the private Configuration object for this class.
	 */
	private Configuration conf;
	
	/**
	 * This method is invoked by an HadoopJobRunnerThread object which is responsible for executing 
	 * a Hadoop MapReduce job submitted to carbon MapReduce infrastructure.
	 */
	public void setConfiguration(Configuration conf) {
		this.conf = conf;
	}
	
	/**
	 * MapReduce job defined in the implementing class should use the Configuration object returned 
	 * by this method only.
	 */
	public Configuration getConfiguration() {
		return this.conf;
	}
	
	
	/**
	 * This is the entry point to the MapReduce job, this method is called by an HadoopJobRunnerThread 
	 * object to start the MapReduce job.
	 * @param args Arguments to the Hadoop MapReduce job.
	 */
	public abstract void run(String[] args);
}
