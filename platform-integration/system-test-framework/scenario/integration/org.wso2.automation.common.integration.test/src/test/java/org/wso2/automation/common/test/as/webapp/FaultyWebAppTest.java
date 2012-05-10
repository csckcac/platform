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
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.ArtifactDeployerUtil;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;
import org.wso2.platform.test.core.utils.webapputils.WebAppUtil;

import java.io.File;
import java.rmi.RemoteException;


/*
Deploy faulty webapp and then redeploy the correct webapp. After redeployment, do a HTTP GET
and verirfy whether the expected output is present.
 */
public class FaultyWebAppTest {

    private static final Log log = LogFactory.getLog(FaultyWebAppTest.class);
    private String backEndUrl = null;
    private String sessionCookie;
    private String webAppName;
    private String webAppURL;

    @BeforeTest(alwaysRun = true)
    public void initializeProperties() throws RemoteException,
                                              LoginAuthenticationExceptionException {
        log.info("Running Faulty webapp deployment test...");
        int userId = 1;
        String webappContext = "/SimpleServlet-faulty/simple-servlet";
        webAppName = "SimpleServlet-faulty.war";
        EnvironmentBuilder builder = new EnvironmentBuilder().as(userId);
        ManageEnvironment environment = builder.build();
        backEndUrl = environment.getAs().getBackEndUrl();
        sessionCookie = environment.getAs().getSessionCookie();
        webAppURL = environment.getAs().getWebAppURL() + webappContext;
    }

    @Test(groups = {"wso2.as"}, description = "Verify faulty webapp deployment", priority = 1)
    public void testFaultyWebApp() throws Exception {
        try {
            WebAppUtil.waitForWebAppDeployment(webAppURL, "Hello");
        } catch (Exception ignored) {
            log.info("Faulty webapp not deployed");
        }
    }

    @Test(groups = {"wso2.as"}, description = "Redeploy correct webApp again", priority = 2)
    public void testRedeployCorrectWebApp() throws Exception {
        log.info("Redeploy valid webapp");
        String webAppArtifactPath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator + "artifacts" +
                File.separator + "AS" + File.separator + "war" + File.separator + "duplicateWar" +
                File.separator + webAppName;

        ArtifactDeployerUtil deployerUtil = new ArtifactDeployerUtil();
        deployerUtil.warFileUploder(sessionCookie, backEndUrl, webAppArtifactPath);
        WebAppUtil.waitForWebAppDeployment(webAppURL, "Hello");
        log.info("Successfully redeployed valid webapp");
    }
}
