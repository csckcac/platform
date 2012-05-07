/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.platform.test.core;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.carbon.admin.service.AdminServiceCarbonServerAdmin;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.platform.test.core.utils.ClientConnectionUtil;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.dbutils.DatabaseFactory;
import org.wso2.platform.test.core.utils.dbutils.DatabaseManager;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.fileutils.FileManager;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.platform.test.core.utils.productutils.PackageCreator;
import org.wso2.platform.test.core.utils.serverutils.ServerManager;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertFalse;

public class ServerGroupManager {
    private static final Log log = LogFactory.getLog(ServerGroupManager.class);

    private static boolean serversRunning = false;
    private static final long TIMEOUT = 60 * 1000;

    private static List<ServerManager> serverManagerList = new ArrayList<ServerManager>();

    public static synchronized void startServers(List<String> productList) throws Exception {
        log.info("Server starting...");
        log.info("Server running " + serversRunning);
        EnvironmentBuilder env = new EnvironmentBuilder();
        if (!serversRunning) {

            Assert.assertNotNull("Deployment Framework Home not provided", env.getFrameworkSettings().
                    getEnvironmentVariables().getDeploymentFrameworkPath());
            Assert.assertTrue(PackageCreator.createPackage());

            ServerManager serverManager;

            try {
                OMElement commonConfig = AXIOMUtil.stringToOM(FileManager.readFile
                        (env.getFrameworkSettings().getEnvironmentVariables().
                                getDeploymentFrameworkPath() + File.separator + "config" +
                         File.separator + "commonConfig.xml").trim());
                OMElement databaseMount = commonConfig.getFirstChildWithName(new QName("MountDatabase"));

                if (databaseMount != null) {
                    createRegistryDB(databaseMount);
                    log.info("WSO2 Registry Created");
                }

                for (String product : productList) {
                    String carbonHome = ProductConstant.getCarbonHome(product);
                    OMElement databaseConfig = getCurrentDatabaseConfig(carbonHome);
                    if (!"org.h2.Driver".equalsIgnoreCase
                            (databaseConfig.getFirstChildWithName(new QName("driverName")).getText().trim())) {
                        createRegistryDB(databaseConfig);
                        log.info("local Registry Created");
                    }
                    serverManager = new ServerManager(carbonHome);
                    serverManager.start();
                    serversRunning = true;
//                    serverManagerList.add(serverManager);
                }

            } catch (ServerConfigurationException e) {
                log.error("Server configuration error " + e.getMessage());
                throw new ServerConfigurationException("Server configuration error " + e.getMessage());
            } catch (ClassNotFoundException e) {
                log.error("Database Driver Not Found " + e.getMessage());
                throw new ClassNotFoundException("Database Driver Not Found " + e.getMessage());
            } catch (SQLException e) {
                log.error("Database Server connection failed " + e.getMessage());
                throw new SQLException("Database Server connection failed " + e.getMessage());
            } catch (IOException e) {
                log.error("Exception while reading  commonConfig.xml in deployment framework " + e.getMessage());
                throw new IOException("Exception while reading  commonConfig.xml in deployment framework " + e.getMessage());
            } catch (XMLStreamException e) {
                log.error("Exception while reading  commonConfig.xml in deployment framework " + e.getMessage());
                throw new XMLStreamException("Exception while reading  commonConfig.xml in deployment framework " + e.getMessage());
            }

            //create users in each server
            new UserPopulator().populateUsers(productList);

//            Runtime.getRuntime().addShutdownHook(new Thread() {
//                public void run() {
//                    try {
//                        log.info("Shutting down servers...");
//                        for (ServerManager sm : serverManagerList) {
//                            sm.shutdown();
//                            log.info("Shutting down Server");
//                        }
//                        serverManagerList.clear();
//                    } catch (Exception e) {
//                        log.error(e);
//                    }
//                }
//            });
        }
    }

    private static String getDatabaseName(String driverName, String url) {
        String databaseName = null;
        if ("com.mysql.jdbc.Driver".equalsIgnoreCase(driverName)) {
//          databaseName = url.split("\\?")[0].split("//?")[2];
            databaseName = url.substring(url.lastIndexOf('/') + 1, url.indexOf('?'));
        } else if ("oracle.jdbc.driver.OracleDriver".equalsIgnoreCase(driverName)) {
//          databaseName = url.split("/")[1].split("@")[0];
            databaseName = url.substring(url.lastIndexOf(':') + 1);
        } else if ("org.h2.Driver".equalsIgnoreCase(driverName)) {

        } else {
            Assert.fail("could not find database name. Not implemented for the driver " + driverName);
        }
        Assert.assertNotNull("Database name null", databaseName);
        return databaseName;
    }

    private static String getJdbcUrl(String driverName, String url) {
        String jdbc = null;
        if ("com.mysql.jdbc.Driver".equalsIgnoreCase(driverName)) {
            jdbc = url.substring(0, url.lastIndexOf('/'));
        } else if ("oracle.jdbc.driver.OracleDriver".equalsIgnoreCase(driverName)) {
            jdbc = url.substring(0, url.lastIndexOf(':'));
        } else if ("org.h2.Driver".equalsIgnoreCase(driverName)) {

        } else {
            Assert.fail("could not find server jdbc url. Not implemented for the driver " + driverName);
        }
        Assert.assertNotNull("jdbc url null", jdbc);
        return jdbc;
    }

