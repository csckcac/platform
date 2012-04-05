<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.transport.mgt.stub.types.carbon.TransportSummary" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@page import="org.wso2.carbon.transport.mgt.ui.TransportAdminClient"%>
<%@page import="org.wso2.carbon.transport.mgt.stub.types.carbon.TransportData"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<script type="text/javascript" src="global-params.js"></script>

<%
    String backendServerURL;
    ConfigurationContext configContext;
    String cookie;
    TransportAdminClient client;
    TransportData[] transportData;

    backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    configContext = (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    client = new TransportAdminClient(cookie, backendServerURL,configContext);

    try {
        transportData = client.getAllTransportData();
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.transport.mgt.ui.i18n.Resources">
    <carbon:breadcrumb
            label="transport.mgmt.breadcrumb"
            resourceBundle="org.wso2.carbon.transport.mgt.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>" />

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>

    <div id="middle">
        <h2 id="listTransport"><fmt:message key="transport.mgmt"/></h2>
        <div id="workArea">
            <script type="text/javascript">
                function diableTransport(page,protocol, sender) {
                    if(protocol=="http" || protocol=="https")
                    {
                        CARBON.showInfoDialog(protocol +' <fmt:message key="cannot.disable.transport"/>');
                    }
                    else if (!sender) {
                        CARBON.showConfirmationDialog("<fmt:message key="confirm.remove.transport"/> " + protocol + " <fmt:message key="confirm.remove.transport.listener"/>",
                                function() {
                                    location.href = page;
                                },
                                null);
                    } else {
                        CARBON.showConfirmationDialog("<fmt:message key="confirm.remove.transport"/> " + protocol + " <fmt:message key="confirm.remove.transport.sender"/>",
                                function() {
                                    location.href = page;
                                },
                                null);
                    }
                }

            </script>
            <p><%=transportData.length%> transport implementation(s) available</p>
            <p>&nbsp;</p>
            <table class="styledLeft" id="availableTransports">
                <thead>
                    <tr style="top: 0;">
                        <th width="20%">Transport Implementation</th>
                        <th width="15%">Receiver/Sender</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
<%
    if (transportData != null && transportData.length > 0) {
        TransportSummary summary;
        for (TransportData data : transportData) {
            summary = data.getSummary();
            String uiContext = summary.getProtocol() + "-config";
            boolean useTrpSpecificUI = CarbonUIUtil.isContextRegistered(config, "/" + uiContext + "/");
%>
                <tr>
                    <td rowspan="2"><%=summary.getProtocol().toUpperCase()%></td>
                    <td><fmt:message key="transport.listener"/></td>
                    <td>
                        <%
                            if (summary.getListenerActive()) {
                                if (useTrpSpecificUI) {
                        %>
                        <a href="../<%=uiContext%>/listener_config.jsp" class="icon-link" style="background-image:url(images/configure.gif);"><fmt:message key="transport.configure"/></a>
                        <%
                            if (!"http".equals(summary.getProtocol()) && !"https".equals(summary.getProtocol())) {
                        %>
                        <a href="javascript:diableTransport('../<%=uiContext%>/listener_disable.jsp','<%=summary.getProtocol()%>', false)" class="icon-link" style="background-image:url(images/disable.gif);"><fmt:message key="transport.disable"/></a>
                        <%
                            }
                        } else {
                        %>
                        <a href="./listener_config.jsp?transport=<%=summary.getProtocol()%>" class="icon-link" style="background-image:url(images/configure.gif);"><fmt:message key="transport.configure"/></a>
                        <%
                            if (!"http".equals(summary.getProtocol()) && !"https".equals(summary.getProtocol())) {
                        %>
                        <a href="javascript:diableTransport('./listener_disable.jsp?transport=<%=summary.getProtocol()%>','<%=summary.getProtocol()%>', false)" class="icon-link" style="background-image:url(images/disable.gif);"><fmt:message key="transport.disable"/></a>
                        <%
                                }
                            }
                        } else {
                            if (useTrpSpecificUI) {
                        %>
                        <a href="../<%=uiContext%>/listener_enable.jsp" class="icon-link" style="background-image:url(images/enable.gif);"><fmt:message key="transport.enable"/></a>
                        <%
                        } else {
                        %>
                        <a href="./listener_enable.jsp?transport=<%=summary.getProtocol()%>" class="icon-link" style="background-image:url(images/enable.gif);"><fmt:message key="transport.enable"/></a>
                        <%
                                }
                            }
                        %>

                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="transport.sender"/></td>
                    <td>                        
                        <%
                            if (summary.getSenderActive()) {
                                if (useTrpSpecificUI) {
                        %>
                        <a href="../<%=uiContext%>/sender_config.jsp" class="icon-link" style="background-image:url(images/configure.gif);"><fmt:message key="transport.configure"/></a>
                        <%
                            if (!"http".equals(summary.getProtocol()) && !"https".equals(summary.getProtocol())) {
                        %>
                        <a href="javascript:diableTransport('../<%=uiContext%>/sender_disable.jsp','<%=summary.getProtocol()%>', true)" class="icon-link" style="background-image:url(images/disable.gif);"><fmt:message key="transport.disable"/></a>
                        <%
                            }
                        } else {
                        %>
                        <a href="./sender_config.jsp?transport=<%=summary.getProtocol()%>" class="icon-link" style="background-image:url(images/configure.gif);"><fmt:message key="transport.configure"/></a>
                        <%
                            if (!"http".equals(summary.getProtocol()) && !"https".equals(summary.getProtocol())) {
                        %>
                        <a href="javascript:diableTransport('./sender_disable.jsp?transport=<%=summary.getProtocol()%>','<%=summary.getProtocol()%>', true)" class="icon-link" style="background-image:url(images/disable.gif);"><fmt:message key="transport.disable"/></a>
                        <%
                                }
                            }
                        } else {
                            if (useTrpSpecificUI) {
                        %>
                        <a href="../<%=uiContext%>/sender_enable.jsp" class="icon-link" style="background-image:url(images/enable.gif);"><fmt:message key="transport.enable"/></a>
                        <%
                        } else {
                        %>
                        <a href="./sender_enable.jsp?transport=<%=summary.getProtocol()%>" class="icon-link" style="background-image:url(images/enable.gif);"><fmt:message key="transport.enable"/></a>
                        <%
                                }
                            }
                        %>

                    </td>
                </tr>
<%
        }
    }
%>

            </table>
        </div>
    </div>
</fmt:bundle>
