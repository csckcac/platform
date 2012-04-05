<%@ page import="org.wso2.salesforce.webapp.salesforce.SalesForceLoginManager" %>
<%
    SalesForceLoginManager manager = (SalesForceLoginManager) session.getAttribute("manager");
    try {
        if (manager != null) {
            manager.logout();
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    session.setAttribute("LoggedIn", false);
    session.invalidate();
    response.sendRedirect("../login.jsp");

%>