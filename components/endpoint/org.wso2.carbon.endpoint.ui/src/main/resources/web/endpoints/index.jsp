<!--
~ Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@page contentType="text/html" pageEncoding="UTF-8"
        import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.endpoint.stub.types.service.EndpointMetaData" %>
<%@ page import="org.wso2.carbon.endpoint.ui.client.EndpointAdminClient" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.EndpointService" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.EndpointStore" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.Collection" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<link rel="stylesheet" type="text/css" href="../yui/build/container/assets/skins/sam/container.css">
<script type="text/javascript" src="../yui/build/container/container-min.js"></script>
<script type="text/javascript" src="../yui/build/element/element-min.js"></script>
<script type="text/javascript" src="../admin/js/widgets.js"></script>
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../resources/js/ressubmitEndpoint.jspource_util.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>
<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript"
        src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>

<fmt:bundle basename="org.wso2.carbon.endpoint.ui.i18n.Resources">
<carbon:breadcrumb
        label="endpoints"
        resourceBundle="org.wso2.carbon.newendpoint.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>


<script type="text/javascript">

// script for tab handling
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
    %>$tabs.tabs('option', 'selected', 0);
    <%
}else if(tabs!=null && tabs.equals("1")){
    %>$tabs.tabs('option', 'selected', 1);
    <%
    }
    %>
    if (!isDefinedSequenceFound && !isDynamicSequenceFound) {
        $tabs.tabs('option', 'selected', 2);
    }
});

function enableStat(endpointName) {
    $.ajax({
               type: 'POST',
               url: 'ajaxprocessors/stat-ajaxprocessor.jsp',
               data: 'endpointName=' + endpointName + '&action=enableStat',
               success: function(msg) {
                   var index = msg.toString().trim().indexOf('<div>');
                   if (index != -1 && msg.toString().trim().indexOf('<div>Error:') == index) {
                       CARBON.showErrorDialog(msg.toString().trim().substring(index + 17));
                   } else {
                       handleStatCallback('enableStat', endpointName);
                   }
               }
           });
}

