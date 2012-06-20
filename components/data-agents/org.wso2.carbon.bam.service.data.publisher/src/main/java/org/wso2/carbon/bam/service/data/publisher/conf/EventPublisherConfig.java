package org.wso2.carbon.bam.service.data.publisher.conf;


import org.wso2.carbon.eventbridge.agent.thrift.DataPublisher;
import org.wso2.carbon.eventbridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.eventbridge.commons.EventStreamDefinition;

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
