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
package org.wso2.carbon.ntask.core.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.coordination.core.services.CoordinationService;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.common.TaskException.Code;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.TaskRepository;
import org.wso2.carbon.ntask.core.TaskUtils;
import org.wso2.carbon.ntask.core.impl.ClusteredTaskManager;
import org.wso2.carbon.ntask.core.impl.RegistryBasedTaskRepository;
import org.wso2.carbon.ntask.core.impl.StandaloneTaskManager;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * This class represents the TaskService implementation.
 * @see TaskService
 */
public class TaskServiceImpl implements TaskService {

	private static final Log log = LogFactory.getLog(TaskServiceImpl.class);
	
	/**
	 * The map is used to keep a task manager for each tenant, 
	 * keyed by the tenant id + task type, task types are for example,
	 * ESB tasks, DSS tasks etc..
	 */
	private Map<TaskManagerId, TaskManager> taskManagerMap;
	
	private Set<String> registeredTaskTypes;
	
	private RegistryTaskAvailabilityManager taskAvailabilityManager;
	
	public TaskServiceImpl() throws TaskException {
		this.registeredTaskTypes = new HashSet<String>();
		this.taskManagerMap = new HashMap<TaskManagerId, TaskManager>();
		this.taskAvailabilityManager = new RegistryTaskAvailabilityManager();
		this.initTaskManagers();
	}
	
	public Set<String> getRegisteredTaskTypes() {
		return registeredTaskTypes;
	}
	
	private Map<TaskManagerId, TaskManager> getTaskManagerMap() throws TaskException {
		return taskManagerMap;
	}
	
	private void initTaskManagers() throws TaskException {
		List<Integer> tenants = this.getAllTenantIds();
		for (int tid : tenants) {
			this.initTaskManagersForTenant(tid);
		}
	}
	
	private void initTaskManagersForTenant(int tid) throws TaskException {
		try {
			SuperTenantCarbonContext.startTenantFlow();
			SuperTenantCarbonContext.getCurrentContext().setTenantId(tid);
			List<String> taskTypes = this.getTaskTypesForTenant(tid);
			TaskManagerId tmId;
			TaskManager taskManager;
			for (String taskType : taskTypes) {
				tmId = new TaskManagerId(tid, taskType);
				if (this.getTaskManagerMap().containsKey(tmId)) {
					continue;
				}
				taskManager = this.createTaskManager(tmId, true);
				this.getTaskManagerMap().put(tmId, taskManager);
			}
		} finally {
			SuperTenantCarbonContext.endTenantFlow();
		}
	}
	
	private RegistryTaskAvailabilityManager getTaskAvailabilityManager() {
		return taskAvailabilityManager;
	}
	
	private List<String> getTaskTypesForTenant(int tid) throws TaskException {
		List<String> types = new ArrayList<String>();
		types.addAll(this.getRegisteredTaskTypes());
		Registry registry = TaskUtils.getGovRegistryForTenant(tid);
		try {
			Resource tmpRes = registry.get(
					RegistryBasedTaskRepository.REG_TASK_REPO_BASE_PATH);
			if (tmpRes == null || !(tmpRes instanceof Collection)) {
				return new ArrayList<String>();
			}
			Collection typeCollection = (Collection) tmpRes;
			String[] children = typeCollection.getChildren();
			for (String path : children) {
				types.add(path.substring(path.lastIndexOf('/') + 1));
			}
			return types;
		} catch (ResourceNotFoundException e) {
			return types;
		} catch (Exception e) {
			throw new TaskException("Error in getting task type for tenant: " + tid,
					Code.UNKNOWN, e);
		}
	}
	
	private List<Integer> getAllTenantIds() throws TaskException {
		try {
			Tenant[] tenants = TasksDSComponent.getRealmService().getTenantManager().
					getAllTenants();
			List<Integer> tids = new ArrayList<Integer>();
			for (Tenant tenant : tenants) {
				tids.add(tenant.getId());
			}
			tids.add(MultitenantConstants.SUPER_TENANT_ID);
			return tids;
		} catch (UserStoreException e) {
			throw new TaskException("Error in listing all the tenants", Code.CONFIG_ERROR, e);
		}
	}
	
	private TaskManager createTaskManager(TaskManagerId taskManagerId, boolean isStartup) 
			throws TaskException {
		TaskRepository taskRepo = new RegistryBasedTaskRepository(taskManagerId.getTenantId(), 
				taskManagerId.getTaskType(), this.getTaskAvailabilityManager());
		CoordinationService coordinationService = TasksDSComponent.getCoordinationService();
		TaskManager taskManager;
		/* if coordination service is enabled, we can handle distributed tasks in a cluster */
		if (coordinationService.isEnabled()) {
			taskManager = new ClusteredTaskManager(taskRepo, isStartup);
		} else {
			/* if coordination service is not configured, 
			 * fallback to standalone task implementation */
			taskManager = new StandaloneTaskManager(taskRepo);
		}
		/* if only if this tenant has any tasks, schedule all the tasks */
		if (this.getTaskAvailabilityManager().checkTasksAvailable(taskManagerId.getTenantId())) {
		    taskManager.scheduleAllTasks();
		    if (log.isDebugEnabled()) {
		        log.debug("Scheduling all tasks in creating a task manager for tenant: " + 
		                taskManagerId.getTenantId());
		    }
		} else {
			if (log.isDebugEnabled()) {
			    log.debug("No tasks available to schedule in creating a task manager for tenant: " + 
			            taskManagerId.getTenantId());
			}
		}
		return taskManager;
	}

	@Override
	public synchronized TaskManager getTaskManager(String taskType) throws TaskException {
		int tid = SuperTenantCarbonContext.getCurrentContext().getTenantId();
		TaskManagerId taskManagerId = new TaskManagerId(tid, taskType);
		if (!this.getTaskManagerMap().containsKey(taskManagerId)) {
			this.getTaskManagerMap().put(taskManagerId, 
					this.createTaskManager(taskManagerId, false));
		}
		return this.getTaskManagerMap().get(taskManagerId);
	}

	/**
	 * This class represents an identifier for a task manager.
	 */
	private class TaskManagerId {

		private int tenantId;
		
		private String taskType;
		
		public TaskManagerId(int tenantId, String taskType) {
			this.tenantId = tenantId;
			this.taskType = taskType;
		}
		
		public int getTenantId() {
			return tenantId;
		}
		
		public String getTaskType() {
			return taskType;
		}
		
		@Override
		public int hashCode() {
			return (this.getTaskType() + ":" + this.getTenantId()).hashCode();
		}
		
		@Override
		public boolean equals(Object rhs) {
			return this.hashCode() == rhs.hashCode();
		}
		
		@Override
		public String toString() {
			return this.getTaskType() + ":" + this.getTenantId();
		}
		
	}

	@Override
	public void newTenantArrived(int tid) throws TaskException {
		this.initTaskManagersForTenant(tid);
	}

	@Override
	public void registerTaskType(String taskType) throws TaskException {
		this.registeredTaskTypes.add(taskType);
		/* the task manager must be initialized again,
		 * to create any missing ones */
		this.initTaskManagers();
	}
	
}
