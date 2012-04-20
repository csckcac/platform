/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.mgt.services;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.humantask.core.HumanTaskServer;
import org.wso2.carbon.humantask.core.deployment.HumanTaskDeploymentException;
import org.wso2.carbon.humantask.core.deployment.SimpleTaskDefinitionInfo;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.store.HumanTaskStore;
import org.wso2.carbon.humantask.skeleton.mgt.services.HumanTaskPackageManagementSkeletonInterface;
import org.wso2.carbon.humantask.skeleton.mgt.services.PackageManagementException;
import org.wso2.carbon.humantask.skeleton.mgt.services.types.DeployedPackagesPaginated;
import org.wso2.carbon.humantask.skeleton.mgt.services.types.DeployedTaskDefinitionsPaginated;
import org.wso2.carbon.humantask.skeleton.mgt.services.types.TaskDefinition_type0;
import org.wso2.carbon.humantask.skeleton.mgt.services.types.TaskStatusType;
import org.wso2.carbon.humantask.skeleton.mgt.services.types.TaskType;
import org.wso2.carbon.humantask.skeleton.mgt.services.types.Task_type0;
import org.wso2.carbon.humantask.skeleton.mgt.services.types.UndeployStatus_type0;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.List;

/**
 * The human task package management service skeleton.
 */
public class HumanTaskPackageManagementSkeleton extends AbstractAdmin
        implements HumanTaskPackageManagementSkeletonInterface {

    private static Log log = LogFactory.getLog(HumanTaskPackageManagementSkeleton.class);

    /**
     * @param page : The page number.
     * @return :
     */
    @Override
    public DeployedPackagesPaginated listDeployedPackagesPaginated(int page) {
        return null;
    }


    /**
     * Lists the tasks in the given package name.
     *
     * @param packageName : The name of the package to list task definitions.
     * @return : The Task_type0 array containing the task definition information.
     */
    @Override
    public Task_type0[] listTasksInPackage(String packageName) throws PackageManagementException {
        if (StringUtils.isEmpty(packageName)) {
            throw new IllegalArgumentException("The provided package name is empty!");
        }
        try {
        List<SimpleTaskDefinitionInfo> taskDefsInPackage =
                getTenantTaskStore().getTaskConfigurationInfoListForPackage(packageName);
        Task_type0[] taskDefArray = new Task_type0[taskDefsInPackage.size()];
        int i = 0;
        for (SimpleTaskDefinitionInfo taskDefinitionInfo : taskDefsInPackage) {
            taskDefArray[i] = createTaskTypeObject(taskDefinitionInfo);
            i++;
        }

        return taskDefArray;
        } catch (Exception ex) {
            String errMsg = "listTasksInPackage operation failed";
            log.error(errMsg, ex);
            throw new PackageManagementException(errMsg, ex);
        }
    }

    @Override
    public DeployedTaskDefinitionsPaginated listDeployedTaskDefinitionsPaginated(int page)
            throws PackageManagementException {
        int tPage = page;
        try {
            DeployedTaskDefinitionsPaginated paginatedTaskDefs = new DeployedTaskDefinitionsPaginated();

            if (tPage < 0 || tPage == Integer.MAX_VALUE) {
                tPage = 0;
            }

            Integer ITEMS_PER_PAGE = 10;
            Integer startIndexForCurrentPage = tPage * ITEMS_PER_PAGE;
            Integer endIndexForCurrentPage = (tPage + 1) * ITEMS_PER_PAGE;


            List<SimpleTaskDefinitionInfo> taskConfigs = getTenantTaskStore().getTaskConfigurationInfoList();

            Integer taskDefListSize = taskConfigs.size();
            Integer pages = (int) Math.ceil((double) taskDefListSize / ITEMS_PER_PAGE);
            paginatedTaskDefs.setPages(pages);

            SimpleTaskDefinitionInfo[] taskDefinitionInfoArray =
                    taskConfigs.toArray(new SimpleTaskDefinitionInfo[taskDefListSize]);

            for (int i = startIndexForCurrentPage;
                 (i < endIndexForCurrentPage && i < taskDefListSize); i++) {
                paginatedTaskDefs.addTaskDefinition(createTaskDefObject(taskDefinitionInfoArray[i]));
            }

            return paginatedTaskDefs;
        } catch (Exception ex) {
            String errMsg = "listDeployedTaskDefinitionsPaginated operation failed";
            log.error(errMsg, ex);
            throw new PackageManagementException(errMsg, ex);
        }
    }

    private TaskDefinition_type0 createTaskDefObject(
            SimpleTaskDefinitionInfo taskConfiguration) {
        TaskDefinition_type0 taskDef = new TaskDefinition_type0();

        taskDef.setPackageName(taskConfiguration.getPackageName());
        taskDef.setTaskName(taskConfiguration.getTaskName());
        taskDef.setState(TaskStatusType.ACTIVE);
        if (org.wso2.carbon.humantask.core.dao.TaskType.TASK.equals(
                taskConfiguration.getTaskType())) {
            taskDef.setType(TaskType.TASK);
        } else if (org.wso2.carbon.humantask.core.dao.TaskType.NOTIFICATION.equals(
                taskConfiguration.getTaskType())) {
            taskDef.setType(TaskType.NOTIFICATION);
        }
        return taskDef;
    }

    private Task_type0 createTaskTypeObject(SimpleTaskDefinitionInfo taskConfiguration) {

        Task_type0 task = new Task_type0();
        task.setName(taskConfiguration.getTaskName());
        if (org.wso2.carbon.humantask.core.dao.TaskType.TASK.equals(taskConfiguration.getTaskType())) {
            task.setType(TaskType.TASK);
        } else if (org.wso2.carbon.humantask.core.dao.TaskType.NOTIFICATION.equals(
                taskConfiguration.getTaskType())) {
            task.setType(TaskType.NOTIFICATION);
        }

        return task;
    }

    @Override
    public UndeployStatus_type0 undeployHumanTaskPackage(String packageName) {
        try {
            // We will only delete the zip file. The HumanTaskDeployer's undeploy method will
            // be executed. The un-deployement logic is written there.
            getTenantTaskStore().deleteHumanTaskArchive(packageName);
        } catch (Exception ex) {
            log.error("undeployHumanTaskPackage operation failed", ex);
            return UndeployStatus_type0.FAILED;
        }
        return UndeployStatus_type0.SUCCESS;
    }

    // Returns the task store for the tenant.
    private HumanTaskStore getTenantTaskStore() {
        ConfigurationContext configContext = getConfigContext();
        Integer tenantId = MultitenantUtils.getTenantId(configContext);
        HumanTaskServer server = HumanTaskServiceComponent.getHumanTaskServer();
        return server.getTaskStoreManager().getHumanTaskStore(tenantId);
    }
}
