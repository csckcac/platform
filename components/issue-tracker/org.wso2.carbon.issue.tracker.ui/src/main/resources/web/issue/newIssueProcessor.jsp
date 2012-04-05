<%@ page import="org.wso2.carbon.issue.tracker.stub.*" %>
<div id="middle">


    <% String account = request.getParameter("account");
        String project = request.getParameter("project");
        String type = request.getParameter("type");
        String summary = request.getParameter("summary");
        String priority = request.getParameter("priority");
        String reporter = request.getParameter("reporter");
        String description = request.getParameter("description");

        IssueTrackerClient client;

        client = IssueTrackerClient.getInstance(config, session);

        if(null!=account){
        GenericCredentials credentials = client.getAccountCredentials(account);
            String token = client.login(credentials);

            GenericIssue issue = new GenericIssue();
            issue.setProjectKey(project);
            issue.setType(type);
            issue.setSummary(summary);
            issue.setPriority(priority);
            issue.setReporter(reporter);
            issue.setDescription(description);
            client.createIssue(issue, token, credentials.getUrl());
        }

    %>

</div>

<script type="text/javascript">
    location.href = "newIssue.jsp";
</script>