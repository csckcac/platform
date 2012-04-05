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
<%@ page import="org.wso2.carbon.endpoint.common.to.DefaultEndpointData" %>
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
<%@ page import="org.wso2.carbon.endpoint.ui.util.TemplateParameterContainer" %>
<%@ page import="org.wso2.carbon.endpoint.ui.factory.TemplateDefinitionFactory" %>
<%@ page import="org.wso2.carbon.endpoint.ui.util.TemplateConfigurationBuilder" %>

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

<!-- Source File -->
<script type="text/javascript" src="../sequences/js/registry-browser.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>

<link type="text/css" rel="stylesheet" href="css/style.css"/>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>

<fmt:bundle basename="org.wso2.carbon.endpoint.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
        request="<%=request%>"/>
<carbon:breadcrumb
        label="default.endpoint"
        resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<%
    DefaultEndpointData defaultEndpointData = null;
    String[] formatOptions = {"soap11", "soap12", "POX", "REST", "GET", "leave-as-is"};
    String[] optimizeOptions = {"SWA", "MTOM", "leave-as-is"};
    String[] actionOptions = {"neverTimeout", "discardMessage", "executeFaultSequence"};

    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    EndpointAdminClient adminClient = new EndpointAdminClient(cookie, url, configContext);

    String endpointName = request.getParameter("endpointName");
    String endpointAction = request.getParameter("endpointAction");
    String design = request.getParameter("design");

    //initializing template specific parameters
    boolean isFromTemplateEditor = session.getAttribute("endpointTemplate") != null ? true:false;
    String templateAdd = request.getParameter("templateAdd");
