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
package org.wso2.carbon.agent.server.conf;

import org.wso2.carbon.agent.internal.utils.AgentConstants;

/**
 * configuration details related to AgentServer
 */
public class AgentServerConfiguration {
    private int secureEventReceiverPort = AgentConstants.DEFAULT_RECEIVER_PORT + AgentConstants.SECURE_EVENT_RECEIVER_PORT_OFFSET;
    private int eventReceiverPort = AgentConstants.DEFAULT_RECEIVER_PORT;

    public AgentServerConfiguration(int defaultSslPort, int defaultPort) {
        secureEventReceiverPort = defaultSslPort;
        eventReceiverPort = defaultPort;
    }

    public int getEventReceiverPort() {
        return eventReceiverPort;
    }

    public void setEventReceiverPort(int eventReceiverPort) {
        this.eventReceiverPort = eventReceiverPort;
    }

    public int getSecureEventReceiverPort() {
        return secureEventReceiverPort;
    }

    public void setSecureEventReceiverPort(int secureEventReceiverPort) {
        this.secureEventReceiverPort = secureEventReceiverPort;
    }
}
