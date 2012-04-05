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

public class BPELExtensionTestCase {
    private static final Log log = LogFactory.getLog(BPELExtensionTestCase.class);

    private static String SERVICE_URL_PREFIX;

    @Test(groups = {"wso2.bps"}, description = "Process Property in BPEL extension test")
    public void processProperty() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<p:hello xmlns:p=\"http://ode/bpel/unit-test.wsdl\">" +
                         "<TestPart>Hello</TestPart></p:hello>";
        String operation = "hello";
        String serviceName = "TestProcessPropertiesService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">Hello From BPS.<");

        log.info("Service: " + SERVICE_URL_PREFIX + serviceName);
        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }

    @BeforeGroups(groups = {"wso2.bps"}, description = "initializing BPEL extension test")
    public void setup() {
        log.info("Initializing BPEL Extension Test...");
        SERVICE_URL_PREFIX = "https://" + FrameworkSettings.HOST_NAME + ":" +
                             FrameworkSettings.HTTPS_PORT + "/services/";
    }
}

