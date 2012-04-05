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
<%@ page import="org.wso2.carbon.bam.ui.client.BAMSummaryQueryDSClient" %>
<%@ page import="org.wso2.carbon.bam.ui.report.beans.AnalyticsReportBean" %>
<%@ page import="org.wso2.carbon.bam.ui.report.ReportTimeFormat" %>
<%@ page import="org.wso2.carbon.bam.util.BAMCalendar" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.*" %>
<%@ page import="org.wso2.carbon.bam.ui.client.BAMListAdminServiceClient" %>
<%@ page import="org.wso2.carbon.bam.stub.listadmin.types.carbon.MonitoredServerDTO" %>
<%@ page import="org.wso2.carbon.bam.stub.summaryquery.SummaryStat" %>
<%@ page import="org.wso2.carbon.bam.stub.listadmin.types.carbon.ServiceDO" %>
<%@ page import="org.wso2.carbon.bam.stub.listadmin.types.carbon.OperationDO" %>


<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<script type="text/javascript" src="js/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/json/json-min.js"></script>
<script type="text/javascript" src="js/element/element-min.js"></script>
<script type="text/javascript" src="js/datasource/datasource-min.js"></script>
<script type="text/javascript" src="js/swf/swf-min.js"></script>
<script type="text/javascript" src="js/charts/charts-min.js"></script>
<script type="text/javascript" src="../yui/build/tabview/tabview-min.js"></script>
<script type="text/javascript" src="js/analytics_data_process.js"></script>

<%!
    private String summaryType;
