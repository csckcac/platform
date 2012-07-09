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
import org.testng.Assert;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.wso2.carbon.automation.core.utils.coreutils.PlatformUtil;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.reportutills.CustomTestNgReportSetter;
import org.wso2.carbon.automation.core.utils.serverutils.ServerManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlatformSuiteManager implements ISuiteListener {

    private static final Log log = LogFactory.getLog(PlatformSuiteManager.class);
    ServerManager serverManager = null;
    List<ServerManager> serverList = new ArrayList<ServerManager>();
    EnvironmentBuilder environmentBuilder;
    ServerGroupManager serverGroupManager;

    /**
     * This method is invoked before the SuiteRunner starts.
     */
    public synchronized void onStart(ISuite suite) {
        PlatformUtil.setKeyStoreProperties();

        environmentBuilder = new EnvironmentBuilder();
        try {

            boolean deploymentEnabled =
                    environmentBuilder.getFrameworkSettings().getEnvironmentSettings().isEnableDipFramework();
            boolean startosEnabled =
                    environmentBuilder.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos();
            boolean builderEnabled =
                    environmentBuilder.getFrameworkSettings().getEnvironmentSettings().is_builderEnabled();
            List<String> defaultProductList =
                    environmentBuilder.getFrameworkSettings().getEnvironmentVariables().getProductList();

            serverGroupManager = new ServerGroupManager(0);
            //stratos user are populated to manager. Therefor product list not required
            if (startosEnabled) {
                new UserPopulator().populateUsers(null);
            } else if (deploymentEnabled) {
                if (suite.getParameter("server.list") != null) {
                    List<String> productList = Arrays.asList(suite.getParameter("server.list").split(","));
                    log.info("Starting servers...");
                    serverGroupManager.startServers(productList);
                    new UserPopulator().populateUsers(productList);
                } else {
                    log.info("Starting servers with default product list...");
                    serverGroupManager.startServers(defaultProductList);
                    new UserPopulator().populateUsers(defaultProductList);
                }
            } else if (builderEnabled) {
                log.info("Ignored - handled by PlatformExecution manager");
            } else {
                log.info("Server startup criterias do not match");
                assert false : "Invalid framework configuration, please update framework.properties file";
            }

        } catch (Exception e) {  /*cannot throw the exception */
            log.error(e);
            CustomTestNgReportSetter reportSetter = new CustomTestNgReportSetter();
            reportSetter.createReport(suite, e);
        }
    }

    /**
     * This method is invoked after the SuiteRunner has run all
     * the test suites.
     */

    public void onFinish(ISuite suite) {
        try {
            EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();

            if (!environmentBuilder.getFrameworkSettings().getEnvironmentSettings().is_builderEnabled()) {
                stopMultipleServers(suite.getParameter("server.list"));
            }

        } catch (Exception e) { /*cannot throw the exception */
            log.error(e);
            CustomTestNgReportSetter reportSetter = new CustomTestNgReportSetter();
            reportSetter.createReport(suite, e);
            Assert.fail("Fail to stop servers " + e);
        }
    }

    /**
     * Responsible for stopping multiple servers after test execution.
     * <p/>
     * Add the @AfterSuite TestNG annotation in the method overriding this method
     *
     * @param serverList server list required to run test scenario
     * @throws Exception if an error occurs while in server stop process
     */
    protected void stopMultipleServers(String serverList) throws Exception {
        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();

        if (environmentBuilder.getFrameworkSettings().getEnvironmentSettings().isEnableDipFramework()
            && !environmentBuilder.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {
            List<String> productList = Arrays.asList(serverList.split(","));
            log.info("Stopping all server");
            ServerGroupManager.shutdownServers(productList);
        }
    }
}
