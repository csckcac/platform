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


<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.endpoint.common.to.AddressEndpointData" %>
<%@ page import="org.wso2.carbon.endpoint.ui.client.EndpointAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="javax.xml.stream.XMLStreamException" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.impl.llom.util.AXIOMUtil" %>
<%@ page import="org.apache.synapse.endpoints.*" %>
<%@ page import="org.apache.synapse.config.xml.endpoints.*" %>
<%@ page import="org.wso2.carbon.endpoint.ui.util.EndpointConfigurationHelper" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.Properties" %>
<%@ page import="org.wso2.carbon.endpoint.common.to.TemplateEndpointData" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.wso2.carbon.endpoint.ui.util.TemplateEndpointHelper" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>

<!-- Dependencies -->
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>

<!-- Connection handling lib -->
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../yui/build/utilities/utilities.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../sequences/js/registry-browser.js"></script>
<!-- Source File -->

<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>

<link type="text/css" rel="stylesheet" href="css/style.css"/>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>

<fmt:bundle basename="org.wso2.carbon.endpoint.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
        request="<%=request%>"
       />
<carbon:breadcrumb
        label="template.endpoint"
        resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<%
    TemplateEndpointData templateEpData = null;

    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    EndpointAdminClient adminClient = new EndpointAdminClient(cookie, url, configContext);

    String endpointName = request.getParameter("endpointName");
    String endpointAction = request.getParameter("endpointAction");
    String design = request.getParameter("design");

    String endpointMode = null; // this holds an annonymos endpoint which can come through proxy and send mediator
    String anonymouseOriginator = null;
    boolean isAnonymous = false;
    String anonymousEndpointXML = null;
    endpointMode = (String) session.getAttribute("epMode");
    if (endpointMode != null && "anon".equals(endpointMode)) {
        isAnonymous = true;
    }

    // coming through index.jsp by clicking the 'edit' link
    if (endpointAction != null && !"".equals(endpointAction) && endpointAction.equals("edit")) {
        session.setAttribute("edit" + endpointName, "edit"); // uses when saving the endpoint
        try {
            templateEpData = adminClient.getTemplateEndpoint(endpointName);
        } catch (Exception e) {
            String msg = "Unable to get Address Endpoint data: " + e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
        }

    } else if (design != null && !"".equals(design)) {
        // coming through source view using switchToDesign
        OMElement element = null;
        try {
            design = design.replaceAll("\\s\\s+|\\n|\\r", ""); // remove the pretty printing from the string
            design = EndpointConfigurationHelper.getValidStringXMlStringForAMP(design); // convert into a valid XML string, when a JMS endpoint is used etc..
            element = AXIOMUtil.stringToOM(design);
            TemplateEndpoint templateEp = (TemplateEndpoint) EndpointFactory.getEndpointFromElement(element, false, new Properties());
            templateEpData = EndpointConfigurationHelper.getTemplateEpFromSynEp(templateEp, session, isAnonymous);
        } catch (XMLStreamException e) {
            String msg = "Unable to get Address Endpoint data: " + e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
        } catch (Exception e) {
            String msg = "Unable to get Address Endpoint data: " + e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
        }

    } else if (isAnonymous) {
        // coming through using either send mediator or proxy services by adding an annonymous endpoint
        // we are in anonymous mode
        anonymouseOriginator = (String) session.getAttribute("anonOriginator");
        anonymousEndpointXML = (String) session.getAttribute("anonEpXML");
        if (anonymousEndpointXML != null && !"".equals(anonymousEndpointXML)) {
            // if a user is here that mean user is trying to edit an existing anoymous endpoint
            try {
                templateEpData = EndpointAdminClient.getTemplateEndpointData(anonymousEndpointXML);
            } catch (XMLStreamException e) {
                session.removeAttribute("anonEpXML");
%>
<script type="text/javascript">
    CARBON.showErrorDialog("Unable to create endpoint with given data");
    window.location.href = "templateEndpoint.jsp";
</script>
<%
            }
        }
    }

    String templateEpName = "";
    String templateEndpointAddress = "";
    String validAddressURL = "";

    String target = "";
    Map<String,String> parameterMap = new HashMap<String ,String >();

    if (templateEpData != null) {
        TemplateEndpointHelper templateEndpointHelper = new TemplateEndpointHelper(templateEpData);
        // Endpoint Name
        if (templateEndpointHelper.containsKey("name")) {
            templateEpName = templateEndpointHelper.getMapFromColonSepArray().get("name");
        }

        // Endpoint Address
        if (templateEndpointHelper.containsKey(("uri"))) {
            templateEndpointAddress = templateEndpointHelper.getMapFromColonSepArray().get("uri");
            validAddressURL = EndpointConfigurationHelper.getValidXMLString(templateEndpointAddress);
        }

        // target Template
        if (templateEpData.getTargetTemplate() != null) {
            target = templateEpData.getTargetTemplate() ;
        }
        parameterMap = templateEndpointHelper.getMapFromColonSepArray();
    }

    Set<String> paramSet = parameterMap.keySet();
    String propertyTableStyle = parameterMap.size() == 0 ? "display:none;" : "";
