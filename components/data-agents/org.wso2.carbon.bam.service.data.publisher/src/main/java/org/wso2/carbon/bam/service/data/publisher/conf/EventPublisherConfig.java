package org.wso2.carbon.bam.service.data.publisher.conf;

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

    /*    private static Map<String, EventStreamDefinition> eventStreamDefMap;
    private static Map<String, AgentConfiguration> agentConfigurationMap;
    private static Map<String, DataPublisher> dataPublisherMap;

    public static Map getEventStreamDefMap() {
        if (eventStreamDefMap == null) {
            eventStreamDefMap = new HashMap<String, EventStreamDefinition>();
        }
        return eventStreamDefMap;
    }

    public static EventStreamDefinition getEventStreamDefinition(String key) {
        if (eventStreamDefMap != null) {
            eventStreamDefMap.get(key);
        }
        return null;
    }*/
}
