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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;
import org.wso2.bps.integration.tests.util.BPSMgtUtils;
import org.wso2.bps.integration.tests.util.BPSTestUtils;
import org.wso2.bps.integration.tests.util.FrameworkSettings;
import org.wso2.bps.integration.tests.util.HumanTaskAdminServiceUtils;
import org.wso2.carbon.bpel.stub.mgt.InstanceManagementException;
import org.wso2.carbon.bpel.stub.mgt.InstanceManagementServiceStub;
import org.wso2.carbon.humantask.stub.ui.task.client.api.*;

import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

/**
 * This test case deals with fault scenarios when task creation by calling the task service interface.
 */
public class TaskCreationFaultTestCase {
    private static final Log log = LogFactory.getLog(TaskCreationFaultTestCase.class);

    private static String SERVICE_URL_PREFIX;

    private InstanceManagementServiceStub instanceManagementServiceStub = null;

    @BeforeGroups(groups = {"wso2.bps"}, description = " Copying sample HumanTask packages")
    protected void init() throws Exception {

        log.info("Initializing HumanTask task fault creation Test...");

        SERVICE_URL_PREFIX = "https://" + FrameworkSettings.HOST_NAME + ":" +
                FrameworkSettings.HTTPS_PORT + "/services/";

        instanceManagementServiceStub = HumanTaskAdminServiceUtils.getInstanceManagementServiceStub();
    }

    @Test(groups = {"wso2.bps"}, description = "Claims approval B4P Fault test case")
    public void createTaskB4PFault() throws XMLStreamException, RemoteException, InterruptedException,
            IllegalArgumentFault, IllegalStateFault, IllegalOperationFault, IllegalAccessFault,
            InstanceManagementException {
        String soapBody =
                "<cla:ClaimApprovalProcessInput xmlns:cla=\"http://www.wso2.org/humantask/claimsapprovalprocessservice.wsdl\">\n" +
                        "         <cla:custID>C001</cla:custID>\n" +
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
        List<String> instances = BPSMgtUtils.listInstances(instanceManagementServiceStub, 1,
                "{http://www.wso2.org/humantask/claimsapprovalprocess.bpel}ClaimsApprovalProcess");
        BPSMgtUtils.getInstanceInfo(instanceManagementServiceStub, "FAILED", null,
                null, instances);

        BPSMgtUtils.deleteInstances(instanceManagementServiceStub, instances.size());
    }
}
