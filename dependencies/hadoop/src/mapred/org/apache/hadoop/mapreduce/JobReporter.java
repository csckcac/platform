package org.apache.hadoop.mapreduce;

import org.apache.hadoop.mapred.RunningJob;

import java.util.HashMap;

/**
 * WSO2 Fix: This class is added to keep track of Job Submitted to Map - Reduce system and status reporting. 
 * Extending class has to implement the underlying reporting mechanism through init() method inherited by this class.
 */

public abstract class JobReporter {
  
  private RunningJob runningJob;

  public abstract void init();

  public void setRunningJob (RunningJob runningJob) {
    this.runningJob = runningJob;
  }

  public RunningJob getRunningJob () {
    return this.runningJob;
  }
	
}
