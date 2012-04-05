<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.wso2.carbon.bam.analyzer.ui.IndexAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<%

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    IndexAdminClient client = new IndexAdminClient(cookie, serverURL, configContext);

    String tableName = request.getParameter("tableName");

    try {
        client.deleteTable(tableName);
    } catch (AxisFault e) {
        String errorString = "Failed to delete table.";
%>
<script type="text/javascript">
    jQuery(document).init(function () {
        CARBON.showErrorDialog('<%=errorString%>', function () {
            location.href = "data-config.jsp";
        });
    });
</script>
<%
        return;
    }
%>

<script type="text/javascript">
    jQuery(document).ready(function () {
        location.href = "data-config.jsp";
    });
</script>