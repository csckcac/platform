/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.wso2.carbon.bam.core.persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.common.clients.BAMConfigurationDSClient;
import org.wso2.carbon.bam.common.dataobjects.activity.ActivityDO;
import org.wso2.carbon.bam.common.dataobjects.activity.MessageDO;
import org.wso2.carbon.bam.common.dataobjects.activity.NamespaceDO;
import org.wso2.carbon.bam.common.dataobjects.activity.PropertyFilterDO;
import org.wso2.carbon.bam.common.dataobjects.common.ClientDO;
import org.wso2.carbon.bam.common.dataobjects.mediation.MediationDataDO;
import org.wso2.carbon.bam.common.dataobjects.service.OperationDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServiceDO;
import org.wso2.carbon.bam.core.admin.BAMGlobalConfigAdmin;
import org.wso2.carbon.bam.core.admin.BAMTenantAdmin;
import org.wso2.carbon.bam.core.cache.CacheConstant;
import org.wso2.carbon.bam.core.cache.CacheData;
import org.wso2.carbon.bam.core.util.BAMConfigurationCache;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.bam.util.BAMCalendar;
import org.wso2.carbon.bam.util.BAMConstants;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.bam.util.TimeRange;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.registry.core.Registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Provides data services and registry access services wrapping suitable clients and admin service classes.
 */
public class BAMPersistenceManager {

    private static final Log log = LogFactory.getLog(BAMPersistenceManager.class);
    private static Registry registry;
    private static BAMPersistenceManager persistenceManager;
    static final int NO_TENANT_MODE = -1;  // This is when no tenant is set in carbon context

    private BAMPersistenceManager(Registry registry) {
        BAMPersistenceManager.registry = registry;
    }

    public static BAMPersistenceManager getInstance() {
        return getPersistenceManager(BAMUtil.getRegistry());
    }

    public static BAMPersistenceManager getPersistenceManager(Registry registry) {
        if (persistenceManager == null) {
            synchronized (BAMPersistenceManager.class) {
                if (persistenceManager == null) {
                    if (registry == null) {
                        registry = BAMUtil.getRegistry();
                    }
                    persistenceManager = new BAMPersistenceManager(registry);
                }
            }
        }
        return persistenceManager;
    }

    public TimeRange getDataRetentionPeriod() throws BAMException {
        TimeRange retention;

        BAMGlobalConfigAdmin configAdmin = new BAMGlobalConfigAdmin(getRegistry());
        try {
            retention = configAdmin.getDataRetentionPeriod();
        } catch (BAMException e) {
            throw new BAMException("Error occurred getting data retention period", e);
        }

        // If the data retention period is not specified or incorrect, use
        // one month as default

        if (retention == null) {
            retention = new TimeRange(BAMCalendar.MONTH, 1);
        }
        return retention;
    }

    public TimeRange getDataArchivalPeriod() throws BAMException {
        TimeRange retention;
        BAMGlobalConfigAdmin configAdmin = new BAMGlobalConfigAdmin(getRegistry());

        try {
            retention = configAdmin.getDataArchivalPeriod();
        } catch (BAMException e) {
            throw new BAMException("Error occurred getting data archive period", e);
        }
        // If the data retention period is not specified or incorrect, use
        // one day as default

        if (retention == null) {
            retention = new TimeRange(BAMCalendar.MONTH, 1);
        }
        return retention;
    }

    public void updateDataRetentionPeriod(TimeRange tr) throws BAMException {
        BAMGlobalConfigAdmin configAdmin = new BAMGlobalConfigAdmin(getRegistry());
        configAdmin.updateDataRetentionPeriod(tr);
    }

    public void updateDataArchivalPeriod(TimeRange tr) throws BAMException {
        BAMGlobalConfigAdmin configAdmin = new BAMGlobalConfigAdmin(getRegistry());
        configAdmin.updateDataArchivalPeriod(tr);
    }

    /*
    * SERVER NEED TO GET RID OF REGISTRY Consider USERNAME/PASSWORD/POLLINGINTERVAL
    */

