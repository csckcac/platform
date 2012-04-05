<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.ui.BAMUIConstants" %>
<%@ page import="org.wso2.carbon.bam.ui.client.BAMConfigAdminServiceClient" %>
<%@ page import="org.wso2.carbon.bam.util.BAMConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.net.MalformedURLException" %>
<%@ page import="java.net.Socket" %>
<%@ page import="java.net.URL" %>
<%@ page import="org.wso2.carbon.bam.stub.configadmin.types.carbon.ServerDO" %>
<%@ page import="org.wso2.carbon.bam.stub.configadmin.types.carbon.MonitoredServerDTO" %>
<%@ page import="org.wso2.carbon.bam.util.BAMUtil" %>
<%@ page import="org.wso2.carbon.bam.ui.BAMConfiguration" %>


<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>
<fmt:bundle basename="org.wso2.carbon.bam.ui.i18n.Resources">
<carbon:breadcrumb label="server.list"
                   resourceBundle="org.wso2.carbon.bam.ui.i18n.Resources"
                   topPage="false"
                   request="<%=request%>"/>

<%
    String topic = "/carbon/bam/data/publishers/service_stats";

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    BAMConfigAdminServiceClient client;
    try {
        client = new BAMConfigAdminServiceClient(cookie, serverURL, configContext);
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp?<%=e.getMessage()%>"/>
<%
        return;
    }