%>
<script type="text/javascript">

    function handleEndpointTemplateGet() {
        jQuery.ajax({
                        type: 'POST',
                        url: '../calltemplate-mediator/endpoint_template_get_available-ajaxprocessor.jsp',
                        success: function(msg) {
                            handleSuccess(msg);
                        },
                        error: function(msg) {
//                        CARBON.showErrorDialog('<fmt:message key="template.trace.enable.link"/>' +
//                                               ' ' + templateName);
                        }
                    });
    }

    function handleSuccess(data){
        jQuery("#templateSelector").html(data);
    }

//    handleEndpointTemplateGet();

</script>

<script type="text/javascript" src="js/addressEndpoint.js"></script>
<script type="text/javascript" src="js/template_param.js"></script>
<script type="text/javascript" src="js/endpoint-util.js"></script>
<script type="text/javascript">


    // invoke when saving addressEndpoint data
    function submitEndpointData(isAnonymous) {
        if (!isValidAddressORWSDLEndpoint(isAnonymous, 'address')) {
            return false;
        }
        document.templateEndpointForm.action = 'saveEndpoint.jsp?endpointType=templateEndpoint';
        document.templateEndpointForm.submit();
        return true;
    }

    function submitDynamicEndpointData() {
        if (!isValidAddressORWSDLEndpoint(true, 'address')) {
            return false;
        }

        var key = document.getElementById('synRegKey').value;
        if (key == '') {
            CARBON.showWarningDialog('<fmt:message key="empty.key.field"/>');
            return false;
        }
        var registry;
        if (document.getElementById("config_reg").checked == true) {
            registry = 'conf';
        } else {
            registry = 'gov';
        }
        document.templateEndpointForm.action = 'saveDynamicEndpoint.jsp?registry='+registry
                +'&endpointType=templateEndpoint&regKey=' + key;
        document.templateEndpointForm.submit();
        return true;
    }

    function switchToSource(isAnonymous) {
        if (!isValidAddressORWSDLEndpoint(isAnonymous, 'address')) {
            return false;
        }
        document.templateEndpointForm.action = 'endpointSourceView.jsp?endpointType=templateEndpoint&retainlastbc=true';
        document.templateEndpointForm.submit();
        return true;
    }

    function showSaveAsForm(show) {
        var formElem = document.getElementById('saveAsForm');
        if (show) {
            formElem.style.display = "";
            var keyField = document.getElementById('synRegKey');
            if (keyField.value == '') {
                keyField.value = document.getElementById("endpointName").value;
            }
        } else {
            formElem.style.display = "none"
        }
    }




</script>

<div id="middle">
<h2>
    <% if (request.getParameter("serviceName") != null) {
        %><%=request.getParameter("serviceName")%>:&nbsp;<%
       }
       if ("edit".equals(endpointAction)) {
        %><fmt:message key="edit.endpoint"/><%
       } else {
        %><fmt:message key="template.endpoint"/><%
       }
    %>
</h2>
<div id="workArea">
<form id="templateEndpointForm" name="templateEndpointForm" action="" method="POST">
<table class="styledLeft">
<thead>
<tr>
    <th><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="design.view.of.the.template.endpoint"/></span>

        <a class="icon-link"
           style="background-image: url(images/source-view.gif);"
           onclick="switchToSource('<%=isAnonymous%>');"
           href="#"><fmt:message key="switch.to.source.view"/></a>
    </th>
</tr>
</thead>
<tbody>
<tr>
<td>
<table class="normal-nopadding">
<tbody>

<tr style="<%=!isAnonymous?"":"display:none"%>">
    <td width="180px"><fmt:message key="endpointName"/> <span
            class="required">*</span></td>
    <td><input name="endpointName" id="endpointName"
               value="<%=templateEpName%>"
            <%=(!"".equals(templateEpName)) ? "disabled=\"disabled\"" : ""%>
               onkeypress="return validateText(event);"/>
        <input type="hidden" name="endpointName" value="<%=templateEpName%>"/>
    </td>
</tr>

<tr>
    <td class="leftCol-small"><fmt:message key="address"/><span class="required"> *</span>
    </td>
    <td><input id="address" name="address" type="text"
               value="<%=validAddressURL%>" size="75"/>
        <input id="testAddress" name="testAddress" type="button" class="button"
               onclick="testURL(document.getElementById('address').value)" value="<fmt:message key="test.url"/>"/>
    </td>
</tr>

