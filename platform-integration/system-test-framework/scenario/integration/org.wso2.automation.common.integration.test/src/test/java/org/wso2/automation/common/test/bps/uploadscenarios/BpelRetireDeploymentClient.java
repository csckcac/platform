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
import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.carbon.admin.service.AdminServiceBpelPackageManager;
import org.wso2.carbon.admin.service.AdminServiceBpelProcessManager;
import org.wso2.carbon.admin.service.AdminServiceBpelUploader;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

public class BpelRetireDeploymentClient {
    String sessionCookie = null;
    private static final Log log = LogFactory.getLog(BpelRetireDeploymentClient.class);
    String backEndUrl = null;
    AdminServiceBpelUploader bpelUploader;
    AdminServiceBpelPackageManager bpelManager;
    AdminServiceBpelProcessManager bpelProcrss;
    AdminServiceAuthentication adminServiceAuthentication;
    @BeforeTest(alwaysRun = true)
    public void setEnvironment() {
        EnvironmentBuilder builder = new EnvironmentBuilder().bps(3);
        ManageEnvironment environment = builder.build();
        backEndUrl = environment.getBps().getBackEndUrl();
        sessionCookie = environment.getBps().getSessionCookie();
        adminServiceAuthentication = environment.getBps().getAdminServiceAuthentication();
        bpelUploader = new AdminServiceBpelUploader(backEndUrl);
        bpelManager = new AdminServiceBpelPackageManager(backEndUrl, sessionCookie);
        bpelProcrss = new AdminServiceBpelProcessManager(backEndUrl, sessionCookie);

    }

    @BeforeClass(alwaysRun = true)
    public void deployArtifact() throws InterruptedException, RemoteException, MalformedURLException {
        bpelUploader.deployBPEL("HelloWorld-retire", sessionCookie);

    }
    @Test(groups = {"wso2.bps", "wso2.bps.manage"}, description = "Tests uploading Bpel with retire true")
    public void testRetireClient() {
        Assert.assertTrue( bpelProcrss.getStatus(bpelProcrss.getProcessId("HelloWorld-retire")).equals("RETIRED".toUpperCase()),"Process State is still Active");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws PackageManagementException, InterruptedException, RemoteException {
        bpelManager.undeployBPEL("HelloWorld-retire");
        adminServiceAuthentication.logOut();
    }
}
