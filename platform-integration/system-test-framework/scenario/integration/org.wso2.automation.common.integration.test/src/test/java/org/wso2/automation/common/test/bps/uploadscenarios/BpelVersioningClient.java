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

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.*;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bpel.stub.mgt.ProcessManagementException;
import org.wso2.carbon.bpel.stub.mgt.types.ProcessStatus;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.RequestSender;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

public class BpelVersioningClient {
    String sessionCookie = null;
    private static final Log log = LogFactory.getLog(BpelVersioningClient.class);
    String backEndUrl = null;
    String serviceUrl = null;
    AdminServiceBpelUploader bpelUploader;
    AdminServiceBpelPackageManager bpelManager;
    AdminServiceBpelProcessManager bpelProcrss;
    AdminServiceBpelInstanceManager bpelInstance;
    AdminServiceAuthentication adminServiceAuthentication;
    RequestSender requestSender;
    List<String> activeStatus;
    boolean activeProcessFound;

    @BeforeTest(alwaysRun = true)
    public void setEnvironment() {
        EnvironmentBuilder builder = new EnvironmentBuilder().bps(3);
        ManageEnvironment environment = builder.build();
        backEndUrl = environment.getBps().getBackEndUrl();
        serviceUrl = environment.getBps().getServiceUrl();
        sessionCookie = environment.getBps().getSessionCookie();
        adminServiceAuthentication = environment.getBps().getAdminServiceAuthentication();
        bpelUploader = new AdminServiceBpelUploader(backEndUrl);
        bpelManager = new AdminServiceBpelPackageManager(backEndUrl, sessionCookie);
        bpelProcrss = new AdminServiceBpelProcessManager(backEndUrl, sessionCookie);
        bpelInstance = new AdminServiceBpelInstanceManager(backEndUrl, sessionCookie);
        requestSender = new RequestSender();

    }

    @BeforeClass(alwaysRun = true)
    public void deployArtifact()
            throws InterruptedException, RemoteException, MalformedURLException,
                   PackageManagementException {
        bpelUploader.deployBPEL("HelloWorld2", sessionCookie);

    }

    @Test(groups = {"wso2.bps", "wso2.bps.manage"}, description = "Tests uploading Bpel Service with In memory", priority = 0)
    public void getVersion() throws RemoteException, XMLStreamException, InterruptedException,
                                    ProcessManagementException {

        Thread.sleep(5000);
        LinkedList<String> processBefore = bpelProcrss.getProcessInfoList("HelloWorld2");
        activeStatus = new LinkedList<String>();
        activeProcessFound = false;
        for (String processid : processBefore) {
            if (bpelProcrss.getStatus(processid).equals("ACTIVE")) {
                activeStatus.add(processid);
            }
        }
        sendRequest();
    }

    private void sendRequest() throws XMLStreamException, AxisFault {
        String payLoad = " <p:hello xmlns:p=\"http://ode/bpel/unit-test.wsdl\">\n" +
                "      <!--Exactly 1 occurrence--><TestPart>test</TestPart>\n" +
                "   </p:hello>";

        String operation = "hello";
        String serviceName = "/HelloService";
        String expectedBefore = "World";
        String expectedAfter = "World-Version";
        requestSender.assertRequest(serviceUrl + serviceName, operation, payLoad,
                1, expectedBefore, true);
    }

    @Test(groups = {"wso2.bps", "wso2.bps.manage"}, description = "Tests uploading Bpel Service with In memory", priority = 1)
    public void deployVersioningBpel()
            throws InterruptedException, RemoteException, PackageManagementException {
        bpelUploader.deployBPEL("HelloWorld2", ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator + "artifacts" + File.separator + "BPS" + File.separator + "bpel" + File.separator + "VersioningSamples", sessionCookie);

    }

    @Test(groups = {"wso2.bps", "wso2.bps.manage"}, description = "Tests uploading Bpel Service with In memory", priority = 2)
    public void checkVersion() throws InterruptedException, XMLStreamException, RemoteException,
                                      ProcessManagementException {

        LinkedList<String> processAfter = null;
        for (int a = 0; a <= 10; a++) {
            Thread.sleep(5000);
            processAfter = bpelProcrss.getProcessInfoList("HelloWorld2");
            if (bpelProcrss.getStatus(activeStatus.get(0)).equals(ProcessStatus.RETIRED.toString()))
                break;
        }

        for (String process : activeStatus)

        {
            Assert.assertTrue(bpelProcrss.getStatus(process).equals(ProcessStatus.RETIRED.toString()), "Versioning failed : Previous Version " + process + "is still active");
        }

        for (String processInfo : processAfter) {
            if (bpelProcrss.getStatus(processInfo).equals("ACTIVE")) {
                activeProcessFound = true;
                for (String process : activeStatus) {
                    Assert.assertFalse(process.equals(processInfo), "Versioning failed : Previous Version " + processInfo + "is still active");
                }
            }
        }

        sendRequest();


    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws PackageManagementException, InterruptedException, RemoteException {
        bpelManager.undeployBPEL("HelloWorld2");
        adminServiceAuthentication.logOut();
    }


}
