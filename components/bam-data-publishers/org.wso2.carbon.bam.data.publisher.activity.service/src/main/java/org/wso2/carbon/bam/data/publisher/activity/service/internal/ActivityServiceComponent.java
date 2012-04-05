/*
 * Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.wso2.carbon.bam.data.publisher.activity.service.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.activity.service.PublisherUtils;
import org.wso2.carbon.bam.data.publisher.activity.service.config.RegistryPersistanceManager;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBrokerInterface;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBroker;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.base.ServerConfiguration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bam.data.publisher.activity.service.ActivityPublisherConstants;
import org.wso2.carbon.bam.data.publisher.activity.service.services.ActivityPublisherAdmin;

/**
 * @scr.component name="org.wso2.carbon.bam.data.publisher.activity.service" immediate="true"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="org.wso2.carbon.util.server"
 * interface="org.wso2.carbon.base.ServerConfiguration" cardinality="1..1"
 * policy="dynamic" bind="setServerConfiguration" unbind="unsetServerConfiguration"
 * @scr.reference name="org.wso2.carbon.registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="org.wso2.carbon.bam.lwevent.core"
 * interface="org.wso2.carbon.bam.lwevent.core.LightWeightEventBrokerInterface"
 * cardinality="1..1" policy="dynamic" bind="setLWEventBroker"
 * unbind="unsetLWEventBroker"
 */

public class ActivityServiceComponent {

    private static Log log = LogFactory.getLog(ActivityServiceComponent.class);

    private static LightWeightEventBrokerInterface lightWeightEventBroker;

    protected void activate(ComponentContext ctxt) {
        try {

            // Engaging Module as a global module,
            PublisherUtils.getConfigurationContext().getAxisConfiguration()
                    .engageModule(ActivityPublisherConstants.BAM_ACTIVITY_PUBLISHER_MODULE_NAME);
            ActivityPublisherAdmin admin = new ActivityPublisherAdmin();
            PublisherUtils.setActivityPublisherAdmin(admin);
            if (log.isDebugEnabled()) {
                log.debug("BAM Activity publisher bundle is activated");
            }

        } catch (Throwable e) {
            log.error("Failed to activate BAM Activity publisher bundle", e);
        }
    }

    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("BAM Activity publisher bundle is deactivated");
        }
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        log.debug("The Configuration Context Service is set");
        PublisherUtils
                .setConfigurationContext(configurationContextService.getServerConfigContext());

    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        PublisherUtils.setConfigurationContext(null);
    }

    public ServerConfiguration getServerConfiguration() {
        return PublisherUtils.getServerConfiguration();
    }

    protected void setServerConfiguration(ServerConfiguration serverConfiguration) {
        PublisherUtils.setServerConfiguration(serverConfiguration);
    }

    protected void unsetServerConfiguration(ServerConfiguration serverConfiguration) {
        PublisherUtils.setServerConfiguration(null);
    }

    protected void setRegistryService(RegistryService registryService) {
        try {
            RegistryPersistanceManager.setRegistry(registryService.getConfigSystemRegistry());
        } catch (Exception e) {
            log.error("Cannot retrieve System Registry", e);
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
        RegistryPersistanceManager.setRegistry(null);
    }

    protected void setLWEventBroker(LightWeightEventBrokerInterface lightWeightEventBroker) {
        this.lightWeightEventBroker = lightWeightEventBroker;
    }

    protected void unsetLWEventBroker(LightWeightEventBrokerInterface lightWeightEventBroker) {
        this.lightWeightEventBroker = null;
    }

}
