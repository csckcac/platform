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
package org.wso2.automation.common.test.greg.features;



import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.*;
import org.wso2.carbon.admin.service.AdminServiceUserMgtService;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkSettings;

import java.rmi.RemoteException;


public class RolePermissionServiceTestClient {
    private static final Log log = LogFactory.getLog(RolePermissionServiceTestClient.class);
    private AdminServiceUserMgtService userAdminStub;
    private EnvironmentVariables gregServer;
    private String gregBackEndUrl;
    private String sessionCookie;
    private String roleName;
    private String userName;
    private String userPassword;


    @BeforeClass(alwaysRun = true)
    public void init() throws RemoteException, LoginAuthenticationExceptionException {
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(3);
        gregServer = builder.build().getGreg();
        sessionCookie = gregServer.getSessionCookie();
        gregBackEndUrl = gregServer.getBackEndUrl();
        userAdminStub = new AdminServiceUserMgtService(gregBackEndUrl);
    }

    @Test(groups = {"wso2.greg"}, description = "test add a role with login permission",
          priority = 1)
    private void testAddLoginPermission() throws UserAdminException {
        roleName = "login";
        userName = "greguser1";
        userPassword = "greguser1";
        String permission[] = {"/permission/admin/login"};
        String userList[] = {"admin"};

        try {
            addRolewithUser(permission, userList);

            deleteRoleAndUsers(roleName, userName);
            log.info("********GReg Create a Role with only Login privilege test - passed ********");
        } catch (UserAdminException e) {
            log.error("Failed to add login Role with User :" + e.getMessage());
            throw new UserAdminException("Failed to add login Role with User :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test add a role with configure permission",
          priority = 2)
    private void testAddConfigurePermission() throws UserAdminException {
        roleName = "configure";
        userName = "greguser2";
        userPassword = "greguser2";
        String permission[] = {"/permission/admin/configure"};
        String userList[] = {"admin"};

        try {
            addRolewithUser(permission, userList);

            deleteRoleAndUsers(roleName, userName);
            log.info("*******GReg Create a Role with only configure permission  test - passed ***");
        } catch (UserAdminException e) {
            log.error("Failed to add configure permission with User :" + e.getMessage());
            throw new UserAdminException("Failed to add configure permission with User :" +
                                         e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test add a role with manage permission",
          priority = 3)
    private void testAddManagePermission() throws UserAdminException {
        roleName = "manage";
        userName = "greguser3";
        userPassword = "greguser3";
        String permission[] = {"/permission/admin/manage"};
        String userList[] = {"admin"};

        try {
            addRolewithUser(permission, userList);

            deleteRoleAndUsers(roleName, userName);
            log.info("*******GReg Create a Role with only manage permission  test - passed ***");
        } catch (UserAdminException e) {
            log.error("Failed to add manage permission with User :" + e.getMessage());
            throw new UserAdminException("Failed to add manage permission with User :" +
                                         e.getMessage());
        }

    }

    @Test(groups = {"wso2.greg"}, description = "test add a role with monitor permission",
          priority = 4)
    public void testAddMonitorPermission() throws UserAdminException {
        roleName = "monitor";
        userName = "greguser4";
        userPassword = "greguser4";
        String permission[] = {"/permission/admin/monitor"};
        String userList[] = {"admin"};

        try {
            addRolewithUser(permission, userList);

            deleteRoleAndUsers(roleName, userName);
            log.info("*******GReg Create a Role with only monitor permission  test - passed ***");
        } catch (UserAdminException e) {
            log.error("Failed to add monitor permission with User :" + e.getMessage());
            throw new UserAdminException("Failed to add monitor permission with User :" +
                                         e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test add a role with multiple permission",
          priority = 5)
    private void testAddMultiplePermissions() throws UserAdminException {
        roleName = "mulitpermission";
        userName = "greguser5";
        userPassword = "greguser5";
        String permission[] = {"/permission/admin/login", "/permission/admin/configure",
                               "/permission/admin/manage", "/permission/admin/monitor"};
        String userList[] = {"admin"};

        try {
            addRolewithUser(permission, userList);

            deleteRoleAndUsers(roleName, userName);
            log.info("*******GReg Create a Role with only monitor permission  test - passed ***");
        } catch (UserAdminException e) {
            log.error("Failed to add monitor permission with User :" + e.getMessage());
            throw new UserAdminException("Failed to add monitor permission with User :" +
                                         e.getMessage());
        }

    }

    @Test(groups = {"wso2.greg"}, description = "test add a role with super admin permission",
          priority = 6)
    private void testAddSuperAdminPermission() throws UserAdminException {
        roleName = "superadmin";
        userName = "greguser6";
        userPassword = "greguser6";
        String permission[] = {"/permission/super admin/configure"};
        String userList[] = {"admin"};

        try {
            addRolewithUser(permission, userList);

            deleteRoleAndUsers(roleName, userName);
            log.info("*******GReg Create a Role with super admin permission  test - passed ***");
        } catch (UserAdminException e) {
            log.error("Failed to add super admin permission with User :" + e.getMessage());
            throw new UserAdminException("Failed to add super admin permission with User :" +
                                         e.getMessage());
        }

    }

    private void addRolewithUser(String[] permission, String[] userList) throws UserAdminException {
        userAdminStub.addRole(roleName, null , permission, sessionCookie);
        log.info("Successfully added Role :" + roleName);

        String roles[] = {roleName};
        userAdminStub.addUser(sessionCookie, userName, userPassword, roles, null);
        log.info("Successfully User Crated :" + userName);
    }


    private void deleteRoleAndUsers(String roleName, String userName) {
        userAdminStub.deleteRole(sessionCookie, roleName);
        log.info("Role " + roleName + " deleted successfully");
        userAdminStub.deleteUser(sessionCookie, userName);
    }


}
