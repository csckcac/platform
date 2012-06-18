/*
 * Copyright 2011-2012 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.appserver.integration.tests;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceExceptionException;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceStub;
import org.wso2.carbon.tenant.mgt.stub.beans.xsd.TenantInfoBean;
import org.wso2.carbon.utils.FileManipulator;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Calendar;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class TenantMgtTestCase {
    private static final String USER_NAME = "test";
    private static final String PASS_WORD = "test123";
    private static final String FIRST_NAME = "test";
    private static final String LAST_NAME = "test";
    private static final String DOMAIN = "test.org";
    private static final String EMAIL = "test@test.org";
    private static final String USAGE_PLAN = "Demo";

    private static final String EXAMPLE_WEBAPP_URL = "http://localhost:9763/t/" + DOMAIN +
                                                     "/webapps/example/";
    private HttpClient httpClient = new HttpClient();
    private LoginLogoutUtil util = new LoginLogoutUtil();
    private TenantMgtAdminServiceStub tenantMgtStub;
    private ConfigurationContext configContext;

    private static final Log log = LogFactory.getLog(TenantMgtTestCase.class);


    @BeforeMethod(groups = {"wso2.as"})
    public void init() throws Exception {
        configContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(
                        System.getProperty("carbon.home") + File.separator + "repository" +
                        File.separator + "deployment" + File.separator + "client",
                        System.getProperty("carbon.home") +
                        File.separator + "repository" + File.separator + "conf" +
                        File.separator + "axis2" + File.separator + "axis2_client.xml");
        String loggedInSessionCookie = util.login();

        String EPR = "https://" + FrameworkSettings.HOST_NAME +
                     ":" + FrameworkSettings.HTTPS_PORT + "/services/TenantMgtAdminService";
        tenantMgtStub = new TenantMgtAdminServiceStub(configContext, EPR);
        ServiceClient client = tenantMgtStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                           loggedInSessionCookie);
    }

    @Test(groups = {"wso2.as"})
    public void testTenantMgt() throws TenantMgtAdminServiceExceptionException, RemoteException {
        testTenantAddition();
        testWebappDeployement();
    }

    private void testTenantAddition()
            throws TenantMgtAdminServiceExceptionException, RemoteException {
        log.info("Running tenant addition test case ...");
        TenantInfoBean tenantInfoBean = new TenantInfoBean();
        tenantInfoBean.setAdmin(USER_NAME);
        tenantInfoBean.setFirstname(FIRST_NAME);
        tenantInfoBean.setLastname(LAST_NAME);
        tenantInfoBean.setAdminPassword(PASS_WORD);
        tenantInfoBean.setTenantDomain(DOMAIN);
        tenantInfoBean.setEmail(EMAIL);
        tenantInfoBean.setUsagePlan(USAGE_PLAN);
        tenantInfoBean.setCreatedDate(Calendar.getInstance());
        tenantMgtStub.addTenant(tenantInfoBean);
    }


    private void testWebappDeployement() {
        log.info("Running webapp deployment test for tenant : " + DOMAIN);
        String sourcePath = System.getProperty("carbon.home") +
                            File.separator + "repository" + File.separator + "deployment" +
                            File.separator + "server" + File.separator + "webapps" +
                            File.separator + "example.war";

        String destPath = System.getProperty("carbon.home") +
                          File.separator + "repository" + File.separator + "tenants" +
                          File.separator + "1" + File.separator + "webapps" +
                          File.separator + "example.war";
        File sourceFile = new File(sourcePath);
        File destFile = new File(destPath);
        try {
            FileManipulator.copyFile(sourceFile, destFile);
        } catch (IOException e) {
            log.error("Error while copying the example webapp sample into tenant " +
                      DOMAIN + "'s space", e);
            fail("Webapp Deployment for tenant : " + DOMAIN + " has failed");
        }

        HttpClientParams params = new HttpClientParams();
        params.setParameter("http.protocol.allow-circular-redirects", true);
        httpClient.setParams(params);
        String url = EXAMPLE_WEBAPP_URL + "carbon/authentication/login.jsp?username=" +
                     USER_NAME + "&password=" + PASS_WORD;
        GetMethod getMethod = new GetMethod(url);
        try {
            log.info("Authenticating test user with carbon user realm");
            int statusCode = httpClient.executeMethod(getMethod);
            int noOfTry = 1;
            while (statusCode != HttpStatus.SC_OK && noOfTry < 10) {
                Thread.sleep(1000);
                noOfTry++;
                statusCode = httpClient.executeMethod(getMethod);
            }
            if (noOfTry == 10) {
                fail("Webapp deployment for tenant : " + DOMAIN + " was not successful");
            }
            if (statusCode == HttpStatus.SC_OK) {
                boolean success = Boolean.
                        parseBoolean(getMethod.getResponseHeader("logged-in").getValue());
                if (success) {
                    String username = getMethod.getResponseHeader("username").getValue();
                    assertEquals(username, USER_NAME);
                } else {
                    fail("Webapp testing for tenant :" + DOMAIN + " failed");
                }
            }

        } catch (Exception e) {
            log.error("Error occurred while trying test webapp for tenant : " + DOMAIN + " : " +
                      e.getMessage());
            fail("Caught exception " + e.toString());
        } finally {
            getMethod.releaseConnection();
        }
    }
}
