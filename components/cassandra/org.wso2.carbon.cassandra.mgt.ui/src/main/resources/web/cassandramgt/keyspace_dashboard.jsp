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
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.ColumnFamilyInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.KeyspaceInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.TokenRangeInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientConstants" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientHelper" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraKeyspaceAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Iterator" %>
<%--<jsp:include page="../dialog/display_messages.jsp"/>--%>
<script type="text/javascript" src="js/cassandra_ui_util.js"></script>

<fmt:bundle basename="org.wso2.carbon.cassandra.mgt.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.cassandra.mgt.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="cassandrajsi18n"/>
<carbon:breadcrumb
        label="cassandra.keyspace"
        resourceBundle="org.wso2.carbon.cassandra.mgt.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<%
    response.setHeader("Cache-Control", "no-cache");
    String keyspace = request.getParameter("name");
    String clusterName = null;
    if (keyspace != null && !"".equals(keyspace.trim())) {
        keyspace = keyspace.trim();
        KeyspaceInformation keyspaceInformation = null;
        TokenRangeInformation[] tokenRangeInformations = null;
        try {
            session.removeAttribute(CassandraAdminClientConstants.CURRENT_KEYSPACE);
            keyspaceInformation = CassandraAdminClientHelper.getKeyspaceInformation(config.getServletContext(),
                                                                                    session, keyspace);
            CassandraKeyspaceAdminClient cassandraKeyspaceAdminClient =
                    new CassandraKeyspaceAdminClient(config.getServletContext(), session);
            tokenRangeInformations = cassandraKeyspaceAdminClient.getTokenRange(keyspace);
            clusterName = cassandraKeyspaceAdminClient.getClusterName();
        } catch (Exception e) {
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg); %>
<script type="text/javascript">
    window.location.href = "../admin/error.jsp";
</script>
<%
    }
    assert keyspaceInformation != null;
    String alias = CassandraAdminClientHelper.getAliasForReplicationStrategyClass(keyspaceInformation.getStrategyClass());
    List endPoints = CassandraAdminClientHelper.getCassandraEndPointList();
