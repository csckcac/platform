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
import org.apache.synapse.task.TaskConstants;
import org.apache.synapse.task.service.TaskManagementService;
import org.apache.synapse.task.TaskScheduler;
import org.osgi.service.component.ComponentContext;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.mashup.javascript.hostobjects.system.multitenancy.SystemHostObjectInitializer;
import org.wso2.carbon.task.services.JobMetaDataProviderService;
import org.wso2.carbon.task.services.TaskDescriptionRepositoryService;
import org.wso2.carbon.task.services.TaskSchedulerService;
import org.wso2.carbon.mashup.javascript.hostobjects.system.FunctionSchedulingAdminService;
import org.wso2.carbon.mashup.javascript.hostobjects.system.FunctionSchedulingJobMetaDataProviderService;
import org.wso2.carbon.mashup.javascript.hostobjects.system.FunctionSchedulingManager;
import org.wso2.carbon.mashup.javascript.hostobjects.system.service.SystemHostObjectService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.Properties;

/**
 * @scr.component name="mashup.javascript.hostobjects.system.dscomponent" immediate="true"
 * @scr.reference name="carbon.core.configurationContextService"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"
 * bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class SystemHostObjectServiceComponent {

    private static final Log log = LogFactory.getLog(SystemHostObjectServiceComponent.class);

    private ConfigurationContext configurationContext = null;

    protected void activate(ComponentContext context) {

        BundleContext bundleContext = context.getBundleContext();
        if (log.isDebugEnabled()) {
            log.debug("Initiating SystemHostObject service components");
        }
        bundleContext.registerService(TaskManagementService.class.getName(),
                new FunctionSchedulingAdminService(), null);
        bundleContext.registerService(JobMetaDataProviderService.class.getName(),
                new FunctionSchedulingJobMetaDataProviderService(), null);
        bundleContext.registerService(SystemHostObjectService.class.getName(),
                new SystemHostObjectService(), null);

        SystemHostObjectInitializer listener = new SystemHostObjectInitializer();
        bundleContext.registerService(
                Axis2ConfigurationContextObserver.class.getName(), listener, null);
        if (configurationContext != null) {
            TaskScheduler scheduler = (TaskScheduler) configurationContext.getProperty(SystemHostObjectInitializer.CARBON_TASK_SCHEDULER);
            if (scheduler == null) {
                scheduler = new TaskScheduler(TaskConstants.TASK_SCHEDULER);
                scheduler.init(null);
                configurationContext.setProperty(SystemHostObjectInitializer.CARBON_TASK_SCHEDULER, scheduler);
            } else if (!scheduler.isInitialized()) {
                scheduler.init(null);
            }
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
}
