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

package org.wso2.carbon.eventbridge.streamdefn.cassandra.datastore;

import me.prettyprint.cassandra.serializers.*;
import org.apache.log4j.Logger;
import org.wso2.carbon.eventbridge.core.beans.Credentials;
import org.wso2.carbon.eventbridge.core.beans.EventStreamDefinition;
import org.wso2.carbon.eventbridge.core.exceptions.StreamDefinitionException;
import org.wso2.carbon.eventbridge.core.streamdefn.AbstractStreamDefnStore;

import java.util.Collection;

/**
 * Cassandra based Event Stream Definition store implementation
 */
public class CassandraStreamDefinitionStore extends AbstractStreamDefnStore {

    private static final String STREAM_NAME_KEY = "Name";
    private static final String STREAM_VERSION_KEY = "Version";
    private static final String STREAM_NICK_NAME_KEY = "Nick_Name";
    private static final String STREAM_DESCRIPTION_KEY = "Description";

    public static final String BAM_META_KEYSPACE = "BAM_AGENT_API_META_DATA";
    public static final String BAM_META_STREAM_ID_CF = "AGENT_STREAM_ID";
    public static final String BAM_META_STREAM_DEF_CF = "AGENT_STREAM_DEF";
    public static final String BAM_META_STREAM_ID_KEY_CF = "STREAM_DEF_ID_KEY";
    public static final String BAM_META_STREAMID_TO_STREAM_ID_KEY = "STREAM_ID_TO_STREAM_ID_KEY";

    public static final String BAM_EVENT_DATA_KEYSPACE = "BAM_EVENT_DATA";
    public static final String BAM_EVENT_DATA_STREAM_DEF_CF = "EVENT_STREAM_DEF";

    private static final String STREAM_ID_KEY = "STREAM_DEF_ID_KEY";
    private static final String STREAM_ID = "STREAM_DEF_ID";
    private static final String STREAM_DEF = "STREAM_DEF";
    private static final String STREAM_DEF_DOMAIN = "STREAM_DEF_DOMAIN";

    private final static StringSerializer stringSerializer = StringSerializer.get();
    // private final static BytesArraySerializer bytesArraySerializer = BytesArraySerializer.get();
    // private final static UUIDSerializer uuidSerializer = UUIDSerializer.get();
    private final static IntegerSerializer integerSerializer = IntegerSerializer.get();
    private final static LongSerializer longSerializer = LongSerializer.get();
    private final static BooleanSerializer booleanSerializer = BooleanSerializer.get();
    private final static FloatSerializer floatSerializer = FloatSerializer.get();
    private final static DoubleSerializer doubleSerializer = DoubleSerializer.get();



    private static final String DOMAIN_NAME = "DOMAIN_NAME";
    private static final String WSO2_CARBON_STAND_ALONE = "WSO2-CARBON-STAND-ALONE";

    Logger log = Logger.getLogger(CassandraStreamDefinitionStore.class);

    CassandraConnector cassandraConnector;

    public CassandraStreamDefinitionStore(){
        cassandraConnector = new CassandraConnector();
    }

    @Override
    protected void saveStreamIdToStore(Credentials credentials, String streamIdKey, String streamId)
            throws StreamDefinitionException {


        cassandraConnector.saveStreamIdToStore(ClusterFactory.getCluster(credentials), streamIdKey, streamId);
    }

    @Override
    protected void saveStreamDefinitionToStore(Credentials credentials, String streamId, EventStreamDefinition eventStreamDefinition) throws StreamDefinitionException {
        cassandraConnector.saveStreamDefinitionToStore(ClusterFactory.getCluster(credentials), streamId, eventStreamDefinition);
    }

    @Override
    protected String getStreamIdFromStore(Credentials credentials, String streamIdKey) throws StreamDefinitionException {
        return cassandraConnector.getStreamIdFromStore(ClusterFactory.getCluster(credentials), streamIdKey);
    }


    @Override
    public EventStreamDefinition getStreamDefinitionFromStore(Credentials credentials, String streamId) throws StreamDefinitionException {
        return cassandraConnector.getStreamDefinitionFromStore(ClusterFactory.getCluster(credentials), streamId);
    }

    @Override
    protected Collection<EventStreamDefinition> getAllStreamDefinitionsFromStore(Credentials credentials) throws StreamDefinitionException {
        return cassandraConnector.getAllStreamDefinitionFromStore(ClusterFactory.getCluster(credentials));
    }


}
