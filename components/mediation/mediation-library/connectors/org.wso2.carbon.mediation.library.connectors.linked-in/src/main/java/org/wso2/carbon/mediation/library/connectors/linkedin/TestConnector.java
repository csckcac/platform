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
package org.wso2.carbon.mediation.library.connectors.linkedin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.mediation.library.connectors.core.AbstractConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserRealm;

public class TestConnector extends AbstractConnector  {
    private static Log log = LogFactory.getLog(TestConnector.class);
    @Override
    public void connect() throws ConnectException {
        MessageContext msg = getMessageContext();
        if(msg != null){
            log.info("MESSAGE CTXT : [OK]");
        }else{
            log.info("MESSAGE CTXT : [FAILED]");
        }
        SynapseEnvironment synEnv = getSynapseEnvironment();
        if(synEnv != null){
            log.info("SynapseEnvironment : [OK]");
        }else{
            log.info("SynapseEnvironment : [FAILED]");
        }
        UserRegistry confReg = getConfigRegistry();
        if(confReg != null){
            log.info("getConfigRegistry : [OK]");
        }else{
            log.info("getConfigRegistry : [FAILED]");
        }
        UserRegistry govReg = getGovernanceRegistry();
        if(govReg != null){
            log.info("getGovernanceRegistry : [OK]");
        }else{
            log.info("getGovernanceRegistry : [FAILED]");
        }
        UserRealm realm = getUserRealm();
        if(realm != null){
            log.info("getUserRealm : [OK]");
        }else{
            log.info("getUserRealm : [FAILED]");
        }
    }
}
