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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.issue.tracker.adapter.api.GenericUser;
import org.wso2.carbon.issue.tracker.adapter.exceptions.IssueTrackerException;
import org.wso2.carbon.issue.tracker.mgt.ldap.ApacheDsLdapConnector;
import org.wso2.carbon.issue.tracker.mgt.ldap.LdapConnector;
import org.wso2.carbon.jira.reporting.adapterImpl.SupportJiraUser;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;


/**
 * This class is responsible for managing LDAP related activities such as adding and removing users
 * from LDAP groups. methods in  OTUserAssociation are overridden to enable add/remove users from LDAP
 * groups instead of JIRA groups ( which is the default behavior).
 */
public class LdapGroupManager extends OTUserAssociation {

    private static Log log = LogFactory.getLog(LdapGroupManager.class);

    /**
     * Method to add users to ldap group
     *
     * @param authToken          this parameter is redundant when using LDAP groups instead of JIRA groups
     * @param genericUser        GenericUser
     * @param isSubscriptionFree whether the  subscription of user is free or not
     * @param jiraUrl            url of jira, this again is redundant when using LDAP
     */
    @Override
    public void addUserToGroup(String authToken, GenericUser genericUser, boolean isSubscriptionFree,
                               String jiraUrl) {

        String groupName;

        // reading group name from stratos.xml based on subscription plan
        init();
        if (isInitialized) {

            if (isSubscriptionFree) {
                groupName = jiraFreeGroupName;
            } else {
                groupName = jiraPayingGroupName;
            }


            LdapConnector ldapConnector = new LdapConnector();
            DirContext ctx = null;
            try {
                // initialize ldap connection
                ctx = ldapConnector.initLDAPConn();
                ApacheDsLdapConnector apacheDsLdapConnector = new ApacheDsLdapConnector();
                //add user to ldap group
                apacheDsLdapConnector.addMemberToLdapGroup(ctx, genericUser.getUsername(), groupName);

            } catch (Exception e) {
                String message = "Adding user to " + groupName + "failed. Email address : " +
                        genericUser.getEmail() + ". Failure reason " + e.getMessage();
                log.warn(message);
            } finally {

                try {
                    if (ctx != null) {
                        ctx.close();
                    }
                } catch (NamingException e) {
                    String message = "Error closing the LDAP connection. Failure reason " + e.getMessage();
                    log.warn(message);
                }
            }


        }
    }


    /**
     * method to remove users from ldap groups
     *
     * @param authToken          this parameter is redundant when using LDAP groups instead of JIRA groups
     * @param genericUser        GenericUser
     * @param isSubscriptionFree whether the  subscription of user is free or not
     * @param jiraUrl            url of jira, this again is redundant when using LDAP
     */
    @Override
    public void removeUserFromGroup(String authToken, GenericUser genericUser,
                                    boolean isSubscriptionFree, String jiraUrl) {


        String groupName;
        init();
        if (isInitialized) {

            if (isSubscriptionFree) {
                groupName = jiraPayingGroupName;
            } else {
                groupName = jiraFreeGroupName;
            }

            LdapConnector ldapConnector = new LdapConnector();
            DirContext ctx = null;
            try {
                // initialize ldap connection
                ctx = ldapConnector.initLDAPConn();
                ApacheDsLdapConnector apacheDsLdapConnector = new ApacheDsLdapConnector();
                //remove the user from ldap group
                apacheDsLdapConnector.removeMemberFromLdapGroup(ctx, genericUser.getUsername(), groupName);

            } catch (NamingException e) {
                String message = "Error initiating the LDAP connection. Failure reason " + e.getMessage();
                log.warn(message);
            } catch (Exception e) {
                String message =
                        "Removing user from " + groupName + " failed. Email address : " + genericUser.getEmail() +
                                ". Failure reason " + e.getMessage();
                log.warn(message);
            }
        }


    }

    /**
     * method to remove users from both free and paid groups. useful when deactivating the account
     *
     * @param authToken   authToken this parameter is redundant when using LDAP groups instead of JIRA groups
     * @param genericUser GenericUser
     * @param jiraUrl     url of jira, this again is redundant when using LDAP
     */
    @Override
    public void removeUserFromAllGroups(String authToken, GenericUser genericUser, String jiraUrl) {
        LdapConnector ldapConnector = new LdapConnector();
        DirContext ctx = null;
        ApacheDsLdapConnector apacheDsLdapConnector = new ApacheDsLdapConnector();

        if (isInitialized) {

            try {
                // initialize ldap connection
                ctx = ldapConnector.initLDAPConn();
                //remove user from paying group
                apacheDsLdapConnector.removeMemberFromLdapGroup(ctx, genericUser.getUsername(), jiraPayingGroupName);
            } catch (NamingException e) {
                String message = "Error initiating the LDAP connection. Failure reason " + e.getMessage();
                log.warn(message);
            } catch (Exception e) {
                String message =
                        "Removing user from " + jiraPayingGroupName + " failed. Email address : " + genericUser.getEmail() +
                                ". Failure reason " + e.getCause();
                log.warn(message);
            }

            try {
                //remove user from free group
                apacheDsLdapConnector.removeMemberFromLdapGroup(ctx, genericUser.getUsername(), jiraFreeGroupName);
            } catch (Exception e) {
                String message =
                        "Removing user from " + jiraFreeGroupName + " failed. Email address : " + genericUser.getEmail() +
                                ". Failure reason " + e.getCause();
                log.warn(message);
            }

        }
    }

}


