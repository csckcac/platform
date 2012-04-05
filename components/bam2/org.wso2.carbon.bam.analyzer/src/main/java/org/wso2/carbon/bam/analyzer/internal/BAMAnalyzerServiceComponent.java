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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.task.TaskDescriptionRepository;
import org.apache.synapse.task.TaskScheduler;
import org.apache.synapse.task.service.TaskManagementService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bam.analyzer.Utils;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerEngine;
import org.wso2.carbon.bam.analyzer.task.BAMAnalyzerJobMetaDataProvider;
import org.wso2.carbon.bam.analyzer.task.BAMAnalyzerTaskMgmtService;
import org.wso2.carbon.bam.core.persistence.IndexManager;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.task.JobMetaDataProviderServiceHandler;
import org.wso2.carbon.task.TaskManagementServiceHandler;
import org.wso2.carbon.task.TaskManager;
import org.wso2.carbon.task.services.JobMetaDataProviderService;
import org.wso2.carbon.task.services.TaskDescriptionRepositoryService;
import org.wso2.carbon.utils.ConfigurationContextService;


/**
 * @scr.component name="bam.analyzer.component" immediate="true"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="cassandra.service" interface="org.wso2.carbon.cassandra.dataaccess.DataAccessService"
 * cardinality="1..1" policy="dynamic" bind="setDataAccessService" unbind="unsetDataAccessService"
 * @scr.reference name="job.metadata.provider.service" interface="org.wso2.carbon.task.services.JobMetaDataProviderService"
 * cardinality="1..1" policy="dynamic" bind="setJobMetaDataProviderService" unbind="unsetJobMetaDataProviderService"
 * @scr.reference name="task.management.service" interface="org.apache.synapse.task.service.TaskManagementService"
 * cardinality="1..1" policy="dynamic" bind="setTaskManagementService" unbind="unsetTaskManagementService"*
 * @scr.reference name="task.description.repository.service"
 * interface="org.wso2.carbon.task.services.TaskDescriptionRepositoryService"
 * cardinality="1..1" policy="dynamic" bind="setTaskDescriptionRepositoryService"
 * unbind="unsetTaskDescriptionRepositoryService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"
 * bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="tenant.registryloader"
 * interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 * cardinality="1..1" policy="dynamic" bind="setTenantRegistryLoader"
 * unbind="unsetTenantRegistryLoader"
 */
public class BAMAnalyzerServiceComponent {


    private static Log log = LogFactory.getLog(BAMAnalyzerServiceComponent.class);

    private static ConfigurationContextService configurationContextService;
    private static final JobMetaDataProviderServiceHandler jobMetaDataProviderServiceHandler =
            new JobMetaDataProviderServiceHandler();
    private static final TaskManagementServiceHandler taskManagementServiceHandler =
            new TaskManagementServiceHandler();
    private static TaskDescriptionRepositoryService repositoryService;
    private static TaskManagementService taskMgtService;
    private static TenantRegistryLoader tenantRegistryLoader;

