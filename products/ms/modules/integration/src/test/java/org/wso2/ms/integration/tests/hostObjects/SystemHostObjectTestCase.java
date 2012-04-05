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
 * Test cases for System Host Object
 */
public class SystemHostObjectTestCase {

    @Test(groups = {"wso2.ms"}, description = "Test including an external JavaScript file")
    public void testIncludeJsFile() throws AxisFault, XMLStreamException {
        ClientConnectionUtil.waitForPort(9763);
        ServiceClient serviceClient = new ServiceClient();
        Options options = new Options();
        options.setTo(new EndpointReference("http://localhost:9763/services/admin/systemTest/"));
        options.setAction("urn:includeJsFile");
        serviceClient.setOptions(options);

        OMElement result = serviceClient.sendReceive(createPayload());
        assertNotNull(result, "Result cannot be null");
        assertEquals(result.toString().trim(),
                     "<ws:includeJsFileResponse xmlns:ws=\"http://services.mashup.wso2.org/systemTe"
                     + "st?xsd\"><return xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:js=\"h"
                     + "ttp://www.wso2.org/ns/jstype\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema"
                     + "-instance\" js:type=\"string\" xsi:type=\"xs:string\">Successfully concaten"
                     + "ated.</return></ws:includeJsFileResponse>"
        );
    }

    @Test(groups = {"wso2.ms"}, description = "Test the Local Host Name")
    public void testLocalHostName() throws AxisFault, XMLStreamException {
        ClientConnectionUtil.waitForPort(9763);
        ServiceClient serviceClient = new ServiceClient();
        Options options = new Options();
        options.setTo(new EndpointReference("http://localhost:9763/services/admin/systemTest/"));
        options.setAction("urn:testLocalHostName");
        serviceClient.setOptions(options);

        OMElement result = serviceClient.sendReceive(createPayload());
        assertNotNull(result, "Result cannot be null");
        assertEquals(result.toString().trim(),
                     "<ws:testLocalHostNameResponse xmlns:ws=\"http://services.mashup.wso2.org/syst"
                     + "emTest?xsd\"><return xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:js"
                     + "=\"http://www.wso2.org/ns/jstype\" xmlns:xsi=\"http://www.w3.org/2001/XMLSc"
                     + "hema-instance\" js:type=\"string\" xsi:type=\"xs:string\">Successfully got "
                     + "localHostName</return></ws:testLocalHostNameResponse>"
        );
    }

    @Test(groups = {"wso2.ms"}, description = "Test logging a sample string")
    public void testLogAString() throws AxisFault, XMLStreamException {
        ClientConnectionUtil.waitForPort(9763);
        ServiceClient serviceClient = new ServiceClient();
        Options options = new Options();
        options.setTo(new EndpointReference("http://localhost:9763/services/admin/systemTest/"));
        options.setAction("urn:logAString");
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        serviceClient.setOptions(options);

        OMElement result = serviceClient.sendReceive(createPayload());
        assertNotNull(result, "Result cannot be null");
        assertEquals(result.toString().trim(),
                     "<ws:logAStringResponse xmlns:ws=\"http://services.mashup.wso2.org/systemTest?"
                     + "xsd\"><return xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:js=\"http"
                     + "://www.wso2.org/ns/jstype\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-in"
                     + "stance\" js:type=\"string\" xsi:type=\"xs:string\">Successfully logged a St"
                     + "ring.</return></ws:logAStringResponse>"
        );
    }

    @Test(groups = {"wso2.ms"}, description = "Test waiting some time")
    public void testWaitSomeTime() throws AxisFault, XMLStreamException {
        ClientConnectionUtil.waitForPort(9763);
        ServiceClient serviceClient = new ServiceClient();
        Options options = new Options();
        options.setTo(new EndpointReference("http://localhost:9763/services/admin/systemTest/"));
        options.setAction("urn:waitSomeTime");
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        serviceClient.setOptions(options);

        OMElement result = serviceClient.sendReceive(createPayload());
        assertNotNull(result, "Result cannot be null");
        assertEquals(result.toString().trim(),
                     "<ws:waitSomeTimeResponse xmlns:ws=\"http://services.mashup.wso2.org/systemTes"
                     + "t?xsd\"><return xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:js=\"ht"
                     + "tp://www.wso2.org/ns/jstype\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-"
                     + "instance\" js:type=\"string\" xsi:type=\"xs:string\">Successfully waited</r"
                     + "eturn></ws:waitSomeTimeResponse>"
        );
    }

    private OMElement createPayload() throws XMLStreamException {
        String request = "<p:sendEmail xmlns:p=\"http://www.wso2.org/types\"></p:sendEmail>";
        return new StAXOMBuilder(new ByteArrayInputStream(request.getBytes())).getDocumentElement();
    }
}
