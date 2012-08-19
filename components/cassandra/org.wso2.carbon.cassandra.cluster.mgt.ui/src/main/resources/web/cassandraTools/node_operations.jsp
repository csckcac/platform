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
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.CassandraClusterToolsNodeOperationsAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<script type="text/javascript" src="js/clusterTools_ui.js"></script>
<%
    String hostName=request.getParameter("hostName");
    String token=request.getParameter("token");
    String nodeCount=session.getAttribute("nodeCount").toString();
    boolean isGossipEnable=true;
    boolean isRPCEnable=true;
    boolean isIncrementalBackUpEnable=false;
    boolean isJoin=true;
    try{
        CassandraClusterToolsNodeOperationsAdminClient cassandraClusterToolsNodeOperationsAdminClient=new CassandraClusterToolsNodeOperationsAdminClient(config.getServletContext(),session);
        isGossipEnable=cassandraClusterToolsNodeOperationsAdminClient.getGossipServerStatus();
        isRPCEnable=cassandraClusterToolsNodeOperationsAdminClient.getRPCServerStatus();
        isIncrementalBackUpEnable=cassandraClusterToolsNodeOperationsAdminClient.getIncrementalBackUpStatus();
        isJoin=cassandraClusterToolsNodeOperationsAdminClient.isJoinedRing();
    }catch (Exception e)
    {
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
            label="cassandra.cluster.node.operaions"
            resourceBundle="org.wso2.carbon.cassandra.cluster.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
<div id="middle">
    <h2><fmt:message key="cassandra.cluster.node.operaions.msg" /></h2>
    <div id="workArea">
<table class="styledLeft" id="nodeOperationTable">
                <thead>
                <tr>
                    <th><fmt:message key="cassandra.cluster.node.operaions.table.header.name" /></th>
                    <th><fmt:message key="cassandra.cluster.node.operaions.table.header.operations" /></th>
                </tr>
                </thead>

                <tbody>
                <tr>
                    <td><a href="#" onclick="displayKeyspaceOperations()" ><%=hostName%></a>
                    </td>
                    <td>
                        <a href="#" onclick="decommissionNode('<%=nodeCount%>','<%=hostName%>')" class="icon-link" style="background-image:url(images/decommission.png);"><fmt:message key="cassandra.cluster.node.operaions.table.decommission" /></a>
                        <a href="#" onclick="drainNode('<%=hostName%>')" class="icon-link" style="background-image:url(images/drain.png);"><fmt:message key="cassandra.cluster.node.operaions.table.drain" /></a>
                        <a href="#" onclick="showTokenForm()" class="icon-link" style="background-image:url(images/move.png);"><fmt:message key="cassandra.cluster.node.operaions.table.move" /></a>
                        <a href="#" onclick="performGC()" class="icon-link" style="background-image:url(images/garbage_collection.png);"><fmt:message key="cassandra.cluster.node.operaions.table.PerformGC" /></a>
                        <a href="#" onclick="showTakeSnapShotForm()" class="icon-link" style="background-image:url(images/backUp.jpg);"><fmt:message key="cassandra.cluster.node.operaions.table.takeSnapShot" /></a>
                        <a href="#" onclick="showClearSnapShotForm()" class="icon-link" style="background-image:url(images/clear.png);"><fmt:message key="cassandra.cluster.node.operaions.table.clearSnapShot" /></a>

                        <%if(isRPCEnable){%>
                        <a href="#" onclick="disableRPC()" class="icon-link" style="background-image:url(images/stop_server.jpg);"><fmt:message key="cassandra.cluster.node.operaions.table.stopRPCServer" /></a>
                        <%}else{%>
                        <a href="#" onclick="enableRPC()" class="icon-link" style="background-image:url(images/start_server.png);"><fmt:message key="cassandra.cluster.node.operaions.table.startRPCServer" /></a>
                        <%}%>

                        <%if(isGossipEnable){%>
                        <a href="#" onclick="disableGossip()" class="icon-link" style="background-image:url(images/stop_server.jpg);"><fmt:message key="cassandra.cluster.node.operaions.table.stopGossip" /></a>
                        <%}else{%>
                        <a href="#" onclick="enableGossip()" class="icon-link" style="background-image:url(images/start_server.png);"><fmt:message key="cassandra.cluster.node.operaions.table.startGossip" /></a>
                        <%}%>

                        <%if(!isJoin){%>
                        <a href="#" onclick="joinRing()" class="icon-link" style="background-image:url(images/join.jpg);"><fmt:message key="cassandra.cluster.node.operaions.table.join" /></a>
                        <%}%>
                        <%if(!isIncrementalBackUpEnable){%>
                        <a href="#" onclick="enableIncrementalBackUp()" class="icon-link" style="background-image:url(images/start_server.png);"><fmt:message key="cassandra.cluster.node.operaions.table.enableIncrementalBackup" /></a>
                        <%}else{%>
                        <a href="#" onclick="disableIncrementalBackUp()" class="icon-link" style="background-image:url(images/stop_server.jpg);"><fmt:message key="cassandra.cluster.node.operaions.table.disableIncrementalBackup" /></a>
                        <%}%>

                        <div style="clear:both">
                            <div id="myDiv" style="display:none">
                                <table cellpadding="0" cellspacing="0" class="styledLeft">
                                    <thead>
                                    <tr>
                                        <th colspan="2"><fmt:message key="cassandra.cluster.tools.move.node.table.header"/> </th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr>
                                        <td><fmt:message key="cassandra.cluster.tools.move.node.table.newToken"/><span class="required">*</span></td>
                                        <td>
                                            <input type="text" id="newToken">
                                            (<fmt:message key="cassandra.cluster.tools.move.node.table.currentToken"/><span>:<%=token%></span>)
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="buttonRow" colspan="2">
                                            <input type="button" class="button" value="Move" onclick="moveNode()">
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                        <div style="clear:both">
                            <div id="divSnapShot" style="display:none">
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
                                            <input type="text" id="snapshotTag">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="buttonRow" colspan="2">
                                            <input type="button" class="button" value="Snapshot" onclick="takeNodeSnapShot()">
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                        <div style="clear:both">
                            <div id="divClearSnapShot" style="display:none">
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
                                            <input type="text" id="clearSnapshotTag">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="buttonRow" colspan="2">
                                            <input type="button" class="button" value="Clear Snapshot" onclick="clearNodeSnapShot()">
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
 </div>
  </div>
</fmt:bundle>