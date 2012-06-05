/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.bps.integration.tests.humantask;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;
import org.wso2.bps.integration.tests.util.BPSTestUtils;
import org.wso2.bps.integration.tests.util.FrameworkSettings;
import org.wso2.bps.integration.tests.util.HumanTaskTestConstants;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertTrue;

/**
 * This test case deals with task creation by calling the task service interface.
 */
public class TaskCreationTestCase {


    private static final Log log = LogFactory.getLog(TaskCreationTestCase.class);

    private LoginLogoutUtil util = new LoginLogoutUtil();

    private static String SERVICE_URL_PREFIX;

    final String USER_MANAGEMENT_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                                               ":" + FrameworkSettings.HTTPS_PORT +
                                               "/services/UserAdmin";

    private UserAdminStub userAdminStub = null;


    @BeforeGroups(groups = {"wso2.bps"}, description = " Copying sample HumanTask packages")
    protected void init() throws Exception {

        log.info("Initializing Basic Activities Test...");

        SERVICE_URL_PREFIX = "https://" + FrameworkSettings.HOST_NAME + ":" +
                             FrameworkSettings.HTTPS_PORT + "/services/";


        initUserAdminStub();
        addRoles();
        addUsers();
    }

    private void initUserAdminStub() throws Exception {

        userAdminStub = new UserAdminStub(USER_MANAGEMENT_SERVICE_URL);
        String loggedInSessionCookie = util.login();

        ServiceClient serviceClient = userAdminStub._getServiceClient();
        Options serviceClientOptions = serviceClient.getOptions();
        serviceClientOptions.setManageSession(true);
        serviceClientOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                                         loggedInSessionCookie);
    }


    private void addRoles() throws Exception {
        userAdminStub.addRole(HumanTaskTestConstants.REGIONAL_CLERKS_ROLE, null, new String[]{"/permission/admin/login",
        "/permission/admin/manage/humantask/viewtasks"});
        userAdminStub.addRole(HumanTaskTestConstants.REGIONAL_MANAGER_ROLE, null, new String[]{"/permission/admin/login",
        "/permission/admin/manage/humantask/viewtasks"});
    }


    private void addUsers()
            throws Exception {
        userAdminStub.addUser(HumanTaskTestConstants.CLERK1_USER, HumanTaskTestConstants.CLERK1_PASSWORD,
                              new String[]{HumanTaskTestConstants.REGIONAL_CLERKS_ROLE}, null, null);
        userAdminStub.addUser(HumanTaskTestConstants.CLERK2_USER, HumanTaskTestConstants.CLERK2_PASSWORD,
                              new String[]{HumanTaskTestConstants.REGIONAL_CLERKS_ROLE}, null, null);

        userAdminStub.addUser(HumanTaskTestConstants.MANAGER_USER, HumanTaskTestConstants.MANAGER_PASSWORD,
                              new String[]{HumanTaskTestConstants.REGIONAL_MANAGER_ROLE}, null, null);

        FlaggedName[] clerkUsers = userAdminStub.getUsersOfRole(HumanTaskTestConstants.REGIONAL_CLERKS_ROLE,
                                                                "clerk*");
        assertTrue(clerkUsers.length == 2, "There should be exactly 2 clerks users in the system!");

        FlaggedName[] managerUsers = userAdminStub.getUsersOfRole(HumanTaskTestConstants.REGIONAL_MANAGER_ROLE,
                                                                  "manager*");
        assertTrue(managerUsers.length == 1,
                   "The manager was not added to the regional manager's role properly");
    }

    @Test(groups = {"wso2.bps"}, description = "Claims approval test case")
    public void createFirstTask() throws XMLStreamException, AxisFault, InterruptedException {
        String soapBody =
                "<p:ClaimApprovalData xmlns:p=\"http://www.example.com/claims/schema\">\n" +
                "      <p:cust>\n" +
                "         <p:id>235235</p:id>\n" +
                "         <p:firstname>sanjaya</p:firstname>\n" +
                "         <p:lastname>vithanagama</p:lastname>\n" +
                "      </p:cust>\n" +
                "      <p:amount>2500</p:amount>\n" +
                "      <p:region>LK</p:region>\n" +
                "      <p:prio>7</p:prio>\n" +
                "      <p:activateAt>2012-12-09T01:01:01</p:activateAt>\n" +
                "</p:ClaimApprovalData>";

        String operation = "approve";
        String serviceName = "ClaimService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("taskid>1<");
        log.info("Calling Service: " + SERVICE_URL_PREFIX + serviceName);
        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, soapBody, 1,
                                 expectedOutput, BPSTestUtils.TWO_WAY);

    }

        @Test(groups = {"wso2.bps"}, description = "Claims approval test case")
    public void createSecondTask() throws XMLStreamException, AxisFault, InterruptedException {
        String soapBody =
                "<p:ClaimApprovalData xmlns:p=\"http://www.example.com/claims/schema\">\n" +
                "      <p:cust>\n" +
                "         <p:id>452422</p:id>\n" +
                "         <p:firstname>John</p:firstname>\n" +
                "         <p:lastname>Doe</p:lastname>\n" +
                "      </p:cust>\n" +
                "      <p:amount>50000</p:amount>\n" +
                "      <p:region>US</p:region>\n" +
                "      <p:prio>1</p:prio>\n" +
                "      <p:activateAt>2012-12-09T01:01:01</p:activateAt>\n" +
                "</p:ClaimApprovalData>";

        String operation = "approve";
        String serviceName = "ClaimService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("taskid>2<");
        log.info("Calling Service: " + SERVICE_URL_PREFIX + serviceName);
        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, soapBody, 1,
                                 expectedOutput, BPSTestUtils.TWO_WAY);

    }


}
