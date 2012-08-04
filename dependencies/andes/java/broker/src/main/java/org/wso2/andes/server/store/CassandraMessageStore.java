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
package org.wso2.andes.server.store;

import me.prettyprint.cassandra.serializers.*;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.ColumnQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;
import me.prettyprint.hector.api.query.SliceQuery;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.andes.AMQException;
import org.wso2.andes.AMQStoreException;
import org.wso2.andes.framing.AMQShortString;
import org.wso2.andes.framing.FieldTable;
import org.wso2.andes.server.ClusterResourceHolder;
import org.wso2.andes.server.cassandra.*;
import org.wso2.andes.server.cluster.ClusterManagementInformationMBean;
import org.wso2.andes.server.cluster.ClusterManager;
import org.wso2.andes.server.cluster.coordination.*;
import org.wso2.andes.server.configuration.ClusterConfiguration;
import org.wso2.andes.server.exchange.Exchange;
import org.wso2.andes.server.information.management.QueueManagementInformationMBean;
import org.wso2.andes.server.logging.LogSubject;
import org.wso2.andes.server.message.AMQMessage;
import org.wso2.andes.server.message.MessageTransferMessage;
import org.wso2.andes.server.protocol.AMQProtocolSession;
import org.wso2.andes.server.queue.*;
import org.wso2.andes.server.store.util.CassandraDataAccessException;
import org.wso2.andes.server.store.util.CassandraDataAccessHelper;
import org.wso2.andes.server.virtualhost.VirtualHostConfigSynchronizer;
import org.wso2.andes.tools.utils.DataCollector;

import java.io.ByteArrayOutputStream;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
/**
 * Class <code>CassandraMessageStore</code> is the Message Store implemented for cassandra
 * Working with andes as an alternative to Derby Message Store
 *
 * */
public class CassandraMessageStore implements MessageStore {

    private Cluster cluster;
    private final String USERNAME_KEY = "username";
    private final String PASSWORD_KEY = "password";
    private final String CONNECTION_STRING = "connectionString";
    private final String CLUSTER_KEY = "cluster";
    private final String ID_GENENRATOR= "idGenerator";



    private Keyspace keyspace;
    private final static String KEYSPACE = "QpidKeySpace";
    private final static String LONG_TYPE = "LongType";
    private final static String UTF8_TYPE = "UTF8Type";
    private final static String INTEGER_TYPE = "IntegerType";
    private final static String QUEUE_COLUMN_FAMILY = "Queue";
    private final static String QUEUE_DETAILS_COLUMN_FAMILY = "QueueDetails";
    private final static String QUEUE_DETAILS_ROW = "QUEUE_DETAILS";
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
    private final static String TOPICS_COLUMN_FAMILY = "topics";
    private final static String TOPICS_ROW = "TOPICS";
    private final static String ACKED_MESSAGE_IDS_COLUMN_FAMILY = "acknowledgedMessageIds";
    private final static String ACKED_MESSAGE_IDS_ROW = "acknowledgedMessageIdsRow";

    private final static String BROWSER_QUEUE_COLUMN_FAMILY="BrowserQueues";

    private final static String NODE_DETAIL_COLUMN_FAMILY = "CusterNodeDetails";
    private final static String NODE_DETAIL_ROW = "NodeDetailsRow";

    private final AtomicLong _messageId = new AtomicLong(0);

    private MessageIdGenerator messageIdGenerator = null;

    private SortedMap<Long,Long> contentDeletionTasks = new ConcurrentSkipListMap<Long,Long>();

    private ConcurrentHashMap<Long,Long> pubSubMessageContentDeletionTasks;

    private ConcurrentHashMap<String,ArrayList<String>> topicSubscribersMap = new ConcurrentHashMap<String, ArrayList<String>>();

    private MessageCacheForCassandra readMessageCacheForCassandra = null;

    private ContentRemoverTask messageContentRemovalTask = null;
    private PubSubMessageContentRemoverTask pubSubMessageContentRemoverTask = null;
    private ClusterManagementInformationMBean clusterManagementMBean;
    private QueueManagementInformationMBean queueManagementMBean;

    private boolean configured = false;


    private static StringSerializer stringSerializer = StringSerializer.get();
    private static LongSerializer longSerializer = LongSerializer.get();
    private static BytesArraySerializer bytesArraySerializer = BytesArraySerializer.get();
    private static IntegerSerializer integerSerializer = IntegerSerializer.get();
    private static ByteBufferSerializer byteBufferSerializer = ByteBufferSerializer.get();


    private PublishMessageWriter publishMessageWriter;
    private PublishMessageContentWriter publishMessageContentWriter;


    private static Log log =
            LogFactory.getLog(CassandraMessageStore.class);

