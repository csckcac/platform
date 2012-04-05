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

package org.wso2.carbon.bam.core.admin;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.core.clients.OperationAdminClient;
import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;
import org.wso2.carbon.bam.core.clients.ServiceGroupAdminClient;
import org.wso2.carbon.bam.core.util.ClientAuthHandler;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.operation.mgt.stub.types.OperationMetaData;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceGroupMetaData;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to fetch details about services/operations of a given server.
 */
public class MonitoredServerServiceInfoAdmin {

    private static Log log = LogFactory.getLog(MonitoredServerServiceInfoAdmin.class);

    public String[] getServiceNames(ServerDO server) throws BAMException {
        String[] services;
        try {
            services = getServiceNamesInternal(server);
        } catch (AxisFault axisFault) {
            if (ClientAuthHandler.checkAuthException(axisFault)) {
                ClientAuthHandler.getClientAuthHandler().authenticateForcefully(server);
                try {
                    services = getServiceNamesInternal(server);
                } catch (RemoteException e) {
                    throw new BAMException("Invalid credentials provided for " + server.getServerURL()) ;
                }
            } else {
                throw new BAMException("Unable to retrieve services list from server [" + server.getServerURL() + "]", axisFault);
            }
        } catch (RemoteException e) {
            throw new BAMException("Unable to retrieve services list from server [" + server.getServerURL() + "]", e);
        }

        return services;
    }

    public String[] getOperationNames(ServerDO server, String serviceName) throws BAMException {
        String[] operations;
        try {
            operations = getOperationNamesInternal(server, serviceName);
        } catch (AxisFault axisFault) {
            if (ClientAuthHandler.checkAuthException(axisFault)) {
                ClientAuthHandler.getClientAuthHandler().authenticateForcefully(server);
                try {
                    operations = getServiceNamesInternal(server);
                } catch (RemoteException e) {
                    log.error("Invalid credential for server " + server.getServerURL());
                    throw new BAMException("Invalid credentials provided!");
                }
            } else {
                throw new BAMException("Unable to retrieve services list from server [" + server.getServerURL() + "]", axisFault);
            }
        } catch (RemoteException e) {
            throw new BAMException("Unable to retrieve services list from server [" + server.getServerURL() + "]", e);
        }
        return operations;
    }


    private String[] getOperationNamesInternal(ServerDO server, String serviceName) throws RemoteException, BAMException {
        String sessionCookie = ClientAuthHandler.getClientAuthHandler().getSessionString(server);
        OperationAdminClient client = new OperationAdminClient(server.getServerURL(), sessionCookie);
        OperationMetaData[] mData = client.getAllOperations(serviceName);


        List<String> ops = new ArrayList<String>();
        for (OperationMetaData opData : mData) {
            ops.add(opData.getName());
        }
        return ops.toArray(new String[ops.size()]);
    }

    private String[] getServiceNamesInternal(ServerDO server) throws BAMException, RemoteException {
        String sessionCookie = ClientAuthHandler.getClientAuthHandler().getSessionString(server);
        ServiceGroupAdminClient client = new ServiceGroupAdminClient(server.getServerURL(), sessionCookie);
        ServiceGroupMetaData[] mData = client.getAllServiceGroups();
        List<String> services = new ArrayList<String>();
        for (ServiceGroupMetaData sgData : mData)
            for (ServiceMetaData sData : sgData.getServices())
                services.add(sData.getName());

        return services.toArray(new String[services.size()]);
    }


}