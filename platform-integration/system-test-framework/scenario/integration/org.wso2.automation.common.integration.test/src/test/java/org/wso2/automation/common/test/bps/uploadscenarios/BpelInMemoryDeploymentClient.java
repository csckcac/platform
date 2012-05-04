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
package org.wso2.automation.common.test.bps.uploadscenarios;

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

public class BpelInMemoryDeploymentClient{

    String sessionCookie = null;
    private static final Log log = LogFactory.getLog(BpelInMemoryDeploymentClient.class);
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
        serviceUrl = environment.getBps().getServiceUrl();
        sessionCookie = environment.getBps().getSessionCookie();
        adminServiceAuthentication = environment.getBps().getAdminServiceAuthentication();
        bpelUploader =  new AdminServiceBpelUploader(backEndUrl, ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION);
        bpelManager = new AdminServiceBpelPackageManager(backEndUrl, sessionCookie);
        bpelProcrss = new AdminServiceBpelProcessManager(backEndUrl, sessionCookie);
        bpelInstance = new AdminServiceBpelInstanceManager(backEndUrl, sessionCookie);
        requestSender = new RequestSender();
    }

    @BeforeClass(alwaysRun = true)
    public void deployArtifact()
            throws InterruptedException, RemoteException, MalformedURLException,
                   PackageManagementException {
        bpelUploader.deployBPEL("CustomerInfo", sessionCookie);
    }


    @Test(groups = {"wso2.bps", "wso2.bps.manage"}, description = "Tests uploading Bpel Service with In memory",priority=0)
    public void testInmemoryUolpad() throws Exception {
        bpelProcrss.getStatus(bpelProcrss.getProcessId("CustomerInfo"));
        RequestSender requestSender = new RequestSender();
        requestSender.waitForProcessDeployment(serviceUrl + "/CustomerInfoService");

        requestSender.assertRequest(serviceUrl + "/CustomerInfoService", "getCustomerSSN", "<p:CustomerInfo xmlns:p=\"http://wso2.org/bps/samples/loan_process/schema\">\n" +
                "      <!--Exactly 1 occurrence-->\n" +
                "      <Name xmlns=\"http://wso2.org/bps/samples/loan_process/schema\">Dharshana</Name>\n" +
                "      <!--Exactly 1 occurrence-->\n" +
                "      <Email xmlns=\"http://wso2.org/bps/samples/loan_process/schema\">dharshanaw@wso2.com</Email>\n" +
                "      <!--Exactly 1 occurrence-->\n" +
                "      <tns:CustomerID xmlns:tns=\"http://wso2.org/bps/samples/loan_process/schema\">?</tns:CustomerID>\n" +
                "      <!--Exactly 1 occurrence-->\n" +
                "      <CreditRating xmlns=\"http://wso2.org/bps/samples/loan_process/schema\">?</CreditRating>\n" +
                "   </p:CustomerInfo>\n", 1, "43235678SSN", true);

        PaginatedInstanceList instanceList = bpelInstance.filterPageInstances(bpelProcrss.getProcessId("CustomerInfo"));
        Assert.assertTrue( instanceList != null,"Service is not running inmemory");
    }

    @AfterClass(alwaysRun = true)
    public void removeArtifacts()
            throws PackageManagementException, InterruptedException, RemoteException,
                   LogoutAuthenticationExceptionException {
        bpelManager.undeployBPEL("CustomerInfo");
        adminServiceAuthentication.logOut();
    }
}

