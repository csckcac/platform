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

package org.wso2.carbon.automation.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.IExecutionListener;
import org.wso2.carbon.automation.core.utils.axis2serverutils.BackendServer;
import org.wso2.carbon.automation.core.utils.axis2serverutils.SampleAxis2Server;
import org.wso2.carbon.automation.core.utils.coreutils.PlatformUtil;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * implementation of  testNG IExecutionListener, this class will call before all test suite..
 * However if you use multiple test module, onExecutionStart and onExecutionFinish methods will call multiple times
 */
public class PlatformExecutionManager implements IExecutionListener {
    private static final Log log = LogFactory.getLog(PlatformExecutionManager.class);
    private ServerGroupManager serverGroupManager;
    private boolean builderEnabled;
    private List<String> serverList;
    protected BackendServer backendServer;


    /**
     * calls before all test suites execution
     */
    public void onExecutionStart() {
        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();
        PlatformUtil.setKeyStoreProperties(); //set keyStore properties
        builderEnabled =
                environmentBuilder.getFrameworkSettings().getEnvironmentSettings().is_builderEnabled();
        if (builderEnabled) {
            serverList = getServerList();
            assert serverList != null : "server list not provided, cannot start servers";
            int defaultPortOffset = 0;
            serverGroupManager = new ServerGroupManager(defaultPortOffset);
            for (String server : serverList) {
                try {
                    serverGroupManager.startServersForBuilder(server);

                    if (server.equals(ProductConstant.ESB_SERVER_NAME)) {
                        startSimpleAxis2Server();
                    }
                } catch (IOException e) {
                    log.error("Unable to start servers " + e);
                }
                try {
                    new UserPopulator().populateUsers(serverList);
                } catch (Exception e) {
                    log.error("Unable to populate users in to servers " + e);
                }
            }
        }
    }


    /**
     * calls after all test suite execution
     */
    public void onExecutionFinish() {
        if (builderEnabled && serverList != null) {
            for (String server : serverList) {
                FrameworkProperties frameworkProperties = FrameworkFactory.getFrameworkProperties(server);
                try {
                    serverGroupManager.stopServer(frameworkProperties);
                    if (backendServer != null && backendServer.isStarted()) {
                        stopSimpleAxis2Server();
                    }
                } catch (Exception e) {
                    log.error("Unable to stop servers " + e);
                }
            }
        }
    }

    private void startSimpleAxis2Server() throws IOException {
        backendServer = new SampleAxis2Server();
        backendServer.deployService(SampleAxis2Server.LB_SERVICE_1);
        backendServer.start();
        backendServer.deployService(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
        backendServer.deployService(SampleAxis2Server.SECURE_STOCK_QUOTE_SERVICE);

    }

    private void stopSimpleAxis2Server() {
        try {
            backendServer.stop();
        } catch (IOException e) {
            log.warn("Error while shutting down the backend server", e);
        }
    }

    private List<String> getServerList() {
        if (System.getProperty("server.list") != null) {
            return Arrays.asList(System.getProperty("server.list").split(","));
        }
        return null;
    }
}
