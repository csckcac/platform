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
<%@ page import="org.wso2.carbon.cassandra.explorer.ui.CassandraExplorerAdminClient" %>
<%@ page
        import="org.wso2.carbon.cassandra.explorer.stub.CassandraExplorerAdminCassandraExplorerException" %>
<%@ page import="java.net.SocketTimeoutException" %>

<%
    CassandraExplorerAdminClient adminClient =
            new CassandraExplorerAdminClient(config.getServletContext(), session);
    String clusterName = request.getParameter("cluster_name");
    String connectionUrl = request.getParameter("connection_url");
    String userName = request.getParameter("user_name");
    String password = request.getParameter("password");
    //Connection URL is being reused as ClusterName since it should be unique
    boolean isConnectionSuccess = false;
    try {
        isConnectionSuccess = adminClient.connectToCassandraCluster(connectionUrl, connectionUrl, userName, password);
    } catch (Exception exception) { %>
<script type="text/javascript">
    location.href = "cassandra_connect.jsp";
</script>
<% }

    if (isConnectionSuccess) {
%>
<script type="text/javascript">
    location.href = "cassandra_keyspaces.jsp";
</script>
<%
    }
%>
