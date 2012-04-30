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
package org.wso2.automation.common.test.dss.fileservice;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.automation.common.test.dss.utils.ConcurrencyTest;
import org.wso2.automation.common.test.dss.utils.exception.ConcurrencyTestFailedError;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;
import org.wso2.automation.common.test.dss.utils.DataServiceTest;

public class GSpreadDataServiceTest extends DataServiceTest {

    private static final Log log = LogFactory.getLog(GSpreadDataServiceTest.class);

    @Override
    protected void setServiceName() {
        serviceName = "GSpreadDataService";
    }

    @Test(priority = 1, dependsOnMethods = {"serviceDeployment"})
    public void selectOperation() throws AxisFault {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice/samples/gspread_sample_service", "ns1");
        OMElement payload = fac.createOMElement("getCustomers", omNs);
        for (int i = 0; i < 5; i++) {
            OMElement result = new AxisServiceClient().sendReceive(payload, serviceEndPoint, "getProducts");
            log.info("Response : " + result);
            Assert.assertTrue((result.toString().indexOf("Customers") == 1), "Expected Result not found on response message");
            Assert.assertTrue(result.toString().contains("<customerNumber>"), "Expected Result not found on response message");
            Assert.assertTrue(result.toString().contains("</Customer>"), "Expected Result not found on response message");

        }
        log.info("Service Invocation success");
    }

    @Test(priority = 2, dependsOnMethods = {"selectOperation"}, timeOut = 1000 * 60 * 2)
    public void concurrencyTest() throws ConcurrencyTestFailedError, InterruptedException {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice/samples/gspread_sample_service", "ns1");
        OMElement payload = fac.createOMElement("getCustomers", omNs);
        ConcurrencyTest concurrencyTest = new ConcurrencyTest(25, 5);
        concurrencyTest.run(serviceEndPoint, payload, "getProducts");
    }

}
