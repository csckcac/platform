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

package org.wso2.bps.integration.tests.bpel;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;
import org.wso2.bps.integration.tests.util.BPSTestUtils;
import org.wso2.bps.integration.tests.util.FrameworkSettings;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class BasicActivitiesTestCase {
    private static final Log log = LogFactory.getLog(BasicActivitiesTestCase.class);

    private static String SERVICE_URL_PREFIX;

    @BeforeGroups(groups = {"wso2.bps"}, description = " Copying sample BPELs")
    protected void init() throws IOException {

        log.info("Initializing Basic Activities Test...");

        SERVICE_URL_PREFIX = "https://" + FrameworkSettings.HOST_NAME + ":" +
                             FrameworkSettings.HTTPS_PORT + "/services/";

    }

    @Test(groups = {"wso2.bps"}, description = "Hello World test case")
    public void helloWorld() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<un:hello xmlns:un=\"http://ode/bpel/unit-test.wsdl\"><TestPart>Hello</TestPart></un:hello>";
        String operation = "hello";
        String serviceName = "HelloService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">Hello World<");

        log.info("Service: " + SERVICE_URL_PREFIX + serviceName);
        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "if true test case")
    public void ifTrue() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<un:hello xmlns:un=\"http://ode/bpel/unit-test.wsdl\">" +
                         "<TestPart>2.0</TestPart></un:hello>";
        String operation = "hello";
        String serviceName = "TestIf";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">Worked<");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "if false test case")
    public void ifFalse() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<un:hello xmlns:un=\"http://ode/bpel/unit-test.wsdl\">" +
                         "<TestPart>1.0</TestPart></un:hello>";
        String operation = "hello";
        String serviceName = "TestIf";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">Failed<");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }
}
