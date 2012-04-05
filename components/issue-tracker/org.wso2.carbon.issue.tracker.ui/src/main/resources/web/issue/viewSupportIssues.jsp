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

<div id="workArea">

<h2><fmt:message key="view.issues.header"/></h2>
<%

    String issueKey = request.getParameter("issueKey");

    if (null != issueKey) {
%>
 <script type="text/javascript">
CARBON.showInfoDialog('Issue <%=issueKey%> is successfully reported. ');
 </script>

<%

    }


    int numberOfPages = 0;
    String pageNumber = request.getParameter("pageNumber");

    if (pageNumber == null) {
        pageNumber = "0";
    }
    int pageNumberInt = 0;
    try {
        pageNumberInt = Integer.parseInt(pageNumber);
    } catch (NumberFormatException ignored) {

    }
    IssueTrackerClient client;
    client = IssueTrackerClient.getInstance(config, session);
    String[] accountNames = client.getAccountNames();
    String param = "";

    if (null != accountNames) {
        boolean isStratosService = client.isStratosService();
        String token = null;
        String url = "";
        GenericIssue[] issues = new GenericIssue[0];
        String account = client.getAccount().getKey();
        param = "viewAccount=" + account;
        GenericCredentials credentials = client.getAccount(account).getCredentials();

        if (null != credentials) {
            url = credentials.getUrl();
            try {
                token = client.login(credentials);
            } catch (Exception e) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog('Cannot connect to the selected account. Incorrect username and password or unreachable url is given.');
</script>
<%
        }
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

%>




<form id="issuesForm" action="viewSupportIssues.jsp" method="get">
    <% if (numberOfPages > 0) {

    %>


    <table class="styledLeft noBorders">
        <tbody align="center">
        <tr>
            <td align="center">
                <carbon:paginator pageNumber="<%=pageNumberInt%>"
                                  numberOfPages="<%=numberOfPages%>"
                                  page="viewSupportIssues.jsp"
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

                if (null == severity) {
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


            <td style="width:170px;"><a href="<%=issueUrl%>" target="_blank">More</a>
            </td>

        </tr>
        <%

                    }
                }
            }
        } else {
            // no issues are reported
        %>
        <p><b>No Issues Found</b></p>

        <%

            }


        %>
           <%
    } else {

        // no account has been added

    %>
        <p><b>No Issues Found</b></p>

    <script type="text/javascript">
        CARBON.showErrorDialog('You have not added any JIRA accounts yet. Please go to the Accounts page and add a new account');
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
</fmt:bundle>