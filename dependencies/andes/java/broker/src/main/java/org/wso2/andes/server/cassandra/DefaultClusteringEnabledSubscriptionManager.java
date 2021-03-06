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
import org.wso2.andes.server.AMQChannel;
import org.wso2.andes.server.ClusterResourceHolder;
import org.wso2.andes.server.cluster.ClusterManager;
import org.wso2.andes.server.cluster.coordination.SubscriptionCoordinationManager;
import org.wso2.andes.server.cluster.coordination.SubscriptionListener;
import org.wso2.andes.server.queue.AMQQueue;
import org.wso2.andes.server.store.CassandraMessageStore;
import org.wso2.andes.server.subscription.Subscription;
import org.wso2.andes.server.subscription.SubscriptionImpl;

import java.util.*;
import java.util.concurrent.*;

public class DefaultClusteringEnabledSubscriptionManager implements ClusteringEnabledSubscriptionManager{

    private static Log log = LogFactory.getLog(DefaultClusteringEnabledSubscriptionManager.class);

    private Map<String,CassandraMessageFlusher> workMap =
            new ConcurrentHashMap<String,CassandraMessageFlusher>();

    /**
     * Keeps Subscription that have for this given queue
     */
    private Map<String,Map<String,CassandraSubscription>> subscriptionMap =
            new ConcurrentHashMap<String,Map<String,CassandraSubscription>>();



    private ExecutorService executor =  null;



    /**
     * Hash map that keeps the unacked messages.
     */
    private Map<AMQChannel, Map<Long, Semaphore>> unAckedMessagelocks =
            new ConcurrentHashMap<AMQChannel, Map<Long, Semaphore>>();


    private Map<AMQChannel,QueueSubscriptionAcknowledgementHandler> acknowledgementHandlerMap =
            new ConcurrentHashMap<AMQChannel,QueueSubscriptionAcknowledgementHandler>();


    public void init()  {
        executor =  Executors.newFixedThreadPool(ClusterResourceHolder.getInstance().getClusterConfiguration().
                getSubscriptionPoolSize());

    }

    /**
     * Register a subscription for a Given Queue
     * This will handle the subscription addition task.
     * @param queue
     * @param subscription
     */
    public void addSubscription(AMQQueue queue, CassandraSubscription subscription) {



        if (subscription.getSubscription() instanceof SubscriptionImpl.BrowserSubscription) {
            try {
                ClusterResourceHolder.getInstance().getCassandraMessageStore()
                        .addUserQueueToGlobalQueue(queue.getResourceName());
            } catch (AMQStoreException e) {
                log.error("error while adding subscription to queue  : " + queue.getName() ,e);
            }
            QueueBrowserFlusher flusher = new QueueBrowserFlusher(subscription.getSubscription(),queue,subscription.getSession());
            flusher.send();

        } else {

            Map<String, CassandraSubscription> subscriptions = subscriptionMap.get(queue.getResourceName());

            if (subscriptions == null || subscriptions.size() == 0) {
                synchronized (subscriptionMap) {
                    subscriptions = subscriptionMap.get(queue.getResourceName());
                    if (subscriptions == null || subscriptions.size() == 0) {
                        subscriptions = subscriptionMap.get(queue.getResourceName());
                        if (subscriptions == null) {
                            subscriptions = new ConcurrentHashMap<String, CassandraSubscription>();
                            subscriptions.put(subscription.getSubscription().getSubscriptionID() + "",
                                    subscription);
                            subscriptionMap.put(queue.getResourceName(), subscriptions);
                            handleSubscription(queue, subscriptions);
                        } else if (subscriptions.size() == 0) {
                            subscriptions.put(subscription.getSubscription().getSubscriptionID() + "",
                                    subscription);
                            handleSubscription(queue, subscriptions);
                        }
                    } else {

                        subscriptions.put(subscription.getSubscription().getSubscriptionID() + "", subscription);
                    }
                }
            } else {
                subscriptions.put(subscription.getSubscription().getSubscriptionID() + "", subscription);
            }

        }

        try {
            ClusterResourceHolder.getInstance().getSubscriptionCoordinationManager().handleSubscriptionChange();
        }catch (Exception e) {
            log.error("Error while notifying Subscription change");
        }
    }


