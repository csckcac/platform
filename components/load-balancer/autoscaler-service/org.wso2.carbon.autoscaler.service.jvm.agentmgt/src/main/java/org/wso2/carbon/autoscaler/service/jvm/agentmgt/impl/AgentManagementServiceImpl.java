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
package org.wso2.carbon.autoscaler.agentmgt.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.autoscaler.agentmgt.service.IAgentManagementService;
import org.wso2.carbon.autoscaler.agentmgt.service.exception.AgentAlreadyRegisteredException;
import org.wso2.carbon.autoscaler.agentmgt.service.exception.AgentNotAlreadyRegisteredException;
import org.wso2.carbon.autoscaler.agentmgt.service.exception.AgentNotFoundException;
import org.wso2.carbon.autoscaler.agentmgt.service.exception.AgentRegisteringException;
import org.wso2.carbon.autoscaler.agentmgt.service.exception.NullAgentException;
import org.wso2.carbon.autoscaler.agentmgt.service.registry.AgentRegistry;

/**
 * Implements the IAgentManagementService interface.
 * 
 * @scr.component name="org.wso2.carbon.autoscaler.agentmgt.service"
 * 
 * @scr.service
 *              value=
 *              "org.wso2.carbon.autoscaler.agentmgt.service.IAgentManagementService"
 * 
 */
public class AgentManagementServiceImpl implements IAgentManagementService {
    
    private static final Log log = LogFactory.getLog(AgentManagementServiceImpl.class);
    
    private AgentRegistry agentRegistry = AgentRegistry.getInstance();
    
    /**
     * List of registered agent's EPRs
     */
    private List<String> registeredAgentList;

    /**
     * Keeping track of last Agent that was picked up.
     */
    private int lastPickedAgent;
    
    /**
     * total of maximum instance count of each Agent
     */
    private int totalMaxInstanceCount;
    
    /**
     * Key: Agent EPR
     * Value: Instance count of that Agent
     */
    private Map<String, Integer> agentEprToInstanceCountMap;
    

    public boolean registerAgent(String epr, int instanceCount) throws NullAgentException,
                                            AgentAlreadyRegisteredException,
                                            AgentRegisteringException {

        //retrieve values from registry
        registeredAgentList = agentRegistry.getRegisteredAgentList();
        totalMaxInstanceCount = agentRegistry.getTotalMaxInstanceCount();
        agentEprToInstanceCountMap = agentRegistry.getAgentEprToInstanceCountMap();
        
        // For a successful registration EPR should not be null, EPR should not be already
        // registered and should successfully get added.
        // is EPR null?
        if (epr == null) {
            String msg = "EPR of the Agent is null.";
            log.error(msg);
            throw new NullAgentException(msg);
        }
        // is EPR already registered?
        else if (registeredAgentList.contains(epr)) {
            String msg = "EPR of the Agent (" + epr + ") is already registered.";
            log.error(msg);
            throw new AgentAlreadyRegisteredException(msg);
        }
        // is EPR successfully added?
        else if (registeredAgentList.add(epr)) {
            log.info("Agent (" + epr + ") is successfully registered!");
            
            //increase the total maxInstanceCount
            addToTotalMaxInstanceCount(instanceCount);
            agentEprToInstanceCountMap.put(epr, instanceCount);
            
            //set them back in registry
            agentRegistry.setRegisteredAgentList(registeredAgentList);
            agentRegistry.setTotalMaxInstanceCount(totalMaxInstanceCount);
            agentRegistry.setAgentEprToInstanceCountMap(agentEprToInstanceCountMap);
            
            return true;
        }
        // EPR failed to add
        else {
            String msg = "EPR (" + epr + ") failed to get added to the data structure.";
            log.error(msg);
            throw new AgentRegisteringException(msg);
        }

    }

