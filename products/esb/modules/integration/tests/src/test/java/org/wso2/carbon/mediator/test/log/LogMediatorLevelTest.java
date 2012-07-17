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

package org.wso2.carbon.mediator.test.log;

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
import org.wso2.carbon.automation.core.utils.axis2serverutils.SampleAxis2Server;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.esb.StockQuoteClient;
import org.wso2.carbon.logging.view.stub.types.axis2.GetLogs;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;

import java.io.IOException;

public class LogMediatorLevelTest {

    StockQuoteClient axis2Client;
    LogViewerClient logViewer;
    LoggingAdminClient logAdmin;

    String sessionCookie = null;
    private static final Log log = LogFactory.getLog(LogMediatorLevelTest.class);
    String backEndUrl = null;
    String serviceUrl = null;
    String nhttpPort = null;
    String hostName=null;
    SampleAxis2Server axis2Server;
    AuthenticatorClient adminServiceAuthentication;


    @BeforeTest(alwaysRun = true)
    public void setEnvironment() throws LoginAuthenticationExceptionException, IOException {
        EnvironmentBuilder builder = new EnvironmentBuilder().esb(3);
        ManageEnvironment environment = builder.build();
        backEndUrl = environment.getEsb().getBackEndUrl();
        serviceUrl = environment.getEsb().getServiceUrl();
        sessionCookie = environment.getEsb().getSessionCookie();
        nhttpPort = environment.getEsb().getProductVariables().getNhttpPort();
        hostName=environment.getEsb().getProductVariables().getHostName();
        adminServiceAuthentication = environment.getEsb().getAdminServiceAuthentication();
        axis2Server = new SampleAxis2Server();
        axis2Server.start();

        axis2Client = new StockQuoteClient();
        logAdmin = new LoggingAdminClient(backEndUrl,sessionCookie);
        logViewer=new LogViewerClient(backEndUrl,sessionCookie);

    }



    @Test(groups = "wso2.esb", description = "Tests level log")
    public void testSendingToDefinedEndpoint() throws Exception, InterruptedException {

        logAdmin.updateLoggerData("org.apache.synapse", LoggingAdminClient.logLevel.DEBUG.name(),true,false);
        OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://" +hostName+":"+nhttpPort,
                                                                     null, "WSO2");
        Assert.assertTrue(response.toString().contains("WSO2"));
        log.info(response);
        Thread.sleep(2000);
        System.out.println(response);
        GetLogs getLogs = new GetLogs();
        getLogs.setKeyword("mediator");
        LogEvent[] getLogsDebug = logViewer.getLogs("DEBUG", "LogMediator");
        LogEvent[] getLogsTrace = logViewer.getLogs("TRACE", "LogMediator");
        LogEvent[] getLogsInfo = logViewer.getLogs("INFO", "LogMediator");




    }





    @AfterTest(groups = "wso2.esb")
    public void close() throws Exception {
        axis2Server.stop();
    }
}