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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bps.integration.tests.util.BPSTestUtils;
import org.wso2.bps.integration.tests.util.FrameworkSettings;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

public class StructuredActivitiesTestCase {

    private static final Log log = LogFactory.getLog(BasicActivitiesTestCase.class);
    private static String SERVICE_URL_PREFIX;

    @Test(groups = {"wso2.bps"}, description = "for each in structured activities",
          dependsOnMethods = "flowLinks")
    private void forEach() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<input xmlns=\"http://www.example.org/jms\">in</input>";
        String operation = "start";
        String serviceName = "ForEachService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("123");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "flow links in structured activities")
    private void flowLinks() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<ns1:ExecuteWorkflow xmlns:ns1=\"workflowns\"><value>foo</value>" +
                         "</ns1:ExecuteWorkflow>";
        String operation = "ExecuteWorkflow";
        String serviceName = "FlowLinkTest";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("foo");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "pick one way in structured activities",
          dependsOnMethods = "forEach")
    private void pickOneWay() throws XMLStreamException, AxisFault, InterruptedException {
        dealDeck();
        pickDiamond();
    }

    private void dealDeck() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<pic:dealDeck xmlns:pic=\"http://www.stark.com/PickService\"><pic:Deck>one</pic:Deck></pic:dealDeck>";
        String operation = "dealDeck";
        String serviceName = "PickService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">one<");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    private void pickDiamond() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<pic:pickDiamond xmlns:pic=\"http://www.stark.com/PickService\"><pic:Deck>one</pic:Deck></pic:pickDiamond>";
        String operation = "pickDiamond";
        String serviceName = "PickService";

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, new ArrayList<String>(), BPSTestUtils.ONE_WAY);
    }

    @BeforeClass(groups = {"wso2.bps"}, description = "Initializing Structured Activties Test")
    public void init() {
        log.info("Inside Init Service in Structured Activities Test...");
        SERVICE_URL_PREFIX = "https://" + FrameworkSettings.HOST_NAME + ":" +
                             FrameworkSettings.HTTPS_PORT + "/services/";
    }


}
