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
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.reporting.template.ui.i18n.Resources">
<script type="text/javascript">


</script>

<script type="text/javascript">

    function submitTableReportData() {
        document.tablereport.action = 'table-data-save.jsp';
        var reportName = document.getElementById("reportName").value;
        if (reportName == '') {
            alert('Please enter report name');
            return false;
        }

        var counter = 0,
                i = 0,
                fieldsStr = '',
                input_obj = document.getElementsByTagName('input');

        for (i = 0; i < input_obj.length; i++) {
            if (input_obj[i].type === 'checkbox' && input_obj[i].checked === true) {
                counter++;
                fieldsStr = fieldsStr + ',' + input_obj[i].value;
            }
        }
        if (counter > 0) {
            fieldsStr = fieldsStr.substr(1);
        }
        else {
            alert('There is no checked checkbox');
            return false;
        }
        document.getElementById('selectedFields').value = fieldsStr;

        document.tablereport.submit();
        return true;
    }

    function cancelTableData() {
        location.href = "../reporting_custom/select-report.jsp";
    }

    function dsChanged(dsName) {
        document.tablereport.action = 'add-table-report.jsp';
        var reportName = document.getElementById("reportName").value;
        var url = 'add-table-report.jsp?selectedDsName=' + dsName + '&reportName=' + reportName;
        location.href = url;
    }

    function tableChanged(dsName, tableName) {
        document.tablereport.action = 'add-table-report.jsp';
        var reportName = document.getElementById("reportName").value;
        var url = 'add-table-report.jsp?selectedDsName=' + dsName + '&selectedTableName=' + tableName + '&reportName=' + reportName;
        location.href = url;
    }

</script>

<%

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    ReportTemplateClient client;
    String message = "";
    String[] datasources = null;
    String errorString = "";
    String dsName = "";
    String tableName = "";
    try {
        client = new ReportTemplateClient(configContext, serverURL, cookie);
        datasources = client.getDatasourceNames();
        if (datasources == null) {
            errorString = "No data source found! Please add a data source!";
%>
<script type="text/javascript">
    CARBON.showConfirmationDialog(msg + "' " + name + " ' ?", function() {
        document.location.href = "delete-template.jsp?" + "reportName=" + name;
    });
    jQuery(document).init(function () {
        CARBON.showErrorDialog('<%=errorString%>', function() {
            document.location.href = "../datasource/index.jsp?region=region1&item=datasource_menu";
        });
    });
    // document.getElementById('save').disabled = true;
</script>

<% }
} catch (Exception e) {
    errorString = e.getMessage();
    CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
    alert(<%=errorString%>);
</script>
<%
        return;
    }
%>

<div id="middle">
<h2>Add Table Report - Step 1</h2>

<div id="workArea">

