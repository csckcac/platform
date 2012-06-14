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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import me.prettyprint.cassandra.serializers.*;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.ColumnQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.eventbridge.core.beans.Attribute;
import org.wso2.carbon.eventbridge.core.beans.AttributeType;
import org.wso2.carbon.eventbridge.core.beans.Event;
import org.wso2.carbon.eventbridge.core.beans.EventStreamDefinition;
import org.wso2.carbon.eventbridge.core.exceptions.MalformedStreamDefinitionException;
import org.wso2.carbon.eventbridge.core.exceptions.StreamDefinitionException;
import org.wso2.carbon.eventbridge.core.utils.StreamDefnConverterUtils;
import org.wso2.carbon.eventbridge.streamdefn.cassandra.Utils.CassandraSDSUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
//import org.wso2.carbon.agent.server.StreamDefnConverterUtils;

/**
 * Cassandra backend connector  and related operations
 */
public class CassandraConnector {
//
//    public static final String CLUSTER_NAME = "Test Cluster";
//    public static final String USERNAME_KEY = "username";
//    public static final String PASSWORD_KEY = "password";
//    public static final String RPC_PORT = "9160";
//
//    public static final String USERNAME_VALUE = "admin";
//    public static final String PASSWORD_VALUE = "admin";
//
//    public static final String LOCAL_NODE = "localhost";


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

    private final static StringSerializer stringSerializer = StringSerializer.get();
    // private final static BytesArraySerializer bytesArraySerializer = BytesArraySerializer.get();
    // private final static UUIDSerializer uuidSerializer = UUIDSerializer.get();
    private final static IntegerSerializer integerSerializer = IntegerSerializer.get();
    private final static LongSerializer longSerializer = LongSerializer.get();
    private final static BooleanSerializer booleanSerializer = BooleanSerializer.get();
    private final static FloatSerializer floatSerializer = FloatSerializer.get();
    private final static DoubleSerializer doubleSerializer = DoubleSerializer.get();

    Log logger = LogFactory.getLog(CassandraConnector.class);



    public CassandraConnector()  {
        // to test agent API without username and passwd in stream definition

    }


//
//    public Cluster getCassandraConnector(String userName, String userPassword) {
//        //check for existing cluster and create new cluster conneciton if it is not in the cache
//        Map<String, String> credentials =
//                new HashMap<String, String>();
//        credentials.put(USERNAME_KEY, userName);
//        credentials.put(PASSWORD_KEY, userPassword);
//
//        String hostList = getCassandraClusterHostPool();
//        return HFactory.createCluster(CLUSTER_NAME,
//                                      new CassandraHostConfigurator(hostList), credentials);
//    }
//
//    private String getCassandraClusterHostPool() {
//
//        //        String hostList = CSS_NODE0 + ":" + RPC_PORT + "," + CSS_NODE1 + ":" + RPC_PORT + ","
////                + CSS_NODE2 + ":" + RPC_PORT;
//        //String hostList = LOCAL_NODE + ":" + RPC_PORT;
//        return LOCAL_NODE + ":" + RPC_PORT;
//    }

