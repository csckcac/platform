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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.endpoint.common.to.EndpointMetaData" %>
<%@ page import="org.wso2.carbon.endpoint.ui.client.EndpointAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonError" %>
<%@ page import="org.wso2.carbon.endpoint.ui.util.EndpointConfigurationHelper" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>

<!-- Dependencies -->
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<link rel="stylesheet" type="text/css" href="../yui/build/container/assets/skins/sam/container.css">

<script type="text/javascript" src="../yui/build/container/container-min.js"></script>
<script type="text/javascript" src="../yui/build/element/element-min.js"></script>
<script type="text/javascript" src="../admin/js/widgets.js"></script>
<!-- Connection handling lib -->
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<!-- Source File -->

<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>
<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet" />
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
 <script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>

<fmt:bundle basename="org.wso2.carbon.endpoint.ui.i18n.Resources">
<carbon:breadcrumb
        label="endpoints"
        resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<script type="text/javascript">

    function enableStat(endpointName) {
        $.ajax({
            type: 'POST',
            url: 'stat-ajaxprocessor.jsp',
            data: 'endpointName=' + endpointName + '&action=enableStat',
            success: function(msg) {
                handleStatCallback('enableStat', endpointName);
            }
        });
    }

    function disableStat(endpointName) {
        $.ajax({
            type: 'POST',
            url: 'stat-ajaxprocessor.jsp',
            data: 'endpointName=' + endpointName + '&action=disableStat',
            success: function(msg) {
                handleStatCallback('disableStat', endpointName);
            }
        });
    }

    function handleStatCallback(action, endpointName) {
        var element;
        if (action == 'enableStat') {
            element = document.getElementById("disableStat" + endpointName);
            element.style.display = "";
            element = document.getElementById("enableStat" + endpointName);
            element.style.display = "none";
        } else {
            element = document.getElementById("disableStat" + endpointName);
            element.style.display = "none";
            element = document.getElementById("enableStat" + endpointName);
            element.style.display = "";
        }
    }

    function switchOn(endpointName) {
        $.ajax({
            type: 'POST',
            url: 'switchOnOff-ajaxprocessor.jsp',
            data: 'endpointName=' + endpointName + '&action=switchOn',
            success: function(msg) {
                handleSwitchOnOffCallback('switchOn', endpointName);
            }
        });
    }

    function switchOff(endpointName) {
        $.ajax({
            type: 'POST',
            url: 'switchOnOff-ajaxprocessor.jsp',
            data: 'endpointName=' + endpointName + '&action=switchOff',
            success: function(msg) {
                handleSwitchOnOffCallback('switchOff', endpointName);
            }
        });
    }

    function handleSwitchOnOffCallback(action, endpointName) {
        var element;
        if (action == 'switchOn') {
            element = document.getElementById("switchOff" + endpointName);
            element.style.display = "";
            element = document.getElementById("switchOn" + endpointName);
            element.style.display = "none";
        } else {
            element = document.getElementById("switchOff" + endpointName);
            element.style.display = "none";
            element = document.getElementById("switchOn" + endpointName);
            element.style.display = "";
        }
    }

    function editEndpoint(endPointName, endPointType) {
        if (endPointType == 0) {
            document.location.href = 'addressEndpoint.jsp?endpointName=' + endPointName + '&endpointAction=edit';
        } else if (endPointType == 1) {
            document.location.href = 'WSDLEndpoint.jsp?endpointName=' + endPointName + '&endpointAction=edit';
        } else if (endPointType == 2) {
            document.location.href = 'failOverEndpoint.jsp?endpointName=' + endPointName + '&endpointAction=edit';
        } else if (endPointType == 3) {
            document.location.href = 'loadBalanceEndpoint.jsp?endpointName=' + endPointName + '&endpointAction=edit';
        } else if (endPointType == 4) {
            document.location.href = 'defaultEndpoint.jsp?endpointName=' + endPointName + '&endpointAction=edit';
        } else if (endPointType == 5) {
            document.location.href = 'templateEndpoint.jsp?endpointName=' + endPointName + '&endpointAction=edit';
        }
    }

    function deleteEndpoint(endpointName) {
        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.the.endpoint"/> " + endpointName + " ?", function() {
            location.href = "deleteEndpoint.jsp?endpointName=" + endpointName;
        });
    }

    function goBack(orginiator) {
        if (orginiator == null) {
            alert('Error: where to go?');
            return false;
        }
        document.location.href = orginiator + '?cancelled=true';
        return true;
    }

    function confirmForceDelete(epr, msg) {
        CARBON.showConfirmationDialog('This endpoint is a dependency for following items!<br/><br/>'
                + msg + '<br/>Force delete?', function() {
            location.href = "deleteEndpoint.jsp?force=true&endpointName=" + epr
        });
    }
    function editRegistryEndpoint(key) {
        if (key != null && key != undefined && key != "") {
            location.href = "registry_endpoint.jsp?anonEpAction=edit&key=" + key;
        } else {
            CARBON.showErrorDialog("Specify the key of the Endpoint to be edited");
        }
    }

    function deleteRegistryEndpoint(key) {
        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.the.endpoint"/> " + key + "?", function() {
            location.href = "deleteEndpoint.jsp?endpointType=registry&endpointName=" + key;
        });
    }

    var isDefinedSequenceFound = false;
    var isDynamicSequenceFound = false;
    $(function() {
        $("#tabs").tabs();
    });

    $(document).ready(function() {
        var $tabs = $('#tabs > ul').tabs({ cookie: { expires: 30 } });
        $('a', $tabs).click(function() {
            if ($(this).parent().hasClass('ui-tabs-selected')) {
                $tabs.tabs('load', $('a', $tabs).index(this));
            }
        });
        <%
        String tabs = request.getParameter("tabs");
        if(tabs!=null && tabs.equals("0")) {
            %>$tabs.tabs('option', 'selected', 0);<%
        }else if(tabs!=null && tabs.equals("1")){
            %>$tabs.tabs('option', 'selected', 1);<%
        } 
        %>
        if (!isDefinedSequenceFound && !isDynamicSequenceFound) {
            $tabs.tabs('option', 'selected', 2);
        }
    });
