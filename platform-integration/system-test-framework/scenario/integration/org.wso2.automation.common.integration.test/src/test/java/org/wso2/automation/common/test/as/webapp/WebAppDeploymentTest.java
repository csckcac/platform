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

package org.wso2.automation.common.test.as.webapp;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceWebAppAdmin;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;
import org.wso2.platform.test.core.utils.webapputils.WebAppUtil;

import java.io.File;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class WebAppDeploymentTest {
    private static final Log log = LogFactory.getLog(WebAppDeploymentTest.class);
    private AdminServiceWebAppAdmin adminServiceWebAppAdmin;
    private static final String webAppName = "SimpleServlet";
    private static final String webappContext = "/SimpleServlet/simple-servlet";
    private String webAppURL;
    private String sessionCookie;


    @BeforeTest(alwaysRun = true)
    public void initializeProperties() throws AxisFault {
        log.info("Running WebApp redeployment test service stat test...");
        int userId = 1;
        EnvironmentBuilder builder = new EnvironmentBuilder().as(userId);
        ManageEnvironment environment = builder.build();
        webAppURL = environment.getAs().getWebAppURL() + webappContext;
        log.info("WebApp URL :" + webAppURL);
        sessionCookie = environment.getAs().getSessionCookie();
        adminServiceWebAppAdmin = new AdminServiceWebAppAdmin(environment.getAs().getBackEndUrl());
    }

    @Test(groups = {"wso2.as"}, description = "Verify simple webApp deployment", priority = 1)
    public void testWebAppDeployment() throws Exception {
        WebAppUtil.waitForWebAppDeployment(webAppURL, "Hello"); //wait for web app deployment
        assertTrue(adminServiceWebAppAdmin.stopWebApp(sessionCookie, webAppName + ".war"),
                   "fail to stop webapp"); //stop web app
        log.debug("WebApp stopped");
        deleteStoppedWebApp(sessionCookie, webAppName + ".war");//delete web app
        waitForWebappDeletion();//wait for deletion
        log.info("WebApp deleted successfully");

        String webAppArtifactPath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator + "artifacts" +
                File.separator + "AS" + File.separator + "war" + File.separator +
                webAppName + ".war";

        deployWebapp(webAppArtifactPath);
        WebAppUtil.waitForWebAppDeployment(webAppURL, "Hello");
        log.info("Successfully redeployed valid webApp");
    }

    private void waitForWebappDeletion() throws Exception {
        WebAppUtil.waitForWebAppUnDeployment(webAppURL, "Hello");
    }

    private void deployWebapp(String filePath) throws RemoteException {
        adminServiceWebAppAdmin.warFileUplaoder(sessionCookie, filePath);
    }

    private void deleteStoppedWebApp(String sessionCookie, String fileName) throws RemoteException {
        adminServiceWebAppAdmin.deleteStoppedWebapps(sessionCookie, fileName);
    }
}
