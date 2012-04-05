<%@include file="includes/head.jsp" %>
<link rel="stylesheet" href="css/feedback.css">  
<link rel="stylesheet" href="css/carbonFormStyles.css" type="text/css">
<img src="images/logo.png" alt="Logo" />
<h2 class="mainTitle">Try this out!</h2>

<%
    String errorMessage = request.getParameter("error");
    String infoMessage = request.getParameter("info");
%>
<div class="loginBox">
	
<form method="post" action="OTLoginServlet" id="loginForm">
    <table>
	<tr>
		<td colspan="2" class="register-msg">
		Please login with <a href="https://wso2.org">Oxygen tank</a> credentials.<br /><br />
Not a member yet? <a href="https://wso2.org/user/register">Register here for free.</a>
		</td>
	</tr>
        <tr>
            <td class="textBox">Username :</td>
            <td class="valueBox"><input id="username" type="text" name="username"/></td>
        </tr>
        <tr>
            <td class="textBox">Password :</td>
            <td class="valueBox"><input id="password" type="password" name="password"></td>
        </tr>
        <tr>
            <td></td>
            <td class="valueBox"><input type="submit" value="Log In" class="button">
                <input type="reset" value="Reset" class="button"></td>
        </tr>
    </table>

    <table>
        <tr>
            <td>
                <%if (errorMessage != null) {%>
                <font color="red">Error login into system - <%=errorMessage%>
                </font>
                <%} else {%>
                &nbsp;
                <%}%>
            </td>
        </tr>
    </table>
</form>
</div>
<%@include file="includes/footer.jsp" %>
