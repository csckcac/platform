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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bps.integration.tests.util.DeploymentAdminServiceUtils;
import org.wso2.carbon.bpel.stub.mgt.BPELPackageManagementServiceStub;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

public class UndeploymentTestCase {

    private static final Log log = LogFactory.getLog(UndeploymentTestCase.class);
    private LoginLogoutUtil util = new LoginLogoutUtil();

    BPELPackageManagementServiceStub bpelPackageManagementServiceStub = null;


    @BeforeClass(groups = {"wso2.bps"})
    public void login() throws java.lang.Exception {
        log.info("Login in Undeployment Test...");
        ClientConnectionUtil.waitForPort(9443);
        bpelPackageManagementServiceStub = DeploymentAdminServiceUtils.getPackageManagementStub();
    }

    @AfterClass(groups = {"wso2.bps"})
    public void logout() throws java.lang.Exception {
        ClientConnectionUtil.waitForPort(9443);
        util.logout();
        log.info("Log Out in Undeployment Test...");
    }


    @Test(groups = {"wso2.bps", "e"}, dependsOnGroups = "d", description = "UnDeployment test")
    public void UndeploymentTestService() throws Exception {
        DeploymentAdminServiceUtils.undeploy("HelloWorld2", bpelPackageManagementServiceStub);
        DeploymentAdminServiceUtils.undeploy("TestPickOneWay", bpelPackageManagementServiceStub);
        DeploymentAdminServiceUtils.undeploy("CleanUpTest1", bpelPackageManagementServiceStub);
    }
}
