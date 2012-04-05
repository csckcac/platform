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


    String indexConfiguration = "    <CFIndexes>\n" +
                                "        <ColumnFamily name=\"Event\" defaultCF=\"true\">\n" +
                                "            <granularity>none</granularity>\n" +
                                "            <!--<rowKey>timeStamp+serverAddress</rowKey>-->\n" +
                                "\t    <rowKey>\n" +
                                "\t\t<part name=\"timeStamp\"/>\n" +
                                "\t\t<part name=\"serverAddress\" storeIndex=\"true\"/>\n" +
                                "\t    </rowKey>\n" +
                                "        </ColumnFamily>\n" +
                                "        <ColumnFamily name=\"WorkFlowId_NodeId\">\n" +
                                "            <granularity>hour</granularity>\n" +
                                "            <!--<rowKey>workFlowId+nodeId+timeStamp</rowKey>-->\n" +
                                "\t    <rowKey>\n" +
                                "\t\t<part name=\"workFlowId\" storeIndex=\"true\"/>\n" +
                                "\t\t<part name=\"nodeId\" storeIndex=\"true\"/>\n" +
                                "\t\t<part name=\"timeStamp\"/>\n" +
                                "\t    </rowKey>\n" +
                                "            <indexRowKey>allKeys</indexRowKey>\n" +
                                "        </ColumnFamily>\n" +
                                "        <ColumnFamily name=\"WorkFlowId_ActivityId\">\n" +
                                "            <granularity>hour</granularity>\n" +
                                "            <!--<rowKey>workFlowId+activityId+timeStamp</rowKey>-->\n" +
                                "\t    <rowKey>\n" +
                                "\t\t<part name=\"workFlowId\" storeIndex=\"true\"/>\n" +
                                "\t\t<part name=\"activityId\" storeIndex=\"true\"/>\n" +
                                "\t\t<part name=\"timeStamp\"/>\n" +
                                "\t    </rowKey>\n" +
                                "            <indexRowKey>allKeys</indexRowKey>\n" +
                                "        </ColumnFamily>\n" +
                                "        <ColumnFamily name=\"WorkFlowId\">\n" +
                                "            <granularity>hour</granularity>\n" +
                                "            <!--<rowKey>workFlowId+activityId+timeStamp</rowKey>-->\n" +
                                "\t    <rowKey>\n" +
                                "\t\t<part name=\"workFlowId\" storeIndex=\"true\"/>\n" +
                                "\t\t<part name=\"timeStamp\"/>\n" +
                                "\t    </rowKey>\n" +
                                "            <indexRowKey>allKeys</indexRowKey>\n" +
                                "        </ColumnFamily>\n" +
                                "        <ColumnFamily name=\"NodeId\">\n" +
                                "            <granularity>hour</granularity>\n" +
                                "            <!--<rowKey>workFlowId+activityId+timeStamp</rowKey>-->\n" +
                                "\t    <rowKey>\n" +
                                "\t\t<part name=\"nodeId\" storeIndex=\"true\"/>\n" +
                                "\t\t<part name=\"timeStamp\"/>\n" +
                                "\t    </rowKey>\n" +
                                "            <indexRowKey>allKeys</indexRowKey>\n" +
                                "        </ColumnFamily>\n" +
                                "    </CFIndexes>";

    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    AnalyzerAdminClient client = new AnalyzerAdminClient(cookie, serverURL, configContext);
    if (request.getParameter("edit") != null) {
        try {
            client.editIndexConfiguration(indexConfiguration);
        } catch (AxisFault e) {
            String message = "Unable to store analyzer sequence. Check the sequence for syntax errors..";

%>
<script type="text/javascript">
    jQuery(document).init(function () {
        CARBON.showErrorDialog('<%=message%>');
    });
</script>
<%

        }
    }
%>

<div class="analizer_background">

    <form action="index3.jsp">
        <input type="submit" value="Add Task"/>
        <input type="hidden" name="add"/>
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