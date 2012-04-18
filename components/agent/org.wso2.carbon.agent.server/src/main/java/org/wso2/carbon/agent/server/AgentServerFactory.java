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
import org.wso2.carbon.agent.server.internal.CarbonAgentServer;
import org.wso2.carbon.agent.server.internal.authentication.AuthenticationHandler;

public class AgentServerFactory {
    public CarbonAgentServer createAgentServer(AgentServerConfiguration agentServerConfiguration,
                                               AuthenticationHandler authenticationHandler,StreamDefinitionStore streamDefinitionStore) {
        return new CarbonAgentServer(agentServerConfiguration, authenticationHandler,streamDefinitionStore);
    }

    public CarbonAgentServer createAgentServer(int authenticatorPort, int receiverPort,
                                               AuthenticationHandler authenticationHandler,StreamDefinitionStore streamDefinitionStore) {
        return new CarbonAgentServer(authenticatorPort, receiverPort, authenticationHandler,streamDefinitionStore);
    }


    public CarbonAgentServer createAgentServer(int receiverPort,
                                               AuthenticationHandler authenticationHandler,StreamDefinitionStore streamDefinitionStore) {
        return new CarbonAgentServer(receiverPort, authenticationHandler,streamDefinitionStore);
    }
}
