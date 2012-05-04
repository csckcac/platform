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
import org.wso2.carbon.admin.service.AdminServiceSecurity;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigExceptionException;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.automation.cloud.regression.stratosutils.asutils.asSecurityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;


public class StratosAppServiceTest {
    private static final Log log = LogFactory.getLog(StratosAppServiceTest.class);
    private static String appServerStratosUrl;
    private static String httpAppServerStratosUrl;

    @BeforeClass
    public void init() throws LoginAuthenticationExceptionException, RemoteException {
        EnvironmentBuilder builder = new EnvironmentBuilder().as(4);
        EnvironmentVariables appServer = builder.build().getAs();
        UserInfo userInfo = UserListCsvReader.getUserInfo(4);
        appServerStratosUrl = "https://" + appServer.getProductVariables().getHostName()
                                + "/t/" + userInfo.getDomain();
        httpAppServerStratosUrl = "http://" + appServer.getProductVariables().getHostName()
                                         + "/services/t/" + userInfo.getDomain();
    }

    @Test(invocationCount = 5)
    public void webApplicationTest() throws IOException {
        webappTest();
    }

    @Test(invocationCount = 5)
    public void JARServiceInvocationTest() throws IOException {
        JARServiceTest();
    }

    @Test(invocationCount = 5)
    public void JAXWSServiceInvocationTest() throws IOException {
        JAXWSTest();
    }

    @Test(invocationCount = 5)
    public void axis2ServiceInvocationTest() throws IOException {
        axis2ServiceTest();
    }

    @Test(invocationCount = 5)
    public void springServiceInvocationTest() throws IOException {
        springServiceTest();
    }

