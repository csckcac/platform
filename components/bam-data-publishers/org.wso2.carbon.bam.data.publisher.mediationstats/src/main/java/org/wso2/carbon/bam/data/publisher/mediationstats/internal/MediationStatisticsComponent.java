/*
 * Copyright 2005-2010 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.data.publisher.mediationstats.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bam.data.publisher.mediationstats.MDPublisherConstants;
import org.wso2.carbon.bam.data.publisher.mediationstats.PublisherUtils;
import org.wso2.carbon.bam.data.publisher.mediationstats.ServiceHolder;
import org.wso2.carbon.bam.data.publisher.mediationstats.config.RegistryPersistenceManager;
import org.wso2.carbon.bam.data.publisher.mediationstats.observer.BAMMediationStatisticsObserver;
import org.wso2.carbon.bam.data.publisher.mediationstats.services.BAMMediationStatsPublisherAdmin;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBrokerInterface;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBroker;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.mediation.statistics.MediationStatisticsStore;
import org.wso2.carbon.mediation.statistics.services.MediationStatisticsService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.statistics.services.SystemStatisticsUtil;
import org.wso2.carbon.utils.ConfigurationContextService;


/**
 * @scr.component name="org.wso2.carbon.bam.data.publisher.mediationstats" immediate="true"
 * @scr.reference name="config.context.service" interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic" bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="server.configuration" interface="org.wso2.carbon.base.ServerConfiguration"
 * cardinality="1..1" policy="dynamic" bind="setServerConfiguration"
 * unbind="unsetServerConfiguration"
 * @scr.reference name="org.wso2.carbon.registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="synapse.env.service"
 * interface="org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService"
 * cardinality="1..1" policy="dynamic" bind="setSynapseEnvironmentService"
 * unbind="unsetSynapseEnvironmentService"
 * @scr.reference name="mediation.statistics"
 * interface="org.wso2.carbon.mediation.statistics.services.MediationStatisticsService"
 * cardinality="1..n" policy="dynamic" bind="setMediationStatisticsService"
 * unbind="unsetMediationStatisticsService"
 * @scr.reference name="org.wso2.carbon.bam.lwevent.core"
 * interface="org.wso2.carbon.bam.lwevent.core.LightWeightEventBrokerInterface"
 * cardinality="1..1" policy="dynamic" bind="setLWEventBroker"
 * unbind="unsetLWEventBroker"
 */
public class MediationStatisticsComponent {

    private static final Log log = LogFactory.getLog(MediationStatisticsComponent.class);

    private ConfigurationContext configContext;
    private ServerConfiguration serverConfiguration;
    private ServiceRegistration statAdminServiceRegistration;
    private MediationStatisticsService mediationStatisticsService;
    private static String BAM_SERVER_URL = "BamServerURL";

    private boolean activated = false;

