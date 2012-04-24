<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>



<%
    Map parameterMap = request.getParameterMap();

    for (Iterator iterator = parameterMap.keySet().iterator(); iterator.hasNext();) {
        String param = (String) iterator.next();
        Object value = parameterMap.get(param);
        session.setAttribute(param, value);
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




%>
<form>
    <p>SQL Statement : <input type="text" size="50%" name="sql"/></p>
    <input type="hidden" name="page" id="page" value="02">

</form>