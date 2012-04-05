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
package org.wso2.carbon.autoscaler.agent.service;

import org.wso2.carbon.autoscaler.agent.exception.ImageNotFoundException;
import org.wso2.carbon.autoscaler.agent.exception.InstanceInitiationFailureException;
import org.wso2.carbon.autoscaler.agent.exception.InstanceTerminationFailureException;

/**
 * This is a <i>RESTful</i> web service.
 * Service is responsible for get registered in <i>AgentManagementService</i>, spawning a new JVM
 * instance, terminating a spawned JVM instance, unregistering itself from
 * <i>AgentManagementService</i>.
 * Further this calculates maximum number of instances that it can spawn.
 */
public interface IAgentService {

    /**
     * Calls the AgentManagementServiceClient and get registered itself.
     * 
     * @return whether the registration was successful or not.
     * @throws Exception
     *             when,
     *             <ul>
     *             <li>an Exception occurred while reading Agent configuration file.</li>
     *             <li>an Exception occurred while establishing a connection with Agent Management
     *             Service.</li>
     *             <li>an Exception occurred while communicating with Agent Management Service.
     *             </li>
     *             </ul>
     */
    public boolean registerInAgentManagementService() throws Exception;

    /**
     * Calls the AgentManagementServiceClient and get unregistered itself.
     * 
     * @return whether itself unregistered successfully or not.
     * @throws Exception
     *             when communication with Agent Management Service fails.
     */
    public boolean unregisterInAgentManagementService() throws Exception;

    /**
     * Starting the requested server instance.
     * 
     * @param domainName
     *            domain of the instance to be started.
     * @param instanceId
     *            instance to be started (this id should be unique.)
     * @return whether the instance started successfully.
     * @throws Exception
     *             when failed while creating required directories or copying directories.
     * @throws ImageNotFoundException
     *             when an image of the requested domainName cannot be found.
     * @throws InstanceInitiationFailureException
     *             when starting the instance failed.
     */
    public boolean startInstance(String domainName, String instanceId) 
            throws ImageNotFoundException, InstanceInitiationFailureException, Exception;

    /**
     * Terminates the instance which has a given instance id.
     * 
     * @param instanceId
     *            instance with this instance id will be terminated.
     * @return whether the process terminated (true) or process cannot be found (false).
     * @throws Exception
     *             when failed to terminate the given instance.
     */
    public boolean terminateInstance(String instanceId) throws InstanceTerminationFailureException,
                                                               Exception;

    /**
     * This can be used to retrieve number of spawned instances by this Agent.
     * 
     * @return number of spawned instances.
     */
    public int getNumberOfInstances();
}