<tr>
    <td>
        <fmt:message key="template.endpoint.target"/>
    </td>
    <td>
        <input class="longInput" type="text" id="mediator.call.target" name="mediator.call.target"
               value="<%=target%>"/>
    </td>
</tr>

<%--<tr>
    <td>
        <fmt:message key="template.endpoint.target.available"/>
    </td>
    <td>
        <select name="templateSelector" id="templateSelector"
                onchange="onTemplateSelectionChange()">
            <option value="default">Select From Templates</option>

        </select>
    </td>
</tr>--%>

<tr>
    <td colspan="2">

        <div style="margin-top:0px;">
            <table id="propertytable" style="<%=propertyTableStyle%>;" class="styledLeft">
                <thead>
                    <tr>
                        <td colspan="3" class="sub-header"><fmt:message key="parameters"/></td>
                    </tr>

                    <tr>
                        <th width="15%"><fmt:message key="th.parameter.name"/></th>
                        <th width="15%"><fmt:message key="th.parameter.value"/></th>
                        <th><fmt:message key="th.action"/></th>
                    </tr>
                    <tbody id="propertytbody">
                                        <%
                                                int i = 0;
                                            for (String param : paramSet) {

                                                    String paramName = param;
                                                    String paramValue = parameterMap.get(paramName);
                                            %>
                                                    <tr id="propertyRaw<%=i%>">
                                                        <td><input type="text" name="propertyName<%=i%>" id="propertyName<%=i%>"
                                                                   class="esb-edit small_textbox"
                                                                   value="<%=paramName%>"/>
                                                        </td>
                                                        <td><input type="text" name="propertyValue<%=i%>" id="propertyValue<%=i%>"
                                                                   value="<%=paramValue%>"/>
                                                        </td>
                                                        <td><a href="#" class="delete-icon-link"
                                                                onclick="deleteProperty(<%=i%>)"><fmt:message key="template.parameter.delete"/></a></td>
                                                    </tr>


                                                <%
                                                    i++;
                                                }%>
                                                <input type="hidden" name="propertyCount" id="propertyCount" value="<%=i%>"/>

                    </tbody>
                </thead>
            </table>
        </div>
    </td>
</tr>

<tr>
            <td>
                <div style="margin-top:10px;">
                    <a name="addNameLink"></a>
                    <a class="add-icon-link"
                       href="#addNameLink"
                       onclick="addParameter(true)"><fmt:message key="template.parameter.add"/></a>
                </div>
            </td>
        </tr>

</tbody>
</table>


<table class="normal-nopadding">
<tbody>

<tr>
    <td class="buttonRow" colspan="2">
        <input type="button" value="<fmt:message key="save"/>" class="button" name="save"
               onclick="javascript:submitEndpointData('<%=isAnonymous%>');"/>
        <%
            if (!isAnonymous)  {
        %>
            <input type="button" value="<fmt:message key="saveas"/>" class="button" name="save"
                   onclick="javascript:showSaveAsForm(true);"/>
        <%
            }
        %>
        <input type="button" value="<fmt:message key="cancel"/>" name="cancel" class="button"
               onclick="javascript:cancelEndpointData('<%=anonymouseOriginator%>');"/>
    </td>
</tr>
</tbody>

</table>
<div style="display:none;" id="saveAsForm">
    <p>&nbsp;</p>
    <table class="styledLeft">
        <thead>
        <tr>
            <th colspan="2">
                <span style="float:left; position:relative; margin-top:2px;"><fmt:message key="save.as.title"/></span>
            </th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td>
                <table class="normal">
                    <tr>
                        <td><fmt:message key="save.in"/></td>
                        <td><fmt:message key="config.registry"/> <input type="radio" name="registry" id="config_reg"
                                                       value="conf:"  checked="checked"
                                                       onclick="document.getElementById('reg').innerHTML='conf:';"/>
                            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                            <fmt:message key="gov.registry"/> <input type="radio" name="registry" id="gov_reg"
                                                       value="gov:"
                                                       onclick="document.getElementById('reg').innerHTML='gov:';"/>
                        </td>
                    </tr>
                    <tr>
                        <td>Key</td>
                        <td><span id="reg">gov:</span><input type="text" size="75" id="synRegKey"/></td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="buttonRow">
                <input type="button" class="button" value="<fmt:message key="save"/>" id="saveSynRegButton" onclick="javascript:submitDynamicEndpointData(); return false;"/>
                <input type="button" class="button" value="<fmt:message key="cancel"/>" id="cancelSynRegButton" onclick="javascript:showSaveAsForm(false); return false;">
            </td>
        </tr>
        </tbody>
    </table>
</div>
</td>
</tr>
</tbody>
</table>
</form>
</div>
</div>
<a name="registryBrowserLink"></a>
</fmt:bundle>