//    System.out.println("tempalteADD: " + templateAdd);
    boolean isTemplateAdd = templateAdd != null && "true".equals(templateAdd) ? true : false;

    String[] params = new String[0];
    Template templateObj = null;
    String templateName = "";
    if (isFromTemplateEditor) {
        templateObj = (Template) session.getAttribute("endpointTemplate");
        if (templateObj != null) {
            params = templateObj.getParameters().toArray(params);
            templateName = templateObj.getName();
        }
        //template mode cant coexist with  annonymous mode
        //remove any annymous mode related session attributes (still any session attribs may exist ie:- if
        // annonymous mode could nt exit properly)
        String epMode = (String) session.getAttribute("epMode");
        if (epMode != null && "anon".equals(epMode)) {
            session.removeAttribute("epMode");
        }
    }
    //this factory will be used to populate/extract template specific parameters starting with '$'
    TemplateDefinitionFactory fac = new TemplateDefinitionFactory();
    TemplateParameterContainer templateMappings = fac.getParameterContainer();
    //end of template specific parameters

    String regEpName = (String)session.getAttribute("regEpName");
    session.removeAttribute("regEpName");
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
            if (isFromTemplateEditor) {
                defaultEndpointData = adminClient.getDefaultEndpoint(templateObj,fac);
                session.setAttribute("edit" + templateName, "edit"); // uses when saving the template
            }
            else{
                defaultEndpointData = adminClient.getDefaultEndpoint(endpointName);
            }

        } catch (Exception e) {
            String msg = "Unable to get Defaulr Endpoint data: " + e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
        }

    } else if (design != null && !"".equals(design)) {
        // coming through source view using switchToDesign
        OMElement element = null;
        try {
            design = design.replaceAll("\\s\\s+|\\n|\\r", ""); // remove the pretty printing from the string
            design = EndpointConfigurationHelper.getValidStringXMlStringForAMP(design); // convert into a valid XML string, when a JMS endpoint is used etc..
            element = AXIOMUtil.stringToOM(design);
            if (isFromTemplateEditor) {
                templateObj = (Template) new TemplateFactory().createEndpointTemplate(element, new Properties());
                if (templateObj != null) {
                    params = templateObj.getParameters().toArray(params);
                    templateName = templateObj.getName();
                }
                DefaultEndpoint defaultEp = (DefaultEndpoint) EndpointFactory.getEndpointFromElement(templateObj.getElement(), fac, false, new Properties());
                defaultEndpointData = EndpointConfigurationHelper.getDefaultEpFromSynEp(defaultEp, session, isAnonymous);
            }
            else{
                DefaultEndpoint defaultEp = (DefaultEndpoint) EndpointFactory.getEndpointFromElement(element, false, new Properties());
                defaultEndpointData = EndpointConfigurationHelper.getDefaultEpFromSynEp(defaultEp, session, isAnonymous);
            }
        } catch (XMLStreamException e) {
            String msg = "Unable to get Default Endpoint data: " + e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
        } catch (Exception e) {
            String msg = "Unable to get Default Endpoint data: " + e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
        }

    } else if (isAnonymous && !isTemplateAdd) {
        //users should not be able to create templates on annonymous mode
        //always reset template editor mode for anonymous mode if a template session already exists
        isFromTemplateEditor = false;
        isTemplateAdd = false;

        // coming through using either send mediator or proxy services by adding an annonymous endpoint
        // we are in anonymous mode
        anonymouseOriginator = (String) session.getAttribute("anonOriginator");
        anonymousEndpointXML = (String) session.getAttribute("anonEpXML");
        if (anonymousEndpointXML != null && !"".equals(anonymousEndpointXML)) {
            // if a user is here that mean user is trying to edit an existing anoymous endpoint
            try {
                defaultEndpointData = EndpointAdminClient.
                        getDefaultEndpointData(anonymousEndpointXML);
            } catch (XMLStreamException e) {
                session.removeAttribute("anonEpXML");
%>
<script type="text/javascript">
    CARBON.showErrorDialog("Unable to create endpoint with given data");
    window.location.href = "defaultEndpoint.jsp";
</script>
<%
            }
        }
    }

    String defaultEpName = "";
    String endpointAddress = "";
    String validAddressURL = "";
    boolean isPox = false, isRest = false, isSoap11 = false, isSoap12 = false , isGet = false;
    boolean isformatDefault = true;
    boolean isSWA = false, isMTOM = false, isOptimizeDefault = true;
    String errorCode = "";
    long suspendDurationOnFailure = 0;
    long maxDuration = 0;
    float factor = 1.0f; // default value
    String timeOutErrorCode = "";
    String retryDisabledErrorCodes = "";
    int retryTimeOut = 0;
    int retryDelayTimeOut = 0;
    boolean isNeverTimeout = true, isDiscardMessage = false, isFaultSequence = false;
    long actionDuration = 0;
    boolean isEnableAddressing = false;
    boolean isSepListener = false;
    boolean isEnableWSSec = false;
    String secPolicy = "";
    boolean isEnableRM = false;
    String rmPolicy = "";
    String description = "";
    String properties = "";

    if (defaultEndpointData != null) {
        // Endpoint Name
        if (defaultEndpointData.getEpName() != null) {
            defaultEpName = defaultEndpointData.getEpName();
        }

         // Description
        if (defaultEndpointData.getDescription() != null) {
            description = defaultEndpointData.getDescription();
        }

        // Format string
        if (defaultEndpointData.isPox()) {
            isPox = true;
        } else if (defaultEndpointData.isRest()) {
            isRest = true;
        } else if (defaultEndpointData.isSoap11()) {
            isSoap11 = true;
        } else if (defaultEndpointData.isSoap12()) {
            isSoap12 = true;
        } else if (defaultEndpointData.isGet()) {
            isGet = true;
        } else {
            isformatDefault = true;
        }
        if (isPox || isRest || isSoap11 || isSoap12 || isGet) {
            isformatDefault = false;
        }

        // Optimize string
        if (defaultEndpointData.isMtom()) {
            isMTOM = true;
        } else if (defaultEndpointData.isSwa()) {
            isSWA = true;
        } else {
            isOptimizeDefault = true;
        }
        if (isMTOM || isSWA) {
            isOptimizeDefault = false;
        }

        // Error codes
        if (defaultEndpointData.getErrorCodes() != null) {
            errorCode = defaultEndpointData.getErrorCodes().trim();
        }

        // Initial duration
        if (defaultEndpointData.getSuspendDurationOnFailure() >= 0) {
            suspendDurationOnFailure = defaultEndpointData.getSuspendDurationOnFailure();
        }

        // Max duration
        if (0 <= defaultEndpointData.getMaxSusDuration() && defaultEndpointData.getMaxSusDuration() < Long.MAX_VALUE) {
            maxDuration = defaultEndpointData.getMaxSusDuration();
        }

        // Factor
        if (0 <= defaultEndpointData.getSusProgFactor() && defaultEndpointData.getSusProgFactor() >= 0.0) {
            factor = defaultEndpointData.getSusProgFactor();
        }

        // TimeOut error code
        if (defaultEndpointData.getTimdedOutErrorCodes() != null) {
            timeOutErrorCode = defaultEndpointData.getTimdedOutErrorCodes().trim();
        }

        //non-retry error codes
         if(defaultEndpointData.getRetryDisabledErrorCodes() != null) {
             retryDisabledErrorCodes = defaultEndpointData.getRetryDisabledErrorCodes().trim();
         }

        // Retry time out
        if (defaultEndpointData.getRetryTimeout() > 0) {
            retryTimeOut = defaultEndpointData.getRetryTimeout();
        }

        // Retry delay timeout
        if (defaultEndpointData.getRetryDelay() > 0) {
            retryDelayTimeOut = defaultEndpointData.getRetryDelay();
        }

        // Action
        if (defaultEndpointData.getTimeoutAct() == 100) { // TODO-remove magic number
            isNeverTimeout = true;
        } else if (defaultEndpointData.getTimeoutAct() == 101) { // TODO-remove magic number
            isDiscardMessage = true;
            actionDuration = defaultEndpointData.getTimeoutActionDur();
        } else {
            isFaultSequence = true;
            actionDuration = defaultEndpointData.getTimeoutActionDur();
        }

        if (isDiscardMessage || isFaultSequence) {
            isNeverTimeout = false;
        }

        // Enable WS-Addressing?
        if (defaultEndpointData.isWsadd()) {
            isEnableAddressing = true;
        }

        // Use seperate listener ?
        if (defaultEndpointData.isSepList()) {
            isSepListener = true;
        }

        // Enable WS-Sec?
        if (defaultEndpointData.isWssec()) {
            isEnableWSSec = true;
            // Sec. policy
            if (defaultEndpointData.getSecPolKey() != null) {
                secPolicy = defaultEndpointData.getSecPolKey();
            }
        }

        // Enable rm ?
        if (defaultEndpointData.isWsrm()) {
            isEnableRM = true;
            // RM. policy
            if (defaultEndpointData.getRmPolKey() != null) {
                rmPolicy = defaultEndpointData.getRmPolKey();
            }
        }

        if (defaultEndpointData.getProperties() != null && defaultEndpointData.getProperties() != "") {
            properties = defaultEndpointData.getProperties();
        }
    }
