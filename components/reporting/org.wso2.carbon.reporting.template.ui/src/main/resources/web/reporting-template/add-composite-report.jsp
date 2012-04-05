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

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.reporting.template.ui.client.ReportTemplateClient" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.reporting.template.ui.i18n.Resources">


    <%

        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        ReportTemplateClient client;
        String[] allReports = null;
        try {
            client = new ReportTemplateClient(configContext, serverURL, cookie);
            allReports = client.getAllTemplateFiles();
        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
    %>
    <script type="text/javascript">
        location.href = "../admin/error.jsp";
    </script>
    <%
            return;
        }
    %>
    <script>
        var reports = new Array(<%
                                for(int i = 0; i < allReports.length; i++) {
                                   out.print("\""+allReports[i]+"\"");
                                 if(i+1 < allReports.length) {
                                     out.print(",");
                                }
                                }
                            %>);
    </script>

    <script type="text/javascript">
        var count = 0;
        var reportIndex = "";

        function addReport() {
            document.compositeReport.action = 'add-composite-report.jsp';
            count = count + 1;
            var curCount = count;
            var d = document.getElementById('contentTableBody');
            var html = '<tr id="report_tr_' + count + '">';
            html = html + '<td>' + '<fmt:message key="report"/>' + ' - ' + count;
            html = html + '<span class = "required" >*</span>' + '</td>';
            html = html + '<td>';
            html = html + getTemplateComboBox();
            html = html + '</td>';
            if (count != 1) {
                html = html + '<td>';
                html = html + '<a class="icon-link spacer-bot" onclick="removeReport(' + curCount + ')">'
                html = html + '<fmt:message key="remove.report"/>' + '</a></td>';
            }
            html = html + '</tr>';
            d.innerHTML = d.innerHTML + html;
            reportIndex = reportIndex + count + ',';
        }


        function removeReport(curCount) {
            var parent = document.getElementById('contentTableBody');
            var child = document.getElementById('report_tr_' + curCount);
            parent.removeChild(child);
            var replaceStr = '' + curCount + ',';
            reportIndex = reportIndex.replace(replaceStr, '');
        }

        function getTemplateComboBox() {
            var html = '<select id="report_' + count + '" name="report_' + count + '">';
            for (i = 0; i < reports.length; i++) {
                var opt = '<option value="' + reports[i] + '">' + reports[i] + '</option>';
                html = html + opt;
            }
            html = html + '</select>';
            return html;
        }

        function cancelTableData() {
            location.href = "../reporting_custom/select-report.jsp";
        }

        function submitCompositeReport() {
            document.compositeReport.action = 'CompositeReportProcessor';

            var msg = '';
            var reportName = document.getElementById("reportName").value;
            if (reportName == '') {
                msg = 'Please enter a report name.\n';
                jQuery(document).init(function () {
                    CARBON.showErrorDialog(msg);
                });
                return false;
            }
            reportIndex = reportIndex.substring(0, reportIndex.length - 1);
            document.getElementById('noReports').value = reportIndex;
            document.compositeReport.submit();
            return true;
        }


        $(window).bind("load", function() {
            addReport();
        });

    </script>

    <div id="middle">
        <h2>Add Composite Report</h2>

        <div id="workArea">

            <form id="compositeReport" name="compositeReport" action="CompositeReportProcessor" method="POST">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="composite.report.information"/></span>
                        </th>
                    </tr>
                    </thead>
                    <tbody>


                    <tr>
                        <td>
                            <table class="normal-nopadding">
                                <tbody id="contentTableBody">

                                <tr>
                                    <td width="180px"><fmt:message key="report.name"/> <span
                                            class="required">*</span></td>
                                    <%
                                        String tempReportName = request.getParameter("reportName");
                                        if (tempReportName == null) {
                                            tempReportName = "";
                                        }
                                    %>
                                    <td><input name="reportName"
                                               id="reportName" value="<%=tempReportName%>"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td><a style="background-image: url(../admin/images/add.gif);"
                                           class="icon-link spacer-bot" onclick="addReport()"><fmt:message
                                            key="add.sub.report"/></a></td>
                                </tr>
                                <input type="hidden" name="noReports" id="noReports"/>
                                </tbody>
                            </table>

                        </td>
                    </tr>

                    </tbody>
                </table>


                <table class="normal-nopadding">
                    <tbody>

                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input type="button" value="<fmt:message key="create"/>"
                                   class="button" name="save"
                                   onclick="submitCompositeReport();"/>
                            <input type="button" value="<fmt:message key="cancel"/>"
                                   name="cancel" class="button"
                                   onclick="cancelTableData();"/>
                        </td>
                    </tr>
                    </tbody>

                </table>
            </form>
        </div>
    </div>


</fmt:bundle>