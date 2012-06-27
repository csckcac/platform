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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;
import org.wso2.bps.integration.tests.util.*;
import org.wso2.carbon.bpel.stub.mgt.InstanceManagementException;
import org.wso2.carbon.bpel.stub.mgt.InstanceManagementServiceStub;
import org.wso2.carbon.humantask.stub.ui.task.client.api.*;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TSimpleQueryCategory;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TSimpleQueryInput;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskSimpleQueryResultRow;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskSimpleQueryResultSet;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;

import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertTrue;

/**
 * This test case deals with task creation by calling the task service interface.
 */
public class TaskCreationTestCase {

    private static final Log log = LogFactory.getLog(TaskCreationTestCase.class);
    private static String SERVICE_URL_PREFIX;

    private UserAdminStub userAdminStub = null;
    private HumanTaskClientAPIAdminStub taskOperationsStub = null;
    private InstanceManagementServiceStub instanceManagementServiceStub = null;

    @BeforeGroups(groups = {"wso2.bps"}, description = " Copying sample HumanTask packages")
    protected void init() throws Exception {

        log.info("Initializing HumanTask task creation Test...");

        SERVICE_URL_PREFIX = "https://" + FrameworkSettings.HOST_NAME + ":" +
                FrameworkSettings.HTTPS_PORT + "/services/";

        userAdminStub = HumanTaskAdminServiceUtils.getUserAdminStub();
        addRoles();
        addUsers();
        taskOperationsStub = HumanTaskAdminServiceUtils.getTaskOperationServiceStub();
        instanceManagementServiceStub = HumanTaskAdminServiceUtils.getInstanceManagementServiceStub();
    }

    private void addRoles() throws Exception {
        userAdminStub.addRole(HumanTaskTestConstants.REGIONAL_CLERKS_ROLE, null,
                new String[]{"/permission/admin/login",
                        "/permission/admin/manage/humantask/viewtasks"});
        userAdminStub.addRole(HumanTaskTestConstants.REGIONAL_MANAGER_ROLE, null,
                new String[]{"/permission/admin/login",
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

    @Test(groups = {"wso2.bps"}, description = "Claims approval B4P test case")
    public void createTaskB4P() throws XMLStreamException, RemoteException, InterruptedException,
            IllegalArgumentFault, IllegalStateFault, IllegalOperationFault, IllegalAccessFault,
            InstanceManagementException {
        String soapBody =
                "<cla:ClaimApprovalProcessInput xmlns:cla=\"http://www.wso2.org/humantask/claimsapprovalprocessservice.wsdl\">\n" +
                        "         <cla:custID>C002</cla:custID>\n" +
                        "         <cla:custFName>Waruna</cla:custFName>\n" +
                        "         <cla:custLName>Ranasinghe</cla:custLName>\n" +
                        "         <cla:amount>10000</cla:amount>\n" +
                        "         <cla:region>Gampaha</cla:region>\n" +
                        "         <cla:priority>2</cla:priority>\n" +
                        "      </cla:ClaimApprovalProcessInput>";

        String operation = "claimsApprovalProcessOperation";
        String serviceName = "ClaimsApprovalProcessService";
        List<String> expectedOutput = Collections.emptyList();
        log.info("Calling Service: " + SERVICE_URL_PREFIX + serviceName);
        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, soapBody, 1,
                expectedOutput, BPSTestUtils.ONE_WAY);
        Thread.sleep(5000);
        BPSMgtUtils.listInstances(instanceManagementServiceStub, 1,
                "{http://www.wso2.org/humantask/claimsapprovalprocess.bpel}ClaimsApprovalProcess");

        TSimpleQueryInput queryInput = new TSimpleQueryInput();
        queryInput.setPageNumber(0);
        queryInput.setSimpleQueryCategory(TSimpleQueryCategory.ALL_TASKS);

        TTaskSimpleQueryResultSet taskResults = taskOperationsStub.simpleQuery(queryInput);

        TTaskSimpleQueryResultRow[] rows = taskResults.getRow();
        TTaskSimpleQueryResultRow b4pTask = null;

        Assert.assertNotNull(rows, "No tasks found. Task creation has failed. ");

        // looking for the latest task
        for (TTaskSimpleQueryResultRow row : rows) {
            if (b4pTask == null) {
                b4pTask = row;
            } else {
                if (Long.parseLong(b4pTask.getId().toString()) < Long.parseLong(row.getId().toString())) {
                    b4pTask = row;
                }
            }
        }

        Assert.assertNotNull(b4pTask, "Task creation has failed");

        String claimApprovalRequest = (String) taskOperationsStub.getInput(b4pTask.getId(), null);

        Assert.assertNotNull(claimApprovalRequest, "The input of the Task:" +
                b4pTask.getId() + " is null.");

        Assert.assertFalse(!claimApprovalRequest.contains("C002"),
                "Unexpected input found for the Task");

        //claim the task before starting.
        taskOperationsStub.claim(b4pTask.getId());

        //start the task before completing.
        taskOperationsStub.start(b4pTask.getId());

        taskOperationsStub.complete(b4pTask.getId(), "<sch:ClaimApprovalResponse xmlns:sch=\"http://www.example.com/claims/schema\">\n" +
                "         <sch:approved>true</sch:approved>\n" +
                "      </sch:ClaimApprovalResponse>");

        Thread.sleep(5000);
        List<String> instances = BPSMgtUtils.listInstances(instanceManagementServiceStub, 1,
                "{http://www.wso2.org/humantask/claimsapprovalprocess.bpel}ClaimsApprovalProcess");

        BPSMgtUtils.getInstanceInfo(instanceManagementServiceStub, "COMPLETED", "b4pOutput",
                ">true<", instances);
        BPSMgtUtils.deleteInstances(instanceManagementServiceStub, instances.size());
    }
}
