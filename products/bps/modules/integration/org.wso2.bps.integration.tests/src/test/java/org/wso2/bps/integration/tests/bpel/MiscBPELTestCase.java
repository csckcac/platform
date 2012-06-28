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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Various BPEL tests
 */
public class MiscBPELTestCase {
    private static final Log log = LogFactory.getLog(MiscBPELTestCase.class);
    private static String SERVICE_URL_PREFIX;

    @BeforeGroups(groups = {"wso2.bps"}, description = "initializing BPEL functionality test")
    public void setup() {
        log.info("Initializing BPEL Functionality Test...");
        SERVICE_URL_PREFIX = "https://" + FrameworkSettings.HOST_NAME + ":" +
                FrameworkSettings.HTTPS_PORT + "/services/";
    }

    @Test(groups = {"wso2.bps"}, description = "Async BPEL sample test case")
    public void testAsyncBPELSample() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<p:ClientRequest xmlns:p=\"urn:ode-apache-org:example:async:client\">\n" +
                "      <p:id>1</p:id>\n" +
                "      <p:input>2</p:input>\n" +
                "   </p:ClientRequest>";
        String operation = "process";
        String serviceName = "ClientService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("Server says 2");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "Correlation with attribute sample test case")
    public void testCorrelationWithAttribute() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<p:TestCorrelationWithAttributeRequest xmlns:p=\"http://wso2.org/bps/sample\">\n" +
                "      <p:input>attributeCorrelation</p:input>\n" +
                "   </p:TestCorrelationWithAttributeRequest>";
        String operation = "process";
        String serviceName = "TestCorrelationWithAttribute";
        List<String> expectedOutput = Collections.emptyList();

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                1, expectedOutput, BPSTestUtils.ONE_WAY);

        Thread.sleep(2000);
        payload = "<p:CallbackOperation xmlns:p=\"http://www.example.org/callback/\" corId=\"attributeCorrelation\">\n" +
                "      <in>99ee992</in>\n" +
                "   </p:CallbackOperation>";
        operation = "CallbackOperation";
        serviceName = "CallbackService";
        expectedOutput = new ArrayList<String>();
        expectedOutput.add("99ee992");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                1, expectedOutput, BPSTestUtils.TWO_WAY);
    }
}
