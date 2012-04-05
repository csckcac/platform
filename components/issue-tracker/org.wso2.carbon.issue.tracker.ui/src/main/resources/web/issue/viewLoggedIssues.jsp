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
~ under the License.
-->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<%@ page import="org.wso2.carbon.issue.tracker.stub.LoggedError" %>
<%@ page import="org.wso2.carbon.issue.tracker.ui.IssueTrackerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<script type="text/javascript" src="js/logviewer.js"></script>
<script type="text/javascript" src="../admin/dialog/js/dialog.js"></script>


<%
    LoggedError[] errors;
    try {
        IssueTrackerClient client = IssueTrackerClient.getInstance(config, session);
        errors = client.getIssueLogs(null, 0, 0);
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>

<%

        return;

    }

%>

<fmt:bundle basename="org.wso2.carbon.logging.view.ui.i18n.Resources">
    <carbon:breadcrumb label="system.logs"
                       resourceBundle="org.wso2.carbon.logging.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
    <div id="middle">
        <h2>
            <fmt:message key="error.logs"/>
        </h2>

        <div id="workArea">

            <table class="styledLeft" id="loggedIssuesTable">
                <thead>
                <tr>
                    <th><fmt:message key="error.log.type"/></th>
                    <th><fmt:message key="error.log.message"/></th>
                </tr>
                </thead>
                <tbody>

                <%
                    for (LoggedError error : errors) {
                %>
                <tr>
                    <td>
                        <%=error.getType()%>
                    </td>
                </tr>
                <tr>
                    <td>
                        <%=error.getMessage()%>
                    </td>
                </tr>
                <%
                    }
                %>

                </tbody>

        </div>
    </div>
</fmt:bundle>