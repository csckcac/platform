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
package org.wso2.carbon.mediator.test.filter;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.esb.ESBTestCaseUtils;
import org.wso2.carbon.automation.utils.esb.StockQuoteClient;

import java.io.File;


public class FilterMediatorTest {
    private static final Log log=LogFactory.getLog(FilterMediatorTest.class);
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
        toUrl="http://localhost:9000/services/SimpleStockQuoteService";



    }

    /**
     * Test for filter mediator  - filter using source and regex
     * @throws Exception
     */
    @Test
    public void filterMediatorWithSourceAndRegexTest() throws Exception{

        ESBTestCaseUtils caseUtils=new ESBTestCaseUtils();

        caseUtils.loadSampleESBConfiguration(1,backEndUrl,esbServer.getSessionCookie());

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(mainSeqUrl, toUrl, "WSO2");


        Assert.assertTrue(response.toString().contains("GetQuoteResponse"));
        Assert.assertTrue(response.toString().contains("WSO2 Company"));


    }

    /**
     * Test for filter mediator  - filter using source and regex with namespace prefix
     * /filters/filter/syanpse1.xml is used
     * @throws Exception
     */
    @Test
    public void filterMediatorWithSourceAndRegexNSTest() throws Exception{

        ESBTestCaseUtils caseUtils=new ESBTestCaseUtils();
        caseUtils.loadESBConfigurationFromClasspath(File.separator+"artifacts"+File.separator+"ESB"+File.separator+"synapseconfig"+File.separator+"filters"+File.separator+"filter"+File.separator+"synapse1.xml", backEndUrl, esbServer.getSessionCookie());
        OMElement response = axis2Client.sendSimpleStockQuoteRequest(mainSeqUrl, toUrl, "IBM");
        Assert.assertTrue(response.toString().contains("GetQuoteResponse"));
        Assert.assertTrue(response.toString().contains("IBM Company"));
    }

    /**
     * With setting "Specify As" set to "Xpath"
     * /filters/filter/syanpse2.xml is used
     * @throws Exception
     */
    @Test
    public void filterMediatorWithXpathTest() throws Exception{

        ESBTestCaseUtils caseUtils=new ESBTestCaseUtils();
        caseUtils.loadESBConfigurationFromClasspath(File.separator+"artifacts"+File.separator+"ESB"+File.separator+"synapseconfig"+File.separator+"filters"+File.separator+"filter"+File.separator+"synapse2.xml", backEndUrl, esbServer.getSessionCookie());
        OMElement response = axis2Client.sendSimpleStockQuoteRequest(mainSeqUrl, null, "IBM");
        Assert.assertTrue(response.toString().contains("GetQuoteResponse"));
        Assert.assertTrue(response.toString().contains("IBM Company"));
        System.out.println();
    }

    /**
     *  Using OR operation with Xpaths
     * /filters/filter/syanpse3.xml is used
     * @throws Exception
     */
    @Test
    public void filterMediatorXpathWithORTest() throws Exception{

        ESBTestCaseUtils caseUtils=new ESBTestCaseUtils();
        caseUtils.loadESBConfigurationFromClasspath(File.separator+"artifacts"+File.separator+"ESB"+File.separator+"synapseconfig"+File.separator+"filters"+File.separator+"filter"+File.separator+"synapse3.xml", backEndUrl, esbServer.getSessionCookie());
        OMElement response11 = axis2Client.sendSimpleStockQuoteSoap11(mainSeqUrl, null, "IBM");
        OMElement response12 = axis2Client.sendSimpleStockQuoteSoap12(mainSeqUrl, null, "IBM");

        Assert.assertTrue(response11.toString().contains("GetQuoteResponse"));
        Assert.assertTrue(response11.toString().contains("IBM Company"));

        Assert.assertTrue(response12.toString().contains("GetQuoteResponse"));
        Assert.assertTrue(response12.toString().contains("IBM Company"));

        System.out.println();
    }

    @AfterTest
    public void afterTest() {
    }


}
