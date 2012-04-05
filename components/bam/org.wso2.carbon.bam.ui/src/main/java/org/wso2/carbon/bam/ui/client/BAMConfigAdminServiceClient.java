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

package org.wso2.carbon.bam.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.stub.configadmin.BAMConfigAdminServiceStub;
import org.wso2.carbon.bam.stub.configadmin.types.carbon.MonitoredServerDTO;
import org.wso2.carbon.bam.stub.configadmin.types.carbon.ServerDO;
import org.wso2.carbon.bam.ui.BAMUIConstants;

public class BAMConfigAdminServiceClient {
    private static final Log log = LogFactory.getLog(BAMConfigAdminServiceClient.class);

    private BAMConfigAdminServiceStub stub;

    public BAMConfigAdminServiceClient(String cookie, String backendServerURL, ConfigurationContext configCtx)
            throws AxisFault {
        String serviceURL = backendServerURL + BAMUIConstants.BAM_CONFIG_ADMIN_SERVICE;
        stub = new BAMConfigAdminServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public int addServer(ServerDO server) throws AxisFault {
        int statesOfAddedServer=-1;
        try {
            statesOfAddedServer=stub.addServer(server);
        } catch (Exception e) {
            handleException("Error while adding the new monitored server entry", e);
        }
        return statesOfAddedServer;
    }

    public void removeServer(MonitoredServerDTO dto) throws AxisFault {
        try {
            stub.removeServer(dto);
        } catch (Exception e) {
            handleException("Error while removing the new monitored server entry", e);
        }
    }

    public void updateServer(ServerDO server) throws AxisFault {
        try {
            stub.updateServer(server);
        } catch (Exception e) {
            handleException("Error while updating the monitored server entry", e);
        }
    }

    public ServerDO[] getServerList() throws AxisFault {
        try {
            return stub.getServerList();
        } catch (Exception e) {
            handleException("Error while requesting monitored servers list", e);
        }
        return new ServerDO[]{};
    }

    public ServerDO getServer(int serverId) throws AxisFault {
        try {
            return stub.getServerDetails(serverId);
        } catch (Exception e) {
            handleException("Error while retrieving server details. ", e);
        }

        return null;
        
    }

    public void deactivateServer(int serverID) throws AxisFault {
        try {
            stub.deactivateServer(serverID);
        } catch (Exception e) {
            handleException("Error while deactivating server", e);
        }
    }

    public void activateServer(int serverID, String subscriptionID) throws AxisFault {
        try {
            stub.activateServer(serverID, subscriptionID);
        } catch (Exception e) {
            handleException("Error while deactivating server", e);
        }
    }

    public String getDataRetentionPeriod() throws AxisFault {
        try {
            return stub.getDataRetentionPeriod();
        } catch (Exception e) {
            handleException("Error while requesting the data retention period from server", e);
        }
        return null;
    }

    public String getDataArchivalPeriod() throws AxisFault {
        try {
            return stub.getDataArchivalPeriod();
        } catch (Exception e) {
            handleException("Error while requesting the data archival period from server", e);
        }
        return null;
    }

    public void updateDataRetentionPeriod(String timeRange) throws AxisFault {
        try {
            stub.setDataRetentionPeriod(timeRange);
        } catch (Exception e) {
            handleException("Error while updating the the data retention period", e);
        }
    }

    public void updateDataArchivalPeriod(String timeRange) throws AxisFault {
        try {
            stub.setDataArchivalPeriod(timeRange);
        } catch (Exception e) {
            handleException("Error while updating the the data archival period", e);
        }
    }

    public String subscribe(String topic, String brokerURL, String subscriberURL, String serverURL, String userName, String password)
            throws AxisFault {
        try {
            return stub.subscribe(topic, brokerURL, subscriberURL, serverURL, userName, password);
        } catch (Exception e) {
            handleException("Error occurred while subscribing to " + brokerURL, e);
        }
        return null;
    }

    public void unsubscribe(String brokerURL, String subscriptionId, String serverType, String serverURL)
            throws AxisFault {
        try {
            stub.unsubscribe(brokerURL, subscriptionId, serverType, serverURL);
        } catch (Exception e) {
            handleException("Error occurred while un-subscribing from " + brokerURL, e);
        }
    }

    /**
     * Logs and wraps the given exception.
     *
     * @param msg Error message
     * @param e   Exception
     * @throws AxisFault
     */
    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
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
}
