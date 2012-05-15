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
    public static final String PROJECT_DEPLOYMENT_CONFIG_ROOT_ELEMENT = "ProjectDeploymentConfig";
    public static final String PROJECT_DEPLOYMENT_CONFIG_STAGE = "Stage";
    public static final String PROJECT_DEPLOYMENT_CONFIG_SERVER_URL = "DeploymentServerURL";

    public static final String WEB_SERVICE_CONFIG_ROOT_ELEMENT = "webService";
    public static final String WEB_SERVICE_CONFIG_ADD_USER_TO_PROJECT = "addUserToProject";
    public static final String WEB_SERVICE_CONFIG_CREATE_PROJECT = "createProject";
    public static final String WEB_SERVICE_CONFIG_GET_ROLES_OF_USER_FOR_PROJECT = "getRolesOfUserForProject";
    public static final String WEB_SERVICE_CONFIG_GET_USERS_OF_PROJECT = "getUsersOfProject";
    public static final String WEB_SERVICE_CONFIG_EMAIL_VERIFICATION_SERVICE = "emailVerificationService";


    public static final String BPEL_CONFIG_ROOT_ELEMENT = "bpel";
    public static final String BPEL_CONFIG_CREATE_USER = "crateUser";
    public static final String BPEL_CONFIG_ACTIVATE_USER = "activateUser";

    public static final String SVN_REPO_MGT_CONFIG_ROOT_ELEMENT = "SVNRepositoryMGTConfig";
    public static final String SCM_SERVER_IP = "SCMServerIp";
    public static final String SCM_SERVER_PORT = "SCMServerPort";
    public static final String SCM_SERVER_REALM_NAME = "SCMServerRealmName";
    public static final String SCM_SERVER_ADMIN_USER_NAME = "SCMServerAdminUserName";
    public static final String SCM_SERVER_ADMIN_PASSWORD = "SCMServerAdminPassword";
    public static final String SCM_READ_WRITE_PERMISSION_NAME = "ReadWritePermissionName";

    public static final String ADMIN_USER_NAME_CONFIG_ROOT_ELEMENT = "adminUserName";
    public static final String ADMIN_PASSWORD_CONFIG_ROOT_ELEMENT = "adminPassword";

    public static final String SSO_CONFIG_ROOT_ELEMENT = "sso";
    public static final String SSO_CONFIG_NAME = "name";
    public static final String SSO_CONFIG_IDENTITY_PROVIDER_URL = "identityProviderURL";
    public static final String SSO_CONFIG_KEY_STORE_PASSWORD = "keyStorePassword";
    public static final String SSO_CONFIG_IDENTITY_ALIAS = "identityAlias";
    public static final String SSO_CONFIG_KEY_STORE_NAME = "keyStoreName";


}
