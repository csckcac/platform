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
package org.wso2.carbon.bam.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.common.ClientUtil;
import org.wso2.carbon.bam.common.dataobjects.activity.*;
import org.wso2.carbon.bam.common.dataobjects.common.ClientDO;
import org.wso2.carbon.bam.common.dataobjects.mediation.MediationDataDO;
import org.wso2.carbon.bam.common.dataobjects.service.OperationDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServiceDO;
import org.wso2.carbon.bam.services.stub.bamconfigurationds.BAMConfigurationDSStub;
import org.wso2.carbon.bam.services.stub.bamconfigurationds.BamSeverIDval;
import org.wso2.carbon.bam.services.stub.bamconfigurationds.DataServiceFaultException;
import org.wso2.carbon.bam.services.stub.bamconfigurationds.types.*;
import org.wso2.carbon.bam.util.BAMConstants;
import org.wso2.carbon.bam.util.BAMException;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class BAMConfigurationDSClient {

    private static final String BAM_CONFIGURATION_DS = "BAMConfigurationDS";
    private BAMConfigurationDSStub stub;
    private static final Log log = LogFactory.getLog(BAMConfigurationDSClient.class);


    public BAMConfigurationDSClient(String backendServerURL, ConfigurationContext configCtx)
            throws BAMException {
        try {
            String serviceURL = ClientUtil.getBackendEPR(backendServerURL, BAM_CONFIGURATION_DS);
            stub = new BAMConfigurationDSStub(configCtx, serviceURL);
        } catch (RemoteException e) {
            throw new BAMException(e.getMessage(), e);
        } catch (MalformedURLException e) {
            throw new BAMException(e.getMessage(), e);
        }
    }

    public void cleanup() {
        try {
            stub._getServiceClient().cleanupTransport();
            stub._getServiceClient().cleanup();
            stub.cleanup();
        } catch (AxisFault axisFault) {
            if (log.isErrorEnabled()) {
                log.error("Stub cleanup failed: " + this.getClass().getName(), axisFault);
            }
        }
    }

    /**
     * @param serverURL
     * @param tenantID
     * @return
     * @throws BAMException
     * @deprecated
     */
    public ServerDO getServer(String serverURL, int tenantID) throws BAMException {
        ServerDO server = null;
        Server[] monitoringServers;

        try {
            monitoringServers = stub.getServerFromURLAndTenantID(serverURL, String.valueOf(tenantID));
        } catch (RemoteException e) {
            throw new BAMException("Error occurred getting server details for " + serverURL, e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("Error occurred getting server details for " + serverURL, e);
        }

        if (monitoringServers != null && monitoringServers.length > 0) {
            server = ClientUtil.convertServerToServerDO(monitoringServers[0]);
        }
        return server;
    }


    public ServerDO getServer(String serverUrl, int tenantId, String serverType, int category) throws BAMException {
        ServerDO server = null;
        Server[] monitoringServers;
        try {
            monitoringServers = stub.getServerFromBAMDB(serverUrl, tenantId, serverType, category);
        } catch (Exception e) {
            throw new BAMException("Error occurred getting server details for " + serverUrl + " with server type: " +
                    serverType, e);
        }
        if (monitoringServers != null && monitoringServers.length > 0) {
            //We should get only one monitoring server - (server url,tenant id, server type and category)-This should be unique.
            server = ClientUtil.convertServerToServerDO(monitoringServers[0]);
        }
        return server;
    }

    public ServerDO getServer(String serverURL) throws BAMException {
        ServerDO server = null;
        Server[] monitoringServers = new Server[0];

        try {
            monitoringServers = stub.getServerFromURL(serverURL);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred getting server details for " + serverURL, e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("Error occurred getting server details for " + serverURL, e);
        }

        if (monitoringServers != null && monitoringServers.length > 0) {
            server = ClientUtil.convertServerToServerDO(monitoringServers[0]);
        }
        return server;
    }

    public ServerDO getServer(int serverId) throws BAMException {
        ServerDO server = null;
        Server[] monitoringServers = new Server[0];

        try {
            monitoringServers = stub.getServer(serverId);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred getting server details for server id " + serverId, e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("Error occurred getting server details for server id " + serverId, e);
        }

        if (monitoringServers != null && monitoringServers.length > 0) {
            server = ClientUtil.convertServerToServerDO(monitoringServers[0]);
        }
        return server;
    }

    public ServerDO[] getServersforServerType(String serverType) throws BAMException {
        List<ServerDO> servers = new ArrayList<ServerDO>();
        try {
            Server[] monitoredServers = stub.getServersForServerType(serverType);
            if (monitoredServers != null) {
                for (Server server : monitoredServers) {
                    servers.add(ClientUtil.convertServerToServerDO(server));
                }
            }
        } catch (RemoteException e) {
            throw new BAMException("Failed to get server details", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("Failed to get server details", e);
        }
        return servers.toArray(new ServerDO[servers.size()]);
    }

    public ServerDO[] getServersForTenant(int tenantID) throws BAMException {
        ServerDO server;
        List<ServerDO> servers = new ArrayList<ServerDO>();
        Server[] monitoringServers;

        try {
            monitoringServers = stub.getServersForTenant(tenantID);
        } catch (RemoteException e) {
            throw new BAMException("Failed to get server details", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("Failed to get server details", e);
        }


        if (monitoringServers != null && monitoringServers.length > 0) {
            for (Server monitorServer : monitoringServers) {
                server = ClientUtil.convertServerToServerDO(monitorServer);
                servers.add(server);
            }
        } else {
            return null;
        }

        return servers.toArray(new ServerDO[servers.size()]);
    }

    public ServerDO[] getServersWithCategoryNameForTenant(int tenantID) throws BAMException {
        ServerDO server;

        List<ServerDO> servers = new ArrayList<ServerDO>();
        ServerWithCategory[] monitoringServers;

        try {
            monitoringServers = stub.getAllServersWithCategoryForTenent(tenantID);
        } catch (RemoteException e) {
            throw new BAMException("Failed to get server details", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("Failed to get server details", e);
        }

        if (monitoringServers != null && monitoringServers.length > 0) {
            for (ServerWithCategory monitorServer : monitoringServers) {
                server = ClientUtil.convertToServerDOWithCategoryName(monitorServer);
                servers.add(server);
            }
        } else {
            return null;
        }

        return servers.toArray(new ServerDO[servers.size()]);

    }

    public ServerDO[] getAllServers() throws BAMException {
        ServerDO server;
        List<ServerDO> servers = new ArrayList<ServerDO>();
        Server[] monitorServers = new Server[0];

        try {
            monitorServers = stub.getAllServers();
        } catch (RemoteException e) {
            throw new BAMException("Failed to get all monitoring servers", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("Failed to get all monitoring servers", e);
        }

        if (monitorServers != null && monitorServers.length > 0) {
            for (Server monitorServer : monitorServers) {
                server = ClientUtil.convertServerToServerDO(monitorServer);
                servers.add(server);
            }
        } else {
            return null;
        }

        return servers.toArray(new ServerDO[servers.size()]);
    }

    public ServerDO[] getAllServersWithCategoryName() throws BAMException {
        ServerDO server;
        List<ServerDO> servers = new ArrayList<ServerDO>();
        ServerWithCategory[] monitorServers = new ServerWithCategory[0];

        try {
            monitorServers = stub.getAllServersWithCategory();
        } catch (RemoteException e) {
            throw new BAMException("Failed to get all monitoring servers", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("Failed to get all monitoring servers", e);
        }

        if (monitorServers != null && monitorServers.length > 0) {
            for (ServerWithCategory monitorServer : monitorServers) {
                server = ClientUtil.convertToServerDOWithCategoryName(monitorServer);
                servers.add(server);
            }
        } else {
            return null;
        }
        return servers.toArray(new ServerDO[servers.size()]);
    }

    public synchronized int addServer(ServerDO server) throws BAMException {
        int serverID = BAMConstants.UNASSIGNED_SERVER_ID;
        String serverType = server.getServerType();
        int tenantId = server.getTenantID();
        String serverUrl = server.getServerURL();
        String serverDescription = server.getDescription();
        int serverCategory = server.getCategory();
        String subscriptionEPR = server.getSubscriptionEPR();
        String subscriptionId = server.getSubscriptionID();

        try {
            BamSeverIDval[] serverIDs = stub.addServer(tenantId, serverType, serverUrl, serverDescription, serverCategory,
                    subscriptionEPR, subscriptionId, server.getUserName(), server.getPassword());
            //we only consider first element, because it returns only one ID.
            serverID = serverIDs[0].getBamServerIDValue();
        } catch (RemoteException e) {
            throw new BAMException("Server " + serverUrl + " adding failed ", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("Server " + serverUrl + " adding failed ", e);
        }
        return serverID;
    }

    public void updateServer(ServerDO server) throws BAMException {

        int serverId = server.getId();
        String serverURL = server.getServerURL();
        String serverDescription = server.getDescription();
        String username = server.getUserName();
        String password = server.getPassword();

        if (server.getSubscriptionEPR() != null) {
            String subscriptionEPR = server.getSubscriptionEPR();
            String subscriptionID = server.getSubscriptionID();

            try {
                stub.updateServerWithSubscription(serverURL, serverDescription, subscriptionEPR,
                        subscriptionID, username, password, serverId);
            } catch (RemoteException e) {
                throw new BAMException("Updating server " + serverURL + "failed", e);
            } catch (DataServiceFaultException e) {
                throw new BAMException("Updating server " + serverURL + "failed", e);
            }

        } else {
            try {
                stub.updateServer(serverURL, serverDescription, username, password, serverId);
            } catch (RemoteException e) {
                throw new BAMException("Updating server " + serverURL + "failed", e);
            } catch (DataServiceFaultException e) {
                throw new BAMException("Updating server " + serverURL + "failed", e);
            }
        }

    }

    public void deactivateServer(int serverId) throws BAMException {

        try {
            stub.deactivateServer(serverId);
        } catch (RemoteException e) {
            throw new BAMException("deactivateServer failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("deactivateServer failed", e);
        }
    }

    public void removeServer(int serverId) throws BAMException {
        try {
            stub.deleteServer(serverId);
        } catch (RemoteException e) {
            throw new BAMException("Removing server failed..", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("Removing server failed..", e);
        }
    }

    public void activateServer(int serverId, String subscriptionId) throws BAMException {
        ActivateServer activateServer = new ActivateServer();
        activateServer.setServerID(serverId);
        activateServer.setSubscriptionID(subscriptionId);

        try {
            stub.activateServer(subscriptionId, serverId);
        } catch (RemoteException e) {
            throw new BAMException("activateServer failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("activateServer failed", e);
        }
    }

    public ServiceDO getService(int serviceId) throws BAMException {
        ServiceDO service = null;
        Service[] services = new Service[0];

        try {
            services = stub.getService(serviceId);
        } catch (RemoteException e) {
            throw new BAMException("getService failed for serviceId: " + serviceId, e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("getService failed for serviceId: " + serviceId, e);
        }

        if (services != null && services.length > 0) {
            service = ClientUtil.convertServiceToServiceDO(services[0]);
        }

        return service;
    }

    public ServiceDO getService(int serverId, String serviceName) throws BAMException {
        ServiceDO service = null;
        Service[] services = new Service[0];

        try {
            services = stub.getServiceForServer(serverId, serviceName);
        } catch (RemoteException e) {
            throw new BAMException("getService failed for serviceId: " + serverId + " serverName: " + serviceName, e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("getService failed for serviceId: " + serverId + " serverName: " + serviceName, e);
        }

        if (services != null && services.length > 0) {
            service = ClientUtil.convertServiceToServiceDO(services[0]);

        }

        return service;
    }

    public ServiceDO[] getAllServices(int serverId) throws BAMException {
        List<ServiceDO> serviceDOList = new ArrayList<ServiceDO>();
        ServiceDO service;
        Service[] services = new Service[0];

        try {
            services = stub.getAllServices(serverId);
        } catch (RemoteException e) {
            throw new BAMException("getAllServices failed for serviceId: " + serverId, e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("getAllServices failed for serviceId: " + serverId, e);
        }

        if (services != null && services.length > 0) {
            for (Service adbService : services) {
                service = ClientUtil.convertServiceToServiceDO(adbService);
                serviceDOList.add(service);
            }
        }
        return serviceDOList.toArray(new ServiceDO[serviceDOList.size()]);
    }


    public synchronized void addService(ServiceDO service) throws BAMException {
        try {

            int serviceId = service.getServerID();
            String serviceName = service.getName();
            String serviceDescription = service.getDescription();

            if (stub.getServiceForServer(serviceId, serviceName) != null) {
                return;
            }
            stub.addService(serviceId, serviceName, serviceDescription);

        } catch (RemoteException e) {
            throw new BAMException("addService failed for service serverId: " + service.getServerID() + " serviceName: "
                    + service.getName(), e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("addService failed for service serverId: " + service.getServerID() + " serviceName: "
                    + service.getName(), e);
        }
    }

    public synchronized void addOperation(OperationDO operation) throws BAMException {

        int serviceId = operation.getServiceID();
        String operationName = operation.getName();
        try {
            if (stub.getOperationFromName(serviceId, operationName) != null) {
                return;
            }
            stub.addOperation(serviceId, operationName, operation.getDescription());

        } catch (RemoteException e) {
            throw new BAMException("addOperation failed for operation with serviceId: " + operation.getServiceID() +
                    " operationName: " + operation.getName(), e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("addOperation failed for operation with serviceId: " + operation.getServiceID() +
                    " operationName: " + operation.getName(), e);
        }
    }

    public OperationDO getOperation(int serviceId, String operationName) throws BAMException {
        OperationDO operationDO = null;
        Operation[] operations = new Operation[0];

        try {
            operations = stub.getOperationFromName(serviceId, operationName);
        } catch (RemoteException e) {
            throw new BAMException("Unable to getOperation", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("Unable to getOperation", e);
        }

        if (operations != null && operations.length > 0) {
            Operation operation = operations[0];
            operationDO = ClientUtil.convertOperationToOperationDO(operation);
        }

        return operationDO;
    }

    public OperationDO getOperation(int operationId) throws BAMException {
        OperationDO operationDO = null;
        Operation[] adbOperationArray = new Operation[0];
        try {
            adbOperationArray = stub.getOperation(operationId);
        } catch (RemoteException e) {
            throw new BAMException("Unable to getOperation", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("Unable to getOperation", e);
        }

        if (adbOperationArray != null && adbOperationArray.length > 0) {
            Operation adbOperation = adbOperationArray[0];
            operationDO = ClientUtil.convertOperationToOperationDO(adbOperation);
        }

        return operationDO;
    }

    // get all operations

    public OperationDO[] getAllOperations(int serviceId) throws BAMException {
        List<OperationDO> operationDOList = new ArrayList<OperationDO>();
        Operation[] operations = new Operation[0];
        try {
            operations = stub.getAllOperationsForService(serviceId);
        } catch (RemoteException e) {
            throw new BAMException("Unable to get operations", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("Unable to get operations", e);
        }

        if (operations != null && operations.length > 0) {
            for (Operation operation : operations) {
                OperationDO operationDO = ClientUtil.convertOperationToOperationDO(operation);
                operationDOList.add(operationDO);
            }

        }
        return operationDOList.toArray(new OperationDO[operationDOList.size()]);
    }

    public MediationDataDO[] getEndpoints(int serverId) throws BAMException {
        List<MediationDataDO> endpointsList = new ArrayList<MediationDataDO>();
        Endpoint[] endpoints = new Endpoint[0];

        try {
            endpoints = stub.getEndpoints(serverId);
        } catch (RemoteException e) {
            throw new BAMException("getEndpoints failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("getEndpoints failed", e);
        }

        if (endpoints != null && endpoints.length > 0) {
            for (Endpoint endpoint : endpoints) {
                MediationDataDO endpointDO = new MediationDataDO();
                endpointDO.setName(endpoint.getEndpoint());
                endpointDO.setDirection("In");
                endpointDO.setServerId(serverId);
                endpointsList.add(endpointDO);

                endpointDO = new MediationDataDO();
                endpointDO.setName(endpoint.getEndpoint());
                endpointDO.setDirection("Out");
                endpointDO.setServerId(serverId);
                endpointsList.add(endpointDO);
            }
        }

        return endpointsList.toArray(new MediationDataDO[endpointsList.size()]);
    }

    public MediationDataDO[] getProxyServices(int serverId) throws BAMException {
        List<MediationDataDO> proxyServicesList = new ArrayList<MediationDataDO>();
        ProxyService[] proxyServices = new ProxyService[0];

        try {
            proxyServices = stub.getProxyServices(serverId);
        } catch (RemoteException e) {
            throw new BAMException("getProxyServices failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("getProxyServices failed", e);
        }

        if (proxyServices != null && proxyServices.length > 0) {
            for (ProxyService proxyService : proxyServices) {
                MediationDataDO proxyDO = new MediationDataDO();
                proxyDO.setName(proxyService.getProxyService());
                proxyDO.setDirection("In");
                proxyDO.setServerId(serverId);
                proxyServicesList.add(proxyDO);

                proxyDO = new MediationDataDO();
                proxyDO.setName(proxyService.getProxyService());
                proxyDO.setDirection("Out");
                proxyDO.setServerId(serverId);
                proxyServicesList.add(proxyDO);
            }
        }

        return proxyServicesList.toArray(new MediationDataDO[proxyServicesList.size()]);
    }

    public MediationDataDO[] getSequences(int serverId) throws BAMException {
        List<MediationDataDO> sequencesList = new ArrayList<MediationDataDO>();

        Sequence[] sequences = new Sequence[0];
        try {
            sequences = stub.getSequences(serverId);
        } catch (RemoteException e) {
            throw new BAMException("Sequences getting  failed for serverId : " + serverId, e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("Sequences getting  failed for serverId : " + serverId, e);
        }

        if (sequences != null && sequences.length > 0) {
            for (Sequence seq : sequences) {
                MediationDataDO sequenceDO = new MediationDataDO();
                sequenceDO.setName(seq.getSequence());
                sequenceDO.setDirection("In");
                sequenceDO.setServerId(serverId);
                sequencesList.add(sequenceDO);

                sequenceDO = new MediationDataDO();
                sequenceDO.setName(seq.getSequence());
                sequenceDO.setDirection("Out");
                sequenceDO.setServerId(serverId);
                sequencesList.add(sequenceDO);
            }
        }
        return sequencesList.toArray(new MediationDataDO[sequencesList.size()]);

    }

    /*
    * Add Activity to DB
    */

    public synchronized void addActivity(ActivityDO activity) throws BAMException {
        try {
            stub.addActivityData(activity.getName(), activity.getDescription(), activity.getActivityId());
        } catch (RemoteException e) {
            throw new BAMException("Adding Activity to the datbase failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("Adding Activity to the datbase failed", e);
        }
    }

    /*
      * Update Activity
      */

    public void updateActivity(String name, String description, int activityKeyId)
            throws BAMException {

        try {
            stub.updateActivity(name, description, activityKeyId);
        } catch (RemoteException e) {
            throw new BAMException("updateActivity failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("updateActivity failed", e);
        }
    }

    /*
     * Get activity using activityID
     */

    public ActivityDO getActivityForActivityID(String activityId) throws BAMException {
        ActivityDO activity = null;
        try {
            Activity[] adbActivityArray = stub.getActivityForActivityID(activityId);

            if (adbActivityArray != null && adbActivityArray.length > 0) {
                Activity adbActivity = adbActivityArray[0];
                activity = new ActivityDO();
                activity.setActivityKeyId(Integer.parseInt(adbActivity.getActivityKeyId()));
                activity.setDescription(adbActivity.getDescription());
                activity.setName(adbActivity.getName());
                activity.setActivityId(adbActivity.getActivityId());
            }

        } catch (RemoteException e) {
            throw new BAMException("getActivityForActivityID failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("getActivityForActivityID failed", e);
        }
        return activity;
    }

    /*
    * Get activity using activityKeyID(primary Key)
    */

    public ActivityDO getActivity(int activityKeyId) throws BAMException {
        ActivityDO activity = null;
        try {

            Activity[] adbActivityArray = stub.getActivity(activityKeyId);

            if (adbActivityArray != null && adbActivityArray.length > 0) {
                Activity adbActivity = adbActivityArray[0];
                activity = new ActivityDO();
                activity.setActivityKeyId(Integer.parseInt(adbActivity.getActivityKeyId()));
                activity.setDescription(adbActivity.getDescription());
                activity.setName(adbActivity.getName());
                activity.setActivityId(adbActivity.getActivityId());

            }
        } catch (RemoteException e) {
            throw new BAMException("getActivity failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("getActivity failed", e);
        }
        return activity;
    }

    /*
    * Get all activities
    */

    public ActivityDO[] getAllActivities() throws BAMException {
        List<ActivityDO> activities = new ArrayList<ActivityDO>();
        Activity[] adbActivityIdArray = new Activity[0];

        try {
            adbActivityIdArray = stub.getAllActivities();
        } catch (RemoteException e) {
            throw new BAMException("getAllActivities failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("getAllActivities failed", e);
        }

        if (adbActivityIdArray != null && adbActivityIdArray.length > 0) {
            for (Activity act : adbActivityIdArray) {
                ActivityDO activity = new ActivityDO();
                activity.setDescription(act.getDescription());
                activity.setActivityKeyId(Integer.parseInt(act.getActivityKeyId()));
                activity.setName(act.getName());
                activity.setActivityId(act.getActivityId());
                activities.add(activity);
            }
        }

        return activities.toArray(new ActivityDO[activities.size()]);

    }

    /*
    * Add message
    */

    public synchronized void addMessage(MessageDO message) throws BAMException {
        try {
            stub.addMessage(message.getOperationId(), message.getMessageId(), message.getActivityKeyId(), message
                    .getTimestamp(), message.getIPAddress(), message.getUserAgent());
        } catch (RemoteException e) {
            throw new BAMException("Add Message failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("Add Message failed", e);
        }
    }

    /*
    * Get Message using MessageKeyID
    */

    public MessageDO getMessage(int messageKeyId) throws BAMException {
        MessageDO message = null;
        Message[] adbMessageArray = new Message[0];

        try {
            adbMessageArray = stub.getMessage(messageKeyId);
        } catch (RemoteException e) {
            throw new BAMException("getMessage failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("getMessage failed", e);
        }

        if (adbMessageArray != null && adbMessageArray.length > 0) {
            Message adbMessage = adbMessageArray[0];
            message = new MessageDO();
            message.setMessageKeyId(Integer.parseInt(adbMessage.getMessageKeyId()));
            message.setActivityKeyId(Integer.parseInt(adbMessage.getActivityKeyId()));
            message.setIPAddress(adbMessage.getIpAddress());
            message.setMessageId(adbMessage.getMessageId());
            message.setOperationId(Integer.parseInt(adbMessage.getOperationId()));
            message.setUserAgent(adbMessage.getUserAgent());
            message.setTimestamp(adbMessage.getTimestamp());
        }

        return message;
    }

    public MessageDO getMessage(String messageId, int operationId, int actiivtyKeyId)
            throws BAMException {
        MessageDO message = null;
        try {
            Message[] adbMessageArray;
            Message adbMessage;

            adbMessageArray = stub.getMessageForMessageandOperationandActivity(messageId, operationId, actiivtyKeyId);

            if (adbMessageArray != null && adbMessageArray.length > 0) {
                adbMessage = adbMessageArray[0];
                message = new MessageDO();
                message.setMessageKeyId(Integer.parseInt(adbMessage.getMessageKeyId()));
                message.setActivityKeyId(Integer.parseInt(adbMessage.getActivityKeyId()));
                message.setIPAddress(adbMessage.getIpAddress());
                message.setMessageId(adbMessage.getMessageId());
                message.setOperationId(Integer.parseInt(adbMessage.getOperationId()));
                message.setUserAgent(adbMessage.getUserAgent());
                message.setTimestamp(adbMessage.getTimestamp());
            }
        } catch (RemoteException e) {
            throw new BAMException("getMessage failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("getMessage failed", e);
        }
        return message;
    }


    /*
     * Get Message using MessageKeyID,ActivityKeyID
     */

    public MessageDataDO getMessageDataForActivityKeyIDandMessageKeyID(int messageKeyID,
                                                                       int activityKeyID)
            throws BAMException {
        MessageDataDO message = null;
        try {

            Messagedata[] adbMessageDataArray = stub.getMessageDataForActivityKeyIDandMessageKeyID(messageKeyID, activityKeyID);

            if (adbMessageDataArray != null && adbMessageDataArray.length > 0) {
                Messagedata adbMessageData = adbMessageDataArray[0];
                message = new MessageDataDO();
                message.setMessageKeyId(Integer.parseInt(adbMessageData.getMessageKeyId()));
                message.setActivityKeyId(Integer.parseInt(adbMessageData.getActivityKeyId()));
                message.setIpAddress(adbMessageData.getIpAddress());
                message.setMessageBody(adbMessageData.getMessage());
                message.setMessageDirection(adbMessageData.getDirection());
                // message.setMsgStatus(adbMessageData.getStatus());
                message.setMessageDataKeyId(Integer.parseInt(adbMessageData.getMessageDataKeyId()));
            }
        } catch (RemoteException e) {
            throw new BAMException("getMessage failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("getMessage failed", e);
        }
        return message;
    }

    public ClientDO[] getAllClients(int serverId) throws BAMException {
        List<ClientDO> clients = new ArrayList<ClientDO>();
        try {

            Client[] adbClientsArray = stub.getAllClients(serverId);

            if (adbClientsArray != null && adbClientsArray.length > 0) {
                for (Client adbClient : adbClientsArray) {
                    ClientDO client = new ClientDO();
                    client.setName(adbClient.getClientname());
                    client.setId(Integer.parseInt(adbClient.getClientbamid()));
                    client.setServerID(Integer.parseInt(adbClient.getClientserverid()));
                    client.setUUID(adbClient.getClientuuid());
                    client.setValue(adbClient.getClientvalue());
                    clients.add(client);
                }

            }
        } catch (RemoteException e) {
            throw new BAMException("getAllClients failed for serverId: " + serverId, e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("getAllClients failed for serverId: " + serverId, e);
        }
        return clients.toArray(new ClientDO[clients.size()]);
    }

    public PropertyFilterDO[] getAllXpathData(int serverID) throws BAMException {
        List<PropertyFilterDO> dataList = new ArrayList<PropertyFilterDO>();

        try {
            XpathConfig[] datas = stub.getAllXpathData(serverID);

            if (datas != null && datas.length > 0) {


                for (XpathConfig data : datas) {
                    PropertyFilterDO propertyFilterDO = new PropertyFilterDO();
                    propertyFilterDO.setId(data.getBamId().intValue());
                    propertyFilterDO.setAlias(data.getBamAlias());
                    propertyFilterDO.setExpressionKey(data.getBamName());
                    propertyFilterDO.setExpression(data.getBamXpath());

                    dataList.add(propertyFilterDO);
                }
            }
        } catch (RemoteException e) {
            throw new BAMException("Fetching xpath configurations failed..", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("Fetching xpath configurations failed..", e);
        }

        return dataList.toArray(new PropertyFilterDO[dataList.size()]);

    }

    public NamespaceDO[] getAllNamespaceData(int xpathID) throws BAMException {
        List<NamespaceDO> nsList = new ArrayList<NamespaceDO>();

        try {
            NamespaceData[] datas = stub.selectNamespaceData(xpathID);

            if (datas != null && datas.length > 0) {

                for (NamespaceData data : datas) {
                    NamespaceDO namespaceDO = new NamespaceDO();
                    namespaceDO.setId(data.getBamId().intValue());
                    namespaceDO.setPrefix(data.getBamPrefix());
                    namespaceDO.setUri(data.getBamURI());

                    nsList.add(namespaceDO);
                }
            }
        } catch (RemoteException e) {
            throw new BAMException("Fetching namespaces for xpath id " + xpathID + " failed..", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("Fetching namespaces for xpath id " + xpathID + " failed..", e);
        }

        return nsList.toArray(new NamespaceDO[nsList.size()]);
    }

    public MessageDO[] getAllMessagesForOperationID(int operationID) throws BAMException {
        throw new RuntimeException("TODO");

    }

    public MessageDO[] getAllMessages() throws BAMException {
        throw new RuntimeException("TODO");
    }

    public void updateXpathData(String alias, String name, String xpath, int serverId, int bamId)
            throws BAMException {
        try {
            stub.updateXpathData(alias, name, xpath, serverId, bamId);
        } catch (RemoteException e) {
            throw new BAMException("updateXpathData failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("updateXpathData failed", e);
        }
    }

    public void addXpathData(String alias, String name, String xpath, int serverId)
            throws BAMException {
        try {
            stub.addXpathData(alias, name, xpath, serverId);
        } catch (RemoteException e) {
            throw new BAMException("addXpathData failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("addXpathData failed", e);
        }
    }


    public void addNamespaceData(int bamXpathId, String prefix, String uri) throws BAMException {
        try {
            stub.addNamespaceData(bamXpathId, prefix, uri);
        } catch (RemoteException e) {
            throw new BAMException("addNamespaceData failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("addNamespaceData failed", e);
        }
    }

    public void deleteNamespaceData(int bamXpathId) throws BAMException {
        try {
            stub.deleteNamespaceData(bamXpathId);
        } catch (RemoteException e) {
            throw new BAMException("Deleting namespace data failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("Deleting namespace data failed", e);
        }
    }

    public void updateNamespaceData(int bamXpathId, String prefix, String uri, int bamId)
            throws BAMException {
        try {
            stub.updateNamespaceData(bamXpathId, prefix, uri, bamId);
        } catch (RemoteException e) {
            throw new BAMException("updateNamespaceData failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("updateNamespaceData failed", e);
        }
    }

    /**
     * Update MessagePropertyTable table with the value of a property
     */
    public void updateMessageProperty(String value, int messagePropertyKeyId) throws BAMException {
        try {
            stub.updateMessageProperty(value, messagePropertyKeyId);
        } catch (RemoteException e) {
            throw new BAMException("updateMessageProperty failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("updateMessageProperty failed", e);
        }
    }

    public PropertyFilterDO getXpathData(String xpathKey, int serverId) throws BAMException {
        PropertyFilterDO propertyFilterDO = null;

        try {
            XpathData[] datas = stub.selectXpathData(xpathKey, serverId);
            XpathData data = null;

            if (datas != null && datas.length > 0) {
                data = datas[0];
                propertyFilterDO = new PropertyFilterDO();
                propertyFilterDO.setExpressionKey(xpathKey);
                propertyFilterDO.setAlias(data.getBamAlias()); // TODO: Include expression key to dbs return
                propertyFilterDO.setId(data.getBamId().intValue());
                propertyFilterDO.setExpression(data.getBamXpath());
            }
        } catch (RemoteException e) {
            throw new BAMException("getXpathData failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("getXpathData failed", e);
        }

        return propertyFilterDO;
    }

    /**
     * check the existance of a message property
     *
     * @param messageKeyId
     * @param activityKeyId
     * @param key
     * @return
     * @throws BAMException
     */
    public MessagePropertyDO getPropertyofMessage(int messageKeyId, int activityKeyId, String key)
            throws BAMException {
        MessagePropertyDO messagePropertyDO = null;
        try {
            Messageproperty[] adbMessageArray;
            Messageproperty adbMessage;

            adbMessageArray = stub.getMessageProperty(messageKeyId, activityKeyId, key);

            if (adbMessageArray != null && adbMessageArray.length > 0) {
                adbMessage = adbMessageArray[0];
                messagePropertyDO = new MessagePropertyDO();
                messagePropertyDO.setMessagePropertyKeyId(Integer.parseInt(adbMessage.getMessagePropertyKeyId()));
                messagePropertyDO.setActivityKeyId(Integer.parseInt(adbMessage.getActivityKeyId()));
                messagePropertyDO.setMessageKeyId(Integer.parseInt(adbMessage.getMessageKeyId()));
                messagePropertyDO.setKey(adbMessage.getKey());
                messagePropertyDO.setValue(adbMessage.getValue());
            }
        } catch (RemoteException e) {
            throw new BAMException("getMessage failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("getMessage failed", e);
        }
        return messagePropertyDO;
    }

    /**
     * Update MessageData table with MessageBody and Direction
     */
    public void updateMessageDump(String messageBody, String messageDir, String ipAddress,
                                  int messageDataKeyId)
            throws BAMException {

        try {
            stub.updateMessageDump(messageBody, messageDir, ipAddress, messageDataKeyId);
        } catch (RemoteException e) {
            throw new BAMException("updateMessageStatus failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("updateMessageStatus failed", e);
        }
    }

    /**
     * Update MessageData table with MessageStatus
     */
    public void updateMessageStatus(String messageStatus, int messageDataKeyId)
            throws BAMException {

        try {
            stub.updateMessageStatus(messageStatus, messageDataKeyId);
        } catch (RemoteException e) {
            throw new BAMException("updateMessageStatus failed", e);
        } catch (DataServiceFaultException e) {
            throw new BAMException("updateMessageStatus failed", e);
        }
    }


}


