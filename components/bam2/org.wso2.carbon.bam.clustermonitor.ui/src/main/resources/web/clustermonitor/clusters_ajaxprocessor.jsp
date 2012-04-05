<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.clustermonitor.ui.ClusterAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="javax.xml.namespace.QName" %>
<%
        String dc = request.getParameter("dc");
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().
                        getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        ClusterAdminClient client = new ClusterAdminClient(cookie, serverURL, configContext);

        String[] clusters = client.getClusters(dc);

for (int i = 0; i < clusters.length; i++) {%><option value="<%=clusters[i]%>"><%=clusters[i]%></option><% } %>