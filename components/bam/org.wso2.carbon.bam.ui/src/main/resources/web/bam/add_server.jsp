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
<%@ page import="org.wso2.carbon.bam.ui.client.BAMConfigAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
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
    String topic = "/carbon/bam/data/publishers/service_stats";
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);

    if (request.getParameter(ACTION_SUBMIT) != null) {

        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().
                        getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        ServerDO server = new ServerDO();

        String monitoredServerURL = request.getParameter(SERVER_URL);
        if (monitoredServerURL != null) {
            monitoredServerURL = monitoredServerURL.trim();
            if (monitoredServerURL.length() > 1 &&
                monitoredServerURL.charAt(monitoredServerURL.length() - 1) == '/') { // remove trailing '/'
                    monitoredServerURL = monitoredServerURL.substring(0, monitoredServerURL.length() - 1);
            }
        }
        server.setServerURL(monitoredServerURL);
        server.setUserName(request.getParameter(USERNAME));
        server.setPassword(request.getParameter(PASSWORD));
        String serverType = request.getParameter("ServerType");
        server.setServerType(serverType);
        server.setDescription(request.getParameter("descriptionText"));

        String statisticsType = request.getParameter("StatisticsType");
        int statType;
        if ("Service".equals(statisticsType)) {
            statType = BAMConstants.SERVICE_STAT_TYPE;
            //subscriberServiceEPR = serverURL + BAMUIConstants.BAM_SERVICE_STATISTICS_SUBSCRIBER_SERVICE;

            topic = "/carbon/bam/data/publishers/service_stats";
        } else if ("Message".equals(statisticsType)) {
            statType = BAMConstants.MESSAGE_STAT_TYPE;
            //subscriberServiceEPR = serverURL + BAMUIConstants.BAM_SERVICE_ACTIVITY_STATISTICS_SUBSCRIBER_SERVICE;

            topic = "/carbon/bam/data/publishers/activity";
        } else if ("Mediation".equals(statisticsType)) {
            statType = BAMConstants.MEDIATION_STAT_TYPE;
            //subscriberServiceEPR = serverURL + BAMUIConstants.BAM_USER_DEFINED_DATA_SUBSCRIBER_SERVICE;

            topic = "/carbon/bam/data/publishers/mediation_stats";
        } else {
            statType = BAMConstants.GENERIC_STAT_TYPE;
        }

        if (serverType.equals(BAMConstants.SERVER_TYPE_GENERIC)) {
            statType = BAMConstants.GENERIC_STAT_TYPE;
            topic = "/carbon/bam/data/publishers/generic";
        }

        server.setCategory(statType);

/*        if (serverType.equals(BAMUIConstants.SERVER_TYPE_PULL)) {
            server.setPollingInterval(Long.parseLong(request.getParameter(POLLING_INTERVAL)));
        }*/

        String subscriberServiceEPR = request.getParameter("epr");
        if (subscriberServiceEPR != null) {
            subscriberServiceEPR=subscriberServiceEPR.trim();
            server.setSubscriptionEPR(subscriberServiceEPR);
        }

        String brokerURL = monitoredServerURL + BAMUIConstants.SERVICES_PATH + BAMConfiguration.getEventBrokerName();
        try {

            if (serverType.equals(BAMConstants.SERVER_TYPE_EVENTING)) {
                BAMConfigAdminServiceClient client =
                        new BAMConfigAdminServiceClient(cookie, serverURL, configContext);

                String subID = client.subscribe(topic, brokerURL, subscriberServiceEPR,
                            request.getParameter(SERVER_URL), server.getUserName(), server.getPassword());
                
                server.setSubscriptionID(subID);
                client.cleanup();
            }

            try {
                BAMConfigAdminServiceClient client;
                client = new BAMConfigAdminServiceClient(cookie, serverURL, configContext);
                int statesOfAddedServer=client.addServer(server);
                if(statesOfAddedServer== BAMConstants.SERVER_SUCCESSFULLY_ADDED){
%>

<script type="text/javascript">
    jQuery(document).init(function() {
        function handleOK() {
            window.location = 'list_servers.jsp?region=region1&item=monitored_server_list_menu';
        }
        CARBON.showInfoDialog("<fmt:message key="server.successfully.added"/>", handleOK);
    });
</script>
<%
                }else if(statesOfAddedServer==BAMConstants.SERVER_ALREADY_EXIST){
                %>
                <script type="text/javascript">
                    CARBON.showWarningDialog('<fmt:message key="server.already.exist"/>');
                </script>
                <%
                }else if(statesOfAddedServer==BAMConstants.SERVER_NOT_RUNNING){
                %>
                <script type="text/javascript">
                    CARBON.showWarningDialog('<fmt:message key="server.not.running"/>');
                </script>
                <%
                }else if(statesOfAddedServer==BAMConstants.SERVER_AUTH_FAILED){
                %>
                <script type="text/javascript">
                    CARBON.showWarningDialog('<fmt:message key="server.auth.fail"/>');
                </script>
                <%
                }else if(statesOfAddedServer==BAMConstants.SERVER_URL_MALFORMED){
                %>
                <script type="text/javascript">
                    CARBON.showWarningDialog('<fmt:message key="server.url.is.wrong"/>');
                </script>
                <%
                }else if(statesOfAddedServer==BAMConstants.SERVER_AUTH_FAILED_404){
                %>
                <script type="text/javascript">
                    CARBON.showWarningDialog('<fmt:message key="server.auth.fail.404"/>');
                </script>
                <%
                }
                client.cleanup();
            }
            catch (Exception e) {
                response.setStatus(500);
                CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp?<%=e.getMessage()%>"/>
<%
        return;
    }
    //CarbonUIMessage.sendCarbonUIMessage("Server successfully added", CarbonUIMessage.INFO, request);
} catch (Exception e) {
    String errorString = "Error adding server.";
    if (serverType.equals(BAMConstants.SERVER_TYPE_EVENTING)) {
        errorString += " Failed to subscribe to " + brokerURL;
    }
%>
<script type="text/javascript">
    jQuery(document).init(function() {
        CARBON.showErrorDialog('<%=errorString%>');
    });
</script>
<%
        }
    }
