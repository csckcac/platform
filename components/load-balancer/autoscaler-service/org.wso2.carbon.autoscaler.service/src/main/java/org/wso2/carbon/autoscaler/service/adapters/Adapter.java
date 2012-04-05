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
package org.wso2.carbon.autoscaler.service.adapters;

/**
 * Every adapter should extend this Abstract class.
 * When a new adapter is added, make sure you edit the default policy specified at
 * <i>Policy</i> object.
 * 
 */
public abstract class Adapter {

    /**
     * 
     * @return the name of the Adapter
     */
    public abstract String getName();

    /**
     * Calls an Agent and spawn an instance of this domain type, when scaling up.
     * 
     * @param domainName
     *            name of the domain requested.
     * @param instanceId
     *            this will be set as the id of the new instance.
     * @return whether an instance is successfully spawned?
     * @throws Exception
     *             failed to spawn an instance.
     */
    public abstract boolean spawnInstance(String domainName, String instanceId) throws Exception;

    /**
     * Finds the Agent who spawned this particular instance, and request it to terminate
     * the instance.
     * 
     * @param instanceId
     *            id of the instance to be terminated.
     * @return whether the termination is successful or not.fill
     * @throws Exception
     *             failed to terminate.
     */
    public abstract boolean terminateInstance(String instanceId) throws Exception;

    /**
     * This method should return total number of running instances.
     * 
     * @return number of running instances.
     */
    public abstract int getRunningInstanceCount();
    
    /**
     * This method should return total number of running instances for a particular domain.
     * 
     * @return number of running instances.
     */
    public abstract int getRunningInstanceCount(String domainName);
    
    /**
     * This method should return total number of pending instances for a given domain.
     * This is required when dealing with EC2 instances. Adapters that are
     * not EC2 can simply return zero.
     * 
     * @return number of pending instances.
     */
    public abstract int getPendingInstanceCount(String domainName);

    /**
     * 
     * @return whether the sanity check is successful!
     */
    public abstract boolean sanityCheck();
    
}
