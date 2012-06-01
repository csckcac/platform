package org.wso2.carbon.bam.eventreceiver.datastore;

import org.apache.log4j.Logger;
import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.agent.server.datastore.StreamDefinitionStore;

import java.util.Collection;


public class CassandraStreamDefinitionStore extends StreamDefinitionStore {
    Logger log = Logger.getLogger(CassandraStreamDefinitionStore.class);

    CassandraConnector cassandraConnector;

    public CassandraStreamDefinitionStore(){
        cassandraConnector = new CassandraConnector();
    }

    @Override
    protected void saveStreamIdToStore(String domainName, String streamIdKey, String streamId) {
        cassandraConnector.saveStreamIdToStore(domainName,streamIdKey,streamId);
    }

    @Override
    protected void saveStreamDefinitionToStore(String domainName, String streamId, EventStreamDefinition eventStreamDefinition) {
        cassandraConnector.saveStreamDefinitionToStore(domainName, streamId, eventStreamDefinition);
    }

    @Override
    protected String getStreamIdFromStore(String domainName, String streamIdKey){
        return cassandraConnector.getStreamIdFromStore(domainName,streamIdKey);
    }

    @Override
    public EventStreamDefinition getStreamDefinitionFromStore(String domainName, String streamId) {
        try {
            return cassandraConnector.getStreamDefinitionFromStore(domainName,streamId);
        } catch (MalformedStreamDefinitionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected Collection<EventStreamDefinition> getAllStreamDefinitionsFromStore(String domainName) {
        try {
            return cassandraConnector.getAllStreamDefinitionFromStore(domainName);
        } catch (MalformedStreamDefinitionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
