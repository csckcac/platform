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

    public QueueSubscriptionAcknowledgementHandler(CassandraMessageStore cassandraMessageStore , String queue) {
        this.cassandraMessageStore = cassandraMessageStore;
    }

    public QueueSubscriptionAcknowledgementHandler(CassandraMessageStore cassandraMessageStore , String queue ,
                                                   boolean isOnceInOrderMode) {
        this.cassandraMessageStore = cassandraMessageStore;
    }



    public boolean checkAndRegisterSent(long deliveryTag , long messageId , String queue) {
        if (!sentMessagesMap.containsKey(messageId)) {
            deliveryTagMessageMap.put(deliveryTag, new QueueMessageTag(queue, deliveryTag, messageId));
            sentMessagesMap.put(messageId, new QueueMessageTag(queue, deliveryTag, messageId));

            return true;
        }
        return false;
    }




    public void handleAcknowledge(long deliveryTag) {

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
            this.messageId = messageId;
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




}
