<%
    String jdbcurl = (session.getAttribute("jdbcurl") != null) ? ((String[]) session.getAttribute("jdbcurl")) [0] : "";
    String driver = (session.getAttribute("driver") != null) ? ((String[]) session.getAttribute("driver")) [0] : "";
    String username = (session.getAttribute("username") != null) ? ((String[]) session.getAttribute("username")) [0] : "";
    String password = (session.getAttribute("password") != null) ? ((String[]) session.getAttribute("password")) [0] : "";
%>
<form>
    <p>JDBC URL : <input type="text" size="50%" name="jdbcurl" value="<%=jdbcurl%>"/></p>
    <p>Driver Class Name : <input type="text" size="50%" name="driver" value="<%=driver%>"/></p>
    <p>Username : <input type="text" size="50%" name="username" value="<%=username%>"/></p>
    <p>Password : <input type="text" size="50%" name="password" value="<%=password%>"/></p>
    <input type="hidden" name="page" id="page" value="01">
</form>