%>
<%
    try {
        String deactivateServerID = request.getParameter("deactivateServer");
        if (deactivateServerID != null) {

            String serverUrl = request.getParameter("serverURL");
            boolean isSeverUp = BAMUtil.isServerUpAndRunning(serverUrl);

            String serverType = request.getParameter("serverType");
            if (BAMConstants.SERVER_TYPE_EVENTING.equals(serverType) && isSeverUp) {
                int statCategory = Integer.parseInt(request.getParameter("statCategory"));
                String brokerURL = serverUrl + BAMUIConstants.SERVICES_PATH + BAMConfiguration.getEventBrokerName();

                client.unsubscribe(brokerURL, request.getParameter("subscriptionID"), serverType, request.getParameter("serverURL"));
                client.deactivateServer(Integer.parseInt(deactivateServerID));

            } else if (BAMConstants.SERVER_TYPE_PULL.equals(serverType) && isSeverUp) {
                client.deactivateServer(Integer.parseInt(deactivateServerID));
            } else if (!isSeverUp) {

                String warningMsg = "Server: " + serverUrl + " is not running or refusing connections.";
%>
<script type="text/javascript">
    jQuery(document).init(function() {
        CARBON.showWarningDialog('<%=warningMsg%>');
    });
</script>
<%
        }
    }
} catch (Exception e) {
        String errorMsg = "Error while deactivating the server." + e.getLocalizedMessage();
%>
<script type="text/javascript">
    jQuery(document).init(function() {
        CARBON.showErrorDialog('<%=errorMsg%>');
    });
</script>
<%
    }

    try {

        String activateServerID = request.getParameter("activateServer");

        if (activateServerID != null) {

            String serverType = request.getParameter("serverType");
            String subID = null;

            if (BAMConstants.SERVER_TYPE_EVENTING.equals(serverType)) {
                String severUrl = request.getParameter("serverURL");
                int statCategory = Integer.parseInt(request.getParameter("statCategory"));
                String brokerURL = severUrl + BAMUIConstants.SERVICES_PATH + BAMConfiguration.getEventBrokerName();
                String subscriberServiceURL = null;

                if (statCategory == BAMConstants.SERVICE_STAT_TYPE) {
                    topic = "/carbon/bam/data/publishers/service_stats";
                    subscriberServiceURL = serverURL + BAMUIConstants.BAM_SERVICE_STATISTICS_SUBSCRIBER_SERVICE;
                } else if (statCategory == BAMConstants.MESSAGE_STAT_TYPE) {
                    topic = "/carbon/bam/data/publishers/activity";
                    subscriberServiceURL = serverURL + BAMUIConstants.BAM_SERVICE_ACTIVITY_STATISTICS_SUBSCRIBER_SERVICE;
                } else if (statCategory == BAMConstants.MEDIATION_STAT_TYPE) {
                    topic = "/carbon/bam/data/publishers/mediation_stats";
                    subscriberServiceURL = serverURL + BAMUIConstants.BAM_USER_DEFINED_DATA_SUBSCRIBER_SERVICE;
                }

                ServerDO[] serverList = client.getServerList();
                for (ServerDO server : serverList) {
                    if (server.getId() == Integer.parseInt(activateServerID)) {
                        subID = client.subscribe(topic, brokerURL, subscriberServiceURL, severUrl,
                                server.getUserName(), server.getPassword());
                    }
                }
            }
            client.activateServer(Integer.parseInt(activateServerID), subID);
        }

    } catch (Exception e) {
        String errorMsg = "Error while activating the server." + e.getLocalizedMessage();
%>
<script type="text/javascript">
    jQuery(document).init(function() {
        CARBON.showErrorDialog('<%=errorMsg%>');
    });
</script>
<%
    }
    String deleteServerID = request.getParameter("delete");
    if (deleteServerID != null) {
        boolean deleteFlag = false;
        MonitoredServerDTO monitoredServerDTO = new MonitoredServerDTO();
        monitoredServerDTO.setServerId(Integer.parseInt(deleteServerID));
        try {
            client.removeServer(monitoredServerDTO);
            ServerDO[] serverList = client.getServerList();
            if (serverList != null) {
                for (ServerDO server : serverList) {
                    if (server.getId() == monitoredServerDTO.getServerId()) {
                        deleteFlag = false;
                        break;
                    }
                    deleteFlag = true;
                }
            }else{
                deleteFlag=true;
            }
            if (deleteFlag) {
%>
<script type="text/javascript">
    jQuery(document).init(function() {
        function handleOK() {
            window.location = 'list_servers.jsp?region=region1&item=monitored_server_list_menu';
        }

        CARBON.showInfoDialog("<fmt:message key="server.successfully.deleted"/>", handleOK);
    });
</script>
<%
} else {
    String errorMsg = "Error while removing the server. There are monitored data in the current database, recorded against the server.";
%>
<script type="text/javascript">
    jQuery(document).init(function() {
        CARBON.showErrorDialog('<%=errorMsg%>');
    });
</script>
<%
    }
} catch (Exception e) {
    String errorMsg = "Error while removing the server." + e.getLocalizedMessage();
%>
<script type="text/javascript">
    jQuery(document).init(function() {
        CARBON.showErrorDialog('<%=errorMsg%>');
    });
</script>
<%
        }
    }

    String verifyServerID = request.getParameter("verify");

    if (verifyServerID != null) {
        try {
            ServerDO[] serverList = client.getServerList();
            for (ServerDO server : serverList) {
                if (server.getId() == Integer.parseInt(verifyServerID)) {
                    String urlStr = server.getServerURL();

                    URL url = null;
                    try {
                        url = new URL(urlStr);
                    } catch (MalformedURLException e) {
                        String errorMsg = "Server URL is not valid..";
%>
<script type="text/javascript">
    jQuery(document).init(function() {
        CARBON.showErrorDialog('<%=errorMsg%>');
    });
</script>
<% break;
}

    if (url != null) {
        int port = (url.getPort() == -1) ? url.getDefaultPort() : url.getPort();

        try {
            Socket s = new Socket(url.getHost(), port);

            String infoMsg = "Server is up and running..";

%>
<script type="text/javascript">
    jQuery(document).init(function() {
        CARBON.showInfoDialog('<%=infoMsg%>');
    });
</script>
<% break;
} catch (IOException e) {
    String infoMsg = "Server is not running or refusing connections..";
%>
<script type="text/javascript">
    jQuery(document).init(function() {
        CARBON.showWarningDialog('<%=infoMsg%>');
    });
</script>
<%
                    break;
                }
            }
        }
    }
} catch (Exception e) {

    String errorMsg = "Error while fetching server data. " + e.getLocalizedMessage();

%>
<script type="text/javascript">
    jQuery(document).init(function() {
        CARBON.showErrorDialog('<%=errorMsg%>');
    });
</script>
<%
        }
    }

    ServerDO[] serverList = client.getServerList();
%>

<script type="text/javascript">

    function deleteServer(serverID) {
        CARBON.showConfirmationDialog("<fmt:message key="remove.server.from.monitoring"/>",
                function() {
                    location.href = "../bam/list_servers.jsp?delete=" + serverID;
                }, null);
    }

    function editServer(serverID) {
        location.href = "../bam/edit_server.jsp?edit='true'&serverId=" + serverID;
    }

    function verifyServer(serverID) {
        CARBON.showPopupDialog('<div style="padding:20px;font-size:14px;color:#666666"><img src="../bam/images/ajax-loader.gif" alt=" " align="left" style="margin-right:10px" /> Attempting to connect to server...</div>', "Testing Connection", 150, null, null, 350);
        location.href = "../bam/list_servers.jsp?verify=" + serverID;
    }

    function deactivateServer(serverID, serverURL, serverType, statCategory, subscriptionID) {
        location.href = "../bam/list_servers.jsp?deactivateServer=" + serverID + "&serverURL=" + serverURL +
                "&serverType=" + serverType + "&statCategory=" + statCategory + "&subscriptionID=" + subscriptionID;
    }

    function activateServer(serverID, serverURL, serverType, statCategory) {
        location.href = "../bam/list_servers.jsp?activateServer=" + serverID + "&serverURL=" + serverURL + "&serverType=" +
                serverType + "&statCategory=" + statCategory;
    }

