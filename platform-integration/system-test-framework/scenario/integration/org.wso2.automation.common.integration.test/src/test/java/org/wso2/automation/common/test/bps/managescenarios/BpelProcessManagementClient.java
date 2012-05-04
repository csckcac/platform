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
package org.wso2.automation.common.test.bps.managescenarios;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.*;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.bpel.stub.mgt.ProcessManagementException;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.RequestSender;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import java.rmi.RemoteException;

public class BpelProcessManagementClient {

    String sessionCookie = null;
    private static final Log log = LogFactory.getLog(BpelProcessManagementClient.class);
    String backEndUrl = null;
    String serviceUrl = null;
    AdminServiceBpelUploader bpelUploader;
    AdminServiceBpelPackageManager bpelManager;
    AdminServiceBpelProcessManager bpelProcrss;
    AdminServiceBpelInstanceManager bpelInstance;
    AdminServiceAuthentication adminServiceAuthentication;
    RequestSender requestSender;

    @BeforeTest(alwaysRun = true)
    public void setEnvironment() throws LoginAuthenticationExceptionException, RemoteException {
        EnvironmentBuilder builder = new EnvironmentBuilder().bps(3);
        ManageEnvironment environment = builder.build();
        backEndUrl = environment.getBps().getBackEndUrl();
        serviceUrl=environment.getBps().getServiceUrl();
        sessionCookie = environment.getBps().getSessionCookie();
        adminServiceAuthentication=environment.getBps().getAdminServiceAuthentication();
        bpelUploader =  new AdminServiceBpelUploader(backEndUrl, ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION);
        bpelManager = new AdminServiceBpelPackageManager(backEndUrl, sessionCookie);
        bpelProcrss = new AdminServiceBpelProcessManager(backEndUrl, sessionCookie);
        bpelInstance = new AdminServiceBpelInstanceManager(backEndUrl, sessionCookie);
        requestSender = new RequestSender();
    }

    @BeforeClass(alwaysRun = true)
    public void deployArtifact() {
    //  bpelUploader.deployBPEL("LoanService", ProductConstant.getResourceLocations(ProductConstant.BPS_SERVER_NAME)+ File.separator+"bpel" , sessionCookie);
    }

    @Test(groups = {"wso2.bps", "wso2.bps.manage"}, description = "Set setvice to Retire State", priority=1)
    public void testServiceRetire() throws ProcessManagementException, RemoteException {
        try {
            String processID = bpelProcrss.getProcessId("XKLoanService");
            bpelProcrss.setStatus(processID, "RETIRED");
            Assert.assertTrue(bpelProcrss.getStatus(processID).equals("RETIRED"), "PPEL process is not set as RETIRED");
            Assert.assertFalse(requestSender.isServiceAvailable(serviceUrl + "/XKLoanService"), "Service is still available");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.error("Process management failed" + e.getMessage());
            Assert.fail(e.getMessage());
        }
    }

    @Test(groups = {"wso2.bps", "wso2.bps.manage"}, description = "Set setvice to Active State",priority=2)
    public void testServiceActive() throws ProcessManagementException, RemoteException {
        try {
            String processID = bpelProcrss.getProcessId("XKLoanService");
            bpelProcrss.setStatus(processID, "ACTIVE");
            Thread.sleep(5000);
            Assert.assertTrue(bpelProcrss.getStatus(processID).equals("ACTIVE"), "PPEL process is not set as ACTIVE");
            Assert.assertTrue(requestSender.isServiceAvailable(serviceUrl + "/XKLoanService"), "Service is not available");
        } catch (InterruptedException e) {
            log.error("Process management failed" + e.getMessage());
            Assert.fail(e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void removeArtifacts() {
      //  bpelManager.undeployBPEL("LoanService");
      //  adminServiceAuthentication.logOut();
    }
}
