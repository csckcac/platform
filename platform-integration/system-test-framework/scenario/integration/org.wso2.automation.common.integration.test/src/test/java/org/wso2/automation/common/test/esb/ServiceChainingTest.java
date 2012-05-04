/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.automation.common.test.esb;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClientUtils;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;


/*
This is used to test the service chaining functionality of ESB.
In this scenario, ESB will receive a credit request from a proxy service called CreditProxy.
The request has the ID of the requestor of the operation and the credit amount. But, to call the
CreditService deployed in the WSO2 Application Server,
the request must also be enriched with the name and address information of the requestor.
This information can be obtained from the PersonInfoService deployed in the WSO2 Application Server.
So, first, we need to call the PersonInfoService and enrich the request with the name and address
before calling the CreditService.
 */

public class ServiceChainingTest {


    private static final Log log = LogFactory.getLog(ServiceChainingTest.class);
    private static String JARSERVICE_EPR;
    private static String CREDITPROXY_EPR;
    private static String PERSONINFOSERVICE_EPR;

    @BeforeTest(alwaysRun = true)
    public void initializeProperties()
            throws LoginAuthenticationExceptionException, RemoteException {
        int userId = 0;
        String jarServiceName = "CreditService";
        String creditProxyName = "CreditProxy";
        String personInfoServiceName = "PersonInfoService";
        EnvironmentBuilder builder = new EnvironmentBuilder().as(userId).esb(userId);
        ManageEnvironment environment = builder.build();
        JARSERVICE_EPR = environment.getAs().getServiceUrl() + "/" + jarServiceName;
        CREDITPROXY_EPR = environment.getEsb().getServiceUrl() + "/" + creditProxyName;
        PERSONINFOSERVICE_EPR = environment.getAs().getServiceUrl() + "/" + personInfoServiceName;
    }


    @Test(groups = {"wso2.esb", "wso2.as"}, description = "Service Chaining Test", priority = 1)
    public void testServiceChaining() throws Exception {
        log.info("Running Service Chaining Test...");
        String operation = "credit";
        AxisServiceClientUtils.waitForServiceDeployment(JARSERVICE_EPR);
        AxisServiceClientUtils.waitForServiceDeployment(CREDITPROXY_EPR);
        AxisServiceClientUtils.waitForServiceDeployment(PERSONINFOSERVICE_EPR);
        Thread.sleep(5000);
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(">true<");
        AxisServiceClientUtils.sendRequest(CREDITPROXY_EPR, operation, createPayLoad().toString(),
                                           1, expectedOutput, true);
    }

    private static OMElement createPayLoad() {
        /* PayLoad =  "<sam:credit xmlns:sam="http://samples.esb.wso2.org\">
        <sam:id>99990000</sam:id><sam:amount>1000</sam:amount>
        </sam:credit>*/


        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://samples.esb.wso2.org", "sam");
        OMElement method = fac.createOMElement("credit", omNs);
        OMElement id = fac.createOMElement("id", omNs);
        OMElement amount = fac.createOMElement("amount", omNs);
        id.addChild(fac.createOMText(id, "99990000"));
        amount.addChild(fac.createOMText(amount, "1000"));
        method.addChild(id);
        method.addChild(amount);
        return method;
    }
}
