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
package org.wso2.carbon.agent.server;

import org.wso2.carbon.agent.server.conf.AgentServerConfiguration;
import org.wso2.carbon.agent.server.datastore.StreamDefinitionStore;
import org.wso2.carbon.agent.server.internal.ThriftAgentServer;
import org.wso2.carbon.agent.server.internal.authentication.AuthenticationHandler;

/**
 * The falconry method that is used to create Agent server
 */
public class AgentServerFactory {
    public ThriftAgentServer createAgentServer(AgentServerConfiguration agentServerConfiguration,
                                               AuthenticationHandler authenticationHandler,StreamDefinitionStore streamDefinitionStore) {
        return new ThriftAgentServer(agentServerConfiguration, authenticationHandler,streamDefinitionStore);
    }

    public ThriftAgentServer createAgentServer(int authenticatorPort, int receiverPort,
                                               AuthenticationHandler authenticationHandler,StreamDefinitionStore streamDefinitionStore) {
        return new ThriftAgentServer(authenticatorPort, receiverPort, authenticationHandler,streamDefinitionStore);
    }


    public ThriftAgentServer createAgentServer(int receiverPort,
                                               AuthenticationHandler authenticationHandler,StreamDefinitionStore streamDefinitionStore) {
        return new ThriftAgentServer(receiverPort, authenticationHandler,streamDefinitionStore);
    }
}
