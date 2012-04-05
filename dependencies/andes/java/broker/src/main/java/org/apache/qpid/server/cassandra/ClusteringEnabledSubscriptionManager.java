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
package org.apache.qpid.server.cassandra;


import org.apache.qpid.server.AMQChannel;
import org.apache.qpid.server.queue.AMQQueue;

import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * <code>ClusteringEnabledSubscriptionManager</code> Manage the Queue Subscriptions Handling
 * Scenarios. This can have many implementation based on the level of
 * 1) Consistency
 * 2) Performance
 * etc..
 * expected from the Broker
 */
public interface ClusteringEnabledSubscriptionManager {


    /**
     * initialize the Subscription manager
     */
    public void init();

    /**
     * Handle the Subscription addition for a queue
     * @param queue AMQQueue object that client subscribing
     * @param subscription  Subscription for a Queue
     */
    public void addSubscription(AMQQueue queue ,CassandraSubscription subscription);

    /**
     * Handle Subscription removal
     * @param queueName
     * @param subscriptionId
     */
    public void removeSubscription(String queueName , String subscriptionId);

    /**
     * Get the Map that keeps the Locks for Un acknowledged messages
     * This Api is provided either to add the message Lock and wait on that lock
     * or get a lock and release that lock in case of a message acknowledgement
     * @return UnAcknowledged message lock map
     */
    public Map<AMQChannel,Map<Long, Semaphore>> getUnAcknowledgedMessageLocks();

}
