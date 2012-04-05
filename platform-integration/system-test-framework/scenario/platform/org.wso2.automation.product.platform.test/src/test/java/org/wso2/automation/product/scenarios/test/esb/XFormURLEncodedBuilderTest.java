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

package org.wso2.automation.product.scenarios.test.esb;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceTracerAdmin;
import org.wso2.carbon.tracer.stub.types.carbon.MessagePayload;
import org.wso2.carbon.tracer.stub.types.carbon.TracerServiceInfo;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClientUtils;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;
import org.wso2.platform.test.core.utils.httpclient.HttpClientUtil;

import static org.testng.Assert.assertTrue;

/*
When sending a post request with content-type  "application/x-www-form-urlencoded", the message builder
(org.apache.axis2.builder.XFormURLEncodedBuilder) drops the body, this test indented to test the fix provided.
*/
public class XFormURLEncodedBuilderTest {
    private static final Log log = LogFactory.getLog(XFormURLEncodedBuilderTest.class);
    private String SERVICE_EPR;
    private String PROXY_EPR;
    private static AdminServiceTracerAdmin soapTracerAdmin;
    private static TracerServiceInfo tracerServiceInfo;

    @BeforeTest
    public void testInitialize() {
        EnvironmentBuilder builder;
        builder = new EnvironmentBuilder().as(1).esb(1);
        ManageEnvironment environment = builder.build();
        SERVICE_EPR = environment.getAs().getServiceUrl() + "/HelloService";
        PROXY_EPR = environment.getEsb().getServiceUrl() + "/SimplePassThoughProxy";
        soapTracerAdmin = new AdminServiceTracerAdmin(environment.getAs().getBackEndUrl(),
                                                      environment.getAs().getSessionCookie());

    }


    @Test(groups = "wso2.esb", description = "Test to verity XFormURLEncodedBuilder, " +
                                             "when sending POST " + "request with content type " +
                                             "application/x-www-form-urlencoded", priority = 1)
    public void testPostRequest() throws Exception {
        tracerServiceInfo = soapTracerAdmin.setMonitoring("ON"); //set soap tracker on
        assertTrue(tracerServiceInfo.getFlag().equals("ON"), "Soap tracker set to off");
        soapTracerAdmin.clearAllSoapMessages(); //empty soup tracer
        AxisServiceClientUtils.waitForServiceDeployment(SERVICE_EPR);
        AxisServiceClientUtils.waitForServiceDeployment(PROXY_EPR);
        String toEpr = PROXY_EPR + "/greet";
        HttpClientUtil httpClient = new HttpClientUtil();
        String params = "name=HelloAutomation"
                        + "&" + "age=" + "12"
                        + "&" + "address=no35a";
        int responseCode = httpClient.postWithContentType(toEpr, params,
                                                          "application/x-www-form-urlencoded");
        assert responseCode == 0 : "response code is null";
        assertEquals(200, responseCode, "Response Code not 200");
        tracerServiceInfo = soapTracerAdmin.getMessages(200, "greet");
        MessagePayload messagePayload = tracerServiceInfo.getLastMessage();
        assertTrue(messagePayload.getRequest().contains("name"), "Name tag not found in the service request");
        assertTrue(messagePayload.getRequest().contains("HelloAutomation"),
                   "HelloAutomation tag not found in the service request");
        log.info("Post request test passed..");
    }

    @Test(groups = "wso2.esb", description = "Test to verity XFormURLEncodedBuilder, " +
                                             "when sending GET " + "request with content type " +
                                             "application/x-www-form-urlencoded", priority = 2)
    public void testGetRequest() throws Exception {
        tracerServiceInfo = soapTracerAdmin.setMonitoring("ON"); //set soap tracker on
        assertTrue(tracerServiceInfo.getFlag().equals("ON"), "Soap tracker set to off");
        soapTracerAdmin.clearAllSoapMessages(); //empty soup tracer
        String toEpr = PROXY_EPR + "/greet";
        HttpClientUtil httpClient = new HttpClientUtil();
        String params = "name=HelloAutomation"
                        + "&" + "age=" + "12"
                        + "&" + "address=no35a";
        OMElement response = httpClient.getWithContentType(toEpr, params, "application/x-www-form-urlencoded");
        assertTrue(response.toString().contains("Hello World, HelloAutomation !!!"),
                   "Response does not contain expect result");
        tracerServiceInfo = soapTracerAdmin.getMessages(200, "greet");
        MessagePayload messagePayload = tracerServiceInfo.getLastMessage();
        assertTrue(messagePayload.getRequest().contains("name"), "Name tag not found in the service request");
        assertTrue(messagePayload.getRequest().contains("HelloAutomation"), "HelloAutomation tag not " +
                                                                            "found in the service request");
        log.info("GET request test passed..");


    }
}
