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
package org.wso2.carbon.jira.reporting;

import com.atlassian.jira.rpc.soap.client.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.stratos.common.util.CommonUtil;
import org.wso2.carbon.issue.tracker.adapter.api.*;
import org.wso2.carbon.issue.tracker.adapter.exceptions.IssueTrackerException;
import org.wso2.carbon.jira.reporting.adapterImpl.JiraIssueConverter;
import org.wso2.carbon.jira.reporting.adapterImpl.SupportJiraUser;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;

/**
 * issue tracker adapter implementation for JIRA
 */
public class JiraIssueReporter implements IssueReporting {

    private JiraSoapService jiraSoapService;

    private final static JiraIssueReporter BE_INSTANCE = new JiraIssueReporter();
    private final static Log log = LogFactory.getLog(JiraIssueReporter.class);

    public static JiraIssueReporter getInstance() {

        return BE_INSTANCE;

    }

    /**
     * method to log in to jira
     *
     * @param credentials credentials of a jira account
     * @return authentication token
     * @throws IssueTrackerException thrown if credentials are invalid or jira sever is unavailable
     */
    public String login(GenericCredentials credentials) throws IssueTrackerException {

        String authToken = null;
        String url = credentials.getUrl() + JiraReportingConstants.JIRA_SOAP_URL;
        String username = credentials.getUsername();
        String password = credentials.getPassword();
        try {
            jiraSoapService = JiraSoapServiceFactory.getJiraSoapService(new URL(url));
        } catch (MalformedURLException e) {
            handleException("JIRA URL " + url + " is malformed", e);
        }
        if (jiraSoapService != null) {
            try {
                authToken = jiraSoapService.login(username, password);
            } catch (RemoteException e) {
                handleException("Incorrect username or password ", e);
            }
        }


        return authToken;
    }

    /**
     * method to report issues to jira
     *
     * @param genericIssue Issue
     * @param authToken    authentication token
     * @return true if the issue is successfully created
     * @throws IssueTrackerException
     */
    public String reportIssue(GenericIssue genericIssue, String authToken, String url) throws
            IssueTrackerException {


        JiraSoapService jiraSoapService = null;
        try {
            jiraSoapService = JiraSoapServiceFactory.getJiraSoapService(new URL(url + JiraReportingConstants.JIRA_SOAP_URL));
        } catch (MalformedURLException e) {
            handleException("JIRA URL " + url + " is malformed", e);
        }


        JiraIssueConverter jiraIssue = new JiraIssueConverter();

        //  remoteIssue is the issue to be reported

        RemoteIssue remoteIssue = null;

        // resultIssue is the resulting issue returned
        RemoteIssue resultIssue = null;

        try {
            remoteIssue = jiraIssue.getSpecificIssue(genericIssue);

            // setting severity level and incident impact description
            // these two field are custom field, hence customField ID is dynamic, should be read from config file (stratos.xml).

            String incidentCustomFieldId = CommonUtil.getStratosConfig().getIncidentCustomFieldId();
            String incidentImpactCustomFieldId = CommonUtil.getStratosConfig().getIncidentImpactCustomFieldId();

            RemoteCustomFieldValue customFieldValue = new RemoteCustomFieldValue(incidentCustomFieldId, "", new String[]{"Serious (Severity Level 3)"});
            RemoteCustomFieldValue customFieldValue2 = new RemoteCustomFieldValue(incidentImpactCustomFieldId, "", new String[]{"Automatically generated incident impact description"});

            RemoteCustomFieldValue[] customFieldValues = new RemoteCustomFieldValue[]{customFieldValue, customFieldValue2};
            remoteIssue.setCustomFieldValues(customFieldValues);

        } catch (IssueTrackerException e) {
            handleException("Error in parsing issue OMElement ", e);
        }

        try {
            if (jiraSoapService != null) {

                resultIssue = jiraSoapService.createIssue(authToken, remoteIssue);
            }
        } catch (RemoteException e) {

            handleException("Error in creating issue ", e);
        }


        assert resultIssue != null;
        return resultIssue.getKey();


    }