    /**
     * Handle Subscription removal for a queue.
     * @param queue  queue for this Subscription
     * @param subId  SubscriptionId
     */
    public void removeSubscription(String queue, String subId) {




        try {
            Map<String,CassandraSubscription> subs = subscriptionMap.get(queue);


        if (subs != null && subs.containsKey(subId)) {
            subs.remove(subId);
            if (subs.size() == 0) {
                // This is the last subscription for this queue
                CassandraMessageFlusher flusher = workMap.remove(queue);
                flusher.stopFlusher();

                    log.debug("Executing subscription removal handler to minimize message losses");
                    ClusterManager cm = ClusterResourceHolder.getInstance().getClusterManager();
                    handleMessageRemoval(queue + "_" + cm.getNodeId(), queue);

                }
            }
        } catch (Exception e) {
            log.error("Error while removing subscription for queue: " + queue,e);
        }

        try {
            ClusterResourceHolder.getInstance().getSubscriptionCoordinationManager().handleSubscriptionChange();
        } catch (Exception e) {
            log.error("Error while notifying Subscription change");
        }

    }


    private void handleMessageRemoval(String userQueue , String globalQueue) throws AMQStoreException {

        /**
         * 1) Remove User userQueue from Global userQueue user userQueue mapping
         * 2) Collect messages from User userQueue
         * 3)
         * 4) Put the messages back to Global Queue
         */

        CassandraMessageStore messageStore = ClusterResourceHolder.getInstance().getCassandraMessageStore();
        messageStore.removeUserQueueFromQpidQueue(globalQueue);

        List<CassandraQueueMessage> messages = messageStore.getMessagesFromUserQueue(userQueue,globalQueue,40);

        while (messages.size() != 0) {
            for (CassandraQueueMessage msg : messages) {

                messageStore.removeMessageFromUserQueue(userQueue, msg.getMessageId());

                try {
                    messageStore.addMessageToGlobalQueue(globalQueue, msg.getMessageId(), msg.getMessage());
                } catch (Exception e) {
                    log.error(e);
                }
            }
            messages = messageStore.getMessagesFromUserQueue(userQueue, globalQueue, 40);
        }


    }

    private void handleSubscription(AMQQueue queue , Map<String,CassandraSubscription>
            cassandraSubscriptions) {
        try {
            ClusterResourceHolder.getInstance().getCassandraMessageStore().
                    addUserQueueToGlobalQueue(queue.getResourceName());
            CassandraMessageFlusher work = new CassandraMessageFlusher(queue,cassandraSubscriptions);
            workMap.put(queue.getResourceName(),work);
            executor.execute(work);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error while adding subscription to queue :" + queue ,e);
        }

    }


    public void markSubscriptionForRemovel(String queue) {
        CassandraMessageFlusher work = workMap.get(queue);

        if (work != null) {
            work.stopFlusher();
        }

    }

    public void stopAllMessageFlushers() {
        Collection<CassandraMessageFlusher> workers = workMap.values();
        for (CassandraMessageFlusher flusher : workers) {
            flusher.stopFlusher();
        }
    }

    public Map<String, CassandraMessageFlusher> getWorkMap() {
        return workMap;
    }






    public Map<AMQChannel, Map<Long, Semaphore>> getUnAcknowledgedMessageLocks() {
        return unAckedMessagelocks;
    }

    @Override
    public Map<AMQChannel, QueueSubscriptionAcknowledgementHandler> getAcknowledgementHandlerMap() {
        return acknowledgementHandlerMap;
    }

}
