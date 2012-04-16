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
import org.wso2.andes.server.ClusterResourceHolder;
import org.wso2.andes.server.protocol.AMQProtocolSession;
import org.wso2.andes.server.queue.AMQQueue;
import org.wso2.andes.server.queue.QueueEntry;
import org.wso2.andes.server.store.CassandraMessageStore;
import org.wso2.andes.server.subscription.Subscription;
import org.wso2.andes.server.subscription.SubscriptionImpl;

import java.util.List;

/**
 * <Code>InOrderMessageFlusher</Code>
 * This class is used to deliver messages to the subscription
 *
 * */

public class InOrderMessageFlusher {
    private Subscription subscription;
    private AMQQueue queue;
    private AMQProtocolSession session;
    private String id;
    private int defaultMessageCount = 1;
    private int messageCount;



    private long maxWaitTimePerMessage = 2*60*1000;

    private static Log log = LogFactory.getLog(CassandraMessageFlusher.class);


    public InOrderMessageFlusher(Subscription subscription, AMQQueue queue, AMQProtocolSession session) {
        this.subscription = subscription;
        this.queue = queue;
        this.session = session;
        this.id = ""+ subscription.getSubscriptionID();
        this.messageCount = defaultMessageCount;


    }



    public void send() {
                CassandraMessageStore messageStore = ClusterResourceHolder.getInstance().
                        getCassandraMessageStore();
                List<QueueEntry> messages = messageStore.
                        getMessagesFromGlobalQueue(subscription.getSubscriptionID(), queue, session, messageCount);
                if (messages.size() > 0) {
                    for (QueueEntry message : messages) {
                        try {
                            if (subscription instanceof SubscriptionImpl.AckSubscription) {
                                //Here we need to get the lock of relevant Channel in order to keep the
                                //proper order in deliveryTagId
                                synchronized (((SubscriptionImpl.AckSubscription) subscription).
                                        getChannel()) {
                                    subscription.send(message);

                                    messageStore.removeMessageFromGlobalQueue(queue.getName(),
                                            message.getMessage().getMessageNumber() + "");
                                }
                            }


                        } catch (Exception e) {
                            log.error("Unexpected Error in Message Flusher Task " +
                                    "while delivering the message : ", e);
                        }
                    }
                }
    }


    public String getSubscriptionId() {
        return id;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public AMQQueue getQueue() {
        return queue;
    }
}
