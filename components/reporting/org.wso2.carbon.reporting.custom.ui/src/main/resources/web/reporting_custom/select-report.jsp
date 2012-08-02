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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.reporting.custom.ui.i18n.Resources">
    <script type="text/javascript">


    </script>

    <div id="middle">
        <h2>Select Report Type</h2>

        <div id="workArea">

            <form id="select-type" name="select-type" action="" method="POST">
                <table id="reportOptions" class="styledLeft" cellpadding="2" cellspacing="2">
                    <thead>
                    <tr>
                        <th colspan="3"><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="select.report.type"/></span>
                        </th>
                    </tr>
                    </thead>
                    <tbody>

                    <%
                        boolean isTemplate = true;
                        try {
                            Class.forName("org.wso2.carbon.reporting.template.ui.client.ReportTemplateClient");
                        } catch (ClassNotFoundException e) {
                            isTemplate = false;
                        }
                        if (isTemplate) {
                    %>
                    <tr>
                        <td colspan="2"><label><a href="../reporting-template/add-table-report.jsp"><fmt:message
                                key="table.type.report"/></a></label>
                        </td>
                        <td>
                            You can obtain a Table type of report easily from the default template provided.
                            This will auto generate the .jrxml file for your report.
                        </td>
                    </tr>

                    <tr>
                        <td colspan="2"><label><a
                                href="../reporting-template/add-chart-report.jsp?reportType=bar_chart_type_report"><fmt:message
                                key="bar.chart.type"/></a></label>
                        </td>
                        <td>
                            You can obtain a Bar Chart type of report easily from the default template provided.
                            This will auto generate the .jrxml file for your report.
                        </td>
                    </tr>

                     <tr>
                        <td colspan="2"><label><a
                                href="../reporting-template/add-chart-report.jsp?reportType=xy_bar_chart_type_report"><fmt:message
                                key="xy.bar.chart.report"/></a></label>
                        </td>
                        <td>
                            You can obtain a XY Bar Chart type of report easily from the default template provided.
                            This will auto generate the .jrxml file for your report.
                        </td>
                    </tr>

                     <tr>
                        <td colspan="2">
                            <label><a
                                    href="../reporting-template/add-chart-report.jsp?reportType=stacked_bar_chart_type_report"><fmt:message
                                    key="stacked.bar.chart.type"/></a></label>
                        </td>
                        <td>
                            You can obtain a Stacked Bar Chart type of report easily from the default template provided.
                            This will auto generate the .jrxml file for your report.
                        </td>
                    </tr>



                    <tr>
                        <td colspan="2"><label><a
                                href="../reporting-template/add-chart-report.jsp?reportType=line_chart_type_report"><fmt:message
                                key="line.chart.type"/></a></label>
                        </td>
                        <td>
                            You can obtain a Line Chart type of report easily from the default template provided.
                            This will auto generate the .jrxml file for your report.
                        </td>
                    </tr>

                     <tr>
                        <td colspan="2"><label><a
                                href="../reporting-template/add-chart-report.jsp?reportType=xy_line_chart_type_report"><fmt:message
                                key="xy.line.chart.report"/></a></label>
                        </td>
                        <td>
                            You can obtain a XY Line Chart type of report easily from the default template provided.
                            This will auto generate the .jrxml file for your report.
                        </td>
                    </tr>

                    <tr>
                        <td colspan="2"><label><a
                                href="../reporting-template/add-chart-report.jsp?reportType=area_chart_type_report"><fmt:message
                                key="area.chart.type"/></a></label>
                        </td>
                        <td>
                            You can obtain a Area Chart type of report easily from the default template provided.
                            This will auto generate the .jrxml file for your report.
                        </td>
                    </tr>



                    <tr>
                        <td colspan="2"><label><a
                                href="../reporting-template/add-chart-report.jsp?reportType=xy_area_chart_type_report"><fmt:message
                                key="xy.area.chart.report"/></a></label>
                        </td>
                        <td>
                            You can obtain a XY Area Chart type of report easily from the default template provided.
                            This will auto generate the .jrxml file for your report.
                        </td>
                    </tr>

                    <tr>
                        <td colspan="2">
                            <label><a
                                    href="../reporting-template/add-chart-report.jsp?reportType=stacked_area_chart_type_report"><fmt:message
                                    key="stacked.area.chart.type"/></a></label>
                        </td>
                        <td>
                            You can obtain a Stacked Area Chart type of report easily from the default template
                            provided.
                            This will auto generate the .jrxml file for your report.
                        </td>
                    </tr>


                    <tr>
                        <td colspan="2"><label><a
                                href="../reporting-template/add-chart-report.jsp?reportType=pie_chart_type_report"><fmt:message
                                key="pie.chart.type"/></a></label>
                        </td>
                        <td>
                            You can obtain a Pie Chart type of report easily from the default template provided.
                            This will auto generate the .jrxml file for your report.
                        </td>
                    </tr>

                    <tr>
                        <td colspan="2"><label><a href="../reporting-template/add-composite-report.jsp"><fmt:message
                                key="composite.report"/></a></label>
                        </td>
                        <td>
                            You can obtain form a master report of composing your selected reports you have defined and
                            produce a combination of report formats mentioned above.
                        </td>
                    </tr>

                    <%
                        }
                    %>
                    <tr>
                        <td colspan="2" width="180px"><label><a href="upload-jrxml.jsp"><fmt:message
                                key="custom.report"/></a></label></td>
                        <td>You can upload your custom defined .jrxml file here and generate report as defined in your
                            .jrxml.
                        </td>
                    </tr>

                    </tbody>
                </table>

            </form>
        </div>
    </div>
    <script type="text/javascript">
        alternateTableRows('reportOptions', 'tableEvenRow', 'tableOddRow');
    </script>

</fmt:bundle>