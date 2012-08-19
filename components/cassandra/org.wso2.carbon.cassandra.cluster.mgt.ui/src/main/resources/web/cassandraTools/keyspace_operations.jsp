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
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<link href="css/clusterTools_ui.css" rel="stylesheet" media="all"/>
<script type="text/javascript" src="js/clusterTools_ui.js"></script>
<%
    String[] ksNames = null;
    String tableStyle="display:none;";
    CassandraClusterToolsKeyspaceAdminClient cassandraClusterToolsKeyspaceAdminClient=null;
    try {
        cassandraClusterToolsKeyspaceAdminClient=new CassandraClusterToolsKeyspaceAdminClient(config.getServletContext(), session);
        ksNames = cassandraClusterToolsKeyspaceAdminClient.listKeyspacesOfCurrentUSer();
        if (ksNames != null && ksNames.length > 0) {
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
            label="cassandra.cluster.tools.keyspaces.operations"
            resourceBundle="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
<div id="middle">
    <h2><fmt:message key="cassandra.cluster.tools.keyspaces.operations.msg" /></h2>
    <div id="workArea">
<table class="styledLeft" id="columnFamilyOperationTable" style="<%=tableStyle%>">
                <thead>
                <tr>
                    <th><fmt:message key="cassandra.cluster.tools.keyspace.operations.table.header.keyspace"/> </th>
                    <th><fmt:message key="cassandra.cluster.tools.keyspace.operations.table.header.operations"/> </th>
                </tr>
                </thead>
                <%
                if(ksNames!=null)
                      {
                      for(String keyspace:ksNames)
                      {
                      %>
                <tbody>
                <tr>
                    <td><a href="#" onclick="displayColumnFamlilyOperations('<%=keyspace%>')" ><%=keyspace%></a>
                <td>
                        <a href="#" onclick="repairKeyspace('<%=keyspace%>')" class="icon-link" style="background-image:url(images/repair.png);"><fmt:message key="cassandra.cluster.operations.repair"/></a>
                        <a href="#" onclick="compactKeyspace('<%=keyspace%>')" class="icon-link" style="background-image:url(images/compact.png);"><fmt:message key="cassandra.cluster.operations.compacr"/></a>
                        <a href="#" onclick="flushKeyspace('<%=keyspace%>')" class="icon-link" style="background-image:url(images/flush.png);"><fmt:message key="cassandra.cluster.operations.flush"/></a>
                        <a href="#" onclick="cleanUpKeyspace('<%=keyspace%>')" class="icon-link" style="background-image:url(images/cleanUp.png);"><fmt:message key="cassandra.cluster.operations.cleanup"/></a>
                        <a href="#" onclick="scrubKeyspace('<%=keyspace%>')" class="icon-link" style="background-image:url(images/scrub.jpg);"><fmt:message key="cassandra.cluster.operations.scrub"/></a>
                        <a href="#" onclick="upgradeSSTablesKeyspace('<%=keyspace%>')" class="icon-link" style="background-image:url(images/upgrade.jpg);"><fmt:message key="cassandra.cluster.operations.upgradeSSTables"/></a>
                        <a href="#" onclick="showKSTakeSnapShotForm('<%=keyspace%>')" class="icon-link" style="background-image:url(images/backUp.jpg);"><fmt:message key="cassandra.cluster.node.operaions.table.takeSnapShot" /></a>
                        <a href="#" onclick="showKSClearSnapShotForm('<%=keyspace%>')" class="icon-link" style="background-image:url(images/clear.png);"><fmt:message key="cassandra.cluster.node.operaions.table.clearSnapShot" /></a>
                    <div style="clear:both">
                        <div id="<%=keyspace%>DivTake" style="display:none">
                            <table cellpadding="0" cellspacing="0" class="styledLeft">
                                <thead>
                                <tr>
                                    <th colspan="2"><fmt:message key="cassandra.cluster.tools.snapshot.node.table.header"/> </th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr>
                                    <td><fmt:message key="cassandra.cluster.tools.snapshot.node.table.tag"/><span class="required">*</span></td>
                                    <td>
                                        <input type="text" id="<%=keyspace%>TakeTag">
                                    </td>
                                </tr>
                                <tr>
                                    <td class="buttonRow" colspan="2">
                                        <input type="button" class="button" value="Snapshot" onclick="takeKSNodeSnapShot('<%=keyspace%>')">
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div style="clear:both">
                        <div id="<%=keyspace%>DivClear" style="display:none">
                            <table cellpadding="0" cellspacing="0" class="styledLeft">
                                <thead>
                                <tr>
                                    <th colspan="2"><fmt:message key="cassandra.cluster.tools.clearSnapshot.node.table.header"/> </th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr>
                                    <td><fmt:message key="cassandra.cluster.tools.clearSnapshot.node.table.tag"/><span class="required">*</span></td>
                                    <td>
                                        <input type="text" id="<%=keyspace%>ClearTag">
                                    </td>
                                </tr>
                                <tr>
                                    <td class="buttonRow" colspan="2">
                                        <input type="button" class="button" value="Move" onclick="clearKSNodeSnapShot('<%=keyspace%>')">
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </td>
                </tr>
                <%}
                 }%>
            </table>

 </div>
  </div>
 </fmt:bundle>