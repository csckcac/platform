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
import org.wso2.carbon.admin.service.AdminServiceLogViewer;
import org.wso2.carbon.admin.service.AdminServiceService;
import org.wso2.carbon.logging.view.stub.types.carbon.LogMessage;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;
import org.wso2.automation.common.test.esb.util.ConfigUploader;

import javax.servlet.ServletException;
import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;

public class MediatorTest {
    public ManageEnvironment environmentObj;
    private String NHTTP_PORT;
    private static final Log log = LogFactory.getLog(MediatorTest.class);


    @BeforeTest
    private void setEnv() throws InterruptedException {
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
        NHTTP_PORT = "http://" + environmentObj.getEsb().getProductVariables().getHostName() + ":" + environmentObj.getEsb().getProductVariables().getNhttpPort();

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

    @Test(alwaysRun = true)
    public void testMediatorInOut() throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorInOut.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorAggregate()
            throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorAggregate.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
    }

    @Test(alwaysRun = true)
    public void testMediatorCache_WithCacheID()
            throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorCache-WithCacheID.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorCache_WithoutCacheID()
            throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorCache-WithoutCacheID.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorCallOut() throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorCallout.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorClone() throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorClone.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorDrop_InSeq()
            throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorDrop-InSeq.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorDrop_OutSeq()
            throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorDrop-OutSeq.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorEnrich() throws RemoteException, XMLStreamException, ServletException {
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

    @Test(alwaysRun = true)
    public void testMediatorHeader() throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorHeader.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorIterate() throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorIterate.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorRouter_anon_seq_and_epr()
            throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorRouter-anon-seq-and-epr.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorRouter_Break_rout()
            throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorRouter-break-router.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorRouter_Continue_After()
            throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorRouter-continue-after.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorRouter_Expression()
            throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorRouter-expression.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorRouter_Match()
            throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorRouter-match.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorRouter_Ref_Target()
            throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorRouter-ref-target.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorRule_Simple_Msg_Trans()
            throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorRule-simple-msg-trans.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorRule_Drools()
            throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorRule_drools.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorRule_Simple()
            throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorRule_simple.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorRule_Simple_Reg()
            throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorRule_simple_reg.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorRule_Switch()
            throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorRule_switch.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorSend() throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorSend.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorSend_EPR_From_Registry()
            throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorSend_EPR_From_Registry.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorSend_Non_EPR()
            throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorSend_NonEpr.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorSend_Proxy()
            throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorSend_Proxy.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorSend_Sequence()
            throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorSequence.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));

    }

    @Test(alwaysRun = true)
    public void testMediatorValidate()
            throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorValidate.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorSwitch() throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorSwitch.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("IBM"));
    }

    @Test(alwaysRun = true)
    public void testMediatorScript() throws RemoteException, XMLStreamException, ServletException {
        new ConfigUploader(environmentObj, "MediatorScript.xml");
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        OMElement omElement = axisServiceClient.sendReceive(createPayLoad("IBM"), NHTTP_PORT, "getQuote");
        log.info("Response : " + omElement.toString());
        Assert.assertTrue("Expected result not found while invoking service.", omElement.toString().contains("MSFT"));
    }

    @Test(alwaysRun = true)
    public void testMediatorLog_Simple()
            throws RemoteException, XMLStreamException, ServletException {
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

    @Test(alwaysRun = true)
    public void testMediatorLog_Header()
            throws RemoteException, XMLStreamException, ServletException {
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

    @Test(alwaysRun = true)
    public void testMediatorLog_Full()
            throws RemoteException, XMLStreamException, ServletException {
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

    @Test(alwaysRun = true)
    public void testMediatorLog_Custom()
            throws RemoteException, XMLStreamException, ServletException {
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