    public void createEventStreamColumnFamily(Cluster cluster,
                                              String streamName) {

        String eventStreamCfName = CassandraSDSUtils.convertStreamNameToCFName(streamName);
        Keyspace keyspace = HFactory.createKeyspace(BAM_EVENT_DATA_KEYSPACE, cluster);
        KeyspaceDefinition keyspaceDef =
                cluster.describeKeyspace(keyspace.getKeyspaceName());
        List<ColumnFamilyDefinition> cfDef = keyspaceDef.getCfDefs();
        for (ColumnFamilyDefinition cfdef : cfDef) {
            if (cfdef.getName().equals(eventStreamCfName)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Column Family" + eventStreamCfName + " is already Exist");
                }
                return;
            }
        }
        ColumnFamilyDefinition columnFamilyDefinition = HFactory.
                createColumnFamilyDefinition(BAM_EVENT_DATA_KEYSPACE, CassandraSDSUtils.convertStreamNameToCFName(streamName));
        cluster.addColumnFamily(columnFamilyDefinition);
    }

    public void createColumnFamily(Cluster cluster, String keyspaceName,  String columnFamilyName) {
        Keyspace keyspace = HFactory.createKeyspace(keyspaceName, cluster);
        KeyspaceDefinition keyspaceDef =
                cluster.describeKeyspace(keyspace.getKeyspaceName());
        List<ColumnFamilyDefinition> cfDef = keyspaceDef.getCfDefs();
        for (ColumnFamilyDefinition cfdef : cfDef) {
            if (cfdef.getName().equals(columnFamilyName)) {

                logger.warn("Column Family" + columnFamilyName + " is already exists.");

                return;
            }
        }
        ColumnFamilyDefinition columnFamilyDefinition = HFactory.
                createColumnFamilyDefinition(keyspaceName, columnFamilyName);
        cluster.addColumnFamily(columnFamilyDefinition);
    }


    public boolean createKeySpaceIfNotExisting(Cluster cluster, String keySpaceName) {

        KeyspaceDefinition keySpaceDef = cluster.describeKeyspace(keySpaceName);

        if (keySpaceDef == null) {
            cluster.addKeyspace(HFactory.createKeyspaceDefinition(keySpaceName));

            keySpaceDef = cluster.describeKeyspace(keySpaceName);
            //Sometimes it takes some time to make keySpaceDef!=null
            int retryCount = 0;
            while (keySpaceDef == null && retryCount < 100) {
                try {
                    Thread.sleep(100);
                    keySpaceDef = cluster.describeKeyspace(keySpaceName);
                    if (keySpaceDef != null) {
                        break;
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
            return true;
        } else {
            return false;
        }


    }


    public void insertEvent(Cluster cluster, Event eventData) throws MalformedStreamDefinitionException, StreamDefinitionException {
        EventStreamDefinition eventStreamDef = null;
        eventStreamDef = getStreamDefinitionFromStore(cluster, eventData.getStreamId());
        String streamColumnFamily = CassandraSDSUtils
                .convertStreamNameToCFName(getStreamKeyFromStreamId(cluster, eventData.getStreamId()));


        Keyspace keyspace = HFactory.createKeyspace(BAM_EVENT_DATA_KEYSPACE, cluster);
        Mutator<String> mutator = HFactory.createMutator(keyspace, new StringSerializer());

        UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();

        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createStringColumn(STREAM_NAME_KEY, eventStreamDef.getName()));
        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createStringColumn(STREAM_VERSION_KEY, eventStreamDef.getVersion()));
        if (eventStreamDef.getDescription() != null && !eventStreamDef.getDescription().isEmpty()) {
            mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createStringColumn(STREAM_DESCRIPTION_KEY, eventStreamDef.getDescription()));
        }
        if (eventStreamDef.getNickName() != null && !eventStreamDef.getNickName().isEmpty()) {
            mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createStringColumn(STREAM_NICK_NAME_KEY, eventStreamDef.getNickName()));
        }

        int eventDataIndex = 0;
        if (eventStreamDef.getMetaData() != null) {
            for (Attribute attribute : eventStreamDef.getMetaData()) {
                if (attribute.getType().equals(AttributeType.STRING)) {
                    String metaVal = (String) eventData.getMetaData()[eventDataIndex];
                    if (metaVal != null && !metaVal.isEmpty()) {
                        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(attribute.getName(), metaVal, stringSerializer, stringSerializer));
                    }
                    eventDataIndex++;
                } else if (attribute.getType().equals(AttributeType.INT)) {
                    Integer metaVal = (Integer) eventData.getMetaData()[eventDataIndex];
                    if (metaVal != null) {
                        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(attribute.getName(), metaVal, stringSerializer, integerSerializer));
                    }
                    eventDataIndex++;
                } else if (attribute.getType().equals(AttributeType.FLOAT)) {
                    Float metaVal = (Float) eventData.getMetaData()[eventDataIndex];
                    if (metaVal != null) {
                        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(attribute.getName(), metaVal, stringSerializer, floatSerializer));
                    }
                    eventDataIndex++;
                } else if (attribute.getType().equals(AttributeType.BOOL)) {
                    Boolean metaVal = (Boolean) eventData.getMetaData()[eventDataIndex];
                    if (metaVal != null) {
                        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(attribute.getName(), metaVal, stringSerializer, booleanSerializer));
                    }
                    eventDataIndex++;
                } else if (attribute.getType().equals(AttributeType.DOUBLE)) {
                    Double metaVal = (Double) eventData.getMetaData()[eventDataIndex];
                    if (metaVal != null) {
                        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(attribute.getName(), metaVal, stringSerializer, doubleSerializer));
                    }
                    eventDataIndex++;
                } else if (attribute.getType().equals(AttributeType.LONG)) {
                    Long metaVal = (Long) eventData.getMetaData()[eventDataIndex];
                    if (metaVal != null) {
                        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(attribute.getName(), metaVal, stringSerializer, longSerializer));
                    }
                    eventDataIndex++;
                }
            }
        }
        //Iterate for correlation  data
        if (eventData.getCorrelationData() != null) {
            eventDataIndex = 0;
            for (Attribute attribute : eventStreamDef.getCorrelationData()) {
                if (attribute.getType().equals(AttributeType.STRING)) {
                    String metaVal = (String) eventData.getCorrelationData()[eventDataIndex];
                    if (metaVal != null && !metaVal.isEmpty()) {
                        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(attribute.getName(), metaVal, stringSerializer, stringSerializer));
                    }
                    eventDataIndex++;
                } else if (attribute.getType().equals(AttributeType.INT)) {
                    Integer metaVal = (Integer) eventData.getCorrelationData()[eventDataIndex];
                    if (metaVal != null) {
                        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(attribute.getName(), metaVal, stringSerializer, integerSerializer));
                    }
                    eventDataIndex++;
                } else if (attribute.getType().equals(AttributeType.FLOAT)) {
                    Float metaVal = (Float) eventData.getCorrelationData()[eventDataIndex];
                    if (metaVal != null) {
                        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(attribute.getName(), metaVal, stringSerializer, floatSerializer));
                    }
                    eventDataIndex++;
                } else if (attribute.getType().equals(AttributeType.BOOL)) {
                    Boolean metaVal = (Boolean) eventData.getCorrelationData()[eventDataIndex];
                    if (metaVal != null) {
                        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(attribute.getName(), metaVal, stringSerializer, booleanSerializer));
                    }
                    eventDataIndex++;
                } else if (attribute.getType().equals(AttributeType.DOUBLE)) {
                    Double metaVal = (Double) eventData.getCorrelationData()[eventDataIndex];
                    if (metaVal != null) {
                        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(attribute.getName(), metaVal, stringSerializer, doubleSerializer));
                    }
                    eventDataIndex++;
                } else if (attribute.getType().equals(AttributeType.LONG)) {
                    Long metaVal = (Long) eventData.getCorrelationData()[eventDataIndex];
                    if (metaVal != null) {
                        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(attribute.getName(), metaVal, stringSerializer, longSerializer));
                    }
                    eventDataIndex++;
                }
            }
        }
        //Iterate for payload data
        if (eventData.getPayloadData() != null) {
            eventDataIndex = 0;
            for (Attribute attribute : eventStreamDef.getPayloadData()) {
                if (attribute.getType().equals(AttributeType.STRING)) {
                    String metaVal = (String) eventData.getPayloadData()[eventDataIndex];
                    if (metaVal != null && !metaVal.isEmpty()) {
                        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(attribute.getName(), metaVal, stringSerializer, stringSerializer));
                    }
                    eventDataIndex++;
                } else if (attribute.getType().equals(AttributeType.INT)) {
                    Integer metaVal = (Integer) eventData.getPayloadData()[eventDataIndex];
                    if (metaVal != null) {
                        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(attribute.getName(), metaVal, stringSerializer, integerSerializer));
                    }
                    eventDataIndex++;
                } else if (attribute.getType().equals(AttributeType.FLOAT)) {
                    Float metaVal = (Float) eventData.getPayloadData()[eventDataIndex];
                    if (metaVal != null) {
                        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(attribute.getName(), metaVal, stringSerializer, floatSerializer));
                    }
                    eventDataIndex++;
                } else if (attribute.getType().equals(AttributeType.BOOL)) {
                    Boolean metaVal = (Boolean) eventData.getPayloadData()[eventDataIndex];
                    if (metaVal != null) {
                        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(attribute.getName(), metaVal, stringSerializer, booleanSerializer));
                    }
                    eventDataIndex++;
                } else if (attribute.getType().equals(AttributeType.DOUBLE)) {
                    Double metaVal = (Double) eventData.getPayloadData()[eventDataIndex];
                    if (metaVal != null) {
                        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(attribute.getName(), metaVal, stringSerializer, doubleSerializer));
                    }
                    eventDataIndex++;
                } else if (attribute.getType().equals(AttributeType.LONG)) {
                    Long metaVal = (Long) eventData.getPayloadData()[eventDataIndex];
                    if (metaVal != null) {
                        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(attribute.getName(), metaVal, stringSerializer, longSerializer));
                    }
                    eventDataIndex++;
                }
            }
        }

        mutator.execute();
    }