    protected void activate(ComponentContext ctxt) {
        try {

            BAMMediationStatsPublisherAdmin bamMediationStatsPublisherAdmin = new BAMMediationStatsPublisherAdmin();

            PublisherUtils.setMediationStatPublisherAdmin(bamMediationStatsPublisherAdmin);


            // at the stratos automatically subscribe

            AxisConfiguration axisConfiguration = configContext.getAxisConfiguration();
            SuperTenantCarbonContext superTenantCarbonContext = SuperTenantCarbonContext.getCurrentContext(axisConfiguration);
            String tenantDomainName = superTenantCarbonContext.getTenantDomain();

            if (tenantDomainName == null) {
                ServerConfiguration serverConfiguration = this.serverConfiguration;
                String mediationServerURL = serverConfiguration.getFirstProperty(BAM_SERVER_URL);

                if (mediationServerURL != null) {
                    LightWeightEventBrokerInterface eventBroker = ServiceHolder.getLWEventBroker();

                    Subscription subscription = new Subscription();

                    subscription.setEventSinkURL(mediationServerURL + MDPublisherConstants.BAM_MEDIATION_STAT_RECEIVER_SERVICE);
                    subscription.setTopicName(MDPublisherConstants.BAM_REG_PATH);

                    try {
                        eventBroker.subscribe(subscription);
                    } catch (Exception e) {
                        throw new AxisFault("Can not subscribe to the event broker ", e);
                    }
                }
            }


            MediationStatisticsStore mediationStatisticsStore = mediationStatisticsService.getStatisticsStore();
            //in any occurence if 'mediationStatisticsStore' is null, observre wont be registered
            BAMMediationStatisticsObserver observer = new BAMMediationStatisticsObserver();
            mediationStatisticsStore.registerObserver(observer);
            observer.setTenantId(mediationStatisticsService.getTenantId()); // 'MediationStat
            // service' will
            // be deployed
            // per tenant (cardinality="1..n")
            observer.setTenantAxisConfiguration(mediationStatisticsService.getConfigurationContext().
                    getAxisConfiguration());
            log.info("Registering  Observer for tenant: " + mediationStatisticsService.getTenantId());


            activated = true;
            log.info("BAM MediationStatisticsComponent activate");
            if (log.isDebugEnabled()) {
                log.debug("BAM Mediation statistics data publisher bundle is activated");
            }
        } catch (Throwable e) {
            log.error("Failed to activate BAM Mediation statistics data publisher bundle", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        // unregistered BAMMediationStatsPublisherAdmin service from the OSGi Service Register.
        statAdminServiceRegistration.unregister();
        if (log.isDebugEnabled()) {
            log.debug("BAM service statistics data publisher bundle is deactivated");
        }
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        this.configContext = contextService.getServerConfigContext();
        PublisherUtils.setConfigurationContextService(contextService);

    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        PublisherUtils.setConfigurationContextService(null);
        this.configContext = null;
    }

    protected void setServerConfiguration(ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    protected void unsetServerConfiguration(ServerConfiguration serConfiguration) {
        this.serverConfiguration = null;
    }

    protected void setRegistryService(RegistryService registryService) {
        try {
            RegistryPersistenceManager.setRegistry(registryService.getConfigSystemRegistry());
        } catch (Exception e) {
            log.error("Cannot retrieve System Registry", e);
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
        RegistryPersistenceManager.setRegistry(null);
    }

    protected void setSystemStatisticsUtil(SystemStatisticsUtil systemStatisticsUtil) {
        PublisherUtils.setSystemStatististicsUtil(systemStatisticsUtil);
    }

    protected void unsetSystemStatisticsUtil(SystemStatisticsUtil systemStatisticsUtil) {
        PublisherUtils.setSystemStatististicsUtil(null);
    }

    protected void setSynapseEnvironmentService(
            SynapseEnvironmentService synapseEnvironmentService) {
        PublisherUtils.setSynapseEnvironmentService(synapseEnvironmentService);
    }

    protected void unsetSynapseEnvironmentService(
            SynapseEnvironmentService synapseEnvironmentService) {
        PublisherUtils.setSynapseEnvironmentService(null);
    }

    protected void setMediationStatisticsService(
            MediationStatisticsService mediationStatisticsService) {
        if (log.isDebugEnabled()) {
            log.debug("Mediation statistics service bound to the BAM mediation statistics component");
        }
        if (activated && mediationStatisticsService != null) {
            MediationStatisticsStore mediationStatisticsStore =
                    mediationStatisticsService.getStatisticsStore();
            BAMMediationStatisticsObserver observer = new BAMMediationStatisticsObserver();
            mediationStatisticsStore.registerObserver(observer);
            observer.setTenantId(mediationStatisticsService.getTenantId());
            observer.setTenantAxisConfiguration(
                    mediationStatisticsService.getConfigurationContext().getAxisConfiguration());
            log.info("Registering BamMediationStatistics Observer for tenant: " +
                     mediationStatisticsService.getTenantId());

        } else {
            this.mediationStatisticsService = mediationStatisticsService;
        }
    }

    protected void unsetMediationStatisticsService(MediationStatisticsService statService) {
        if (log.isDebugEnabled()) {
            log.debug("Mediation statistics service unbound from the BAM mediation statistics component");
        }
        // mediationStatisticsService = null;
    }

    protected void setLWEventBroker(LightWeightEventBrokerInterface broker) {
        PublisherUtils.setEventBroker(broker);
        ServiceHolder.setLWEventBroker(broker);
    }

    protected void unsetLWEventBroker(LightWeightEventBrokerInterface broker) {
        PublisherUtils.setEventBroker(null);
        ServiceHolder.setLWEventBroker(null);
    }

}

