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
package org.wso2.carbon.autoscaler.service.adapters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.autoscaler.agentmgt.service.IAgentManagementService;
import org.wso2.carbon.autoscaler.service.agent.clients.AgentServiceClient;
import org.wso2.carbon.autoscaler.service.internal.AgentManagementDSHolder;
import org.wso2.carbon.autoscaler.service.recovery.JVMAgentServiceRecoverer;

/**
 * JVM specific Adapter, which handles spawning/ terminating JVM instances.
 */
public class JVMAdapter extends Adapter{

	
	/**
	 * Pointer to the AgentManagementService
	 */
	private IAgentManagementService agentManagementService;

	/**
	 * Name of the Adapter.
	 */
	private static final String ADAPTER_NAME = "jvm";
	
	private static final Log log = LogFactory.getLog(JVMAdapter.class);
	
	/**
     * Key - InstanceId, Value - Agent EPR
     */
    private Map<String, String> instanceIdToAgentEprMap = new HashMap<String, String>();
    
    /**
     * Key - domain name, Value - number of instances started in this domain
     */
    private Map<String, Integer> domainNameToInstanceCountMap = 
    		new HashMap<String, Integer>();
  
  	
	@Override
    public boolean spawnInstance(String domainName, String instanceId) throws Exception {

        agentManagementService = AgentManagementDSHolder.getInstance().getAgentMgtService();

        if (agentManagementService != null) {

            // first check whether we can spawn any instance in this adapter
            if (instanceIdToAgentEprMap.size() > 
                    agentManagementService.getTotalMaxInstanceCount() - 1) {

                String msg =
                    "Adapter (" + getName() + ") has reached the maximum possible instance" +
                        " count (" + instanceIdToAgentEprMap.size() + ").";
                log.warn(msg);
                throw new Exception(msg);
            }

            // picks an Agent
            String agentEpr = agentManagementService.pickAnAgent();

            // try to spawn the instance
            return tryToStartInstance(agentEpr, domainName, instanceId,
                                      agentManagementService.getNumberOfRegisteredAgents());

        } else {
            String msg = "Failed to establish a connection with Agent Management Service.";
            log.error(msg);
            throw new Exception(msg);
        }

    }
	
	@Override
    public String getName() {
        return ADAPTER_NAME;
    }
    

    @Override
    public boolean terminateInstance(String instanceId) throws Exception {

        String agentEpr = instanceIdToAgentEprMap.get(instanceId);

        if (agentEpr == null) {
            String msg =
                "Instance (" + instanceId + ") requested to terminate cannot be found in " +
                    "the Adapter (" + getName() + ").";
            log.error(msg);
            throw new Exception(msg);
        }

        AgentServiceClient agentClient = null;
        
        try {
            agentClient = new AgentServiceClient(agentEpr);
            
        } catch (AxisFault e) {
            // couldn't connect to AgentService, hence try to recover
            JVMAgentServiceRecoverer recoverer = new JVMAgentServiceRecoverer(agentEpr);
            recoverer.start();
        }
        
        try {
            if (agentClient != null && agentClient.terminateInstance(instanceId)) {
                // remove relevant entry
                instanceIdToAgentEprMap.remove(instanceId);

                return true;
            }
        } catch (Exception e) {
            String msg = "Failed to terminate the instance in Agent (" + agentEpr +").";
            log.error(msg, e);
            throw new Exception(msg, e);

        }
        
        return false;
    }

    @Override
    public int getRunningInstanceCount(String domainName) {

    	//if domain name can be found return the corresponding instance count
    	//else return 0.
        return domainNameToInstanceCountMap.containsKey(domainName) ?
        		domainNameToInstanceCountMap.get(domainName) : 0;
    }
    
    @Override
	public int getPendingInstanceCount(String domainName) {
		//In the context of JVM, there's no such instances called pending.
		return 0;
	}

	@Override
	public int getRunningInstanceCount() {
		return instanceIdToAgentEprMap.size();
	}
    
