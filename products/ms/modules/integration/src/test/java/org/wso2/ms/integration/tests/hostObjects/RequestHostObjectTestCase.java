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
 * Test cases for Request Host Object
 */
public class RequestHostObjectTestCase {

// TODO
//    @Test(groups = {"wso2.ms"},
//          description = "Test Authenticated User of the Request Host Object")
//    public void testTestAuthenticatedUser() throws AxisFault, XMLStreamException {
//        ClientConnectionUtil.waitForPort(9763);
//        ServiceClient serviceClient = new ServiceClient();
//        Options options = new Options();
//        options.setTo(new EndpointReference("http://localhost:9763/services/admin/requestTest/"));
//        options.setAction("urn:testAuthenticatedUser");
//        serviceClient.setOptions(options);
//
//        OMElement result = serviceClient.sendReceive(createPayload());
//        assertNotNull(result, "Result cannot be null");
//        assertEquals(result.toString().trim(),
//                     "<ws:clearSessionResponse xmlns:ws=\"http://services.mashup.wso2.org/session"
//                     + "Test?xsd\"><return xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:js"
//                     + "=\"http://www.wso2.org/ns/jstype\" xmlns:xsi=\"http://www.w3.org/2001/XML"
//                     + "Schema-instance\" js:type=\"xml\" xsi:type=\"xs:anyType\"><success /></re"
//                     + "turn></ws:clearSessionResponse>"
//        );
//    }

    @Test(groups = {"wso2.ms"},
          description = "Test Remote IP of the Request Host Object")
    public void testTestRemoteIp() throws AxisFault, XMLStreamException {
        ClientConnectionUtil.waitForPort(9763);
        ServiceClient serviceClient = new ServiceClient();
        Options options = new Options();
        options.setTo(new EndpointReference("http://localhost:9763/services/admin/requestTest/"));
        options.setAction("urn:testRemoteIp");
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        serviceClient.setOptions(options);

        OMElement result = serviceClient.sendReceive(createPayload());
        assertNotNull(result, "Result cannot be null");
        assertEquals(result.toString().trim(),
                     "<ws:testRemoteIpResponse xmlns:ws=\"http://services.mashup.wso2.org/request"
                     + "Test?xsd\"><return xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:js"
                     + "=\"http://www.wso2.org/ns/jstype\" xmlns:xsi=\"http://www.w3.org/2001/XMLS"
                     + "chema-instance\" js:type=\"string\" xsi:type=\"xs:string\">Remote IP is not"
                     + " empty or null</return></ws:testRemoteIpResponse>"
        );
    }

    @Test(groups = {"wso2.ms"},
          description = "Test invoked URL of the Request Host Object")
    public void testTestInvokedUrl() throws AxisFault, XMLStreamException {
        ClientConnectionUtil.waitForPort(9763);
        ServiceClient serviceClient = new ServiceClient();
        Options options = new Options();
        options.setTo(new EndpointReference("http://localhost:9763/services/admin/requestTest/"));
        options.setAction("urn:testInvokedUrl");
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        serviceClient.setOptions(options);

        OMElement result = serviceClient.sendReceive(createPayload());
        assertNotNull(result, "Result cannot be null");
        assertEquals(result.toString().trim(),
                     "<ws:testInvokedUrlResponse xmlns:ws=\"http://services.mashup.wso2.org/reque"
                     + "stTest?xsd\"><return xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmln"
                     + "s:js=\"http://www.wso2.org/ns/jstype\" xmlns:xsi=\"http://www.w3.org/200"
                     + "1/XMLSchema-instance\" js:type=\"string\" xsi:type=\"xs:string\">URL is not"
                     + " empty or null</return></ws:testInvokedUrlResponse>"
        );
    }

    private OMElement createPayload() throws XMLStreamException {
        String request = "<p:sendRequest xmlns:p=\"http://www.wso2.org/types\">" +
                         "<name>maninda</name></p:sendRequest>";
        return new StAXOMBuilder(new ByteArrayInputStream(request.getBytes())).getDocumentElement();
    }

}
