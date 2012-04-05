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
package org.wso2.carbon.bam.activity.mediation.data.publisher.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bam.activity.mediation.data.publisher.conf.RegistryPersistenceManager;
import org.wso2.carbon.bam.activity.mediation.data.publisher.publish.ActivityProcessor;
import org.wso2.carbon.bam.activity.mediation.data.publisher.publish.DataPublisher;
import org.wso2.carbon.bam.activity.mediation.data.publisher.queue.ActivityQueue;
import org.wso2.carbon.bam.activity.mediation.data.publisher.util.ActivityPublisherUtils;
import org.wso2.carbon.bam.data.publisher.util.PublisherConfiguration;
import org.wso2.carbon.bam.data.publisher.util.PublisherUtil;
import org.wso2.carbon.bam.data.publisher.util.stats.StatsPrinterTimerTask;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.mediation.statistics.services.MediationStatisticsService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;


/**
 *@scr.component name="org.wso2.carbon.bam.activity.mediation.data.publisher" immediate="true"
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
 */
public class ActivityMediatorServiceComponent {

    private static Log log = LogFactory.getLog(ActivityMediatorServiceComponent.class);
    private ActivityQueue activityQueue;
    private MediationStatisticsService statService;

    protected void activate(ComponentContext context) {

        PublisherConfiguration configuration = PublisherUtil.readConfigurationFromAgentConfig();
        ActivityPublisherUtils.setPublisherConfiguration(configuration);

        ActivityProcessor activityProcessor = new DataPublisher();
        activityQueue = new ActivityQueue(activityProcessor,configuration);
        ActivityPublisherUtils.setActivityQueue(activityQueue);

        new Thread(new StatsPrinterTimerTask()).start();
        //Load previously saved configurations
        new RegistryPersistenceManager().load();

        log.info("BAM activity mediator bundle is activated");
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
            RegistryPersistenceManager.setRegistryService(registryService);
        } catch (Exception e) {
            log.error("Cannot retrieve System Registry", e);
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
        RegistryPersistenceManager.setRegistryService(null);
    }
}