    public int addMonitoredServer(ServerDO server) throws BAMException {
        int addingServerStatus = -1;
        BAMConfigurationDSClient bamConfigurationDSClient = BAMUtil.getBAMConfigurationDSClient();
        int tenantId;
        if (server.getTenantID() == NO_TENANT_MODE) {
            BAMTenantAdmin bamTenantAdmin = new BAMTenantAdmin();
            tenantId = bamTenantAdmin.getTenantId();
        } else {
            tenantId = server.getTenantID();
        }

        String serverUrl = server.getServerURL();
        String severType = server.getServerType();
        int category = server.getCategory();
        ServerDO monitoredServerDO = bamConfigurationDSClient.getServer(serverUrl, tenantId, severType, category);

        try {
            if (monitoredServerDO == null) {
                synchronized (this) {
                    if (monitoredServerDO == null) {
                        server.setTenantID(tenantId);
                        server.setPassword(encryptPassword(server.getPassword()));
                        int serverID = bamConfigurationDSClient.addServer(server);
                        if (serverID != BAMConstants.UNASSIGNED_SERVER_ID) {
                            addingServerStatus = BAMConstants.SERVER_SUCCESSFULLY_ADDED;
                        }
                    }
                }
            } else {
                addingServerStatus = BAMConstants.SERVER_ALREADY_EXIST;
            }
        } catch (CryptoException e) {
            throw new BAMException("Unable to encrypt password of server " + serverUrl, e);
        } finally {
            if (bamConfigurationDSClient != null) {
                bamConfigurationDSClient.cleanup();
            }
        }
        return addingServerStatus;
    }

    private String encryptServerPassword(ServerDO server) throws CryptoException {
        return encryptPassword(server.getPassword());
    }

    private String encryptPassword(String plainTextPassword) throws CryptoException {
        if (plainTextPassword != null) {
            return CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(plainTextPassword.getBytes());
        }

        return null;
    }

    private String decryptPassword(String encryptedPassword) throws CryptoException {
        String decryptedPassword=null;
        if(encryptedPassword!=null){
           decryptedPassword=new String(CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(encryptedPassword));
        }
        return decryptedPassword;
    }

