<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.CassandraClusterToolsNodeOperationsAdminClient" %>
<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.CassandraClusterToolsAdminClientException" %>
%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%
    JSONObject backendStatus = new JSONObject();
    backendStatus.put("success","no");
    try{
    CassandraClusterToolsNodeOperationsAdminClient cassandraClusterToolsNodeOperationsAdminClient=new CassandraClusterToolsNodeOperationsAdminClient(config.getServletContext(),session);
    cassandraClusterToolsNodeOperationsAdminClient.decommissionNode();
    backendStatus.put("success","yes");
    }catch (Exception e)
    {}
    out.print(backendStatus);
    out.flush();
%>