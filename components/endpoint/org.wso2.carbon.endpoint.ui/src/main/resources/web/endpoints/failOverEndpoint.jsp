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
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="java.util.regex.Matcher" %>
<%@ page import="org.wso2.carbon.endpoint.ui.client.EndpointAdminClient" %>
<%@ page import="javax.xml.stream.XMLStreamException" %>
<%@ page import="org.apache.synapse.endpoints.FailoverEndpoint" %>
<%@ page import="org.apache.synapse.config.xml.endpoints.EndpointFactory" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.impl.llom.util.AXIOMUtil" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.endpoint.ui.util.EndpointConfigurationHelper" %>
<%@ page import="java.util.Properties" %>
<%@ page import="org.apache.synapse.mediators.MediatorProperty" %>
<%@ page import="java.util.Collection" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>


<link type="text/css" rel="stylesheet" href="css/style.css"/>
<link type="text/css" rel="stylesheet" href="css/menu.css"/>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../sequences/js/registry-browser.js"></script>

<fmt:bundle basename="org.wso2.carbon.endpoint.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
        request="<%=request%>"/>
<carbon:breadcrumb
label="failover.endpoint"
resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
topPage="false"
request="<%=request%>"/>


<%
    ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.sequences.ui.i18n.Resources", request.getLocale());
    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    EndpointAdminClient adminClient = new EndpointAdminClient(cookie, url, configContext);
    String failOverData = null;

    String endpointName = request.getParameter("endpointName");
    String endpointAction = request.getParameter("endpointAction");
    String design = request.getParameter("design");

    String endpointMode = null; // this holds an annonymos endpoint which can come trough proxy and send mediator
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
        if (endpointName != null) {
            //TODO - add a method which returns a failOver object given the endpointName
            try {
                failOverData = adminClient.getEndpoint(endpointName);
            } catch (Exception e) {
                String msg = "Unable to get failOver Endpoint data: " + e.getMessage();
                CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
            }
        }

    } else if (design != null && !"".equals(design)) {
        // coming through source view using switchToDesign
        failOverData = design.replaceAll("\\s\\s+|\\n|\\r", ""); // remove the pretty printing from the string
        failOverData = EndpointConfigurationHelper.getValidStringXMlStringForAMP(failOverData);

    } else if (isAnonymous) {
        // coming through using either send mediator or proxy services by adding an annonymous endpoint
        //we are in anonymous mode
        anonymouseOriginator = (String) session.getAttribute("anonOriginator");
        anonymousEndpointXML = (String) session.getAttribute("anonEpXML");

        if (anonymousEndpointXML != null && !"".equals(anonymousEndpointXML)) {
            // if a user is here that mean user is trying to edit an existing anoymous endpoint
            try {
                failOverData = EndpointAdminClient.getAnonEpXMLwithName(anonymousEndpointXML);
            } catch (XMLStreamException e) {
                session.removeAttribute("anonEpXML");
%>
<script type="text/javascript">
    CARBON.showErrorDialog("Unable to create endpoint with given data");
    window.location.href = "failOverEndpoint.jsp";
</script>
<%
            }
        }
    }

    // if failOver data is null that mean user is adding a new failOver endpoint
    String failOverEndpointName = "";
    String properties = "";
    FailoverEndpoint failOverDataElement = null;
    if (failOverData != null) {
        String formattedEpDate;
        Pattern pattern = Pattern.compile("\\s+");
        Matcher matcher = pattern.matcher(failOverData);
        formattedEpDate = "" + matcher.replaceAll(" ") + "\r\n";
        formattedEpDate = formattedEpDate.replace("> <", "><");
        formattedEpDate = formattedEpDate.trim();
        OMElement element = null;

        try {
            // we have a omelement of the endpoint configuration
            element = AXIOMUtil.stringToOM(formattedEpDate);
            failOverData = formattedEpDate;
        } catch (XMLStreamException e) {
            String msg = "Unable to create omelement from endpoint data: " + e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
        }

        try {
             failOverDataElement = (FailoverEndpoint) EndpointFactory.getEndpointFromElement(element, false, new Properties());
        } catch(Exception e){
             String msg = "Unable to create Failover endpoint from endpoint data: " + e.getMessage();
             CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
        }

        if (failOverDataElement != null && failOverDataElement.getName() != null) {
            failOverEndpointName = failOverDataElement.getName();
        }else{
            failOverEndpointName = endpointName;
        }
        Collection<MediatorProperty> props = failOverDataElement.getProperties();
        properties = EndpointConfigurationHelper.buildPropertyString(props);
    }

