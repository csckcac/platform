package org.wso2.carbon.mediator.test.callOut;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.logging.LogViewerClient;
import org.wso2.carbon.automation.api.clients.logging.LoggingAdminClient;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.automation.utils.esb.ESBTestCaseUtils;
import org.wso2.carbon.automation.utils.esb.StockQuoteClient;
import org.wso2.carbon.mediator.test.log.LogMediatorLevelTest;

import java.io.IOException;

import static org.testng.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: amila
 * Date: 7/18/12
 * Time: 10:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class SourceXpathTargetXpath {



    StockQuoteClient axis2Client;
    LogViewerClient logViewer;
    LoggingAdminClient logAdmin;

    String sessionCookie = null;
    private static final Log log = LogFactory.getLog(LogMediatorLevelTest.class);
    String backEndUrl = null;

    private OMElement response;

    private EnvironmentVariables esbServer;
    UserInfo userInfo;


    @BeforeTest(alwaysRun = true)
    public void setEnvironment() throws Exception, IOException {
        axis2Client = new StockQuoteClient();
        userInfo = UserListCsvReader.getUserInfo(1);
        EnvironmentBuilder builder = new EnvironmentBuilder().esb(1);
        esbServer = builder.build().getEsb();
        ESBTestCaseUtils caseUtils=new ESBTestCaseUtils();
        backEndUrl = esbServer.getBackEndUrl();
        sessionCookie = esbServer.getSessionCookie();
        caseUtils.loadESBConfigurationFromClasspath("/artifacts/ESB/mediatorconfig/callout/synapse_sample_430.xml", backEndUrl, sessionCookie);
    }


    @Test (alwaysRun = true)

        public void test_sourceXpathTargetXpath() throws AxisFault {


      response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/","","IBM");    // send the simplestockquote request. service url is set at the synapse


        boolean ResponseContainsIBM = response.getFirstElement().toString().contains("IBM");      //checks whether the  response contains IBM

        assertTrue(ResponseContainsIBM);


       }

    @AfterClass
    public void cleanup() {
        axis2Client.destroy();
        userInfo = null;
        esbServer = null;
    }






    protected void loadSampleESBConfiguration(int sampleNo) throws Exception {
        ESBTestCaseUtils esbUtils = new ESBTestCaseUtils();
        esbUtils.loadSampleESBConfiguration(sampleNo, esbServer.getBackEndUrl(), esbServer.getSessionCookie());
    }





}
