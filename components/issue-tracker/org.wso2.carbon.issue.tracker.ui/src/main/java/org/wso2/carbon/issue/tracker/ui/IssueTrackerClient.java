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
package org.wso2.carbon.issue.tracker.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.issue.tracker.stub.*;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;


/**
 * client class of Issue Tracker. this class consumes services of IssueTrackerAdmin
 */

public class IssueTrackerClient {

    private static final Log log = LogFactory.getLog(IssueTrackerClient.class);

    private IssueTrackerAdminStub stub;

    public IssueTrackerClient(String cookie,
                              String backendServerURL,
                              ConfigurationContext configCtx) throws AxisFault {

        String serviceURL = backendServerURL + "IssueTrackerAdmin";
        stub = new IssueTrackerAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

    }


    public static IssueTrackerClient getInstance(ServletConfig config, HttpSession session)
            throws AxisFault {

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        return new IssueTrackerClient(cookie, backendServerURL, configContext);
    }


    public String login(GenericCredentials credentials) throws
            RemoteException {
        try {
            return stub.login(credentials);
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error login : ", e);
        }

    }

    public boolean validateCredentials(GenericCredentials credentials) throws AxisFault {
        String token;
        try {
            token = stub.validateCredentials(credentials);
        } catch (RemoteException e) {
            throw new AxisFault("Error authenticating credentials.", e);
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error authenticating credentials.", e);
        }

        return (null != token && !"".equals(token));

    }

    public void persistAccount(AccountInfo accountInfo) throws AxisFault {

        try {
            stub.captureAccountInfo(accountInfo);
        } catch (RemoteException e) {
            throw new AxisFault("Error persisting account information.", e);
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error persisting account information.", e);
        }
    }

    public String createIssue(GenericIssue issue, String token, String url) throws AxisFault {
        String issueKey;
        try {
            issueKey = stub.captureIssueInfo(issue, token, url);

        } catch (RemoteException e) {
            throw new AxisFault("Error creating issue. ", e);
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error creating issue. ", e);
        }

        return issueKey;

    }

    public AccountInfo[] getAccountInfo() throws AxisFault {

        AccountInfo[] serverInfo;
        try {
            serverInfo = stub.getAccountInfo();
        } catch (RemoteException e) {
            throw new AxisFault("Error retrieving data from registry");
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error retrieving data from registry");
        }

        return serverInfo;
    }

    public String[] getAccountNames() throws AxisFault {
        String[] serverNames;
        try {
            serverNames = stub.getAccountNames();
        } catch (RemoteException e) {
            throw new AxisFault("Error retrieving account data");
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error retrieving account data");
        }


        return serverNames;
    }

    public boolean deleteAccount(String key) throws AxisFault {

        boolean isDeleted;
        try {
            isDeleted = stub.deleteAccount(key);
        } catch (RemoteException e) {
            throw new AxisFault("Error deleting account" + key);
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error deleting account" + key);
        }
        return isDeleted;

    }

    public AccountInfo getAccount(String key) throws AxisFault {

        AccountInfo accountInfo;

        try {
            accountInfo = stub.getAccount(key);

        } catch (RemoteException e) {
            throw new AxisFault("Error obtaining credentials for the account " + key + "from registry");
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error obtaining credentials for the account " + key + "from registry");
        }

        return accountInfo;

    }


    public AccountInfo getAccount() throws AxisFault {

        AccountInfo accountInfo;

        try {
            accountInfo = stub.getAccountWhenService();

        } catch (RemoteException e) {
            throw new AxisFault("Error obtaining credentials for the account from registry");
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error obtaining credentials for the account from registry");
        }

        return accountInfo;

    }


    public String getAccountSpecificData(String accountName, String url) throws AxisFault {

        String data = null;

        try {
            data = stub.getAccountSpecificData(accountName, url);

        } catch (RemoteException e) {
            throw new AxisFault("Error retrieving available projects,types or priorities.");
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error retrieving available projects,types or priorities.");
        }
        return data;

    }

    public String getAccountSpecificDetails(GenericCredentials credentials) throws AxisFault {

        try {

            return stub.getAccountSpecificDetails(credentials);
        } catch (RemoteException e) {
            throw new AxisFault("Error retrieving available projects,types or priorities.");
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error retrieving available projects,types or priorities.");
        }
    }


    public String getAccountSpecificDataInService() throws AxisFault {

        try {
            return stub.getAccountSpecificDataInService();
        } catch (RemoteException e) {
            throw new AxisFault("Error retrieving available types or priorities.");
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error retrieving available types or priorities.");
        }
    }


    public void uploadAttachments(String token, String issueKey, AttachmentData[] attachmentData, String url) throws AxisFault {

        try {
            stub.attachFiles(token, issueKey, attachmentData, url);
        } catch (RemoteException e) {
            throw new AxisFault("Error attaching files to " + issueKey);
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error attaching files to " + issueKey);
        } catch (IssueTrackerAdminIOExceptionException e) {
            throw new AxisFault("Error attaching files to " + issueKey);
        }

    }

