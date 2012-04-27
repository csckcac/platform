<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Enumeration" %>
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




%>
<script type="text/javascript">


    function uiElementChange() {
        if ($("#uielement").val() == "bar") {
            $("#bar-chart-options").show();
        }
    }
    $(document).ready(function () {

        uiElementChange();


        $("#uielement").change(function() {
            uiElementChange();
        })
    })

</script>
<form>
    <select name="uielement" id="uielement">
        <option value="bar">Bar Chart</option>
        <option value="table">Table</option>
    </select>
    <div id="bar-chart-options">
        <p><label>X-Axis Label : </label><input type="text" name="bar-xlabel" value=""/></p>
        <p><label>X-Axis Column : </label><input type="text" name="bar-xcolumn"/></p>
        <p><label>Y-Axis Label : </label><input type="text" name="bar-ylabel"/></p>
        <p><label>Y-Axis Column : </label><input type="text" name="bar-ycolumn"/></p>
    </div>
    <input type="hidden" name="page" id="page" value="03"/>

</form>