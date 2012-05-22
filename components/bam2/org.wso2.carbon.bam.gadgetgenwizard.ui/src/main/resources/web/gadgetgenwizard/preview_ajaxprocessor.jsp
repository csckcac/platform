<%@ page import="java.util.Map" %>
<%
    Map parameterMap = request.getParameterMap();
    for (Object o : parameterMap.keySet()) {
        String param = (String) o;
        Object value = parameterMap.get(param);
        session.setAttribute(param, value);
    }


%>
<tr>
    <td>Gadget Title<font color="red">*</font>
    </td>
    <td><input type="text" name="gadget-title" value="Gadget Generation Magnifique"/></td>
</tr>
<tr>
    <td>Gadget File Name<font color="red">*</font>
    </td>
    <td><input type="text" name="gadget-filename" value="generated-gadget" style="width:150px"/></td>
</tr>
<tr>
    <td>Refresh Rate (in Seconds)<font color="red">*</font>
    </td>
    <td><input type="text" name="gadget-filename" value="generated-gadget" style="width:150px"/></td>
</tr>
<input type="hidden" name="page" id="page" value="4">
