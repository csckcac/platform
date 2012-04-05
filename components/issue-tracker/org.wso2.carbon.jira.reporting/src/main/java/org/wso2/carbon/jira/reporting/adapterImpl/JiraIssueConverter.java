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
package org.wso2.carbon.jira.reporting.adapterImpl;

import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.issue.tracker.adapter.api.GenericIssue;
import org.wso2.carbon.issue.tracker.adapter.api.IssueConverter;
import org.wso2.carbon.issue.tracker.adapter.exceptions.IssueTrackerException;
import org.wso2.carbon.issue.tracker.core.ExceptionHandler;
import org.wso2.carbon.issue.tracker.core.OperatingEnvironment;

import java.util.Calendar;


public class JiraIssueConverter implements IssueConverter<RemoteIssue> {

    private final static Log log = LogFactory.getLog(JiraIssueConverter.class);


    public RemoteIssue getSpecificIssue(GenericIssue genericIssue) throws IssueTrackerException {

        RemoteIssue remoteIssue = new RemoteIssue();

        setProject(genericIssue, remoteIssue);

        setSummary(genericIssue, remoteIssue);

        setDescription(genericIssue, remoteIssue);

        setAssignee(genericIssue, remoteIssue);

        setType(genericIssue, remoteIssue);

        setPriority(genericIssue, remoteIssue);

        setReporter(genericIssue, remoteIssue);

        setDue(genericIssue, remoteIssue);

        setCreated(genericIssue, remoteIssue);

        setEnvironment(remoteIssue);

        //todo setting components,versions,attachments

        return remoteIssue;


    }

    private void setEnvironment(RemoteIssue remoteIssue) {
        OperatingEnvironment environment = new OperatingEnvironment();

        String environmentData = OperatingEnvironment.getEnvironmentData(environment);

        remoteIssue.setEnvironment(environmentData);
    }

    private void setCreated(GenericIssue genericIssue, RemoteIssue remoteIssue) {

        Calendar created = genericIssue.getCreated();

        if (null != created) {

            remoteIssue.setCreated(created);
        }
    }

    private void setDue(GenericIssue genericIssue, RemoteIssue remoteIssue) {

        Calendar due = genericIssue.getDueDate();

        if (null != due) {
            remoteIssue.setDuedate(due);
        }
    }

    private void setReporter(GenericIssue genericIssue, RemoteIssue remoteIssue) {

        String reporter = genericIssue.getReporter();

        if (null != reporter) {

            remoteIssue.setReporter(reporter);
        }
    }

    private void setPriority(GenericIssue genericIssue, RemoteIssue remoteIssue) {

        String priority = genericIssue.getPriority();

        if (null != priority) {

            remoteIssue.setPriority(priority);
        }
    }

    private void setType(GenericIssue genericIssue, RemoteIssue remoteIssue) {

        String type = genericIssue.getType();

        if (null != type) {

            remoteIssue.setType(type);
        }
    }

    private void setAssignee(GenericIssue genericIssue, RemoteIssue remoteIssue) {


        String assignee = genericIssue.getAssignee();

        if (null != assignee) {

            remoteIssue.setAssignee(assignee);
        }
    }

    private void setDescription(GenericIssue genericIssue, RemoteIssue remoteIssue) {

        String desc = genericIssue.getDescription();

        if (null != desc) {

            remoteIssue.setDescription(desc);

        }
    }

    private void setSummary(GenericIssue genericIssue, RemoteIssue remoteIssue) {

        String summary = genericIssue.getSummary();

        if (null != summary) {

            remoteIssue.setSummary(summary);

        }
    }

    private void setProject(GenericIssue genericIssue, RemoteIssue remoteIssue) throws IssueTrackerException {

        String projectKey = genericIssue.getProjectKey();

        if (null != projectKey) {

            remoteIssue.setProject(projectKey);

        } else {

            ExceptionHandler.handleException("Missing project name, unable to create issue. ", log);

        }
    }


}
