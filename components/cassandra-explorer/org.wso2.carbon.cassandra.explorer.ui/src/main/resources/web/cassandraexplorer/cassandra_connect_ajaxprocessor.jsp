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
<%@ page import="org.wso2.carbon.cassandra.explorer.stub.CassandraExplorerAdmin" %>
<%@ page import="org.wso2.carbon.explorer.ui.CassandraExplorerAdminClient" %>
<%
    CassandraExplorerAdminClient adminClient =
            new CassandraExplorerAdminClient(config.getServletContext(), session);
    String clusterName = request.getParameter("cluster_name");
    String connectionUrl = request.getParameter("connection_url");
    String userName = request.getParameter("user_name");
    String password = request.getParameter("password");
    boolean isConnectionSuccess = adminClient.connectToCassandraCluster(clusterName, connectionUrl,
                                                                        userName, password);
    if (isConnectionSuccess) {
%>
<script type="text/javascript">
    location.href = "cassandra_Keyspaces.jsp";
</script>
<%
    }
%>
