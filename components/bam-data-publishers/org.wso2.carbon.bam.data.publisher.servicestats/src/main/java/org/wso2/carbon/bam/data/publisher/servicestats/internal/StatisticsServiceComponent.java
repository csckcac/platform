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

package org.wso2.carbon.bam.data.publisher.servicestats.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.bam.data.publisher.servicestats.*;
import org.wso2.carbon.bam.data.publisher.servicestats.config.RegistryPersistenceManager;
import org.wso2.carbon.bam.data.publisher.servicestats.services.ServiceStatisticsPublisherAdmin;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBrokerInterface;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.core.util.EventBrokerConstants;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBroker;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.statistics.services.SystemStatisticsUtil;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;



/**
 * @scr.component name="org.wso2.carbon.bam.data.publisher.servicestats" immediate="true"
 *
 * @scr.reference name="realm.service"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" bind="setRealmService"
 *                unbind="unsetRealmService"
 * 
 * @scr.reference name="config.context.service"
 *                interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 *                policy="dynamic" bind="setConfigurationContextService"
 *                unbind="unsetConfigurationContextService"
 * @scr.reference name="server.configuration" interface="org.wso2.carbon.base.ServerConfiguration"
 *                cardinality="1..1" policy="dynamic" bind="setServerConfiguration"
 *                unbind="unsetServerConfiguration"
 * @scr.reference name="org.wso2.carbon.statistics.services"
 *                interface="org.wso2.carbon.statistics.services.SystemStatisticsUtil"
 *                cardinality="1..1" policy="dynamic" bind="setSystemStatisticsUtil"
 *                unbind="unsetSystemStatisticsUtil"
 * @scr.reference name="org.wso2.carbon.registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService"
 *                unbind="unsetRegistryService"
 * @scr.reference name="org.wso2.carbon.bam.lwevent.core"
 *                interface="org.wso2.carbon.bam.lwevent.core.LightWeightEventBrokerInterface"
 *                cardinality="1..1" policy="dynamic" bind="setLWEventBroker"
 *                unbind="unsetLWEventBroker"
 */

public class StatisticsServiceComponent {

	private static Log log = LogFactory.getLog(StatisticsServiceComponent.class);

    private static SystemStatisticsUtil systemStatisticsUtil = null;
    private static ServiceStatisticsPublisherAdmin serviceStatisticsPublisherAdmin = null;
    private static ConfigurationContext configurationContext;
//    private static EventBroker eventBroker;
    private static LightWeightEventBrokerInterface lightWeightEventBroker;
    private static ServerConfiguration serverConfiguration;
    private static ServiceStatisticsQueue serviceStatisticsQueue;
    private static RealmService realmService;
    private static RegistryService registryService;


