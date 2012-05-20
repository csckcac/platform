<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.gadgetgenwizard.stub.beans.WSMap" %>
<%@ page import="org.wso2.carbon.bam.gadgetgenwizard.ui.GGWUIUtils" %>
<%@ page import="org.wso2.carbon.bam.gadgetgenwizard.ui.GadgetGenAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.*" %>
<%
    Map parameterMap = request.getParameterMap();
    for (Iterator iterator = parameterMap.keySet().iterator(); iterator.hasNext();) {
        String param = (String) iterator.next();
        Object value = parameterMap.get(param);
        session.setAttribute(param, value);
    }


    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    GadgetGenAdminClient gadgetGenAdminClient = new GadgetGenAdminClient(cookie, serverURL, configContext);

    List<String> attrKeys = new ArrayList<String>();

    String[] genericKeys = new String[] {"jdbcurl", "username", "password", "driver", "sql"};
    attrKeys.addAll(Arrays.asList(genericKeys));

    String[] gadgetKeys = new String[] {"gadget-title", "gadget-filename", "refresh-rate"};
    attrKeys.addAll(Arrays.asList(gadgetKeys));

    if ((session.getAttribute("uielement") != null) && (((String[]) session.getAttribute("uielement"))[0].equals("bar")))                     {
        String[] barChartKeys = new String[] {"bar-xlabel", "bar-xcolumn", "bar-ylabel", "bar-ycolumn", "bar-title"};
        attrKeys.addAll(Arrays.asList(barChartKeys));
    }

    WSMap wsMap = GGWUIUtils.constructWSMap(session, attrKeys);

    String gadgetXMLPath = null;
    String errorMsg = null;
    try {
        gadgetXMLPath = gadgetGenAdminClient.generateGraph(wsMap);
    } catch (Exception e) {
        errorMsg = "Error trying to generate graph. " + e.getMessage();
    }
    if (errorMsg == null) {
%>
<p>
    <label>Gadget Generated at : <input type="text" name="gadget-path" disabled="disabled" value="<%=gadgetXMLPath%>"/></label>
    <a href="../dashboard/index.jsp">Go to Dashboard</a> </p>
<p><i>Copy the path to so that it can be added to the dashboard</i></p>
<%  } else { %>

<%=errorMsg%>
<%  } %>
<input type="hidden" name="page" id="page" value="05">

