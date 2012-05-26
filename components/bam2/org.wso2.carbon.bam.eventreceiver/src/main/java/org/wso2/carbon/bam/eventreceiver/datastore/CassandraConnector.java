package org.wso2.carbon.bam.eventreceiver.datastore;

import me.prettyprint.cassandra.serializers.*;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.StringKeyIterator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.ColumnQuery;
import me.prettyprint.hector.api.query.QueryResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.agent.commons.Attribute;
import org.wso2.carbon.agent.commons.AttributeType;
import org.wso2.carbon.agent.commons.Event;
import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.agent.server.internal.utils.EventConverter;

import java.util.*;


public class CassandraConnector {

    public static final String CLUSTER_NAME = "Test Cluster";
    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    public static final String RPC_PORT = "9160";

    public static final String USERNAME_VALUE = "admin";
    public static final String PASSWORD_VALUE = "admin";
//    public static final String CSS_NODE0 = "css0.stratoslive.wso2.com";
//    public static final String CSS_NODE1 = "css1.stratoslive.wso2.com";
//    public static final String CSS_NODE2 = "css2.stratoslive.wso2.com";

    public static final String LOCAL_NODE = "localhost";
    public static final String BAM_EVENT_DATA_KEYSPACE = "BAM_EVENT_DATA";
    //public static final String EVENT_DATA = "EVENT_DATA";

    private static final String STREAM_NAME_KEY = "Name";
    private static final String STREAM_VERSION_KEY = "Version";
    private static final String STREAM_NICK_NAME_KEY = "Nick_Name";
    private static final String STREAM_DESCRIPTION_KEY = "Description";

    public static final String BAM_META_KEYSPACE = "BAM_AGENT_API_META_DATA";
    public static final String BAM_META_STREAM_ID_CF = "AGENT_STEAM_ID";
    public static final String BAM_META_STREAM_DEF_CF = "AGENT_STEAM_DEF";

    public static final String STREAM_ID_NAME = "STREAM_ID";
    public static final String STREAM_DEF_NAME = "STREAM_ID";

    private final static StringSerializer stringSerializer = StringSerializer.get();
    // private final static BytesArraySerializer bytesArraySerializer = BytesArraySerializer.get();
    // private final static UUIDSerializer uuidSerializer = UUIDSerializer.get();
    private final static IntegerSerializer integerSerializer = IntegerSerializer.get();
    private final static LongSerializer longSerializer = LongSerializer.get();
    private final static BooleanSerializer booleanSerializer = BooleanSerializer.get();
    private final static FloatSerializer floatSerializer = FloatSerializer.get();
    private final static DoubleSerializer doubleSerializer = DoubleSerializer.get();

    Log logger = LogFactory.getLog(CassandraConnector.class);


    private Cluster cluster = null;

    public CassandraConnector() {
        // to test agent API without username and passwd in stream definition
        Map<String, String> credentials =
                new HashMap<String, String>();
        credentials.put(USERNAME_KEY, USERNAME_VALUE);
        credentials.put(PASSWORD_KEY, PASSWORD_VALUE);
////        String hostList = CSS_NODE0 + ":" + RPC_PORT + "," + CSS_NODE1 + ":" + RPC_PORT + ","
////                + CSS_NODE2 + ":" + RPC_PORT;
        String hostList = LOCAL_NODE + ":" + RPC_PORT;
        cluster = HFactory.createCluster(CLUSTER_NAME,
                new CassandraHostConfigurator(hostList), credentials);
    }

    public Cluster getCassandraConnector(String userName, String userPassword) {
        //check for existing cluster and create new cluster conneciton if it is not in the cache
        Map<String, String> credentials =
                new HashMap<String, String>();
        credentials.put(USERNAME_KEY, userName);
        credentials.put(PASSWORD_KEY, userPassword);

        String hostList = getCassandraClusterHostPool();
        return HFactory.createCluster(CLUSTER_NAME,
                new CassandraHostConfigurator(hostList), credentials);
    }

    private String getCassandraClusterHostPool() {

        //        String hostList = CSS_NODE0 + ":" + RPC_PORT + "," + CSS_NODE1 + ":" + RPC_PORT + ","
//                + CSS_NODE2 + ":" + RPC_PORT;
        //String hostList = LOCAL_NODE + ":" + RPC_PORT;
        return LOCAL_NODE + ":" + RPC_PORT;
    }

