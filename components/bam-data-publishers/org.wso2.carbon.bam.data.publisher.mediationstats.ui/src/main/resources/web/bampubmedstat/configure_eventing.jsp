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
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.bam.data.publisher.mediationstats.ui.Utils" %>
<%@ page import="org.wso2.carbon.bam.data.publisher.mediationstats.ui.MediationStatPublisherAdminClient" %>
<%@ page import="org.wso2.carbon.bam.data.publisher.mediationstats.stub.config.MediationStatConfig"%>
<fmt:bundle basename="org.wso2.carbon.bam.data.publisher.mediationstats.ui.i18n.Resources">
    <carbon:breadcrumb
            label="system.statistics"
            resourceBundle="org.wso2.carbon.bam.data.publisher.mediationstats.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>

    <script type="text/javascript" src="js/statistics.js"></script>
    <script type="text/javascript" src="js/graphs.js"></script>
    <script type="text/javascript" src="../admin/js/jquery.flot.js"></script>
    <script type="text/javascript" src="../admin/js/excanvas.js"></script>

    <%
    int sequenceRequestThreshold = 0;
    if (request.getParameter("sequenceRequestThreshold") != null) {
        try {
            sequenceRequestThreshold = Integer.parseInt(request.getParameter("sequenceRequestThreshold"));
        } catch (NumberFormatException ignored) {
            // let sequenceRequestThreshold be 0, meaning it is not set
        }
    }

    int proxyRequestThreshold = 0;
    if (request.getParameter("proxyRequestThreshold") != null) {
            try {
                proxyRequestThreshold = Integer.parseInt(request.getParameter("proxyRequestThreshold"));
            } catch (NumberFormatException ignored) {
                // let proxyRequestThreshold be 0, meaning it is not set
            }
        }

    int endpointRequestThreshold = 0;
    if (request.getParameter("endpointRequestThreshold") != null) {
            try {
                endpointRequestThreshold = Integer.parseInt(request.getParameter("endpointRequestThreshold"));
            } catch (NumberFormatException ignored) {
                // let endpointRequestThreshold be 0, meaning it is not set
            }
        }

    String setConfig = request.getParameter("setConfig"); // hidden parameter to check if the form is being submitted
    String enableEventing = request.getParameter("enableEventing"); // String value is "on" of checkbox clicked, else null

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    MediationStatPublisherAdminClient client = new MediationStatPublisherAdminClient(cookie, backendServerURL,
                                                             configContext, request.getLocale());
    MediationStatConfig eventingConfigData = null;
    if (setConfig != null) { // form submitted requesing to set eventing config
        eventingConfigData = new MediationStatConfig();
        if (enableEventing != null) {
            eventingConfigData.setEnableEventing(Utils.EVENTING_ON);
            eventingConfigData.setSequenceRequestCountThreshold(sequenceRequestThreshold);
            eventingConfigData.setProxyRequestCountThreshold(proxyRequestThreshold);
            eventingConfigData.setEndpointRequestCountThreshold(endpointRequestThreshold);
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
            if(e.getCause().getMessage().toLowerCase().indexOf("you are not authorized") == -1){
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
            if(e.getCause().getMessage().toLowerCase().indexOf("you are not authorized") == -1){
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

        sequenceRequestThreshold = eventingConfigData.getSequenceRequestCountThreshold();
        proxyRequestThreshold = eventingConfigData.getProxyRequestCountThreshold();
        endpointRequestThreshold = eventingConfigData.getEndpointRequestCountThreshold();
    }
%>
    <script id="source" type="text/javascript">
        function showHideDiv(divId){
            var theDiv = document.getElementById(divId);
            if(theDiv.style.display=="none"){
                theDiv.style.display="";
            }else{
                theDiv.style.display="none";
            }
        }
    </script>

    <div id="middle">
        <h2>
            <fmt:message key="bam.statpublisher.config"/>
        </h2>

        <div id="workArea">
            <div id="result"></div>
            <p>&nbsp;</p>

            <form action="configure_eventing.jsp" method="post">
                <input type="hidden" name="setConfig" value="on" />
                <table width="100%" class="styledLeft" style="margin-left: 0px;">
                    <thead>
                    <tr>
                        <th colspan="4">
                            <fmt:message key="eventing.configuration"/>
                        </th>
                    </tr>
                    </thead>
                    <tr>
                        <td>
                            <% if (eventingOn) { %>
                            <input type="checkbox" name="enableEventing" onclick="showHideDiv('thresholdTr')" checked="true">&nbsp;&nbsp;&nbsp;&nbsp;
                            <% } else { %>
                            <input type="checkbox" name="enableEventing" onclick="showHideDiv('thresholdTr')">&nbsp;&nbsp;&nbsp;&nbsp;
                            <% }  %>
                            <fmt:message key="enable.eventing"/>

                        </td>
                    </tr>
                    <tr>
                        <td colspan="4">&nbsp;</td>
                    </tr>

                     <% if (eventingOn) { %>
                    <tr id="thresholdTr" style="display:block">
                    <% } else { %>
                    <tr id="thresholdTr" style="display:none">
                    <% }  %>

                        <td>
                            <table>
                                <tr>
                                    <td colspan="2">
                                        <strong><i>
                                            <fmt:message key="eventing.note.1"/><br/>
                                            <fmt:message key="eventing.note.2"/>
                                        </i></strong></td>
                                </tr>
                                <tr>
                                    <td width="20%">
                                        <fmt:message key="sequence.request.threshold"/>
                                    </td>
                                    <td width="30%">
                                        <input type="text" size="5" value="<%= sequenceRequestThreshold%>"
                                               name="sequenceRequestThreshold"
                                               maxlength="4"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td width="20%">
                                        <fmt:message key="proxy.request.threshold"/>
                                    </td>
                                    <td width="30%">
                                        <input type="text" size="5" value="<%= proxyRequestThreshold%>"
                                               name="proxyRequestThreshold"
                                               maxlength="4"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td width="20%">
                                        <fmt:message key="endpoint.request.threshold"/>
                                    </td>
                                    <td width="30%">
                                        <input type="text" size="5" value="<%= endpointRequestThreshold%>"
                                               name="endpointRequestThreshold"
                                               maxlength="4"/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>

                    <tr>
                        <td colspan="4" class="buttonRow">
                            <input type="submit" class="button" value="<fmt:message key="update"/>" id="updateStats"/>&nbsp;&nbsp;
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>
