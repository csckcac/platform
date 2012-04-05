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

package org.wso2.carbon.agent.server.internal.utils;


public class AgentServerConstants {

    public static int NO_OF_WORKER_THREADS = 10;
    public static int EVENT_CAPACITY = 10000;

    public static final String AGENT_SERVER_CONF = "agent-server-config.xml";
    public static final String AGENT_SERVER_CONF_NAMESPACE = "http://wso2.org/carbon/agent";
    public static final String AGENT_SERVER_CONF_ELE_ROOT = "agentServerConfiguration";

    public static final String AUTHENTICATOR_PORT = "authenticatorPort";
    public static final String EVENT_RECEIVER_PORT = "eventReceiverPort";

    public static int THRIFT_DEFAULT_SSL_PORT = 7611;
    public static int THRIFT_DEFAULT_PORT = 7711;
    public static int CARBON_DEFAULT_PORT_OFFSET = 0;
    public static String CARBON_CONFIG_PORT_OFFSET_NODE = "Ports.Offset";
}
