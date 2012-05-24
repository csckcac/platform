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
    private String webServiceEPRAddUserToApplication;
    private String webServiceEPRCreateApplication;
    private String webServiceEPRCreateRepo;
    private String webServiceEPRGetRolesOfUserForApplication;
    private String webServiceEPRGetUsersOfApplication;
    private String webServiceEPREmailVerificationService;
    private String webServiceEPRCreateUser;
    private String webServiceEPRGetAllApps;
    private String webServiceEPRGetAuthCookie;
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
    private Set<String> defaultApplicationUserRoles;

    public AppFactoryConfiguration() {
        deploymentServerUrls = new HashMap<String, List<String>>();
        defaultApplicationUserRoles = new HashSet<String>();
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

    public String getWebServiceEPRAddUserToApplication() {
        return webServiceEPRAddUserToApplication;
    }

    public void setWebServiceEPRAddUserToApplication(String webServiceEPRAddUserToApplication) {
        this.webServiceEPRAddUserToApplication = webServiceEPRAddUserToApplication;
    }

    public String getWebServiceEPRCreateApplication() {
        return webServiceEPRCreateApplication;
    }

    public void setWebServiceEPRCreateApplication(String webServiceEPRCreateApplication) {
        this.webServiceEPRCreateApplication = webServiceEPRCreateApplication;
    }

    public String getWebServiceEPRCreateRepo() {
        return webServiceEPRCreateRepo;
    }

    public void setWebServiceEPRCreateRepo(String webServiceEPRCreateRepo) {
        this.webServiceEPRCreateRepo = webServiceEPRCreateRepo;
    }

    public String getWebServiceEPRGetRolesOfUserForApplication() {
        return webServiceEPRGetRolesOfUserForApplication;
    }

    public void setWebServiceEPRGetRolesOfUserForApplication(
            String webServiceEPRGetRolesOfUserForApplication) {
        this.webServiceEPRGetRolesOfUserForApplication = webServiceEPRGetRolesOfUserForApplication;
    }

    public String getWebServiceEPRGetUsersOfApplication() {
        return webServiceEPRGetUsersOfApplication;
    }

    public void setWebServiceEPRGetUsersOfApplication(String webServiceEPRGetUsersOfApplication) {
        this.webServiceEPRGetUsersOfApplication = webServiceEPRGetUsersOfApplication;
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

    public Set<String> getDefaultApplicationUserRoles() {
        return defaultApplicationUserRoles;
    }

    public void addDefaultApplicationUserRole(String[] roles) {
        for (String role : roles) {
            if (role != null && !("".equals(role.trim()))) {
                defaultApplicationUserRoles.add(role);
            }
        }
    }

    public String getWebServiceEPRGetAllApps() {
        return webServiceEPRGetAllApps;
    }

    public void setWebServiceEPRGetAllApps(String webServiceEPRGetAllApps) {
        this.webServiceEPRGetAllApps = webServiceEPRGetAllApps;
    }

    public String getWebServiceEPRGetAuthCookie() {
        return webServiceEPRGetAuthCookie;
    }

    public void setWebServiceEPRGetAuthCookie(String webServiceEPRGetAuthCookie) {
        this.webServiceEPRGetAuthCookie = webServiceEPRGetAuthCookie;
    }
}
