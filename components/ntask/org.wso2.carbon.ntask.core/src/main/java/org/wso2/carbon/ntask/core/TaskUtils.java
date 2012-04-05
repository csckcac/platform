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
package org.wso2.carbon.ntask.core;

import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.common.TaskException.Code;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * This class contains utitilty functions related to tasks.
 */
public class TaskUtils {

	public static Registry getGovRegistryForTenant(int tid) throws TaskException {
		try {
			/* be super tenant to retrieve the registry of a given tenant id */
			SuperTenantCarbonContext.startTenantFlow();
			SuperTenantCarbonContext.getCurrentContext().setTenantId(
					MultitenantConstants.SUPER_TENANT_ID);
			return TasksDSComponent.getRegistryService().getGovernanceSystemRegistry(tid);
		} catch (RegistryException e) {
			throw new TaskException("Error in retrieving registry instance", Code.UNKNOWN, e);
		} finally {
			/* go out of being super tenant */
			SuperTenantCarbonContext.endTenantFlow();
		}
	}
	
}
