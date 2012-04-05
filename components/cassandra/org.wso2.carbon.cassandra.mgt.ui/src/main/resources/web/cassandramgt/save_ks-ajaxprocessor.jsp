<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.stub.ks.xsd.KeyspaceInformation" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientConstants" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraAdminClientHelper" %>
<%@ page import="org.wso2.carbon.cassandra.mgt.ui.CassandraKeyspaceAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%
    String name = request.getParameter("name");
    String rf = request.getParameter("rf");
    String rs = request.getParameter("rs");
    String mode = request.getParameter("mode");
//    String replicationStrategy = CassandraAdminClientHelper.getReplicationStrategyClassForAlias(rs);

    JSONObject backendStatus = new JSONObject();
    backendStatus.put("isExist","no");

//    KeyspaceInformation keyspaceInformation = CassandraAdminClientHelper.getKeyspaceInformation(config.getServletContext(), session, name);

     String[] ksNames = null;

//    String ksTableDisplay = "display:none;";
    try {
        session.removeAttribute(CassandraAdminClientConstants.CURRENT_KEYSPACE);
        CassandraKeyspaceAdminClient cassandraKeyspaceAdminClient = new CassandraKeyspaceAdminClient(config.getServletContext(), session);
        ksNames = cassandraKeyspaceAdminClient.listKeyspacesOfCurrentUSer();
        if (ksNames != null && ksNames.length > 0) {
//            ksTableDisplay = "";
                for (String  ks : ksNames) {
                    if(name.equals(ks)){
                      backendStatus.put("isExist","yes");
                    }
                }
        }
    } catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
    }

    out.print(backendStatus);
    out.flush();
%>

    <%--try {--%>
        <%--CassandraKeyspaceAdminClient cassandraKeyspaceAdminClient = new CassandraKeyspaceAdminClient(config.getServletContext(), session);--%>
        <%--KeyspaceInformation keyspaceInformation = new KeyspaceInformation();--%>
        <%--keyspaceInformation.setName(name);--%>
        <%--int replicationFactor = 1;--%>
        <%--if (rf != null && !"".equals(rf.trim())) {--%>
            <%--try {--%>
                <%--replicationFactor = Integer.parseInt(rf.trim());--%>
            <%--} catch (NumberFormatException ignored) {--%>
            <%--}--%>
        <%--}--%>
        <%--keyspaceInformation.setReplicationFactor(replicationFactor);--%>
        <%--keyspaceInformation.setStrategyClass(replicationStrategy);--%>
        <%--if ("add".equals(mode)) {--%>
            <%--cassandraKeyspaceAdminClient.addKeyspace(keyspaceInformation);--%>
        <%--} else {--%>
            <%--cassandraKeyspaceAdminClient.updateKeyspace(keyspaceInformation);--%>
        <%--}--%>
    <%--} catch (Exception e) {--%>
        <%--CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);--%>
        <%--session.setAttribute(CarbonUIMessage.ID, uiMsg);--%>
<%--%>--%>
<%--<script type="text/javascript">--%>
    <%--window.location.href = "../admin/error.jsp";--%>
<%--</script>--%>
<%--<%--%>
    <%--}--%>
<%--%>--%>
<%--<script type="text/javascript">--%>
    <%--location.href = "../cassandramgt/cassandra_keyspaces.jsp?region=region1&item=cassandra_ks_list_menu";--%>
<%--</script>--%>
