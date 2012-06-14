/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mashup.javascript.hostobjects.system.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.mashup.javascript.hostobjects.system.MSTaskConstants;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="mashup.javascript.hostobjects.system.dscomponent" immediate="true"
 * @scr.reference name="carbon.core.configurationContextService"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"
 * bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="ntask.component" interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService" unbind="unsetTaskService"
 * @scr.reference name="tenant.registryloader"
 * interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 * cardinality="1..1" policy="dynamic" bind="setTenantRegistryLoader"
 * unbind="unsetTenantRegistryLoader"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 **/
public class SystemHostObjectServiceComponent {

    private static final Log log = LogFactory.getLog(SystemHostObjectServiceComponent.class);

    private ConfigurationContext configurationContext = null;
    
    private static TaskService taskService;
    
    private static RegistryService registryService;
    
    private static TenantRegistryLoader tenantRegistryLoader;

    protected void activate(ComponentContext context) {

        try {
            /* register the data service task type */
            getTaskService().registerTaskType(MSTaskConstants.MS_TASK_TYPE);
            if (log.isDebugEnabled()) {
                log.debug("Data Services task bundle is activated ");
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            /* don't throw exception */
        }
    }

    protected void deactivate(ComponentContext ctxt) {
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        this.configurationContext = configurationContextService.getServerConfigContext();
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        this.configurationContext = null;
    }
    
    protected void setTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Task Service");
        }
        SystemHostObjectServiceComponent.taskService = taskService;
    }

    protected void unsetTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Task Service");
        }
        SystemHostObjectServiceComponent.taskService = null;
    }

    public static TaskService getTaskService() {
        return SystemHostObjectServiceComponent.taskService;
    }
    
    protected void setRegistryService(RegistryService registryService) {
    	SystemHostObjectServiceComponent.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {
    	SystemHostObjectServiceComponent.registryService = null;
    }
    
    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        SystemHostObjectServiceComponent.tenantRegistryLoader = tenantRegistryLoader;
    }

    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
    	SystemHostObjectServiceComponent.tenantRegistryLoader = null;
    }

}
