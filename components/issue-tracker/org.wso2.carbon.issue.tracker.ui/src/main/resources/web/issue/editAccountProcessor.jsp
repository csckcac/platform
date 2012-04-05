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
<%@ page import="org.wso2.carbon.issue.tracker.stub.AccountInfo" %>
<%@ page import="org.wso2.carbon.issue.tracker.stub.AutoReportingSettings" %>
<%@ page import="org.wso2.carbon.issue.tracker.stub.GenericCredentials" %>
<%@ page import="org.wso2.carbon.issue.tracker.ui.IssueTrackerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<div id="middle">


    <%
        IssueTrackerClient client;
        AccountInfo accountInfo = new AccountInfo();
        String oldKey;
        try {

            client = IssueTrackerClient.getInstance(config, session);

            String isStratosService = request.getParameter("isStratosService");
            String email = request.getParameter("email").trim();

            String uid = request.getParameter("username");

            if (isStratosService.equals(false)) {
                String key = request.getParameter("key").trim();


                oldKey = request.getParameter("oldKey").trim();

                accountInfo.setKey(key);

            } else {
                accountInfo = client.getAccount();
                oldKey = accountInfo.getKey();
            }

            GenericCredentials credentials = new GenericCredentials();

            if (isStratosService.equals(false)) {
                credentials.setUrl(request.getParameter("URL").trim());
            }
            credentials.setUsername(email);

            credentials.setPassword(request.getParameter("password").trim());

            accountInfo.setCredentials(credentials);

            accountInfo.setEmail(email);


            accountInfo.setUid(uid);
            String enableAutoReporting = request.getParameter("autoReporting");


            if (null != enableAutoReporting && "on".equals(enableAutoReporting)) {
                accountInfo.setAutoReportingEnable(true);
                AutoReportingSettings settings = new AutoReportingSettings();

                settings.setProjectName(request.getParameter("projectList"));

                String priority = request.getParameter("priority");
                if (null != priority && !"--Select--".equals(priority)) {
                    settings.setPriority(priority);
                }

                String type = request.getParameter("type");
                if (null != type && !"--Select--".equals(type)) {
                    settings.setIssueType(type);
                }


                accountInfo.setAutoReportingSettings(settings);

            } else {
                accountInfo.setAutoReportingEnable(false);
            }


            boolean valid = client.validateCredentials(credentials);


            if (valid) {

                client.deleteAccount(oldKey);
                client.persistAccount(accountInfo);
    %>

    <script type="text/javascript">
        CARBON.showInfoDialog('Account is successfully edited.');
    </script>

    <%


    } else {

    %>
    <script type="text/javascript">
        CARBON.showErrorDialog('Error saving the account details.Authentication failure or you dont have necessary permission to connect to the JIRA. Please re check your email and password. If you are getting the issue still, please contact system administrator.');
    </script>
    <%
        }

    } catch (Exception e) {

    %>
    <script type="text/javascript">
        CARBON.showErrorDialog('Unable to edit JIRA account. Incorrect user name and password or unreachable url is given.');
    </script>
    <%

        }
    %>
</div>

<script type="text/javascript">
    location.href = "viewAccounts.jsp";
</script>