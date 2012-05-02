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
import org.wso2.carbon.hosting.wnagent.stub.services.AgentServiceAgentServiceException;
import org.wso2.carbon.hosting.wnagent.stub.services.AgentServiceIOException;
import org.wso2.carbon.hosting.wnagent.stub.services.AgentServiceInterruptedException;
import org.wso2.carbon.hosting.wnagent.stub.services.AgentServiceStub;
import org.wso2.carbon.hosting.wnagent.stub.services.xsd.common.ContainerInformation;

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
    
    /**
     * 
     * 
     */
    public boolean startContainerInstance(String domainName,
			org.wso2.carbon.lb.common.dto.ContainerInformation containerInfo) throws Exception {
    	return stub.createAndStartContainer(domainName, convertContainerInformation(containerInfo));    	
    }

    // This method converts a dto.ContainerInformation object to a xsd.ContainerInformation object
   private ContainerInformation convertContainerInformation(
			org.wso2.carbon.lb.common.dto.ContainerInformation containerInfo) {
		ContainerInformation info = new ContainerInformation();
		info.setBridge(containerInfo.getBridge());
		info.setContainerId(containerInfo.getContainerId());
		info.setContainerKeysFile(containerInfo.getContainerKeysFile());
		info.setContainerRoot(containerInfo.getContainerRoot());
		info.setIp(containerInfo.getIp());
		info.setNetGateway(containerInfo.getNetGateway());
		info.setNetMask(containerInfo.getNetMask());
		info.setType(containerInfo.getType());
		return info;
	}

	public boolean terminateContainerInstance(String instanceId, String containerRoot ) throws Exception {
		return stub.stopAndDestroyContainer(instanceId, containerRoot);
    }
    
	public int getNumberOfInstances() throws RemoteException {

		//return stub.getNumberOfInstances();
		return 0;
	}

}