<form id="tablereport" name="tablereport" action="" method="POST">
<table class="styledLeft">
    <thead>
    <tr>
        <th><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="table.report.information"/></span>
        </th>
    </tr>
    </thead>
    <tbody>


    <tr>
        <td>
            <table class="normal-nopadding">
                <tbody>

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
                    <td class="leftCol-small"><fmt:message key="datasource.name"/><span
                            class="required"> *</span>
                    </td>
                    <td>
                        <% if (datasources != null && datasources.length > 0) {
                            dsName = request.getParameter("selectedDsName");
                            if (dsName == null || dsName.equals("")) {
                                dsName = datasources[0];
                            }
                        %>
                        </thead>
                        <select id="datasource" name="datasource">
                            <%
                                for (int i = 0; i < datasources.length; i++) {
                                    String datasource = datasources[i];

                            %>
                            <option value="<%=datasource%>" <%=datasource.equalsIgnoreCase(dsName) ? "selected=\"selected\"" : ""%>
                                    onclick="dsChanged('<%=datasource%>')">
                                <%=datasource%>
                            </option>
                            <% }%>
                        </select>

                        <% } else {
                            errorString = "No datasource found";
                        %>
                        <script type="text/javascript">
                            jQuery(document).init(function () {
                                CARBON.showErrorDialog('<%=errorString%>');
                            });
                        </script>
                        <% } %>
                    </td>
                </tr>

                <tr>
                    <td class="leftCol-small"><fmt:message key="table.name"/><span
                            class="required"> *</span>
                    </td>
                    <td>
                        <% String[] tableNames = null;
                            try {
                                tableNames = client.getTableNames(dsName);
                            } catch (AxisFault e) {
                                errorString = e.getMessage();
                            }
                            if (tableNames != null && tableNames.length > 0) { %>

                        <select id="tableName" name="tableName">
                            <%
                                tableName = request.getParameter("selectedTableName");
                                if (tableName == null || tableName.equals("")) {
                                    tableName = tableNames[0];
                                }
                                for (int i = 0; i < tableNames.length; i++) {
                                    String aTableName = tableNames[i];

                            %>
                            <option value="<%=aTableName%>" <%= aTableName.equalsIgnoreCase(tableName) ? "selected=\"selected\"" : ""%>
                                    onclick="tableChanged('<%=dsName%>', '<%=aTableName%>')">
                                <%=aTableName%>
                            </option>
                            <% }%>
                        </select>

                        <% } else {
                            errorString = "No Tables found in datasource " + dsName;
                        %>
                        <script type="text/javascript">
                            jQuery(document).init(function () {
                                CARBON.showErrorDialog('<%=errorString%>');
                            });
                        </script>
                        <% } %>
                    </td>
                </tr>

                <tr>
                    <td class="leftCol-small"><fmt:message key="field.names"/><span
                            class="required"> *</span>
                    </td>
                    <td>
                        <% String[] fieldNames = null;
                            try {
                                fieldNames = client.getFieldNames(dsName, tableName);
                            } catch (AxisFault e) {
                                errorString = e.getMessage();
                            }
                            if (fieldNames != null && fieldNames.length > 0) {
                                for (int i = 0; i < fieldNames.length; i++) {

                                    String aField = fieldNames[i];
                        %>
                        <input type="checkbox" name="<%=aField%>"
                               value="<%=aField%>" <%=i == 0 ? "checked" : ""%>/><%=aField%><br/>

                        <% }
                        } else {
                            errorString = "No fields found in table " + tableName;
                        %>
                        <script type="text/javascript">
                            jQuery(document).init(function () {
                                CARBON.showErrorDialog('<%=errorString%>');
                            });
                        </script>
                        <% } %>
                    </td>

                </tr>

                <input id="selectedFields" name="selectedFields" type="hidden"/>
                <input id="selectedDsName" name="selectedDsName" type="hidden"/>
                <input id="selectedTableName" name="selectedTableName" type="hidden"/>
                <tr>
                    <td class="leftCol-small"><fmt:message key="primary.field"/><span
                            class="required"> *</span>
                    </td>
                    <td>
                        <%
                            if (fieldNames != null && fieldNames.length > 0) {
                        %>
                        <select id="primaryField" name="primaryField">
                                    <%
                            for (int i = 0; i < fieldNames.length; i++) {

                                String aField = fieldNames[i];
                    %>
                            <option value="<%=aField%>" <%=i == 0 ? "selected=\"selected\"" : ""%>
                                    onclick="processTableMetaData('<%=aField%>')">
                                <%=aField%>
                            </option>

                                    <% }
                    } else {
                        errorString = "No fields found in table "+ tableName;
                    %>
                            <script type="text/javascript">
                                jQuery(document).init(function () {
                                    CARBON.showErrorDialog('<%=errorString%>');
                                });
                            </script>
                                    <% } %>
                    </td>
                </tr>

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
            <input type="button" value="<fmt:message key="next"/>"
                   class="button" name="save" id="save"
                   onclick="submitTableReportData();"/>
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