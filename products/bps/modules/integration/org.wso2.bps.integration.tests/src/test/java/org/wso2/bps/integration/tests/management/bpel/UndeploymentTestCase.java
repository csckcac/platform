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
import org.wso2.bps.integration.tests.util.FrameworkSettings;
import org.wso2.carbon.bpel.stub.mgt.BPELPackageManagementServiceStub;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bpel.stub.mgt.types.DeployedPackagesPaginated;
import org.wso2.carbon.bpel.stub.mgt.types.Package_type0;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

import java.rmi.RemoteException;

import static org.testng.Assert.assertFalse;

public class UndeploymentTestCase {

    private static final Log log = LogFactory.getLog(UndeploymentTestCase.class);
    private LoginLogoutUtil util = new LoginLogoutUtil();

    final String PACKAGE_MANAGEMENT_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                                                  ":" + FrameworkSettings.HTTPS_PORT +
                                                  "/services/BPELPackageManagementService";

    BPELPackageManagementServiceStub bpelPackageManagementServiceStub = null;


    @BeforeClass(groups = {"wso2.bps"})
    public void login() throws java.lang.Exception {
        log.info("Login in Undeployment Test...");
        ClientConnectionUtil.waitForPort(9443);
        String loggedInSessionCookie = util.login();

        bpelPackageManagementServiceStub =
                new BPELPackageManagementServiceStub(PACKAGE_MANAGEMENT_SERVICE_URL);
        ServiceClient bpelPkgManagementServiceClient = bpelPackageManagementServiceStub._getServiceClient();
        Options bpelPkgManagementServiceClientOptions = bpelPkgManagementServiceClient.getOptions();
        bpelPkgManagementServiceClientOptions.setManageSession(true);
        bpelPkgManagementServiceClientOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                                                          loggedInSessionCookie);


    }

    @AfterClass(groups = {"wso2.bps"})
    public void logout() throws java.lang.Exception {
        ClientConnectionUtil.waitForPort(9443);
        util.logout();
        log.info("Log Out in Undeployment Test...");
    }


    @Test(groups = {"wso2.bps", "e"}, dependsOnGroups = "d", description = "Undeployment test")
    public void UndeploymentTestService() throws Exception {

        undeploy("HelloWorld2", bpelPackageManagementServiceStub);
        undeploy("TestPickOneWay", bpelPackageManagementServiceStub);
        undeploy("CleanUpTest1", bpelPackageManagementServiceStub);

    }

    private void undeploy(String packageName,
                          BPELPackageManagementServiceStub bpelPackageManagementServiceStub)
            throws PackageManagementException, RemoteException, InterruptedException {

        bpelPackageManagementServiceStub.undeployBPELPackage(packageName);

        Thread.sleep(10000);

        DeployedPackagesPaginated deployedPackages = bpelPackageManagementServiceStub.
                listDeployedPackagesPaginated(0);

        boolean packageUndeployed = true;
        for (Package_type0 bpelPackage : deployedPackages.get_package()) {
            log.info(bpelPackage.getName());
            if (bpelPackage.getName().equals(packageName)) {
                log.info(packageName + " has undeployed successfully");
                packageUndeployed = false;
            }
        }
        assertFalse(!packageUndeployed, packageName + " undeplyment failed");

    }

}
