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
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;

import java.rmi.RemoteException;


public class StratosESBServiceTest {
    private static Log log = LogFactory.getLog(StratosESBServiceTest.class);
    private static String httpEsbStratosEpr;

    @BeforeClass
    public void init() throws LoginAuthenticationExceptionException, RemoteException {
        EnvironmentBuilder builder = new EnvironmentBuilder().esb(4);
        EnvironmentVariables esbServer = builder.build().getEsb();
        UserInfo userInfo = UserListCsvReader.getUserInfo(4);
        httpEsbStratosEpr = "http://" + esbServer.getProductVariables().getHostName() + ":"
                               + esbServer.getProductVariables().getNhttpPort() + "/services/t/"
                               + userInfo.getDomain();
    }

    @Test(invocationCount = 5)
    public void proxyServiceTest() throws AxisFault {
        demoProxyTestClient();
    }

    private static OMElement createPayLoad() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://service.carbon.wso2.org", "p");
        OMNamespace omNs2 = fac.createOMNamespace("http://service.carbon.wso2.org", "xs");
        OMElement method = fac.createOMElement("echoString", omNs);
        OMElement value = fac.createOMElement("s", omNs2);
        value.addChild(fac.createOMText(value, "Hello World"));
        method.addChild(value);
        return method;
    }

    private void demoProxyTestClient() throws AxisFault {
        long soTimeout = 2 * 60 * 1000; // Two minutes
        OMElement payload = createPayLoad();
        System.out.println(payload);
        OMElement result;
        ServiceClient serviceclient = new ServiceClient();
        Options opts = new Options();
        opts.setTimeOutInMilliSeconds(soTimeout);
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        opts.setTo(new EndpointReference(httpEsbStratosEpr + "/DemoProxy"));
        opts.setAction("urn:echoString");
        serviceclient.setOptions(opts);
        log.info(opts.getTo());
        result = serviceclient.sendReceive(payload);
        Assert.assertNotNull(result, "Response Message is null");
        Assert.assertTrue(result.toString().contains("Hello World"), "Demo proxy service invocation failed");
    }

}
