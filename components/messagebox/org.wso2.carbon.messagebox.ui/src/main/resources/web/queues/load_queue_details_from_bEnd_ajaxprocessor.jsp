<%@ page import="org.apache.axis2.client.Options" %>
<%@ page import="org.apache.axis2.client.ServiceClient" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.messagebox.stub.internal.QueueManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>

<%
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
//Server URL which is defined in the server.xml
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                                                 session) + "QueueManagerAdminService.QueueManagerAdminServiceHttpsSoap12Endpoint";
    QueueManagerAdminServiceStub stub = new QueueManagerAdminServiceStub(configContext, serverURL);

    if (session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE) != null && request.getParameter("queueName") != null) {
        String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        session.removeAttribute("queue");
        session.removeAttribute("queueUserPermission");

        String queueName = request.getParameter("queueName");
        session.setAttribute("queueUserPermission", stub.getQueueUserPermissions(queueName));
        session.setAttribute("queue", queueName);

    } else {
        String error = "SessionTimeOut";
%>
<%=error%>
<%
    }
%>