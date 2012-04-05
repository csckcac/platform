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

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>


<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.bam.ui.client.BAMStatQueryDSClient" %>
<%@ page import="org.wso2.carbon.bam.ui.client.BAMSummaryQueryDSClient" %>
<%@ page import="org.wso2.carbon.bam.ui.report.beans.AnalyticsReportBean" %>
<%@ page import="org.wso2.carbon.bam.ui.report.ReportTimeFormat" %>
<%@ page import="org.wso2.carbon.bam.util.BAMCalendar" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.*" %>
<%@ page import="org.wso2.carbon.bam.stub.listadmin.types.carbon.MonitoredServerDTO" %>
<%@ page import="org.wso2.carbon.bam.stub.summaryquery.MedSummaryStat" %>
<%@ page import="org.wso2.carbon.bam.stub.statquery.Endpoint" %>
<%@ page import="org.wso2.carbon.bam.stub.statquery.ProxyService" %>
<%@ page import="org.wso2.carbon.bam.stub.statquery.Sequence" %>
<%@ page import="org.wso2.carbon.bam.stub.summaryquery.SummaryStat" %>
<%@ page import="org.wso2.carbon.bam.ui.client.BAMListAdminServiceClient" %>

<%!
    private List<AnalyticsReportBean> countBeanList;
    private String summaryType;
%><fmt:bundle basename="org.wso2.carbon.bam.ui.i18n.Resources">

<script type="text/javascript" src="js/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/json/json-min.js"></script>
<script type="text/javascript" src="js/element/element-min.js"></script>
<script type="text/javascript" src="js/datasource/datasource-min.js"></script>
<script type="text/javascript" src="js/swf/swf-min.js"></script>
<script type="text/javascript" src="js/charts/charts-min.js"></script>
<script type="text/javascript" src="../yui/build/tabview/tabview-min.js"></script>
<script type="text/javascript" src="js/analytics_data_process.js"></script>
<link rel="stylesheet" type="text/css" href="../yui/build/tabview/assets/skins/sam/tabview.css"/>
<style type="text/css">

    #chartlinecount {
        width: <%=request.getParameter("chartWidth")%>px;
        height: 350px;
    }

    #chartlinetime {
        width: <%=request.getParameter("chartWidth")%>px;
        height: 350px;
    }

    #chartbarcount {
        width: <%=request.getParameter("chartWidth")%>px;
        height: 350px;
    }

    #chartbartime {
        width: <%=request.getParameter("chartWidth")%>px;
        height: 350px;
    }

    #chartcolumncount {
        width: <%=request.getParameter("chartWidth")%>px;
        height: 350px;
    }

    #chartcolumntime {
        width: <%=request.getParameter("chartWidth")%>px;
        height: 350px;
    }

</style>
<%
    String strSmall;
    Calendar calendar = new GregorianCalendar();

    String timeStart = request.getParameter("time_start");
    String timeEnd = request.getParameter("time_end");
    String pageMode = request.getParameter("pageMode");

    if (timeStart != null && timeEnd != null) {
        long l = new Long(timeStart);
        Date dStart = new Date(l);

        l = new Long(timeEnd);
        Date dEnd = new Date(l);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        calendar.setTime(dStart);
        String tmpStr = timeStart + " -> " + timeEnd;
        timeStart = formatter.format(dStart);
        timeEnd = formatter.format(dEnd);
    }
%>
<script type="text/javascript">
    var firstNodeSelected = false;
    var hasDataAnywhere = false;
    var firstNode = "";
    var serverArraysCount_firstNode = new Array();
    var serverArraysTime_firstNode = new Array();
</script>
<script type="text/javascript">
    var selectedTask = null;
    function selectedTaskForReport(task) {
        selectedTask = task;

    }
</script>

<table cellpadding="0" cellspacing="0" style="width:100%">
<tr>
<td valign="top" class="treeBox">