</script>

<%
    String endpointTyepes[] = {"Address Endpoint", "WSDL Endpoint", "Failover group",
            "Load-balance group","Default Endpoint","Template Endpoint","Invalid Configuration"};
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
            getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    EndpointAdminClient client = new EndpointAdminClient(cookie, serverURL, configContext);
    EndpointMetaData[] ePMetaData = null;
    String[] dynamicEndpoints = null;
    CarbonError carbonError;

    String pageNumberStr = request.getParameter("pageNumber");
    String dynamicPageNumberStr = request.getParameter("dynamicPageNumber");

    //template specific params
    String templateAdd = request.getParameter("templateAdd");
//    System.out.println("tempalteADD From template editor: " + templateAdd);
    boolean isTemplateAdd = templateAdd != null && "true".equals(templateAdd) ? true : false;

    int pageNumber = 0;
    int dynamicPageNumber = 0;
    if (pageNumberStr != null) {
        pageNumber = Integer.parseInt(pageNumberStr);
    }
    if(dynamicPageNumberStr!=null){
        dynamicPageNumber = Integer.parseInt(dynamicPageNumberStr);
    }
    int numberOfPages = 0;
    int numberOfDynamicPages = 0;

    try {
        ePMetaData = client.getEndpointMetaData(pageNumber,EndpointAdminClient.ENDPOINT_PER_PAGE);
        dynamicEndpoints = client.getDynamicEndpoints(dynamicPageNumber,EndpointAdminClient.ENDPOINT_PER_PAGE);

        int epCount = client.getEndpointCount();
        int dynamicEpCount = client.getDynamicEndpointCount();

        if (epCount % EndpointAdminClient.ENDPOINT_PER_PAGE == 0) {
            numberOfPages = epCount / EndpointAdminClient.ENDPOINT_PER_PAGE;
        } else {
            numberOfPages = epCount / EndpointAdminClient.ENDPOINT_PER_PAGE + 1;
        }

        if (dynamicEpCount % EndpointAdminClient.ENDPOINT_PER_PAGE == 0) {
            numberOfDynamicPages = dynamicEpCount / EndpointAdminClient.ENDPOINT_PER_PAGE;
        } else {
            numberOfDynamicPages = dynamicEpCount / EndpointAdminClient.ENDPOINT_PER_PAGE + 1;
        }
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
        <script type="text/javascript">
               location.href = "../admin/error.jsp";
        </script>
<%


    }
    String endpointMode = null; // this holds an annonymos endpoint which can come trough proxy and send mediator
    String anonymouseOriginator = null;
    boolean isAnonymous = false;
    String anonymousEndpointXML = null;

    // came throuh clicking menu on the left
    if (request.getParameter("region") != null && request.getParameter("item") != null) {
        session.removeAttribute("epMode");
        session.removeAttribute("anonEpXML");
        session.removeAttribute("proxy");
        session.removeAttribute("header");
        session.removeAttribute("endpointTemplate");
        session.removeAttribute("templateEdittingMode");
        session.removeAttribute("templateRegKey");
    } else {
        // a user is adding an anonymous endpoint
        endpointMode = (String) session.getAttribute("epMode");
        if (endpointMode != null && endpointMode.equals("anon")) {
            // if the user is here that means the user is going to add (not edit an existing endpoint)
            // a new endpoint
            isAnonymous = true;
            anonymouseOriginator = (String) session.getAttribute("anonOriginator");
        }
    }

    String dependencyMgtError = (String) session.getAttribute("dependency.mgt.error");
    if (dependencyMgtError != null) {
        String eprToDelete = (String) session.getAttribute("dependency.mgt.error.epr");
%>
<script type="text/javascript">
    confirmForceDelete('<%=eprToDelete%>', '<%=dependencyMgtError%>');
</script>
<%
        session.removeAttribute("dependency.mgt.error");
        session.removeAttribute("dependency.mgt.error.epr");
    }
    String proxyServiceName = request.getParameter("serviceName");
