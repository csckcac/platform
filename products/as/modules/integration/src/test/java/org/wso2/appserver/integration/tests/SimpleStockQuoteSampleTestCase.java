/*
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
 */

package org.wso2.appserver.integration.tests;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;

import static org.testng.Assert.assertEquals;

public class SimpleStockQuoteSampleTestCase {

    private static final Log log = LogFactory.getLog(CommodityQuoteTestCase.class);
    private static final String endpoint = "http://localhost:9763/services/SimpleStockQuoteService";

    @Test(groups = {"wso2.as"}, enabled = false)
    public void testGetSimpleQuoteRequest() throws AxisFault, XMLStreamException {

        String action = "getSimpleQuote";
        ClientConnectionUtil.waitForPort(9763);
        ServiceClient serviceClient = new ServiceClient();
        Options options = new Options();
        options.setTo(new EndpointReference(endpoint));
        options.setAction("urn:" + action);
        serviceClient.setOptions(options);

        OMElement payload = createPayload(action);
        assert payload != null : "Payload cannot be null";
        OMElement result = serviceClient.sendReceive(payload);
        assert result != null : "Result cannot be null";
        OMElement name = result.getFirstElement().getFirstChildWithName(
                new QName("http://services.samples/xsd", "name"));
        OMElement symbol = result.getFirstElement().getFirstChildWithName(
                new QName("http://services.samples/xsd", "symbol"));

        assertEquals("WSO2 Company", name.getText().trim());
        assertEquals("http://services.samples/xsd", name.getNamespace().getNamespaceURI());

        assertEquals("WSO2", symbol.getText().trim());
        assertEquals("http://services.samples/xsd", symbol.getNamespace().getNamespaceURI());

    }

    @Test(groups = {"wso2.as"}, enabled = false)
    public void testMarketActivityRequest() throws AxisFault, XMLStreamException {
        String action = "getMarketActivity";
        ClientConnectionUtil.waitForPort(9763);
        ServiceClient serviceClient = new ServiceClient();
        Options options = new Options();
        options.setTo(new EndpointReference(endpoint));
        options.setAction("urn:" + action);
        serviceClient.setOptions(options);

        OMElement payload = createPayload(action);
        assert payload != null : "Payload cannot be null";
        OMElement result = serviceClient.sendReceive(payload);
        assert result != null : "Result cannot be null";

        OMElement quotes = result.getFirstElement().getFirstChildWithName(
                new QName("http://services.samples/xsd", "quotes"));
        OMElement name = quotes.getFirstChildWithName(
                new QName("http://services.samples/xsd", "name"));
        OMElement symbol = quotes.getFirstChildWithName(
                new QName("http://services.samples/xsd", "symbol"));

        assertEquals("WSO2 Company", name.getText().trim());
        assertEquals("http://services.samples/xsd", name.getNamespace().getNamespaceURI());
        assertEquals("WSO2", symbol.getText().trim());
        assertEquals("http://services.samples/xsd", symbol.getNamespace().getNamespaceURI());

        OMElement quotesNext = (OMElement) quotes.getNextOMSibling();
        OMElement nameLK = quotesNext.getFirstChildWithName(
                new QName("http://services.samples/xsd", "name"));
        OMElement symbolLK = quotesNext.getFirstChildWithName(
                new QName("http://services.samples/xsd", "symbol"));

        assertEquals("LK Company", nameLK.getText().trim());
        assertEquals("http://services.samples/xsd", nameLK.getNamespace().getNamespaceURI());
        assertEquals("LK", symbolLK.getText().trim());
        assertEquals("http://services.samples/xsd", symbolLK.getNamespace().getNamespaceURI());
    }

    private OMElement createPayload(String action) throws XMLStreamException {

        String request = null;
        if ("getSimpleQuote".equals(action)) {
            request = "<ns1:" + action + "Request xmlns:ns1=\"http://services.samples\">" +
                    "<symbol>WSO2</symbol></ns1:" + action + "Request>";
        } else if ("getMarketActivity".equals(action)) {

            request = "<ns1:" + action + "Request xmlns:ns1=\"http://services.samples\">" +
                    "<ns1:request>" +
                        "<symbols>WSO2</symbols>" +
                        "<symbols>LK</symbols>" +
                    "</ns1:request>" + "</ns1:" + action + "Request>";
        }

        log.debug(request);
        return new StAXOMBuilder(new ByteArrayInputStream(request.getBytes())).getDocumentElement();
    }
}
