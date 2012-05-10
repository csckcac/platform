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
package org.wso2.automation.common.test.manager.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.account.mgt.stub.beans.xsd.AccountInfoBean;
import org.wso2.carbon.account.mgt.stub.services.GetFullnameExceptionException;
import org.wso2.carbon.account.mgt.stub.services.UpdateFullnameExceptionException;
import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.carbon.admin.service.AdminServiceStratosAccountMgt;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;

public class FullNameVerificationTest {

    private static final Log log = LogFactory.getLog(FullNameVerificationTest.class);
    private AdminServiceStratosAccountMgt adminServiceStratosAccountMgt;
    private String sessionCookie;
    private AccountInfoBean oldAccountInfoBean;
    private EnvironmentBuilder builder;


    @BeforeClass(groups = "stratos.manager", description = "initializing properties")
    public void initializeProperties() throws RemoteException, GetFullnameExceptionException,
                                              LoginAuthenticationExceptionException {
        int tenantId = 13;
        builder = new EnvironmentBuilder().manager(tenantId);
        ManageEnvironment manageEnvironment = builder.build();
        sessionCookie = manageEnvironment.getManager().getSessionCookie();
        adminServiceStratosAccountMgt =
                new AdminServiceStratosAccountMgt(manageEnvironment.getManager().getBackEndUrl());
        oldAccountInfoBean = adminServiceStratosAccountMgt.
                getTenantFullName(sessionCookie);
    }

    @Test(groups = "stratos.manager", description = "update tenant full name", priority = 1)
    public void testFullNameUpdate()
            throws UpdateFullnameExceptionException, RemoteException,
                   GetFullnameExceptionException {
        log.info("Runner tenant full name update test");
        //First name and last name to be updated
        String newFirstName = "newFirstName";
        String newLastName = "newLastName";

        AccountInfoBean setAccountInfoBean = new AccountInfoBean();
        //set first name and last name to info bean
        setAccountInfoBean.setFirstname(newFirstName);
        setAccountInfoBean.setLastname(newLastName);

        //update tenant full name
        adminServiceStratosAccountMgt.updateTenantFullName(sessionCookie, setAccountInfoBean);
        assertEquals(newFirstName, adminServiceStratosAccountMgt.getTenantFullName(sessionCookie).
                getFirstname(), "Tenant first Name does not match ");
        assertEquals(newLastName, adminServiceStratosAccountMgt.getTenantFullName(sessionCookie).
                getLastname(), "Tenant last Name does not match ");
        log.info("Tenant full name updated successfully");
    }

    @Test(groups = "stratos.manager", description = "Reset full name", priority = 2)
    public void cleanup()
            throws UpdateFullnameExceptionException, RemoteException,
                   GetFullnameExceptionException {
        //reset to older info bean - First and last names will be rest to previous
        adminServiceStratosAccountMgt.updateTenantFullName(sessionCookie, oldAccountInfoBean);
        log.info("Tenant full name reset to previous one");
        assertEquals(oldAccountInfoBean.getFirstname(), adminServiceStratosAccountMgt.
                getTenantFullName(sessionCookie).getFirstname(), "Tenant first Name does not match ");
        assertEquals(oldAccountInfoBean.getLastname(), adminServiceStratosAccountMgt.
                getTenantFullName(sessionCookie).getLastname(), "Tenant last Name does not match ");
        log.info("Test full name reset");
        log.info("Test full name update test passed....");
    }

    protected static String login(String userName, String password, String hostName)
            throws LoginAuthenticationExceptionException, RemoteException {
        AdminServiceAuthentication loginClient = new AdminServiceAuthentication(hostName);
        return loginClient.login(userName, password, hostName);
    }
}