    public JiraSoapService getJiraSoapService(String url) throws IssueTrackerException {

        JiraSoapService jiraSoapService = null;
        try {
            jiraSoapService = JiraSoapServiceFactory.getJiraSoapService(new URL(url + JiraReportingConstants.JIRA_SOAP_URL));
        } catch (MalformedURLException e) {
            handleException("JIRA URL " + url + " is malformed", e);
        }
        return jiraSoapService;
    }

    /**
     * method to retrieve issues for a particular user and a project
     *
     * @param authToken       authentication token
     * @param offset          starting index of requested issue set
     * @param maxNumOfResults maximum number of results
     * @param url             jira url
     * @return list of issues
     * @throws IssueTrackerException thrown if unable to obtain issues from jira
     */
    public List<GenericIssue> retrieveIssues(String authToken, int offset, int maxNumOfResults, String url) throws
            IssueTrackerException {


        RemoteIssue[] remoteIssues = new RemoteIssue[0];

        List<GenericIssue> genericIssues = new ArrayList<GenericIssue>();

        Map<String, String> issueTypeMap = new HashMap<String, String>();

        Map<String, String> priorityTypeMap = new HashMap<String, String>();


        try {

            JiraSoapService jiraSoapService = (this.getJiraSoapService(url));

            remoteIssues = jiraSoapService.getIssuesFromFilterWithLimit(authToken,
                    JiraReportingConstants.FILTER_ID, offset, maxNumOfResults);

            RemoteIssueType[] issueTypes = jiraSoapService.getIssueTypes(authToken);

            for (RemoteIssueType type : issueTypes) {
                issueTypeMap.put(type.getId(), type.getName());
            }

            RemotePriority[] priorities = jiraSoapService.getPriorities(authToken);

            for (RemotePriority type : priorities) {
                priorityTypeMap.put(type.getId(), type.getName());
            }


        } catch (RemoteException e) {
            handleException("Error in retrieving remoteIssues for the logged user ");
        }

        for (RemoteIssue remoteIssue : remoteIssues) {
            GenericIssue genericIssue = new GenericIssue();
            genericIssue.setSummary(remoteIssue.getSummary());
            genericIssue.setProjectKey(remoteIssue.getProject());

            String issueType = issueTypeMap.get(remoteIssue.getType());

            genericIssue.setType(issueType);

            String priority = priorityTypeMap.get(remoteIssue.getPriority());
            genericIssue.setPriority(priority);
            genericIssue.setIssueKey(remoteIssue.getKey());
            String issueUrl = JiraReportingConstants.BROWSE + remoteIssue.getKey();
            genericIssue.setUrl(issueUrl);
            genericIssues.add(genericIssue);

        }

        return genericIssues;

    }

