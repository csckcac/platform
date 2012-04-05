/**
 *  Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.ntask.core.impl;

import java.util.Map;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.wso2.carbon.ntask.common.TaskConstants;
import org.wso2.carbon.ntask.core.Task;

/**
 * This class represents an adapter class used to wrap a Task in a Quartz Job.
 */
public class TaskQuartzJobAdapter implements Job {

	private Task task;
		
	public TaskQuartzJobAdapter() {		
	}
		
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JobExecutionContext ctx) throws JobExecutionException {
		if (this.getTask() == null) {
			JobDataMap dataMap = ctx.getJobDetail().getJobDataMap();
			String taskClassName = dataMap.getString(TaskConstants.TASK_CLASS_NAME);
			if (taskClassName == null) {
				throw new JobExecutionException("The task class is missing in the job data map");
			}
			try {
				this.task = (Task) Class.forName(taskClassName).newInstance();
				Map<String, String> properties = (Map<String, String>) dataMap.get(TaskConstants.TASK_PROPERTIES);
				this.task.setProperties(properties);
				this.task.init();
			} catch (Exception e) {
				throw new JobExecutionException("Error in creating an object of task class: "
						+ taskClassName, e);
			}
		}
		this.getTask().execute();
	}
	
	public Task getTask() {
		return task;
	}
	
}
