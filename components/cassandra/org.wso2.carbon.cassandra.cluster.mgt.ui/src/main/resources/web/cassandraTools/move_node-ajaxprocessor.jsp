<%@ page import="org.wso2.carbon.cassandra.cluster.mgt.ui.CassandraClusterToolsNodeOperationsAdminClient" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.json.simple.JSONObject" %>
<%
    JSONObject backendStatus = new JSONObject();
    backendStatus.put("success","no");
    try{
        String newToken=request.getParameter("newToken");
        CassandraClusterToolsNodeOperationsAdminClient   cassandraClusterToolsNodeOperationsAdminClient=new CassandraClusterToolsNodeOperationsAdminClient(config.getServletContext(),session);
        cassandraClusterToolsNodeOperationsAdminClient.moveNode(newToken);
        backendStatus.put("success","yes");
    }catch (Exception e)
    {}
    out.print(backendStatus);
    out.flush();
%>