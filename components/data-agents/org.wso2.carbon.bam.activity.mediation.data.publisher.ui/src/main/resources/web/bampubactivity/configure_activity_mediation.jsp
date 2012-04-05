<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page
        import="org.wso2.carbon.bam.activity.mediation.data.publisher.ui.ActivityPublisherAdminClient" %>
<%@ page
        import="org.wso2.carbon.bam.activity.mediation.data.publisher.stub.conf.ActivityConfigData" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<fmt:bundle basename="org.wso2.carbon.bam.activity.mediation.data.publisher.ui.i18n.Resources">

<carbon:breadcrumb
        label="system.statistics"
        resourceBundle="org.wso2.carbon.bam.activity.mediation.data.publisher.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>
<%
    String setConfig = request.getParameter("setConfig"); // hidden parameter to check if the form is being submitted
    String enableActivity = request.getParameter("enableActivity"); // String value is "on" of checkbox clicked, else null
    String url = request.getParameter("url");
    String userName = request.getParameter("user_name");
    String password = request.getParameter("password");
    String transport = request.getParameter("transport");
    String port = null;
    if (transport != null && transport.equals("socketTransport")) {
        port = request.getParameter("port");
    }

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ActivityPublisherAdminClient client = new ActivityPublisherAdminClient(
            cookie, backendServerURL, configContext, request.getLocale());
    ActivityConfigData activityConfigData = null;

    if (setConfig != null) {    // form submitted request to set eventing config
        activityConfigData = new ActivityConfigData();
        if (enableActivity != null) {
            activityConfigData.setMessageDumpingEnable(true);
        } else {
            activityConfigData.setMessageDumpingEnable(false);
        }
        if (url != null) {
            activityConfigData.setUrl(url);
        }
        if (userName != null) {
            activityConfigData.setUserName(userName);
        }
        if (password != null) {
            activityConfigData.setPassword(password);
        }

        if (transport != null && transport.equals("httpTransport")) {
            activityConfigData.setHttpTransportEnable(true);
        } else {
            activityConfigData.setHttpTransportEnable(false);
        }

        if (transport != null && transport.equals("socketTransport")) {
            activityConfigData.setSocketTransportEnable(true);
            if (port != null) {
                activityConfigData.setPort(Integer.parseInt(port));
            }
        } else {
            activityConfigData.setSocketTransportEnable(false);
        }


        try {
            client.setEventingConfigData(activityConfigData);

%>
<script type="text/javascript">
    jQuery(document).init(function() {
        function handleOK() {

        }

        CARBON.showInfoDialog("Eventing Configuration Successfully Updated!", handleOK);
    });
</script>
<%
} catch (Exception e) {
    if (e.getCause().getMessage().toLowerCase().indexOf("you are not authorized") == -1) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        }
    }
} else {
    try {
        activityConfigData = client.getEventingConfigData();
    } catch (Exception e) {
        if (e.getCause().getMessage().toLowerCase().indexOf("you are not authorized") == -1) {
            response.setStatus(500);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
            }
        }
    }

    boolean isMessageDumpingEnable = activityConfigData.getMessageDumpingEnable();
    boolean isHttpTransportEnable = activityConfigData.getHttpTransportEnable();
    boolean isSocketTransportEnable = activityConfigData.getSocketTransportEnable();

    if (url == null) {
        url = activityConfigData.getUrl();
    }
    if (userName == null) {
        userName = activityConfigData.getUserName();
    }
    if (password == null) {
        password = activityConfigData.getPassword();
    }
    if (port == null) {
        port = String.valueOf(activityConfigData.getPort());
    }
%>

<script id="source" type="text/javascript">

    function showHideSocketPort(id) {
        if (id == "httpTransportId") {
            if ($("#" + id).attr("checked")) {
                $("#socketPortIdDiv").hide();
            } else {
                $("#socketPortIdDiv").show();
            }
        }
        else {
            if ($("#" + id).attr("checked")) {
                $("#socketPortIdDiv").show();
            } else {
                $("#socketPortIdDiv").hide();
            }
        }

    }

    function hideDiv(divId) {
        $("#" + divId).hide();
    }
