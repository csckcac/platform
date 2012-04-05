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
import org.wso2.bps.integration.tests.util.FrameworkSettings;
import org.wso2.carbon.bpel.stub.mgt.ProcessManagementException;
import org.wso2.carbon.bpel.stub.mgt.ProcessManagementServiceStub;
import org.wso2.carbon.bpel.stub.mgt.types.LimitedProcessInfoType;
import org.wso2.carbon.bpel.stub.mgt.types.PaginatedProcessInfoList;
import org.wso2.carbon.bpel.stub.mgt.types.ProcessInfoType;
import org.wso2.carbon.bpel.stub.mgt.types.ProcessStatus;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;

import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertFalse;


public class ProcessManagementTestCase {
    ProcessManagementServiceStub processManagementServiceStub = null;
    private static final Log log = LogFactory.getLog(DeploymentTestCase.class);
    private LoginLogoutUtil util = new LoginLogoutUtil();

    @BeforeClass(groups = {"wso2.bps"})
    public void login() throws java.lang.Exception {
        log.info("Login in Process Mgmt...");
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
        log.info("Log Out in Process Mgmt Test...");
    }

    @Test(groups = {"wso2.bps", "b"}, dependsOnGroups = "a", description = "Process management test")
    public void ProcessManagementTest() throws Exception {

        final String processFilter = "name}}* namespace=*";
        final String processListOrderBy = "-deployed";

        String helloProcessID = "{http://ode/bpel/unit-test}HelloWorld2";
        String pickProcessID = "{http://emcs/www.stark.com/PickProcess}PickProcess";


        PaginatedProcessInfoList processes =
                processManagementServiceStub.getPaginatedProcessList(processFilter,
                                                                     processListOrderBy, 0);
        assertFalse("Process list cannot be empty", !processes.isProcessInfoSpecified() ||
                                                    processes.getProcessInfo().length == 0);

        boolean helloProcessFound = false;
        boolean pickProcessFound = false;
        for (LimitedProcessInfoType processInfo : processes.getProcessInfo()) {
            if (processInfo.getPid().contains(helloProcessID + "-")) {
                helloProcessID = processInfo.getPid();
                helloProcessFound = true;
            }
            if (processInfo.getPid().contains(pickProcessID + "-")) {
                pickProcessID = processInfo.getPid();
                pickProcessFound = true;
            }
        }

        assertFalse("Process: " + helloProcessID + " cannot be found", !helloProcessFound);
        assertFalse("Process: " + pickProcessID + " cannot be found", !pickProcessFound);

        checkStatus(processManagementServiceStub, helloProcessID, "ACTIVE");
        setStatus(processManagementServiceStub, helloProcessID, "RETIRED");
        checkStatus(processManagementServiceStub, helloProcessID, "RETIRED");
        setStatus(processManagementServiceStub, helloProcessID, "ACTIVE");
        checkStatus(processManagementServiceStub, helloProcessID, "ACTIVE");

    }

    private void setStatus(ProcessManagementServiceStub processManagementServiceStub,
                           String processID, String status)
            throws ProcessManagementException, RemoteException {

        if (ProcessStatus.ACTIVE.getValue().equals(status.toUpperCase())) {
            processManagementServiceStub.activateProcess(QName.valueOf(processID));
        } else if (ProcessStatus.RETIRED.getValue().equals(status.toUpperCase())) {
            processManagementServiceStub.retireProcess(QName.valueOf(processID));
        } else {
            fail("Invalid process status " + status);
        }

    }

    private void checkStatus(ProcessManagementServiceStub processManagementServiceStub,
                             String processID, String status)
            throws ProcessManagementException, RemoteException {

        ProcessInfoType processInfo = processManagementServiceStub.
                getProcessInfo(QName.valueOf(processID));
        assertFalse("Process: " + processID + " Expected status: " + status +
                    " Actual status: " + processInfo.getStatus().getValue(),
                    !processInfo.getStatus().getValue().equals(status.toUpperCase()));

    }


}
