<%@ page import="java.util.Map" %>
<%
    Map parameterMap = request.getParameterMap();
    for (Object o : parameterMap.keySet()) {
        String param = (String) o;
        Object value = parameterMap.get(param);
        session.setAttribute(param, value);
    }


%>
<table class="normal">
    <tbody>
    <tr>
        <td>Gadget Title<span style="color: red; ">*</span>
        </td>
        <td><input type="text" name="gadget-title" value="" style="width:200px"/></td>
    </tr>
    <tr>
        <td>Gadget File Name<span style="color: red; ">*</span>
        </td>
        <td><input type="text" name="gadget-filename" value="" style="width:200px"/></td>
    </tr>
    <tr>
        <td>Refresh Rate (in Seconds)<span style="color: red; ">*</span>
        </td>
        <td><input type="text" name="refresh-rate" value="10" style="width:200px"/></td>
    </tr>
    <input type="hidden" name="page" id="page" value="4">
    </tbody>
</table>