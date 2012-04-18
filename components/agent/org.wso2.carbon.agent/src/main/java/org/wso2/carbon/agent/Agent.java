/**
 *
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.agent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.wso2.carbon.agent.conf.AgentConfiguration;
import org.wso2.carbon.agent.internal.pool.BoundedExecutor;
import org.wso2.carbon.agent.internal.pool.authenticator.AuthenticatorClientPoolFactory;
import org.wso2.carbon.agent.internal.pool.client.ClientPool;
import org.wso2.carbon.agent.internal.pool.client.ClientPoolFactory;
import org.wso2.carbon.agent.internal.publisher.AgentAuthenticator;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Agent who connects to the AgentServer and Sends Events
 * There has to be one Agent server in a JVM since else it will use all resources of the OS
 */
public class Agent {

    private static Log log = LogFactory.getLog(Agent.class);

    private AgentConfiguration agentConfiguration;
    private GenericKeyedObjectPool transportPool;
    private Semaphore queueSemaphore;
    private AgentAuthenticator agentAuthenticator;
    private List<DataPublisher> dataPublisherList;
    private BoundedExecutor threadPool;
    private long keepAliveTime = 20;


    public Agent() {
        this(new AgentConfiguration());
    }

    public Agent(AgentConfiguration agentConfiguration) {
        this.agentConfiguration = agentConfiguration;
        this.transportPool = new ClientPool().getClientPool(
                new ClientPoolFactory(), agentConfiguration.getMaxPoolSize(),
                agentConfiguration.getMaxIdleConnections(), true, agentConfiguration.getEvictionTimePeriod(),
                agentConfiguration.getMinIdleTimeInPool());
        this.agentAuthenticator = new AgentAuthenticator(
                new AuthenticatorClientPoolFactory(agentConfiguration.getTrustStore(), agentConfiguration.getTrustStorePassword()), agentConfiguration.getAuthenticatorMaxPoolSize(),
                agentConfiguration.getAuthenticatorMaxIdleConnections(), true, agentConfiguration.getEvictionTimePeriod(),
                agentConfiguration.getMinIdleTimeInPool());
        this.dataPublisherList = new LinkedList<DataPublisher>();
        this.queueSemaphore = new Semaphore(agentConfiguration.getTaskQueueSize());
        this.threadPool = new BoundedExecutor(
                new ThreadPoolExecutor(agentConfiguration.getCorePoolSize(),
                                       agentConfiguration.getMaxPoolSize(),
                                       keepAliveTime, TimeUnit.SECONDS,
                                       new ArrayBlockingQueue<Runnable>(
                                               agentConfiguration.getTaskQueueSize()
                                       )),
                agentConfiguration.getTaskQueueSize());
    }

    void addDataPublisher(DataPublisher dataPublisher) {
        dataPublisherList.add(dataPublisher);
    }

    void removeDataPublisher(DataPublisher dataPublisher) {
        dataPublisherList.remove(dataPublisher);
    }

    /**
     * To shutdown Agent and DataPublishers
     */
    void shutdown(DataPublisher dataPublisher) {
        removeDataPublisher(dataPublisher);
        if (dataPublisherList.size() == 0) {
            shutdown();
        }
    }

    /**
     * To shutdown Agent
     */
    public void shutdown() {
        try {
            transportPool.close();
            threadPool.shutdown();
        } catch (Exception e) {
            log.warn("Agent shutdown failed");
        }
    }

    AgentConfiguration getAgentConfiguration() {
        return agentConfiguration;
    }

    GenericKeyedObjectPool getTransportPool() {
        return transportPool;
    }

    Semaphore getQueueSemaphore() {
        return queueSemaphore;
    }

    AgentAuthenticator getAgentAuthenticator() {
        return agentAuthenticator;
    }

    List<DataPublisher> getDataPublisherList() {
        return dataPublisherList;
    }

    BoundedExecutor getThreadPool() {
        return threadPool;
    }
}
