<%@ page import="org.apache.axis2.client.Options" %>
<%@ page import="org.apache.axis2.client.ServiceClient" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.CEPAdminServiceStub" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.BucketDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.InputDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.QueryDTO" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.util.LinkedList" %>
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


    BucketDTO bucket = (BucketDTO) session.getAttribute("editingBucket");
    LinkedList<InputDTO> inputs = (LinkedList<InputDTO>) session.getAttribute("inputs");
    LinkedList<QueryDTO> queries = (LinkedList<QueryDTO>) session.getAttribute("queries");

    String message = "";
    if (inputs != null) {
        bucket.setInputs(inputs.toArray(new InputDTO[inputs.size()]));
    }

    if (queries != null) {
        bucket.setQueries(queries.toArray(new QueryDTO[queries.size()]));
    }

    try {
        stub.editBucket(bucket);
        message = "Bucket Modified Successfully";

        session.removeAttribute("editingBucket");
        session.removeAttribute("inputs");
        session.removeAttribute("queries");
        session.removeAttribute("tempBucketInformation");
    } catch (Exception e) {
        message = "Error in adding bucket :" + e.toString();
    }
%><%=message%>