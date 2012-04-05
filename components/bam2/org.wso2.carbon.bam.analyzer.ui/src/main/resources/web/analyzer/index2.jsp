<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.bam.analyzer.ui.AnalyzerAdminClient" %>
<%@ page import="org.apache.axis2.AxisFault" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
        "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title></title>
    <script type="text/javascript" src="js/jstree/_lib/jquery.js"></script>
    <script type="text/javascript" src="js/jstree/_lib/jquery.hotkeys.js"></script>
    <script type="text/javascript" src="js/jstree/_lib/jquery.cookie.js"></script>
    <script type="text/javascript" src="js/jstree/jquery.jstree.js"></script>


    <script type="text/javascript" src="js-lib/treeNode.js"></script>
    <script type="text/javascript" src="js-lib/operations.js"></script>

    <link type="text/css" rel="stylesheet" href="css/analizer.css">
    
</head>
<body>
<div id="dialog-overlay"></div>
<div id="dialog-box">
    <div class="dialog-content">
        <div id="dialog-message"></div>
        <a href="#" class="button">Close</a>
    </div>
</div>
<div class="xmlTreeAttributes" id="nodeData"></div>

<%
    String topic = "/carbon/bam/data/publishers/service_stats";
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);


    String analyzerXML = "<analyzerSequence name=\"workflowSummarizer\">\n" +
            "\t    \n" +
            "\t    <trigger cron=\"1 * * * * ? *\" />\n" +
            "\t    <!--<trigger cron=\"* * * * * ? *\" />-->\n" +
            "\t    <analyzers>\n" +
            "\t\t<get name=\"WorkFlowId\">\n" +
            "\t\t\t<index name=\"workFlowId\" start=\"\" end=\"\"/>\n" +
            "\t\t\t<!--<index name=\"nodeId\" start=\"AS\" end=\"FB\"/>-->\n" +
            "\t\t\t<!--<groupBy index=\"workFlowId\"/>-->\n" +
            "\t\t\t<granularity index=\"timeStamp\" type=\"hour\"/>\n" +
            "\t\t</get>\n" +
            "\t\t<lookup name=\"Event\"/>\n" +
            "\t\t<aggregate>\n" +
            "\t\t\t<measure name=\"RequestCount\" aggregationType=\"SUM\"/>\n" +
            "\t\t\t<measure name=\"ResponseCount\" aggregationType=\"SUM\"/>\n" +
            "\t\t\t<measure name=\"MaximumResponseTime\" aggregationType=\"AVG\"/>\n" +
            "\t\t</aggregate>\n" +
            "\t\t<put name=\"WorkflowResult\" dataSource=\"allKeys\"/>\n" +
            "\t\t<log/>\n" +
            "\t\n" +
            "\t\t<get name=\"WorkflowResult\">\n" +
            "\t\t\t<index name=\"workFlowId\" start=\"\" end=\"\"/>\n" +
            "\t\t\t<groupBy index=\"workFlowId\"/>\n" +
            "\t\t</get>\n" +
            "\t\t<aggregate>\n" +
            "\t\t\t<measure name=\"RequestCount\" aggregationType=\"CUMULATIVE\"/>\n" +
            "\t\t\t<measure name=\"ResponseCount\" aggregationType=\"CUMULATIVE\"/>\n" +
            "\t\t\t<measure name=\"MaximumResponseTime\" aggregationType=\"AVG\"/>\n" +
            "\t\t</aggregate>\n" +
            "\t\t<put name=\"WorkflowAccumilator\" dataSource=\"allKeys\"/>\n" +
            "\t\t<log/>\n" +
            "\t    </analyzers>\n" +
            "\t</analyzerSequence>";

    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    AnalyzerAdminClient client = new AnalyzerAdminClient(cookie, serverURL, configContext);
    if (request.getParameter("add") != null) {
        try {
            client.addAnalyzer(analyzerXML);
        } catch (AxisFault e) {
            String errorString = "Unable to store analyzer sequence. Check the sequence for syntax errors..";
%>
<script type="text/javascript">
    jQuery(document).init(function () {
        CARBON.showErrorDialog('<%=errorString%>');
    });
</script>
<%
        }

    }
    if (request.getParameter("delete") != null) {
        try {
            client.deleteAnalyzer(analyzerXML);
        } catch (AxisFault e) {
            String errorString = "Error while deleting analyzer sequence..";
%>
<script type="text/javascript">
    jQuery(document).init(function () {
        CARBON.showErrorDialog('<%=errorString%>');
    });
</script>
<%
        }
    }
%>

<div class="analizer_background">

    <form action="index.jsp">
        <input type="submit" value="Add Task"/>
        <input type="hidden" name="add"/>
    </form>
    <form action="index.jsp">
        <input type="submit" value="Delete Task"/>
        <input type="hidden" name="delete"/>
    </form>
    <div id="analizer_nav">
        <div id="analizer_toolbar"></div>
    </div>
    <div id="analizer_content">
        <div id="analizer_header">
            <div class="subtitle">Multi Node Summarizer</div>
        </div>
        <div class="treeContainer">
            <div id="analizerTreeContainer"></div>
        </div>
    </div>
    <div id="analizer_footer">footer</div>
</div>
</body>
</html>