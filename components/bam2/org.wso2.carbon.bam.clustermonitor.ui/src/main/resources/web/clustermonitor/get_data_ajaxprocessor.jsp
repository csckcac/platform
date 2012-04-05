<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.clustermonitor.ui.ClusterAdminClient" %>
<%@ page import="org.wso2.carbon.bam.clustermonitor.ui.Point" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="org.json.simple.JSONObject" %>
<%
    String cluster = request.getParameter("cluster");
    String dataCenter = request.getParameter("dataCenter");
    String service = request.getParameter("service");
    String operation = request.getParameter("operation");

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    ClusterAdminClient client = new ClusterAdminClient(cookie, serverURL, configContext);

    Point[] pointsRequestCount = null;
    Point[] pointsResponseCount = null;
    Point[] pointsFaultCount = null;
    Point[] pointsResponseTime = null;

    String[] parmArray = new String[2];

    if (!operation.equals("all") && !operation.equals("null")) {
        parmArray = new String[4];
        parmArray[3] = operation;
        parmArray[2] = service;
    } else if (!service.equals("all") && !service.equals("null")) {
        parmArray = new String[3];
        parmArray[2] = service;
    }
    parmArray[0] = dataCenter;
    parmArray[1] = cluster;

    pointsRequestCount = client.getRequestCount(parmArray);
    pointsResponseCount = client.getResponseCount(parmArray);
    pointsFaultCount = client.getFaultCount(parmArray);
    pointsResponseTime = client.getResponseTime(parmArray);

%>

<data>[<%for(int i=0;i<pointsRequestCount.length;i++){%>
        <%=pointsRequestCount[i].getY()%>
        <%if((i+1) != pointsRequestCount.length)%>,<%}%>]
    <data>[<%for(int i=0;i<pointsResponseCount.length;i++){%>
            <%=pointsResponseCount[i].getY()%>
            <%if((i+1) != pointsResponseCount.length)%>,<%}%>]
        <data>[<%for(int i=0;i<pointsFaultCount.length;i++){%>
                <%=pointsFaultCount[i].getY()%>
                <%if((i+1) != pointsFaultCount.length)%>,<%}%>]
            <data>[<%for(int i=0;i<pointsResponseTime.length;i++){%>
                    <%=pointsResponseTime[i].getY()%>
                    <%if((i+1) != pointsResponseTime.length)%>,<%}%>]