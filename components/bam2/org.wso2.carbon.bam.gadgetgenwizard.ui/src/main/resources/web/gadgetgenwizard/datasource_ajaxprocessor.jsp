<form>
    <p>JDBC URL : <input type="text" size="50%" name="jdbcurl" value="<%=session.getAttribute("jdbcurl")%>"/></p>
    <p>Driver Class Name : <input type="text" size="50%" name="driver" value="<%=session.getAttribute("driver")%>"/></p>
    <p>Username : <input type="text" size="50%" name="username" value="<%=session.getAttribute("username")%>"/></p>
    <p>Password : <input type="text" size="50%" name="password" value="<%=session.getAttribute("password")%>"/></p>
    <input type="hidden" name="page" id="page" value="01">
</form>