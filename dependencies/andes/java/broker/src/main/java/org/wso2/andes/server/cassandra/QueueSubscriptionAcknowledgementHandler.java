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
package org.wso2.andes.server.cassandra;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.andes.AMQStoreException;
import org.wso2.andes.server.cassandra.OnflightMessageTracker.MsgData;
import org.wso2.andes.server.store.CassandraMessageStore;
import org.wso2.andes.tools.utils.DataCollector;

import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TODO handle message timeouts
 */
public class QueueSubscriptionAcknowledgementHandler {
    /** 
     * this is a delivery performance counter
     */
    private static AtomicLong deliveryCount =  new AtomicLong();
    private static long last10kMessageTimestamp = System.currentTimeMillis(); 
    

    private CassandraMessageStore cassandraMessageStore;

    private Map<Long, QueueMessageTag> deliveryTagMessageMap = new ConcurrentHashMap<Long, QueueMessageTag>();

    private Map<Long, QueueMessageTag> sentMessagesMap = new ConcurrentHashMap<Long, QueueMessageTag>();

    private SortedMap<Long, Long> timeStampAckedMessageIdMap = new ConcurrentSkipListMap<Long, Long>();

    private SortedMap<Long, Long> timeStampMessageIdMap = new ConcurrentSkipListMap<Long, Long>();

    private QueueMessageTagCleanupJob cleanupJob;

    private Map<Long, Long> messageDeliveryTimeRecorderMap = new ConcurrentHashMap<Long, Long>();

    private long timeOutInMills = 10000;

    private long ackedMessageTimeOut = 3 * timeOutInMills;

    private static Log log = LogFactory.getLog(QueueSubscriptionAcknowledgementHandler.class);

    private OnflightMessageTracker messageTracker = OnflightMessageTracker.getInstance();

    //TODO we have two implementations for tracking message Acked and avoid duplicates. Need to get 
    //rid of the old one when we are happy with the new one
    boolean old = false;

    public QueueSubscriptionAcknowledgementHandler(CassandraMessageStore cassandraMessageStore, String queue) {
        this.cassandraMessageStore = cassandraMessageStore;
    }

    public boolean checkAndRegisterSent(long deliveryTag, long messageId, String queue, int channelID) {
        if (!old) {
            return messageTracker.testAndAddMessage(deliveryTag, messageId, queue, channelID);
        } else {
            synchronized (this) {
                if (!sentMessagesMap.containsKey(messageId) && !timeStampAckedMessageIdMap.containsValue(messageId)) {

                    sentMessagesMap.put(messageId, new QueueMessageTag(queue, deliveryTag, messageId));
                    deliveryTagMessageMap.put(deliveryTag, new QueueMessageTag(queue, deliveryTag, messageId));

                    if (DataCollector.enable) {
                        messageDeliveryTimeRecorderMap.put(deliveryTag, System.nanoTime());
                    }

                    timeStampMessageIdMap.put(System.currentTimeMillis(), messageId);

                    if (cleanupJob == null) {
                        synchronized (this) {
                            if (cleanupJob == null) {
                                cleanupJob = new QueueMessageTagCleanupJob();

                                Thread t = new Thread(cleanupJob);
                                t.setName(cleanupJob.getClass().getSimpleName());
                                t.start();
                            }
                        }
                    }
                    return true;
                }
                return false;
            }
        }
    }