	@Override
	public boolean sanityCheck() {
		// TODO Auto-generated method stub
		return false;
	}

	

	/**
	 * This helper method tries to start a new instance. If it failed to start it will
	 * tries another agent. This will try only one round through all Agents.
	 * @param agentEpr epr of the selected Agent
	 * @param domainName 
	 * @param instanceId
	 * @param numberOfAgents this tracks down the agents we should try.
	 * @return any successful try or not?
	 * @throws Exception
	 */
	private boolean tryToStartInstance(String agentEpr, String domainName, String instanceId,
	                                               int numberOfAgents) throws Exception {

        AgentServiceClient agentClient = null;

        // we only go one round to successfully spawn an instance
        if (numberOfAgents > 0) {
            
            //First try to connect to an Agent
            try {
                
                agentClient = new AgentServiceClient(agentEpr);
                
            } catch (AxisFault e) {
                //couldn't connect to AgentService, hence try to recover
                JVMAgentServiceRecoverer recoverer = new JVMAgentServiceRecoverer(agentEpr);
                recoverer.start();
                
                // we're not waiting till recovery is finished. We look for another agent.
                tryToStartInstanceInAnotherAgent(agentManagementService.pickAnAgent(), domainName,
                                                 instanceId, numberOfAgents);
                
            }
            
            //We connected successfully, now we can try to start an instance
            try{
                if (agentClient != null) {
                    // this will either return true or throws an exception
                    agentClient.startInstance(domainName, instanceId);

                    // if we reach here, that means an instance is successfully started.
                    // addToAgentEprToInstanceIdMap(agentEpr, instanceId);
                    addToInstanceIdToAgentEprMap(instanceId, agentEpr);
                    addToDomainNameToInstancesMap(domainName);

                    return true;
                }
                
            }catch (Exception ex) {

                log.debug("Failed to spawn the instance in Agent (" + agentEpr +
                    "). Hence trying on" + " another Agent.");
                
                // We look for another agent.
                tryToStartInstanceInAnotherAgent(agentManagementService.pickAnAgent(), domainName,
                                                 instanceId, numberOfAgents);
            } 

        }

        return false;
    }

	private void tryToStartInstanceInAnotherAgent(
	        String agentEpr, String domainName, String instanceId, int numberOfAgents) 
	            throws Exception{

        // Failed when starting the instance, thus try in another Agent.
        // tracking the position of the round
        numberOfAgents--;

        // We need to throw the exception caught, thus doing this check.
        if (numberOfAgents > 0) {

            // try to pass the job to another Agent
            this.tryToStartInstance(agentEpr, domainName, instanceId,
                                    numberOfAgents);

        } else {
            String msg = "Tried to spawn the instance in all registered agents, but failed.";
            log.error(msg);
            throw new Exception(msg);
        }

	    
    }

    /**
	 * Add this pair to the {@link #instanceIdToAgentEprMap}.
	 * @param instanceId  
	 * @param agentEpr
	 */
    private void addToInstanceIdToAgentEprMap(String instanceId, String agentEpr) {

        if (!instanceIdToAgentEprMap.containsKey(instanceId)) {
            instanceIdToAgentEprMap.put(instanceId, agentEpr);
        } else {
            String msg =
                "This instance id (" + instanceId + ") is already in the Map. " +
                    "This is something exceptional.";
            log.error(msg);
            throw new RuntimeException(msg);
        }
    }
    
    /**
	 * Add this entry to the {@link #domainNameToInstanceCountMap}.
	 * @param domainName domain name
	 */
    private void addToDomainNameToInstancesMap(String domainName) {
    	
    	//if key already exists, reads the value, increment it and replace.
        if(domainNameToInstanceCountMap.containsKey(domainName)){
        	
        	int count = domainNameToInstanceCountMap.get(domainName);
        	count++;
        	domainNameToInstanceCountMap.put(domainName, count);
        	return;
        	
        }
        //if not instance count should set to 1
        domainNameToInstanceCountMap.put(domainName, 1);
    }

}
