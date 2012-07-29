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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.mediation.SynapseConfigAdminClient;
import org.wso2.carbon.automation.core.utils.axis2serverutils.SampleAxis2Server;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.axis2client.AxisServiceClientUtils;
import org.wso2.carbon.automation.utils.esb.StockQuoteClient;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;

public class SendMediatorTest {
    private static final Log log = LogFactory.getLog(SendMediatorTest.class);

    StockQuoteClient axis2Client;
    String backEndUrl = null;
    String nhttpPort = null;
    String hostName = null;
    SampleAxis2Server axis2Server1;
    SampleAxis2Server axis2Server2;
    SynapseConfigAdminClient synapseConfigAdminClient;

    @BeforeTest(alwaysRun = true)
    public void initServers() throws IOException {
        axis2Server1=new SampleAxis2Server("test_axis2_server_9001.xml");
        axis2Server2=new SampleAxis2Server("test_axis2_server_9002.xml");

        axis2Server1.deployService(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
        axis2Server1.deployService(SampleAxis2Server.LB_SERVICE_1);
        axis2Server1.start();

        axis2Server2.deployService(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
        axis2Server2.deployService(SampleAxis2Server.LB_SERVICE_1);
        axis2Server2.start();
    }
    @BeforeClass(alwaysRun = true)
    public void setEnvironment()
            throws LoginAuthenticationExceptionException, IOException, TransformerException,
                   XMLStreamException, SAXException, ServletException,
                   ParserConfigurationException {
        EnvironmentBuilder builder = new EnvironmentBuilder().esb(3);
        ManageEnvironment environment = builder.build();
        backEndUrl = environment.getEsb().getBackEndUrl();
        nhttpPort = environment.getEsb().getProductVariables().getNhttpPort();
        hostName = environment.getEsb().getProductVariables().getHostName();

        synapseConfigAdminClient = new SynapseConfigAdminClient(backEndUrl, "admin", "admin");
        String synapseXmlPath = getClass().getResource(File.separator+"artifacts"+File.separator+"ESB"+File.separator+"synapseconfig"+File.separator+"sendMediatorConfig"+File.separator+"synapse.xml").getPath();
        File synapseXmlFile = new File(synapseXmlPath);
        synapseConfigAdminClient.updateConfiguration(synapseXmlFile);

        axis2Client = new StockQuoteClient();
        //Test weather all the axis2 servers are up and running
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:9000/services/SimpleStockQuoteService",null, "WSO2");
        Assert.assertTrue(response.toString().contains("WSO2 Company"));
        response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:9001/services/SimpleStockQuoteService",null, "WSO2");
        Assert.assertTrue(response.toString().contains("WSO2 Company"));
        response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:9002/services/SimpleStockQuoteService",null, "WSO2");
        Assert.assertTrue(response.toString().contains("WSO2 Company"));

    }

    @AfterTest(groups = "wso2.esb")
    public void close() throws Exception {
        log.info("Tests Are Completed");
        if(axis2Server1.isStarted()){axis2Server1.stop(); }
        if(axis2Server2.isStarted()){axis2Server2.stop(); }
    }

    @Test(groups = "wso2.esb", description = "Test sending request to Address Endpoint")
    public void testSendingAddressEndpoint() throws IOException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://" + hostName + ":" + nhttpPort+"/services/addressEndPoint",
                                                                     null, "WSO2");
        Assert.assertNotNull(response);
        Assert.assertTrue(response.toString().contains("WSO2 Company"));
    }

    @Test(groups = "wso2.esb", description = "Test sending request to Default Endpoint")
    public void testSendingDefaultEndpoint() throws IOException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://" + hostName + ":" + nhttpPort+"/services/defaultEndPoint",
                                                                     "http://localhost:9000/services/SimpleStockQuoteService", "WSO2");
        Assert.assertNotNull(response);
        Assert.assertTrue(response.toString().contains("WSO2 Company"));
    }

    @Test(groups = "wso2.esb", description = "Test sending request to WSDL Endpoint")
    public void testSendingWSDLEndpoint() throws IOException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://" + hostName + ":" + nhttpPort+"/services/wsdlEndPoint",
                                                                     null, "WSO2");
        Assert.assertNotNull(response);
        Assert.assertTrue(response.toString().contains("WSO2 Company"));
    }

    @Test(groups = "wso2.esb", description = "Test sending request to Fail Over Endpoint")
    public void testSendingFailOverEndpoint() throws IOException, InterruptedException {

        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://" + hostName + ":" + nhttpPort+"/services/failoverEndPoint",
                                                                     null, "WSO2");
        Assert.assertTrue(response.toString().contains("WSO2 Company"));

        axis2Server1.stop();
        response = axis2Client.sendSimpleStockQuoteRequest("http://" + hostName + ":" + nhttpPort+"/services/failoverEndPoint",
                                                           null, "WSO2");
        Assert.assertTrue(response.toString().contains("WSO2 Company"));

        axis2Server1.start();
        axis2Server2.stop();

        Thread.sleep(2000);

        int counter=0;
        while(!AxisServiceClientUtils.isServiceAvailable("http://localhost:9001/services/SimpleStockQuoteService")) {
            if(counter>100){
                break;
            }
            counter++;
        }

        if(counter>100){
            throw new AssertionError("Axis2 Server didn't started with in expected time period.") ;
        }
        else{

            response = axis2Client.sendSimpleStockQuoteRequest("http://" + hostName + ":" + nhttpPort+"/services/failoverEndPoint",
                                                               null, "WSO2");
            Assert.assertTrue(response.toString().contains("WSO2 Company"));

        }
        if(!axis2Server1.isStarted()){axis2Server1.start(); }
        if(!axis2Server2.isStarted()){axis2Server2.start(); }
        Thread.sleep(2000);
    }
    



}

