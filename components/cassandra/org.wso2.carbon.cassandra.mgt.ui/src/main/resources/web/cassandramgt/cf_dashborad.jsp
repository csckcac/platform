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
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.cluster.xsd.ColumnFamilyStats" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.ColumnFamilyInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.ColumnInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.KeyspaceInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientConstants" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientHelper" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraClusterAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<script type="text/javascript" src="js/cassandra_ui_util.js"></script>
<%
    response.setHeader("Cache-Control", "no-cache");
    String keyspace = request.getParameter("keyspace");
    String columnFamily = request.getParameter("columnFamily");
    ColumnFamilyInformation cfInformation = null;
    ColumnFamilyStats columnFamilyStats = null;
    String clusterName = null;

    try {
        KeyspaceInformation keyspaceInformation =
                CassandraAdminClientHelper.getKeyspaceInformation(config.getServletContext(), session, keyspace);
        cfInformation = CassandraAdminClientHelper.getColumnFamilyInformationOfCurrentUser(keyspaceInformation, columnFamily);
        CassandraClusterAdminClient cassandraClusterAdminClient =
                new CassandraClusterAdminClient(config.getServletContext(), session);
        columnFamilyStats = cassandraClusterAdminClient.getColumnFamilyStats(keyspace, columnFamily);
        clusterName = CassandraAdminClientHelper.getClusterName(config.getServletContext(), session);

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
<fmt:bundle basename="org.wso2.carbon.cassandra.mgt.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.cassandra.mgt.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="cassandrajsi18n"/>
<carbon:breadcrumb
        label="cassandra.cf"
        resourceBundle="org.wso2.carbon.cassandra.mgt.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<% if (cfInformation != null) {
    boolean isSuperCl = CassandraAdminClientConstants.COLUMN_TYPE_SUPER.equals(cfInformation.getType());
    String comparator = CassandraAdminClientHelper.getAliasForComparatorTypeClass(cfInformation.getComparatorType());
    String subComparator = CassandraAdminClientHelper.getAliasForComparatorTypeClass(cfInformation.getSubComparatorType());
    String validationClass =
            CassandraAdminClientHelper.getAliasForComparatorTypeClass(cfInformation.getDefaultValidationClass());
%>
<div id="middle">
<h2><fmt:message key="cassandra.cf.dashboard"/> ( <%=clusterName%> > <%=keyspace%>
                                                > <%=columnFamily%>) </h2>

<div id="workArea">

<table width="100%" cellspacing="0" cellpadding="0" border="0">
<tr>
<td width="50%">
    <table class="styledLeft" id="cfInfoTable" style="margin-left: 0px;" width="100%">
        <thead>
        <tr>
            <th colspan="2" align="left"><fmt:message key="cassandra.cf.details"/></th>
        </tr>
        </thead>
        <tr>
            <td><fmt:message key="cassandra.field.name"/></td>
            <td align="left"><%=cfInformation.getName()%>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="cassandra.field.comment"/></td>
            <td align="left"><%=cfInformation.getComment()%>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="cassandra.field.columnType"/></td>
            <td align="left">
                <% if (isSuperCl) {%>
                <fmt:message key="cassandra.field.columnType.super"/>
                <% } else { %>
                <fmt:message
                        key="cassandra.field.columnType.standard"/>
                <% } %>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="cassandra.field.comparator"/></td>
            <td align="left">
                <% if (CassandraAdminClientConstants.ASCIITYPE.equals(comparator)) {%>
                <fmt:message
                        key="cassandra.field.comparator.ascii"/>
                <% } else if (CassandraAdminClientConstants.UTF8TYPE.equals(comparator)) {%>
                <fmt:message
                        key="cassandra.field.comparator.utf8"/>
                <% } else if (CassandraAdminClientConstants.LEXICALUUIDTYPE.equals(comparator)) {%>

                <fmt:message
                        key="cassandra.field.comparator.lexicalUUID"/>
                <% } else if (CassandraAdminClientConstants.TIMEUUIDTYPE.equals(comparator)) {%>
                <fmt:message
                        key="cassandra.field.comparator.timeUUID"/>
                <% } else if (CassandraAdminClientConstants.LONGTYPE.equals(comparator)) {%>
                <fmt:message
                        key="cassandra.field.comparator.long"/>
                <% } else if (CassandraAdminClientConstants.INTEGERTYPE.equals(comparator)) {%>
                <fmt:message
                        key="cassandra.field.comparator.integer"/>
                <% } else {%>
                <fmt:message
                        key="cassandra.field.comparator.bytes"/>
                <% }%>
            </td>
        </tr>
        <tr id="sub_column_comparator_row" style="<%=isSuperCl?"":"display:none;"%>">
            <td><fmt:message key="cassandra.field.subComparator"/></td>
            <td align="left">
                <% if (CassandraAdminClientConstants.ASCIITYPE.equals(subComparator)) {%>
                <fmt:message
                        key="cassandra.field.comparator.ascii"/>
                <% } else if (CassandraAdminClientConstants.UTF8TYPE.equals(subComparator)) {%>
                <fmt:message
                        key="cassandra.field.comparator.utf8"/>
                <% } else if (CassandraAdminClientConstants.LEXICALUUIDTYPE.equals(subComparator)) {%>

                <fmt:message
                        key="cassandra.field.comparator.lexicalUUID"/>
                <% } else if (CassandraAdminClientConstants.TIMEUUIDTYPE.equals(subComparator)) {%>
                <fmt:message
                        key="cassandra.field.comparator.timeUUID"/>
                <% } else if (CassandraAdminClientConstants.LONGTYPE.equals(subComparator)) {%>
                <fmt:message
                        key="cassandra.field.comparator.long"/>
                <% } else if (CassandraAdminClientConstants.INTEGERTYPE.equals(subComparator)) {%>
                <fmt:message
                        key="cassandra.field.comparator.integer"/>
                <% } else {%>
                <fmt:message
                        key="cassandra.field.comparator.bytes"/>
                <% }%>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="cassandra.field.keyCacheSize"/></td>
            <td align="left"><%=cfInformation.getKeyCacheSize()%>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="cassandra.field.rowCacheSize"/></td>
            <td align="left"><%=cfInformation.getRowCacheSize()%>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="cassandra.field.rowcachetime"/></td>
            <td align="left"><%=cfInformation.getRowCacheSavePeriodInSeconds()%>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="cassandra.field.gcgrace"/></td>
            <td align="left"><%=cfInformation.getGcGraceSeconds()%>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="cassandra.field.minThreshold"/></td>
            <td align="left"><%=cfInformation.getMinCompactionThreshold()%>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="cassandra.field.maxThreshold"/></td>
            <td align="left"><%=cfInformation.getMaxCompactionThreshold()%>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="cassandra.field.defaultValidationclass"/></td>
            <td align="left">
                <% if (CassandraAdminClientConstants.ASCIITYPE.equals(validationClass)) {%>
                <fmt:message
                        key="cassandra.field.comparator.ascii"/>
                <% } else if (CassandraAdminClientConstants.UTF8TYPE.equals(validationClass)) {%>
                ><fmt:message
                    key="cassandra.field.comparator.utf8"/>
                <% } else if (CassandraAdminClientConstants.LEXICALUUIDTYPE.equals(validationClass)) {%>

                <fmt:message
                        key="cassandra.field.comparator.lexicalUUID"/>
                <% } else if (CassandraAdminClientConstants.TIMEUUIDTYPE.equals(validationClass)) {%>
                <fmt:message
                        key="cassandra.field.comparator.timeUUID"/>
                <% } else if (CassandraAdminClientConstants.LONGTYPE.equals(validationClass)) {%>
                <fmt:message
                        key="cassandra.field.comparator.long"/>
                <% } else if (CassandraAdminClientConstants.INTEGERTYPE.equals(validationClass)) {%>
                <fmt:message
                        key="cassandra.field.comparator.integer"/>
                <% } else {%>

                <fmt:message
                        key="cassandra.field.comparator.bytes"/>
                <% }%>
            </td>
        </tr>
    </table>
</td>
<%

    if (columnFamilyStats != null) {
%>
<td width="10px">&nbsp;</td>
<td>

    <div id="cfstatsTableDIv">
        <table class="styledLeft" id="cfstatsTable"
               style="margin-left: 0px;" width="100%">
            <thead>
            <tr>
                <th colspan="2">
                    <fmt:message key="cassandra.cf.stats"/>
                </th>
            </tr>
            </thead>
            <tr>
                <td>
                    <fmt:message key="cassandra.cf.stats.liveSSTableCount"/>
                </td>
                <td>
                    <%=columnFamilyStats.getLiveSSTableCount()%>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="cassandra.cf.stats.liveDiskSpaceUsed"/>
                </td>
                <td>
                    <%=columnFamilyStats.getLiveDiskSpaceUsed()%>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="cassandra.cf.stats.totalDiskSpaceUsed"/>
                </td>
                <td>
                    <%=columnFamilyStats.getTotalDiskSpaceUsed()%>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="cassandra.cf.stats.memtableColumnsCount"/>
                </td>
                <td>
                    <%=columnFamilyStats.getMemtableColumnsCount()%>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="cassandra.cf.stats.memtableDataSize"/>
                </td>
                <td>
                    <%=columnFamilyStats.getMemtableDataSize()%>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="cassandra.cf.stats.memtableSwitchCount"/>
                </td>
                <td>
                    <%=columnFamilyStats.getMemtableSwitchCount()%>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="cassandra.cf.stats.readCount"/>
                </td>
                <td>
                    <%=columnFamilyStats.getReadCount()%>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="cassandra.cf.stats.readLatency"/>
                </td>
                <td>
                    <% double readLatency = columnFamilyStats.getReadLatency();
                        if (Double.isNaN(readLatency)) {
                    %>-<%
                } else {
                %> <%=readLatency%>
                    <%} %>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="cassandra.cf.stats.writeCount"/>
                </td>
                <td>
                    <%=columnFamilyStats.getWriteCount()%>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="cassandra.cf.stats.writeLatency"/>
                </td>
                <td>
                    <% double writeLatency = columnFamilyStats.getWriteLatency();
                        if (Double.isNaN(writeLatency)) {
                    %>-<%
                } else {
                %> <%=writeLatency%>
                    <%} %>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="cassandra.cf.stats.pendingTasks"/>
                </td>
                <td>
                    <%=columnFamilyStats.getPendingTasks()%>
                </td>
            </tr>
        </table>
    </div>
</td>
<% } %>
</tr>
<tr>
    <td colspan="3">&nbsp;</td>
</tr>
<tr>
    <%
        ColumnInformation[] columns = cfInformation.getColumns();
        String clTableDisplay = "display:none;";
        if (columns != null && columns.length != 0) {
            clTableDisplay = "";
        }
    %>

    <td width="50%">
        <div id="serviceClientDiv" style="<%=clTableDisplay%>">
            <table class="styledLeft" id="clTable" style="margin-left: 0px;" width="100%">
                <thead>
                <tr>
                    <th width="20%"><fmt:message key="cassandra.cl.name"/></th>
                    <th width="60%"><fmt:message key="cassandra.actions"/></th>
                </tr>
                </thead>
                <tbody id="clBody">
                <%
                    int j = 0;
                    if (columns != null && columns.length != 0) {
                        for (; j < columns.length; j++) {
                            ColumnInformation column = columns[j];
                            String name = column.getName();
                %>
                <tr id="clRaw<%=j%>">
                    <td id="clTD<%=j%>"><%=name%>
                    </td>
                    <td>
                        <input type="hidden" name="clName<%=j%>" id="clName<%=j%>"
                               value="<%=name%>"/>
                        <a class="edit-icon-link"
                           onclick="showCLEditor('edit','<%=name%>','<%=columnFamily%>','<%=keyspace%>');"
                           href="#"><fmt:message
                                key="cassandra.actions.edit"/></a>
                        <a class="delete-icon-link"
                           onclick="deleteCL('<%=columnFamily%>','<%=j%>');"
                           href="#"><fmt:message
                                key="cassandra.actions.delete"/></a>
                    </td>
                </tr>
                <%
                        }
                    }
                %>
                <input type="hidden" name="clCount" id="clCount" value="<%=j%>"/>
                </tbody>

            </table>
        </div>
        <div style="margin-top:0px;">
            <a class="add-icon-link" onclick="addCL('<%=columnFamily%>','<%=keyspace%>');" href="#">
                <fmt:message key="cassandra.new.cl"/></a>
        </div>
    </td>
</tr>
</table>
<script type="text/javascript">
    alternateTableRows('cfInfoTable', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('cfstatsTable', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('clTable', 'tableEvenRow', 'tableOddRow');
</script>
</div>
</div>
<% }%>
</fmt:bundle>

