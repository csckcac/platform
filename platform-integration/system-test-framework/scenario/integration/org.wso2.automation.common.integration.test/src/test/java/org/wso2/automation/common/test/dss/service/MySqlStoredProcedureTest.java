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
package org.wso2.automation.common.test.dss.service;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;
import org.wso2.automation.common.test.dss.utils.DataServiceTest;

public class MySqlStoredProcedureTest extends DataServiceTest {
    private static final Log log = LogFactory.getLog(MySqlStoredProcedureTest.class);


    private final OMFactory fac = OMAbstractFactory.getOMFactory();
    private final OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice/samples/StoredProcedure", "ns1");

    @Override
    protected void setServiceName() {
        serviceName = "StoredProcedureServiceTest";
    }

    @Test(priority = 1, dependsOnMethods = {"serviceDeployment"})
    public void INParamsOperation() throws AxisFault {
        for (int i = 1; i < 6; i++) {
            addEmployee(i + "");
        }
        log.info("Stored procedure IN parameter verified");
    }

    @Test(priority = 2, dependsOnMethods = {"serviceDeployment", "INParamsOperation"})
    public void INOUTParamsOperation() throws AxisFault {
        for (int i = 1; i < 6; i++) {
            getEmployeeById(i + "");
        }
        log.info("stored procedure INOUT parameter verified");
    }

    @Test(priority = 3, dependsOnMethods = {"serviceDeployment"})
    public void OUTParamsOperation() throws AxisFault {
        getEmployees();
        log.info("stored procedure OUT parameter verified");
    }

    private void addEmployee(String employeeNumber) throws AxisFault {

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

        new AxisServiceClient().sendRobust(payload, serviceEndPoint, "addEmployee");

    }

    private OMElement getEmployeeById(String employeeNumber) throws AxisFault {
        OMElement payload = fac.createOMElement("getEmployeeByEmpNo", omNs);

        OMElement empNo = fac.createOMElement("employeeNumber", omNs);
        empNo.setText(employeeNumber);
        payload.addChild(empNo);

        OMElement result = new AxisServiceClient().sendReceive(payload, serviceEndPoint, "getEmployeeByEmpNo");
        Assert.assertTrue(result.toString().contains("<Employee>"), "Result Set Not found in response message");
        Assert.assertTrue(result.toString().contains("<EmployeeNumber>" + employeeNumber + "</EmployeeNumber>"), "Expected Result Mismatched");

        return result;
    }

    private OMElement getEmployees() throws AxisFault {
        OMElement payload = fac.createOMElement("getEmployees", omNs);

        OMElement result = new AxisServiceClient().sendReceive(payload, serviceEndPoint, "getEmployees");
        Assert.assertTrue(result.toString().contains("<FirstName>AAA</FirstName>"), "Expected Result Mismatched");
        return result;
    }

}
