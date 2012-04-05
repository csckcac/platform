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
 * Tests the partner invocations where the partner has the binding style rpc literal
 */
public class RPCPartnerInvocationTestCase {
    private static final Log log = LogFactory.getLog(RPCPartnerInvocationTestCase.class);

    private static String SERVICE_URL_PREFIX;

    @BeforeClass(groups = {"wso2.bps"}, description = "Setting up RPC invoke test")
    public void setup() {
        log.info("initializing RPC invoke Test...");
        SERVICE_URL_PREFIX = "https://" + FrameworkSettings.HOST_NAME + ":" +
                             FrameworkSettings.HTTPS_PORT + "/services/";
    }


    @Test(groups = {"wso2.bps"}, description = "invoke RPC client process")
    private void invokeRPCClientProcess()
            throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<rpc:RPCClientProcessRequest xmlns:rpc=\"http://wso2.org/bps/rpcclient\">" +
                         "<rpc:input>Hello</rpc:input></rpc:RPCClientProcessRequest>";
        String operation = "hello";
        String serviceName = "RPCClientProcessService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">Hello World<");

        log.info("Service: " + SERVICE_URL_PREFIX + serviceName);
        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY);
    }
}

