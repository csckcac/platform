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
package org.wso2.carbon.mediator.test.property;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.automation.api.clients.application.mgt.CarbonAppUploaderClient;
import org.wso2.carbon.automation.api.clients.logging.LogViewerClient;
import org.wso2.carbon.automation.api.clients.logging.LoggingAdminClient;
import org.wso2.carbon.automation.api.clients.soap.tracer.TracerAdminClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.axis2serverutils.SampleAxis2Server;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.esb.ESBTestCaseUtils;
import org.wso2.carbon.automation.utils.esb.StockQuoteClient;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.activation.DataHandler;



public class PropertyMediatorTest {

    private static final Log log = LogFactory.getLog(PropertyMediatorTest.class);
    protected EnvironmentVariables esbServer;
    StockQuoteClient axis2Client;
    LogViewerClient logViewer;
    LoggingAdminClient logAdmin;

    String sessionCookie = null;
    String backEndUrl = null;
    SampleAxis2Server axis2Server;


    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception, IOException {
        EnvironmentBuilder builder = new EnvironmentBuilder().esb(3);
        ManageEnvironment environment = builder.build();
        backEndUrl = environment.getEsb().getBackEndUrl();
        sessionCookie = environment.getEsb().getSessionCookie();
        esbServer=builder.build().getEsb();
        axis2Client = new StockQuoteClient();
        logAdmin = new LoggingAdminClient(backEndUrl,sessionCookie);
        logViewer=new LogViewerClient(backEndUrl,sessionCookie);

        ESBTestCaseUtils esbU=new ESBTestCaseUtils();
        esbU.loadESBConfigurationFromClasspath("/artifacts/ESB/synapseconfig/propertMediatorConfig/synapse.xml",backEndUrl,sessionCookie);
    }

    @Test(groups = "wso2.esb", description = "Set a new property value (static text value) and retrieve it using get-property(property-name) Xpath function  (in default scope)")
    public void testStaticValue() throws Exception, InterruptedException {
        TracerAdminClient tac=new TracerAdminClient(backEndUrl,"admin","admin");
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/static", null
                , "MSFT");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("MSFT Company"));

    }

    @Test(groups = "wso2.esb", description = "Set a new property - Select \"Set Action As\" expression and give an Xpath expression (in default scope)")
    public void testXpath() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/static", null, "MSFT");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("MSFT Company"));

    }

    @Test(groups = "wso2.esb", description = "Set a new property - Select \"Set Action As\" expression and give an Xpath expression - use name spaces (in default scope)")
    public void testXpathWithNameSpace() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/static", null, "WSO2");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("WSO2 Company"));

    }


    @Test(groups = "wso2.esb", description = "Set action as \"experssion\" and type STRING (default scope)")
    public void testStringXpath() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/stringXpathProperty", null, "WSO2");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("WSO2 Company"));

    }


    @Test(groups = "wso2.esb", description = "Set action as \"value\" and type STRING (default scope)")
    public void testStringValue() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/stringValProperty", null, "WSO2");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("WSO2 Company"));


    }

    @Test(groups = "wso2.esb", description = "Set action as \"expression\" and type INTEGER (default scope)")
    public void testIntegerXpath() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/integerXpathProperty", null, "88888888");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("88888888 Company"));

    }

    @Test(groups = "wso2.esb", description = "Set action as \"value\" and type INTEGER (default scope)")
    public void testIntegerVal() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/integerValProperty", null, "88888888");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("88888888 Company"));

    }

    @Test(groups = "wso2.esb", description = "Set action as \"expression\" and type Boolean (default scope)")
    public void testBooleanXpath() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/booleanXpath", null, "TRUE");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("TRUE Company"));

    }

    @Test(groups = "wso2.esb", description = "Set action as \"value\" and type BOOLEAN (default scope)")
    public void testBooleanVal() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/booleanVal", null, "FALSE");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("FALSE Company"));

    }

    @Test(groups = "wso2.esb", description = "Set action as \"expression\" and type Short (default scope)")
    public void testShortXpath() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/shortXpath", null, "88");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("88 Company"));

    }

    @Test(groups = "wso2.esb", description = "Set action as \"value\" and type Short (default scope)")
    public void testShortVal() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/shortVal", null, "88");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("88 Company"));


    }

    @Test(groups = "wso2.esb", description = "Set action as \"expression\" and type Long (default scope)")
    public void testLongXpath() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/longXpath", null, "8888888888");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("8888888888 Company"));


    }

    @Test(groups = "wso2.esb", description = "Set action as \"value\" and type Long (default scope)")
    public void testLongVal() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/longVal", null, "8888888888");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("8888888888 Company"));

    }

    @Test(groups = "wso2.esb", description = "Set action as \"expression\" and type Double (default scope)")
    public void testDoubleXpath() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/doubleXpath", null, "8888888888.8888888888888");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("8888888888.8888888888888 Company"));

    }

    @Test(groups = "wso2.esb", description = "Set action as \"value\" and type Double (default scope)")
    public void testDoubletVal() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/doubleVal", null, "8888888888.8888888888888");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("8888888888.8888888888888 Company"));

    }

    @Test(groups = "wso2.esb", description = "Set action as \"expression\" and type Float (default scope)")
    public void testFloatXpath() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/floatXpath", null, "8888.8888");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("8888.8888 Company"));
    }

    @Test(groups = "wso2.esb", description = "Set action as \"value\" and type Float (default scope)")
    public void testFloatVal() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/floatVal", null, "8888.8888");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("8888.8888 Company"));


    }
    @Test(groups = "wso2.esb", description = "SOAP header specific properties")
    public void testSOAPHeaders() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/Axis2ProxyService", null, "WSO2");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("WSO2 Company"));


    }

    @Test(groups = "wso2.esb", description = "Specify invalid Xpath function when setting a property" ,expectedExceptions = org.apache.axis2.AxisFault.class)
    public void testInvalidXpath() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/negative", null, "MSFT");
    }

    @Test(groups = "wso2.esb", description = "Set the property in one scope and read it from another scope")
    public void testInvalidScope() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/negative", null, "WSO2");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("WSO2 Company"));


    }

    @Test(groups = "wso2.esb", description = "Synapse Xpath variables")
    public void testSynapseXpathVariables() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/SynapseXpathvariables", null, "WSO2");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("WSO2 Company"));


    }

    @Test(groups = "wso2.esb", description = "Synapse Properties")
    public void testSynapseProperties() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/SynapseProperties", null, "WSO2");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("WSO2 Company"));


    }

    @Test(groups = "wso2.esb", description = "HTTP Properties")
    public void testHttpProperties() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/HttpProperties", null, "WSO2");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("WSO2 Company"));


    }

    @Test(groups = "wso2.esb", description = "Generic Properties")
    public void testGenericProperties() throws Exception, InterruptedException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/services/GenericProperties", null, "WSO2");
        Assert.assertTrue(response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd","name")).toString().contains("WSO2 Company"));


    }

    @AfterClass(groups = "wso2.esb")
    public void close() throws Exception {
        log.info("Tests Are Completed");
    }

    protected void loadSampleESBConfiguration(int sampleNo) throws Exception {
        ESBTestCaseUtils esbUtils = new ESBTestCaseUtils();
        esbUtils.loadSampleESBConfiguration(sampleNo, esbServer.getBackEndUrl(), esbServer.getSessionCookie());
    }
}


