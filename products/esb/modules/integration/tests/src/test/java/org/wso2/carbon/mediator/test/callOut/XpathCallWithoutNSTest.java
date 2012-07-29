/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.mediator.test.callOut;

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


public class XpathCallWithoutNSTest {
    private static final Log log = LogFactory.getLog(XpathCallWithoutNSTest.class);
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
    String proxyServiceName="SplitAggregateProxy";

    @BeforeTest(alwaysRun =true)
    public void setEnvironment() throws LoginAuthenticationExceptionException, RemoteException, IOException, XMLStreamException, ServletException {
        EnvironmentBuilder builder = new EnvironmentBuilder().esb(3);
        ManageEnvironment environment;
        environment = builder.build();
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

    @Test(expectedExceptions =AxisFault.class, groups = {"wso2.esb"}, description = "Sample 750 Call Template Test")
    public void test() throws AxisFault {

        OMElement response= axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURL(), null, "IBM");
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
        esbUtils.loadESBConfigurationFromClasspath("/artifacts/ESB/synapseconfig/config10/synapse.xml",backEndUrl,sessionCookie);

    }

}
