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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.net.*;

/**
 * REST process invocation tests
 */
public class RestInvocationTestCase {
    private static final Log log = LogFactory.getLog(RestInvocationTestCase.class);
    private static String SERVICE_URL_PREFIX;
    private static String JIRA_URL = "https://wso2.org/jira/";

    @Test(groups = {"wso2.bps"}, description = "Tests REST process invocation")
    public void InvokeRestService()
            throws XMLStreamException, IOException, InterruptedException, UnknownHostException {
        try {
            URL jira = new URL(JIRA_URL);
            jira.openStream();
            log.info(JIRA_URL + " is reachable...");
            String payload = "<p:testRest xmlns:p=\"http://ws.apache.org/axis2\"><issue>CARBON-9659</issue>" +
                             "</p:testRest>";
            String operation = "testRest";
            String serviceName = "RestTestService";
            List<String> expectedOutput = new ArrayList<String>();
            expectedOutput.add("<link>https://wso2.org/jira/browse/CARBON-9659</link>");

            log.info("Service: " + SERVICE_URL_PREFIX + serviceName);
            BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                     1, expectedOutput, BPSTestUtils.TWO_WAY);
        } catch (IOException e) {
            log.error("Internet connection is not available. Ignoring the external REST invocation...", e);
        }

    }

    @BeforeClass(groups = {"wso2.bps"}, description = "initializing Rest Invocation test")
    public void setup() {
        log.info("Initializing in REST Invocation Test...");
        SERVICE_URL_PREFIX = "https://" + FrameworkSettings.HOST_NAME + ":" +
                             FrameworkSettings.HTTPS_PORT + "/services/";
    }

}
