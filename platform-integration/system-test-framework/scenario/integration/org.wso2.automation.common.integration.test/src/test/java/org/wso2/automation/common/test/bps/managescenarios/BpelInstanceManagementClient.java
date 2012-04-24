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

import org.apache.axis2.addressing.EndpointReference;
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
import org.wso2.carbon.bpel.stub.mgt.InstanceManagementException;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bpel.stub.mgt.ProcessManagementException;
import org.wso2.carbon.bpel.stub.mgt.types.LimitedInstanceInfoType;
import org.wso2.carbon.bpel.stub.mgt.types.PaginatedInstanceList;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.RequestSender;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import javax.xml.stream.XMLStreamException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

public class BpelInstanceManagementClient {

    String sessionCookie = null;
    private static final Log log = LogFactory.getLog(BpelProcessManagementClient.class);
    String backEndUrl = null;
    String serviceUrl = null;
    LimitedInstanceInfoType instanceInfo = null;
    AdminServiceBpelUploader bpelUploader;
    AdminServiceBpelPackageManager bpelManager;
    AdminServiceBpelProcessManager bpelProcrss;
    AdminServiceBpelInstanceManager bpelInstance;
    AdminServiceAuthentication adminServiceAuthentication;
    RequestSender requestSender;

    @BeforeTest(alwaysRun = true)
    public void setEnvironment() {


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
            throws InterruptedException, RemoteException, MalformedURLException {
        //  bpelUploader.deployBPEL("TestPickOneWay", sessionCookie);
    }

    @Test(groups = {"wso2.bps", "wso2.bps.manage"}, description = "Set setvice to Active State", priority = 1)
    public void testCreateInstance()
            throws InterruptedException, XMLStreamException, RemoteException,
                   ProcessManagementException, InstanceManagementException {
        EndpointReference epr = new EndpointReference(serviceUrl + "/PickService" + "/" + "dealDeck");
        requestSender.sendRequest("<pic:dealDeck xmlns:pic=\"http://www.stark.com/PickService\">" +
                                  "   <pic:Deck>testPick</pic:Deck>" +
                                  "</pic:dealDeck>", epr);
        Thread.sleep(5000);
        PaginatedInstanceList instanceList = bpelInstance.filterPageInstances(bpelProcrss.getProcessId("PickProcess"));
        instanceInfo = instanceList.getInstance()[0];
        if (instanceList.getInstance().length == 0) {
            Assert.fail("Instance failed to create");
        }
    }

    @Test(groups = {"wso2.bps", "wso2.bps.manage"}, description = "Suspends The Service", priority = 2)
    public void testSuspendInstance()
            throws InterruptedException, InstanceManagementException, RemoteException {
        bpelInstance.performAction(instanceInfo.getIid(), AdminServiceBpelInstanceManager.InstanceOperation.SUSPEND);
        Thread.sleep(5000);
        Assert.assertTrue(bpelInstance.getInstanceInfo(instanceInfo.getIid()).getStatus().getValue().equals("SUSPENDED"), "The Service Is not Suspended");
    }

    @Test(groups = {"wso2.bps", "wso2.bps.manage"}, description = "Suspends The Service", priority = 3)
    public void testResumeInstance()
            throws InterruptedException, InstanceManagementException, RemoteException {
        bpelInstance.performAction(instanceInfo.getIid(), AdminServiceBpelInstanceManager.InstanceOperation.RESUME);
        Thread.sleep(5000);
        Assert.assertTrue(bpelInstance.getInstanceInfo(instanceInfo.getIid()).getStatus().getValue().equals("ACTIVE"), "The Service Is not Suspended");
    }

    @Test(groups = {"wso2.bps", "wso2.bps.manage"}, description = "Suspends The Service", priority = 4)
    public void testTerminateInstance()
            throws InterruptedException, InstanceManagementException, RemoteException {
        bpelInstance.performAction(instanceInfo.getIid(), AdminServiceBpelInstanceManager.InstanceOperation.TERMINATE);
        Thread.sleep(5000);
        Assert.assertTrue(bpelInstance.getInstanceInfo(instanceInfo.getIid()).getStatus().getValue().equals("TERMINATED"), "The Service Is not Terminated");
    }

    @Test(groups = {"wso2.bps", "wso2.bps.manage"}, description = "Suspends The Service", priority = 5)
    public void testDeleteInstance()
            throws InterruptedException, InstanceManagementException, RemoteException {
        bpelInstance.deleteInstance(instanceInfo.getIid());
        Thread.sleep(5000);
    }

    @AfterClass(alwaysRun = true)
    public void removeArtifacts()
            throws PackageManagementException, InterruptedException, RemoteException {
        // bpelManager.undeployBPEL("TestPickOneWay");
        adminServiceAuthentication.logOut();
    }
}
