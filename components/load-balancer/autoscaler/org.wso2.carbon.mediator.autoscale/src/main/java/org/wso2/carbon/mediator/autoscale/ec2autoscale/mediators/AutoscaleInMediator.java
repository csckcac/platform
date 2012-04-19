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
package org.wso2.carbon.mediator.autoscale.ec2autoscale.mediators;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.lb.common.conf.LoadBalancerConfiguration;
import org.wso2.carbon.mediator.autoscale.ec2autoscale.AutoscaleConstants;
import org.wso2.carbon.mediator.autoscale.ec2autoscale.AutoscaleUtil;
import org.wso2.carbon.mediator.autoscale.ec2autoscale.context.AppDomainContext;

import java.util.Map;

/**
 * This Synapse mediator generates a token per request received. These tokens are used for tracking
 * the number of requests in flight. Once a response is received, the relevant token will be removed
 * by the {@link AutoscaleOutMediator}
 *
 * @see AutoscaleOutMediator
 */
public class AutoscaleInMediator extends AbstractMediator {

    private String configuration;
    private LoadBalancerConfiguration LBConfig;

    public void setLBConfig(LoadBalancerConfiguration LBConfig) {
        this.LBConfig = LBConfig;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public boolean mediate(MessageContext synCtx) {
        ConfigurationContext configCtx =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext().getConfigurationContext();
        String uuid = org.apache.axiom.util.UIDGenerator.generateUID();
        synCtx.setProperty(AutoscaleConstants.REQUEST_ID, uuid);

        Map<String, AppDomainContext> appDomainContexts = AutoscaleUtil.getAppDomainContexts(configCtx,
                LBConfig);
        String targetHost = AutoscaleUtil.getTargetHost(synCtx);
        int tenantId;
        //TODO tenant aware fix should go here, and following line is commented thus far
        String domain = "";//LBConfig.getDomain(targetHost, tenantId);
        synCtx.setProperty(AutoscaleConstants.TARGET_DOMAIN, domain);
        AppDomainContext appDomainContext = appDomainContexts.get(domain);
        if (appDomainContext != null) {
            appDomainContext.addRequestToken(uuid);
        } else {
            log.error("AppDomainContext not found for domain " + domain);
        }

        return true;
    }
}
