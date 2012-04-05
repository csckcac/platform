/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.hosting.wnagent.service;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.hosting.wnagent.WNAgentConstants;
import org.wso2.carbon.hosting.wnagent.dto.ContainerInformation;
import org.wso2.carbon.hosting.wnagent.exception.WNAgentException;
import org.wso2.carbon.hosting.wnagent.util.PropertyFileReaderUtil;
import org.wso2.carbon.hosting.wnregistration.stub.services.WorkerNodeRegistrationServiceStub;
import org.wso2.carbon.hosting.wnregistration.stub.services.xsd.dto.WorkerNode;
import org.wso2.carbon.hosting.wnregistration.stub.services.xsd.dto.ZoneResourcePlan;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

/**
 * In this class Stratos Worker Node Agent related stuff defined.
 * 
 * Creating/destroying a container, starting/stopping a created container are
 * covered in this class. Container is a
 * physical or virtual machine allocated to a tenant.
 * 
 */

public class WorkerNodeAgentService {

	private static final Log log = LogFactory.getLog(WorkerNodeAgentService.class);

	/**
	 * 
	 * @param containerInfo
	 *            container information
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * 
	 *             Create a container corresponding to the container user. This
	 *             method will be called when the tenant user
	 *             upload his first application.
	 */
	public int createContainer(String tenantName, String tenantPassword,
	                           ContainerInformation containerInfo) throws IOException,
	                                                              InterruptedException,
	                                                              WNAgentException {

		StringBuilder logmsg =
		                       new StringBuilder().append("Starting to create a container for tenant [")
		                                          .append(tenantName)
		                                          .append("] with Container ip [ ")
		                                          .append(containerInfo.getIp()).append("]");
		log.info(logmsg);

		ProcessBuilder pbInit =
		                        new ProcessBuilder(WNAgentConstants.CONTAINER_ACTION,
		                                           WNAgentConstants.WSO2_HOSTING_HOME,
		                                           WNAgentConstants.CONTAINER_CREATE_ACTION,
		                                           tenantName, tenantPassword,
		                                           containerInfo.getIp(),
		                                           containerInfo.getNetMask(),
		                                           containerInfo.getNetGateway(),
		                                           containerInfo.getBridge(),
		                                           containerInfo.getContainerRoot(),
		                                           containerInfo.getJailKeysFile(),
		                                           containerInfo.getTemplate(),
		                                           containerInfo.getMemory(),
		                                           containerInfo.getSwap(),
		                                           containerInfo.getCpuShares(),
		                                           containerInfo.getCpuSetShares());
		pbInit.directory(new File(WNAgentConstants.WSO2_HOSTING_HOME));
		Process procInit = pbInit.start();
		int exitVal = procInit.waitFor();
		if (exitVal == 0) {
			String msg = "Hosting container create executed successfully";
			log.debug(msg);
		} else {
			String msg = "Hosting container create execution failed";
			log.debug(msg);
			throw new WNAgentException(msg);
		}
		return exitVal;
	}

	/**
	 * 
	 * @param tenantName
	 *            tenant user
	 * @param containerRoot
	 *            This is root of the file system where containers will be
	 *            created
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * 
	 *             Destroy a container environment corresponding to a container
	 *             user. When this method is called the container
	 *             will be deleted.
	 */
	public int destroyContainer(String tenantName, String containerRoot) throws IOException,
	                                                                    InterruptedException,
	                                                                    WNAgentException {

		ProcessBuilder pbInit =
		                        new ProcessBuilder(WNAgentConstants.CONTAINER_ACTION,
		                                           WNAgentConstants.WSO2_HOSTING_HOME,
		                                           WNAgentConstants.CONTAINER_DESTROY_ACTION,
		                                           tenantName, containerRoot);
		pbInit.directory(new File(WNAgentConstants.WSO2_HOSTING_HOME));
		Process procInit = pbInit.start();
		int exitVal = procInit.waitFor();
		if (exitVal == 0) {
			String msg = "Hosting container destroy executed successfully";
			log.debug(msg);
		} else {
			String msg = "Hosting container destroy execution failed";
			log.debug(msg);
		}
		return exitVal;
	}

