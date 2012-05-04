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
package org.wso2.automation.common.test.bps.mgtstructuredactivities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.carbon.admin.service.AdminServiceBpelInstanceManager;
import org.wso2.carbon.admin.service.AdminServiceBpelPackageManager;
import org.wso2.carbon.admin.service.AdminServiceBpelProcessManager;
import org.wso2.carbon.admin.service.AdminServiceBpelUploader;
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

public class BpelStructAct_FlowClient {
    String sessionCookie = null;
    private static final Log log = LogFactory.getLog(BpelStructAct_WhileClient.class);
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
        bpelUploader = new AdminServiceBpelUploader(backEndUrl, ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION);
        bpelManager = new AdminServiceBpelPackageManager(backEndUrl, sessionCookie);
        bpelProcrss = new AdminServiceBpelProcessManager(backEndUrl, sessionCookie);
        bpelInstance = new AdminServiceBpelInstanceManager(backEndUrl, sessionCookie);
        requestSender = new RequestSender();
    }

    @BeforeClass(alwaysRun = true)
    public void deployArtifact()
            throws InterruptedException, RemoteException, MalformedURLException {
        // bpelUploader.deployBPEL("TestFlowLinks", sessionCookie);
    }

    @Test(groups = {"wso2.bps", "wso2.bps.structures"}, description = "Deploys Bpel with Flow", priority = 1)
    public void testFlowLinks() throws Exception, RemoteException {
        int instanceCount = 0;

        String processID = bpelProcrss.getProcessId("TestCase");
        PaginatedInstanceList instanceList = new PaginatedInstanceList();
        instanceList = bpelInstance.filterPageInstances(processID);
        if (instanceList.getInstance() != null) {
            instanceCount = instanceList.getInstance().length;
        }
        if (!processID.isEmpty()) {
            try {
                this.flowLinksRequest();
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

    @AfterClass(alwaysRun = true)
    public void removeArtifacts()
            throws PackageManagementException, InterruptedException, RemoteException,
                   LogoutAuthenticationExceptionException {
        //  bpelManager.undeployBPEL("TestFlowLinks");
        adminServiceAuthentication.logOut();
    }

    private void flowLinksRequest() throws Exception {
        String payload = "<ns1:ExecuteWorkflow xmlns:ns1=\"workflowns\"><value>foo</value></ns1:ExecuteWorkflow>";
        String operation = "ExecuteWorkflow";
        String serviceName = "/FlowLinkTest";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("foo");
        requestSender.sendRequest(serviceUrl + serviceName, operation, payload,
                                  1, expectedOutput, true);
    }
}
