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
package org.wso2.carbon.autoscaler.service;

import org.wso2.carbon.autoscaler.service.exception.NoInstanceFoundException;

import java.sql.SQLException;

/**
 * AutoScaler task should communicate with AutoscalerService, when it decides to scale up
 * or down. Only {@link #startInstance(String)} and {@link #terminateInstance(String)}
 * operations are provided by this service.
 * 
 */
public interface IAutoscalerService {

    
    /**
     * This will be called by the Autoscaler task, if it wants to scale up.
     * @param domainName spawning instance should be in this domain.
     * @return whether an instance started successfully or not.
     */
    public boolean startInstance(String domainName) throws ClassNotFoundException, SQLException;
    
   
    /**
     * This will be called by the Autoscaler task, if it wants to scale down.
     * @param domainName terminating instance should be in this domain.
     * @return whether an instance terminated successfully or not.
     * @throws NoInstanceFoundException if no instance in this particular domain has
     *  spawned.
     */
	public boolean terminateInstance(String domainName)
            throws NoInstanceFoundException, SQLException;
	
	/**
	 * This will be called by the Autoscaler task, in order to get the pending instances
	 * count of a particular domain.
	 * @param domainName name of the domain.
	 * @return number of pending instances for this domain. If this domain is not present,
	 * zero will be returned.
	 */
	public int getPendingInstanceCount(String domainName);
	
	/**
	 * This should be called in order to add to pending instance count for this domain.
	 * If you want to deduct, you can simply send a negative value.
	 * @param domainName name of the domain.
	 * @param count number of instances to be added for this domain.
	 */
	public void addPendingInstanceCount(String domainName, int count);
    
}
