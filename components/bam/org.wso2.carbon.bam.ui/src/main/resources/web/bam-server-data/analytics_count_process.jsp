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
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.wso2.carbon.bam.ui.report.beans.AnalyticsReportBean" %>

<%
    String reportCategory = request.getParameter("reportCategory");
    String selectedTask = request.getParameter("selectedTask");

    if (reportCategory.equals("service")) {
        Object reportDataObject = request.getSession().getAttribute("serviceStatCounts");

        List<AnalyticsReportBean> msgCountBeans = (List<AnalyticsReportBean>) reportDataObject;
        List<AnalyticsReportBean> newMsgCount = new ArrayList<AnalyticsReportBean>();
        for (int i = 0; i < msgCountBeans.size(); i++) {

            if (msgCountBeans.get(i).getTask().equals(selectedTask)) {
                newMsgCount.add(msgCountBeans.get(i));
            }
        }
        session.setAttribute("serviceStat", newMsgCount);

    } else if (reportCategory.equals("mediation")) {
        Object reportDataObject = request.getSession().getAttribute("mediationStatCounts");
        List<AnalyticsReportBean> timeBeans = (List<AnalyticsReportBean>) reportDataObject;
        List<AnalyticsReportBean> newTimeBeans = new ArrayList<AnalyticsReportBean>();

        for (int i = 0; i < timeBeans.size(); i++) {
            if (timeBeans.get(i).getTask().equals(selectedTask)) {
                newTimeBeans.add(timeBeans.get(i));
            }
        }
        session.setAttribute("mediationStat", newTimeBeans);
    }
%>