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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Model to represent the appfactory.xml
 */
public class AppFactoryConfiguration {
    private String ssoRelyingPartyName;
    private String ssoIdentityProviderEpr;
    private String ssoKeyStorePassword;
    private String ssoIdentityAlias;
    private String ssoKeyStoreName;
    private String webServiceEPRAddUserToProject;
    private String webServiceEPRCreateProject;
    private String webServiceEPRCreateRepo;
    private String webServiceEPRGetRolesOfUserForProject;
    private String webServiceEPRGetUsersOfProject;
    private String webServiceEPREmailVerificationService;
    private String webServiceEPRCreateUser;
    private String webServiceEPRActivateUser;
    private String adminUserName;
    private String adminPassword;
    private String scmServerIp;
    private String scmServerPort;
    private String scmServerRealmName;
    private String scmServerAdminUserName;
    private String scmServerAdminPassword;
    private String scmReadWritePermissionName;
    private Map<String, List<String>> deploymentServerUrls;
    private Set<String> defaultProjectUserRoles;

    public AppFactoryConfiguration() {
        deploymentServerUrls = new HashMap<String, List<String>>();
        defaultProjectUserRoles = new HashSet<String>();
    }

    public String getSsoRelyingPartyName() {
        return ssoRelyingPartyName;
    }

    public void setSsoRelyingPartyName(String ssoRelyingPartyName) {
        this.ssoRelyingPartyName = ssoRelyingPartyName;
    }

    public String getSsoIdentityProviderEpr() {
        return ssoIdentityProviderEpr;
    }

    public void setSsoIdentityProviderEpr(String ssoIdentityProviderEpr) {
        this.ssoIdentityProviderEpr = ssoIdentityProviderEpr;
    }

    public String getSsoKeyStorePassword() {
        return ssoKeyStorePassword;
    }

    public void setSsoKeyStorePassword(String ssoKeyStorePassword) {
        this.ssoKeyStorePassword = ssoKeyStorePassword;
    }

    public String getSsoIdentityAlias() {
        return ssoIdentityAlias;
    }

    public void setSsoIdentityAlias(String ssoIdentityAlias) {
        this.ssoIdentityAlias = ssoIdentityAlias;
    }

    public String getSsoKeyStoreName() {
        return ssoKeyStoreName;
    }

    public void setSsoKeyStoreName(String ssoKeyStoreName) {
        this.ssoKeyStoreName = ssoKeyStoreName;
    }

    public String getWebServiceEPRAddUserToProject() {
        return webServiceEPRAddUserToProject;
    }

    public void setWebServiceEPRAddUserToProject(String webServiceEPRAddUserToProject) {
        this.webServiceEPRAddUserToProject = webServiceEPRAddUserToProject;
    }

    public String getWebServiceEPRCreateProject() {
        return webServiceEPRCreateProject;
    }

    public void setWebServiceEPRCreateProject(String webServiceEPRCreateProject) {
        this.webServiceEPRCreateProject = webServiceEPRCreateProject;
    }

    public String getWebServiceEPRCreateRepo() {
        return webServiceEPRCreateRepo;
    }

    public void setWebServiceEPRCreateRepo(String webServiceEPRCreateRepo) {
        this.webServiceEPRCreateRepo = webServiceEPRCreateRepo;
    }

    public String getWebServiceEPRGetRolesOfUserForProject() {
        return webServiceEPRGetRolesOfUserForProject;
    }

    public void setWebServiceEPRGetRolesOfUserForProject(
            String webServiceEPRGetRolesOfUserForProject) {
        this.webServiceEPRGetRolesOfUserForProject = webServiceEPRGetRolesOfUserForProject;
    }

    public String getWebServiceEPRGetUsersOfProject() {
        return webServiceEPRGetUsersOfProject;
    }

    public void setWebServiceEPRGetUsersOfProject(String webServiceEPRGetUsersOfProject) {
        this.webServiceEPRGetUsersOfProject = webServiceEPRGetUsersOfProject;
    }

    public String getWebServiceEPREmailVerificationService() {
        return webServiceEPREmailVerificationService;
    }

    public void setWebServiceEPREmailVerificationService(
            String webServiceEPREmailVerificationService) {
        this.webServiceEPREmailVerificationService = webServiceEPREmailVerificationService;
    }

    public String getWebServiceEPRCreateUser() {
        return webServiceEPRCreateUser;
    }

    public void setWebServiceEPRCreateUser(String webServiceEPRCreateUser) {
        this.webServiceEPRCreateUser = webServiceEPRCreateUser;
    }

    public String getWebServiceEPRActivateUser() {
        return webServiceEPRActivateUser;
    }

    public void setWebServiceEPRActivateUser(String webServiceEPRActivateUser) {
        this.webServiceEPRActivateUser = webServiceEPRActivateUser;
    }

    public String getAdminUserName() {
        return adminUserName;
    }

    public void setAdminUserName(String adminUserName) {
        this.adminUserName = adminUserName;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getScmServerIp() {
        return scmServerIp;
    }

    public void setScmServerIp(String scmServerIp) {
        this.scmServerIp = scmServerIp;
    }

    public String getScmServerPort() {
        return scmServerPort;
    }

    public void setScmServerPort(String scmServerPort) {
        this.scmServerPort = scmServerPort;
    }

    public String getScmServerRealmName() {
        return scmServerRealmName;
    }

    public void setScmServerRealmName(String scmServerRealmName) {
        this.scmServerRealmName = scmServerRealmName;
    }

    public String getScmServerAdminUserName() {
        return scmServerAdminUserName;
    }

    public void setScmServerAdminUserName(String scmServerAdminUserName) {
        this.scmServerAdminUserName = scmServerAdminUserName;
    }

    public String getScmServerAdminPassword() {
        return scmServerAdminPassword;
    }

    public void setScmServerAdminPassword(String scmServerAdminPassword) {
        this.scmServerAdminPassword = scmServerAdminPassword;
    }

    public String getScmReadWritePermissionName() {
        return scmReadWritePermissionName;
    }

    public void setScmReadWritePermissionName(String scmReadWritePermissionName) {
        this.scmReadWritePermissionName = scmReadWritePermissionName;
    }

    public void addDeploymentServerUrls(String stage, List<String> locations) {
        deploymentServerUrls.put(stage, locations);
    }

    public List<String> getDeploymentServerUrls(String stage) {
        return deploymentServerUrls.get(stage);
    }

    public Set<String> getDefaultProjectUserRoles() {
        return defaultProjectUserRoles;
    }

    public void addDefaultProjectUserRole(String[] roles) {
        for (String role : roles) {
            if (role != null && !("".equals(role.trim()))) {
                defaultProjectUserRoles.add(role);
            }
        }
    }
}