%>

<script type="text/javascript" src="js/addressEndpoint.js"></script>
<script type="text/javascript" src="js/endpoint-util.js"></script>
<script type="text/javascript" src="js/template_param.js"></script>
<script type="text/javascript" src="js/endpoint-params.js"></script>
<script type="text/javascript">

    YAHOO.util.Event.onDOMReady(init);

    function init(){
        generateServiceParamTable();
    }

    // invoke when saving addressEndpoint data
    function submitEndpointData(isAnonymous) {
        if (!isValidAddressORWSDLEndpoint(isAnonymous, 'address')) {
            <%
                 //ignore any ui validation for templates ie:- due to parameterized arguments present $uri,etc
                 //may show error messages
                 if (!isFromTemplateEditor) {
            %>
                    return false;
             <%
                    }
            %>
        }
        document.defaultEndpointForm.endpointProperties.value = populateServiceParams("headerTable");
        document.defaultEndpointForm.action = 'saveEndpoint.jsp?endpointType=defaultEndpoint';
        document.defaultEndpointForm.submit();
        return true;
    }

    function submitDynamicEndpointData() {
             <%
                 //ignore any ui validation for templates ie:- due to parameterized arguments present $uri,etc
                 //may show error messages
                 if (!isFromTemplateEditor) {
            %>
        if (isEmptyField('endpointName')) {
            CARBON.showWarningDialog(jsi18n['name.field.cannot.be.empty']);
            return false;
        }
             <%
                    }
             %>

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
        document.defaultEndpointForm.endpointProperties.value = populateServiceParams("headerTable");
        document.defaultEndpointForm.action = 'saveDynamicEndpoint.jsp?registry='+registry
                +'&endpointType=defaultEndpoint&regKey=' + key;
        document.defaultEndpointForm.submit();
        return true;
    }

    function switchToSource(isAnonymous) {
        if (!isValidAddressORWSDLEndpoint(isAnonymous, 'default')) {
            <%
                 //ignore any ui validation for templates ie:- due to parameterized arguments present $uri,etc
                 //may show error messages
                 if (!isFromTemplateEditor) {
            %>
                    return false;
             <%
                    }
            %>
        }
        document.defaultEndpointForm.endpointProperties.value = populateServiceParams("headerTable");
        document.defaultEndpointForm.action = 'endpointSourceView.jsp?endpointType=defaultEndpoint&retainlastbc=true';
        document.defaultEndpointForm.submit();
        return true;
    }

    function showSaveAsForm(show) {
        var formElem = document.getElementById('saveAsForm');
        if (show) {
            formElem.style.display = "";
            var keyField = document.getElementById('synRegKey');
            if (keyField.value == '') {
                <%
                 if (!isFromTemplateEditor) {
            %>
                       keyField.value = document.getElementById("endpointName").value;
            <%
                 } else{
            %>
                       keyField.value = document.getElementById("templateName").value;
            <%
                }
            %>
            }
        } else {
            formElem.style.display = "none"
        }
    }

    function generateServiceParamTable() {
        var str = '<%=properties%>';
        if (str != '') {
            var params;
            params = str.split("::");
            var i, param;
            for (i = 0; i < params.length; i++) {
                param = params[i].split(",");
                addServiceParamRow(param[0], param[1], param[2],"headerTable");
            }
        }
    }