<div id="noDataError" style="display:none;"><fmt:message key="no.mediation.stats"/></div>
<ul class="root-list">

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    BAMListAdminServiceClient listAdminServiceClient = new BAMListAdminServiceClient(cookie, serverURL,
            configContext, request.getLocale());

    BAMSummaryQueryDSClient summaryQueryDSClient = new BAMSummaryQueryDSClient(cookie, serverURL,
            configContext, request.getLocale());

    BAMStatQueryDSClient statQueryDSClient = new BAMStatQueryDSClient(cookie, serverURL,
            configContext, request.getLocale());

    MonitoredServerDTO[] serverList = listAdminServiceClient.getServerList();
    List<AnalyticsReportBean> mediationAnalyticsCountBeans = new ArrayList<AnalyticsReportBean>();
    if (serverList != null) {
        int i = 0;
        for (MonitoredServerDTO server : serverList) {
            if (server != null) {
                i++;
                int j = 0;  //initiate second level tree which is always 0,1,2

%>
<script type="text/javascript">
    var maxCount<%=i%> = 0;
    var maxTime<%=i%> = 0;
    var serverArraysCount<%=i%> = new Array();
    var serverArraysTime<%=i%> = new Array();
    var hasData<%=i%> = false;

</script>
<li <% if(serverList.length != i){%>class="vertical-line" <% } %> id="server<%=i%>"
    style="display:none">
<div class="minus-icon" onclick="treeColapse(this)"></div>
<div class="mediators level1">
    <%
        strSmall = server.getServerURL();
        if (strSmall.length() > 35) {
            strSmall = strSmall.substring(0, 32);
            strSmall += "..";
        }
    %>
    <span title="<%=server.getServerURL()%>"><%=strSmall%></span>

    <%
        SummaryStat[] summaryStats = null;
        Calendar startTime = BAMCalendar.parseTimestamp(timeStart);
        Calendar endTime = BAMCalendar.parseTimestamp(timeEnd);
        if (pageMode.equals("hour")) {
            summaryType = "Hourly Summary Data";
            summaryStats = summaryQueryDSClient.getServerStatHourlySummaries(server,startTime ,endTime );
        } else if (pageMode.equals("day")) {
            summaryType = "Daily Summary Data";
            summaryStats = summaryQueryDSClient.getServerStatDailySummaries(server,startTime ,endTime);
        } else if (pageMode.equals("month")) {
            summaryType = "Monthly Summary Data";
            summaryStats = summaryQueryDSClient.getServerStatMonthlySummaries(server,startTime ,endTime);
        }
        if (summaryStats != null) {
            for (SummaryStat summaryStat : summaryStats) {
                if (summaryStat != null) {

                    String summaryStatsTimeStamp = summaryStat.getTimestamp();
                    String summaryStatReqCount = summaryStat.getReqCount();
                    String summaryStatResTime = summaryStat.getResCount();
                    String summaryStatFault = summaryStat.getFaultCount();
                    String summaryStatAvgTime = summaryStat.getAvgResTime();
                    String summaryStatMaxTime = summaryStat.getMaxResTime();
                    String summaryStatMinTime = summaryStat.getMinResTime();


    %>
    <script type="text/javascript">
        serverArraysCount<%=i%>.push({ timestmp: genTime('<%=summaryStatsTimeStamp%>'),time: formatDate("<%=summaryStatsTimeStamp%>"), request: <%=summaryStatReqCount%>, response: <%=summaryStatResTime%>,fault:<%=summaryStatFault%> });
        serverArraysTime<%=i%>.push({ timestmp: genTime('<%=summaryStatsTimeStamp%>'),time: genTime('<%=summaryStatsTimeStamp%>'),average: <%=summaryStatAvgTime%>, maximum: <%=summaryStatMaxTime%>,minimum:<%=summaryStatMinTime%> });
    </script>
    <%
                }
            }
        }
    %>
    <span class="BarClear"></span>
</div>

    <%--Endpoint start--%>

<div class="branch-node"></div>
<%
    Endpoint[] endpointsList = statQueryDSClient.getEndpoints(server.getServerId());

    int k = 0;
    if (endpointsList != null && endpointsList.length > 0) { %>
<ul class="child-list">
    <li class="vertical-line">
        <div class="minus-icon" onclick="treeColapse(this)"></div>
        <div class="mediators levelSub<%=j%>">
            Endpoints
        </div>
        <div class="branch-node"></div>

        <%
            j++;
            for (Endpoint endpoint : endpointsList) {

                if (endpoint != null) {

                    k++;

        %>
        <script type="text/javascript">
            var serverArraysCount<%=i%><%=j%><%=k%> = new Array();
            var serverArraysTime<%=i%><%=j%><%=k%> = new Array();
        </script>
        <ul class="child-list">
            <li <% if(endpointsList.length != k){%>class="vertical-line" <% } %>>
                <div class="alone-icon" onclick="treeColapse(this)"></div>
                <div class="mediators levelSubChild<%=j%>">
                    <%
                        strSmall = endpoint.getEndpoint();
                        if (strSmall.length() > 15) {
                            strSmall = strSmall.substring(0, 14);
                            strSmall += "..";
                        }
                    %>
                    <a href="#"
                       onclick="showAllGraphs(serverArraysCount<%=i%><%=j%><%=k%>,'count','<%=server.getServerURL()%> - <%=endpoint.getEndpoint()%>');showAllGraphs(serverArraysTime<%=i%><%=j%><%=k%>,'time','<%=server.getServerURL()%> - <%=endpoint.getEndpoint()%>');selectedTaskForReport('<%=server.getServerURL()%> - <%=endpoint.getEndpoint()%>');processData('mediation','<%=server.getServerURL()%> - <%=endpoint.getEndpoint()%>');"
                       title="<%=endpoint.getEndpoint()%>"><%=strSmall%>
                    </a>

                    <%
                        MedSummaryStat[] endpointSummaryStats = null;
                        int serverId = server.getServerId();

                        if (pageMode.equals("hour")) {
                            summaryType = "Hourly Summary Data";
                            endpointSummaryStats = summaryQueryDSClient.getEndpointStatHourlySummaries(serverId, endpoint, startTime , endTime);
                        } else if (pageMode.equals("day")) {
                            summaryType = "Daily Summary Data";
                            endpointSummaryStats = summaryQueryDSClient.getEndpointStatDailySummaries(serverId, endpoint, startTime , endTime);
                        } else if (pageMode.equals("month")) {
                            summaryType = "Monthly Summary Data";
                            endpointSummaryStats = summaryQueryDSClient.getEndpointStatMonthlySummaries(serverId, endpoint, startTime , endTime);
                        }
                        if (endpointSummaryStats != null) {
                            for (MedSummaryStat summaryStat : endpointSummaryStats) {
                                if (summaryStat != null) {

                                    String summaryStatEndPointTimeStamp = summaryStat.getTimestamp();
                                    String summaryStatEndPointReqCount = summaryStat.getReqCount();
                                    String summaryStatEndPointFaultCount = summaryStat.getFaultCount();
                                    String summaryStatEndPointAvgTime = summaryStat.getAvgResTime();
                                    String summaryStatEndPointMaxTime = summaryStat.getMaxResTime();
                                    String summaryStatEndPointMinTime = summaryStat.getMinResTime();

                                    AnalyticsReportBean mediationAnalytics = new AnalyticsReportBean();

                                    String task = server.getServerURL() + " - " + endpoint.getEndpoint();
                                    mediationAnalytics.setTask(task);
                                    mediationAnalytics.setRequest(summaryStatEndPointReqCount);
                                    mediationAnalytics.setFault(summaryStatEndPointFaultCount);
                                    mediationAnalytics.setOperation("End Points");
                                    mediationAnalytics.setTimeRange(summaryType);
                                    mediationAnalytics.setServerURL(server.getServerURL());
                                    mediationAnalytics.setTimeStamp(new ReportTimeFormat().formatTime(summaryStatEndPointTimeStamp));
                                    mediationAnalytics.setAvgTime(summaryStatEndPointAvgTime);
                                    mediationAnalytics.setMaxTime(summaryStatEndPointMaxTime);
                                    mediationAnalytics.setMaxTime(summaryStatEndPointMaxTime);
                                    mediationAnalytics.setMinTime(summaryStatEndPointMinTime);
                                    mediationAnalytics.setServerURL(server.getServerURL());

                                    mediationAnalyticsCountBeans.add(mediationAnalytics);
                    %>
                    <script type="text/javascript">
                        serverArraysCount<%=i%><%=j%><%=k%>.push({ timestmp: genTime("<%=summaryStatEndPointTimeStamp%>"),time: formatDate("<%=summaryStatEndPointTimeStamp%>"), request: <%=summaryStatEndPointReqCount%>, fault:<%=summaryStatEndPointFaultCount%> });
                        serverArraysTime<%=i%><%=j%><%=k%>.push({ timestmp: genTime("<%=summaryStatEndPointTimeStamp%>"),time: formatDate("<%=summaryStatEndPointTimeStamp%>"),average: <%=summaryStatEndPointAvgTime%>, maximum: <%=summaryStatEndPointMaxTime%>,minimum:<%=summaryStatEndPointMinTime%> });
                    </script>
                    <%
                            }
                        }
                    %>
                    <script type="text/javascript">
                        if (!hasDataAnywhere) {
                            firstNode = "<%=server.getServerURL()%> - <%=endpoint.getEndpoint()%>";
                            serverArraysCount_firstNode = serverArraysCount<%=i%><%=j%><%=k%>;
                            serverArraysTime_firstNode = serverArraysTime<%=i%><%=j%><%=k%>;
                        }
                        hasData<%=i%> = true;
                        hasDataAnywhere = true;

                    </script>
                    <%
                        }
                    %>

                    <span class="BarClear"></span>
                </div>
                <div class="empty-brake"/>

            </li>
        </ul>
        <%
                }
            }
        %>

    </li>
</ul>
<%
    }
%>

<div class="branch-node"></div>
<%
    ProxyService[] proxyServicesList = statQueryDSClient.getProxyServices(server.getServerId());

    k = 0;
    if (proxyServicesList != null && proxyServicesList.length > 0) { %>
<ul class="child-list">
    <li class="vertical-line">
        <div class="minus-icon" onclick="treeColapse(this)"></div>
        <div class="mediators levelSub<%=j%>">
            Proxy Services
        </div>
        <div class="branch-node"></div>

        <%
            j++;
            for (ProxyService proxyService : proxyServicesList) {
                if (proxyService != null) {
                    k++;

        %>
        <script type="text/javascript">
            var serverArraysCount<%=i%><%=j%><%=k%> = new Array();
            var serverArraysTime<%=i%><%=j%><%=k%> = new Array();
        </script>
        <ul class="child-list">
            <li <% if(proxyServicesList.length != k){%>class="vertical-line" <% } %>>
                <div class="alone-icon" onclick="treeColapse(this)"></div>
                <div class="mediators levelSubChild<%=j%>">
                    <%
                        strSmall = proxyService.getProxyService();
                        if (strSmall.length() > 15) {
                            strSmall = strSmall.substring(0, 14);
                            strSmall += "..";
                        }
                    %>
                    <a href="#navigateto"
                       onclick="showAllGraphs(serverArraysCount<%=i%><%=j%><%=k%>,'count','<%=server.getServerURL()%> - <%=proxyService.getProxyService()%>');showAllGraphs(serverArraysTime<%=i%><%=j%><%=k%>,'time','<%=server.getServerURL()%> - <%=proxyService.getProxyService()%>');selectedTaskForReport('<%=server.getServerURL()%> - <%=proxyService.getProxyService()%>');processData('mediation','<%=server.getServerURL()%> - <%=proxyService.getProxyService()%>');"
                       title="<%=proxyService.getProxyService()%>"><%=strSmall%>
                    </a>

                    <%
                        MedSummaryStat[] proxyServiceSummaryStats = null;
                        if (pageMode.equals("hour")) {
                            summaryType = "Hourly Summary Data";
                            proxyServiceSummaryStats = summaryQueryDSClient.getProxyStatHourlySummaries(server.getServerId(), proxyService, BAMCalendar.parseTimestamp(timeStart), BAMCalendar.parseTimestamp(timeEnd));
                        } else if (pageMode.equals("day")) {
                            summaryType = "Daily Summary Data";
                            proxyServiceSummaryStats = summaryQueryDSClient.getProxyStatDailySummaries(server.getServerId(), proxyService, BAMCalendar.parseTimestamp(timeStart), BAMCalendar.parseTimestamp(timeEnd));
                        } else if (pageMode.equals("month")) {
                            summaryType = "Monthly Summary Data";
                            proxyServiceSummaryStats = summaryQueryDSClient.getProxyStatMonthlySummaries(server.getServerId(), proxyService, BAMCalendar.parseTimestamp(timeStart), BAMCalendar.parseTimestamp(timeEnd));
                        }
                        if (proxyServiceSummaryStats != null) {
                            for (MedSummaryStat summaryStat : proxyServiceSummaryStats) {

                                if (summaryStat != null) {

                                    String summaryStatProxyTimeStamp = summaryStat.getTimestamp();
                                    String summaryStatProxyReqCount = summaryStat.getReqCount();
                                    String summaryStatProxyFaultCount = summaryStat.getFaultCount();
                                    String summaryStatProxyAvgTime = summaryStat.getAvgResTime();
                                    String summaryStatProxyMaxTime = summaryStat.getMaxResTime();
                                    String summaryStatProxyMinTime = summaryStat.getMinResTime();

                                    String task = server.getServerURL() + " - " + proxyService.getProxyService();
                                    AnalyticsReportBean mediationAnalytics = new AnalyticsReportBean();

                                    mediationAnalytics.setTask(task);
                                    mediationAnalytics.setRequest(summaryStatProxyReqCount);
                                    mediationAnalytics.setFault(summaryStatProxyFaultCount);
                                    mediationAnalytics.setOperation("Proxy Services");
                                    mediationAnalytics.setServerURL(server.getServerURL());
                                    mediationAnalytics.setTimeRange(summaryType);
                                    mediationAnalytics.setAvgTime(summaryStatProxyAvgTime);
                                    mediationAnalytics.setMaxTime(summaryStatProxyMaxTime);
                                    mediationAnalytics.setMinTime(summaryStatProxyMinTime);
                                    mediationAnalytics.setTimeStamp(new ReportTimeFormat().formatTime(summaryStatProxyTimeStamp));
                                    mediationAnalytics.setServerURL(server.getServerURL());

                                    mediationAnalyticsCountBeans.add(mediationAnalytics);


                    %>
                    <script type="text/javascript">
                        serverArraysCount<%=i%><%=j%><%=k%>.push({ timestmp: genTime("<%=summaryStatProxyTimeStamp%>"),time: formatDate("<%=summaryStatProxyTimeStamp%>"), request: <%=summaryStatProxyReqCount%>,fault:<%=summaryStatProxyFaultCount%> });
                        serverArraysTime<%=i%><%=j%><%=k%>.push({ timestmp: genTime("<%=summaryStatProxyTimeStamp%>"),time: formatDate("<%=summaryStatProxyTimeStamp%>"),average: <%=summaryStatProxyAvgTime%>, maximum: <%=summaryStatProxyMaxTime%>,minimum:<%=summaryStatProxyMinTime%> });
                    </script>
                    <%
                            }
                        }
                    %>
                    <script type="text/javascript">
                        if (!hasDataAnywhere) {
                            firstNode = "<%=server.getServerURL()%> - <%=proxyService.getProxyService()%>";
                            serverArraysCount_firstNode = serverArraysCount<%=i%><%=j%><%=k%>;
                            serverArraysTime_firstNode = serverArraysTime<%=i%><%=j%><%=k%>;
                        }
                        hasData<%=i%> = true;
                        hasDataAnywhere = true;

                    </script>
                    <%
                        }
                    %>
                    <span class="BarClear"></span>
                </div>
                <div class="empty-brake"/>

            </li>
        </ul>
        <%
                }
            }
        %>

    </li>
</ul>
<%
    }
%>

<div class="branch-node"></div>
<%
    Sequence[] sequenceList = statQueryDSClient.getSequences(server.getServerId());

    k = 0;
    if (sequenceList != null && sequenceList.length > 0) { %>
<ul class="child-list">
    <li>
        <div class="minus-icon" onclick="treeColapse(this)"></div>
        <div class="mediators levelSub<%=j%>">
            Sequences
        </div>
        <div class="branch-node"></div>

        <%
            j++;
            for (Sequence sequence : sequenceList) {
                if (sequence != null) {
                    k++;

        %>
        <script type="text/javascript">
            var serverArraysCount<%=i%><%=j%><%=k%> = new Array();
            var serverArraysTime<%=i%><%=j%><%=k%> = new Array();
        </script>
        <ul class="child-list">
            <li <% if(sequenceList.length != k){%>class="vertical-line" <% } %>>
                <div class="alone-icon" onclick="treeColapse(this)"></div>
                <div class="mediators levelSubChild<%=j%>">
                    <%
                        strSmall = sequence.getSequence();
                        if (strSmall.length() > 15) {
                            strSmall = strSmall.substring(0, 14);
                            strSmall += "..";
                        }
                    %>
                    <a href="#navigateto"
                       onclick="showAllGraphs(serverArraysCount<%=i%><%=j%><%=k%>,'count','<%=server.getServerURL()%> - <%=sequence.getSequence()%>');showAllGraphs(serverArraysTime<%=i%><%=j%><%=k%>,'time','<%=server.getServerURL()%> - <%=sequence.getSequence()%>');selectedTaskForReport('<%=server.getServerURL()%> - <%=sequence.getSequence()%>');processData('mediation','<%=server.getServerURL()%> - <%=sequence.getSequence()%>');"
                       title="<%=sequence.getSequence()%>"><%=strSmall%>
                    </a>

                    <%
                        MedSummaryStat[] sequenceSummaryStats = null;
                        if (pageMode.equals("hour")) {
                            summaryType = "Hourly Summary Data";
                            sequenceSummaryStats = summaryQueryDSClient.getSequenceStatHourlySummaries(server.getServerId(), sequence, BAMCalendar.parseTimestamp(timeStart), BAMCalendar.parseTimestamp(timeEnd));
                        } else if (pageMode.equals("day")) {
                            summaryType = "Daily Summary Data";
                            sequenceSummaryStats = summaryQueryDSClient.getSequenceStatDailySummaries(server.getServerId(), sequence, BAMCalendar.parseTimestamp(timeStart), BAMCalendar.parseTimestamp(timeEnd));
                        } else if (pageMode.equals("month")) {
                            summaryType = "Monthly Summary Data";
                            sequenceSummaryStats = summaryQueryDSClient.getSequenceStatMonthlySummaries(server.getServerId(), sequence, BAMCalendar.parseTimestamp(timeStart), BAMCalendar.parseTimestamp(timeEnd));
                        }
                        if (sequenceSummaryStats != null) {
                            for (MedSummaryStat summaryStat : sequenceSummaryStats) {
                                if (summaryStat != null) {

                                    String summaryStatSequenceTimeStamp = summaryStat.getTimestamp();
                                    String summaryStatSequenceReqCount = summaryStat.getReqCount();
                                    String summaryStatSequenceFaultCount = summaryStat.getFaultCount();
                                    String summaryStatSequenceAvgTime = summaryStat.getAvgResTime();
                                    String summaryStatSequenceMaxTime = summaryStat.getMaxResTime();
                                    String summaryStatSequenceMinTime = summaryStat.getMinResTime();

                                    String task = server.getServerURL() + " - " + sequence.getSequence();

                                    AnalyticsReportBean mediationAnalytics = new AnalyticsReportBean();
                                    mediationAnalytics.setTask(task);
                                    mediationAnalytics.setRequest(summaryStatSequenceReqCount);
                                    mediationAnalytics.setFault(summaryStatSequenceFaultCount);
                                    mediationAnalytics.setOperation("Sequences");
                                    mediationAnalytics.setTimeRange(summaryType);
                                    mediationAnalytics.setServerURL(server.getServerURL());
                                    mediationAnalytics.setTimeStamp(new ReportTimeFormat().formatTime(summaryStatSequenceTimeStamp));
                                    mediationAnalytics.setAvgTime(summaryStatSequenceAvgTime);
                                    mediationAnalytics.setMaxTime(summaryStatSequenceMaxTime);
                                    mediationAnalytics.setMinTime(summaryStatSequenceMinTime);

                                    mediationAnalyticsCountBeans.add(mediationAnalytics);


                    %>
                    <script type="text/javascript">
                        serverArraysCount<%=i%><%=j%><%=k%>.push({ timestmp: genTime("<%=summaryStatSequenceTimeStamp%>"),time: formatDate("<%=summaryStatSequenceTimeStamp%>"), request: <%=summaryStatSequenceReqCount%>,fault:<%=summaryStatSequenceFaultCount%> });
                        serverArraysTime<%=i%><%=j%><%=k%>.push({ timestmp: genTime("<%=summaryStatSequenceTimeStamp%>"),time: formatDate("<%=summaryStatSequenceTimeStamp%>"),average: <%=summaryStatSequenceAvgTime%>, maximum: <%=summaryStatSequenceMaxTime%>,minimum:<%=summaryStatSequenceMinTime%> });
                    </script>
                    <%
                            }
                        }
                    %>
                    <script type="text/javascript">
                        if (!hasDataAnywhere) {
                            firstNode = "<%=server.getServerURL()%> - <%=sequence.getSequence()%>";
                            serverArraysCount_firstNode = serverArraysCount<%=i%><%=j%><%=k%>;
                            serverArraysTime_firstNode = serverArraysTime<%=i%><%=j%><%=k%>;
                        }
                        hasData<%=i%> = true;
                        hasDataAnywhere = true;

                    </script>
                    <%
                        }
                    %>
                    <span class="BarClear"></span>
                </div>
                <div class="empty-brake"/>

            </li>
        </ul>
        <%
                }
            }
        %>

    </li>
</ul>
<%
    }
%>

<div class="empty-brake"/>


    <%--Sequence ends--%>
</li>
<script type="text/javascript">
    if (hasData<%=i%>) {
        document.getElementById("server<%=i%>").style.display = "";
    }
</script>
<%
            }


        }
    }

    request.getSession().setAttribute("mediationStatCounts", mediationAnalyticsCountBeans);

    listAdminServiceClient.cleanup();
    summaryQueryDSClient.cleanup();
    statQueryDSClient.cleanup();

