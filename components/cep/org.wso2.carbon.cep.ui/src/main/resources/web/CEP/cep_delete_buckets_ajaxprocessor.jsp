<%@ page import="org.apache.axis2.client.Options" %>
<%@ page import="org.apache.axis2.client.ServiceClient" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.CEPAdminServiceStub" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.CEPAdminServiceCEPAdminException" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.CEPAdminServiceCEPAdminException" %>
<%
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    //Server URL which is defined in the server.xml
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
            session) + "CEPAdminService.CEPAdminServiceHttpsSoap12Endpoint";
    CEPAdminServiceStub stub = new CEPAdminServiceStub(configContext, serverURL);

    String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);

    ServiceClient client = stub._getServiceClient();
    Options option = client.getOptions();
    option.setManageSession(true);
    option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

    String message = "";


    String[] buckets = request.getParameterValues("buckets");
    String pageNumber = request.getParameter("pageNumber");
    String deleteAllBuckets = request.getParameter("deleteAllbuckets");
    int pageNumberInt = 0;
    if (pageNumber != null) {
        pageNumberInt = Integer.parseInt(pageNumber);
    }


    try {
        if (deleteAllBuckets != null) {
            stub.removeAllBuckets();
        } else {
            for (String bucketName : buckets) {
                stub.removeBucket(bucketName);
            }
        }
        message = "buckets removed successfully";
    } catch (CEPAdminServiceCEPAdminException e) {
       message = e.getFaultMessage().getCEPAdminException().getErrorMessage();
    }

%><%=message%>
<script type="text/javascript">
    location.href = "cep_deployed_buckets.jsp?pageNumber=<%=pageNumberInt%>";
</script>