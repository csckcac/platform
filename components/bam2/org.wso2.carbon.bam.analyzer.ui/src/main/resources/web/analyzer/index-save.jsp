<%@ page import="org.wso2.carbon.bam.index.stub.service.types.IndexDTO" %>
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.bam.analyzer.ui.IndexAdminClient" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
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

    String mode = request.getParameter("mode");

    if (mode.equals("save")) {
        String indexName = request.getParameter("indexName");
        String indexedTable = request.getParameter("table");
        String dataSourceType = request.getParameter("dataSourceType");
        String columns = request.getParameter("indexedColumns");
        String cron = request.getParameter("cron");
        String granularity = request.getParameter("granularity");
        
        if (granularity!= null && granularity.equals("--Index unused--")) {
            granularity = null;
        }

        String[] columnArray = null;
        if (columns != null && !"".equals(columns)) {
            columnArray = columns.split(":");
        }
        
        List<String> columnList = new ArrayList<String>();
        for (String column : columnArray) {
            if (!column.equals("---None---")) {
                columnList.add(column);   
            }
        }

        IndexDTO index = new IndexDTO();
        index.setIndexName(indexName);
        index.setIndexedTable(indexedTable);
        index.setDataSourceType(dataSourceType);
        index.setIndexedColumns(columnList.toArray(new String[]{}));
        index.setCron(cron);
        index.setGranularity(granularity);

        try {
            client.createIndex(index);
        } catch (AxisFault e) {
            String errorString = "Failed to create index.";
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
    } else if (mode.equals("edit")) {
        String indexName = request.getParameter("indexName");
        String cron = request.getParameter("cron");

        IndexDTO index;
        try {
            index = client.getIndex(indexName);
        } catch (AxisFault e) {
            String errorString = "Failed to get index data.";
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

        String dataSourceType = index.getDataSourceType();

        index.setDataSourceType(dataSourceType.split(":")[0]); // Only get data source type name...
        index.setCron(cron); // Modify the indexing frequency

        try {
            client.editIndex(index);
        } catch (AxisFault e) {
            String errorString = "Failed to edit index..";
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

    }
%>

<script type="text/javascript">
    jQuery(document).ready(function () {
        location.href = "data-config.jsp";
    });
</script>