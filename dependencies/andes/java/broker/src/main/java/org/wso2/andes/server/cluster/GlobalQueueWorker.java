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
package org.wso2.andes.server.cluster;

import org.wso2.andes.server.ClusterResourceHolder;
import org.wso2.andes.server.cassandra.CassandraQueueMessage;
import org.wso2.andes.server.cassandra.DefaultClusteringEnabledSubscriptionManager;
import org.wso2.andes.server.store.CassandraMessageStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * <code>GlobalQueueWorker</code> is responsible for polling global queues
 * and distribute messages to the subscriber userQueues.
 */
public class GlobalQueueWorker implements Runnable{


    private String globalQueueName;
    private boolean running;
    private DefaultClusteringEnabledSubscriptionManager cassandraSubscriptionManager;
    private int messageCount;
    private CassandraMessageStore cassandraMessageStore;


    private int count = 0;

    private static int mc = 0;
    public GlobalQueueWorker(String queueName,
                             CassandraMessageStore cassandraMessageStore,
                             int messageCount) {
        this.cassandraMessageStore = cassandraMessageStore;
        this.globalQueueName = queueName;
        this.cassandraSubscriptionManager = (DefaultClusteringEnabledSubscriptionManager)ClusterResourceHolder.
                getInstance().getSubscriptionManager();
        this.messageCount = messageCount;
    }


    public void run() {


        while (running) {
            try {
                /**
                 * Steps
                 *
                 * 1)Poll Global queue and get chunk of messages
                 * 2) Put messages one by one to user queues and delete them
                 */
                Queue<CassandraQueueMessage> cassandraMessages =
                        cassandraMessageStore.getMessagesFromGlobalQueue(globalQueueName, messageCount);
                int size = cassandraMessages.size();


                List<String> subscriptions =
                        cassandraSubscriptionManager.getUserQueues(globalQueueName);
                Random jRandom = new Random();
                if (subscriptions != null && subscriptions.size() > 0) {
                     List<String> addedMsgs = new ArrayList<String>();
                    try {

                        for (int i = 0; i < size; i++) {

//                        System.out.println("Count : " + size + " id : " + ClusterResourceHolder.getInstance().
//                                getClusterManager().getNodeId());

                            CassandraQueueMessage msg = cassandraMessages.poll();

                            //handle the failures .. and only remove what has moved
                            int random = jRandom.nextInt(subscriptions.size());
                            cassandraMessageStore.addMessageToUserQueue(
                                    subscriptions.get(random),
                                    msg.getMessageId(),
                                    msg.getMessage());
                            addedMsgs.add(msg.getMessageId());


                        }
                    } finally {

                        for (String mid : addedMsgs) {
                            cassandraMessageStore.removeMessageFromGlobalQueue(globalQueueName,
                                    mid);
                        }
                    }
                } else {
                    try {
                        Thread.sleep(ClusterResourceHolder.getInstance().getClusterConfiguration().getQueueWorkerInterval());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
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
