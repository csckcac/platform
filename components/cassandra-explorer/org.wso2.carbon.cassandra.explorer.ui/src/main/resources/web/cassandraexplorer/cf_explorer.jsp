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

<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.cassandra.explorer.ui.CassandraExplorerAdminClient" %>
<%@ page import="org.wso2.carbon.cassandra.explorer.stub.data.xsd.Column" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<script type="text/javascript" src="js/cassandra_ui_util.js"></script>
<%
    response.setHeader("Cache-Control", "no-cache");
    String keyspace = request.getParameter("keyspace");
    String columnFamily = request.getParameter("columnFamily");
    String rowID = request.getParameter("rowID");
    String startRowKey = request.getParameter("startRowKey");
    String navigationDirection = request.getParameter("naviDirection");
    String pageSize = request.getParameter("pageSize");
    CassandraExplorerAdminClient cassandraExplorerAdminClient = null;

    try {
        cassandraExplorerAdminClient =
                new CassandraExplorerAdminClient(config.getServletContext(), session);

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
<%--TODO refactor JSI bundle--%>
<fmt:bundle basename="org.wso2.carbon.cassandra.explorer.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.cassandra.explorer.ui.i18n.Resources"
        request="<%=request%>" i18nObjectName="cassandrajsi18n"/>
<carbon:breadcrumb
        label="cassandra.cf"
        resourceBundle="org.wso2.carbon.cassandra.explorer.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<style type="text/css">
    .disabled {
        color: #CCCCCC;
        cursor: default;
    }

    a.disabled:hover {
        color: #CCCCCC;
    }

    .enabled {
        color: #2F7ABD;
        cursor: pointer;
    }
</style>

<script type="text/javascript" src="js/json2/json2.js"></script>
<script type="text/javascript" src="js/cassandra_cf_explorer.js"></script>
<script type="text/javascript">
    function updateButtonStatus(rowID) {
        var keySpace = "<%=keyspace%>";
        var columnFamily = "<%=columnFamily%>";
        var startKey;
        var endKey;
        var isReversed;

        startKey = document.getElementById("hfEndKey_" + rowID).value;
        endKey = "";
        isReversed = "false";
        updateNextButtonStatusWithAJAX(keySpace, columnFamily, rowID, startKey, endKey, isReversed);

        startKey = "";
        endKey = document.getElementById("hfStartKey_" + rowID).value;
        isReversed = "false";
        updatePrevButtonStatusWithAJAX(keySpace, columnFamily, rowID, startKey, endKey, isReversed);
    }
</script>
<div id="middle">
<table>
    <tr>
        <td>Page Size:</td>
        <td width="40%">
        <select id="ddlPageSize" onchange="loadPageSize('<%=keyspace%>','<%=columnFamily%>');">
            <option>10</option>
            <option>25</option>
            <option>50</option>
            <option>100</option>
        </select>
    </td>
        <form>
            <td>Row Key:</td>
            <td><input type="text" name="rowid" id="rowid"/></td>
            <td><input type="button" value="Explore Row "
                       onclick="getDataForRow('<%=keyspace%>',
                               '<%=columnFamily%>',document.getElementById('rowid').value);"/></td>
        </form>
    </tr>
</table>

