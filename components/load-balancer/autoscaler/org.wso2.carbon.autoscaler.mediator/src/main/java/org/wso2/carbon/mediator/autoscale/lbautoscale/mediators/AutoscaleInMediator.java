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
package org.wso2.carbon.mediator.autoscale.lbautoscale.mediators;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.mediator.autoscale.lbautoscale.AppDomainContext;
import org.wso2.carbon.mediator.autoscale.lbautoscale.AutoscaleConstants;
import org.wso2.carbon.mediator.autoscale.lbautoscale.AutoscaleUtil;
import org.wso2.carbon.mediator.autoscale.lbautoscale.EC2LoadBalancerConfiguration;

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
    private EC2LoadBalancerConfiguration ec2LBConfig;

    public void setEc2LBConfig(EC2LoadBalancerConfiguration ec2LBConfig) {
        this.ec2LBConfig = ec2LBConfig;
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
                                                                                             ec2LBConfig);
        String targetHost = AutoscaleUtil.getTargetHost(synCtx);
        String domain = ec2LBConfig.getDomain(targetHost);
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
