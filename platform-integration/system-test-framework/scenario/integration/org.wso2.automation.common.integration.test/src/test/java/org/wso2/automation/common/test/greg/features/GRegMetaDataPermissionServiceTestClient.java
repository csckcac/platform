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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.carbon.admin.service.AdminServiceResourceAdmin;
import org.wso2.carbon.admin.service.AdminServiceUserMgtService;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;

import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;


public class GRegMetaDataPermissionServiceTestClient {
    private static final Log log = LogFactory.getLog(GRegMetaDataPermissionServiceTestClient.class);
    private AdminServiceUserMgtService userAdminStub;
    private static AdminServiceAuthentication userAuthenticationStub;
    private static AdminServiceResourceAdmin admin_service_resource_admin;
    private EnvironmentVariables gregServer;
    private String gregBackEndUrl;
    private String gregHostName;
    private String sessionCookie;
    private String roleName;
    private String userName;
    private String userPassword;

    @BeforeClass(alwaysRun = true)
    public void init() throws AxisFault {
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(0);
        gregServer = builder.build().getGreg();
        sessionCookie = gregServer.getSessionCookie();
        gregBackEndUrl = gregServer.getBackEndUrl();
        gregHostName = gregServer.getProductVariables().getHostName();
        userAdminStub = new AdminServiceUserMgtService(gregBackEndUrl);
        userAuthenticationStub = new AdminServiceAuthentication(gregBackEndUrl);
        admin_service_resource_admin = new AdminServiceResourceAdmin(gregBackEndUrl);
        roleName = "meta_role";
        userName = "greg_meta_user";

        if (userAdminStub.roleNameExists(roleName, sessionCookie)) {  //delete the role if exists
            userAdminStub.deleteRole(sessionCookie, roleName);
        }

        if (userAdminStub.userNameExists(roleName, sessionCookie, userName)) { //delete user if exists
            userAdminStub.deleteUser(sessionCookie, userName);
        }
    }


    @Test(groups = {"wso2.greg"}, description = "test add a role with login permission",
          priority = 1)
    public void testAddMetaDataPermissionUser()
            throws UserAdminException, RemoteException, ResourceAdminServiceExceptionException {

        userPassword = "welcome";
        String permission1[] = {"/permission/admin/login",
                                "/permission/admin/manage/resources/govern/metadata"};
        String permission2[] = {"/permission/admin/login",
                                "/permission/admin/manage/resources"};
        String userList[] = {"admin"};
        String sessionCookieUser;
        boolean status;
        String resourceName = "echo.wsdl";
        String fetchUrl = "http://people.wso2.com/~evanthika/wsdls/echo.wsdl";
        addRolewithUser(permission1, userList);
        sessionCookieUser = userAuthenticationStub.login(userName, userPassword, gregHostName);
        log.info("Newly Created User Loged in :" + userName);


        try {
            status = false;
            // greg_meta_user does not have permission to add a Text resource
            admin_service_resource_admin.addTextResource(sessionCookieUser, "/", "resource.txt",
                                                         "", "", "");
        } catch (RemoteException e) {
            status = true;
            assertTrue(e.getMessage().indexOf("Access Denied.") > 0, "Access Denied Remote" +
                                                                     " Exception assertion Failed :");
            log.info("greg_login_user does not have permission to add a text resource :");
        }
        assertTrue(status, "Only Login user permission has uploaded a text resource ? :");
        userAuthenticationStub.logOut();
        deleteRoleAndUsers(roleName, userName);
        addRolewithUser(permission2, userList);
        sessionCookieUser = userAuthenticationStub.login(userName, userPassword, gregHostName);
        log.info("Newly Created User Loged in :" + userName);
        admin_service_resource_admin.addWSDL(sessionCookieUser, resourceName, "", fetchUrl);
        admin_service_resource_admin.deleteResource(sessionCookieUser,
                                                    "/_system/governance/trunk/services/" +
                                                    "org/wso2/carbon/core/services/echo/");
        admin_service_resource_admin.deleteResource(sessionCookieUser,
                                                    "/_system/governance/trunk/wsdls/org/" +
                                                    "wso2/carbon/core/services/echo/");
        userAuthenticationStub.logOut();
        deleteRoleAndUsers(roleName, userName);
        log.info("*********GReg Metadata Permission Asigning Scenario test - Passed**********");
    }

    private void addRolewithUser(String[] permission, String[] userList) throws
                                                                         UserAdminException {
        userAdminStub.addRole(roleName, userList, permission, sessionCookie);
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
