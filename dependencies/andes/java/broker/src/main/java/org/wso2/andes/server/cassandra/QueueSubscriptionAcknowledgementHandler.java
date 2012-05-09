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
import org.wso2.andes.server.store.CassandraMessageStore;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO handle message timeouts
 */
public class QueueSubscriptionAcknowledgementHandler {

    private CassandraMessageStore cassandraMessageStore;

    private Map<Long,QueueMessageTag> deliveryTagMessageMap = new ConcurrentHashMap<Long,QueueMessageTag>();

    private Map<Long,QueueMessageTag> sentMessagesMap = new ConcurrentHashMap<Long,QueueMessageTag>();

    private SortedMap<Long,Long> timeStampMessageIdMap = new TreeMap<Long,Long>();

    private QueueMessageTagCleanupJob cleanupJob ;

    private long timeOutInMills;

    private static Log log = LogFactory.getLog(QueueSubscriptionAcknowledgementHandler.class);

    public QueueSubscriptionAcknowledgementHandler(CassandraMessageStore cassandraMessageStore , String queue) {
        this.cassandraMessageStore = cassandraMessageStore;
    }




    public boolean checkAndRegisterSent(long deliveryTag , long messageId , String queue) {
        if (!sentMessagesMap.containsKey(messageId)) {
            deliveryTagMessageMap.put(deliveryTag, new QueueMessageTag(queue, deliveryTag, messageId));
            sentMessagesMap.put(messageId, new QueueMessageTag(queue, deliveryTag, messageId));
            timeStampMessageIdMap.put(System.currentTimeMillis(),messageId);

            if(cleanupJob == null) {
                synchronized (this) {
                    if(cleanupJob == null) {
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




    public void handleAcknowledgement(long deliveryTag) {

        if(deliveryTagMessageMap.containsKey(deliveryTag)) {
            QueueMessageTag tag= deliveryTagMessageMap.get(deliveryTag);
            try {
                if (tag != null) {
                    cassandraMessageStore.removeMessageFromUserQueue(tag.getQueue(), tag.getMessageId());
                    if (sentMessagesMap.containsKey(tag.getMessageId())) {
                        sentMessagesMap.remove(tag.getMessageId());
                    }
                    deliveryTagMessageMap.remove(deliveryTag);
                }

            } catch (AMQStoreException e) {
                e.printStackTrace();
            }
        }

    }


    private class QueueMessageTag {

        private long deliveryTag ;

        private long messageId;

        private String queue;

        public QueueMessageTag(String queue , long deliveryTag , long msgId) {
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
    private class QueueMessageTagCleanupJob implements Runnable{

        private boolean running = true;
        @Override
        public void run() {

            long currentTime = System.currentTimeMillis();

            while (running) {
                try {
                    if(timeStampMessageIdMap.lastKey() + timeOutInMills <= currentTime) {
                        // we should handle timeout
                        SortedMap<Long,Long> tailMap =
                                timeStampMessageIdMap.headMap(currentTime-timeOutInMills);

                        if(tailMap.size() > 0) {
                            for(Long l : tailMap.keySet()) {
                                long mid = tailMap.get(l);
                                QueueMessageTag mtag =  sentMessagesMap.get(mid);


                                if (mtag != null) {

                                    long deliveryTag = mtag.getDeliveryTag();
                                    if (deliveryTagMessageMap.containsKey(deliveryTag)) {
                                        QueueMessageTag tag = deliveryTagMessageMap.get(deliveryTag);
                                        try {
                                            if (tag != null) {
                                                cassandraMessageStore.removeMessageFromUserQueue(tag.getQueue(), tag.getMessageId());
                                                if (sentMessagesMap.containsKey(tag.getMessageId())) {
                                                    sentMessagesMap.remove(tag.getMessageId());
                                                }
                                                deliveryTagMessageMap.remove(deliveryTag);
                                            }

                                        } catch (AMQStoreException e) {
                                           log.error("Error while running Queue Message Tag Cleanup Task" ,e );
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Error while running Queue Message Tag Cleanup Task" ,e );
                } finally {
                    try {
                        Thread.sleep(20*60*1000);
                    } catch (InterruptedException e) {
                        //Ignore
                    }
                }
            }

        }

        public void stop() {

        }
    }


}
