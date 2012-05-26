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
    //private String streamColumnFamily = null;
    //private String eventStreamId = null;
    //private EventStreamDefinition eventStreamDef;

    public BAMAgentCallback(){
        cassandraConnector = new CassandraConnector();
    }

    @Override
    public void definedEventStream(EventStreamDefinition eventStreamDefinition, String userName, String userPassword, String domainName) {
//        try {
//            if(cassandraConnector.getStreamDefinition(eventStreamDefinition.getStreamId()) != null){
//                return;
//            }
//        } catch (MalformedStreamDefinitionException e) {
//            e.printStackTrace();
//        }
        cassandraConnector.createColumnFamily(domainName,eventStreamDefinition.getName(),userName,userPassword);
    }

    @Override
    public void receive(List<Event> events, String userName, String userPassword, String domainName) {

        for(Event event:events){
            log.info("Event Data :" + event.toString());
            try {
                cassandraConnector.insertEvent(event,userName,userPassword);
            } catch (MalformedStreamDefinitionException e) {
                e.printStackTrace();
            }
        }
    }
}