    /**
     * method to retrieve issues using a JQL query
     * @param authToken   authentication token
     * @param jiraUrl     jira url
     * @param maxResults  maximum number of results to be retrieved. this is required since the JIRA aPI doesnt provide a method to
     * specify first and last number of issues. hence to reduce pagination overhead we are limiting the number of results explicitly.
     * @return
     * @throws IssueTrackerException
     */
    public List<GenericIssue> retrieveIssuesByQuery(String authToken, String jiraUrl, int maxResults) throws IssueTrackerException {

        RemoteIssue[] remoteIssues;

        List<GenericIssue> genericIssues = new ArrayList<GenericIssue>();

        Map<String, String> issueTypeMap = new HashMap<String, String>();

        Map<String, String> priorityTypeMap = new HashMap<String, String>();


        try {

            JiraSoapService jiraSoapService = this.getJiraSoapService(jiraUrl);


            remoteIssues = jiraSoapService.getIssuesFromJqlSearch(authToken,
                    JiraReportingConstants.JQL_QUERY, maxResults);


            List<RemoteIssue> remoteIssueList = Arrays.asList(remoteIssues);

            RemoteIssueType[] issueTypes = jiraSoapService.getIssueTypes(authToken);

            for (RemoteIssueType type : issueTypes) {
                issueTypeMap.put(type.getId(), type.getName());
            }

            RemotePriority[] priorities = jiraSoapService.getPriorities(authToken);

            for (RemotePriority type : priorities) {
                priorityTypeMap.put(type.getId(), type.getName());
            }


            for (RemoteIssue remoteIssue : remoteIssueList) {
                GenericIssue genericIssue = new GenericIssue();
                genericIssue.setSummary(remoteIssue.getSummary());
                genericIssue.setProjectKey(remoteIssue.getProject());
                String issueType = issueTypeMap.get(remoteIssue.getType());

                genericIssue.setType(issueType);

                String priority = priorityTypeMap.get(remoteIssue.getPriority());
                genericIssue.setPriority(priority);
                genericIssue.setIssueKey(remoteIssue.getKey());
                String url = JiraReportingConstants.BROWSE + remoteIssue.getKey();
                genericIssue.setUrl(url);
                genericIssues.add(genericIssue);

            }


        } catch (RemoteException e) {
            handleException("Error in retrieving remoteIssues for the logged user ");
        }


        return genericIssues;

    }


    public List<String> getJiraProjects(String authToken, String jiraUrl) throws IssueTrackerException {

        RemoteProject[] remoteProjects;

        List<String> names = new ArrayList<String>();

        try {

            JiraSoapService jiraSoapService = this.getJiraSoapService(jiraUrl);
            remoteProjects = jiraSoapService.getProjectsNoSchemes(authToken);

            for (RemoteProject project : remoteProjects) {

                names.add(project.getKey());
            }

        } catch (RemoteException e) {
            handleException("Unable to connect to JIRA account with the given credentials", e);
        }

        return names;
    }


    public List<GenericIssueType> getIssueTypes(String authToken, String jiraUrl) throws IssueTrackerException {

        List<GenericIssueType> issueTypes = new ArrayList<GenericIssueType>();
        try {

            JiraSoapService jiraSoapService = this.getJiraSoapService(jiraUrl);

            RemoteIssueType[] remoteIssueTypes = jiraSoapService.getIssueTypes(authToken);

            for (RemoteIssueType issueType : remoteIssueTypes) {

                GenericIssueType type = new GenericIssueType();

                type.setId(issueType.getId());
                type.setIssueType(issueType.getName());
                type.setIcon(issueType.getIcon());

                issueTypes.add(type);
            }
        } catch (RemoteException e) {
            handleException("Unable to obtain issue types for the given credentials", e);
        }


        return issueTypes;

    }


    public List<GenericPriority> getPriorityTypes(String authToken, String jiraUrl) throws IssueTrackerException {

        List<GenericPriority> priorityList = new ArrayList<GenericPriority>();

        try {
            JiraSoapService jiraSoapService = this.getJiraSoapService(jiraUrl);

            RemotePriority[] remotePriorities = jiraSoapService.getPriorities(authToken);
            for (RemotePriority priority : remotePriorities) {

                GenericPriority genericPriority = new GenericPriority();
                genericPriority.setId(priority.getId());
                genericPriority.setName(priority.getName());

                priorityList.add(genericPriority);
            }

        } catch (RemoteException e) {
            handleException("Unable to obtain priority types for the given credentials", e);
        }

        return priorityList;
    }


    public boolean attachFiles(String token, String issueKey, String[] fileNames,
                               String[] attachmentData, String jiraUrl) throws IssueTrackerException {

        boolean success = false;
        JiraSoapService jiraSoapService = this.getJiraSoapService(jiraUrl);
        try {


            success = jiraSoapService.addBase64EncodedAttachmentsToIssue(token, issueKey,
                    fileNames, attachmentData);


        } catch (RemoteException e) {
            handleException("Unable to attach files to issue" + issueKey, e);
        }

        return success;


    }

