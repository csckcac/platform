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
<%@ page import="org.wso2.carbon.bam.data.publisher.activity.mediation.ui.Utils" %>
<%@ page import="org.wso2.carbon.bam.data.publisher.activity.mediation.stub.config.EventingConfigData" %>
<%@ page import="org.wso2.carbon.bam.data.publisher.activity.mediation.ui.ActivityPublisherAdminClient" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>

<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<fmt:bundle
        basename="org.wso2.carbon.bam.data.publisher.activity.mediation.ui.i18n.Resources">
<carbon:breadcrumb label="activity.statistics"
                   resourceBundle="org.wso2.carbon.bam.data.publisher.activity.mediation.ui.i18n.Resources"
                   topPage="true" request="<%=request%>"/>

<%
    int messageThreshold = 0;
    if (request.getParameter("messageThreshold") != null) {
        try {
            messageThreshold = Integer.parseInt(request.getParameter("messageThreshold"));
        } catch (NumberFormatException ignored) {
            // let systemRequestThreshold be 0, meaning it is not set
        }
    }

    String setConfig = request.getParameter("setConfig"); // hidden parameter to check if the form is being submitted
    String enableEventing = request.getParameter("enableEventing"); // String value is "on" of checkbox clicked, else null
    String msgLookup = request.getParameter("msgLookup");
    String msgDumping = request.getParameter("msgDumping");

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ActivityPublisherAdminClient client = new ActivityPublisherAdminClient(cookie, backendServerURL,
            configContext, request.getLocale());
    EventingConfigData eventingConfigData = null;

    if (setConfig != null) { // form submitted requesing to set eventing config
        eventingConfigData = new EventingConfigData();
        if (enableEventing != null) {
            eventingConfigData.setEnableEventing(Utils.EVENTING_ON);
            eventingConfigData.setMessageThreshold(messageThreshold);
            eventingConfigData.setEnableMessageLookup(msgLookup);
            //eventingConfigData.setXPathExpressions(expList.toArray(new String[]{}));
            eventingConfigData.setEnableMessageDumping(msgDumping);
        } else {
            eventingConfigData.setEnableEventing("OFF");
        }

        try {
            client.setEventingConfigData(eventingConfigData);

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
} else { // this is the initial loading of the page, hence load current values from backend
    try {
        eventingConfigData = client.getEventingConfigData();
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
    boolean eventingOn = Utils.EVENTING_ON.equals(eventingConfigData.getEnableEventing());
    if (eventingOn) {
        messageThreshold = eventingConfigData.getMessageThreshold();
        msgDumping = eventingConfigData.getEnableMessageDumping();
        msgLookup = eventingConfigData.getEnableMessageLookup();
    }
%>
<script id="source" type="text/javascript">
    function showHideDiv(divId) {
        var theDiv = document.getElementById(divId);
        if (theDiv.style.display == "none") {
            theDiv.style.display = "";
        } else {
            theDiv.style.display = "none";
        }
    }
</script>

<%--<script type="text/javascript">


    function submitForm() {
        document.configForm.submit();
    }

    function msgFlushFunc() {
        var msgFulsh = document.getElementById("messageFlush");
        msgFulsh.value = "ON";
        document.configForm.submit();
    }


</script>--%>
<script type="text/javascript" src="../yui/build/yahoo/dom-min.js"></script>
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript"
        src="../yui/build/connection/connection-min.js"></script>
<div id="middle">
    <h2><fmt:message key="bam.statpublisher.config"/></h2>

    <div id="workArea">
        <div id="result"></div>
        <p>&nbsp;</p>

        <form action="configure_eventing.jsp" method="post" name="configForm">
            <input type="hidden" name="setConfig" value="on"/>
            <table width="100%" class="styledLeft noBorders"
                   style="margin-left: 0px;">
                <thead>
                <tr>
                    <th colspan="4"><fmt:message key="eventing.configuration"/></th>
                </tr>
                </thead>
                <tr>
                    <td>
                        <%
                            if (eventingOn) {
                        %> <input type="checkbox" name="enableEventing"
                                  onclick="showHideDiv('thresholdTr')" checked="true">&nbsp;&nbsp;&nbsp;&nbsp;
                        <%
                        } else {
                        %> <input type="checkbox" name="enableEventing"
                                  onclick="showHideDiv('thresholdTr')">&nbsp;&nbsp;&nbsp;&nbsp;
                        <%
                            }
                        %> <fmt:message key="enable.eventing"/></td>

<%--                    <td width="20%"><fmt:message key="message.flush"/></td>
                    <td colspan="4" class="buttonRow"><input type="button"
                                                             class="button" onclick="msgFlushFunc()"
                                                             value="<fmt:message key="flush"/>" id="updateFlush"/></td>--%>
                </tr>
                <tr>
                    <td colspan="4">&nbsp;</td>
                </tr>

                <%
                    if (eventingOn) {
                %>
                <tr id="thresholdTr" style="display: block">
                            <%
                                } else {
                            %>

                <tr id="thresholdTr" style="display: none">
                    <%
                        }
                    %>

                    <td>
                        <table id="eventingTbl" class="noBorders" style="border: medium none">
                            <tr>
                                <td colspan="2"><strong><i> <fmt:message
                                        key="eventing.note.1"/><br/>
                                    <fmt:message key="eventing.note.2"/> </i></strong></td>
                            </tr>
                            <tr>
                                <td width="20%"><fmt:message key="message.threshold"/></td>
                                <td width="30%"><input type="text" size="5"
                                                       value="<%=messageThreshold%>" name="messageThreshold"
                                                       maxlength="4"/></td>
                            </tr>

                            <tr>
                                <td width="20%"><fmt:message key="message.dumping"/></td>
                                <td><input type="radio" name="msgDumping" value="ON"
                                        <%if (!(msgDumping == null) && msgDumping.equals("ON")) {%>
                                           checked="" <%}%>><fmt:message key="message.dumpingTypeON"/>
                                    <input type="radio" name="msgDumping" value="OFF"
                                            <%if (msgDumping == null || !msgDumping.equals("ON")) {%>
                                           checked="" <%}%>><fmt:message key="message.dumpingTypeOFF"/>
                                </td>
                            </tr>

                            <tr>
                                <td width="20%"><fmt:message key="message.lookup"/></td>
                                <td>
                                    <input type="radio"
                                           name="msgLookup"
                                           value="ON"
                                            <%
                                                if (!(msgLookup == null) && msgLookup.equals("ON")) {
                                            %>
                                           checked="" <%}%> />
                                    <fmt:message key="message.lookupON"/>

                                    <input type="radio"
                                           name="msgLookup"
                                           value="OFF" <% if (msgLookup == null || !msgLookup.equals("ON")) {%>
                                           checked="" <%}%> />
                                    <fmt:message key="message.lookupOFF"/>
                                </td>
                            </tr>


                        </table>

                    </td>
                </tr>

                <tr>
                    <td colspan="4" class="buttonRow"><input type="submit"
                                                             class="button"
                                                             value="<fmt:message key="update"/>"
                                                             id="updateStats"/>&nbsp;&nbsp;
                    </td>
                </tr>
            </table>
        </form>
    </div>
</div>
</fmt:bundle>