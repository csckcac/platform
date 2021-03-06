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
import java.util.List;
import java.util.Map;

import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskRepository;

/**
 * This class represents a single node task server manager, which is created when the server is run
 * in standalone mode.
 */
public class StandaloneTaskManager extends AbstractQuartzTaskManager {
		
	public StandaloneTaskManager(TaskRepository taskRepository) throws TaskException {
		super(taskRepository);
	}
	
	@Override
	public void scheduleAllTasks() throws TaskException {
		this.scheduleLocalAllTasks();
	}

	@Override
	public void scheduleTask(String taskName) throws TaskException {
		this.scheduleLocalTask(taskName);
	}

	@Override
	public void deleteTask(String taskName) throws TaskException {
		this.deleteLocalTask(taskName, true);
	}

	@Override
	public void pauseTask(String taskName) throws TaskException {
		this.pauseLocalTask(taskName);		
	}

	@Override
	public void registerTask(TaskInfo taskInfo) throws TaskException {
		this.registerLocalTask(taskInfo);
	}

	@Override
	public TaskState getTaskState(String taskName) throws TaskException {
		return this.getLocalTaskState(taskName);
	}

	@Override
	public Map<String, TaskState> getAllTaskStates() throws TaskException {
		return this.getAllLocalTaskStates();
	}

	@Override
	public TaskInfo getTask(String taskName) throws TaskException {
		return this.getTaskRepository().getTask(taskName);
	}

	@Override
	public List<TaskInfo> getAllTasks() throws TaskException {
		return this.getTaskRepository().getAllTasks();
	}

	@Override
	public int getServerCount() throws TaskException {
		return 1;
	}

	@Override
	public List<TaskInfo> getTasksInServer(int location) throws TaskException {
		if (location == 0) {
			return this.getAllLocalScheduledTasks();
		} else {
			return new ArrayList<TaskInfo>();
		}
	}

	@Override
	public List<List<TaskInfo>> getAllTasksInServers() throws TaskException {
		List<List<TaskInfo>> tasks = new ArrayList<List<TaskInfo>>();
		tasks.add(this.getTasksInServer(0));
		return tasks;
	}

	@Override
	public void rescheduleTask(String taskName) throws TaskException {
		this.rescheduleLocalTask(taskName);
	}

	@Override
	public void resumeTask(String taskName) throws TaskException {
		this.resumeLocalTask(taskName);
	}

	@Override
	public boolean isTaskScheduled(String taskName) throws TaskException {
		return this.isLocalTaskScheduled(taskName);
	}

}