    /**
     * this method get the issue count for a filter. custom filter id is need to be known in advance
     *
     * @param token   authn token
     * @param jiraUrl JIRA url
     * @return issue count
     * @throws IssueTrackerException thrown if the filter is unavailable or if an error occurred while getting the count
     */
    public long getIssueCount(String token, String jiraUrl) throws IssueTrackerException {

        long count = 0;
        try {
            JiraSoapService jiraSoapService = this.getJiraSoapService(jiraUrl);
            count = jiraSoapService.getIssueCountForFilter(token, JiraReportingConstants.FILTER_ID);
        } catch (RemoteException e) {
            handleException("Unable to obtain issue count for the filter " +
                    JiraReportingConstants.FILTER_ID);
        }
        return count;
    }

    /**
     * this method delete an issue given the issue key
     *
     * @param token    authn token
     * @param issueKey issue key
     * @param jiraUrl  JIRA url
     * @return true if the issue is deleted
     * @throws IssueTrackerException thrown in case of a failure to establish the connection with JiraSoapService or an
     *                               erron in deleting the issue.
     */
    public boolean deleteIssues(String token, String issueKey, String jiraUrl) throws IssueTrackerException {
        boolean isIssueDeleted = false;
        JiraSoapService jiraSoapService = null;
        try {
            jiraSoapService = this.getJiraSoapService(jiraUrl);
        } catch (IssueTrackerException e) {
            handleException("Unable to connect to the jiraSoapService", e);
        }

        try {
            if (jiraSoapService != null) {
                jiraSoapService.deleteIssue(token, issueKey);
                isIssueDeleted = true;
            }
        } catch (RemoteException e) {
            handleException("Error deleting issue " + issueKey + " .", e);
        }

        return isIssueDeleted;

    }


    public String getProjectLead(String token, String projectKey) throws IssueTrackerException {

        RemoteProject remoteProject = null;
        try {
            if (jiraSoapService != null) {
                remoteProject = jiraSoapService.getProjectByKey(token, projectKey);
                return remoteProject.getLead();

            }
        } catch (RemoteException e) {
            handleException("Unable to obtain project lead for the project " + projectKey, e);
        }

        return remoteProject.getLead();


    }


    public void setDefaultAssignee(GenericIssue genericIssue, String authToken) throws IssueTrackerException {

        //   issues are assigned to the project lead automatically
        if (null == genericIssue.getAssignee() || "".equals(genericIssue.getAssignee())) {

            RemoteProject remoteProject = null;
            try {
                if (jiraSoapService != null) {
                    remoteProject = jiraSoapService.getProjectByKey(authToken, genericIssue.getProjectKey());
                }
            } catch (RemoteException e) {
                handleException("Unable to obtain project info of " + genericIssue.getProjectKey(), e);
            }
            if (remoteProject != null) {
                genericIssue.setAssignee(remoteProject.getLead());
            }

        }

    }

    // this is used  for services-when the project is fixed

