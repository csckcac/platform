<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.wso2.carbon.bam.index.stub.service.types.IndexDTO" %>
<%@ page import="org.wso2.carbon.bam.analyzer.ui.IndexAdminClient" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.bam.index.stub.service.types.TableDTO" %>
<!--
~ Copyright 2010 WSO2, Inc. (http://wso2.com)
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

<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<%

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    IndexAdminClient client = new IndexAdminClient(cookie, serverURL, configContext);

    String tableName = request.getParameter("tableName");
    String dataSourceType = request.getParameter("dataSourceType");
    String columns = request.getParameter("columns");

    TableDTO table = new TableDTO();
    table.setTableName(tableName);
    table.setDataSourceType(dataSourceType);
    table.setColumns(new String[]{columns});

    try {
        client.createTable(table);
    } catch (AxisFault e) {
        String errorString = "Failed to create table.";
%>
<script type="text/javascript">
    jQuery(document).init(function () {
        CARBON.showErrorDialog('<%=errorString%>', function () {
            location.href = "data-config.jsp";
        });
    });
</script>

<%
        return;
    }
%>

<script type="text/javascript">
    jQuery(document).ready(function () {
        location.href = "data-config.jsp";
    });
</script>
