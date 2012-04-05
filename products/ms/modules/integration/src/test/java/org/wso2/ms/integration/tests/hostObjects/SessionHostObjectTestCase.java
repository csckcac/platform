/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.ms.integration.tests.hostObjects;

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

/**
 * Test cases for Session Host Object
 */
public class SessionHostObjectTestCase {
    @Test(groups = {"wso2.ms"}, description = "Test putting a sample value into the Session Host Object")
    public void testPutValue() throws AxisFault, XMLStreamException {
        ClientConnectionUtil.waitForPort(9763);
        ServiceClient serviceClient = new ServiceClient();
        Options options = new Options();
        options.setTo(new EndpointReference("http://localhost:9763/services/admin/sessionTest/"));
        options.setAction("urn:putValue");
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        serviceClient.setOptions(options);

        OMElement result = serviceClient.sendReceive(createPayload());
        assertNotNull(result, "Result cannot be null");
        assertEquals(result.toString().trim(),
                     "<ws:putValueResponse xmlns:ws=\"http://services.mashup.wso2.org/sessionTest"
                     + "?xsd\"><return xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:js"
                     + "=\"http://www.wso2.org/ns/jstype\" xmlns:xsi=\"http://www.w3.org/2001/XMLS"
                     + "chema-instance\" js:type=\"string\" xsi:type=\"xs:string\">number</return"
                     + "></ws:putValueResponse>"
        );
    }

    @Test(groups = {"wso2.ms"}, dependsOnMethods = "testPutValue",
          description = "Test getting a sample value from the Session Host Object")
    public void testGetValue() throws AxisFault, XMLStreamException {
        ClientConnectionUtil.waitForPort(9763);
        ServiceClient serviceClient = new ServiceClient();
        Options options = new Options();
        options.setTo(new EndpointReference("http://localhost:9763/services/admin/sessionTest/"));
        options.setAction("urn:getValue");
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        serviceClient.setOptions(options);

        OMElement result = serviceClient.sendReceive(createPayload());
        assertNotNull(result, "Result cannot be null");
        assertEquals(result.toString().trim(),
                     "<ws:getValueResponse xmlns:ws=\"http://services.mashup.wso2.org/sessionTest"
                     + "?xsd\"><return xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:js=\"ht"
                     + "tp://www.wso2.org/ns/jstype\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchem"
                     + "a-instance\" js:type=\"number\" xsi:type=\"xs:double\">2</return></ws:getV"
                     + "alueResponse>"
        );
    }

    @Test(groups = {"wso2.ms"}, dependsOnMethods = "testGetValue",
          description = "Test removing a sample value from the Session Host Object")
    public void testRemoveValue() throws AxisFault, XMLStreamException {
        ClientConnectionUtil.waitForPort(9763);
        ServiceClient serviceClient = new ServiceClient();
        Options options = new Options();
        options.setTo(new EndpointReference("http://localhost:9763/services/admin/sessionTest/"));
        options.setAction("urn:removeValue");
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        serviceClient.setOptions(options);

        OMElement result = serviceClient.sendReceive(createPayload());
        assertNotNull(result, "Result cannot be null");
        assertEquals(result.toString().trim(),
                     "<ws:removeValueResponse xmlns:ws=\"http://services.mashup.wso2.org/session"
                     + "Test?xsd\"><return xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:js"
                     + "=\"http://www.wso2.org/ns/jstype\" xmlns:xsi=\"http://www.w3.org/2001/XML"
                     + "Schema-instance\" js:type=\"xml\" xsi:type=\"xs:anyType\"><success/></ret"
                     + "urn></ws:removeValueResponse>"
        );
    }

    @Test(groups = {"wso2.ms"}, dependsOnMethods = "testRemoveValue",
          description = "Test clearing the Session Host Object")
    public void testClearSession() throws AxisFault, XMLStreamException {
        ClientConnectionUtil.waitForPort(9763);
        ServiceClient serviceClient = new ServiceClient();
        Options options = new Options();
        options.setTo(new EndpointReference("http://localhost:9763/services/admin/sessionTest/"));
        options.setAction("urn:clearSession");
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        serviceClient.setOptions(options);

        OMElement result = serviceClient.sendReceive(createPayload());
        assertNotNull(result, "Result cannot be null");
        assertEquals(result.toString().trim(),
                     "<ws:clearSessionResponse xmlns:ws=\"http://services.mashup.wso2.org/session"
                     + "Test?xsd\"><return xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:js"
                     + "=\"http://www.wso2.org/ns/jstype\" xmlns:xsi=\"http://www.w3.org/2001/XML"
                     + "Schema-instance\" js:type=\"xml\" xsi:type=\"xs:anyType\"><success/></re"
                     + "turn></ws:clearSessionResponse>"
        );
    }

    private OMElement createPayload() throws XMLStreamException {
        String request = "<p:sendEmail xmlns:p=\"http://www.wso2.org/types\">" +
                         "<name>maninda</name></p:sendEmail>";
        return new StAXOMBuilder(new ByteArrayInputStream(request.getBytes())).getDocumentElement();
    }
}
