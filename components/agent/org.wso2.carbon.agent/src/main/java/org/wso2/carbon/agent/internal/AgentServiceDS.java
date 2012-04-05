/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.agent.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.agent.Agent;
import org.wso2.carbon.agent.internal.utils.AgentBuilder;

/**
 * @scr.component name="agentservice.component" immediate="true"
 */
public class AgentServiceDS {
    private static final Log log = LogFactory.getLog(AgentServiceDS.class);
    private Agent agent;
    private ServiceRegistration agentServerService;

    /**
     * initialize the agent here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            if (null != AgentBuilder.loadAgentConfiguration()) {
                agent = new Agent(AgentBuilder.loadAgentConfiguration());
            } else {
                agent = new Agent();
            }
            agentServerService = context.getBundleContext().
                    registerService(Agent.class.getName(), agent, null);
            log.info("Successfully deployed Agent Client");
        } catch (Throwable e) {
            log.error("Can not create and start Agent ", e);
        }
    }


    protected void deactivate(ComponentContext context) {
        context.getBundleContext().ungetService(agentServerService.getReference());
        agent.shutdown();
        if (log.isDebugEnabled()) {
            log.debug("Successfully stopped agent");
        }
    }

}
