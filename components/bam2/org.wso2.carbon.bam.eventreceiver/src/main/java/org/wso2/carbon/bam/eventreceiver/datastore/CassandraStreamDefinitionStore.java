/*
 * Copyright 2012 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.bam.eventreceiver.datastore;

import org.apache.log4j.Logger;
import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.agent.server.datastore.StreamDefinitionStore;

import java.util.Collection;

/**
 * Cassandra based Event Stream Definition store implementation
 */
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
        } catch (MalformedStreamDefinitionException malformedStreamDefinitionException) {
            log.error("Event stream definition retrieval error: ", malformedStreamDefinitionException);
            malformedStreamDefinitionException.printStackTrace();
        }
        return null;
    }

    @Override
    protected Collection<EventStreamDefinition> getAllStreamDefinitionsFromStore(String domainName) {
        try {
            return cassandraConnector.getAllStreamDefinitionFromStore(domainName);
        } catch (MalformedStreamDefinitionException malformedStreamDefinitionException) {
            log.error("Event stream definitions retrieval error: ", malformedStreamDefinitionException);
            malformedStreamDefinitionException.printStackTrace();
            malformedStreamDefinitionException.printStackTrace();
        }
        return null;
    }
}
