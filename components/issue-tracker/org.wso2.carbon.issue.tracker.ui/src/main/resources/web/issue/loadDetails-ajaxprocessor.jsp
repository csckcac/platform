<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the Lice nse.
-->
<%@ page import="org.wso2.carbon.issue.tracker.ui.IssueTrackerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.issue.tracker.stub.*" %>
<%

    String url = request.getParameter("url");
    String username=request.getParameter("username");
    String password=request.getParameter("password");

    if (null != url && null !=username && null != password) {

        IssueTrackerClient issueTrackerClient = IssueTrackerClient.getInstance(config, session);
        GenericCredentials credentials =new GenericCredentials();
        credentials.setUrl(url);
        credentials.setUsername(username);
        credentials.setPassword(password);
        String details = issueTrackerClient.getAccountSpecificDetails(credentials);

        out.println(details);

    } else {

    }
%>