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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.common.TaskException.Code;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskRepository;
import org.wso2.carbon.ntask.core.TaskUtils;
import org.wso2.carbon.ntask.core.service.impl.RegistryTaskAvailabilityManager;

/**
 * Registry based task repository implementation.
 */
public class RegistryBasedTaskRepository implements TaskRepository {
	
	private Log log = LogFactory.getLog(RegistryBasedTaskRepository.class);
	
	public static final String REG_TASK_REPO_BASE_PATH = "/repository/components/org.wso2.carbon.tasks/types";

	private Registry registry;
	
	private String taskType;
	
	private Marshaller taskMarshaller;
	
	private Unmarshaller taskUnmarshaller;
	
	private int tid;
	
	private RegistryTaskAvailabilityManager taskAvailabilityManager;
		
	public RegistryBasedTaskRepository(int tid, String taskType, RegistryTaskAvailabilityManager
			taskAvailabilityManager) throws TaskException {
		this.tid = tid;
		this.taskType = taskType;
		this.taskAvailabilityManager = taskAvailabilityManager;
		try {
		    JAXBContext ctx = JAXBContext.newInstance(TaskInfo.class);
		    this.taskMarshaller = ctx.createMarshaller();
		    this.taskUnmarshaller = ctx.createUnmarshaller();
		} catch (JAXBException e) {
			throw new TaskException("Error creating task marshaller/unmarshaller", 
					Code.CONFIG_ERROR, e);
		}
	}
	
	public RegistryTaskAvailabilityManager getTaskAvailabilityManager() {
		return taskAvailabilityManager;
	}
	
	public int getTenantId() {
		return tid;
	}

	public Marshaller getTaskMarshaller() {
		return taskMarshaller;
	}

	public Unmarshaller getTaskUnmarshaller() {
		return taskUnmarshaller;
	}

	public synchronized Registry getRegistry() throws TaskException {
		if (this.registry == null) {
		    this.registry = TaskUtils.getGovRegistryForTenant(this.getTenantId());
		    if (log.isDebugEnabled()) {
		        log.debug("Retrieving the governance registry for tenant: " + this.getTenantId());
		    }
		}
		return registry;
	}
	
	public String getTaskType() {
		return taskType;
	}

	@Override
	public List<TaskInfo> getAllTasks() throws TaskException {
		List<TaskInfo> result = new ArrayList<TaskInfo>();
		String tasksPath = this.getMyTasksPath();
		try {
			this.getRegistry().beginTransaction();
			if (this.getRegistry().resourceExists(tasksPath)) {
				Collection tasksCollection = (Collection) this.getRegistry().get(tasksPath);
				String[] taskPaths = tasksCollection.getChildren();
				TaskInfo taskInfo;
				for (String taskPath : taskPaths) {
					taskInfo = this.getTaskInfoRegistryPath(taskPath);
					result.add(taskInfo);
				}
			}
			this.getRegistry().commitTransaction();
			return result;
		} catch (Exception e) {
			try {
				this.getRegistry().rollbackTransaction();
			} catch (RegistryException e2) {
				log.error(e2);
			}
			throw new TaskException("Error in getting all tasks from repository", 
					Code.CONFIG_ERROR, e);
		}
	}
	
	private int getTaskCount() throws TaskException {
		String tasksPath = this.getMyTasksPath();
		try {
			if (this.getRegistry().resourceExists(tasksPath)) {
				Collection tasksCollection = (Collection) this.getRegistry().get(tasksPath);
				return tasksCollection.getChildCount();
			} else {
				return 0;
			}
		} catch (Exception e) {			
			throw new TaskException("Error in getting task count from repository", 
					Code.CONFIG_ERROR, e);
		}
	}

	@Override
	public TaskInfo getTask(String taskName) throws TaskException {
		String tasksPath = this.getMyTasksPath();
		String currentTaskPath = tasksPath + "/" + taskName;
		try {
			this.getRegistry().beginTransaction();
			if (!this.getRegistry().resourceExists(currentTaskPath)) {
				throw new TaskException("The task with name: " + taskName + 
						" doesn't exist in the repository for reload", Code.NO_TASK_EXISTS);
			}
			TaskInfo taskInfo = this.getTaskInfoRegistryPath(currentTaskPath);
			this.getRegistry().commitTransaction();
			return taskInfo;
		} catch (Exception e) {
			try {
				this.getRegistry().rollbackTransaction();
			} catch (RegistryException e2) {
				log.error(e2);
			}
			throw new TaskException("Error in reloading task '" + taskName + "' from registry", 
					Code.CONFIG_ERROR, e);
		}
	}

	@Override
	public synchronized void addTask(TaskInfo taskInfo) throws TaskException {
		String tasksPath = this.getMyTasksPath();
		String currentTaskPath = tasksPath + "/" + taskInfo.getName();
		try {
			this.getRegistry().beginTransaction();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			this.getTaskMarshaller().marshal(taskInfo, out);
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			Resource resource = this.getRegistry().newResource();
			resource.setContentStream(in);
			this.getRegistry().put(currentTaskPath, resource);
			this.getTaskAvailabilityManager().setTasksAvailable(this.getTenantId(), true);
			this.getRegistry().commitTransaction();
		} catch (Exception e) {
			try {
				this.getRegistry().rollbackTransaction();
				this.processTasksAvailable();
			} catch (RegistryException e2) {
				log.error(e2);
			}
			throw new TaskException("Error in adding task '" + taskInfo.getName()
					+ "' to the repository: " + e.getMessage(), Code.CONFIG_ERROR, e);
		}
	}

	private void processTasksAvailable() throws TaskException {
		if (this.getTaskCount() == 0) {
			this.getTaskAvailabilityManager().setTasksAvailable(this.getTenantId(), false);
		}
	}
	
	@Override
	public synchronized void deleteTask(String taskName) throws TaskException {
		String tasksPath = this.getMyTasksPath();
		String currentTaskPath = tasksPath + "/" + taskName;
		try {
			this.getRegistry().beginTransaction();
			if (!this.getRegistry().resourceExists(currentTaskPath)) {
				throw new TaskException("The task with name: " + taskName + 
						" doesn't exist in the repository for deletion", Code.NO_TASK_EXISTS);
			}
			this.getRegistry().delete(currentTaskPath);
			this.getRegistry().commitTransaction();
			this.processTasksAvailable();
		} catch (RegistryException e) {
			try {
				this.getRegistry().rollbackTransaction();
			} catch (RegistryException e2) {
				log.error(e2);
			}
			throw new TaskException("Error in deleting task '" + taskName
					+ "' in the repository", Code.CONFIG_ERROR, e);
		}
	}
	
	private String getMyTasksPath() {
		return REG_TASK_REPO_BASE_PATH + "/" + this.getTasksType();
	}
	
	private TaskInfo getTaskInfoRegistryPath(String path) throws Exception {
		Resource resource = this.getRegistry().get(path);
		InputStream in = resource.getContentStream();
		TaskInfo taskInfo = (TaskInfo) this.getTaskUnmarshaller().unmarshal(in);
		in.close();
		taskInfo.getProperties().put(TaskInfo.TENANT_ID_PROP, String.valueOf(this.getTenantId()));
		return taskInfo;
	}
	
	@Override
	public String getTasksType() {
		return taskType;
	}

}
