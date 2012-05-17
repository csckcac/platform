<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>


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

    Object sqlParam = session.getAttribute("sql");
    String sql = (sqlParam == null) ? "select * from productsummary" : ((String[])sqlParam) [0];

%>
<form>
    <p>SQL Statement : <input type="text" size="50%" name="sql" value="<%=sql%>"/></p>
    <input type="hidden" name="page" id="page" value="02">

</form>