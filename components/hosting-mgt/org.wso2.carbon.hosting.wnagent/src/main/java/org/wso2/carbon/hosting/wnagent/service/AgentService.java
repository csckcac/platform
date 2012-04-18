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

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.hosting.wnagent.WNAgentConstants;
import org.wso2.carbon.hosting.wnagent.dto.ContainerInformation;
import org.wso2.carbon.hosting.wnagent.dto.PlanConfig;
import org.wso2.carbon.hosting.wnagent.exception.AgentServiceException;
import org.wso2.carbon.hosting.wnagent.util.PropertyFileReaderUtil;

/**
 * This web service is responsible for managing Linux Containers in the host machine (worker node) where the service
 * is deployed. 
 * 
 * Linux  Containers(LXC) are lightweight virtual machines, and are created from pre-created OS images 
 * with required products already installed, which is also called a template. 
 * 
 * Further this service is responsible for initiating registration / unregistration of worker nodes in 
 * AgentManagementService
 * 
 * 
 */
public class AgentService {

	private static final Log log = LogFactory.getLog(AgentService.class);

	private MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
	
	public AgentService() throws Exception {	
		PropertyFileReaderUtil.readAgentConfig();
    }
	
	
	/**
	 * 
	 * @param domainName
	 * @param userName This will be the tenant name, if not provided default value will be used
	 * @param containerInfo
	 * @return
	 * @throws AgentServiceException
	 * 
	 * Creates a container from a pre configured OS image template
	 * 
	 */
	public boolean createContainer(String domainName, String userName, ContainerInformation containerInfo)
																				throws AgentServiceException {
		
		if(log.isDebugEnabled()) {
			
		StringBuilder logmsg =
		                       new StringBuilder().append("Trying to create a container for tenant [")
		                                          .append(userName)
		                                          .append("] with Container ip [ ")
		                                          .append(containerInfo.getIp()).append("]");
		log.debug(logmsg);
		
		}

		// TODO validate mandatory parameters
		
		String template = getTemplateForDomain(domainName);
		PlanConfig plan = getPlanForType(containerInfo.getType());
	
		ProcessBuilder pbInit =
		                        new ProcessBuilder(WNAgentConstants.CONTAINER_ACTION,
		                                           WNAgentConstants.WSO2_HOSTING_HOME,
		                                           WNAgentConstants.CONTAINER_CREATE_ACTION,
		                                           userName,
		                                           getDefaultTenantPassword(),
		                                           containerInfo.getIp(),
		                                           containerInfo.getNetMask(),
		                                           containerInfo.getNetGateway(),
		                                           containerInfo.getBridge(),
		                                           containerInfo.getContainerRoot()
		                                           /*can read from the config file,
		                                            why passing it to database*/,
		                                           containerInfo.getContainerKeysFile(),
		                                           template,
		                                           plan.getMemory(),
		                                           plan.getSwap(),
		                                           plan.getCpuShares(),
		                                           plan.getCpuSets());
		pbInit.directory(new File(WNAgentConstants.WSO2_HOSTING_HOME));
		
		Process procInit;
		int exitVal = 0;
        try {
	        procInit = pbInit.start();
	        exitVal = procInit.waitFor();
	        
        } catch (Exception e) {
        	String msg = " Exception occurred in creating container. Reason :" + e.getMessage();
        	log.error(msg);
        	throw new AgentServiceException(msg);	        
        }
        
		if (exitVal == 0) {
			String msg = "Container is successfully created";
			log.debug(msg);
			
			// Check whether the remaining memory is enough for creating the largest container
			return isAvailableMemoryEnoughForMaxContainer();
		}
		
		String msg = "Container creation is failed";
		log.error(msg);		
        throw new AgentServiceException(msg);

	}

	
	/**
	 * 
	 * @param tenantName
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws AgentServiceException
	 * 
	 * Start the container. Starting the container means starting the application installed in the container.
	 * A container is normally started after creating it or, when it is in the
	 * stopped state and user try to access it again. Also the container start operation is invoked from 
	 * web UI directly
	 * 
	 */
	public int startContainer(String tenantName) throws IOException,
	                                                                   InterruptedException,
	                                                                   AgentServiceException {
		ProcessBuilder pbInit =
		                        new ProcessBuilder(WNAgentConstants.CONTAINER_ACTION,
		                                           WNAgentConstants.WSO2_HOSTING_HOME,
		                                           WNAgentConstants.CONTAINER_START_ACTION,
		                                           tenantName);
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
	 * @param tenantName
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws AgentServiceException
	 * 
	 * Stop the container. Stopping the container means shutting down the application instances running inside 
	 * the container. A container is normally stopped when the user idle for a predefined time period or specifically 
	 * stopped using the web UI
	 * 
	 */
	public int stopContainer(String tenantName) throws IOException,
	                                                                 InterruptedException,
	                                                                 AgentServiceException {

		ProcessBuilder pbInit =
		                        new ProcessBuilder(WNAgentConstants.CONTAINER_ACTION,
		                                           WNAgentConstants.WSO2_HOSTING_HOME,
		                                           WNAgentConstants.CONTAINER_STOP_ACTION,
		                                           tenantName);
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

		
	/**
	 * 
	 * @param tenantName
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws AgentServiceException
	 * 
	 * Destroy a container environment corresponding to a container user. 
	 * When this method is called the container will be deleted.
	 * 
	 */
	public int destroyContainer(String tenantName) throws IOException,
	                                                                    InterruptedException,
	                                                                    AgentServiceException {

		ProcessBuilder pbInit =
		                        new ProcessBuilder(WNAgentConstants.CONTAINER_ACTION,
		                                           WNAgentConstants.WSO2_HOSTING_HOME,
		                                           WNAgentConstants.CONTAINER_DESTROY_ACTION,
		                                           tenantName);
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

	
	
	
	private String getDefaultTenantPassword() {
	    return PropertyFileReaderUtil.readDefaultPassword();
    }

	
	private PlanConfig getPlanForType(String type) {	    
	    return PropertyFileReaderUtil.readPlanForType(type);
    }

	/**
	 * 
	 * Checks whether available memory is enough to create the largest 
	 * container 
	 * 
	 * @return
	 */
	private boolean isAvailableMemoryEnoughForMaxContainer() {
		
		long maxContainerMemory = PropertyFileReaderUtil.getMaxMemoryContainer();
		long freeMemory;
        try {
	        freeMemory = calculateFreeMemory();
        } catch (Exception e) {        	
        	log.error("Failed to calculate free system memory", e);
	        return false;
        } 
		
		if(log.isDebugEnabled()){
			log.debug(" Free Memory [" + freeMemory +"]. MaxContainerMemory ["+ maxContainerMemory +"]");
		}
		
		if(freeMemory > maxContainerMemory) {
			return true;
		}
		return false;
    }


	private long calculateFreeMemory() throws Exception {
	    
		ObjectName osBean = null;
		String freeMemory = null;

		osBean = new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
		freeMemory = mBeanServer.getAttribute(osBean, "FreePhysicalMemorySize").toString();
		return Long.parseLong(freeMemory);
    }


	/**
	 * Returns the corresponding template name for the domain
	 * 
	 * @param domainName
	 * @return
	 * 
	 */
	private String getTemplateForDomain(String domainName) {
	    return PropertyFileReaderUtil.readTemplateForDomain(domainName);
    }

	
	public void registerInAgentManagementService() {

		// Invokes WorkerNodeRegistration Service method..
		// With the machine details and
		
		
		/*try {
			WorkerNodeRegistrationServiceStub workerNodeRegStub =
			                                                      new WorkerNodeRegistrationServiceStub(getEpr());
			workerNodeRegStub.registerWorkerNode(getWorkerNode(), getZoneResourcePlan());

		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}*/

	}
	
	public void unregisterInAgentManagementService() {
		
	}

	private String getEpr() {
		return PropertyFileReaderUtil.readAgentMgtServiceEpr();
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
