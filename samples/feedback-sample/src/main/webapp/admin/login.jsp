<%!
    private String getTenantDomain(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String temp = requestURI.substring(requestURI.indexOf("/t/") + 3);
        String tenantDomain = temp.substring(0, temp.indexOf("/"));
        return tenantDomain;
    }
%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  
    <script type="text/javascript">
        function submitForm() {
            document.loginFrm.j_username.value = document.loginFrm.tmpUsername.value;
            document.loginFrm.submit();
        }
    </script>
</head>
<body>

        <form method="POST" name="loginFrm" action='<%= response.encodeURL("j_security_check") %>'>
            <table border="0" cellspacing="5">
                <tr>
                    <th align="right">Username:</th>
                    <td align="left"><input id="tmpUsername" type="text" name="tmpUsername"/><input  id="j_username" type="hidden"
                                                                                   name="j_username">
                    </td>
                </tr>
                <tr>
                    <th align="right">Password:</th>
                    <td align="left"><input type="password" name="j_password"></td>
                </tr>
                <tr>
                    <td align="right">&nbsp;</td>
                    <td align="left"><input type="button" value="Log In" onclick="submitForm()"><input type="reset" value="Cancel">
                   
                    </td>
                </tr>
            </table>
        </form>
</body>
</html>