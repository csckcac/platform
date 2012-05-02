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
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.autoscaler.service.jvm.agentmgt.stub.AgentManagementServiceStub;
import org.wso2.carbon.autoscaler.service.jvm.agentmgt.stub.beans.xsd.dto.Bridge;
import org.wso2.carbon.autoscaler.service.jvm.agentmgt.stub.beans.xsd.dto.HostMachine;
import org.wso2.carbon.hosting.wnagent.WNAgentConstants;
import org.wso2.carbon.hosting.wnagent.beans.PlanConfig;
import org.wso2.carbon.hosting.wnagent.exception.AgentServiceException;
import org.wso2.carbon.hosting.wnagent.util.PropertyFileReaderUtil;
import org.wso2.carbon.hosting.wnagent.util.ServerStartupDetector;
import org.wso2.carbon.lb.common.dto.ContainerInformation;


/**
 * This web service is responsible for managing Linux Containers in the host machine where
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
		if(log.isDebugEnabled()){
			log.debug("Property file is successfully read");
		}
    }
	
	
	/**
	 * 
	 * @param domainName
	 * @param containerInfo
	 * @return true if container is created successfully
	 * and also, further containers can be created in the same host machine
	 * false, if container is created successfully but 
	 * further containers cannot be created in the host machine.
	 * @throws AgentServiceException if container creation is failed
	 * 
	 * Creates a container from a pre configured OS image template
	 * 
	 */
	public boolean createContainer(String domainName, ContainerInformation containerInfo)
																				throws AgentServiceException {
    	
    	if(log.isDebugEnabled()) {    		
    		StringBuilder logmsg =
                new StringBuilder().append("Trying to create a container with id [")
                                   .append(containerInfo.getContainerId())
                                   .append("]");
    		log.debug(logmsg);
    	}
		
		return invokeContainerCreateProcess(domainName, containerInfo);

	}


	
	
	/**
	 * 
	 * @param containerName
	 * @return
	 * @throws AgentServiceException
	 * 
	 * Start the container. Starting the container means starting the application installed in the container.
	 * A container is normally started after creating it or, when it is in the
	 * stopped state and user try to access it again. Also the container start operation is invoked from 
	 * web UI directly
	 * 
	 */
	public boolean startContainer(String containerName, String containerRoot) throws AgentServiceException {
		
		if (log.isDebugEnabled()) {

			String msg =
			             new StringBuilder().append("Trying to start container [")
			                                .append(containerName).append("]").toString();

			log.debug(msg);
		}
		
		return invokeContainerStartProcess(containerName, containerRoot);
	}


	
	/**
	 * 
	 * @param domainName
	 * @param containerInfo
	 * @return true if container is created and started successfully
	 * and also, further containers can be created in the same host machine
	 * false, if container is created and started successfully but 
	 * further containers cannot be created in the host machine.
	 * @throws AgentServiceException if the container creation of start operatoin is
	 * failed 
	 * 
	 * This operation creates a container from a pre configured OS image template and starts it
	 * 
	 */
	public boolean createAndStartContainer(String domainName, ContainerInformation containerInfo) throws AgentServiceException{
		
		boolean canCreateMoreContainers =
		                                  invokeContainerCreateProcess(domainName,containerInfo);
		try {
			invokeContainerStartProcess(containerInfo.getContainerId(), containerInfo.getContainerRoot());
		} catch (AgentServiceException e) {
			// If container failed to start, destroy it
			invokeContainerDestroyProcess(containerInfo.getContainerId(), containerInfo.getContainerRoot());
			throw e;
		}
		
		ServerStartupDetector detector = new ServerStartupDetector(this, containerInfo, domainName);
		// we do not want to block this thread, hence use start()
		detector.start();

		return canCreateMoreContainers;
	}
	
	
	
	/**
     * @param domainName
     * @param containerInfo
     * @return
     * @throws AgentServiceException
     * 
     */
    private boolean invokeContainerCreateProcess(String domainName,
                                                 ContainerInformation containerInfo)
                                                                                    throws AgentServiceException {
    	String template = getTemplateForDomain(domainName);
		PlanConfig plan = getPlanForType(containerInfo.getType());
	
		// TODO Add default user name parameter to ProcessBuilder, because UUID pattern
		// is not allowed as Linux user names
		ProcessBuilder pbInit =
		                        new ProcessBuilder(WNAgentConstants.CONTAINER_ACTION,
		                                           getScriptsPath(),
		                                           WNAgentConstants.CONTAINER_CREATE_ACTION,
		                                           containerInfo.getContainerId(),
		                                           getDefaultContainerPassword(),
		                                           containerInfo.getIp(), /*ContainerIp*/
		                                           containerInfo.getNetMask(), /*NetMask*/
		                                           containerInfo.getBridge(), /*BridgeIp*/
		                                           containerInfo.getNetGateway(), /*Bridge name (br-lxc)*/
		                                           containerInfo.getContainerRoot(),
		                                           "tst",
		                                           template,
		                                           plan.getMemory(),
		                                           plan.getSwap(),
		                                           plan.getCpuShares(),
		                                           plan.getCpuSets(),
		                                           "sss" /*svn location*/,
		                                           "sss");
		if (log.isDebugEnabled()) {
			log.debug("*** Logging create container commands ***" + pbInit.command());
		}
		pbInit.directory(new File(getScriptsPath()));
		
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
     * @param containerName
     * @param containerRoot
     * @return
     * @throws AgentServiceException
     */
    private boolean invokeContainerStartProcess(String containerName, String containerRoot)
                                                                                       throws AgentServiceException {
	    boolean containerStartStatus = false;
    	
    	ProcessBuilder pbInit =
		                        new ProcessBuilder(WNAgentConstants.CONTAINER_ACTION,
		                                           getScriptsPath(),
		                                           WNAgentConstants.CONTAINER_START_ACTION,
		                                           containerName,
		                                           containerRoot);
		pbInit.directory(new File(getScriptsPath()));
		Process procInit = null;
		int exitVal = 0;
        try {
	        procInit = pbInit.start();
	        exitVal = procInit.waitFor();	        
        } catch (Exception e) {
        	String msg = " Exception occurred in starting container. Reason :" + e.getMessage();
        	log.error(msg);
	        throw new AgentServiceException(msg, e);
        }

		if (exitVal == 0) {
			containerStartStatus = true;
			String msg = new StringBuilder()
							.append("Container is started successfully with UserName [")
							.append(containerName)
							.append("]").toString();
			if(log.isDebugEnabled())
				log.debug(msg);
		} else {
			String msg = new StringBuilder().append("Exception is occurred when starting container ")
							.append(containerName).toString();
			if(log.isDebugEnabled())
				log.debug(msg);
			throw new AgentServiceException(msg);
		}
		
		return containerStartStatus;
    }
	
	

	
	/**
	 * 
	 * @param containerName
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
	public boolean stopContainer(String containerName, String containerRoot) throws AgentServiceException {

		
		if (log.isDebugEnabled()) {
			String msg = new StringBuilder().append("Trying to stop container [")
			                                .append(containerName).append("]").toString();
			log.debug(msg);
		}
		
		return invokeContainerStopProcess(containerName, containerRoot);
	}


	/**
     * @param containerName
     * @param containerRoot
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws AgentServiceException
     */
    private boolean invokeContainerStopProcess(String containerName, String containerRoot) throws AgentServiceException {
    	
    	int exitVal = 0;
    	boolean containerStopStatus = false;
		try {
			ProcessBuilder pbInit =
			                        new ProcessBuilder(WNAgentConstants.CONTAINER_ACTION,
			                                           getScriptsPath(),
			                                           WNAgentConstants.CONTAINER_STOP_ACTION,
			                                           containerName, containerRoot);
			pbInit.directory(new File(getScriptsPath()));
			Process procInit = pbInit.start();
			exitVal = procInit.waitFor();

		} catch (Exception ex) {
			String msg = " Exception occurred in starting container. Reason :" + ex.getMessage();
			log.error(msg);
			throw new AgentServiceException(msg, ex);
		}
		
		if (exitVal == 0) {
			containerStopStatus = true;
			String msg =
			             new StringBuilder().append("Container is stopped successfully with UserName [")
			                                .append(containerName).append("]").toString();
			if (log.isDebugEnabled())
				log.debug(msg);
		} else {
			String msg =
			             new StringBuilder().append("Exception is occurred when stopping container ")
			                                .append(containerName).toString();
			if (log.isDebugEnabled())
				log.debug(msg);
			throw new AgentServiceException(msg);
		}
		return containerStopStatus;
    }

		
	/**
	 * 
	 * @param containerName
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws AgentServiceException
	 * 
	 * Destroy a container environment corresponding to a container user. 
	 * When this method is called the container will be deleted.
	 * 
	 */
	public boolean destroyContainer(String containerName, String containerRoot) throws IOException,
	                                                                    InterruptedException,
	                                                                    AgentServiceException {

		if (log.isDebugEnabled()) {

			String msg =
			             new StringBuilder().append("Trying to destroy container [")
			                                .append(containerName).append("]").toString();

			log.debug(msg);
		}
		
		return invokeContainerDestroyProcess(containerName, containerRoot);
	}


	/**
     * @param containerName
     * @param containerRoot
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws AgentServiceException
     */
    private  boolean invokeContainerDestroyProcess(String containerName, String containerRoot)
                                                                                         throws AgentServiceException {
	    
    	boolean containerDestroyStatus = false; 
    	int exitVal = 0;
    	try {
    	ProcessBuilder pbInit =
		                        new ProcessBuilder(WNAgentConstants.CONTAINER_ACTION,
		                                           getScriptsPath(),
		                                           WNAgentConstants.CONTAINER_DESTROY_ACTION,
		                                           containerName,
		                                           containerRoot);
		pbInit.directory(new File(getScriptsPath()));
		Process procInit = pbInit.start();
		exitVal = procInit.waitFor();
    	} catch (Exception e) {
    		String msg = " Exception occurred in destroying container. Reason :" + e.getMessage();
			log.error(msg);
			throw new AgentServiceException(msg, e);
		}
    	
		if (exitVal == 0) {
			containerDestroyStatus = true;
			String msg =
			             new StringBuilder().append("Container is destroyed successfully with UserName [")
			                                .append(containerName).append("]").toString();
			if (log.isDebugEnabled())
				log.debug(msg);
		} else {
			String msg =
			             new StringBuilder().append("Exception is occurred when destroying container ")
			                                .append(containerName).toString();
			if (log.isDebugEnabled())
				log.debug(msg);
			throw new AgentServiceException(msg);
		}
		return containerDestroyStatus;
    }	

	
	/**
	 * 
	 * @param containerName
	 * @param containerRoot
	 * @return
	 * @throws AgentServiceException
	 */
    public boolean stopAndDestroyContainer(String containerName, String containerRoot) throws AgentServiceException {
    	
    	// TODO limitation : if stopContainer is successful and destroyProcess gets failed, this will return an exception
    	// so the controller thinks this container (instance) is not yet destroyed properly, hence will invoke this 
    	// method with the same containerName (instanceId). Now, containerStop operation will fail because the container (instance) 
    	// is already stopped. So, the container never gets destroyed
    	// To avoid that check whether containerStopProcess returns an explicit exception of container already stopped exception, 
    	// and handle accordingly
    	
    	invokeContainerStopProcess(containerName, containerRoot);
    	return invokeContainerDestroyProcess(containerName, containerRoot);
    }
    
	/**
	 * This method is responsible for registering a Host Machine in AgentManagementService, and this is invoked
	 * using a startup script
	 * @throws Exception 
	 */
	public void registerInAgentManagementService() throws Exception {
		
		try {
			String agentMgtUrl = PropertyFileReaderUtil.readAgentMgtServiceEpr();
        	//log.info(" ** AgentMgmntService Url :" + agentMgtUrl);
	        AgentManagementServiceStub stub = new AgentManagementServiceStub(agentMgtUrl);
	        
	        org.wso2.carbon.lb.common.dto.HostMachine hostMachineConfig = PropertyFileReaderUtil.readHostMachineConfig();
	        HostMachine hostMachine = new HostMachine();
	        hostMachine.setAvailable(hostMachineConfig.isAvailable());
	        hostMachine.setContainerRoot(hostMachineConfig.getContainerRoot());
	        hostMachine.setBridges(convertBridgeListToArray(hostMachineConfig));
	        hostMachine.setEpr(getServiceUrl());
	        hostMachine.setIp(hostMachineConfig.getIp());
	        hostMachine.setZone(hostMachineConfig.getZone());
	        String[] domainList = convertDomainListToArray(PropertyFileReaderUtil.readDomainList());
	        stub.registerAgent(hostMachine, domainList);
	        
        } catch (Exception e) {
	        log.error("Exception is occurred when registering host machine. Reason "+e.getMessage());
	        throw e;
        }

	}

	/**
	 * Unregisters an agent from the system
	 * @throws Exception 
	 */
	public void unregisterInAgentManagementService() throws Exception {
		
		AgentManagementServiceStub stub;
        try {
        	String agentMgtUrl = PropertyFileReaderUtil.readAgentMgtServiceEpr();
        	//log.info(" ** AgentMgmntService Url :" + agentMgtUrl);
	        stub = new AgentManagementServiceStub(agentMgtUrl);
	        stub.unregisterAgent(getServiceUrl());
        } catch (Exception e) {
        	log.error("Exception is occurred when un-registering host machine. Reason "+e.getMessage());
	        throw e;
        }
		
	}
	
	/**
     * @return the path where the container management scripts are located
     * Location is <CARBON_HOME>/bin directory 
     *  
     */
    private String getScriptsPath() {
	    return new StringBuilder().append(WNAgentConstants.CARBON_HOME).append(File.separator).append("bin").toString(); 
	    //return "/home/wso2/work/Tropos/wso2_container_image/bin";
	}
	
	private String getDefaultContainerPassword() {
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

	

	private String[] convertDomainListToArray(List<String> domainList) {
		return domainList.toArray(new String[domainList.size()]);
    }


	/**
     * @param workerNode
     * @return
     */
    private Bridge[] convertBridgeListToArray(org.wso2.carbon.lb.common.dto.HostMachine workerNode) {
    	
    	List<Bridge> bridgeList = new ArrayList<Bridge>();
    	
    	org.wso2.carbon.lb.common.dto.Bridge[] bridgeArray = workerNode.getBridges();
    	
    	for (int i = 0; i < bridgeArray.length; i++) {
    		
    		org.wso2.carbon.lb.common.dto.Bridge bridgeConf = bridgeArray[i];
	        Bridge bridge = new Bridge();
	        bridge.setAvailable(bridgeConf.isAvailable());
	        bridge.setBridgeIp(bridgeConf.getBridgeIp());
	        bridge.setCurrentCountIps(bridgeConf.getCurrentCountIps());
	        bridge.setHostMachine(getServiceUrl());
	        bridge.setMaximumCountIps(bridgeConf.getMaximumCountIps());
	        bridge.setNetGateway(bridgeConf.getNetGateway());
	        bridge.setNetMask(bridgeConf.getNetMask());
	        bridgeList.add(bridge);
        }    	
    	
	    return bridgeList.toArray(new Bridge[bridgeList.size()]);
    }
	
	


	public String getServiceUrl() {
		String baseURL = null;
		MessageContext messageContext = MessageContext.getCurrentMessageContext();
		AxisConfiguration configuration =
		                                  messageContext.getConfigurationContext()
		                                                .getAxisConfiguration();
		TransportInDescription inDescription = configuration.getTransportIn("https");
		
		EndpointReference[] eprs = null;
		try {
			eprs =
			                           inDescription.getReceiver()
			                                        .getEPRsForService(messageContext.getAxisService()
			                                                                         .getName(),
			                                                           null);
			//log.info("EPR count : " + eprs.length);
			baseURL = eprs[0].getAddress();
		} catch (AxisFault axisFault) {
		}
		
		log.info("*** Service URL of AgentService is : " + baseURL);
        		
		return baseURL;
	}

}
