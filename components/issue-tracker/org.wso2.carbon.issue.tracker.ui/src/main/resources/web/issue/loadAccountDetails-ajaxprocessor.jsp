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
<%@ page import="org.wso2.carbon.issue.tracker.stub.GenericCredentials" %>
<%@ page import="org.wso2.carbon.issue.tracker.ui.IssueTrackerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%

    String url = request.getParameter("URL").trim();
    String username= request.getParameter("username").trim();
    String password = request.getParameter("password").trim();
        
    GenericCredentials credentials = new GenericCredentials();
    credentials.setUrl(url);

    credentials.setUsername(username);

    credentials.setPassword(password);

    IssueTrackerClient client = IssueTrackerClient.getInstance(config, session);

    if (client.validateCredentials(credentials)) {

        String details = client.getAccountSpecificDetails(credentials);     

        out.println(details);

    } else {
        String msg = "Unable to obtain project list ";
        CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request,
                response, "../issue/addAccount.jsp");
    }

       

%>