/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.bam.data.publisher.activity.mediation.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bam.data.publisher.activity.mediation.ActivityProcessor;
import org.wso2.carbon.bam.data.publisher.activity.mediation.ActivityPublisherUtils;
import org.wso2.carbon.bam.data.publisher.activity.mediation.ActivityQueue;
import org.wso2.carbon.bam.data.publisher.activity.mediation.config.RegistryPersistanceManager;
import org.wso2.carbon.bam.data.publisher.activity.mediation.eventing.EventGenerator;
import org.wso2.carbon.bam.data.publisher.activity.mediation.services.ActivityPublisherAdmin;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBroker;
import org.wso2.carbon.mediation.statistics.services.MediationStatisticsService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;


/**
 * @scr.component name="org.wso2.carbon.mediator.bam.activity" immediate="true"
 * @scr.reference name="config.context.service" interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic" bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="org.wso2.carbon.util.server"
 * interface="org.wso2.carbon.base.ServerConfiguration" cardinality="1..1"
 * policy="dynamic" bind="setServerConfiguration" unbind="unsetServerConfiguration"
 * @scr.reference name="mediation.statistics"
 * interface="org.wso2.carbon.mediation.statistics.services.MediationStatisticsService"
 * cardinality="1..1" policy="dynamic" bind="setMediationStatisticsService"
 * unbind="unsetMediationStatisticsService"
 * @scr.reference name="org.wso2.carbon.registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="org.wso2.carbon.bam.lwevent.core"
 * interface="org.wso2.carbon.bam.lwevent.core.LightWeightEventBroker"
 * cardinality="1..1" policy="dynamic" bind="setLWEventBroker"
 * unbind="unsetLWEventBroker"
 */
public class ActivityMediatorServiceComponent {

    private static Log log = LogFactory.getLog(ActivityMediatorServiceComponent.class);
    private MediationStatisticsService statService;
    private ActivityQueue activityQueue;

    protected void activate(ComponentContext ctxt) {
        if (statService != null) {
            /*log.info("Registering the BAM mediation statistics observer...");
            ActivityMediationStatisticsObserver bamObserver = new ActivityMediationStatisticsObserver();
            statService.getStatisticsStore().registerObserver(bamObserver);*/
        }

        ActivityProcessor activityProcessor = null;

        // Check a custom activity processor is specified through the system property
        String processor = System.getProperty("bam.activity.processor");
        if (processor != null) {
            try {
                Class clazz = getClass().getClassLoader().loadClass(processor);
                activityProcessor = (ActivityProcessor) clazz.newInstance();
            } catch (Exception e) {
                log.error("Error while initializing the activity processor: " + processor, e);
            }
        }

        // If no processor is specified default to the eventing based activity processor
        if (activityProcessor == null) {
            activityProcessor = new EventGenerator();
        }
        this.activityQueue = new ActivityQueue(activityProcessor);

        // Check whether a threshold value has been specified for the activity queue
        String thresholdProperty = System.getProperty("bam.activity.threshold");
        if (thresholdProperty != null) {
            activityQueue.setThreshold(Integer.parseInt(thresholdProperty));
        } else {
            activityQueue.setThreshold(1);
        }

        ActivityPublisherUtils.setActivityQueue(activityQueue);

        ActivityPublisherAdmin admin = new ActivityPublisherAdmin();
        ActivityPublisherUtils.setActivityPublisherAdmin(admin);

        log.info("BAM activity mediator bundle is activated");
    }

    protected void deactivate(ComponentContext context) {
        activityQueue.cleanup();
        if (log.isDebugEnabled()) {
            log.debug("BAM activity Mediator bundle is deactivated");
        }
    }

    protected void setServerConfiguration(ServerConfiguration serverConfiguration) {
        ActivityPublisherUtils.setServerConfiguration(serverConfiguration);
    }

    protected void unsetServerConfiguration(ServerConfiguration serverConfiguration) {
        ActivityPublisherUtils.setServerConfiguration(null);
    }

    protected void setMediationStatisticsService(MediationStatisticsService statService) {
        if (log.isDebugEnabled()) {
            log.debug("Mediation statistics service bound to the BAM mediation statistics component");
        }
        this.statService = statService;
    }

    protected void unsetMediationStatisticsService(MediationStatisticsService statService) {
        if (log.isDebugEnabled()) {
            log.debug("Mediation statistics service unbound from the BAM mediation statistics component");
        }
        this.statService = null;
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        ActivityPublisherUtils.setConfigurationContextService(contextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        ActivityPublisherUtils.setConfigurationContextService(null);
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

    protected void setLWEventBroker(LightWeightEventBroker lightWeightEventBroker) {
        ActivityPublisherUtils.setEventBroker(lightWeightEventBroker);
    }

    protected void unsetLWEventBroker(LightWeightEventBroker lightWeightEventBroker) {
        ActivityPublisherUtils.setEventBroker(null);
    }

}