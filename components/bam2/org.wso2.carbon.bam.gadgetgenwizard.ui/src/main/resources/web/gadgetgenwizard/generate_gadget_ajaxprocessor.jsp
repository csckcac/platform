<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.gadgetgenwizard.stub.types.WSMap" %>
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
        System.out.println("============= Request Map ===================");
        if (value instanceof String[]) {
            String[] strings = (String[]) value;
            for (int i = 0; i < strings.length; i++) {
                String string = strings[i];
                System.out.println("param key : " + param + " param value : " +  string);

            }

        } else {
            System.out.println("param key : " + param + " param value : " +  value);
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


    gadgetGenAdminClient.generateGraph(wsMap);
%>