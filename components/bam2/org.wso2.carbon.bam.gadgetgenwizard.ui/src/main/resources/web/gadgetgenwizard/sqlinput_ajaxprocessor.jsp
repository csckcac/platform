<%@ page import="java.util.Map" %>


<%
    Map parameterMap = request.getParameterMap();
    for (Object o : parameterMap.keySet()) {
        String param = (String) o;
        Object value = parameterMap.get(param);
        session.setAttribute(param, value);
    }

    Object sqlParam = session.getAttribute("sql");
    String sql = (sqlParam == null) ? "" : ((String[])sqlParam) [0];

%>
<script type="text/javascript" src="../gadgetgenwizard/js/jquery.dataTables.min.js"></script>
<script type="text/javascript">
    $(document).ready(function() {
        $("#query-results-holder").hide();

        $("#execute-sql").click(function() {
            $.post("execute_sql_ajaxprocessor.jsp", $("form").serialize(), function(html) {
                var success = !(html.toLowerCase().match(/error/));
                function getaoColumns(columnNames) {
                    var json = [];
                    for (var i = 0; i < columnNames.length; i++) {
                        var columnName = columnNames[i];
                        json.push({ sTitle : columnName});
                    }
                    return json;
                }
                if (success) {
                    var respJson = JSON.parse(html);

                    $("#query-results-holder").html("<table id=\"query-results\"></table>");
                    $("#query-results").dataTable({
                        "aaData" : respJson.Rows,
                        "aoColumns" : getaoColumns(respJson.ColumnNames)
                    });
                    $("#query-results-holder").show();
                } else {
                    CARBON.showErrorDialog(html);
                }
            })
        });
    });
</script>
<table class="normal">
    <tbody>
    <tr>
        <td>SQL Statement<font color="red">*</font>
        </td>
        <td><input type="text" name="sql" value="<%=sql%>" style="width:200px"/></td>
        <td><input type="button" id="execute-sql" value="Preview SQL Results"/></td>
    </tr>
    <tr>
        <td colspan="3">
            <div id="query-results-holder" style="padding-top:25px"></div>
        </td>
    </tr>
    <input type="hidden" name="page" id="page" value="2">
    </tbody>
</table>
