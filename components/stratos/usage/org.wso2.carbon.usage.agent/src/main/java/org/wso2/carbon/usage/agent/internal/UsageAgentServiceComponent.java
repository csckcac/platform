/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.usage.agent.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.usage.agent.util.PublisherUtils;
import org.wso2.carbon.usage.agent.util.Util;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * this class is used to activate and deactivate the UsageAgentServiceComponent, set and unset
 * Serverconfigurarion, set and unset EventBrokerService.
 *
 * @scr.component name="org.wso2.carbon.usage.agent" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="0..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="server.configuration"
 * interface="org.wso2.carbon.base.api.ServerConfigurationService" cardinality="1..1"
 * policy="dynamic" bind="setServerConfiguration" unbind="unsetServerConfiguration"
 * @scr.reference name="eventbroker.service"
 * interface="org.wso2.carbon.event.core.EventBroker" cardinality="1..1"
 * policy="dynamic" bind="setEventBrokerService" unbind="unsetEventBrokerService"
 */
public class UsageAgentServiceComponent {
    private static Log log = LogFactory.getLog(UsageAgentServiceComponent.class);

    /**
     * method to activate UsageAgentServiceComponent
     *
     * @param context componentContext
     */
    protected void activate(ComponentContext context) {
        try {
            SuperTenantCarbonContext.startTenantFlow();
            SuperTenantCarbonContext.getCurrentContext().setTenantId(
                    MultitenantConstants.SUPER_TENANT_ID);
            SuperTenantCarbonContext.getCurrentContext().getTenantDomain(true);
            SuperTenantCarbonContext.getCurrentContext().setUsername(
                    CarbonConstants.REGISTRY_SYSTEM_USERNAME);
            SuperTenantCarbonContext.getCurrentContext().setUserRealm(
                    Util.getRealmService().getBootstrapRealm());
            
            // initialize listeners
            // currently we have RegistryUsagePersistingListener only
            Util.initializeAllListeners();

            //initialize persistenceManager for persisting BandwidthUsage
            Util.initializePersistenceManager();

            // create statistic event subscription
            Util.createStaticEventSubscription();

            // register the request data persistor osgi service so that we can
            // store service request bandwidths
            // TODO: Remove this and ServiceDataPersistor after fixing ESB metering
//            context.getBundleContext().registerService(
//                    RequestDataPersister.class.getName(), new ServiceDataPersistor(), null);

            log.debug("******* Multitenancy Usage Agent bundle is activated ******* ");
        } catch (Throwable e) {
            log.error("******* Failed to activate Multitenancy Usage Agent bundle ****", e);
        } finally {
            SuperTenantCarbonContext.endTenantFlow();
        }
    }

    /**
     * method to deactivate bundle
     *
     * @param context ComponentContext
     */
    protected void deactivate(ComponentContext context) {
        log.debug("******* Multitenancy Metering Usage Agent bundle is deactivated ******* ");
    }

    /**
     * method to set RealmService
     *
     * @param realmService RealmService
     */
    protected void setRealmService(RealmService realmService) {
        Util.setRealmService(realmService);
    }

    /**
     * method to unsetRealmService
     *
     * @param realmService RealmService
     */

    protected void unsetRealmService(RealmService realmService) {
        Util.setRealmService(null);
    }

    /**
     * method to set ConfigurationContextService
     *
     * @param contextService ConfigurationContextService
     */
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        Util.setConfigurationContextService(contextService);
        PublisherUtils.setConfigurationContextService(contextService);
        try {
            contextService.getServerConfigContext().getAxisConfiguration().engageModule("metering");
        } catch (AxisFault e) {
            log.error("Failed to engage request metering Module", e);
        }
    }

    /**
     * method to unset ConfigurationContextService
     *
     * @param contextService ConfigurationContextService
     */
    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        try {
            AxisConfiguration axisConfig =
                    contextService.getServerConfigContext().getAxisConfiguration();
            axisConfig.disengageModule(axisConfig.getModule("metering"));
        } catch (AxisFault e) {
            log.error("Failed to disengage request metering Module", e);
        }
        Util.setConfigurationContextService(null);
    }

    /**
     * method to set server configurations
     *
     * @param serverConfiguration ServerConfiguration
     */
    protected void setServerConfiguration(ServerConfigurationService serverConfiguration) {
        Util.setServerConfiguration(serverConfiguration);
    }

    /**
     * method to unset server configurations
     *
     * @param serverConfiguration ServerConfiguration
     */
    protected void unsetServerConfiguration(ServerConfigurationService serverConfiguration) {
        Util.setServerConfiguration(null);
    }

    /**
     * method to set EventBrokerService
     *
     * @param registryEventBrokerService EventBroker
     */

    protected void setEventBrokerService(EventBroker registryEventBrokerService) {
        Util.setEventBrokerService(registryEventBrokerService);
    }

    /**
     * method to unset EventBrokerService
     *
     * @param registryEventBrokerService EventBroker
     */
    protected void unsetEventBrokerService(EventBroker registryEventBrokerService) {
        Util.setEventBrokerService(null);
    }

}