    public void uploadBundleInfo(String token, String issueKey, String url) throws AxisFault {

        try {
            stub.attachBundleInfo(token, issueKey, url);
        } catch (RemoteException e) {
            throw new AxisFault("Error attaching bundles.info file to " + issueKey);
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error attaching bundles.info file to " + issueKey);
        }
    }

    public boolean uploadThreadDump(String token, String issueKey, String url) throws AxisFault {
        boolean isUploaded;
        try {
            isUploaded = stub.attachThreadDump(token, issueKey, url);
        } catch (RemoteException e) {
            throw new AxisFault("Error attaching thread dump file to " + issueKey);
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error attaching thread dump file to " + issueKey);
        }

        return isUploaded;
    }

    public boolean uploadLogFile(String token, String issueKey, String url) throws AxisFault {
        boolean isUploaded;
        try {
            isUploaded = stub.attachLogFile(token, issueKey, url);
        } catch (RemoteException e) {
            throw new AxisFault("Error attaching log file to " + issueKey);
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error attaching log file to " + issueKey);
        }
        return isUploaded;
    }

    public long getIssueCount(String token, String url) throws AxisFault {

        long issueCount = 0;
        try {
            issueCount = stub.getIssueCount(token, url);
        } catch (RemoteException e) {
            throw new AxisFault("Error retrieving issue count");
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error retrieving issue count");
        }
        return issueCount;
    }

    public PaginatedIssueInfo retrievePaginatedIssueInfo(String token, String url, int pageNumber) throws AxisFault {

        PaginatedIssueInfo issueInfo;
        try {
            issueInfo = stub.getPaginatedIssueInfo(pageNumber, token, url);
        } catch (RemoteException e) {
            throw new AxisFault("Error retrieving issues");
        } catch (IssueTrackerAdminExceptionException e) {
            throw new AxisFault("Error retrieving issues");
        }
        return issueInfo;

    }


    public boolean deleteIssue(String token, String issueKey, String url) throws AxisFault {

        try {
            return stub.deleteIssue(token, issueKey, url);
        } catch (RemoteException e) {
            throw new AxisFault("Error deleting the issue.");
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error deleting the issue.");
        }


    }

    public boolean isStratosService() throws AxisFault {

        try {
            return stub.isStratosService();
        } catch (RemoteException e) {
            throw new AxisFault("Error checking whether a stratos service.");
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error checking whether a stratos service.");
        }
    }


    public String getSupportInfoUrl() throws AxisFault {

        try {
            return stub.getSupportInfoUrl();
        } catch (RemoteException e) {
            throw new AxisFault("Error obtaining url of support info page.");
        }


    }

    // method to obtain issues in a service

    public PaginatedIssueInfo getPaginatedIssuesForTenant(String token, String url, int pageNumber) throws AxisFault {

        PaginatedIssueInfo issueInfo;
        try {
            issueInfo = stub.getPaginatedIssuesForTenant(pageNumber, token, url);
        } catch (RemoteException e) {
            throw new AxisFault("Error retrieving issues");
        } catch (IssueTrackerAdminExceptionException e) {
            throw new AxisFault("Error retrieving issues");
        }
        return issueInfo;

    }


    public String createUserInOT(GenericUser user) throws AxisFault {
        String username;
        try {
            username = stub.createUserInOT(user);

        } catch (RemoteException e) {
            throw new AxisFault("Error creating user in OT");
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error creating user in OT");
        }
        return username;

    }

    public boolean createSupportAccount(SupportJiraUser supportJiraUser) throws AxisFault {
        boolean success = false;
        try {
            success = stub.associateUserWithSupportJira(supportJiraUser);

        } catch (RemoteException e) {
            throw new AxisFault("Error creating user in support JIRA");
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error creating user in support JIRA");
        }

        return success;

    }

    public GenericUser authenticateWithOT(GenericCredentials credentials) throws AxisFault {
        try {
            return stub.authenticateWithOT(credentials);
        } catch (RemoteException e) {
            throw new AxisFault("Error authenticating user in OT.");
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error authenticating user in OT.");
        }
    }


    public boolean isTenantSubscriptionFree() throws AxisFault {

        try {
            return stub.isTenantSubscriptionFree();
        } catch (RemoteException e) {
            throw new AxisFault("Error obtaining tenant subscription.");
        } catch (IssueTrackerAdminIssueTrackerExceptionException e) {
            throw new AxisFault("Error obtaining tenant subscription.");
        }
    }

    public String getForumLink() throws AxisFault {
         try {
        return stub.getForumUrl();
              } catch (RemoteException e) {
            throw new AxisFault("Error obtaining tenant subscription.");
        } 
    }


}
