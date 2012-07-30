/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.mediator.autoscale.lbautoscale.mediators;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.lb.common.conf.LoadBalancerConfiguration;
import org.wso2.carbon.lb.common.conf.util.HostContext;
import org.wso2.carbon.lb.common.conf.util.TenantDomainContext;
import org.wso2.carbon.mediator.autoscale.lbautoscale.context.AppDomainContext;
import org.wso2.carbon.mediator.autoscale.lbautoscale.util.AutoscaleConstants;
import org.wso2.carbon.mediator.autoscale.lbautoscale.util.AutoscaleUtil;

import java.util.Map;

/**
 * This Synapse mediator generates a token per request received. These tokens are used for tracking
 * the number of requests in flight. Once a response is received, the relevant token will be removed
 * by the {@link AutoscaleOutMediator}
 *
 * @see AutoscaleOutMediator
 */
public class AutoscaleInMediator extends AbstractMediator implements ManagedLifecycle {

    private LoadBalancerConfiguration lbConfig;

    public AutoscaleInMediator(LoadBalancerConfiguration lbconf) {

        this.lbConfig = lbconf;
    }

    public boolean mediate(MessageContext synCtx) {

        if (log.isDebugEnabled()) {
            log.debug("Mediation started .......... " + AutoscaleInMediator.class.getName());

        }

        ConfigurationContext configCtx =
                                         ((Axis2MessageContext) synCtx).getAxis2MessageContext()
                                                                       .getConfigurationContext();
        String uuid = org.apache.axiom.util.UIDGenerator.generateUID();
        synCtx.setProperty(AutoscaleConstants.REQUEST_ID, uuid);

        Map<String, Map<String, AppDomainContext>> appDomainContexts =
                                                                       AutoscaleUtil.getAppDomainContexts(configCtx,
                                                                                                          lbConfig);
        String targetHost = AutoscaleUtil.getTargetHost(synCtx);
        int tenantId = AutoscaleUtil.getTenantId(synCtx.toString());

        String domain = null, subDomain = null;

        HostContext ctxt = lbConfig.getHostContextMap().get(targetHost);

        if (ctxt == null) {
            throwException("Host Context is null for host: " + targetHost);
        }

        TenantDomainContext tenantCtxt = ctxt.getTenantDomainContext(tenantId);

        if (tenantCtxt == null) {

            throwException("Tenant Domain Context is null for host: " + targetHost +
                           " - tenant id: " + tenantId);

        }

        // gets the corresponding domain
        domain = tenantCtxt.getDomain();
        synCtx.setProperty(AutoscaleConstants.TARGET_DOMAIN, domain);

        // gets the corresponding sub domain
        subDomain = tenantCtxt.getSubDomain();
        synCtx.setProperty(AutoscaleConstants.TARGET_SUB_DOMAIN, subDomain);

        // for (TenantDomainRangeContext ctxt : lbConfig.getHostDomainMap().get(targetHost)) {
        //
        // if (ctxt.getTenantDomainContextMap().containsKey(tenantId)) {
        // // gets the corresponding domain
        // domain = ctxt.getClusterDomainFromTenantId(tenantId);
        // synCtx.setProperty(AutoscaleConstants.TARGET_DOMAIN, domain);
        //
        // // gets the corresponding sub domain
        // subDomain = ctxt.getClusterSubDomainFromTenantId(tenantId);
        // synCtx.setProperty(AutoscaleConstants.TARGET_SUB_DOMAIN, subDomain);
        //
        // break;
        // }
        //
        // }

        if (appDomainContexts.get(domain) == null) {
            log.error("AppDomainContext not found for domain " + domain);

        } else {
            AppDomainContext appDomainContext = appDomainContexts.get(domain).get(subDomain);

            if (appDomainContext != null) {
                appDomainContext.addRequestToken(uuid);
                System.setProperty(AutoscaleConstants.IS_TOUCHED, "true");

            } else {
                log.error("AppDomainContext not found for sub domain: " + subDomain +
                          " of domain: " + domain);
            }
        }

        return true;
    }

    @Override
    public void destroy() {

        log.info("Mediator destroyed! " + AutoscaleInMediator.class.getName());
    }

    @Override
    public void init(SynapseEnvironment arg0) {

        if (log.isDebugEnabled()) {
            log.debug("Mediator initialized! " + AutoscaleInMediator.class.getName());
        }
    }
    
    private void throwException(String msg){
        log.error(msg);
        throw new RuntimeException(msg);
    }
}