</script>

<div id="middle">
<h2>
    <% if (request.getParameter("serviceName") != null) {
        %><%=request.getParameter("serviceName")%>:&nbsp;<%
       }
       if ("edit".equals(endpointAction) && isFromTemplateEditor) {
        %><fmt:message key="edit.endpoint.template"/><%
       } else if ("edit".equals(endpointAction)) {
        %><fmt:message key="edit.endpoint"/><%
       } else if (isFromTemplateEditor) {
        %><fmt:message key="default.endpoint.template"/><%
       } else {
        %><fmt:message key="default.endpoint"/><%
       }
    %>
</h2>

<div id="workArea">
<form id="defaultEndpointForm" name="defaultEndpointForm" action="" method="POST">
<table class="styledLeft">
<thead>
<tr>
    <th><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="default.endpoint"/></span>

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

<%
                                if (isFromTemplateEditor && isTemplateAdd) {
                    %>
                     <tr>
                         <td width="180px"><fmt:message key="templateName"/> <span
                                 class="required">*</span></td>
                         <td><input name="templateName" id="templateName"
                                    value="<%=templateName%>"
                                    />
                         </td>
                     </tr>
                    <%
                                 } else if(isFromTemplateEditor){
                    %>
                     <tr>
                         <td width="180px"><fmt:message key="templateName"/> <span
                                 class="required">*</span></td>
                         <td><input name="templateNameBox" id="templateNameBox"
                                    value="<%=templateName%>"
                                    disabled="disabled" />
                             <input type="hidden" name="templateName" id="templateName"
                                value="<%=templateName%>" />
                         </td>
                     </tr>

                    <%
                                 }
                    %>

