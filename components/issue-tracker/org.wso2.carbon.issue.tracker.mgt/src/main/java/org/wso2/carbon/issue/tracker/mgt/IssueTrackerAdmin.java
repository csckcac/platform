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
package org.wso2.carbon.issue.tracker.mgt;


import com.atlassian.jira.rpc.soap.client.RemoteUser;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.account.mgt.stub.services.BillingDataAccessServiceExceptionException;
import org.wso2.carbon.account.mgt.stub.services.BillingDataAccessServiceStub;
import org.wso2.carbon.account.mgt.stub.services.beans.xsd.Subscription;
import org.wso2.carbon.authenticator.proxy.AuthenticationAdminClient;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.issue.tracker.mgt.internal.LdapGroupManager;
import org.wso2.carbon.stratos.common.config.CloudServiceConfig;
import org.wso2.carbon.stratos.common.config.CloudServiceConfigParser;
import org.wso2.carbon.stratos.common.util.CommonUtil;
import org.wso2.carbon.stratos.common.util.StratosConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.issue.tracker.adapter.api.*;
import org.wso2.carbon.issue.tracker.adapter.exceptions.IssueTrackerException;
import org.wso2.carbon.issue.tracker.core.*;
import org.wso2.carbon.issue.tracker.mgt.config.ManagerConfigurations;
import org.wso2.carbon.issue.tracker.mgt.internal.IssueTrackerAdminServiceComponent;
import org.wso2.carbon.issue.tracker.mgt.internal.OTUserAssociation;
import org.wso2.carbon.jira.reporting.JiraIssueReporter;
import org.wso2.carbon.jira.reporting.adapterImpl.SupportJiraUser;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.DataPaginator;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class IssueTrackerAdmin extends RegistryAbstractAdmin {

    private final static Log log = LogFactory.getLog(IssueTrackerAdmin.class);


    /**
     * admin service to capture credentials and login
     *
     * @param credentials
     * @return
     * @throws IssueTrackerException
     */
    public String login(GenericCredentials credentials) throws IssueTrackerException {

        JiraIssueReporter reporter = JiraIssueReporter.getInstance();

        String token = null;
        try {
            token = reporter.login(credentials);
        } catch (IssueTrackerException e) {
            ExceptionHandler.handleException("Error in login.", e, log);
        }
        return token;

    }


    /**
     * admin service to validate credentials
     *
     * @param credentials
     * @return
     * @throws IssueTrackerException
     */
    public String validateCredentials(GenericCredentials credentials) throws IssueTrackerException {

        JiraIssueReporter reporter = JiraIssueReporter.getInstance();
        String token = null;

        if (isStratosService()) {
            credentials.setUrl(getJiraUrlFromConfig());
            StratosConfiguration stratosConfiguration = CommonUtil.getStratosConfig();

            // need admin login auth token to check groups

            String jiraConnectionName = stratosConfiguration.getStratosEventListenerPropertyValue("jiraConnectionName");
            String jiraConnectionPass = stratosConfiguration.getStratosEventListenerPropertyValue("jiraConnectionPass");
            String jiraServerUrl = stratosConfiguration.getStratosEventListenerPropertyValue("jiraServerUrl");
            String paidGroupName = stratosConfiguration.getStratosEventListenerPropertyValue("jiraPayingGroupName");
            String nonPaidGroupName = stratosConfiguration.getStratosEventListenerPropertyValue("jiraFreeGroupName");

            GenericCredentials adminCredentials = new GenericCredentials();
            adminCredentials.setUsername(jiraConnectionName);
            adminCredentials.setPassword(jiraConnectionPass);
            adminCredentials.setUrl(jiraServerUrl);

            boolean isUserInGroup = false;


            // whether the user is in one of the account 

            String adminToken = reporter.login(adminCredentials);

            //todo : this check might not be required as per the new work flow.review and remove
            if (reporter.isAddedToGroup(credentials.getUsername(), credentials.getUrl(), paidGroupName, adminToken)) {
                isUserInGroup = true;
            } else if (reporter.isAddedToGroup(credentials.getUsername(), credentials.getUrl(), nonPaidGroupName, adminToken)) {
                isUserInGroup = true;
            }

            if (isUserInGroup) {
                try {
                    token = reporter.login(credentials);
                } catch (IssueTrackerException e) {
                    ExceptionHandler.handleException("Error in login.", e, log);
                }
            }

        } else {
            try {
                token = reporter.login(credentials);
            } catch (IssueTrackerException e) {
                ExceptionHandler.handleException("Error in login.", e, log);
            }

        }
        return token;
    }


    public void captureAccountInfo(AccountInfo accountInfo) throws IssueTrackerException {

        Registry registry = getGovernanceRegistry();
        int tenantID;
        try {
            if (isStratosService()) {

                tenantID = CarbonContext.getCurrentContext().getTenantId();
                String key;
                key = tenantID + IssueTrackerConstants.JIRA_CREDENTIALS_REGISTRY_PREFIX_NONPAID;
                accountInfo.setKey(key);
                accountInfo.getCredentials().setUrl(getJiraUrlFromConfig());
            }

            RegistryResourceHandler.persistCredentials(registry, accountInfo);
        } catch (IssueTrackerException e) {
            ExceptionHandler.handleException("Error persisting account info.", e, log);
        }

    }

    /**
     * admin service to obtain JIRA account names from registry
     *
     * @return
     * @throws IssueTrackerException
     */
    public List<String> getAccountNames() throws IssueTrackerException {

        Registry registry = getGovernanceRegistry();

        List<AccountInfo> accountInfoList = RegistryResourceHandler.getAccounts(registry);
        List<String> serverNameList = new ArrayList<String>();

        for (AccountInfo s : accountInfoList) {

            serverNameList.add(s.getKey());
        }

        return serverNameList;

    }

    /**
     * admin service to obtain JIRA account information from the registry
     *
     * @return
     * @throws IssueTrackerException
     */
    public List<AccountInfo> getAccountInfo() throws IssueTrackerException {

        Registry registry = getGovernanceRegistry();
        List<AccountInfo> accountInfoList = null;
        try {
            accountInfoList = RegistryResourceHandler.getAccounts(registry);
        } catch (IssueTrackerException e) {
            ExceptionHandler.handleException("Error in retrieving account info from registry", e, log);
        }

        return accountInfoList;

    }

    /**
     * admin service to obtain account credentials given account name
     *
     * @param name
     * @return
     * @throws IssueTrackerException
     */
    public GenericCredentials getAccountCredentials(String name) throws IssueTrackerException {

        List<AccountInfo> accountInfoList = null;
        GenericCredentials credentials = new GenericCredentials();

        try {
            accountInfoList = this.getAccountInfo();
        } catch (IssueTrackerException e) {
            ExceptionHandler.handleException("Error in retrieving account info from registry", e, log);
        }

        if (null != accountInfoList) {

            for (AccountInfo info : accountInfoList) {

                if (name.equals(info.getKey())) {
                    credentials.setUrl(info.getCredentials().getUrl());
                    credentials.setUsername(info.getCredentials().getUsername());
                    credentials.setPassword(info.getCredentials().getPassword());
                    break;
                }
            }
        }
        return credentials;
    }


    public AccountInfo getAccount(String name) throws IssueTrackerException {

        List<AccountInfo> accountInfoList = null;
        GenericCredentials credentials = new GenericCredentials();
        AccountInfo accountInfo = null;

        try {
            accountInfoList = this.getAccountInfo();
        } catch (IssueTrackerException e) {
            ExceptionHandler.handleException("Error in retrieving account info from registry", e, log);
        }

        if (null != accountInfoList) {

            for (AccountInfo info : accountInfoList) {

                if (name.equals(info.getKey())) {
                    accountInfo = info;
                    break;
                }
            }

        }
        return accountInfo;

    }


    public AccountInfo getAccountWhenService() throws IssueTrackerException {

        List<AccountInfo> accountInfoList = null;
        GenericCredentials credentials = new GenericCredentials();
        AccountInfo accountInfo = null;
        String email = null;
        String username = null;

        int tenantId = CarbonContext.getCurrentContext().getTenantId();


        try {
            accountInfoList = this.getAccountInfo();
        } catch (IssueTrackerException e) {
            ExceptionHandler.handleException("Error in retrieving account info from registry", e, log);
        }


        if (null != accountInfoList && accountInfoList.size() == 1) {

            for (AccountInfo info : accountInfoList) {
                accountInfo = info;
            }

        }

        return accountInfo;

    }


    /**
     * admin service to capture issue info and report to JIRA
     *
     * @param genericIssue
     * @param token
     * @param url
     * @return
     * @throws IssueTrackerException
     */
    public String captureIssueInfo(GenericIssue genericIssue, String token, String url) throws IssueTrackerException {

        String issueKey = null;
        JiraIssueReporter reporter = JiraIssueReporter.getInstance();

        // in stratos only paid tenants are allowed to report issues
        if (isStratosService() && !this.isTenantSubscriptionFree()) {
            url = this.getJiraUrlFromConfig();
            String projectKey = this.getJiraProjectFromConfig();
            genericIssue.setProjectKey(projectKey);
            genericIssue.setAssignee(this.getAssigneeInService(projectKey));

        } else {
            reporter.setDefaultAssignee(genericIssue, token);
        }

        try {
            issueKey = reporter.reportIssue(genericIssue, token, url);
        } catch (IssueTrackerException e) {
            ExceptionHandler.handleException("Error in reporting the issue", e, log);
        }

        return issueKey;

    }


    /**
     * method to obtain project names as a JSONArray
     *
     * @param token
     * @param url
     * @return
     * @throws IssueTrackerException
     */
    private JSONArray getProjectNames(String token, String url) throws IssueTrackerException {

        JiraIssueReporter reporter = JiraIssueReporter.getInstance();

        List<String> projectNames = null;
        try {
            projectNames = reporter.getJiraProjects(token, url);
        } catch (IssueTrackerException e) {
            ExceptionHandler.handleException("Error retrieving project names.", e, log);
        }

        JSONArray jsonArray = new JSONArray();
        if (projectNames != null) {
            for (String name : projectNames) {
                jsonArray.put(name);
            }
        }

        return jsonArray;

    }

    /**
     * method to add issue types to JSONObject
     *
     * @param token
     * @param jsonObject
     * @param url
     * @return
     * @throws IssueTrackerException
     */
    private JSONObject getIssueTypes(String token, JSONObject jsonObject, String url) throws IssueTrackerException {

        JiraIssueReporter reporter = JiraIssueReporter.getInstance();
        List<GenericIssueType> issueTypes = null;

        try {
            issueTypes = reporter.getIssueTypes(token, url);
        } catch (IssueTrackerException e) {
            ExceptionHandler.handleException("Error obtaining issue types", e, log);
        }

        JSONArray issueNames = new JSONArray();
        JSONArray issueID = new JSONArray();

        if (issueTypes != null) {
            for (GenericIssueType type : issueTypes) {
                issueNames.put(type.getIssueType());
                issueID.put(type.getId());
            }
        }
        try {
            jsonObject.put("issueId", issueID);
            jsonObject.put("issueType", issueNames);
        } catch (JSONException e) {
            ExceptionHandler.handleException("Error creating JSON object for issue type.", e, log);
        }

        return jsonObject;


    }

    /**
     * method to add priority types to JSONObject
     *
     * @param token
     * @param jsonObject
     * @param url
     * @return
     * @throws IssueTrackerException
     */
    private JSONObject getPriorityTypes(String token, JSONObject jsonObject, String url) throws IssueTrackerException {

        JiraIssueReporter reporter = JiraIssueReporter.getInstance();
        List<GenericPriority> priorities = null;

        try {
            priorities = reporter.getPriorityTypes(token, url);
        } catch (IssueTrackerException e) {
            ExceptionHandler.handleException("Error obtaining priority types", e, log);
        }

        JSONArray priorityNames = new JSONArray();
        JSONArray priorityId = new JSONArray();

        if (priorities != null) {
            for (GenericPriority priority : priorities) {
                priorityNames.put(priority.getName());
                priorityId.put(priority.getId());
            }
        }

        try {
            jsonObject.put("priorityName", priorityNames);
            jsonObject.put("priorityId", priorityId);
        } catch (JSONException e) {
            ExceptionHandler.handleException("Error creating JSON object for priority types.", e, log);
        }


        return jsonObject;
    }

    /**
     * method to obtain project names and issue types as json string
     *
     * @param accountName
     * @param url
     * @return
     * @throws org.wso2.carbon.issue.tracker.adapter.exceptions.IssueTrackerException
     *
     */
    public String getAccountSpecificData(String accountName, String url) throws IssueTrackerException {

        AccountInfo accountInfo = this.getAccount(accountName);
        String token = this.login(accountInfo.getCredentials());

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("project", this.getProjectNames(token, url));
            jsonObject = this.getIssueTypes(token, jsonObject, url);
            jsonObject = this.getPriorityTypes(token, jsonObject, url);
        } catch (JSONException e) {
            ExceptionHandler.handleException("Error constructing json object", e, log);
        } catch (IssueTrackerException e) {
            ExceptionHandler.handleException("Error obtaining project and issue types ", e, log);
        }

        return jsonObject.toString();

    }


    public String getAccountSpecificDetails(GenericCredentials credentials) throws IssueTrackerException {

        String token = this.login(credentials);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("project", this.getProjectNames(token, credentials.getUrl()));
            jsonObject = this.getIssueTypes(token, jsonObject, credentials.getUrl());
            jsonObject = this.getPriorityTypes(token, jsonObject, credentials.getUrl());
        } catch (JSONException e) {
            ExceptionHandler.handleException("Error constructing json object", e, log);
        } catch (IssueTrackerException e) {
            ExceptionHandler.handleException("Error obtaining project and issue types ", e, log);
        }

        return jsonObject.toString();
    }


    /**
     * admin service to delete accounts
     *
     * @param key
     * @return
     * @throws IssueTrackerException
     */
    public boolean deleteAccount(String key) throws IssueTrackerException {

        boolean isDeleted = false;
        String path = IssueTrackerConstants.ISSUE_TRACKERS_RESOURCE_PATH + key;

        Registry registry = getGovernanceRegistry();
        try {

            if (registry.resourceExists(path)) {
                registry.delete(path);
                isDeleted = true;
            }
        } catch (RegistryException e) {
            ExceptionHandler.handleException("Unable to delete " + key + " from registry", e, log);
        }

        return isDeleted;
    }

    /**
     * admin service to retrieve issues
     *
     * @param token
     * @param url
     * @param maxResults
     * @return
     * @throws IssueTrackerException
     */
    public List<GenericIssue> retrieveIssues(String token, String url, int maxResults) throws IssueTrackerException {

        List<GenericIssue> genericIssues = new ArrayList<GenericIssue>();
        JiraIssueReporter reporter = JiraIssueReporter.getInstance();
        try {

            genericIssues = reporter.retrieveIssuesByQuery(token, url, maxResults);

        } catch (IssueTrackerException e) {
            ExceptionHandler.handleException("Unable to retrieve issues for ", e, log);
        }

        return genericIssues;

    }

    /**
     * admin service to attach files
     *
     * @param token
     * @param issueKey
     * @param attachmentData
     * @param url
     * @return
     * @throws IssueTrackerException
     * @throws java.io.IOException
     */
    public boolean attachFiles(String token, String issueKey, AttachmentData[] attachmentData, String url) throws IssueTrackerException, IOException {

        List<String> fileNames = new ArrayList<String>();
        List<String> data = new ArrayList<String>();

        for (AttachmentData attachment : attachmentData) {

            String fileName = attachment.getFileName();

            String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());

            fileNames.add(attachment.getFileName());

            if ("png".equals(fileExtension) || "jpg".equals(fileExtension) || "jpeg".equals(fileExtension)
                    || "gif".equals(fileExtension)) {

                FileOutputStream fileOutputStream = null;
                try {


                    File tempAttachment = new File(System.getProperty(IssueTrackerConstants.JAVA_IO_TEMP_DIR) + "/" + attachment.getFileName());

                    fileOutputStream = new FileOutputStream(tempAttachment);

                    attachment.getDataHandler().writeTo(fileOutputStream);
                    fileOutputStream.close();

                    BufferedImage _image = ImageIO.read(tempAttachment);

                    if (_image != null) {

                        java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
                        ImageIO.write(_image, "jpg", os);
                        byte[] bytes = os.toByteArray();
                        String image = new sun.misc.BASE64Encoder().encode(bytes);


                        data.add(image);
                        os.close();
                        boolean isDeleted = tempAttachment.delete();

                    }


                } catch (IOException e) {
                    ExceptionHandler.handleException("Unable to attach images ", e, log);
                } finally {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                }

            } else {


                try {
                    InputStream inputStream = attachment.getDataHandler().getDataSource().getInputStream();
                    byte[] bytes = new byte[inputStream.available()];
                    int size = inputStream.read(bytes);
                    String base64String = new sun.misc.BASE64Encoder().encode(bytes);

                    data.add(base64String);

                } catch (IOException e) {
                    ExceptionHandler.handleException("Unable to read attachments ", e, log);
                }
            }

        }

        JiraIssueReporter reporter = JiraIssueReporter.getInstance();

        String[] fileNameArray = fileNames.toArray(new String[fileNames.size()]);

        String[] dataArray = data.toArray(new String[data.size()]);

        return reporter.attachFiles(token, issueKey, fileNameArray, dataArray, url);
    }

    /**
     * admin service to attach bundle.info file
     *
     * @param token
     * @param issueKey
     * @param url
     * @return
     * @throws IssueTrackerException
     */
    public boolean attachBundleInfo(String token, String issueKey, String url) throws IssueTrackerException {

        boolean success = false;

        String carbonHome = new File("").getAbsolutePath();

        String bundlesInfoPath = carbonHome + IssueTrackerConstants.BUNDLE_INFO_PATH +
                IssueTrackerConstants.BUNDLE_INFO_FILE_NAME;

        File bundleInfoFile = new File(bundlesInfoPath);

        if (bundleInfoFile.exists()) {

            AttachmentData[] attachmentData = new AttachmentData[1];

            AttachmentData data = new AttachmentData();

            data.setFileName(IssueTrackerConstants.BUNDLE_INFO_FILE_NAME);

            DataHandler dataHandler = new DataHandler(new FileDataSource(bundleInfoFile));

            data.setDataHandler(dataHandler);

            attachmentData[0] = data;

            try {
                success = attachFiles(token, issueKey, attachmentData, url);
            } catch (IssueTrackerException e) {
                String msg = "Unable to attach bundles.info file " + carbonHome +
                        IssueTrackerConstants.BUNDLE_INFO_PATH + IssueTrackerConstants.BUNDLE_INFO_FILE_NAME;
                ExceptionHandler.handleException(msg, e, log);
            } catch (IOException e) {
                String msg = "Unable to attach bundles.info file " + carbonHome +
                        IssueTrackerConstants.BUNDLE_INFO_PATH + IssueTrackerConstants.BUNDLE_INFO_FILE_NAME;
                ExceptionHandler.handleException(msg, e, log);
            }

        }

        return success;
    }

    /**
     * admin service to attach thread dump to the issue
     *
     * @param token
     * @param issueKey
     * @param url
     * @return
     * @throws IssueTrackerException
     */
    public boolean attachThreadDump(String token, String issueKey, String url) throws IssueTrackerException {

        boolean success = false;
        ThreadDump threadDump = new ThreadDump();

        try {
            File file = threadDump.saveThreadDump();

            if (file.exists()) {

                AttachmentData[] attachmentData = new AttachmentData[1];

                AttachmentData data = new AttachmentData();

                data.setFileName(IssueTrackerConstants.THREAD_DUMP_FILE_NAME);

                DataHandler dataHandler = new DataHandler(new FileDataSource(file));

                data.setDataHandler(dataHandler);

                attachmentData[0] = data;

                success = attachFiles(token, issueKey, attachmentData, url);
            }
        } catch (IssueTrackerException e) {
            ExceptionHandler.handleException("Unable to attach bundles.info file", e, log);
        } catch (IOException e) {
            ExceptionHandler.handleException("Unable to attach bundles.info file", e, log);
        }
        return success;

    }

    /**
     * admin service to attach log file to the issue
     *
     * @param token
     * @param issueKey
     * @param url
     * @return
     * @throws IssueTrackerException
     */
    public boolean attachLogFile(String token, String issueKey, String url) throws IssueTrackerException {

        boolean success = false;

        String carbonHome = new File("").getAbsolutePath();

        String logFilePath = carbonHome + IssueTrackerConstants.LOG_FILE_PATH + IssueTrackerConstants.LOG_FILE_NAME;

        File logFile = new File(logFilePath);

        if (logFile.exists()) {

            AttachmentData[] attachmentData = new AttachmentData[1];

            AttachmentData data = new AttachmentData();

            data.setFileName(IssueTrackerConstants.LOG_FILE_NAME);

            DataHandler dataHandler = new DataHandler(new FileDataSource(logFilePath));

            data.setDataHandler(dataHandler);

            attachmentData[0] = data;

            try {
                success = attachFiles(token, issueKey, attachmentData, url);
            } catch (IssueTrackerException e) {
                String msg = "Unable to attach file " + carbonHome +
                        IssueTrackerConstants.LOG_FILE_PATH + IssueTrackerConstants.LOG_FILE_NAME;
                ExceptionHandler.handleException(msg, e, log);
            } catch (IOException e) {
                String msg = "Unable to attach file " + carbonHome +
                        IssueTrackerConstants.LOG_FILE_PATH + IssueTrackerConstants.LOG_FILE_NAME;
                ExceptionHandler.handleException(msg, e, log);
            }

        }

        return success;

    }

    /**
     * admin service to obtain issue count from a filter
     *
     * @param token
     * @param url
     * @return
     * @throws IssueTrackerException
     */
    public long getIssueCount(String token, String url) throws IssueTrackerException {

        JiraIssueReporter reporter = JiraIssueReporter.getInstance();

        long count = 0;

        try {
            count = reporter.getIssueCount(token, url);
        } catch (IssueTrackerException e) {
            ExceptionHandler.handleException("Error retrieving issue count.", e, log);
        }

        return count;
    }


    /**
     * admin service to obtain paginated issue info
     *
     * @param pageNumber
     * @param token
     * @param url
     * @return
     * @throws Exception
     */
    public PaginatedIssueInfo getPaginatedIssueInfo(int pageNumber, String token, String url) throws Exception {

        //page numbers starts from zero
        //1 is added to see whether there are more pages
        int maxNumberOfResults = (pageNumber + 1) * IssueTrackerConstants.ISSUES_PER_PAGE +
                IssueTrackerConstants.ISSUES_PER_PAGE * 3 + 1;

        List<GenericIssue> issueInfoList = retrieveIssues(token, url, maxNumberOfResults);

        // Pagination
        PaginatedIssueInfo paginatedIssueInfo = new PaginatedIssueInfo();
        DataPaginator.doPaging(pageNumber, issueInfoList, paginatedIssueInfo);
        return paginatedIssueInfo;
    }


    public PaginatedIssueInfo getPaginatedIssuesForTenant(int pageNumber, String token, String url) throws Exception {

        //page numbers starts from zero
        //1 is added to see whether there are more pages
        int maxNumberOfResults = (pageNumber + 1) * IssueTrackerConstants.ISSUES_PER_PAGE +
                IssueTrackerConstants.ISSUES_PER_PAGE * 3 + 1;

        String projectName = this.getJiraProjectFromConfig();
        List<GenericIssue> issueInfoList = retrieveIssuesByProject(token, url, projectName, maxNumberOfResults);
        // Pagination
        PaginatedIssueInfo paginatedIssueInfo = new PaginatedIssueInfo();
        DataPaginator.doPaging(pageNumber, issueInfoList, paginatedIssueInfo);
        return paginatedIssueInfo;
    }


    /**
     * admin service to delete issues
     *
     * @param token    authn token
     * @param issueKey issue key
     * @param url      JIRA url
     * @return true if the issue is deleted
     * @throws IssueTrackerException thrown in case of a failure to establish the connection with JiraSoapService or an
     *                               erron in deleting the issue.
     */
    public boolean deleteIssue(String token, String issueKey, String url) throws IssueTrackerException {

        boolean isIssueDeleted = false;

        JiraIssueReporter reporter = JiraIssueReporter.getInstance();

        try {
            isIssueDeleted = reporter.deleteIssues(token, issueKey, url);
        } catch (IssueTrackerException e) {
            ExceptionHandler.handleException("Error deleting the issue " + issueKey + ".", e, log);
        }

        return isIssueDeleted;

    }

    /**
     * method to check whether this is a product or service. this is needed since we are not allowing to add
     * accounts in stratos environment
     *
     * @return
     * @throws IssueTrackerException
     */
    public boolean isStratosService() throws IssueTrackerException {

        boolean isStratosService = false;

        Map<String, CloudServiceConfig> cloudServiceConfigs = null;
        try {
            cloudServiceConfigs = CloudServiceConfigParser.
                    loadCloudServicesConfiguration().getCloudServiceConfigs();
        } catch (Exception e) {
            ExceptionHandler.handleException("Error reading cloud configurations.", e, log);
        }


        String serviceName = ServerConfiguration.getInstance().getFirstProperty("Name");

        if (cloudServiceConfigs != null) {

            Set<String> configKeys = cloudServiceConfigs.keySet();
            for (String configKey : configKeys) {
                CloudServiceConfig cloudServiceConfig = cloudServiceConfigs.get(configKey);
                String name = cloudServiceConfig.getName();
                if (name.equals(serviceName)) {
                    isStratosService = true;
                    break;
                }

            }
        }
        return isStratosService;
    }


    String getJiraUrlFromConfig() throws IssueTrackerException {
        String url = "";
        url = CommonUtil.getStratosConfig().getPaidJIRAUrl();
        return url;
    }


    private String getJiraProjectFromConfig() throws IssueTrackerException {
        String project = "";
        project = CommonUtil.getStratosConfig().getPaidJIRAProject();
        return project;
    }

    /**
     * this method is used in stratos environment when the project is fixed to obtain issue types and priorities
     *
     * @return
     */
    public String getAccountSpecificDataInService() throws IssueTrackerException {

        int tenantId = CarbonContext.getCurrentContext().getTenantId();

        String accountName;
        if (this.isTenantSubscriptionFree(tenantId)) {
            accountName = tenantId + IssueTrackerConstants.JIRA_CREDENTIALS_REGISTRY_PREFIX_NONPAID;
        } else {
            accountName = tenantId + IssueTrackerConstants.JIRA_CREDENTIALS_REGISTRY_PREFIX_PAID;
        }

        AccountInfo accountInfo = this.getAccountWhenService();

        JSONObject jsonObject;
        if (null != accountInfo) {
            String token = this.login(accountInfo.getCredentials());
            String url = accountInfo.getCredentials().getUrl();
            jsonObject = new JSONObject();
            try {
                jsonObject = this.getIssueTypes(token, jsonObject, url);
                jsonObject = this.getPriorityTypes(token, jsonObject, url);
            } catch (IssueTrackerException e) {
                ExceptionHandler.handleException("Error obtaining project and issue types ", e, log);
            }
        } else {
            jsonObject = new JSONObject();
            try {
                jsonObject.put("success", "fail");
            } catch (JSONException e) {
                ExceptionHandler.handleException("Error creating JSON string.", e, log);
            }

        }

        return jsonObject.toString();
    }

    /**
     * admin service to check whether the active subscription of tenant is free or not. this is required to determine
     * whether the user need to be redirected to forum or JIRA
     *
     * @return
     * @throws IssueTrackerException
     */
    public boolean isTenantSubscriptionFree() throws IssueTrackerException {

        String cookie = null;
        String epr = getMangerEpr();
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        ConfigurationContext configContext = IssueTrackerAdminServiceComponent.getConfigCtxService().getClientConfigContext();

        String sessionCookie = getAdminCookieForManager(epr + "/services/", configContext);

        BillingDataAccessServiceStub stub = getBillingDataAccessServiceStub(epr, configContext, sessionCookie);

        Subscription subscription = getActiveSubscriptionOfTenant(tenantId, stub);

        String subscriptionPlan = "";
        if (null != subscription) {
            subscriptionPlan = subscription.getSubscriptionPlan();
        }

        if (IssueTrackerConstants.FREE_SUBSCRIPTION_PLAN_NAME.equals(subscriptionPlan) || null == subscription) {
            return true;
        } else {
            return false;
        }

    }


    boolean isTenantSubscriptionFree(int tenantId) throws IssueTrackerException {


        String cookie = null;
        String epr = getMangerEpr();
        ConfigurationContext configContext = IssueTrackerAdminServiceComponent.getConfigCtxService().getClientConfigContext();

        String sessionCookie = getAdminCookieForManager(epr + "/services/", configContext);

        BillingDataAccessServiceStub stub = getBillingDataAccessServiceStub(epr, configContext, sessionCookie);

        Subscription subscription = getActiveSubscriptionOfTenant(tenantId, stub);

        String subscriptionPlan = "";
        if (null != subscription) {
            subscriptionPlan = subscription.getSubscriptionPlan();
        }

        if (IssueTrackerConstants.FREE_SUBSCRIPTION_PLAN_NAME.equals(subscriptionPlan) || null == subscription) {
            return true;
        } else {
            return false;
        }

    }

    private Subscription getActiveSubscriptionOfTenant(int tenantId, BillingDataAccessServiceStub stub) throws IssueTrackerException {
        Subscription subscription = null;
        try {
            if (stub != null) {
                subscription = stub.getActiveSubscriptionOfCustomerByTenant();
            }
        } catch (RemoteException e) {
            ExceptionHandler.handleException("Error reading subscription for tenant tid = " + tenantId, e, log);
        } catch (BillingDataAccessServiceExceptionException e) {
            ExceptionHandler.handleException("Error reading subscription for tenant tid = " + tenantId, e, log);
        }
        return subscription;
    }

    private BillingDataAccessServiceStub getBillingDataAccessServiceStub(String epr, ConfigurationContext configContext, String sessionCookie) throws IssueTrackerException {
        BillingDataAccessServiceStub stub = null;

        try {
            stub = new BillingDataAccessServiceStub(configContext, epr + "/services/BillingDataAccessService");
            ServiceClient serviceClient = stub._getServiceClient();
            Options options = serviceClient.getOptions();
            options.setManageSession(true);
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                    sessionCookie);

        } catch (AxisFault axisFault) {
            ExceptionHandler.handleException("Error reading cloud configurations.", axisFault, log);
        }
        return stub;
    }

    private String getMangerEpr() throws IssueTrackerException {

        String epr = "";
        Map<String, CloudServiceConfig> cloudServiceConfigs = null;
        try {
            cloudServiceConfigs = CloudServiceConfigParser.
                    loadCloudServicesConfiguration().getCloudServiceConfigs();
        } catch (Exception e) {
            ExceptionHandler.handleException("Error reading cloud configurations.", e, log);
        }

        String serviceName = ServerConfiguration.getInstance().getFirstProperty("Name");

        if (cloudServiceConfigs != null) {

            Set<String> configKeys = cloudServiceConfigs.keySet();
            for (String configKey : configKeys) {
                CloudServiceConfig cloudServiceConfig = cloudServiceConfigs.get(configKey);
                String name = cloudServiceConfig.getName();
                if (name.equals(IssueTrackerConstants.MANAGER_SERVICE_NAME)) {
                    epr = cloudServiceConfig.getLink();
                }

            }
        }
        return epr;
    }

    private String getAdminCookieForManager(String managerURL, ConfigurationContext configContext) throws IssueTrackerException {
        String sessionCookie = null;
        AuthenticationAdminClient client = null;
        String username = null, password = null;

        ManagerConfigurations managerConfigurations = IssueTrackerAdminServiceComponent.getManagerConfigurations();
        try {
            username = managerConfigurations.getStratosConfiguration().getAdminUserName();
            password = managerConfigurations.getStratosConfiguration().getAdminPassword();
        } catch (Exception e) {
            ExceptionHandler.handleException("Error obtaining manager configurations from throttling-agent-config.xml ", e, log);
        }

        try {
            client = new AuthenticationAdminClient(configContext,
                    managerURL, null, null, false);
        } catch (AxisFault axisFault) {
            ExceptionHandler.handleException("Error constructing AuthenticationAdminClient ", axisFault, log);
        }
        //TODO : get the correct IP
        boolean isLogin = false;

        try {
            if (client != null) {
                isLogin = client.login(username, password, "127.0.0.1");
            }
        } catch (AuthenticationException e) {
            ExceptionHandler.handleException("Error login in via AuthenticationAdminClient. ", e, log);
        }
        if (isLogin) {
            sessionCookie = client.getAdminCookie();
        }
        return sessionCookie;
    }


    public String getSupportInfoUrl() {
        StratosConfiguration stratosConfiguration = CommonUtil.getStratosConfig();

        if (null != stratosConfiguration) {
            String url = stratosConfiguration.getSupportInfoUrl();
            if (null != url) {
                return url;
            } else {
                return " ";
            }
        } else {
            return " ";
        }
    }

    private String getAssigneeInService(String projectKey) throws IssueTrackerException {

        StratosConfiguration stratosConfiguration = CommonUtil.getStratosConfig();
        String jiraConnectionName = stratosConfiguration.getStratosEventListenerPropertyValue("jiraConnectionName");
        String jiraConnectionPass = stratosConfiguration.getStratosEventListenerPropertyValue("jiraConnectionPass");
        String jiraServerUrl = stratosConfiguration.getStratosEventListenerPropertyValue("jiraServerUrl");
        String projectLead = null;

        GenericCredentials credentials = new GenericCredentials();
        credentials.setUsername(jiraConnectionName);
        credentials.setPassword(jiraConnectionPass);
        credentials.setUrl(jiraServerUrl);

        JiraIssueReporter reporter = JiraIssueReporter.getInstance();
        try {
            String token = reporter.login(credentials);
            reporter.getProjectLead(token, projectKey);
        } catch (IssueTrackerException e) {
            ExceptionHandler.handleException("Error obtaining project lead. ", e, log);
        }
        return projectLead;

    }


    private List<GenericIssue> retrieveIssuesByProject(String token, String url, String projectName, int maxResults) throws IssueTrackerException {

        int tenantId = CarbonContext.getCurrentContext().getTenantId();

        List<GenericIssue> genericIssues = new ArrayList<GenericIssue>();
        JiraIssueReporter reporter = JiraIssueReporter.getInstance();
        try {

            genericIssues = reporter.retrieveIssuesByQuery(token, url, maxResults, projectName);

        } catch (IssueTrackerException e) {
            ExceptionHandler.handleException("Unable to retrieve issues for ", e, log);
        }

        return genericIssues;

    }


    public String createUserInOT(GenericUser user) throws IssueTrackerException {
        OTUserAssociation otUserAssociation = new OTUserAssociation();
        String username = "";
        try {
            username = otUserAssociation.createUserInOTLDAP(user);
        } catch (IssueTrackerException e) {
            ExceptionHandler.handleException("Unable to create an OT account. ", e, log);
        }

        // persist account info in registry
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setEmail(user.getEmail());
        accountInfo.setUid(username);
        accountInfo.setHasSupportAccount(false);

        GenericCredentials credentials = new GenericCredentials();
        credentials.setUsername(user.getEmail());
        accountInfo.setCredentials(credentials);

        this.captureAccountInfo(accountInfo);

        return username;
    }


    public boolean associateUserWithSupportJira(SupportJiraUser supportJiraUser) throws IssueTrackerException {

        boolean success = false;

        String jiraUrl = null;
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        JiraIssueReporter reporter = JiraIssueReporter.getInstance();

        OTUserAssociation otUserAssociation = new LdapGroupManager();

        String authToken = null;
        try {
            authToken = otUserAssociation.getJiraAdminAuthToken();
        } catch (IssueTrackerException e) {
            ExceptionHandler.handleException("Unable to obtain admin authentication token ", e, log);
        }

        // set jira url based on subscription

        GenericCredentials credentials = supportJiraUser.getCredentials();
        if (null != credentials) {
            jiraUrl = this.getJiraUrlFromConfig();
            credentials.setUrl(jiraUrl);
        }

        try {

            //if firstname. lastname. email is not given,set the values to tenant's values
            String firstName = supportJiraUser.getFirstName();
            String lastName = supportJiraUser.getLastName();

            if (null == firstName || null == lastName || "".equals(firstName) || "".equals(lastName)) {
                supportJiraUser = otUserAssociation.setDefaultValues(supportJiraUser, tenantId);
            }

            RemoteUser remoteUser = reporter.createUserInJIRA(supportJiraUser, authToken);

            // add the user to relevant group
            boolean isSubscriptionFree = this.isTenantSubscriptionFree(tenantId);
            otUserAssociation.addUserToGroup(authToken, supportJiraUser, isSubscriptionFree, jiraUrl);

            //persist account details to registry
            AccountInfo accountInfo = new AccountInfo();
            accountInfo.setCredentials(credentials);
            accountInfo.setEmail(supportJiraUser.getEmail());
            accountInfo.setUid(supportJiraUser.getUsername());
            accountInfo.setHasSupportAccount(true);
            this.captureAccountInfo(accountInfo);
            success = true;

        } catch (IssueTrackerException e) {
            ExceptionHandler.handleException("Unable to create user" + " in JIRA", e, log);
        }

        return success;
    }


    public GenericUser authenticateWithOT(GenericCredentials credentials) throws IssueTrackerException {

        GenericUser user = null;
        OTUserAssociation otUserAssociation = new OTUserAssociation();
        try {
            user = otUserAssociation.authenticateWithOT(credentials);
        } catch (IssueTrackerException e) {
            ExceptionHandler.handleException("Unable to authenticate user in OT for the email" + credentials.getUsername(), e, log);
        }

        return user;

    }


    public String getForumUrl() {

        String url = "";
        StratosConfiguration stratosConfiguration = CommonUtil.getStratosConfig();
        if (null != stratosConfiguration) {
            url = stratosConfiguration.getForumUrl();
        }

        return url;
    }

}

  


