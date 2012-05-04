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
package org.wso2.automation.common.test.is;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.carbon.admin.service.AdminServiceUserMgtService;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import java.rmi.RemoteException;

public class AuthorizationCachingTest {

    private static final Log log = LogFactory.getLog(AuthorizationCachingTest.class);
    private AdminServiceUserMgtService userAdminStub;
    private UserInfo userInfo;
    private String sessionCookie;
    private String roleName;
    private String userName;
    private String userPassword;
    private EnvironmentBuilder builder;
    private ManageEnvironment environment;

    @BeforeClass
    public void initializeProperties()
            throws LoginAuthenticationExceptionException, RemoteException {
        builder = new EnvironmentBuilder().is(10);
        environment = builder.build();
        userInfo = UserListCsvReader.getUserInfo(10);
        sessionCookie = environment.getIs().getSessionCookie();
        userName = "wso2automationUser103";
        roleName = "admin";
        userPassword = "wso2automationPassword";
        userAdminStub = new AdminServiceUserMgtService(environment.getIs().getBackEndUrl());
    }

    @Test(groups = "wso2.is", description = "authorization cashing test", priority = 1)
    public void testAuthorizationCaching()
            throws UserAdminException, LoginAuthenticationExceptionException, RemoteException {
        log.info("Running Authorization cashing test");
        deleteUsers(); //delete the user if exists
        //Create user and add him to admin role
        userAdminStub.addUser(sessionCookie, userName, userPassword, new String[]{roleName}, null);
        userAdminStub.updateUserListOfRole(roleName, new String[]{userName}, null); //Assign admin role
        login(userName + "@" + userInfo.getDomain(), userPassword, environment.getIs().getBackEndUrl());
        log.debug("User " + userName + " logged in successfully");

        //remove user from admin role
        userAdminStub.updateUserListOfRole(roleName, null, new String[]{userName});
        loginAfterRoleRemoval();

        //Assign admin role to the user again
        userAdminStub.updateUserListOfRole(roleName, new String[]{userName}, null);
        //login after role update
        login(userName + "@" + userInfo.getDomain(), userPassword, environment.getIs().getBackEndUrl());
        log.debug("User " + userName + " logged in successfully after role update");
    }

    private void loginAfterRoleRemoval() {
        //login after role deletion
        log.info("Try login after role deletion");
        try {
            unsuccessfulLogin(userName + "@" + userInfo.getDomain(), userPassword,
                              environment.getIs().getBackEndUrl());
        } catch (LoginAuthenticationExceptionException e) {  //handling the exceptions intentionally
            log.info("Deleted users cannot login");
        } catch (RemoteException e) {
            log.info("Deleted users cannot login");
        }
    }


    @AfterClass
    public void deleteUsers() {
        userAdminStub.deleteUser(sessionCookie, userName);
        //login after user deletion
        log.info("Try login after user deletion");
        try {
            unsuccessfulLogin(userName + "@" + userInfo.getDomain(), userPassword,
                              environment.getIs().getBackEndUrl());
        } catch (LoginAuthenticationExceptionException e) {  //handling the exceptions intentionally
            log.info("Deleted users cannot login");
        } catch (RemoteException e) {
            log.info("Deleted users cannot login");
        }
    }

    protected static String login(String userName, String password, String hostName)
            throws LoginAuthenticationExceptionException, RemoteException {
        AdminServiceAuthentication loginClient = new AdminServiceAuthentication(hostName);
        return loginClient.login(userName, password, hostName);
    }

    private boolean unsuccessfulLogin(String userName, String password, String hostName)
            throws LoginAuthenticationExceptionException, RemoteException {
        AdminServiceAuthentication loginClient = new AdminServiceAuthentication(hostName);
        return loginClient.unsuccessfulLogin(userName, password, hostName);
    }
}
