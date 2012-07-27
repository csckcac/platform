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
package org.wso2.carbon.mediator.test.iterate;

import org.apache.axiom.om.OMElement;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.mediator.test.ESBMediatorTest;

import java.io.File;
import java.net.URL;

/*
 * Test sending large messages (~1MB) in large numbers (~50) through the
 * Iterate mediator and verify the load can be handled while iterating
 */

public class IterateLargeMessageTestCase extends ESBMediatorTest {
    private String symbol;

    @BeforeClass
    public void setEnvironment() throws Exception {
        init();
        URL url = getClass().getResource("/artifacts/ESB/mediatorconfig/iterate/iterate.txt");
        symbol = FileUtils.readFileToString(new File(url.toURI()));

    }

    @Test(groups = "wso2.esb", description = "Tests large message in small number 20")
    public void testSmallNumbers() throws Exception {
        loadESBConfigurationFromClasspath("/artifacts/ESB/mediatorconfig/iterate/simple_iterator.xml");

        OMElement response;
        for (int i = 0; i < 20; i++) {
            response =
                    axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(),
                                                            null, symbol);
            Assert.assertNotNull(response);
            Assert.assertTrue(response.toString().contains("WSO2"));
        }
    }

    @Test(groups = "wso2.esb", description = "Tests large message in large number 50")
    public void testLargeNumbers() throws Exception {
        loadESBConfigurationFromClasspath("/artifacts/ESB/mediatorconfig/iterate/simple_iterator.xml");
        OMElement response;
        for (int i = 0; i < 50; i++) {
            response =
                    axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(),
                                                            null, symbol);
            Assert.assertNotNull(response);
            Assert.assertTrue(response.toString().contains("WSO2"));
        }
    }

    @AfterClass
    public void close() throws Exception {
        super.cleanup();
    }

}
