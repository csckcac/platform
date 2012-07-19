/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.humantask.core.store;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.bpel.common.ServiceConfigurationUtil;
import org.wso2.carbon.bpel.common.config.EndpointConfiguration;
import org.wso2.carbon.humantask.TNotification;
import org.wso2.carbon.humantask.TTask;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.deployment.DeploymentUtil;
import org.wso2.carbon.humantask.core.deployment.HumanTaskDeploymentException;
import org.wso2.carbon.humantask.core.deployment.HumanTaskDeploymentUnit;
import org.wso2.carbon.humantask.core.deployment.SimpleTaskDefinitionInfo;
import org.wso2.carbon.humantask.core.engine.HumanTaskEngine;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.integration.AxisHumanTaskMessageReceiver;
import org.wso2.carbon.humantask.core.integration.CallBackServiceImpl;
import org.wso2.carbon.humantask.core.integration.HumanTaskSchemaURIResolver;
import org.wso2.carbon.humantask.core.integration.HumanTaskWSDLLocator;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.utils.FileManipulator;
import org.wso2.carbon.utils.ServerConstants;

import javax.wsdl.Definition;
import javax.wsdl.OperationType;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Manages human task deployments for a tenant. There will be HumanTaskStore per tenant. Handles the deployment
 * of human tasks, and management of deployed tasks.
 */
public class HumanTaskStore {
    private static final Log log = LogFactory.getLog(HumanTaskStore.class);

    private int tenantId;

    private ConfigurationContext configContext;

    private List<HumanTaskBaseConfiguration> taskConfigurations =
            new ArrayList<HumanTaskBaseConfiguration>();

    private File humanTaskDeploymentRepo;

    public HumanTaskStore(int tenantId, ConfigurationContext configContext) {
        this.tenantId = tenantId;
        this.configContext = configContext;
    }

    public void deploy(HumanTaskDeploymentUnit humanTaskDU) throws HumanTaskDeploymentException {
        TTask[] tasks = humanTaskDU.getTasks();
        if (tasks != null) {
            for (TTask task : tasks) {
                QName taskQName = new QName(humanTaskDU.getNamespace(), task.getName());
                TaskConfiguration taskConf =
                        new TaskConfiguration(task,
                                humanTaskDU.getTaskServiceInfo(taskQName),
                                humanTaskDU.getHumanInteractionsDefinition(),
                                humanTaskDU.getWSDLs(),
                                humanTaskDU.getNamespace(),
                                humanTaskDU.getName(),
                                getTenantAxisConfig(),
                                humanTaskDU.getName(),
                                humanTaskDU.getHumanTaskDefinitionFile());
                taskConfigurations.add(taskConf);
                deploy(taskConf);
                createCallBackService(taskConf);
            }
        }

        TNotification[] notifications = humanTaskDU.getNotifications();
        if (notifications != null) {
            for (TNotification notification : notifications) {
                QName notificationQName = new QName(humanTaskDU.getNamespace(), notification.getName());
                NotificationConfiguration notificationConf =
                        new NotificationConfiguration(notification,
                                humanTaskDU.getNotificationServiceInfo(notificationQName),
                                humanTaskDU.getHumanInteractionsDefinition(),
                                humanTaskDU.getWSDLs(),
                                humanTaskDU.getNamespace(),
                                humanTaskDU.getName(),
                                getTenantAxisConfig(),
                                humanTaskDU.getName(),
                                humanTaskDU.getHumanTaskDefinitionFile());
                taskConfigurations.add(notificationConf);
                deploy(notificationConf);
            }
        }

        for (TNotification inlineNotification : humanTaskDU.getInlineNotifications()) {
            QName notificationQName = new QName(humanTaskDU.getNamespace(), inlineNotification.getName());
            NotificationConfiguration notificationConf =
                    new NotificationConfiguration(inlineNotification,
                            humanTaskDU.getNotificationServiceInfo(notificationQName),
                            humanTaskDU.getHumanInteractionsDefinition(),
                            humanTaskDU.getWSDLs(),
                            humanTaskDU.getNamespace(),
                            humanTaskDU.getName(),
                            getTenantAxisConfig(),
                            humanTaskDU.getName(),
                            humanTaskDU.getHumanTaskDefinitionFile());
            taskConfigurations.add(notificationConf);
        }
    }

