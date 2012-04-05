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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;

import static org.testng.Assert.assertEquals;

public class CommodityQuoteTestCase {

    private static final Log log = LogFactory.getLog(CommodityQuoteTestCase.class);

    String HTTPS_SERVICE_URL = null;
    String HTTP_SERVICE_URL = null;
    String[] operations = {"getQuote", "getSymbols"};

    @BeforeMethod(groups = {"wso2.as"})
    public void init() throws java.lang.Exception {
        log.info("Initializing Commodity Quote Tests");

//      "https://localhost:9443/services/CommodityQuote";
//      "http://localhost:9763/services/CommodityQuote";
        HTTPS_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                ":" + FrameworkSettings.HTTPS_PORT + "/services/CommodityQuote";
        HTTP_SERVICE_URL = "http://" + FrameworkSettings.HOST_NAME +
                ":" + FrameworkSettings.HTTP_PORT + "/services/CommodityQuote";
    }

    @Test(groups = {"wso2.as"}, enabled = true)
    public void testGetQuoteRequest() throws AxisFault, XMLStreamException {
        ClientConnectionUtil.waitForPort(9763);
        ServiceClient serviceClient = new ServiceClient();
        Options options = new Options();
        options.setTo(new EndpointReference(HTTP_SERVICE_URL));
        //"http://localhost:9763/servi3ces/CommodityQuote"
        options.setAction("urn:" + operations[0]);
        serviceClient.setOptions(options);

        OMElement payload = createPayload();
        assert payload != null : "Payload cannot be null";
        OMElement result = serviceClient.sendReceive(payload);
        assert result != null : "Result cannot be null";
        OMElement name = result.getFirstElement().getFirstChildWithName(new QName("name"));
        OMElement symbol = result.getFirstElement().getFirstChildWithName(new QName("symbol"));
        assertEquals("<name>mn</name>", name.toString().trim());
        assertEquals("<symbol>Manganese</symbol>", symbol.toString().trim());
    }

    private OMElement createPayload() throws XMLStreamException {
        String request = "<ns1:getQuoteRequest xmlns:ns1=\"http://www.wso2.org/types\">" +
                "<symbol>mn</symbol></ns1:getQuoteRequest>";
        return new StAXOMBuilder(new ByteArrayInputStream(request.getBytes())).getDocumentElement();
    }
}
