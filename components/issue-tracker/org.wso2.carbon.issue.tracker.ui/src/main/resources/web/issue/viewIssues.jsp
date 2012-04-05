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
<%@ page import="org.wso2.carbon.issue.tracker.stub.GenericCredentials" %>
<%@ page import="org.wso2.carbon.issue.tracker.stub.GenericIssue" %>
<%@ page import="org.wso2.carbon.issue.tracker.stub.PaginatedIssueInfo" %>
<%@ page import="org.wso2.carbon.issue.tracker.ui.IssueTrackerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<jsp:include page="../admin/error.jsp"/>
<script type="text/javascript" src="../issue/js/viewIssue.js"></script>


<fmt:bundle basename="org.wso2.carbon.issue.tracker.ui.i18n.Resources">
<carbon:breadcrumb label="View Issues"
                   resourceBundle="org.wso2.carbon.issue.tracker.ui.i18n.Resources"
                   topPage="false" request="<%=request%>"/>
<div id="middle">

<script type="text/javascript">

    var issueTypes;
    var issueIdList;
    var priorityTypes;
    var priorityId;


    function setOptions(dropdown) {

        var myindex = dropdown.selectedIndex;
        var SelValue = dropdown.options[myindex].value;
        dropdown.selected = SelValue;
        document.getElementById("issuesForm").submit();
        loadProjects(SelValue);
    }


    <%

    String issueKey = request.getParameter("issueKey");

    if(null!=issueKey){
    %>

    CARBON.showInfoDialog('Issue <%=issueKey%> is successfully created. ');


    <%

   }

    IssueTrackerClient client;

    client = IssueTrackerClient.getInstance(config, session);

    GenericCredentials credentials=null  ;


    int numberOfPages=0;
    String pageNumber = request.getParameter("pageNumber");

    if (pageNumber == null) {
         pageNumber = "0";
    }
    int pageNumberInt = 0;
    try {
         pageNumberInt = Integer.parseInt(pageNumber);
    } catch (NumberFormatException ignored) {
     
    }

    %>

</script>

<h2><fmt:message key="view.issues.header"/></h2>

<div id="workArea">

<form id="issuesForm" action="viewIssues.jsp" method="get">

<table class="styledLeft" id="accountsTable">
    <thead>
    <tr>
        <th><fmt:message key="view.account"/></th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td class="formRow">


            <select id="viewAccount" name="viewAccount"
                    style="width:200px" onchange="this.form.submit()">
                <option value="--Select--">--Select--</option>
                <%
                    client = IssueTrackerClient.getInstance(config, session);
                    String[] accountNames = client.getAccountNames();

                    boolean isStratosService = client.isStratosService();

                    String token = null;
                    String url;
                    GenericIssue[] issues = new GenericIssue[0];

                    String account = request.getParameter("viewAccount");


                    if (null != accountNames) {

                        String selected = account;
                        for (String name : accountNames) {
                            if (null != selected && name.equals(selected)) {


                %>

                <option value="<%=name%>" selected="selected"><%= name%>
                </option>
                <%


                } else {


                %>
                <option value="<%=name%>"><%=name%>
                </option>
                <%

                            }
                        }
                    }

                %>
            </select>
            <%

                String param = "viewAccount=" + account;

                if ((account != null) && (!account.equals("")) && (!account.equals("--Select--"))) {


                    credentials = client.getAccount(account).getCredentials();

                    if (null != credentials) {
                        url = credentials.getUrl();

                        try {
                            token = client.login(credentials);
                        } catch (Exception e) {
            %>
            <script type="text/javascript">
                document.getElementById("viewAccount").value = '--Select--';
                CARBON.showErrorDialog('Cannot connect to the selected account. Incorrect username and password or unreachable url is given.');
            </script>
            <%
                }

                if (null != token) {
                    try {

                        PaginatedIssueInfo paginatedIssueInfo;
                        if (!isStratosService) {
                            paginatedIssueInfo = client.retrievePaginatedIssueInfo(token, url, pageNumberInt);
                        } else {
                            paginatedIssueInfo = client.getPaginatedIssuesForTenant(token, url, pageNumberInt);
                        }

                        if (null != paginatedIssueInfo) {
                            numberOfPages = paginatedIssueInfo.getNumberOfPages();
                            issues = paginatedIssueInfo.getIssueInfo();
                        } else {

                            response.setStatus(500);
                            String msg = "No matching issues found for the selected account.";
                            CarbonUIMessage uiMsg = new CarbonUIMessage(msg, CarbonUIMessage.INFO);
                            session.setAttribute(CarbonUIMessage.ID, uiMsg);
                            return;

                        }
                    } catch (Exception e) {
            %>

            <% return;
            }
            }
            }
            }

            %>


            <input type="hidden" name="requestedPage" id="requestedPage"
                   value="<%=pageNumberInt%>"/>

        </td>
    </tr>

    </tbody>
