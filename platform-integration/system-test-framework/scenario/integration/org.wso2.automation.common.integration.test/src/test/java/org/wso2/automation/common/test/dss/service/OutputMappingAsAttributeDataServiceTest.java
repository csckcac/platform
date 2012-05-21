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
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.automation.common.test.dss.utils.DataServiceTest;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;

import javax.xml.namespace.QName;
import java.util.Iterator;

/*This class provide a test for CARBON-13024*/
public class OutputMappingAsAttributeDataServiceTest extends DataServiceTest {
    @Override
    protected void setServiceName() {
        serviceName = "OutputMappingAttributeDataService";
    }

    @Test(priority = 1, dependsOnMethods = {"serviceDeployment"})
    public void getOffices() throws AxisFault {
        AxisServiceClient serviceClient = new AxisServiceClient();
        OMElement response = serviceClient.sendReceive(getPayloadGetOffice(), serviceEndPoint, "showAllOffices");
        Assert.assertNotNull(response, "Response Message in null");
        Iterator<OMElement> itr = response.getChildrenWithName(new QName("Office"));
        int recordCount = 0;
        while (itr.hasNext()) {
            OMElement office = itr.next();
            Assert.assertNotNull(office.getAttribute(new QName("officeCode")), "officeCode attribute not found with response");
            Assert.assertFalse(office.getAttributeValue(new QName("officeCode")).equalsIgnoreCase(""), "Attribute Name officeCode value empty");

            if (office.getAttribute(new QName("country")) != null) {
                Assert.assertFalse(office.getAttributeValue(new QName("country")).equalsIgnoreCase(""), "Attribute Name country value empty");
            }

            ++recordCount;
        }
        Assert.assertEquals(recordCount, 7, "All offices on the table not in response.invalid response. " +
                                            "because one of attribute value is null");

    }

    @Test(priority = 2)
    public void addOffice() throws AxisFault {
        AxisServiceClient serviceClient = new AxisServiceClient();
        serviceClient.sendRobust(getPayloadAddOffice("25", "NY", "Boston", "EMEA"), serviceEndPoint, "addOffice");
        serviceClient.sendRobust(getPayloadAddOffice("26", "NY", "Boston", null), serviceEndPoint, "addOffice");
        serviceClient.sendRobust(getPayloadAddOffice("27", "NY", "Boston", "NULL"), serviceEndPoint, "addOffice");
    }

    private OMElement getPayloadGetOffice() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice", "ns1");
        return fac.createOMElement("showAllOffices", omNs);
    }

    private OMElement getPayloadAddOffice(String officeCode, String city, String state,
                                          String territory) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice", "ns1");
        OMElement payload = fac.createOMElement("showAllOffices", omNs);

        OMElement officeCodeOme = fac.createOMElement("officeCode", omNs);
        officeCodeOme.setText(officeCode);
        payload.addChild(officeCodeOme);

        OMElement cityOme = fac.createOMElement("city", omNs);
        cityOme.setText(city);
        payload.addChild(cityOme);

        OMElement stateOme = fac.createOMElement("state", omNs);
        stateOme.setText(state);
        payload.addChild(stateOme);

        OMElement territoryOme = fac.createOMElement("territory", omNs);
        territoryOme.setText(territory);
        payload.addChild(territoryOme);

        return payload;
    }
}
