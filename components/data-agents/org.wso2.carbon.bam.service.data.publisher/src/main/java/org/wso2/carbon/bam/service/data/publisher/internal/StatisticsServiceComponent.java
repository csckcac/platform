/*
* Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.bam.service.data.publisher.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bam.service.data.publisher.conf.RegistryPersistenceManager;
import org.wso2.carbon.bam.service.data.publisher.util.ServiceStatisticsPublisherConstants;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.statistics.services.SystemStatisticsUtil;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.bam.service.data.publisher " immediate="true"
 * @scr.reference name="org.wso2.carbon.statistics.services"
 * interface="org.wso2.carbon.statistics.services.SystemStatisticsUtil"
 * cardinality="1..1" policy="dynamic" bind="setSystemStatisticsUtil"
 * unbind="unsetSystemStatisticsUtil"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="org.wso2.carbon.registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unsetRegistryService"
 */

public class StatisticsServiceComponent {

    private static SystemStatisticsUtil systemStatisticsUtil;
    private static ConfigurationContext configurationContext;
//    private static ServiceStatisticsQueue serviceStatisticsQueue;
//    private static ActivityQueue activityQueue;
//    private static EventQueue eventQueue;
    private static Log log = LogFactory.getLog(StatisticsServiceComponent.class);

    protected void activate(ComponentContext context) {

        try {
            // Engaging StatisticsModule as a global module
            configurationContext.getAxisConfiguration().engageModule(
                    ServiceStatisticsPublisherConstants.BAM_SERVICE_STATISTICS_PUBLISHER_MODULE_NAME);
            BundleContext bundleContext = context.getBundleContext();
            bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(),
                                          new ServiceStatisticsAxis2ConfigurationContextObserver(), null);



            //use event publisher as the serviceStatsProcessor
//            StatsProcessor statsProcessor = new DataPublisher1();
//            eventQueue = new EventQueue(statsProcessor, configuration);
//            ServiceAgentUtil.setEventQueue(eventQueue);
/*            serviceStatisticsQueue = new ServiceStatisticsQueue(statsProcessor, configuration);
            ServiceAgentUtil.setServiceStatisticQueue(serviceStatisticsQueue);

            activityQueue = new ActivityQueue(statsProcessor);
            ServiceAgentUtil.setActivityInQueue(activityQueue);*/

            new RegistryPersistenceManager().load();

            log.info("BAM service statistics data publisher bundle is activated");
        } catch (AxisFault axisFault) {
            if (log.isErrorEnabled()) {
                log.error("Failed to activate BAM service statistics data publisher bundle", axisFault);
            }
        } catch (Throwable t) {
            log.error("Failed to activate BAM service statistics data publisher bundle", t);
        }
    }

    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("BAM service statistics data publisher bundle is deactivated");
        }
    }


    protected void setSystemStatisticsUtil(SystemStatisticsUtil systemStatisticsUtil) {
        this.systemStatisticsUtil = systemStatisticsUtil;
    }

    public static SystemStatisticsUtil getSystemStatisticsUtil() {
        return systemStatisticsUtil;
    }

    protected void unsetSystemStatisticsUtil(SystemStatisticsUtil sysStatUtil) {
        systemStatisticsUtil = null;
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        configurationContext = configurationContextService.getServerConfigContext();

    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        configurationContext = null;
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
