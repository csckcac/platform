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
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="org.wso2.carbon.utils.xml.XMLPrettyPrinter" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<script src="../editarea/edit_area_full.js" type="text/javascript"></script>

<link type="text/css" rel="stylesheet" href="css/style.css"/>

<%
    boolean isError = false;
    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    EndpointAdminClient client = new EndpointAdminClient(cookie, url, configContext);

    EndpointConfigurationHelper epConfigHelper = new EndpointConfigurationHelper();
    String endpointType = request.getParameter("endpointType");
    String endpointName = request.getParameter("endpointName");
    String templateName = request.getParameter("templateName");

    String endpointAction = (String) session.getAttribute("edit" + endpointName);

    String endpointMode = null; // this holds an annonymos endpoint which can come trough proxy and send mediator
    String anonymouseOriginator = null;
    anonymouseOriginator = (String) session.getAttribute("anonOriginator");
    boolean isAnonymous = false;
    String anonymousEndpointXML = null;
    endpointMode = (String) session.getAttribute("epMode");
    if (endpointMode != null && "anon".equals(endpointMode)) {
        isAnonymous = true;
    }

    boolean isFromTemplateEditor = session.getAttribute("endpointTemplate") != null ? true:false;

    String configuration = null;

    // build the configuration
    if (endpointType.equals("addressEndpoint") && !"".equals("addressEndpoint")) {
        if (isFromTemplateEditor) {
            configuration = epConfigHelper.buildTemplateConfiguration(request, endpointType, isAnonymous);
        }else{
            configuration = epConfigHelper.buildAddressOrWSDLEpConfiguration(request, endpointType, isAnonymous);
        }
    } else if (endpointType.equals("WSDLEndpoint") && !"".equals("WSDLEndpoint")) {
        if (isFromTemplateEditor) {
            configuration = epConfigHelper.buildTemplateConfiguration(request, endpointType, isAnonymous);
        }else{
            configuration = epConfigHelper.buildAddressOrWSDLEpConfiguration(request, endpointType, isAnonymous);
        }
    } else if(endpointType.equals("defaultEndpoint") && !"".equals("defaultEndpoint")) {
        if (isFromTemplateEditor) {
            configuration = epConfigHelper.buildTemplateConfiguration(request, endpointType, isAnonymous);
        }else{
            configuration = epConfigHelper.buildAddressOrWSDLEpConfiguration(request, endpointType, isAnonymous);
        }
    }else if (endpointType.equals("loadBalanceEndpoint") && !"".equals("loadBalanceEndpoint")) {
        configuration = request.getParameter("loadBalanceConf");

    } else if (endpointType.equals("failOverEndpoint") && !"".equals("failOverEndpoint")) {
        configuration = request.getParameter("failOverConf");

    } else if (endpointType.equals("templateEndpoint")) {
            configuration = EndpointConfigurationHelper.buildTemplateEndpointConfiguration(request, endpointType, isAnonymous);
    }

    String prettyPrintPayload = "";
    if (configuration != null) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(configuration.getBytes());
        XMLPrettyPrinter xmlPrettyPrinter = new XMLPrettyPrinter(inputStream);
        prettyPrintPayload = xmlPrettyPrinter.xmlFormat();
    }

