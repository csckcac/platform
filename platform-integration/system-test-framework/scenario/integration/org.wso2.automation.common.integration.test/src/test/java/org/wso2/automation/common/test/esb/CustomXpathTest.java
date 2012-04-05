/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.automation.common.test.esb;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceCarbonServerAdmin;
import org.wso2.carbon.admin.service.AdminServiceLogViewer;
import org.wso2.carbon.logging.view.stub.types.carbon.LogMessage;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.ArtifactDeployerUtil;
import org.wso2.platform.test.core.utils.ClientConnectionUtil;
import org.wso2.platform.test.core.utils.LoginLogoutUtil;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClientUtils;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * Test case to verity the fix - custom xpath extensions not working after configuration updated from source view
 * https://wso2.org/jira/browse/CARBON-11045
 */
public class CustomXpathTest {
    private static final Log log = LogFactory.getLog(CustomXpathTest.class);
    private String PROXY_EPR;
    private AdminServiceCarbonServerAdmin serverAdmin;
    private ManageEnvironment environment;
    private static final long TIMEOUT = 2 * 60 * 1000;
    private AdminServiceLogViewer logViewer;
    private EnvironmentBuilder builder;


    @BeforeTest(alwaysRun = true)
    public void testInitialize() throws InterruptedException, RemoteException {
        builder = new EnvironmentBuilder().esb(1);
        environment = builder.build();
        PROXY_EPR = environment.getEsb().getServiceUrl(); //send to main sequence
        serverAdmin = new AdminServiceCarbonServerAdmin(environment.getEsb().getBackEndUrl());

    }

    @Test(groups = "wso2.esb", description = "gracefully restart the server after applying new " +
                                             "configurations", priority = 1)
    public void testServerRestart()
            throws org.wso2.carbon.server.admin.stub.Exception, RemoteException,
                   InterruptedException {
        log.info("Restarting server after config updates");
        serverAdmin.restartGracefully(environment.getEsb().getSessionCookie());
        Thread.sleep(5000); //This sleep should be there, since we have to give some time for
                            //the server to initiate restart. Otherwise, "waitForPort" call
                            //might happen before server initiate restart.
        int httpsPort = Integer.parseInt(environment.getEsb().getProductVariables().getHttpsPort());
        String hostName = environment.getEsb().getProductVariables().getHostName();
        ClientConnectionUtil.waitForPort(httpsPort, TIMEOUT, true, hostName);
        LoginLogoutUtil loginLogoutUtil = new LoginLogoutUtil(httpsPort, hostName);
        Thread.sleep(15000);
        UserInfo info = UserListCsvReader.getUserInfo(1);
        log.info("Login after server restart");
        loginLogoutUtil.login(info.getUserName(), info.getPassword(),
                              environment.getEsb().getBackEndUrl()); //login to verity server restart
        log.info("Server restart was successful");
    }


    @Test(groups = {"wso2.esb"}, description = "Send request to main sequence which log the custom " +
                                               "xpath", priority = 2, dependsOnMethods = "testServerRestart")
    public void testCustomXpath() throws XMLStreamException, AxisFault {
        AxisServiceClientUtils.sendRequestOneWay(createPayLoad().toString(),
                                                 new EndpointReference(PROXY_EPR));
        log.info("Request send to main sequence");
    }

    @Test(groups = "wso2.esb", description = "verity logs for xpath properties", priority = 3,
          dependsOnMethods = "testCustomXpath")
    public void testLogs() throws RemoteException {
        builder = new EnvironmentBuilder().esb(1);
        environment = builder.build();//generating the environment after server restart
        logViewer = new AdminServiceLogViewer(environment.getEsb().getSessionCookie(),
                                              environment.getEsb().getBackEndUrl());
        LogMessage[] logMessages = logViewer.getLogs("INFO", "LogMediator");
        boolean logStatus = false;
        for (LogMessage logMessage : logMessages) {
            System.out.println(logMessage.getLogMessage());
            if (logMessage.getLogMessage().contains("helloworldVersion = 3.1.0, helloworldName = " +
                                                    "synapse, helloworldDate = 12/12/2010")) {
                logStatus = true;
                log.info("Log entry Found");
            }
        }
        assertTrue(logStatus, "Custom XPath logs are not available");
        log.info("ESB logs contain expected custom Xpath logs");
    }

    @Test(groups = "wso2.esb", description = "verity logs for xpath properties", priority = 4,
          dependsOnMethods = "testLogs")
    public void testUpdateSynapseConfiguration()
            throws IOException, TransformerException, XMLStreamException, SAXException,
                   ServletException, ParserConfigurationException {
        String scenarioConfigDir = ProductConstant.getResourceLocations(ProductConstant.ESB_SERVER_NAME) +
                                   File.separator + "synapseconfig" + File.separator + "config8";
        new ArtifactDeployerUtil().updateSynapseConfig(environment.getEsb().getSessionCookie(),
                                                       environment.getEsb().getBackEndUrl(), scenarioConfigDir);
    }

    @Test(groups = {"wso2.esb"}, description = "Send request again to main sequence after synapse xml update" +
                                               "xpath", priority = 5, dependsOnMethods = "testUpdateSynapseConfiguration")
    public void testSendRequestAgain() throws XMLStreamException, AxisFault {
        AxisServiceClientUtils.sendRequestOneWay(createPayLoad().toString(),
                                                 new EndpointReference(PROXY_EPR));
        log.info("Request send to main sequence");
    }

    @Test(groups = "wso2.esb", description = "verity logs for after synapse config update", priority = 6,
          dependsOnMethods = "testSendRequestAgain")
    public void testLogsAfterConfigUpdate() throws RemoteException {
        builder = new EnvironmentBuilder().esb(1);
        environment = builder.build();//generating the environment after server restart
        LogMessage[] logMessages = logViewer.getLogs("INFO", "LogMediator");
        boolean logStatus = false;
        int counter = 0;
        for (LogMessage logMessage : logMessages) {
            if (logMessage.getLogMessage().contains("helloworldVersion = 3.1.0, helloworldName = " +
                                                    "synapse, helloworldDate = 12/12/2010")) {
                logStatus = true;
                counter ++;
                log.info("Log entry Found");
            }
        }
        assertTrue(counter > 1, "More than one occurrence of the log message must be there");
        assertTrue(logStatus, "Custom XPath logs are not available");
        log.info("ESB logs contain expected custom Xpath logs");
    }

    private static OMElement createPayLoad() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://services.samples", "p");
        OMElement outer = fac.createOMElement("HELLO_WORLD", omNs);
        OMElement name = fac.createOMElement("name", omNs);
        name.setText("ESB");
        OMElement version = fac.createOMElement("version", omNs);
        version.setText("4.0.3");
        OMElement releaseDate = fac.createOMElement("release_date", omNs);
        releaseDate.setText("20/12/2010");
        outer.addChild(name);
        outer.addChild(version);
        outer.addChild(releaseDate);
        log.debug("Payload :" + outer);
        return outer;
    }
}
