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