    public CassandraMessageStore() {
        ClusterResourceHolder.getInstance().setCassandraMessageStore(this);
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
                addMessageToGlobalQueue(destinationQueue.getResourceName(), messageId,
                        underlying);
            } catch (Exception e) {
               throw new RuntimeException("Error while adding messages to queues  " ,e);
            }
        }
    }

    public List<QueueEntry> getMessagesFromUserQueue(AMQQueue queue,
                                                     int messageCount) throws AMQStoreException {


        List<QueueEntry> messages = null;
        SimpleQueueEntryList list = new SimpleQueueEntryList(queue);
        ClusterManager clusterManager = ClusterResourceHolder.getInstance().getClusterManager();
        String key = queue.getResourceName()+"_" + clusterManager.getNodeId();
        try {
            messages = new ArrayList<QueueEntry>();
            ColumnSlice<Long,byte[]> columnSlice = CassandraDataAccessHelper.getMessagesFromQueue(key,
                    USER_QUEUES_COLUMN_FAMILY,keyspace,messageCount);
            for (Object column : columnSlice.getColumns()) {
                if (column instanceof HColumn) {
                    long messageId = ((HColumn<Long, byte[]>) column).getName();
                    byte[] value = ((HColumn<Long, byte[]>) column).getValue();
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
            throw new AMQStoreException("Error while accessing user queue" + key,e);
        } catch (Exception e) {
            throw new AMQStoreException("Error while accessing user queue" + key,e);
        }


        return messages;
    }

    /**
     *  Get a given Number of Messages from User queue using the given offset
     * @param queue Queue name
     * @param messageCount messagecount
     * @param lastMessageId  last processed message id. we will try  to get messages from
     * lasProcessedMessageId+1 .. lasProcessedMessageId+1 + count
     * @return List of messages
     * @throws AMQStoreException in case of an Data Access Error
     */
    public List<QueueEntry> getMessagesFromUserQueue(AMQQueue queue,
                                                     int messageCount , long lastMessageId) throws AMQStoreException {

        List<QueueEntry> messages = null;
        SimpleQueueEntryList list = new SimpleQueueEntryList(queue);
        ClusterManager clusterManager = ClusterResourceHolder.getInstance().getClusterManager();
        String key = queue.getResourceName()+"_" + clusterManager.getNodeId();
        try {
            messages = new ArrayList<QueueEntry>();
            ColumnSlice<Long,byte[]> columnSlice = CassandraDataAccessHelper.getMessagesFromQueue(key,
                    USER_QUEUES_COLUMN_FAMILY,keyspace,lastMessageId,messageCount);
            for (Object column : columnSlice.getColumns()) {
                if (column instanceof HColumn) {
                    long messageId = ((HColumn<Long, byte[]>) column).getName();
                    byte[] value = ((HColumn<Long, byte[]>) column).getValue();
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
            throw new AMQStoreException("Error while accessing user queue" + key,e);
        } catch (Exception e) {
            throw new AMQStoreException("Error while accessing user queue" + key,e);
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
    public List<CassandraQueueMessage> getMessagesFromUserQueue(String  userQueue,String globalQueue,
                                                                int messageCount) {

        List<CassandraQueueMessage> messages = new ArrayList<CassandraQueueMessage>();

        ClusterManager clusterManager = ClusterResourceHolder.getInstance().getClusterManager();
        try {
            ColumnSlice<Long, byte[]> columnSlice = CassandraDataAccessHelper.getMessagesFromQueue(userQueue.trim(),
                    USER_QUEUES_COLUMN_FAMILY,keyspace,messageCount);
            for (Object column : columnSlice.getColumns()) {
                if (column instanceof HColumn) {
                    long messageId = ((HColumn<Long, byte[]>) column).getName();
                    byte[] value = ((HColumn<Long, byte[]>) column).getValue();

                    byte[] dataAsBytes = value;
                    ByteBuffer buf = ByteBuffer.wrap(dataAsBytes);
                    buf.position(1);
                    buf = buf.slice();
                    CassandraQueueMessage cqm = new CassandraQueueMessage(messageId,globalQueue,dataAsBytes);
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

    public int getMessageCountOfUserQueues(String queueName) {
        int messageCount = 0;
        try {

            List<String> userQueues = getUserQueues(queueName);
            for(String userQueue : userQueues){

                ColumnSlice<Long, byte[]> columnSlice = CassandraDataAccessHelper.getMessagesFromQueue(userQueue.trim(),
                        USER_QUEUES_COLUMN_FAMILY, keyspace, Integer.MAX_VALUE);

                messageCount =+ columnSlice.getColumns().size();

            }
        } catch (NumberFormatException e) {
            log.error("Number format error in getting messages from global queue : " + queueName, e);
        } catch (Exception e) {
            log.error("Error in getting messages from global queue: " + queueName, e);
        }
        return messageCount;
    }


    public int getMessageCountOfGlobalQueue(String queueName) {
        int messageCount =0;
        try {
            ColumnSlice<Long, byte[]> columnSlice = CassandraDataAccessHelper.getMessagesFromQueue(queueName.trim(),
                    GLOBAL_QUEUES_COLUMN_FAMILY,keyspace,Integer.MAX_VALUE);

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
     * @param queueName    Global queue Name
     * @param messageCount Number of messages that must be fetched.
     * @return List of Messages.
     */
    public Queue<CassandraQueueMessage> getMessagesFromGlobalQueue(String queueName,
                                                                   int messageCount) throws AMQStoreException {
        Queue<CassandraQueueMessage> messages = null;

        try {
            messages = new LinkedList<CassandraQueueMessage>();
            ColumnSlice<Long,byte[]> columnSlice = CassandraDataAccessHelper.getMessagesFromQueue(queueName.trim(),
                    GLOBAL_QUEUES_COLUMN_FAMILY, keyspace, messageCount);
            for (Object column : columnSlice.getColumns()) {
                if (column instanceof HColumn) {
                    long messageId = ((HColumn<Long, byte[]>) column).getName();
                    byte[] value = ((HColumn<Long, byte[]>) column).getValue();
                    CassandraQueueMessage msg
                            = new CassandraQueueMessage(messageId, queueName, value);
                    messages.add(msg);
                }
            }
        } catch (NumberFormatException e) {
           throw new AMQStoreException("Number format error in getting messages from global queue : "+ queueName,e );
        } catch (Exception e) {
            throw new AMQStoreException("Error in getting messages from global queue: "+ queueName ,e);
        }


        return messages;
    }
    public List<QueueEntry> getMessagesFromGlobalQueue(AMQQueue queue,
                                                     AMQProtocolSession session, int messageCount) throws AMQStoreException {
        List<QueueEntry> messages = null;
        SimpleQueueEntryList list = new SimpleQueueEntryList(queue);
        try {
            messages = new ArrayList<QueueEntry>();
            ColumnSlice<Long, byte[]> columnSlice = CassandraDataAccessHelper.
                    getMessagesFromQueue(queue.getName().trim(), GLOBAL_QUEUES_COLUMN_FAMILY, keyspace, messageCount);

            for (Object column : columnSlice.getColumns()) {
                if (column instanceof HColumn) {
                    long messageId = ((HColumn<Long, byte[]>) column).getName();
                    byte[] value = ((HColumn<Long, byte[]>) column).getValue();
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
        } catch (Exception e) {
           throw new AMQStoreException("Error while getting messages from queue : "  + queue ,e);
        }


        return messages;
    }





    public void dequeueMessages(AMQQueue queue, List<QueueEntry> messagesToDelete) {

        try {
            List<QueueEntry> messages = messagesToDelete;
            ClusterManager clusterManager = ClusterResourceHolder.getInstance().getClusterManager();
            String key = queue.getResourceName() +"_" + clusterManager.getNodeId();

            for (QueueEntry queueEntry : messages) {
                removeMessageFromUserQueue(key, queueEntry.getMessage().getMessageNumber());
            }
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
    public void removeMessageFromUserQueue(String queueName, long messageId) throws AMQStoreException {
        try {
            CassandraDataAccessHelper.deleteLongColumnFromRaw(USER_QUEUES_COLUMN_FAMILY, queueName, messageId, keyspace);
        } catch (CassandraDataAccessException e) {
            throw new AMQStoreException("Error while removing message from User queue",e);
        }

    }

    /**
     * Remove List of Message From Cassandra Message Store. Use this to Delete set of messages in  CassandraMessageStore
     * In one DB Call
     * @param queueName User Queue name
     * @param msgList  Message List
     * @throws AMQStoreException  If Error occurs while removing data.
     */
    public void removeMessageBatchFromUserQueue(String queueName, List<CassandraQueueMessage> msgList)
            throws AMQStoreException {

        Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);
        try {


            for (CassandraQueueMessage msg : msgList) {
                CassandraDataAccessHelper.deleteLongColumnFromRaw(USER_QUEUES_COLUMN_FAMILY, queueName,
                        msg.getMessageId(), mutator, false);
            }
        } catch (CassandraDataAccessException e) {
            throw new AMQStoreException("Error while removing messages from User queue", e);
        } finally {
            mutator.execute();
        }

    }


    /**
     * Remove a message from Global queue
     *
     * @param queueName
     * @param messageId
     */
    public void removeMessageFromGlobalQueue(String queueName, long messageId) {
        try {
            CassandraDataAccessHelper.deleteLongColumnFromRaw(GLOBAL_QUEUES_COLUMN_FAMILY,
                    queueName, messageId, keyspace);
        } catch (CassandraDataAccessException e) {
            log.error("Error while removing messages from global queue " + queueName ,e);
        }
    }

    public void removeMessageFromGlobalQueue(String queueName, long messageId , Mutator<String> mutator) {

        try {
            CassandraDataAccessHelper.deleteLongColumnFromRaw(GLOBAL_QUEUES_COLUMN_FAMILY,
                    queueName, messageId, mutator,false);
        } catch (CassandraDataAccessException e) {
            log.error("Error while removing messages from global queue " + queueName ,e);
        }
    }

    public void transferMessageBatchFromGlobalQueueToUserQueue(CassandraQueueMessage[] list ,

                                                               String globalQueueName) {
        Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);
        try {
            for(CassandraQueueMessage msg : list) {
                addMessageToUserQueue(msg.getQueue(),msg.getMessageId(),msg.getMessage(),mutator);
                removeMessageFromGlobalQueue(globalQueueName,msg.getMessageId(),mutator);
            }
        } catch (CassandraDataAccessException e) {
            e.printStackTrace();
            log.error("Error while transferring messages from Global Queue to User Queues");
        } finally {
            mutator.execute();
        }
    }

    public void removeMessageBatchFromGlobalQueue(List<CassandraQueueMessage> list , String globalQUeueName) {
        Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);
        try {
            for(CassandraQueueMessage msg : list) {
                removeMessageFromGlobalQueue(globalQUeueName,msg.getMessageId(),mutator);
            }
        } finally {
            mutator.execute();
        }

    }




    public void recover(ConfigurationRecoveryHandler recoveryHandler) throws AMQException {


        boolean readyOrTimeOut = false;
        boolean error = false;

        int initTimeOut = 10;
        int count = 0;
        int maxTries = 10;

        while (!readyOrTimeOut) {
            try {
                ConfigurationRecoveryHandler.QueueRecoveryHandler qrh = recoveryHandler.begin(this);
                loadQueues(qrh);

                ConfigurationRecoveryHandler.ExchangeRecoveryHandler erh = qrh.completeQueueRecovery();
                List<String> exchanges = loadExchanges(erh);
                ConfigurationRecoveryHandler.BindingRecoveryHandler brh = erh.completeExchangeRecovery();
                recoverBindings(brh, exchanges);
                brh.completeBindingRecovery();
            } catch (Exception e) {
                error = true;
                log.error("Error recovering persistent state: " + e.getMessage(), e);
            } finally {
                if(!error) {
                    readyOrTimeOut = true;
                    continue;
                } else {
                    long waitTime = initTimeOut*1000*(long)Math.pow(2,count);
                    log.warn("Waiting for Cluster data to be synced Please ,start the other nodes soon, wait time: "
                            + waitTime + "ms");
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {

                    }
                    if(count > maxTries)
                    {
                        readyOrTimeOut = true;
                        throw new AMQStoreException("Max Backoff attempts expired for data recovery");
                    }
                    count++;
                }
            }

        }


    }


    private Keyspace createKeySpace() throws CassandraDataAccessException {

        this.keyspace = CassandraDataAccessHelper.createKeySpace(cluster, KEYSPACE);


        CassandraDataAccessHelper.createColumnFamily(QUEUE_COLUMN_FAMILY, KEYSPACE, this.cluster, LONG_TYPE);
        CassandraDataAccessHelper.createColumnFamily(BINDING_COLUMN_FAMILY, KEYSPACE, this.cluster, UTF8_TYPE);
        CassandraDataAccessHelper.createColumnFamily(MESSAGE_CONTENT_COLUMN_FAMILY, KEYSPACE, this.cluster, INTEGER_TYPE);
        CassandraDataAccessHelper.createColumnFamily(MESSAGE_CONTENT_ID_COLUMN_FAMILY, KEYSPACE, this.cluster, LONG_TYPE);
        CassandraDataAccessHelper.createColumnFamily(SQ_COLUMN_FAMILY, KEYSPACE, this.cluster, UTF8_TYPE);
        CassandraDataAccessHelper.createColumnFamily(GLOBAL_QUEUE_TO_USER_QUEUE_COLUMN_FAMILY, KEYSPACE, this.cluster,
                UTF8_TYPE);
        CassandraDataAccessHelper.createColumnFamily(QMD_COLUMN_FAMILY, KEYSPACE, this.cluster, LONG_TYPE);
        CassandraDataAccessHelper.createColumnFamily(QUEUE_DETAILS_COLUMN_FAMILY, KEYSPACE, this.cluster, UTF8_TYPE);
        CassandraDataAccessHelper.createColumnFamily(QUEUE_ENTRY_COLUMN_FAMILY, KEYSPACE, this.cluster, UTF8_TYPE);
        CassandraDataAccessHelper.createColumnFamily(EXCHANGE_COLUMN_FAMILY, KEYSPACE, this.cluster, UTF8_TYPE);
        CassandraDataAccessHelper.createColumnFamily(USER_QUEUES_COLUMN_FAMILY, KEYSPACE, this.cluster, LONG_TYPE);
        CassandraDataAccessHelper.createColumnFamily(MESSAGE_QUEUE_MAPPING_COLUMN_FAMILY, KEYSPACE, this.cluster,
                UTF8_TYPE);
        CassandraDataAccessHelper.createColumnFamily(GLOBAL_QUEUES_COLUMN_FAMILY, KEYSPACE, this.cluster, LONG_TYPE);
        CassandraDataAccessHelper.createColumnFamily(GLOBAL_QUEUE_LIST_COLUMN_FAMILY, KEYSPACE, this.cluster, UTF8_TYPE);
        CassandraDataAccessHelper.createColumnFamily(TOPIC_EXCHANGE_MESSAGE_IDS, KEYSPACE, this.cluster, LONG_TYPE);
        CassandraDataAccessHelper.createColumnFamily(PUB_SUB_MESSAGE_IDS, KEYSPACE, this.cluster, LONG_TYPE);
        CassandraDataAccessHelper.createColumnFamily(TOPIC_SUBSCRIBERS, KEYSPACE, this.cluster, UTF8_TYPE);
        CassandraDataAccessHelper.createColumnFamily(TOPICS_COLUMN_FAMILY, KEYSPACE, this.cluster, UTF8_TYPE);
        CassandraDataAccessHelper.createColumnFamily(ACKED_MESSAGE_IDS_COLUMN_FAMILY, KEYSPACE, this.cluster, LONG_TYPE);
        CassandraDataAccessHelper.createColumnFamily(BROWSER_QUEUE_COLUMN_FAMILY,KEYSPACE,this.cluster,LONG_TYPE);
        CassandraDataAccessHelper.createColumnFamily(NODE_DETAIL_COLUMN_FAMILY, KEYSPACE, this.cluster, UTF8_TYPE);

        return keyspace;
    }

    private int getUserQueueCount(String qpidQueueName) throws AMQStoreException {
        int queueCount = 0;
        try {
            ColumnSlice<String, String> columnSlice = CassandraDataAccessHelper.
                    getStringTypeColumnsInARow(qpidQueueName, GLOBAL_QUEUE_TO_USER_QUEUE_COLUMN_FAMILY, keyspace,
                            Integer.MAX_VALUE);
            queueCount = columnSlice.getColumns().size();
        } catch (Exception e) {
            throw new AMQStoreException("Error in getting user queue count",e);
        }
        return queueCount;
    }

    /**
     * Add a Message to Internal User level Queue
     *
     * @param userQueue User Queue Name
     * @param messageId message id
     * @param message   message content.
     */
    public void addMessageToUserQueue(String userQueue, long messageId, byte[] message , Mutator<String> mutator)
            throws CassandraDataAccessException {
        try {

            CassandraDataAccessHelper.addMessageToQueue(USER_QUEUES_COLUMN_FAMILY, userQueue,
                        messageId, message, mutator, false);
            CassandraDataAccessHelper.addMappingToRaw(MESSAGE_QUEUE_MAPPING_COLUMN_FAMILY, MESSAGE_QUEUE_MAPPING_ROW,
                        "" + messageId, userQueue, mutator, false);
        } catch (Exception e) {
            throw new CassandraDataAccessException("Error in adding message :" + messageId +" to user queue :" +
                    userQueue,e);
        }
    }


public void addMessageBatchToUserQueues(CassandraQueueMessage[] messages) throws CassandraDataAccessException {

    try {
        Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);
        try {
            for (CassandraQueueMessage message : messages) {
                addMessageToUserQueue(message.getQueue(), message.getMessageId(), message.getMessage(), mutator);
            }
        } finally {
            mutator.execute();
        }


    } catch (CassandraDataAccessException e) {
        throw new CassandraDataAccessException("Error in adding message batch to Queues ", e);
    }
    }

    public void addMessageToBrowserQueue(String queue,List<CassandraQueueMessage> messages) throws Exception{

        try {
            for (CassandraQueueMessage message: messages) {
                if(log.isDebugEnabled()){
                    log.debug("Adding message with id "+ message.getMessageId() +" to browser queue "+ queue);
                }
                CassandraDataAccessHelper.addMessageToQueue(BROWSER_QUEUE_COLUMN_FAMILY,queue,message.getMessageId(),message.getMessage(),keyspace);
            }
        } catch (CassandraDataAccessException e) {
             log.error("Error in adding message to browser queue " + queue, e);
           throw new Exception("Error in adding message to browser queue " + queue, e);
        }
    }

    public List<QueueEntry> getMessagesFromBrowserQueue(AMQQueue queue,
                                                     AMQProtocolSession session, int messageCount) throws AMQStoreException {
        List<QueueEntry> messages = null;
        SimpleQueueEntryList list = new SimpleQueueEntryList(queue);
        try {
            messages = new ArrayList<QueueEntry>();
            ColumnSlice<Long, byte[]> columnSlice = CassandraDataAccessHelper.
                    getMessagesFromQueue(queue.getName().trim(), BROWSER_QUEUE_COLUMN_FAMILY, keyspace, messageCount);
            for (Object column : columnSlice.getColumns()) {
                if (column instanceof HColumn) {
                    long messageId = ((HColumn<Long, byte[]>) column).getName();
                    byte[] value = ((HColumn<Long, byte[]>) column).getValue();
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
        } catch (Exception e) {
           throw new AMQStoreException("Error while getting messages from queue : "  + queue ,e);
        }


        return messages;
    }

   public void clearBrowserQueue(List<QueueEntry> messages,String queueName) throws CassandraDataAccessException {
       for (QueueEntry message: messages) {
           CassandraDataAccessHelper.deleteStringColumnFromRaw(BROWSER_QUEUE_COLUMN_FAMILY,queueName,message.getMessage().getMessageNumber()+"",keyspace);
       }
   }

    /**
     * Add message to global queue
     * @param queue
     * @param messageId
     * @param message
     */
    public void addMessageToGlobalQueue(String queue, long messageId, byte[] message) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Adding Message with id " + messageId + " to Queue " + queue);
        }
        publishMessageWriter.addMessage(queue,messageId,message);
    }


    public void addMessageContent(String messageId, int offset, ByteBuffer src) throws AMQStoreException {

        try {
            String rowKey = "mid" + messageId;
            src = src.slice();
            byte[] chunkData = new byte[src.limit()];

            src.duplicate().get(chunkData);
            publishMessageContentWriter.addMessage(rowKey.trim(), offset, chunkData);

        } catch (Exception e) {
            throw new AMQStoreException("Error in adding message content" ,e);
        }
    }

    public void removeMessageContent(String messageId) throws AMQStoreException {
        try {
            String rowKey = "mid" + messageId;
            CassandraDataAccessHelper.deleteIntegerColumnFromRow(MESSAGE_CONTENT_COLUMN_FAMILY,rowKey.trim(),null,keyspace);
        } catch (Exception e) {
            throw new AMQStoreException("Error in removing message content", e);
        }
    }



    private int getContent(String messageId, int offsetValue, ByteBuffer dst) {

        int written = 0;
        written = readMessageCacheForCassandra.getContent(messageId,offsetValue,dst);
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
            HColumn<Long, byte[]> column = CassandraDataAccessHelper.
                    getLongByteArrayColumnInARow(QMD_ROW_NAME,QMD_COLUMN_FAMILY,messageId,keyspace);
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

    private void removeMetaData(long messageId) throws AMQStoreException {
        try {
            Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);
            CassandraDataAccessHelper.deleteLongColumnFromRaw(QMD_COLUMN_FAMILY,QMD_ROW_NAME,messageId,mutator,true);
        } catch (Exception e) {
            throw new AMQStoreException("Error in removing metadata", e);
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
    private void removeAckedMessage(long messageId) throws AMQStoreException {
        try {
            Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);
            CassandraDataAccessHelper.deleteLongColumnFromRaw(ACKED_MESSAGE_IDS_COLUMN_FAMILY, ACKED_MESSAGE_IDS_ROW,
                    messageId, mutator, true);
        } catch (Exception e) {
            throw new AMQStoreException("Error in storing meta data", e);
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

                if (column != null  &&  column.getValue() != null ) {
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
                    return false;
                }
            }else{
                return true;
            }

        } catch (Exception e) {
            log.error("Error while removing Message data",e);
            return false;
        }
    }

    public void addBinding(Exchange exchange, AMQQueue amqQueue, String routingKey) throws CassandraDataAccessException {
        if (keyspace == null) {
            return;
        }

            String columnName = routingKey;
            String columnValue = amqQueue.getName();
            CassandraDataAccessHelper.addMappingToRaw(BINDING_COLUMN_FAMILY, exchange.getName(), columnName,
                    columnValue, keyspace);

    }


    public void removeBinding(Exchange exchange, AMQQueue amqQueue, String routingKey)
            throws CassandraDataAccessException {

        if(keyspace == null) {
            return;
        }
        CassandraDataAccessHelper.deleteStringColumnFromRaw(BINDING_COLUMN_FAMILY,exchange.getName(),routingKey,keyspace);

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
        try {
            List<String> registeredSubscribers = getRegisteredSubscribersForTopic(topic);
            for (String subscriber : registeredSubscribers) {

                try {
                    addMessageIdToSubscriberQueue(subscriber, messageId);
                } catch (AMQStoreException e) {
                    log.error("Error adding message id " + messageId + "To subscriber " + subscriber);
                }
            }
        } catch (Exception e) {
            log.error("Error while adding Message Id to Subscriber queue" ,e);
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
                if(messageMetaData != null) {
                    StoredCassandraMessage storedCassandraMessage = new StoredCassandraMessage(messageId, messageMetaData, true);
                    MessageTransferMessage message = new MessageTransferMessage(storedCassandraMessage, null);
                    messages.add(message);
                }
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
            if(topic!= null && (topicSubscribersMap.get(topic) == null)) {
                topicSubscribersMap.put(topic,new ArrayList<String>());
            }
            CassandraDataAccessHelper.addMappingToRaw(TOPICS_COLUMN_FAMILY, TOPICS_ROW, topic, topic, keyspace);
        } catch (Exception e) {
            log.error("Error in registering queue for the topic", e);
        }
    }

    /**
     * Getting all the topics where subscribers exists
     * */
    public List<String> getTopics() throws Exception {
        try {

            List<String> topicList = CassandraDataAccessHelper.getRowList(TOPICS_COLUMN_FAMILY, TOPICS_ROW, keyspace);
            return topicList;

        } catch (Exception e) {
            log.error("Error in getting the topic list", e);
            throw e;
        }

    }


    public List<String> getUserQueues(String qpidQueueName) throws Exception {
        if (keyspace == null) {
            return new ArrayList<String>();
        }
        try {
            List<String> userQueues = CassandraDataAccessHelper.getRowList(GLOBAL_QUEUE_TO_USER_QUEUE_COLUMN_FAMILY,
                    qpidQueueName, keyspace);
            return  userQueues;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in getting user queues for qpid queue :" + qpidQueueName,e);
            throw e;
        }
    }


    public List<String> getGlobalQueues() throws Exception {

        if (keyspace == null) {
            return new ArrayList<String>();
        }

        try {
            List<String> globalQueues = CassandraDataAccessHelper.getRowList(GLOBAL_QUEUE_LIST_COLUMN_FAMILY,
                    GLOBAL_QUEUE_LIST_ROW, keyspace);
            return globalQueues;
        } catch (Exception e) {
            log.error("Error in getting global queues", e);
            throw e;
        }
    }

    /**
     * Remove the topic from the topics column family when there are no subscribers for that topic
     * @param topic
     * */
    private void unRegisterTopic(String topic) throws AMQStoreException {
          try {
            if(log.isDebugEnabled()){
                log.debug(" removing topic : " + topic  );
            }
            CassandraDataAccessHelper.deleteStringColumnFromRaw(TOPICS_COLUMN_FAMILY, TOPICS_ROW, topic, keyspace);
        } catch (Exception e) {
           throw new AMQStoreException("Error in un registering topic" ,e);
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
            CassandraDataAccessHelper.addMappingToRaw(TOPIC_SUBSCRIBERS, topic, queueName, queueName, keyspace);
            ClusterResourceHolder.getInstance().getTopicSubscriptionCoordinationManager().handleSubscriptionChange(topic);
        } catch (Exception e) {
            log.error("Error in registering queue for the topic",e);
        }
    }

    /**
     * Retrieving the names of the subscriptions (Queue Names) which are subscribed for the
     * provided topic
     * @param topic - Name of the topic
     * @return List of names
     *
     * */
    public List<String> getRegisteredSubscribersForTopic(String topic) throws Exception {
        try {
            List<String> queueList = topicSubscribersMap.get(topic);
            return queueList;
        } catch (Exception e) {
            log.error("Error in getting registered subscribers for the topic", e);
            throw e;
        }
    }
    /**
     * Removing the subscription entry from the subscribers list for the topic
     * @param topic - Name of the topic
     * @param queueName - Queue name to be removed
     * */
    public void unRegisterQueueFromTopic(String topic, String queueName) {

        try {
            if(log.isDebugEnabled()){
                log.debug(" removing queue = " + queueName + " from topic =" + topic);
            }
            CassandraDataAccessHelper.deleteStringColumnFromRaw(TOPIC_SUBSCRIBERS, topic, queueName, keyspace);
            if ((getRegisteredSubscribersForTopic(topic) != null) && (getRegisteredSubscribersForTopic(topic).size() == 0)) {
                unRegisterTopic(topic);
                topicSubscribersMap.remove(topic);
            }
            ClusterResourceHolder.getInstance().getTopicSubscriptionCoordinationManager().handleSubscriptionChange(topic);
        } catch (Exception e) {
           log.error("Error in un registering queue from the topic" ,e);
        }
    }
    /**
     * Adding message id to the subscriber queue
     * @param queueName - Name of the queue
     * @param messageId - Message ID
     * */
    private void addMessageIdToSubscriberQueue(String queueName, long messageId) throws AMQStoreException {
        try {
            long columnName = messageId;
            long columnValue = messageId;
            CassandraDataAccessHelper.addLongContentToRow(PUB_SUB_MESSAGE_IDS, queueName, columnName,columnValue,keyspace);

        } catch (Exception e) {
           throw new AMQStoreException("Error in adding message Id to subscriber queue" ,e);
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
                log.error("Error in retriving message ids of the queue", e);
            }
            return queueList;
        }

    /**
     * Remove delivered messages from the provided queue
     * @param messageIdsToBeRemoved - List of delivered message ids to be removed
     * @param queueName - name of the queue
     * */
    public void removeDeliveredMessageIds(List<Long> messageIdsToBeRemoved, String queueName)
            throws AMQStoreException {
        try {
            Mutator<String> mutator =   HFactory.createMutator(keyspace, stringSerializer);
            for (Long mid : messageIdsToBeRemoved) {
                CassandraDataAccessHelper.
                        deleteLongColumnFromRaw(PUB_SUB_MESSAGE_IDS, queueName, mid, mutator, false);
                if(log.isDebugEnabled()){
                    log.debug(" removing mid = " + mid + " from ="  + queueName);
                }
            }
            mutator.execute();
        } catch (Exception e) {
            throw new AMQStoreException("Error in removing message ids from subscriber queue" ,e);
        }
    }


    public void synchBindings(VirtualHostConfigSynchronizer vhcs) {
        try {


            Mutator<String> mutator =
                    HFactory.createMutator(keyspace, stringSerializer);


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

            Mutator<String>  mutator = HFactory.createMutator(keyspace, stringSerializer);



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
            sliceQuery.setRange(Long.parseLong("0"), Long.MAX_VALUE, false, 10000);

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

    public AtomicLong currentMessageId() {
        return _messageId;
    }




    public void synchQueues(VirtualHostConfigSynchronizer vhcs) throws Exception {


        try {

            // Retrieving multiple rows with Range Slice Query
            ColumnSlice<String,String> columnSlice = CassandraDataAccessHelper.
                    getStringTypeColumnsInARow(QUEUE_DETAILS_ROW, QUEUE_DETAILS_COLUMN_FAMILY, keyspace, Integer.MAX_VALUE);
            for (Object column : columnSlice.getColumns()) {
                if (column instanceof HColumn) {
                    String columnName = ((HColumn<String, String>) column).getName();
                    String value = ((HColumn<String, String>) column).getValue();
                    String[] valuesFields = value.split("|");
                    String owner = valuesFields[1];
                    boolean isExclusive = Boolean.parseBoolean(valuesFields[2]);
                    vhcs.queue(columnName, owner, isExclusive, null);


                }
            }
        } catch (Exception e) {
            throw new AMQStoreException("Error in queue synchronization" ,e);
        }


    }


    public void loadQueues(ConfigurationRecoveryHandler.QueueRecoveryHandler qrh) throws Exception {


        try {


            // Retriving multiple rows with Range Slice Query
            ColumnSlice<String, String> columnSlice = CassandraDataAccessHelper.
                    getStringTypeColumnsInARow(QUEUE_DETAILS_ROW,QUEUE_DETAILS_COLUMN_FAMILY,keyspace,
                            Integer.MAX_VALUE);
            for (Object column : columnSlice.getColumns()) {
                if (column instanceof HColumn) {
                    String columnName = ((HColumn<String, String>) column).getName();
                    String value = ((HColumn<String, String>) column).getValue();
                    String[] valuesFields = value.split("|");
                    String owner = valuesFields[1];
                    boolean isExclusive = Boolean.parseBoolean(valuesFields[2]);
                    qrh.queue(columnName, owner, isExclusive, null);
                }
            }
        } catch (Exception e) {
           throw new AMQStoreException("Error in loading queues",e);
        }


    }



    /**
     * Add Global Queue to User Queue Mapping
     * @param globalQueueName
     */
    public void addUserQueueToGlobalQueue(String globalQueueName) throws AMQStoreException {

        try {
            ClusterManager clusterManager = ClusterResourceHolder.getInstance().getClusterManager();
            String userQueueName = globalQueueName + "_" + clusterManager.getNodeId();
            Mutator<String> qqMutator = HFactory.createMutator(keyspace, stringSerializer);
            CassandraDataAccessHelper.addMappingToRaw(GLOBAL_QUEUE_TO_USER_QUEUE_COLUMN_FAMILY, globalQueueName,
                    userQueueName, userQueueName, qqMutator, false);
            CassandraDataAccessHelper.addMappingToRaw(GLOBAL_QUEUE_LIST_COLUMN_FAMILY, GLOBAL_QUEUE_LIST_ROW, globalQueueName,
                    globalQueueName, qqMutator, true);
        } catch (Exception e) {
           throw new AMQStoreException("Error in adding user queue to global queue",e);
        }
    }

    public void removeUserQueueFromQpidQueue(String globalQueueName) {
        try {
            ClusterManager clusterManager = ClusterResourceHolder.getInstance().getClusterManager();
            String userQueueName = globalQueueName + "_" +
                    clusterManager.getNodeId();
            CassandraDataAccessHelper.deleteStringColumnFromRaw(GLOBAL_QUEUE_TO_USER_QUEUE_COLUMN_FAMILY,
                    globalQueueName.trim(), userQueueName, keyspace);
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

        Object connections = configuration.getProperty(CONNECTION_STRING);
        String connectionString = "";
        if(connections instanceof ArrayList) {
            ArrayList<String> cons = (ArrayList<String>)connections;

            for(String c : cons) {
                connectionString += c + ",";
            }

            connectionString = connectionString.substring(0,connectionString.length()-1);
        } else if(connectionString instanceof String) {
            connectionString = (String) connections;
        }
        String clusterName = (String) configuration.getProperty(CLUSTER_KEY);
        String idGeneratorImpl = (String) configuration.getProperty(ID_GENENRATOR);

        cluster = CassandraDataAccessHelper.createCluster(userName, password, clusterName, connectionString);
        keyspace = createKeySpace();


        if(idGeneratorImpl != null && !"".equals(idGeneratorImpl)) {
            try {
                Class clz = Class.forName(idGeneratorImpl);

                Object o = clz.newInstance();
                messageIdGenerator = (MessageIdGenerator)o;
            } catch (Exception e) {
                log.error("Error while loading Message id generator implementation : " + idGeneratorImpl +
                        " adding TimeStamp based implementation as the default",e);
                messageIdGenerator = new TimeStampBasedMessageIdGenerator();
            }
        } else {
            messageIdGenerator = new TimeStampBasedMessageIdGenerator();
        }

        messageContentRemovalTask = new ContentRemoverTask(ClusterResourceHolder.getInstance().getClusterConfiguration().
                getContentRemovalTaskInterval());
        messageContentRemovalTask.setRunning(true);
        Thread t = new Thread(messageContentRemovalTask);
        t.setName(messageContentRemovalTask.getClass().getSimpleName()+"-Thread");
        t.start();

        pubSubMessageContentDeletionTasks = new ConcurrentHashMap<Long,Long>();

        ClusterConfiguration clusterConfiguration = ClusterResourceHolder.getInstance().getClusterConfiguration();
        pubSubMessageContentRemoverTask = new PubSubMessageContentRemoverTask(clusterConfiguration.
                getPubSubMessageRemovalTaskInterval());
        pubSubMessageContentRemoverTask.setRunning(true);
        Thread th = new Thread(pubSubMessageContentRemoverTask);
        th.start();


        publishMessageWriter = new PublishMessageWriter();
        publishMessageWriter.start();
        Thread messageWriter= new Thread(publishMessageWriter);
        messageWriter.setName(PublishMessageWriter.class.getName());
        messageWriter.start();

        publishMessageContentWriter = new PublishMessageContentWriter();
        publishMessageContentWriter.start();
        Thread contentWriter = new Thread(publishMessageContentWriter);
        contentWriter.setName(PublishMessageContentWriter.class.getName());
        contentWriter.start();

        readMessageCacheForCassandra = new MessageCacheForCassandra();

        AndesConsistantLevelPolicy consistencyLevel = new AndesConsistantLevelPolicy();

        keyspace.setConsistencyLevelPolicy(consistencyLevel);




            if(ClusterResourceHolder.getInstance().getSubscriptionCoordinationManager() == null) {

                SubscriptionCoordinationManager subscriptionCoordinationManager =
                        new SubscriptionCoordinationManagerImpl();
                subscriptionCoordinationManager.init();
                ClusterResourceHolder.getInstance().setSubscriptionCoordinationManager(subscriptionCoordinationManager);
            }

            if(ClusterResourceHolder.getInstance().getTopicSubscriptionCoordinationManager() == null) {

                TopicSubscriptionCoordinationManager topicSubscriptionCoordinationManager =
                        new TopicSubscriptionCoordinationManager();
                topicSubscriptionCoordinationManager.init();
                ClusterResourceHolder.getInstance().setTopicSubscriptionCoordinationManager(topicSubscriptionCoordinationManager);
            }
         ClusterManager clusterManager = null;

            if (clusterConfiguration.isClusteringEnabled()) {
                clusterManager = new ClusterManager(ClusterResourceHolder.getInstance().
                        getCassandraMessageStore(), clusterConfiguration.getZookeeperConnection());
            } else {
                clusterManager = new ClusterManager(ClusterResourceHolder.getInstance().getCassandraMessageStore());
            }


            ClusterResourceHolder.getInstance().setClusterManager(clusterManager);
            clusterManager.init();


        clusterManagementMBean = new ClusterManagementInformationMBean(clusterManager);
        clusterManagementMBean.register();

        queueManagementMBean = new QueueManagementInformationMBean();
        queueManagementMBean.register();


        if (ClusterResourceHolder.getInstance().getClusterConfiguration().isOnceInOrderSupportEnabled()) {
            ClusteringEnabledSubscriptionManager subscriptionManager =
                    new OnceInOrderEnabledSubscriptionManager();
            ClusterResourceHolder.getInstance().setSubscriptionManager(subscriptionManager);
            subscriptionManager.init();

        } else {
            ClusteringEnabledSubscriptionManager subscriptionManager =
                    new DefaultClusteringEnabledSubscriptionManager();
            ClusterResourceHolder.getInstance().setSubscriptionManager(subscriptionManager);
            subscriptionManager.init();

        }

        configured = true;

    }

    public void syncTopicSubscriptionsWithDatabase(String topic) throws Exception{
        if(topic!=null) {
            ArrayList<String> subscriberQueues = new ArrayList<String>();
            List<String> subscribers = CassandraDataAccessHelper.getRowList(TOPIC_SUBSCRIBERS, topic, keyspace);
            for(String subscriber : subscribers) {
                subscriberQueues.add(subscriber);
            }
            topicSubscribersMap.remove(topic);
            topicSubscribersMap.put(topic, subscriberQueues);
        }
        if(log.isDebugEnabled()) {
            log.debug("Synchronizing subscribers for topic" +topic);
        }
    }

    @Override
    public void close() throws Exception {
        if(!ClusterResourceHolder.getInstance().getClusterManager().isClusteringEnabled()) {
            ClusterResourceHolder.getInstance().getClusterManager().shutDownMyNode();
        }
        if(ClusterResourceHolder.getInstance().getClusterManager().isClusteringEnabled()) {
            deleteNodeData(""+ClusterResourceHolder.getInstance().getClusterManager().getNodeId());
        }
        if(messageContentRemovalTask != null && messageContentRemovalTask.isRunning()) {
            messageContentRemovalTask.setRunning(false);
        }

        if(pubSubMessageContentRemoverTask != null && pubSubMessageContentRemoverTask.isRunning()) {
            pubSubMessageContentRemoverTask.setRunning(false);
        }
    }

    @Override
    public <T extends StorableMessageMetaData> StoredMessage<T> addMessage(T metaData) {
        long mid = messageIdGenerator.getNextId();
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
            String name = exchange.getName();
            String type = exchange.getTypeShortString().asString();
            Short autoDelete = exchange.isAutoDelete() ? (short) 1 : (short) 0;
            String value = name + "|" + type + "|" + autoDelete;
            CassandraDataAccessHelper.addMappingToRaw(EXCHANGE_COLUMN_FAMILY, EXCHANGE_ROW, name, value, keyspace);
        } catch (Exception e) {
            throw new AMQStoreException("Error in creating exchange "+ exchange.getName() ,e);
        }
    }


    public List<String> loadExchanges(ConfigurationRecoveryHandler.ExchangeRecoveryHandler erh)
            throws Exception {

        List<String> exchangeNames = new ArrayList<String>();
        try {
            ColumnSlice<String, String> columnSlice = CassandraDataAccessHelper.
                    getStringTypeColumnsInARow(EXCHANGE_ROW, EXCHANGE_COLUMN_FAMILY, keyspace, Integer.MAX_VALUE);
            for (Object column : columnSlice.getColumns()) {
                if (column instanceof HColumn) {
                    String columnName = ((HColumn<String, String>) column).getName();
                    String value = ((HColumn<String, String>) column).getValue();
                    String[] valuesFields = value.split("|");
                    String type = valuesFields[1];
                    short autoDelete = Short.parseShort(valuesFields[2]);
                    exchangeNames.add(columnName);
                    erh.exchange(columnName, type, autoDelete != 0);

                }
            }
        } catch (Exception e) {
            throw new AMQStoreException("Error in loading exchanges",e);
        }


        return exchangeNames;
    }

    public List<String> synchExchanges(VirtualHostConfigSynchronizer vhcs) throws Exception {

        List<String> exchangeNames = new ArrayList<String>();
        try {
            // Retriving multiple rows with Range Slice Query
            ColumnSlice<String, String> columnSlice = CassandraDataAccessHelper.
                    getStringTypeColumnsInARow(EXCHANGE_ROW, EXCHANGE_COLUMN_FAMILY, keyspace, Integer.MAX_VALUE);
            for (Object column : columnSlice.getColumns()) {
                if (column instanceof HColumn) {
                    String columnName = ((HColumn<String, String>) column).getName();
                    String value = ((HColumn<String, String>) column).getValue();
                    String[] valuesFields = value.split("|");
                    String type = valuesFields[1];
                    short autoDelete = Short.parseShort(valuesFields[2]);
                    exchangeNames.add(columnName);
                    vhcs.exchange(columnName, type, autoDelete != 0);

                }
            }
        } catch (Exception e) {
            throw new AMQStoreException("Error in synchronizing exchanges",e);
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

        try {
            addBinding(exchange, queue, routingKey.asString());
        } catch (CassandraDataAccessException e) {
            throw new AMQStoreException("Error adding Binding details to cassandra store" ,e);
        }

    }

    @Override
    public void unbindQueue(Exchange exchange, AMQShortString routingKey, AMQQueue queue, FieldTable args) throws AMQStoreException {
        try {
            removeBinding(exchange,queue,routingKey.asString());
        } catch (CassandraDataAccessException e) {
            throw  new AMQStoreException("Error removing binding details from cassandra store" ,e);
        }
    }

    @Override
    public void createQueue(AMQQueue queue, FieldTable arguments) throws AMQStoreException {
        createQueue(queue);
    }


    public void createQueue(AMQQueue queue) {
        try {
            String owner = queue.getOwner() == null ? null : queue.getOwner().toString();
            String value = queue.getNameShortString().toString() + "|" + owner + "|" + (queue.isExclusive() ? "true" : "false");
            CassandraDataAccessHelper.addMappingToRaw(QUEUE_DETAILS_COLUMN_FAMILY, QUEUE_DETAILS_ROW,
                    queue.getNameShortString().toString(), value, keyspace);



        } catch (Exception e) {
            throw new RuntimeException("Error While creating a Queue creating queue" ,e);
        }
    }

    /**
     * Add Node details to cassandra
     * @param nodeId node id
     * @param data  node data
     */
    public void addNodeDetails(String nodeId, String data) {
        try {
            CassandraDataAccessHelper.addMappingToRaw(NODE_DETAIL_COLUMN_FAMILY, NODE_DETAIL_ROW, nodeId, data, keyspace);
        } catch (CassandraDataAccessException e) {
            throw new RuntimeException("Error writing Node details to cassandra database" ,e);
        }
    }

    /**
     * Get Node data from a given node
     * @param nodeId  node id assigned by the cluster manager
     * @return Node data
     */
    public String getNodeData(String nodeId) {
        try {
            ColumnSlice<String,String> values= CassandraDataAccessHelper.getStringTypeColumnsInARow(NODE_DETAIL_ROW,NODE_DETAIL_COLUMN_FAMILY,
                    keyspace,Integer.MAX_VALUE);


            Object column = values.getColumnByName(nodeId);

            String columnName = ((HColumn<String, String>) column).getName();
            String value = ((HColumn<String, String>) column).getValue();
            return value;

        } catch (CassandraDataAccessException e) {
            throw new RuntimeException("Error accessing Node details to cassandra database");
        }
    }

    /**
     *  Returns list of all Node ids stored as Cluster nodes in the cassandra database
     * @return
     */
    public List<String> storedNodeDetails() {
        try {
            ColumnSlice<String,String> values= CassandraDataAccessHelper.getStringTypeColumnsInARow(NODE_DETAIL_ROW,NODE_DETAIL_COLUMN_FAMILY,
                    keyspace,Integer.MAX_VALUE);


            List<HColumn<String,String>> columns = values.getColumns();
            List<String> nodes = new ArrayList<String>();
            for(HColumn<String,String> column : columns) {
                nodes.add(column.getName());
            }

            return nodes;

        } catch (CassandraDataAccessException e) {
            throw new RuntimeException("Error accessing Node details to cassandra database");
        }
    }


    public void deleteNodeData(String nodeId) {
        try {
            CassandraDataAccessHelper.deleteStringColumnFromRaw(NODE_DETAIL_COLUMN_FAMILY,NODE_DETAIL_ROW,nodeId,keyspace);
        } catch (CassandraDataAccessException e) {
            throw new RuntimeException("Error accessing Node details to cassandra database");
        }
    }

    /**
     * Create a Global Queue in Cassandra MessageStore
     * @param queueName
     */
    public void createGlobalQueue(String queueName) throws AMQStoreException{

        try {
            CassandraDataAccessHelper.addMappingToRaw(GLOBAL_QUEUE_LIST_COLUMN_FAMILY, GLOBAL_QUEUE_LIST_ROW, queueName,
                      queueName, keyspace);
        } catch (CassandraDataAccessException e) {
            throw new AMQStoreException("Error while adding Global Queue to Cassandra message store",e);
        }

    }
    @Override
    public void removeQueue(AMQQueue queue) throws AMQStoreException {

        try {
            String queueName = queue.getNameShortString().toString();
            CassandraDataAccessHelper.deleteStringColumnFromRaw(QUEUE_DETAILS_COLUMN_FAMILY,QUEUE_DETAILS_ROW,
                    queueName,keyspace);
        } catch (CassandraDataAccessException e) {
            throw new AMQStoreException("Error while deleting queue : " + queue ,e);
        }

    }


    /**
     * Removes a global queue from Cassandra Message Store
     * This will remove the Global queue and associated User queues from the Stores
     * With all the message entries and message content with it.
     * @param queueName Global QueueName
     * @throws AMQStoreException  If Error occurs while deleting the queues
     */
    public void removeGlobalQueue(String queueName) throws AMQStoreException{
        try {

            List<String> userQueues = getUserQueues(queueName);

            for (String userQ : userQueues) {
                CassandraDataAccessHelper.deleteStringColumnFromRaw(GLOBAL_QUEUE_TO_USER_QUEUE_COLUMN_FAMILY,
                        queueName, userQ, keyspace);
            }

            CassandraDataAccessHelper.deleteStringColumnFromRaw(GLOBAL_QUEUE_LIST_COLUMN_FAMILY,GLOBAL_QUEUE_LIST_ROW,
                    queueName,keyspace);

        } catch (Exception e) {
            throw new AMQStoreException("Error while removing Global Queue  : " + queueName , e);
        }
    }

    @Override
    public void updateQueue(AMQQueue queue) throws AMQStoreException {
        try {
            String owner = queue.getOwner() == null ? null : queue.getOwner().toString();
            String value = queue.getNameShortString().toString() + "|" + owner + "|" + (queue.isExclusive() ? "true" : "false");
            CassandraDataAccessHelper.addMappingToRaw(QUEUE_DETAILS_COLUMN_FAMILY, QUEUE_DETAILS_ROW,
                    queue.getNameShortString().toString(), value, keyspace);
        } catch (CassandraDataAccessException e) {
           throw new AMQStoreException("Error in updating the queue",e);
        }
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

            try {
                CassandraMessageStore.this.addMessageContent(_messageId + "", offsetInMessage, src);
            } catch (AMQStoreException e) {
                log.error("Error while adding Message Content to Store",e);
            }
        }

        @Override
        public int getContent(int offsetInMessage, ByteBuffer dst) {
            int c= CassandraMessageStore.this.getContent(_messageId + "", offsetInMessage, dst);
            return c;
        }

        @Override
        public TransactionLog.StoreFuture flushToStore() {
            return IMMEDIATE_FUTURE;
        }

        @Override
        public void remove() {

//            if(ClusterResourceHolder.getInstance().getClusterConfiguration().isOnceInOrderSupportEnabled()){
//                return;
//            }
//            ColumnQuery<String, String, String> columnQuery =
//                    HFactory.createColumnQuery(keyspace, stringSerializer, stringSerializer ,
//                            stringSerializer);
//            columnQuery.setColumnFamily(MESSAGE_QUEUE_MAPPING_COLUMN_FAMILY).
//                    setKey(MESSAGE_QUEUE_MAPPING_ROW).setName("" + _messageId);
//            QueryResult<HColumn<String, String>> result = columnQuery.execute();
//
//            HColumn<String, String> rc = result.get();
//            if (rc != null) {
//                String qname = result.get().getValue();
//                try {
//                    CassandraMessageStore.this.removeMessageFromUserQueue(qname,_messageId);
//                } catch (AMQStoreException e) {
//                    log.error("Error remove message",e);
//                }
//                contentDeletionTasks.add(_messageId);
//            } else {
//                throw new RuntimeException("Can't remove message : message does not exist");
//            }


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
                String name = queue.getResourceName();
                CassandraDataAccessHelper.deleteStringColumnFromRaw(QUEUE_ENTRY_COLUMN_FAMILY,QUEUE_DETAILS_ROW,name,
                        keyspace);
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


        private int waitInterval = 5000;

        private long timeOutPerMessage = 60000; //10s
        private boolean running = true;

        public ContentRemoverTask(int waitInterval) {
            this.waitInterval = waitInterval;
        }

        public void run() {

            while (running) {
                try {

                    if (!contentDeletionTasks.isEmpty()) {
                        long currentTime = System.currentTimeMillis();

                        SortedMap<Long, Long> timedOutList = contentDeletionTasks.headMap(currentTime - timeOutPerMessage);


                        for (Long key : timedOutList.keySet()) {
                            CassandraMessageStore.this.removeMessageContent("" + timedOutList.get(key));
                        }

                        for(Long key : timedOutList.keySet()) {
                            contentDeletionTasks.remove(key);
                        }
                    }

                    try {
                        Thread.sleep(waitInterval);
                    } catch (InterruptedException e) {
                        log.error("Error while Executing content removal Task", e);
                    }
                } catch (Throwable e) {
                    log.error("Error while Executing content removal Task", e);
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

    public void addContentDeletionTask(long messageId) {
        contentDeletionTasks.put(System.currentTimeMillis(),messageId);
    }


    public class PublishMessageWriter implements Runnable {


        private boolean start = false;

        private int writeCount = 200;

        private BlockingQueue<PublishMessageWriterMessage> messageQueue =
                new LinkedBlockingQueue<PublishMessageWriterMessage>();

        private List<PublishMessageWriterMessage> writtenMessages =
                new ArrayList<PublishMessageWriterMessage>();

        public PublishMessageWriter() {
            writeCount = ClusterResourceHolder.getInstance().getClusterConfiguration().
                    getMetadataPublisherMessageBatchSize();
        }
        @Override
        public void run() {
            Mutator<String> messageMutator = HFactory.createMutator(keyspace, stringSerializer);
            Mutator<String> mappingMutator = HFactory.createMutator(keyspace, stringSerializer);
            while (start) {


                int count = 0;

                PublishMessageWriterMessage msg = null;
                try {

                    msg = messageQueue.peek();

                    if (msg == null) {
                        /**
                         * If Queue is empty we flush all the current messages
                         * Notify all the waiting threads
                         * reset counters
                         */
                        messageMutator.execute();
                        mappingMutator.execute();
                        count = 0;
                        for (PublishMessageWriterMessage m : writtenMessages) {
                            m.release();
                        }
                        writtenMessages.clear();

                        msg = messageQueue.take();

                        // We need to add this message too
                        bufferMessageToCassandra(msg, messageMutator, mappingMutator);
                        count++;

                    } else {
                        //add to mutators
                        msg = messageQueue.take();
                        bufferMessageToCassandra(msg, messageMutator, mappingMutator);

                        count++;

                        if (count >= writeCount) {
                            messageMutator.execute();
                            mappingMutator.execute();
                            count = 0;
                            for (PublishMessageWriterMessage m : writtenMessages) {
                                m.release();
                            }
                            writtenMessages.clear();
                        }
                    }

                } catch (InterruptedException e) {
                    log.error("Error while writing incoming messages", e);
                    continue;
                }


                if (log.isDebugEnabled()) {
                    log.debug("Adding Message with id " + msg.messageId + " to Queue " + msg.queue);
                }


            }

        }


        private void bufferMessageToCassandra(PublishMessageWriterMessage msg, Mutator<String> messageMutator,
                                              Mutator<String> mappingMutator) {
            ClusterManager clusterManager = ClusterResourceHolder.getInstance().getClusterManager();

            try {
                long sTime = System.nanoTime();
                CassandraDataAccessHelper.addMessageToQueue(CassandraMessageStore.GLOBAL_QUEUES_COLUMN_FAMILY,
                        msg.queue, msg.messageId, msg.message, messageMutator, false);

                CassandraDataAccessHelper.addMappingToRaw(CassandraMessageStore.GLOBAL_QUEUE_LIST_COLUMN_FAMILY,
                        CassandraMessageStore.GLOBAL_QUEUE_LIST_ROW, msg.queue,
                        msg.queue, mappingMutator, false);
                writtenMessages.add(msg);

                long eTime = System.nanoTime();
                DataCollector.write(DataCollector.PUBLISHER_WRITE_LATENCY, (eTime - sTime));
                DataCollector.flush();
                // clusterManager.handleQueueAddition(msg.queue);

            } catch (Exception e) {
                log.error("Error in adding message to global queue", e);
            }
        }

        public void addMessage(String queue, long messageId, byte[] message) {
            try {
                PublishMessageWriterMessage msg = new PublishMessageWriterMessage(queue, messageId, message);
                messageQueue.add(msg);
                msg.waitForToBeWritten();

            } catch (InterruptedException e) {
                throw new RuntimeException("Error while adding Incomming message", e);
            }
        }

        public void start() {
            start = true;
        }

        public void stop() {
            start = false;
        }


        private class PublishMessageWriterMessage {
            private Semaphore messageCallBack;

            private String queue;
            private long messageId;
            private byte[] message;

            public PublishMessageWriterMessage(String queue, long messageId, byte[] message)
                    throws InterruptedException {
                this.queue = queue;
                this.messageId = messageId;
                this.message = message;
                this.messageCallBack = new Semaphore(1);
                messageCallBack.acquire();
            }

            public void release() {
                messageCallBack.release();
            }

            public void waitForToBeWritten() throws InterruptedException {
                messageCallBack.acquire();
            }

        }
    }

    public class PublishMessageContentWriter implements Runnable {


        private boolean start = false;

        private int writeCount = 200;

        private BlockingQueue<PublishMessageContentWriterMessage> messageQueue =
                new LinkedBlockingQueue<PublishMessageContentWriterMessage>();

        private List<PublishMessageContentWriterMessage> writtenMessages =
                new ArrayList<PublishMessageContentWriterMessage>();

        public PublishMessageContentWriter() {
            writeCount = ClusterResourceHolder.getInstance().getClusterConfiguration().
                    getContentPublisherMessageBatchSize();
        }

        @Override
        public void run() {
            Mutator<String> messageMutator = HFactory.createMutator(keyspace, stringSerializer);
            while (start) {

                int count = 0;

                PublishMessageContentWriterMessage msg = null;
                try {

                    msg = messageQueue.peek();

                    if (msg == null) {
                        /**
                         * If Queue is empty we flush all the current messages
                         * Notify all the waiting threads
                         * reset counters
                         */
                        messageMutator.execute();
                        count = 0;
                        for (PublishMessageContentWriterMessage m : writtenMessages) {
                            m.release();
                        }
                        writtenMessages.clear();

                        msg = messageQueue.take();

                        // We need to add this message too
                        bufferMessageToCassandra(msg, messageMutator);
                        count++;

                    } else {
                        //add to mutators
                        msg = messageQueue.take();

                        bufferMessageToCassandra(msg, messageMutator);

                        count++;

                        if (count >= writeCount) {
                            messageMutator.execute();
                            count = 0;
                            for (PublishMessageContentWriterMessage m : writtenMessages) {
                                m.release();
                            }
                            writtenMessages.clear();
                        }
                    }

                } catch (InterruptedException e) {
                    log.error("Error while writing incoming messages content", e);
                    continue;
                }


            }

        }


        private void bufferMessageToCassandra(PublishMessageContentWriterMessage msg, Mutator<String> messageMutator
        ) {


            try {
                long sTime = System.nanoTime();
                CassandraDataAccessHelper.addIntegerByteArrayContentToRaw(MESSAGE_CONTENT_COLUMN_FAMILY, msg.rowKey,
                        msg.offset, msg.message, messageMutator, false);

                writtenMessages.add(msg);
                long eTime = System.nanoTime();

                DataCollector.write(DataCollector.ADD_MESSAGE_CONTENT, (eTime - sTime));
                DataCollector.flush();

            } catch (Exception e) {
                log.error("Error in adding message to global queue", e);
            }
        }

        public void addMessage(String rowKey, int offset, byte[] message) {
            try {
                PublishMessageContentWriterMessage msg =
                        new PublishMessageContentWriterMessage(rowKey, offset, message);
                messageQueue.add(msg);
                msg.waitForToBeWritten();

            } catch (InterruptedException e) {
                throw new RuntimeException("Error while adding Incomming message", e);
            }
        }

        public void start() {
            start = true;
        }

        public void stop() {
            start = false;
        }


        private class PublishMessageContentWriterMessage {
            private Semaphore messageCallBack;

            private String rowKey;
            private int offset;
            private byte[] message;

            public PublishMessageContentWriterMessage(String rowKey, int offset, byte[] message)
                    throws InterruptedException {
                this.rowKey = rowKey;
                this.offset = offset;
                this.message = message;
                this.messageCallBack = new Semaphore(1);
                messageCallBack.acquire();
            }

            public void release() {
                messageCallBack.release();
            }

            public void waitForToBeWritten() throws InterruptedException {
                messageCallBack.acquire();
            }

        }
    }

    public class MessageCacheForCassandra {

        private final int cacheSize = ClusterResourceHolder.getInstance().
                getClusterConfiguration().getMessageReadCacheSize();

        LinkedHashMap<String, byte[]> topicContentCache = new LinkedHashMap<String, byte[]>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
                return this.size() > cacheSize;
            }
        };

        private String parseKey(String messageId, int offset) {

            return messageId + ":" + offset;

        }

        public int getContent(String messageId, int offsetValue, ByteBuffer dst) {
            int written = 0;
            int chunkSize = 65534;
            byte[] content = null;
            String keyToGet = parseKey(messageId, offsetValue);
            if (topicContentCache.containsKey(keyToGet)) {

                content = (topicContentCache.get(keyToGet));
                if (content != null) {
                    if (offsetValue == 0) {
                        final int size = (int) content.length;
                        int posInArray = offsetValue + written - offsetValue;
                        int count = size - posInArray;
                        if (count > dst.remaining()) {
                            count = dst.remaining();
                        }
                        dst.put(content, 0, count);
                        written = count;
                    } else {
                        int k = offsetValue / chunkSize;
                        final int size = (int) content.length;
                        int posInArray = offsetValue - (k * chunkSize);
                        int count = size - posInArray;
                        if (count > dst.remaining()) {
                            count = dst.remaining();
                        }

                        dst.put(content, posInArray, count);

                        written += count;
                    }
                } else {
                    throw new RuntimeException("Unexpected Error , cache content might be corrupted");
                }
            } else {
                //load from DB
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
                            content = bytesArraySerializer.fromByteBuffer(column.getValue());

                            final int size = (int) content.length;
                            int posInArray = offset + written - offset;
                            int count = size - posInArray;
                            if (count > dst.remaining()) {
                                count = dst.remaining();
                            }
                            dst.put(content, 0, count);
                            written = count;
                        } else {
                            throw new RuntimeException("Unexpected Error , content already deleted");
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
                        boolean added = false;
                        for (HColumn<Integer, ByteBuffer> column : columnSlice.getColumns()) {
                            added = true;
                            byteOutputStream.write(bytesArraySerializer.fromByteBuffer(column.getValue()));
                        }
                        content = byteOutputStream.toByteArray();
                        final int size = (int) content.length;
                        int posInArray = offsetValue - (k * chunkSize);
                        int count = size - posInArray;
                        if (count > dst.remaining()) {
                            count = dst.remaining();
                        }

                        dst.put(content, posInArray, count);

                        written += count;
                    }

                    // add a new entry to the cache. If cache is full eldest entry will be removed.

                    byte[] cacheValue = new byte[content.length];
                    System.arraycopy(content, 0, cacheValue, 0, content.length);
                    addEntryToCache(keyToGet, cacheValue);

                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Error in reading content", e);
                }
            }

            return written;
        }

        private synchronized void addEntryToCache(String key, byte[] cacheVal) {
            topicContentCache.put(key, cacheVal);
        }

    }

}

