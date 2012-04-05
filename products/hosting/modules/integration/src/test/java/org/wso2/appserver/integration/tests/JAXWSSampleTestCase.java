/*
*Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.appserver.integration.tests;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class JAXWSSampleTestCase {

    @Test(groups = {"wso2.as"})
    public void testJAXWSRequest() throws AxisFault, XMLStreamException {
        ClientConnectionUtil.waitForPort(9763);
        ServiceClient serviceClient = new ServiceClient();
        Options options = new Options();
        options.setTo(new EndpointReference("http://localhost:9763/services/" +
                                            "Calculator.CalculatorSOAP"));
        options.setAction("urn:add");
        serviceClient.setOptions(options);

        OMElement result = serviceClient.sendReceive(createPayload());
        assertNotNull(result, "Result cannot be null");
        assertEquals(("<addResponse xmlns=\"http://calculator.jaxws.sample.appserver.wso2.org\">" +
                      "<return>6</return></addResponse>"),
                     result.toString().trim());
    }

    private OMElement createPayload() throws XMLStreamException {
        String request = "<add xmlns=\"http://calculator.jaxws.sample.appserver.wso2.org\">" +
                         "<value1>1</value1><value2>5</value2></add>";
        return new StAXOMBuilder(new ByteArrayInputStream(request.getBytes())).getDocumentElement();
    }
}