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
    <link rel="stylesheet" href="css/cart-styles.css" type="text/css"/>
    <link rel="stylesheet" href="css/navigation-styles.css" type="text/css"/>
    <link rel="stylesheet" href="css/admin-styles.css" type="text/css"/>

    <!--Jquery includes -->
    <script type="text/javascript" src="js/jquery/jquery.js"></script>

    <%--Additional Includes--%>
    <script type="text/javascript">
        function submitForm() {
            document.loginFrm.j_username.value = document.loginFrm.tmpUsername.value;
            document.loginFrm.submit();
        }
        $(document).ready(
                function() {
                    $('#j_password').keypress(function(e)
                    {
                        // if the key pressed is the enter key
                        if (e.which == 13)
                        {
                            submitForm();
                        }
                    });
                }
                );
    </script>
</head>
<body>
<div class="pageSizer">
    <img src="images/logo.png" alt="ACME" />

    <div class="navigation">
        <ul id="topnav">
        </ul>
    </div>
    <div class="clear"></div>
    <div class="content">
        
        <table class="contentTable">
            <tr>
                <td class="leftCol">
                    <div class="leftMenu">
                        <div class="leftMenu-inside">
                            <div class="catagories-block">
                                
                            </div>
                        </div>
                    </div>
                </td>
                <td class="contentCol">
                    <form method="POST" name="loginFrm"
                          action='<%= response.encodeURL("j_security_check") %>'>
                        <table border="0" cellspacing="5">
                            <tr>
                                <th align="right">Username:</th>
                                <td align="left"><input type="text" name="tmpUsername"/><input
                                        type="hidden"
                                        name="j_username">
                                </td>
                            </tr>
                            <tr>
                                <th align="right">Password:</th>
                                <td align="left"><input type="password" id="j_password" name="j_password"></td>
                            </tr>
                            <tr>
                                <td align="right">&nbsp;</td>
                                <td align="left"><input type="button" value="Log In"
                                                        onclick="submitForm()">&nbsp;<input
                                        type="reset"></td>
                            </tr>
                        </table>
                    </form>
                </td>
            </tr>
        </table>
    </div>
</div>

<jsp:include page="footer.jsp"/>

</body>
</html>