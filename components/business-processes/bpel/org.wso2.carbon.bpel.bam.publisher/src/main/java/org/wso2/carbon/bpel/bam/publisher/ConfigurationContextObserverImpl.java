/*
 * Copyright (c) 20012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.bam.publisher;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.agent.DataPublisher;
import org.wso2.carbon.bpel.bam.publisher.internal.BamPublisherServiceComponent;
import org.wso2.carbon.bpel.bam.publisher.skeleton.BamServerInformation;
import org.wso2.carbon.bpel.bam.publisher.util.BamPublisherUtils;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;


public class ConfigurationContextObserverImpl extends AbstractAxis2ConfigurationContextObserver {

    private static Log log = LogFactory.getLog(ConfigurationContextObserverImpl.class);


    public ConfigurationContextObserverImpl(){
        // To handle supper tenant scenario. createdConfigurationContext() does not invoked for the
        // supper tenant.
        createEventReceiverFromRegistry();
    }


    @Override
    public void createdConfigurationContext(ConfigurationContext configContext) {
         createEventReceiverFromRegistry();
    }

    private void createEventReceiverFromRegistry(){
        int tenantId = SuperTenantCarbonContext.getCurrentContext().getTenantId();
        try {

            UserRegistry configSystemRegistry = BamPublisherServiceComponent.getRegistryService().
                    getConfigSystemRegistry(tenantId);
            BamServerInformation bamServerDataFromRegistry = BamPublisherUtils.
                    getBamServerDataFromRegistry(configSystemRegistry, tenantId);
            if (null != bamServerDataFromRegistry) {
                DataPublisher dataPublisher = TenantBamAgentHolder.getInstance().getDataPublisher(tenantId);
                if(null != dataPublisher) {
                    BamPublisherUtils.configureBamDataPublisher(dataPublisher, bamServerDataFromRegistry);
                }else {
                    dataPublisher = BamPublisherUtils.createBamDataPublisher(bamServerDataFromRegistry);
                    TenantBamAgentHolder.getInstance().addDataPublisher(tenantId, dataPublisher);
                }
            }
        } catch (RegistryException e) {
            String msg = "Error obtaining the registry service for tenant id " + tenantId;
            log.error(msg, e);
        }
    }
    @Override
    public void terminatingConfigurationContext(ConfigurationContext configCtx) {
        int tenantId = SuperTenantCarbonContext.getCurrentContext().getTenantId();
        TenantBamAgentHolder.getInstance().removeDataPublisher(tenantId);
    }
}
