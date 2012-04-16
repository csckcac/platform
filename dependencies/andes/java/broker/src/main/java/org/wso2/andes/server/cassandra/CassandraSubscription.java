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

import org.wso2.andes.server.protocol.AMQProtocolSession;
import org.wso2.andes.server.subscription.Subscription;

/**
 * <code>CassandraSubscription</code> represents the Subscription made by a client for a given queue.
 * This is used in Clustering implementation  to represent a  subscription
 */
public class CassandraSubscription {

    /**
     * AMQP subscription object which can be using to send out the message
     */
    private Subscription subscription;


    /**
     * AMQP protocol session for this client subscription
     */
    private AMQProtocolSession session;


    /**
     * Global queue that this subscription is made for
     */
    private String queue;


    /**
     * Create a Cassandra Subscription representation
     * @param subscription AMQP subscription object which can be using to send out the message
     * @param session AMQP protocol session for this client subscription
     * @param queue  Global queue that this subscription is made for
     */
    public CassandraSubscription(Subscription subscription , AMQProtocolSession session , String queue) {
        this.session = session;
        this.subscription = subscription;
        this.queue =  queue;
    }

    /**
     * Get the AMQP subscription object
     * @return AMQP subscription object
     */
    public Subscription getSubscription() {
        return subscription;
    }

    /**
     * Get AMQP protocol session for this client subscription
     * @return AMQP protocol session for this client subscription
     */
    public AMQProtocolSession getSession() {
        return session;
    }

    /**
     * Get Global queue that this subscription is made for
     * @return  Global queue that this subscription is made for
     */
    public String getQueue() {
        return queue;
    }
}
