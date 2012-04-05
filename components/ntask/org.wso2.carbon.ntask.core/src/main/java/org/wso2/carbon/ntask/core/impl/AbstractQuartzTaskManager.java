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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.ntask.common.TaskConstants;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.common.TaskException.Code;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.TaskRepository;
import org.wso2.carbon.ntask.core.TaskInfo.TriggerInfo;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;

/**
 * This class represents an abstract class implementation of TaskManager based on Quartz Scheduler.
 * @see TaskManager
 */
public abstract class AbstractQuartzTaskManager implements TaskManager {

	private static final Log log = LogFactory.getLog(AbstractQuartzTaskManager.class);
	
	private TaskRepository taskRepository;
	
	private Scheduler scheduler;
	
	public AbstractQuartzTaskManager(TaskRepository taskRepository) throws TaskException {
		this.taskRepository = taskRepository;
	    this.scheduler = TasksDSComponent.getScheduler(); 
	}

	public TaskRepository getTaskRepository() {
		return taskRepository;
	}
	
	protected Scheduler getScheduler() {
		return scheduler;
	}
	
	protected TaskState getLocalTaskState(String taskName) throws TaskException {
		String taskGroup = this.getTenantTaskGroup();
		if (!this.containsLocalTask(taskName, taskGroup)) {
			throw new TaskException("Non-existing task with name: " + taskName + 
					", to check the state.", Code.NO_TASK_EXISTS);
		}
		try {
			return triggerStateToTaskState(this.getScheduler().getTriggerState(
					new TriggerKey(taskName, taskGroup)));
		} catch (SchedulerException e) {
			throw new TaskException("Error in checking state of the task with the name: "
					+ taskName, Code.UNKNOWN, e);
		}
	}
	
	protected Map<String, TaskState> getAllLocalTaskStates() throws TaskException {
		try {
			Set<TriggerKey> keys = this.getScheduler().getTriggerKeys(
					GroupMatcher.triggerGroupEquals(this.getTenantTaskGroup()));
			Map<String, TaskState> states = new HashMap<String, TaskManager.TaskState>();
			for (TriggerKey key : keys) {
				states.put(key.getName(), triggerStateToTaskState(
						this.getScheduler().getTriggerState(
					    new TriggerKey(key.getName(), key.getGroup()))));
			}
			return states;
		} catch (SchedulerException e) {
			throw new TaskException("Error in retrieving task states", Code.UNKNOWN, e);
		}
	}
	
	protected void registerLocalTask(TaskInfo taskInfo) throws TaskException {
		this.getTaskRepository().addTask(taskInfo);
	}
	
	private TaskState triggerStateToTaskState(TriggerState triggerState) {
		if (triggerState == TriggerState.NONE) {
			return TaskState.STOPPED;
		} else if (triggerState == TriggerState.PAUSED) {
			return TaskState.PAUSED;
		} else if (triggerState == TriggerState.COMPLETE) {
			return TaskState.FINISHED;
		} else if (triggerState == TriggerState.ERROR) {
			return TaskState.ERROR;
		} else if (triggerState == TriggerState.NORMAL) {
			return TaskState.STARTED;
		} else {
			return TaskState.UNKNOWN;
		}
	}
	
	protected synchronized void deleteLocalTask(String taskName, boolean removeRegistration) throws TaskException {
		String taskGroup = this.getTenantTaskGroup();
		try {
		    this.getScheduler().deleteJob(new JobKey(taskName, taskGroup));
		} catch (SchedulerException e) {
			throw new TaskException("Error in deleting task with name: " + taskName,
					Code.UNKNOWN, e);
		}
		if (removeRegistration) {
		    this.getTaskRepository().deleteTask(taskName);
		}
	}
	
	protected synchronized void pauseLocalTask(String taskName) throws TaskException {
		String taskGroup = this.getTenantTaskGroup();
		try {
		    this.getScheduler().pauseJob(new JobKey(taskName, taskGroup));
		} catch (SchedulerException e) {
			throw new TaskException("Error in pausing task with name: " + taskName,
					Code.UNKNOWN, e);
		}
	}
		
	private String getTenantTaskGroup() {
		return "TENANT" + SuperTenantCarbonContext.getCurrentContext().getTenantId();
	}
	
	private JobDataMap getJobDataMapFromTaskInfo(TaskInfo taskInfo) {
		JobDataMap dataMap = new JobDataMap();
		dataMap.put(TaskConstants.TASK_CLASS_NAME, taskInfo.getTaskClass());
		dataMap.put(TaskConstants.TASK_PROPERTIES, taskInfo.getProperties());
		return dataMap;
	}
	
	protected synchronized void scheduleLocalAllTasks() throws TaskException {
		List<TaskInfo> tasks = this.getTaskRepository().getAllTasks();
		for (TaskInfo task : tasks) {
			try {
			    this.scheduleTask(task.getName());
			} catch (Exception e) {
				log.error("Error in scheduling task: " + e.getMessage(), e);
			}
		}
	}
	
