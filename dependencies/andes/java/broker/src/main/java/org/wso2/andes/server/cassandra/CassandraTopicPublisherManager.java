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

/**
 * CassandraTopicPublisherManager
 *
 * Thread pool worker for CassandraTopicPublisher
 * */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CassandraTopicPublisherManager {

    private Map<String, CassandraTopicPublisher> workMap =
            new ConcurrentHashMap<String, CassandraTopicPublisher>();
    private Queue<String> subscriptionQueue = new ConcurrentLinkedQueue<String>();
    private HashMap<String, List<String>> userQueuesMap = new HashMap<String, List<String>>();
    private static Log log =  LogFactory.getLog(CassandraTopicPublisherManager.class);

    private ExecutorService executor = null;

    private boolean active = true;
    private CassandraTopicPublisher currentWork;

    public static final int poolSize = 20;



    public CassandraTopicPublisherManager(){

    }

    public void init() {
        executor = Executors.newFixedThreadPool(poolSize);
    }

    public void start() {
        active = true;
        executor.submit(new CassandraTopicPublisherManagerTask());
    }


    public void stop() {

        active = false;
    }

    public void addWork(String id, CassandraTopicPublisher work) {

        workMap.put(id, work);
        subscriptionQueue.offer(id);
    }

    public CassandraTopicPublisher getCurrentWork() {
        return currentWork;
    }

    public void markSubscriptionForRemoval(String id) {
        CassandraTopicPublisher work = workMap.get(id);
        if (work != null) {
            work.setMarkedForRemoval(true);
        }
    }

    public int getSubscriptionCount() {
        return subscriptionQueue.size();
    }


    public Map<String, CassandraTopicPublisher> getWorkMap() {
        return workMap;
    }

    public Queue<String> getSubscriptionQueue() {
        return subscriptionQueue;
    }

    public List<String> getUserQueues(String amqpQueueName) {
        return userQueuesMap.get(amqpQueueName);
    }


    private class CassandraTopicPublisherManagerTask implements Runnable {

        @Override
        public void run() {
            while (active) {
                if (subscriptionQueue.size() > 0) {

                    String id = subscriptionQueue.peek();
                    if (workMap.containsKey(id)) {
                        CassandraTopicPublisher work = workMap.get(id);
                        if (work.isMarkedForRemoval()) {
                            workMap.remove(id);
                            subscriptionQueue.remove();
                            if(log.isDebugEnabled()){
                                log.debug("Removing subscription queue "+id+" from work map");
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
                    log.error("Error in thread sleep" ,e);
                }


            }
        }
    }
}