<tr style="<%=!isAnonymous?"":"display:none"%>">
    <td width="180px"><fmt:message key="endpointName"/> <span
            class="required">*</span></td>
    <td><input name="endpointName" id="endpointName"
               value="<%=defaultEpName%>"
            <%=(!"".equals(defaultEpName)) ? "disabled=\"disabled\"" : ""%>
               <%=isFromTemplateEditor?"":"onkeypress=\"return validateText(event);\""%>  size="50"/>
        <input type="hidden" name="endpointName" value="<%=defaultEpName%>"/>
    </td>
</tr>
<%
    if(regEpName != null) {%>
    <tr>
        <td width="180px"><fmt:message key="endpointName"/></td>
        <td><input type="text" value="<%=regEpName%>" disabled="disabled" size="50"/></td>
    </tr>
    <%}%>


<%
                            if (isFromTemplateEditor) {
                                String propertyTableStyle = params.length == 0 ? "display:none;" : "";
        %>

    <div style="margin-top:0px;">

                <tr>


                            <table id="propertytable" style="<%=propertyTableStyle%>" class="styledInner">
                                <thead>
                                    <tr>
                                        <th width="75%"><fmt:message key="template.parameter.name"/></th>
                                        <th><fmt:message key="template.parameter.action"/></th>
                                    </tr>
                                    <tbody id="propertytbody">
                                        <%
                                                int i = 0;
                                                for(;i< params.length ; i++){
                                                    String paramName = params[i];

                                        %>
                                                    <tr id="propertyRaw<%=i%>">
                                                        <td><input type="text" name="propertyName<%=i%>" id="propertyName<%=i%>"
                                                                   class="esb-edit small_textbox"
                                                                   value="<%=paramName%>"/>
                                                        </td>
                                                        <td><a href="#" class="delete-icon-link"
                                                                onclick="deleteProperty(<%=i%>)"><fmt:message key="template.parameter.delete"/></a></td>
                                                    </tr>


                                                <%
                                                }%>
                                                <input type="hidden" name="propertyCount" id="propertyCount" value="<%=i%>"/>

                                            </tbody>
                                        </thead>
                                   </table>
                                </div>


</tr>
<tr>
            <td>
                <div style="margin-top:10px;">
                    <a name="addNameLink"></a>
                    <a class="add-icon-link"
                       href="#addNameLink"
                       onclick="addParameter()"><fmt:message key="template.parameter.add"/></a>
                </div>
            </td>
        </tr>

        <%
            }
        %>



<tr>
    <td><span id="_adv" style="float: left; position: relative;">
            <a class="icon-link"  onclick="javascript:showAdvancedOptions('');"
               style="background-image: url(images/down.gif);"><fmt:message key="show.advanced.options"/></a>
        </span>
    </td>
</tr>
</tbody>
</table>

<div id="_advancedForm" style="display:none">
<table class="normal-nopadding">
<tbody>
<tr>
    <td colspan="2" class="sub-header"><fmt:message key="message.content"/></td>
</tr>
<tr>
    <td width="180px"><fmt:message key="format"/></td>
    <td><select name="format">
        <option value="<%=formatOptions[0]%>" <%=isSoap11 ? "selected=\"selected\"" : ""%>>
            <fmt:message key="soap.1.1"/></option>
        <option value="<%=formatOptions[1]%>" <%=isSoap12 ? "selected=\"selected\"" : ""%>>
            <fmt:message key="soap.1.2"/></option>
        <option value="<%=formatOptions[2]%>" <%=isPox ? "selected=\"selected\"" : ""%>>
            <fmt:message key="plain.old.xml.pox"/></option>
        <option value="<%=formatOptions[3]%>" <%=isRest ? "selected=\"selected\"" : ""%>>
            <fmt:message key="representational.state.transer.rest.get"/></option>
        <option value="<%=formatOptions[4]%>" <%=isGet ? "selected=\"selected\"" : ""%>>
            <fmt:message key="get"/></option>
        <option value="<%=formatOptions[5]%>" <%=isformatDefault ? "selected=\"selected\"" : ""%>>
            <fmt:message
                    key="leave.as.is"/></option>
    </select>
    </td>
