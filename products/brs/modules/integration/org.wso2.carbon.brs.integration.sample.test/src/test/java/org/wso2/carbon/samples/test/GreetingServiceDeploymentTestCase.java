///*
//*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//*
//*  WSO2 Inc. licenses this file to you under the Apache License,
//*  Version 2.0 (the "License"); you may not use this file except
//*  in compliance with the License.
//*  You may obtain a copy of the License at
//*
//*    http://www.apache.org/licenses/LICENSE-2.0
//*
//* Unless required by applicable law or agreed to in writing,
//* software distributed under the License is distributed on an
//* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//* KIND, either express or implied.  See the License for the
//* specific language governing permissions and limitations
//* under the License.
//*/
//package org.wso2.carbon.samples.test;
//
//import org.apache.axiom.om.OMElement;
//import org.apache.axiom.om.impl.builder.StAXOMBuilder;
//import org.apache.axis2.AxisFault;
//import org.apache.axis2.addressing.EndpointReference;
//import org.apache.axis2.client.Options;
//import org.apache.axis2.client.ServiceClient;
//import org.testng.annotations.AfterClass;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
//import org.wso2.carbon.integration.framework.ClientConnectionUtil;
//import org.wso2.carbon.integration.framework.LoginLogoutUtil;
//import org.wso2.carbon.rule.ws.stub.fileupload.RuleServiceFileUploadAdminStub;
//
//import java.lang.Exception;
//import java.rmi.RemoteException;
//
//
//import javax.xml.stream.XMLStreamException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.Scanner;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//
//import junit.framework.Assert;
//
//
//import javax.activation.DataHandler;
//import javax.activation.FileDataSource;
//import javax.xml.stream.XMLStreamException;
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.lang.String;
//
//import org.wso2.carbon.samples.test.greetingService.greeting.*;
//
//import static org.testng.Assert.assertNotNull;
//import static org.testng.Assert.assertNotEquals;
//
//public class GreetingServiceDeploymentTestCase {
//
//    private static final Log log = LogFactory.getLog(GreetingServiceDeploymentTestCase.class);
//    private LoginLogoutUtil util = new LoginLogoutUtil();
//    private RuleServiceFileUploadAdminStub ruleServiceFileUploadAdminStub;
//
//    @BeforeClass(groups = {"wso2.brs"})
//    public void login() throws Exception {
//        ClientConnectionUtil.waitForPort(9443);
//        String loggedInSessionCookie = util.login();
//        ruleServiceFileUploadAdminStub =
//                new RuleServiceFileUploadAdminStub("https://localhost:9443/services/RuleServiceFileUploadAdmin");
//        ServiceClient client = ruleServiceFileUploadAdminStub._getServiceClient();
//        Options options = client.getOptions();
//        options.setManageSession(true);
//        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
//                loggedInSessionCookie);
//    }
//
//    @AfterClass(groups = {"wso2.brs"})
//    public void logout() throws Exception {
//        ClientConnectionUtil.waitForPort(9443);
//        util.logout();
//    }
//
//    @Test(groups = {"wso2.brs"})
//    public void uploadGreetingService() throws Exception {
//        String samplesDir = System.getProperty("samples.dir");
//        String greetingServiceAAR = samplesDir + File.separator + "greeting.service/target/GreetingService.aar";
//
//        System.out.println(greetingServiceAAR);
//        FileDataSource fileDataSource = new FileDataSource(greetingServiceAAR);
//        DataHandler dataHandler = new DataHandler(fileDataSource);
//        ruleServiceFileUploadAdminStub.uploadService("GreetingService.aar", dataHandler);
//
//    }
//
//    @Test(groups = {"wso2.brs"}, dependsOnMethods = {"uploadGreetingService"})
//    public void callGreet() throws XMLStreamException, AxisFault {
//
//        String result = "";
//        waitForProcessDeployment("http://localhost:9763/services/GreetingService");
//
//        try {
//            GreetingServiceStub greetingServiceStub = new GreetingServiceStub("http://localhost:9763/services/GreetingService");
//
//            UserE userRequest = new UserE();
//            User user = new User();
//            user.setName("Ishara");
//            User[] users = new User[1];
//            users[0] = user;
//            userRequest.setUser(users);
//
//            GreetingMessage[] greetingMessages = greetingServiceStub.greetMe(users);
//            result = greetingMessages[0].getMessage();
//            System.out.println("Greeting service results1 : " + result);
//
//        } catch (AxisFault axisFault) {
//            axisFault.printStackTrace();
//            assertNotNull(null);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//            assertNotNull(null);
//        }
//        System.out.println("Greeting service results2 : " + result);
//        assertNotNull(result, "Result cannot be null");
//        assertNotEquals(result, "");
//
//
//    }
//
//
//    public static void waitForProcessDeployment(String serviceUrl) {
//        int serviceTimeOut = 0;
//        while (!isServiceAvailable(serviceUrl)) {
//            //TODO - this looping is only happening for 14 times. Need to find the exact cause for this.
//            if (serviceTimeOut == 0) {
//                log.info("Waiting for the " + serviceUrl + ".");
//            } else if (serviceTimeOut > 20) {
//                log.error("Time out");
//                Assert.fail(serviceUrl + " service is not found");
//                break;
//            }
//            try {
//                Thread.sleep(5000);
//                serviceTimeOut++;
//            } catch (InterruptedException e) {
//                Assert.fail(e.getMessage());
//            }
//        }
//    }
//
//
//    public static boolean isServiceAvailable(String serviceUrl) {
//        URL wsdlURL;
//        InputStream wsdlIS = null;
//        try {
//            wsdlURL = new URL(serviceUrl + "?wsdl");
//
//            try {
//                wsdlIS = wsdlURL.openStream();
//            } catch (IOException e) {
//                return false;// do nothing, wait for the service
//            }
//
//            if (wsdlIS != null) {
//
//                String wsdlContent = convertStreamToString(wsdlIS);
//                if (wsdlContent.indexOf("definitions") > 0) {
//                    return true;
//                }
//                return false;
//            }
//            return false;
//
//
//        } catch (MalformedURLException e) {
//            return false;
//        } finally {
//            if (wsdlIS != null) {
//                try {
//                    wsdlIS.close();
//                } catch (IOException e) {
//                    log.error("Error occurred when closing the wsdl input stream");
//                }
//            }
//        }
//
//    }
//
//    public static String convertStreamToString(InputStream is) {
//        return new Scanner(is).useDelimiter("\\A").next();
//    }
//
//
//}
