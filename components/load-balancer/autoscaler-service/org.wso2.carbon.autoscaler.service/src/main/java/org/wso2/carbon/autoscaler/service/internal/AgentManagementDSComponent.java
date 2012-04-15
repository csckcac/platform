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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.autoscaler.service.jvm.agentmgt.IAgentManagementService;

/**
 * OSGi declarative service component to acquire <i>Agent Management Service</i>.
 * 
 * @scr.component name="org.wso2.carbon.autoscaler.service.internal.AgentManagementDSComponent"
 *                immediate="true"
 * @scr.reference name="org.wso2.carbon.autoscaler.service.jvm.agentmgt"
 *                interface="org.wso2.carbon.autoscaler.service.jvm.agentmgt.IAgentManagementService"
 *                cardinality="1..1" bind="setAgentManagementService"
 *                unbind="unsetAgentManagementService"
 *                policy="dynamic"
 */
public class AgentManagementDSComponent {
    
    private static final Log log = LogFactory.getLog(AgentManagementDSComponent.class);

    protected void setAgentManagementService(IAgentManagementService agentMgt) {
        AgentManagementDSHolder.getInstance().registerAgentMgtService(agentMgt);
        
        if(log.isDebugEnabled()){
            log.debug("Agent Management Service ("+agentMgt+") is being set.");
        }
        
    }

    protected void unsetAgentManagementService(IAgentManagementService agentMgt) {
        AgentManagementDSHolder.getInstance().unRegisterAgentMgtService(agentMgt);
        
        if(log.isDebugEnabled()){
            log.debug("Agent Management Service ("+agentMgt+") is being unset.");
        }
    }

}
