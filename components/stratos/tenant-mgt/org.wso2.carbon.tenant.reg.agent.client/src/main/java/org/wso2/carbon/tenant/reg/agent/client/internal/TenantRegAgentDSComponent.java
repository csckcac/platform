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
package org.wso2.carbon.tenant.reg.agent.client.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.stratos.common.listeners.TenantActivationListener;
import org.wso2.carbon.tenant.reg.agent.client.util.*;

import java.util.Stack;

/**
 * @scr.component name="org.wso2.carbon.tenant.reg.agent.internal.TenantRegAgentDSComponent" immediate="true"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 */
public class TenantRegAgentDSComponent {

    private static Log log = LogFactory.getLog(TenantRegListenerServer.class);
    private static Stack<ServiceRegistration> registrations = null;

    protected void activate(ComponentContext ctxt) {
        try {
            Util.loadConfig();
        } catch (Exception e) {
            String msg = "Error in loading config.";
            log.error(msg);
        }
        TenantRegListener listener = new TenantRegListener();
        registrations = new Stack<ServiceRegistration>();
        registrations.push(ctxt.getBundleContext().registerService(
                TenantMgtListener.class.getName(), listener, null));
        registrations.push(ctxt.getBundleContext().registerService(
                TenantActivationListener.class.getName(), listener, null));
    }

    protected void deactivate(ComponentContext context) {
        while (!registrations.isEmpty()) {
            registrations.pop().unregister();
        }
        registrations = null;
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        DataHolder.getInstance().setClientConfigContext(contextService.getClientConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        DataHolder.getInstance().setClientConfigContext(null);
    }
}
