<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.clustermonitor.ui.ClusterAdminClient" %>
<%@ page import="org.wso2.carbon.bam.clustermonitor.ui.Point" %>
<%@ page import="org.wso2.carbon.bam.clustermonitor.ui.data.ServiceTableData" %>
<%@ page import="org.wso2.carbon.bam.clustermonitor.ui.data.OperationData" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="java.util.*" %>
<%
       String cluster = request.getParameter("cluster");
      String dataCenter = request.getParameter("dc");
      String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
      ConfigurationContext configContext =
              (ConfigurationContext) config.getServletContext().
                      getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
      String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

      ClusterAdminClient client = new ClusterAdminClient(cookie, serverURL, configContext);

      ServiceTableData clusterStatistics = client.getClusterStatistics(dataCenter,cluster);
      HashMap<String,List<OperationData>> serviceDataMap =  clusterStatistics.getServiceData();
      %>
      <table class="styledLeft">
          <thead>
            <tr>
                <th></th>
                <th style="width:106px;">Request Count</th>
                <th style="width:106px;">Response Count</th>
                <th style="width:106px;">Fault Count</th>
            </tr>
          </thead>
          <tbody>
      <%

      for(Map.Entry entry:serviceDataMap.entrySet()){
          String serviceName = (String)entry.getKey();
          List<OperationData> operationDataList = (List<OperationData>)entry.getValue();
          %><tr><td colspan="4"><%
          %><h3><%=serviceName%></h3><table class="styledLeft noBorders" style="margin-left:20px;width:99%"><%
          %><%
          for (OperationData operationData : operationDataList) {
              String operationName = operationData.getOperationName();
              String requestCount = operationData.getRequestCount();
              String responseCount = operationData.getResponseCount();
              String faultCount = operationData.getFaultCount();
              %><tr><td><%
              %><%=operationName%><%
              %></td><td style="width:95px;"><%
              %><%=requestCount%><%
              %></td><td style="width:95px;"><%
              %><%=responseCount%><%
              %></td><td style="width:95px;"><%
              %><%=faultCount%><%
              %></td></tr><%
          }
          %></table></td></tr><%

      }
%>
      </tbody>
      </table>
