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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

/**
 * This class represents an configuration context observer, used to load the tasks, when a new
 * tenant arrives.
 */
public class TaskAxis2ConfigurationContextObserver extends AbstractAxis2ConfigurationContextObserver {

	private static final Log log = LogFactory.getLog(TaskAxis2ConfigurationContextObserver.class);
	
	private TaskService taskService;
	
	public TaskAxis2ConfigurationContextObserver(TaskService taskService) {
		this.taskService = taskService;
	}
	
	@Override
	public void createdConfigurationContext(ConfigurationContext configContext) {
		int tid = SuperTenantCarbonContext.getCurrentContext(configContext).getTenantId();
		try {
			this.getTaskService().newTenantArrived(tid);
		} catch (TaskException e) {
			log.error("Error in initializing tasks for tenant: " + tid, e);
		}
	}
	
	public TaskService getTaskService() {
		return taskService;
	}
	
}