%>

<div id="middle">
    <h2>
    <%
        if(!isAnonymous) {
            if(isTemplateAdd) {
                %><fmt:message key="manage.endpoints.template"/><%
            } else {
                %><fmt:message key="manage.endpoints"/><%
            }
        } else {
            if (proxyServiceName != null) {
                %><%=proxyServiceName%>:&nbsp;<%
            }
            %><fmt:message key="create.anon.endpoint"/><%
        }
    %>
    </h2>
    <div id="workArea" style="background-color:#F4F4F4;">
    <%
        if (!isAnonymous) { //hide tabs during anonymous mode
    %>
    <div id="tabs">
    <ul>
        <% if(!isTemplateAdd) { %>
            <li><a href="#tabs-1"><fmt:message key="defined.endpoints"/></a></li>
            <li><a href="#tabs-2"><fmt:message key="dynamic.endpoints"/></a></li>
        <%}%>
        <li><a href="#tabs-3"><fmt:message key="<%=isTemplateAdd ?"add.endpoint.template":"add.endpoint"%>"/></a></li>
    </ul>
    <% if(!isTemplateAdd) { %>
    <div id="tabs-1">
        <div id="noEpDiv" style="<%=ePMetaData!=null || isAnonymous?"display:none":""%>">
            <fmt:message
                    key="no.endpoints.in.synapse.config"></fmt:message>

        </div>
        <% if (ePMetaData != null && !isAnonymous) {%>
        <script type="text/javascript">
            isDefinedSequenceFound = true;
        </script>
        <p><fmt:message key="endpoints.synapse.text"/></p>
        <br/>
        <carbon:paginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                  page="index.jsp" pageNumberParameterName="pageNumber"
                  resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
                  prevKey="prev" nextKey="next"
                  parameters="<%=""%>" />
        <br/>
        <table class="styledLeft" cellpadding="1" id="endpointListTable">
            <thead>
            <tr>
                <th style="width:20%"><fmt:message key="endpoint.name"/></th>
                <th style="width:20%"><fmt:message key="type"/></th>
                <th colspan="4"><fmt:message key="action"/></th>
            </tr>
            </thead>
            <tbody>
            <%for (EndpointMetaData endpoint : ePMetaData) {%>
            <tr>
                <td><% if (endpoint.getDescription() != null) { %>
                    <span href="#"
                       onmouseover="showTooltip(this,'<%=endpoint.getDescription()%>')"><%= endpoint.getName()%>
                    </span>
                    <%
                    } else {
                    %>
                    <span href="#"><%= endpoint.getName()%></span>
                    <%
                        }
                    %>
                </td>
                <td>
                    <%
                        int type;
                        try {
                            type = endpoint.getType();
                        } catch (Exception e) {
                            type = 6;
                        }
                        if (type < 0 || type > 6) {
                            type = 6;
                        }
                    %>
                    <%=endpointTyepes[type]%>
                </td>
                <td style="border-right:none;border-left:none;width:100px">
                    <div class="inlineDiv">
                        <% if (endpoint.isSwitchOn()) { %>
                        <div id="switchOff<%=endpoint.getName()%>">
                            <a href="#" onclick="switchOff('<%= endpoint.getName() %>')"
                               class="icon-link"
                               style="background-image:url(images/endpoint-on.gif);"><fmt:message
                                    key="switch.off"/></a>
                        </div>
                        <div id="switchOn<%=endpoint.getName()%>" style="display:none;">
                            <a href="#" onclick="switchOn('<%= endpoint.getName() %>')"
                               class="icon-link"
                               style="background-image:url(images/endpoint-off.gif);"><fmt:message
                                    key="switch.on"/></a>
                        </div>
                        <%} else {%>
                        <div id="switchOff<%=endpoint.getName()%>" style="display:none;">
                            <a href="#" onclick="switchOff('<%= endpoint.getName() %>')"
                               class="icon-link"
                               style="background-image:url(images/endpoint-on.gif);"><fmt:message
                                    key="switch.off"/></a>
                        </div>
                        <div id="switchOn<%=endpoint.getName()%>" style="">
                            <a href="#" onclick="switchOn('<%= endpoint.getName() %>')"
                               class="icon-link"
                               style="background-image:url(images/endpoint-off.gif);"><fmt:message
                                    key="switch.on"/></a>
                        </div>
                        <% }

                            // display stat icon only for Address and WSDL endpoint
                    if (endpointTyepes[type].equals("Address Endpoint")
                            || endpointTyepes[type].equals("WSDL Endpoint")
                            || endpointTyepes[type].equals("Default Endpoint")
                            || endpointTyepes[type].equals("Template Endpoint")) {
                        // TODO-we can avoid back end service call if we try setting this in session
                        if (endpoint.isEnableStatistics()) { %>
                        <td style="border-right:none;border-left:none;width:100px">
                            <div id="disableStat<%= endpoint.getName()%>">
                                <a href="#" onclick="disableStat('<%= endpoint.getName() %>')"
                                   class="icon-link"
                                   style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                        key="disable.statistics"/></a>
                            </div>
                            <div id="enableStat<%= endpoint.getName()%>" style="display:none;">
                                <a href="#" onclick="enableStat('<%= endpoint.getName() %>')"
                                   class="icon-link"
                                   style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                        key="enable.statistics"/></a>
                            </div>
                        </td>
                    </div>
                </td>
                <%
                } else { %>
                <td style="border-right:none;border-left:none;width:100px">
                    <div class="inlineDiv">
                        <div id="enableStat<%= endpoint.getName()%>">
                            <a href="#" onclick="enableStat('<%= endpoint.getName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                    key="enable.statistics"/></a>
                        </div>
                        <div id="disableStat<%= endpoint.getName()%>" style="display:none">
                            <a href="#" onclick="disableStat('<%= endpoint.getName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                    key="disable.statistics"/></a>
                        </div>
                    </div>
                </td>

                <% }
                } else {%>
                <td style="border-right:none;border-left:none;width:100px"></td>
                <%
                    }
                %>
                <td style="border-left:none;border-right:none;width:100px">
                    <div class="inlineDiv">
                        <a href="#"
                           onclick="editEndpoint('<%= endpoint.getName() %>','<%=endpoint.getType()%>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/edit.gif);"><fmt:message
                                key="edit"/></a>
                    </div>
                </td>
                <td style="border-left:none;width:100px">
                    <div class="inlineDiv">
                        <a href="#" onclick="deleteEndpoint('<%= endpoint.getName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/delete.gif);"><fmt:message
                                key="delete"/></a>
                    </div>
                </td>
            </tr>
            <%}%>
            </tbody>
        </table>
        <br/>
        <carbon:paginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                          page="index.jsp" pageNumberParameterName="pageNumber"
                          resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"
                          parameters="<%=""%>" />
            <% } %>
        </div>
    <div id="tabs-2">
        <div id="noEpDiv"
             style="<%=dynamicEndpoints!=null || isAnonymous?"display:none":""%>">
            <fmt:message
                    key="no.endpoints.in.registry"></fmt:message>

        </div>

        <%

            if ((dynamicEndpoints != null) && (dynamicEndpoints.length > 0) && (!isAnonymous)) {
        %>
        <script type="text/javascript">
            isDynamicSequenceFound = true;
        </script>
        <p><fmt:message key="endpoints.dynamic.text"/></p>
        <br/>
        <carbon:paginator pageNumber="<%=dynamicPageNumber%>" numberOfPages="<%=numberOfDynamicPages%>"
                          page="index.jsp" pageNumberParameterName="dynamicPageNumber"
                          resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"
                          parameters="<%=""%>"/>
        <br/>
        <table class="styledLeft" cellspacing="1" id="dynamicEndpointsTable">
            <thead>
            <tr>
                <th style="width:30%">
                    <fmt:message key="endpoint.name"/>
                </th>
                <th style="width:20%">
                    <fmt:message key="type"/>
                </th>
                <th colspan="2">
                    <fmt:message key="action"/>
                </th>
            </tr>
            </thead>
            <tbody>
            <% for (String endpoint : dynamicEndpoints) { %>
            <tr>
                <td>
                    <%=endpoint %>
                </td>
                <td>
                     <%
                        int type;
                        try {
                            type = EndpointConfigurationHelper.getDynamicEndpointType(endpoint, client);
                        } catch (Exception e) {
                            type = 6;
                        }
                        if (type < 0 || type > 6) {
                            type = 6;
                        }
                    %>
                    <%=endpointTyepes[type]%>
                </td>
                <td style="border-right:none;width:100px">
                    <%if (type != 6) {%>
                    <div class="inlineDiv">
                        <a href="#" onclick="editRegistryEndpoint('<%=endpoint%>')" class="icon-link"
                           style="background-image:url(../admin/images/edit.gif);"><fmt:message
                                key="edit"/></a>
                    </div>
                    <%}%>
                </td>
                <td style="border-left:none;width:100px">
                    <div class="inlineDiv">
                        <a href="#" onclick="deleteRegistryEndpoint('<%= endpoint %>')" class="icon-link"
                           style="background-image:url(../admin/images/delete.gif);"><fmt:message
                                key="delete"/></a>
                    </div>
                </td>
            </tr>
            <%}%>
            </tbody>
        </table>
        <br/>
        <carbon:paginator pageNumber="<%=dynamicPageNumber%>" numberOfPages="<%=numberOfDynamicPages%>"
                          page="index.jsp" pageNumberParameterName="dynamicPageNumber"
                          resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"
                          parameters="<%=""%>"/>
        <%
            }
        %>
        <br/>
        </div>
    <%}%>
    <%} //hide tabs during anonymous mode%>
    <div id="tabs-3">
        <table id="endpointOptionTable" class="styledLeft" cellpadding="1">
            <thead>
                <tr>
                    <th colspan="2">
                        <fmt:message key="select.endpoint.type"/>
                    </th>
                </tr>
            </thead>
            <%
                String proxyServiceParam = proxyServiceName != null ?
                        "serviceName=" + proxyServiceName : "";
                String fullQueryString = (isTemplateAdd ? "templateAdd=true&" : "") +
                    proxyServiceParam;
            %>
            <tr>
                <td width="155px">
                    <a class="icon-link"
                       href="addressEndpoint.jsp?<%=fullQueryString%>"
                       style="background-image: url(../admin/images/add.gif);">
                        <fmt:message key="<%=isTemplateAdd ?"address.endpoint.template":"address.endpoint"%>"/>
                    </a>
                </td>
                <td>
                  <fmt:message key="address.message"/>
                </td>
            </tr>
            <tr>
                <td>
                    <a class="icon-link"
                       href="defaultEndpoint.jsp?<%=fullQueryString%>"
                       style="background-image: url(../admin/images/add.gif);">
                        <fmt:message key="<%=isTemplateAdd ?"default.endpoint.template":"default.endpoint"%>"/>
                    </a>
                </td>
                <td>
                  <fmt:message key="default.message"/>
                </td>
            </tr>
            <tr>
                <td>
                    <a class="icon-link"
                       href="WSDLEndpoint.jsp?<%=fullQueryString%>"
                       style="background-image: url(../admin/images/add.gif);">
                        <fmt:message key="<%=isTemplateAdd ?"wsdl.endpoint.template":"wsdl.endpoint"%>"/>
                    </a>
                </td>
                <td>
                    <fmt:message key="wsdl.message"/>
                </td>
            </tr>
            <%if (!isTemplateAdd) { %>
            <tr>
                <td>
                    <a class="icon-link"
                       href="failOverEndpoint.jsp?<%=proxyServiceParam%>"
                       style="background-image: url(../admin/images/add.gif);">
                        <fmt:message key="failover.endpoint"/>
                    </a>
                </td>
                <td>
                    <fmt:message key="failover.message"/>
                </td>
            </tr>
            <tr>
                <td>
                    <a class="icon-link"
                       href="loadBalanceEndpoint.jsp?<%=proxyServiceParam%>"
                       style="background-image: url(../admin/images/add.gif);">
                        <fmt:message key="load.balance.group"/>
                    </a>
                </td>
                <td>
                    <fmt:message key="loadbalance.message"/>
                </td>
            </tr>
            <tr>
                <td>
                    <a class="icon-link"
                       href="templateEndpoint.jsp?<%=proxyServiceParam%>"
                       style="background-image: url(../admin/images/add.gif);">
                        <fmt:message key="template.endpoint"/>
                    </a>
                </td>
                <td>
                    <fmt:message key="template.endpoint.message"/>
                </td>
            </tr>
            <% } %>
            <tr id="btnRow" style="<%=isAnonymous?"":"display:none"%>">
                <td colspan="2" class="buttonRow">
                    <input id="cancelBtn" type="button" value="<fmt:message key="back"/>"
                           class="button"
                           onclick="goBack('<%=anonymouseOriginator%>');return false"/>
                </td>
            </tr>
        </table>
    </div>
    <% if (!isAnonymous) { //hide tabs during anonymous mode%>
    </div>
    <%}%>
    </div>
</div>
<script type="text/javascript">
    alternateTableRows('endpointListTable', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('dynamicEndpointsTable', 'tableEvenRow', 'tableOddRow');
</script>

</fmt:bundle>