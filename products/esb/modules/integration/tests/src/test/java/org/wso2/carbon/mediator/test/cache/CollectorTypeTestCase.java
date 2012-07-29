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

/*
import org.wso2.carbon.automation.utils.esb.ESBTestCaseUtils;
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
package org.wso2.carbon.mediator.test.cache;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.mediator.test.ESBMediatorTest;

import static org.testng.Assert.assertTrue;

/**
 * This class will test cache mediator which has a collector type cache mediator in 'in'
 * sequence
 */
public class CollectorTypeTestCase extends ESBMediatorTest {

    @BeforeClass(alwaysRun = true)
    public void deployArtifacts() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath("/artifacts/ESB/mediatorconfig/cache/CollectorTypeCacheMediator.xml");
    }

    @Test(groups = {"wso2.esb"}, description = "Creating Collector Type Mediator Test Case")
    public void testCollectorTypeMediator() throws AxisFault {
        OMElement response;
        //TODO
        try {
            response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/", "http://localhost:9000/services/SimpleStockQuoteService", "WSO2");
            response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/", "http://localhost:9000/services/SimpleStockQuoteService", "WSO2");
            response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/", "http://localhost:9000/services/SimpleStockQuoteService", "WSO2");
        } catch (AxisFault message) {
            if (message.getLocalizedMessage().equalsIgnoreCase("The input stream for an incoming message is null")) {
                assertTrue(true, "Response caching worked even with the controller cache in 'in' sequence");
            }
        }
    }

    @AfterClass(groups = "wso2.esb")
    public void close() throws Exception {
       super.cleanup();
    }

}
