/**
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.usage.summary.generator.internal;

import java.util.Timer;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.local.LocalTransportReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bam.core.summary.generators.SummaryGeneratorFactory;
import org.wso2.carbon.usage.summary.generator.MeteringSummaryGeneratorFactory;
import org.wso2.carbon.usage.summary.generator.RegistryUsageSummaryGeneratorTask;
import org.wso2.carbon.usage.summary.generator.client.UsageSummaryGeneratorClient;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.stratos.common.util.StratosConfiguration;
import org.wso2.carbon.stratos.common.util.CommonUtil;
/**
 * @scr.component name="org.wso2.carbon.usage.summary.generator" immediate="true"
 * @scr.reference name="config.context.service" 
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1" 
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="user.realmservice.default" 
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1" 
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 */
public class UsageSummaryGeneratorServiceComponent {
    private static Log log = LogFactory.getLog(UsageSummaryGeneratorServiceComponent.class);
    private static ConfigurationContextService configurationContextService;
    private static RealmService realmService;
    
    public static final long DEFAULT_INITIAL_SUMMARY_GEN_DELAY = 10 * 60000; //10 minutes
    public static final long DEFAULT_SUMMARY_GEN_INTERVAL = 60 * 60000 * 24; // one Day


    protected void activate(ComponentContext context) {
        try {
            StratosConfiguration stratosConfigurations = CommonUtil.getStratosConfig();
            if (stratosConfigurations.isSkipSummaryGenerator() == false) {
            //Create Usage Summary Generator Client
            UsageSummaryGeneratorClient client = new UsageSummaryGeneratorClient(
                    configurationContextService.getServerConfigContext());
            
            //Create Metering Summary Generator factory and register it
            MeteringSummaryGeneratorFactory summaryGeneratorFactory = 
                new MeteringSummaryGeneratorFactory(client);
            context.getBundleContext().registerService(
                    SummaryGeneratorFactory.class.getName(), summaryGeneratorFactory, null);
            
            //Create Registry usage summary generator start the timer
            RegistryUsageSummaryGeneratorTask regUsageSummaryGeneratorTask = 
                new RegistryUsageSummaryGeneratorTask(client, realmService);
            Timer summaryTimer = new Timer(true);
            summaryTimer.schedule(regUsageSummaryGeneratorTask, DEFAULT_INITIAL_SUMMARY_GEN_DELAY,
                    DEFAULT_SUMMARY_GEN_INTERVAL);
              log.info("Stratos Usage Summary Generation Enabled.");
            } else {
                log.info("Stratos Usage Summary Generation Disabled.");
            }

        } catch (Throwable e) {
            log.error("******* Error in activating Usage bundle ******* ", e);
        }
    }

    protected void deactivate(ComponentContext context) {
        log.debug("******* Usage Summary Generation bundle is deactivated ******* ");
    }

    protected void setConfigurationContextService(ConfigurationContextService ccService) {
        //commented to work with local transport
	/*ConfigurationContext serverCtx = ccService.getServerConfigContext();
        AxisConfiguration serverConfig = serverCtx.getAxisConfiguration();
        LocalTransportReceiver.CONFIG_CONTEXT = new ConfigurationContext(serverConfig);
        LocalTransportReceiver.CONFIG_CONTEXT.setServicePath("services");
        LocalTransportReceiver.CONFIG_CONTEXT.setContextRoot("local:/");
	*/
        configurationContextService = ccService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService ccService) {
        configurationContextService = null;
    }
    
    protected void setRealmService(RealmService realmService) {
        UsageSummaryGeneratorServiceComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        realmService = null;
    }
}
