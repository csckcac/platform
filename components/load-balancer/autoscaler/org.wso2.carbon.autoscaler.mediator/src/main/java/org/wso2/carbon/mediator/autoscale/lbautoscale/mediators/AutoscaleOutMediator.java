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

import java.util.Map;

/**
 * This Synapse mediator removes the the request token geenrated by the {@link AutoscaleInMediator}
 *
 * @see AutoscaleInMediator
 */
public class AutoscaleOutMediator extends AbstractMediator {

    public boolean mediate(MessageContext synCtx) {
        ConfigurationContext configCtx =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext().getConfigurationContext();
        String domain = (String) synCtx.getProperty(AutoscaleConstants.TARGET_DOMAIN);
        String tokenId = (String) synCtx.getProperty(AutoscaleConstants.REQUEST_ID);
        Map<String, AppDomainContext> appDomainContexts =
                (Map<String, AppDomainContext>) configCtx.getPropertyNonReplicable(AutoscaleConstants.APP_DOMAIN_CONTEXTS);
        AppDomainContext appDomainContext = appDomainContexts.get(domain);
        if (appDomainContext != null) {
            appDomainContext.removeRequestToken(tokenId);
        } else {
            log.error("AppDomainContext not found for domain " + domain);
        }
        return true;
    }
}
