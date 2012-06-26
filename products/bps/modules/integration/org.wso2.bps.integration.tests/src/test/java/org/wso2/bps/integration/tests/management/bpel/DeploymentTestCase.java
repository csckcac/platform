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

package org.wso2.bps.integration.tests.management.bpel;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bps.integration.tests.util.DeploymentAdminServiceUtils;
import org.wso2.bps.integration.tests.util.FrameworkSettings;
import org.wso2.carbon.bpel.stub.mgt.BPELPackageManagementServiceStub;
import org.wso2.carbon.bpel.stub.upload.BPELUploaderStub;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

//import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
//    import org.wso2.carbon.integration.framework.utils.

public class DeploymentTestCase {
    private static final Log log = LogFactory.getLog(DeploymentTestCase.class);

    private BPELUploaderStub bpelUploaderStub = null;
    private BPELPackageManagementServiceStub bpelPackageManagementServiceStub = null;

    @BeforeClass(groups = {"wso2.bps", "a"})
    public void login() throws java.lang.Exception {
        log.info("Logging in for Deployment Test");
        ClientConnectionUtil.waitForPort(FrameworkSettings.HTTPS_PORT);
        bpelUploaderStub = DeploymentAdminServiceUtils.getBpelUploaderStub();
        bpelPackageManagementServiceStub = DeploymentAdminServiceUtils.getPackageManagementStub();
    }

    @AfterClass(groups = {"wso2.bps"})
    public void logout() throws java.lang.Exception {
        ClientConnectionUtil.waitForPort(FrameworkSettings.HTTPS_PORT);
        LoginLogoutUtil util = new LoginLogoutUtil();
        util.logout();
        log.info("Logging out in Deployment Test...");
    }

    @Test(groups = {"wso2.bps", "a"}, description = "Process Deployment tests")
    public void deploymentTestService() throws Exception {
        log.info("Starting Deployment Tests...");

        DeploymentAdminServiceUtils.deployPackage("HelloWorld2", "HelloService", bpelUploaderStub);
        DeploymentAdminServiceUtils.checkProcessDeployment("HelloWorld2", bpelPackageManagementServiceStub);

        DeploymentAdminServiceUtils.deployPackage("TestPickOneWay", "PickService", bpelUploaderStub);
        DeploymentAdminServiceUtils.checkProcessDeployment("TestPickOneWay", bpelPackageManagementServiceStub);

        DeploymentAdminServiceUtils.deployPackage("CleanUpTest1", "CleanUpTest1Service",
                bpelUploaderStub);
        DeploymentAdminServiceUtils.checkProcessDeployment("CleanUpTest1", bpelPackageManagementServiceStub);
    }

    @Test(groups = {"wso2.bps", "a"}, description = "Process Deployment tests - failure scenarios")
    public void deploymentFailureTest() throws Exception {
        log.info("Starting Deployment Failure Tests...");

        DeploymentAdminServiceUtils.deployPackage("ExtVar", "ExtVarService", bpelUploaderStub);
        DeploymentAdminServiceUtils.checkProcessDeployment("ExtVar", false, bpelPackageManagementServiceStub);
    }
}