	protected synchronized void scheduleLocalTask(String taskName) throws TaskException {
		TaskInfo taskInfo = this.getTaskRepository().getTask(taskName);
		String taskGroup = this.getTenantTaskGroup();
		if (taskInfo == null) {
			throw new TaskException("Non-existing task for scheduling with name: " + taskName,
					Code.NO_TASK_EXISTS);
		}
		if (this.containsLocalTask(taskName, taskGroup)) {
			throw new TaskException("The task with name: " + taskName + ", already started.",
					Code.TASK_ALREADY_STARTED);
		}		
		JobDetail job = JobBuilder.newJob(TaskQuartzJobAdapter.class).withIdentity(
				taskName, taskGroup).usingJobData(
				this.getJobDataMapFromTaskInfo(taskInfo)).build();		
		Trigger trigger = this.getTriggerFromInfo(taskName, taskGroup, taskInfo.getTriggerInfo());
		try {
			this.getScheduler().scheduleJob(job, trigger);
			log.info("Task scheduled: " + taskName);
		} catch (SchedulerException e) {
			throw new TaskException("Error in scheduling task with name: " + taskName,
					Code.UNKNOWN, e);
		}
	}
	
	private Trigger getTriggerFromInfo(String taskName, String taskGroup,
			TriggerInfo triggerInfo) {
		TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger().withIdentity(
				taskName, taskGroup);
		if (triggerInfo.getStartTime() == null) {
			triggerBuilder = triggerBuilder.startNow();
		} else {
			triggerBuilder = triggerBuilder.startAt(triggerInfo.getStartTime());
		}
		if (triggerInfo.getEndTime() != null) {
			triggerBuilder.endAt(triggerInfo.getEndTime());
		}
		Trigger trigger;
		if (triggerInfo.getCronExpression() != null) {
			trigger = triggerBuilder.withSchedule(this.getCronScheduleBuilder(
					triggerInfo)).build();
		} else {
			if (triggerInfo.getRepeatCount() == 0) {
				/* only once executed */
				trigger = triggerBuilder.build();
			} else {
			    trigger = triggerBuilder.withSchedule(this.getSimpleScheduleBuilder(
					triggerInfo)).build();
			}
		}
		return trigger;
	}
	
	protected synchronized void rescheduleLocalTask(String taskName) throws TaskException {
		String taskGroup = this.getTenantTaskGroup();
		TaskInfo taskInfo = this.getTaskRepository().getTask(taskName);
		Trigger trigger = this.getTriggerFromInfo(taskName, taskGroup, taskInfo.getTriggerInfo());
		try {
			Date resultDate = this.getScheduler().rescheduleJob(
					new TriggerKey(taskName, taskGroup), trigger);
			if (resultDate == null) {
				/* do normal schedule */
				this.scheduleLocalTask(taskName);
			}
		} catch (SchedulerException e) {
			throw new TaskException("Error in rescheduling task with name: " + taskName,
					Code.UNKNOWN, e);
		}
	}
	
	protected synchronized void resumeLocalTask(String taskName) throws TaskException {
		String taskGroup = this.getTenantTaskGroup();
		if (!this.containsLocalTask(taskName, taskGroup)) {
			throw new TaskException("Non-existing task for resuming with name: " + taskName,
					Code.NO_TASK_EXISTS);
		}
		try {
			this.getScheduler().resumeJob(new JobKey(taskName, taskGroup));
		} catch (SchedulerException e) {
			throw new TaskException("Error in resuming task with name: " + taskName,
					Code.UNKNOWN, e);
		}
	}
	
	protected synchronized boolean isLocalTaskScheduled(String taskName) throws TaskException {
		String taskGroup = this.getTenantTaskGroup();
		return this.containsLocalTask(taskName, taskGroup);
	}
	
	protected List<TaskInfo> getAllLocalScheduledTasks() throws TaskException {
		List<TaskInfo> tasks = this.getTaskRepository().getAllTasks();
		List<TaskInfo> result = new ArrayList<TaskInfo>();
		for (TaskInfo taskInfo : tasks) {
			if (this.isLocalTaskScheduled(taskInfo.getName())) {
				result.add(taskInfo);
			}
		}
		return result;
	}
	
	private boolean containsLocalTask(String taskName, String taskGroup) throws TaskException {
		try {
		    return this.getScheduler().checkExists(new JobKey(taskName, taskGroup));
		} catch (SchedulerException e) {
			throw new TaskException("Error in retrieving task details", Code.UNKNOWN, e);
		}
	}
	
	private CronScheduleBuilder getCronScheduleBuilder(TriggerInfo triggerInfo) {
		return CronScheduleBuilder.cronSchedule(triggerInfo.getCronExpression()).
				withMisfireHandlingInstructionIgnoreMisfires();
	}
	
	private SimpleScheduleBuilder getSimpleScheduleBuilder(TriggerInfo triggerInfo) {
		SimpleScheduleBuilder scheduleBuilder = null;
		if (triggerInfo.getRepeatCount() == -1) {
			scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().repeatForever();
		} else if (triggerInfo.getRepeatCount() > 0) {
			scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withRepeatCount(
					triggerInfo.getRepeatCount());
		}
		scheduleBuilder = scheduleBuilder.withIntervalInMilliseconds(
				triggerInfo.getIntervalMillis());
		scheduleBuilder = scheduleBuilder.withMisfireHandlingInstructionNextWithRemainingCount(); 
		return scheduleBuilder;
	}
	
}
