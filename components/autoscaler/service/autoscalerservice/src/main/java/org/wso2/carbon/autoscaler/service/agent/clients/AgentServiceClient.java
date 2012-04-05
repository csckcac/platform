/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.autoscaler.service.agent.clients;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.autoscaler.agent.service.stub.AgentServiceStub;

/**
 * Client to communicate with Agent Service through Agent Service Stub.
 */
public class AgentServiceClient {

    private static final Log log = LogFactory.getLog(AgentServiceClient.class);

    private AgentServiceStub stub;

    public AgentServiceClient(String epr) throws AxisFault {

        try {
            stub = new AgentServiceStub(epr);

        } catch (AxisFault axisFault) {
            String msg =
                "Failed to initiate AgentService client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new AxisFault(msg, axisFault);
        }
    }

    public boolean startInstance(String domainName, String instanceId) throws Exception {

        return stub.startInstance(domainName, instanceId);
    }

    public boolean terminateInstance(String instanceId) throws Exception {

        return stub.terminateInstance(instanceId);
    }
    
    public int getNumberOfInstances() throws RemoteException {
        
        return stub.getNumberOfInstances();
    }

}
