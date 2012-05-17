<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.gadgetgenwizard.stub.types.WSMap" %>
<%@ page import="org.wso2.carbon.bam.gadgetgenwizard.ui.GadgetGenAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.*" %>
<%@ page import="org.wso2.carbon.bam.gadgetgenwizard.stub.GadgetGenAdminServiceGadgetGenException" %>
<%
    Map parameterMap = request.getParameterMap();
    for (Object o : parameterMap.keySet()) {
        String param = (String) o;
        Object value = parameterMap.get(param);
        session.setAttribute(param, value);
        System.out.println("============= Request Map ===================");
        if (value instanceof String[]) {
            String[] strings = (String[]) value;
            for (String string : strings) {
                System.out.println("param key : " + param + " param value : " + string);

            }

        } else {
            System.out.println("param key : " + param + " param value : " + value);
        }

    }
    System.out.println("============== Session Map ===================");
    Enumeration attributeNames = session.getAttributeNames();
    while (attributeNames.hasMoreElements()) {
        String attribute = (String) attributeNames.nextElement();
        System.out.println("param key : " + attribute + " param value : " + session.getAttribute(attribute) );
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

    System.out.println("UI element session attribute : " + ((String[]) session.getAttribute("uielement"))[0]);
    System.out.println("Is equal to bar : " + ((String[]) session.getAttribute("uielement"))[0].equals("bar"));

    if ((session.getAttribute("uielement") != null) && (((String[]) session.getAttribute("uielement"))[0].equals("bar")))                     {
        String[] barChartKeys = new String[] {"bar-xlabel", "bar-xcolumn", "bar-ylabel", "bar-ycolumn", "bar-title"};
        attrKeys.addAll(Arrays.asList(barChartKeys));
    }
    WSMap wsMap = gadgetGenAdminClient.constructWSMap(session, attrKeys);


    String gadgetXMLPath = null;
    String errorMsg = null;
    try {
        gadgetXMLPath = gadgetGenAdminClient.generateGraph(wsMap);
    } catch (GadgetGenAdminServiceGadgetGenException e) {
        errorMsg = "Error trying to generate graph. " + e.getMessage();
    }
    if (errorMsg == null) {
%>
<p><label>Gadget Generated at :
    <input type="text" name="gadget-path" disabled="disabled" value="<%=gadgetXMLPath%>"/>
</label><input type="button" onclick="" value="Go to Dashboard"></p>
<p><i>Copy the path to so that it can be added to the dashboard</i></p>
<%  } else { %>

<%=errorMsg%>
<%  } %>
<input type="hidden" name="page" id="page" value="05">

