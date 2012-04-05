<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.analyzer.ui.AnalyzerAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    AnalyzerAdminClient client = new AnalyzerAdminClient(cookie, serverURL, configContext);
    String seqName = request.getParameter("seqname");
    String xmlString = client.getAnalyzerXML(seqName);   

    client.deleteAnalyzer(xmlString);
%>
<script type="text/javascript">
    window.location.href = "../analyzer/index.jsp";
</script>
