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
package org.wso2.carbon.bam.gauges.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.stub.listadmin.BAMListAdminServiceBAMException;
import org.wso2.carbon.bam.stub.listadmin.BAMListAdminServiceStub;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.ActivityDTO;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.ClientDTO;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.MonitoredServerDTO;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.NamespaceDTO;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.OperationDO;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.PropertyFilterDTO;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.ServiceDO;
import org.wso2.carbon.bam.util.BAMException;

import java.rmi.RemoteException;
import java.util.Locale;

public class BAMListAdminServiceClient {

    private static final Log log = LogFactory.getLog(BAMListAdminServiceClient.class);

    BAMListAdminServiceStub bamListAdminServiceStub;

    public BAMListAdminServiceClient(String cookie, String backendServerURL,
                                     ConfigurationContext configCtx,
                                     Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "BAMListAdminService";

        bamListAdminServiceStub = new BAMListAdminServiceStub(configCtx, serviceURL);
        ServiceClient client = bamListAdminServiceStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

    }

    public MonitoredServerDTO[] getServerList() throws BAMException {
        try {
            return bamListAdminServiceStub.getServerList();
        } catch (RemoteException e) {
            throw new BAMException("failed to get monitored server list", e);
        } catch (BAMListAdminServiceBAMException e) {
            throw new BAMException("failed to get monitored server list", e);
        }
    }

    public ServiceDO[] getServicesList(int serverId) throws BAMException {
        try {
            return bamListAdminServiceStub.getServiceList(serverId);
        } catch (RemoteException e) {
            throw new BAMException("failed to get services list for serverId : " + serverId, e);
        } catch (BAMListAdminServiceBAMException e) {
            throw new BAMException("failed to get monitored server list", e);
        }
    }

    public OperationDO[] getOperationList(int serviceId) throws BAMException {
        try {
            return bamListAdminServiceStub.getOperationList(serviceId);
        } catch (RemoteException e) {
            throw new BAMException(" failed to get operations for serviceId : " + serviceId, e);
        } catch (BAMListAdminServiceBAMException e) {
            throw new BAMException("failed to get monitored server list", e);
        }
    }

    public ActivityDTO[] getActivityList() throws BAMException {
        try {
            return bamListAdminServiceStub.getActivityList();
        } catch (RemoteException e) {
            throw new BAMException("failed to get activities", e);
        } catch (BAMListAdminServiceBAMException e) {
            throw new BAMException("failed to get monitored server list", e);
        }
    }

    public ClientDTO[] getClientList(int serverID) throws BAMException {
        try {
            return bamListAdminServiceStub.getClientList(serverID);
        } catch (RemoteException e) {
            throw new BAMException("failed to get client list for serverId : " + serverID, e);
        } catch (BAMListAdminServiceBAMException e) {
            throw new BAMException("failed to get monitored server list", e);
        }
    }

    public PropertyFilterDTO[] getXpathConfigurations(int serverId) throws BAMException {
        try {
            return bamListAdminServiceStub.getXpathConfigurations(serverId);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred getting xpath configurations for server id " + serverId, e);
        } catch (BAMListAdminServiceBAMException e) {
            throw new BAMException("failed to get monitored server list", e);
        }
    }

    public NamespaceDTO[] getNamespaces(int xpathId) throws BAMException {
        try {
            return bamListAdminServiceStub.getNamespaces(xpathId);
        } catch (RemoteException e) {
            throw new BAMException("Error occurred getting namespaces for xpath id " + xpathId, e);
        } catch (BAMListAdminServiceBAMException e) {
            throw new BAMException("Error occurred getting namespaces for xpath id " + xpathId, e);
        }
    }
}
