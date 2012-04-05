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

/**
 * BPEL message routing tests
 */
public class MessageRoutingTestCase {
    private static final Log log = LogFactory.getLog(MessageRoutingTestCase.class);
    private static String SERVICE_URL_PREFIX;

    @Test(groups = {"wso2.bps"}, description = "correlation opaque init foo in message routing test")
    private void correlationOpaqueInitFoo()
            throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<un:init xmlns:un=\"http://example.com/bpel/counter\"><name>foo</name>" +
                         "<alias>foo.alias</alias></un:init>";
        String operation = "init";
        String serviceName = "counterService2";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("initResponse");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "correlation opaque get bar in message routing test",
          dependsOnMethods = "correlationOpaqueGetAndIncrementFoo")
    private void correlationOpaqueGetBar()
            throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<un:get xmlns:un=\"http://example.com/bpel/counter\">" +
                         "<name>bar</name><alias>get.alias</alias></un:get>";
        String operation = "get";
        String serviceName = "counterService2";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("<name>get.alias</name>");
        expectedOutput.add("<value>0.0</value>");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

/*    @Test(groups = {"wso2.bps"}, description = "correlation opaque get bar fault in message routing test",
          dependsOnMethods = "correlationOpaqueGetBar", expectedExceptions = AxisFault.class)*/
    private void correlationOpaqueGetBarFault() throws XMLStreamException, AxisFault {
        String payload = "<un:get xmlns:un=\"http://example.com/bpel/counter\">" +
                         "<name>bar</name><alias>get.alias</alias></un:get>";
        String operation = "get";
        String serviceName = "counterService2";

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, "AxisFault", BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "correlation opaque init bar in message routing test",
          dependsOnMethods = "correlationOpaqueInitFoo")
    private void correlationOpaqueInitBar()
            throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<un:init xmlns:un=\"http://example.com/bpel/counter\">" +
                         "<name>bar</name><alias>bar.alias</alias></un:init>";
        String operation = "init";
        String serviceName = "counterService2";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("initResponse");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "correlation opaque get and increment foo in message" +
                                               " routing test", dependsOnMethods = "correlationOpaqueInitBar")
    private void correlationOpaqueGetAndIncrementFoo()
            throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<un:getAndIncrement xmlns:un=\"http://example.com/bpel/counter\">" +
                         "<name>foo</name><alias>incr.alias</alias></un:getAndIncrement>";
        String operation = "getAndIncrement";
        String serviceName = "counterService2";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("<name>foo</name>");
        expectedOutput.add("<value>0.0</value>");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

/*    @Test(groups = {"wso2.bps"}, description = "correlation opaque GetAndIncrementFooFault in message routing test",
          dependsOnMethods = "correlationOpaqueGetAndIncrementFoo", expectedExceptions = AxisFault.class)*/
    private void correlationOpaqueGetAndIncrementFooFault() throws XMLStreamException, AxisFault {
        String payload = "<un:getAndIncrement xmlns:un=\"http://example.com/bpel/counter\">" +
                         "<name>foo</name><alias>incr.alias</alias></un:getAndIncrement>";
        String operation = "getAndIncrement";
        String serviceName = "counterService2";

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, "AxisFault", BPSTestUtils.TWO_WAY);
    }

/*    @Test(groups = {"wso2.bps"}, description = "correlation opaque InitFooFault in message routing test",
          dependsOnMethods = "correlationOpaqueInitFoo", expectedExceptions = AxisFault.class)*/
    private void correlationOpaqueInitFooFault() throws XMLStreamException, AxisFault {
        String payload = "<un:init xmlns:un=\"http://example.com/bpel/counter\"><name>foo</name>" +
                         "<alias>foo.alias</alias></un:init>";
        String operation = "init";
        String serviceName = "counterService2";

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, "AxisFault", BPSTestUtils.TWO_WAY);
    }

    @Test(groups = {"wso2.bps"}, description = "my Role MEX retention in message routing test",
          dependsOnMethods = "correlationOpaqueGetBar")
    private void myRoleMEXRetention() throws XMLStreamException, AxisFault, InterruptedException {
        // https://wso2.org/jira/browse/CARBON-9659

        String payload = "<sam:MyRoleMexTestProcessRequest xmlns:sam=\"http://wso2.org/bpel/sample\">" +
                         "<sam:input>test</sam:input></sam:MyRoleMexTestProcessRequest>";
        String operation = "init";
        String serviceName = "MyRoleMexTestProcessService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">test<");

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);

        payload = "<sam:process xmlns:sam=\"http://wso2.org/bpel/sample\"><sam:in>test</sam:in>" +
                  "</sam:process>";
        operation = "process";

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @BeforeClass(groups = {"wso2.bps"}, description = "initializing message routing test")
    public void setup() {
        log.info("Initializing Message Routing Test...");
        SERVICE_URL_PREFIX = "https://" + FrameworkSettings.HOST_NAME + ":" +
                             FrameworkSettings.HTTPS_PORT + "/services/";
    }

}