<div id="workArea">
<%
    String[] rows = new String[0];
    String[] previousRows = new String[0];
    String[] nextRows = new String[0];
    boolean previousRowsExists = true;
    boolean nextRowsExists = true;

    //if a row ID has been inserted to field
    if (rowID != null) {
        rows = cassandraExplorerAdminClient.getRows(keyspace, columnFamily, rowID, rowID, 50);
        if (rows == null || rows.length == 0) {
%>
<script type="text/javascript">
    jQuery(document).ready(function () {
        CARBON.showInfoDialog('No Results found', function () {
            CARBON.closeWindow();
            reloadDataTable('<%=keyspace%>', '<%=columnFamily%>');
        }, function () {
            CARBON.closeWindow();
            reloadDataTable('<%=keyspace%>', '<%=columnFamily%>');
        });
    });
</script>
<%
        }
        //check if user is paginating
    } else if (startRowKey != null && navigationDirection != null) {
        if ("next".equals(navigationDirection)) {
            rows = cassandraExplorerAdminClient.getRows(keyspace, columnFamily, startRowKey, "",
                    Integer.parseInt(pageSize));
            if (rows != null) {
                previousRows = cassandraExplorerAdminClient
                        .getRows(keyspace, columnFamily, "", rows[0], 2);
                nextRows = cassandraExplorerAdminClient.getRows(keyspace, columnFamily,
                        rows[rows.length - 1], "", 2);

                if (previousRows != null && previousRows.length > 1) {
                    previousRowsExists = true;
                } else {
                    previousRowsExists = false;
                }
                if (nextRows != null && nextRows.length > 1) {
                    nextRowsExists = true;
                } else {
                    nextRowsExists = false;
                }
            }
        } else if ("prev".equals(navigationDirection)) {
            rows = cassandraExplorerAdminClient.getRows(keyspace, columnFamily, "", startRowKey,
                    Integer.parseInt(pageSize));
            if (rows != null) {
                previousRows = cassandraExplorerAdminClient
                        .getRows(keyspace, columnFamily, "", rows[0], 2);
                nextRows = cassandraExplorerAdminClient.getRows(keyspace, columnFamily,
                        rows[rows.length - 1], "", 2);

                if (previousRows!= null && previousRows.length > 1) {
                    previousRowsExists = true;
                } else {
                    previousRowsExists = false;
                }
                if (nextRows!= null && nextRows.length > 1) {
                    nextRowsExists = true;
                } else {
                    nextRowsExists = false;
                }
            }
        }
        // loading intially or reloading data table.
    } else {
        int intPageSize;
        if (pageSize != null) {
            intPageSize = Integer.parseInt(pageSize);
        } else {
            intPageSize = 10;
        }
        rows = cassandraExplorerAdminClient.getRows(keyspace, columnFamily, "", "", intPageSize);
        if (rows != null) {
            previousRows = cassandraExplorerAdminClient
                    .getRows(keyspace, columnFamily, "", rows[0], 2);
            nextRows = cassandraExplorerAdminClient
                    .getRows(keyspace, columnFamily, rows[rows.length - 1], "", 2);

            if (previousRows != null && previousRows.length > 1) {
                previousRowsExists = true;
            } else {
                previousRowsExists = false;
            }
            if (nextRows != null && nextRows.length > 1) {
                nextRowsExists = true;
            } else {
                nextRowsExists = false;
            }
        }
    }

    if (pageSize != null) {
%>

<script type="text/javascript">
    document.getElementById("ddlPageSize").value = "<%=pageSize%>";
</script>

<%
    }
    if (rows != null) {
%>
<input type="hidden" id="hfStartRowKey" value="<%=rows[0]%>"/>
<input type="hidden" id="hfEndRowKey" value="<%=rows[rows.length-1]%>"/>
<%
    for (int i = 0; i < rows.length; i++) {
        String cfExplorerTableID = "CfExplorerTable_" + rows[i];
        String startKeyId = "hfStartKey_" + rows[i];
        String endKeyId = "hfEndKey_" + rows[i];

%>
<input type="hidden" id="<%=startKeyId%>" value=""/>
<input type="hidden" id="<%=endKeyId%>" value=""/>
<table class="styledLeft" id="<%=cfExplorerTableID%>" width="100%" style="margin-left: 0px;">
    <thead>
    <tr>
      <th width="25%">
        </th>
        <%
            Column[] columns = cassandraExplorerAdminClient.
                    getColumnsForRowName(keyspace, columnFamily, rows[i], "", "", 3, false);
            if (columns != null) {
                String startKey = columns[0].getName();
                String endKey = columns[columns.length - 1].getName();
                for (int k = 0; k < columns.length; k++) {
        %>
        <script type="text/javascript">
            document.getElementById("<%=startKeyId%>").value = "<%=startKey%>";
            document.getElementById("<%=endKeyId%>").value = "<%=endKey%>";
        </script>
        <th><%=columns[k].getName()%>
        </th>
        <% } %>
        <th width="100px"></th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>
            <a onclick="getDataPageForRow('<%=keyspace%>','<%=columnFamily%>','<%= rows[i] %>')"
               class="view-icon-link" href="#">
                <%=rows[i]%>
            </a>
        </td>
        <%
            String currentRowID = rows[i];
            for (int j = 0; j < columns.length; j++) {
        %>
        <td><%=columns[j].getValue()%>
        </td>
        <% }
        %>
        <td>
                    <span>
                        <a class="enabled" id="btnPrevCols"
                           onclick="loadPrevious('<%=currentRowID%>','<%=keyspace%>',
                                   '<%=columnFamily%>');">
                            &lt;Prev </a>
                    </span>
                    <span>
                        <a class="enabled" id="btnNextCols"
                           onclick="loadNext('<%=currentRowID%>','<%=keyspace%>',
                                   '<%=columnFamily%>');">
                            Next&gt;</a>
                    </span>
            <script type="text/javascript">
                updateButtonStatus("<%=currentRowID%>");
            </script>
        </td>
    </tr>
    </tbody>
</table>
<br>
<%
}// here if columns retrun null we display "No data"
else {%>
<th width="75%"></th>
</tr>
</thead>
<tbody>
<td width="25%"><%=rows[i]%>
</td>
<td width="75%">Empty Row</td>
</tbody>
</table>
<br>
<%
        }
    }%>
<table>
    <tr>
        <td><input type="button" id="btnPrevRows" value="&lt;Prev "
                   onclick="loadPreviousRows('<%=keyspace%>','<%=columnFamily%>');"/></td>
        <td><input type="button" id="btnNextRows" value=" Next&gt;"
                   onclick="loadNextRows('<%=keyspace%>','<%=columnFamily%>');"/></td>
        <script type="text/javascript">
            if (<%=!previousRowsExists%>) {
                document.getElementById("btnPrevRows").disabled = true;
            }
            if (<%=!nextRowsExists%>) {
                document.getElementById("btnNextRows").disabled = true;
            }
        </script>
    </tr>
</table>
</div>
</div>
<%
} else {%>

<script type="text/javascript">
    jQuery(document).ready(function () {
        CARBON.showInfoDialog('No Results found', function () {
            CARBON.closeWindow();
        }, function () {
            CARBON.closeWindow();
        });
    });
</script>
<%}%>
</fmt:bundle>

