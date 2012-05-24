/**
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.common.TaskException.Code;
import org.wso2.carbon.ntask.core.TaskUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * This class manages the setting/retrieving the information of, if a specific tenant's 
 * task repository has any tasks registered. This information is stored in the 
 * super tenant's registry, and is used to avoid loading each tenant's registries 
 * if there aren't any tasks there to be scheduled.
 */
public class RegistryTaskAvailabilityManager {

	private static final Log log = LogFactory.getLog(RegistryTaskAvailabilityManager.class);
	
	public static final String REG_TASK_AVAIL_MANAGE_BASE_PATH = "/repository/components/org.wso2.carbon.tasks/availability";
	
	private Registry registry;

	public RegistryTaskAvailabilityManager() throws TaskException {
		this.registry = TaskUtils.getGovRegistryForTenant(MultitenantConstants.SUPER_TENANT_ID);
	}
	
	public Registry getRegistry() {
		return registry;
	}
	
	private String generateTaskAvailablePath(int tenantId) {
		return REG_TASK_AVAIL_MANAGE_BASE_PATH + "/" + tenantId;
	}
	
	public boolean checkTasksAvailable(int tenantId) throws TaskException {
		try {
			boolean result = this.registry.resourceExists(
					this.generateTaskAvailablePath(tenantId));
			if (log.isDebugEnabled()) {
				log.debug("Checking tasks available for tenant: " + tenantId +
						", result: " + result);
			}
			return result;
		} catch (RegistryException e) {
			throw new TaskException("Error in checking tasks availability: " + e.getMessage(),
					Code.UNKNOWN, e);
		}
	}
	
	public void setTasksAvailable(int tenantId, boolean exists) throws TaskException {
		try {
			if (exists) {
			    this.getRegistry().put(this.generateTaskAvailablePath(tenantId), 
					    this.getRegistry().newResource());
			} else {
				this.getRegistry().delete(this.generateTaskAvailablePath(tenantId));
			}
			if (log.isDebugEnabled()) {
				log.debug("Setting tasks available for tenant: " + tenantId +
						", exists: " + exists);
			}
		} catch (Exception e) {
			throw new TaskException("Error in setting tasks availability: " + e.getMessage(),
					Code.UNKNOWN, e);
		}
	}
	
}