%>

<script type="text/javascript">

    function validate() {
        value = document.getElementsByName("<%=SERVER_URL%>")[0].value;
        if (value == '') {
            CARBON.showWarningDialog("<fmt:message key="server.url.is.required"/>");
            return false;
        } else {
            var regex = new RegExp("(http|https)://.*");
            if ((document.getElementById('pullServerInput').checked || document.getElementById('eventingServerInput').checked) && (!regex.test(value))) {
                CARBON.showWarningDialog("<fmt:message key="server.url.is.wrong"/>");
                return false;
            }
        }
        var localHostRegEx = new RegExp("(http|https)://localhost.*");
        if (localHostRegEx.test(value)) {
            CARBON.showWarningDialog("<fmt:message key="localhost.not.allowed"/>");
            return false;
        }
        value = document.getElementsByName("<%=USERNAME%>")[0].value;
        if (value == '' && document.getElementById('pullServerInput').checked) {
            CARBON.showWarningDialog("<fmt:message key="username.is.required"/>");
            return false;
        }
        value = document.getElementsByName("<%=PASSWORD%>")[0].value;
        if (value == '' && document.getElementById('pullServerInput').checked) {
            CARBON.showWarningDialog("<fmt:message key="password.is.required"/>");
            return false;
        }
        value = document.getElementsByName("<%=POLLING_INTERVAL%>")[0].value;
        if ((value == '' || !(/^[0-9]+$/.test(value))) && document.getElementById('pullServerInput').checked) {
            CARBON.showWarningDialog("<fmt:message key="invalid.polling.interval"/>");
            return false;
        }
        value = document.getElementsByName("<%=EPR%>")[0].value;
        if (document.getElementById('eventingServerInput').checked && value == '') {
            CARBON.showWarningDialog("<fmt:message key="subscription.url.is.required"/>");
            return false;
        } else {
            var regex = new RegExp("(http|https)://.*");
            if (document.getElementById('eventingServerInput').checked && (!regex.test(value))) {
                CARBON.showWarningDialog("<fmt:message key="subscription.url.is.wrong"/>");
                return false;
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
        setDefaultEPR();
    }
    function displayGenericServerOptions() {
        document.getElementById('statTypeTR').style.display = 'none';
        document.getElementById('usernameTR').style.display = 'none';
        document.getElementById('passwordTR').style.display = 'none';
        document.getElementById('pollingIntervalTR').style.display = 'none';
    }

    function setDefaultEPR() {
        var defaultURL = '<%=serverURL%>';

        if(document.getElementById('service').checked) {
            defaultURL = defaultURL + '<%= BAMUIConstants.BAM_SERVICE_STATISTICS_SUBSCRIBER_SERVICE %>';
        } else if (document.getElementById('mediation').checked) {
            defaultURL = defaultURL + '<%= BAMUIConstants.BAM_USER_DEFINED_DATA_SUBSCRIBER_SERVICE %>';
        } else if(document.getElementById('message').checked) {
            defaultURL = defaultURL + '<%= BAMUIConstants.BAM_SERVICE_ACTIVITY_STATISTICS_SUBSCRIBER_SERVICE %>';
        }

        document.getElementById('epr').value = defaultURL;

    }

</script>
<div id="middle">
    <h2><fmt:message key="add.monitored.server"/></h2>

    <div id="workArea">
        <form method="post" name="addJMXServerForm" action="../bam/add_server.jsp"
              target="_self">
            <table style="width: 100%" class="styledLeft">
                <thead>
                <tr><th colspan="2"><fmt:message key="add.server.to.monitor"/></th> </tr>
                </thead>
                <tbody>
                <tr>
                    <td class="formRow">
                        <table class="normal" cellspacing="0" id="eventingTbl">
                            <tr>
                                <td class="leftCol-small"><fmt:message key="server.url"/><span
                                        class="required">*</span></td>
                                <td><input class="text-box-big" id="serverURL" name="serverURL" type="text"
                                        title="[protocol]://[hostname/ip]:[port]"></td>
                                <td>e.g: [protocol] : // [hostname/ip] : [port]</td>
                            </tr>
                            <tr>
                                <td class="leftCol-small"><fmt:message key="server.type"/><span class="required">*</span></td>
                                <td><input id="pullServerInput" type="radio"
                                           name="ServerType" value="PullServer" checked onclick="displayPullServerOptions();">
                                    <fmt:message key="server.type.polling"/>
                                    <input id="eventingServerInput" type="radio" name="ServerType" value="EventingServer"
                                                                           onclick="displayPushServerOptions();">
                                    <fmt:message key="server.type.eventing"/>
                                    <input type="radio" name="ServerType" value="GenericServer"
                                                                                onclick="displayGenericServerOptions();">
                                    <fmt:message key="server.type.generic"/> <br>
                                </td>
                            </tr>

                            <tr id="statTypeTR">
                                <td class="leftCol-small"><fmt:message key="statistics.type"/><span class="required">*</span></td>
                                <td><input id="service" type="radio" name="StatisticsType" value="Service" onclick="setDefaultEPR();" checked>
                                    <fmt:message key="statistics.type.service"/>
                                    <span id="statTypeSpan" style="display: none;">
                                        <input id="mediation" type="radio" name="StatisticsType" value="Mediation" onclick="setDefaultEPR();">
                                        <fmt:message  key="statistics.type.mediation"/>
                                        <input id="message" type="radio" name="StatisticsType" value="Message" onclick="setDefaultEPR();">
                                        <fmt:message key="statistics.type.message"/> </span> <br/>
                                </td>
                            </tr>

                            <tr id="subscriptions" style="display:none">
                                <td class="leftCol-small"><fmt:message key="subscription.epr"/><span class="required">*</span>
                                </td>
<%--                                <td style="padding-top: 20px !important;">Default Subscription EPR - BAM Server<br>
                                    <a onclick="addRow();" class="icon-link" style="background-image: url(../admin/images/add.gif);">
                                        <fmt:message key="add.monitored.server.subscription"/> </a>
                                </td>--%>
                                <td><input class="text-box-big" id="epr" name="epr" title="Default EPR set to this server EPR." type="text"></td>
                            </tr>
                            <tr id="usernameTR">
                                <td class="leftCol-small"><fmt:message key="user.name"/><span class="required">*</span></td>
                                <td><input class="text-box-big" id="username"  name="username" type="text"></td>
                            </tr>
                            <tr id="passwordTR">
                                <td class="leftCol-small"><fmt:message key="password"/><span class="required">*</span></td>
                                <td><input class="text-box-big" id="password" name="password" type="password"></td>
                            </tr>
                            <tr id="pollingIntervalTR" style="display: none">
                                <td class="leftCol-small"><fmt:message key="polling.interval"/>
                                <td><input class="text-box-big" id="pollingInterval" name="pollingInterval" type="text" value="60"></td>
                            </tr>
                            <tr id="pollingDelayTR" style="display: none">
                                <td class="leftCol-small"><fmt:message key="polling.delay"/>
                                <td><input class="text-box-big" id="pollingDelay" name="pollingDelay" type="text" value="60"></td>
                            </tr>
                            <tr>
                                <td class="leftCol-small"><fmt:message key="server.description"/></td>
                                <td><textarea id="descriptionText" name="descriptionText"  style="width: 250px"></textarea></td>
                            </tr>
                            <input type="hidden" id="submitAction"
                                   name="submitAction" type="text" value="submitAction">
                        </table>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow" colspan="2"><input name="adduser" type="button" class="button"  value="<fmt:message key="add"/>"
                                                             onclick="validate();"/>
                        <input type="button" class="button" onclick="javascript:location.href='list_servers.jsp?region=region1&item=monitored_server_list_menu'"
                            value="<fmt:message key="cancel"/>"/>
                    </td>
                </tr>
                </tbody>
            </table>

        </form>
    </div>
</div>
</fmt:bundle>
