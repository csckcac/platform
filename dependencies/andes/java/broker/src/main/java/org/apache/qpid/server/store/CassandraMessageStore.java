/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.qpid.server.store;

import me.prettyprint.cassandra.serializers.*;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.ColumnQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;
import me.prettyprint.hector.api.query.SliceQuery;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.qpid.AMQException;
import org.apache.qpid.AMQStoreException;
import org.apache.qpid.framing.AMQShortString;
import org.apache.qpid.framing.FieldTable;
import org.apache.qpid.server.ClusterResourceHolder;
import org.apache.qpid.server.cassandra.AndesConsistantLevelPolicy;
import org.apache.qpid.server.cassandra.CassandraQueueMessage;
import org.apache.qpid.server.cassandra.DefaultClusteringEnabledSubscriptionManager;
import org.apache.qpid.server.cassandra.CassandraTopicPublisherManager;
import org.apache.qpid.server.cluster.ClusterManager;
import org.apache.qpid.server.configuration.ClusterConfiguration;
import org.apache.qpid.server.exchange.Exchange;
import org.apache.qpid.server.logging.LogSubject;
import org.apache.qpid.server.message.AMQMessage;
import org.apache.qpid.server.message.MessageMetaData;
import org.apache.qpid.server.message.MessageMetaData_0_10;
import org.apache.qpid.server.message.MessageTransferMessage;
import org.apache.qpid.server.protocol.AMQProtocolSession;
import org.apache.qpid.server.queue.*;
import org.apache.qpid.server.virtualhost.VirtualHostConfigSynchronizer;

import java.io.ByteArrayOutputStream;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
/**
 * CassandraMessageStore
 *
 * Message Store implemented using cassandra
 * Working with qpid as and alternative to Derby Message Store
 *
 * */
public class CassandraMessageStore implements MessageStore {

    private Cluster cluster;
    private final String USERNAME_KEY = "username";
    private final String PASSWORD_KEY = "password";
    private final String HOST_KEY = "host";
    private final String PORT_KEY = "port";
    private final String CLUSTER_KEY = "cluster";
    private final String CONTENT_REMOVING_INTERVAL = "cleanUpInterval";



    private Keyspace keyspace;
    private final static String KEYSPACE = "QpidKeySpace";
    private final static String LONG_TYPE = "LongType";
    private final static String UTF8_TYPE = "UTF8Type";
    private final static String INTEGER_TYPE = "IntegerType";
    private final static String QUEUE_COLUMN_FAMILY = "Queue";
    private final static String QUEUE_DETAILS_COLUMN_FAMILY = "QueueDetails";
    private final static String QUEUE_ENTRY_COLUMN_FAMILY = "QueueEntries";
    private final static String QUEUE_ENTRY_ROW = "QueueEntriesRow";
    private final static String EXCHANGE_COLUMN_FAMILY = "ExchangeColumnFamily";
    private final static String EXCHANGE_ROW = "ExchangesRow";
    private final static String BINDING_COLUMN_FAMILY = "Binding";
    private final static String MESSAGE_CONTENT_COLUMN_FAMILY = "MessageContent";
    private final static String MESSAGE_CONTENT_ID_COLUMN_FAMILY = "MessageContentIDs";
    private final static String MESSAGE_QUEUE_MAPPING_COLUMN_FAMILY =
            "MessageQueueMappingColumnFamily";
    private final static String MESSAGE_QUEUE_MAPPING_ROW =
            "MessageQueueMappingRow";
    private final static String SQ_COLUMN_FAMILY = "SubscriptionQueues";
    private final static String GLOBAL_QUEUE_TO_USER_QUEUE_COLUMN_FAMILY = "QpidQueues";
    private final static String USER_QUEUES_COLUMN_FAMILY = "UserQueues";
    private final static String GLOBAL_QUEUES_COLUMN_FAMILY =
            "GlobalQueue";
    private final static String GLOBAL_QUEUE_LIST_COLUMN_FAMILY = "GlobalQueueList";
    private final static String GLOBAL_QUEUE_LIST_ROW = "GlobalQueueListRow";

    private final static String QMD_COLUMN_FAMILY = "MetaData";
    private final static String QMD_ROW_NAME = "qpidMetaData";

    private final static String MSG_CONTENT_IDS_ROW = "messageContentIds";
    private final static String TOPIC_EXCHANGE_MESSAGE_IDS = "TopicExchangeMessageIds";
    private final static String PUB_SUB_MESSAGE_IDS = "pubSubMessages";
    private final static String TOPIC_SUBSCRIBERS = "topicSubscribers";
    private final static String TOPICS = "topics";
    private final static String ACKED_MESSAGE_IDS_COLUMN_FAMILY = "acknowledgedMessageIds";
    private final static String ACKED_MESSAGE_IDS_ROW = "acknowledgedMessageIdsRow";

    private final AtomicLong _messageId = new AtomicLong(0);

    private Queue<Long> contentDeletionTasks;
    private ConcurrentHashMap<Long,Long> pubSubMessageContentDeletionTasks;

    private ContentRemoverTask removerTask;
    private PubSubMessageContentRemoverTask pubSubContentRemoverTask;
    private boolean configured = false;


    private static StringSerializer stringSerializer = StringSerializer.get();
    private static LongSerializer longSerializer = LongSerializer.get();
    private static BytesArraySerializer bytesArraySerializer = BytesArraySerializer.get();
    private static IntegerSerializer integerSerializer = IntegerSerializer.get();
    private static ByteBufferSerializer byteBufferSerializer = ByteBufferSerializer.get();

    private static int mc =0;
    private static int smc = 0;
    private static Log log =
            org.apache.commons.logging.LogFactory.getLog(CassandraMessageStore.class);


    public CassandraMessageStore() {
        ClusterResourceHolder.getInstance().setCassandraMessageStore(this);
    }

    /**
     * Create a Cluster
     *
     * @param userName the name to be used to authenticate to Cassandra cluster
     * @param password the password to be used to authenticate to Cassandra cluster
     * @return <code>Cluster</code> instance
     */
    private Cluster createCluster(String userName, String password, String clusterName,
                                  String hostName, String port) {

        Map<String, String> credentials = new HashMap<String, String>();
        credentials.put(USERNAME_KEY, userName);
        credentials.put(PASSWORD_KEY, password);

        CassandraHostConfigurator hostConfigurator =
                new CassandraHostConfigurator(hostName + ":" + port);

        hostConfigurator.setMaxActive(2000);

        Cluster cluster = HFactory.getCluster(clusterName);

        if (cluster == null) {
            cluster = HFactory.createCluster(clusterName, hostConfigurator, credentials);
        }

        return cluster;
    }


    private void createColumnFamily(String name, String comparatorType, KeyspaceDefinition ksDef) {
        ColumnFamilyDefinition cfDef =
                new ThriftCfDef(KEYSPACE, /*"Queue"*/name,
                        ComparatorType.getByClassName(/*"LongType"*/comparatorType));
        if (ksDef == null) {
            cluster.addColumnFamily(cfDef);
        } else {
            List<ColumnFamilyDefinition> cfDefsList = ksDef.getCfDefs();
            HashSet cfNames = new HashSet();
            for (ColumnFamilyDefinition columnFamilyDefinition : cfDefsList) {
                cfNames.add(columnFamilyDefinition.getName());
            }
            if (!cfNames.contains(name)) {
                cluster.addColumnFamily(cfDef);
            }
        }

    }

    public void addMessage(IncomingMessage message)  {
        long messageId = message.getMessageNumber();
        StorableMessageMetaData metaData = message.headersReceived();

        for (BaseQueue destinationQueue : message.getDestinationQueues()) {
            try {

                final int bodySize = 1 + metaData.getStorableSize();
                byte[] underlying = new byte[bodySize];
                underlying[0] = (byte) metaData.getType().ordinal();
                ByteBuffer buf = ByteBuffer.wrap(underlying);
                buf.position(1);
                buf = buf.slice();

                metaData.writeToBuffer(0, buf);
                addMessageToGlobalQueue(destinationQueue.getResourceName(), messageId + "",
                        underlying);
            } catch (Exception e) {
               log.error("Error in adding incoming message",e);
                e.printStackTrace();
            }
        }
    }