    public void createColumnFamily(String domainName, String streamName,String userName, String userPassword) {
        if (domainName != null) {
            userName = userName + "@" + domainName;
        }
        Cluster tenantCluster = getCassandraConnector(userName, userPassword);
        String eventStreamCfName =  streamName.replace(".", "_");
        Keyspace keyspace = HFactory.createKeyspace(BAM_EVENT_DATA_KEYSPACE, tenantCluster);
        KeyspaceDefinition keyspaceDef =
                tenantCluster.describeKeyspace(keyspace.getKeyspaceName());
        List<ColumnFamilyDefinition> cfDef = keyspaceDef.getCfDefs();
        for (ColumnFamilyDefinition cfdef : cfDef) {
            logger.info("Column Name: " + cfdef.getName());
            if (cfdef.getName().equals(eventStreamCfName)) {
                logger.info("Column Family is already Exist");
                return;
            }
        }
        ColumnFamilyDefinition columnFamilyDefinition = HFactory.
                createColumnFamilyDefinition(BAM_EVENT_DATA_KEYSPACE, streamName.replace(".", "_"));
        cluster.addColumnFamily(columnFamilyDefinition);
    }


    public void insertEvent(Event eventData, String userName, String userPassword) throws MalformedStreamDefinitionException {
        String sessionId[] = eventData.getStreamId().split("-");
        EventStreamDefinition eventStreamDef = getStreamDefinition(sessionId[0] + "-" + sessionId[1]);
        Cluster tenantCluster = getCassandraConnector(userName, userPassword);
        // String streamColumnFamily = getStreamDefinition(eventData.getStreamId()).getName();
        String streamColumnFamily = sessionId[0].replace(".", "_");
        //To change body of created methods use File | Settings | File Templates.
        Keyspace keyspace = HFactory.createKeyspace(BAM_EVENT_DATA_KEYSPACE, tenantCluster);
        Mutator<String> mutator = HFactory.createMutator(keyspace, new StringSerializer());
        // CF key  - to be changed based on analyser
        UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();
        //mutator.insert(randomUUIDString, streamColumnFamily, HFactory.createStringColumn(EVENT_DATA, eventData));
        //add / dupicate CF meta data in the columns
        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createStringColumn(STREAM_NAME_KEY, eventStreamDef.getName()));
        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createStringColumn(STREAM_VERSION_KEY, eventStreamDef.getVersion()));
        if (eventStreamDef.getDescription() != null && !eventStreamDef.getDescription().isEmpty()) {
            mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createStringColumn(STREAM_DESCRIPTION_KEY, eventStreamDef.getDescription()));
        }
        if (eventStreamDef.getNickName() != null && !eventStreamDef.getNickName().isEmpty()) {
            mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createStringColumn(STREAM_NICK_NAME_KEY, eventStreamDef.getNickName()));
        }
        // To insert Data type specific data
        //Iterate for Meta data

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

    public void insertEventDataColumnKeyLess(Event eventData, String userName, String userPassword) throws MalformedStreamDefinitionException {
        EventStreamDefinition eventStreamDef = getStreamDefinition(eventData.getStreamId());
        Cluster tenantCluster = getCassandraConnector(userName, userPassword);
        String streamColumnFamily = eventData.getStreamId();
        //To change body of created methods use File | Settings | File Templates.
        Keyspace keyspace = HFactory.createKeyspace(BAM_EVENT_DATA_KEYSPACE, tenantCluster);
        Mutator<String> mutator = HFactory.createMutator(keyspace, new StringSerializer());
        // CF key  - to be changed based on analyser
        UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();
        //mutator.insert(randomUUIDString, streamColumnFamily, HFactory.createStringColumn(EVENT_DATA, eventData));
        //add / dupicate CF meta data in the columns
        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createStringColumn(STREAM_NAME_KEY, eventStreamDef.getName()));
        mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createStringColumn(STREAM_VERSION_KEY, eventStreamDef.getVersion()));
        if (eventStreamDef.getDescription() != null && !eventStreamDef.getDescription().isEmpty()) {
            mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createStringColumn(STREAM_DESCRIPTION_KEY, eventStreamDef.getDescription()));
        }
        if (eventStreamDef.getNickName() != null && !eventStreamDef.getNickName().isEmpty()) {
            mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createStringColumn(STREAM_NICK_NAME_KEY, eventStreamDef.getNickName()));
        }

        if (eventData.getMetaData() != null) {
            for (Object eventMetaData : eventData.getMetaData()) {
                String columnKeyUUID = UUID.randomUUID().toString();
                //test the object.tostring result
                mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(columnKeyUUID, eventMetaData.toString(), stringSerializer, stringSerializer));
            }
        }
        if (eventData.getCorrelationData() != null) {
            for (Object eventCorrelationData : eventData.getCorrelationData()) {
                String columnKeyUUID = UUID.randomUUID().toString();
                //test the object.tostring result
                mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(columnKeyUUID, eventCorrelationData.toString(), stringSerializer, stringSerializer));
            }
        }
        if (eventData.getPayloadData() != null) {
            for (Object eventPayloadData : eventData.getPayloadData()) {
                String columnKeyUUID = UUID.randomUUID().toString();
                //test the object.tostring result
                mutator.addInsertion(randomUUIDString, streamColumnFamily, HFactory.createColumn(columnKeyUUID, eventPayloadData.toString(), stringSerializer, stringSerializer));
            }
        }
        mutator.execute();
    }

    public String getStreamId(String domainName, String streamName, String streamVersion) {

        String streamIdKey = createStreamIdKey(streamName, streamVersion);
        //Careate KS if it is not there - DO We really need this in getStreamId
//        if (!isKeyspaceExist(BAM_META_KEYSPACE)) {
//            Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);
//            ColumnFamilyDefinition columnFamilyDefinition = HFactory.
//                    createColumnFamilyDefinition(BAM_META_KEYSPACE, BAM_META_STREAM_ID_CF);
//            cluster.addColumnFamily(columnFamilyDefinition);
//        }
        Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);
        ColumnQuery<String, String, String> columnQuery =
                HFactory.createStringColumnQuery(keyspace);
        columnQuery.setColumnFamily(BAM_META_STREAM_ID_CF).setKey(streamIdKey).setName(STREAM_ID_NAME);
        QueryResult<HColumn<String, String>> result = columnQuery.execute();
        HColumn<String, String> hColumn = result.get();
        if (hColumn != null) {
            return hColumn.getValue();
        }
        return null;

    }

    public void saveStreamDefinitionToCSS(String domainName,
                                          EventStreamDefinition eventStreamDefinition) {
        //Create KS if it no there
        if (!isKeyspaceExist(BAM_META_KEYSPACE)) {
            HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);
            ColumnFamilyDefinition columnFamilyDefinition = HFactory.
                    createColumnFamilyDefinition(BAM_META_KEYSPACE, BAM_META_STREAM_ID_CF);
            cluster.addColumnFamily(columnFamilyDefinition);
            columnFamilyDefinition = HFactory.
                    createColumnFamilyDefinition(BAM_META_KEYSPACE, BAM_META_STREAM_DEF_CF);
            cluster.addColumnFamily(columnFamilyDefinition);
        }
        Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);
        //add streamId to Stream Definition CF
        Mutator<String> mutator = HFactory.createMutator(keyspace, new StringSerializer());
        String streamIdKey = createStreamIdKey(eventStreamDefinition.getName(), eventStreamDefinition.getVersion());
        mutator.insert(streamIdKey, BAM_META_STREAM_DEF_CF, HFactory.createStringColumn(STREAM_DEF_NAME, EventConverter.convertToJson(eventStreamDefinition)));
        //add streamId to StreamId CF  - do we need this
        mutator.insert(streamIdKey, BAM_META_STREAM_ID_CF, HFactory.createStringColumn(STREAM_ID_NAME, streamIdKey));
        //add CF for the event stream

    }

    public EventStreamDefinition getStreamDefinition(String streamId) throws MalformedStreamDefinitionException {
        Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);
        ColumnQuery<String, String, String> columnQuery =
                HFactory.createStringColumnQuery(keyspace);
        columnQuery.setColumnFamily(BAM_META_STREAM_DEF_CF).setKey(streamId).setName(STREAM_ID_NAME);
        QueryResult<HColumn<String, String>> result = columnQuery.execute();
        HColumn<String, String> hColumn = result.get();
        if (hColumn != null) {
            return EventConverter.convertFromJson(hColumn.getValue());
        }
        return null;
    }

    private String createStreamIdKey(String streamName, String streamVersion) {
        return streamName + "-" + streamVersion;
    }

    public boolean isKeyspaceExist(String keyspaceName) {
       // Keyspace keyspace = HFactory.createKeyspace(keyspaceName, cluster);
        if (cluster.describeKeyspace(keyspaceName).getName().equals(BAM_META_KEYSPACE)) {
            return true;
        }
        return false;
    }

//    public boolean isColumnFamilyExist(String columnFamilyName) {
//
//        return false;
//    }

    public Collection<EventStreamDefinition> getAllStreamDefinition() throws MalformedStreamDefinitionException {
        List<EventStreamDefinition> eventStreamDefinition = new ArrayList<EventStreamDefinition>();
        Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);
        ColumnQuery<String, String, String> columnQuery =
                HFactory.createStringColumnQuery(keyspace);
        StringKeyIterator stringKeyIterator = new StringKeyIterator(keyspace,BAM_META_STREAM_DEF_CF);
        for (String streamId :stringKeyIterator){
            columnQuery.setColumnFamily(BAM_META_STREAM_DEF_CF).setKey(streamId).setName(STREAM_ID_NAME);
            QueryResult<HColumn<String, String>> result = columnQuery.execute();
            HColumn<String, String> hColumn = result.get();
            if (hColumn != null) {
                eventStreamDefinition.add(EventConverter.convertFromJson(hColumn.getValue()));
            }
        }
      return eventStreamDefinition;
    }
}
