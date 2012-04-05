<%@ page import="org.wso2.carbon.issue.tracker.stub.GenericCredentials" %>
<%@ page import="org.wso2.carbon.issue.tracker.stub.GenericUser" %>
<%@ page import="org.wso2.carbon.issue.tracker.stub.SupportJiraUser" %>
<%@ page import="org.wso2.carbon.issue.tracker.ui.IssueTrackerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
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

<%
    try {
        IssueTrackerClient client;

        client = IssueTrackerClient.getInstance(config, session);

        GenericUser user = (GenericUser) session.getAttribute("user");
        session.removeAttribute("user");


        //get the email and password
        GenericCredentials credentials = new GenericCredentials();
        String email = request.getParameter("email").trim();
//        String username = request.getParameter("username").trim();

        //here we use email since we use email to login
        credentials.setUsername(email);

        String password = request.getParameter("password").trim();
        credentials.setPassword(password);

        //validate against OT LDAP
        GenericUser genericUser = client.authenticateWithOT(credentials);

        // if username is empty get the value sent upone authenticating with OT

        if (null == genericUser) {
            // validation with OT has failed


%>
<script type="text/javascript">
    location.href = "newOTAccount.jsp";
</script>
<%


} else {


    //set values for a support user
    SupportJiraUser supportJiraUser = new SupportJiraUser();
    //set the username of credentials as OT uid of the user imported from OT
    // remove this line to login using email
    //credentials.setUsername(genericUser.getUsername());
    supportJiraUser.setCredentials(credentials);



    supportJiraUser.setUsername(genericUser.getUsername());
    supportJiraUser.setEmail(email);

    if (null != user) {
        supportJiraUser.setEmail(user.getEmail());

        supportJiraUser.setFirstName(user.getFirstName());
        supportJiraUser.setLastName(user.getLastName());

    }
    // if valid,create user in support


    boolean success = client.createSupportAccount(supportJiraUser);

    if (success) {
%>
<script type="text/javascript">
    location.href = "viewAccounts.jsp?success=true";
</script>

<%

} else {
%>
<script type="text/javascript">
    CARBON.showErrorDialog('Error creating a support account. Please contact system administrator for more details.');
</script>
<%
        }

    }

    //else error message

} catch (Exception e) {
    CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request,
            e);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
        return;
    }
%>