</script>

<div id="middle">
    <h2><fmt:message key="list.monitored.servers"/></h2>

    <div id="workArea">
        <div style="height: 30px;">
            <a href="../bam/add_server.jsp" class="icon-link"
               style="background-image: url(../admin/images/add.gif);">
                <fmt:message key="add.monitored.server"/> </a>
        </div>
        <%
            if (serverList != null) {
        %>
        <table style="width: 100%" class="styledLeft">
            <thead>
            <tr>
                <th><fmt:message key="server"/></th>
                <th><fmt:message key="server.type"/></th>
                <th><fmt:message key="statistics.type"/></th>
                <th><fmt:message key="activate.deactivate"/></th>
                <th><fmt:message key="server.description"/></th>
                <th><fmt:message key="actions"/></th>
            </tr>
            </thead>
            <tbody>
                    <%
				for (ServerDO server : serverList) {
					boolean userServer = true;
			%>
            <tr>
                <td><%=server.getServerURL()%>
                </td>
                <td>
                    <%
                        if (BAMConstants.SERVER_TYPE_EVENTING.equals(server.getServerType())) {
                    %> <fmt:message key="server.type.eventing"/>
                    <%
                    } else if (BAMConstants.SERVER_TYPE_PULL.equals(server.getServerType())) {
                    %> <fmt:message key="server.type.polling"/>
                    <%
                    } else if (BAMConstants.SERVER_TYPE_JMX.equals(server.getServerType())) {
                    %> <fmt:message key="server.type.jmx"/>
                    <%
                    } else {
                    %> <fmt:message key="server.type.generic"/>
                    <%
                        }
                    %>
                </td>
                <td>
                    <%
                        int statType = server.getCategory();
                        if (statType == 1) {
                    %> <fmt:message key="statistics.type.service"/> <br/>
                    <%
                    } else if (statType == 2) {
                    %> <fmt:message key="statistics.type.message"/> <br/>
                    <%
                    } else if (statType == 4) {
                    %> <fmt:message key="statistics.type.mediation"/> <br/>
                    <%
                    } else if (statType == 8) {
                    %> <fmt:message key="statistics.type.any"/> <br/>
                    <%
                    } else {
                        userServer = false;
                    %> <fmt:message key="server.type.generic"/> <br/>
                    <%
                        }
                    %>
                </td>
                <td>
                    <%
                        if (userServer) { // check if this is a user added server
                            boolean state = false;
                            if (server.getActive()) {
                                state = true;
                            }
                            if (state) {
                    %> <a title="<fmt:message key="deactivate"/>"
                          onclick="deactivateServer('<%=server.getId()%>', '<%=server.getServerURL()%>',
					 '<%=server.getServerType()%>', '<%=server.getCategory()%>', '<%=server.getSubscriptionID()%>');
					  return false;" href='#' class='icon-link'
                          style='background-image: url(../admin/images/static-icon.gif);'>
                    <fmt:message key="deactivate"/></a> <%
                } else {
                %> <a title="<fmt:message key="activate"/>"
                      onclick="activateServer('<%=server.getId()%>', '<%=server.getServerURL()%>', '<%=server.getServerType()%>',
					 '<%=server.getCategory()%>'); return false;"
                      href='#' class='icon-link'
                      style='background-image: url(../admin/images/static-icon-disabled.gif);'><fmt:message
                        key="activate"/></a> <%
                        }
                    }
                %>
                </td>
                <td>
                    <%
                        String description = server.getDescription();
                        if (description != null) {
                    %> <%=description%> <%
                    }
                %>
                </td>
                <td class="buttonRow" colspan="2">
                    <%
                        if (userServer) { // check if this is a user added server
                    %>
                    <input name="editServer"
                           type="button" class="button" value="<fmt:message key="edit"/>"
                           onclick="editServer('<%=server.getId()%>'); return false;"/>
                    <input name="verifyServer"
                           type="button" class="button" value="<fmt:message key="verify"/>"
                           onclick="verifyServer('<%=server.getId()%>'); return false;"/>
                    <input name="deleteserver"
                           type="button" class="button" value="<fmt:message key="delete"/>"
                           onclick="deleteServer('<%=server.getId()%>'); return false;"/>
                    <%
                        }
                    %>
                </td>

            </tr>

                    <%
				}
			%>

        </table>
        <%
        } else {
        %> <fmt:message key="no.servers.configured"/> <%
        }
    %>
    </div>
</div>
</fmt:bundle>
