package org.wso2.carbon.mediator.test.call;/*
import org.wso2.carbon.automation.utils.esb.ESBTestCaseUtils;
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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.mediator.test.ESBMediatorTest;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class CallTemplateWithValuesAndExpressionTestCase extends ESBMediatorTest {

    private final String proxyServiceName = "SplitAggregateProxy";
    private MessageContext outMsgCtx;
    private ConfigurationContext cfgCtx;
    private ServiceClient serviceClient;
    private OMNamespace omNs;
    private OMElement method;
    private SOAPFactory fac;
    private SOAPEnvelope envelope;
    private final int iterations = 4;
    private String symbol = "IBM";
    private String repositoryPath = "samples" + File.separator + "axis2Client" + File.separator +
                                    "client_repo";
    private File repository = new File(repositoryPath);

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath("/artifacts/ESB/mediatorconfig/call/synapse_expressions.xml");

    }

    @Test(groups = {"wso2.esb"}, description = "Template with values and expressions")
    public void testTemplateWithValuesAndExpressions() throws IOException, XMLStreamException {
        String soapResponse = getResponse();
        assertNotNull(soapResponse, "Response message is null");
        OMElement response = AXIOMUtil.stringToOM(soapResponse);
        OMElement soapBody = response.getFirstElement();
        Iterator quoteBody = soapBody.getChildElements();
        int count = 0;
        while (quoteBody.hasNext()) {
            OMElement getQuote = (OMElement) quoteBody.next();
            String test = getQuote.getLocalName();
            assertEquals(test, "getQuoteResponse", "getQuoteResponse not match");
            OMElement omElement = getQuote.getFirstElement();
            String symbolResponse = omElement.getFirstChildWithName(
                    new QName("http://services.samples/xsd", "symbol")).getText();
            assertEquals(symbolResponse, "WSO2", "Request symbol not changed");

            count++;
        }
        assertEquals(count, iterations, "number of responses different from requests");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() {
        super.cleanup();
    }

    private OMElement createMultipleQuoteRequestBody(String symbol, int iterations) {
        omNs = fac.createOMNamespace("http://services.samples", "ns");
        method = fac.createOMElement("getQuote", omNs);

        for (int i = 0; i < iterations; i++) {
            OMElement value1 = fac.createOMElement("request", omNs);
            OMElement value2 = fac.createOMElement("symbol", omNs);
            value2.addChild(fac.createOMText(value1, symbol));
            value1.addChild(value2);
            method.addChild(value1);
        }
        return method;
    }

    private String getResponse() throws IOException {
        cfgCtx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                repository.getCanonicalPath(), null);
        serviceClient = new ServiceClient(cfgCtx, null);
        OperationClient operationClient = serviceClient.createClient(ServiceClient.ANON_OUT_IN_OP);
        setMessageContext();
        outMsgCtx.setEnvelope(createSOAPEnvelope());
        operationClient.addMessageContext(outMsgCtx);
        operationClient.execute(true);
        MessageContext inMsgtCtx = operationClient.getMessageContext("In");
        SOAPEnvelope response = inMsgtCtx.getEnvelope();
        return response.toString();

    }

    private void setMessageContext() {
        outMsgCtx = new MessageContext();
        //assigning message context&rsquo;s option object into instance variable
        Options opts = outMsgCtx.getOptions();
        //setting properties into option
        opts.setTo(new EndpointReference(getProxyServiceURL(proxyServiceName)));
        opts.setAction("urn:getQuote");
    }

    public SOAPEnvelope createSOAPEnvelope() {
        fac = OMAbstractFactory.getSOAP11Factory();
        envelope = fac.getDefaultEnvelope();
        envelope.getBody().addChild(createMultipleQuoteRequestBody(symbol, iterations));
        return envelope;
    }
}


