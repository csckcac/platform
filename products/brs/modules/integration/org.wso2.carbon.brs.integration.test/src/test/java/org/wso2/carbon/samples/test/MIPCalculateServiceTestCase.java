/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.samples.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;

import static org.testng.Assert.assertNotNull;

public class MIPCalculateServiceTestCase {

    @Test(groups = {"wso2.brs"})
    public void testCalculate() throws AxisFault, XMLStreamException {
        ClientConnectionUtil.waitForPort(9763);
        ServiceClient serviceClient = new ServiceClient();
        Options options = new Options();
        options.setTo(new EndpointReference("http://localhost:9763/services/MIPCalculateService"));
        options.setAction("urn:calculate");
        serviceClient.setOptions(options);

        OMElement result = serviceClient.sendReceive(createPayload());
        assertNotNull(result, "Result cannot be null");
    }

    private OMElement createPayload() throws XMLStreamException {
        String request = "<p:calculateRequest xmlns:p=\"http://brs.carbon.wso2.org\">\n" +
                         "   <!--Zero or more repetitions:-->\n" +
                         "   <p:Client>\n" +
                         "      <!--Zero or 1 repetitions:-->\n" +
                         "      <xs:downPayment xmlns:xs=\"http://MIPCalculate.samples/xsd\">90000</xs:downPayment>\n" +
                         "      <!--Zero or 1 repetitions:-->\n" +
                         "      <xs:loanType xmlns:xs=\"http://MIPCalculate.samples/xsd\">FHA</xs:loanType>\n" +
                         "      <!--Zero or 1 repetitions:-->\n" +
                         "      <xs:mortgageValue xmlns:xs=\"http://MIPCalculate.samples/xsd\">100000</xs:mortgageValue>\n" +
                         "   </p:Client>\n" +
                         "</p:calculateRequest>";
        return new StAXOMBuilder(new ByteArrayInputStream(request.getBytes())).getDocumentElement();
    }
}
