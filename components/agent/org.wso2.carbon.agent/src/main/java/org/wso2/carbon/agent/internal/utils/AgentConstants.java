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


package org.wso2.carbon.agent.internal.utils;

/**
 * The Agent Constants
 */
public final class AgentConstants {


    private AgentConstants() { }

    public static final String HOSTNAME_AND_PORT_SEPARATOR = ":";
    public static final String ENDPOINT_SEPARATOR = ",";

    public static final String AGENT_CONF = "agent-config.xml";
    public static final String AGENT_CONF_NAMESPACE = "http://wso2.org/carbon/agent";
    public static final String AGENT_CONF_ELE_ROOT = "agentConfiguration";

    public static final String BUFFERED_EVENTS_SIZE = "bufferedEventsSize";
    public static final String POOL_SIZE = "poolSize";

    public static final String MAX_TRANSPORT_POOL_SIZE = "maxTransportPoolSize";
    public static final String MAX_IDLE_CONNECTIONS = "maxIdleConnections";
    public static final String EVICTION_TIME_PERIOD = "evictionTimePeriod";
    public static final String MIN_IDLE_TIME_IN_POOL = "minIdleTimeInPool";

    public static final String SECURE_MAX_TRANSPORT_POOL_SIZE = "secureMaxTransportPoolSize";
    public static final String SECURE_MAX_IDLE_CONNECTIONS = "secureMaxIdleConnections";
    public static final String SECURE_EVICTION_TIME_PERIOD = "secureEvictionTimePeriod";
    public static final String SECURE_MIN_IDLE_TIME_IN_POOL = "secureMinIdleTimeInPool";

    public static final String MAX_MESSAGE_BUNDLE_SIZE = "maxMessageBundleSize";

    public static final String THRUST_STORE = "trustStore";
    public static final String THRUST_STORE_PASSWORD = "trustStorePassword";

    public static final String DEFAULT_DEFINITION_STORE = "org.wso2.carbon.agent.server.datastore.InMemoryStreamDefinitionStore";
//    public static final String DEFAULT_DEFINITION_STORE = "org.wso2.carbon.bam.eventreceiver.datastore.CassandraStreamDefinitionStore";
    public static final int AGENT_RECONNECTION_TIMES = 3;
    public static final int SECURE_EVENT_RECEIVER_PORT_OFFSET = 100;
    public static final int DEFAULT_RECEIVER_PORT = 7611;

    public static final long DEFAULT_KEEP_ALIVE_TIME = 20;

    //AgentConfiguration
    public static final int DEFAULT_BUFFERED_EVENTS_SIZE = 200;
    public static final int DEFAULT_POOL_SIZE = 30;

    public static final int DEFAULT_MAX_TRANSPORT_POOL_SIZE = 250;
    public static final int DEFAULT_MAX_IDLE_CONNECTIONS = 250;
    public static final long DEFAULT_EVICTION_IDLE_TIME_IN_POOL = 5500;
    public static final long DEFAULT_MIN_IDLE_TIME_IN_POOL = 5000;

    public static final int DEFAULT_MAX_MESSAGE_BUNDLE_SIZE = 100;

    public static final int DEFAULT_SECURE_MAX_TRANSPORT_POOL_SIZE = 250;
    public static final int DEFAULT_SECURE_MAX_IDLE_CONNECTIONS = 250;
    public static final long DEFAULT_SECURE_EVICTION_IDLE_TIME_IN_POOL = 5500;
    public static final long DEFAULT_SECURE_MIN_IDLE_TIME_IN_POOL = 5000;
}
