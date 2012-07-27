package org.wso2.carbon.mediator.test.enrich;/*
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
import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class EnrichMediatorReplaceEnvelopeTestCase extends ESBMediatorTest{
    private ConfigurationContext cfgCtx;
    private String repositoryPath = "samples" + File.separator + "axis2Client" + File.separator + "client_repo";
    private File repository = new File(repositoryPath);
    private ServiceClient serviceClient;
    private MessageContext outMsgCtx;
    private SOAPFactory fac;
    private SOAPEnvelope envelope;
    private OMNamespace omNs;
    private OMElement method;


    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception{
        super.init();
        String filePath="/artifacts/ESB/synapseconfig/core/synapse_envelope1.xml" ;
        loadESBConfigurationFromClasspath(filePath);
    }

    @Test(groups = "wso2.esb" , description = "Replace envelope ")
       public void testEnrichMediator() throws Exception{


        String soapResponse=getResponse();
        assertNotNull(soapResponse, "Response message null");

        OMElement response= AXIOMUtil.stringToOM(soapResponse);
        OMElement soapBody=response.getFirstChildWithName(
                new QName("http://www.w3.org/2003/05/soap-envelope","Body"));
        OMElement soapHeader=response.getFirstChildWithName(
                new QName("http://www.w3.org/2003/05/soap-envelope","Header"));
        OMElement quoteBody= soapBody.getFirstElement();
        OMElement returnBody=  quoteBody.getFirstElement();



        String symbolResponse=returnBody.getFirstChildWithName(
                new QName("http://services.samples/xsd","symbol")).getText();

        assertEquals(symbolResponse,"ABC","envelope body not changed");

        String nameResponse= returnBody.getFirstChildWithName(
                new QName("http://services.samples/xsd","name")).getText();
        assertEquals(nameResponse,"ABC Company","envelope body not changed");
        String newTagResponse= returnBody.getFirstChildWithName(
                new QName("http://services.samples/xsd","newTag")).getText();
        assertEquals(newTagResponse,"Test Field","envelope body not changed");

        String headerContent=soapHeader.getFirstChildWithName(
                new QName("http://ws.apache.org/ns/synapse","TestHeader")).getText();
        assertEquals(headerContent,"Test","envelope header not changed");

    }

    //create a new service client to get the response with the soap headers.
    private String getResponse() throws IOException {
        cfgCtx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repository.getCanonicalPath(), null);
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
    //create the soap envelope of the request
    private SOAPEnvelope createSOAPEnvelope() {
        fac = OMAbstractFactory.getSOAP11Factory();
        envelope = fac.getDefaultEnvelope();
        envelope.getBody().addChild(createQuoteRequestBody("WSO2"));
        return envelope;
    }


    //create the message context of the request
    private void setMessageContext()
    {
        outMsgCtx = new MessageContext();

        Options opts = outMsgCtx.getOptions();

        opts.setTo(new EndpointReference(getProxyServiceURL("enrichSample2")));
        opts.setAction("urn:getQuote");
    }
    //create soap body of the request
    private OMElement createQuoteRequestBody(String symbol) {
        omNs = fac.createOMNamespace("http://services.samples", "ns");
        method = fac.createOMElement("getQuote", omNs);


            OMElement value1 = fac.createOMElement("request", omNs);
            OMElement value2 = fac.createOMElement("symbol", omNs);
            value2.addChild(fac.createOMText(value1, symbol));
            value1.addChild(value2);
            method.addChild(value1);

        return method;
    }

    @AfterClass(alwaysRun = true)
    public void close(){
        super.cleanup();
    }

}
