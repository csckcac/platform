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

package org.wso2.automation.cloud.regression;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;

public class StratosBRSServiceTest {

    private static final Log log = LogFactory.getLog(StratosBRSServiceTest.class);
    private static String httpRuleStratosUrl;

    @BeforeClass
    public void init() {
        EnvironmentBuilder builder = new EnvironmentBuilder().brs(4);
        EnvironmentVariables brsServer = builder.build().getBrs();
        UserInfo userInfo = UserListCsvReader.getUserInfo(4);
        httpRuleStratosUrl = "http://" + brsServer.getProductVariables().getHostName()
                                    + "/services/t/" + userInfo.getDomain();
    }

    @Test(invocationCount = 5)
    public void greetingServiceTest() throws AxisFault {
        greetingService();
    }


    private static OMElement createPayLoad() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://brs.carbon.wso2.org", "ns1");
        OMNamespace nameNs = fac.createOMNamespace("http://greeting.samples/xsd", "ns2");
        OMElement method = fac.createOMElement("greetMe", omNs);
        OMElement value = fac.createOMElement("User", omNs);
        OMElement NameValue = fac.createOMElement("name", nameNs);
        NameValue.addChild(fac.createOMText(NameValue, "QAuser"));
        value.addChild(NameValue);
        method.addChild(value);
        return method;
    }

    private void greetingService() throws AxisFault {
        OMElement payload = createPayLoad();
        ServiceClient serviceclient = new ServiceClient();
        Options opts = new Options();
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        opts.setTo(new EndpointReference(httpRuleStratosUrl + "/GreetingService/"));
        opts.setAction("http://brs.carbon.wso2.org/greetMe");

        serviceclient.setOptions(opts);
        OMElement result;
        result = serviceclient.sendReceive(payload);
        log.info(result);
        Assert.assertNotNull(result, "Response Message is null");
        Assert.assertTrue(result.toString().contains("QAuser"), "Greeting service invocation failed");


    }

}
