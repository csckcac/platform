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
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%
    String name = request.getParameter("name");
    String role = request.getParameter("role");
    String action = request.getParameter("action");
    try {
        CassandraKeyspaceAdminClient cassandraKeyspaceAdminClient = new CassandraKeyspaceAdminClient(config.getServletContext(), session);
        String path = "/cassandra/keyspaces/" + name; //TODO use a constant
        if ("clear".equals(action)) {
            cassandraKeyspaceAdminClient.clearResource(role, path);
        } else {
            cassandraKeyspaceAdminClient.shareResource(role, path);
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
<script type="text/javascript">
    location.href = "../cassandramgt/cassandra_keyspaces.jsp?region=region1&item=cassandra_ks_list_menu";
</script>

