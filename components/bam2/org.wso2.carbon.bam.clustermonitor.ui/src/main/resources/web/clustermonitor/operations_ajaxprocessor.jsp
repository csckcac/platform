<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.clustermonitor.ui.ClusterAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="javax.xml.namespace.QName" %>
<%
        String service = request.getParameter("service");
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().
                        getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        ClusterAdminClient client = new ClusterAdminClient(cookie, serverURL, configContext);

        String[] operations = client.getOperations(service);
 %>
<option value="all" selected="selected">All</option>
<%if(operations!=null){
for (int i = 0; i < operations.length; i++) {%><option value="<%=operations[i]%>"><%=operations[i]%></option><% }
}%>