<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.CassandraClusterToolsAdminClientException" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.CassandraClusterToolsColumnFamilyOperationsAdminClient" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.json.simple.JSONObject" %>
<%
    JSONObject backendStatus = new JSONObject();
    backendStatus.put("success","no");
    try{
        String keyspace=request.getParameter("keyspace");
        String column_family=request.getParameter("columnFamily");
        CassandraClusterToolsColumnFamilyOperationsAdminClient cassandraClusterToolsColumnFamilyOperationsAdminClient=new CassandraClusterToolsColumnFamilyOperationsAdminClient(config.getServletContext(),session);
        cassandraClusterToolsColumnFamilyOperationsAdminClient.compactColumnFamily(keyspace,column_family);
        backendStatus.put("success","yes");
    }catch (Exception e) {}
    out.print(backendStatus);
    out.flush();
%>