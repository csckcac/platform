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
package org.wso2.andes.server.cluster.coordination.lock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.andes.server.ClusterResourceHolder;
import org.wso2.andes.server.cassandra.ClusteringEnabledSubscriptionManager;
import org.wso2.andes.server.cassandra.OnceInOrderEnabledSubscriptionManager;
import org.wso2.andes.server.cluster.coordination.CoordinationConstants;
import org.wso2.andes.server.cluster.coordination.CoordinationException;
import org.wso2.andes.server.cluster.coordination.ZooKeeperAgent;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

/**
 * Class <code>QueueResourceLock</code>  is the Implementation of the lock
 * that is created per given queue.
 *
 * This will use apache zookeeper when broker is running in the Cluster enabled mode.
 * This will use in memory coordination when broker is running in standalone mode.
 */
public class QueueResourceLock {

    private final Object lock = new Object();

    private String myNode;
    private String myZNode = null;
    private int myId;
    private ZooKeeperAgent zkAgent;


    private String connectionString;
    private String queue;


    /**
     * Queue Lock that is used when Lock is used the local coordination mode.
     */
    private Semaphore queueLock;

    private static Log log = LogFactory.getLog(QueueResourceLock.class);

    /**
     * Creates a Distributed Lock for a queue.
     * @param connectionString zookeeper instance connection String
     * @param queue queue name
     */
    public QueueResourceLock(String connectionString ,String queue) {

        this.connectionString = connectionString;
        this.queue = queue;

    }

    /**
     * Creates a Distributed Lock for a queue
     * @param queue
     */
    public QueueResourceLock(String queue) {
        this.queue=queue;

        ClusteringEnabledSubscriptionManager sm = ClusterResourceHolder.getInstance().getSubscriptionManager();
        if (sm instanceof OnceInOrderEnabledSubscriptionManager) {
            this.queueLock = ((OnceInOrderEnabledSubscriptionManager) sm).getQueueLock().get(queue);
        }
    }


    /**
     * Acquire the lock for the queue. This will get blocked till the it get
     * the lock for the queue
     * @throws InterruptedException
     * @throws CoordinationException
     */
    public void acquire() throws InterruptedException, CoordinationException {

        if (!ClusterResourceHolder.getInstance().getClusterConfiguration().isClusteringEnabled()) {
            if(queueLock != null) {
                queueLock.acquire();
            } else {
                throw new CoordinationException("Queue Resource Lock not initialized properly");
            }
        } else {
            try {


                if (zkAgent == null) {
                    synchronized (lock) {
                        if (zkAgent == null) {
                            zkAgent = new ZooKeeperAgent(connectionString);
                            zkAgent.initQueueResourceLockCoordination(queue);
                        }
                    }
                }
                createNode();
                proceed();


            } catch (Exception e) {
                throw new CoordinationException("Error Acquiring Lock ", e);
            }
        }


    }


    /**
     * Release the Lock for the queue
     * @throws CoordinationException
     */
    public void release() throws CoordinationException {
        if (!ClusterResourceHolder.getInstance().getClusterConfiguration().isClusteringEnabled()) {
            if (queueLock != null) {
                queueLock.release();
            } else {
                throw new CoordinationException("Queue Resource Lock not initialized properly");
            }
        }
        {
            try {
                deleteNode();
            } catch (Exception e) {
                throw new CoordinationException("Error while releasing lock", e);
            }
        }

    }


    private void createNode() throws InterruptedException, KeeperException {


        final String nodeName = CoordinationConstants.QUEUE_RESOURCE_LOCK_NODE +
                (UUID.randomUUID()).toString().replace("-", "_");
        this.myNode = nodeName.replace("/", "");
        String path = CoordinationConstants.QUEUE_RESOURCE_LOCK_PARENT
                + "_" + queue + nodeName;
        zkAgent.getZooKeeper().create(path, new byte[0],
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

    }


    private void deleteNode() throws InterruptedException , KeeperException{
        if(zkAgent!=null) {
            String path = CoordinationConstants.QUEUE_RESOURCE_LOCK_PARENT+"_"+queue+
                    CoordinationConstants.NODE_SEPARATOR + myZNode;
            zkAgent.getZooKeeper().delete(path,-1);
        }
    }


    private List<String> getChildren() throws InterruptedException, KeeperException {

        return zkAgent.getZooKeeper().getChildren(CoordinationConstants.
                QUEUE_RESOURCE_LOCK_PARENT + "_" + queue, false);
    }


    private boolean proceed() throws InterruptedException, KeeperException {

        while (true) {
            final Semaphore lock = new Semaphore(1);
            lock.acquire();
            List<String> childNodes = getChildren();
            HashMap<Integer, String> nodeIdMap = new HashMap<Integer, String>();

            String selectedNode = null;
            int currentMin = Integer.MAX_VALUE;
            for (String child : childNodes) {
                String id = child.substring(myNode.length());
                int seqNumber = Integer.parseInt(id);
                if (child.contains(myNode)) {
                    myId = seqNumber;
                    myZNode = child;
                }
                nodeIdMap.put(seqNumber, child);
                if (seqNumber < currentMin) {
                    selectedNode = child;
                    currentMin = seqNumber;
                }
            }

            if (selectedNode.contains(myNode)) {
                log.debug("Lock acquired..");
                break;
            } else {
                int myLockHolder = --myId;
                Stat stat = zkAgent.getZooKeeper().exists(CoordinationConstants.QUEUE_RESOURCE_LOCK_PARENT +
                        "_" + queue + CoordinationConstants.NODE_SEPARATOR +
                        nodeIdMap.get(myLockHolder),
                        new Watcher() {

                            @Override
                            public void process(WatchedEvent watchedEvent) {
                                if (Event.EventType.NodeDeleted == watchedEvent.getType()) {
                                    log.debug("Locked Release Detected.. Trying to acquire lock again..");
                                    lock.release();

                                }
                            }
                        });

                if (stat == null) {
                    log.debug("Locked Release Detected.. Trying to acquire lock again..");
                    continue;
                }

                lock.acquire();
            }


        }

        return true;
    }

    /**
     * Cleanup allocated Zookeeper Resources for this Lock
     */
    public void destroy() throws CoordinationException {

        // We only need to do housekeeping when broker is running in cluster mode.
        if (ClusterResourceHolder.getInstance().getClusterConfiguration().isClusteringEnabled()) {
            try {
                zkAgent.getZooKeeper().close();
            } catch (InterruptedException e) {
                throw new CoordinationException("Error while releasing the Queue Lock ", e);
            } finally {
                zkAgent = null;
            }
        }
    }

}