</table>


<% if (numberOfPages > 0) {

%>


<table class="styledLeft noBorders">
    <tbody align="center">

    <tr>
        <td align="center">
            <carbon:paginator pageNumber="<%=pageNumberInt%>"
                              numberOfPages="<%=numberOfPages%>"
                              page="viewIssues.jsp"
                              pageNumberParameterName="pageNumber"
                              parameters="<%=param%>"/>

        </td>
    </tr>
    </tbody>
</table>


<table border="1" class="styledLeft" id="issuesTable">
    <thead>
    <tr>
        <th class="formRow"><fmt:message key="view.issues.key"/></th>
        <th class="formRow"><fmt:message key="view.issues.summary"/></th>
        <th class="formRow"><fmt:message key="view.issues.type"/></th>
        <th class="formRow"><fmt:message key="view.issues.status"/></th>
        <th class="formRow"><fmt:message key="view.issues.assignee"/></th>
        <th class="formRow"><fmt:message key="view.issues.last.updated"/></th>
        <%
            if (!isStratosService) {
        %>
        <th class="formRow"><fmt:message key="view.issues.priority"/></th>
        <%
        } else {
        %>
        <th class="formRow"><fmt:message key="view.issues.severity"/></th>
        <%


            }
        %>
        <th class="formRow"><fmt:message key="view.issues.more"/></th>

    </tr>
    </thead>
    <tbody>


    <%
        if (null != issues || issues.length != 0) {

            for (GenericIssue issue : issues) {


    %>


    <tr>
        <%

            String key = issue.getIssueKey();
            String summary = issue.getSummary();
            String status = issue.getStatus();
            String assignee = issue.getAssignee();
            String lastUpdated = issue.getLastUpdated().getTime().toString();
        %>

        <td style="width:200px;height: 20px">
            <%=key%>
        </td>
        <td style="width:300px;height: 20px"><%=summary%>
        </td>
        <%
            String type = issue.getType();

            String issueType = "";
        %>

        <td style="width:200px;height: 20px" id="issueType"><%=type%>
        </td>

        <td style="width:200px;height: 20px" id="issueStatus"><%=status%>
        </td>

        <td style="width:200px;height: 20px" id="issueAssignee"><%=assignee%>
        </td>

        <td style="width:200px;height: 20px" id="issueLastUpdated"><%=lastUpdated%>
        </td>

        <%
            String priority = issue.getPriority();

            String issueUrl = credentials.getUrl() + issue.getUrl();

            String severity = issue.getSeverity();

            if(null == severity){
                severity = "";
            }

            //for a service priority is not shown
            if (!isStratosService) {
        %>
        <td style="width:200px;height: 20px"><%=priority%>
        </td>

        <%
        } else {
        %>
        <td style="width:200px;height: 20px"><%=severity%>
        </td>

        <%

            }
        %>

        <td style="width:170px;"><a href="<%=issueUrl%>" target="_blank">More</a>
        </td>

    </tr>
    <%

            }

        }

    %>


    <%
    } else if (null != account && null != token) {
    %>

    <script type="text/javascript">
        document.getElementById("viewAccount").value = '--Select--';
        var msg = 'No issues found for the account <%=account%> .';
        CARBON.showInfoDialog(msg);
    </script>


    <%

        }
    %>

    </tbody>
</table>


<table class="styledLeft noBorders">
    <tbody align="center">
    <tr>
        <td align="center">
            <carbon:paginator pageNumber="<%=pageNumberInt%>"
                              numberOfPages="<%=numberOfPages%>"
                              page="viewIssues.jsp"
                              pageNumberParameterName="pageNumber"
                              parameters="<%=param%>"/>

        </td>
    </tr>
    </tbody>
</table>
</form>

</div>
</div>

<script type="text/javascript">
    alternateTableRows('issuesTable', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('accountsTable', 'tableEvenRow', 'tableOddRow');
</script>


<script type="text/javascript">
    function loadProjects(name) {
        $.post("loadProject-ajaxprocessor.jsp?accountNames=" + document.getElementById("accountNames").value, {},
                function(loadResponse) {
                    loadResponse = loadResponse.replace(/^\s+|\s+$/g, '');

                    projectList = eval('(' + loadResponse + ')');

                    issueTypes = projectList.issueType;
                    issueIdList = projectList.issueId;
                    priorityTypes = projectList.priorityName;
                    priorityId = projectList.priorityId;


                });
    }


</script>


</fmt:bundle>