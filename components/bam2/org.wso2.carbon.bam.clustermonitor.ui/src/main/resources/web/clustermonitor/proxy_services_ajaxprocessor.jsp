<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.clustermonitor.ui.ClusterAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%
        String cluster = request.getParameter("cluster");
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().
                        getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        ClusterAdminClient client = new ClusterAdminClient(cookie, serverURL, configContext);

        String[] services = client.getProxyServices(cluster);
 %>
<option value="all" selected="selected">All</option>
<%
for (int i = 0; i < services.length; i++) {%><option value="<%=services[i]%>"><%=services[i]%></option><% } %>