%>
<script type="text/javascript">
    createTabs("<fmt:message key="time"/>");
    createTabs("<fmt:message key="count"/>");
</script>
</ul>
</td>
<td valign="top" style="padding-left:5px;*padding-left:10px;">
    <h3 id="titleRight"></h3>

    <div id="report_ui_1">
        <jsp:include page="../bam-server-data/graph_ui.jsp" flush="true">
            <jsp:param name="template" value="mediation_analytics_msg"/>
            <jsp:param name="reportDataSession" value="mediationStatCounts"/>
            <jsp:param name="component" value="org.wso2.carbon.bam.core"/>
        </jsp:include>
    </div>
    <div class="dashboard_sub">
        <div class="rounder"></div>
        <div class="dashboard_intro_h2 dashboard_sub_h2"><fmt:message key="message.counts"/></div>
    </div>


    <div class="dashboard_content yui-skin-sam">
        <div id="tabContainer_count"></div>
    </div>
    <div id="report_ui_2">
        <jsp:include page="../bam-server-data/graph_ui_second.jsp" flush="true">
            <jsp:param name="template" value="mediation_analytics_time"/>
            <jsp:param name="reportDataSession" value="mediationStatCounts"/>
            <jsp:param name="component" value="org.wso2.carbon.bam.core"/>
        </jsp:include>
    </div>
    <div class="dashboard_sub">
        <div class="rounder"></div>
        <div class="dashboard_intro_h2 dashboard_sub_h2"><fmt:message
                key="message.response.times"/></div>
    </div>
    <div class="dashboard_content yui-skin-sam">
        <div id="tabContainer_time"></div>
    </div>

</td>
</tr>
</table>

<script type="text/javascript">
    if (!hasDataAnywhere) {
        document.getElementById("noDataError").style.display = "";
        document.getElementById("titleRight").style.display = "none";
        document.getElementById("tabContainer_time").style.display = "none";
        document.getElementById("tabContainer_count").style.display = "none";
        document.getElementById("report_ui_1").style.display = "none";
        document.getElementById("report_ui_2").style.display = "none";

    } else {
        document.getElementById("report_ui_1").style.display = "";
        document.getElementById("report_ui_2").style.display = "";
    }

    if (!firstNodeSelected) {
        processData('mediation', firstNode);
        selectedTaskForReport(firstNode);
        showAllGraphs(serverArraysCount_firstNode, "<fmt:message key="count"/>", null);
        showAllGraphs(serverArraysTime_firstNode, "<fmt:message key="time"/>", null);
        firstNodeSelected = true;
        YAHOO.util.Dom.get("titleRight").innerHTML = firstNode;
    }
</script>


</fmt:bundle>
