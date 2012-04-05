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
package org.wso2.carbon.multiple.instance.endpoint.mgt.internal;

import org.osgi.service.component.ComponentContext;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.wso2.carbon.mediation.initializer.services.SynapseConfigurationService;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.multiple.instance.endpoint.mgt.EndPointManager;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * @scr.component name="application.mgt.synapse.dscomponent" immediate="true"
 * @scr.reference name="synapse.config.service" interface="org.wso2.carbon.mediation.initializer.services.SynapseConfigurationService"
 * cardinality="1..1" policy="dynamic" bind="setSynapseConfigurationService" unbind="unsetSynapseConfigurationService"
 * @scr.reference name="synapse.env.service"
 * interface="org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService"
 * cardinality="1..1" policy="dynamic" bind="setSynapseEnvironmentService" unbind="unsetSynapseEnvironmentService"
 */
public class EndPointManagerServiceComponent {
    private static Log log = LogFactory.getLog(EndPointManagerServiceComponent.class);

    private static SynapseConfigurationService scService;
    private static SynapseEnvironmentService synEnvService;

    protected void activate(ComponentContext ctxt) {
        /* We need this code to run only on master, otherwise instances started by the master will
            start trying load balancing between endpoints written in to the file.
         */
        if (!CarbonUtils.isChildNode()) {
            EndPointManager endPointManager = new EndPointManager();
            try {
                endPointManager.start();
            } catch (Exception e) {
                log.error("Error Occure while running Multiple Instance Port updator", e);
            }
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivated SynapseAppServiceComponent");
        }
    }

    protected void setSynapseConfigurationService(
            SynapseConfigurationService synapseConfigurationService) {
        scService = synapseConfigurationService;
    }

    protected void unsetSynapseConfigurationService(
            SynapseConfigurationService synapseConfigurationService) {
        scService = null;
    }

    protected void setSynapseEnvironmentService(SynapseEnvironmentService synEnvironmentService) {
        synEnvService = synEnvironmentService;
    }

    protected void unsetSynapseEnvironmentService(SynapseEnvironmentService synEnvironmentService) {
        synEnvService = null;
    }

    public static SynapseConfigurationService getScService() throws Exception {
        return scService;
    }

    public static SynapseEnvironmentService getSynapseEnvService() throws Exception {
        return synEnvService;
    }

}