    public List<QueueEntry> getMessagesFromUserQueue(AMQQueue queue,
                                                     int messageCount) {


        List<QueueEntry> messages = null;
        SimpleQueueEntryList list = new SimpleQueueEntryList(queue);
        ClusterManager clusterManager = ClusterResourceHolder.getInstance().getClusterManager();
        String key = queue.getResourceName()+"_" + clusterManager.getNodeId();
        try {
            messages = new ArrayList<QueueEntry>();

            LongSerializer ls = LongSerializer.get();
            BytesArraySerializer bs = BytesArraySerializer.get();
            SliceQuery<String, String, byte[]> sliceQuery =
                    HFactory.createSliceQuery(keyspace, stringSerializer, stringSerializer, bs);
            sliceQuery.setKey(key.trim());
            sliceQuery.setRange("", "", false, messageCount);
            sliceQuery.setColumnFamily(USER_QUEUES_COLUMN_FAMILY);


            QueryResult<ColumnSlice<String, byte[]>> result = sliceQuery.execute();
            ColumnSlice<String, byte[]> columnSlice = result.get();

            long maxId = 0;

            for (Object column : columnSlice.getColumns()) {
                if (column instanceof HColumn) {
                    String columnName = ((HColumn<String, byte[]>) column).getName();
                    byte[] value = ((HColumn<Long, byte[]>) column).getValue();


                    long messageId = Long.parseLong(columnName);

                    if (messageId > maxId) {
                        maxId = messageId;
                    }

                    byte[] dataAsBytes = value;
                    ByteBuffer buf = ByteBuffer.wrap(dataAsBytes);
                    buf.position(1);
                    buf = buf.slice();
                    MessageMetaDataType type = MessageMetaDataType.values()[dataAsBytes[0]];
                    StorableMessageMetaData metaData = type.getFactory().createMetaData(buf);
                    StoredCassandraMessage message = new StoredCassandraMessage(messageId, metaData);
                    AMQMessage amqMessage = new AMQMessage(message);
                    messages.add(list.add(amqMessage));
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            log.error(e);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e);
        }


        return messages;
    }

    /**
     * Get given number of messages from User Queue. If number of messages in the queue (qn) is less than the requested
     * Number of messages(rn) (qn <= rn) this will return all the messages in the given user queue
     * @param userQueue  User Queue name
     * @param globalQueue Global Queue name
     * @param messageCount max message count
     * @return  List of Messages
     */
    public List<CassandraQueueMessage> getMessagesFromUserQueue(String  userQueue,String globalQueue , int messageCount) {

        List<CassandraQueueMessage> messages = new ArrayList<CassandraQueueMessage>();

        ClusterManager clusterManager = ClusterResourceHolder.getInstance().getClusterManager();
        try {

            LongSerializer ls = LongSerializer.get();
            BytesArraySerializer bs = BytesArraySerializer.get();
            SliceQuery<String, String, byte[]> sliceQuery =
                    HFactory.createSliceQuery(keyspace, stringSerializer, stringSerializer, bs);
            sliceQuery.setKey(userQueue.trim());
            sliceQuery.setRange("", "", false, messageCount);
            sliceQuery.setColumnFamily(USER_QUEUES_COLUMN_FAMILY);


            QueryResult<ColumnSlice<String, byte[]>> result = sliceQuery.execute();
            ColumnSlice<String, byte[]> columnSlice = result.get();

            long maxId = 0;

            for (Object column : columnSlice.getColumns()) {
                if (column instanceof HColumn) {
                    String columnName = ((HColumn<String, byte[]>) column).getName();
                    byte[] value = ((HColumn<Long, byte[]>) column).getValue();


                    long messageId = Long.parseLong(columnName);

                    if (messageId > maxId) {
                        maxId = messageId;
                    }

                    byte[] dataAsBytes = value;
                    ByteBuffer buf = ByteBuffer.wrap(dataAsBytes);
                    buf.position(1);
                    buf = buf.slice();
                    CassandraQueueMessage cqm = new CassandraQueueMessage(""+messageId,globalQueue,dataAsBytes);
                    messages.add(cqm);
                }
            }
        } catch (NumberFormatException e) {
            log.error(e);
        } catch (Exception e) {
            log.error(e);
        }
        return  messages;
    }


    public int getMessageCountOfGlobalQueue(String queueName) {
        int messageCount =0;
        try {
            BytesArraySerializer bs = BytesArraySerializer.get();
            SliceQuery<String, String, byte[]> sliceQuery =
                    HFactory.createSliceQuery(keyspace, stringSerializer, stringSerializer, bs);
            sliceQuery.setKey(queueName.trim());
            sliceQuery.setRange("", "", false, Integer.MAX_VALUE);
            sliceQuery.setColumnFamily(GLOBAL_QUEUES_COLUMN_FAMILY);


            QueryResult<ColumnSlice<String, byte[]>> result = sliceQuery.execute();
            ColumnSlice<String, byte[]> columnSlice = result.get();

            messageCount = columnSlice.getColumns().size();
        } catch (NumberFormatException e) {
            log.error("Number format error in getting messages from global queue : " + queueName, e);
        } catch (Exception e) {
            log.error("Error in getting messages from global queue: " + queueName, e);
        }
        return messageCount;
    }

    /**
     * Get List of messages from a given Global queue
     *
     * @param queueName    Global queue Name
     * @param messageCount Number of messages that must be fetched.
     * @return List of Messages.
     */
    public Queue<CassandraQueueMessage> getMessagesFromGlobalQueue(String queueName,
                                                                   int messageCount) {
        Queue<CassandraQueueMessage> messages = null;

        try {
            messages = new LinkedList<CassandraQueueMessage>();


            BytesArraySerializer bs = BytesArraySerializer.get();
            SliceQuery<String, String, byte[]> sliceQuery =
                    HFactory.createSliceQuery(keyspace,stringSerializer,stringSerializer, bs);
            sliceQuery.setKey(queueName.trim());
            sliceQuery.setRange("", "", false, messageCount);
            sliceQuery.setColumnFamily(GLOBAL_QUEUES_COLUMN_FAMILY);


            QueryResult<ColumnSlice<String, byte[]>> result = sliceQuery.execute();
            ColumnSlice<String, byte[]> columnSlice = result.get();

            for (Object column : columnSlice.getColumns()) {
                if (column instanceof HColumn) {
                    String messageId = ((HColumn<String, byte[]>) column).getName();
                    byte[] value = ((HColumn<Long, byte[]>) column).getValue();
                    CassandraQueueMessage msg
                            = new CassandraQueueMessage(messageId, queueName, value);
                    messages.add(msg);
                }
            }
        } catch (NumberFormatException e) {
           log.error("Number format error in getting messages from global queue : "+ queueName,e );
        } catch (Exception e) {
            log.error("Error in getting messages from global queue: "+ queueName ,e);
        }


        return messages;
    }
    public List<QueueEntry> getMessagesFromGlobalQueue(long subscriptionID, AMQQueue queue,
                                                     AMQProtocolSession session, int messageCount) {
        List<QueueEntry> messages = null;
        SimpleQueueEntryList list = new SimpleQueueEntryList(queue);
        try {
            messages = new ArrayList<QueueEntry>();

            BytesArraySerializer bs = BytesArraySerializer.get();
            SliceQuery<String, String, byte[]> sliceQuery =
                    HFactory.createSliceQuery(keyspace,stringSerializer,stringSerializer, bs);
            sliceQuery.setKey(queue.getName().trim());
            sliceQuery.setRange("", "", false, messageCount);
            sliceQuery.setColumnFamily(GLOBAL_QUEUES_COLUMN_FAMILY);


            QueryResult<ColumnSlice<String, byte[]>> result = sliceQuery.execute();
            ColumnSlice<String, byte[]> columnSlice = result.get();
            long maxId = 0;

            for (Object column : columnSlice.getColumns()) {
                if (column instanceof HColumn) {
                    String columnName = ((HColumn<String, byte[]>) column).getName();
                    byte[] value = ((HColumn<Long, byte[]>) column).getValue();


                    long messageId = Long.parseLong(columnName);

                    if (messageId > maxId) {
                        maxId = messageId;
                    }

                    byte[] dataAsBytes = value;
                    ByteBuffer buf = ByteBuffer.wrap(dataAsBytes);
                    buf.position(1);
                    buf = buf.slice();
                    MessageMetaDataType type = MessageMetaDataType.values()[dataAsBytes[0]];
                    StorableMessageMetaData metaData = type.getFactory().createMetaData(buf);
                    StoredCassandraMessage message = new StoredCassandraMessage(messageId, metaData);
                    AMQMessage amqMessage = new AMQMessage(message);
                    amqMessage.setClientIdentifier(session);
                    messages.add(list.add(amqMessage));
                }
            }
        } catch (NumberFormatException e) {
            log.error(e);
        } catch (Exception e) {
            log.error(e);
        }


        return messages;
    }





    public void dequeueMessages(AMQQueue queue, List<QueueEntry> messagesToDelete) {

        try {

            Mutator<String> mutator = HFactory.createMutator(keyspace,stringSerializer);
            List<QueueEntry> messages = messagesToDelete;
            ClusterManager clusterManager = ClusterResourceHolder.getInstance().getClusterManager();
            String key = queue.getResourceName() +"_" + clusterManager.getNodeId();

            for (QueueEntry queueEntry : messages) {

                String messageID = queueEntry.getMessage().getMessageNumber()+"";

                removeMessageFromUserQueue(key, messageID);
            }
            mutator.execute();
        } catch (Exception e) {
            log.error("Error in dequeuing messages from "+ queue.getName(),e);
        }
    }


    /**
     * Remove a message from User Queue
     *
     * @param queueName User queue name
     * @param messageId message id
     */
    public void removeMessageFromUserQueue(String queueName, String messageId) {
        Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);
        mutator.addDeletion(queueName, USER_QUEUES_COLUMN_FAMILY, messageId, stringSerializer);
        mutator.execute();

    }


