<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.analyzer.ui.AnalyzerAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<%
    String mode = request.getParameter("mode");
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    AnalyzerAdminClient client = new AnalyzerAdminClient(cookie, serverURL, configContext);
    String xmlString = request.getParameter("xmlString");

    if (mode.equals("new")) {
        client.addAnalyzer(xmlString);
    } else if (mode.equals("edit")) {
        client.editAnalyer(xmlString);
    }
%>