    private void createCallBackService(TaskConfiguration taskConf)
            throws HumanTaskDeploymentException {
        EndpointConfiguration endpointConfig =
                taskConf.getEndpointConfiguration(taskConf.getCallbackServiceName().getLocalPart(),
                        taskConf.getCallbackPortName());
        CallBackServiceImpl callbackService = new CallBackServiceImpl(tenantId,
                taskConf.getCallbackServiceName(), taskConf.getCallbackPortName(),
                taskConf.getName(), taskConf.getResponseWSDL(), taskConf.getResponseOperation(),
                endpointConfig);
        taskConf.setCallBackService(callbackService);
    }

    private void deploy(HumanTaskBaseConfiguration taskConfig) throws HumanTaskDeploymentException {
        /**
         * Creating AxisService for HI
         */
        AxisService axisService;
        Definition wsdlDef = taskConfig.getWSDL();

        if (taskConfig instanceof TaskConfiguration) {
            //to get the task id as response
            wsdlDef.getPortType(taskConfig.getPortType()).getOperation(
                    taskConfig.getOperation(),
                    null, null).setStyle(OperationType.REQUEST_RESPONSE);
        } else {
            //ONE_WAY no feed back for NOTIFICATIONS
            wsdlDef.getPortType(taskConfig.getPortType()).getOperation(
                    taskConfig.getOperation(),
                    null, null).setStyle(OperationType.ONE_WAY);
        }

        WSDL11ToAxisServiceBuilder serviceBuilder = createAxisServiceBuilder(taskConfig, wsdlDef);

        try {
            axisService = createAxisService(serviceBuilder);
            ServiceConfigurationUtil.configureService(axisService,
                    taskConfig.getEndpointConfiguration(taskConfig.getServiceName().getLocalPart(),
                            taskConfig.getPortName()),
                    getConfigContext());
            ArrayList<AxisService> serviceList = new ArrayList<AxisService>();
            serviceList.add(axisService);
            DeploymentEngine.addServiceGroup(createServiceGroupForService(axisService), serviceList,
                    null, null, getTenantAxisConfig());
        } catch (AxisFault axisFault) {
            //Do not print stacktrace here since it will be printed in another level
            String errMsg = "Error populating the service";
            log.error(errMsg);
            //TODO rollback
//            rolebackRegistry(taskConfig.getName());
            throw new HumanTaskDeploymentException(errMsg, axisFault);
        }


    }

    //Creates the AxisServiceBuilder object.
    private WSDL11ToAxisServiceBuilder createAxisServiceBuilder(
            HumanTaskBaseConfiguration taskConfig, Definition wsdlDef) {
        WSDL11ToAxisServiceBuilder serviceBuilder =
                new WSDL11ToAxisServiceBuilder(wsdlDef,
                        taskConfig.getServiceName(), taskConfig.getPortName());
        String wsdlBaseURI = wsdlDef.getDocumentBaseURI();
        serviceBuilder.setBaseUri(wsdlBaseURI);
        /*we don't need custom resolvers since registry takes care of it*/
        serviceBuilder.setCustomResolver(new HumanTaskSchemaURIResolver());
        serviceBuilder.setCustomWSDLResolver(new HumanTaskWSDLLocator());
        serviceBuilder.setServerSide(true);
        return serviceBuilder;
    }

    //Creates the AxisService object from the provided ServiceBuilder object.
    private AxisService createAxisService(WSDL11ToAxisServiceBuilder serviceBuilder)
            throws AxisFault {
        AxisService axisService;
        axisService = serviceBuilder.populateService();
        axisService.setParent(getTenantAxisConfig());
        axisService.setWsdlFound(true);
        axisService.setCustomWsdl(true);
        //axisService.setFileName(new URL(taskConfig.getWsdlDefLocation()));
        axisService.setClassLoader(getTenantAxisConfig().getServiceClassLoader());
        Utils.setEndpointsToAllUsedBindings(axisService);
        axisService.addParameter(new Parameter("modifyUserWSDLPortAddress", "true"));

        /* Setting service type to use in service management*/
        axisService.addParameter(ServerConstants.SERVICE_TYPE, "humantask");

        /* Fix for losing of security configuration  when updating human-task package*/
        axisService.addParameter(new Parameter(CarbonConstants.PRESERVE_SERVICE_HISTORY_PARAM,
                "true"));

        Iterator operations = axisService.getOperations();
        AxisHumanTaskMessageReceiver msgReceiver = new AxisHumanTaskMessageReceiver();
        msgReceiver.setHumanTaskEngine(HumanTaskServiceComponent.getHumanTaskServer().
                getTaskEngine());

        while (operations.hasNext()) {
            AxisOperation operation = (AxisOperation) operations.next();
            // Setting Message Receiver even if operation has a message receiver specified.
            // This is to fix the issue when build service configuration using services.xml(Always RPCMessageReceiver
            // is set to operations).
            operation.setMessageReceiver(msgReceiver);
            getTenantAxisConfig().getPhasesInfo().setOperationPhases(operation);
        }
        return axisService;
    }

