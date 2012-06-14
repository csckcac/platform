/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.mashup.javascript.hostobjects.system;

import java.util.Calendar;
import java.util.Date;

import org.wso2.carbon.mashup.javascript.hostobjects.system.internal.SystemHostObjectServiceComponent;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.common.TaskException.Code;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskInfo.TriggerInfo;
import org.wso2.carbon.ntask.core.TaskManager;

/**
 * This class represents a utility class for scheduled tasks.  
 */
public class MSTaskUtils {

	public static MSTaskInfoDTO convert(TaskInfo taskInfo) {
		MSTaskInfoDTO msTaskInfo = new MSTaskInfoDTO();
		msTaskInfo.setName(taskInfo.getName());
		TriggerInfo triggerInfo = taskInfo.getTriggerInfo();
		msTaskInfo.setCronExpression(triggerInfo.getCronExpression());
		msTaskInfo.setStartTime(dateToCal(triggerInfo.getStartTime()));
		msTaskInfo.setEndTime(dateToCal(triggerInfo.getEndTime()));
		msTaskInfo.setTaskCount(triggerInfo.getRepeatCount());
		msTaskInfo.setTaskInterval(triggerInfo.getIntervalMillis());
		return msTaskInfo;
	}
	
	public static MSTaskInfo convert(MSTaskInfoDTO msTaskInfo) {
		TriggerInfo triggerInfo = new TriggerInfo();
		triggerInfo.setCronExpression(msTaskInfo.getCronExpression());
		if (msTaskInfo.getStartTime() != null) {
		    triggerInfo.setStartTime(msTaskInfo.getStartTime().getTime());
		}
		if (msTaskInfo.getEndTime() != null) {
		    triggerInfo.setEndTime(msTaskInfo.getEndTime().getTime());
		}
		triggerInfo.setIntervalMillis((int)msTaskInfo.getTaskInterval());
		triggerInfo.setRepeatCount(msTaskInfo.getTaskCount());
		return new MSTaskInfo(msTaskInfo.getName(), MSTask.class.getName(), 
				msTaskInfo.getTaskProperties(), triggerInfo, msTaskInfo.getRuntimeProperties());
	}

	public static MSTaskInfo getTaskInfo(String taskManagerName, String taskName) 
			throws TaskException {
		
		if(SystemHostObjectServiceComponent.getTaskService() == null) {
			throw new TaskException("Task service unavailable", Code.CONFIG_ERROR);
		}
		
		TaskManager taskManager = SystemHostObjectServiceComponent.getTaskService().
				getTaskManager(taskManagerName);
		TaskInfo taskInfo = taskManager.getTask(taskName);
		
		if(taskInfo == null) {
			throw new TaskException("No task exist", Code.NO_TASK_EXISTS);
		}
		
		return (MSTaskInfo)taskInfo;
	}
	
	public static Calendar dateToCal(Date date) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}


}
