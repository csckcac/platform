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
import me.prettyprint.hector.api.beans.ColumnSlice;
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
import me.prettyprint.hector.api.query.SliceQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.eventbridge.commons.Attribute;
import org.wso2.carbon.eventbridge.commons.AttributeType;
import org.wso2.carbon.eventbridge.commons.Event;
import org.wso2.carbon.eventbridge.commons.EventStreamDefinition;
import org.wso2.carbon.eventbridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.eventbridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.eventbridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.eventbridge.core.Utils.EventBridgeUtils;
import org.wso2.carbon.eventbridge.core.exception.EventProcessingException;
import org.wso2.carbon.eventbridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.eventbridge.streamdefn.cassandra.Utils.CassandraSDSUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
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

    public static final String BAM_EVENT_DATA_KEYSPACE = "EVENT_KS";
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
    private final static ByteBufferSerializer byteBufferSerializer = ByteBufferSerializer.get();


    Log logger = LogFactory.getLog(CassandraConnector.class);
    private static final String STREAM_TIMESTAMP_KEY = "Timestamp";


    public CassandraConnector() {
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

//    public void createEventStreamColumnFamily(Cluster cluster,
//                                              String streamName) {
//
//        String eventStreamCfName = CassandraSDSUtils.convertStreamNameToCFName(streamName);
//        Keyspace keyspace = HFactory.createKeyspace(BAM_EVENT_DATA_KEYSPACE, cluster);
//        KeyspaceDefinition keyspaceDef =
//                cluster.describeKeyspace(keyspace.getKeyspaceName());
//        List<ColumnFamilyDefinition> cfDef = keyspaceDef.getCfDefs();
//        for (ColumnFamilyDefinition cfdef : cfDef) {
//            if (cfdef.getName().equals(eventStreamCfName)) {
//                if (logger.isDebugEnabled()) {
//                    logger.debug("Column Family" + eventStreamCfName + " is already Exist");
//                }
//                return;
//            }
//        }
//        ColumnFamilyDefinition columnFamilyDefinition = HFactory.
//                createColumnFamilyDefinition(BAM_EVENT_DATA_KEYSPACE,
//                        CassandraSDSUtils.convertStreamNameToCFName(streamName));
//        cluster.addColumnFamily(columnFamilyDefinition);
//    }

    public void createColumnFamily(Cluster cluster, String keyspaceName, String columnFamilyName) {
        Keyspace keyspace = HFactory.createKeyspace(keyspaceName, cluster);
        KeyspaceDefinition keyspaceDef =
                cluster.describeKeyspace(keyspace.getKeyspaceName());
        List<ColumnFamilyDefinition> cfDef = keyspaceDef.getCfDefs();
        for (ColumnFamilyDefinition cfdef : cfDef) {
            if (cfdef.getName().equals(columnFamilyName)) {

                logger.warn("Column Family" + columnFamilyName + " already exists.");

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

    private Mutator<String> prepareBatchMutate(Attribute attribute, Object[] data, DataType dataType, int eventDataIndex
            , String rowKey, String streamColumnFamily, Mutator<String> mutator) {
        String columnName = dataType.name() + "." + attribute.getName();
        if (attribute.getType().equals(AttributeType.STRING)) {
            String metaVal = (String) data[eventDataIndex];
            if (metaVal != null && !metaVal.isEmpty()) {
                mutator.addInsertion(rowKey, streamColumnFamily,
                        HFactory.createColumn(columnName, metaVal, stringSerializer, stringSerializer));
            }
        } else if (attribute.getType().equals(AttributeType.INT)) {
            Integer metaVal = ((Double) data[eventDataIndex]).intValue();
            if (metaVal != null) {
                mutator.addInsertion(rowKey, streamColumnFamily,
                        HFactory.createColumn(columnName, metaVal, stringSerializer, integerSerializer));
            }
        } else if (attribute.getType().equals(AttributeType.FLOAT)) {
            Float metaVal = (Float) data[eventDataIndex];
            if (metaVal != null) {
                mutator.addInsertion(rowKey, streamColumnFamily,
                        HFactory.createColumn(columnName, metaVal, stringSerializer, floatSerializer));
            }
        } else if (attribute.getType().equals(AttributeType.BOOL)) {
            Boolean metaVal = (Boolean) data[eventDataIndex];
            if (metaVal != null) {
                mutator.addInsertion(rowKey, streamColumnFamily,
                        HFactory.createColumn(columnName, metaVal, stringSerializer, booleanSerializer));
            }
        } else if (attribute.getType().equals(AttributeType.DOUBLE)) {
            Double metaVal = (Double) data[eventDataIndex];
            if (metaVal != null) {
                mutator.addInsertion(rowKey, streamColumnFamily,
                        HFactory.createColumn(columnName, metaVal, stringSerializer, doubleSerializer));
            }
        } else if (attribute.getType().equals(AttributeType.LONG)) {
            Long metaVal = (Long) data[eventDataIndex];
            if (metaVal != null) {
                mutator.addInsertion(rowKey, streamColumnFamily,
                        HFactory.createColumn(columnName, metaVal, stringSerializer, longSerializer));
            }
        }
        return mutator;
    }


    public String insertEvent(Cluster cluster, Event eventData)
            throws MalformedStreamDefinitionException,  StreamDefinitionStoreException {
        EventStreamDefinition eventStreamDef;
        eventStreamDef = getStreamDefinitionFromStore(cluster, eventData.getStreamId());
        String streamColumnFamily = getCFNameFromStreamId(cluster, eventData.getStreamId());


        Keyspace keyspace = HFactory.createKeyspace(BAM_EVENT_DATA_KEYSPACE, cluster);
        Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);

        // / add  current server time as time stamp if time stamp is not set
        long timestamp;
        if (eventData.getTimeStamp() != 0L) {
            timestamp = eventData.getTimeStamp();
        } else {
            timestamp = System.currentTimeMillis();
        }

        UUID uuid = UUID.randomUUID();
        String rowKey = CassandraSDSUtils.createRowKey(timestamp, uuid);

        mutator.addInsertion(rowKey, streamColumnFamily,
                HFactory.createStringColumn(STREAM_ID_KEY, eventStreamDef.getStreamId()));
        mutator.addInsertion(rowKey, streamColumnFamily,
                HFactory.createStringColumn(STREAM_NAME_KEY, eventStreamDef.getName()));
        mutator.addInsertion(rowKey, streamColumnFamily,
                HFactory.createStringColumn(STREAM_VERSION_KEY, eventStreamDef.getVersion()));
        mutator.addInsertion(rowKey, streamColumnFamily,
                HFactory.createStringColumn(STREAM_DESCRIPTION_KEY, eventStreamDef.getDescription()));
        mutator.addInsertion(rowKey, streamColumnFamily,
                HFactory.createStringColumn(STREAM_NICK_NAME_KEY, eventStreamDef.getNickName()));

        mutator.addInsertion(rowKey, streamColumnFamily,
                HFactory.createColumn(STREAM_TIMESTAMP_KEY, timestamp, stringSerializer,
                        longSerializer));


        int eventDataIndex = 0;
        if (eventStreamDef.getMetaData() != null) {
            for (Attribute attribute : eventStreamDef.getMetaData()) {
                prepareBatchMutate(attribute, eventData.getMetaData(), DataType.meta, eventDataIndex,
                        rowKey, streamColumnFamily, mutator);
                eventDataIndex++;
            }
        }
        //Iterate for correlation  data
        if (eventData.getCorrelationData() != null) {
            eventDataIndex = 0;
            for (Attribute attribute : eventStreamDef.getCorrelationData()) {
                prepareBatchMutate(attribute, eventData.getCorrelationData(), DataType.correlation, eventDataIndex,
                        rowKey, streamColumnFamily, mutator);
                eventDataIndex++;
            }
        }
        //Iterate for payload data
        if (eventData.getPayloadData() != null) {
            eventDataIndex = 0;
            for (Attribute attribute : eventStreamDef.getPayloadData()) {
                prepareBatchMutate(attribute, eventData.getPayloadData(), DataType.payload,  eventDataIndex,
                        rowKey, streamColumnFamily, mutator);
                eventDataIndex++;
            }
        }

        mutator.execute();

        return rowKey;
    }

    public Event getEvent(Cluster cluster, String streamId, String rowKey ) throws EventProcessingException {

       // get Event definition

        EventStreamDefinition streamDefinition;
        try {
            streamDefinition = getStreamDefinitionFromStore(cluster, streamId);
        } catch (StreamDefinitionStoreException e) {
            String errorMsg = "Error processing stream definition for stream Id : " + streamId;
            logger.error(errorMsg, e);
            throw new EventProcessingException(errorMsg, e);
        }
        List<Attribute> payloadDefinitions = streamDefinition.getPayloadData();
        List<Attribute> correlationDefinitions = streamDefinition.getCorrelationData();
        List<Attribute> metaDefinitions = streamDefinition.getMetaData();


        // start conversion

        SliceQuery<String,String,ByteBuffer> sliceQuery =
                HFactory.createSliceQuery(HFactory.createKeyspace(BAM_EVENT_DATA_KEYSPACE, cluster),
                        stringSerializer, stringSerializer, byteBufferSerializer);
        String cfName = getCFNameFromStreamId(cluster, streamId);
        sliceQuery.setKey(rowKey).setRange("", "", true, Integer.MAX_VALUE).setColumnFamily(
                cfName);
        ColumnSlice<String, ByteBuffer> columnSlice = sliceQuery.execute().get();

        Event event = new Event();
        List<Object> metaData = new ArrayList<Object>();
        List<Object> correlationData = new ArrayList<Object>();
        List<Object> payloadData = new ArrayList<Object>();

        try {
            event.setStreamId(CassandraSDSUtils.getString(columnSlice.getColumnByName(STREAM_ID_KEY).getValue()));
            event.setTimeStamp(CassandraSDSUtils.getLong(columnSlice.getColumnByName(STREAM_TIMESTAMP_KEY).getValue()));

            if (payloadDefinitions != null) {
                for (Attribute payloadDefinition : payloadDefinitions) {
                    payloadData.add(getValueForDataTypeList(columnSlice, payloadDefinition, DataType.payload));
                }
            }

            if (metaDefinitions != null) {
                for (Attribute payloadDefinition : metaDefinitions) {
                    metaData.add(getValueForDataTypeList(columnSlice, payloadDefinition, DataType.meta));
                }
            }

            if (correlationDefinitions != null) {
                for (Attribute payloadDefinition : correlationDefinitions) {
                    correlationData.add(correlationData
                            .add(getValueForDataTypeList(columnSlice, payloadDefinition, DataType.correlation)));
                }
            }
        } catch (IOException e) {
            String errorMsg = "Error during event data conversions.";
            logger.error(errorMsg, e);
            throw new EventProcessingException(errorMsg, e);
        }

        Object[] metas = metaDefinitions == null ? null : metaData.toArray();
        Object[] correlations = correlationDefinitions == null ? null : correlationData.toArray();
        Object[] payloads = payloadDefinitions == null ? null : payloadData.toArray();
        event.setMetaData(metas);
        event.setCorrelationData(correlations);
        event.setPayloadData(payloads);

        return event;
    }

    private String getCFNameFromStreamId(Cluster cluster, String streamId) {
        return CassandraSDSUtils.convertStreamNameToCFName(EventBridgeUtils
                .getStreamNameFromStreamKey(getStreamKeyFromStreamId(cluster, streamId)));
    }

    private Object getValueForDataTypeList(ColumnSlice<String, ByteBuffer> columnSlice,
                                         Attribute payloadDefinition, DataType dataType) throws IOException {
        HColumn<String, ByteBuffer> eventCol =
                columnSlice.getColumnByName(CassandraSDSUtils.getColumnName(dataType, payloadDefinition));
        return CassandraSDSUtils
                .getOriginalValueFromColumnValue(eventCol.getValue(), payloadDefinition.getType());
    }

//    public void insertEventDataColumnKeyLess(Event eventData, String userName,
// String userPassword) throws MalformedStreamDefinitionException {
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
//        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createStringColumn(STREAM_NAME_KEY,
// eventStreamDef.getName()));
//        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createStringColumn(STREAM_VERSION_KEY,
// eventStreamDef.getVersion()));
//        if (eventStreamDef.getDescription() != null && !eventStreamDef.getDescription().isEmpty()) {
//            mutator.addInsertion(randomUUIDString, streamColumnFamily,
// HFactory.createStringColumn(STREAM_DESCRIPTION_KEY, eventStreamDef.getDescription()));
//        }
//        if (eventStreamDef.getNickName() != null && !eventStreamDef.getNickName().isEmpty()) {
//            mutator.addInsertion(randomUUIDString, streamColumnFamily,
// HFactory.createStringColumn(STREAM_NICK_NAME_KEY, eventStreamDef.getNickName()));
//        }
//
//        if (eventData.getMetaData() != null) {
//            for (Object eventMetaData : eventData.getMetaData()) {
//                String columnKeyUUID = UUID.randomUUID().toString();
//                //test the object.tostring result
//                mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(columnKeyUUID,
// eventMetaData.toString(), stringSerializer, stringSerializer));
//            }
//        }
//        if (eventData.getCorrelationData() != null) {
//            for (Object eventCorrelationData : eventData.getCorrelationData()) {
//                String columnKeyUUID = UUID.randomUUID().toString();
//                //test the object.tostring result
//                mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(columnKeyUUID,
// eventCorrelationData.toString(), stringSerializer, stringSerializer));
//            }
//        }
//        if (eventData.getPayloadData() != null) {
//            for (Object eventPayloadData : eventData.getPayloadData()) {
//                String columnKeyUUID = UUID.randomUUID().toString();
//                //test the object.tostring result
//                mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(columnKeyUUID,
// eventPayloadData.toString(), stringSerializer, stringSerializer));
//            }
//        }
//        mutator.execute();
//    }


    public String getStreamKeyFromStreamId(Cluster cluster, EventStreamDefinition streamDefinition) {
        return StreamKeyCache.getStreamKeyFromStreamId(cluster, streamDefinition.getStreamId());
    }

    public String getStreamKeyFromStreamId(Cluster cluster, String streamId) {
        return StreamKeyCache.getStreamKeyFromStreamId(cluster, streamId);
    }

    private static class StreamKeyCache {
        private static LoadingCache<StreamIdClusterBean, String> streamKeyCache = null;

        private StreamKeyCache() {
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
                                Keyspace keyspace =
                                        HFactory.createKeyspace(BAM_META_KEYSPACE, streamIdClusterBean.getCluster());
                                ColumnQuery<String, String, String> columnQuery =
                                        HFactory.createStringColumnQuery(keyspace)
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

                if (!cluster.equals(that.cluster)) return false;
                if (!streamId.equals(that.streamId)) return false;

                return true;
            }

            @Override
            public int hashCode() {
                int result = cluster.hashCode();
                result = 31 * result + streamId.hashCode();
                return result;
            }

            //            @Override
//            public boolean equals(Object o) {
//                if (this == o) return true;
//                if (o == null || getClass() != o.getClass()) return false;
//
//                StreamIdClusterBean that = (StreamIdClusterBean) o;
//
//                //cluster is equal if credentials are equal
//                return cluster.getCredentials().equals(that.cluster.getCredentials()) && streamId.equals(that
//                        .streamId);
//
//            }
//
//            @Override
//            public int hashCode() {
//                // get credentials hashcode
//                int result = cluster.getCredentials().hashCode();
//                result = 31 * result + streamId.hashCode();
//                return result;
//            }
        }
    }


    /**
     * Store event stream definition to Cassandra data store
     *
     * @param cluster               Cluster of the tenant
     * @param eventStreamDefinition Event stream definition
     */
    public void saveStreamDefinitionToStore(Cluster cluster,
                                            EventStreamDefinition eventStreamDefinition) {
         saveStreamDefinitionToStore(cluster, eventStreamDefinition.getStreamId(), eventStreamDefinition);
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
        createColumnFamily(cluster, BAM_EVENT_DATA_KEYSPACE, EventBridgeUtils
                .getStreamNameFromStreamKey(CassandraSDSUtils
                        .convertStreamNameToCFName(EventBridgeUtils.getStreamNameFromStreamKey
                                (getStreamKeyFromStreamId
                                (cluster, streamId)))));
        Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);
        mutator.addInsertion(streamId, BAM_META_STREAM_DEF_CF,
                HFactory.createStringColumn(STREAM_DEF, EventDefinitionConverterUtils
                        .convertToJson(eventStreamDefinition)
                ));

        //mutator.addInsertion(domainName, BAM_META_STREAM_DEF_CF, HFactory.createStringColumn(STREAM_DEF,
        // StreamDefnConverterUtils.convertToJson(eventStreamDefinition)));
        mutator.execute();

    }

    /**
     * Store stream Id and the stream Id key to Cassandra data store
     *
     * @param cluster     Tenant cluster
     *
     * @param eventStreamDefinition stream defn
     */
    public void saveStreamIdToStore(Cluster cluster, EventStreamDefinition eventStreamDefinition) {

        saveStreamIdToStore(cluster, EventBridgeUtils.constructStreamKey(eventStreamDefinition.getName(),
                eventStreamDefinition.getVersion()), eventStreamDefinition.getStreamId());
    }

    /**
     * Store stream Id and the stream Id key to Cassandra data store
     *
     * @param cluster     Tenant cluster
     * @param streamIdKey Stream Id Key
     * @param streamId    Stream Id
     */
    public void saveStreamIdToStore(Cluster cluster, String streamIdKey, String streamId) {

        Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);
        Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);
