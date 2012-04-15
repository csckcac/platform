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
package org.wso2.carbon.autoscaler.service.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton class to keep data of JVM Adapter.
 * FIXME: make these data persistence
 */
public class JVMAdapterRegistry {

    private static JVMAdapterRegistry jvmAdapterRegistryInstance = new JVMAdapterRegistry();

    /**
     * Key - InstanceId, Value - Agent EPR
     */
    private Map<String, String> instanceIdToAgentEprMap = new HashMap<String, String>();
    
    /**
     * List to keep EPR's of Agents whose are unreachable.
     */
    private List<String> temporarilySkippedAgentEprList = new ArrayList<String>();
    

    /**
     * Key - domain name, Value - number of instances started in this domain
     */
    private Map<String, Integer> domainNameToInstanceCountMap = 
    		new HashMap<String, Integer>();

    private JVMAdapterRegistry() {
    }

    public static JVMAdapterRegistry getInstance() {
        return jvmAdapterRegistryInstance;
    }

    public Map<String, String> getInstanceIdToAgentEprMap() {
        return instanceIdToAgentEprMap;
    }

    public void setInstanceIdToAgentEprMap(Map<String, String> instanceIdToAgentEprMap) {
        this.instanceIdToAgentEprMap = instanceIdToAgentEprMap;
    }

    public List<String> getTemporarilySkippedAgentEprList() {
        return temporarilySkippedAgentEprList;
    }

    public void setTemporarilySkippedAgentEprList(List<String> temporarilySkippedAgentEprList) {
        this.temporarilySkippedAgentEprList = temporarilySkippedAgentEprList;
    }

	public Map<String, Integer> getDomainNameToInstanceCountMap() {
		return domainNameToInstanceCountMap;
	}

	public void setDomainNameToInstanceCountMap(
			Map<String, Integer> domainNameToInstanceCountMap) {
		this.domainNameToInstanceCountMap = domainNameToInstanceCountMap;
	}

    

}
