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

import org.wso2.andes.server.ClusterResourceHolder;
import org.wso2.andes.server.cluster.ClusterManager;
import org.wso2.andes.server.cluster.coordination.CoordinationException;
import org.wso2.andes.server.cluster.coordination.lock.QueueResourceLock;

import java.util.List;
import java.util.Random;
/**
 * <Code>CassandraReliableMessageCoordinator</Code>
 * This class will coordinate the message among the subscriptions for the queue.
 * Since this class will be used only when supporting reliable messaging concept
 * There will be only one thread running for a particular queue with in the
 * zookeeper cluster.
 *
 * With the QueueResourceLock it acquires the lock first and then get a message from the
 * queue of cassandra storage. Then it delivers this message to one of the subscriptions
 * for that queue and delete that message from the storage and then release the lock.
 *
 * Then another thread running for that particular queue will acquire the lock and do the
 * same process.
 *
 * With this model, there will be no message loss
 *
 * */
public class CassandraReliableMessageCoordinator extends Thread{
    private String queueName;
    private List<InOrderMessageFlusher> flusherList;
    private boolean working = false;
    private boolean markedForRemoval;
    private String zkServer = "127.0.0.1:2181";
    private QueueResourceLock queueResourceLock;


    public CassandraReliableMessageCoordinator(String queueName, List<InOrderMessageFlusher> flushers) {
        this.queueName = queueName;
        this.flusherList = flushers;
        ClusterManager clusterManagerInstance = ClusterResourceHolder.getInstance().getClusterManager();
        if(clusterManagerInstance != null){
            this.zkServer  = clusterManagerInstance.getZkConnectionString();
            if (ClusterResourceHolder.getInstance().getClusterConfiguration().isClusteringEnabled()) {
                this.queueResourceLock = new QueueResourceLock(zkServer, queueName);
            } else {
                this.queueResourceLock = new QueueResourceLock(queueName);
            }
        }
    }

    public void addFlusher(InOrderMessageFlusher flusher){
        flusherList.add(flusher);
    }

    @Override
    public void run() {
        try {
            working = true;
            queueResourceLock.acquire();
            int random = new Random().nextInt(flusherList.size());
            InOrderMessageFlusher flusher = flusherList.get(random);
            flusher.send();
            queueResourceLock.release();
            working =false;

        } catch (CoordinationException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isWorking() {
        return working;
    }

    public void setWorking(boolean working) {
        this.working = working;
    }

    public void setMarkedForRemoval(boolean markedForRemoval) {
        this.markedForRemoval = markedForRemoval;
        try {
            this.queueResourceLock.destroy();
        } catch (CoordinationException e) {
            e.printStackTrace();
        }
    }

    public boolean isMarkedForRemoval() {
        return markedForRemoval;
    }

    public int removeSubscription(String subscriptionId) {
        int index = -1;
        for (int i = 0; i < flusherList.size(); i++) {
            if (flusherList.get(i).getSubscriptionId().equals(subscriptionId)) {
                index = i;
                break;
            }
        }
        flusherList.remove(index);
        return flusherList.size();
    }
}
