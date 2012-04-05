<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.gadget.ide.ui.GadgetIDEAdminClient" %>
<%@ page import="org.wso2.carbon.gadget.ide.ui.GadgetIDEUIConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="org.wso2.carbon.gadget.ide.ui.Util" %>
<%@page contentType="application/javascript" %>
<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    GadgetIDEAdminClient admin = new GadgetIDEAdminClient(cookie, backendServerURL,
            configContext, request.getLocale());


%>
<%= admin.getStub(
        request.getParameter(GadgetIDEUIConstants.WSDL_URL_PARAMETER),
        request.getParameter(GadgetIDEUIConstants.ENDPOINT_PARAMETER),
        request.getParameter(GadgetIDEUIConstants.OPERATION_PARAMETER))
%>
