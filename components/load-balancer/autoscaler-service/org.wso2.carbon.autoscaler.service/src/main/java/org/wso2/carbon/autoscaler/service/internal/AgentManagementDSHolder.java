/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.autoscaler.service.internal;

import org.wso2.carbon.autoscaler.service.jvm.agentmgt.IAgentManagementService;

/**
 * Singleton class to hold Agent Management Service
 */
public class AgentManagementDSHolder {
    
    private IAgentManagementService agentMgtService;

    private static AgentManagementDSHolder instance = new AgentManagementDSHolder();

    private AgentManagementDSHolder(){

    }

    public static AgentManagementDSHolder getInstance(){
        return instance;
    }

    public IAgentManagementService getAgentMgtService(){
        return this.agentMgtService;
    }

    public void registerAgentMgtService(IAgentManagementService agentMgtService){
        this.agentMgtService = agentMgtService;
    }

     public void unRegisterAgentMgtService(IAgentManagementService cepService){
        this.agentMgtService = null;
    }


}
