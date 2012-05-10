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
package org.wso2.automation.common.test.manager.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.carbon.admin.service.AdminServiceTenantMgtServiceAdmin;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.tenant.mgt.stub.beans.xsd.TenantInfoBean;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.testng.Assert.*;

public class UpdateTenantInfoTest {
    private static final Log log = LogFactory.getLog(UpdateTenantInfoTest.class);
    private static TenantInfoBean tenantInfoBeanGet;
    private static AdminServiceTenantMgtServiceAdmin tenantStub;
    private String sessionCookie;
    private UserInfo userInfo;
    private EnvironmentBuilder builder;
    private ManageEnvironment manageEnvironment;

    @BeforeClass
    public void initializeProperties()
            throws LoginAuthenticationExceptionException, RemoteException {
        int tenantId = 13;
        builder = new EnvironmentBuilder().manager(tenantId);
        manageEnvironment = builder.build();
        tenantStub = new AdminServiceTenantMgtServiceAdmin
                (manageEnvironment.getManager().getBackEndUrl());
        EnvironmentBuilder superBuilder = new EnvironmentBuilder().manager(0);
        ManageEnvironment superManager = superBuilder.build();
        UserInfo superTenantDetails = UserListCsvReader.getUserInfo(0); //get super tenant credential
        sessionCookie = superManager.getManager().getSessionCookie();
        userInfo = UserListCsvReader.getUserInfo(13);
    }

    @Test(groups = "wso2.stratos", description = "update tenant info test", priority = 1)
    public void testTenantInfoUpdate()
            throws LoginAuthenticationExceptionException, RemoteException {
        log.info("Running update tenant info test");
        //get user credentials
        tenantInfoBeanGet = tenantStub.getTenant(sessionCookie, userInfo.getDomain());

        if (log.isDebugEnabled()) {
            log.debug("TenantID " + tenantInfoBeanGet.getTenantId());
            log.debug("Usage plan before update" + tenantInfoBeanGet.getUsagePlan());
        }

        //create calendar object to set tenant created time
        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        //tenant update properties
        String password = "admin123Updated";
        String firstName = "automatedTenantUpdated";
        String updatedLastName = firstName + "wso2automationUpdated";
        String updateEmail = "wso2automation.test.update@wso2.com";
        String usagePlan = "SMB";

        //get tenant admin name
        String adminName =
                userInfo.getDomain().substring(0, userInfo.getUserName().indexOf('@'));
        log.info("Admin Name " + adminName);

        //set updated values to tenantInfoBean
        TenantInfoBean setTenantInfoBean = new TenantInfoBean();
        setTenantInfoBean.setActive(true);
        setTenantInfoBean.setAdminPassword(password);
        setTenantInfoBean.setAdmin(adminName);
        setTenantInfoBean.setEmail(updateEmail);
        setTenantInfoBean.setFirstname(firstName);
        setTenantInfoBean.setLastname(updatedLastName);
        setTenantInfoBean.setUsagePlan(usagePlan);
        setTenantInfoBean.setSuccessKey("true");
        setTenantInfoBean.setCreatedDate(calendar);
        setTenantInfoBean.setTenantDomain(userInfo.getDomain());
        setTenantInfoBean.setTenantId(tenantInfoBeanGet.getTenantId());

        tenantStub.updateTenant(sessionCookie, setTenantInfoBean);


        TenantInfoBean getUpdatedTenantInfoBean =
                tenantStub.getTenant(sessionCookie, userInfo.getDomain());

        assertTrue(getUpdatedTenantInfoBean.getActive(),
                   "Tenant is not active after update");
        assertEquals(tenantInfoBeanGet.getAdmin(),
                     getUpdatedTenantInfoBean.getAdmin(), "Admin user name has changed after update");
        assertEquals(updateEmail, getUpdatedTenantInfoBean.
                getEmail(), "Email address hasn't been updated");
        assertEquals(tenantInfoBeanGet.getFirstname(),
                     getUpdatedTenantInfoBean.getFirstname(), "Tenant first name hasn't been updated");
        assertEquals(tenantInfoBeanGet.getLastname(),
                     getUpdatedTenantInfoBean.getLastname(), "Tenant last name hasn't been updated");
        assertEquals(usagePlan, getUpdatedTenantInfoBean.
                getUsagePlan(), "Tenant usage plan hasn't been updated");
        assertEquals(tenantInfoBeanGet.getTenantId(),
                     getUpdatedTenantInfoBean.getTenantId(), "Tenant Ids are not matching after updation");
        assertEquals(tenantInfoBeanGet.getTenantDomain(),
                     getUpdatedTenantInfoBean.getTenantDomain(), "Tenant Domain are not matching updation");
        assertNotNull(getUpdatedTenantInfoBean.getCreatedDate().getTime(), "Created date is null");

        login(userInfo.getUserName(),
              userInfo.getPassword(), manageEnvironment.getManager().getBackEndUrl());
        log.info("Tenant info updated");
        log.info("Login successful with updated tenant credentials");
    }

    @Test(groups = "wso2.stratos", description = "Reset tenant info", priority = 2)
    public void testResetTenantInfo()
            throws LoginAuthenticationExceptionException, RemoteException {
        //Reset tenant info to older values
        TenantInfoBean setTenantInfoBean = new TenantInfoBean();
        setTenantInfoBean.setActive(true);
        setTenantInfoBean.setAdminPassword(userInfo.getPassword());
        setTenantInfoBean.setAdmin(tenantInfoBeanGet.getAdmin());
        setTenantInfoBean.setEmail(tenantInfoBeanGet.getEmail());
        setTenantInfoBean.setFirstname(tenantInfoBeanGet.getFirstname());
        setTenantInfoBean.setLastname(tenantInfoBeanGet.getLastname());
        setTenantInfoBean.setUsagePlan(tenantInfoBeanGet.getUsagePlan());
        setTenantInfoBean.setSuccessKey("true");
        setTenantInfoBean.setTenantDomain(tenantInfoBeanGet.getTenantDomain());
        setTenantInfoBean.setTenantId(tenantInfoBeanGet.getTenantId());
        log.info("Tenant info reset to default begins");
        tenantStub.updateTenant(sessionCookie, setTenantInfoBean);
        log.info("Tenant inf reset to default");

        //tenant login check after tenant info reset
        login(userInfo.getUserName(),
              userInfo.getPassword(), manageEnvironment.getManager().getBackEndUrl());
        log.info("Tenant login successful with default credentials");
    }

    protected static String login(String userName, String password, String hostName)
            throws LoginAuthenticationExceptionException, RemoteException {
        AdminServiceAuthentication loginClient = new AdminServiceAuthentication(hostName);
        return loginClient.login(userName, password, hostName);
    }
}
