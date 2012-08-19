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
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.CassandraClusterToolsKeyspaceAdminClient" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<script type="text/javascript" src="js/clusterTools_ui.js"></script>
<%
    String keyspace=request.getParameter("keyspace");
    String tableStyle="display:none;";
    String[] columnFamilyNames = null;
    boolean isColumnFamilyEmpty=true;
    CassandraClusterToolsKeyspaceAdminClient cassandraClusterToolsKeyspaceAdminClient=new CassandraClusterToolsKeyspaceAdminClient(config.getServletContext(), session);
    try{
    columnFamilyNames= cassandraClusterToolsKeyspaceAdminClient.listColumnFamiliesOfCurrentUser(keyspace);
        if (columnFamilyNames != null && columnFamilyNames.length > 0) {
            tableStyle="";
        }
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
<fmt:bundle basename="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.Resources">
    <carbon:jsi18n
    resourceBundle="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.JSResources"
    request="<%=request%>" i18nObjectName="cassandrajsi18n"/>
    <carbon:breadcrumb
    label="cassandra.cluster.tools.column.family.operaions"
    resourceBundle="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.Resources"
    topPage="false"
    request="<%=request%>"/>

<div id="middle">
    <h2><fmt:message key="cassandra.cluster.tools.column.family.operaions.msg"/> </h2>

    <div id="workArea">
<table class="styledLeft" id="columnFamilyOperationTable" style="<%=tableStyle%>">
                <thead>
                <tr>
                    <th><fmt:message key="cassandra.cluster.tools.column.family.operations.table.header.keysace"/> </th>
                    <th><fmt:message key="cassandra.cluster.tools.column.family.operations.table.header.column.family"/></th>
                    <th><fmt:message key="cassandra.cluster.tools.column.family.operations.table.header.operations"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td><%=keyspace%></td>
                <td>
                <select name="column_family" id="column_family">
                <%
                   for(String columnFamily:columnFamilyNames)
                   {
                %>
                      <option value="<%=columnFamily%>"><%=columnFamily%></option>

                   <%}%>
                </select>
                </td>
                    <td>
                        <a href="#" onclick="repairColumnFamily('<%=keyspace%>')" class="icon-link" style="background-image:url(images/repair.png);">Repair</a>
                        <a href="#" onclick="compactColumnFamily('<%=keyspace%>')" class="icon-link" style="background-image:url(images/compact.png);">Compact</a>
                        <a href="#" onclick="flushColumnFamily('<%=keyspace%>')" class="icon-link" style="background-image:url(images/flush.png);">Flush</a>
                        <a href="#" onclick="cleanUpColumnFamily('<%=keyspace%>')" class="icon-link" style="background-image:url(images/cleanUp.png);">Cleanup</a>
                        <a href="#" onclick="scrubColumnFamily('<%=keyspace%>')" class="icon-link" style="background-image:url(images/scrub.jpg);">Scrub</a>
                        <a href="#" onclick="upgradeSSTablesColumnFamily('<%=keyspace%>')" class="icon-link" style="background-image:url(images/upgrade.jpg);">UpgradeSSTables</a>
                    </td>
            </table>
 </div>
  </div>
    </fmt:bundle>