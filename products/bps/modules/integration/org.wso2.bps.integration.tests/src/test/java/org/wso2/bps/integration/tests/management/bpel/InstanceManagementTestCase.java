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
import org.wso2.bps.integration.tests.util.BPSMgtUtils;
import org.wso2.bps.integration.tests.util.BPSTestUtils;
import org.wso2.bps.integration.tests.util.FrameworkSettings;
import org.wso2.carbon.bpel.stub.mgt.InstanceManagementServiceStub;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

import java.util.ArrayList;
import java.util.List;

//import org.wso2.bps.test.utils.BPSMgtUtils;
//import org.wso2.bps.test.utils.BPSTestUtils;

public class InstanceManagementTestCase {
    private static final Log log = LogFactory.getLog(DeploymentTestCase.class);
    private LoginLogoutUtil util = new LoginLogoutUtil();

    final String HELLO_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                                     ":" + FrameworkSettings.HTTPS_PORT +
                                     "/services/HelloService";
    final String PICK_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                                    ":" + FrameworkSettings.HTTPS_PORT +
                                    "/services/PickService";
    final String INSTANCE_MANAGEMENT_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                                                   ":" + FrameworkSettings.HTTPS_PORT +
                                                   "/services/InstanceManagementService";

    List<String> instanceIds = new ArrayList<String>();

    InstanceManagementServiceStub instanceManagementServiceStub = null;

