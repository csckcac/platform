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
import org.wso2.carbon.admin.service.AdminServiceMassageStoreAdmin;
import org.wso2.carbon.admin.service.AdminServiceStatistic;
import org.wso2.carbon.message.store.stub.MessageInfo;
import org.wso2.carbon.statistics.stub.types.carbon.ServiceStatistics;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClientUtils;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/*
Test to verity fix for scheduled Message Forwarding Processor automatically get deactivated after configuring with
parameter max.deliver.attempts
JIRA -  https://wso2.org/jira/browse/CARBON-10925
 */
public class MessageProcessorTest {

    private static final Log log = LogFactory.getLog(MessageProcessorTest.class);
    private static String SERVICE_EPR;
    private static String PROXY_EPR;
    private AdminServiceMassageStoreAdmin messageStoreAdmin;
    private static String messageStoreName;
    private AdminServiceStatistic serviceStatistics;
    private ServiceStatistics serviceStatBefore;
    private ServiceStatistics serviceStatAfter;
    private static String serviceName;
    private static final int REQUEST_COUNT = 30;

    @BeforeTest(alwaysRun = true)
    public void testInitialize() throws InterruptedException, RemoteException {
        EnvironmentBuilder builder;
        builder = new EnvironmentBuilder().as(1).esb(1);
        ManageEnvironment environment = builder.build();
        serviceName = "SimpleStockQuoteService";
        SERVICE_EPR = environment.getAs().getServiceUrl() + "/" + serviceName;
        PROXY_EPR = environment.getEsb().getServiceUrl(); //send to main sequence
        String AsBackendURL = environment.getAs().getBackEndUrl();
        String AsSessionCookie = environment.getAs().getSessionCookie();
        serviceStatistics = new AdminServiceStatistic(AsBackendURL, AsSessionCookie);
        messageStoreAdmin = new AdminServiceMassageStoreAdmin
                (environment.getEsb().getBackEndUrl(), environment.getEsb().getSessionCookie());
        messageStoreName = "MyStore";
        serviceStatBefore = serviceStatistics.getServiceStatistics(serviceName);
    }

    @Test(groups = "wso2.esb", description = "wait for service to get deployed", priority = 1)
    public void testWaitForServiceDeployment() throws InterruptedException {
        AxisServiceClientUtils.waitForServiceDeployment(SERVICE_EPR);
        Thread.sleep(30000); //force wait
    }


    @Test(groups = "wso2.esb", invocationCount = REQUEST_COUNT,
          dependsOnMethods = "testWaitForServiceDeployment",
          description = "Test to verify processor deactivation", priority = 2)
    public void testPostRequest() throws Exception {
        AxisServiceClientUtils.sendRequestOneWay(createPayLoad().toString(),
                                                 new EndpointReference(PROXY_EPR));

    }

    @Test(groups = "wso2.esb", description = "Test to verify message store",
          dependsOnMethods = "testPostRequest", priority = 3)
    public void testMessageStore() throws RemoteException {
        assertTrue(verifyMessageStore(messageStoreName), "No message store found with the name - " +
                                                         messageStoreName);
        log.info("Message store name verification passed..");
    }

    @Test(groups = "wso2.esb",
          description = "Test to verify message store", dependsOnMethods = "testPostRequest", priority = 4)
    public void testMessageCount() throws RemoteException, InterruptedException {
        assertEquals(Integer.toString(messageStoreAdmin.getMessageCount(messageStoreName)), "30",
                     "Expected message count not available in the store");
        Thread.sleep(60000); //wait for messages to consume
        assertTrue(messageStoreAdmin.getMessageCount(messageStoreName) == 0,
                   "Expected message count not available in the store"); //check for empty message store
        log.info("Message count verification passed..");

    }

    @Test(groups = "wso2.esb",
          description = "Test to verify service stats", dependsOnMethods = "testMessageStore", priority = 5)
    public void testVerifyServiceStats() throws RemoteException {
        serviceStatAfter = serviceStatistics.getServiceStatistics(serviceName);
        int statDifference = serviceStatAfter.getTotalRequestCount() - serviceStatBefore.getTotalRequestCount();
        assertEquals(statDifference, REQUEST_COUNT, "All request haven't been sent to backend service");
        log.info("Service stat verification passed..");

    }

    @Test(groups = "wso2.esb", invocationCount = REQUEST_COUNT,
          description = "Resending requests", dependsOnMethods = "testVerifyServiceStats", priority = 6)
    public void testResendRequests() throws Exception {
        AxisServiceClientUtils.waitForServiceDeployment(SERVICE_EPR);
        AxisServiceClientUtils.sendRequestOneWay(createPayLoad().toString(),
                                                 new EndpointReference(PROXY_EPR));
    }

    @Test(groups = "wso2.esb",
          description = "Test to verify service stats after idle period",
          dependsOnMethods = "testResendRequests", priority = 7)
    public void testVerifyServiceStatsAfterResend() throws RemoteException, InterruptedException {
        Thread.sleep(60000); //wait for messages to consume
        serviceStatAfter = serviceStatistics.getServiceStatistics(serviceName);
        int statDifference = serviceStatAfter.getTotalRequestCount() -
                             (serviceStatBefore.getTotalRequestCount() + REQUEST_COUNT);
        assertEquals(statDifference, REQUEST_COUNT, "All request haven't been sent to backend service");
        log.info("Service stat verification after resending passed..");
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

    private boolean verifyMessageBody(String messageContent, String messageStore)
            throws RemoteException {
        for (MessageInfo info : messageStoreAdmin.getAllMessages(messageStore)) {
            if (info.getSoapXml().contains(messageContent)) {
                return true;
            }
        }
        return false;
    }
}
