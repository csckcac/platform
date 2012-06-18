/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.mediationstats.data.publisher.conf;


import org.wso2.carbon.agent.DataPublisher;
import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.conf.AgentConfiguration;

import java.util.HashMap;
import java.util.Map;

public class EventPublisherConfig {

    DataPublisher dataPublisher;
    AgentConfiguration agentConfiguration;
    Map<EventStreamDefinition,String> eventStreamDefinitionMap;

    public DataPublisher getDataPublisher() {
        return dataPublisher;
    }

    public void setDataPublisher(DataPublisher dataPublisher) {
        this.dataPublisher = dataPublisher;
    }

    public AgentConfiguration getAgentConfiguration() {
        return agentConfiguration;
    }

    public void setAgentConfiguration(AgentConfiguration agentConfiguration) {
        this.agentConfiguration = agentConfiguration;
    }

    public Map<EventStreamDefinition,String> getEventStreamDefinitionMap() {
        if(eventStreamDefinitionMap==null){
            eventStreamDefinitionMap = new HashMap<EventStreamDefinition,String>();
        }
        return eventStreamDefinitionMap;
    }

    public void setEventStreamDefinitionList(
            Map<EventStreamDefinition,String> streamDefinitionMap) {
        this.eventStreamDefinitionMap = streamDefinitionMap;
    }

}
