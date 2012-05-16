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
package org.wso2.carbon.autoscaler.service.tropos.agentmgt;

import org.wso2.carbon.autoscaler.service.tropos.agentmgt.exception.AgentAlreadyRegisteredException;
import org.wso2.carbon.autoscaler.service.tropos.agentmgt.exception.AgentNotAlreadyRegisteredException;
import org.wso2.carbon.autoscaler.service.tropos.agentmgt.exception.AgentNotFoundException;
import org.wso2.carbon.autoscaler.service.tropos.agentmgt.exception.AgentRegisteringException;
import org.wso2.carbon.autoscaler.service.tropos.agentmgt.exception.NullAgentException;
import org.wso2.carbon.lb.common.dto.HostMachine;

import java.sql.SQLException;

/**
 * This service is responsible for insert host machines to data base after added to the zone and
 * also exposes operations to pick up an Container information based on a selection algorithm
 * (default: least load host machine selected).
 * Also this service allow Agents to get registered and unregistered.
 * 
 */
public interface ITroposAgentManagementService {

    /**
     * Registers an Agent's EPR.
     * 
     * @param hostMachine
     *            Host machine object includes all the information about the new machine added. It
     *            will have bridges, end point(unique), container root, and a zone.
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
    public boolean registerAgent(HostMachine hostMachine, String[] domains) throws NullAgentException,
                                                               AgentAlreadyRegisteredException,
                                                               AgentRegisteringException,
                                                               ClassNotFoundException, SQLException;

    /**
     * Unregisters an Agent's EPR.
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
    public boolean unregisterAgent(String epr) throws NullAgentException,
                                                      AgentNotAlreadyRegisteredException,
                                                      ClassNotFoundException, SQLException;
    

    /**
     * Pick information of a container possible to create before creation from the host machine of
     * least loaded host machine.
     *
     * @return ContainerInformation all the information needed for creation of container newly.
     *
     * @throws AgentNotFoundException if no resources(host machine) to create a container.
     */
    public org.wso2.carbon.lb.common.dto.ContainerInformation pickAContainer(String zone) throws AgentNotFoundException,
                                                                                     ClassNotFoundException,
                                                                                     SQLException;

}
