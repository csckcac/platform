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

package org.wso2.bps.integration.tests.instanceCleanup;


import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.bps.integration.tests.util.BPSMgtUtils;
import org.wso2.bps.integration.tests.util.BPSTestUtils;
import org.wso2.bps.integration.tests.util.FrameworkSettings;
import org.wso2.carbon.bpel.stub.mgt.InstanceManagementServiceStub;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;


/**
 * Test the instance cleanup schedule task configured at bps.xml
 */
public class InstanceCleanupTestCase {
    private static final Log log = LogFactory.getLog(InstanceCleanupTestCase.class);

    private LoginLogoutUtil util = new LoginLogoutUtil();
    InstanceManagementServiceStub instanceManagementServiceStub = null;


    @Test(groups = {"wso2.bps", "cleanup", "bps"}, description = "Instance cleanup test")
    public void instanceCleanupTestService() throws Exception {
        //invoke sample process
        helloWorld();

        //get the instance count
        final String INSTANCE_MANAGEMENT_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                                                       ":" + FrameworkSettings.HTTPS_PORT +
                                                       "/services/InstanceManagementService";

        ClientConnectionUtil.waitForPort(FrameworkSettings.HTTPS_PORT);
        String loggedInSessionCookie = util.login();


        instanceManagementServiceStub = new InstanceManagementServiceStub(INSTANCE_MANAGEMENT_SERVICE_URL);
        ServiceClient instanceManagementServiceClient = instanceManagementServiceStub._getServiceClient();
        Options instanceManagementServiceClientOptions = instanceManagementServiceClient.getOptions();
        instanceManagementServiceClientOptions.setManageSession(true);
        instanceManagementServiceClientOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                                                           loggedInSessionCookie);


        //Waiting for instance to be cleaned up. sleep time
        //depend on the cron scheduler configurations in bps.xml

        log.info("Waiting 3000ms");
        Thread.sleep(3 * 1000);

        //assert instance count based on cron expression
        BPSMgtUtils.listInstances(instanceManagementServiceStub, 0,
                "{http://ode/bpel/unit-test}HelloWorld2");
    }

    private void helloWorld() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<un:hello xmlns:un=\"http://ode/bpel/unit-test.wsdl\">" +
                         "<TestPart>Hello</TestPart></un:hello>";
        String operation = "hello";
        String serviceName = "HelloService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">Hello World<");

        String SERVICE_URL_PREFIX = "https://" + FrameworkSettings.HOST_NAME + ":" +
                                    FrameworkSettings.HTTPS_PORT + "/services/";
        log.info("Service: " + SERVICE_URL_PREFIX + serviceName);
        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

}
