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

package org.wso2.carbon.databridge.streamdefn.cassandra.datastore;

import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;

import java.util.Collection;

/**
 * Cassandra based Event Stream Definition store implementation
 */
public class CassandraStreamDefinitionStore extends AbstractStreamDefinitionStore {

    Logger log = Logger.getLogger(CassandraStreamDefinitionStore.class);

    CassandraConnector cassandraConnector;

    public CassandraStreamDefinitionStore(){
        cassandraConnector = new CassandraConnector();
    }

    @Override
    protected void saveStreamIdToStore(Credentials credentials, String streamIdKey, String streamId)
            throws StreamDefinitionStoreException {


        cassandraConnector.saveStreamIdToStore(ClusterFactory.getCluster(credentials), streamIdKey, streamId);
    }

    @Override
    protected void saveStreamDefinitionToStore(Credentials credentials, String streamId, StreamDefinition streamDefinition) throws StreamDefinitionStoreException {
        cassandraConnector.saveStreamDefinitionToStore(ClusterFactory.getCluster(credentials), streamId, streamDefinition);
    }

    @Override
    protected String getStreamIdFromStore(Credentials credentials, String streamIdKey)
            throws StreamDefinitionStoreException {
        return cassandraConnector.getStreamIdFromStore(ClusterFactory.getCluster(credentials), streamIdKey);
    }


    @Override
    public StreamDefinition getStreamDefinitionFromStore(Credentials credentials, String streamId)
            throws StreamDefinitionStoreException {
        return cassandraConnector.getStreamDefinitionFromStore(ClusterFactory.getCluster(credentials), streamId);
    }

    @Override
    protected Collection<StreamDefinition> getAllStreamDefinitionsFromStore(Credentials credentials) throws
            StreamDefinitionStoreException {
        return cassandraConnector.getAllStreamDefinitionFromStore(ClusterFactory.getCluster(credentials));
    }


}
