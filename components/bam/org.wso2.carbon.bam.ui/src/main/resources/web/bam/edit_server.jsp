<!--
~ Copyright 2009 WSO2, Inc. (http://wso2.com)
~
~ Licensed under the Apache License, Version 2.0 (the "License");
~ you may not use this file except in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing, software
~ distributed under the License is distributed on an "AS IS" BASIS,
~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~ See the License for the specific language governing permissions and
~ limitations under the License.
-->

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.ui.BAMUIConstants" %>
<%@ page import="static org.wso2.carbon.bam.ui.BAMUIConstants.*" %>
<%@ page import="static org.wso2.carbon.bam.ui.BAMUIConstants.ACTION_SUBMIT" %>
<%@ page import="org.wso2.carbon.bam.ui.client.BAMConfigAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.bam.util.BAMConstants" %>
<%@ page import="org.wso2.carbon.bam.stub.configadmin.types.carbon.ServerDO" %>
<%@ page import="org.wso2.carbon.bam.ui.BAMConfiguration" %>

<!--link media="all" type="text/css" rel="stylesheet" href="css/registration.css"/-->

<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>
<script type="text/javascript" src="js/config_server_epr.js"></script>

<fmt:bundle basename="org.wso2.carbon.bam.ui.i18n.Resources">
<carbon:breadcrumb label="add.server"
                   resourceBundle="org.wso2.carbon.bam.ui.i18n.Resources"
                   topPage="false" request="<%=request%>"/>
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.bam.ui.i18n.JSResources"
        request="<%=request%>"/>

<%
    final int NULL_SEVER = -1;

    String topic = "/carbon/bam/data/publishers/service_stats";
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);

    ConfigurationContext configContext =
            (ConfigurationContext) config.
                    getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

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

    ServerDO server = null; // UI will be populated with details of this server at initial loading.

    if (request.getParameter(ACTION_SUBMIT) != null) {    //This is the form submission


        server = new ServerDO();
        String monitoredServerURL = request.getParameter(SERVER_URL);
        if (monitoredServerURL != null) {
            monitoredServerURL = monitoredServerURL.trim();
            if (monitoredServerURL.length() > 1 &&
                monitoredServerURL.charAt(monitoredServerURL.length() - 1) == '/') { // remove trailing '/'
                monitoredServerURL = monitoredServerURL.substring(0, monitoredServerURL.length() - 1);
            }
        }
        server.setServerURL(monitoredServerURL);
/*        server.setUserName(request.getParameter(USERNAME));
        server.setPassword(request.getParameter(PASSWORD));
        String serverType = request.getParameter("ServerType");
        server.setServerType(serverType);*/
        String description = request.getParameter("descriptionText");
        if (description != null) {
            server.setDescription(description.trim());
        }

        String serverID = request.getParameter("serverID");
        server.setId(Integer.parseInt(serverID)); // This must be present in order for update to succeed.

        String active = request.getParameter("active");
        if (active != null && Boolean.parseBoolean(active)) {
            server.setActive(BAMConstants.SERVER_ACTIVE_STATE);
        } else {
            server.setActive(BAMConstants.SERVER_INACTIVE_STATE);
        }

        String serverType = request.getParameter("serverType");
        server.setServerType(serverType);

        String currentEPR = request.getParameter("epr");

        String priorEPR = request.getParameter("priorEpr");
        String subscriptionID = request.getParameter("subscriptionID");

        String userName = request.getParameter("userName");
        server.setUserName(userName);

        String password = request.getParameter("password");
        server.setPassword(password);

        String brokerURL = monitoredServerURL + BAMUIConstants.SERVICES_PATH + BAMConfiguration.getEventBrokerName();

        try {

            if (BAMConstants.SERVER_TYPE_EVENTING.equals(serverType) ||
                BAMConstants.SERVER_TYPE_GENERIC.equals(serverType)) {

                if (active != null) {
                    // First unsubscribe earlier subscriber before subscribing the new one.
                    // Skip this if the EPR has not changed.
                    if (currentEPR != null && (!currentEPR.equals(priorEPR))) {
                        client.unsubscribe(brokerURL, subscriptionID, serverType,
                                           request.getParameter(SERVER_URL));
                        String subID = client.subscribe(topic, brokerURL, currentEPR,
                                                        request.getParameter(SERVER_URL),
                                                        userName, password);
                        server.setSubscriptionID(subID);
                        server.setSubscriptionEPR(currentEPR);
                    } else { // Just set the previous values since the subscription wasn't changed
                        server.setSubscriptionEPR(currentEPR);
                        server.setSubscriptionID(subscriptionID);
                    }
                } else {
                    // We allow user to change the EPR even when the server is inactive so that next
                    // time when server is activated it will get subscribed to new EPR. But first
                    // we have to remove the existing subscription if it exists.
                    if (currentEPR != null && (!currentEPR.equals(priorEPR))) {
                        client.unsubscribe(brokerURL, subscriptionID, serverType,
                                           request.getParameter(SERVER_URL));
                        server.setSubscriptionEPR(currentEPR);
                    } else { // Just set the previous values since the subscription wasn't changed
                        server.setSubscriptionEPR(currentEPR);
                        server.setSubscriptionID(null);
                    }

                }
            }

            try {
                client.updateServer(server);
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

<script type="text/javascript">
    jQuery(document).init(function() {
        function handleOK() {
            window.location = 'list_servers.jsp?region=region1&item=monitored_server_list_menu';
        }

        CARBON.showInfoDialog("<fmt:message key="server.successfully.updated"/>", handleOK);
    });
</script>
<%
    //CarbonUIMessage.sendCarbonUIMessage("Server successfully added", CarbonUIMessage.INFO, request);
} catch (Exception e) {
    String errorString = "Error editing server details.";
    if (BAMConstants.SERVER_TYPE_EVENTING.equals(serverType)) {
        errorString += " Failed to subscribe to " + brokerURL;
    }
    server.setSubscriptionEPR(priorEPR); // Sets to currently subscribed epr which in this case is the earlier epr
%>
<script type="text/javascript">
    jQuery(document).init(function() {
        CARBON.showErrorDialog('<%=errorString%>');
    });
</script>
<%
    }

} else if (request.getParameter("edit") != null) { //This is the initial loading of the page. Get server details and populate the UI.
    int serverId = NULL_SEVER;
    try {
        serverId = Integer.parseInt(request.getParameter("serverId"));
    } catch (Exception e) {
        String errorString = "Server id field should be numeric. Current server id: "
                             + request.getParameter("serverId") + ". Error in database update??";

%>
<script type="text/javascript">
    jQuery(document).init(function() {
        function handleOK() {
            window.location = '../bam/list_servers.jsp?region=region1&item=monitored_server_list_menu';
        }

        CARBON.showErrorDialog('<%=errorString%>', handleOK);
    });
</script>

<%
    }

    if (serverId != NULL_SEVER) {
        try {
            server = client.getServer(serverId);
        } catch (Exception e) {
            String errorString = "Error while retrieving data for server id: " + serverId;

%>
<script type="text/javascript">
    jQuery(document).init(function() {
        function handleOK() {
            window.location = '../bam/list_servers.jsp?region=region1&item=monitored_server_list_menu';
        }

        CARBON.showErrorDialog('<%=errorString%>', handleOK);
    });
</script>

<%
        }
    }

    if (server == null) {
        String errorString = "Server is not present in the database.";
%>
<script type="text/javascript">
    jQuery(document).init(function() {
        function handleOK() {
            window.location = '../bam/list_servers.jsp?region=region1&item=monitored_server_list_menu';
        }

        CARBON.showErrorDialog('<%=errorString%>', handleOK);
    });
</script>

<%
        }

    }
%>

<script type="text/javascript">

    function validate() {
        var value = document.getElementsByName("<%=SERVER_URL%>")[0].value;
        if (value == '') {
            CARBON.showWarningDialog("<fmt:message key="server.url.is.required"/>");
            return false;
        } else {
            var regex = new RegExp("(http|https)://.*");
            if ((!regex.test(value))) {
                CARBON.showWarningDialog("<fmt:message key="server.url.is.wrong"/>");
                return false;
            }
        }
        var localHostRegEx = new RegExp("(http|https)://localhost.*");
        if (localHostRegEx.test(value)) {
            CARBON.showWarningDialog("<fmt:message key="localhost.not.allowed"/>");
            return false;
        }
        if (document.getElementById('epr') != null) {
            value = document.getElementById('epr').value;
            if (value == '') {
                CARBON.showWarningDialog("<fmt:message key="subscription.url.is.required"/>");
                return false;
            } else {
                regex = new RegExp("(http|https)://.*");
                if ((!regex.test(value))) {
                    CARBON.showWarningDialog("<fmt:message key="subscription.url.is.wrong"/>");
                    return false;
                }
            }
        }
        document.addJMXServerForm.submit();

        return true;
    }

    function displayPullServerOptions() {
        document.getElementById('statTypeTR').style.display = '';
        document.getElementById('statTypeSpan').style.display = 'none';
        document.getElementById('usernameTR').style.display = '';
        document.getElementById('passwordTR').style.display = '';
        document.getElementById('pollingIntervalTR').style.display = 'none';
        document.getElementById('pollingDelayTR').style.display = 'none';
        document.getElementById('subscriptions').style.display = 'none';
    }
    function displayPushServerOptions() {
        document.getElementById('statTypeTR').style.display = '';
        document.getElementById('statTypeSpan').style.display = '';
        document.getElementById('usernameTR').style.display = '';
        document.getElementById('passwordTR').style.display = '';
        document.getElementById('subscriptions').style.display = '';
    }
    function displayGenericServerOptions() {
        document.getElementById('statTypeTR').style.display = 'none';
        document.getElementById('usernameTR').style.display = 'none';
        document.getElementById('passwordTR').style.display = 'none';
        document.getElementById('pollingIntervalTR').style.display = 'none';
    }
</script>
<div id="middle">
    <h2><fmt:message key="edit.monitored.server"/></h2>

    <div id="workArea">
        <form method="post" name="addJMXServerForm" action="../bam/edit_server.jsp"
              target="_self">
            <%
                boolean isActive = server.getActive();
                if (isActive) {
            %>
            <input id="active" type="hidden" name="active"
                   value="<%=isActive%>"/>
            <%
                }

                if (BAMConstants.SERVER_TYPE_EVENTING.equals(server.getServerType()) ||
                    BAMConstants.SERVER_TYPE_GENERIC.equals(server.getServerType())) {
            %>
            <input id="priorEpr" type="hidden" name="priorEpr"
                   value="<%= (server.getSubscriptionEPR() != null) ? server.getSubscriptionEPR() : "" %>"/>
            <input id="subscriptionID" type="hidden" name="subscriptionID"
                   value="<%= (server.getSubscriptionID() != null) ? server.getSubscriptionID() : "" %>"/>
            <%
                }
            %>

<%--            <input id="userName" type="hidden" name="userName"
                   value="<%= (server.getUserName() != null) ? server.getUserName() : "" %>"/>
            <input id="password" type="hidden" name="password"
                   value="<%= (server.getPassword() != null) ? server.getPassword() : "" %>"/>--%>
            <input id="serverID" type="hidden" name="serverID"
                   value="<%= server.getId() %>"/>
            <input id="serverType" type="hidden" name="serverType"
                   value="<%= (server.getServerType() != null) ? server.getServerType() : "" %>"/>

            <table style="width: 100%" class="styledLeft">
                <thead>
                <tr>
                    <th colspan="2"><fmt:message key="edit.monitored.server"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td class="formRow">
                        <table class="normal" cellspacing="0" id="eventingTbl">
                            <tr>
                                <td class="leftCol-small">
                                    <fmt:message key="server.url"/>
                                    <span class="required">*</span>
                                </td>
                                <td><input class="text-box-big" id="<%=SERVER_URL%>"
                                           name="<%=SERVER_URL%>" type="text"
                                           value="<%= server.getServerURL()%>"/>
                                </td>
                            </tr>
                            <%
                                if (BAMConstants.SERVER_TYPE_EVENTING.equals(server.getServerType()) ||
                                    BAMConstants.SERVER_TYPE_GENERIC.equals(server.getServerType())) {
                            %>
                            <tr id="subscriptions">
                                <td class="leftCol-small">
                                    <fmt:message key="subscription.epr"/>
                                    <span class="required">*</span>
                                </td>
                                <td>
                                    <input class="text-box-big" id="epr" name="epr"
                                           title="Default EPR set to this server EPR."
                                           type="text" value="<%= server.getSubscriptionEPR()%>"/>
                                </td>
                            </tr>
                            <%
                                }
                            %>
                            <tr>
                                <td class="leftCol-small">
                                    <fmt:message key="user.name"/>
                                </td>
                                <td>
                                    <input id="userName" type="text" name="userName" 
                                           value="<%= (server.getUserName() != null) ? server.getUserName() : "" %>"/>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-small">
                                    <fmt:message key="password"/>
                                </td>
                                <td>
                                    <input id="password" name="password" type="password"
                                           value="<%= (server.getPassword() != null) ? server.getPassword() : "" %>"/>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-small">
                                    <fmt:message key="server.description"/>
                                </td>
                                <td>
                                    <textarea id="descriptionText" name="descriptionText"
                                              style="width: 250px">
                                        <%= (server.getDescription() != null) ? server.getDescription().trim() : "" %>
                                    </textarea>
                                </td>
                            </tr>
                            <input type="hidden" id="<%=ACTION_SUBMIT%>"
                                   name="<%=ACTION_SUBMIT%>" type="text" value="<%=ACTION_SUBMIT%>">
                        </table>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow" colspan="2"><input name="edit" type="button"
                                                             class="button"
                                                             value="<fmt:message key="edit"/>"
                                                             onclick="validate();"/>
                        <input type="button" class="button"
                               onclick="javascript:location.href='list_servers.jsp?region=region1&item=monitored_server_list_menu'"
                               value="<fmt:message key="cancel"/>"/>
                    </td>
                </tr>
                </tbody>
            </table>

        </form>
    </div>
</div>
</fmt:bundle>
