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

package org.wso2.carbon.health.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.AxisFault;
import org.apache.log4j.*;
import org.osgi.service.component.ComponentContext;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.eventing.broker.services.EventBrokerService;

import java.net.SocketException;

/**
 * @scr.component name="org.wso2.carbon.health.service" immediate="true"
 * @scr.reference name="configuration.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="listener.manager.service"
 * interface="org.apache.axis2.engine.ListenerManager" cardinality="0..1" policy="dynamic"
 * bind="setListenerManager" unbind="unsetListenerManager"
 * @scr.reference name="eventbroker.service"
 * interface="org.wso2.carbon.eventing.broker.services.EventBrokerService"
 * cardinality="1..1" policy="dynamic" target="(name=HealthMonitorEventBroker)"
 * bind="setHealthMonitorEventBrokerService" unbind="unsetHealthMonitorEventBrokerService"
 */
public class HealthMonitorEventingServiceComponent {
    private static Log log = LogFactory.getLog(HealthMonitorEventingServiceComponent.class);

    private boolean configurationDone = false;

    private ConfigurationContextService configurationContextService = null;

    private ListenerManager listenerManager = null;

    private boolean initialized = false;

    private static EventBrokerService healthMonitorEventBrokerService = null;

    /** The service endpoint */
    private String endpoint = null;

    private static BundleContext bundleContext = null;

    static Logger logger = Logger.getLogger(HealthMonitorEventingServiceComponent.class);
    //static BundleContext bundleContxt;
    Logger rootLogger = LogManager.getRootLogger();

    protected void activate(ComponentContext context) {
        bundleContext = context.getBundleContext();
    }

     public static BundleContext getBundleContext() {
        return bundleContext;
    }

    /**
     * Initializes the JiraAppender
     */
    private void initializeAppender() {
        if (listenerManager == null || healthMonitorEventBrokerService == null) {
            return;
        }
        System.out.println("Carbon Health Monitor started!");

        rootLogger.addAppender(new JiraAppender());
        log.info("Jira appender added to the root logger");
        // Setting level of root logger
        //rootLogger.setLevel(Level.DEBUG);

        addAppenders();
        rootLogger.error("This log message is used to trigger the JiraAppender");
    }

    /**
     * Add other appenders to the rootlogger. This is written for testing purposes
     */
    public void addAppenders() {
        SimpleLayout layout = new SimpleLayout();
        FileAppender appender = null;
        try {
            appender = new FileAppender(layout, "/tmp/output1.txt", false);
        } catch (Exception e) {
            logger.error(e);
        }

        rootLogger.addAppender(appender);
        logger.debug("DEBUG message 2");
        logger.debug("DEBUG message 3");
        log.info("Appender added to the logger");
    }

    protected void deactivate(ComponentContext context) {
    }

    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        this.configurationContextService  = configurationContextService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
    }

    protected void setHealthMonitorEventBrokerService(EventBrokerService healthMonitorEventBrokerService) {
        this.healthMonitorEventBrokerService = healthMonitorEventBrokerService;
        initializeAppender();
    }

    protected void unsetHealthMonitorEventBrokerService(EventBrokerService healthMonitorEventBrokerService) {
        this.healthMonitorEventBrokerService = null;
    }

    protected void setListenerManager(ListenerManager listenerManager) {
        this.listenerManager = listenerManager;
        initialize();
        initializeAppender();
    }

    protected void unsetListenerManager(ListenerManager listenerManager) {
        this.listenerManager = null;
    }

    private void initialize() {
        ConfigurationContext serverConfigurationContext = configurationContextService.getServerConfigContext();
        if (!configurationDone && listenerManager != null) {
            String host = null;
            try {
                host = NetworkUtils.getLocalHostname();
            } catch (SocketException e) { }
            if (serverConfigurationContext != null) {
                AxisConfiguration config = serverConfigurationContext.getAxisConfiguration();
                if (config != null && config.getTransportIn("http") != null &&
                        config.getTransportIn("http").getReceiver() != null) {
                    try {
                        EndpointReference[] eprArray = config.getTransportIn("http")
                                .getReceiver().getEPRsForService("HealthMonitorEventingService",
                                        host);
                        if (eprArray != null && eprArray[0] != null) {
                            endpoint = eprArray[0].getAddress();
                        }
                    } catch (AxisFault e) { }
                }
            }
            configurationDone = true;
        }
    }

    public static EventBrokerService getHealthMonitorEventBrokerService() {
        return healthMonitorEventBrokerService;
    }
}