	/**
	 * Start the container. Starting the container
	 * means starting the lamp applications. A container is normally started
	 * after creating it or, when it is in the
	 * stopped state and user try to access it again.
	 * 
	 * @param tenantName
	 *            tenant user
	 * @param containerRoot
	 *            This is root of the file system where containers will be
	 *            created
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * 
	 */
	public int startContainer(String tenantName, String containerRoot) throws IOException,
	                                                                  InterruptedException,
	                                                                  WNAgentException {
		ProcessBuilder pbInit =
		                        new ProcessBuilder(WNAgentConstants.CONTAINER_ACTION,
		                                           WNAgentConstants.WSO2_HOSTING_HOME,
		                                           WNAgentConstants.CONTAINER_START_ACTION,
		                                           tenantName, containerRoot);
		pbInit.directory(new File(WNAgentConstants.WSO2_HOSTING_HOME));
		Process procInit = pbInit.start();
		int exitVal = procInit.waitFor();
		if (exitVal == 0) {
			String msg = "Hosting container start action executed successfully";
			log.debug(msg);
		} else {
			String msg = "Hosting container start action execution failed";
			log.debug(msg);
		}
		return exitVal;
	}

	/**
	 * 
	 * Stop the container. Stopping the container
	 * means shutdown the lamp applications. A container is normally stopped
	 * when the user idle for a predefined time
	 * period.
	 * 
	 * @param tenantName
	 *            tenant user
	 * @param containerRoot
	 *            This is root of the file system where containers will be
	 *            created
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * 
	 */
	public int stopContainer(String tenantName, String containerRoot) throws IOException,
	                                                                 InterruptedException,
	                                                                 WNAgentException {

		ProcessBuilder pbInit =
		                        new ProcessBuilder(WNAgentConstants.CONTAINER_ACTION,
		                                           WNAgentConstants.WSO2_HOSTING_HOME,
		                                           WNAgentConstants.CONTAINER_STOP_ACTION,
		                                           tenantName, containerRoot);
		pbInit.directory(new File(WNAgentConstants.WSO2_HOSTING_HOME));
		Process procInit = pbInit.start();
		int exitVal = procInit.waitFor();
		if (exitVal == 0) {
			String msg = "Hosting container stop action executed successfully";
			log.debug(msg);
		} else {
			String msg = "Hosting container stop action execution failed";
			log.debug(msg);
		}
		return exitVal;
	}

	public void registerWorkerNode() {

		// Invokes WorkerNodeRegistration Service method..
		// With the machine details and
		
		try {
			WorkerNodeRegistrationServiceStub workerNodeRegStub =
			                                                      new WorkerNodeRegistrationServiceStub(
			                                                                                            "");
			workerNodeRegStub.registerWorkerNode(getWorkerNode(),getZoneResourcePlan());

		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @return
	 */
	private WorkerNode getWorkerNode() {
		return PropertyFileReaderUtil.readWorkerNodeFromFile();
	}

	/**
	 * 
	 * @return
	 */
	private ZoneResourcePlan getZoneResourcePlan() {
		return PropertyFileReaderUtil.readZoneResourcePlanFromFile();
	}

	private String getServiceUrl() {
		String baseURL = null;
		MessageContext messageContext = MessageContext.getCurrentMessageContext();
		AxisConfiguration configuration =
		                                  messageContext.getConfigurationContext()
		                                                .getAxisConfiguration();
		TransportInDescription inDescription = configuration.getTransportIn("http");
		try {
			EndpointReference[] eprs =
			                           inDescription.getReceiver()
			                                        .getEPRsForService(messageContext.getAxisService()
			                                                                         .getName(),
			                                                           null);
			baseURL = eprs[0].getAddress();
		} catch (AxisFault axisFault) {
		}

		return baseURL;
	}

}
