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
import java.util.List;

public class BPELFunctionalityTestCase {

    private static final Log log = LogFactory.getLog(BPELFunctionalityTestCase.class);
    private static String SERVICE_URL_PREFIX;

    @Test(groups = {"wso2.bps"}, description = "onAlarm BPEL functionality test case")
    public void onAlarm() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<exam:start xmlns:exam=\"http://ode.apache.org/example\">4</exam:start>";
        String operation = "receive";
        String serviceName = "CanonicServiceForClient";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("</start>");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "Dynamic Partner Links and Dynamic Addressing in BPEL")
    public void dynamicPartner() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<ns2:dummy xmlns:ns2=\"http://ode/bpel/responder.wsdl\">fire!</ns2:dummy>";
        String operation = "execute";
        String serviceName = "DynMainService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">OK<");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "ifElse BPEL functionality test case")
    public void ifElse() throws XMLStreamException, AxisFault, InterruptedException {
        // test <if>
        String payload = "<unit:hello xmlns:unit=\"http://ode/bpel/unit-test.wsdl\">" +
                         "         <TestPart>2</TestPart>" +
                         "      </unit:hello>";
        String operation = "hello";
        String serviceName = "TestIf";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">Worked<");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);

        //test <else/>
        payload = "<unit:hello xmlns:unit=\"http://ode/bpel/unit-test.wsdl\">" +
                  "         <TestPart>0</TestPart>" +
                  "      </unit:hello>";
        expectedOutput.clear();
        expectedOutput.add(">Failed<");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "forEach BPEL functionality test case")
    public void forEach() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<jms:input xmlns:jms=\"http://www.example.org/jms\">testIf</jms:input>";
        String operation = "start";
        String serviceName = "ForEachService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">testIf123<");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }


    @Test(groups = {"wso2.bps"}, description = "Pick BPEL functionality test case")
    public void pick() throws XMLStreamException, AxisFault, InterruptedException {
        //create new instance
        String payload = "<pic:dealDeck xmlns:pic=\"http://www.stark.com/PickService\">" +
                         "         <pic:Deck>testPick</pic:Deck>" +
                         "      </pic:dealDeck>";
        String operation = "dealDeck";
        String serviceName = "PickService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">testPick<");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);

        //try the pick service
        payload = "<pic:pickClub xmlns:pic=\"http://www.stark.com/PickService\">" +
                  "         <pic:Deck>testPick</pic:Deck>" +
                  "      </pic:pickClub>";
        operation = "pickClub";
        serviceName = "PickService";

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @BeforeGroups(groups = {"wso2.bps"}, description = "initializing BPEL functionality test")
    public void setup() {
        log.info("Initializing BPEL Functionality Test...");
        SERVICE_URL_PREFIX = "https://" + FrameworkSettings.HOST_NAME + ":" +
                             FrameworkSettings.HTTPS_PORT + "/services/";
    }

}


