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
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraKeyspaceAdminClient" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%
    String name = request.getParameter("name");
    String role = request.getParameter("role");
    String keyspace = request.getParameter("keyspace");
    String action = request.getParameter("action");
    String path = "/cassandra/keyspaces/" + keyspace + "/" + name;   //TODO use a constant
    try {
        CassandraKeyspaceAdminClient cassandraKeyspaceAdminClient = new CassandraKeyspaceAdminClient(config.getServletContext(), session);
        if ("clear".equals(action)) {
            cassandraKeyspaceAdminClient.clearResource(role, path);
        } else {
            cassandraKeyspaceAdminClient.shareResource(role, path);
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