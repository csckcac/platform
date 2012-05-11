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
package org.wso2.automation.common.test.bps.bpelactivities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.*;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bpel.stub.mgt.types.PaginatedInstanceList;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.RequestSender;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class BpelActCombineUrl {
    String sessionCookie = null;
    private static final Log log = LogFactory.getLog(BpelActCombineUrl.class);
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
        EnvironmentBuilder builder =   new EnvironmentBuilder().bps(4);
        ManageEnvironment environment = builder.build();
        backEndUrl = environment.getBps().getBackEndUrl();
        serviceUrl=environment.getBps().getServiceUrl();
        sessionCookie = environment.getBps().getSessionCookie();
        bpelUploader = new AdminServiceBpelUploader(backEndUrl, ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION);
        bpelManager = new AdminServiceBpelPackageManager(backEndUrl, sessionCookie);
        bpelProcrss = new AdminServiceBpelProcessManager(backEndUrl, sessionCookie);
        bpelInstance = new AdminServiceBpelInstanceManager(backEndUrl, sessionCookie);
        adminServiceAuthentication=  environment.getBps().getAdminServiceAuthentication();
        requestSender = new RequestSender();
    }

    @BeforeClass(alwaysRun = true,groups = {"wso2.bps", "wso2.bps.bpelactivities"})
    public void deployArtifact() throws InterruptedException, RemoteException, MalformedURLException {
       // bpelUploader.deployBPEL("TestCombineUrl", sessionCookie);
    }

    @Test(groups = {"wso2.bps", "wso2.bps.bpelactivities"}, description = "Invike combine URL Bpel")
    public void testCombineUrl() throws Exception, RemoteException {
        int instanceCount = 0;

        String processID = bpelProcrss.getProcessId("TestCombineUrl");
        PaginatedInstanceList instanceList = new PaginatedInstanceList();
        instanceList = bpelInstance.filterPageInstances(processID);
        if (instanceList.getInstance() != null) {
            instanceCount = instanceList.getInstance().length;
        }
        if (!processID.isEmpty()) {
            try {
                this.forEachRequest();
                Thread.sleep(5000);
                if (instanceCount >= bpelInstance.filterPageInstances(processID).getInstance().length) {
                    Assert.fail("Instance is not created for the request");
                }
            } catch (InterruptedException e) {
                log.error("Process management failed" + e.getMessage());
                Assert.fail(e.getMessage());
            }
            bpelInstance.clearInstancesOfProcess(processID);
        }
    }

    @AfterClass(alwaysRun = true,groups = {"wso2.bps", "wso2.bps.bpelactivities"})
    public void removeArtifacts()
            throws PackageManagementException, InterruptedException, RemoteException,
                   LogoutAuthenticationExceptionException {
       // bpelManager.undeployBPEL("TestCombineUrl");
        adminServiceAuthentication.logOut();
    }

    public void forEachRequest() throws Exception {
        String payload = "      <p:combineUrl xmlns:p=\"http://ode/bpel/unit-test.wsdl\">\n" +
                "      <!--Exactly 1 occurrence-->\n" +
                "     <base>http://www.google.lk/</base>\n" +
                "     <!--Exactly 1 occurrence-->\n" +
                "      <relative>search</relative>\n" +
                "   </p:combineUrl>";
        String operation = "combineUrl";
        String serviceName = "TestCombineUrlService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("http://www.google.lk/search");
        requestSender.sendRequest(serviceUrl+"/" + serviceName, operation, payload,
                1, expectedOutput, true);
    }
}