    public List<GenericIssue> retrieveIssuesByQuery(String authToken, String jiraUrl, int maxResults, String projectName) throws IssueTrackerException {

        RemoteIssue[] remoteIssues;

        List<GenericIssue> genericIssues = new ArrayList<GenericIssue>();

        Map<String, String> issueTypeMap = new HashMap<String, String>();

        Map<String, String> priorityTypeMap = new HashMap<String, String>();

        Map<String, String> statusTypeMap = new HashMap<String, String>();

        try {

            JiraSoapService jiraSoapService = this.getJiraSoapService(jiraUrl);

            String jqlQuery = JiraReportingConstants.JQL_QUERY_SEARCH_BY_PROJECT + projectName;

            remoteIssues = jiraSoapService.getIssuesFromJqlSearch(authToken,
                    jqlQuery, maxResults);

            List<RemoteIssue> remoteIssueList = Arrays.asList(remoteIssues);

            RemoteIssueType[] issueTypes = jiraSoapService.getIssueTypes(authToken);


            for (RemoteIssueType type : issueTypes) {
                issueTypeMap.put(type.getId(), type.getName());
            }

            RemotePriority[] priorities = jiraSoapService.getPriorities(authToken);

            for (RemotePriority type : priorities) {
                priorityTypeMap.put(type.getId(), type.getName());
            }

            RemoteStatus[] remoteStatus = jiraSoapService.getStatuses(authToken);

            for (RemoteStatus status : remoteStatus) {
                statusTypeMap.put(status.getId(), status.getName());
            }

            for (RemoteIssue remoteIssue : remoteIssueList) {
                GenericIssue genericIssue = new GenericIssue();

                String issueType = issueTypeMap.get(remoteIssue.getType());
                String priority = priorityTypeMap.get(remoteIssue.getPriority());
                String url = JiraReportingConstants.BROWSE + remoteIssue.getKey();
                String status = statusTypeMap.get(remoteIssue.getStatus());
                Calendar lastUpdated = remoteIssue.getUpdated();
                String incidentCustomFieldId = CommonUtil.getStratosConfig().getIncidentCustomFieldId();
                String assignee = jiraSoapService.getUser(authToken, remoteIssue.getAssignee()).getFullname();

                genericIssue.setSummary(remoteIssue.getSummary());
                genericIssue.setProjectKey(remoteIssue.getProject());
                genericIssue.setType(issueType);
                genericIssue.setPriority(priority);
                genericIssue.setIssueKey(remoteIssue.getKey());
                genericIssue.setUrl(url);
                genericIssue.setAssignee(assignee);
                genericIssue.setStatus(status);
                genericIssue.setLastUpdated(lastUpdated);

                RemoteCustomFieldValue[] customFieldValues = remoteIssue.getCustomFieldValues();
                for (RemoteCustomFieldValue value : customFieldValues) {
                    if (incidentCustomFieldId.equals(value.getCustomfieldId())) {
                        genericIssue.setSeverity(value.getValues()[0]);
                    }
                }
                genericIssues.add(genericIssue);

            }


        } catch (RemoteException e) {
            handleException("Error in retrieving remoteIssues for the logged user ");
        }

        return genericIssues;

    }

    //this check should be done using a admin user

    public boolean isAddedToGroup(String email, String url, String groupName, String authToken) throws IssueTrackerException {
        boolean isUserInGroup = false;
        try {
            JiraSoapService jiraSoapService = this.getJiraSoapService(url);
            try {
                RemoteGroup remoteGroup = jiraSoapService.getGroup(authToken, groupName);
                RemoteUser[] remoteUsers = remoteGroup.getUsers();

                for (RemoteUser user : remoteUsers) {

                    if (email.equals(user.getEmail())) {
                        isUserInGroup = true;
                        break;
                    }
                }


            } catch (RemoteException e) {
                handleException("Error obtaining group " + groupName + ".");
            }
        } catch (IssueTrackerException e) {
            handleException("Error checking whether the user is added to a group.");
        }

        return isUserInGroup;

    }


