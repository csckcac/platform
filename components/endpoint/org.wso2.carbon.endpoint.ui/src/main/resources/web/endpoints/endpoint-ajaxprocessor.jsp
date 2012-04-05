<%@ page import="org.wso2.carbon.endpoint.ui.client.EndpointAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.endpoint.common.to.EndpointMetaData" %>
<%
    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    EndpointAdminClient client = new EndpointAdminClient(cookie, url, configContext);
    EndpointMetaData[] endpoints = client.getEndpointMetaData(0, 100);
%>
<div id="endpointData">
    <table width="100%">
        <thead>
            <tr>
                <th>Endpoint</th>
                <th>Type</th>
            </tr>
        </thead>
        <tbody>
            <%
                if (endpoints != null && endpoints.length > 0 && endpoints[0] != null) {
                    for (EndpointMetaData endpoint : endpoints) {
            %>
                    <tr>
                        <td><%=endpoint.getName()%></td>
                        <%
                            String type = "";
                            switch (endpoint.getType()) {
                                case 0: type = "Address Endpoint"; break;
                                case 1: type = "WSDL Endpoint"; break;
                                case 2: type = "Failover Endpoint"; break;
                                case 3: type = "Load balance Endpoint"; break;
                                case 4: type = "Default Endpoint"; break;
                            }
                        %>
                        <td><%=type%></td>
                    </tr>
            <%
                    }
                } else {
            %>
                <tr><td colspan="2">No endpoints defined</td></tr>
            <%
                }
            %>
        </tbody>
    </table>
</div>