// domainName));
        mutator.addInsertion(streamId, BAM_META_STREAM_ID_KEY_CF, HFactory.createStringColumn(STREAM_ID_KEY, streamIdKey));
        mutator.addInsertion(streamIdKey, BAM_META_STREAM_ID_CF, HFactory.createStringColumn(STREAM_ID, streamId));
        mutator.execute();
    }

    /**
     * Returns Stream ID stored under  key domainName-streamIdKey
     *
     * @param cluster     Tenant cluster
     * @param streamDefinition Stream Definition
     * @return Returns stored stream Ids
     */
    public String getStreamIdFromStore(Cluster cluster, EventStreamDefinition streamDefinition) {
        String streamIdKey = EventBridgeUtils.constructStreamKey(streamDefinition.getName(), streamDefinition.getVersion());
        return StreamIdCache.getStreamIdFromStreamKey(cluster, streamIdKey);
    }

    /**
     * Returns Stream ID stored under  key domainName-streamIdKey
     *
     * @param cluster     Tenant cluster
     * @param streamIdKey Stream Id key streamName::streamVersion
     * @return Returns stored stream Ids
     */
    public String getStreamIdFromStore(Cluster cluster, String streamIdKey) {
        return StreamIdCache.getStreamIdFromStreamKey(cluster, streamIdKey);
    }

    private static class StreamIdCache {

        private static LoadingCache<StreamKeyClusterBean, String> streamIdCache = null;

        private static void init() {
            synchronized (StreamKeyCache.class) {
                if (streamIdCache != null) {
                    return;
                }
                streamIdCache = CacheBuilder.newBuilder()
                        .maximumSize(1000)
                        .expireAfterAccess(30, TimeUnit.MINUTES)
                        .build(new CacheLoader<StreamKeyClusterBean, String>() {
                            @Override
                            public String load(StreamKeyClusterBean streamKeyClusterBean) throws Exception {
                                Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE,
                                        streamKeyClusterBean.getCluster());
                                ColumnQuery<String, String, String> columnQuery =
                                        HFactory.createStringColumnQuery(keyspace);
                                columnQuery.setColumnFamily(BAM_META_STREAM_ID_CF)
                                        .setKey(streamKeyClusterBean.getStreamKey())
                                        .setName(STREAM_ID);
                                QueryResult<HColumn<String, String>> result = columnQuery.execute();
                                HColumn<String, String> hColumn = result.get();
                                if (hColumn != null) {
                                    return hColumn.getValue();
                                }
                                return null;
                            }
                        }
                        );
            }

        }

        public static String getStreamIdFromStreamKey(Cluster cluster, String streamKey) {
            init();
            return streamIdCache.getUnchecked(new StreamKeyClusterBean(cluster, streamKey));
        }

        private static class StreamKeyClusterBean {
            private Cluster cluster;
            private String streamKey;

            private StreamKeyClusterBean(Cluster cluster, String streamKey) {
                this.cluster = cluster;
                this.streamKey = streamKey;
            }

            public Cluster getCluster() {
                return cluster;
            }

            public String getStreamKey() {
                return streamKey;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                StreamKeyClusterBean that = (StreamKeyClusterBean) o;

                if (!cluster.equals(that.cluster)) return false;
                if (!streamKey.equals(that.streamKey)) return false;

                return true;
            }

            @Override
            public int hashCode() {
                int result = cluster.hashCode();
                result = 31 * result + streamKey.hashCode();
                return result;
            }
        }
    }


    /**
     * Retrun Stream Definition   stored in stream definition column family under key domainName-streamIdKey
     *
     * @param cluster  Tenant cluster
     * @param streamId Stream Id
     * @return Returns event stream definition stored in BAM meta data keyspace
     * @throws StreamDefinitionException Thrown if the stream definitions are malformed
     */

    public EventStreamDefinition getStreamDefinitionFromStore(Cluster cluster, String streamId)
            throws StreamDefinitionStoreException {

        Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);
        ColumnQuery<String, String, String> columnQuery =
                HFactory.createStringColumnQuery(keyspace);
        columnQuery.setColumnFamily(BAM_META_STREAM_DEF_CF).setKey(streamId).setName(STREAM_DEF);
        QueryResult<HColumn<String, String>> result = columnQuery.execute();
        HColumn<String, String> hColumn = result.get();
        try {
            if (hColumn != null) {
                return EventDefinitionConverterUtils.convertFromJson(hColumn.getValue());
            }
        } catch (MalformedStreamDefinitionException e) {
            throw new StreamDefinitionStoreException("Retrieved definition from Cassandra store is malformed. Retrieved "
                    +
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
     */
    public Collection<EventStreamDefinition> getAllStreamDefinitionFromStore(Cluster cluster)
            throws StreamDefinitionStoreException {

        List<EventStreamDefinition> eventStreamDefinition = new ArrayList<EventStreamDefinition>();
        Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);
        ColumnQuery<String, String, String> columnQuery =
                HFactory.createStringColumnQuery(keyspace);
        RangeSlicesQuery<String, String, String> query =
                HFactory.createRangeSlicesQuery(keyspace, stringSerializer, stringSerializer, stringSerializer);
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
                    eventStreamDefinition.add(EventDefinitionConverterUtils.convertFromJson(hColumn.getValue()));
                }
            } catch (MalformedStreamDefinitionException e) {
                throw new StreamDefinitionStoreException(
                        "Retrieved definition from Cassandra store is malformed. Retrieved " +
                                "value : " + hColumn.getValue());
            }
        }
        return eventStreamDefinition;
    }

    /**
     * Insert event definition to tenant event definition column family
     *
     * @param cluster               Tenant cluster
     * @param eventStreamDefinition Event stream definition
     */
    public void insertEventDefinition(Cluster cluster,
                                      EventStreamDefinition eventStreamDefinition) {
        Keyspace keyspace = HFactory.createKeyspace(BAM_EVENT_DATA_KEYSPACE, cluster);
        Mutator<String> mutator = HFactory.createMutator(keyspace, new StringSerializer());
        mutator.addInsertion(eventStreamDefinition.getStreamId(), BAM_EVENT_DATA_STREAM_DEF_CF,
                HFactory.createStringColumn(STREAM_DEF, EventDefinitionConverterUtils
                        .convertToJson(eventStreamDefinition)));
        mutator.execute();
    }
}
