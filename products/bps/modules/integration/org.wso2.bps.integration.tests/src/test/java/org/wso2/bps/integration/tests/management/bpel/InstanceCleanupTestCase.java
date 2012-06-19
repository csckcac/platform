/*
 *
 *   Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.bps.integration.tests.management.bpel;


import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bps.integration.tests.util.BPSMgtUtils;
import org.wso2.bps.integration.tests.util.BPSTestUtils;
import org.wso2.bps.integration.tests.util.FrameworkSettings;
import org.wso2.carbon.bpel.stub.mgt.InstanceManagementServiceStub;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

import java.util.ArrayList;
import java.util.List;

//import org.wso2.bps.test.utils.BPSMgtUtils;
//import org.wso2.bps.test.utils.BPSTestUtils;
//import org.wso2.carbon.bpel.stub.mgt.BPELPackageManagementServiceStub;
//import org.wso2.carbon.bpel.stub.upload.BPELUploaderStub;
//import org.wso2.carbon.integration.framework.utils.FrameworkSettings;


public class InstanceCleanupTestCase {

    private static final Log log = LogFactory.getLog(DeploymentTestCase.class);
    private LoginLogoutUtil util = new LoginLogoutUtil();


    final String INSTANCE_CLEANUP_TEST1_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                                                      ":" + FrameworkSettings.HTTPS_PORT +
                                                      "/services/CleanUpTest1Service";
    final String INSTANCE_MANAGEMENT_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                                                   ":" + FrameworkSettings.HTTPS_PORT +
                                                   "/services/InstanceManagementService";

    InstanceManagementServiceStub instanceManagementServiceStub = null;


    @BeforeClass(groups = {"wso2.bps"})
    public void login() throws Exception {
        log.info("Login in CleanUp Test...");
        ClientConnectionUtil.waitForPort(9443);
        String loggedInSessionCookie = util.login();


        instanceManagementServiceStub = new InstanceManagementServiceStub(INSTANCE_MANAGEMENT_SERVICE_URL);
        ServiceClient instanceManagementServiceClient = instanceManagementServiceStub._getServiceClient();
        Options instanceManagementServiceClientOptions = instanceManagementServiceClient.getOptions();
        instanceManagementServiceClientOptions.setManageSession(true);
        instanceManagementServiceClientOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                                                           loggedInSessionCookie);


    }

    @AfterClass(groups = {"wso2.bps"})
    public void logout() throws java.lang.Exception {
        ClientConnectionUtil.waitForPort(9443);
        util.logout();
        log.info("Log Out in Instance Cleanup...");
    }


    @Test(groups = {"wso2.bps", "c"}, dependsOnGroups = "b", description = "Instance cleanup test")
    public void instanceCleanupTestService() throws Exception {


        BPSMgtUtils.listInstances(instanceManagementServiceStub, 0,
                "{http://ode/bpel/unit-test}HelloWorld2");
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">Hello World<");

        BPSTestUtils.sendRequest(INSTANCE_CLEANUP_TEST1_SERVICE_URL,
                                 "cleanUpTest1",
                                 "<un:cleanUpTest1 xmlns:un=\"http://ode/bpel/unit-test.wsdl\">" +
                                 "<TestPart>Hello</TestPart></un:cleanUpTest1>",
                                 1,
                                 expectedOutput,
                                 true);

        List<String> iids = BPSMgtUtils.listInstances(instanceManagementServiceStub, 1,
                "{http://ode/bpel/unit-test}HelloWorld2");
        BPSMgtUtils.getInstanceInfo(instanceManagementServiceStub, "COMPLETED", null, null, iids);
        BPSMgtUtils.deleteInstances(instanceManagementServiceStub, 1);
    }
}
