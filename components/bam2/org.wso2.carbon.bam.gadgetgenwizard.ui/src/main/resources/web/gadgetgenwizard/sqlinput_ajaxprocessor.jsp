<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>


<%
    Map parameterMap = request.getParameterMap();
    for (Iterator iterator = parameterMap.keySet().iterator(); iterator.hasNext();) {
        String param = (String) iterator.next();
        Object value = parameterMap.get(param);
        session.setAttribute(param, value);

    }

    Object sqlParam = session.getAttribute("sql");
    String sql = (sqlParam == null) ? "select * from productsummary" : ((String[])sqlParam) [0];

%>
<script type="text/javascript" src="../gadgetgenwizard/js/jquery.dataTables.min.js"/>
<script type="text/javascript">
    $(document).ready(function() {
        $.("#query-results-holder").hide();

        $("#execute-sql").click(function() {
            $.post("execute_sql.jsp", $("form").serialize(), function(html) {
                var success = !(html.toLowerCase().match(/error/));
                function getaoColumns(columns) {
                    var json = [];
                    for (var i = 0; i < columns.length; i++) {
                        var column = columns[i];
                        json.push({"sTitle" : column});
                    }
                    return json;
                }
                if (success) {
                    $("#query-results-holder").show();
                    $("#query-results-holder").html("<table class=\"normal\" id=\"query-results\"></table>");
                    $("#query-results").dataTable({
                        "aaData" : html.rows,
                        "aoColumns" : getaoColumns(html.columns)
                    });
                } else {
                    CARBON.showErrorDialog(html);
                }
            })
        });
    });
</script>
<form>
    <tr>
        <td>SQL Statement<font color="red">*</font>
        </td>
        <td><input type="text" name="sql" value="<%=sql%>" style="width:150px"/></td>
        <td><input type="button" id="execute-sql"/></td>
    </tr>
    <tr id="query-results-holder">

    </tr>
    <input type="hidden" name="page" id="page" value="2">

</form>