%>
<fmt:bundle basename="org.wso2.carbon.endpoint.ui.i18n.Resources">
    <carbon:breadcrumb
            label="address.endpoint.source"
            resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript" src="js/endpoint-util.js"></script>
    <script type="text/javascript">

        function submitEndpointData(isAnonymous) {
            // we just submit the data to saveEndpoint.jsp page since the validation is already done
            // in switchToSoruce method
            document.getElementById("xmlPay").value = editAreaLoader.getValue("xmlPay");
            var isValidXML = isValidXml(trim(document.getElementById('xmlPay').value));
            if(!isValidXML){
                return false;
            }
            if (isAnonymous == 'true') {
                document.sourceViewForm.action = 'saveEndpoint.jsp?endpointType=<%=endpointType%>';
            } else {
                document.sourceViewForm.action = 'saveEndpoint.jsp?endpointType=<%=endpointType%>&endpointName=<%=endpointName%><%=isFromTemplateEditor?"&templateName="+templateName:""%>';
            }

            document.sourceViewForm.submit();
            return true;
        }

        function submitDynamicEndpointData() {
            document.getElementById("xmlPay").value = editAreaLoader.getValue("xmlPay");
            var isValidXML = isValidXml(trim(document.getElementById('xmlPay').value));
            if(!isValidXML){
                return false;
            }
            var key = document.getElementById('synRegKey').value;
            if (key == '') {
                CARBON.showWarningDialog('<fmt:message key="empty.key.field"/>');
                return false;
            }

            // we just submit the data to saveDynamicEndpoint.jsp page since the validation is already done
            // in switchToSoruce method
            var registry;
            if (document.getElementById("config_reg").checked) {
                registry = 'conf';
            } else {
                registry = 'gov';
            }
            document.sourceViewForm.action = 'saveDynamicEndpoint.jsp?registry='+registry
                    +'&endpointType=<%=endpointType%>&endpointName=<%=endpointName%><%=isFromTemplateEditor?"&templateName="+templateName:""%>&regKey=' + key;
            document.sourceViewForm.submit();
            return true;
        }

        function showSaveAsForm(show) {
            var formElem = document.getElementById('saveAsForm');
            if (show) {
                formElem.style.display = "";
            } else {
                formElem.style.display = "none";
            }
        }

        function switchToDesgin() {
            document.getElementById("xmlPay").value = editAreaLoader.getValue("xmlPay");
            var isValidXML = isValidXml(trim(document.getElementById('xmlPay').value));
            if(!isValidXML){
                return false;
            }
            var endpointType = '<%=endpointType%>';
            if (endpointType == 'addressEndpoint') {
                document.sourceViewForm.action = 'addressEndpoint.jsp?ordinal=1';
            } else if (endpointType == 'WSDLEndpoint') {
                document.sourceViewForm.action = 'WSDLEndpoint.jsp?ordinal=1';
            } else if (endpointType == 'failOverEndpoint') {
                document.sourceViewForm.action = 'failOverEndpoint.jsp?ordinal=1';
            } else if (endpointType == 'loadBalanceEndpoint') {
                document.sourceViewForm.action = 'loadBalanceEndpoint.jsp?ordinal=1';
            } else if (endpointType == 'defaultEndpoint') {
                document.sourceViewForm.action = 'defaultEndpoint.jsp?ordinal=1';                
            } else if (endpointType == 'templateEndpoint') {
                document.sourceViewForm.action = 'templateEndpoint.jsp?ordinal=1';
            }
            document.sourceViewForm.endpointName.value = '<%=endpointName%>';
            document.sourceViewForm.submit();
        }

    </script>

    <div id="middle">
        <h2><fmt:message key="source.of.endpoint"/></h2>

        <div id="workArea">
            <form name="sourceViewForm" action="" method="POST">
                <input type="hidden" id="endpointName" name="endpointName" value=""/>
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th>
                            <span style="float: left; position: relative; margin-top: 2px;">
                                <%
                                    if ("addressEndpoint".equals(endpointType)) {
                                %><fmt:message key="source.view.of.the.addressendpoint"/><%
                            } else if ("defaultEndpoint".equals(endpointType)) {
                            %><fmt:message key="source.view.of.the.defaultendpoint"/><%
                            } else if ("wsdlEndpoint".equals(endpointType)) {
                            %><fmt:message key="source.view.of.the.wsdlendpoint"/><%
                            } else if ("loadBalanceEndpoint".equals(endpointType)) {
                            %><fmt:message key="source.view.of.the.loadbalance.endpoint"/><%
                            } else if ("failOverEndpoint".equals(endpointType)) {
                            %><fmt:message key="source.view.of.the.failovergroup"/><%
                                }
                            %>
                            </span>
                            <a href="#" class="icon-link"
                               style="background-image: url(images/design-view.gif);"
                               onclick="switchToDesgin()"> <fmt:message
                                    key="switch.to.design.view"/></a>
                        </th>
                    </tr>
                    </thead>
                    <tr>
                        <td>
                            <textarea id="xmlPay" name="design"
                                      style="border: 0px solid rgb(204, 204, 204); width: 99%; height: 275px; margin-top: 5px;"
                                      rows="30"><%=prettyPrintPayload%>
                            </textarea>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" value="<fmt:message key="save"/>" class="button"
                                   name="save" onclick="submitEndpointData('<%=isAnonymous%>');"/>
                            <%
                                if (!isAnonymous)  {
                            %>
                            <input type="button" value="<fmt:message key="saveas"/>" class="button" name="save"
                                   onclick="javascript:showSaveAsForm(true);"/>
                            <%
                                }
                            %>
                            <input type="button" value="<fmt:message key="cancel"/>" name="cancel"
                                   class="button"
                                   onclick="cancelEndpointData('<%=anonymouseOriginator%>','<%=isFromTemplateEditor%>');"/>
                        </td>
                    </tr>

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
                                        <td><fmt:message key="gov.registry"/> <input type="radio" name="registry" id="gov_reg"
                                                                       value="gov:" checked="checked"
                                                                       onclick="document.getElementById('reg').innerHTML='gov:';"/>
                                            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                            <fmt:message key="config.registry"/> <input type="radio" name="registry" id="config_reg"
                                                                   value="conf:"
                                                                   onclick="document.getElementById('reg').innerHTML='conf:';"/>
                                        </td>
                                    </tr>
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

    <script type="text/javascript">
        editAreaLoader.init({
            id : "xmlPay"		// textarea id
            ,syntax: "xml"			// syntax to be uses for highgliting
            ,start_highlight: true		// to display with highlight mode on start-up
        });
    </script>
</fmt:bundle>
