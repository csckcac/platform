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
<%@ page import="org.wso2.carbon.endpoint.ui.util.EndpointConfigurationHelper" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.mediation.templates.ui.EndpointTemplateAdminClient" %>


<%
    boolean isError = false;
    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    EndpointAdminClient client = new EndpointAdminClient(cookie, url, configContext);

    // there are two ways to hit these page.1. By clicking save in design view 2. By clicking saven in
    // source view

    String forwardTo = "index.jsp?tabs=1";
    String sourceViewXML = request.getParameter("design");
    String endpointType = request.getParameter("endpointType");
    String keyName = request.getParameter("regKey");
    String endpointName = request.getParameter("endpointName");
    String templateName = request.getParameter("templateName");

    String registry = request.getParameter("registry");
    if ("conf".equals(registry)) {
        keyName = "conf:" + request.getParameter("regKey");
    } else if ("gov".equals(registry)) {
        keyName = "gov:" + request.getParameter("regKey");
    }
    boolean updateSynapseReg = false;
    if ("true".equals(request.getParameter("updateSynapseReg"))) {
        updateSynapseReg = true;
    }

    boolean isFromTemplateEditor = session.getAttribute("endpointTemplate") != null ? true : false;
//    String endpointAction = (String) session.getAttribute("edit" + endpointName);
    String endpointAction;
    if (!isFromTemplateEditor) {
        endpointAction = (String) session.getAttribute("edit" + endpointName);
    }else{
        //fowrward to templates page registry tempalted tab
        forwardTo = "../templates/list_templates.jsp?region=region1&item=templates_menu#tabs-4";
        //editing a endpoint template
        endpointAction = (String) session.getAttribute("edit" + templateName);
        //template mode cant coexist with  annonymous mode
        //remove any annymous mode related session attributes (still any session attribs may exist ie:- if
        // annonymous mode could nt exit properly)
        String epMode = (String) session.getAttribute("epMode");
        if (epMode != null && "anon".equals(epMode)) {
            session.removeAttribute("epMode");
        }
    }

    String endpointMode; // this holds an annonymos endpoint which can come trough proxy and send mediator
    String rawConfString;
    endpointMode = (String) session.getAttribute("epMode");
    if (endpointMode != null && "anon".equals(endpointMode)) {
    %>
<script type="text/javascript">
    jQuery(document).ready(function() {
        function gotoPage() {
            history.go(-1);
        }

        CARBON.showErrorDialog('Unable to save to the Synapse registry in the current mode', gotoPage);
    });
</script>
    <%
        return;
    }

    if (keyName == null) {
    %>
<script type="text/javascript">
    jQuery(document).ready(function() {
        function gotoPage() {
            history.go(-1);
        }

        CARBON.showErrorDialog('Registry key must not be empty', gotoPage);
    });
</script>
    <%
        return;
    }

    String configuration = null;

    // came through source -> save
    if (sourceViewXML != null && !"".equals(sourceViewXML)) {
        configuration = sourceViewXML.replaceAll("\\s\\s+|\\n|\\r", ""); // remove the pretty printing from the string

    } else { // came through design-> save
        // build and save each and every configuration
        if (endpointType.equals("addressEndpoint")) {
            if (isFromTemplateEditor) {
                configuration = EndpointConfigurationHelper.buildTemplateConfiguration(request, endpointType, false);
            } else {
                configuration = EndpointConfigurationHelper.buildAddressOrWSDLEpConfiguration(request, endpointType, true);
            }
        } else if (endpointType.equals("defaultEndpoint")) {
            if (isFromTemplateEditor) {
                configuration = EndpointConfigurationHelper.buildTemplateConfiguration(request, endpointType, false);
            } else {
                configuration = EndpointConfigurationHelper.buildAddressOrWSDLEpConfiguration(request, endpointType, true);
            }
        } else if (endpointType.equals("WSDLEndpoint")) {
            if (isFromTemplateEditor) {
                configuration = EndpointConfigurationHelper.buildTemplateConfiguration(request, endpointType, false);
            } else {
                configuration = EndpointConfigurationHelper.buildAddressOrWSDLEpConfiguration(request, endpointType, true);
            }
        } else if (endpointType.equals("loadBalanceEndpoint")) {
            rawConfString = request.getParameter("loadBalanceConf");
            configuration = rawConfString.replaceAll("\\s\\s+|\\n|\\r", "");// this to ensure  that the final configuration doesn't have any spaces etc..

        } else if (endpointType.equals("failOverEndpoint")) {
            rawConfString = request.getParameter("failOverConf");
            configuration = rawConfString.replaceAll("\\s\\s+|\\n|\\r", ""); // this to ensur that the final configuration doesn't have any spaces etc..

        } else if (endpointType.equals("templateEndpoint")) {
            configuration = EndpointConfigurationHelper.buildTemplateEndpointConfiguration(request, endpointType, false);
        }
    }

    if (endpointMode == null) {
        try {
            if (updateSynapseReg) {
                if (!isFromTemplateEditor) {
                     // save an existing one
                    client.saveDynamicEndpoint(keyName, configuration);
                    session.removeAttribute("edit" + endpointName);
                }else{
                    //editing a endpoint template
                    EndpointTemplateAdminClient templateClient = new EndpointTemplateAdminClient(config, session);
                    templateClient.saveDynamicTemplate(keyName, configuration);
                    session.removeAttribute("edit" + templateName);
                }
            } else {
                //add a new endpoint
                if (!isFromTemplateEditor) {
                    //add a new endpoint
                    client.addDynamicEndpoint(keyName, configuration);
                }else{
                    //add new endpoint template
                    EndpointTemplateAdminClient templateClient = new EndpointTemplateAdminClient(config, session);
                    templateClient.addDynamicTemplate(keyName, configuration);
                }
            }
        } catch (Exception e) {
            isError = true;
            String errMsg = e.getMessage();
            errMsg = errMsg.replace("\'", ""); // this is to ensure that error message doesn't have ' or " marking unterminated strings
            errMsg = errMsg.replace("\"", "");
            errMsg = errMsg.replace("\n", "");
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
