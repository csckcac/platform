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
<%@ page import="org.wso2.carbon.mediation.statistics.ui.client.MediationStatisticsClient" %>
<%@ page import="org.wso2.carbon.mediation.statistics.stub.GraphData" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<script type="text/javascript" src="js/statistics.js"></script>
<script type="text/javascript" src="js/graphs.js"></script>
<script language="javascript" src="js/mochikit/MochiKit.js"></script>
<script language="javascript" src="js/plotkit/Base.js"></script>
<script language="javascript" src="js/plotkit/Layout.js"></script>
<script language="javascript" src="js/plotkit/Canvas.js"></script>
<script language="javascript" src="js/plotkit/SweetCanvas.js"></script>
<script language="javascript" src="js/plotkit/excanvas.js"></script>
<fmt:bundle basename="org.wso2.carbon.mediation.statistics.ui.i18n.Resources">
    <carbon:breadcrumb label="Mediation Statistics"
                       resourceBundle="org.wso2.carbon.mediation.statistics.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
    <%
        // Set standard HTTP/1.1 no-cache headers.
        response.setHeader("Cache-Control", "no-store, max-age=0, no-cache, must-revalidate");
        // Set IE extended HTTP/1.1 no-cache headers.
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        response.setHeader("Pragma", "no-cache");

        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        GraphData graphData = null;
        try {

            MediationStatisticsClient client = new MediationStatisticsClient(configContext, serverURL, cookie);
            graphData = client.getDataForGraph();
            if (graphData != null) {
    %>
    <script type="text/javascript">
        var serverStr = '<%=graphData.getServerData()%>';
        var psStr = '<%=graphData.getProxyServiceData()%>';
        var epStr = '<%=graphData.getEndPointData()%>';
        var seqStr = '<%=graphData.getSequenceData()%>';
        populateAllGraphs(serverStr, psStr, epStr, seqStr);
        MochiKit.DOM.addLoadEvent(drawGraphs);
    </script>

    <div id="middle">
        <h2><fmt:message key="mediation.statistics"/></h2>

        <div id="workArea">
            <div id="output">
                <table width="100%">
                    <tr>
                        <td width="50%">
                            <table class="styledLeft" id="serverTable" width="100%">
                                <thead>
                                <tr>
                                    <th>
                                        <a href="server_statistics_view.jsp"><fmt:message
                                                key="server.statistics"/></a>
                                    </th>
                                </tr>
                                </thead>
                                <tr>
                                    <td>
                                        <div id="serverdiv">
                                            <% if (graphData.getServerData() == null || graphData.getServerData().trim().equals("")) { %>
                                            <p><i><fmt:message key="no.server.data"/></i></p>
                                            <% } else { %>
                                            <canvas id="serverGraph" height="200"
                                                    width="500"></canvas>
                                            <% } %>
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </td>
                        <td width="10">&nbsp;</td>
                        <td width="50%">
                            <table class="styledLeft" id="proxyServiceTable" width="100%">
                                <thead>
                                <tr>
                                    <th>
                                        <a href="proxyservice_statistics_view.jsp"><fmt:message
                                                key="proxy.service.statistics"/></a>
                                    </th>
                                </tr>
                                </thead>
                                <tr>
                                    <td>
                                        <div>
                                            <% if (graphData.getProxyServiceData() == null || graphData.getProxyServiceData().trim().equals("")) { %>
                                            <p><i>
                                                    <fmt:message key="no.proxy.service.data"/>
                                                        <% } else { %>
                                                <canvas id="proxyServiceGraph" height="200"
                                                        width="500"></canvas>
                                                        <% } %>
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr height="10"/>
                    <tr>
                        <td width="50%">
                            <table class="styledLeft" id="sequenceTable" width="100%">
                                <thead>
                                <tr>
                                    <th>
                                        <a href="sequence_statistics_view.jsp"><fmt:message
                                                key="sequence.statistics"/></a>
                                    </th>
                                </tr>
                                </thead>
                                <tr>
                                    <td>
                                        <div>
                                            <% if (graphData.getSequenceData() == null || graphData.getSequenceData().trim().equals("")) { %>
                                            <p><i><fmt:message key="no.sequence.data"/></i></p>
                                            <% } else { %>
                                            <canvas id="sequenceGraph" height="200"
                                                    width="500"></canvas>
                                            <% } %>
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </td>
                        <td width="10">&nbsp;</td>
                        <td width="50%">
                            <table class="styledLeft" id="endPointTable" width="100%">
                                <thead>
                                <tr>
                                    <th>
                                        <a href="endpoints_statistics_view.jsp"><fmt:message
                                                key="endpoint.statistics"/></a>
                                    </th>
                                </tr>
                                </thead>
                                <tr>
                                    <td>
                                        <div>
                                            <% if (graphData.getEndPointData() == null || graphData.getEndPointData().trim().equals("")) { %>
                                            <p><i><fmt:message key="no.endpoint.data"/></i></p>
                                            <% } else { %>
                                            <canvas id="endPointGraph" height="200"
                                                    width="500"></canvas>
                                            <% } %>
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </div>
            <script type="text/javascript">
                jQuery.noConflict();
                var refresh;
                var firstTimeRefresh = true;

                function refreshStats() {
                    if (!firstTimeRefresh) {
                        var url = "main_graphs_ajaxprocessor.jsp";
                        jQuery("#output").load(url, null, function (responseText, status, XMLHttpRequest) {
                            if (status != "success") {
                                stopRefreshStats();
                            }
                        });
                    } else {
                        firstTimeRefresh = false;
                    }
                }
                function stopRefreshStats() {
                    if (refresh) {
                        clearInterval(refresh);
                    }
                }
                jQuery(document).ready(function() {
                    refreshStats();
                    refresh = setInterval("refreshStats()", 30000);
                });
            </script>
        </div>
    </div>
    <%
            }
        } catch (Exception e) {
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);    
            %>
                <script type="text/javascript">
                    window.location.href = "../admin/error.jsp";
                </script>
            <%
        }
    %>

</fmt:bundle>
