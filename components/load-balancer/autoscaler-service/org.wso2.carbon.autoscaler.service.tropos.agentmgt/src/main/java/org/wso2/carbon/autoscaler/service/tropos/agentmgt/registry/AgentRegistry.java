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
package org.wso2.carbon.autoscaler.service.tropos.agentmgt.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton class to keep registered Agents and last picked agent.
 */
public class AgentRegistry {

    private static AgentRegistry agentRegistryInstance = new AgentRegistry();

    /**
     * List of registered agent's EPRs
     */
    private List<String> registeredAgentList = new ArrayList<String>();
    
    private int totalMaxInstanceCount = 0;
    
    /**
     * Keeps track of max instance count of each Agent. This is needed when a recovery of a 
     * service is failed, and we forcefully unregistering that agent.
     */
    private Map<String, Integer> agentEprToInstanceCountMap = new HashMap<String, Integer>();

    /**
     * Keeping track of last Agent that was picked up.
     */
    private int lastPickedAgent = -1;

    private AgentRegistry() {
    }

    public static AgentRegistry getInstance() {
        return agentRegistryInstance;
    }

    public List<String> getRegisteredAgentList() {
        return registeredAgentList;
    }

    public void setRegisteredAgentList(List<String> registeredAgentList) {
        this.registeredAgentList = registeredAgentList;
    }

    public int getLastPickedAgent() {
        return lastPickedAgent;
    }

    public void setLastPickedAgent(int lastPickedAgent) {
        this.lastPickedAgent = lastPickedAgent;
    }

    public int getTotalMaxInstanceCount() {
        return totalMaxInstanceCount;
    }

    public void setTotalMaxInstanceCount(int totalMaxInstanceCount) {
        this.totalMaxInstanceCount = totalMaxInstanceCount;
    }

    public Map<String, Integer> getAgentEprToInstanceCountMap() {
        return agentEprToInstanceCountMap;
    }

    public void setAgentEprToInstanceCountMap(Map<String, Integer> agentEprToInstanceCountMap) {
        this.agentEprToInstanceCountMap = agentEprToInstanceCountMap;
    }

}