    @BeforeClass(groups = {"wso2.bps"})
    public void login() throws java.lang.Exception {
        log.info("Login in Instance Management Test...");
        ClientConnectionUtil.waitForPort(FrameworkSettings.HTTPS_PORT);
        String loggedInSessionCookie = util.login();


        instanceManagementServiceStub = new InstanceManagementServiceStub(INSTANCE_MANAGEMENT_SERVICE_URL);
        ServiceClient instanceManagementServiceClient = instanceManagementServiceStub._getServiceClient();
        Options instanceManagementServiceClientOptions = instanceManagementServiceClient.getOptions();
        instanceManagementServiceClientOptions.setManageSession(true);
        instanceManagementServiceClientOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                                                           loggedInSessionCookie);


    }

    @AfterClass(groups = {"wso2.bps"})
    public void logout() throws java.lang.Exception {
        ClientConnectionUtil.waitForPort(FrameworkSettings.HTTPS_PORT);
        util.logout();
        log.info("Log Out in Instance Management Test...");
    }

    @Test(groups = {"wso2.bps", "d"}, dependsOnGroups = "c", description = "Instance management test")
    public void instanceManagementTestService() throws Exception {
        String processId = "{http://ode/bpel/unit-test}HelloWorld2";
        List<String> iids = BPSMgtUtils.listInstances(instanceManagementServiceStub, 0, processId);
        instanceIds.addAll(iids);
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">Hello World<");

        BPSTestUtils.sendRequest(HELLO_SERVICE_URL,
                                 "hello",
                                 "<un:hello xmlns:un=\"http://ode/bpel/unit-test.wsdl\">" +
                                 "<TestPart>Hello</TestPart></un:hello>",
                                 1,
                                 expectedOutput,
                                 true);
        iids = BPSMgtUtils.listInstances(instanceManagementServiceStub, 1, processId);
        instanceIds.addAll(iids);
        BPSMgtUtils.getInstanceInfo(instanceManagementServiceStub, "COMPLETED", "tmpVar", ">Hello<", instanceIds);
        BPSMgtUtils.deleteInstances(instanceManagementServiceStub, 1);
        instanceIds.clear();

        BPSTestUtils.sendRequest(HELLO_SERVICE_URL,
                                 "hello",
                                 "<un:hello xmlns:un=\"http://ode/bpel/unit-test.wsdl\">" +
                                 "<TestPart>Hello</TestPart></un:hello>",
                                 5,
                                 expectedOutput,
                                 true);
        iids = BPSMgtUtils.listInstances(instanceManagementServiceStub, 5, processId);
        instanceIds.addAll(iids);
        BPSMgtUtils.deleteInstances(instanceManagementServiceStub, 5);
        instanceIds.clear();

        BPSTestUtils.sendRequest(PICK_SERVICE_URL,
                                 "dealDeck",
                                 "<pic:dealDeck xmlns:pic=\"http://www.stark.com/PickService\">" +
                                 "   <pic:Deck>testPick</pic:Deck>" +
                                 "</pic:dealDeck>",
                                 1,
                                 new ArrayList<String>(),
                                 false);
        iids = BPSMgtUtils.listInstances(instanceManagementServiceStub, 1, processId);
        instanceIds.addAll(iids);
        BPSMgtUtils.getInstanceInfo(instanceManagementServiceStub, "ACTIVE", null, null, instanceIds);
        expectedOutput.clear();
        expectedOutput.add(">testPick<");

        BPSTestUtils.sendRequest(PICK_SERVICE_URL,
                                 "pickClub",
                                 "<pic:pickClub xmlns:pic=\"http://www.stark.com/PickService\">" +
                                 "   <pic:Deck>testPick</pic:Deck>" +
                                 "</pic:pickClub>",
                                 1,
                                 expectedOutput,
                                 true);
        BPSMgtUtils.getInstanceInfo(instanceManagementServiceStub, "ACTIVE", null, null, instanceIds);
        BPSMgtUtils.performAction(instanceManagementServiceStub, instanceIds.get(0),
                                  BPSMgtUtils.InstanceOperation.SUSPEND, instanceIds);
        BPSMgtUtils.deleteInstances(instanceManagementServiceStub, 1);
        instanceIds.clear();

        BPSTestUtils.sendRequest(PICK_SERVICE_URL,
                                 "dealDeck",
                                 "<pic:dealDeck xmlns:pic=\"http://www.stark.com/PickService\">" +
                                 "   <pic:Deck>testPick</pic:Deck>" +
                                 "</pic:dealDeck>",
                                 1,
                                 new ArrayList<String>(),
                                 false);
        iids = BPSMgtUtils.listInstances(instanceManagementServiceStub, 1, processId);
        instanceIds.addAll(iids);
        BPSMgtUtils.getInstanceInfo(instanceManagementServiceStub, "ACTIVE", null, null, instanceIds);

        BPSTestUtils.sendRequest(PICK_SERVICE_URL,
                                 "pickClub",
                                 "<pic:pickClub xmlns:pic=\"http://www.stark.com/PickService\">" +
                                 "   <pic:Deck>testPick</pic:Deck>" +
                                 "</pic:pickClub>",
                                 1,
                                 expectedOutput,
                                 true);
        BPSMgtUtils.getInstanceInfo(instanceManagementServiceStub, "ACTIVE", null, null, instanceIds);
        BPSMgtUtils.performAction(instanceManagementServiceStub, instanceIds.get(0),
                                  BPSMgtUtils.InstanceOperation.TERMINATE, instanceIds);
        BPSMgtUtils.deleteInstances(instanceManagementServiceStub, 1);
        instanceIds.clear();

        BPSTestUtils.sendRequest(PICK_SERVICE_URL,
                                 "dealDeck",
                                 "<pic:dealDeck xmlns:pic=\"http://www.stark.com/PickService\">" +
                                 "   <pic:Deck>testPick</pic:Deck>" +
                                 "</pic:dealDeck>",
                                 1,
                                 new ArrayList<String>(),
                                 false);
        iids = BPSMgtUtils.listInstances(instanceManagementServiceStub, 1, processId);
        instanceIds.addAll(iids);
        BPSMgtUtils.getInstanceInfo(instanceManagementServiceStub, "ACTIVE", null, null, instanceIds);

        BPSTestUtils.sendRequest(PICK_SERVICE_URL,
                                 "pickClub",
                                 "<pic:pickClub xmlns:pic=\"http://www.stark.com/PickService\">" +
                                 "   <pic:Deck>testPick</pic:Deck>" +
                                 "</pic:pickClub>",
                                 1,
                                 expectedOutput,
                                 true);
        BPSMgtUtils.getInstanceInfo(instanceManagementServiceStub, "ACTIVE", null, null, instanceIds);
        BPSMgtUtils.performAction(instanceManagementServiceStub, instanceIds.get(0),
                                  BPSMgtUtils.InstanceOperation.SUSPEND, instanceIds);
        BPSMgtUtils.performAction(instanceManagementServiceStub, instanceIds.get(0),
                                  BPSMgtUtils.InstanceOperation.RESUME, instanceIds);
        BPSTestUtils.sendRequest(PICK_SERVICE_URL,
                                 "pickClub",
                                 "<pic:pickClub xmlns:pic=\"http://www.stark.com/PickService\">" +
                                 "   <pic:Deck>testPick</pic:Deck>" +
                                 "</pic:pickClub>",
                                 1,
                                 expectedOutput,
                                 true);
        BPSMgtUtils.deleteInstances(instanceManagementServiceStub, 1);
        instanceIds.clear();

        BPSTestUtils.sendRequest(PICK_SERVICE_URL,
                                 "dealDeck",
                                 "<pic:dealDeck xmlns:pic=\"http://www.stark.com/PickService\">" +
                                 "   <pic:Deck>testPick</pic:Deck>" +
                                 "</pic:dealDeck>",
                                 1,
                                 new ArrayList<String>(),
                                 false);
        iids = BPSMgtUtils.listInstances(instanceManagementServiceStub, 1, processId);
        instanceIds.addAll(iids);
        BPSMgtUtils.getInstanceInfo(instanceManagementServiceStub, "ACTIVE", null, null, instanceIds);

        BPSTestUtils.sendRequest(PICK_SERVICE_URL,
                                 "pickClub",
                                 "<pic:pickClub xmlns:pic=\"http://www.stark.com/PickService\">" +
                                 "   <pic:Deck>testPick</pic:Deck>" +
                                 "</pic:pickClub>",
                                 1,
                                 expectedOutput,
                                 true);
        BPSMgtUtils.getInstanceInfo(instanceManagementServiceStub, "ACTIVE", null, null, instanceIds);
        BPSMgtUtils.performAction(instanceManagementServiceStub, instanceIds.get(0),
                                  BPSMgtUtils.InstanceOperation.SUSPEND, instanceIds);
        BPSMgtUtils.performAction(instanceManagementServiceStub, instanceIds.get(0),
                                  BPSMgtUtils.InstanceOperation.TERMINATE, instanceIds);
        BPSMgtUtils.deleteInstances(instanceManagementServiceStub, 1);
        instanceIds.clear();

        BPSTestUtils.sendRequest(PICK_SERVICE_URL,
                                 "dealDeck",
                                 "<pic:dealDeck xmlns:pic=\"http://www.stark.com/PickService\">" +
                                 "   <pic:Deck>testPick</pic:Deck>" +
                                 "</pic:dealDeck>",
                                 1,
                                 new ArrayList<String>(),
                                 false);
        iids = BPSMgtUtils.listInstances(instanceManagementServiceStub, 1, processId);
        instanceIds.addAll(iids);
        BPSMgtUtils.getInstanceInfo(instanceManagementServiceStub, "ACTIVE", null, null, instanceIds);

        BPSTestUtils.sendRequest(PICK_SERVICE_URL,
                                 "pickClub",
                                 "<pic:pickClub xmlns:pic=\"http://www.stark.com/PickService\">" +
                                 "   <pic:Deck>testPick</pic:Deck>" +
                                 "</pic:pickClub>",
                                 1,
                                 expectedOutput,
                                 true);
        BPSMgtUtils.getInstanceInfo(instanceManagementServiceStub, "ACTIVE", null, null, instanceIds);
        BPSMgtUtils.performAction(instanceManagementServiceStub, instanceIds.get(0),
                                  BPSMgtUtils.InstanceOperation.SUSPEND, instanceIds);
        BPSMgtUtils.performAction(instanceManagementServiceStub, instanceIds.get(0),
                                  BPSMgtUtils.InstanceOperation.RESUME, instanceIds);
        BPSMgtUtils.performAction(instanceManagementServiceStub, instanceIds.get(0),
                                  BPSMgtUtils.InstanceOperation.TERMINATE, instanceIds);
        BPSMgtUtils.deleteInstances(instanceManagementServiceStub, 1);
        instanceIds.clear();
    }
}