</tr>

<tr>
    <td><fmt:message key="optimize"/></td>
    <td><select name="optimize">
        <option value="<%=optimizeOptions[0]%>" <%=isSWA ? "selected" : ""%>>
            <fmt:message key="swa"/></option>
        <option value="<%=optimizeOptions[1]%>" <%=isMTOM ? "selected" : ""%>>
            <fmt:message key="mtom"/></option>
        <option value="<%=optimizeOptions[2]%>" <%=isOptimizeDefault ? "selected" : ""%>>
            <fmt:message key="leave.as.is"/></option>
        <option value="<%=templateMappings.getTemplateMapping(TemplateParameterContainer.EndpointDefKey.optimize)%>" <%=templateMappings.contains(TemplateParameterContainer.EndpointDefKey.optimize) ? "selected" : ""%>>
            <fmt:message key="mtom"/></option>
    </select></td>
</tr>

<tr>
    <td colspan="2" class="sub-header"><fmt:message
            key="suspend"/></td>
</tr>
<tr>
    <td><fmt:message key="error.codes"/></td>
    <td><input type="text" id="suspendErrorCode" name="suspendErrorCode"
               value="<%="".equals(errorCode.trim())?TemplateConfigurationBuilder.getMappingFrom(templateMappings, TemplateParameterContainer.EndpointDefKey.suspendErrorCodes):errorCode%>" size="75"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="intial.duration.millis"/></td>
    <td><input type="text" id="suspendDuration" name="suspendDuration"
               value="<%=suspendDurationOnFailure==0?TemplateConfigurationBuilder.getMappingFrom(templateMappings, TemplateParameterContainer.EndpointDefKey.initialSuspendDuration):suspendDurationOnFailure %>"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="max.duration.millis"/></td>
    <td><input type="text" id="suspendMaxDuration" name="suspendMaxDuration"
               value="<%=maxDuration==0?TemplateConfigurationBuilder.getMappingFrom(templateMappings, TemplateParameterContainer.EndpointDefKey.suspendMaximumDuration):maxDuration%>"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="factor"/></td>
    <td><input type="text" id="factor" name="factor"
               value="<%=(factor==1.0)?TemplateConfigurationBuilder.getMappingFrom(templateMappings, TemplateParameterContainer.EndpointDefKey.suspendProgressionFactor):factor%>"/>
    </td>
</tr>

<tr>
    <td colspan="2" class="sub-header"><fmt:message
            key="on.timedout"/></td>
</tr>
<tr>
    <td><fmt:message key="error.codes"/></td>
    <td><input type="text" id="retryErroCode" name="retryErroCode"
               value="<%="".equals(timeOutErrorCode.trim())?TemplateConfigurationBuilder.getMappingFrom(templateMappings, TemplateParameterContainer.EndpointDefKey.timeoutErrorCodes):timeOutErrorCode%>" size="75"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="retry"/></td>
    <td><input type="text" id="retryTimeOut" name="retryTimeOut"
               value="<%=retryDelayTimeOut==0?TemplateConfigurationBuilder.getMappingFrom(templateMappings, TemplateParameterContainer.EndpointDefKey.retriesOnTimeoutBeforeSuspend):retryDelayTimeOut%>"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="retry.delay.millis"/></td>
    <td><input type="text" id="retryDelay" name="retryDelay"
               value="<%=(retryTimeOut==0)?TemplateConfigurationBuilder.getMappingFrom(templateMappings, TemplateParameterContainer.EndpointDefKey.retryDurationOnTimeout):retryTimeOut%>"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="disabled.error.codes"/></td>
    <td><input type="text" id="disabledErrorCodes" name="disabledErrorCodes"
               value="<%=retryDisabledErrorCodes%>" size="75"></td>
