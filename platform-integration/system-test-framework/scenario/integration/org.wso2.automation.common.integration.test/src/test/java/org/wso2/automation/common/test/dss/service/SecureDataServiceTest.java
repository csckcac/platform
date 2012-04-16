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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigExceptionException;
import org.wso2.platform.test.core.utils.axis2client.SecureAxisServiceClient;
import org.wso2.automation.common.test.dss.utils.DataServiceUtility;
import org.wso2.automation.common.test.dss.utils.DataServiceTest;

import java.rmi.RemoteException;

public class SecureDataServiceTest extends DataServiceTest {

    private static final Log log = LogFactory.getLog(SecureDataServiceTest.class);

    @Override
    protected void setServiceName() {
        serviceName = "SecureDataService";
    }


    @Test(priority = 1, dependsOnMethods = {"serviceDeployment"}, description = "Provides Authentication." +
                                                                   " Clients have Username Tokens")
    public void securityPolicy1() throws Exception {
        final int policyId = 1;
        //todo this sleep should be removed after fixing CARBON-11900 gira
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Assert.fail("InterruptedException :" + e.getMessage());

        }
        this.secureService(policyId);
        SecureAxisServiceClient secureAxisServiceClient = new SecureAxisServiceClient();
        serviceEndPoint = DataServiceUtility.getServiceEndpointHttps(sessionCookie, dssBackEndUrl, serviceName);
        OMElement response;
        for (int i = 0; i < 5; i++) {
            response = secureAxisServiceClient.sendReceive(userInfo.getUserName(), userInfo.getPassword(),
                                                           serviceEndPoint, "showAllOffices",
                                                           getPayload(), policyId);
            Assert.assertTrue(response.toString().contains("<Office>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<officeCode>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<city>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<phone>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("</Office>"), "Expected Result not Found");
        }
        log.info("UsernameToken verified");
    }

    @Test(priority = 2, dependsOnMethods = {"serviceDeployment"})
    public void securityPolicy2() throws Exception {
        final int policyId = 2;
        this.secureService(policyId);
        SecureAxisServiceClient secureAxisServiceClient = new SecureAxisServiceClient();
        serviceEndPoint = DataServiceUtility.getServiceEndpointHttp(sessionCookie, dssBackEndUrl, serviceName);
        OMElement response;
        for (int i = 0; i < 5; i++) {
            response = secureAxisServiceClient.sendReceive(userInfo.getUserName(), userInfo.getPassword(),
                                                           serviceEndPoint, "showAllOffices",
                                                           getPayload(), policyId);
            Assert.assertTrue(response.toString().contains("<Office>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<officeCode>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<city>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<phone>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("</Office>"), "Expected Result not Found");
        }
        log.info("Non-repudiation verified");
    }

    @Test(priority = 3, dependsOnMethods = {"serviceDeployment"})
    public void securityPolicy3() throws Exception {
        final int policyId = 3;
        this.secureService(policyId);
        SecureAxisServiceClient secureAxisServiceClient = new SecureAxisServiceClient();
        serviceEndPoint = DataServiceUtility.getServiceEndpointHttp(sessionCookie, dssBackEndUrl, serviceName);
        OMElement response;
        for (int i = 0; i < 5; i++) {
            response = secureAxisServiceClient.sendReceive(userInfo.getUserName(), userInfo.getPassword(),
                                                           serviceEndPoint, "showAllOffices",
                                                           getPayload(), policyId);
            Assert.assertTrue(response.toString().contains("<Office>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<officeCode>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<city>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<phone>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("</Office>"), "Expected Result not Found");
        }
        log.info("Integrity verified");
    }

    @Test(priority = 4, dependsOnMethods = {"serviceDeployment"})
    public void securityPolicy4() throws Exception {
        final int policyId = 4;
        this.secureService(policyId);
        SecureAxisServiceClient secureAxisServiceClient = new SecureAxisServiceClient();
        serviceEndPoint = DataServiceUtility.getServiceEndpointHttp(sessionCookie, dssBackEndUrl, serviceName);
        OMElement response;
        for (int i = 0; i < 5; i++) {
            response = secureAxisServiceClient.sendReceive(userInfo.getUserName(), userInfo.getPassword(),
                                                           serviceEndPoint, "showAllOffices",
                                                           getPayload(), policyId);
            Assert.assertTrue(response.toString().contains("<Office>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<officeCode>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<city>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<phone>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("</Office>"), "Expected Result not Found");
        }
        log.info("Confidentiality verified");
    }

    @Test(priority = 5, dependsOnMethods = {"serviceDeployment"})
    public void securityPolicy5() throws Exception {
        final int policyId = 5;
        this.secureService(policyId);
        SecureAxisServiceClient secureAxisServiceClient = new SecureAxisServiceClient();
        serviceEndPoint = DataServiceUtility.getServiceEndpointHttp(sessionCookie, dssBackEndUrl, serviceName);
        OMElement response;
        for (int i = 0; i < 5; i++) {
            response = secureAxisServiceClient.sendReceive(userInfo.getUserName(), userInfo.getPassword(),
                                                           serviceEndPoint, "showAllOffices",
                                                           getPayload(), policyId);
            Assert.assertTrue(response.toString().contains("<Office>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<officeCode>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<city>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<phone>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("</Office>"), "Expected Result not Found");
        }
        log.info("Sign and encrypt - X509 Authentication verified");
    }

    @Test(priority = 6, dependsOnMethods = {"serviceDeployment"})
    public void securityPolicy6() throws Exception {
        final int policyId = 6;
        this.secureService(policyId);
        SecureAxisServiceClient secureAxisServiceClient = new SecureAxisServiceClient();
        serviceEndPoint = DataServiceUtility.getServiceEndpointHttp(sessionCookie, dssBackEndUrl, serviceName);
        OMElement response;
        for (int i = 0; i < 5; i++) {
            response = secureAxisServiceClient.sendReceive(userInfo.getUserName(), userInfo.getPassword(),
                                                           serviceEndPoint, "showAllOffices",
                                                           getPayload(), policyId);
            Assert.assertTrue(response.toString().contains("<Office>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<officeCode>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<city>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<phone>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("</Office>"), "Expected Result not Found");
        }
        log.info("Sign and Encrypt - Anonymous clients verified");
    }

    @Test(priority = 7, dependsOnMethods = {"serviceDeployment"})
    public void securityPolicy7() throws Exception {
        final int policyId = 7;
        this.secureService(policyId);
        SecureAxisServiceClient secureAxisServiceClient = new SecureAxisServiceClient();
        serviceEndPoint = DataServiceUtility.getServiceEndpointHttp(sessionCookie, dssBackEndUrl, serviceName);
        OMElement response;
        for (int i = 0; i < 5; i++) {
            response = secureAxisServiceClient.sendReceive(userInfo.getUserName(), userInfo.getPassword(),
                                                           serviceEndPoint, "showAllOffices", getPayload(),
                                                           policyId);
            Assert.assertTrue(response.toString().contains("<Office>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<officeCode>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<city>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<phone>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("</Office>"), "Expected Result not Found");
        }
        log.info("Encrypt only - Username Token Authentication verified");
    }

    @Test(priority = 8, dependsOnMethods = {"serviceDeployment"})
    public void securityPolicy8() throws Exception {
        final int policyId = 8;
        this.secureService(policyId);
        SecureAxisServiceClient secureAxisServiceClient = new SecureAxisServiceClient();
        serviceEndPoint = DataServiceUtility.getServiceEndpointHttp(sessionCookie, dssBackEndUrl, serviceName);
        OMElement response;
        for (int i = 0; i < 5; i++) {
            response = secureAxisServiceClient.sendReceive(userInfo.getUserName(), userInfo.getPassword(),
                                                           serviceEndPoint, "showAllOffices",
                                                           getPayload(), policyId);
            Assert.assertTrue(response.toString().contains("<Office>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<officeCode>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<city>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<phone>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("</Office>"), "Expected Result not Found");
        }
        log.info("Sign and Encrypt - Username Token Authentication verified");
    }

    @Test(priority = 9, dependsOnMethods = {"serviceDeployment"})
    public void securityPolicy9() throws Exception {
        final int policyId = 9;
        this.secureService(policyId);
        SecureAxisServiceClient secureAxisServiceClient = new SecureAxisServiceClient();
        serviceEndPoint = DataServiceUtility.getServiceEndpointHttp(sessionCookie, dssBackEndUrl, serviceName);
        OMElement response;
        for (int i = 0; i < 5; i++) {
            response = secureAxisServiceClient.sendReceive(userInfo.getUserName(), userInfo.getPassword(),
                                                           serviceEndPoint, "showAllOffices",
                                                           getPayload(), policyId);
            Assert.assertTrue(response.toString().contains("<Office>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<officeCode>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<city>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<phone>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("</Office>"), "Expected Result not Found");
        }
        log.info("SecureConversation - Sign only - Service as STS - Bootstrap policy - Sign and Encrypt ," +
                 " X509 Authentication verified");
    }

    @Test(priority = 10, dependsOnMethods = {"serviceDeployment"})
    public void securityPolicy10() throws Exception {
        final int policyId = 10;
        this.secureService(policyId);
        SecureAxisServiceClient secureAxisServiceClient = new SecureAxisServiceClient();
        serviceEndPoint = DataServiceUtility.getServiceEndpointHttp(sessionCookie, dssBackEndUrl, serviceName);
        OMElement response;
        for (int i = 0; i < 5; i++) {
            response = secureAxisServiceClient.sendReceive(userInfo.getUserName(), userInfo.getPassword(),
                                                           serviceEndPoint, "showAllOffices",
                                                           getPayload(), policyId);
            Assert.assertTrue(response.toString().contains("<Office>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<officeCode>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<city>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<phone>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("</Office>"), "Expected Result not Found");
        }
        log.info("SecureConversation - Encrypt only - Service as STS - Bootstrap policy - Sign and Encrypt ," +
                 " X509 Authentication verified");
    }

    @Test(priority = 11, dependsOnMethods = {"serviceDeployment"})
    public void securityPolicy11() throws Exception {
        final int policyId = 11;
        this.secureService(policyId);
        SecureAxisServiceClient secureAxisServiceClient = new SecureAxisServiceClient();
        serviceEndPoint = DataServiceUtility.getServiceEndpointHttp(sessionCookie, dssBackEndUrl, serviceName);
        OMElement response;
        for (int i = 0; i < 5; i++) {
            response = secureAxisServiceClient.sendReceive(userInfo.getUserName(), userInfo.getPassword(),
                                                           serviceEndPoint, "showAllOffices",
                                                           getPayload(), policyId);
            Assert.assertTrue(response.toString().contains("<Office>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<officeCode>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<city>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<phone>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("</Office>"), "Expected Result not Found");
        }
        log.info("SecureConversation - Sign and Encrypt - Service as STS - Bootstrap policy - Sign and Encrypt , X509 Authentication verified");
    }

    @Test(priority = 12, dependsOnMethods = {"serviceDeployment"})
    public void securityPolicy12() throws Exception {
        final int policyId = 12;
        this.secureService(policyId);
        SecureAxisServiceClient secureAxisServiceClient = new SecureAxisServiceClient();
        serviceEndPoint = DataServiceUtility.getServiceEndpointHttp(sessionCookie, dssBackEndUrl, serviceName);
        OMElement response;
        for (int i = 0; i < 5; i++) {
            response = secureAxisServiceClient.sendReceive(userInfo.getUserName(), userInfo.getPassword(),
                                                           serviceEndPoint, "showAllOffices",
                                                           getPayload(), policyId);
            Assert.assertTrue(response.toString().contains("<Office>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<officeCode>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<city>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<phone>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("</Office>"), "Expected Result not Found");
        }
        log.info("SecureConversation - Sign Only - Service as STS - Bootstrap policy - Sign and Encrypt ," +
                 " Anonymous clients verified");
    }

    @Test(priority = 13, dependsOnMethods = {"serviceDeployment"})
    public void securityPolicy13() throws Exception {
        final int policyId = 13;
        this.secureService(policyId);
        SecureAxisServiceClient secureAxisServiceClient = new SecureAxisServiceClient();
        serviceEndPoint = DataServiceUtility.getServiceEndpointHttp(sessionCookie, dssBackEndUrl, serviceName);
        OMElement response;
        for (int i = 0; i < 5; i++) {
            response = secureAxisServiceClient.sendReceive(userInfo.getUserName(), userInfo.getPassword(),
                                                           serviceEndPoint, "showAllOffices",
                                                           getPayload(), policyId);
            Assert.assertTrue(response.toString().contains("<Office>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<officeCode>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<city>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<phone>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("</Office>"), "Expected Result not Found");
        }
        log.info("SecureConversation - Sign and Encrypt - Service as STS - Bootstrap policy - " +
                 "Sign and Encrypt , Anonymous clients verified");
    }

    @Test(priority = 14, dependsOnMethods = {"serviceDeployment"})
    public void securityPolicy14() throws Exception {
        final int policyId = 14;
        this.secureService(policyId);
        SecureAxisServiceClient secureAxisServiceClient = new SecureAxisServiceClient();
        serviceEndPoint = DataServiceUtility.getServiceEndpointHttp(sessionCookie, dssBackEndUrl, serviceName);
        OMElement response;
        for (int i = 0; i < 5; i++) {
            response = secureAxisServiceClient.sendReceive(userInfo.getUserName(),userInfo.getPassword(),
                                                           serviceEndPoint, "showAllOffices",
                                                           getPayload(), policyId);
            Assert.assertTrue(response.toString().contains("<Office>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<officeCode>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<city>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<phone>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("</Office>"), "Expected Result not Found");
        }
        log.info("SecureConversation - Encrypt Only - Service as STS - Bootstrap policy - " +
                 "Sign and Encrypt , Username Token Authentication verified");
    }

    @Test(priority = 15, dependsOnMethods = {"serviceDeployment"})
    public void securityPolicy15() throws Exception {
        final int policyId = 15;
        this.secureService(policyId);
        SecureAxisServiceClient secureAxisServiceClient = new SecureAxisServiceClient();
        serviceEndPoint = DataServiceUtility.getServiceEndpointHttp(sessionCookie, dssBackEndUrl, serviceName);
        OMElement response;
        for (int i = 0; i < 5; i++) {
            response = secureAxisServiceClient.sendReceive(userInfo.getUserName(),
                                                           userInfo.getPassword(), serviceEndPoint,
                                                           "showAllOffices", getPayload(), policyId);
            Assert.assertTrue(response.toString().contains("<Office>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<officeCode>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<city>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<phone>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("</Office>"), "Expected Result not Found");
        }
        log.info("SecureConversation - Sign and Encrypt - Service as STS - Bootstrap policy - " +
                 "Sign and Encrypt , Username Token Authentication verified");
    }

///*@Test(dependsOnMethods = {"uploadArtifactTest"})
//    public void securityPolicy16() {
//        this.secureService(16);
//        SecureAxisServiceClient secureAxisServiceClient = new SecureAxisServiceClient();
//        serviceEndPoint = DataServiceUtility.getServiceEndpointHttp(sessionCookie, dssBackEndUrl, serviceName);
//        OMElement response;
//        for (int i = 0; i < 5; i++) {
//            response = secureAxisServiceClient.sendReceive(userInfo.getUserName(), userInfo.getPassword(), serviceEndPoint, "showAllOffices", getPayload(), 16);
//            Assert.assertTrue("Expected Result not Found", (response.toString().indexOf("<Office>") > 1));
//            Assert.assertTrue("Expected Result not Found", (response.toString().indexOf("</Office>") > 1));
//        }
//    log.info("Kerberos Authentication - Sign - Sign based on a Kerberos Token verified");
//    }*/


    private void secureService(int policyId)
            throws SecurityAdminServiceSecurityConfigExceptionException, RemoteException {
        if (environment.is_runningOnStratos()) {
            adminServiceClientDSS.applySecurity(sessionCookie, serviceName, policyId + "", new String[]{"admin"}, new String[]{userInfo.getDomain().replace('.', '-') + ".jks"}, userInfo.getDomain().replace('.', '-') + ".jks");
        } else {
            adminServiceClientDSS.applySecurity(sessionCookie, serviceName, policyId + "", new String[]{"admin"}, new String[]{"wso2carbon.jks"}, "wso2carbon.jks");
        }
        log.info("Security Scenario " + policyId + " Applied");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Assert.fail("InterruptedException :" + e.getMessage());

        }
    }

    private OMElement getPayload() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice/samples/secure_dataservice", "ns1");
        return fac.createOMElement("showAllOffices", omNs);
    }
}
