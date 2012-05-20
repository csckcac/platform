<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Enumeration" %>
<%

    Map parameterMap = request.getParameterMap();
    for (Object o : parameterMap.keySet()) {
        String param = (String) o;
        Object value = parameterMap.get(param);
        session.setAttribute(param, value);
    }

%>
<script type="text/javascript">


    function uiElementChange() {
        if ($("#uielement").val() == "bar") {
            $("#bar-chart-options").show();
        } else {
            $("#bar-chart-options").hide();
        }
    }
    $(document).ready(function () {

        uiElementChange();


        $("#uielement").change(function() {
            uiElementChange();
        })
    })

</script>

    <select name="uielement" id="uielement">
        <option value="bar">Bar Chart</option>
        <option value="table">Table</option>
    </select>
    <div id="bar-chart-options">
        <p><label>Chart Title : </label><input type="text" name="bar-title" value="Product vs Total Amount"/></p>
        <p><label>X-Axis Label : </label><input type="text" name="bar-xlabel" value="Product Name"/></p>
        <p><label>X-Axis Column : </label><input type="text" name="bar-xcolumn" value="prod_name"/></p>
        <p><label>Y-Axis Label : </label><input type="text" name="bar-ylabel" value="Total Amount (Rs.)"/></p>
        <p><label>Y-Axis Column : </label><input type="text" name="bar-ycolumn" value="total_amount"/></p>
    </div>
    <input type="hidden" name="page" id="page" value="3"/>

