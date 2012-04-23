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
import org.wso2.andes.server.AMQChannel;
import org.wso2.andes.server.ClusterResourceHolder;
import org.wso2.andes.server.cluster.ClusterManager;
import org.wso2.andes.server.queue.AMQQueue;
import org.wso2.andes.server.store.CassandraMessageStore;

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

    /**
     * Map that Contain Global Queue to User Queue Mapping
     */
    private HashMap<String, List<String>> userQueuesMap = new HashMap<String,List<String>>();

    private ExecutorService executor =  null;



    /**
     * Hash map that keeps the unacked messages.
     */
    private Map<AMQChannel, Map<Long, Semaphore>> unAckedMessagelocks =
            new ConcurrentHashMap<AMQChannel, Map<Long, Semaphore>>();


    public void init()  {
        executor =  Executors.newFixedThreadPool(ClusterResourceHolder.getInstance().getClusterConfiguration().
                getSubscriptionPoolSize());
        executor.submit(new CassandraSubscriptionsSynchronizer());
    }

    /**
     * Register a subscription for a Given Queue
     * This will handle the subscription addition task.
     * @param queue
     * @param subscription
     */
    public void addSubscription(AMQQueue queue , CassandraSubscription subscription) {
        Map<String,CassandraSubscription> subscriptions = subscriptionMap.get(queue.getResourceName());


        if(subscriptions == null || subscriptions.size() ==0) {
            synchronized (subscriptionMap) {
                subscriptions = subscriptionMap.get(queue.getResourceName());
                if(subscriptions == null || subscriptions.size() == 0) {
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


    /**
     * Handle Subscription removal for a queue.
     * @param queue  queue for this Subscription
     * @param subId  SubscriptionId
     */
    public void removeSubscription(String queue, String subId) {

        Map<String,CassandraSubscription> subs = subscriptionMap.get(queue);

        if (subs != null) {
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

    }


    private void handleMessageRemoval(String userQueue , String globalQueue) {

        /**
         * 1) Remove User userQueue from Global userQueue user userQueue mapping
         * 2) Collect messages from User userQueue
         * 3)
         * 4) Put the messages back to Global Queue
         */

        CassandraMessageStore messageStore = ClusterResourceHolder.getInstance().getCassandraMessageStore();
        messageStore.removeUserQueueFromQpidQueue(userQueue);

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

        //1) UserQueueTo GlobalQueue Mapping
        //2) Add A Flusher
        CassandraMessageFlusher work = new CassandraMessageFlusher(queue,cassandraSubscriptions);

        workMap.put(queue.getResourceName(),work);
        executor.execute(work);

        ClusterResourceHolder.getInstance().getCassandraMessageStore().
                addUserQueueToGlobalQueue(work.getQueue().getResourceName());
    }


    public void markSubscriptionForRemovel(String queue) {
        CassandraMessageFlusher work = workMap.get(queue);

        if (work != null) {
            work.stopFlusher();
        }

    }




    public Map<String, CassandraMessageFlusher> getWorkMap() {
        return workMap;
    }



    public List<String> getUserQueues(String amqpQueueName){
        return userQueuesMap.get(amqpQueueName);
    }


    public Map<AMQChannel, Map<Long, Semaphore>> getUnAcknowledgedMessageLocks() {
        return unAckedMessagelocks;
    }


    private class CassandraSubscriptionsSynchronizer implements Runnable {
        @Override
        public void run() {
            while (true) {

                CassandraMessageStore messageStore = ClusterResourceHolder.getInstance().
                        getCassandraMessageStore();
                List<String> globalQueueList = messageStore.getGlobalQueues();
                for (String  globalQueue : globalQueueList) {
                   List<String> userQueueList = messageStore.getUserQueues(globalQueue);
                    userQueuesMap.put(globalQueue,userQueueList);

                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {

                }
            }
        }
    }


}