    protected void activate(ComponentContext ctx) {
        try {


            BundleContext bundleContext = ctx.getBundleContext();
            bundleContext.registerService(JobMetaDataProviderService.class.getName(),
                                          new BAMAnalyzerJobMetaDataProvider(), null);

            configurationContextService = Utils.getConfigurationContextService();

            taskMgtService = new BAMAnalyzerTaskMgmtService(Utils.getConfigurationContextService()
                    .getServerConfigContext());
            bundleContext.registerService(TaskManagementService.class.getName(),
                                          taskMgtService, null);
//            bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(),
//                                          new TaskSchedulerInitializer(), null);

            /*
            * Adding the services to the jobMetaDataProviderServiceHandler which implement the
            * JobMetaDataProviderService interface, available in the current bundleContext.
            */
            jobMetaDataProviderServiceHandler.addService(bundleContext.getService(
                    bundleContext.getServiceReference(JobMetaDataProviderService.class.getName())));

            /*
            * Adding the services to the taskManagementServiceHandler which implement the
            * TaskManagementService interface, available in the current bundleContext.
            */
            taskManagementServiceHandler.addService(bundleContext.getService(
                    bundleContext.getServiceReference(TaskManagementService.class.getName())));

            getConfigurationContext().setProperty(
                    TaskManager.CARBON_TASK_JOB_METADATA_SERVICE, jobMetaDataProviderServiceHandler);
            getConfigurationContext().setProperty(
                    TaskManager.CARBON_TASK_MANAGEMENT_SERVICE, taskManagementServiceHandler);

            // initialize the task manager
            TaskManager taskManager = new TaskManager();
            taskManager.setTaskDescriptionRepository(
                    repositoryService.getTaskDescriptionRepository());

            taskManager.init(jobMetaDataProviderServiceHandler, taskManagementServiceHandler);

            this.configurationContextService.getServerConfigContext().setProperty(TaskManager.CARBON_TASK_MANAGER,
                                                                                  taskManager);
            this.configurationContextService.getServerConfigContext().setProperty(TaskManager.CARBON_TASK_REPOSITORY,
                                                                                  repositoryService.getTaskDescriptionRepository());

            this.configurationContextService.getServerConfigContext().setProperty(
                    TaskManager.CARBON_TASK_JOB_METADATA_SERVICE,
                    jobMetaDataProviderServiceHandler);

            this.configurationContextService.getServerConfigContext().setProperty(
                    TaskManager.CARBON_TASK_MANAGEMENT_SERVICE,
                    taskManagementServiceHandler);


            /*
            * In this particular context, the TaskDescriptionRepository class of the
            * Carbon Scheduled Tasks Component is used.
            * Since the Scheduled Tasks Component itself does not initialize a
            * TaskDescriptionRepository instance,this code snippet is used to initialize a
            * the required instance manually.
            */
            TaskDescriptionRepository taskDescriptionRepository = (TaskDescriptionRepository)
                    BAMAnalyzerServiceComponent.getConfigurationContext().getProperty(TaskManager.CARBON_TASK_REPOSITORY);
            if (taskDescriptionRepository == null) {
                taskDescriptionRepository = new TaskDescriptionRepository();
                BAMAnalyzerServiceComponent.getConfigurationContext().setProperty(
                        TaskManager.CARBON_TASK_REPOSITORY, taskDescriptionRepository);
            }

            /*
            * In this particular context, the TaskScheduler class of the
            * Carbon Scheduled Tasks Component is used.
            * Since the Scheduled Tasks Component itself does not initialize a
            * TaskScheduler instance,this code snippet is used to initialize a
            * the required instance manually.
            */
            TaskScheduler taskScheduler = (TaskScheduler) BAMAnalyzerServiceComponent.getConfigurationContext().getProperty(
                    TaskManager.CARBON_TASK_SCHEDULER);
            if (taskScheduler == null) {
                taskScheduler = new TaskScheduler(TaskManager.CARBON_TASK_SCHEDULER);
                taskScheduler.init(null);
                BAMAnalyzerServiceComponent.getConfigurationContext().setProperty(
                        TaskManager.CARBON_TASK_SCHEDULER, taskScheduler);
            } else if (taskScheduler.isInitialized()) {
                taskScheduler.init(null);
            }

//            String analyzerFilePath = CarbonUtils.getCarbonConfigDirPath() +
//                                      File.separator + AnalyzerConfigConstants.ANALYZER_FILE_NAME;
//            InputStream analyzerStream = new FileInputStream(analyzerFilePath);
            AnalyzerEngine analyzerEngine = new AnalyzerEngine(taskMgtService);
            Utils.setEngine(analyzerEngine);

            IndexManager.getInstance().registerIndexingTaskProvider(analyzerEngine);

            if (log.isDebugEnabled()) {
                log.debug("BAM analyzer bundle is activated");
            }
        } catch (Throwable e) {
            log.error("BAM analyzer bundle cannot be started", e);
        }

    }

    public static ConfigurationContext getConfigurationContext() {
        return configurationContextService.getServerConfigContext();
    }

    protected void deactivate(ComponentContext ctx) {
        Utils.getEngine().shutdown();
        Utils.setEngine(null);
        if (log.isDebugEnabled()) {
            log.debug("BAM analyzer bundle is deactivated");
        }
    }

    protected static JobMetaDataProviderServiceHandler getJobMetaDataProviderServiceHandler() {
        return jobMetaDataProviderServiceHandler;
    }

    protected void setJobMetaDataProviderService(
            JobMetaDataProviderService jobMetaDataProviderService) {
        if (log.isDebugEnabled()) {
            log.debug("Adding a JobMetaDataProviderService");
        }
        this.jobMetaDataProviderServiceHandler.addService(jobMetaDataProviderService);
    }

    protected void unsetJobMetaDataProviderService(
            JobMetaDataProviderService jobMetaDataProviderService) {

        this.jobMetaDataProviderServiceHandler.removeService(jobMetaDataProviderService);
    }

    protected static TaskManagementServiceHandler getTaskManagementServiceHandler() {
        return taskManagementServiceHandler;
    }

    protected void setTaskManagementService(
            TaskManagementService taskManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Adding a TaskManagementService");
        }
        this.taskManagementServiceHandler.addService(taskManagementService);
    }

    protected void unsetTaskManagementService(
            TaskManagementService taskManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing a TaskManagementService");
        }
        this.taskManagementServiceHandler.removeService(taskManagementService);
    }

    protected static TaskDescriptionRepositoryService getTaskDescriptionRepositoryService() {
        return repositoryService;
    }

    protected void setTaskDescriptionRepositoryService(
            TaskDescriptionRepositoryService repositoryService) {
        if (log.isDebugEnabled()) {
            log.debug("TaskDescriptionRepositoryService  bound to the ESB initialization process");
        }
        this.repositoryService = repositoryService;
    }

    protected void unsetTaskDescriptionRepositoryService(
            TaskDescriptionRepositoryService repositoryService) {
        if (log.isDebugEnabled()) {
            log.debug("TaskDescriptionRepositoryService  unbound from the ESB environment");
        }
        this.repositoryService = null;
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService bound to the ESB initialization process");
        }
        Utils.setConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService unbound from the ESB environment");
        }
        Utils.setConfigurationContextService(null);
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

}