    public void handleAcknowledgement(long deliveryTag, int channelID) {
        /*
         * Following code is only a performance counter. No effect of broker logic
         */
        Long localCount = deliveryCount.incrementAndGet();
        if(localCount%10000 == 0){
            long timetook = System.currentTimeMillis() - last10kMessageTimestamp;
            log.info("delivered "+ localCount + ", throughput ="+ (10000*1000/timetook) + " msg/sec, "+ timetook);
            last10kMessageTimestamp = System.currentTimeMillis();
        }
        /*
         * End of perofrmance counter
         */
        
        if (!old) {
            try {
                try {
                    // We first delete the message so even this fails here, no harm
                    // done
                    MsgData msgData = messageTracker.ackReceived(deliveryTag,channelID);
                    cassandraMessageStore.removeMessageFromUserQueue(msgData.queue, msgData.msgID);
                    // then update the tracker
                    cassandraMessageStore.addContentDeletionTask(msgData.msgID);
                    log.debug("Ack:" + msgData.msgID + " " + deliveryTag);
                } catch (AMQStoreException e) {
                    log.error("Error while handling the ack for " + deliveryTag, e);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (deliveryTagMessageMap.containsKey(deliveryTag)) {
                QueueMessageTag tag = deliveryTagMessageMap.get(deliveryTag);
                if (DataCollector.enable) {
                    long currentTime = System.nanoTime();
                    synchronized (this) {
                        long sentTime = messageDeliveryTimeRecorderMap.remove(deliveryTag);
                        DataCollector.write(DataCollector.DELIVERY_ACK_LATENCY, (currentTime - sentTime));
                    }
                    DataCollector.flush();
                }

                try {
                    if (tag != null) {
                        cassandraMessageStore.removeMessageFromUserQueue(tag.getQueue(), tag.getMessageId());
                        synchronized (this) {
                            timeStampAckedMessageIdMap.put(System.currentTimeMillis(), tag.messageId);
                            sentMessagesMap.remove(tag.getMessageId());
                            deliveryTagMessageMap.remove(deliveryTag);
                        }
                        cassandraMessageStore.addContentDeletionTask(tag.getMessageId());
                        // if(log.isDebugEnabled()){
                        log.info("Ack:" + tag.getMessageId() + " " + tag.getDeliveryTag());
                        // }
                    }
                } catch (AMQStoreException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private class QueueMessageTag {

        private long deliveryTag;

        private long messageId;

        private String queue;

        public QueueMessageTag(String queue, long deliveryTag, long msgId) {
            this.queue = queue;
            this.deliveryTag = deliveryTag;
            this.messageId = msgId;
        }

        public long getDeliveryTag() {
            return deliveryTag;
        }

        public long getMessageId() {
            return messageId;
        }

        public String getQueue() {
            return queue;
        }
    }

    /**
     * This will clean up TimedOut QueueMessageTags from the Maps
     */
    private class QueueMessageTagCleanupJob implements Runnable {

        private boolean running = true;

        @Override
        public void run() {

            long currentTime = System.currentTimeMillis();

            while (running) {
                try {
                    synchronized (cassandraMessageStore) {
                        // Here timeStampMessageIdMap.firstKey() is the oldest
                        if (timeStampMessageIdMap.firstKey() + timeOutInMills <= currentTime) {
                            // we should handle timeout
                            SortedMap<Long, Long> headMap = timeStampMessageIdMap.headMap(currentTime - timeOutInMills);
                            if (headMap.size() > 0) {
                                for (Long l : headMap.keySet()) {
                                    long mid = headMap.get(l);
                                    QueueMessageTag mtag = sentMessagesMap.get(mid);

                                    if (mtag != null) {

                                        long deliveryTag = mtag.getDeliveryTag();
                                        if (deliveryTagMessageMap.containsKey(deliveryTag)) {
                                            QueueMessageTag tag = deliveryTagMessageMap.get(deliveryTag);

                                            if (tag != null) {

                                                if (sentMessagesMap.containsKey(tag.getMessageId())) {
                                                    sentMessagesMap.remove(tag.getMessageId());
                                                }
                                                deliveryTagMessageMap.remove(deliveryTag);
                                            }

                                        }
                                    }
                                }

                                for (Long key : headMap.keySet()) {
                                    timeStampMessageIdMap.remove(key);
                                }
                            }

                            if (timeStampAckedMessageIdMap.firstKey() + ackedMessageTimeOut < currentTime) {
                                SortedMap<Long, Long> headAckedMessagesMap = timeStampAckedMessageIdMap
                                        .headMap(currentTime - ackedMessageTimeOut);

                                for (long key : headAckedMessagesMap.keySet()) {
                                    timeStampAckedMessageIdMap.remove(key);
                                }

                            }

                        }

                    }
                } catch (Exception e) {
                    log.error("Error while running Queue Message Tag Cleanup Task", e);
                } finally {
                    try {
                        Thread.sleep(60 * 1000);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
            }

        }

        public void stop() {

        }
    }

}
