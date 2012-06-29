/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package sonia.scm.carbon.auth;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.user.User;
import sonia.scm.web.security.AuthenticationResult;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class CarbonAuthClient {
    private static final Logger logger =
            LoggerFactory.getLogger(CarbonAuthClient.class);

    private sonia.scm.carbon.auth.AuthenticationAdminStub authStub = null;
    private UserAdminStub userAdminStub;
    private String baseDir;

    private CarbonAuthConfig config;

    public void setConfig(CarbonAuthConfig config) {
        this.config = config;
    }

    public CarbonAuthClient(String baseDir) {
        this.baseDir = baseDir;
        authStub = new sonia.scm.carbon.auth.AuthenticationAdminStub();
        userAdminStub = new UserAdminStub();
    }

    public void init() {
        String location;
        String locationFromConfig = config.getKeyStoreLocation();
        String serverUrl = config.getBackEndServerUrl();
        String authEPR = serverUrl + "/services/AuthenticationAdmin";
        String userAdminEPR = serverUrl + "/services/UserAdmin";
        if (locationFromConfig.startsWith(File.separator)) {
            location = locationFromConfig;
        } else {
            location = new File(baseDir).getParent() + File.separator + locationFromConfig;
        }


        logger.info(location);
        System.setProperty("javax.net.ssl.trustStore", location);
        System.setProperty("javax.net.ssl.trustStorePassword", config.getKeyStorePassword());

        authStub.setEpr(authEPR);
        userAdminStub.setUserAdminEPR(userAdminEPR);
    }

    public boolean authenticateUser(String username, String password, String remoteAddress) {
        String cookie;
        boolean loggedIn = false;
        try {
            cookie = authStub.login(username, password, remoteAddress);
            loggedIn = (cookie != null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (loggedIn) {
            logger.debug("The user " + username + " authenticated successfully.");
        } else {
            logger.info("The user " + username + "is failed in authentication");
        }
        return loggedIn;
    }


    public AuthenticationResult authorizeUser(HttpServletRequest request, String userName) {
        logger.info(request.getRequestURI());
        String applicationName = request.getRequestURI().split("/")[3];
        String serverIp = request.getRemoteAddr();
        String cookie = getTenantAdminCookie(applicationName, serverIp);
        String rolesXML;
        AuthenticationResult result;
        try {
            rolesXML = userAdminStub.getRolesXMLOfUser(userName, cookie);
            if (rolesXML.contains(config.getRoleOfSVNRW().concat("true"))) {

                result = new AuthenticationResult(getUser(userName, applicationName), getGroups(applicationName));
                logger.debug("user " + userName + " from " + applicationName + " authorized sucessfully");

            } else {
                result = AuthenticationResult.NOT_FOUND;
                logger.warn("user " + userName + " from " + applicationName + "  failed in authorization ");
            }
        } catch (Exception e) {
            result = AuthenticationResult.NOT_FOUND;
            logger.error("user " + userName + " is not a member in " + applicationName, e);
        }
        return result;
    }



    private String getTenantAdminCookie(String tenantDomain, String serverIp) {


        String tenantAdminUserName = config.getAdminUserName().concat("@").concat(tenantDomain);
        String tenantAdminPassword = config.getDefaultTenantPassword();
        String cookie = null;
        try {
            cookie = authStub.login(tenantAdminUserName, tenantAdminPassword, serverIp);
        } catch (Exception e) {
            logger.info("Could not get the tenant admin cookie for " + tenantDomain, e);
        }
        return cookie;
    }

    public User getUser(String userName, String tenantDomain) {
        User user = new User();
        user.setName(userName);
        user.setType(CarbonAuthHandler.TYPE);
        user.setDisplayName(userName);
        user.setMail("dummy@example.com"); //we have to just pass an email to get this passed. 
        return user;
    }

    public Set<String> getGroups(String projectKey) {
        Set<String> groups = new HashSet<String>();
        groups.add(projectKey);
        return groups;
    }


}
