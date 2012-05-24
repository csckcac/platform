/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.wso2.carbon.appfactory.common;

/**
 * Constants for AppFactory configuration
 */
public class AppFactoryConstants {
    public static final String APPFACTORY_CONFIG_FILE_NAME = "appfactory.xml";
    public static final String APPFACTORY_CONFIG_NAMESPACE = "http://www.wso2.org/appfactory/";

    public static final String APPFACTORY_CONFIG_ROOT_ELEMENT = "AppFactory";

    public static final String APPFACTORY_CONFIG_ADMIN_USER = "AdminUserName";
    public static final String APPFACTORY_CONFIG_ADMIN_PASSWORD = "AdminPassword";

    public static final String APPFACTORY_CONFIG_PROJECT_MGT = "ApplicationMgt";
    public static final String APPFACTORY_CONFIG_PROJECT_USER_ROLES = "DefaultApplicationUserRoles";

    public static final String SSO_CONFIG_ROOT_ELEMENT = "SSORelyingParty";
    public static final String SSO_CONFIG_NAME = "Name";
    public static final String SSO_CONFIG_IDENTITY_PROVIDER_URL = "IdentityProviderURL";
    public static final String SSO_CONFIG_KEY_STORE_PASSWORD = "KeyStorePassword";
    public static final String SSO_CONFIG_IDENTITY_ALIAS = "IdentityKeyAlias";
    public static final String SSO_CONFIG_KEY_STORE_NAME = "KeyStoreName";

    public static final String WEB_SERVICE_CONFIG_ROOT_ELEMENT = "WebServiceEndPoints";
    public static final String WEB_SERVICE_CONFIG_ADD_USER_TO_PROJECT = "AddUserToApplication";
    public static final String WEB_SERVICE_CONFIG_CREATE_USER = "CreateUser";
    public static final String WEB_SERVICE_CONFIG_ACTIVATE_USER = "ActivateUser";
    public static final String WEB_SERVICE_CONFIG_CREATE_REPO = "CreateRepo";
    public static final String WEB_SERVICE_CONFIG_CREATE_PROJECT = "CreateApplication";
    public static final String WEB_SERVICE_CONFIG_GET_ROLES_OF_USER_FOR_PROJECT = "GetRolesOfUserForApplication";
    public static final String WEB_SERVICE_CONFIG_GET_USERS_OF_PROJECT = "GetUsersOfApplication";
    public static final String WEB_SERVICE_CONFIG_EMAIL_VERIFICATION_SERVICE = "EmailVerificationService";
    public static final String WEB_SERVICE_CONFIG_GET_ALL_APPS = "GetAllApps";
    public static final String WEB_SERVICE_CONFIG_GET_AUTH_COOKIE = "GetAuthCookie";


    public static final String REPO_MGT_CONFIG_ROOT_ELEMENT = "RepositoryMGTConfig";
    public static final String SCM_SERVER_IP = "SCMServerIp";
    public static final String SCM_SERVER_PORT = "SCMServerPort";
    public static final String SCM_SERVER_REALM_NAME = "SCMServerRealmName";
    public static final String SCM_SERVER_ADMIN_USER_NAME = "SCMServerAdminUserName";
    public static final String SCM_SERVER_ADMIN_PASSWORD = "SCMServerAdminPassword";
    public static final String SCM_READ_WRITE_PERMISSION_NAME = "ReadWritePermissionName";

    public static final String PROJECT_DEPLOYMENT_ROOT_ELEMENT = "ApplicationDeployment";
    public static final String PROJECT_DEPLOYMENT_STAGE = "DeploymentStage";
    public static final String PROJECT_DEPLOYMENT_SERVER_URL = "DeploymentServerURL";


}
