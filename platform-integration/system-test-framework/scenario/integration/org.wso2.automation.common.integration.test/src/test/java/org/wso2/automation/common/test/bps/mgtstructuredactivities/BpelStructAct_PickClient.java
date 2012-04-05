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

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.admin.service.*;
import org.wso2.carbon.bpel.stub.mgt.InstanceManagementException;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bpel.stub.mgt.types.PaginatedInstanceList;
import org.wso2.platform.test.core.RequestSender;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import javax.xml.stream.XMLStreamException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

public class BpelStructAct_PickClient {
    String sessionCookie = null;
    private static final Log log = LogFactory.getLog(BpelStructAct_WhileClient.class);
    String backEndUrl = null;
    String serviceUrl = null;
    String processID;
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
        serviceUrl=environment.getBps().getServiceUrl();
        sessionCookie = environment.getBps().getSessionCookie();
        adminServiceAuthentication=environment.getBps().getAdminServiceAuthentication();
        bpelUploader = new AdminServiceBpelUploader(backEndUrl);
        bpelManager = new AdminServiceBpelPackageManager(backEndUrl, sessionCookie);
        bpelProcrss = new AdminServiceBpelProcessManager(backEndUrl, sessionCookie);
        bpelInstance = new AdminServiceBpelInstanceManager(backEndUrl, sessionCookie);
        requestSender = new RequestSender();
    }
    @BeforeClass(alwaysRun = true)
    public void deployArtifact() throws InterruptedException, RemoteException, MalformedURLException {
       // bpelUploader.deployBPEL("TestPickOneWay", sessionCookie);
        System.out.println("TestPickOneWay");
    }

    @Test(groups = {"wso2.bps", "wso2.bps.structures"}, description = "Deploys Bpel with If activity", priority=2)
    public void runSuccessCase() throws InstanceManagementException, RemoteException {
        int instanceCount = 0;

        String processID = bpelProcrss.getProcessId("PickProcess");
        PaginatedInstanceList instanceList = new PaginatedInstanceList();
        instanceList = bpelInstance.filterPageInstances(processID);
        if (instanceList.getInstance() != null) {
            instanceCount = instanceList.getInstance().length;
        }
        if (!processID.isEmpty()) {
            try {
                this.pickRequest();
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
    public void cleanup() throws PackageManagementException, InterruptedException, RemoteException {
      //  bpelManager.undeployBPEL("TestPickOneWay");
        adminServiceAuthentication.logOut();
    }


     private void pickRequest()   {
        String payload = " <p:dealDeck xmlns:p=\"http://www.stark.com/PickService\">\n" +
                "      <!--Exactly 1 occurrence-->\n" +
                "      <xsd:Deck xmlns:xsd=\"http://www.stark.com/PickService\">10</xsd:Deck>\n" +
                "   </p:dealDeck>";
        String operation = "dealDeck";
        String serviceName = "/PickService";
                EndpointReference epr = new EndpointReference(serviceUrl + "/PickService" + "/" + "dealDeck");
         try {
             requestSender.sendRequest(payload,epr);
         } catch (XMLStreamException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (AxisFault axisFault) {
             axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
}
