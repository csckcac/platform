/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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


package org.wso2.carbon.automation.core.utils.serverutils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.server.admin.ui.ServerAdminClient;
import org.wso2.carbon.utils.ArchiveManipulator;
import org.wso2.carbon.utils.FileManipulator;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.automation.core.utils.ClientConnectionUtil;
import org.wso2.carbon.automation.core.utils.LoginLogoutUtil;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.coreutils.CodeCoverageUtils;
import org.wso2.carbon.automation.core.utils.coreutils.InputStreamHandler;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;

import java.io.File;
import java.io.IOException;

/**
 * A set of utility methods such as starting & stopping a Carbon server.
 */
public class ServerUtils {
    private static final Log log = LogFactory.getLog(ServerUtils.class);

    private Process process;
    private String carbonHome;
    private String originalUserDir = null;
    private InputStreamHandler inputStreamHandler;
    private static final String DEFAULT_SCRIPT_NAME = "wso2server";

    private static final String SERVER_SHUTDOWN_MESSAGE = "Halting JVM";
    private static final long DEFAULT_START_STOP_WAIT_MS = 1000 * 60 * 5;
    private int defaultHttpsPort = 9443;

    public synchronized void startServerUsingCarbonHome(String carbonHome, final int portOffset,
                                                        FrameworkProperties frameworkProperties) {
        startServerUsingCarbonHome(carbonHome, carbonHome, DEFAULT_SCRIPT_NAME, portOffset, null, frameworkProperties);
    }

