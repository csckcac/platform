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

public class DataHandlingTestCase {
    private static final Log log = LogFactory.getLog(DataHandlingTestCase.class);

    private static String SERVICE_URL_PREFIX;


    @Test(groups = {"wso2.bps"}, description = "this test will use " +
                                               "composeUrl with the element argument")
    public void composeURLWithElement() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<un:composeUrl xmlns:un=\"http://ode/bpel/unit-test.wsdl\">" +
                         "<template>http://example.com/{user}/{tag}/{a_missing_var}</template>" +
                         "<name/><value/><pairs><user>bill</user><tag>ruby</tag></pairs>" +
                         "</un:composeUrl>";
        String operation = "composeUrl";
        String serviceName = "TestComposeUrlService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">http://example.com/bill/ruby/<");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "this test will use composeUrl with a list of " +
                                               "name, value, name, value, etc")
    public void composeURL() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<un:composeUrl xmlns:un=\"http://ode/bpel/unit-test.wsdl\">" +
                         "<template>http://example.com/{user}/{a_missing_var}</template>" +
                         "<name>user</name><value>bill</value><pairs /></un:composeUrl>";
        String operation = "composeUrl";
        String serviceName = "TestComposeUrlService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">http://example.com/bill/<");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    public void flexibleAssign() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<flex:typeA xmlns:flex=\"http://wso2.org/bps/schema/FlexibleAssign\">" +
                         "<flex:paramA>ee</flex:paramA></flex:typeA>";
        String operation = "operation1";
        String serviceName = "FlexibleAssign";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">ee<");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    public void ignoreMissingFromData() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<test:Input xmlns:test=\"test:test\">yy</test:Input>";
        String operation = "process";
        String serviceName = "TestIgnoreMissingFromDataService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">Test passed.<");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "Combine URL on data handling test case")
    public void combineUrl() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<un:combineUrl xmlns:un=\"http://ode/bpel/unit-test.wsdl\">" +
                         "<base>http://example.com/html/about.html</base>" +
                         "<relative>../experts/</relative></un:combineUrl>";
        String operation = "combineUrl";
        String serviceName = "TestCombineUrlService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">http://example.com/experts/<");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "expanding template with element")
    public void expandTemplateWithElement()
            throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<un:expandTemplate xmlns:un=\"http://ode/bpel/unit-test.wsdl\">" +
                         "<template>http://example.com/{user}/{tag}/{a_missing_var}/" +
                         "{another_missing_var=but_with_a_default}</template><name/><value/>" +
                         "<pairs><user>bill</user><tag>ruby</tag></pairs></un:expandTemplate>";
        String operation = "expandTemplate";
        String serviceName = "TestExpandTemplateService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">http://example.com/bill/ruby/{a_missing_var}/but_with_a_default<");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "expanding template")
    public void expandTemplate() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<un:expandTemplate xmlns:un=\"http://ode/bpel/unit-test.wsdl\">" +
                         "<template>http://example.com/{user}/{a_missing_var}/" +
                         "{another_missing_var=but_with_a_default}</template><name>user</name>" +
                         "<value>bill</value><pairs/></un:expandTemplate>";
        String operation = "expandTemplate";
        String serviceName = "TestExpandTemplateService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">http://example.com/bill/{a_missing_var}/but_with_a_default<");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "assign activity in data handling test case")
    public void assignActivity1() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<un:testAssign xmlns:un=\"http://ode/bpel/unit-test.wsdl\">" +
                         "<TestPart>Hello</TestPart></un:testAssign>";
        String operation = "testAssign";
        String serviceName = "TestAssignService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">Hello World7true3<");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "assign activity in data handling test case")
    public void assignActivity2() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<un:testAssign xmlns:un=\"http://ode/bpel/unit-test.wsdl\">" +
                         "<TestPart>Hello</TestPart></un:testAssign>";
        String operation = "testAssign";
        String serviceName = "TestAssignService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">Hello World7true3<");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "assign data in data handling test case")
    public void assignDate() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<ns1:TaskRequest xmlns:ns1=\"http://example.com/NewDiagram/Pool\">" +
                         "start</ns1:TaskRequest>";
        String operation = "Task";
        String serviceName = "TestAssignDateService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">OK<");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "XSLT transform in data handling test case")
    public void XSLTransform() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<un:helloXsl xmlns:un=\"http://ode/bpel/unit-test.wsdl\"><TestPart>" +
                         "<content>Hello</content></TestPart></un:helloXsl>";
        String operation = "helloXsl";
        String serviceName = "HelloXslService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">HelloXsl World<");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "split in data handling test case")
    public void split() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<un:split xmlns:un=\"http://ode/bpel/unit-test.wsdl\">" +
                         "<TestPart>split,me,this,please</TestPart></un:split>";
        String operation = "split";
        String serviceName = "TestSplitService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("<chunk>split</chunk>");
        expectedOutput.add("<chunk>me</chunk>");
        expectedOutput.add("<chunk>this</chunk>");
        expectedOutput.add("<chunk>please</chunk>");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "counter in data handling test case")
    public void counter() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<un:initialize xmlns:un=\"http://example.com/bpel/counter\">" +
                         "<counterName>foo</counterName></un:initialize>";
        String operation = "initialize";
        String serviceName = "counterService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">10.0<");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "test attribute manipulations in data handling test case")
    public void manipulateXMLAttributes() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<p:XMLAttributeProcessRequest xmlns:p=\"http://eclipse.org/bpel/sample\">\n" +
                "      <p:input>1</p:input>\n" +
                "   </p:XMLAttributeProcessRequest>";
        String operation = "initiate";
        String serviceName = "XMLAttributeProcessService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("testUserIdAttribute=\"1\"");
        expectedOutput.add("testAttribute=\"testAttributeValue\"");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                1, expectedOutput, BPSTestUtils.TWO_WAY);
    }


    @BeforeClass(groups = {"wso2.bps"})
    public void setup() {
        log.info("Setting up Data Handling Test...");
        SERVICE_URL_PREFIX = "https://" + FrameworkSettings.HOST_NAME + ":" +
                             FrameworkSettings.HTTPS_PORT + "/services/";
    }
}
