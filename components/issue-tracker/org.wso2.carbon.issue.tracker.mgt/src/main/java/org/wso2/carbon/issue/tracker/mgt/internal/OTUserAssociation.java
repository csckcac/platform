/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/


package org.wso2.carbon.issue.tracker.mgt.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.util.ClaimsMgtUtil;
import org.wso2.carbon.stratos.common.util.CommonUtil;
import org.wso2.carbon.stratos.common.util.StratosConfiguration;
import org.wso2.carbon.issue.tracker.adapter.api.GenericCredentials;
import org.wso2.carbon.issue.tracker.adapter.api.GenericUser;
import org.wso2.carbon.issue.tracker.adapter.exceptions.IssueTrackerException;
import org.wso2.carbon.issue.tracker.core.ExceptionHandler;
import org.wso2.carbon.jira.reporting.JiraIssueReporter;
import org.wso2.carbon.jira.reporting.adapterImpl.SupportJiraUser;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class OTUserAssociation {

    public static String jiraConnectionName = null;
    public static String jiraConnectionPass = null;
    public static String jiraServerUrl = null;
    public static String jiraPayingGroupName = null;
    public static String jiraFreeGroupName = null;
    public static String GOOGLE_APPS_IDP_NAME = "GoogleApps";
    public static boolean isInitialized = false;
    private static Log log = LogFactory.getLog(OTUserAssociation.class);


    public SupportJiraUser setDefaultValues(SupportJiraUser supportJiraUser, int tenantId) throws IssueTrackerException {

        if (tenantId > 0) {
            try {
                init();
                if (isInitialized) {
                    RealmService realmService = IssueTrackerAdminServiceComponent.getRealmService();
                    Tenant tenant;
                    try {
                        tenant = realmService.getTenantManager().getTenant(tenantId);
                    } catch (Exception e) {
                        String message = "Cannot obtain the realm with the tenant id" + tenantId;
                        log.error(message, e);
                        throw new StratosException(message, e);
                    }

                    String firstName = null;
                    String lastName = null;
                    try {
                        firstName =
                                ClaimsMgtUtil.getFirstName(realmService, tenantId);


                        lastName =
                                ClaimsMgtUtil.getLastName(realmService, tenantId);


                    } catch (Exception e) {
                        String message = "Cannot obtain the first name and last name of " + tenantId;
                        ExceptionHandler.handleException(message, e, log);
                    }

                    supportJiraUser.setFirstName(firstName);
                    supportJiraUser.setLastName(lastName);
                }
            } catch (Exception e) {
                String message = "Error reading support JIRA configurations for the tenant " + tenantId;
                ExceptionHandler.handleException(message, e, log);
            }

        }

        return supportJiraUser;

    }


    public String createUserInOTLDAP(GenericUser user)
            throws IssueTrackerException {

        String username = null;

        HttpClient httpclient = new HttpClient();

        PostMethod post = new PostMethod("https://wso2.org/services/rest/ws/create.xml");

        try {

            String encodedEmail = URLEncoder.encode(user.getEmail(), "UTF-8");
            post.setRequestEntity(new StringRequestEntity("data={\"mail\":\"" + encodedEmail +
                    "\",\"profile_first_name\":\"" + user.getFirstName() +
                    "\",\"profile_last_name\":\"" + user.getLastName() +
                    "\"}&validation=N", "application/x-www-form-urlencoded", "utf-8"));

        } catch (UnsupportedEncodingException e) {
            String message =
                    "Creating user with the email " + user.getEmail() + " failed due to " +
                            e.getMessage();
            ExceptionHandler.handleException(message, e, log);
        }

        try {
            int result = httpclient.executeMethod(post);

            if (result == 200) {
                username = this.getUserNameFromResponse(post.getResponseBodyAsStream());
            }

        } catch (Exception e) {
            String message =
                    "Creating user " + user.getEmail() + " at OT failed due to " +
                            e.getMessage();
            ExceptionHandler.handleException(message, e, log);
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }

        return username;
    }


    public GenericUser authenticateWithOT(GenericCredentials credentials) throws IssueTrackerException {

        GenericUser user = null;
        HttpClient httpclient = new HttpClient();
        String encoded_password;
        String encoded_name;

        PostMethod post = null;
        try {
            encoded_password = URLEncoder.encode(credentials.getPassword(), "UTF-8");
            encoded_name = URLEncoder.encode(credentials.getUsername(), "UTF-8");
            post = new PostMethod("https://wso2.org/services/rest/ws/login.xml");
            post.setRequestEntity(new StringRequestEntity("mail=" + encoded_name + "&password=" +
                    encoded_password, "application/x-www-form-urlencoded", "utf-8"));

        } catch (UnsupportedEncodingException e) {
            String message =
                    "Creating user with the email " + user.getEmail() + " failed due to " +
                            e.getMessage();
            ExceptionHandler.handleException(message, e, log);
        }
        try {
            int result = httpclient.executeMethod(post);

            if (result == 200) {
                user = this.getAuthenticatedUserFromOT(post.getResponseBodyAsStream());

            }
        } catch (Exception e) {
            String message =
                    "Authenticating user " + credentials.getUsername() + " at OT failed due to " +
                            e.getMessage();
            ExceptionHandler.handleException(message, e, log);
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }

        return user;
    }


    public void addUserToGroup(String authToken, GenericUser genericUser, boolean isSubscriptionFree, String jiraUrl) {

        String groupName;
        init();
        if (isInitialized) {
            JiraIssueReporter reporter = JiraIssueReporter.getInstance();

            if (isSubscriptionFree) {
                groupName = jiraFreeGroupName;
            } else {
                groupName = jiraPayingGroupName;
            }

            try {
                reporter.addUserToGroup(genericUser, authToken, jiraUrl, groupName);

            } catch (Exception e) {
                String message =
                        "Adding user to " + groupName + "failed. Email address : " + genericUser.getEmail() +
                                ". Failure reason " + e.getMessage();
                log.warn(message);
            }


        }

    }


    public void removeUserFromGroup(String authToken, GenericUser genericUser, boolean isSubscriptionFree, String jiraUrl) {
        String groupName;
        init();
        if (isInitialized) {
            JiraIssueReporter reporter = JiraIssueReporter.getInstance();
            if (isSubscriptionFree) {
                groupName = jiraPayingGroupName;
            } else {
                groupName = jiraFreeGroupName;
            }

            try {
                reporter.removeUserFromGroup(genericUser, authToken, jiraUrl, groupName);
            } catch (IssueTrackerException e) {
                String message =
                        "Removing user from " + groupName + " failed. Email address : " + genericUser.getEmail() +
                                ". Failure reason " + e.getMessage();
                log.error(message);
            }
        }

    }


    public void removeUserFromAllGroups(String authToken, GenericUser genericUser, String jiraUrl) {
        if (isInitialized) {
            JiraIssueReporter reporter = JiraIssueReporter.getInstance();
            try {
                reporter.removeUserFromGroup(genericUser, authToken, jiraUrl, jiraPayingGroupName);
            } catch (IssueTrackerException e) {
                String message =
                        "Removing user from " + jiraPayingGroupName + " failed. Email address : " + genericUser.getEmail() +
                                ". Failure reason " + e.getCause();
                log.error(message);
            }

            try {
                reporter.removeUserFromGroup(genericUser, authToken, jiraUrl, jiraFreeGroupName);

            } catch (IssueTrackerException e) {
                String message =
                        "Removing user from " + jiraFreeGroupName + " failed. Email address : " + genericUser.getEmail() +
                                ". Failure reason " + e.getCause();
                log.error(message);
            }

        }
    }

    protected void init() {
           if (!OTUserAssociation.isInitialized) {
               synchronized (OTUserAssociation.class) {
                   if (!OTUserAssociation.isInitialized) {
                       StratosConfiguration stratosConfig = CommonUtil.getStratosConfig();
                       jiraConnectionName =
                               stratosConfig.getStratosEventListenerPropertyValue("jiraConnectionName");
                       jiraConnectionPass =
                               stratosConfig.getStratosEventListenerPropertyValue("jiraConnectionPass");
                       jiraServerUrl =
                               stratosConfig.getStratosEventListenerPropertyValue("jiraServerUrl");
                       jiraPayingGroupName =
                               stratosConfig.getStratosEventListenerPropertyValue("jiraPayingGroupName");
                       jiraFreeGroupName =
                               stratosConfig.getStratosEventListenerPropertyValue("jiraFreeGroupName");

                       if (!("".equals(jiraConnectionName) && "".equals(jiraConnectionPass) && "".equals(jiraServerUrl) &&
                               "".equals(jiraPayingGroupName) && "".equals(jiraFreeGroupName))) {
                           isInitialized = true;
                       }
                   }
               }
           }
       }


    ////////////////// private methods /////////////////

    private boolean isGappUser(int tenantId) throws StratosException {

        RealmService realmService = IssueTrackerAdminServiceComponent.getRealmService();

        RealmConfiguration realmConfiguration;

        try {
            realmConfiguration = ((UserRealm) realmService.getTenantUserRealm(tenantId)).getRealmConfiguration();
        } catch (UserStoreException e) {
            String message =
                    "Failed to get realmConfigurations. " +
                            e.getMessage();
            log.error(message, e);
            throw new StratosException(message, e);
        }


        return !GOOGLE_APPS_IDP_NAME.equals(realmConfiguration.
                getUserStoreProperties().
                get(UserCoreConstants.RealmConfig.PROPERTY_EXTERNAL_IDP));
    }


    private String getUserNameFromResponse(InputStream inputStream) throws IssueTrackerException {

        OMElement responseElement = null;
        String username = null;

        final String USERNAME_ELEMENT_NAME = "name";
        try {
            responseElement = (new StAXOMBuilder(inputStream)).getDocumentElement();
        } catch (XMLStreamException e) {
            String message = "Error parsing response : " + e.getMessage();
            ExceptionHandler.handleException(message, e, log);
        }
        if (responseElement != null) {
            responseElement.build();
        }


        if (responseElement != null) {

            OMElement userChildIt = responseElement.getFirstChildWithName(new QName(USERNAME_ELEMENT_NAME));

            if (null != userChildIt) {
                username = userChildIt.getText();
            } else {
                username = "";
            }

        }
        return username;

    }


    private GenericUser getAuthenticatedUserFromOT(InputStream inputStream) throws IssueTrackerException {
        OMElement responseElement;
        final String USER_ELEMENT_NAME = "user";
        final String USERNAME_ELEMENT_NAME = "name";
        final String EMAIL_ELEMENT_NAME = "mail";
        final String FIRSTNAME_ELEMENT_NAME = "profile_first_name";
        final String LASTNAME_ELEMENT_NAME = "profile_last_name";
        GenericUser user = null;
        try {
            responseElement = (new StAXOMBuilder(inputStream)).getDocumentElement();
            responseElement.build();
            OMElement userChildIt = responseElement.getFirstChildWithName(new QName(USER_ELEMENT_NAME));
            OMElement usernameElement = userChildIt.getFirstChildWithName(new QName(USERNAME_ELEMENT_NAME));
            OMElement mailElement = userChildIt.getFirstChildWithName(new QName(EMAIL_ELEMENT_NAME));
            OMElement firstnameElement = userChildIt.getFirstChildWithName(new QName(FIRSTNAME_ELEMENT_NAME));
            OMElement lastnameElement = userChildIt.getFirstChildWithName(new QName(LASTNAME_ELEMENT_NAME));

            user = new GenericUser();
            user.setUsername(usernameElement.getText());
            user.setEmail(mailElement.getText());
            user.setFirstName(firstnameElement.getText());
            user.setLastName(lastnameElement.getText());


        } catch (XMLStreamException e) {
            String message = "Error parsing response : " + e.getMessage();
            ExceptionHandler.handleException(message, e, log);
        }

        return user;


    }


    public String getJiraAdminAuthToken() throws IssueTrackerException {
        init();
        JiraIssueReporter reporter = JiraIssueReporter.getInstance();
        GenericCredentials credentials = new GenericCredentials();
        credentials.setUrl(jiraServerUrl);
        credentials.setUsername(jiraConnectionName);
        credentials.setPassword(jiraConnectionPass);

        String authToken = null;
        try {
            authToken = reporter.login(credentials);
        } catch (IssueTrackerException e) {
            String message = "Unable to obtain admin authentication token";
            ExceptionHandler.handleException(message, e, log);
        }

        return authToken;

    }
}