    private static OMElement getCurrentDatabaseConfig(String carbonHome) {
        OMElement wso2registry = null;
        try {
            wso2registry = AXIOMUtil.stringToOM(FileManager.readFile(carbonHome + File.separator + "repository" + File.separator + "conf" + File.separator + "registry.xml").trim());

        } catch (IOException e) {
            log.error("Exception while reading  registry.xml in deployment framework " + e.getMessage());
            Assert.fail("Exception while reading  registry.xml in deployment framework " + e.getMessage());
        } catch (XMLStreamException e) {
            log.error("Exception while reading  registry.xml in deployment framework " + e.getMessage());
            Assert.fail("Exception while reading  registry.xml in deployment framework " + e.getMessage());
        }
        OMElement currentDBConfig = wso2registry.getFirstChildWithName(new QName("currentDBConfig"));
        Iterator dbConfigList = wso2registry.getChildrenWithName(new QName("dbConfig"));

        while (dbConfigList.hasNext()) {
            OMElement dbConfig = (OMElement) dbConfigList.next();
            String value = dbConfig.getAttributeValue(new QName("name"));
            if (currentDBConfig.getText().trim().equals(value)) {
                return dbConfig;

            }

        }
        Assert.fail("Database Configuration not Found in registry.xml ");
        return null;
    }

    private static void createRegistryDB(OMElement dbConfig)
            throws ClassNotFoundException, SQLException {
        DatabaseManager databaseManager;
        String driverName;
        String url;
        String userName;
        String password;
        String databaseName;
        String jdbc;

        driverName = dbConfig.getFirstChildWithName(new QName("driverName")).getText().trim();
        url = dbConfig.getFirstChildWithName(new QName("url")).getText().trim();
        userName = dbConfig.getFirstChildWithName(new QName("userName")).getText().trim();
        password = dbConfig.getFirstChildWithName(new QName("password")).getText().trim();

        databaseName = getDatabaseName(driverName, url);
        jdbc = getJdbcUrl(driverName, url);

        databaseManager = DatabaseFactory.getDatabaseConnector(driverName, jdbc, userName, password);
        databaseManager.executeUpdate("DROP DATABASE IF EXISTS " + databaseName);
        databaseManager.executeUpdate("CREATE DATABASE " + databaseName);
        log.info("Database created");
        databaseManager.disconnect();
    }

    public static synchronized void shutdownServers(List<String> productList) throws Exception {
        if (serversRunning) {
            serversRunning = false;
            UserInfo adminDetails = UserListCsvReader.getUserInfo(0);//get admin user of all products
            AdminServiceCarbonServerAdmin adminServiceCarbonServerAdmin;
            try {
                for (String product : productList) {
                    FrameworkProperties properties = FrameworkFactory.getFrameworkProperties(product);
                    String hostName = properties.getProductVariables().getHostName();
                    String backEndUrl = properties.getProductVariables().getBackendUrl();
                    String sessionCookieUser = login(adminDetails.getUserName(), adminDetails.getPassword(), backEndUrl, hostName);
                    adminServiceCarbonServerAdmin = new AdminServiceCarbonServerAdmin(backEndUrl);
                    adminServiceCarbonServerAdmin.shutdownGracefully(sessionCookieUser);
                    waitForServerShutDown(Integer.parseInt(properties.getProductVariables().
                            getHttpsPort()), properties.getProductVariables().getHostName());
                    assertFalse(ClientConnectionUtil.isPortOpen(Integer.parseInt(properties.getProductVariables().
                            getHttpsPort()), properties.getProductVariables().getHostName()),
                                "Port " + Integer.parseInt(properties.getProductVariables().getHttpsPort()) +
                                " shouldn't be open when the server is gracefully shutting down");

                }
            } catch (Exception e) {
                log.error("Error when shutting down the server.", e);
                throw new Exception("Error when shutting down the server.", e);
            }
        }
    }

    private static void waitForServerShutDown(int port, String hostName) {
        long startTime = System.currentTimeMillis();
        boolean isPortOpen = true;
        while ((System.currentTimeMillis() - startTime) < TIMEOUT && isPortOpen) {
            Socket socket = null;
            try {
                InetAddress address = InetAddress.getByName(hostName);
                socket = new Socket(address, port);
                isPortOpen = socket.isConnected();
                if (isPortOpen) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    return;
                }
            } catch (IOException e) {
                log.info("Cannot create the socket");
                return;
            } finally {
                try {
                    if ((socket != null) && (!socket.isConnected())) {
                        socket.close();
                    }
                } catch (IOException e) {
                    log.error("Can not close the socket with is used to check the server status ",
                              e);
                }
            }
        }
        throw new RuntimeException("Port " + port + " is still open");
    }

    private static String login(String userName, String password, String backEndUrl,
                                String hostName)
            throws LoginAuthenticationExceptionException, RemoteException {
        AdminServiceAuthentication loginClient = new AdminServiceAuthentication(backEndUrl);
        return loginClient.login(userName, password, hostName);
    }
}