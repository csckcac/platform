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
package org.wso2.carbon.mediator.test.in;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.esb.ESBTestCaseUtils;
import org.wso2.carbon.automation.utils.esb.StockQuoteClient;

/**
 * This test case covers following test scenarios.
 * 1. Have an "In" sequence with set of child mediators and an 'out' path. Send a message and observe if the In path is correctly invoked.
 * 2. Observe if 'In' path is skipped when the response is received at ESB.
 *
 */
public class InSequenceMediatorTest {

    private static final Log log=LogFactory.getLog(InSequenceMediatorTest.class);
    private String backEndUrl=null;
    private String serviceUrl=null;
    private String toUrl=null;
    private String mainSeqUrl;
    private StockQuoteClient axis2Client;
    private EnvironmentVariables esbServer;
    private UserInfo userInfo;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        axis2Client = new StockQuoteClient();
        userInfo = UserListCsvReader.getUserInfo(1);

        EnvironmentBuilder builder = new EnvironmentBuilder().esb(1);
        esbServer = builder.build().getEsb();

        ManageEnvironment environment=builder.build();
        backEndUrl=environment.getEsb().getBackEndUrl();

        mainSeqUrl= "http://" + esbServer.getProductVariables().getHostName() + ":" + esbServer.getProductVariables().getNhttpPort();

        ESBTestCaseUtils caseUtils=new ESBTestCaseUtils();
        caseUtils.loadESBConfigurationFromClasspath("/artifacts/ESB/synapseconfig/filters/in/synapse.xml", backEndUrl, esbServer.getSessionCookie());

    }

    @Test
    public void inSequenceTest() throws Exception{

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(mainSeqUrl, null, "WSO2");

        Assert.assertTrue(response.toString().contains("GetQuoteResponse"));
        Assert.assertTrue(response.toString().contains("WSO2 Company"));

    }

}
