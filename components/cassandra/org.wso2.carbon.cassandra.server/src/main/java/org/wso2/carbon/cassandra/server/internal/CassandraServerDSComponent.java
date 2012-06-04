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
package org.wso2.carbon.cassandra.server.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cassandra.server.CassandraServerComponentManager;
import org.wso2.carbon.cassandra.server.CassandraServerController;
import org.wso2.carbon.cassandra.server.service.CassandraServerService;
import org.wso2.carbon.cassandra.server.service.CassandraServerServiceImpl;
import org.wso2.carbon.identity.authentication.AuthenticationService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.base.api.ServerConfigurationService;

import java.io.File;

/**
 * @scr.component name="org.wso2.carbon.cassandra.server.component" immediate="true"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"  unbind="unsetRealmService"
 * @scr.reference name="org.wso2.carbon.identity.authentication.internal.AuthenticationServiceComponent"
 * interface="org.wso2.carbon.identity.authentication.AuthenticationService"
 * cardinality="1..1" policy="dynamic" bind="setAuthenticationService"  unbind="unsetAuthenticationService"
 * @scr.reference name="server.configuration"
 * interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1" policy="dynamic"  bind="setServerConfiguration" unbind="unsetServerConfiguration"
 */
public class CassandraServerDSComponent {

    private static Log log = LogFactory.getLog(CassandraServerDSComponent.class);

    private static final String CASSANDRA_SERVER_CONF = File.separator + "repository" + File.separator + "conf"
            + File.separator + "etc" + File.separator + "cassandra.yaml";
    private static final String DEFAULT_CONF = "/org/wso2/carbon/cassandra/server/deployment/cassandra_default.yaml";
    private static final int CASSANDRA_RPC_PORT = 9160;
    private static final int CASSANDRA_STORAGE_PORT = 7000;
    private static final int CASSANDRA_SSL_STORAGE_PORT = 7001;
    /**
     * WSO2 Carbon Port for carbon.xml
     */
    private static int CARBON_DEFAULT_PORT_OFFSET = 0;
    private static String CARBON_CONFIG_CASSANDRA_RPC_PORT = "Ports.EmbeddedCassandra.RPCPort";
    private static String CARBON_CONFIG_CASSANDRA_STORAGE_PORT = "Ports.EmbeddedCassandra.StoragePort";
    private static String CARBON_CONFIG_PORT_OFFSET = "Ports.Offset";


    private static String DISABLE_CASSANDRA_SERVER_STARTUP = "disable.cassandra.server.startup";

    private CassandraServerController cassandraServerController;
    private RealmService realmService;
    private AuthenticationService authenticationService;
    private ServerConfigurationService serverConfigurationService;


