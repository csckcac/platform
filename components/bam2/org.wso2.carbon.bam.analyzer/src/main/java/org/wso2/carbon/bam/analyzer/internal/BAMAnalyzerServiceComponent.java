/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.analyzer.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bam.analyzer.Utils;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerEngine;
import org.wso2.carbon.bam.core.persistence.IndexManager;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * @scr.component name="bam.analyzer.component" immediate="true"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="cassandra.service" interface="org.wso2.carbon.cassandra.dataaccess.DataAccessService"
 * cardinality="1..1" policy="dynamic" bind="setDataAccessService" unbind="unsetDataAccessService"
 * @scr.reference name="tenant.registryloader"
 * interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 * cardinality="1..1" policy="dynamic" bind="setTenantRegistryLoader"
 * unbind="unsetTenantRegistryLoader"
 * @scr.reference name="ntask.component" interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService" unbind="unsetTaskService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="0..1" policy="dynamic"
 * bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class BAMAnalyzerServiceComponent {

    private static Log log = LogFactory.getLog(BAMAnalyzerServiceComponent.class);

    public static final String BAM_ANALYSER_TASK = "BAM_ANALYSER_TASK";
    
    protected void activate(ComponentContext ctx) {
        try {
            /* Registering task type for BAM Analyzer task. This particular type of tasks then later
             * be filtered out using the given type name. */
            SuperTenantCarbonContext.startTenantFlow();
            SuperTenantCarbonContext.getCurrentContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            TaskService taskService = Utils.getTaskService();
            if (taskService != null) {
                taskService.registerTaskType(BAMAnalyzerServiceComponent.BAM_ANALYSER_TASK);
                TaskManager taskManager =
                        taskService.getTaskManager(BAMAnalyzerServiceComponent.BAM_ANALYSER_TASK);

                AnalyzerEngine analyzerEngine = new AnalyzerEngine(taskManager);
                Utils.setEngine(analyzerEngine);
                IndexManager.getInstance().registerIndexingTaskProvider(analyzerEngine);


//            String analyzerFilePath = CarbonUtils.getCarbonConfigDirPath() +
//                                      File.separator + AnalyzerConfigConstants.ANALYZER_FILE_NAME;
//            InputStream analyzerStream = new FileInputStream(analyzerFilePath);
//
            } else {
                log.error("Task service is null");
            }

            if (log.isDebugEnabled()) {
                log.debug("BAM analyzer bundle is activated");
            }
        } catch (Throwable e) {
            log.error("BAM analyzer bundle cannot be started", e);
        } finally {
            SuperTenantCarbonContext.endTenantFlow();
        }

    }

    protected void deactivate(ComponentContext ctx) {
        //Utils.getEngine().shutdown();
        Utils.setEngine(null);
        if (log.isDebugEnabled()) {
            log.debug("BAM analyzer bundle is deactivated");
        }
    }

    protected void setDataAccessService(DataAccessService dataAccessService) {
        Utils.setDataAccessService(dataAccessService);
    }

    protected void unsetDataAccessService(DataAccessService dataAccessService) {
        Utils.setDataAccessService(null);
    }

    protected void setRegistryService(RegistryService registryService) throws RegistryException {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService set in BAM bundle");
        }
        Utils.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        Utils.setRegistryService(null);
        if (log.isDebugEnabled()) {
            log.debug("RegistryService unset in BAM bundle");
        }
    }

    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        Utils.setTenantRegistryLoader(tenantRegistryLoader);
        if (log.isDebugEnabled()) {
            log.debug("TenantRegistryLoader set in BAM bundle");
        }
    }

    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        Utils.setTenantRegistryLoader(null);
        if (log.isDebugEnabled()) {
            log.debug("TenantRegistryLoader unset in BAM bundle");
        }
    }

    protected void setTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Task Service");
        }
        Utils.setTaskService(taskService);
    }

    protected void unsetTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Task Service");
        }
        Utils.setTaskService(null);
    }

    protected void setConfigurationContextService(ConfigurationContextService configCtxService) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService is set in BAM bundle");
        }
        Utils.setConfigurationContextService(configCtxService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configCtxService) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService is un-set in BAM bundle");
        }
        Utils.setConfigurationContextService(null);
    }


}
