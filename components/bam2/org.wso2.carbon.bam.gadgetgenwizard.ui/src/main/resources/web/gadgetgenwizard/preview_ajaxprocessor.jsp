<%@ page import="java.util.Map" %>
<%
    Map parameterMap = request.getParameterMap();
    for (Object o : parameterMap.keySet()) {
        String param = (String) o;
        Object value = parameterMap.get(param);
        session.setAttribute(param, value);
    }


%>
<form>
    <p><label>Gadget Title : </label><input type="text" name="gadget-title" value="Gadget Generation Magnifique"/></p>
    <p><label>Gadget File Name : </label><input type="text" name="gadget-filename" value="generated-gadget"/></p>
    <p><label>Refresh Rate (in Seconds) : </label><input type="text" name="refresh-rate" value="10"/></p>
    <input type="hidden" name="page" id="page" value="4">
</form>