%>

<script type="text/javascript" src="js/addressEndpoint.js"></script>
<script type="text/javascript" src="js/WSDLendpoint.js"></script>
<script type="text/javascript" src="js/fo-lb-endpoint-util.js"></script>
<script type="text/javascript" src="js/endpoint-util.js"></script>
<script type="text/javascript" src="js/endpoint-form.js"></script>
<script type="text/javascript" src="js/endpoint-params.js"></script>
<script type="text/javascript">

    YAHOO.util.Event.onDOMReady(init);

    function init(){
        generateServiceParamTable();
    }

    //    debugger;
    // the saved endpoint confiuration which is used to generate the design view if available
    var endpointConfiguration = '<%=failOverData%>';
    <%--var endpointConfiguration = new String(<%=failOverData%>);--%>

    // keep track  the adding endpoint configuration
    var doc;

    // We are dealing with failOver type
    var mainType = 'failover';

    // inilize the 'doc' above
    initDoc();

    //This is the variable use to keep the selected tag in the browser
    var selectedFlag = mainType + '.0';

    //Creating the menu
    var oMenu;
    createMenu();

    // once the document is ready starting from initilazing the endpoints
    jQuery(document).ready(function() {
        initEndpoints();
    });

    function submitEndpointData(isAnonymous) {
        //update current editing address/wsdl endpoint
        if (document.getElementById('endpointDesign').style.display == '') {
            if (!updateCurrentEndpoint()) {
                return false;
            }
        }
        // update the endpoint name
        if (isAnonymous == 'false') {
            var endpointName = document.getElementById('failover.name').value;
            if (endpointName == undefined || endpointName == null || endpointName == '') {
                CARBON.showWarningDialog(jsi18n['empty.loadbalance.failover.endpooint.name']);
                return false;
            }
            getNodeById("failover.0").setAttribute('name', endpointName);
        }
        updateProperties(getNodeById("failover.0"));
        var failOverConfDoc = cleanIDs(doc); // clean 'id' attributes
        var failOverConfString = xmlToString(failOverConfDoc);
        failOverConfString = failOverConfString.replace(/ xmlns=""/g,"");
        document.failOverEndpointForm.action = 'saveEndpoint.jsp?endpointType=failOverEndpoint';
        document.failOverEndpointForm.failOverConf.value = failOverConfString;
        document.failOverEndpointForm.submit();
        return true;
    }

    function submitDynamicEndpointData() {
        //update current editing address/wsdl endpoint
        if (document.getElementById('endpointDesign').style.display == '') {
            if (!updateCurrentEndpoint()) {
                return false;
            }
        }
        var key = document.getElementById('synRegKey').value;
        if (key == '') {
            CARBON.showWarningDialog('<fmt:message key="empty.key.field"/>');
            return false;
        }
        
        // update the endpoint name
        var endpointName = document.getElementById('failover.name').value;
        getNodeById("failover.0").setAttribute('name', endpointName);
        updateProperties(getNodeById("failover.0"));
        var failOverConfDoc = cleanIDs(doc); // clean 'id' attributes
        var failOverConfString = xmlToString(failOverConfDoc);
        failOverConfString = failOverConfString.replace(/ xmlns=""/g,"");
        var registry;
        if (document.getElementById("config_reg").checked == true) {
            registry = 'conf';
        } else {
            registry = 'gov';
        }
        document.failOverEndpointForm.action = 'saveDynamicEndpoint.jsp?registry='+registry
                +'&endpointType=failOverEndpoint&regKey=' + key;
        document.failOverEndpointForm.failOverConf.value = failOverConfString;
        document.failOverEndpointForm.submit();
        return true;
    }

    function updateCurrentEndpoint() {
        var endpointNode = getNodeById(selectedFlag);
        var ePtype;
        var formID = "form_" + selectedFlag;
        if (selectedFlag.indexOf('address') != -1) {
            ePtype = 'address';
        } else if (selectedFlag.indexOf('wsdl') != -1) {
            ePtype = 'wsdl';
        }
        // validate and update
        if (ePtype == 'address' || ePtype == 'wsdl') {
            if (!isValidChildAddressOrWSDLEndpoint(formID, ePtype)) {
                return false;
            }
            // updating the child endpoint
            updateAddressOrWSDLEndpoint(formID, ePtype, endpointNode);
        } else if (ePtype == 'loadbalance') {
            // we are updating the child endpoint
            if (!updateLoadBalanceEndpoint(formID, endpointNode)) {
                CARBON.showWarningDialog(jsi18n['error.updating.the.configuration']);
                return false;
            }
        }

        // hide the child endpoing form
        var childEndpointForm = document.getElementById('endpointDesign');
        if (childEndpointForm != null && childEndpointForm != undefined) {
            childEndpointForm.style.display = 'none';
            return true;
        }
        return false;
    }

    function switchToSource(isAnonymous) {
        //update current editing address/wsdl endpoint
        if (document.getElementById('endpointDesign').style.display == '') {
            if (!updateCurrentEndpoint()) {
                return false;
            }
        }
        
        // update the endpoint name
        var endpointName;
        if (isAnonymous == 'false') {
            endpointName = document.getElementById('failover.name').value;
            if (endpointName == undefined || endpointName == null || endpointName == '') {
                CARBON.showWarningDialog(jsi18n['empty.loadbalance.failover.endpooint.name']);
                return false;
            }
            getNodeById("failover.0").setAttribute('name', endpointName);
        }
        updateProperties(getNodeById("failover.0"));
        var failOverConfDoc = cleanIDs(doc); // clean 'id' attributes
        var failOverConfString = xmlToString(failOverConfDoc);
        failOverConfString = failOverConfString.replace(/ xmlns=""/g,"");
        document.failOverEndpointForm.action = 'endpointSourceView.jsp?endpointType=failOverEndpoint&retainlastbc=true';
        document.failOverEndpointForm.failOverConf.value = failOverConfString;
        document.failOverEndpointForm.endpointName.value = endpointName;
        document.failOverEndpointForm.submit();
        return true;
    }

    function showSaveAsForm(show) {
        var formElem = document.getElementById('saveAsForm');
        if (show) {
            formElem.style.display = "";
            var keyField = document.getElementById('synRegKey');
            if (keyField.value == '') {
                keyField.value = document.getElementById('failover.name').value;
            }
        } else {
            formElem.style.display = "none";
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

    function updateProperties(endpointNode){
        var propertyCount = 0;
        for (i = 0; i < endpointNode.childNodes.length; i++) {
            if (endpointNode.childNodes[i].nodeName == 'property') {
                ++propertyCount;
            }
        }

        for (i = 0; i < propertyCount; i++) {
            for (j = 0; j < endpointNode.childNodes.length; j++) {
                if (endpointNode.childNodes[j].nodeName == 'property') {
                    var n = endpointNode.childNodes[j];
                    endpointNode.removeChild(n);
                }
            }
        }
        var headerTable = document.getElementById("headerTable");
        for (var j = 1; j < headerTable.rows.length; j++) {
            var propertyName = headerTable.rows[j].getElementsByTagName("input")[0].value;
            var propertyValue = headerTable.rows[j].getElementsByTagName("input")[1].value;
            var propertyScope = headerTable.rows[j].getElementsByTagName("select")[0].value;
            var propNode = doc.createElement('property');
            propNode.setAttribute('name', propertyName);
            propNode.setAttribute('value', propertyValue);
            propNode.setAttribute('scope', propertyScope);
            endpointNode.appendChild(propNode);
        }
        return true;
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
            %><fmt:message key="failover.endpoint"/><%
           }
        %>
    </h2>

    <div id="workArea">
        <form id="failOverEndpointForm" name="failOverEndpointForm" action="" method="POST">
            <input type="hidden" id="failOverConf" name="failOverConf" value=""/>
            <table class="styledLeft" cellspacing="0">
                <thead>
                <tr>
                    <th>
                <span style="float: left; position: relative; margin-top: 2px;"><fmt:message
                        key="design.view.of.the.failover.group"/></span>
                        <a href="#" onclick="switchToSource('<%=isAnonymous%>');" class="icon-link"
                           style="background-image: url(images/source-view.gif);"><fmt:message
                                key="switch.to.source.view"/></a>
                    </th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        <table class="normal" width="100%">
                            <tr style="<%=!isAnonymous?"":"display:none"%>">
                                <td class="leftCol-small">
                                    <fmt:message key="endpoint.name"/> <span
                                        class="required">*</span>
                                </td>
                                <td>
                                    <input type="text" id="failover.name"
                                           value="<%=failOverEndpointName %>" <%= !"".equals(failOverEndpointName) ? "disabled=\"disabled\"" : "" %>
                                           onkeypress="return validateText(event);"/>
                                    <input type="hidden" name="endpointName"
                                           value="<%=failOverEndpointName%>"/>
                                    <input type="hidden" name="isAnnonEndpointID"
                                           id="isAnnonEndpointID" value="<%=isAnonymous%>"/>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="3">
                                    <div class="treePane" id="treePane"
                                         style="height: 300px; overflow: auto; width: auto; border: 1px solid rgb(204, 204, 204);position:relative;">
                                        <div style="position:absolute;padding:20px;">
                                            <ul class="root-list" id="failoverTree">
                                                <li>
                                                    <div class="minus-icon"
                                                         onclick="treeColapse(this)"
                                                         id="treeColapser"></div>
                                                    <div class="endpoints" id="failover.0">
                                                        <a class="root-endpoint"><fmt:message
                                                                key="endpoint.root"/></a>

                                                        <div class="sequenceToolbar"
                                                             style="width:100px;">
                                                            <div>
                                                                <a class="addChildStyle"><fmt:message
                                                                        key="add.endpoint"/></a>
                                                            </div>
                                                        </div>
                                                    </div>

                                                </li>
                                            </ul>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        </table>
                        <div id="endpointDesign" style="display:none">
                            <table class="normal" width="100%">
                                <tr>
                                    <td>
                                        <table class="styledLeft" cellspacing="0">
                                            <tr>
                                                <td class="middle-header">
                                        <span style="float:left;position:relative; margin-top:2px;">
                                            <fmt:message key="design.view.of.the.endpoint"/></span>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="padding: 0px !important;">
                                                    <div id="info" class="tabPaneContentMain"
                                                         style="width:auto;padding:0px;"></div>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </div>


                    </td>
                </tr>
                <tr>
                        <td>
                            <table class="normal-nopadding">
                                <tbody>
                                <tr>
                                    <td colspan="2" class="sub-header"><fmt:message
                                            key="fo.endpoint.property.header"/></td>
                                </tr>
                                <tr><td colspan="2">
                                    <a href="#" onclick="addServiceParams('headerTable')"
                                       style="background-image: url('../admin/images/add.gif');"
                                       class="icon-link">Add
                                        Property</a>
                                    <input type="hidden" name="endpointProperties"
                                           id="endpointProperties"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                    <table cellpadding="0" cellspacing="0" border="0"
                                           class="styledLeft"
                                           id="headerTable"
                                           style="display:none;">
                                        <thead>
                                        <tr>
                                            <th style="width:25%"><fmt:message
                                                    key="param.name"/></th>
                                            <th style="width:25%"><fmt:message
                                                    key="param.value"/></th>
                                            <th style="width:25%"><fmt:message
                                                    key="param.scope"/></th>
                                            <th style="width:25%"><fmt:message
                                                    key="param.action"/></th>
                                        </tr>
                                        </thead>
                                        <tbody></tbody>
                                    </table>
                                </tr>
                                </tbody>
                            </table>
                            </td>
                        </td>
                    </tr>
                <tr>
                    <td class="buttonRow">
                        <input type="button" value="<fmt:message key="save"/>"
                               class="button"
                               name="save"
                               onclick="submitEndpointData('<%=isAnonymous%>');"/>
                        <%
                            if (!isAnonymous)  {
                        %>
                        <input type="button" value="<fmt:message key="saveas"/>" class="button" name="save"
                               onclick="javascript:showSaveAsForm(true);"/>
                        <%
                            }
                        %>
                        <input type="button" value="<fmt:message key="cancel"/>"
                               name="cancel"
                               class="button"
                               onclick="cancelEndpointData('<%=anonymouseOriginator%>');"/>
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
                                <tr>
                                    <td><fmt:message key="registry.key"/></td>
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
        </form>
    </div>
</div>


</fmt:bundle>