%><fmt:bundle basename="org.wso2.carbon.bam.ui.i18n.Resources">
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
    String firstNode = "";
    String strSmall = "";
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
<div id="noDataError" style="display:none;"><fmt:message key="no.service.stats"/></div>

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
    MonitoredServerDTO[] serverList = listAdminServiceClient.getServerList();
    List<AnalyticsReportBean> analyticsMsgCountBeans = new ArrayList<AnalyticsReportBean>();

    if (serverList != null) {
        int i = 0;

        for (MonitoredServerDTO server : serverList) {
            if (server != null) {
                i++;
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
    <a href="#navigateto" onclick="showAllGraphs(serverArraysCount<%=i%>,'count','<%=server.getServerURL()%>');showAllGraphs(serverArraysTime<%=i%>,'time','<%=server.getServerURL()%>');selectedTaskForReport('<%=server.getServerURL()%>');processData('service','<%=server.getServerURL()%>');"
       title="<%=server.getServerURL()%>"><%=strSmall%>
    </a>

    <%
        SummaryStat[] summaryStats = null;
        Calendar startCalendar = BAMCalendar.parseTimestamp(timeStart);
        Calendar endCalendar = BAMCalendar.parseTimestamp(timeEnd);
        if (pageMode.equals("hour")) {
            summaryType = "Hourly Summary Data";
            summaryStats = summaryQueryDSClient.getServerStatHourlySummaries(server,startCalendar ,endCalendar);
        } else if (pageMode.equals("day")) {
            summaryType = "Daily Summary Data";
            summaryStats = summaryQueryDSClient.getServerStatDailySummaries(server,startCalendar ,endCalendar);
        } else if (pageMode.equals("month")) {
            summaryType = "Monthly Summary Data";
            summaryStats = summaryQueryDSClient.getServerStatMonthlySummaries(server,startCalendar ,endCalendar);
        }
        if (summaryStats != null) {

            for (SummaryStat summaryStat : summaryStats) {
                if (summaryStat != null) {
                    String serverSummaryReqCount = summaryStat.getReqCount();
                    String serverSummaryResCount = summaryStat.getResCount();
                    String serverSummaryFaultCount = summaryStat.getFaultCount();
                    String serverSummaryTimeStamp = summaryStat.getTimestamp();
                    String serverSummaryAvgResTime = summaryStat.getAvgResTime();
                    String serverSummaryMaxResTime = summaryStat.getMaxResTime();
                    String serverSummaryMinResTime = summaryStat.getMinResTime();
                    // for reporting purpose
                    AnalyticsReportBean analytics = new AnalyticsReportBean();
                    analytics.setTask(server.getServerURL());
                    analytics.setTimeStamp(new ReportTimeFormat().formatTime(serverSummaryTimeStamp));
                    analytics.setRequest(serverSummaryReqCount);
                    analytics.setResponse(serverSummaryResCount);
                    analytics.setFault(serverSummaryFaultCount);
                    analytics.setAvgTime(serverSummaryAvgResTime);
                    analytics.setMaxTime(serverSummaryMaxResTime);
                    analytics.setMinTime(serverSummaryMinResTime);
                    analytics.setTimeRange(summaryType);
                    analyticsMsgCountBeans.add(analytics);


    %>
    <script type="text/javascript">
        serverArraysCount<%=i%>.push({ timestmp: genTime("<%=serverSummaryTimeStamp%>"),time: formatDate("<%=serverSummaryTimeStamp%>"), request: <%=serverSummaryReqCount%>, response: <%=serverSummaryResCount%>,fault:<%=serverSummaryFaultCount%> });
        serverArraysTime<%=i%>.push({ timestmp: genTime("<%=serverSummaryTimeStamp%>"),time: formatDate("<%=serverSummaryTimeStamp%>"),average: <%=serverSummaryAvgResTime%>, maximum: <%=serverSummaryMaxResTime%>,minimum:<%=serverSummaryMinResTime%> });
        if (parseInt("<%=serverSummaryReqCount%>") != 0 || parseInt("<%=serverSummaryAvgResTime%>") != 0) {
            hasData<%=i%> = true;
            hasDataAnywhere = true;
        }
    </script>
    <%
                }
            }
        }
        firstNode = server.getServerURL();
    %>
    <span class="BarClear"></span>
</div>
<%
    ServiceDO[] servicesList = listAdminServiceClient.getServicesList(server.getServerId());
    // ServiceDTOClient[] serviceDTOClient=  BeanConverter.getServiceDTOClient(servicesList);

    if (servicesList != null) {
%>
<div class="branch-node"></div>
<% } else { %>
<div class="empty-brake"/>
<% }
    if (servicesList != null) {
        int j = 0;
        for (ServiceDO service : servicesList) {
            if (service != null) {
                j++;
%>
<script type="text/javascript">
    var maxCount<%=i%>_<%=j%> = 0;
    var maxTime<%=i%>_<%=j%> = 0;
    var serverArraysCount<%=i%>_<%=j%> = new Array();
    var serverArraysTime<%=i%>_<%=j%> = new Array();
</script>
<ul class="child-list">
    <li <% if(servicesList.length != j){%>class="vertical-line" <% } %> >
        <div class="minus-icon" onclick="treeColapse(this)"></div>
        <div class="mediators level2">
            <%
                strSmall = service.getName();
                if (strSmall.length() > 30) {
                    strSmall = strSmall.substring(0, 27);
                    strSmall += "..";
                }
            %>
            <a href="#navigateto" onclick="showAllGraphs(serverArraysCount<%=i%>_<%=j%>,'count','<%=server.getServerURL()%>'+' - '+'<%=service.getName()%>');showAllGraphs(serverArraysTime<%=i%>_<%=j%>,'time','<%=server.getServerURL()%>'+' - '+'<%=service.getName()%>');selectedTaskForReport('<%=server.getServerURL()%>' + ' -- ' + '<%=service.getName()%>');processData('service','<%=server.getServerURL()%>' + ' -- ' + '<%=service.getName()%>');"
               title="<%=service.getName()%>"><%=strSmall%>
            </a>

            <%
                SummaryStat[] serviceSummaryStats = null;
                if (pageMode.equals("hour")) {
                    summaryType = "Hourly Summary Data";
                    serviceSummaryStats = summaryQueryDSClient.getServiceStatHourlySummaries(service, startCalendar,endCalendar);
                } else if (pageMode.equals("day")) {
                    summaryType = "Daily Summary Data";
                    serviceSummaryStats = summaryQueryDSClient.getServiceStatDailySummaries(service, startCalendar,endCalendar);
                } else if (pageMode.equals("month")) {
                    summaryType = "Monthly Summary Data";
                    serviceSummaryStats = summaryQueryDSClient.getServiceStatMonthlySummaries(service, startCalendar,endCalendar);
                }
                if (serviceSummaryStats != null && serviceSummaryStats.length > 0) {
                    for (SummaryStat serviceSummaryStat : serviceSummaryStats) {
                        if (serviceSummaryStat != null) {

                            String serviceSummaryReqCount = serviceSummaryStat.getReqCount();
                            String serviceSummaryResCount = serviceSummaryStat.getResCount();
                            String serviceSummaryFaultCount = serviceSummaryStat.getFaultCount();
                            String serviceSummaryTimeStamp = serviceSummaryStat.getTimestamp();
                            String serviceSummaryAvgTime = serviceSummaryStat.getAvgResTime();
                            String serviceSummaryMaxTime = serviceSummaryStat.getMaxResTime();
                            String serviceSummaryMinTime = serviceSummaryStat.getMinResTime();
                            //for reporting data collection
                            AnalyticsReportBean analytics = new AnalyticsReportBean();

                            String taskValue = server.getServerURL() + " -- " + service.getName();
                            analytics.setTask(taskValue);
                            analytics.setTimeStamp(new ReportTimeFormat().formatTime(serviceSummaryTimeStamp));
                            analytics.setRequest(serviceSummaryReqCount);
                            analytics.setResponse(serviceSummaryResCount);
                            analytics.setFault(serviceSummaryFaultCount);
                            analytics.setAvgTime(serviceSummaryAvgTime);
                            analytics.setMaxTime(serviceSummaryMaxTime);
                            analytics.setMinTime(serviceSummaryMinTime);
                            analytics.setTimeRange(summaryType);
                            analyticsMsgCountBeans.add(analytics);

            %>
            <script type="text/javascript">
                serverArraysCount<%=i%>_<%=j%>.push({ timestmp: genTime("<%=serviceSummaryTimeStamp%>"), time: formatDate("<%=serviceSummaryTimeStamp%>"), request: <%=serviceSummaryReqCount%>, response: <%=serviceSummaryResCount%>,fault:<%=serviceSummaryFaultCount%> });
                serverArraysTime<%=i%>_<%=j%>.push({ timestmp: genTime("<%=serviceSummaryTimeStamp%>"),time: formatDate("<%=serviceSummaryTimeStamp%>"), average: <%=serviceSummaryAvgTime%>, maximum: <%=serviceSummaryMaxTime%>,minimum:<%=serviceSummaryMinTime%> });
                if (parseInt("<%=serviceSummaryReqCount%>") != 0 || parseInt("<%=serviceSummaryAvgTime%>") != 0) {
                    hasData<%=i%> = true;
                    hasDataAnywhere = true;
                }
            </script>
            <%
                        }
                    }
                }
            %>
        </div>
        <%
            OperationDO[] operationList = listAdminServiceClient.getOperationList(service.getId());

            if (operationList != null) {
        %>
        <div class="branch-node"></div>
        <% } else { %>
        <div class="empty-brake"/>
        <% }
            if (operationList != null) {
                int k = 0;
                for (OperationDO operation : operationList) {
                    if (operation != null) {
                        k++;
        %>
        <script type="text/javascript">
            var maxCount<%=i%>_<%=j%>_<%=k%> = 0;
            var maxTime<%=i%>_<%=j%>_<%=k%> = 0;
            var serverArraysCount<%=i%>_<%=j%>_<%=k%> = new Array();
            var serverArraysTime<%=i%>_<%=j%>_<%=k%> = new Array();
        </script>
        <ul class="child-list">
            <li <% if(operationList.length != k){%>class="vertical-line" <% } %>>
                <div class="alone-icon" onclick="treeColapse(this)"></div>
                <div class="mediators level3">
                    <%
                        strSmall = operation.getName();
                        if (strSmall.length() > 55) {
                            strSmall = strSmall.substring(0, 14);
                            strSmall += "..";
                        }
                    %>
                    <a href="#navigateto" onclick="showAllGraphs(serverArraysCount<%=i%>_<%=j%>_<%=k%>,
                                                    'count',
                                                    '<%=server.getServerURL()%>' + ' - ' + '<%=operation.getName()%>');

                                                    showAllGraphs(serverArraysTime<%=i%>_<%=j%>_<%=k%>,
                                                    'time',
                                                    '<%=server.getServerURL()%>'+' - '+'<%=operation.getName()%>');
                                                    selectedTaskForReport('<%=server.getServerURL()%>'+' -- '+'<%=service.getName()%>'+' -- '+'<%=operation.getName()%>');processData('service','<%=server.getServerURL()%>'+' -- '+'<%=service.getName()%>'+' -- '+'<%=operation.getName()%>');"
                       title="<%=operation.getName()%>">
                        <%=strSmall%>
                    </a>
                    <%
                        SummaryStat[] opSummaryStats = null;
                        if (pageMode.equals("hour")) {
                            summaryType = "Hourly Summary Data";
                            opSummaryStats = summaryQueryDSClient.getOperationStatHourlySummaries(operation, startCalendar,endCalendar);
                        } else if (pageMode.equals("day")) {
                            summaryType = "Daily Summary Data";
                            opSummaryStats = summaryQueryDSClient.getOperationStatDailySummaries(operation, startCalendar,endCalendar);
                        } else if (pageMode.equals("month")) {
                            summaryType = "Monthly Summary Data";
                            opSummaryStats = summaryQueryDSClient.getOperationStatMonthlySummaries(operation, startCalendar,endCalendar);
                        }
                        if (opSummaryStats != null && opSummaryStats.length > 0) {
                            for (SummaryStat opSummaryStat : opSummaryStats) {
                                if (opSummaryStat != null) {

                                    String operationSummaryReqCount = opSummaryStat.getReqCount();
                                    String operationSummaryResCount = opSummaryStat.getResCount();
                                    String operationSummaryFaultCount = opSummaryStat.getFaultCount();
                                    String operationSummaryTimeStamp = opSummaryStat.getTimestamp();
                                    String operationSummaryAvgTime = opSummaryStat.getAvgResTime();
                                    String operationSummaryMaxTime = opSummaryStat.getMaxResTime();
                                    String operationSummaryMinTime = opSummaryStat.getMinResTime();
                                    // for reporting data collection
                                    AnalyticsReportBean analytics = new AnalyticsReportBean();
                                    String taskOp = server.getServerURL() + " -- " + service.getName() + " -- " + operation.getName();
                                    analytics.setTask(taskOp);
                                    analytics.setTimeStamp(new ReportTimeFormat().formatTime(operationSummaryTimeStamp));
                                    analytics.setRequest(operationSummaryReqCount);
                                    analytics.setResponse(operationSummaryResCount);
                                    analytics.setFault(operationSummaryFaultCount);
                                    analytics.setAvgTime(operationSummaryAvgTime);
                                    analytics.setMaxTime(operationSummaryMaxTime);
                                    analytics.setMinTime(operationSummaryMinTime);
                                    analytics.setTimeRange(summaryType);
                                    analyticsMsgCountBeans.add(analytics);
                    %>
                    <script type="text/javascript">

                        serverArraysCount<%=i%>_<%=j%>_<%=k%>.push({ timestmp: genTime("<%=operationSummaryTimeStamp%>"),time: formatDate("<%=operationSummaryTimeStamp%>"), request: <%=operationSummaryReqCount%>, response: <%=operationSummaryResCount%>,fault:<%=operationSummaryFaultCount%> });
                        serverArraysTime<%=i%>_<%=j%>_<%=k%>.push({ timestmp: genTime("<%=operationSummaryTimeStamp%>"), time: formatDate("<%=operationSummaryTimeStamp%>"), average: <%=operationSummaryAvgTime%>, maximum: <%=operationSummaryMaxTime%>,minimum:<%=operationSummaryMinTime%> });
                        if (parseInt("<%=operationSummaryReqCount%>") != 0 || parseInt("<%=operationSummaryAvgTime%>") != 0) {
                            hasData<%=i%> = true;
                            hasDataAnywhere = true;
                        }
                    </script>
                    <%
                                }
                            }
                        }
                    %>

                </div>
                <div class="empty-brake"/>
            </li>
        </ul>
        <%
                }
            }

        %>
        <div class="empty-brake"/>
    </li>
</ul>
<%
            }
        }
    }
%>
<div class="empty-brake"/>
</li>

<script type="text/javascript">
    if (hasData<%=i%>) {
        document.getElementById("server<%=i%>").style.display = "";
    }

    if (hasData<%=i%> && !firstNodeSelected) {
        firstNode = "<%=server.getServerURL()%>";
        serverArraysCount_firstNode = serverArraysCount<%=i%>;
        serverArraysTime_firstNode = serverArraysTime<%=i%>;
        firstNodeSelected = true;
    }
</script>
<%
                }
            }

        }

    }

    request.getSession().setAttribute("serviceStatCounts", analyticsMsgCountBeans);

    listAdminServiceClient.cleanup();
    summaryQueryDSClient.cleanup();

%>
</ul>
<script type="text/javascript">
    createTabs("<fmt:message key="time"/>");
    createTabs("<fmt:message key="count"/>");
</script>
</td>
<td valign="top" style="padding-left:5px;*padding-left:10px;">
    <h3 id="titleRight"></h3>

    <div id="report_ui_1">
        <jsp:include page="../bam-server-data/graph_ui.jsp" flush="true">
            <jsp:param name="template" value="server_analytics_msg"/>
            <jsp:param name="reportDataSession" value="serviceStat"/>
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
            <jsp:param name="template" value="server_analytics_time"/>
            <jsp:param name="reportDataSession" value="serviceStat"/>
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

    if (firstNodeSelected) {
        processData('service',firstNode);
        selectedTaskForReport(firstNode);
        showAllGraphs(serverArraysCount_firstNode, 'count', null);
        showAllGraphs(serverArraysTime_firstNode, 'time', null);
        firstNodeSelected = true;
        YAHOO.util.Dom.get("titleRight").innerHTML = firstNode;
    }
</script>
</fmt:bundle>
