<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.CassandraClusterToolsAdminClientException" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.CassandraClusterToolsKeyspaceOperationsAdminClient" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%
    JSONObject backendStatus = new JSONObject();
    backendStatus.put("success","no");
    try{
        String keyspace=request.getParameter("keyspace");
        CassandraClusterToolsKeyspaceOperationsAdminClient cassandraClusterToolsKeyspaceOperationsAdminClient=new CassandraClusterToolsKeyspaceOperationsAdminClient(config.getServletContext(),session);
        cassandraClusterToolsKeyspaceOperationsAdminClient.compactKeyspace(keyspace);
        backendStatus.put("success","yes");
    }catch (CassandraClusterToolsAdminClientException e)
    {}
    out.print(backendStatus);
    out.flush();
%>
