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
package org.wso2.automation.common.test.dss.syntax;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.automation.common.test.dss.utils.DataServiceTest;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;

//https://wso2.org/jira/browse/CARBON-12361
public class ReturnRequestStatusTest extends DataServiceTest {
    private static final Log log = LogFactory.getLog(WhiteSpaceWithQueryParamsTest.class);

    private final OMFactory fac = OMAbstractFactory.getOMFactory();
    private final OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice/samples/rdbms_sample", "ns1");

    @Override
    protected void setServiceName() {
        serviceName = "ReturnRequestStatusTest";
    }

    @Test(priority = 1, dependsOnMethods = {"serviceDeployment"})
    public void requestStatusNameSpaceQualifiedForInsertOperation() throws AxisFault {

        addEmployee(serviceEndPoint, String.valueOf(180));
        log.info("Insert Operation Success");
    }

    @Test(priority = 2, dependsOnMethods = {"requestStatusNameSpaceQualifiedForInsertOperation"})
    public void requestStatusNameSpaceQualifiedForDeleteOperation() throws AxisFault {
        deleteEmployeeById(String.valueOf(180));
        log.info("Delete operation success");
    }

    private void addEmployee(String serviceEndPoint, String employeeNumber) throws AxisFault {
        OMElement result;
        OMElement payload = fac.createOMElement("addEmployee", omNs);

        OMElement empNo = fac.createOMElement("employeeNumber", omNs);
        empNo.setText(employeeNumber);
        payload.addChild(empNo);

        OMElement lastName = fac.createOMElement("lastName", omNs);
        lastName.setText("BBB");
        payload.addChild(lastName);

        OMElement fName = fac.createOMElement("firstName", omNs);
        fName.setText("AAA");
        payload.addChild(fName);

        OMElement email = fac.createOMElement("email", omNs);
        email.setText("aaa@ccc.com");
        payload.addChild(email);

        OMElement salary = fac.createOMElement("salary", omNs);
        salary.setText("50000");
        payload.addChild(salary);

        result = new AxisServiceClient().sendReceive(payload, serviceEndPoint, "addEmployee");
        Assert.assertTrue(result.toString().contains("SUCCESSFUL"), "Response Not Successful");
        OMNamespace nameSpace = result.getNamespace();
        Assert.assertNotNull(nameSpace, "Response Message NameSpace not qualified");
        Assert.assertEquals(nameSpace.getPrefix(), "axis2ns1", "Invalid prefix");
        Assert.assertEquals(nameSpace.getNamespaceURI(), "http://ws.wso2.org/dataservice", "Invalid NamespaceURI");


    }

    private void deleteEmployeeById(String employeeNumber) throws AxisFault {
        OMElement result;
        OMElement payload = fac.createOMElement("deleteEmployeeById", omNs);

        OMElement empNo = fac.createOMElement("employeeNumber", omNs);
        empNo.setText(employeeNumber);
        payload.addChild(empNo);

        result = new AxisServiceClient().sendReceive(payload, serviceEndPoint, "deleteEmployeeById");

        Assert.assertTrue(result.toString().contains("SUCCESSFUL"), "Response Not Successful");
        OMNamespace nameSpace = result.getNamespace();
        Assert.assertEquals(nameSpace.getPrefix(), "axis2ns1", "Invalid prefix");
        Assert.assertEquals(nameSpace.getNamespaceURI(), "http://ws.wso2.org/dataservice", "Invalid NamespaceURI");
    }

}
