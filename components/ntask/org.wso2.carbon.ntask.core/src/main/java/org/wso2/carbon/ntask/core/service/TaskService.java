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
package org.wso2.carbon.ntask.core.service;

import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskManager;

/**
 * This interface represents the task OSGi service.
 */
public interface TaskService {

	/**
	 * Returns a task manager for the current tenant's task type. 
	 * @param taskType The type of the tasks, e.g. DSS, ESB, MS
	 * @return The created / looked-up task manager
	 * @throws TaskException	 
	 */
	public TaskManager getTaskManager(String taskType) throws TaskException;
	
	/**
	 * This method is called when a new tenant has arrived, and respective task managers should be
	 * initialized for this tenant.
     * @param tid tenant id of the newly arrived tenant 
	 * @throws TaskException
	 */
	public void newTenantArrived(int tid) throws TaskException;
	
	/**
	 * This method registers a task type in the server,
	 * this must be done for the task managers for the current tenant
	 * to be started up immediately.
	 * @param taskType The task type
	 * @throws TaskException 
	 */
	public void registerTaskType(String taskType) throws TaskException;
	
	/**
	 * Notifies the task service implementation that the server is fully initialized.
	 */
	public void serverInitialized();
	
}
