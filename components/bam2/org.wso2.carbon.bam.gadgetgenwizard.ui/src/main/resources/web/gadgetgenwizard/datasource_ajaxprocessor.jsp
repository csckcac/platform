<form>
    <p>JDBC URL : <input type="text" size="50%" name="jdbcurl" value="<%=request.getParameter("jdbcurl")%>"/></p>
    <p>Driver Class Name : <input type="text" size="50%" name="driver" value="<%=request.getParameter("driver")%>"/></p>
    <p>Username : <input type="text" size="50%" name="username" value="<%=request.getParameter("username")%>"/></p>
    <p>Password : <input type="text" size="50%" name="password" value="<%=request.getParameter("password")%>"/></p>
    <input type="hidden" name="page" id="page" value="01">
</form>