<%@ page import="org.apache.axis2.client.Options" %>
<%@ page import="org.apache.axis2.client.ServiceClient" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page
        import="org.wso2.carbon.messagebox.stub.internal.QueueManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.messagebox.stub.internal.admin.QueueUserPermissionBean" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.util.ArrayList" %>
<%
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
//Server URL which is defined in the server.xml
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                                                 session) + "QueueManagerAdminService.QueueManagerAdminServiceHttpsSoap12Endpoint";
    QueueManagerAdminServiceStub stub = new QueueManagerAdminServiceStub(configContext, serverURL);

    String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);

    ServiceClient client = stub._getServiceClient();
    Options option = client.getOptions();
    option.setManageSession(true);
    option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    String message = "";

    String queue = (String) session.getAttribute("queue");
    String permissions = request.getParameter("permissions");
    String userOrRole = request.getParameter("userOrRole");
    String[] permissionParams = permissions.split(",");
    if (userOrRole != null && userOrRole.equals("User")) {
        ArrayList<QueueUserPermissionBean> queueUserPermissionArrayList = new ArrayList<QueueUserPermissionBean>();
        for (int i = 0; i < permissionParams.length; i++) {
            String user = permissionParams[i];
            i++;
            String allowedConsume = permissionParams[i];
            i++;
            String allowedPublish = permissionParams[i];

            QueueUserPermissionBean queueUserPermission = new QueueUserPermissionBean();
            queueUserPermission.setUserName(user);
            queueUserPermission.setAllowedToConsume(Boolean.parseBoolean(allowedConsume));
            queueUserPermission.setAllowedToPublish(Boolean.parseBoolean(allowedPublish));
            queueUserPermissionArrayList.add(queueUserPermission);

        }
        session.removeAttribute("queueUserPermission");

        QueueUserPermissionBean[] userPermissionBeans = new QueueUserPermissionBean[queueUserPermissionArrayList.size()];
        try {
            stub.updateUserPermissions(queue, queueUserPermissionArrayList.toArray(userPermissionBeans));
            message = "Updated permissions successfully";
        } catch (Exception e) {
            message = "Failed to update user permissions on queue: " + queue + " due to " + e.getMessage();
        }

        try {
            session.setAttribute("queueUserPermission", stub.getQueueUserPermissions(queue));
        } catch (Exception e) {
            message = "Failed to get user permissions on queue" + queue + " due to " + e.getMessage();
        }
    }
%>
<%=message%>

