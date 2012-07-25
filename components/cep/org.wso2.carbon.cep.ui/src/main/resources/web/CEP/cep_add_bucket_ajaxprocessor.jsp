<%@ page import="org.apache.axis2.client.Options" %>
<%@ page import="org.apache.axis2.client.ServiceClient" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.CEPAdminServiceStub" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.BucketDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.CEPEngineProviderConfigPropertyDTO" %>
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


    BucketDTO bucket = new BucketDTO();
    LinkedList<InputDTO> inputs = (LinkedList<InputDTO>) session.getAttribute("inputs");
    LinkedList<QueryDTO> queries = (LinkedList<QueryDTO>) session.getAttribute("queries");
    String bucketName = request.getParameter("bucketName");
    String bucketDescription = request.getParameter("bucketDescription");
    String engineProvider = request.getParameter("engineProvider");
    String engineProviderConfig = request.getParameter("engineProviderConfig");

    if(engineProviderConfig!=null&& !engineProviderConfig.equals("")){
        String[] properties=engineProviderConfig.split("-__-");
        CEPEngineProviderConfigPropertyDTO[] configProperties=new  CEPEngineProviderConfigPropertyDTO[properties.length];
        for (int i = 0, propertiesLength = properties.length; i < propertiesLength; i++) {
            String property = properties[i];
            String[] vals = property.split("-_-");
            configProperties[i]=new CEPEngineProviderConfigPropertyDTO();
            configProperties[i].setNames(vals[0]);
            configProperties[i].setValues(vals[1]);
        }
        bucket.setEngineProviderConfigProperty(configProperties);
    }
    if(bucketName != null){
        bucketName = bucketName.trim();
    }

    bucket.setName(bucketName);
    bucket.setDescription(bucketDescription);
    bucket.setEngineProvider(engineProvider.trim());

    String message = "";
    if (inputs != null) {
        bucket.setInputs(inputs.toArray(new InputDTO[inputs.size()]));
    }

    if (queries != null) {
        bucket.setQueries(queries.toArray(new QueryDTO[queries.size()]));
    }

    try {
        stub.addBucket(bucket);
        message = "Bucket Added Successfully";

        session.removeAttribute("editingBucket");
        session.removeAttribute("inputs");
        session.removeAttribute("queries");
        session.removeAttribute("tempBucketInformation");
    } catch (Exception e) {
        message = "Error in adding bucket :" + e.toString();
    }
%><%=message%>