%>
<div id="middle">
    <h2><fmt:message key="cassandra.keyspace.dashboard"/> (<%=clusterName%> > <%=keyspace%>) </h2>

    <div id="workArea">

        <table width="100%" cellspacing="0" cellpadding="0" border="0">
            <tr>
                <td width="50%">
                    <table class="styledLeft" id="keyspaceInfoTable" style="margin-left: 0px;"
                           width="100%">
                        <thead>
                        <tr>
                            <th colspan="2" align="left"><fmt:message
                                    key="cassandra.keyspace.details"/></th>
                        </tr>
                        </thead>
                        <tr>
                            <td width="30%"><fmt:message key="cassandra.cluster.name"/></td>
                            <td><%=clusterName%>
                            </td>
                        </tr>
                        <tr>
                            <td width="30%"><fmt:message key="cassandra.keyspace.name"/></td>
                            <td><%=keyspaceInformation.getName()%>
                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="cassandra.field.ReplicationFactor"/></td>
                            <td><%=keyspaceInformation.getReplicationFactor()%>
                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="cassandra.field.ReplicationStrategy"/></td>
                            <td align="left">
                                <% if (CassandraAdminClientConstants.OLD_NETWORK.equals(alias)) { %>
                                <fmt:message
                                        key="cassandra.field.ReplicationStrategy.oldnetwork"/>
                                <%} else if (CassandraAdminClientConstants.NETWORK.equals(alias)) {%>
                                <fmt:message
                                        key="cassandra.field.ReplicationStrategy.network"/>
                                <%} else {%>
                                <fmt:message
                                        key="cassandra.field.ReplicationStrategy.simple"/>
                                <% } %>
                            </td>
                        </tr>
                    </table>
                </td>
                <%

                    if (tokenRangeInformations != null && tokenRangeInformations.length > 0) {
                %>
                <td width="10px">&nbsp;</td>
                <td>

                    <div id="tokenRanageTableDIv">
                        <table class="styledLeft" id="tokenRanageTable"
                               style="margin-left: 0px;" width="100%">
                            <thead>
                            <tr>
                                <th>
                                    <fmt:message key="cassandra.keyspac.endponts"/>
                                </th>
                                    <%--<th>--%>
                                    <%--<fmt:message key="cassandra.keyspac.starttoken"/>--%>
                                    <%--</th>--%>
                                    <%--<th>--%>
                                    <%--<fmt:message key="cassandra.keyspac.endtoken"/>--%>
                                    <%--</th>--%>
                            </tr>
                            </thead>
                            <% if (!endPoints.isEmpty()) {
                                for (Iterator ep = endPoints.iterator(); ep.hasNext(); ) {
                            %>
                            <tr>
                                <td><%=ep.next().toString()%>
                                </td>
                            </tr>
                            <%
                                }
                            %>
                            <%} else {%>

                            <% for (TokenRangeInformation rangeInformation : tokenRangeInformations) {%>
                            <tr>
                                <td>
                                    <%
                                        String[] eps = rangeInformation.getEndpoints();
                                        String epsAsString = "";
                                        if (eps != null && eps.length > 0) {
                                            for (String ep : eps) {
                                                epsAsString += "," + ep;
                                            }
                                        }
                                        epsAsString = epsAsString.substring(1);
                                    %>
                                    <%=epsAsString%>
                                </td>
                                    <%--<td>--%>
                                    <%--<%=rangeInformation.getStartToken()%>--%>
                                    <%--</td>--%>
                                    <%--<td>--%>
                                    <%--<%=rangeInformation.getEndToken()%>--%>
                                    <%--</td>--%>
                            </tr>
                            <% }%>
                            <%} %>
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
                    ColumnFamilyInformation[] columnFamilies = keyspaceInformation.getColumnFamilies();
                    String cfTableDisplay = "display:none;";
                    if (columnFamilies != null && columnFamilies.length != 0) {
                        cfTableDisplay = "";
                    }
                %>

                <td width="50%">
                    <div id="serviceClientDiv" style="<%=cfTableDisplay%>">
                        <table class="styledLeft" id="cfTable" style="margin-left: 0px;"
                               width="100%">
                            <thead>
                            <tr>
                                <th width="20%"><fmt:message key="cassandra.cf.name"/></th>
                                <th width="60%"><fmt:message key="cassandra.actions"/></th>
                            </tr>
                            </thead>
                            <tbody id="cfBody">
                            <%
                                int j = 0;
                                if (columnFamilies != null && columnFamilies.length != 0) {
                                    for (; j < columnFamilies.length; j++) {
                                        ColumnFamilyInformation columnFamily = columnFamilies[j];
                                        String name = columnFamily.getName();
                            %>
                            <tr id="cfRaw<%=j%>">
                                <td id="clTD<%=j%>">
                                    <a id="clTDLink<%=j%>"
                                       onclick="viewCLs('<%=keyspace%>','<%=name%>');"
                                       href="#"><%=name%>
                                    </a>
                                </td>
                                <td>
                                     <input type="hidden" name="cfName<%=j%>" id="cfName<%=j%>"
                                           value="<%=name%>"/>
                                    <a class="edit-icon-link"
                                       onclick="showShareCFEditor('<%=keyspace%>','<%=j%>');"
                                       href="#"><fmt:message
                                            key="cassandra.actions.share"/></a>
                                    <a class="edit-icon-link"
                                       onclick="showCFEditor('<%=keyspace%>','<%=j%>');"
                                       href="#"><fmt:message
                                            key="cassandra.actions.edit"/></a>
                                    <a class="delete-icon-link"
                                       onclick="deletecf('<%=keyspace%>','<%=j%>');"
                                       href="#"><fmt:message
                                            key="cassandra.actions.delete"/></a>
                                </td>
                            </tr>
                            <%
                                    }
                                }
                            %>
                            <input type="hidden" name="cfCount" id="cfCount" value="<%=j%>"/>
                            </tbody>
                        </table>
                    </div>
                    <div style="margin-top:0px;">
                        <a class="add-icon-link" onclick="addcf('<%=keyspace%>');" href="#">
                            <fmt:message key="cassandra.add.new.cf"/></a>
                    </div>
                </td>
            </tr>
        </table>

        <form name="dataForm" method="post" action="">
            <input name="backURL" type="hidden" id="hiddenField" value="">
        </form>

        <script type="text/javascript">
            alternateTableRows('keyspaceInfoTable', 'tableEvenRow', 'tableOddRow');
            alternateTableRows('cfTable', 'tableEvenRow', 'tableOddRow');
        </script>
        <%
            }
        %>
    </div>
</div>
</fmt:bundle>