	protected void activate(ComponentContext context) {
		try {

			// Engaging StatisticsModule as a global module


            configurationContext.getAxisConfiguration().engageModule(
                        ServiceStatisticsPublisherConstants.BAM_SERVICE_STATISTISTICS_PUBLISHER_MODULE_NAME);

            BundleContext bundleContext = context.getBundleContext();
            bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(),
                                          new ServiceStatisticsAxis2ConfigurationContextObserver(), null);

            serviceStatisticsPublisherAdmin = new ServiceStatisticsPublisherAdmin();
            //use event publisher as the serviceStatsProcessor
            ServiceStatsProcessor serviceStatsProcessor = new EventPublisher();
            serviceStatisticsQueue = new ServiceStatisticsQueue(serviceStatsProcessor);

            PublisherUtils.setServiceStatisticQueue(serviceStatisticsQueue);

//            // Call if ESB is restarted
//
//            try {
//                SuperTenantCarbonContext.startTenantFlow();
//                int tenantId = SuperTenantCarbonContext.getCurrentContext(StatisticsServiceComponent.getConfigurationContext()).getTenantId();
//                SuperTenantCarbonContext.getCurrentContext().setTenantId(tenantId);
//                SuperTenantCarbonContext.getCurrentContext().getTenantDomain(true);
//                SuperTenantCarbonContext.getCurrentContext().setUsername(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
//                SuperTenantCarbonContext.getCurrentContext().setUserRealm(realmService.getBootstrapRealm());
//                getLightWeightEventBroker().getInstance().publish(ServiceStatisticsPublisherConstants.SERVICE_STATS_TOPIC);
////                        broker.subscribe(subscription);
//
////                    } catch (EventBrokerException e) {
////                        log.error("Can not subscribe to static endpoint ", e);
//            } finally {
//                SuperTenantCarbonContext.endTenantFlow();
//            }

            // Read bam subscriber service endpoint from carbon.xml of wsas and subscribe
			// automatically(for cloud scenario)
			if (SuperTenantCarbonContext.getCurrentContext(configurationContext.getAxisConfiguration())
                    .getTenantDomain() == null) {
                String serverURL = serverConfiguration.getFirstProperty(
                        ServiceStatisticsPublisherConstants.BAM_SERVER_URL);

                if (serverURL != null) {


//                    EventBroker broker = eventBroker;

                    Subscription subscription = new Subscription();

                    subscription.setEventSinkURL(serverURL +
                            ServiceStatisticsPublisherConstants.
                                    BAM_SERVICE_STAT_RECEIVER_SERVICE);
                    subscription.setTopicName(ServiceStatisticsPublisherConstants.BAM_REG_PATH);

                    subscription.setOwner(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
                    subscription.setEventDispatcherName(EventBrokerConstants.WS_EVENT_DISPATCHER_NAME);

                    try {
                        SuperTenantCarbonContext.startTenantFlow();
                        int tenantId = SuperTenantCarbonContext.getCurrentContext(StatisticsServiceComponent.getConfigurationContext()).getTenantId();
                        SuperTenantCarbonContext.getCurrentContext().setTenantId(tenantId);
                        SuperTenantCarbonContext.getCurrentContext().getTenantDomain(true);
                        SuperTenantCarbonContext.getCurrentContext().setUsername(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
                        SuperTenantCarbonContext.getCurrentContext().setUserRealm(realmService.getBootstrapRealm());
                        ServiceHolder.getLWEventBroker().subscribe(subscription);
//                        broker.subscribe(subscription);

//                    } catch (EventBrokerException e) {
//                        log.error("Can not subscribe to static endpoint ", e);
                    } finally {
                        SuperTenantCarbonContext.endTenantFlow();
                    }

                }

			}

            log.info("BAM service statistics data publisher bundle is activated");

		} catch (Throwable e) {
			if (log.isErrorEnabled()) {
				log.error("Failed to activate BAM service statistics data publisher bundle", e);
			}
        }
	}

	protected void deactivate(ComponentContext context) {
		if (log.isDebugEnabled()) {
			log.debug("BAM service statistics data publisher bundle is deactivated");
		}
        serviceStatisticsQueue.cleanup();
	}

    protected void setLWEventBroker(LightWeightEventBrokerInterface lightWeightEventBroker)  {
         ServiceHolder.setLWEventBroker(lightWeightEventBroker);
    }

    protected void unsetLWEventBroker(LightWeightEventBrokerInterface lightWeightEventBroker) {
        ServiceHolder.unsetLWEventBroker(lightWeightEventBroker);
    }

	protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        configurationContext = configurationContextService.getServerConfigContext();
        ServiceHolder.setConfigurationContextService(configurationContextService.getServerConfigContext());

	}

	protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        configurationContext = null;
        ServiceHolder.unsetConfigurationContextService(configurationContextService.getServerConfigContext());
	}

    public static ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

//	protected void setEventBroker(EventBroker broker) {
//        eventBroker = broker;
//	}
//
//	protected void unsetEventBroker(EventBroker broker) {
//		eventBroker = null;
//	}
//
//    public static EventBroker getEventBroker() {
//        return eventBroker;
//    }

	public static ServerConfiguration getServerConfiguration() {
		return serverConfiguration;
	}

	protected void setServerConfiguration(ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
	}

	protected void unsetServerConfiguration(ServerConfiguration serverConfiguration) {
		serverConfiguration = null;
	}

	protected void setRegistryService(RegistryService registryService) {
		try {
            this.registryService = registryService;
			RegistryPersistenceManager.setRegistry(registryService.getConfigSystemRegistry());
		} catch (Exception e) {
			log.error("Cannot retrieve System Registry", e);
		}
	}

    protected void setRealmService(RealmService realmService){
         this.realmService=realmService;
    }
    protected void unsetRealmService(RealmService realmService){
         this.realmService=null;
    }

	protected void unsetRegistryService(RegistryService registryService) {
        this.registryService = null;
		RegistryPersistenceManager.setRegistry(null);
	}

    public static RegistryService getRegistryService() {
        return registryService;
    }

    protected void setSystemStatisticsUtil(SystemStatisticsUtil systemStatisticsUtil) {
        this.systemStatisticsUtil = systemStatisticsUtil;
	}

	protected void unsetSystemStatisticsUtil(SystemStatisticsUtil sysStatUtil) {
        systemStatisticsUtil = null;
	}

    public static SystemStatisticsUtil getSystemStatisticsUtil() {
        return systemStatisticsUtil;
    }


    public static ServiceStatisticsPublisherAdmin getServiceStatisticsPublisherAdmin() {
        return serviceStatisticsPublisherAdmin;
    }

    public static LightWeightEventBrokerInterface getLightWeightEventBroker() {
        return lightWeightEventBroker;
    }
}
