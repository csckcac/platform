/*
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.qpid.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.qpid.server.BrokerOptions;
import org.apache.qpid.server.Main;
import org.apache.qpid.server.registry.ApplicationRegistry;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.andes.wso2.service.QpidNotificationService;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.ServerStatus;
import org.wso2.carbon.qpid.service.QpidService;
import org.wso2.carbon.qpid.service.QpidServiceImpl;
import org.wso2.carbon.qpid.authentication.service.AuthenticationService;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.event.core.EventBundleNotificationService;
import org.wso2.carbon.event.core.qpid.QpidServerDetails;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @scr.component  name="org.wso2.carbon.andes.internal.QpidServiceComponent"
 *                              immediate="true"
 * @scr.reference    name="org.wso2.carbon.andes.authentication.service.AuthenticationService"
 *                              interface="org.wso2.carbon.qpid.authentication.service.AuthenticationService"
 *                              cardinality="1..1"
 *                              policy="dynamic"
 *                              bind="setAccessKey"
 *                              unbind="unsetAccessKey"
 * @scr.reference    name="org.wso2.andes.wso2.service.QpidNotificationService"
 *                              interface="org.wso2.andes.wso2.service.QpidNotificationService"
 *                              cardinality="1..1"
 *                              policy="dynamic"
 *                              bind="setQpidNotificationService"
 *                              unbind="unsetQpidNotificationService"
 * @scr.reference    name="server.configuration"
 *                              interface="org.wso2.carbon.base.api.ServerConfigurationService"
 *                              cardinality="1..1"
 *                              policy="dynamic"
 *                              bind="setServerConfiguration"
 *                              unbind="unsetServerConfiguration"
 * @scr.reference    name="event.broker"
 *                              interface="org.wso2.carbon.event.core.EventBundleNotificationService"
 *                              cardinality="1..1"
 *                              policy="dynamic"
 *                              bind="setEventBundleNotificationService"
 *                              unbind="unsetEventBundleNotificationService"
 */
public class QpidServiceComponent {

    private static final Log log = LogFactory.getLog(QpidServiceComponent.class);

    private static final String VM_BROKER_AUTO_CREATE = "amqj.AutoCreateVMBroker";
    private static final String DERBY_LOG_FILE = "derby.stream.error.file";
    private static final String QPID_DERBY_LOG_FILE = "/repository/logs/qpid-derby-store.log";

    private ServiceRegistration qpidService = null;


    private boolean activated = false;

    protected void activate(ComponentContext ctx) {


        if (ctx.getBundleContext().getServiceReference(QpidService.class.getName()) != null) {
            return;
        }

        // Make it possible to create VM brokers automatically
        System.setProperty(VM_BROKER_AUTO_CREATE, "true");
        // Set Derby log filename
        System.setProperty(DERBY_LOG_FILE, System.getProperty(ServerConstants.CARBON_HOME) + QPID_DERBY_LOG_FILE);
        
        QpidServiceImpl qpidServiceImpl =
                new QpidServiceImpl(QpidServiceDataHolder.getInstance().getAccessKey());

        // Start Qpid broker
        try {
            System.setProperty(BrokerOptions.QPID_HOME, qpidServiceImpl.getQpidHome());
            String[] args = {"-p" + qpidServiceImpl.getPort(), "-s" + qpidServiceImpl.getSSLPort()};
            //Main.setStandaloneMode(false);
            Main.main(args);

            // Remove Qpid shutdown hook so that I have control over shutting the broker down
            Runtime.getRuntime().removeShutdownHook(ApplicationRegistry.getShutdownHook());

            // Wait until the broker has started
            while (!isBrokerRunning()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {}
            }

            //check whether the tcp port has started. some times the server started thread may return
            //before Qpid server actually bind to the tcp port. in that case there are some connection
            //time out issues.
            boolean isServerStarted = false;
            int port = Integer.parseInt(qpidServiceImpl.getPort());
            while (!isServerStarted) {
                Socket socket = null;
                try {
                    InetAddress address = InetAddress.getByName("localhost");
                    socket = new Socket(address, port);
                    isServerStarted = socket.isConnected();
                    if (isServerStarted) {
                        log.info("Successfully connected to the server on port " + qpidServiceImpl.getPort());
                    }
                } catch (IOException e) {
                    log.info("Wait until Qpid server starts on port " + qpidServiceImpl.getPort());
                    Thread.sleep(500);
                } finally {
                    try {
                        if ((socket != null) && (socket.isConnected())) {
                            socket.close();
                        }
                    } catch (IOException e) {
                        log.error("Can not close the socket with is used to check the server status ");
                    }
                }
            }

        } catch (Exception e) {
            log.error("Failed to start Qpid broker : " + e.getMessage());
        } finally {
            // Publish Qpid properties
            qpidService = ctx.getBundleContext().registerService(
                    QpidService.class.getName(), qpidServiceImpl, null);
            QpidServerDetails qpidServerDetails =
                          new QpidServerDetails(qpidServiceImpl.getAccessKey(),
                                  qpidServiceImpl.getClientID(),
                                  qpidServiceImpl.getVirtualHostName(),
                                  qpidServiceImpl.getHostname(),
                                  qpidServiceImpl.getPort());
            QpidServiceDataHolder.getInstance().getEventBundleNotificationService().notifyStart(qpidServerDetails);
             activated =true;
        }
    }

    protected void deactivate(ComponentContext ctx) {
        // Unregister QpidService
        try {
            if (null != qpidService) {
                qpidService.unregister();
            }
        } catch (Exception e) {}

        // Shutdown the Qpid broker
        ApplicationRegistry.remove();
    }

    protected void setAccessKey(AuthenticationService authenticationService) {
        QpidServiceDataHolder.getInstance().setAccessKey(authenticationService.getAccessKey());
    }

    protected void unsetAccessKey(AuthenticationService authenticationService) {
        QpidServiceDataHolder.getInstance().setAccessKey(null);
    }
    
    protected void setQpidNotificationService(QpidNotificationService qpidNotificationService) {
        // Qpid broker should not start until Qpid bundle is activated.
        // QpidNotificationService informs that the Qpid bundle has started.

    }

    protected void unsetQpidNotificationService(QpidNotificationService qpidNotificationService) {}

    protected void setServerConfiguration(ServerConfigurationService serverConfiguration) {
        QpidServiceDataHolder.getInstance().setCarbonConfiguration(serverConfiguration);
    }

    protected void unsetServerConfiguration(ServerConfigurationService serverConfiguration) {
        QpidServiceDataHolder.getInstance().setCarbonConfiguration(null);
    }

    protected void setEventBundleNotificationService(EventBundleNotificationService eventBundleNotificationService){
        QpidServiceDataHolder.getInstance().registerEventBundleNotificationService(eventBundleNotificationService);
    }

    protected void unsetEventBundleNotificationService(EventBundleNotificationService eventBundleNotificationService){
        // unsetting
    }

    /**
        * Check if the broker is up and running
        *
        * @return
        *           true if the broker is running or false otherwise 
        */
    private boolean isBrokerRunning() {
        boolean response = false;

        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> set = mBeanServer.queryNames(
                    new ObjectName("org.apache.qpid:type=VirtualHost.VirtualHostManager,*"), null);

            if (set.size() > 0) { // Virtual hosts created, hence broker running.
                response = true;
            }
        } catch (MalformedObjectNameException e) {
        }

        return response;
    }
}
