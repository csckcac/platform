package org.wso2.carbon.bam.eventreceiver.datastore;

import org.apache.log4j.Logger;
import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.agent.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.agent.server.datastore.StreamDefinitionStore;
import org.wso2.carbon.agent.server.exception.StreamDefinitionNotFoundException;

import java.util.Collection;
import java.util.List;


public class CassandraStreamDefinitionStore implements StreamDefinitionStore {
    Logger log = Logger.getLogger(CassandraStreamDefinitionStore.class);

    CassandraConnector cassandraConnector;

    public CassandraStreamDefinitionStore(){
        cassandraConnector = new CassandraConnector();
    }

    private String constructNameVersionKey(String name, String version) {
        return name + "::" + version;
    }
    @Override
    public boolean containsStreamDefinition(String domainName, String userName, String version) {

        return false;
    }

    @Override
    public EventStreamDefinition getStreamDefinition(String domainName, String streamName, String streamVersion) throws StreamDefinitionNotFoundException {
        String streamId = getStreamId(domainName, streamName, streamVersion);
        return getStreamDefinition(domainName, streamId);
    }

    @Override
    public void saveStreamDefinition(String domainName, EventStreamDefinition eventStreamDefinition) throws DifferentStreamDefinitionAlreadyDefinedException {
        cassandraConnector.saveStreamDefinitionToCSS(domainName,eventStreamDefinition);
    }

    @Override
    public EventStreamDefinition getStreamDefinition(String domainName, String streamId) throws StreamDefinitionNotFoundException {
        EventStreamDefinition eventStreamDefinition = null;
        try {
            eventStreamDefinition = cassandraConnector.getStreamDefinition(streamId);
        } catch (MalformedStreamDefinitionException e) {
            e.printStackTrace();
        }
        if(eventStreamDefinition == null){
            throw new StreamDefinitionNotFoundException("No definitions exist on " + domainName + " for " + streamId);
        }
        return eventStreamDefinition;
    }

    @Override
    public Collection<EventStreamDefinition> getAllStreamDefinitions(String domainName) throws StreamDefinitionNotFoundException {
        try {
            return cassandraConnector.getAllStreamDefinition();
        } catch (MalformedStreamDefinitionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    @Override
    public String getStreamId(String domainName, String streamName, String streamVersion) throws StreamDefinitionNotFoundException {
        String streamId = cassandraConnector.getStreamId(domainName, streamName, streamVersion);
        if(streamId == null || streamId.isEmpty()){
            throw new StreamDefinitionNotFoundException("No definitions exist on " + domainName + " for " + streamName + " " + streamVersion);
        }
        return streamId;
    }

    @Override
    public List<EventStreamDefinition> getStreamDefinition(String domainName) throws StreamDefinitionNotFoundException {
        return null;
    }
}
