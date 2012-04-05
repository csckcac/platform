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
        options.setTo(new EndpointReference("http://localhost:9763/" +
                "java_first_jaxws/services/hello_world"));

        options.setAction("");
        serviceClient.setOptions(options);

        // Call sayHi operation..
        String request1 = "<ns2:sayHi xmlns:ns2=\"http://server.hw.demo/\">" +
                "<arg0>World</arg0></ns2:sayHi>";
        OMElement result1 = serviceClient.sendReceive(createPayload(request1));
        assertNotNull(result1, "Result cannot be null");
        assertEquals(("<ns1:sayHiResponse xmlns:ns1=\"http://server.hw.demo/\">" +
                "<return>Hello World</return></ns1:sayHiResponse>"),
                result1.toString().trim());

        // Call sayHiToUser operation..
        String request2 = "<ns2:sayHiToUser xmlns:ns2=\"http://server.hw.demo/\">" +
                "<arg0><name>World</name></arg0></ns2:sayHiToUser>";
        OMElement result2 = serviceClient.sendReceive(createPayload(request2));
        assertNotNull(result2, "Result cannot be null");
        assertEquals(("<ns1:sayHiToUserResponse xmlns:ns1=\"http://server.hw.demo/\">" +
                "<return>Hello World</return></ns1:sayHiToUserResponse>"),
                result2.toString().trim());

        // Call sayHiToUser operation again..
        String request3 = "<ns2:sayHiToUser xmlns:ns2=\"http://server.hw.demo/\">" +
                "<arg0><name>Galaxy</name></arg0></ns2:sayHiToUser>";
        OMElement result3 = serviceClient.sendReceive(createPayload(request3));
        assertNotNull(result3, "Result cannot be null");
        assertEquals(("<ns1:sayHiToUserResponse xmlns:ns1=\"http://server.hw.demo/\">" +
                "<return>Hello Galaxy</return></ns1:sayHiToUserResponse>"),
                result3.toString().trim());

        // Call sayHi operation..
        String request4 = "<ns2:getUsers xmlns:ns2=\"http://server.hw.demo/\"/>";
        OMElement result4 = serviceClient.sendReceive(createPayload(request4));
        assertNotNull(result4, "Result cannot be null");
        assertEquals(("<ns1:getUsersResponse xmlns:ns1=\"http://server.hw.demo/\">" +
                "<return><entry><id>1</id><user><name>World</name></user></entry><entry>" +
                "<id>2</id><user><name>Galaxy</name></user></entry></return>" +
                "</ns1:getUsersResponse>"),
                result4.toString().trim());
    }

    private OMElement createPayload(String request) throws XMLStreamException {
        return new StAXOMBuilder(new ByteArrayInputStream(request.getBytes())).getDocumentElement();
    }
}