    public synchronized void startServerUsingCarbonHome(String carbonHome, String carbonFolder,
                                                        String scriptName, final int portOffset,
                                                        final String carbonManagementContext,
                                                        final FrameworkProperties frameworkProperties) {
        if (process != null) { // An instance of the server is running
            return;
        }
        Process tempProcess;
        try {
            CodeCoverageUtils.instrument(carbonFolder);
//            FrameworkSettings.init();
            defaultHttpsPort = Integer.parseInt(frameworkProperties.getProductVariables().getHttpsPort());
            int defaultHttpPort = Integer.parseInt(frameworkProperties.getProductVariables().getHttpPort());
            System.setProperty(ServerConstants.CARBON_HOME, carbonFolder);
            originalUserDir = System.getProperty("user.dir");
            System.setProperty("user.dir", carbonFolder);
            File commandDir = new File(carbonHome);
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                commandDir = new File(carbonHome + File.separator + "bin");
                tempProcess =
                        Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", scriptName + ".bat",
                                                               "-DportOffset=" + String.valueOf(portOffset),
                                                               "-Demma.properties=" + System.getProperty("emma.properties"),
                                                               "-Demma.rt.control.port=" + (47653 + portOffset)},
                                                  null, commandDir);
            } else {
                tempProcess =
                        Runtime.getRuntime().exec(new String[]{"sh", "bin/" + scriptName + ".sh",
                                                               "-DportOffset=" + String.valueOf(portOffset),
                                                               "-Demma.properties=" + System.getProperty("emma.properties"),
                                                               "-Demma.rt.control.port=" + (47653 + portOffset)},
                                                  null, commandDir);
            }
            InputStreamHandler errorStreamHandler =
                    new InputStreamHandler("errorStream", tempProcess.getErrorStream());
            inputStreamHandler = new InputStreamHandler("inputStream", tempProcess.getInputStream());

            // start the stream readers
            inputStreamHandler.start();
            errorStreamHandler.start();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        log.info("Shutting down server...");
                        if (carbonManagementContext == null || carbonManagementContext.trim().equals("")) {
                            shutdown(portOffset, frameworkProperties);
                        } else {
                            shutdown(portOffset, carbonManagementContext, frameworkProperties);
                        }
                    } catch (Exception e) {
                        log.error("Cannot shutdown server", e);
                    }
                }
            });
            ClientConnectionUtil.waitForPort(defaultHttpsPort,
                                             DEFAULT_START_STOP_WAIT_MS, false, frameworkProperties.getProductVariables().getHostName());
            ClientConnectionUtil.waitForPort(defaultHttpPort + portOffset,
                                             DEFAULT_START_STOP_WAIT_MS, false, frameworkProperties.getProductVariables().getHostName());
            if (carbonManagementContext == null || carbonManagementContext.trim().equals("")) {
                ClientConnectionUtil.waitForLogin(defaultHttpsPort, frameworkProperties.getProductVariables().getHostName(),
                                                  frameworkProperties.getProductVariables().getBackendUrl());
            } else {
                ClientConnectionUtil.waitForLogin(defaultHttpsPort, carbonManagementContext,
                                                  frameworkProperties.getProductVariables().getBackendUrl());
            }
            log.info("Server started successfully.");
        } catch (IOException e) {
            throw new RuntimeException("Unable to start server", e);
        }
        process = tempProcess;
    }

    public synchronized String setUpCarbonHome(String carbonServerZipFile)
            throws IOException {
        if (process != null) { // An instance of the server is running
            return carbonHome;
        }
        CodeCoverageUtils.init();
        int indexOfZip = carbonServerZipFile.lastIndexOf(".zip");
        if (indexOfZip == -1) {
            throw new IllegalArgumentException(carbonServerZipFile + " is not a zip file");
        }
        String fileSeparator = (File.separator.equals("\\")) ? "\\" : "/";
        if (fileSeparator.equals("\\")) {
            carbonServerZipFile = carbonServerZipFile.replace("/", "\\");
        }
        String extractedCarbonDir =
                carbonServerZipFile.substring(carbonServerZipFile.lastIndexOf(fileSeparator) + 1,
                                              indexOfZip);
        FileManipulator.deleteDir(extractedCarbonDir);
        String extractDir = "carbontmp" + System.currentTimeMillis();
        new ArchiveManipulator().extract(carbonServerZipFile, extractDir);
        String baseDir = (System.getProperty("basedir", ".")) + File.separator + "target";
        return carbonHome =
                new File(baseDir).getAbsolutePath() + File.separator + extractDir + File.separator +
                extractedCarbonDir;
    }

    public synchronized void shutdown(int portOffset, String carbonManagementContext,
                                      FrameworkProperties properties) throws Exception {
        if (process != null) {
            if (ClientConnectionUtil.isPortOpen(defaultHttpsPort, properties.getProductVariables().getHostName())) {
                if (carbonManagementContext == null || carbonManagementContext.trim().equals("")) {
                    shutdownServer(portOffset, properties);
                } else {
                    shutdownServer(portOffset, carbonManagementContext, properties);
                }
                long time = System.currentTimeMillis() + DEFAULT_START_STOP_WAIT_MS;
                while (!inputStreamHandler.getOutput().contains(SERVER_SHUTDOWN_MESSAGE) &&
                       System.currentTimeMillis() < time) {
                    // wait until server shutdown is completed
                }
                log.info("Server stopped successfully...");
            }
            process.destroy();
            process = null;
            CodeCoverageUtils.generateReports();
            System.clearProperty(ServerConstants.CARBON_HOME);
            System.setProperty("user.dir", originalUserDir);
        }
    }

    public synchronized void shutdown(int portOffset, FrameworkProperties properties)
            throws Exception {
        if (process != null) {
            if (ClientConnectionUtil.isPortOpen(defaultHttpsPort, properties.getProductVariables().getHostName())) {
                shutdownServer(portOffset, properties);
                long time = System.currentTimeMillis() + DEFAULT_START_STOP_WAIT_MS;
                while (!inputStreamHandler.getOutput().contains(SERVER_SHUTDOWN_MESSAGE) &&
                       System.currentTimeMillis() < time) {
                    // wait until server shutdown is completed
                }

                log.info("Server stopped successfully...");
            }
            process.destroy();
            process = null;
            CodeCoverageUtils.generateReports();
            System.clearProperty(ServerConstants.CARBON_HOME);
            System.setProperty("user.dir", originalUserDir);
        }
    }

    private void shutdownServer(int portOffset, FrameworkProperties properties) throws Exception {
        shutdownServer(portOffset, null, properties);
    }

    private void shutdownServer(int portOffset, String carbonManagementContext,
                                FrameworkProperties properties) throws Exception {
        int httpsPort = defaultHttpsPort;
        UserInfo userInfo = UserListCsvReader.getUserInfo(ProductConstant.ADMIN_USER_ID);
        try {
            String serviceBaseURL;
            String sessionCookie;
            if (carbonManagementContext == null || carbonManagementContext.trim().equals("")) {
//                serviceBaseURL = "https://localhost:" + httpsPort + "/services/";
                sessionCookie = new LoginLogoutUtil(httpsPort, properties.getProductVariables().
                        getHostName()).login(userInfo.getUserName(), userInfo.getPassword(), properties.getProductVariables().getBackendUrl());
            } else {
//                serviceBaseURL = "https://localhost:" + httpsPort + "/" + carbonManagementContext + "/services/";
                sessionCookie = new LoginLogoutUtil(httpsPort, properties.getProductVariables().
                        getHostName()).login(userInfo.getUserName(), userInfo.getPassword(), properties.getProductVariables().getBackendUrl());
            }
            //shutdown the server through ServerAdmin
            ServerAdminClient serverAdminClient =
                    new ServerAdminClient(null, properties.getProductVariables().getBackendUrl(), sessionCookie, null);
            serverAdminClient.shutdownGracefully();
        } catch (Exception e) {
            log.error("Error when shutting down the server.", e);
            throw e;
        }
    }

}
