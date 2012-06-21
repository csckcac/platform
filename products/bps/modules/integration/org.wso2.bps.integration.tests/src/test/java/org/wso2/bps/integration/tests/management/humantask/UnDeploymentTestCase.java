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

package org.wso2.bps.integration.tests.management.humantask;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bps.integration.tests.util.FrameworkSettings;
import org.wso2.bps.integration.tests.util.HumanTaskTestConstants;
import org.wso2.carbon.humantask.stub.mgt.HumanTaskPackageManagementStub;
import org.wso2.carbon.humantask.stub.mgt.PackageManagementException;
import org.wso2.carbon.humantask.stub.mgt.types.DeployedTaskDefinitionsPaginated;
import org.wso2.carbon.humantask.stub.mgt.types.TaskDefinition_type0;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

import java.rmi.RemoteException;

import static org.testng.Assert.assertFalse;

/**
 * Tests the un-deployment functionality of a human task package.
 */
public class UnDeploymentTestCase {

    private static final Log log = LogFactory.getLog(UnDeploymentTestCase.class);
    private LoginLogoutUtil util = new LoginLogoutUtil();

    final String PACKAGE_MANAGEMENT_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                                                  ":" + FrameworkSettings.HTTPS_PORT +
                                                  "/services/HumanTaskPackageManagement";

    private HumanTaskPackageManagementStub humanTaskPackageManagementStub = null;


    @BeforeClass(groups = {"wso2.bps"})
    public void login() throws Exception {
        log.info("Login in UnDeployment Test...");
        ClientConnectionUtil.waitForPort(9443);
        String loggedInSessionCookie = util.login();

        humanTaskPackageManagementStub =
                new HumanTaskPackageManagementStub(PACKAGE_MANAGEMENT_SERVICE_URL);
        ServiceClient humanTaskPkgManagementServiceClient = humanTaskPackageManagementStub._getServiceClient();
        Options humanTaskPkgManagementServiceClientOptions = humanTaskPkgManagementServiceClient.getOptions();
        humanTaskPkgManagementServiceClientOptions.setManageSession(true);
        humanTaskPkgManagementServiceClientOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                                                               loggedInSessionCookie);
    }

    @AfterClass(groups = {"wso2.bps"})
    public void logout() throws Exception {
        ClientConnectionUtil.waitForPort(9443);
        util.logout();
        log.info("Log Out in Un-deployment Test...");
    }


    @Test(groups = {"wso2.bps", "g"}, dependsOnGroups = "f", description = "HumanTask Un-deployment test")
    public void UnDeploymentTestService() throws Exception {
        unDeploy(HumanTaskTestConstants.CLAIMS_APPROVAL_PACKAGE_NAME);
    }

    private void unDeploy(String packageName)
            throws PackageManagementException, RemoteException, InterruptedException {

        humanTaskPackageManagementStub.undeployHumanTaskPackage(packageName);

        Thread.sleep(10000);

        DeployedTaskDefinitionsPaginated deployedPackages = humanTaskPackageManagementStub.
                listDeployedTaskDefinitionsPaginated(0);

        boolean packageUnDeployed = true;
        if (deployedPackages.getTaskDefinition() != null && deployedPackages.getTaskDefinition().length > 0) {
            for (TaskDefinition_type0 humanTaskPackage : deployedPackages.getTaskDefinition()) {
                if (humanTaskPackage.getPackageName().equals(packageName)) {
                    packageUnDeployed = false;
                }
            }
        }
        assertFalse(!packageUnDeployed, packageName + " unDeployment has failed");
    }

}
