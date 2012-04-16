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
import org.wso2.andes.server.queue.AMQQueue;

import java.util.*;
import java.util.concurrent.*;

/**
 * Class <code>OnceInOrderEnabledSubscriptionManager</code>
 * Enable the In order delivery of messages to a subscribers.
 * This will use per message coordination to ensure that one message in a queue is delivered
 * once to only obe subscriber in order queue order. So this will be fairly slow when using in a
 * broker cluster
 */

public class OnceInOrderEnabledSubscriptionManager implements ClusteringEnabledSubscriptionManager{
    private Map<String,CassandraReliableMessageCoordinator> workMap =
            new ConcurrentHashMap<String,CassandraReliableMessageCoordinator>();
    private Queue<String> subscriptionQueue = new ConcurrentLinkedQueue<String>();
    private static Log log = LogFactory.getLog(OnceInOrderEnabledSubscriptionManager.class);

    private ExecutorService executor = null;

    private boolean active = true;

/**
     * Hash map that keeps the unacked messages.
     */
    private Map<AMQChannel, Map<Long, Semaphore>> unAckedMessagelocks =
            new ConcurrentHashMap<AMQChannel, Map<Long, Semaphore>>();


    /**
     * The Local Queue Lock to handle the Coordination between multiple subscribers for a same queue.
     */
    private Map<String,Semaphore> queueLock = new ConcurrentHashMap<String,Semaphore>();

    public void init() {
        executor = Executors.newFixedThreadPool(ClusterResourceHolder.getInstance().getClusterConfiguration().
                getSubscriptionPoolSize());
        start();
    }

    @Override
    public void addSubscription(AMQQueue queue, CassandraSubscription subscription) {
        InOrderMessageFlusher sender = new InOrderMessageFlusher(subscription.getSubscription(),queue,
                subscription.getSession());
        if(!ClusterResourceHolder.getInstance().getClusterConfiguration().isClusteringEnabled()) {

            if(queueLock.get(queue.getResourceName()) == null) {
                Semaphore ql = new Semaphore(1);
                queueLock.put(queue.getResourceName(),ql);
            }
        }

        addWork(sender);
    }

    @Override
    public void removeSubscription(String queueName, String subscriptionId) {
        CassandraReliableMessageCoordinator coordinator = workMap.get(queueName);
        if (coordinator != null) {
          int remainingSubscriptionCount =   coordinator.removeSubscription(subscriptionId);
            if(remainingSubscriptionCount ==0){
                coordinator.setMarkedForRemoval(true);
                queueLock.remove(queueName);
            }
        }
    }

    @Override
   public Map<AMQChannel, Map<Long, Semaphore>> getUnAcknowledgedMessageLocks() {
        return unAckedMessagelocks;
    }

    private void start() {
        active = true;
        executor.submit(new CassandraReliableMessageFlusherManagerTask());
    }


    public void stop() {

        active = false;
    }

    /**
     * When a new subscription create, it will create a flusher for that
     * subscription and send that flusher to this method. There is a
     * Reliable Message Coordinator for each and every queue in the node.
     * If a reliable message coordinator already created for the queue of
     * the subscription, it will add the new flusher to the subscription
     * list of the coordinator and if there is no coordinator for the
     * subscription, it will create a new coordinator and add the flusher
     * to that.
     * @param flusher - InOrderMessageFlusher
     *
     * */
    private void addWork(InOrderMessageFlusher flusher) {
        CassandraReliableMessageCoordinator coordinator = workMap.get(flusher.getQueue().getName());
        if(coordinator == null){
            List<InOrderMessageFlusher> flusherList = new ArrayList<InOrderMessageFlusher>();
            flusherList.add(flusher);
            coordinator = new CassandraReliableMessageCoordinator(flusher.getQueue().getName(),flusherList);
            subscriptionQueue.offer(flusher.getQueue().getName());
            workMap.put(flusher.getQueue().getName(), coordinator);
        } else{
            coordinator.addFlusher(flusher);
        }

    }


    /**
     * <Code></Code> This class with work on Reliable Message Coordinators
     * */

    private class CassandraReliableMessageFlusherManagerTask implements Runnable {

        @Override
        public void run() {
            while (active) {
                if (subscriptionQueue.size() > 0) {

                    String id = subscriptionQueue.peek();
                    if (workMap.containsKey(id)) {
                        CassandraReliableMessageCoordinator work = workMap.get(id);
                        if (work.isMarkedForRemoval()) {
                            workMap.remove(id);
                            subscriptionQueue.remove();
                            if (log.isDebugEnabled()) {
                                log.debug("Removing subscription queue " + id + " from work map");
                            }
                        } else {
                            if (!work.isWorking()) {
                                executor.execute(work);
                            }
                            subscriptionQueue.remove();
                            subscriptionQueue.offer(id);
                        }
                    } else {
                        subscriptionQueue.remove();
                    }

                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Error in thread sleep", e);
                }


            }
        }
    }

    public Map<String, Semaphore> getQueueLock() {
        return queueLock;
    }
}