    public RemoteUser createUserInJIRA(SupportJiraUser supportJiraUser, String adminAuthToken) throws IssueTrackerException {


        RemoteUser remoteUser;

        GenericCredentials credentials = supportJiraUser.getCredentials();

        String jiraUrl = null;
        String password = null;


        String username = supportJiraUser.getUsername();
        String email = supportJiraUser.getEmail();
        String fullName = supportJiraUser.getFirstName() + " " + supportJiraUser.getLastName();
        if (null != credentials) {
            jiraUrl = credentials.getUrl();
            password = credentials.getPassword();
        }

        try {

            JiraSoapService jiraSoapService = null;
            try {
                jiraSoapService = this.getJiraSoapService(jiraUrl);
            } catch (IssueTrackerException e) {
                handleException("Unable to connect to the jiraSoapService", e);
            }

            if (jiraSoapService != null) {
                jiraSoapService.createUser(adminAuthToken, username, password, fullName, email);
            }
        } catch (RemoteValidationException e) {
           // user already exist. Reuse this account
            String message =
                    "Creating user " + email + " at Support JIRA failed due to " +
                            e.getFaultString();
            log.warn(message);
        } catch (RemotePermissionException e) {
            String message =
                    "Unable to add user with the " + email + " at Support JIRA failed due to " +
                            e.getFaultString();
            log.error(message, e);
        } catch (RemoteAuthenticationException e) {
            String message =
                    "Unable to add user with the " + email + " at Support JIRA failed due to " +
                           e.getFaultString();
            log.error(message, e);
        } catch (com.atlassian.jira.rpc.soap.client.RemoteException e) {
            String message =
                    "Unable to add user with the " + email + " at Support JIRA failed due to " +
                           e.getFaultString();
            log.error(message, e);
        } catch (RemoteException e) {
            String message =
                    "Unable to add user with the " + email + " at Support JIRA failed due to " +
                            e.getMessage();
            log.error(message, e);
        }


        remoteUser = new RemoteUser();
        remoteUser.setEmail(email);
        remoteUser.setFullname(fullName);
        remoteUser.setName(username);
        return remoteUser;
    }


    public void addUserToGroup(GenericUser genericUser, String authToken, String url, String groupName) throws IssueTrackerException {


        JiraSoapService jiraSoapService = null;
        try {
            jiraSoapService = this.getJiraSoapService(url);
        } catch (IssueTrackerException e) {
            handleException("Unable to connect to the jiraSoapService", e);
        }

        RemoteUser remoteUser = new RemoteUser();
        remoteUser.setEmail(genericUser.getEmail());
        remoteUser.setName(genericUser.getUsername());
        remoteUser.setFullname(genericUser.getFirstName() + " " + genericUser.getLastName());

        RemoteGroup remoteGroup;
        try {
            if (jiraSoapService != null) {
                remoteGroup = jiraSoapService.getGroup(authToken, groupName);
                jiraSoapService.addUserToGroup(authToken, remoteGroup, remoteUser);
            }

        } catch (RemoteValidationException e) {
            log.warn("cannot add user " + genericUser.getUsername() + " from the group " + groupName + ". " + e.getFaultString());
        } catch (Exception e) {
            log.error("cannot add user " + genericUser.getUsername() + " from the group " + groupName + ". " + e.getMessage());
        }

    }


    public void removeUserFromGroup(GenericUser genericUser, String authToken, String jiraUrl, String groupName) throws IssueTrackerException {


        JiraSoapService jiraSoapService = null;
        try {
            jiraSoapService = this.getJiraSoapService(jiraUrl);
        } catch (IssueTrackerException e) {
            handleException("Unable to connect to the jiraSoapService", e);
        }
        RemoteGroup remoteGroup;
        RemoteUser remoteUser = new RemoteUser();
        remoteUser.setEmail(genericUser.getEmail());
        remoteUser.setName(genericUser.getUsername());
        remoteUser.setFullname(genericUser.getFirstName() + " " + genericUser.getLastName());
        try {
            if (jiraSoapService != null) {
                remoteGroup = jiraSoapService.getGroup(authToken, groupName);
                try {
                    jiraSoapService.removeUserFromGroup(authToken, remoteGroup, remoteUser);
                } catch (RemoteValidationException e) {
                    // this occurs if the use is not a member of the relevant group. in this case we dont need to do anything
                }
            }
        } catch (RemoteException e) {
            String message = "Removing user from " + groupName + "failed. Email address : " + genericUser.getEmail() +
                    ". Failure reason " + e.getMessage();
            log.error(message, e);

        }
    }


    private static void handleException(String msg, Exception e) throws IssueTrackerException {

        log.error(msg, e);

        throw new IssueTrackerException(msg, e);

    }

    private static void handleException(String msg) throws IssueTrackerException {

        log.error(msg);

        throw new IssueTrackerException(msg);

    }


}
