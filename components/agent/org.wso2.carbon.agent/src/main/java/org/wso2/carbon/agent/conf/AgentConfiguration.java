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


package org.wso2.carbon.agent.conf;

import org.wso2.carbon.agent.internal.utils.AgentConstants;

/**
 * Configuration details of Agent
 */
public class AgentConfiguration {

    private int taskQueueSize = AgentConstants.DEFAULT_TASK_QUEUE_SIZE;
    private int corePoolSize = AgentConstants.DEFAULT_CORE_POOL_SIZE;
    private int maxPoolSize = AgentConstants.DEFAULT_MAX_POOL_SIZE ;

    private int maxIdleConnections = AgentConstants.DEFAULT_MAX_IDLE_CONNECTIONS ;
    private long evictionTimePeriod = AgentConstants.DEFAULT_EVICTION_IDLE_TIME_IN_POOL ;
    private long minIdleTimeInPool = AgentConstants.DEFAULT_MIN_IDLE_TIME_IN_POOL ;

    private int maxMessageBundleSize = AgentConstants.DEFAULT_MAX_MESSAGE_BUNDLE_SIZE ;

    private int authenticatorMaxPoolSize = AgentConstants.DEFAULT_AUTHENTICATOR_MAX_POOL_SIZE ;
    private int authenticatorMaxIdleConnections = AgentConstants.DEFAULT_AUTHENTICATOR_MAX_IDLE_CONNECTIONS;

    private String trustStore=null;
    private String trustStorePassword=null;


    public int getMaxMessageBundleSize() {
        return maxMessageBundleSize;
    }

    public void setMaxMessageBundleSize(int maxMessageBundleSize) {
        this.maxMessageBundleSize = maxMessageBundleSize;
    }

    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    public void setMaxIdleConnections(int maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }

    public long getEvictionTimePeriod() {
        return evictionTimePeriod;
    }

    public void setEvictionTimePeriod(long evictionTimePeriod) {
        this.evictionTimePeriod = evictionTimePeriod;
    }

    public long getMinIdleTimeInPool() {
        return minIdleTimeInPool;
    }

    public void setMinIdleTimeInPool(long minIdleTimeInPool) {
        this.minIdleTimeInPool = minIdleTimeInPool;
    }

    public int getTaskQueueSize() {
        return taskQueueSize;
    }

    public void setTaskQueueSize(int taskQueueSize) {
        this.taskQueueSize = taskQueueSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getAuthenticatorMaxPoolSize() {
        return authenticatorMaxPoolSize;
    }

    public void setAuthenticatorMaxPoolSize(int authenticatorMaxPoolSize) {
        this.authenticatorMaxPoolSize = authenticatorMaxPoolSize;
    }

    public int getAuthenticatorMaxIdleConnections() {
        return authenticatorMaxIdleConnections;
    }

    public void setAuthenticatorMaxIdleConnections(int authenticatorMaxIdleConnections) {
        this.authenticatorMaxIdleConnections = authenticatorMaxIdleConnections;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }
}