    public boolean unregisterAgent(String epr, int instanceCount) throws NullAgentException,
                                              AgentNotAlreadyRegisteredException {
        
        // retrieve values from registry
        registeredAgentList = agentRegistry.getRegisteredAgentList();
        totalMaxInstanceCount = agentRegistry.getTotalMaxInstanceCount();
        agentEprToInstanceCountMap = agentRegistry.getAgentEprToInstanceCountMap();

        // In order to successfully unregistered EPR should not be null,
        // EPR should be a registered one.
        // is EPR null?
        if (epr == null) {
            String msg = "EPR of the Agent is null.";
            log.error(msg);
            throw new NullAgentException(msg);
        }
        // is a registered EPR?
        else if (registeredAgentList.remove(epr)) {
            log.info("Agent (" + epr + ") is successfully unregistered!");

            if (instanceCount == 0) {
                // this is probably a forceful unregistering
                if (agentEprToInstanceCountMap.containsKey(epr)) {
                    
                    // gets the instance count for this agent
                    int instanceCountTemp = agentEprToInstanceCountMap.get(epr);

                    // decrease the total maxInstanceCount
                    addToTotalMaxInstanceCount(-instanceCountTemp);
                }

            } else {
                // decrease the total maxInstanceCount
                addToTotalMaxInstanceCount(-instanceCount);
            }

            // set them back in registry
            agentRegistry.setRegisteredAgentList(registeredAgentList);
            agentRegistry.setTotalMaxInstanceCount(totalMaxInstanceCount);
            agentRegistry.setAgentEprToInstanceCountMap(agentEprToInstanceCountMap);

            return true;
        }
        // not a registered EPR
        else {
            String msg = "EPR (" + epr + ") is not a registered one.";
            log.error(msg);
            throw new AgentNotAlreadyRegisteredException(msg);
        }

    }

    public String pickAnAgent() throws AgentNotFoundException {
        
        //retrieve values from registry
        registeredAgentList = agentRegistry.getRegisteredAgentList();
        lastPickedAgent = agentRegistry.getLastPickedAgent();

        // registeredAgentList can never be null since we've initialized it, but
        // it can be empty.
        if (registeredAgentList.size() > 0) {

            // this is the first time we're picking an agent
            if (lastPickedAgent == -1) {
                // increment the lastPickedAgent
                lastPickedAgent++;
                
            } else {
                // increment the lastPickedAgent, since somehow we are going to
                // pick an agent.
                lastPickedAgent++;

                if (lastPickedAgent == (registeredAgentList.size())) {
                    // We got to the end, start again from the beginning.
                    lastPickedAgent = 0;
                    
                } else {
                    // We are at the middle of a round, pick it and return!
                }
            }
            
            // set lastPickedAgent back in registry, NOTE: no changes done to registeredAgentList
            agentRegistry.setLastPickedAgent(lastPickedAgent);
            
            // pick it and return!
            return registeredAgentList.get(lastPickedAgent);
        }
        // No Agent can be found.
        else {
            String msg = "No Agent is registered in Agent Management Service.";
            log.error(msg);
            throw new AgentNotFoundException(msg);
        }
    }

    public int getNumberOfRegisteredAgents() {
        //retrieve values from registry
        registeredAgentList = agentRegistry.getRegisteredAgentList();
        
        log.debug("Number of Agents registered in AgentManagementService is " +
                  registeredAgentList.size() + ".");
        return registeredAgentList.size();
    }

    @Override
    public int getTotalMaxInstanceCount() {
        //retrieve values from registry
        totalMaxInstanceCount = agentRegistry.getTotalMaxInstanceCount();
        
        return totalMaxInstanceCount;
    }
    
    @Override
    public boolean unregisterAgentForcefully(String epr) throws NullAgentException, 
                                                AgentNotAlreadyRegisteredException {

        return unregisterAgent(epr, 0);
    }
    
    private void addToTotalMaxInstanceCount(int instanceCount) {

        totalMaxInstanceCount += instanceCount;
    }


    // /**
    // * This method attempts to get the Base URI for this service. This will be
    // used to construct
    // * the URIs for the various details returned.
    // * @return
    // */
    // private String getBaseURL() {
    // if (baseURL == null) {
    // MessageContext messageContext =
    // MessageContext.getCurrentMessageContext();
    // AxisConfiguration configuration = messageContext
    // .getConfigurationContext().getAxisConfiguration();
    // TransportInDescription inDescription =
    // configuration.getTransportIn("http");
    // try {
    // EndpointReference[] eprs = inDescription.getReceiver()
    // .getEPRsForService(messageContext.getAxisService().getName(), null);
    // baseURL = eprs[0].getAddress();
    // } catch (AxisFault axisFault) {
    // }
    // }
    // return baseURL;
    // }

}