function disableStat(endpointName) {
    $.ajax({
               type: 'POST',
               url: 'ajaxprocessors/stat-ajaxprocessor.jsp',
               data: 'endpointName=' + endpointName + '&action=disableStat',
               success: function(msg) {
                   var index = msg.toString().trim().indexOf('<div>');
                   if (index != -1 && msg.toString().trim().indexOf('<div>Error:') == index) {
                       CARBON.showErrorDialog(msg.toString().trim().substring(index + 17));
                   } else {
                       handleStatCallback('disableStat', endpointName);
                   }
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
               url: 'ajaxprocessors/switchOnOff-ajaxprocessor.jsp',
               data: 'endpointName=' + endpointName + '&action=switchOn',
               success: function(msg) {
                   var index = msg.toString().trim().indexOf('<div>');
                   if (index != -1 && msg.toString().trim().indexOf('<div>Error:') == index) {
                       CARBON.showErrorDialog(msg.toString().trim().substring(index + 17));
                   } else {
                       handleSwitchOnOffCallback('switchOn', endpointName);
                   }
               }
           });
}

function switchOff(endpointName) {
    $.ajax({
               type: 'POST',
               url: 'ajaxprocessors/switchOnOff-ajaxprocessor.jsp',
               data: 'endpointName=' + endpointName + '&action=switchOff',
               success: function(msg) {
                   var index = msg.toString().trim().indexOf('<div>');
                   if (index != -1 && msg.toString().trim().indexOf('<div>Error:') == index) {
                       CARBON.showErrorDialog(msg.toString().trim().substring(index + 17));
                   } else {
                       handleSwitchOnOffCallback('switchOff', endpointName);
                   }
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

function goBack(orginiator) {
    if (orginiator == null) {
        alert('Error: Origin not found');
        return false;
    }
    document.location.href = orginiator + '?cancelled=true';
    return true;
}

function deleteEndpoint(endpointName) {
    CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.the.endpoint"/> " + endpointName + " ?", function() {
        $.ajax({
                   type: 'POST',
                   url: 'ajaxprocessors/deleteEndpoint-ajaxprocessor.jsp',
                   data: 'endpointName=' + endpointName + '&force=false',
                   success: function(msg) {
                       var index = msg.toString().trim().indexOf('<div>Dep Error:</div>');
                       if (index != -1) {
                           confirmForceDelete(endpointName, msg.toString().trim().substring(index + 21));
                       } else {
                           loadEndpointsAfterDeletion();
                       }
                   }
               });
    });

}

function loadEndpointsAfterDeletion() {
    var url = "ajaxprocessors/deleteEndpoint-ajaxprocessor.jsp?loadpage=true";
    jQuery("#tabs-1").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (status != "success") {
            CARBON.showErrorDialog(jsi18n["endpoint.design.load.error"]);
        }
    });
}

function deleteDynamicEndpoint(key) {
    CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.the.endpoint"/> " + key + "?", function() {
        var url = "ajaxprocessors/deleteDynamicEndpoint-ajaxprocessor.jsp?endpointName=" + key;
        jQuery("#tabs-2").load(url, null, function (responseText, status, XMLHttpRequest) {
            if (status != "success") {
                CARBON.showErrorDialog(jsi18n["endpoint.design.load.error"]);
            }
        });
    });

}

function confirmForceDelete(endpointName, msg) {
    CARBON.showConfirmationDialog('This endpoint is a dependency for following items!<br/><br/>'
                                          + msg + '<br/>Force delete?', function() {
        $.ajax({
                   type: 'POST',
                   url: 'ajaxprocessors/deleteEndpoint-ajaxprocessor.jsp',
                   data: 'endpointName=' + endpointName + '&force=true',

                   success: function(msg) {
                       loadEndpointsAfterDeletion();
                   }
               });
    });
}

function editEndpoint(endpointType, endPointName) {
    document.location.href = endpointType + 'Endpoint.jsp?endpointName=' + endPointName + '&endpointAction=edit';
}

function editDynamicEndpoint(key) {
    if (key != null && key != undefined && key != "") {
        location.href = "dynamicEndpoint.jsp?anonEpAction=edit&key=" + key;
    } else {
        CARBON.showErrorDialog("Specify the key of the Endpoint to be edited");
    }
}

</script>

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    EndpointAdminClient client;
    EndpointMetaData[] ePMetaData = null;
    String[] dynamicEndpoints = null;
    try {
        client = new EndpointAdminClient(cookie, serverURL, configContext);

        ePMetaData = client.getEndpointMetaData();
        dynamicEndpoints = client.getDynamicEndpoints();

    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
        return;
    }

    //Template specific parameters
    String templateAdd = request.getParameter("templateAdd");
    boolean isTemplateAdd = templateAdd != null && "true".equals(templateAdd) ? true : false;

    // Anonymous Endpoint specific parameters
    String endpointMode = null; // this holds an anonymous endpoint which can come trough proxy and send mediator
    String anonymousOriginator = null;
    boolean isAnonymous = false;
    String anonymousEndpointXML = null;

    // came through clicking menu on the left
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
            anonymousOriginator = (String) session.getAttribute("anonOriginator");
        }
    }
    String proxyServiceName = request.getParameter("serviceName");
%>

