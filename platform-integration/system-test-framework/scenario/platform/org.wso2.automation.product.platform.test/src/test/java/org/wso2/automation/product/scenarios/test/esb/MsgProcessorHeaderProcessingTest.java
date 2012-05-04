package org.wso2.automation.product.scenarios.test.esb;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceLogViewer;
import org.wso2.carbon.admin.service.AdminServiceMassageStoreAdmin;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.logging.view.stub.types.carbon.LogMessage;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClientUtils;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/*
Scheduled forwarding message processor does forward SOAP headers
Test for the fix - https://wso2.org/jira/browse/CARBON-10977
 */
public class MsgProcessorHeaderProcessingTest {

    private static final Log log = LogFactory.getLog(MessageProcessorTest.class);
    private static String SERVICE_EPR;
    private static String PROXY_EPR;
    private static final int REQUEST_COUNT = 30;
    private static String messageStoreName;
    private AdminServiceMassageStoreAdmin messageStoreAdmin;
    private AdminServiceLogViewer logViewer;

    @BeforeTest(alwaysRun = true)
    public void testInitialize()
            throws InterruptedException, RemoteException, LoginAuthenticationExceptionException {
        EnvironmentBuilder builder;
        builder = new EnvironmentBuilder().as(1).esb(1);
        ManageEnvironment environment = builder.build();
        String serviceName = "SimpleStockQuoteService";
        SERVICE_EPR = environment.getAs().getServiceUrl() + "/" + serviceName;
        PROXY_EPR = environment.getEsb().getServiceUrl(); //send to main sequence
        messageStoreAdmin = new AdminServiceMassageStoreAdmin
                (environment.getEsb().getBackEndUrl(), environment.getEsb().getSessionCookie());
        logViewer = new AdminServiceLogViewer(environment.getEsb().getSessionCookie(),
                                              environment.getEsb().getBackEndUrl());
        messageStoreName = "MyStore";
    }

    @Test(groups = "wso2.esb", description = "wait for service to get deployed", priority = 1)
    public void testWaitForServiceDeployment() throws InterruptedException {
        AxisServiceClientUtils.waitForServiceDeployment(SERVICE_EPR);
        Thread.sleep(30000); //force wait
    }

    @Test(groups = "wso2.esb", description = "Test to verify message store", dependsOnMethods =
            "testPostRequest", priority = 2)
    public void testMessageStore() throws RemoteException {
        assertTrue(verifyMessageStore(messageStoreName), "No message store found with the name - " +
                                                         messageStoreName);
        log.info("Message store name verification passed..");
    }

    @Test(groups = "wso2.esb", invocationCount = REQUEST_COUNT,
          dependsOnMethods = "testWaitForServiceDeployment",
          description = "Send messages to esb main sequence", priority = 2)
    public void testPostRequest() throws Exception {
        AxisServiceClientUtils.sendRequestOneWay(createPayLoad().toString(),
                                                 new EndpointReference(PROXY_EPR));
    }

    @Test(groups = "wso2.esb", description = "verity logs for header properties", priority = 3)
    public void testLogs() throws RemoteException {
        LogMessage[] logMessages = logViewer.getLogs("INFO", "LogMediator");
        boolean logStatus = false;
        for (LogMessage logMessage : logMessages) {
            System.out.println(logMessage.getLogMessage());
            if (logMessage.getLogMessage().contains("testHeaderAutomation : testHeaderValue")) {
                logStatus = true;
                log.info("Message Found");
            }
        }
        assertTrue(logStatus, "Message header logs not available");
        log.info("Message consumed by message processor contains expected header");
    }

    private static OMElement createPayLoad() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://services.samples", "ser");
        OMNamespace omNSxsd = fac.createOMNamespace("http://services.samples/xsd", "xsd");
        OMElement method = fac.createOMElement("getQuote", omNs);
        OMElement request = fac.createOMElement("request", omNs);
        OMElement symbol = fac.createOMElement("symbol", omNSxsd);
        symbol.setText("IBM");
        request.addChild(symbol);
        method.addChild(request);
        log.info("Created payload is :" + method);
        return method;
    }

    private boolean verifyMessageStore(String messageStoreName) throws RemoteException {
        for (String storeName : messageStoreAdmin.getMessageStores()) {
            if (storeName.equals(messageStoreName)) {
                return true;
            }
        }
        return false;

    }
}
