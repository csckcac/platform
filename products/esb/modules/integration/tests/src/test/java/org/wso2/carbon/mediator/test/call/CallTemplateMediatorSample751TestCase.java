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
package org.wso2.carbon.mediator.test.call;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.authenticators.AuthenticatorClient;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.esb.ESBTestCaseUtils;
import org.wso2.carbon.automation.utils.esb.StockQuoteClient;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class CallTemplateMediatorSample751TestCase {
    private StockQuoteClient axis2Client;
    private AuthenticatorClient adminServiceAuthentication;
    private String sessionCookie = null;
    private static final Log log = LogFactory.getLog(CallTemplateMediatorSample751TestCase.class);
    private String backEndUrl = null;
    private String nhttpPort = null;
    private String hostName=null;
    private String proxyServiceName="SplitAggregateProxy";
    private MessageContext outMsgCtx;
    private ConfigurationContext cfgCtx;
    private ServiceClient serviceClient;
    private OMNamespace omNs;
    private OMElement method;
    private SOAPFactory fac;
    private SOAPEnvelope envelope;
    private int iterations=4;
    private String symbol="IBM";
    private String repositoryPath = "samples" + File.separator + "axis2Client" +File.separator + "client_repo";
    private File repository = new File(repositoryPath);
    @BeforeTest(alwaysRun = true)
    public void setEnvironment() throws Exception, IOException {

        EnvironmentBuilder builder = new EnvironmentBuilder().esb(3);
        ManageEnvironment environment = builder.build();
        backEndUrl = environment.getEsb().getBackEndUrl();
        sessionCookie = environment.getEsb().getSessionCookie();
        nhttpPort = environment.getEsb().getProductVariables().getNhttpPort();
        hostName=environment.getEsb().getProductVariables().getHostName();
        adminServiceAuthentication = environment.getEsb().getAdminServiceAuthentication();
        loadSampleESBConfiguration(751);

    }

    @Test(groups = {"wso2.esb"}, description = "Stereotyping XSLT Transformations with Templates " +
                                               ":Test using sample 751")
    public void testXSLTTransformationWithTemplates() throws IOException, XMLStreamException {
        // OMElement response=axis2Client.sendMultipleQuoteRequest(getProxyServiceURL(),null,"IBM",4);
        String soapResponse=getResponse();
        assertNotNull(soapResponse,"Response message is null");
        OMElement response=AXIOMUtil.stringToOM(soapResponse);
        OMElement soapBody=response.getFirstElement();
        Iterator quoteBody= soapBody.getChildElements();
        int count = 0;
        while (quoteBody.hasNext())
        {
            OMElement getQuote = (OMElement)quoteBody.next();
            String test=getQuote.getLocalName();
            assertEquals(test,"getQuoteResponse","getQuoteResponse tag not in response");
            OMElement omElement=getQuote.getFirstElement();
            String symbolResponse=omElement.getFirstChildWithName
                    (new QName("http://services.samples/xsd","symbol")).getText();
            assertEquals(symbolResponse,symbol,"Symbol not match");
            count++;
        }
        assertEquals(count,iterations,"Iterations not match");
    }

    protected String getProxyServiceURL() {
        return "http://" + hostName + ":" +nhttpPort+"/services/"+proxyServiceName;
    }

    protected void loadSampleESBConfiguration(int sampleNo) throws Exception {
        ESBTestCaseUtils esbUtils = new ESBTestCaseUtils();
        esbUtils.loadSampleESBConfiguration(sampleNo,backEndUrl,sessionCookie);
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

    private void setMessageContext()
    {
        outMsgCtx = new MessageContext();
        //assigning message context&rsquo;s option object into instance variable
        Options opts = outMsgCtx.getOptions();
        //setting properties into option
        opts.setTo(new EndpointReference(getProxyServiceURL()));
        opts.setAction("urn:getQuote");
    }
    public SOAPEnvelope createSOAPEnvelope() {
        fac = OMAbstractFactory.getSOAP11Factory();
        envelope = fac.getDefaultEnvelope();
        envelope.getBody().addChild(createMultipleQuoteRequestBody(symbol, iterations));
        return envelope;
    }
}
