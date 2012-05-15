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
import java.util.List;
import java.util.Map;

/**
 * Model to represent the appfactory.xml
 */
public class AppFactoryConfiguration {
    private String sSOName;
    private String sSOIdentityProviderEPR;
    private String sSOKeyStorePassword;
    private String sSOIdentityAlias;
    private String sSOKeyStoreName;
    private String webServiceEPRAddUserToProject;
    private String webServiceEPRCreateProject;
    private String webServiceEPRCreateRepo;
    private String webServiceEPRGetRolesOfUserForProject;
    private String webServiceEPRGetUsersOfProject;
    private String webServiceEPREmailVarificationService;
    private String bpelEPRCreateUser;
    private String bpelEPRActivateUser;
    private String adminUserName;
    private String adminPassword;
    private String sCMServerIp;
    private String sCMServerPort;
    private String sCMServerRealmName;
    private String sCMServerAdminUserName;
    private String sCMServerAdminPassword;
    private String sCMReadWritePermissionName;
    private String svnBaseURL;
    private Map<String, List<String>> deploymentServerLocations;

    public AppFactoryConfiguration() {
        deploymentServerLocations = new HashMap<String, List<String>>();
    }

    public String getsSOName() {
        return sSOName;
    }

    public void setsSOName(String sSOName) {
        this.sSOName = sSOName;
    }

    public String getsSOIdentityProviderEPR() {
        return sSOIdentityProviderEPR;
    }

    public void setsSOIdentityProviderEPR(String sSOIdentityProviderEPR) {
        this.sSOIdentityProviderEPR = sSOIdentityProviderEPR;
    }

    public String getsSOKeyStorePassword() {
        return sSOKeyStorePassword;
    }

    public void setsSOKeyStorePassword(String sSOKeyStorePassword) {
        this.sSOKeyStorePassword = sSOKeyStorePassword;
    }

    public String getsSOIdentityAlias() {
        return sSOIdentityAlias;
    }

    public void setsSOIdentityAlias(String sSOIdentityAlias) {
        this.sSOIdentityAlias = sSOIdentityAlias;
    }

    public String getsSOKeyStoreName() {
        return sSOKeyStoreName;
    }

    public void setsSOKeyStoreName(String sSOKeyStoreName) {
        this.sSOKeyStoreName = sSOKeyStoreName;
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

    public String getWebServiceEPREmailVarificationService() {
        return webServiceEPREmailVarificationService;
    }

    public void setWebServiceEPREmailVarificationService(
            String webServiceEPREmailVarificationService) {
        this.webServiceEPREmailVarificationService = webServiceEPREmailVarificationService;
    }

    public String getBpelEPRCreateUser() {
        return bpelEPRCreateUser;
    }

    public void setBpelEPRCreateUser(String bpelEPRCreateUser) {
        this.bpelEPRCreateUser = bpelEPRCreateUser;
    }

    public String getBpelEPRActivateUser() {
        return bpelEPRActivateUser;
    }

    public void setBpelEPRActivateUser(String bpelEPRActivateUser) {
        this.bpelEPRActivateUser = bpelEPRActivateUser;
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

    public String getsCMServerIp() {
        return sCMServerIp;
    }

    public void setsCMServerIp(String sCMServerIp) {
        this.sCMServerIp = sCMServerIp;
    }

    public String getsCMServerPort() {
        return sCMServerPort;
    }

    public void setsCMServerPort(String sCMServerPort) {
        this.sCMServerPort = sCMServerPort;
    }

    public String getsCMServerRealmName() {
        return sCMServerRealmName;
    }

    public void setsCMServerRealmName(String sCMServerRealmName) {
        this.sCMServerRealmName = sCMServerRealmName;
    }

    public String getsCMServerAdminUserName() {
        return sCMServerAdminUserName;
    }

    public void setsCMServerAdminUserName(String sCMServerAdminUserName) {
        this.sCMServerAdminUserName = sCMServerAdminUserName;
    }

    public String getsCMServerAdminPassword() {
        return sCMServerAdminPassword;
    }

    public void setsCMServerAdminPassword(String sCMServerAdminPassword) {
        this.sCMServerAdminPassword = sCMServerAdminPassword;
    }

    public String getsCMReadWritePermissionName() {
        return sCMReadWritePermissionName;
    }

    public void setsCMReadWritePermissionName(String sCMReadWritePermissionName) {
        this.sCMReadWritePermissionName = sCMReadWritePermissionName;
    }

    public String getSvnBaseURL() {
        return svnBaseURL;
    }

    public void setSvnBaseURL(String svnBaseURL) {
        this.svnBaseURL = svnBaseURL;
    }

    public Map<String, List<String>> getDeploymentServerLocations() {
        return deploymentServerLocations;
    }

    public void setDeploymentServerLocations(Map<String, List<String>> deploymentServerLocations) {
        this.deploymentServerLocations = deploymentServerLocations;
    }

    @Override
    public String toString() {
        return "AppFactoryConfiguration{" +
               "sSOName='" + sSOName + '\'' +
               ", sSOIdentityProviderEPR='" + sSOIdentityProviderEPR + '\'' +
               ", sSOKeyStorePassword='" + sSOKeyStorePassword + '\'' +
               ", sSOIdentityAlias='" + sSOIdentityAlias + '\'' +
               ", sSOKeyStoreName='" + sSOKeyStoreName + '\'' +
               ", webServiceEPRAddUserToProject='" + webServiceEPRAddUserToProject + '\'' +
               ", webServiceEPRCreateProject='" + webServiceEPRCreateProject + '\'' +
               ", webServiceEPRCreateRepo='" + webServiceEPRCreateRepo + '\'' +
               ", webServiceEPRGetRolesOfUserForProject='" + webServiceEPRGetRolesOfUserForProject + '\'' +
               ", webServiceEPRGetUsersOfProject='" + webServiceEPRGetUsersOfProject + '\'' +
               ", webServiceEPREmailVarificationService='" + webServiceEPREmailVarificationService + '\'' +
               ", bpelEPRCreateUser='" + bpelEPRCreateUser + '\'' +
               ", bpelEPRActivateUser='" + bpelEPRActivateUser + '\'' +
               ", adminUserName='" + adminUserName + '\'' +
               ", adminPassword='" + adminPassword + '\'' +
               ", sCMServerIp='" + sCMServerIp + '\'' +
               ", sCMServerPort='" + sCMServerPort + '\'' +
               ", sCMServerRealmName='" + sCMServerRealmName + '\'' +
               ", sCMServerAdminUserName='" + sCMServerAdminUserName + '\'' +
               ", sCMServerAdminPassword='" + sCMServerAdminPassword + '\'' +
               ", sCMReadWritePermissionName='" + sCMReadWritePermissionName + '\'' +
               ", svnBaseURL='" + svnBaseURL + '\'' +
               ", deploymentServerLocations=" + deploymentServerLocations +
               '}';
    }
}
