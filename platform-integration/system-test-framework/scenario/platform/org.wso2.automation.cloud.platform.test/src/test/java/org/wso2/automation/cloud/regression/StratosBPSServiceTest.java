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

/*
Test class for Stratos BPS service test automation
 */
public class StratosBPSServiceTest {

    private static final Log log = LogFactory.getLog(StratosBPSServiceTest.class);
    private static String httpBrsStratosUrl;


    @BeforeClass
    public void init() throws LoginAuthenticationExceptionException, RemoteException {
        EnvironmentBuilder builder = new EnvironmentBuilder().bps(4);
        EnvironmentVariables bpsServer = builder.build().getBps();
        UserInfo userInfo = UserListCsvReader.getUserInfo(4);
        httpBrsStratosUrl = "http://" + bpsServer.getProductVariables().getHostName()
                                   + "/services/t/" + userInfo.getDomain();
    }

    @Test(invocationCount = 5)
    public void functionProcessServiceTest() throws AxisFault, InterruptedException {
        functionProcessService();
    }

    @Test(invocationCount = 5)
    public void customerInfoServiceTest() throws AxisFault {
        customerInfoService();
    }

    @Test(invocationCount = 5)
    public void helloWorldServiceTest() throws AxisFault {
        helloWorld();
    }

    private static OMElement createPayLoad() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ode/bpel/unit-test.wsdl", "ns1");
        OMElement method = fac.createOMElement("hello", omNs);
        OMElement value = fac.createOMElement("TestPart", null);
        value.addChild(fac.createOMText(value, "Hello"));
        method.addChild(value);
        return method;
    }

    private void helloWorld() throws AxisFault {
        OMElement payload = createPayLoad();

        OMElement result;

        ServiceClient serviceclient = new ServiceClient();
        Options opts = new Options();
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        opts.setTo(new EndpointReference(httpBrsStratosUrl + "/HelloService/"));
        opts.setAction("http://ode/bpel/unit-test.wsdl/hello");

        serviceclient.setOptions(opts);

        result = serviceclient.sendReceive(payload);
        log.info(result);
        Assert.assertNotNull(result, "Response Message is null");
        Assert.assertTrue(result.toString().contains("Hello World"), "HelloWorld BPEL invocation failed");

    }

    private void customerInfoService() throws AxisFault {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://wso2.org/bps/samples/loan_process/schema", "ns");
        OMElement payload = fac.createOMElement("CustomerInfo", omNs);
        OMElement name = fac.createOMElement("Name", omNs);
        OMElement email = fac.createOMElement("Email", omNs);
        OMElement cusID = fac.createOMElement("CustomerID", omNs);
        OMElement creditRating = fac.createOMElement("CreditRating", omNs);
        name.addChild(fac.createOMText(payload, "ManualQA0001"));
        email.addChild(fac.createOMText(payload, "testwso2qa@wso2.org"));
        cusID.addChild(fac.createOMText(payload, "1234"));
        creditRating.addChild(fac.createOMText(payload, "123"));
        payload.addChild(name);
        payload.addChild(email);
        payload.addChild(cusID);
        payload.addChild(creditRating);

        OMElement result;

        ServiceClient serviceclient = new ServiceClient();
        Options opts = new Options();
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        opts.setTo(new EndpointReference(httpBrsStratosUrl + "/CustomerInfoService/"));
        opts.setAction("http://wso2.org/bps/samples/loan_process/schema/getCustomerSSN");

        serviceclient.setOptions(opts);

        result = serviceclient.sendReceive(payload);
        log.info(result);
        Assert.assertNotNull(result, "Response Message is null");
        Assert.assertTrue(result.toString().contains("43235678SSN"), "CustomerInfo BPEL invocation failed");

    }


    private void functionProcessService() throws AxisFault, InterruptedException {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://www.example.org/messages", "mes");
        OMElement payload = fac.createOMElement("functionProcessServiceRequest", omNs);
        OMElement param0 = fac.createOMElement("param0", omNs);
        OMElement param1 = fac.createOMElement("param1", omNs);
        param0.addChild(fac.createOMText(payload, "2"));
        param1.addChild(fac.createOMText(payload, "2"));
        payload.addChild(param0);
        payload.addChild(param1);

        OMElement result;

        ServiceClient serviceclient = new ServiceClient();
        Options opts = new Options();
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        opts.setTo(new EndpointReference(httpBrsStratosUrl + "/FunctionProcessServiceService/"));
        opts.setAction("http://www.example.org/messages/FunctionProcessServiceOperation");

        serviceclient.setOptions(opts);

        result = serviceclient.sendReceive(payload);
        log.info(result);
//        Thread.sleep(60000);
        Assert.assertNotNull(result, "Response Message is null");
        Assert.assertTrue(result.toString().contains("256"), "FunctionProcessService BPEL invocation failed");

    }

}
