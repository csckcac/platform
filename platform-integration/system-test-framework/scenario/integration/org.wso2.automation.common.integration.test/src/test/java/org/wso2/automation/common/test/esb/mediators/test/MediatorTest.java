/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.automation.common.test.esb.mediators.test;

import junit.framework.Assert;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.automation.common.test.esb.util.ConfigUploader;
import org.wso2.carbon.admin.service.AdminServiceLogViewer;
import org.wso2.carbon.admin.service.AdminServiceService;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.logging.view.stub.types.carbon.LogMessage;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import javax.servlet.ServletException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.rmi.RemoteException;

public class MediatorTest {
    public ManageEnvironment environmentObj;
    private String NHTTP_PORT;
    private static final Log log = LogFactory.getLog(MediatorTest.class);


    @BeforeTest
    private void setEnv()
            throws InterruptedException, RemoteException, LoginAuthenticationExceptionException {
        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder().esb(1).as(1);
        environmentObj = environmentBuilder.build();
        AdminServiceService adminServiceService =
                new AdminServiceService(environmentObj.getAs().getBackEndUrl());
        int loopCount = 0;
        while (true) {
            String serviceName = adminServiceService.getServiceGroup
                    (environmentObj.getAs().getSessionCookie(), "SimpleStockQuoteService");
            if (serviceName != null) {
                break;
            } else if (loopCount >= 15) {
                Assert.fail("Unable to deploy SimpleStockQuoteService in App server instance");
                break;
            }
            Thread.sleep(2000);
            loopCount++;
        }
        if (environmentBuilder.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {
            NHTTP_PORT = environmentObj.getEsb().getServiceUrl();
        } else {
            NHTTP_PORT = "http://" + environmentObj.getEsb().getProductVariables().getHostName() + ":"
                    + environmentObj.getEsb().getProductVariables().getNhttpPort();
        }

    }


    public static OMElement createPayLoad(String symbol) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://services.samples", "ns");
        OMElement method = fac.createOMElement("getQuote", omNs);
        OMElement value1 = fac.createOMElement("request", omNs);
        OMElement value2 = fac.createOMElement("symbol", omNs);

        value2.addChild(fac.createOMText(value1, symbol));
        value1.addChild(value2);
        method.addChild(value1);

        return method;
    }

