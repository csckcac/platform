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

public class StratosMSServiceTest {
    private static final Log log = LogFactory.getLog(StratosAppServiceTest.class);
    private static String httpMsStratosUrl;
    private static String userName;

    @BeforeClass
    public void init() throws LoginAuthenticationExceptionException, RemoteException {
        EnvironmentBuilder builder = new EnvironmentBuilder().ms(4);
        EnvironmentVariables msServer = builder.build().getMs();
        UserInfo userInfo = UserListCsvReader.getUserInfo(4);
        userName = userInfo.getUserName().substring(0, userInfo.getUserName().indexOf('@'));
        httpMsStratosUrl = "http://" + msServer.getProductVariables().getHostName()
                           + "/services/t/" + userInfo.getDomain();
    }

    @Test(invocationCount = 5)
    public void schemaTestMashupTest() throws AxisFault {
        schemaTestMashup();
    }

    @Test(invocationCount = 5)
    public void httpClientServiceTest() throws AxisFault {
        httpClientService();
    }

    @Test(invocationCount = 5)
    public void requestServiceTest() throws AxisFault {
        requestService();
    }

    @Test(invocationCount = 5)
    public void sessionServiceTes() throws AxisFault {
        sessionService();
    }

    @Test(invocationCount = 5)
    public void feedServiceTest() throws AxisFault {
        feedService();
    }

    @Test(invocationCount = 5)
    public void WSRequestTest() throws AxisFault {
        WSRequest();
    }

    @Test(invocationCount = 5, enabled = false)
    public void IMServiceTest() throws AxisFault {
        IMService();
    }

    private static OMElement createPayLoad() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://services.mashup.wso2.org/schemaTest1", "ns1");
        OMElement method = fac.createOMElement("echoJSString", omNs);
        OMElement value = fac.createOMElement("param", null);
        value.addChild(fac.createOMText(value, "Hello World"));
        method.addChild(value);
        return method;
    }

    private void schemaTestMashup() throws AxisFault {

        OMElement payload = createPayLoad();
        OMElement result;
        ServiceClient serviceclient = new ServiceClient();
        Options opts = new Options();
        opts.setTo(new EndpointReference(httpMsStratosUrl + "/" + userName + "/TestMSServices/"));
        opts.setAction("http://services.mashup.wso2.org/schemaTest1");
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        serviceclient.setOptions(opts);

        result = serviceclient.sendReceive(payload);
        log.info(result);
        Assert.assertNotNull(result, "Response Message is null");
        Assert.assertTrue(result.toString().contains("Hello World"), "Schema test mashup service invocation failed");
    }

    private void httpClientService() throws AxisFault {

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://services.mashup.wso2.org/httpClient?xsd", "ns1");
        OMElement payload = fac.createOMElement("searchGoogle", omNs);
        OMElement query = fac.createOMElement("query", null);
        query.setText("wso2.com");
        payload.addChild(query);

        OMElement result;
        ServiceClient serviceclient = new ServiceClient();
        Options opts = new Options();

        opts.setTo(new EndpointReference(httpMsStratosUrl + "/" + userName + "/httpClient"));
        opts.setAction(userName + "/searchGoogle");
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        serviceclient.setOptions(opts);

        result = serviceclient.sendReceive(payload);
        Assert.assertNotNull(result, "Response message is null");
        Assert.assertTrue(result.toString().contains("html"), "HttpClient test mashup service invocation failed");
        Assert.assertTrue(result.toString().contains("http://www.google.com/search?q=wso2.com"), "HttpClient test mashup service invocation failed");

    }


    private void requestService() throws AxisFault {

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://services.mashup.wso2.org/request?xsd", "ns1");
        OMElement payload = fac.createOMElement("returnURL", omNs);

        OMElement result;
        ServiceClient serviceclient = new ServiceClient();
        Options opts = new Options();

        opts.setTo(new EndpointReference(httpMsStratosUrl + "/" + userName + "/request/"));
        opts.setAction(userName + "/returnURL");
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        serviceclient.setOptions(opts);

        result = serviceclient.sendReceive(payload);
        log.info(result);
        Assert.assertNotNull(result, "Response Message is null");
        Assert.assertTrue(result.toString().contains(userName), "Request test mashup service invocation failed");
    }


    private void IMService() throws AxisFault {

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://services.mashup.wso2.org/IMAllScenarios?xsd", "ns1");
        OMElement payload = fac.createOMElement("gabberSingleMsg", omNs);

        OMElement result;
        ServiceClient serviceclient = new ServiceClient();
        Options opts = new Options();

        opts.setTo(new EndpointReference(httpMsStratosUrl + "/" + userName + "/IMAllScenarios/"));
        opts.setAction(userName + "/gabberSingleMsg");
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        serviceclient.setOptions(opts);

        result = serviceclient.sendReceive(payload);
        log.info(result);
        Assert.assertNotNull(result, "Response Message is null");
        Assert.assertTrue(result.toString().contains("successful"), "IM test mashup service invocation failed");

    }


    private void sessionService() throws AxisFault {
        Boolean status = false;

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://services.mashup.wso2.org/session?xsd", "ns1");
        OMElement payload = fac.createOMElement("putValue", omNs);
        OMElement param = fac.createOMElement("param", null);
        payload.addChild(param);

        OMElement result;
        ServiceClient serviceclient = new ServiceClient();
        Options opts = new Options();

        opts.setTo(new EndpointReference(httpMsStratosUrl + "/" + userName + "/ApplicationScopeService/"));
        opts.setAction(userName + "/putValue");
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        serviceclient.setOptions(opts);

        result = serviceclient.sendReceive(payload);
        Assert.assertNotNull(result, "Response Message is null");

        if ((result.toString().indexOf("success")) > 0) {
            payload = fac.createOMElement("getValue", omNs);
            param = fac.createOMElement("param", null);
            payload.addChild(param);
            result = serviceclient.sendReceive(payload);
            if ((result.toString().indexOf("200")) > 0) {
                status = true;
            }
        }
        Assert.assertTrue(status, "Session test mashup service invocation failed");

    }


    private void feedService() throws AxisFault {

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://services.mashup.wso2.org/feedReader2?xsd", "ns1");
        OMElement payload = fac.createOMElement("test", omNs);

        OMElement result;
        ServiceClient serviceclient = new ServiceClient();
        Options opts = new Options();

        opts.setTo(new EndpointReference(httpMsStratosUrl + "/" + userName + "/feedReader2/"));
        opts.setAction(userName + "/test");
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        serviceclient.setOptions(opts);

        result = serviceclient.sendReceive(payload);
        Assert.assertNotNull(result, "Response Message is null");
        Assert.assertTrue(result.toString().contains("http://www.formula1.com/news/headlines/"), "Feed test mashup service invocation failed");

    }


    private void WSRequest() throws AxisFault {

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://services.mashup.wso2.org/WSRequest?xsd", "ns1");
        OMElement payload = fac.createOMElement("invokeGetVersion", omNs);

        OMElement result;
        ServiceClient serviceclient = new ServiceClient();
        Options opts = new Options();

        opts.setTo(new EndpointReference(httpMsStratosUrl + "/" + userName + "/WSRequest/"));
        opts.setAction(userName + "/invokeGetVersion");
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        serviceclient.setOptions(opts);

        result = serviceclient.sendReceive(payload);
        Assert.assertNotNull(result, "Response Message is null");
        Assert.assertTrue(result.toString().contains("WSO2 Stratos Application Server"), "WSRequest test mashup service invocation failed");

    }


}
