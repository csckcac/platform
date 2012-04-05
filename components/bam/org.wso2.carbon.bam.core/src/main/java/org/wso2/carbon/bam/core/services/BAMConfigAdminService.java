/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.wso2.carbon.bam.core.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.common.dataobjects.common.MonitoredServerDTO;
import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;
import org.wso2.carbon.bam.core.persistence.BAMPersistenceManager;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.bam.util.TimeRange;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.event.client.broker.BrokerClient;
import org.wso2.carbon.event.client.broker.BrokerClientException;

import java.io.IOException;
import org.wso2.carbon.bam.core.util.ClientAuthHandler;
import org.wso2.carbon.bam.core.clients.AuthenticationAdminClient_3_2_0;
import org.wso2.carbon.bam.util.BAMConstants;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;

public class BAMConfigAdminService extends AbstractAdmin {
    private static Log log = LogFactory.getLog(BAMConfigAdminService.class);
    static BAMPersistenceManager persistenceManager;
    public BAMConfigAdminService() {
        persistenceManager = BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry());
    }

    public int addServer(ServerDO monitoredServer) throws BAMException {
        try {
            String urlStr = monitoredServer.getServerURL();
            URL url = new URL(urlStr);
            int port = url.getPort();
            if (port == -1) {
                port = url.getDefaultPort();
            }
            try {
                new Socket(url.getHost(), port); //this is just to check if the server is up
                boolean authenticated;
                try {
                    AuthenticationAdminClient_3_2_0 authenticationAdminClient = new AuthenticationAdminClient_3_2_0(monitoredServer.getServerURL());
                    authenticated = authenticationAdminClient.authenticate(monitoredServer.getUserName(), monitoredServer.getPassword());
                } catch (Exception e) {
                    if (e.getMessage().indexOf("404") > 0) {
                        return BAMConstants.SERVER_AUTH_FAILED_404;
                    } else {
                        //this is for backward compatibility with the old stubs
                        authenticated = ClientAuthHandler.getClientAuthHandler().isAuthenticateWithServer(monitoredServer);
                    }
                }
                if (authenticated) {
                    int stateOfServerAdded = persistenceManager.addMonitoredServer(monitoredServer);
                    return stateOfServerAdded;
                } else {
                    return BAMConstants.SERVER_AUTH_FAILED;
                }
            } catch (IOException e) {
                return BAMConstants.SERVER_NOT_RUNNING;
            }
        } catch (MalformedURLException e) {
            return BAMConstants.SERVER_URL_MALFORMED;
        }
    }

    public void deactivateServer(int serverID) throws BAMException {
        persistenceManager.deactivateServer(serverID);
    }

    public void activateServer(int serverID, String subscriptionID) throws BAMException {
        persistenceManager.activateServer(serverID, subscriptionID);
    }

    public void removeServer(MonitoredServerDTO dto) throws BAMException {
        persistenceManager.removeMonitoredServer(dto.getServerId());
    }

    public void updateServer(ServerDO server) throws BAMException {
        if(server != null) {
            persistenceManager.updateMonitoredServer(server);
        }
    }

    public ServerDO[] getServerList() throws BAMException {

        int tenantId = BAMUtil.getTenantID(getTenantDomain());
        List<ServerDO> serverList = persistenceManager.getMonitoredServers(tenantId);

        if(serverList != null) {
            return serverList.toArray(new ServerDO[serverList.size()]);
        } else {
            return new ServerDO[]{};
        }

    }

    public ServerDO getServerDetails(int serverId) throws BAMException {

        ServerDO server = persistenceManager.getMonitoredServer(serverId);
        return server;
    }

    public String getDataRetentionPeriod() throws BAMException {

        TimeRange timeRange = null;
        try {
            timeRange = persistenceManager.getDataRetentionPeriod();
        } catch (BAMException e) {
            throw new BAMException("Error occurred getting DataRetentionPeriod" ,e);
        }
        return timeRange.toString();
    }

    public void setDataRetentionPeriod(String timeRange) throws BAMException {
        persistenceManager.updateDataRetentionPeriod(TimeRange.parseTimeRange(timeRange));
    }

    public String getDataArchivalPeriod() throws BAMException {
        TimeRange timeRange = persistenceManager.getDataArchivalPeriod();
        return timeRange.toString();
    }

    public void setDataArchivalPeriod(String timeRange) throws BAMException {
        persistenceManager.updateDataArchivalPeriod(TimeRange.parseTimeRange(timeRange));
    }

    public static String subscribe(String topic, String brokerURL, String subscriberURL, String serverURL, String userName, String password)
            throws BAMException {

        String subId = null;
        try {
            BrokerClient  client = BAMUtil.getBrokerClient(brokerURL, userName, password);
            subId = client.subscribe(topic, subscriberURL);
            log.info("Subscription to server:" + serverURL + " with subscriber URL: " + subscriberURL + " and topic:" + topic + " is successful..");

//        } catch (BrokerClientException e) {
//            throw new BAMException("Failed to subscribe : " +subscriberURL+ "to topic : " +topic ,e);
        } catch (Exception e) {
            try {
                ServerDO server = new ServerDO();
                server.setServerURL(serverURL);
                server.setUserName(userName);
                server.setPassword(password);
                boolean authenticated = ClientAuthHandler.getClientAuthHandler().isAuthenticateWithServer(server);
                if (authenticated) {
                    String cookie = ClientAuthHandler.getClientAuthHandler().getSessionString(server);
                    String brokerURL_2_0_3 = serverURL + "/services/BAMServiceStatisticsPublisherService";
                    subId = BAMUtil.getBrokerClient(brokerURL_2_0_3, cookie).subscribe(topic, subscriberURL);
                }
            } catch (Exception innerException) {
                log.error("BAM cannot suscribe to " + subscriberURL,innerException);
                throw new BAMException("Failed to subscribe : " +subscriberURL+ "to topic : " +topic ,innerException);
            }
        }

        return subId;

    }
    public static void unsubscribe(String brokerURL, String identifier, String serverType, String serverURL) throws BAMException {
        ServerDO monitoredServer = null;
        try {

            monitoredServer = persistenceManager.getMonitoredServer(serverURL);

            BrokerClient client = new BrokerClient(BAMUtil.getConfigurationContextService().getServerConfigContext(),
                                                   brokerURL, monitoredServer.getUserName(), monitoredServer.getPassword());
            client.unsubscribe(identifier);
        } catch (Exception e) {
            try {
                ServerDO server = new ServerDO();
                server.setServerURL(serverURL);
                server.setUserName(monitoredServer.getUserName());
                server.setPassword(monitoredServer.getPassword());
                boolean authenticated = ClientAuthHandler.getClientAuthHandler().isAuthenticateWithServer(server);
                if (authenticated) {
                    String cookie = ClientAuthHandler.getClientAuthHandler().getSessionString(server);
                    String brokerURL_2_0_3 = serverURL + "/services/BAMServiceStatisticsPublisherService";
                    BAMUtil.getBrokerClient(brokerURL_2_0_3, cookie).unsubscribe(identifier);
                } else {
                    throw e;
                }
            } catch (Exception innerException) {
                String msg = "Failed to Un-subscribe - brokerURL: " + brokerURL + " or you are not super admin";
                log.error(msg, innerException);
                throw new BAMException(msg, innerException);
            }
        }
    }
}
