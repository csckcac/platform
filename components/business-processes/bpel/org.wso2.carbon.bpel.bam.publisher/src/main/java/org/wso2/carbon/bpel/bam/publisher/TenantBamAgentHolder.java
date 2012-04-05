/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.bam.publisher;

import org.wso2.carbon.bam.agent.conf.AgentConfiguration;
import org.wso2.carbon.bam.agent.core.Agent;
import org.wso2.carbon.bam.agent.publish.EventReceiver;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TenantBamAgentHolder {
    private static TenantBamAgentHolder instance;

    private final Map<Integer, EventReceiver> tenantEventReceiverMap =
            new ConcurrentHashMap<Integer, EventReceiver>();

    private static volatile Agent agent;

    private TenantBamAgentHolder() {
    }

    public static TenantBamAgentHolder getInstance() {
        if (null == instance) {
            instance = new TenantBamAgentHolder();
        }
        createAgent();
        return instance;
    }

    private static void createAgent(){
         if(agent == null){
            AgentConfiguration configuration = new AgentConfiguration();
            agent = new Agent(configuration);
        }
    }

    public Agent getAgent(Integer tenantId) {
        return agent;
    }

    public EventReceiver getEventReceiver(Integer tenantId) {
        return tenantEventReceiverMap.get(tenantId);
    }

    public void addEventReceiver(Integer tenantId, EventReceiver receiver) {
        tenantEventReceiverMap.put(tenantId, receiver);
    }

    public void removeEventReceiver(Integer tenantId) {
        tenantEventReceiverMap.remove(tenantId);
    }

    public void removeAgent() {
        agent.shutdown();
    }
}
