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

package org.wso2.platform.test.core;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.carbon.admin.service.AdminServiceResourceAdmin;
import org.wso2.carbon.admin.service.AdminServiceTenantMgtServiceAdmin;
import org.wso2.carbon.admin.service.AdminServiceUserMgtService;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
//import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceExceptionException;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceExceptionException;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.ClusterReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkSettings;

import java.rmi.RemoteException;
import java.util.List;

public class UserPopulator {
    private static final Log log = LogFactory.getLog(UserPopulator.class);
    private AdminServiceUserMgtService userMgtAdmin;
    private boolean isUsersPopulated = false;
    EnvironmentBuilder env;
    FrameworkSettings framework;

    UserPopulator() {
        env = new EnvironmentBuilder();
        framework = env.getFrameworkSettings();
    }

    public void populateUsers(List<String> productList)
            throws UserAdminException, RemoteException, LoginAuthenticationExceptionException,
                   TenantMgtAdminServiceExceptionException {
        FrameworkProperties manProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.MANAGER_SERVER_NAME);
        log.info("Populating Users....");

        if (!isUsersPopulated) {
            if (framework.getEnvironmentSettings().is_runningOnStratos()) {
                AdminServiceTenantMgtServiceAdmin tenantStub =
                        new AdminServiceTenantMgtServiceAdmin(manProperties.getProductVariables().
                                getBackendUrl());
                UserInfo superTenantDetails = UserListCsvReader.getUserInfo(0);
                int userCount = UserListCsvReader.getUserCount();
                createStratosUsers(tenantStub, superTenantDetails, userCount);
                log.info("Users Populated");
            } else {
                int adminUserId = 0;
                UserInfo adminDetails = UserListCsvReader.getUserInfo(adminUserId);
                String[] permissions = {"/permission/"};
                String[] userList = null;
                ClusterReader clusterReader = new ClusterReader();
                if (framework.getEnvironmentSettings().isClusterEnable()) {
                    clusterReader.getClusterList();
                    for (String id : clusterReader.getClusterList()) {
                        if (productList.contains(clusterReader.getProductName(id).toUpperCase())) {
                            FrameworkProperties properties = FrameworkFactory.getClusterProperties(id);
                            String backendURL = properties.getProductVariables().getBackendUrl();
                            String hostName = properties.getProductVariables().getHostName();
                            userMgtAdmin = new AdminServiceUserMgtService(backendURL);
                            log.info("Populate users to " + id + " server");
                            createProductUsers(adminDetails, permissions, userList, backendURL, hostName);
                        }
                    }
                } else {
                    for (String product : productList) {
                        FrameworkProperties properties = FrameworkFactory.getFrameworkProperties(product);
                        String backendURL = properties.getProductVariables().getBackendUrl();
                        String hostName = properties.getProductVariables().getHostName();
                        userMgtAdmin = new AdminServiceUserMgtService(backendURL);
                        log.info("Populate user to " + product + " server");
                        createProductUsers(adminDetails, permissions, userList, backendURL, hostName);
                    }
                }
            }
            //users are populated. user population disabled
            isUsersPopulated = true;
//        }
        }
    }

    private void createProductUsers(UserInfo adminDetails, String[] permissions, String[] userList,
                                    String backendUrl,
                                    String hostName)
            throws UserAdminException, RemoteException, LoginAuthenticationExceptionException {
        String sessionCookieUser = login(adminDetails.getUserName(), adminDetails.getPassword(), backendUrl, hostName);
        AdminServiceResourceAdmin resourceAdmin = new AdminServiceResourceAdmin(backendUrl);
        String[] roleName = {"testRole"};
        int roleNameIndex = 0;

        try {
            if (!userMgtAdmin.roleNameExists(roleName[roleNameIndex], sessionCookieUser)) {
                userMgtAdmin.addRole(roleName[roleNameIndex], userList, permissions, sessionCookieUser);
                resourceAdmin.addResourcePermission(sessionCookieUser, "/", "testRole", "3", "1");
                resourceAdmin.addResourcePermission(sessionCookieUser, "/", "testRole", "2", "1");
                resourceAdmin.addResourcePermission(sessionCookieUser, "/", "testRole", "4", "1");
                resourceAdmin.addResourcePermission(sessionCookieUser, "/", "testRole", "5", "1");
                log.info("Role " + roleName[roleNameIndex] + " was created successfully");
                log.info("Role " + roleName[roleNameIndex] + " was created successfully");
            }
        } catch (UserAdminException e) {
            log.error("Unable to add Role :" + e);
            throw new UserAdminException("Unable to add Role :" + e);
        } catch (AxisFault axisFault) {
            log.error("Unable assign registry permission to the role :", axisFault);
            throw new AxisFault("Unable assign registry permission to the role :", axisFault);
        }

        for (int userId = 0; userId < UserListCsvReader.getUserCount(); userId++) {
            if (userId != 0) {
                String userId_str = Integer.toString(userId);
                int userIdValue = UserListCsvReader.getUserId(userId_str);
                UserInfo userDetails = UserListCsvReader.getUserInfo(userIdValue);
                try {
                    if (!userMgtAdmin.userNameExists(roleName[roleNameIndex],
                                                     sessionCookieUser, userDetails.getUserName())) {
                        userMgtAdmin.addUser(sessionCookieUser, userDetails.getUserName(),
                                             userDetails.getUserName(), roleName, null);
                        log.info("User " + userDetails.getUserName() + " was created successfully");
                    }
                } catch (UserAdminException e) {
                    log.error("Unable to add users :" + e);
                    throw new UserAdminException("Unable to add role :" + e);
                }
            }
        }
    }

    private void createStratosUsers(AdminServiceTenantMgtServiceAdmin tenantStub,
                                    UserInfo superTenantDetails, int userCount)
            throws LoginAuthenticationExceptionException, RemoteException,
                   TenantMgtAdminServiceExceptionException {
        FrameworkProperties manProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.MANAGER_SERVER_NAME);
        String sessionCookie =
                login(superTenantDetails.getUserName(), superTenantDetails.getPassword(),
                      manProperties.getProductVariables().getBackendUrl(),
                      manProperties.getProductVariables().getHostName());

        for (int userId = 0; userId < userCount; userId++) {
            if (userId != 0) {
                String userId_str = Integer.toString(userId);
                int tenantId = UserListCsvReader.getUserId(userId_str);
                UserInfo tenantDetails = UserListCsvReader.getUserInfo(tenantId);
                tenantStub.addTenant(sessionCookie, tenantDetails.getDomain(),
                                     tenantDetails.getPassword(), "admin123", "free");
            }
        }
    }

    protected static String login(String userName, String password, String backendUrl,
                                  String hostName)
            throws RemoteException, LoginAuthenticationExceptionException {
        AdminServiceAuthentication loginClient = new AdminServiceAuthentication(backendUrl);
        return loginClient.login(userName, password, hostName);
    }


}