</tr>
<tr>
    <td colspan="2" class="sub-header"><fmt:message key="timeout"/></td>
</tr>
<tr>
    <td>
        <div class="indented"><fmt:message key="action"/></div>
    </td>
    <td><select name="actionSelect" onchange="activateDurationField(this)">
        <option value="<%=actionOptions[0]%>" <%=isNeverTimeout ? "selected=\"selected\"" : ""%>>
            <fmt:message key="action.never.timeout"/></option>
        <option value="<%=actionOptions[1]%>" <%=isDiscardMessage ? "selected=\"selected\"" : ""%>>
            <fmt:message key="action.discard.message"/></option>
        <option value="<%=actionOptions[2]%>" <%=isFaultSequence ? "selected=\"selected\"" : ""%>>
            <fmt:message key="action.execute.fault.sequence"/></option>
    </select>
    </td>
</tr>
<tr>
    <td>
        <div class="indented"><fmt:message key="duration.millis"/>
        </div>
    </td>
    <td><input id="actionDuration" type="text" name="actionDuration"
               value="<%=(actionDuration==0)?TemplateConfigurationBuilder.getMappingFrom(templateMappings, TemplateParameterContainer.EndpointDefKey.timeoutDuration):actionDuration%>"
        <%=actionDuration == 0 ? "disabled=\"disabled\"" : ""%>
    </td>
</tr>

<tr>
    <td colspan="2" class="sub-header"><fmt:message key="qos"/></td>
</tr>
<tr>
    <td><fmt:message key="ws.addressing"/></td>
    <td><input type="checkbox" onchange="showHideOnSelect('wsAddressing','tr_separate_listener')" id="wsAddressing" name="wsAddressing" value="wsAddressing"
        <%=isEnableAddressing ? "checked" : ""%>
    </td>
</tr>
<tr id="tr_separate_listener" style="display:<%=isEnableAddressing?"":"none" %>">
    <td>
        <div class="indented"><fmt:message
                key="seperate.listener"/></div>
    </td>
    <td><input type="checkbox" id="sepListener" name="sepListener" value="sepListener"
        <%=isSepListener ? "checked=\"checked\"" : ""%>
    </td>
</tr>
<tr>
    <td><fmt:message key="ws.security"/></td>
    <td><input type="checkbox" onclick="showHideOnSelect('wsSecurity','tr_ws_sec_policy_key')" id="wsSecurity" name="wsSecurity" value="wsSecurity"
            <%=isEnableWSSec ? "checked=\"checked\"" : ""%> />
    </td>
</tr>
<tr id="tr_ws_sec_policy_key" style="display:<%=isEnableWSSec?"":"none" %>">
    <td>
        <div class="indented"><fmt:message key="policy.key"/></div>
    </td>
    <td>
        <table class="normal">
            <tr>
                <td>
                    <input class="longInput" type="text" id="wsSecPolicyKeyID"
                           name="wsSecPolicyKeyID"
                           value="<%="".equals(secPolicy.trim())?TemplateConfigurationBuilder.getMappingFrom(templateMappings, TemplateParameterContainer.EndpointDefKey.policy):secPolicy%>"   <%=isFromTemplateEditor?"":"readonly=\"true\""%> />
                </td>
                <td>
                    <a href="#registryBrowserLink"
                       class="registry-picker-icon-link"
                       style="padding-left:20px;padding-right:20px"
                       onclick="showRegistryBrowser('wsSecPolicyKeyID', '/_system/config')"><fmt:message
                            key="registry.conf.keys"/></a>
                    <a href="#registryBrowserLink"
                       class="registry-picker-icon-link"
                       style="padding-left:20px"
                       onclick="showRegistryBrowser('wsSecPolicyKeyID', '/_system/governance')"><fmt:message
                            key="registry.gov.keys"/></a>
                </td>
            </tr>
        </table>

    </td>