//    public void insertEventDataColumnKeyLess(Event eventData, String userName, String userPassword) throws MalformedStreamDefinitionException {
//        Cluster tenantCluster = getCassandraConnector(userName, userPassword);
//        EventStreamDefinition eventStreamDef = getStreamDefinition(tenantCluster, eventData.getStreamId());
//        String streamColumnFamily = eventData.getStreamId();
//        //To change body of created methods use File | Settings | File Templates.
//        Keyspace keyspace = HFactory.createKeyspace(BAM_EVENT_DATA_KEYSPACE, tenantCluster);
//        Mutator<String> mutator = HFactory.createMutator(keyspace, new StringSerializer());
//        // CF key  - to be changed based on analyser
//        UUID uuid = UUID.randomUUID();
//        String randomUUIDString = uuid.toString();
//        //mutator.insert(randomUUIDString, streamColumnFamily, HFactory.createStringColumn(EVENT_DATA, eventData));
//        //add / dupicate CF meta data in the columns
//        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createStringColumn(STREAM_NAME_KEY, eventStreamDef.getName()));
//        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createStringColumn(STREAM_VERSION_KEY, eventStreamDef.getVersion()));
//        if (eventStreamDef.getDescription() != null && !eventStreamDef.getDescription().isEmpty()) {
//            mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createStringColumn(STREAM_DESCRIPTION_KEY, eventStreamDef.getDescription()));
//        }
//        if (eventStreamDef.getNickName() != null && !eventStreamDef.getNickName().isEmpty()) {
//            mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createStringColumn(STREAM_NICK_NAME_KEY, eventStreamDef.getNickName()));
//        }
//
//        if (eventData.getMetaData() != null) {
//            for (Object eventMetaData : eventData.getMetaData()) {
//                String columnKeyUUID = UUID.randomUUID().toString();
//                //test the object.tostring result
//                mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(columnKeyUUID, eventMetaData.toString(), stringSerializer, stringSerializer));
//            }
//        }
//        if (eventData.getCorrelationData() != null) {
//            for (Object eventCorrelationData : eventData.getCorrelationData()) {
//                String columnKeyUUID = UUID.randomUUID().toString();
//                //test the object.tostring result
//                mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(columnKeyUUID, eventCorrelationData.toString(), stringSerializer, stringSerializer));
//            }
//        }
//        if (eventData.getPayloadData() != null) {
//            for (Object eventPayloadData : eventData.getPayloadData()) {
//                String columnKeyUUID = UUID.randomUUID().toString();
//                //test the object.tostring result
//                mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(columnKeyUUID, eventPayloadData.toString(), stringSerializer, stringSerializer));
//            }
//        }
//        mutator.execute();
//    }

    /**
     * Returns stream definition for a given streamId
     *
     * @param tenantCluster Cluster connection for the tenant.
     * @param streamId      Stream ID
     * @return Stream definition
     * @throws MalformedStreamDefinitionException If the stream definitions are malformed
     *
     */
