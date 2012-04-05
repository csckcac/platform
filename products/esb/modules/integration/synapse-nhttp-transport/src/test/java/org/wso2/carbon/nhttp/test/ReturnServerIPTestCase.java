/*  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.

  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/

package org.wso2.carbon.nhttp.test;

/*checking for get-property('SERVER_IP') test returning whether the correct host ip*/

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.core.utils.StockQuoteClient;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.esb.integration.ESBIntegrationTestCase;

/*uncomment and add the server IP for the following line in the axis2.xml file, "<parameter name="bind-address" locked="false">127.0.0.1</parameter>,
the same value should be displayed in the log to pass this test"*/
public class ReturnServerIPTestCase extends ESBIntegrationTestCase{

    private static final Log log = LogFactory.getLog(ReturnServerIPTestCase.class);


    @BeforeMethod(groups = "wso2.esb")
    public void init() throws Exception {
    }

    @Test(groups = "wso2.esb", description = "Test Return Server IP Address")
    public void testReturnServerIP() throws Exception {

        try {
        StockQuoteClient stockQuoteClient = new StockQuoteClient();

        loadESBConfigurationFromClasspath("/serverIP.xml");

        //launchStockQuoteService();

        String trpUrl = "http://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTP_PORT;
        OMElement result = stockQuoteClient.stockQuoteClientforProxy(trpUrl, null, "IBM");

        log.info(result);
        System.out.println(result);

        System.out.println(result);

    }

    catch (Exception e) {
        log.error("Message Relay for Server IP Test doesn't work : " + e.getMessage());

    }
    }

    @AfterMethod(groups = "wso2.esb")
    public void close() throws Exception {
        //cleanup();
    }

}
