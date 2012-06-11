package org.wso2.carbon.bam.eventreceiver.datastore;

import me.prettyprint.cassandra.model.IndexedSlicesQuery;
import me.prettyprint.cassandra.serializers.*;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
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

    public static final String LOCAL_NODE = "localhost";


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

    Log logger = LogFactory.getLog(CassandraConnector.class);

    private Cluster cluster = null;


    private static final String DOMAIN_NAME = "DOMAIN_NAME";
    private static final String WSO2_CARBON_STAND_ALONE = "WSO2-CARBON-STAND-ALONE";

    public CassandraConnector() {
        // to test agent API without username and passwd in stream definition
        Map<String, String> credentials =
                new HashMap<String, String>();
        credentials.put(USERNAME_KEY, USERNAME_VALUE);
        credentials.put(PASSWORD_KEY, PASSWORD_VALUE);
////        String hostList = CSS_NODE0 + ":" + RPC_PORT + "," + CSS_NODE1 + ":" + RPC_PORT + ","
////                + CSS_NODE2 + ":" + RPC_PORT;                                                                                String
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

    public void createEventStreamColumnFamily(String domainName, String streamName, String userName,
                                   String userPassword) {
        if (domainName != null) {
            userName = userName + "@" + domainName;
        }
        Cluster tenantCluster = getCassandraConnector(userName, userPassword);
        String eventStreamCfName = streamName.replace(".", "_");
        Keyspace keyspace = HFactory.createKeyspace(BAM_EVENT_DATA_KEYSPACE, tenantCluster);
        KeyspaceDefinition keyspaceDef =
                tenantCluster.describeKeyspace(keyspace.getKeyspaceName());
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
                createColumnFamilyDefinition(BAM_EVENT_DATA_KEYSPACE, streamName.replace(".", "_"));
        tenantCluster.addColumnFamily(columnFamilyDefinition);
    }

    public void createColumnFamily(String domainName, String columnFamilyName, String userName,
                                   String userPassword) {
        if (domainName != null) {
            userName = userName + "@" + domainName;
        }
        Cluster tenantCluster = getCassandraConnector(userName, userPassword);
        Keyspace keyspace = HFactory.createKeyspace(BAM_EVENT_DATA_KEYSPACE, tenantCluster);
        KeyspaceDefinition keyspaceDef =
                tenantCluster.describeKeyspace(keyspace.getKeyspaceName());
        List<ColumnFamilyDefinition> cfDef = keyspaceDef.getCfDefs();
        for (ColumnFamilyDefinition cfdef : cfDef) {
            if (cfdef.getName().equals(columnFamilyName)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Column Family" + columnFamilyName + " is already Exist");
                }
                return;
            }
        }
        ColumnFamilyDefinition columnFamilyDefinition = HFactory.
                createColumnFamilyDefinition(BAM_EVENT_DATA_KEYSPACE, columnFamilyName);
        tenantCluster.addColumnFamily(columnFamilyDefinition);
    }


    public boolean createKeySpaceIfNotExisting(String keySpaceName, String userName,
                                               String password) {

        Cluster cluster = getCassandraConnector(userName, password);

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


    public void insertEvent(Event eventData, String userName, String userPassword,
                            String domainName) throws MalformedStreamDefinitionException {
        String sessionId[] = eventData.getStreamId().split("-");
        Cluster tenantCluster = getCassandraConnector(userName, userPassword);
        EventStreamDefinition eventStreamDef = getStreamDefinition(tenantCluster, eventData.getStreamId());
        String streamColumnFamily = sessionId[0].replace(".", "_");

        if (domainName != null) {
            userName = userName + "@" + domainName;
        }

        createKeySpaceIfNotExisting(BAM_EVENT_DATA_KEYSPACE, userName, userPassword);

        Keyspace keyspace = HFactory.createKeyspace(BAM_EVENT_DATA_KEYSPACE, tenantCluster);
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


    public EventStreamDefinition getStreamDefinition(Cluster tenantCluster, String streamId)
            throws MalformedStreamDefinitionException {
        Keyspace keyspace = HFactory.createKeyspace(BAM_EVENT_DATA_KEYSPACE, tenantCluster);
        ColumnQuery<String, String, String> columnQuery =
                HFactory.createStringColumnQuery(keyspace);
        columnQuery.setColumnFamily(BAM_EVENT_DATA_STREAM_DEF_CF).setKey(streamId).setName(STREAM_DEF);
        QueryResult<HColumn<String, String>> result = columnQuery.execute();
        HColumn<String, String> hColumn = result.get();
        if (hColumn != null) {
            return EventConverter.convertFromJson(hColumn.getValue());
        }
        return null;
    }

    /**
     * Store stream Id and the stream Id key to Cassandra data store
     *
     * @param domainName  Tenant domain name
     * @param streamIdKey Stream Id Key
     * @param streamId    Stream Id
     */
    public void saveStreamIdToStore(String domainName, String streamIdKey, String streamId) {
        if (domainName == null) {
            domainName = WSO2_CARBON_STAND_ALONE;
        }
        Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);
        Mutator<String> mutator = HFactory.createMutator(keyspace, new StringSerializer());
        mutator.insert(streamId, BAM_META_STREAM_ID_CF, HFactory.createStringColumn(STREAM_DEF_DOMAIN, domainName));
        mutator.addInsertion(streamIdKey, BAM_META_STREAM_ID_KEY_CF, HFactory.createStringColumn(STREAM_DEF_DOMAIN, domainName));
        mutator.addInsertion(streamId, BAM_META_STREAMID_TO_STREAM_ID_KEY, HFactory.createStringColumn(STREAM_ID_KEY, streamId));
        mutator.execute();
    }

    /**
     * Store event stream definition to Cassandra data store
     *
     * @param domainName            Domain name
     * @param streamId              Stream Id
     * @param eventStreamDefinition Event stream definition
     */
    public void saveStreamDefinitionToStore(String domainName, String streamId,
                                            EventStreamDefinition eventStreamDefinition) {
        if (domainName == null) {
            domainName = WSO2_CARBON_STAND_ALONE;
        }
        Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);
        Mutator<String> mutator = HFactory.createMutator(keyspace, new StringSerializer());
        mutator.addInsertion(domainName + "-" + streamId, BAM_META_STREAM_DEF_CF, HFactory.createStringColumn(STREAM_DEF, EventConverter.convertToJson(eventStreamDefinition)));
        //mutator.addInsertion(domainName, BAM_META_STREAM_DEF_CF, HFactory.createStringColumn(STREAM_DEF, EventConverter.convertToJson(eventStreamDefinition)));
        mutator.execute();

    }

    /**
     * Returns Stream ID stored under  key domainName-streamIdKey
     *
     * @param domainName  Tenant domain
     * @param streamIdKey Stream Id key streamName::streamVersion
     * @return Returns stored stream Ids
     */
    public String getStreamIdFromStore(String domainName, String streamIdKey) {
        if (domainName == null) {
            domainName = WSO2_CARBON_STAND_ALONE;
        }
        Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);
        ColumnQuery<String, String, String> columnQuery =
                HFactory.createStringColumnQuery(keyspace);
        columnQuery.setColumnFamily(BAM_META_STREAM_ID_CF).setKey(domainName + "-" + streamIdKey).setName(STREAM_ID);
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
     * @param domainName Tenant domain name
     * @param streamId   Stream Id
     * @return Returns event stream definition stored in BAM meta data keyspace
     * @throws MalformedStreamDefinitionException
     *
     */

    public EventStreamDefinition getStreamDefinitionFromStore(String domainName, String streamId)
            throws MalformedStreamDefinitionException {
        if (domainName == null) {
            domainName = WSO2_CARBON_STAND_ALONE;
        }
        Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);
        ColumnQuery<String, String, String> columnQuery =
                HFactory.createStringColumnQuery(keyspace);
        columnQuery.setColumnFamily(BAM_META_STREAM_DEF_CF).setKey(domainName + "-" + streamId).setName(STREAM_DEF);
        QueryResult<HColumn<String, String>> result = columnQuery.execute();
        HColumn<String, String> hColumn = result.get();
        if (hColumn != null) {
            return EventConverter.convertFromJson(hColumn.getValue());
        }
        return null;

    }

    /**
     * Retrun all stream definitions stored under one domain
     *
     * @param domainName Tenant domain name
     * @return All stream definitions related to given tenant domain
     * @throws MalformedStreamDefinitionException
     *
     */
    public Collection<EventStreamDefinition> getAllStreamDefinitionFromStore(String domainName)
            throws MalformedStreamDefinitionException {
        if (domainName == null) {
            domainName = WSO2_CARBON_STAND_ALONE;
        }
        List<EventStreamDefinition> eventStreamDefinition = new ArrayList<EventStreamDefinition>();
        Keyspace keyspace = HFactory.createKeyspace(BAM_META_KEYSPACE, cluster);
        ColumnQuery<String, String, String> columnQuery =
                HFactory.createStringColumnQuery(keyspace);
        IndexedSlicesQuery<String, String, String> query = HFactory.createIndexedSlicesQuery(keyspace, stringSerializer, stringSerializer, stringSerializer);
        query.addEqualsExpression(DOMAIN_NAME, domainName);
        query.setColumnFamily(BAM_META_STREAM_ID_CF);
        query.setStartKey("");
        QueryResult<OrderedRows<String, String, String>> result = query.execute();
        for (Row<String, String, String> row : result.get()) {
            if (row == null) {
                continue;
            }
            String streamId = row.getKey();
            columnQuery.setColumnFamily(BAM_META_STREAM_DEF_CF).setKey(domainName + "-" + streamId).setName(STREAM_DEF);
            QueryResult<HColumn<String, String>> streamDef = columnQuery.execute();
            HColumn<String, String> hColumn = streamDef.get();
            if (hColumn != null) {
                eventStreamDefinition.add(EventConverter.convertFromJson(hColumn.getValue()));
            }
        }
        return eventStreamDefinition;
    }

    /**
     * Insert event definition to tenant event definition column family
     *
     * @param userName              Tenant user name
     * @param userPassword          Tenant password
     * @param eventStreamDefinition Event stream definition
     */
    public void insertEventDefinition(String userName, String userPassword,
                                      EventStreamDefinition eventStreamDefinition) {
        Cluster tenantCluster = getCassandraConnector(userName, userPassword);
        Keyspace keyspace = HFactory.createKeyspace(BAM_EVENT_DATA_KEYSPACE, tenantCluster);
        Mutator<String> mutator = HFactory.createMutator(keyspace, new StringSerializer());
        mutator.addInsertion(eventStreamDefinition.getStreamId(), BAM_EVENT_DATA_STREAM_DEF_CF, HFactory.createStringColumn(STREAM_DEF, EventConverter.convertToJson(eventStreamDefinition)));
        mutator.execute();
    }
}
