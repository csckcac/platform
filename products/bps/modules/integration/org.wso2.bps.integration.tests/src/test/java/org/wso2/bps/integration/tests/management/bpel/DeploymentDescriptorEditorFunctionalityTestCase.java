/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.bps.integration.tests.management.bpel;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bps.integration.tests.util.BPSTestUtils;
import org.wso2.bps.integration.tests.util.FrameworkSettings;
import org.wso2.carbon.bpel.stub.mgt.*;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bpel.stub.mgt.types.*;
import org.wso2.carbon.bpel.stub.mgt.types.ProcessManagementException;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;

/**
 * Tests the functionality implemented at org.wso2.carbon.bpel.ui.clients.ProcessManagementServiceClient#updateDeployInfo
 * This also can be considerred as a child test cases under org.wso2.bps.integration.tests
 * .management.bpel.ProcessManagementTestCase as well.
 */
public class DeploymentDescriptorEditorFunctionalityTestCase {
    /**
     * Class Logger
     */
    private static Log log = LogFactory.getLog(DeploymentDescriptorEditorFunctionalityTestCase.class);
    ProcessManagementServiceStub processManagementServiceStub = null;
    private LoginLogoutUtil util = new LoginLogoutUtil();

    @BeforeClass(groups = {"wso2.bps"})
    public void login() throws java.lang.Exception {
        log.info("Log-in to Process Deployment Functionality Test...");
        final String PROCESS_MANAGEMENT_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                                                      ":" + FrameworkSettings.HTTPS_PORT +
                                                      "/services/ProcessManagementService";

        ClientConnectionUtil.waitForPort(FrameworkSettings.HTTPS_PORT);
        String loggedInSessionCookie = util.login();

        processManagementServiceStub =
                new ProcessManagementServiceStub(PROCESS_MANAGEMENT_SERVICE_URL);
        ServiceClient processManagementServiceClient = processManagementServiceStub._getServiceClient();
        Options processManagementServiceClientOptions = processManagementServiceClient.getOptions();
        processManagementServiceClientOptions.setManageSession(true);
        processManagementServiceClientOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                                                          loggedInSessionCookie);
    }

    @AfterClass(groups = {"wso2.bps"})
    public void logout() throws java.lang.Exception {
        ClientConnectionUtil.waitForPort(FrameworkSettings.HTTPS_PORT);
        util.logout();
        log.info("Log Out in Process Deployment Functionality Test...");
    }

    @Test(groups = {"wso2.bps", "f"}, dependsOnGroups = "a", description = "Deployment Descriptor" +
                                                                           " Functionality test")
    public void testProcessInMemoryInvocation() throws Exception {

        final String processFilter = "name}}* namespace=*";
        final String processListOrderBy = "-deployed";

        String helloProcessID = "{http://ode/bpel/unit-test}HelloWorld2";
        String helloProcessIDWithVersion = null;

        //Find the deployed process id with version
        PaginatedProcessInfoList processes =
                processManagementServiceStub.getPaginatedProcessList(processFilter,
                                                                     processListOrderBy, 0);
        AssertJUnit.assertFalse("Process list cannot be empty", !processes.isProcessInfoSpecified() ||
                                                                processes.getProcessInfo().length == 0);

        boolean helloProcessFound = false;
        for (LimitedProcessInfoType processInfo : processes.getProcessInfo()) {
            if (processInfo.getPid().contains(helloProcessID + "-")) {
                helloProcessIDWithVersion = processInfo.getPid();
                helloProcessFound = true;
            }
        }

        //Test-case 1
        testInMemoryProcessInvocation(helloProcessIDWithVersion);
    }

    /**
     * Check whether the changing in-memory from "false" to "true" have any effect on process
     * invocation.
     * @param processIDWithVersion process id with correct version
     * @throws Exception
     */
    private void testInMemoryProcessInvocation (String processIDWithVersion) throws Exception {
        //Test in-memory=false process invocation
        invokeProcess();

        ProcessDeployDetailsList_type0 newDTO = modifyDeploymentDescriptorUpdater(processManagementServiceStub.getProcessDeploymentInfo(QName.valueOf(processIDWithVersion)));
        processManagementServiceStub.updateDeployInfo(newDTO);

        //Test in-memory=true process invocation
        invokeProcess();
    }

    /**
     * Modify the existing the DTO related to process-deploy-details
     *
     * @param dto existing process details DTO
     * @return modified dto
     */
    private ProcessDeployDetailsList_type0 modifyDeploymentDescriptorUpdater(ProcessDeployDetailsList_type0 dto) {
        //Change the in-memory status
        dto.setIsInMemory(true);
        return dto;
    }

    private void invokeProcess() throws Exception {
        final String SERVICE_URL_PREFIX = "https://" + FrameworkSettings.HOST_NAME +
                                          ":" + FrameworkSettings.HTTPS_PORT +
                                          "/services/";
        String payload = "<un:hello xmlns:un=\"http://ode/bpel/unit-test.wsdl\"><TestPart>Hello</TestPart></un:hello>";
        String operation = "hello";
        String serviceName = "HelloService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">Hello World<");

        log.info("Service: " + SERVICE_URL_PREFIX + serviceName);
        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }
}
