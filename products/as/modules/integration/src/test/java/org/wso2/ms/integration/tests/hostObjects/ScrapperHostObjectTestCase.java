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
 * Test cases for Scrapper Host Object
 */
public class ScrapperHostObjectTestCase {

    @Test(groups = {"wso2.ms"},
          description = "Test Scrapper Host Object")
    public void testScrap() throws AxisFault, XMLStreamException {
        ClientConnectionUtil.waitForPort(9763);
        ServiceClient serviceClient = new ServiceClient();
        Options options = new Options();
        options.setTo(new EndpointReference("http://localhost:9763/services/admin/scrapperTest/"));
        options.setAction("urn:testScrap");
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        serviceClient.setOptions(options);

        OMElement result = serviceClient.sendReceive(createPayload());
        assertNotNull(result, "Result cannot be null");
        assertEquals(result.toString().trim(),
                     "<ws:testScrapResponse xmlns:ws=\"http://services.mashup.wso2.org/scrapperTest"
                     + "?xsd\"><return xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:js=\"htt"
                     + "p://www.wso2.org/ns/jstype\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-i"
                     + "nstance\" js:type=\"string\" xsi:type=\"xs:string\">Response is not null or"
                     + " empty</return></ws:testScrapResponse>"
        );
    }

    private OMElement createPayload() throws XMLStreamException {
        String request = "<p:testScrap xmlns:p=\"http://www.wso2.org/types\">" +
                         "<name>maninda</name></p:testScrap>";
        return new StAXOMBuilder(new ByteArrayInputStream(request.getBytes())).getDocumentElement();
    }

}
