package org.wso2.carbon.mediator.test.call;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.authenticators.AuthenticatorClient;
import org.wso2.carbon.automation.api.clients.logging.LogViewerClient;
import org.wso2.carbon.automation.api.clients.logging.LoggingAdminClient;
import org.wso2.carbon.automation.core.utils.axis2serverutils.SampleAxis2Server;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.esb.ESBTestCaseUtils;
import org.wso2.carbon.automation.utils.esb.StockQuoteClient;

import javax.servlet.ServletException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.rmi.RemoteException;

/**
 * Created with IntelliJ IDEA.
 * User: hasinthaindrajee
 * Date: 7/18/12
 * Time: 12:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class FuncCallWithoutParamsTest {
    private static final Log log = LogFactory.getLog(FuncCallWithoutParamsTest.class);
    StockQuoteClient axis2Client;
    LogViewerClient logViewer;
    LoggingAdminClient logAdmin;
    String sessionCookie = null;

    String backEndUrl = null;
    String serviceUrl = null;
    String nhttpPort = null;
    String hostName=null;
    SampleAxis2Server axis2Server;
    AuthenticatorClient adminServiceAuthentication;
    String proxyServiceName="StockQuoteProxy";

    @BeforeTest(alwaysRun =true)
    public void setEnvironment() throws LoginAuthenticationExceptionException, RemoteException, IOException, XMLStreamException, ServletException {
        EnvironmentBuilder builder = new EnvironmentBuilder().esb(3);
        ManageEnvironment environment = builder.build();
        backEndUrl = environment.getEsb().getBackEndUrl();
        serviceUrl = environment.getEsb().getServiceUrl();
        sessionCookie = environment.getEsb().getSessionCookie();
        nhttpPort = environment.getEsb().getProductVariables().getNhttpPort();
        hostName=environment.getEsb().getProductVariables().getHostName();
        adminServiceAuthentication = environment.getEsb().getAdminServiceAuthentication();
        axis2Server = new SampleAxis2Server();
        //axis2Server.start();

        axis2Client = new StockQuoteClient();
        logAdmin = new LoggingAdminClient(backEndUrl,sessionCookie);
        logViewer=new LogViewerClient(backEndUrl,sessionCookie);
       loadSampleConfiguration();


    }

    @Test(groups = {"wso2.esb"}, description = "Sample 750 Call Template Test")
    public void test() throws AxisFault {

        OMElement response= axis2Client.sendCustomQuoteRequest(getProxyServiceURL(), null, "IBM");
        Assert.assertNotNull(response,"Response message is null");
        Assert.assertTrue(response.toString().contains("CheckPriceResponse"));
        Assert.assertTrue(response.toString().contains("Price"));
        Assert.assertTrue(response.toString().contains("Code"));

    }

    @AfterTest
    public void closeTestArtifacts(){
        //axis2Server.stop();
    }

    protected String getProxyServiceURL() {
        return "http://" + hostName + ":" +
                nhttpPort + "/services/" + proxyServiceName;
    }

    public void loadSampleConfiguration() throws XMLStreamException, ServletException, RemoteException {
        ESBTestCaseUtils  esbUtils = new ESBTestCaseUtils();
        esbUtils.loadESBConfigurationFromClasspath("/artifacts/ESB/synapseconfig/config9/synapse.xml",backEndUrl,sessionCookie);

    }

}