</tr>
<tr>
    <td><fmt:message key="ws.rm"/></td>
    <td><input type="checkbox" onclick="showHideOnSelect('wsRM','tr_ws_rm_policy_key')" id="wsRM" name="wsRM" value="wsRM"
            <%=isEnableRM ? "checked=\"checked\"" : ""%> />
    </td>
</tr>
<tr id='tr_ws_rm_policy_key' style="display:<%=isEnableRM?"":"none" %>">
    <td>
        <div class="indented"><fmt:message
                key="policy.key"/></div>
    </td>
    <td>
        <table class="normal">
            <tr>
                <td><input class="longInput" type="text" id="wsrmPolicyKeyID"
                           name="wsrmPolicyKeyID"
                           value="<%="".equals(rmPolicy.trim())?TemplateConfigurationBuilder.getMappingFrom(templateMappings, TemplateParameterContainer.EndpointDefKey.wsRMPolicyKey):rmPolicy%>" <%=isFromTemplateEditor?"":"readonly=\"true\""%> />
                </td>
                <td><a href="#registryBrowserLink"
                       class="registry-picker-icon-link"
                       style="padding-left:20px;padding-right:20px"
                       onclick="showRegistryBrowser('wsrmPolicyKeyID', '/_system/config')"><fmt:message
                        key="registry.conf.keys"/></a>
                    <a href="#registryBrowserLink"
                       class="registry-picker-icon-link"
                       style="padding-left:20px"
                       onclick="showRegistryBrowser('wsrmPolicyKeyID', '/_system/governance')"><fmt:message
                        key="registry.gov.keys"/></a>
                </td>
            </tr>
        </table>
    </td>
</tr>
<tr>
    <tr>
        <td colspan="2" class="sub-header"><fmt:message key="endpoint.description.hader"/></td>
    </tr>
    <tr>
    <td>
        <div class="indented"><fmt:message key="endpoint.description"/></div>
    </td>
    <td>
        <textarea name="endpointDescription" id="endpointDescription" title="Endpoint Description"
                                          cols="100" rows="3"><%= description %></textarea>
    </td>
</tr>
</tr>

</tbody>
</table>
</div>
<table class="normal-nopadding">
     <tbody>
     <tr>
         <td colspan="2" class="sub-header"><fmt:message key="endpoint.property.header"/></td>
     </tr>
    <tr><td colspan="2">
        <a href="#" onclick="addServiceParams('headerTable')"
           style="background-image: url('../admin/images/add.gif');" class="icon-link">Add
            Property</a>
        <input type="hidden" name="endpointProperties" id="endpointProperties"/>
        </td>
    </tr>
    <tr>
        <table cellpadding="0" cellspacing="0" border="0" class="styledLeft"
               id="headerTable"
               style="display:none;">
            <thead>
            <tr>
                <th style="width:25%"><fmt:message key="param.name"/></th>
                <th style="width:25%"><fmt:message key="param.value"/></th>
                <th style="width:25%"><fmt:message key="param.scope"/></th>
                <th style="width:25%"><fmt:message key="param.action"/></th>
            </tr>
            </thead>
            <tbody></tbody>
        </table>
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
            if (!isAnonymous && !isFromTemplateEditor)  {
        %>
            <input type="button" value="<fmt:message key="saveas"/>" class="button" name="save"
                   onclick="javascript:showSaveAsForm(true);"/>
        <%
            } else if (isFromTemplateEditor && session.getAttribute("templateEdittingMode") == null) {
        %>
        <input type="button" value="<fmt:message key="saveas"/>" class="button" name="save"
               onclick="javascript:showSaveAsForm(true);"/>
        <%
            }
        %>
        <input type="button" value="<fmt:message key="cancel"/>" name="cancel" class="button"
               onclick="javascript:cancelEndpointData('<%=anonymouseOriginator%>','<%=isFromTemplateEditor%>');"/>
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
                                                       value="conf:" checked="checked"
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
