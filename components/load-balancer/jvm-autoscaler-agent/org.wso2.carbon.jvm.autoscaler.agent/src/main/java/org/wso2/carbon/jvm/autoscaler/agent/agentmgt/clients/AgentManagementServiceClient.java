/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.jvm.autoscaler.agent.agentmgt.clients;

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.autoscaler.agentmgt.service.stub.AgentManagementServiceStub;

/**
 * This class acts as a client to AgentManagementService.
 * Communication happens through the AgentManagementServiceStub.
 */
public class AgentManagementServiceClient {

    private static final Log log = LogFactory.getLog(AgentManagementServiceClient.class);

    private static final String BACKEND_SERVICE = "AgentManagementService";

    private AgentManagementServiceStub stub;

    /**
     * Initiating AgentManagementService client.
     * 
     * @param backendServerURL
     *            URL up to service name (without name part).
     * @throws Exception
     *             throws when failed to initiate a client for AgentManagementService.
     */
    public AgentManagementServiceClient(String backendServerURL) throws Exception {

        String epr = backendServerURL + BACKEND_SERVICE;

        try {
            stub = new AgentManagementServiceStub(epr);

        } catch (RemoteException ex) {
            String msg = "Failed to initiate a client for AgentManagementService at " + epr + ".";
            log.error(msg, ex);
            throw new Exception(ex);
        }
    }

    /**
     * Calling AgentManagementService to get registered an epr.
     * 
     * @param epr
     *            EPR of the Agent.
     * @param instanceCount
     *            maximum number of instance that will be spawned in Agent.
     * @return what is returned by AgentManagementService.
     * @throws Exception
     *             if failed when registering Agent.
     */
    public boolean registerAgent(String epr, int instanceCount) throws Exception {

        return stub.registerAgent(epr, instanceCount);

    }

    /**
     * Calling AgentManagementService to get unregistered an epr.
     * 
     * @param epr
     *            EPR of the Agent.
     * @param instanceCount
     *            maximum number of instance that will be spawned in Agent.
     * @return what is returned by AgentManagementService.
     * @throws Exception
     *             if failed when unregistering Agent.
     */
    public boolean unregisterAgent(String epr, int instanceCount) throws Exception {

        return stub.unregisterAgent(epr, instanceCount);

    }

}