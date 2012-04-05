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
<%@ page import="org.wso2.carbon.issue.tracker.stub.GenericUser" %>
<%@ page import="org.wso2.carbon.issue.tracker.stub.AutoReportingSettings" %>
<%@ page import="org.wso2.carbon.issue.tracker.stub.GenericCredentials" %>
<%@ page import="org.wso2.carbon.issue.tracker.ui.IssueTrackerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<%

    try {
        IssueTrackerClient client = IssueTrackerClient.getInstance(config, session);
        GenericUser user = new GenericUser();
        String email = request.getParameter("email").trim();
        String firstName = request.getParameter("firstName").trim();
        String lastName = request.getParameter("lastName").trim();
        String company = request.getParameter("company").trim();
        String jobTitle = request.getParameter("jobTitle").trim();

        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        String username = client.createUserInOT(user);
        user.setUsername(username);

        if (null == username || "".equals(username)) {
            // there have been an error in registering
%>
<script type="text/javascript">
   
    location.href = "supportUserRegistration.jsp?success=fail";
</script>
<%

} else {

    session.setAttribute("user", user);
%>
<script type="text/javascript">
    location.href = "associateSupport.jsp?newAccount=true";
</script>

<%
    }
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