</script>

<div id="middle">
    <h2>
        <fmt:message key="bam.activity.mediation.config"/>
    </h2>

    <div id="workArea">
        <div id="result"></div>
        <p>&nbsp;</p>

        <form action="configure_activity_mediation.jsp" method="post">
            <input type="hidden" name="setConfig" value="on"/>
            <table width="100%" class="styledLeft" style="margin-left: 0px;">
                <thead>
                <tr>
                    <th colspan="4">
                        <fmt:message key="activity.mediation.configuration"/>
                    </th>
                </tr>
                </thead>
                <tr>
                    <td>
                        <% if (isMessageDumpingEnable) { %>
                        <input type="checkbox" name="enableActivity"
                               checked="true">&nbsp;&nbsp;&nbsp;&nbsp;
                        <% } else { %>
                        <input type="checkbox" name="enableActivity">&nbsp;&nbsp;&nbsp;&nbsp;
                        <% } %>
                        <fmt:message key="enable.activity.mediation"/>

                    </td>
                </tr>

                    <%--                    <% if (isServiceStatsEnable || isMsgDumpingEnable) { %>--%>
                <thead>
                <tr>
                    <th colspan="4">
                        <fmt:message key="bam.credential"/>
                    </th>
                </tr>
                </thead>
                <tr>
                    <td><fmt:message key="bam.url"/></td>
                    <td><input type="text" name="url" value="<%=url%>"/></td>
                </tr>
                <tr>
                    <td><fmt:message key="username"/></td>
                    <td><input type="text" name="user_name" value="<%=userName%>"/></td>
                </tr>
                <tr>
                    <td><fmt:message key="password"/></td>
                    <td><input type="password" name="password" value="<%=password%>"/></td>
                </tr>
                <thead>
                <tr>
                    <th colspan="4">
                        <fmt:message key="publisher.transport"/>
                    </th>
                </tr>
                </thead>
                <tr>
                    <td>
                        <% if (isHttpTransportEnable) { %>
                        <input type="radio" id="httpTransportId" name="transport"
                               onclick="showHideSocketPort('httpTransportId')" checked="true"
                               value="httpTransport"/>
                        <% } else { %>
                        <input type="radio" id="httpTransportId" name="transport"
                               onclick="showHideSocketPort('httpTransportId')"
                               value="httpTransport"/>
                        <% } %>
                        <fmt:message key="http.transport"/>
                    </td>
                    <td></td>
                </tr>
                <tr>
                    <td>
                        <%if (isSocketTransportEnable) { %>
                        <input type="radio" id="socketTransportId" name="transport"
                               onclick="showHideSocketPort('socketTransportId')" checked="true"
                               value="socketTransport"/>
                        <fmt:message key="socket.transport"/>
                    </td>
                    <td id="socketPortId">
                        <div id="socketPortIdDiv"><fmt:message key="port"/>
                            <input type="text" name="port" value="<%=port%>"/>
                        </div>
                    </td>
                    <%} else { %>
                    <input type="radio" name="transport" id="socketTransportId"
                           onclick="showHideSocketPort('socketTransportId');"
                           value="socketTransport"/>
                    <fmt:message key="socket.transport"/>
                    </td>
                    <td id="socketPortId">
                        <div id="socketPortIdDiv" style="display: none">
                            <fmt:message key="port"/>
                            <input type="text" name="port" value="<%=port%>"/>
                        </div>
                    </td>
                    <%}%>
                </tr>

                    <%--                    <% } %>--%>


                <tr>
                    <td colspan="4" class="buttonRow">
                        <input type="submit" class="button" value="<fmt:message key="update"/>"
                               id="updateStats"/>&nbsp;&nbsp;
                    </td>
                </tr>
            </table>
        </form>
    </div>
</div>


</fmt:bundle>

