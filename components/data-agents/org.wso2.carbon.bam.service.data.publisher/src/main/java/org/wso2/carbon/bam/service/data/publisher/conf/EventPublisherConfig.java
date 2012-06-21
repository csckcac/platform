package org.wso2.carbon.bam.service.data.publisher.conf;


import org.wso2.carbon.eventbridge.agent.thrift.DataPublisher;
import org.wso2.carbon.eventbridge.agent.thrift.conf.AgentConfiguration;

public class EventPublisherConfig {

    DataPublisher dataPublisher;
    AgentConfiguration agentConfiguration;

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


}