    private AxisServiceGroup createServiceGroupForService(AxisService svc) throws AxisFault {
        AxisServiceGroup svcGroup = new AxisServiceGroup();
        svcGroup.setServiceGroupName(svc.getName());
        svcGroup.addService(svc);
        svcGroup.addParameter(new Parameter(CarbonConstants.PRESERVE_SERVICE_HISTORY_PARAM, "true"));

        return svcGroup;
    }

    public HumanTaskBaseConfiguration getTaskConfiguration(QName portType, String operation) {
        for (HumanTaskBaseConfiguration taskConf : taskConfigurations) {
            if (taskConf.getPortType().equals(portType) && taskConf.getOperation().equals(operation)) {
                return taskConf;
            }
        }
        return null;
    }

    /**
     * @return : The tenant id of this task store.
     */
    public int getTenantId() {
        return tenantId;
    }

    /**
     * @return : The tenant axis configuration of this task store.
     */
    public AxisConfiguration getTenantAxisConfig() {
        return configContext.getAxisConfiguration();
    }

    /**
     * @return : The deployement repository location.
     */
    public File getHumanTaskDeploymentRepo() {
        return humanTaskDeploymentRepo;
    }

    /**
     * @param humanTaskDeploymentRepo : The deployment repository location to set.
     */
    public void setHumanTaskDeploymentRepo(File humanTaskDeploymentRepo) {
        this.humanTaskDeploymentRepo = humanTaskDeploymentRepo;
    }

    /**
     * @return : The list of task configurations in the store.
     */
    public List<HumanTaskBaseConfiguration> getTaskConfigurations() {
        return this.taskConfigurations;
    }

    /**
     * Gets the simple task definition information for a given package name.
     *
     * @param packageName : The package name.
     * @return : The matching package information list.
     */
    public List<SimpleTaskDefinitionInfo> getTaskConfigurationInfoListForPackage(
            String packageName) {
        List<SimpleTaskDefinitionInfo> matchingTaskDefinitions = new ArrayList<SimpleTaskDefinitionInfo>();
        for (HumanTaskBaseConfiguration taskBaseConfiguration : this.taskConfigurations) {
            if (taskBaseConfiguration.getPackageName().equals(packageName)) {
                matchingTaskDefinitions.add(DeploymentUtil.getSimpleTaskDefinitionInfo(taskBaseConfiguration));
            }
        }
        return matchingTaskDefinitions;
    }

    /**
     * Gets the list of SimpleTaskDefinitionInfo objects for the all the HumanTaskBaseConfiguration objects in this store.
     *
     * @return :
     */
    public List<SimpleTaskDefinitionInfo> getTaskConfigurationInfoList() {
        return DeploymentUtil.getTaskConfigurationsInfoList(this.getTaskConfigurations());
    }

    /**
     * UnDeploys a given HumanTask package.
     * Logic : 1. Make all the tasks OBSOLETE.
     * 2. Remove the task definition files from the tenant repo.
     *
     * @param packageName : The package name to be unDeployed.
     */
    public void unDeploy(String packageName) {
        boolean matchingPackageFound = removeMatchingPackageAfterTaskObsoletion(packageName);
        if (matchingPackageFound) {
            deleteHumanTaskPackageFromRepo(packageName);
        } else {
            throw new HumanTaskRuntimeException("There are no matching " +
                    "packages in the repository with name " + packageName);
        }
    }

    /**
     * Gets the HumanTaskBaseConfiguration object for the given task QName.
     *
     * @param taskName : The task name of which the task configuration is to be retrieved.
     * @return : The matching HumanTaskBaseConfiguration object.
     */
    public HumanTaskBaseConfiguration getTaskConfiguration(QName taskName) {
        HumanTaskBaseConfiguration matchingTaskConfiguration = null;
        for (HumanTaskBaseConfiguration taskConfig : taskConfigurations) {
            if (taskConfig.getName().equals(taskName)) {
                matchingTaskConfiguration = taskConfig;
                break;
            }
        }
        return matchingTaskConfiguration;
    }

