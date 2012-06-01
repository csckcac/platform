package org.wso2.carbon.bam.eventreceiver.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.agent.commons.Event;
import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.agent.server.AgentCallback;
import org.wso2.carbon.bam.eventreceiver.datastore.CassandraConnector;

import java.util.List;


public class BAMAgentCallback implements AgentCallback {
    private static Log log = LogFactory.getLog(BAMAgentCallback.class);

   private CassandraConnector cassandraConnector;
    private static final String WSO2_CARBON_STAND_ALONE = "WSO2-CARBON-STAND-ALONE";

    public BAMAgentCallback(){
        cassandraConnector = new CassandraConnector();

    }

    @Override
    public void definedEventStream(EventStreamDefinition eventStreamDefinition, String userName, String userPassword, String domainName) {

        if(domainName == null){
            domainName = WSO2_CARBON_STAND_ALONE;
        }
        cassandraConnector.insertEventDefinition(userName,userPassword,eventStreamDefinition);
        cassandraConnector.createColumnFamily(domainName,eventStreamDefinition.getName(),userName,userPassword);
    }

    @Override
    public void receive(List<Event> events, String userName, String userPassword, String domainName) {
        for(Event event:events){
            try {
                cassandraConnector.insertEvent(event,userName,userPassword);
            } catch (MalformedStreamDefinitionException e) {
                e.printStackTrace();
            }
        }
    }
}