//
//    public EventStreamDefinition getStreamDefinition(Cluster tenantCluster, String streamId)
//            throws MalformedStreamDefinitionException {
//        Keyspace keyspace = HFactory.createKeyspace(BAM_EVENT_DATA_KEYSPACE, tenantCluster);
//        ColumnQuery<String, String, String> columnQuery =
//                HFactory.createStringColumnQuery(keyspace);
//        columnQuery.setColumnFamily(BAM_EVENT_DATA_STREAM_DEF_CF).setKey(streamId).setName(STREAM_DEF);
//        QueryResult<HColumn<String, String>> result = columnQuery.execute();
//        HColumn<String, String> hColumn = result.get();
//        if (hColumn != null) {
//            return StreamDefnConverterUtils.convertFromJson(hColumn.getValue());
//        }
//        return null;
//    }

    /**
     * Store stream Id and the stream Id key to Cassandra data store
     *
     * @param cluster  Tenant cluster
     * @param streamIdKey Stream Id Key
     * @param streamId    Stream Id
     */
    public void saveStreamIdToStore(Cluster cluster, String streamIdKey, String streamId) {

        Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);
        Mutator<String> mutator = HFactory.createMutator(keyspace, new StringSerializer());
//        mutator.insert(streamId, BAM_META_STREAM_ID_CF, HFactory.createStringColumn(STREAM_DEF_DOMAIN, domainName));
//        mutator.addInsertion(streamIdKey, BAM_META_STREAM_ID_KEY_CF, HFactory.createStringColumn(STREAM_DEF_DOMAIN, domainName));
        mutator.addInsertion(streamId, BAM_META_STREAMID_TO_STREAM_ID_KEY, HFactory.createStringColumn(STREAM_ID_KEY, streamIdKey ));
        mutator.execute();
    }

    public String getStreamKeyFromStreamId(Cluster cluster, String streamId) {
        return StreamKeyCache.getStreamKeyFromStreamId(cluster, streamId);
    }

    private static class StreamKeyCache {
        private static LoadingCache<StreamIdClusterBean, String> streamKeyCache = null;

        private StreamKeyCache() {
           synchronized (StreamKeyCache.class) {
                if (streamKeyCache != null) {
                    return;
                }
                streamKeyCache = CacheBuilder.newBuilder()
                        .maximumSize(1000)
                        .expireAfterAccess(30, TimeUnit.MINUTES)
                        .build(new CacheLoader<StreamIdClusterBean, String>() {
                            @Override
                            public String load(StreamIdClusterBean streamIdClusterBean) throws Exception {
                                Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, streamIdClusterBean.getCluster());
                                ColumnQuery<String, String, String> columnQuery = HFactory.createStringColumnQuery(keyspace)
                                        .setColumnFamily(BAM_META_STREAM_ID_KEY_CF)
                                        .setKey(streamIdClusterBean.getStreamId())
                                        .setName(STREAM_ID_KEY);
                                HColumn<String, String> queryResult = columnQuery.execute().get();
                                return queryResult.getValue();
                            }
                        }
                        );
            }
        }

        private static void init() {
            synchronized (StreamKeyCache.class) {
                if (streamKeyCache != null) {
                    return;
                }
                streamKeyCache = CacheBuilder.newBuilder()
                        .maximumSize(1000)
                        .expireAfterAccess(30, TimeUnit.MINUTES)
                        .build(new CacheLoader<StreamIdClusterBean, String>() {
                            @Override
                            public String load(StreamIdClusterBean streamIdClusterBean) throws Exception {
                                Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, streamIdClusterBean.getCluster());
                                ColumnQuery<String, String, String> columnQuery = HFactory.createStringColumnQuery(keyspace)
                                        .setColumnFamily(BAM_META_STREAM_ID_KEY_CF)
                                        .setKey(streamIdClusterBean.getStreamId())
                                        .setName(STREAM_ID_KEY);
                                HColumn<String, String> queryResult = columnQuery.execute().get();
                                return queryResult.getValue();
                            }
                        }
                        );
            }

        }

        public static String getStreamKeyFromStreamId(Cluster cluster, String streamId) {
            init();
            return streamKeyCache.getUnchecked(new StreamIdClusterBean(cluster, streamId));
        }

        private static class StreamIdClusterBean {
            private Cluster cluster;
            private String streamId;

            private StreamIdClusterBean(Cluster cluster, String streamId) {
                this.cluster = cluster;
                this.streamId = streamId;
            }

            public Cluster getCluster() {
                return cluster;
            }

            public String getStreamId() {
                return streamId;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                StreamIdClusterBean that = (StreamIdClusterBean) o;

                //cluster is equal if credentials are equal
                return cluster.getCredentials().equals(that.cluster.getCredentials()) && streamId.equals(that.streamId);

            }

            @Override
            public int hashCode() {
                // get credentials hashcode
                int result = cluster.getCredentials().hashCode();
                result = 31 * result + streamId.hashCode();
                return result;
            }
        }
    }




    /**
     * Store event stream definition to Cassandra data store
     *
     * @param cluster               Cluster of the tenant
     * @param streamId              Stream Id
     * @param eventStreamDefinition Event stream definition
     */
    public void saveStreamDefinitionToStore(Cluster cluster, String streamId,
                                            EventStreamDefinition eventStreamDefinition) {

        Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);
        Mutator<String> mutator = HFactory.createMutator(keyspace, new StringSerializer());
        mutator.addInsertion(streamId, BAM_META_STREAM_DEF_CF, HFactory.createStringColumn(STREAM_DEF, StreamDefnConverterUtils.convertToJson(eventStreamDefinition)));
        //mutator.addInsertion(domainName, BAM_META_STREAM_DEF_CF, HFactory.createStringColumn(STREAM_DEF, StreamDefnConverterUtils.convertToJson(eventStreamDefinition)));
        mutator.execute();

    }

    /**
     * Returns Stream ID stored under  key domainName-streamIdKey
     *
     * @param cluster   Tenant cluster
     * @param streamIdKey Stream Id key streamName::streamVersion
     * @return Returns stored stream Ids
     */
    public String getStreamIdFromStore(Cluster cluster, String streamIdKey) {
        Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);
        ColumnQuery<String, String, String> columnQuery =
                HFactory.createStringColumnQuery(keyspace);
        columnQuery.setColumnFamily(BAM_META_STREAM_ID_CF).setKey(streamIdKey).setName(STREAM_ID);
        QueryResult<HColumn<String, String>> result = columnQuery.execute();
        HColumn<String, String> hColumn = result.get();
        if (hColumn != null) {
            return hColumn.getValue();
        }
        return null;
    }

    /**
     * Retrun Stream Definition   stored in stream definition column family under key domainName-streamIdKey
     *
     * @param cluster Tenant cluster
     * @param streamId   Stream Id
     * @return Returns event stream definition stored in BAM meta data keyspace
     * @throws StreamDefinitionException  Thrown if the stream definitions are malformed
     */

    public EventStreamDefinition getStreamDefinitionFromStore(Cluster cluster, String streamId) throws StreamDefinitionException {

            Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);
            ColumnQuery<String, String, String> columnQuery =
                    HFactory.createStringColumnQuery(keyspace);
            columnQuery.setColumnFamily(BAM_META_STREAM_DEF_CF).setKey(streamId).setName(STREAM_DEF);
            QueryResult<HColumn<String, String>> result = columnQuery.execute();
            HColumn<String, String> hColumn = result.get();
        try {
            if (hColumn != null) {
                return StreamDefnConverterUtils.convertFromJson(hColumn.getValue());
            }
        } catch (MalformedStreamDefinitionException e) {
            throw new StreamDefinitionException("Retrieved definition from Cassandra store is malformed. Retrieved " +
                    "value : " + hColumn.getValue());
        }
        return null;

    }

    /**
     * Retrun all stream definitions stored under one domain
     *
     * @param cluster Tenant cluster
     * @return All stream definitions related to given tenant domain
     * @throws StreamDefinitionException If the stream definitions are malformed
     *
     */
    public Collection<EventStreamDefinition> getAllStreamDefinitionFromStore(Cluster cluster)
            throws StreamDefinitionException {

        List<EventStreamDefinition> eventStreamDefinition = new ArrayList<EventStreamDefinition>();
        Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);
        ColumnQuery<String, String, String> columnQuery =
                HFactory.createStringColumnQuery(keyspace);
        RangeSlicesQuery<String, String, String> query = HFactory.createRangeSlicesQuery(keyspace, stringSerializer, stringSerializer, stringSerializer);
        query.setColumnFamily(BAM_META_STREAM_ID_CF);
        query.setKeys("", "");
        QueryResult<OrderedRows<String, String, String>> result = query.execute();
        for (Row<String, String, String> row : result.get()) {
            if (row == null) {
                continue;
            }
            String streamId = row.getKey();
            columnQuery.setColumnFamily(BAM_META_STREAM_DEF_CF).setKey(streamId).setName(STREAM_DEF);
            QueryResult<HColumn<String, String>> streamDef = columnQuery.execute();
            HColumn<String, String> hColumn = streamDef.get();
            try {
                if (hColumn != null) {
                    eventStreamDefinition.add(StreamDefnConverterUtils.convertFromJson(hColumn.getValue()));
                }
            } catch (MalformedStreamDefinitionException e) {
                throw new StreamDefinitionException("Retrieved definition from Cassandra store is malformed. Retrieved " +
                        "value : " + hColumn.getValue());
            }
        }
        return eventStreamDefinition;
    }

    /**
     * Insert event definition to tenant event definition column family
     *
     * @param cluster Tenant cluster
     * @param eventStreamDefinition Event stream definition
     */
    public void insertEventDefinition(Cluster cluster,
                                      EventStreamDefinition eventStreamDefinition) {
        Keyspace keyspace = HFactory.createKeyspace(BAM_EVENT_DATA_KEYSPACE, cluster);
        Mutator<String> mutator = HFactory.createMutator(keyspace, new StringSerializer());
        mutator.addInsertion(eventStreamDefinition.getStreamId(), BAM_EVENT_DATA_STREAM_DEF_CF, HFactory.createStringColumn(STREAM_DEF, StreamDefnConverterUtils.convertToJson(eventStreamDefinition)));
        mutator.execute();
    }
}
