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
package org.wso2.carbon.mediator.test.rule;

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.mediator.test.ESBMediatorTest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class WithOutRuleSetPropertyTestCase extends ESBMediatorTest {


    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath("/artifacts/ESB/synapseconfig/config_without_rule/synapse.xml");

    }

    @Test(groups = "wso2.esb",
          description = "scenario without rules")
    public void testInvokeInvalidRule() throws Exception {
        try {
            axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(), null, "IBM");
            fail("Request should throws AxisFault");
        } catch (AxisFault axisFault) {
            assertEquals(axisFault.getMessage(), "The input stream for an incoming message is null.",
                         "Fault: value mismatched, should be 'The input stream for an incoming message is null.'");
        }

    }


    @AfterClass(alwaysRun = true)
    public void destroy() {
        super.cleanup();
    }
}
