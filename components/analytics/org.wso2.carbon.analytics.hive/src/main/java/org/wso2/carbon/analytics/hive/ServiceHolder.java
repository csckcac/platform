/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.hive;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.hive.conf.HiveConnectionManager;
import org.wso2.carbon.analytics.hive.service.HiveExecutorService;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.datasource.DataSourceInformationRepositoryService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;

public class ServiceHolder {

    private static final Log log = LogFactory.getLog(ServiceHolder.class);

    private static HiveExecutorService hiveExecutorService;
    private static RegistryService registryService;
    private static ConfigurationContextService configurationContextService;
    private static ServerConfiguration serverConfiguration;
    private static TaskService taskService;
    private static DataSourceInformationRepositoryService dataSourceInfoService;
    private static HiveConnectionManager connectionManager;
    private static DataSourceService dataSourceService;

    public static void setHiveExecutorService(HiveExecutorService service) {
        hiveExecutorService = service;
    }

    public static HiveExecutorService getHiveExecutorService() {
        return hiveExecutorService;
    }

    public static void setRegistryService(RegistryService service) {
        registryService = service;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        ServiceHolder.configurationContextService = configurationContextService;
    }

    public static TaskService getTaskService() {
        return taskService;
    }

    public static void setTaskService(TaskService taskService) {
        ServiceHolder.taskService = taskService;
    }

    public static void setDataSourceInformationRepositoryService(
            DataSourceInformationRepositoryService dataSourceInfoService) {
        ServiceHolder.dataSourceInfoService = dataSourceInfoService;
    }

    public static DataSourceInformationRepositoryService getDataSourceInformationRepositoryService() {
        return ServiceHolder.dataSourceInfoService;
    }

    public static TaskManager getTaskManager() {
        TaskService taskService = ServiceHolder.getTaskService();
        try {
            taskService.registerTaskType(HiveConstants.HIVE_TASK);
            return taskService.getTaskManager(HiveConstants.HIVE_TASK);
        } catch (TaskException e) {
            log.error("Error while initializing TaskManager. Script scheduling may not" +
                    " work properly..", e);
            return null;
        }

    }

    public static void setHiveConnectionManager(HiveConnectionManager connectionManager) {
        ServiceHolder.connectionManager = connectionManager;
    }

    public static HiveConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public static void setCarbonConfiguration(ServerConfiguration configurationService) {
        ServiceHolder.serverConfiguration = configurationService;
    }

    public static ServerConfiguration getCarbonConfiguration() {
        return serverConfiguration;
    }

    public static void setCarbonDataSourceService(DataSourceService dataSourceService) {
        ServiceHolder.dataSourceService = dataSourceService;
    }

    public static DataSourceService getCarbonDataSourceService() {
        return dataSourceService;
    }

}
