/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.mediator.tests.xquery;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.SampleAxis2Server;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class XQueryCustom extends ESBIntegrationTestCase {
    private StockQuoteClient axis2Client;

    public void init() throws Exception {
        axis2Client = new StockQuoteClient();
        String filePath = "/mediators/xquery/synapse101.xml";
        loadESBConfigurationFromClasspath(filePath);

        launchBackendAxis2Service(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
    }

    @Test(groups = {"wso2.esb"},
          description = "Do XQuery transformation with target attribute specified as XPath value - <xquery key=\"string\" target = xpath>")
    public void testXQueryTransformation() throws AxisFault {
        OMElement response;

        response = sendReceive(
                getProxyServiceURL("StockQuoteProxy", false),
                "IBM");
        assertNotNull(response, "Response message null");
        assertTrue(response.toString().contains("IBM"));

    }

    @Override
    protected void cleanup() {
        super.cleanup();
        axis2Client.destroy();
    }

    private OMElement sendReceive(String endPointReference, String symbol)
            throws AxisFault {
        ServiceClient sender;
        Options options;
        OMElement response = null;

        sender = new ServiceClient();
        options = new Options();
        options.setTo(new EndpointReference(endPointReference));
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        options.setAction("urn:getQuote");

        sender.setOptions(options);

        response = sender.sendReceive(getCustomPayload(symbol));

        return response;
    }

    private OMElement getCustomPayload(String symbol) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://services.samples", "ns");
        OMElement payload = fac.createOMElement("getQuote", omNs);
        OMElement request = fac.createOMElement("request", omNs);
        OMElement symbols = fac.createOMElement("symbols", omNs);

        OMElement company = fac.createOMElement("company", omNs);
        company.setText(symbol);

        symbols.addChild(company);
        request.addChild(symbols);
        payload.addChild(request);
        return payload;
    }

}
