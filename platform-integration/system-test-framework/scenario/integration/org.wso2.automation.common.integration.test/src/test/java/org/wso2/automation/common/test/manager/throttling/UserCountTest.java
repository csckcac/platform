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

package org.wso2.automation.common.test.manager.throttling;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.carbon.admin.service.AdminServiceBillingDataAccessService;
import org.wso2.carbon.admin.service.AdminServiceUserMgtService;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class UserCountTest {
    private static final Log log = LogFactory.getLog(UserCountTest.class);
    private AdminServiceUserMgtService userAdminStub;
    private AdminServiceBillingDataAccessService billingDataAccessStub;
    private String sessionCookie;
    private String sessionCookie_billingStub;
    private String roleName;
    private String userName;
    int tenantUserCount = 20;
    private String userPassword;
    private UserInfo tenantAdminDetails;
    private ManageEnvironment environment;

    @BeforeClass
    public void init() throws AxisFault {
        int tenantId = 13;
        EnvironmentBuilder builder = new EnvironmentBuilder().is(tenantId).manager(tenantId);
        environment = builder.build();
        userAdminStub = new AdminServiceUserMgtService(environment.getIs().getBackEndUrl());
        billingDataAccessStub =
                new AdminServiceBillingDataAccessService(environment.getManager().getBackEndUrl());
        sessionCookie = environment.getIs().getSessionCookie();
        sessionCookie_billingStub = environment.getManager().getSessionCookie();
        userName = "wso2automationUser";
        roleName = "wso2automationRole3";
        userPassword = "wso2automationPassword";
        tenantAdminDetails = UserListCsvReader.getUserInfo(tenantId);
    }

    @Test(groups = "wso2.stratos", description = "Test for user count throttling " +
                                                 "plan with different usage plans",
          priority = 1)
    public void testUserManagementTest() throws Exception {
        //delete user and roles, if exists
        deleteRoleAndUsers();
        //update usage plan to demo and add 20 users
        usagePlanUpdate(ProductConstant.MULTITENANCY_FREE_PLAN);
        addRoleWithUser();
        deleteRoleAndUsers();
        //update usage plan to SMB and add 20 users
        usagePlanUpdate(ProductConstant.MULTITENANCY_SMALL_PLAN);
        addRoleWithUser();
        deleteRoleAndUsers();
        //update usage plan to Professional and add 20 users
        usagePlanUpdate(ProductConstant.MULTITENANCY_MEDIUM_PLAN);
        addRoleWithUser();
        deleteRoleAndUsers();
        //update usage plan to Enterprise and add 20 users
        usagePlanUpdate(ProductConstant.MULTITENANCY_LARGE_PLAN);
        addRoleWithUser();
        deleteRoleAndUsers();
    }

    @Test(groups = "wso2.stratos", description = "Reset usage plan to demo", priority = 2)
    public void resetUsagePlan() throws Exception {
        usagePlanUpdate(ProductConstant.MULTITENANCY_FREE_PLAN); //Reset usage plan to demo
    }

    private void usagePlanUpdate(String usagePlan) throws Exception {
        assertTrue(billingDataAccessStub.updateUsagePlan(sessionCookie_billingStub,
                                                         tenantAdminDetails.getDomain(),
                                                         usagePlan), "Usage plan update fail");
        assertEquals(usagePlan,
                     billingDataAccessStub.getUsagePlanName(sessionCookie_billingStub,
                                                            tenantAdminDetails.getDomain()),
                     "Usage plan doesn't get updated");
        log.info("Usage plan has been updated to " + usagePlan);
    }


    private void addRoleWithUser() throws UserAdminException {
        String permission[] = {"/permission/admin/login"};
        String userList[] = {"admin123"};
        int tenantUserCount = 20;

        //add role with login permission before adding user list
        userAdminStub.addRole(roleName, userList, permission, sessionCookie);
        log.info("Role added successfully");
        String roles[] = {roleName};

        for (int userCount = 0; userCount < tenantUserCount; userCount++) {
            userAdminStub.addUser(sessionCookie, userName + userCount, userPassword + userCount,
                                  roles, null);
            login(userName + userCount + "@" + tenantAdminDetails.getDomain(),
                  userPassword + userCount, environment.getIs().getBackEndUrl());
            log.debug("User " + userName + userCount + " logged in successfully");
        }
    }


    private void deleteRoleAndUsers()
            throws LoginAuthenticationExceptionException, RemoteException {
        userAdminStub.deleteRole(sessionCookie, roleName);
        log.info("Role " + roleName + " deleted successfully");
        for (int userCount = 0; userCount < tenantUserCount; userCount++) {
            userAdminStub.deleteUser(sessionCookie, userName + userCount);
            log.debug("User " + userName + userCount + " deleted successfully");
            try {
                //login after user deletion
                log.debug("Try login after user deletion");
                unsuccessfulLogin(userName + userCount + "@" +
                                  tenantAdminDetails.getDomain(), userPassword + userCount,
                                  environment.getIs().getBackEndUrl());
            } catch (AxisFault axisFault) {
                log.info("Already deleted uses cannot login");
            }
        }
    }

    protected static String login(String userName, String password, String hostName) {
        AdminServiceAuthentication loginClient = new AdminServiceAuthentication(hostName);
        return loginClient.login(userName, password, hostName);
    }

    private boolean unsuccessfulLogin(String userName, String password, String hostName)
            throws LoginAuthenticationExceptionException, RemoteException {
        AdminServiceAuthentication loginClient = new AdminServiceAuthentication(hostName);
        return loginClient.unsuccessfulLogin(userName, password, hostName);
    }


}
