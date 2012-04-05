<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.clustermonitor.ui.ClusterAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="java.util.*" %>
<%@ page import="org.wso2.carbon.bam.clustermonitor.ui.data.ProxyServiceTableData" %>
<%@ page import="org.wso2.carbon.bam.clustermonitor.ui.data.ProxyServiceData" %>
<%
       String cluster = request.getParameter("cluster");
      String dataCenter = request.getParameter("dc");
      String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
      ConfigurationContext configContext =
              (ConfigurationContext) config.getServletContext().
                      getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
      String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

      ClusterAdminClient client = new ClusterAdminClient(cookie, serverURL, configContext);

      ProxyServiceTableData clusterStatistics = client.getProxyServiceTableData(dataCenter, cluster);
      HashMap<String,List<ProxyServiceData>> serviceDataMap =  clusterStatistics.getServiceData();
      %>
      <table class="styledLeft">
          <thead>
            <tr>
                <th>Proxy Name</th>
                <th style="width:106px;">Count</th>
                <th style="width:106px;">Fault Count</th>
                <th style="width:106px;">Response Time</th>
            </tr>
          </thead>
          <tbody>
      <%

      for(Map.Entry entry:serviceDataMap.entrySet()){
          String serviceName = (String)entry.getKey();
          List<ProxyServiceData> proxyDataList = (List<ProxyServiceData>)entry.getValue();
          %><tr><td colspan="4"><%
          %><h3><%=serviceName%></h3><table class="styledLeft noBorders" style="margin-left:20px;width:99%"><%
          %><%
          for (ProxyServiceData proxyData : proxyDataList) {
              String count = proxyData.getCount();
              String direction = proxyData.getDirection();
              String responseTime = proxyData.getResponseTime();
              String faultCount = proxyData.getFaultCount();
              %><tr><td ><%
              %><%=direction%><%
              %></td><td style="width:95px;"><%
              %><%=count%><%
              %></td><td style="width:95px;"><%
              %><%=faultCount%><%
              %></td><td style="width:95px;"><%
              %><%=responseTime%><%
              %></td></tr><%
          }
          %></table></td></tr><%

      }
%>
      </tbody>
      </table>
