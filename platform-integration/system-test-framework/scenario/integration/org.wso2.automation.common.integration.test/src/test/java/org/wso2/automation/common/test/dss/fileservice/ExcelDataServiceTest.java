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

public class ExcelDataServiceTest extends DataServiceTest {
    private static final Log log = LogFactory.getLog(ExcelDataServiceTest.class);

    @Override
    protected void setServiceName() {
        serviceName = "ExcelDataService";
    }

    @Test(priority = 1, dependsOnMethods = {"serviceDeployment"})
    public void selectOperation() throws AxisFault {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice", "ns1");
        OMElement payload = fac.createOMElement("getProducts", omNs);
        for (int i = 0; i < 5; i++) {
            OMElement result = new AxisServiceClient().sendReceive(payload, serviceEndPoint, "getProducts");
            log.info("Response :" + result);
            Assert.assertTrue((result.toString().indexOf("Products") == 1), "Expected Result Not found");
            Assert.assertTrue(result.toString().contains("<Product>"), "Expected Result Not found");
            Assert.assertTrue(result.toString().contains("<ID>"), "Expected Result Not found");
            Assert.assertTrue(result.toString().contains("<Name>"), "Expected Result Not found");

        }
        log.info("Service invocation success");
    }

    @Test(priority = 2, dependsOnMethods = {"serviceDeployment"})
    public void xsltTransformation() throws AxisFault {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice", "ns1");
        OMElement payload = fac.createOMElement("getProductClassifications", omNs);
        for (int i = 0; i < 5; i++) {
            OMElement result = new AxisServiceClient().sendReceive(payload, serviceEndPoint, "getProductClassifications");
            if (log.isDebugEnabled()) {
                log.debug("Response :" + result);
            }
            Assert.assertTrue((result.toString().indexOf("Products") == 1), "Expected Result Not found");
            Assert.assertTrue(result.toString().contains("<Product>"), "Expected Result Not found");
            Assert.assertTrue(result.toString().contains("<Product-Name>"), "Expected Result Not found");
            Assert.assertTrue(result.toString().contains("<Product-Classification>"), "Expected Result Not found");

        }
        log.info("XSLT Transformation Success");
    }

    @Test(priority = 2, dependsOnMethods = {"selectOperation"}, timeOut = 1000 * 60 * 1)
    public void concurrencyTest() throws ConcurrencyTestFailedError, InterruptedException {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice", "ns1");
        OMElement payload = fac.createOMElement("getProducts", omNs);
        ConcurrencyTest concurrencyTest = new ConcurrencyTest(25, 20);
        concurrencyTest.run(serviceEndPoint, payload, "getProducts");
    }

}
