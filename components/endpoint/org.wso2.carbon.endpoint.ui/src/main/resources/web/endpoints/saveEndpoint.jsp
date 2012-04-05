<%--
  ~  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  Licensed under the Apache License, Version 2.0 (the "License");
  ~  you may not use this file except in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~        http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing, software
  ~  distributed under the License is distributed on an "AS IS" BASIS,
  ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~  See the License for the specific language governing permissions and
  ~  limitations under the License.
  --%>

<%@page contentType="text/html" pageEncoding="UTF-8"
        import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.endpoint.ui.client.EndpointAdminClient" %>
<%@ page import="org.wso2.carbon.mediation.templates.ui.EndpointTemplateAdminClient" %>
<%@ page import="org.wso2.carbon.endpoint.ui.util.EndpointConfigurationHelper" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>


<%
    String endpointName = request.getParameter("endpointName");
    if (endpointName == null) { // user has hit this page just after a session timeout.
        // he has to start over as session attributes are lost.
        %>
        <script type="text/javascript">
            document.location.href = '../endpoints/index.jsp?region=region1&item=endpoints_menu';
        </script>
        <%
        return;
    }
    boolean isError = false;
    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    EndpointAdminClient client = new EndpointAdminClient(cookie, url, configContext);

    EndpointConfigurationHelper epConfigHelper = new EndpointConfigurationHelper();

    // there are two ways to hit these page.1. By clicking save in design view 2. By clicking saven in
    // source view

    String forwardTo = "../endpoints/index.jsp?region=region1&item=endpoints_menu&tabs=0";
    String sourceViewXML = request.getParameter("design");
    String endpointType = request.getParameter("endpointType");
    String templateName = request.getParameter("templateName");

    String endpointMode = null; // this holds an annonymos endpoint which can come trough proxy and send mediator
    String anonymouseOriginator = null;
    boolean isAnonymous = false;
    String anonymousEndpointXML = null;
    String rawConfString;

    boolean isFromTemplateEditor = session.getAttribute("endpointTemplate") != null ? true:false;
    boolean isTemplateRegMode = false ;

    String endpointAction;
    if (!isFromTemplateEditor) {
        endpointAction = (String) session.getAttribute("edit" + endpointName);
    }else{
        //fowrward to templates page local templates tab
        forwardTo = "../templates/list_templates.jsp?region=region1&item=templates_menu#tabs-3";
        //editing a endpoint template
        endpointAction = (String) session.getAttribute("edit" + templateName);
        //template mode cant coexist with  annonymous mode
        //remove any annymous mode related session attributes (still any session attribs may exist ie:- if
        // annonymous mode could nt exit properly)
        String epMode = (String) session.getAttribute("epMode");
        //to identify we are saving a reg template
        isTemplateRegMode = session.getAttribute("templateEdittingMode") != null ? true:false;
        if (epMode != null && "anon".equals(epMode)) {
            session.removeAttribute("epMode");
        }
    }

    endpointMode = (String) session.getAttribute("epMode");
    if (endpointMode != null && "anon".equals(endpointMode)) {
        isAnonymous = true;
        isFromTemplateEditor = false;
    }
    String configuration = null;

    // came through source -> save
    if (sourceViewXML != null && !"".equals(sourceViewXML)) {
        configuration = sourceViewXML.replaceAll("\\s\\s+|\\n|\\r", ""); // remove the pretty printing from the string
        configuration = sourceViewXML.replace("&","&amp;"); // this is to ensure that url is properly encoded

    } else { // came through design-> save
        // build and save each and every configuration
        if (endpointType.equals("addressEndpoint") && !"".equals("addressEndpoint")) {
            if (isFromTemplateEditor) {
                configuration = epConfigHelper.buildTemplateConfiguration(request, endpointType, isAnonymous);
            } else {
                configuration = epConfigHelper.buildAddressOrWSDLEpConfiguration(request, endpointType, isAnonymous);
            }
        } else if(endpointType.equals("defaultEndpoint") && !"".equals("defaultEndpoint")){
            if (isFromTemplateEditor) {
                configuration = epConfigHelper.buildTemplateConfiguration(request, endpointType, isAnonymous);
            } else {
                configuration = epConfigHelper.buildAddressOrWSDLEpConfiguration(request, endpointType, isAnonymous);
            }
        } else if (endpointType.equals("WSDLEndpoint") && !"".equals("WSDLEndpoint")) {
            if (isFromTemplateEditor) {
                configuration = epConfigHelper.buildTemplateConfiguration(request, endpointType, isAnonymous);
            } else {
                configuration = epConfigHelper.buildAddressOrWSDLEpConfiguration(request, endpointType, isAnonymous);
            }
        } else if (endpointType.equals("loadBalanceEndpoint") && !"".equals("loadBalanceEndpoint")) {
            rawConfString = request.getParameter("loadBalanceConf");
            configuration = rawConfString.replaceAll("\\s\\s+|\\n|\\r", "");// this to ensure  that the final configuration doesn't have any spaces etc..

        } else if (endpointType.equals("failOverEndpoint") && !"".equals("failOverEndpoint")) {
            rawConfString = request.getParameter("failOverConf");
            configuration = rawConfString.replaceAll("\\s\\s+|\\n|\\r", ""); // this to ensure that the final configuration doesn't have any spaces etc..

        }else if (endpointType.equals("templateEndpoint")) {
            configuration = EndpointConfigurationHelper.buildTemplateEndpointConfiguration(request, endpointType, isAnonymous);
        }
    }

    if (endpointMode == null && !isTemplateRegMode) {
        try {
            if (endpointAction != null && endpointAction.equals("edit") && !"".equals(endpointAction)) {
                if (!isFromTemplateEditor) {
                    // save an existing one
                    client.saveEndpoint(configuration);
                    session.removeAttribute("edit" + endpointName);
                }else{
                    //editing a endpoint template
                    EndpointTemplateAdminClient templateClient = new EndpointTemplateAdminClient(config, session);
                    templateClient.saveTemplate(configuration);
                    session.removeAttribute("edit" + templateName);
                }
            } else {
                if (!isFromTemplateEditor) {
                    //add a new endpoint
                    client.addEndpoint(configuration);
                }else{
                    //add new endpoint template
                    EndpointTemplateAdminClient templateClient = new EndpointTemplateAdminClient(config, session);
                    templateClient.addTemplate(configuration);
                }
            }
        } catch (Exception e) {
            isError = true;
            String errMsg = e.getMessage();
            errMsg = errMsg.replace("\'", ""); // this is to ensure that error message doesn't have ' or " marking unterminated strings
            errMsg = errMsg.replace("\"", "");
            errMsg = errMsg.replace("\n", "");

            /*this is a temporary fix. have to handle this from the backend side, but
                there needs lots of changes to be done. Therefore this temporary fix is added
             */
            if("EndpointAdminException".equalsIgnoreCase(errMsg) ||
                    "EndpointAdminEndpointAdminException".equalsIgnoreCase(errMsg)){
                errMsg = "Unable to add the endpoint. Please check the endpoint configuration and try again";
            }
%>
<script type="text/javascript">
    jQuery(document).ready(function() {
        function gotoPage() {
            history.go(-1);
        }

        CARBON.showErrorDialog('<%=errMsg%>', gotoPage);
    });
</script>
<%
        }
    } else if (isAnonymous) {
        // coming through using either send mediator or proxy services by adding an annonymous endpoint
        // we are in anonymous mode
        anonymouseOriginator = (String) session.getAttribute("anonOriginator");
        request.setAttribute("endpointName", "anon"); // set the endpoint name to Annonymous
        session.setAttribute("anonEpXML", configuration);
        forwardTo = anonymouseOriginator + "?originator=../endpoints/saveEndpoint.jsp";
        //System.out.println("saving in annonymous mode .....Forward To : " + forwardTo);
    } else if (isTemplateRegMode) {
        session.setAttribute("anonEpXML", configuration);
        forwardTo = "registry_endpoint.jsp" + "?originator=../endpoints/saveEndpoint.jsp";
    }

    if (!isError) {
%>
<script type="text/javascript">
    // just forward to relevent page if there is no error
    document.location.href = '<%=forwardTo%>'; //TODO-use JQuery AJAX here
</script>
<%
    }
%>
