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
package org.wso2.carbon.bam.receiver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.receiver.internal.EventQueue;
import org.wso2.carbon.bam.receiver.persistence.PersistenceManager;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

public class ReceiverUtils {

    private static final Log log = LogFactory.getLog(ReceiverUtils.class);

    private static Registry registry;
    private static ConfigurationContextService configurationContextService;
    private static RealmService realmService;
    private static DataAccessService dataAccessService;

    private static EventQueue queue;

    private static PersistenceManager persistentManager;

    private static ServerConfiguration serverConfiguration;

    public static EventQueue getQueue() {
        return queue;
    }

    public static void setQueue(EventQueue queue) {
        ReceiverUtils.queue = queue;
    }

    public static DataAccessService getDataAccessService() {
        return dataAccessService;
    }

    public static void setDataAccessService(DataAccessService dataAccessService) {
        ReceiverUtils.dataAccessService = dataAccessService;
    }

    public static Registry getRegistry() {
        return registry;
    }

    public static void setRegistry(Registry registry) {
        ReceiverUtils.registry = registry;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        ReceiverUtils.configurationContextService = configurationContextService;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    public static void setRealmService(RealmService realmService) {
        ReceiverUtils.realmService = realmService;
    }

    public static void setPersistentManager(PersistenceManager persistentManager) {
        ReceiverUtils.persistentManager = persistentManager;
    }

    public static PersistenceManager getPersistentManager() {
        return persistentManager;
    }

    public static void setCarbonConfiguration(ServerConfiguration serverConfiguration) {
        ReceiverUtils.serverConfiguration = serverConfiguration;
    }

    public static ServerConfiguration getCarbonConfiguration() {
        return serverConfiguration;
    }
    
}
