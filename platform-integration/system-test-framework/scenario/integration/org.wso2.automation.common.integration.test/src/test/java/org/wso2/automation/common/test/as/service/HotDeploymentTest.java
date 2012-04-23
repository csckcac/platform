/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.automation.common.test.as.service;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.aarservices.stub.ExceptionException;
import org.wso2.carbon.admin.service.AdminServiceCarbonServerAdmin;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.ArtifactCleanerUtil;
import org.wso2.platform.test.core.utils.ArtifactDeployerUtil;
import org.wso2.platform.test.core.utils.ClientConnectionUtil;
import org.wso2.platform.test.core.utils.LoginLogoutUtil;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClientUtils;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import java.io.File;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * Test to verify fix for "Deployers don't work when Carbon is in Maintenance mode"
 * https://wso2.org/jira/browse/CARBON-10973
 */
public class HotDeploymentTest {

    private static final Log log = LogFactory.getLog(HotDeploymentTest.class);
    private static String AXIS2SERVICE_EPR;
    private EnvironmentBuilder builder;
    private ManageEnvironment environment;
    private static final long TIMEOUT = 2 * 60 * 1000;
    private static final int userId = 1;

    @BeforeTest(alwaysRun = true)
    public void initializeProperties() {
        log.info("Running maintenance mode hot deployment test...");
        String serviceName = "Axis2Service";
        builder = new EnvironmentBuilder().as(userId);
        environment = builder.build();
        AXIS2SERVICE_EPR = environment.getAs().getServiceUrl() + "/" + serviceName;
    }

    @Test(groups = {"wso2.as"}, description = "AAR upload and invocation", priority = 1)
    public void testAarUpload() throws InterruptedException, RemoteException,
                                       org.wso2.carbon.server.admin.stub.Exception,
                                       ExceptionException {
        log.info("Running AAR upload test...");

        AdminServiceCarbonServerAdmin serverAdmin =
                new AdminServiceCarbonServerAdmin(environment.getAs().getBackEndUrl());
        ArtifactDeployerUtil artifactUtils = new ArtifactDeployerUtil();
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator +
                              "artifacts" + File.separator + "AS" + File.separator + "aar" + File.separator +
                              "Axis2Service.aar";
        log.info("Deploying - Axis2Service.aar ...");
        artifactUtils.aarFileUploder(environment.getAs().getSessionCookie(), environment.getAs().getBackEndUrl(),
                                     "Axis2Service.aar", resourcePath);
        serverAdmin.restartGracefully(environment.getAs().getSessionCookie());//restart the server gracefully
        Thread.sleep(5000); //This sleep should be there, since we have to give some time for
        //the server to initiate restart. Otherwise, "waitForPort" call
        //might happen before server initiate restart
    }

    @Test(groups = {"wso2.as"}, description = "Verity service deployment after graceful restart", priority = 2)
    public void testInvokeService() throws InterruptedException, AxisFault {
        int httpsPort = Integer.parseInt(environment.getAs().getProductVariables().getHttpsPort());
        String operation = "echoInt";
        String expectedValue = "123";
        String hostName = environment.getAs().getProductVariables().getHostName();
        ClientConnectionUtil.waitForPort(httpsPort, TIMEOUT, true, hostName);
        LoginLogoutUtil loginLogoutUtil = new LoginLogoutUtil(httpsPort, hostName);
        Thread.sleep(15000);
        UserInfo info = UserListCsvReader.getUserInfo(1);
        log.info("Login after server restart");
        loginLogoutUtil.login(info.getUserName(), info.getPassword(),
                              environment.getAs().getBackEndUrl()); //login to verity server restart
        log.info("Server restart was successful");

        AxisServiceClientUtils.waitForServiceDeployment(AXIS2SERVICE_EPR); // wait for service deployment
        long deploymentDelay =
                builder.getFrameworkSettings().getEnvironmentVariables().getDeploymentDelay();
        Thread.sleep(deploymentDelay);//force wait till service deployment
        OMElement result = new AxisServiceClient().sendReceive(createPayLoad(operation, expectedValue),
                                                               AXIS2SERVICE_EPR, operation);
        log.debug("Response returned " + result);
        assertTrue((result.toString().indexOf(expectedValue) >= 1));
    }

    @AfterClass(groups = {"wso2.as"}, description = "Clean the deployed artifacts")
    public void testCleanup() throws InterruptedException, RemoteException {
        builder = new EnvironmentBuilder().as(userId);
        environment = builder.build();
        ArtifactCleanerUtil artifactCleanerUtil = new ArtifactCleanerUtil();
        artifactCleanerUtil.deleteServiceByGroup(environment.getAs().getSessionCookie(),
                                                 environment.getAs().getBackEndUrl(),"Axis2Service.aar");
    }

    private static OMElement createPayLoad(String operation, String expectedValue) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://service.carbon.wso2.org", "ns1");
        OMElement method = fac.createOMElement(operation, omNs);
        OMElement value = fac.createOMElement("x", omNs);
        value.addChild(fac.createOMText(value, expectedValue));
        method.addChild(value);
        log.debug("Created payload is :" + method);
        return method;
    }
}
