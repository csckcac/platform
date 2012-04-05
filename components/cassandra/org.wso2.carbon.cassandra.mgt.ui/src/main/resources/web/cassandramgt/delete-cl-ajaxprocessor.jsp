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
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.ColumnFamilyInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.KeyspaceInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientConstants" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientHelper" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraKeyspaceAdminClient" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%
    String cf = request.getParameter("cf");
    if (cf == null || "".equals(cf.trim())) {
        throw new RuntimeException("Column Family Name is null or empty"); //TODO
    }

    String name = request.getParameter("name");
    if (name == null || "".equals(name.trim())) {
        throw new RuntimeException("Column Name is null or empty"); //TODO
    }
    name = name.trim();

    try {
        CassandraKeyspaceAdminClient cassandraKeyspaceAdminClient = new CassandraKeyspaceAdminClient(config.getServletContext(), session);

        KeyspaceInformation keyspaceInformation =
                (KeyspaceInformation) session.getAttribute(CassandraAdminClientConstants.CURRENT_KEYSPACE);
        if (keyspaceInformation != null) {
            ColumnFamilyInformation columnFamilyInformation =
                    CassandraAdminClientHelper.getColumnFamilyInformationOfCurrentUser(keyspaceInformation, cf);
            CassandraAdminClientHelper.removeColumnInformation(columnFamilyInformation, name);
            cassandraKeyspaceAdminClient.updateColumnFamily(columnFamilyInformation);
        }
    } catch (Exception e) {
%>
<script type="text/javascript">
    jQuery(document).ready(function() {
        CARBON.showErrorDialog('<%=e.getMessage()%>', function () {
            CARBON.closeWindow();
        }, function () {
            CARBON.closeWindow();
        });
    });
</script>
<%}%>