    @Test(groups = {"wso2.esb"}, description = "Testing Mediator IN and OUT")
    public void testMediatorInOut() throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorInOut.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(groups = {"wso2.esb"}, description = "Testing Mediator Aggregate")
    public void testMediatorAggregate()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorAggregate.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
    }

    @Test(groups = {"wso2.esb"}, description = "Testing cache mediator with cache ID")
    public void testMediatorCache_WithCacheID()
            throws IOException, XMLStreamException, ServletException, InterruptedException {

        new ConfigUploader(environmentObj, "MediatorCache-WithCacheID.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(groups = {"wso2.esb"}, description = "Testing cache mediator without cache ID")
    public void testMediatorCache_WithoutCacheID()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorCache-WithoutCacheID.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    /*   @Test(alwaysRun = true)
    public void testMediatorCallOut() throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorCallout.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }*/

    @Test(groups = {"wso2.esb"}, description = "Testing clone mediator")
    public void testMediatorClone() throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorClone.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(groups = {"wso2.esb"}, description = "Testing drop mediator within InSequence")
    public void testMediatorDrop_InSeq()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorDrop-InSeq.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(groups = {"wso2.esb"}, description = "Testing drop mediator with in OutSequence")
    public void testMediatorDrop_OutSeq()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorDrop-OutSeq.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(groups = {"wso2.esb"}, description = "Testing enrich mediator")
    public void testMediatorEnrich() throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorEnrich.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

// ToDo Issue need to be fix

/* @Test(alwaysRun = true)
    public void testMediatorFilter_RegX()throws RemoteException, XMLStreamException, ServletException {
            new ConfigUploader(environmentObj, "MediatorFilter-regx.xml");
            AxisServiceClient axisServiceClient = new AxisServiceClient();
            OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
            log.info("Response : " + omElement.toString());
            Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }*/


    @Test(groups = {"wso2.esb"}, description = "Testing header mediator")
    public void testMediatorHeader() throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorHeader.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(groups = {"wso2.esb"}, description = "Testing Iterate mediator")
    public void testMediatorIterate() throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorIterate.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(groups = {"wso2.esb"}, description = "Testing Router mediator")
    public void testMediatorRouter_anon_seq_and_epr()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorRouter-anon-seq-and-epr.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(groups = {"wso2.esb"}, description = "Testing router mediator")
    public void testMediatorRouter_Break_rout()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorRouter-break-router.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(groups = {"wso2.esb"}, description = "Testing router mediator")
    public void testMediatorRouter_Continue_After()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorRouter-continue-after.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(groups = {"wso2.esb"}, description = "Testing router mediator")
    public void testMediatorRouter_Expression()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorRouter-expression.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(groups = {"wso2.esb"}, description = "Testing router mediator")
    public void testMediatorRouter_Match()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorRouter-match.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(groups = {"wso2.esb"}, description = "Testing router mediator")
    public void testMediatorRouter_Ref_Target()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorRouter-ref-target.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(enabled = false, groups = {"wso2.esb"}, description = "Testing rule mediator")
    public void testMediatorRule_Simple_Msg_Trans()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorRule-simple-msg-trans.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(enabled = false, groups = {"wso2.esb"}, description = "Testing rule mediator")
    public void testMediatorRule_Drools()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorRule_drools.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(enabled = false, groups = {"wso2.esb"}, description = "Testing rule mediator")
    public void testMediatorRule_Simple()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorRule_simple.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(enabled = false, groups = {"wso2.esb"}, description = "Testing rule mediator")
    public void testMediatorRule_Simple_Reg()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorRule_simple_reg.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(enabled = false, groups = {"wso2.esb"}, description = "Testing rule mediator")
    public void testMediatorRule_Switch()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorRule_switch.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(groups = {"wso2.esb"}, description = "Testing send mediator")
    public void testMediatorSend() throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorSend.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(groups = {"wso2.esb"}, description = "Testing send mediator")
    public void testMediatorSend_EPR_From_Registry()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorSend_EPR_From_Registry.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(groups = {"wso2.esb"}, description = "Testing send mediator")
    public void testMediatorSend_Non_EPR()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorSend_NonEpr.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(groups = {"wso2.esb"}, description = "Testing send mediator")
    public void testMediatorSend_Proxy()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorSend_Proxy.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(groups = {"wso2.esb"}, description = "Testing send mediator")
    public void testMediatorSend_Sequence()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorSequence.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));

    }

    @Test(groups = {"wso2.esb"}, description = "Testing validate mediator")
    public void testMediatorValidate()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorValidate.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(groups = {"wso2.esb"}, description = "Testing switch mediator")
    public void testMediatorSwitch() throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorSwitch.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(groups = {"wso2.esb"}, description = "Testing script mediator")
    public void testMediatorScript() throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorScript.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("MSFT"));
    }

    @Test(groups = {"wso2.esb"}, description = "Testing log mediator")
    public void testMediatorLog_Simple()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorLog-simple.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("WSO2"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue(omElement.toString().contains("WSO2"));
        LogMessage[] logMessages =
                new AdminServiceLogViewer(environmentObj.getEsb().getSessionCookie(),
                        environmentObj.getEsb().getBackEndUrl()).
                        getLogs("INFO", "LogMediator");

        Assert.assertTrue(logMessages != null && logMessages.length > 0 && logMessages[0] != null);

        boolean requestLogOk = false;
        boolean responseLogOk = false;
        for (LogMessage l : logMessages) {

            String message = l.getLogMessage();

            if (message.contains("inComing = ***Incoming Message***") &&
                    message.contains("inExpression = Echo String - urn:getQuote") &&
                    !message.contains("Envelope") && !message.contains("WSO2")) {
                requestLogOk = true;
            }

            if (message.contains("outgoing = ***Outgoing Message***") &&
                    !message.contains("Envelope") && !message.contains("WSO2 Company")) {
                responseLogOk = true;
            }
        }
        Assert.assertTrue("Log mediator error in message request", requestLogOk);
        Assert.assertTrue("Log mediator error in message response", responseLogOk);
    }

    @Test(groups = {"wso2.esb"}, description = "Testing log mediator")
    public void testMediatorLog_Header()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorLog-header.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("WSO2"), NHTTP_PORT, "getQuote", "TestHeader", "http://test.wso2.org", "TestHeaderValue");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue(omElement.toString().contains("WSO2"));
        LogMessage[] logMessages = new AdminServiceLogViewer(environmentObj.getEsb().getSessionCookie(), environmentObj.getEsb().getBackEndUrl()).getLogs("INFO", "LogMediator");
        Assert.assertTrue(logMessages != null && logMessages.length > 0 && logMessages[0] != null);

        boolean requestLogOk = false;
        boolean responseLogOk = false;
        for (LogMessage l : logMessages) {

            String message = l.getLogMessage();

            if (message.contains("inComing = ***Incoming Message***") &&
                    message.contains("inExpression = Echo String - urn:getQuote") &&
                    !message.contains("Envelope") && !message.contains("WSO2") &&
                    message.contains("TestHeader") && message.contains("TestHeaderValue")) {
                requestLogOk = true;
            }

            if (message.contains("outgoing = ***Outgoing Message***") &&
                    message.contains("Envelope") && message.contains("WSO2 Company")) {
                responseLogOk = true;
            }
        }
        Assert.assertTrue("Log mediator error in message request", requestLogOk);
        Assert.assertTrue("Log mediator error in message response", responseLogOk);
    }

    @Test(groups = {"wso2.esb"}, description = "Testing log mediator")
    public void testMediatorLog_Full()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorLog-full.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("WSO2"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue(omElement.toString().contains("WSO2"));
        LogMessage[] logMessages = new AdminServiceLogViewer(environmentObj.getEsb().getSessionCookie(), environmentObj.getEsb().getBackEndUrl()).getLogs("INFO", "LogMediator");
        Assert.assertTrue(logMessages != null && logMessages.length > 0 && logMessages[0] != null);

        boolean requestLogOk = false;
        boolean responseLogOk = false;
        for (LogMessage l : logMessages) {

            String message = l.getLogMessage();

            if (message.contains("inComing = ***Incoming Message***") &&
                    message.contains("inExpression = Echo String - urn:getQuote") &&
                    message.contains("Envelope") && message.contains("WSO2")) {
                requestLogOk = true;
            }

            if (message.contains("outgoing = ***Outgoing Message***") &&
                    message.contains("Envelope") && message.contains("WSO2 Company")) {
                responseLogOk = true;
            }
        }
        Assert.assertTrue("Log mediator error in message request", requestLogOk);
        Assert.assertTrue("Log mediator error in message response", responseLogOk);
    }

    @Test(groups = {"wso2.esb"}, description = "Testing log mediator")
    public void testMediatorLog_Custom()
            throws IOException, XMLStreamException, ServletException, InterruptedException {
        new ConfigUploader(environmentObj, "MediatorLog-custom.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("WSO2"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue(omElement.toString().contains("WSO2"));
        LogMessage[] logMessages = new AdminServiceLogViewer(environmentObj.getEsb().getSessionCookie(), environmentObj.getEsb().getBackEndUrl()).getLogs("INFO", "LogMediator");
        Assert.assertTrue(logMessages != null && logMessages.length > 0 && logMessages[0] != null);

        boolean requestLogOk = false;
        boolean responseLogOk = false;
        for (LogMessage l : logMessages) {

            String message = l.getLogMessage();

            if (message.contains("inComing = ***Incoming Message***") &&
                    message.contains("inExpression = Echo String - urn:getQuote") &&
                    !message.contains("Envelope") && !message.contains("WSO2") &&
                    !message.contains("Direction") && !message.contains("SOAPAction")) {
                requestLogOk = true;
            }

            if (message.contains("outgoing = ***Outgoing Message***") &&
                    !message.contains("Envelope") && !message.contains("WSO2 Company") &&
                    !message.contains("Direction") && !message.contains("SOAPAction")) {
                responseLogOk = true;
            }
        }
        Assert.assertTrue("Log mediator error in message request", requestLogOk);
        Assert.assertTrue("Log mediator error in message response", responseLogOk);
    }


}