    private static OMElement createPayLoad() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://service.carbon.wso2.org", "ns1");
        OMElement method = fac.createOMElement("echoString", omNs);
        OMElement value = fac.createOMElement("s", omNs);
        value.addChild(fac.createOMText(value, "Hello World"));
        method.addChild(value);
        return method;
    }

    private void webappTest() throws IOException {
        URL webAppURL;
        BufferedReader in;
        boolean webappStatus = false;

        log.info("Invoking webapp on app server service");
        webAppURL = new URL(appServerStratosUrl + "/webapps/SimpleServlet/simple-servlet");
        log.info("Web App URL :" + webAppURL.toString());
        URLConnection yc;
        yc = webAppURL.openConnection();

        in = new BufferedReader(
                new InputStreamReader(
                        yc.getInputStream()));

        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            if (inputLine.indexOf("Hello, World") > 1) {
                webappStatus = true;
                break;
            }
        }

        in.close();

        Assert.assertTrue(webappStatus, "Web app invocation failed");
    }

    private void axis2ServiceTest() throws AxisFault {

        OMElement result;
        OMElement payload = createPayLoad();
        ServiceClient serviceclient = new ServiceClient();
        Options opts = new Options();

        opts.setTo(new EndpointReference(httpAppServerStratosUrl + "/Axis2Service"));
        opts.setAction("http://service.carbon.wso2.org/echoString");
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        serviceclient.setOptions(opts);

        result = serviceclient.sendReceive(payload);
        Assert.assertNotNull(result, "Response message null");
        Assert.assertTrue(result.toString().contains("Hello World"), "Axis2Service invocation failed");

    }


    private void JAXWSTest() throws AxisFault {

        OMElement result;
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://jaxws.carbon.wso2.org", "ns1");
        OMElement payload = fac.createOMElement("echoInt", omNs);
        OMElement value1 = fac.createOMElement("arg0", omNs);
        value1.addChild(fac.createOMText(value1, "1"));
        payload.addChild(value1);


        ServiceClient serviceclient;

        serviceclient = new ServiceClient();
        Options opts = new Options();

        opts.setTo(new EndpointReference(httpAppServerStratosUrl + "/JaxWSTestService"));
        opts.getTo();
        opts.setAction("echoInt");
        //bypass http protocol exception
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);

        serviceclient.setOptions(opts);

        result = serviceclient.sendReceive(payload);
        Assert.assertNotNull(result, "Response message null");
        Assert.assertTrue(result.toString().contains("1"), "Jax-WS service invocation failed");

    }

    private void JARServiceTest() throws AxisFault {
        OMElement result;
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.apache.org/axis2", "ns1");
        OMElement payload = fac.createOMElement("echo", omNs);
        OMElement value = fac.createOMElement("args0", omNs);
        value.addChild(fac.createOMText(value, "Hello-World"));
        payload.addChild(value);

        ServiceClient serviceclient;
        serviceclient = new ServiceClient();
        Options opts = new Options();

        opts.setTo(new EndpointReference(httpAppServerStratosUrl + "/SimpleJarService"));
        opts.setAction("echo");
        //bypass http protocol exception
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);

        serviceclient.setOptions(opts);

        log.info("EPR :" + opts.getTo());
        log.info("Operation :" + opts.getAction());

        result = serviceclient.sendReceive(payload);
        Assert.assertNotNull(result, "Response message is null");
        Assert.assertTrue(result.toString().contains("Hello-World"), "Jar service invocation failed");

    }

    private void springServiceTest() throws AxisFault {
        OMElement result;
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://echo.services.tungsten.wso2.com", "ns1");
        OMElement payload = fac.createOMElement("echoString", omNs);
        OMElement value = fac.createOMElement("in", omNs);
        value.addChild(fac.createOMText(value, "Hello World!"));
        payload.addChild(value);

        ServiceClient serviceclient;
        serviceclient = new ServiceClient();
        Options opts = new Options();

        opts.setTo(new EndpointReference(httpAppServerStratosUrl + "/echoBean"));
        opts.getTo();
        opts.setAction("echoString");
        //bypass http protocol exception
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);

        serviceclient.setOptions(opts);

        result = serviceclient.sendReceive(payload);
        Assert.assertNotNull(result, "Response message is null");
        Assert.assertTrue(result.toString().contains("Hello World!"), "Jar service invocation failed");

    }

    private static void securityTestService(String sessionCookie, String securityAdminServiceURL) {
        OMElement result;
        String serviceEndpoint = "https://appserver.stratoslive.wso2.com/services/t/manualQA0001.org/SimpleService/";
        String clientKeyName = "wso2AloadPolicyutoClient.jks";
        String clientkeyPassword = "Admin123";
        String[] trustedKeyStore = {"wso2carbon.jks"};
        String privateStore = "wso2Autoservice.jks";
        String[] group = {"admin123"};
        String serviceName = "SimpleService";


        try {
            for (int scenarioNum = 1; scenarioNum <= 16; scenarioNum++) {

                AdminServiceSecurity adminServiceSecurity = new AdminServiceSecurity(securityAdminServiceURL);
                adminServiceSecurity.applySecurity(sessionCookie, serviceName, String.valueOf(scenarioNum),
                                                   group, trustedKeyStore, privateStore);

                result = asSecurityUtils.runSecurityClient(scenarioNum, serviceEndpoint, "urn:echo",
                                                           "   <p:echo xmlns:p=\"http://ws.apache.org/axis2\">\n" +
                                                           "      <!--0 to 1 occurrence-->\n" +
                                                           "      <xs:args0 xmlns:xs=\"http://ws.apache.org/axis2\">Hello World, 123 !!!</xs:args0>\n" +
                                                           "   </p:echo>", clientKeyName, clientkeyPassword);
                System.out.println(scenarioNum);
                Assert.assertFalse(!result.toString().contains("Hello World, 123 !!!"),
                                   "Incorrect Test Result: " + result.toString());

                Thread.sleep(5000);
                adminServiceSecurity.disableSecurity(sessionCookie, serviceName);
                Thread.sleep(5000);
            }
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
            log.error("Axis2 Exception login failed" + axisFault.getMessage());
            Assert.fail("Axis2 Exception login failed" + axisFault.getMessage());
        } catch (RemoteException e) {
            e.printStackTrace();
            log.error("RMI exception" + e.getMessage());
            Assert.fail("Securiy invocation failed" + e.getMessage());
        } catch (SecurityAdminServiceSecurityConfigExceptionException e) {
            e.printStackTrace();
            log.error("Security admin services failed" + e.getMessage());
            Assert.fail("Security Admin services failed" + e.getMessage());
        } catch (Exception e1) {
            e1.printStackTrace();
            log.error("Security failed" + e1.getMessage());
            Assert.fail("Security failed" + e1.getMessage());
        }
    }
}



