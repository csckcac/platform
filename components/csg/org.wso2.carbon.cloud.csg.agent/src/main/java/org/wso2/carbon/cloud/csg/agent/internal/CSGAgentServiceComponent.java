/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cloud.csg.agent.internal;


import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cloud.csg.agent.observer.CSGServiceObserver;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.cloud.csg.agent.internal.CSGAgentServiceComponent" immediate="true"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 */

@SuppressWarnings({"UnusedDeclaration"})
public class CSGAgentServiceComponent {

    private static Log log = LogFactory.getLog(CSGAgentServiceComponent.class);

    private ConfigurationContextService configurationContextService;

    protected void activate(ComponentContext context) {
        if (this.configurationContextService == null) {
            log.error("Cloud not activated the CSGAgentServiceComponent. ConfigurationContextService is null!");
            return;
        }
        AxisConfiguration axisConfig =
                this.configurationContextService.getServerConfigContext().getAxisConfiguration();
        CSGServiceObserver observer = new CSGServiceObserver();
        axisConfig.addObservers(observer);
        if (log.isDebugEnabled()) {
            log.info("Activated the CSGAgentServiceComponent");
        }
    }

    protected void deactivate(ComponentContext context) {

    }

    protected void setConfigurationContextService(ConfigurationContextService configCtxService) {
        this.configurationContextService = configCtxService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        if (this.configurationContextService != null) {
            this.configurationContextService = null;
        }
    }
}
