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
package org.wso2.carbon.autoscaler.service.jvm.agentmgt;

import org.wso2.carbon.autoscaler.service.jvm.agentmgt.exception.AgentAlreadyRegisteredException;
import org.wso2.carbon.autoscaler.service.jvm.agentmgt.exception.AgentNotAlreadyRegisteredException;
import org.wso2.carbon.autoscaler.service.jvm.agentmgt.exception.AgentNotFoundException;
import org.wso2.carbon.autoscaler.service.jvm.agentmgt.exception.AgentRegisteringException;
import org.wso2.carbon.autoscaler.service.jvm.agentmgt.exception.NullAgentException;

/**
 * This service is responsible for keep a list of Agents that get registered to this service and
 * also exposes operations to pick up an Agent based on a selection algorithm
 * (default: round robin) and to retrieve number of registered Agents. Also this service allow
 * Agents to get registered and unregistered.
 * 
 */
public interface IAgentManagementService {

    /**
     * Registers an Agent's EPR.
     * 
     * @param epr
     *            EPR of the Agent to be registered.
     * @param instanceCount
     *            maximum number of instances this registering Agent can spawn.
     * @return whether the registration is successful i.e this will return true if and only if
     *         all following 3 conditions satisfied.
     *         <ul>
     *         <li>epr is not null.</li>
     *         <li>epr is not already registered.</li>
     *         <li>epr is successfully added.</li>
     *         </ul>
     *         <p>
     *         This will never return false, but an exception.
     *         </p>
     * @throws NullAgentException
     *             when epr is null.
     * @throws AgentAlreadyRegisteredException
     *             when epr is already registered.
     * @throws AgentRegisteringException
     *             when epr failed to added to the list.
     */
    public boolean registerAgent(String epr, int instanceCount) throws NullAgentException,
                                            AgentAlreadyRegisteredException,
                                            AgentRegisteringException;

    /**
     * Unregisters an Agent's EPR.
     * 
     * @param epr
     *            EPR of the Agent to be unregistered.
     * @param instanceCount
     *            maximum number of instances this unregistering Agent can spawn.
     * @return whether the Agent is successfully unregistered. i.e this will return true
     *         if and only if all following 2 conditions satisfied.
     *         <ul>
     *         <li>epr is not null.</li>
     *         <li>epr is a registered EPR.</li>
     *         </ul>
     * @throws NullAgentException
     *             when epr is null.
     * @throws AgentNotAlreadyRegisteredException
     *             when epr is not a registered one.
     */
    public boolean unregisterAgent(String epr, int instanceCount) throws NullAgentException,
                                              AgentNotAlreadyRegisteredException;
    
    /**
     * Unregisters an Agent's EPR. This should ideally be called when a recovery of Agent Service
     * is failed.
     * 
     * @param epr
     *            EPR of the Agent to be unregistered.
     * @return whether the Agent is successfully unregistered. i.e this will return true
     *         if and only if all following 2 conditions satisfied.
     *         <ul>
     *         <li>epr is not null.</li>
     *         <li>epr is a registered EPR.</li>
     *         </ul>
     * @throws NullAgentException
     *             when epr is null.
     * @throws AgentNotAlreadyRegisteredException
     *             when epr is not a registered one.
     */
    public boolean unregisterAgentForcefully(String epr) throws NullAgentException,
                                              AgentNotAlreadyRegisteredException;

    /**
     * Pick an Agent in Round Robin manner.
     * 
     * @return EPR of the picked Agent.
     * 
     * @throws AgentNotFoundException
     *             if no Agent can be found.
     */
    public String pickAnAgent() throws AgentNotFoundException;

    /**
     * Tells whether the Agent's EPR given, is a registered one or not.
     * @param epr an EPR
     * @return true: if the EPR is a registered one, else false.
     */
    public boolean isRegisteredAgent(String epr);
    
    /**
     * Returns the number of registered Agents.
     * 
     * @return number of registered Agents
     */
    public int getNumberOfRegisteredAgents();
    
//    /**
//     * Given instance count will be added to the total instance count.
//     * This will be accessed by an Agent when it get registered and unregistered.
//     * @param instanceCount maximum number of instances that an Agent can spawn.
//     */
//    public void addToTotalMaxInstanceCount(int instanceCount);
    
    /**
     * Autoscaler Service's JVM Adapter will call this.
     * @return total maximum instance count of all registered agents.
     */
    public int getTotalMaxInstanceCount();

}