<div id="middle">
<h2>
    <%
        if (!isAnonymous) {
            if (isTemplateAdd) {
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
    <% if (!isTemplateAdd) { %>
    <li><a href="#tabs-1"><fmt:message key="defined.endpoints"/></a></li>
    <li><a href="#tabs-2"><fmt:message key="dynamic.endpoints"/></a></li>
    <%}%>
    <li><a href="#tabs-3"><fmt:message
            key="<%=isTemplateAdd ?"add.endpoint.template":"add.endpoint"%>"/></a></li>
</ul>

<!--Tab 1: Endpoint List-->
<% if (!isTemplateAdd) { %>
<div id="tabs-1">
    <div id="noEpDiv" style="<%=ePMetaData!=null ?"display:none":""%>">
        <fmt:message
                key="no.endpoints.in.synapse.config"></fmt:message>
    </div>

    <% if (ePMetaData != null) {%>
    <script type="text/javascript">
        isDefinedSequenceFound = true;
    </script>
    <p><fmt:message key="endpoints.synapse.text"/></p>
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
                    <span href="#">
                          <%= endpoint.getName()%>
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
                    EndpointService ePService = client.getEndpointService(endpoint);
                %>
                <%=ePService.getDisplayName()%>
            </td>
            <td style="border-right:none;border-left:none;width:100px">
                <div class="inlineDiv">
                    <% if (endpoint.getSwitchOn()) { %>
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
                        if (ePService.isStatisticsAvailable()) {
                            if (endpoint.getEnableStatistics()) { %>
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
                       class="icon-link"
                       onclick="editEndpoint('<%=ePService.getUIPageName()%>','<%= endpoint.getName() %>')"
                       style="background-image:url(../admin/images/edit.gif);"><fmt:message
                            key="edit"/></a>
                </div>
            </td>
            <td style="border-left:none;width:100px">
                <div class="inlineDiv">
                    <a href="#"
                       onclick="deleteEndpoint('<%= endpoint.getName() %>')"
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
    <% } %>
</div>

<!--Tab 2: Dynamic Endpoints-->
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
                    String epXML = client.getDynamicEndpoint(endpoint);
                    EndpointService epService = client.getEndpointService(epXML);
                %>
                <%=epService.getDisplayName()%>
            </td>
            <td style="border-right:none;width:100px">
                <div class="inlineDiv">
                    <a href="#" onclick="editDynamicEndpoint('<%=endpoint%>')" class="icon-link"
                       style="background-image:url(../admin/images/edit.gif);"><fmt:message
                            key="edit"/></a>
                </div>
            </td>
            <td style="border-left:none;width:100px">
                <div class="inlineDiv">
                    <a href="#" onclick="deleteDynamicEndpoint('<%= endpoint %>')"
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
    <%
        }
    %>
    <br/>
</div>

<%}%>
<%} //hide tabs during anonymous mode%>

<!-- Tab 3: Add new Endpoints -->
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
            Collection<EndpointService> endpointServices = EndpointStore.getInstance().getRegisteredEndpoints();
            for (EndpointService endpointService : endpointServices) {
                if (isTemplateAdd && endpointService.canAddAsTemplate()) {
        %>
        <tr>
            <td width="155px">
                <a class="icon-link"
                   href="<%=endpointService.getUIPageName()%>Endpoint.jsp?<%=fullQueryString%>"
                   style="background-image: url(../admin/images/add.gif);">
                    <%=endpointService.getDisplayName()%> Template
                </a>
            </td>
            <td>
                <%=endpointService.getDescription()%>
            </td>
        </tr>
        <% } else if (!isTemplateAdd) { %>
        <tr>
            <td width="155px">
                <a class="icon-link"
                   href="<%=endpointService.getUIPageName()%>Endpoint.jsp?<%=fullQueryString%>"
                   style="background-image: url(../admin/images/add.gif);">
                    <%=endpointService.getDisplayName() %>
                </a>
            </td>
            <td>
                <%=endpointService.getDescription()%>
            </td>
        </tr>
        <% }
        }
        %>
        <tr id="btnRow" style="<%=isAnonymous?"":"display:none"%>">
            <td colspan="2" class="buttonRow">
                <input id="cancelBtn" type="button" value="<fmt:message key="back"/>"
                       class="button"
                       onclick="goBack('<%=anonymousOriginator%>');return false"/>
            </td>
        </tr>
    </table>
</div>
<% if (!isAnonymous) { //hide tabs during anonymous mode%>
</div>
<%}%>
</div>
</div>

</fmt:bundle>