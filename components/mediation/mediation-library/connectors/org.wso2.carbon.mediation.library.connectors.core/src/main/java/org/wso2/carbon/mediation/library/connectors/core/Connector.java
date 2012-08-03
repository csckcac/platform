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
package org.wso2.carbon.mediation.library.connectors.core;

import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserRealm;

public interface Connector extends Mediator{
    /**
     * implements the connection logic to external API or custom service pattern
     * @throws ConnectException when error during conenction to APIs or unexpected errors
     */
    public void connect() throws ConnectException;

    UserRegistry getConfigRegistry() throws ConnectException;

    UserRegistry getGovernanceRegistry() throws ConnectException;

    UserRealm getUserRealm() throws ConnectException;

    SynapseEnvironment getSynapseEnvironment() throws ConnectException;

}
