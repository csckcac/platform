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
import org.wso2.andes.server.ClusterResourceHolder;
import org.wso2.andes.server.protocol.AMQProtocolSession;
import org.wso2.andes.server.queue.AMQQueue;
import org.wso2.andes.server.queue.QueueEntry;
import org.wso2.andes.server.store.CassandraMessageStore;
import org.wso2.andes.server.store.util.CassandraDataAccessException;
import org.wso2.andes.server.subscription.Subscription;
import org.wso2.andes.server.subscription.SubscriptionImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * From JMS Spec
 * -----------------
 *
 * A client uses a QueueBrowser to look at messages on a queue without removing
 * them.
 * The browse methods return a java.util.Enumeration that is used to scan the
 * queue’s messages. It may be an enumeration of the entire content of a queue or
 * it may only contain the messages matching a message selector.
 * Messages may be arriving and expiring while the scan is done. JMS does not
 * require the content of an enumeration to be a static snapshot of queue content.
 * Whether these changes are visible or not depends on the JMS provider.
 */

public class QueueBrowserFlusher extends Thread {

    private Subscription subscription;
    private AMQQueue queue;
    private AMQProtocolSession session;
    private String id;
    private int defaultMessageCount = Integer.MAX_VALUE;
    private int messageCount;

    private static Log log = LogFactory.getLog(QueueBrowserFlusher.class);

    public QueueBrowserFlusher(Subscription subscription, AMQQueue queue, AMQProtocolSession session) {
        this.subscription = subscription;
        this.queue = queue;
        this.session = session;
        this.id = "" + subscription.getSubscriptionID();
        this.messageCount = defaultMessageCount;
    }


    public void send(){
           List<QueueEntry> messages = null;
        try {
            messages = getSortedMessages();
            if (messages.size() > 0) {
                for (QueueEntry message : messages) {
                    try {
                        if (subscription instanceof SubscriptionImpl.BrowserSubscription) {
                            subscription.send(message);
                        }

                    } catch (Exception e) {
                        log.error("Unexpected Error in Message Flusher Task " +
                                "while delivering the message : ", e);
                    }
                }

            }
        } catch (AMQStoreException e) {
            log.error("Error while sending message for Browser subscription",e);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
             // It is essential to confirm auto close , since in the client side it waits to know the end of the messages
                subscription.confirmAutoClose();
            try {
                if (messages.size() >0) {
                    clearBrowserQueue(messages);
                }
            } catch (CassandraDataAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private List<QueueEntry> getSortedMessages() throws Exception {

        CassandraMessageStore messageStore = ClusterResourceHolder.getInstance().
                getCassandraMessageStore();
        List<String> userQueues = messageStore.getUserQueues(queue.getResourceName());
        List<CassandraQueueMessage> queueMessages = new ArrayList<CassandraQueueMessage>();
        for (String userQueue : userQueues) {
            List<CassandraQueueMessage> messages
                    =  messageStore.getMessagesFromUserQueue(userQueue, queue.getResourceName(), Integer.MAX_VALUE);
            for (CassandraQueueMessage message : messages) {
                queueMessages.add(message);
            }
        }
        messageStore.addMessageToBrowserQueue(queue.getResourceName(), queueMessages);
        return messageStore.getMessagesFromBrowserQueue(queue, session, messageCount);
    }

    private void clearBrowserQueue(List<QueueEntry> queueEntries ) throws CassandraDataAccessException {
        CassandraMessageStore messageStore = ClusterResourceHolder.getInstance().
                getCassandraMessageStore();
        messageStore.clearBrowserQueue(queueEntries,queue.getResourceName());
    }


}