    protected void activate(ComponentContext componentContext) {
        try {

            if (log.isDebugEnabled()) {
                log.debug("Starting the Cassandra Server component");
            }

            CassandraServerComponentManager.getInstance().init(realmService, authenticationService, serverConfigurationService);

            // initialize and start the Cassandra server
            String cassandraConfLocation = DEFAULT_CONF;
            if (isConfigurationExists()) {
                cassandraConfLocation = "file:" + System.getProperty(ServerConstants.CARBON_HOME) + CASSANDRA_SERVER_CONF;
            }
            System.setProperty("cassandra.config", cassandraConfLocation);
            System.setProperty("cassandra-foreground", "yes");
            int carbonPortOffset = readPortOffset();
            int cassandrRPCPort = CASSANDRA_RPC_PORT + carbonPortOffset;
            System.setProperty("cassandra.rpc_port", Integer.toString(cassandrRPCPort));
            int cassandraStoragePort = CASSANDRA_STORAGE_PORT + carbonPortOffset;
            System.setProperty("cassandra.storage_port", Integer.toString(cassandraStoragePort));
            int cassandraSSLStoragePort = CASSANDRA_SSL_STORAGE_PORT + carbonPortOffset;
            System.setProperty("cassandra.ssl_storage_port", Integer.toString(cassandraStoragePort));


            //set Cassnadra ports form carbon.xml

//            int rpcPort = 9160;
//            String carbonCassandraRPCPort = readRPCPortFromCarbonConfig();
//            int carbonPortOffset = readPortOffset();
//            try {
//                if (!carbonCassandraRPCPort.isEmpty()) {
//                    rpcPort = Integer.parseInt(carbonCassandraRPCPort) + carbonPortOffset;
//                } else {
//                    rpcPort += carbonPortOffset;
//                }
//                if (rpcPort > 0 && rpcPort < 65535) {
//                    System.setProperty("cassandra.rpcport", Integer.toString(rpcPort));
//                } else {
//                    log.error("Error setting Cassandra RPC port : Port out of range :" + rpcPort);
//                }
//            } catch (NumberFormatException e) {
//                log.debug("Error Reading Cassandra RPC Port");
//            }
//
//            int storagePort = 0;
//            String carbonCassandraStoragePort = readStoragePortFromCarbonConfig();
//
//            if (!carbonCassandraStoragePort.isEmpty()) {
//                try {
//                    storagePort = Integer.parseInt(carbonCassandraStoragePort) + carbonPortOffset;
//                    if (storagePort > 0 && storagePort < 65535) {
//                        System.setProperty("cassandra.storageport", Integer.toString(storagePort));
//                    }
//                } catch (NumberFormatException e) {
//                    log.debug("Error Reading Cassandra RPC Port");
//                }
//            }
//

            cassandraServerController = new CassandraServerController();

            //register OSGI service

            CassandraServerService cassandraServerService =
                    new CassandraServerServiceImpl(cassandraServerController);
            componentContext.getBundleContext().registerService(
                    CassandraServerService.class.getName(), cassandraServerService, null);

            String disableServerStartup = System.getProperty(DISABLE_CASSANDRA_SERVER_STARTUP);


            if ("true".equals(disableServerStartup)) {
                log.debug("Cassandra server is not started in service activator");
                return;
            }

            cassandraServerController.start();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    protected void deactivate(ComponentContext componentContext) {

        if (log.isDebugEnabled()) {
            log.debug("Stopping the Cassandra Server component");
        }
        // stop and destroy the Cassandra server
        if (cassandraServerController != null) {
            cassandraServerController.shutdown();
        }
        CassandraServerComponentManager.getInstance().destroy();
    }

    protected void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        this.realmService = null;
    }

    protected void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    protected void unsetAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = null;
    }

    protected void setServerConfiguration(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    protected void unsetServerConfiguration(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = null;
    }

    /**
     * Checks the existence of the cassandra conf
     *
     * @return true if it is there in advanced directory
     */
    private boolean isConfigurationExists() {
        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        String path = carbonHome + CASSANDRA_SERVER_CONF;
        if (!new File(path).exists()) {
            log.info("There is no " + CASSANDRA_SERVER_CONF + ". Using the default configuration");
            return false;
        } else {
            return true;
        }
    }

//    /**
//        * Read port from carbon.xml
//        *
//        * @return
//        */
//    private String readRPCPortFromCarbonConfig() {
//        ServerConfiguration carbonConfig = ServerConfiguration.getInstance();
//        String port = carbonConfig.getFirstProperty(CARBON_CONFIG_CASSANDRA_RPC_PORT);
//
//        return ((port != null) ? port.trim() : "");
//    }
//
//    private String readStoragePortFromCarbonConfig() {
//        ServerConfiguration carbonConfig = ServerConfiguration.getInstance();
//        String port = carbonConfig.getFirstProperty(CARBON_CONFIG_CASSANDRA_STORAGE_PORT);
//
//        return ((port != null) ? port.trim() : "");
//    }

    private int readPortOffset() {
        ServerConfiguration carbonConfig = ServerConfiguration.getInstance();
        String portOffset = carbonConfig.getFirstProperty(CARBON_CONFIG_PORT_OFFSET);

        try {
            return ((portOffset != null) ? Integer.parseInt(portOffset.trim()) : CARBON_DEFAULT_PORT_OFFSET);
        } catch (NumberFormatException e) {
            return CARBON_DEFAULT_PORT_OFFSET;
        }
    }


}