    public void removeMonitoredServer(int serverID) throws BAMException {
        BAMConfigurationDSClient client = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            try {
                client.removeServer(serverID);
            } catch (Exception e) {
                log.error("Can not delete monitored server entry from DB", e);
            }

        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    public void updateMonitoredServer(ServerDO server) throws BAMException {
        BAMConfigurationDSClient client = null;
        HashMap<String,CacheData> cacheServer = BAMUtil.getBAMCache();
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            server.setPassword(encryptPassword(server.getPassword()));

            try {
                String cacheKey = server.getServerURL() + CacheConstant.CACHE_SEPARATOR +
                                  server.getTenantID() + CacheConstant.CACHE_SEPARATOR +
                                  server.getServerType() + CacheConstant.CACHE_SEPARATOR +
                                  server.getCategory();
                client.updateServer(server);
                if (cacheServer.get(cacheKey) != null) {
                    //In here we invalidate the existing cache, because we don't have server id in server object.
                    cacheServer.put(cacheKey, null);
                }
            } catch (Exception e) {
                log.error("Could not update the server in DB", e);
            }
        } catch (CryptoException e) {
            throw new BAMException("Unable to encrypt password of server " + server.getServerURL(), e);
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    public List<ServerDO> getMonitoredServers(int tenantID) throws BAMException {
        BAMConfigurationDSClient client = BAMUtil.getBAMConfigurationDSClient();

        ServerDO[] servers;
        try {
            if (tenantID == NO_TENANT_MODE) {
                servers = client.getAllServers();
            } else {
                servers = client.getServersForTenant(tenantID);
            }
            if (servers != null) {
                for (ServerDO server : servers) {
                    if (server.getPassword() != null) {
                        server.setPassword(decryptPassword(server.getPassword()));
                    }
                }
                return Arrays.asList(servers);
            }
        } catch (CryptoException e) {
            throw new BAMException("Cannot decrypt password for server ", e);
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
        return new ArrayList<ServerDO>();
    }

    public List<ServerDO> getMonitoredServerListWithCategoryName(int tenantID) throws BAMException {
        BAMConfigurationDSClient client = BAMUtil.getBAMConfigurationDSClient();

        ServerDO[] servers;
        try {
            if (tenantID == NO_TENANT_MODE) {
                servers = client.getAllServersWithCategoryName();
            } else {
                servers = client.getServersWithCategoryNameForTenant(tenantID);
            }
            if (servers != null) {
                for (ServerDO server : servers) {
                    server.setPassword(decryptPassword(server.getPassword()));
                }
                return Arrays.asList(servers);
            }
        } catch (CryptoException e) {
            throw new BAMException("Cannot decrypt password for server ", e);
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
        return new ArrayList<ServerDO>();
    }


    public ServerDO getMonitoredServer(int serverID) throws BAMException {
        BAMConfigurationDSClient client = null;
        ServerDO server = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            server = client.getServer(serverID);
            if (server != null && server.getPassword() != null) {
                server.setPassword(decryptPassword(server.getPassword()));
            }

        } catch (CryptoException e) {
            throw new BAMException("Cannot decrypt password for server " + server.getServerURL(), e);
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
        return server;
    }

    public ServerDO getMonitoredServer(String serverURL) throws BAMException {
        BAMConfigurationDSClient client = null;
        ServerDO server = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            server = client.getServer(serverURL);
            if (server != null && server.getPassword() != null) {
                server.setPassword(decryptPassword(server.getPassword()));
            }

        } catch (CryptoException e) {
            throw new BAMException("Cannot decrypt password for server " + server.getServerURL(), e);
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
        return server;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void deactivateServer(int serverID) throws BAMException {
        BAMConfigurationDSClient client = null;

        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            client.deactivateServer(serverID);
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    public void activateServer(int serverID, String subscriptionID) throws BAMException {
        BAMConfigurationDSClient client = null;

        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            client.activateServer(serverID, subscriptionID);
        } catch (BAMException e) {
            throw e;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }


    public void addService(ServiceDO service) throws BAMException {
        // we should not add the server if it is not in the config tables.
        // unlike in the case with services when operations are added.
        BAMConfigurationDSClient client = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            client.addService(service);
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    public ServiceDO getService(int serviceID) throws BAMException {
        ServiceDO service = BAMConfigurationCache.getService(serviceID);
        BAMConfigurationDSClient client = null;
        try {
            if (service == null) {
                client = BAMUtil.getBAMConfigurationDSClient();
                return client.getService(serviceID);
            }
            if (service != null) {
                BAMConfigurationCache.addService(service);
            }

            return service;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    public ServiceDO getService(int serverID, String serviceName) throws BAMException {
        ServiceDO cashService = BAMConfigurationCache.getService(serverID, serviceName);
        ServiceDO service = null;
        BAMConfigurationDSClient configurationDSClient = null;
        boolean raceCondition=false;

        try {
            if (cashService == null) {
                configurationDSClient = BAMUtil.getBAMConfigurationDSClient();
                if (service == null) {

                    synchronized (this) {
                        if (service == null) {
                            // service not there in the DB, hence we need to add it
                            service = new ServiceDO();
                            service.setName(serviceName);
                            service.setServerID(serverID);
                            // This is to guard against occasional race condition of service
                            // being added while operation is also being added.
                            raceCondition = false;
                            try {
                                configurationDSClient.addService(service);
                            } catch (Exception ignore) {
                                raceCondition = true;
                            }

                        }
                    }
                    service = configurationDSClient.getService(serverID, serviceName);
                    if (raceCondition && service != null) {
                        log.info("Recovered from race condition. " + serviceName + " successfully added!");
                    } else if (raceCondition && service == null) {
                        log.error("Failed to recover from race condition, in adding service " + serviceName);
                    }
                }
            }
            return service;
        } catch (BAMException e) {
            throw e;
        } finally {
            if (configurationDSClient != null) {
                configurationDSClient.cleanup();
            }
        }
    }

    public List<ServiceDO> getAllServices(int serverID) throws BAMException {

        List<ServiceDO> services = BAMConfigurationCache.getAllServices(serverID);

        BAMConfigurationDSClient client = null;
        try {
            if (services == null || services.size() <= 0) {
                client = BAMUtil.getBAMConfigurationDSClient();
                ServiceDO[] servicesArray = client.getAllServices(serverID);
                services = new ArrayList<ServiceDO>(servicesArray.length);
                services.addAll(Arrays.asList(servicesArray));
            }

            for (ServiceDO svc : services) {
                BAMConfigurationCache.addService(svc);
            }

            return services;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    public List<OperationDO> getAllOperations(int serviceID) throws BAMException {
        List<OperationDO> operations = BAMConfigurationCache.getAllOperations(serviceID);

        BAMConfigurationDSClient client = null;
        try {
            if (operations == null || operations.size() <= 0) {
                client = BAMUtil.getBAMConfigurationDSClient();
                OperationDO[] operationsArray = client.getAllOperations(serviceID);

                operations = new ArrayList<OperationDO>();
                operations.addAll(Arrays.asList(operationsArray));
            }
            return operations;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    public void syncServer(ServerDO server) throws BAMException {

        if (getMonitoredServer(server.getId()) == null) {
            addMonitoredServer(server);
        }
    }

    public void syncOperation(OperationDO operation) throws BAMException {
        if (getOperation(operation.getOperationID()) == null) {
            addOperation(operation);
        }
    }

    public void syncService(ServiceDO service) throws BAMException {
        if (getService(service.getId()) == null) {
            addService(service);
        }
    }

    public void addOperation(OperationDO operation) throws BAMException {
        BAMConfigurationDSClient client = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            client.addOperation(operation);
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    public OperationDO getOperation(int serviceID, String operationName) throws BAMException {

        BAMConfigurationDSClient client = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            OperationDO operation = client.getOperation(serviceID, operationName);

            if (operation == null) {
                synchronized (this) {
                    if (operation == null) {
                        operation = new OperationDO();
                        operation.setName(operationName);
                        operation.setServiceID(serviceID);

                        try {
                            client.addOperation(operation);
                        } catch (BAMException bamException) {
                            // If this occurs, that means the operation is already in the DB, can ignore
                            log.info("Unable to add operation, because operation is already in DB");
                        }
                    }
                }
                operation = client.getOperation(serviceID, operationName);
            }

            if (operation != null) {
                BAMConfigurationCache.addOperation(operation);
            }

            return operation;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }

    }

    public OperationDO getOperation(int operationID) throws BAMException {
        OperationDO operation = BAMConfigurationCache.getOperation(operationID);
        BAMConfigurationDSClient client = null;
        try {
            if (operation == null) {
                client = BAMUtil.getBAMConfigurationDSClient();
                operation = client.getOperation(operationID);
            }
            BAMConfigurationCache.addOperation(operation);
            return operation;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    public List<MediationDataDO> getEndpoints(int serverId) throws BAMException {
        BAMConfigurationDSClient client = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            MediationDataDO[] endpoints = client.getEndpoints(serverId);
            List<MediationDataDO> endpointList = new ArrayList<MediationDataDO>(endpoints.length);
            endpointList.addAll(Arrays.asList(endpoints));
            return endpointList;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    public List<MediationDataDO> getSequences(int serverId) throws BAMException {
        BAMConfigurationDSClient client = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            MediationDataDO[] sequences = client.getSequences(serverId);
            List<MediationDataDO> sequenceList = new ArrayList<MediationDataDO>(sequences.length);
            sequenceList.addAll(Arrays.asList(sequences));
            return sequenceList;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    public List<MediationDataDO> getProxyServices(int serverId) throws BAMException {
        BAMConfigurationDSClient client = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            MediationDataDO[] proxySvcs = client.getProxyServices(serverId);
            List<MediationDataDO> proxyList = new ArrayList<MediationDataDO>(proxySvcs.length);
            proxyList.addAll(Arrays.asList(proxySvcs));
            return proxyList;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    /*
      * Add activity
      */

    public void addActivity(ActivityDO activity) throws BAMException {
        BAMConfigurationDSClient client = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            client.addActivity(activity);
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }

    }

    /*
    * Get activity using activitykeyID(primary key)
    */

    public ActivityDO getActivity(int activityKeyId) throws BAMException {
        ActivityDO activity = BAMConfigurationCache.getActivity(activityKeyId);
        BAMConfigurationDSClient client = null;
        try {
            if (activity == null) {
                client = BAMUtil.getBAMConfigurationDSClient();
                activity = client.getActivity(activityKeyId);
            }
            BAMConfigurationCache.addActivity(activityKeyId, activity);
            return activity;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    /*
    * Get all activities
    */

    public List<ActivityDO> getAllActivities() throws BAMException {
        List<ActivityDO> activities = BAMConfigurationCache.getAllActivities();

        BAMConfigurationDSClient client = null;
        try {
            if (activities == null || activities.size() <= 0) {
                client = BAMUtil.getBAMConfigurationDSClient();
                ActivityDO[] activitiesArray = client.getAllActivities();

                if (activitiesArray != null) {
                    activities = new ArrayList<ActivityDO>(activitiesArray.length);
                    activities.addAll(Arrays.asList(activitiesArray));
                }
            }
            for (ActivityDO activity : activities) {
                BAMConfigurationCache.addActivity(activity.getActivityKeyId(), activity);
            }
            return activities;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }

    }

    public List<PropertyFilterDO> getAllXPathConfigurations(int serverID) throws BAMException {
        List<PropertyFilterDO> xpathConfigs = BAMConfigurationCache.getAllXPathConfigurations(serverID);

        BAMConfigurationDSClient client = null;
        try {
            if (xpathConfigs == null || xpathConfigs.size() <= 0) {
                client = BAMUtil.getBAMConfigurationDSClient();
                PropertyFilterDO[] xpathConfigsArray = client.getAllXpathData(serverID);

                if (xpathConfigsArray != null) {
                    xpathConfigs = new ArrayList<PropertyFilterDO>(xpathConfigsArray.length);
                    xpathConfigs.addAll(Arrays.asList(xpathConfigsArray));
                }
            }
/*            for (PropertyFilterDO xpathConfig : xpathConfigs) {
                BAMConfigurationCache.addXpathConfig(xpathConfig);
            }*/
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }

        return xpathConfigs;
    }

    public List<NamespaceDO> getAllNamespaces(int xpathId) throws BAMException {
        List<NamespaceDO> nsList = BAMConfigurationCache.getAllNamespaces(xpathId);

        BAMConfigurationDSClient client = null;
        try {
            if (nsList == null || nsList.size() <= 0) {
                client = BAMUtil.getBAMConfigurationDSClient();
                NamespaceDO[] nsArray = client.getAllNamespaceData(xpathId);

                if (nsArray != null) {
                    nsList = new ArrayList<NamespaceDO>(nsArray.length);
                    nsList.addAll(Arrays.asList(nsArray));
                }
            }
/*            for (NamespaceDO ns : nsArray) {
                BAMConfigurationCache.addXpathConfig(ns);
            }*/
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }

        return nsList;

    }

    /*
    * Add message
    */

    public void addMessage(MessageDO message) throws BAMException {
        BAMConfigurationDSClient client = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            client.addMessage(message);
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }

    }

    /*
    * Get message using messgaeKeyId(primary Key)
    */

    public MessageDO getMessage(int messageKeyId) throws BAMException {
        MessageDO message = BAMConfigurationCache.getMessage(messageKeyId);
        BAMConfigurationDSClient client = null;
        try {
            if (message == null) {
                client = BAMUtil.getBAMConfigurationDSClient();
                message = client.getMessage(messageKeyId);
            }

            BAMConfigurationCache.addMessage(message);
            return message;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }


    /*
    * Get all messages for operationID
    */

    public List<MessageDO> getAllMessagesForOperationID(int operationID) throws BAMException {
        List<MessageDO> messages = BAMConfigurationCache.getAllMessagesForOperationID(operationID);
        BAMConfigurationDSClient client = null;
        try {
            if (messages == null || messages.size() <= 0) {
                client = BAMUtil.getBAMConfigurationDSClient();
                MessageDO[] messagesArray = client.getAllMessagesForOperationID(operationID);
                messages = new ArrayList<MessageDO>(messagesArray.length);
                messages.addAll(Arrays.asList(messagesArray));
            }

            for (MessageDO message : messages) {
                BAMConfigurationCache.addMessage(message);
            }
            return messages;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    /*
    * Get all messages.
    */

    public List<MessageDO> getAllMessages() throws BAMException {
        List<MessageDO> messages = BAMConfigurationCache.getAllMessages();

        BAMConfigurationDSClient client = null;
        try {
            if (messages == null || messages.size() <= 0) {
                client = BAMUtil.getBAMConfigurationDSClient();
                MessageDO[] messagesArray = client.getAllMessages();
                messages = new ArrayList<MessageDO>(messagesArray.length);
                messages.addAll(Arrays.asList(messagesArray));
            }

            for (MessageDO message : messages) {
                BAMConfigurationCache.addMessage(message);
            }
            return messages;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    /*
    * Get all clients
    */

    public List<ClientDO> getAllClients(int serverID) throws BAMException {
        List<ClientDO> clients = BAMConfigurationCache.getAllClients(serverID);

        BAMConfigurationDSClient client = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            if (clients == null || clients.size() <= 0) {
                client = BAMUtil.getBAMConfigurationDSClient();
                ClientDO[] clientsArray = client.getAllClients(serverID);
                clients = new ArrayList<ClientDO>(clientsArray.length);
                clients.addAll(Arrays.asList(clientsArray));
            }

            for (ClientDO svcclient : clients) {
                BAMConfigurationCache.addClient(svcclient);
            }
            return clients;

        } finally {
            if (client != null) {
                client.cleanup();
            }
        }

    }

    public ServerDO[] getMonitoredServersByType(String serverType) throws BAMException {

        BAMConfigurationDSClient client = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            ServerDO[] servers = client.getServersforServerType(serverType);
            if (servers != null) {
                for (ServerDO server : servers) {
                    if (server.getPassword() != null) {
                        server.setPassword(decryptPassword(server.getPassword()));
                    }
                }
            }
            return servers;
        } catch (CryptoException e) {
            throw new BAMException("Cannot decrypt password for server ", e);
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }
}
