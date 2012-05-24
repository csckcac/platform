<%
    String jdbcurl = (session.getAttribute("jdbcurl") != null) ? ((String[]) session.getAttribute("jdbcurl")) [0] : "";
    String driver = (session.getAttribute("driver") != null) ? ((String[]) session.getAttribute("driver")) [0] : "";
    String username = (session.getAttribute("username") != null) ? ((String[]) session.getAttribute("username")) [0] : "";
    String password = (session.getAttribute("password") != null) ? ((String[]) session.getAttribute("password")) [0] : "";
%>

<script type="text/javascript">
    $("#validate").click(function() {
        $.post("validate_db_conn_ajaxprocessor.jsp", $("form").serialize(), function(html) {
            var success = !(html.toLowerCase().match(/error/));
            if (success) {
                CARBON.showInfoDialog(html);
            } else {
                CARBON.showErrorDialog(html);
            }
        });

    });
</script>

<table class="normal">
    <tbody>
    <tr>
        <td>JDBC URL<font color="red">*</font>
        </td>
        <td><input type="text" name="jdbcurl" value="<%=jdbcurl%>" style="width:150px"/></td>
    </tr>
    <tr>
        <td>Driver Class Name<font color="red">*</font></td>
        <td><input type="text" name="driver" value="<%=driver%>" style="width:150px"/></td>
    </tr>
    <tr>
        <td>User Name<font color="red">*</font></td>
        <td><input type="text" name="username" value="<%=username%>" style="width:150px"/></td>
    </tr>
    <tr>
        <td>Password<font color="red">*</font></td>
        <td><input type="password" name="password" value="<%=password%>" style="width:150px"></td>
    </tr>
    <tr>
        <td><input type="button" class="button" value="Validate Connection" id="validate"/></td>
    </tr>
    <input type="hidden" name="page" id="page" value="1">
    </tbody>
</table>