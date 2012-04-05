/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.eventing.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.savan.SavanConstants;
import org.apache.savan.configuration.ConfigurationManager;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.eventing.configuration.RegistryBasedConfigurator;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.osgi.service.component.ComponentContext;

/**
 * @scr.component name="eventing.services" immediate="true"
 * @scr.reference name="registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService"
 *                unbind="unsetRegistryService"
 * @scr.reference name="config.context.service"
 *                interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 *                policy="dynamic" bind="setConfigurationContextService"
 *                unbind="unsetConfigurationContextService"
 */
public class EventingServiceComponent {
    private static Log log = LogFactory.getLog(EventingServiceComponent.class);
    private ConfigurationContext configContext;
    private static Registry registry;

    /**
     * 
     */
    public EventingServiceComponent() {
    }

    /**
     * 
     * @param ctxt
     */
    protected void activate(ComponentContext ctxt) {
        ConfigurationManager manager = null;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Eventing bundle is activated ");
            }
            manager = new ConfigurationManager(new RegistryBasedConfigurator());
            manager.configure();

            configContext.setProperty(SavanConstants.CONFIGURATION_MANAGER, manager);
        } catch (Throwable e) {
            log.error("Error occured while activating Eventing bundle", e);
        }
    }

    /**
     * 
     * @return
     */
    public static Registry getRegistry() {
        return registry;
    }

    /**
     * 
     * @param ctxt
     */
    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Eventing bundle is deactivated");
        }
    }

    /**
     * 
     * @param contextService
     */
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.info("ConfigurationContextService set in Eventing bundle");
        }
        configContext = contextService.getServerConfigContext();
    }

    /**
     * 
     * @param contextService
     */
    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.info("ConfigurationContextService unset in Eventing bundle");
        }
    }

    /**
     * 
     * @param registryService
     */
    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.info("RegistryService set in Eventing bundle");
        }

        try {
            registry = registryService.getConfigSystemRegistry();
        } catch (Throwable e) {
            log.error("Failed to set RegistryService in eventing bundle", e);
        }
    }

    /**
     * 
     * @param registryService
     */
    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.info("RegistryService unset in Eventing bundle");
        }
    }
}