    private boolean removeMatchingPackageAfterTaskObsoletion(String packageName) {
        final HumanTaskEngine taskEngine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
        boolean matchingPackagesFound = false;
        final int tId = this.tenantId;
        List<HumanTaskBaseConfiguration> matchingTaskConfigurations =
                new ArrayList<HumanTaskBaseConfiguration>();
        for (final HumanTaskBaseConfiguration configuration : this.getTaskConfigurations()) {
            if (configuration.getPackageName().equals(packageName)) {
                matchingTaskConfigurations.add(configuration);
                try {
                    taskEngine.getScheduler().execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            taskEngine.getDaoConnectionFactory().getConnection().obsoleteTasks(
                                    configuration.getName().toString(), tId);
                            return null;
                        }
                    });
                } catch (Exception e) {
                    String errMsg = "Error occurred while making tasks obsolete";
                    log.error(errMsg);
                    throw new HumanTaskRuntimeException(errMsg, e);
                }
                // we don't want the associated service once the task configuration is removed!
                removeAxisServiceForTaskConfiguration(configuration);
                matchingPackagesFound = true;
            }
        }

        // remove the task configurations with the matching package name.
        for (HumanTaskBaseConfiguration removableConfiguration : matchingTaskConfigurations) {
            this.getTaskConfigurations().remove(removableConfiguration);
        }

        return matchingPackagesFound;
    }

    /**
     * Remove the service associated with the task configuration.
     *
     * @param removableConfiguration : The task configuration.
     */
    public void removeAxisServiceForTaskConfiguration(
            HumanTaskBaseConfiguration removableConfiguration) {
        try {
            //If there are matching axis services we remove them.
            if (removableConfiguration.getServiceName() != null &&
                    StringUtils.isNotEmpty(removableConfiguration.getServiceName().getLocalPart())) {
                String axisServiceName = removableConfiguration.getServiceName().getLocalPart();
                AxisService axisService = getTenantAxisConfig().getService(axisServiceName);
                if (axisService != null) {
                    axisService.releaseSchemaList();
                    getTenantAxisConfig().stopService(axisServiceName);
                    getTenantAxisConfig().removeServiceGroup(axisServiceName);
                } else {
                    log.warn("Could not find matching AxisService in " +
                            "Tenant AxisConfiguration for service name :" + axisServiceName);
                }
            } else {
                log.warn(String.format("Could not find a associated service name for " +
                        "[%s] configuration [%s]",
                        removableConfiguration.getConfigurationType(),
                        removableConfiguration.getName().toString()));
            }
        } catch (AxisFault axisFault) {
            String error = "Error occurred while removing the axis service " +
                    removableConfiguration.getServiceName();

            log.error(error);
            throw new HumanTaskRuntimeException(error, axisFault);
        }
    }

    /**
     * deletes the human task package archive from the repository.
     *
     * @param packageName : The package name to be deleted.
     */
    public void deleteHumanTaskArchive(String packageName) {
        File humanTaskArchive = getHumanTaskArchiveLocation(packageName);
        log.info("UnDeploying HumanTask package " + packageName + ". Deleting HumanTask archive " +
                humanTaskArchive.getName() + "....");
        if (humanTaskArchive.exists()) {
            if (!humanTaskArchive.delete()) {
                //For windows
                humanTaskArchive.deleteOnExit();
            }
        } else {
            log.warn("HumanTask archive [" + humanTaskArchive.getAbsolutePath() +
                    "] not found. This can happen if you delete " +
                    "the HumanTask archive from the file system.");
        }
    }

    /**
     * Return the human task archive file for the given package name.
     *
     * @param packageName : The human task archive package name.
     * @return : The matching human task archive file.
     */
    public File getHumanTaskArchiveLocation(String packageName) {
        String humanTaskArciveLocation = getTenantAxisConfig().getRepository().
                getPath() + HumanTaskConstants.HUMANTASK_REPO_DIRECTORY + File.separator +
                packageName + "." + HumanTaskConstants.HUMANTASK_PACKAGE_EXTENSION;
        return new File(humanTaskArciveLocation);
    }

    public ConfigurationContext getConfigContext() {
        return configContext;
    }

    //removes the package from the human task package repository.
    private void deleteHumanTaskPackageFromRepo(String packageName) {

        String humanTaskPackageLocation = this.humanTaskDeploymentRepo.getAbsolutePath() +
                File.separator + tenantId + File.separator +
                packageName;
        File humanTaskPackageDirectory = new File(humanTaskPackageLocation);

        log.info("UnDeploying HumanTask package. " + "Deleting " + humanTaskPackageDirectory + " HumanTask package");

        if (humanTaskPackageDirectory.exists()) {
            FileManipulator.deleteDir(humanTaskPackageDirectory);
        } else {
            log.warn("HumanTask package " + humanTaskPackageDirectory.getAbsolutePath() +
                    " not found. This can happen if you delete " +
                    "the HumanTask package from the file system.");
        }
    }
}