    /**
     * Remove a message from Global queue
     *
     * @param queueName
     * @param messageId
     */
    public void removeMessageFromGlobalQueue(String queueName, String messageId) {
        LongSerializer ls = LongSerializer.get();

        Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);
        mutator.addDeletion(queueName, GLOBAL_QUEUES_COLUMN_FAMILY, messageId,stringSerializer);
        mutator.execute();
    }


    public void recover(ConfigurationRecoveryHandler recoveryHandler) throws AMQException {

        try {
            ConfigurationRecoveryHandler.QueueRecoveryHandler qrh = recoveryHandler.begin(this);
            loadQueues(qrh);

            ConfigurationRecoveryHandler.ExchangeRecoveryHandler erh = qrh.completeQueueRecovery();
            List<String> exchanges = loadExchanges(erh);
            ConfigurationRecoveryHandler.BindingRecoveryHandler brh = erh.completeExchangeRecovery();
            recoverBindings(brh, exchanges);
            brh.completeBindingRecovery();
        } catch (Exception e) {

            throw new AMQStoreException("Error recovering persistent state: " + e.getMessage(), e);
        }


    }


    private Keyspace createKeyspace() {

        //Define the keyspace
        KeyspaceDefinition definition = new ThriftKsDef(KEYSPACE);

        KeyspaceDefinition def = cluster.describeKeyspace(KEYSPACE);
        if (def == null) {
            //Adding keyspace to the cluster
            cluster.addKeyspace(definition);
        }
        this.keyspace = HFactory.createKeyspace(KEYSPACE, cluster);
        CassandraConsistencyLevelPolicy policy = new CassandraConsistencyLevelPolicy();
        this.keyspace.setConsistencyLevelPolicy(policy);

        createColumnFamily(QUEUE_COLUMN_FAMILY, LONG_TYPE, def);
        createColumnFamily(BINDING_COLUMN_FAMILY, UTF8_TYPE, def);
        createColumnFamily(MESSAGE_CONTENT_COLUMN_FAMILY, INTEGER_TYPE, def);
        createColumnFamily(MESSAGE_CONTENT_ID_COLUMN_FAMILY, LONG_TYPE, def);
        createColumnFamily(SQ_COLUMN_FAMILY, UTF8_TYPE, def);
        createColumnFamily(GLOBAL_QUEUE_TO_USER_QUEUE_COLUMN_FAMILY, UTF8_TYPE, def);
        createColumnFamily(QMD_COLUMN_FAMILY, LONG_TYPE, def);
        createColumnFamily(QUEUE_DETAILS_COLUMN_FAMILY, UTF8_TYPE, def);
        createColumnFamily(QUEUE_ENTRY_COLUMN_FAMILY, UTF8_TYPE, def);
        createColumnFamily(EXCHANGE_COLUMN_FAMILY, UTF8_TYPE, def);
        createColumnFamily(USER_QUEUES_COLUMN_FAMILY, UTF8_TYPE, def);
        createColumnFamily(MESSAGE_QUEUE_MAPPING_COLUMN_FAMILY, UTF8_TYPE, def);
        createColumnFamily(GLOBAL_QUEUES_COLUMN_FAMILY, UTF8_TYPE, def);
        createColumnFamily(GLOBAL_QUEUE_LIST_COLUMN_FAMILY, UTF8_TYPE, def);
        createColumnFamily(TOPIC_EXCHANGE_MESSAGE_IDS,LONG_TYPE,def);
        createColumnFamily(PUB_SUB_MESSAGE_IDS,LONG_TYPE,def);
        createColumnFamily(TOPIC_SUBSCRIBERS,UTF8_TYPE,def);
        createColumnFamily(TOPICS,UTF8_TYPE,def);
        createColumnFamily(ACKED_MESSAGE_IDS_COLUMN_FAMILY,LONG_TYPE,def);

        //Create keyspace in client side
        keyspace = HFactory.createKeyspace("QpidKeySpace", cluster);

        return keyspace;
    }

    private int getUserQueueCount(String qpidQueueName) {
        int queueCount = 0;
        try {
            SliceQuery sliceQuery = HFactory.createSliceQuery(keyspace,stringSerializer,
                    stringSerializer, stringSerializer);
            sliceQuery.setKey(qpidQueueName);
            sliceQuery.setColumnFamily(GLOBAL_QUEUE_TO_USER_QUEUE_COLUMN_FAMILY);
            sliceQuery.setRange("", "", false, 1000);

            QueryResult<ColumnSlice<String, String>> result = sliceQuery.execute();
            ColumnSlice<String, String> columnSlice = result.get();
            queueCount = columnSlice.getColumns().size();
        } catch (Exception e) {
            log.error("Error in getting user queue count",e);
        }
        return queueCount;
    }

    public List<String> getUserQueues(String qpidQueueName) {
        ArrayList<String> userQueues = new ArrayList<String>();

        if (keyspace == null) {
            return userQueues;
        }

        try {
            SliceQuery sliceQuery = HFactory.createSliceQuery(keyspace,stringSerializer,
                    stringSerializer,stringSerializer);
            sliceQuery.setKey(qpidQueueName);
            sliceQuery.setColumnFamily(GLOBAL_QUEUE_TO_USER_QUEUE_COLUMN_FAMILY);
            sliceQuery.setRange("", "", false, 10000);

            QueryResult<ColumnSlice<String, String>> result = sliceQuery.execute();
            ColumnSlice<String, String> columnSlice = result.get();
            for (HColumn<String, String> column : columnSlice.getColumns()) {
                userQueues.add(column.getName());
            }
        } catch (Exception e) {
            log.error("Error in getting user queues for qpid queue :" + qpidQueueName,e);
        }
        return userQueues;
    }


    public List<String> getGlobalQueues() {

        ArrayList<String> globalQueues = new ArrayList<String>();
        if (keyspace == null) {
            return globalQueues;
        }
        try {

            SliceQuery sliceQuery = HFactory.createSliceQuery(keyspace, stringSerializer,
                    stringSerializer, stringSerializer);
            sliceQuery.setKey(GLOBAL_QUEUE_LIST_ROW);
            sliceQuery.setColumnFamily(GLOBAL_QUEUE_LIST_COLUMN_FAMILY);
            sliceQuery.setRange("", "", false, 10000);

            QueryResult<ColumnSlice<String, String>> result = sliceQuery.execute();
            ColumnSlice<String, String> columnSlice = result.get();
            for (HColumn<String, String> column : columnSlice.getColumns()) {
                globalQueues.add(column.getName());
            }
        } catch (Exception e) {
            log.error("Error in getting global queues" ,e);
        }

        return globalQueues;
    }


    /**
     * Add a Message to Internal User level Queue
     *
     * @param userQueue User Queue Name
     * @param messageId message id
     * @param message   message content.
     */
    public void addMessageToUserQueue(String userQueue, String messageId, byte[] message) {
        BytesArraySerializer bs = BytesArraySerializer.get();
        try {



            Mutator<String> mutator = HFactory.createMutator(keyspace,stringSerializer);
            mutator.addInsertion(userQueue.trim(), USER_QUEUES_COLUMN_FAMILY,
                    HFactory.createColumn(messageId, message,stringSerializer, bs));
            mutator.addInsertion(MESSAGE_QUEUE_MAPPING_ROW, MESSAGE_QUEUE_MAPPING_COLUMN_FAMILY,
                    HFactory.createColumn(messageId, userQueue.trim(),stringSerializer,stringSerializer));
            mutator.execute();

        } catch (Exception e) {
            log.error("Error in adding message :" + messageId +" to user queue :" + userQueue,e);
        }
    }

        /**
         * Add message to global queue
         *
         * @param queue
         * @param messageId
         * @param message
         */

    public void addMessageToGlobalQueue(String queue, String messageId, byte[] message) throws Exception {
        BytesArraySerializer bs = BytesArraySerializer.get();
        if (log.isDebugEnabled()) {
            log.debug("Adding Message with id " + messageId + " to Queue " + queue);
        }

//        mc++;

//       if(mc%400 ==0) {
//           System.out.println("In " + mc + " node id : " + ClusterResourceHolder.getInstance().getClusterManager().
//                   getNodeId());
//       }

        ClusterManager clusterManager = ClusterResourceHolder.getInstance().getClusterManager();

        try {
            Mutator<String>  mutator = HFactory.createMutator(keyspace,stringSerializer);

            mutator.addInsertion(queue.trim(), GLOBAL_QUEUES_COLUMN_FAMILY,
                    HFactory.createColumn(messageId, message,stringSerializer, bs));
            mutator.addInsertion(GLOBAL_QUEUE_LIST_ROW, GLOBAL_QUEUE_LIST_COLUMN_FAMILY,
                    HFactory.createColumn(queue, queue,stringSerializer,stringSerializer));
            mutator.execute();
            clusterManager.handleQueueAddition(queue);

        } catch (Exception e) {
           log.error("Error in adding message to global queue", e);
           throw new Exception("Error in adding message to global queue", e);
        }
    }


    public void addMessageContent(String messageId, int offset, ByteBuffer src) {

        try {
            String rowKey = "mid" + messageId;
            src = src.slice();
            byte[] chunkData = new byte[src.limit()];

            src.duplicate().get(chunkData);


            Mutator<String> messageContentMutator = HFactory.createMutator(keyspace,
                    stringSerializer);

            messageContentMutator.addInsertion(
                    rowKey.trim(),
                    MESSAGE_CONTENT_COLUMN_FAMILY,
                    HFactory.createColumn(offset, chunkData, integerSerializer, bytesArraySerializer));
            messageContentMutator.execute();
        } catch (Exception e) {
            log.error("Error in adding message content" ,e);
        }
    }

    public void removeMessageContent(String messageId, boolean commit) {
        try {
            String rowKey = "mid" + messageId;

            Mutator<String> messageContentMutator = HFactory.createMutator(keyspace,
                    stringSerializer);

            messageContentMutator.addDeletion(rowKey.trim(), MESSAGE_CONTENT_COLUMN_FAMILY, null, integerSerializer);
            if (commit) {
                messageContentMutator.execute();
            }
        } catch (Exception e) {
            log.error("Error in removing message content", e);
        }
    }



    private int getContent(String messageId, int offsetValue, ByteBuffer dst) {

        int written = 0;
        int chunkSize = 65534;
        try {

            String rowKey = "mid" + messageId;
            if (offsetValue == 0) {

                ColumnQuery columnQuery = HFactory.createColumnQuery(keyspace, stringSerializer,
                        integerSerializer, byteBufferSerializer);
                columnQuery.setColumnFamily(MESSAGE_CONTENT_COLUMN_FAMILY);
                columnQuery.setKey(rowKey.trim());
                columnQuery.setName(offsetValue);

                QueryResult<HColumn<Integer, ByteBuffer>> result = columnQuery.execute();
                HColumn<Integer, ByteBuffer> column = result.get();
                if (column != null) {
                    int offset = column.getName();
                    byte[] content = bytesArraySerializer.fromByteBuffer(column.getValue());

                    final int size = (int) content.length;
                    int posInArray = offset + written - offset;
                    int count = size - posInArray;
                    if (count > dst.remaining()) {
                        count = dst.remaining();
                    }
                    dst.put(content, posInArray, count);
                    written += count;
                }
            } else {
                ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
                int k = offsetValue / chunkSize;
                SliceQuery query = HFactory.createSliceQuery(keyspace, stringSerializer,
                        integerSerializer, byteBufferSerializer);
                query.setColumnFamily(MESSAGE_CONTENT_COLUMN_FAMILY);
                query.setKey(rowKey.trim());
                query.setRange(k * chunkSize, (k + 1) * chunkSize + 1, false, 10);

                QueryResult<ColumnSlice<Integer, ByteBuffer>> result = query.execute();
                ColumnSlice<Integer, ByteBuffer> columnSlice = result.get();
                for (HColumn<Integer, ByteBuffer> column : columnSlice.getColumns()) {
                    byteOutputStream.write(bytesArraySerializer.fromByteBuffer(column.getValue()));
                }
                byte[] content = byteOutputStream.toByteArray();
                final int size = (int) content.length;
                int posInArray = offsetValue + written - (k * chunkSize);
                int count = size - posInArray;
                if (count > dst.remaining()) {
                    count = dst.remaining();
                }
                dst.put(content, posInArray, count);
                written += count;
            }

        } catch (Exception e) {
            log.error("Error in reading content",e);
        }

        return written;
    }

    public void storeMetaData(long messageId, StorableMessageMetaData metaData) {
        try {
            final int bodySize = 1 + metaData.getStorableSize();
            byte[] underlying = new byte[bodySize];
            underlying[0] = (byte) metaData.getType().ordinal();
            java.nio.ByteBuffer buf = java.nio.ByteBuffer.wrap(underlying);
            buf.position(1);
            buf = buf.slice();
            metaData.writeToBuffer(0, buf);

            Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);


            mutator.addInsertion(QMD_ROW_NAME, QMD_COLUMN_FAMILY, HFactory.createColumn(messageId,
                    underlying, longSerializer, bytesArraySerializer));
            mutator.execute();
        } catch (Exception e) {
            log.error("Error in storing meta data" ,e);
        }
    }

    private StorableMessageMetaData getMetaData(long messageId) {


        StorableMessageMetaData metaData = null;
        try {
            LongSerializer ls = LongSerializer.get();
            BytesArraySerializer bs = BytesArraySerializer.get();

            ColumnQuery columnQuery = HFactory.createColumnQuery(keyspace, stringSerializer, ls, bs);
            columnQuery.setColumnFamily(QMD_COLUMN_FAMILY);
            columnQuery.setKey(QMD_ROW_NAME);
            columnQuery.setName(messageId);

            QueryResult<HColumn<Long, byte[]>> result = columnQuery.execute();

            HColumn<Long, byte[]> column = result.get();
            byte[] dataAsBytes = column.getValue();


            ByteBuffer buf = ByteBuffer.wrap(dataAsBytes);
            buf.position(1);
            buf = buf.slice();
            MessageMetaDataType type = MessageMetaDataType.values()[dataAsBytes[0]];
            metaData = type.getFactory().createMetaData(buf);
        } catch (Exception e) {
            log.error("Error in getting meta data of provided message id",e);
        }
        return metaData;
    }

    private void removeMetaData(long messageId) {
        try {
            Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);
            mutator.addDeletion(QMD_ROW_NAME, QMD_COLUMN_FAMILY, messageId, longSerializer);
            if (log.isDebugEnabled()) {
                log.debug(" removing metadata of message id = " + messageId);
            }
            mutator.execute();
        } catch (Exception e) {
            log.error("Error in removing metadata", e);
        }
    }

    /**
     * Acknowledged messages are added to this column family with the current system
     * time as the acknowledged time  */
    public void addAckedMessage(long messageId) {
        try {
            pubSubMessageContentDeletionTasks.put(messageId, messageId);
            Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);
            long ackTime = System.currentTimeMillis();

            mutator.addInsertion(ACKED_MESSAGE_IDS_ROW, ACKED_MESSAGE_IDS_COLUMN_FAMILY, HFactory.createColumn(messageId,
                    ackTime, longSerializer, longSerializer));
            mutator.execute();
        } catch (Exception e) {
            log.error("Error in storing meta data", e);
        }
    }
    /**
     * When message contents are ready to remove , removing the reference to that from the acknowledged message
     * column family
     * */
    private void removeAckedMessage(long messageId) {
        try {
            Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);
            mutator.addDeletion(ACKED_MESSAGE_IDS_ROW, ACKED_MESSAGE_IDS_COLUMN_FAMILY, messageId, longSerializer);
            mutator.execute();
        } catch (Exception e) {
            log.error("Error in storing meta data", e);
        }
    }
    /**
     * Checking whether the message is ready to remove and remove the message if conditions satisfied
     * */
    public boolean isReadyAndRemovedMessageContent(long messageId) {
        long currentSystemTime = System.currentTimeMillis();
        try {
            ColumnQuery<String, Long, Long> columnQuery =
                    HFactory.createColumnQuery(keyspace, stringSerializer, longSerializer, longSerializer);
            columnQuery.setKey(ACKED_MESSAGE_IDS_ROW);
            columnQuery.setColumnFamily(ACKED_MESSAGE_IDS_COLUMN_FAMILY);
            columnQuery.setName(messageId);

            QueryResult<HColumn<Long, Long>> result = columnQuery.execute();

            if (result != null) {
                HColumn<Long, Long> column = result.get();
                //Checking whether the message is ready to remove

                ClusterConfiguration clusterConfiguration = ClusterResourceHolder.getInstance().
                        getClusterConfiguration();
                if ((currentSystemTime - column.getValue()) >= clusterConfiguration.getContentRemovalTimeDifference()) {
                    removeMetaData(messageId);
                    removeAckedMessage(messageId);
                    return true;
                }else{
                    return false;
                }
            }else{
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addBinding(Exchange exchange, AMQQueue amqQueue, String routingKey) {
        if (keyspace == null) {
            return;
        }
        try {

            Mutator<String>  mutator = HFactory.createMutator(keyspace,stringSerializer);

            String columnName = routingKey;
            String columnValue = amqQueue.getName();

            mutator.insert(exchange.getName(), BINDING_COLUMN_FAMILY,
                    HFactory.createColumn(columnName, columnValue, stringSerializer, stringSerializer));
            mutator.execute();
        } catch (Exception e) {
            log.error("Error in adding binding ",e);
        }

    }
    /**
     * When a new message arrived for a topic
     * it searches for the registered subscribers for that topic
     * once it got the list of registered subscribers for that topic
     * it adds the received message for all of those subscription queues
     * @param topic - Topic
     * @param messageId - Id of the new message
     * */
    public void addTopicExchangeMessageIds(String topic, long messageId) {
        List<String> registeredSubscribers = getRegisteredSubscribersForTopic(topic);
        for (String subscriber : registeredSubscribers) {
            addMessageIdToSubscriberQueue(subscriber, messageId);
        }
    }
    /**
     * Getting messages from the provided queue
     *
     * This method retrives message from the queue. It search for the message ids
     * from the provided id to above
     *
     * @param  queue - AMQQueue
     * @param  lastDeliveredMid - Id of the last delivered message
     * @return List of messages to be delivered
     * */
    public List<MessageTransferMessage> getSubscriberMessages(AMQQueue queue, long lastDeliveredMid) {
        List<MessageTransferMessage> messages = null;
        List<Long> messageIds = getPendingMessageIds(queue.getName(), lastDeliveredMid);
        if (messageIds.size() > 0) {
            messages = new ArrayList<MessageTransferMessage>();
            for (long messageId : messageIds) {
                StorableMessageMetaData messageMetaData = getMetaData(messageId);
                StoredCassandraMessage storedCassandraMessage = new StoredCassandraMessage(messageId, messageMetaData, true);
                MessageTransferMessage message = new MessageTransferMessage(storedCassandraMessage, null);
                messages.add(message);
            }
        }
        return messages;
    }
    /**
     * Registers topic
     * Add an entry to the Topics column family to indicate that there is a subscriber for this topic
     * @param topic - Topic name
     * */
    private void registerTopic(String topic) {
        try {
            Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);
            String columnName = topic;
            String columnValue = topic;

            mutator.insert("TOPICS", TOPICS,
                    HFactory.createColumn(columnName, columnValue, stringSerializer, stringSerializer));
            mutator.execute();
        } catch (Exception e) {
            log.error("Error in registering queue for the topic", e);
        }
    }

    /**
     * Getting all the topics where subscribers exists
     * */
    public List<String> getTopics() {
        List<String> topicList = new ArrayList<String>();
        try {
            SliceQuery<String, String, String> sliceQuery
                    = HFactory.createSliceQuery(keyspace, stringSerializer, stringSerializer, stringSerializer);
            sliceQuery.setKey("TOPICS");
            sliceQuery.setColumnFamily(TOPICS);
            sliceQuery.setRange("", "", false, 1000);

            QueryResult<ColumnSlice<String, String>> result = sliceQuery.execute();
            ColumnSlice<String, String> columnSlice = result.get();
            for (HColumn<String, String> column : columnSlice.getColumns()) {
                topicList.add(column.getValue());
            }

        } catch (Exception e) {
            log.error("Error in getting registered subscribers for the topic", e);
        }
        return topicList;
    }

    /**
     * Remove the topic from the topics column family when there are no subscribers for that topic
     * @param topic
     * */
    private void unRegisterTopic(String topic){
          try {
            Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);
            mutator.addDeletion("TOPICS", TOPICS, topic, stringSerializer);
            if(log.isDebugEnabled()){
                log.debug(" removing topic = " + topic  );
            }
            mutator.execute();
        } catch (Exception e) {
           log.error("Error in unregistering topic" ,e);
        }
    }

    /**
     * Registers subscriber for topic
     * Simply adding the queue name as a subscriber for the provided topic
     * @param topic - Topic to be subscribed
     * @param queueName - Name of the queue
     * */
    public void registerSubscriberForTopic(String topic, String queueName) {
        if (keyspace == null) {
            return;
        }
        try {
            registerTopic(topic);
            Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);
            String columnName = queueName;
            String columnValue = queueName;

            mutator.insert(topic, TOPIC_SUBSCRIBERS,
                    HFactory.createColumn(columnName, columnValue, stringSerializer, stringSerializer));
            mutator.execute();
        } catch (Exception e) {
            log.error("Error in registering queue for the topic",e);
        }
    }

    /**
     * Retriving the names of the subscriptions (Queue Names) which are subscribed for the
     * provided topic
     * @param topic - Name of the topic
     * @return List of names
     *
     * */
    public List<String> getRegisteredSubscribersForTopic(String topic) {
        List<String> queueList = new ArrayList<String>();
        try {
            SliceQuery<String, String, String> sliceQuery
                    = HFactory.createSliceQuery(keyspace, stringSerializer, stringSerializer, stringSerializer);
            sliceQuery.setKey(topic);
            sliceQuery.setColumnFamily(TOPIC_SUBSCRIBERS);
            sliceQuery.setRange("", "", false, 1000);

            QueryResult<ColumnSlice<String, String>> result = sliceQuery.execute();
            ColumnSlice<String, String> columnSlice = result.get();
            for (HColumn<String, String> column : columnSlice.getColumns()) {
                queueList.add(column.getValue());
            }

        } catch (Exception e) {
            log.error("Error in getting registered subscribers for the topic" ,e);
        }
        return queueList;
    }
    /**
     * Removing the subscription entry from the subscribers list for the topic
     * @param topic - Name of the topic
     * @param queueName - Queue name to be removed
     * */
    public void unRegisterQueueFromTopic(String topic, String queueName) {
        try {
            Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);
            mutator.addDeletion(topic, TOPIC_SUBSCRIBERS, queueName, stringSerializer);
            if(log.isDebugEnabled()){
                log.debug(" removing queue = " + queueName + " from topic =" + topic);
            }
            mutator.execute();
        } catch (Exception e) {
           log.error("Error in unregistering queue from the topic" ,e);
        }
        if( getRegisteredSubscribersForTopic(topic).size()==0){
            unRegisterTopic(topic);
        }
    }
    /**
     * Adding message id to the subscriber queue
     * @param queueName - Name of the queue
     * @param messageId - Message ID
     * */
    private void addMessageIdToSubscriberQueue(String queueName, long messageId){
        if (keyspace == null) {
            return;
        }
        try {
            Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);
            long columnName = messageId;
            long columnValue = messageId;

            mutator.insert(queueName, PUB_SUB_MESSAGE_IDS,
                    HFactory.createColumn(columnName, columnValue, longSerializer, longSerializer));
            mutator.execute();
        } catch (Exception e) {
           log.error("Error in adding message Id to subscriber queue" ,e);
        }
    }

    /**
     * Search and return message ids of the provided queue begining from the
     * provided message id to above
     * @param queueName - Name of the queue
     * @param lastDeliveredMid - Last delivered message Id
     * @return list of message IDs
     * */
    private List<Long> getPendingMessageIds(String queueName, long lastDeliveredMid) {
            List<Long> queueList = new ArrayList<Long>();
            try {
                SliceQuery<String, Long, Long> sliceQuery =
                        HFactory.createSliceQuery(keyspace, stringSerializer, longSerializer, longSerializer);
                sliceQuery.setKey(queueName);
                sliceQuery.setColumnFamily(PUB_SUB_MESSAGE_IDS);
                sliceQuery.setRange(lastDeliveredMid,Long.MAX_VALUE, false, 1000);

                QueryResult<ColumnSlice<Long, Long>> result = sliceQuery.execute();
                ColumnSlice<Long, Long> columnSlice = result.get();
                for (HColumn<Long, Long> column : columnSlice.getColumns()) {
                    queueList.add(column.getValue());
                }

            } catch (Exception e) {
                log.error("Error in retriving message ids of the queue",e);
            }
            return queueList;
        }

    /**
     * Remove delivered messages from the provided queue
     * @param messageIdsToBeRemoved - List of delivered message ids to be removed
     * @param queueName - name of the queue
     * */
    public void removeDeliveredMessageIds(List<Long> messageIdsToBeRemoved, String queueName) {
        try {
            Mutator<String> mutator =   HFactory.createMutator(keyspace, stringSerializer);
            for (Long mid : messageIdsToBeRemoved) {
                mutator.addDeletion(queueName, PUB_SUB_MESSAGE_IDS, mid, longSerializer);
                if(log.isDebugEnabled()){
                    log.debug(" removing mid = " + mid + " from ="  + queueName);
                }
            }
            mutator.execute();
        } catch (Exception e) {
            log.error("Error in removing message ids from subscriber queue" ,e);
        }
    }


    public void synchBindings(VirtualHostConfigSynchronizer vhcs) {
        try {


            Mutator<String> mutator =
                    HFactory.createMutator(keyspace,stringSerializer);


            RangeSlicesQuery<String, String, String> rangeSliceQuery =
                    HFactory.createRangeSlicesQuery(keyspace, stringSerializer, stringSerializer,
                            stringSerializer );
            rangeSliceQuery.setKeys("", "");
            rangeSliceQuery.setColumnFamily(BINDING_COLUMN_FAMILY);
            rangeSliceQuery.setRange("", "", false, 100);

            QueryResult<OrderedRows<String, String, String>> result = rangeSliceQuery.execute();
            OrderedRows<String, String, String> orderedRows = result.get();
            List<Row<String, String, String>> rowArrayList = orderedRows.getList();
            for (Row<String, String, String> row : rowArrayList) {
                String exchange = row.getKey();
                ColumnSlice<String, String> columnSlice = row.getColumnSlice();
                for (Object column : columnSlice.getColumns()) {
                    if (column instanceof HColumn) {
                        String columnName = ((HColumn<String, String>) column).getName();
                        String value = ((HColumn<String, String>) column).getValue();
                        vhcs.binding(exchange, value, columnName, null);
                    }
                }
            }
        } catch (NumberFormatException e) {
            log.error("Error in synchronizing bindings" ,e);
        }

    }

    public void recoverBindings(ConfigurationRecoveryHandler.BindingRecoveryHandler brh,
                                List<String> exchanges)
            throws Exception {

        try {


            Mutator<String>  mutator = HFactory.createMutator(keyspace,stringSerializer);

            RangeSlicesQuery<String, String, String> rangeSliceQuery =
                    HFactory.createRangeSlicesQuery(keyspace, stringSerializer, stringSerializer,
                            stringSerializer);
            rangeSliceQuery.setKeys("", "");
            rangeSliceQuery.setColumnFamily(BINDING_COLUMN_FAMILY);
            rangeSliceQuery.setRange("", "", false, 100);

            QueryResult<OrderedRows<String, String, String>> result = rangeSliceQuery.execute();
            OrderedRows<String, String, String> orderedRows = result.get();
            List<Row<String, String, String>> rowArrayList = orderedRows.getList();
            for (Row<String, String, String> row : rowArrayList) {
                String exchange = row.getKey();
                ColumnSlice<String, String> columnSlice = row.getColumnSlice();
                for (Object column : columnSlice.getColumns()) {
                    if (column instanceof HColumn) {
                        String columnName = ((HColumn<String, String>) column).getName();
                        String value = ((HColumn<String, String>) column).getValue();
                        brh.binding(exchange, value, columnName, null);


                    }
                }
            }
        } catch (NumberFormatException e) {
            log.error("Number formatting error occured when recovering bindings" ,e);
        }


    }

    private List<String> getBindings(String routingKey) {
        List<String> bindings = new ArrayList<String>();
        try {

            Mutator<String>  mutator = HFactory.createMutator(keyspace,stringSerializer);



            // Retrieving multiple rows with Range Slice Query
            RangeSlicesQuery<String, String, String> rangeSlicesQuery =
                    HFactory.createRangeSlicesQuery(keyspace, stringSerializer, stringSerializer,
                            stringSerializer);
            rangeSlicesQuery.setKeys("DirectExchange", "DirectExchange");
            rangeSlicesQuery.setColumnFamily(BINDING_COLUMN_FAMILY);
            rangeSlicesQuery.setRange(routingKey, "", false, 10);


            QueryResult<OrderedRows<String, String, String>> result = rangeSlicesQuery.execute();
            OrderedRows<String, String, String> columnSlice = result.get();
            List<Row<String, String, String>> rows = columnSlice.getList();


            for (Object column : columnSlice.getList().get(0).getColumnSlice().getColumns()) {
                if (column instanceof HColumn) {
                    String columnName = ((HColumn<String, String>) column).getName();
                    String value = ((HColumn<String, String>) column).getValue();
                    String stringValue = new String(value);
                    bindings.add(stringValue);

                }
            }
        } catch (Exception e) {
            log.error("Error in getting bindings" ,e);
        }
        return bindings;
    }

    private void recoverMessages(MessageStoreRecoveryHandler recoveryHandler) {

        StorableMessageMetaData metaData = null;
        long maxId = 0;
        try {
            LongSerializer ls = LongSerializer.get();
            BytesArraySerializer bs = BytesArraySerializer.get();

            SliceQuery sliceQuery = HFactory.createSliceQuery(keyspace,stringSerializer, ls, bs);
            sliceQuery.setColumnFamily(QMD_COLUMN_FAMILY);
            sliceQuery.setKey(QMD_ROW_NAME);
            sliceQuery.setRange(Long.parseLong("0"), Long.parseLong("999999"), false, 10000);

            QueryResult<ColumnSlice<Long, byte[]>> result = sliceQuery.execute();

            ColumnSlice<Long, byte[]> columnSlice = result.get();

            List<HColumn<Long, byte[]>> columnList = columnSlice.getColumns();

            for (HColumn<Long, byte[]> column : columnList) {

                long key = column.getName();
                if (key > maxId) {
                    maxId = key;
                }
                byte[] dataAsBytes = column.getValue();

                ByteBuffer buf = ByteBuffer.wrap(dataAsBytes);
                buf.position(1);
                buf = buf.slice();
                MessageMetaDataType type = MessageMetaDataType.values()[dataAsBytes[0]];
                metaData = type.getFactory().createMetaData(buf);
            }
            _messageId.set(maxId);
        } catch (Exception e) {
            log.error("Error in recovering bindings",e);
        }
    }


    public void createQueue(AMQQueue queue) {
        try {
            String owner = queue.getOwner() == null ? null : queue.getOwner().toString();
            addQueueDetails(queue.getNameShortString().toString(), owner, queue.isExclusive());

        } catch (Exception e) {
            log.error("Error in creating queue" ,e);
        }
    }

    private void addQueueDetails(String queueName, String owner, boolean isExclusive) {
        try {

            Mutator<String> mutator = HFactory.createMutator(keyspace,stringSerializer);

            String value = queueName + "|" + owner + "|" + (isExclusive ? "true" : "false");

            mutator.addInsertion("QUEUE_DETAILS", QUEUE_DETAILS_COLUMN_FAMILY,
                    HFactory.createColumn(queueName, value, stringSerializer, stringSerializer));
            mutator.execute();
        } catch (Exception e) {
            log.error("Error in adding queue details");
        }
    }

    public void synchQueues(VirtualHostConfigSynchronizer vhcs) throws Exception {


        try {


            Mutator<String> mutator = HFactory.createMutator(keyspace,stringSerializer);



            // Retriving multiple rows with Range Slice Query
            SliceQuery<String, String, String> sliceQuery =
                    HFactory.createSliceQuery(keyspace, stringSerializer, stringSerializer,
                            stringSerializer);
            sliceQuery.setKey("QUEUE_DETAILS");
            sliceQuery.setRange("", "", false, 100);
            sliceQuery.setColumnFamily(QUEUE_DETAILS_COLUMN_FAMILY);

            QueryResult<ColumnSlice<String, String>> result = sliceQuery.execute();
            ColumnSlice<String, String> columnSlice = result.get();

            long maxId = 0;

            for (Object column : columnSlice.getColumns()) {
                if (column instanceof HColumn) {
                    String columnName = ((HColumn<String, String>) column).getName();
                    String value = ((HColumn<String, String>) column).getValue();
                    String stringValue = new String(value);
                    String[] valuesFields = value.split("|");
                    String owner = valuesFields[1];
                    boolean isExclusive = Boolean.parseBoolean(valuesFields[2]);

                    vhcs.queue(columnName, owner, isExclusive, null);


                }
            }
        } catch (NumberFormatException e) {
            log.error("Error in queue synchronization" ,e);
        }


    }


    public void loadQueues(ConfigurationRecoveryHandler.QueueRecoveryHandler qrh) throws Exception {


        try {


            Mutator<String>    mutator = HFactory.createMutator(keyspace,stringSerializer);



            // Retriving multiple rows with Range Slice Query
            SliceQuery<String, String, String> sliceQuery = HFactory.createSliceQuery(keyspace,
                    stringSerializer,stringSerializer,stringSerializer);
            sliceQuery.setKey("QUEUE_DETAILS");
            sliceQuery.setRange("", "", false, 100);
            sliceQuery.setColumnFamily(QUEUE_DETAILS_COLUMN_FAMILY);

            QueryResult<ColumnSlice<String, String>> result = sliceQuery.execute();
            ColumnSlice<String, String> columnSlice = result.get();

            long maxId = 0;

            for (Object column : columnSlice.getColumns()) {
                if (column instanceof HColumn) {
                    String columnName = ((HColumn<String, String>) column).getName();
                    String value = ((HColumn<String, String>) column).getValue();
                    String stringValue = new String(value);


                    String[] valuesFields = value.split("|");
                    String owner = valuesFields[1];
                    boolean isExclusive = Boolean.parseBoolean(valuesFields[2]);

                    qrh.queue(columnName, owner, isExclusive, null);


                }
            }
        } catch (NumberFormatException e) {
           log.error("Error in loading queues",e);
        }


    }



    /**
     * Add Global Queue to User Queue Mapping
     *
     * @param globalQueueName
     */
    public void addUserQueueToGlobalQueue(String globalQueueName) {

        try {
            ClusterManager clusterManager = ClusterResourceHolder.getInstance().getClusterManager();
            String userQueueName = globalQueueName + "_" + clusterManager.getNodeId();
            Mutator<String> qqMutator = HFactory.createMutator(keyspace, stringSerializer);
            qqMutator.addInsertion(globalQueueName, GLOBAL_QUEUE_TO_USER_QUEUE_COLUMN_FAMILY,
                    HFactory.createColumn(userQueueName, userQueueName, stringSerializer,
                            stringSerializer));
            qqMutator.addInsertion(GLOBAL_QUEUE_LIST_ROW, GLOBAL_QUEUE_LIST_COLUMN_FAMILY,
                    HFactory.createColumn(globalQueueName, globalQueueName, stringSerializer,
                            stringSerializer));

            qqMutator.execute();
        } catch (Exception e) {
           log.error("Error in adding user queue to global queue",e);
        }
    }

    public void removeUserQueueFromQpidQueue(String globalQueueName) {
        try {
            ClusterManager clusterManager = ClusterResourceHolder.getInstance().getClusterManager();
            String userQueueName = globalQueueName + "_" +
                    clusterManager.getNodeId();

            Mutator<String> qqMutator = HFactory.createMutator(keyspace, stringSerializer);
            qqMutator.addDeletion(
                    globalQueueName.trim(),
                    GLOBAL_QUEUE_TO_USER_QUEUE_COLUMN_FAMILY,
                    userQueueName,
                    stringSerializer);
            qqMutator.execute();
        } catch (Exception e) {
            log.error("Error in removing user queue from qpid queue",e);
        }
    }

    @Override
    public void configureMessageStore(String name, MessageStoreRecoveryHandler recoveryHandler,
                                      Configuration config, LogSubject logSubject) throws Exception {
        if (!configured) {
            performCommonConfiguration(config);
            ClusterResourceHolder resourceHolder = ClusterResourceHolder.getInstance();

            CassandraTopicPublisherManager cassandraTopicPublisherManager =
                    resourceHolder.getCassandraTopicPublisherManager();
            if(cassandraTopicPublisherManager == null) {
                cassandraTopicPublisherManager = new CassandraTopicPublisherManager();
                resourceHolder.setCassandraTopicPublisherManager(cassandraTopicPublisherManager);
            }
            cassandraTopicPublisherManager.init();
            cassandraTopicPublisherManager.start();

        }

        recoverMessages(recoveryHandler);
    }


    private void performCommonConfiguration(Configuration configuration) throws Exception {
        String userName = (String) configuration.getProperty(USERNAME_KEY);
        String password = (String) configuration.getProperty(PASSWORD_KEY);
        String hostName = (String) configuration.getProperty(HOST_KEY);
        String port = (String) configuration.getProperty(PORT_KEY);
        String clusterName = (String) configuration.getProperty(CLUSTER_KEY);


        cluster = createCluster(userName, password, clusterName, hostName, port);


        contentDeletionTasks = new LinkedList<Long>();


        removerTask = new ContentRemoverTask(ClusterResourceHolder.getInstance().getClusterConfiguration().
                getContentRemovalTaskInterval());
        removerTask.setRunning(true);
        Thread t = new Thread(removerTask);
        t.setName(removerTask.getClass().getSimpleName()+"-Thread");
        t.start();

        pubSubMessageContentDeletionTasks = new ConcurrentHashMap<Long,Long>();

        ClusterConfiguration clusterConfiguration = ClusterResourceHolder.getInstance().getClusterConfiguration();
        pubSubContentRemoverTask = new PubSubMessageContentRemoverTask(clusterConfiguration.
                getPubSubMessageRemovalTaskInterval());
        pubSubContentRemoverTask.setRunning(true);
        Thread th = new Thread(pubSubContentRemoverTask);
        th.start();

        keyspace = createKeyspace();

        AndesConsistantLevelPolicy consistencyLevel = new AndesConsistantLevelPolicy();

        keyspace.setConsistencyLevelPolicy(consistencyLevel);

        configured = true;

    }

    @Override
    public void close() throws Exception {
        throw new UnsupportedOperationException("Close function is not supported");
    }

    @Override
    public <T extends StorableMessageMetaData> StoredMessage<T> addMessage(T metaData) {
         long timeStamp = 0;
        if(metaData instanceof MessageMetaData){
           timeStamp = ((MessageMetaData) metaData).getArrivalTime();
        }else {
             timeStamp = ((MessageMetaData_0_10) metaData).getArrivalTime();
        }

        StringBuffer midStr = new StringBuffer();
        timeStamp = ClusterResourceHolder.getInstance().getReferenceTime().getTime(timeStamp);
        midStr.append(timeStamp).append(_messageId.incrementAndGet()).
                append(ClusterResourceHolder.getInstance().getClusterManager().getNodeId());
        long mid = Long.parseLong(midStr.toString());
        return new StoredCassandraMessage(mid, metaData);

    }


    @Override
    public boolean isPersistent() {
        return false;
    }

    @Override
    public void configureConfigStore(String name, ConfigurationRecoveryHandler recoveryHandler,

                                     Configuration config, LogSubject logSubject) throws Exception {
        if (!configured) {
            performCommonConfiguration(config);
            recover(recoveryHandler);

            ClusterResourceHolder resourceHolder = ClusterResourceHolder.getInstance();
            CassandraTopicPublisherManager cassandraTopicPublisherManager =
                    resourceHolder.getCassandraTopicPublisherManager();
            if(cassandraTopicPublisherManager == null) {
                cassandraTopicPublisherManager = new CassandraTopicPublisherManager();
                resourceHolder.setCassandraTopicPublisherManager(cassandraTopicPublisherManager);
            }
            cassandraTopicPublisherManager.init();
            cassandraTopicPublisherManager.start();
        }

    }



    @Override
    public void createExchange(Exchange exchange) throws AMQStoreException {
        try {

            Mutator<String> mutator = HFactory.createMutator(keyspace,stringSerializer);

            String name = exchange.getName();
            String type = exchange.getTypeShortString().asString();
            Short autoDelete = exchange.isAutoDelete() ? (short) 1 : (short) 0;

            String value = name + "|" + type + "|" + autoDelete;

            mutator.addInsertion(EXCHANGE_ROW, EXCHANGE_COLUMN_FAMILY,
                    HFactory.createColumn(name, value, stringSerializer, stringSerializer));
            mutator.execute();
        } catch (Exception e) {
            log.error("Error in creating exchange "+ exchange.getName() ,e);
        }
    }


    public List<String> loadExchanges(ConfigurationRecoveryHandler.ExchangeRecoveryHandler erh)
            throws Exception {

        List<String> exchangeNames = new ArrayList<String>();
        try {


            Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);


            SliceQuery<String, String, String> sliceQuery =
                    HFactory.createSliceQuery(keyspace, stringSerializer  , stringSerializer,
                            stringSerializer);
            sliceQuery.setKey(EXCHANGE_ROW);
            sliceQuery.setRange("", "", false, 100);
            sliceQuery.setColumnFamily(EXCHANGE_COLUMN_FAMILY);

            QueryResult<ColumnSlice<String, String>> result = sliceQuery.execute();
            ColumnSlice<String, String> columnSlice = result.get();

            long maxId = 0;

            for (Object column : columnSlice.getColumns()) {
                if (column instanceof HColumn) {
                    String columnName = ((HColumn<String, String>) column).getName();
                    String value = ((HColumn<String, String>) column).getValue();
                    String stringValue = new String(value);


                    String[] valuesFields = value.split("|");
                    String type = valuesFields[1];
                    short autoDelete = Short.parseShort(valuesFields[2]);
                    exchangeNames.add(columnName);
                    erh.exchange(columnName, type, autoDelete != 0);

                }
            }
        } catch (NumberFormatException e) {
            log.error("Error in loading exchanges",e);
        }


        return exchangeNames;
    }

    public List<String> synchExchanges(VirtualHostConfigSynchronizer vhcs) throws Exception {

        List<String> exchangeNames = new ArrayList<String>();


        try {


            Mutator<String> mutator =
                    HFactory.createMutator(keyspace,stringSerializer);

            // Retriving multiple rows with Range Slice Query
            SliceQuery<String, String, String> sliceQuery = HFactory.createSliceQuery(keyspace,
                    stringSerializer, stringSerializer, stringSerializer);

            sliceQuery.setKey(EXCHANGE_ROW);
            sliceQuery.setRange("", "", false, 100);
            sliceQuery.setColumnFamily(EXCHANGE_COLUMN_FAMILY);

            QueryResult<ColumnSlice<String, String>> result = sliceQuery.execute();
            ColumnSlice<String, String> columnSlice = result.get();

            long maxId = 0;

            for (Object column : columnSlice.getColumns()) {
                if (column instanceof HColumn) {
                    String columnName = ((HColumn<String, String>) column).getName();
                    String value = ((HColumn<String, String>) column).getValue();
                    String stringValue = new String(value);


                    String[] valuesFields = value.split("|");
                    String type = valuesFields[1];
                    short autoDelete = Short.parseShort(valuesFields[2]);
                    exchangeNames.add(columnName);
                    vhcs.exchange(columnName, type, autoDelete != 0);

                }
            }
        } catch (NumberFormatException e) {
            log.error("Error in synchronizing exchanges",e);
        }


        return exchangeNames;
    }


    @Override
    public void removeExchange(Exchange exchange) throws AMQStoreException {
        throw new UnsupportedOperationException("removeExchange function is unsupported");
    }

    @Override
    public void bindQueue(Exchange exchange, AMQShortString routingKey,
                          AMQQueue queue, FieldTable args) throws AMQStoreException {

        addBinding(exchange, queue, routingKey.asString());

    }

    @Override
    public void unbindQueue(Exchange exchange, AMQShortString routingKey, AMQQueue queue, FieldTable args) throws AMQStoreException {

    }

    @Override
    public void createQueue(AMQQueue queue, FieldTable arguments) throws AMQStoreException {
        createQueue(queue);
    }

    @Override
    public void removeQueue(AMQQueue queue) throws AMQStoreException {
        throw new UnsupportedOperationException("removeQueue function is unsupported");
    }

    @Override
    public void updateQueue(AMQQueue queue) throws AMQStoreException {
        throw new UnsupportedOperationException("updateQueue function is unsupported");
    }

    @Override
    public void configureTransactionLog(String name, TransactionLogRecoveryHandler recoveryHandler,
                                        Configuration storeConfiguration, LogSubject logSubject) throws Exception {
    }

    @Override
    public Transaction newTransaction() {
        return new CassandraTransaction();
    }

    public boolean isConfigured() {
        return configured;
    }

    private class StoredCassandraMessage implements StoredMessage {

        private final long _messageId;
        private volatile SoftReference<StorableMessageMetaData> _metaDataRef;

        private StoredCassandraMessage(long messageId, StorableMessageMetaData metaData) {
            this._messageId = messageId;
            this._metaDataRef = new SoftReference<StorableMessageMetaData>(metaData);
            storeMetaData(_messageId, metaData);
        }

        private StoredCassandraMessage(long messageId, StorableMessageMetaData metaData ,boolean isTopics){
            this._messageId = messageId;
            this._metaDataRef = new SoftReference<StorableMessageMetaData>(metaData);
        }


        @Override
        public StorableMessageMetaData getMetaData() {
            StorableMessageMetaData metaData = _metaDataRef.get();
            if (metaData == null) {
                metaData = CassandraMessageStore.this.getMetaData(_messageId);
                _metaDataRef = new SoftReference<StorableMessageMetaData>(metaData);
            }
            return metaData;
        }

        @Override
        public long getMessageNumber() {
            return _messageId;
        }

        @Override
        public void addContent(int offsetInMessage, ByteBuffer src) {

            CassandraMessageStore.this.addMessageContent(_messageId + "", offsetInMessage, src);
        }

        @Override
        public int getContent(int offsetInMessage, ByteBuffer dst) {

            return CassandraMessageStore.this.getContent(_messageId + "", offsetInMessage, dst);
        }

        @Override
        public TransactionLog.StoreFuture flushToStore() {
            return IMMEDIATE_FUTURE;
        }

        @Override
        public void remove() {

            if(ClusterResourceHolder.getInstance().getClusterConfiguration().isOnceInOrderSupportEnabled()){
                return;
            }
            ColumnQuery<String, String, String> columnQuery =
                    HFactory.createColumnQuery(keyspace, stringSerializer, stringSerializer ,
                            stringSerializer);
            columnQuery.setColumnFamily(MESSAGE_QUEUE_MAPPING_COLUMN_FAMILY).
                    setKey(MESSAGE_QUEUE_MAPPING_ROW).setName("" + _messageId);
            QueryResult<HColumn<String, String>> result = columnQuery.execute();

            HColumn<String, String> rc = result.get();
            if (rc != null) {
                String qname = result.get().getValue();
                CassandraMessageStore.this.removeMessageFromUserQueue(qname, "" + _messageId);
                contentDeletionTasks.add(_messageId);
            } else {
                throw new RuntimeException("Can't remove message : message does not exist");
            }


        }


    }

    private class CassandraTransaction implements Transaction {

        public void enqueueMessage(TransactionLogResource queue, Long messageId)
                throws AMQStoreException {
            try {

                Mutator<String> mutator = HFactory.createMutator(keyspace,stringSerializer);



                String name = queue.getResourceName();
                LongSerializer ls = LongSerializer.get();
                mutator.addInsertion(QUEUE_ENTRY_ROW, QUEUE_ENTRY_COLUMN_FAMILY,
                        HFactory.createColumn(name, messageId, stringSerializer, ls));
                mutator.execute();
            } catch (Exception e) {
                log.error("Error adding Queue Entry ",e);
                throw new AMQStoreException("Error adding Queue Entry "
                        + queue.getResourceName(), e);
            }
        }

        public void dequeueMessage(TransactionLogResource queue, Long messageId) throws AMQStoreException {
            try {

                Mutator<String> mutator =
                        HFactory.createMutator(keyspace, stringSerializer);
                String name = queue.getResourceName();

                mutator.addDeletion(QUEUE_ENTRY_ROW, QUEUE_ENTRY_COLUMN_FAMILY, name,
                        stringSerializer);
                mutator.execute();
            } catch (Exception e) {
                log.error("Error deleting Queue Entry" ,e);
                throw new AMQStoreException("Error deleting Queue Entry :"
                        + queue.getResourceName(), e);
            }

        }

        public void commitTran() throws AMQStoreException {

        }

        public StoreFuture commitTranAsync() throws AMQStoreException {
            return new StoreFuture() {
                public boolean isComplete() {
                    return true;
                }

                public void waitForCompletion() {

                }
            };
        }

        public void abortTran() throws AMQStoreException {

        }
    }

    private class ContentRemoverTask implements Runnable {


        private int waitInterval = 1000;
        private boolean running = true;

        public ContentRemoverTask(int waitInterval) {
            this.waitInterval = waitInterval;
        }

        public void run() {
            while (running) {
                try {
                while (!contentDeletionTasks.isEmpty()) {
                    long messageID = contentDeletionTasks.poll();

                    CassandraMessageStore.this.removeMessageContent("" + messageID, true);
                }

                try {
                    Thread.sleep(waitInterval);
                } catch (InterruptedException e) {
                    log.error(e);
                }
                }catch (Throwable e ) {
                  //  e.printStackTrace();
                }
            }


        }

        public boolean isRunning() {
            return running;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }
    }
   /**
    * <code>PubSubMessageContentRemoverTask</code>
    * This task is used to remove message content from database when the message
    * published and acknowledged from client.
    * It checks the acknowledged message was delivered before a time difference of
    * CONTENT_REMOVAL_TIME_DEFFERENCE and it condition satisfies, it removes messages from
    * data store */
    private class PubSubMessageContentRemoverTask implements Runnable {


        private int waitInterval = 5000;
        private boolean running = true;

        public PubSubMessageContentRemoverTask(int waitInterval) {
            this.waitInterval = waitInterval;
        }

        public void run() {
            while (running) {

                try {
                    while (!pubSubMessageContentDeletionTasks.isEmpty()) {
                        Set<Long> messageIds = pubSubMessageContentDeletionTasks.keySet();
                        for (long messageID : messageIds) {
                            // If ready to remove , remove it from content table
                            if (CassandraMessageStore.this.isReadyAndRemovedMessageContent(messageID)) {
                                pubSubMessageContentDeletionTasks.remove(messageID);
                            }
                        }

                        try {
                            Thread.sleep(waitInterval);
                        } catch (InterruptedException e) {
                            log.error(e);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

        }

        public boolean isRunning() {
            return running;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }
    }


}

