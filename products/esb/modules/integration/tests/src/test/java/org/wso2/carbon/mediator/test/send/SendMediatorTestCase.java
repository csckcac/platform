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

package org.wso2.carbon.mediator.test.send;

import org.apache.axiom.om.OMElement;
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
import org.wso2.carbon.automation.api.clients.mediation.SynapseConfigAdminClient;
import org.wso2.carbon.automation.core.utils.axis2serverutils.SampleAxis2Server;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.esb.StockQuoteClient;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;

public class SendMediatorTestCase {

    StockQuoteClient axis2Client;
    LogViewerClient logViewer;
    LoggingAdminClient logAdmin;

    String sessionCookie = null;
    private static final Log log = LogFactory.getLog(SendMediatorTestCase.class);
    String backEndUrl = null;
    String serviceUrl = null;
    String nhttpPort = null;
    String hostName = null;
    SampleAxis2Server axis2Server;
    AuthenticatorClient adminServiceAuthentication;
    SynapseConfigAdminClient synapseConfigAdminClient;


    @BeforeTest(alwaysRun = true)
    public void setEnvironment()
            throws LoginAuthenticationExceptionException, IOException, TransformerException,
                   XMLStreamException, SAXException, ServletException,
                   ParserConfigurationException {
        EnvironmentBuilder builder = new EnvironmentBuilder().esb(3);
        ManageEnvironment environment = builder.build();
        backEndUrl = environment.getEsb().getBackEndUrl();
        serviceUrl = environment.getEsb().getServiceUrl();
        sessionCookie = environment.getEsb().getSessionCookie();
        nhttpPort = environment.getEsb().getProductVariables().getNhttpPort();
        hostName = environment.getEsb().getProductVariables().getHostName();
        adminServiceAuthentication = environment.getEsb().getAdminServiceAuthentication();
        axis2Client = new StockQuoteClient();
        synapseConfigAdminClient = new SynapseConfigAdminClient(backEndUrl, "admin", "admin");

        String synapseXmlPath = getClass().getResource(File.separator+"artifacts"+File.separator+"ESB"+File.separator+"synapseconfig"+File.separator+"send_mediator"+File.separator+"synapse.xm").getPath();

        File synapseXmlFile = new File(synapseXmlPath);
        synapseConfigAdminClient.updateConfiguration(synapseXmlFile);
        System.out.println("sds");

    }

    @Test(groups = "wso2.esb", description = "Test sending request to defined endpoint")
    public void testSendingToDefinedEndpoint() throws IOException {

        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://" + hostName + ":" + nhttpPort,
                                                                     null, "WSO2");
        Assert.assertTrue(response.toString().contains("WSO2"));
        ;
    }

    @AfterTest(groups = "wso2.esb")
    public void close() throws Exception {

    }

}

