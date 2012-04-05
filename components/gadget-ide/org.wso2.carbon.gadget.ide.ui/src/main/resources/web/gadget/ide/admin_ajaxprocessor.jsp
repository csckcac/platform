<%@ page import="org.wso2.carbon.gadget.ide.ui.GadgetIDEAdminClient" %>
<%@ page import="org.wso2.carbon.gadget.ide.ui.GadgetIDEUIConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="org.wso2.carbon.gadget.ide.ui.Util" %>
<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    GadgetIDEAdminClient admin = new GadgetIDEAdminClient(cookie, backendServerURL,
            configContext, request.getLocale());

    final String action = request.getParameter(GadgetIDEUIConstants.AJAX_ACTION_PARAMETER);

    if (GadgetIDEUIConstants.AJAX_ACTION_GET_OPERATIONS.equals(action)) {
        String[] operations = admin.getOperations(
                request.getParameter(GadgetIDEUIConstants.WSDL_URL_PARAMETER),
                request.getParameter(GadgetIDEUIConstants.ENDPOINT_PARAMETER));
%>
<%--
 action : getOperations
 param : {string} wsdlUrl
 param : {string} endpoint
 return : {{operations:Array.<string>}}

 example : client.jsp?action=getOperations&wsdlUrl=http://localhost:9763/services/echo?wsdl2&endpoint=echoHttpEndpoint
e--%>
{"operations":<%=Util.toJson(operations) %>}
<%
} else if (GadgetIDEUIConstants.AJAX_ACTION_GET_ENDPOINTS.equals(action)) {
    String[] endpoints = admin.getEndpoints(
            request.getParameter(GadgetIDEUIConstants.WSDL_URL_PARAMETER));
%>
<%--
 action : getEndpoints
 param : {string} wsdlUrl
 return : {{endpoints:Array.<string>}}

 example : client.jsp?action=getEndpoints&wsdlUrl=http://localhost:9763/services/echo?wsdl2
 --%>
{"endpoints":<%=Util.toJson(endpoints)%>}
<%
} else if (GadgetIDEUIConstants.AJAX_ACTION_SAVE_SETTINGS.equals(action)) {
%>
<%--
 action : saveSettings
 param : {Document} settings
 return : {saved:boolean}

 example : client.jsp?action=saveSettings&settings=<settings>foo</settings>
 --%>
{"saved":<%= admin.saveTempSettings(
        request.getParameter(GadgetIDEUIConstants.SETTINGS_PARAMETER)
)%>}

<%
    //HACK:
    admin.generateCode();
    admin.deploy(request.getParameter(GadgetIDEUIConstants.FILE_NAME_PARAMETER));

} else if (GadgetIDEUIConstants.AJAX_ACTION_GENERATE_CODE.equals(action)) {
%>
<%--
 action : genGadget
 return : TODO:

 example : client.jsp?action=genGadget
 --%>
{"genarated":<%= admin.generateCode() %>}
<%
    }else if(GadgetIDEUIConstants.AJAX_ACTION_GET_RESPONSE_XML.equals(action)){
%>
<%--TODO: move to backend--%>

<%--
 action : getResponseXml
 param : {string} wsdlUrl
 param : {string} endpoint
 param : {string} operation
 return : {{responseSchema:string}}

 example : client.jsp?action=getResponseXml&wsdlUrl=http://localhost:9763/services/echo?wsdl2&endpoint=echoHttpEndpoint&operation=echoInt
 --%>
{"responseSchema":"<%=
        Util.generateResponseXMLFromSig(admin.getOperationSig(
            request.getParameter(GadgetIDEUIConstants.WSDL_URL_PARAMETER),
            request.getParameter(GadgetIDEUIConstants.ENDPOINT_PARAMETER),
            request.getParameter(GadgetIDEUIConstants.OPERATION_PARAMETER)))
                .replaceAll("\"","\\\\\"")
%>"}

<% } %>



