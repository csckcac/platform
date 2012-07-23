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
    public static final String CONFIG_FOLDER = "appfactory";
    public static final String CONFIG_FILE_NAME = "appfactory.xml";
    public static final String CONFIG_NAMESPACE = "http://www.wso2.org/appfactory/";

    public static final String SERVER_ADMIN_NAME = "AdminUserName";
    public static final String SERVER_ADMIN_PASSWORD = "AdminPassword";
    public static final String DEPLOYMENT_STAGES = "ApplicationDeployment.DeploymentStage";
    public static final String DEPLOYMENT_URL = "DeploymentServerURL";

    public static final String ENDPOINT_DEPLOY_TO_STAGE = "WebServiceEndPoints.DeployToStage";

    public static final String SCM_ADMIN_NAME = "RepositoryMGTConfig.SCMServerAdminUserName";
    public static final String SCM_ADMIN_PASSWORD = "RepositoryMGTConfig.SCMServerAdminPassword";
    public static final String SCM_SERVER_URL = "RepositoryMGTConfig.SCMServerURL";
    public static final String SCM_READ_WRITE_ROLE = "RepositoryMGTConfig.ReadWriteRole";


    public static final String DEFAULT_APPLICATION_USER_ROLE = "ApplicationMgt.DefaultApplicationUserRole";
    public static final String PERMISSION = "Permission";
    public static final String REVISION_CONTROLLER_SERVICE_EPR = "WebServiceEndPoints.RevisionControllerService";

    public static final String REGISTRY_GOVERNANCE_PATH = "/_system/governance";
    public static final String REGISTRY_APPLICATION_PATH = "/repository/applications";

    public static final String APPLICATION_ID = "applicationId";
    public static final String APPLICATION_REVISION = "revision";
    public static final String APPLICATION_VERSION = "version";
    public static final String APPLICATION_STAGE = "stage";
    public static final String APPLICATION_BUILD = "build";

    public static final String TRUNK = "trunk";
    public static final String BRANCH = "branch";
}
