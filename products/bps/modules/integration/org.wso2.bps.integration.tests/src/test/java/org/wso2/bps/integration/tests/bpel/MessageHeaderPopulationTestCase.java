/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.bps.integration.tests.bpel;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
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
 * This test class intended to verify https://wso2.org/jira/browse/CARBON-12386
 */
public class MessageHeaderPopulationTestCase {
    /**
     * Class Logger
     */
    private static Log log = LogFactory.getLog(MessageHeaderPopulationTestCase.class);
    private static String SERVICE_URL_PREFIX;

    @BeforeClass
    public void init() {
        log.info("Initializing Message Header Manipulation Test...");

        SERVICE_URL_PREFIX = "https://" + FrameworkSettings.HOST_NAME + ":" +
                             FrameworkSettings.HTTPS_PORT + "/services/";
    }

    /**
     * Test whether the header assigned by the invoking BPEL logic is correctly rectified to the client
     * See below for the particular assign operation
     *
     * <bpel:copy>
     *   <bpel:from>
     *       <bpel:literal>
     *           <wsa:ReplyTo     xmlns:wsa="http://www.w3.org/2005/08/addressing">
     *               <wsa:Address>http://10.100.3.111:9763/services/headerModifyProcesService</wsa:Address>
     *           </wsa:ReplyTo>
     *       </bpel:literal>
     *   </bpel:from>
     *   <bpel:to variable="output" header="ReplyTo"/>
     *  </bpel:copy>
     *
     * @throws XMLStreamException
     * @throws AxisFault
     * @throws InterruptedException
     */
    @Test(description = "Header Assignment test")
    public void testHeaderAssignment() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<sam:headerModifyProcessRequest xmlns:sam=\"http://eclipse.org/bpel/sample\">\n" +
                         "         <sam:input>1</sam:input>\n" +
                         "      </sam:headerModifyProcessRequest>";
        String operation = "process";
        String serviceName = "headerModifyProcesService";
        List<String> expectedOutput = new ArrayList<String>();

        //Nothing expected from Body element as here we test Header related functionality
        expectedOutput.add("");

        List<OMElement> expectedHeaderList = new ArrayList<OMElement>();
        String expectedHeaderString = "<wsa:ReplyTo xmlns:wsa=\"http://www.w3" +
                                      ".org/2005/08/addressing\">\n" +
                                      "<wsa:Address>http://10.100.3.111:9763/services/headerModifyProcesService</wsa:Address>\n" +
                                      "</wsa:ReplyTo>";
        OMElement expectedHeader = AXIOMUtil.stringToOM(expectedHeaderString);
        expectedHeaderList.add(expectedHeader);

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY, null,
                                 expectedHeaderList);
    }

    /**
     * Test whether the header included by client is propagated back from the BPEL engine
     * Methodology - Here we add a header <Address1/> and check whether it's included in the
     * reply SOAP Envelop's header.
     * @throws XMLStreamException
     * @throws AxisFault
     * @throws InterruptedException
     */
    @Test(description = "Header Propagation test")
    public void testHeaderPropagation() throws XMLStreamException, AxisFault, InterruptedException {
        String payload = "<sam:headerModifyProcessRequest xmlns:sam=\"http://eclipse.org/bpel/sample\">\n" +
                         "         <sam:input>2</sam:input>\n" +
                         "      </sam:headerModifyProcessRequest>";
        String operation = "process";
        String serviceName = "headerModifyProcesService";
        List<String> expectedOutput = new ArrayList<String>();

        //Nothing expected from Body element as here we test Header related functionality
        expectedOutput.add("");

        String headerString = "<Address1>http://10.100.3.111:9763/services/headerModifyProcesService</Address1>";
        OMElement header = AXIOMUtil.stringToOM(headerString);
        List<OMElement> headerList = new ArrayList<OMElement>();
        headerList.add(header);

        List<OMElement> expectedHeaderList = new ArrayList<OMElement>();
        String expectedHeaderString = "<wsa:ReplyTo xmlns:wsa=\"http://www.w3" +
                                      ".org/2005/08/addressing\">\n" +
                                      "<wsa:Address>http://10.100.3.111:9763/services/headerModifyProcesService</wsa:Address>\n" +
                                      "</wsa:ReplyTo>";
        OMElement expectedHeader = AXIOMUtil.stringToOM(expectedHeaderString);
        //This header is included by the BPEL logic. So it's added as an expected header.
        expectedHeaderList.add(expectedHeader);

        //Adding the already existing header to the expected header-List
        expectedHeaderList.add(header);

        BPSTestUtils.sendRequest(SERVICE_URL_PREFIX + serviceName, operation, payload,
                                 1, expectedOutput, BPSTestUtils.TWO_WAY, headerList, expectedHeaderList);
    }

}
