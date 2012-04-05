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

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira_soapclient.SOAPSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.spi.LoggingEvent;
import org.wso2.carbon.issue.tracker.adapter.api.GenericIssue;
import org.wso2.carbon.issue.tracker.adapter.exceptions.IssueTrackerException;
import org.wso2.carbon.issue.tracker.core.AccountInfo;
import org.wso2.carbon.issue.tracker.mgt.IssueTrackerAdmin;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;

/**
 * This class used to create and submit issues to the jira. To be used for automatic issue creation
 */
public class JiraCreateTask implements Runnable {

    private static final Log log = LogFactory.getLog(JiraCreateTask.class);

    private JiraSoapService jiraSoapService;
    private LoggingEvent loggingEvent;

    public JiraCreateTask(LoggingEvent loggingEvent) {
        this.loggingEvent = loggingEvent;
    }

    /**
     * Authentication to the jira system and the issue creation is happening here
     */
    public void run() {


        IssueTrackerAdmin admin = new IssueTrackerAdmin();

        List<AccountInfo> accountList;
        try {
            accountList = admin.getAccountInfo();
        } catch (IssueTrackerException e) {
            log.error("Error occured while ", e);
            return;
        }

        for (AccountInfo account : accountList) {

            if (account.isAutoReportingEnable()) {

                if (log.isDebugEnabled()) {
                    log.debug("Reporting to project : \n\n" + account.getAutoReportingSettings().getProjectName());
                }

                String token = null;
                try {
                  token=  admin.login(account.getCredentials());
                } catch (IssueTrackerException e) {
                     log.error("Error connceting to JIRA",e);
                }

                GenericIssue genericIssue = new GenericIssue();
                genericIssue.setProjectKey(account.getAutoReportingSettings().getProjectName());
                genericIssue.setPriority(account.getAutoReportingSettings().getPriority());
                genericIssue.setType(account.getAutoReportingSettings().getIssueType());
                genericIssue.setSummary(loggingEvent.getRenderedMessage());

                // getting and adding the throwable information to the remote issue
                String throwableStr[] = loggingEvent.getThrowableStrRep();
                if (throwableStr != null) {
                    StringBuilder stringBuilder = new StringBuilder();

                    for (String description : throwableStr) {
                        stringBuilder.append(description).append("\n");
                    }
                    genericIssue.setDescription(stringBuilder.toString());
                }


                try {
                    admin.captureIssueInfo(genericIssue,token,account.getCredentials().getUrl());
                } catch (IssueTrackerException e) {
                   log.error("Error reporting error to JIRA",e);
                }


            }


        }



    }

}

