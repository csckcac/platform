<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head><title>Sales Force Data Retrieval Application</title>

<link rel="stylesheet" href="../css/feedback.css">  
<link rel="stylesheet" href="../css/carbonFormStyles.css" type="text/css">

<script type="text/javascript" src="../js/jquery/jquery-1.5.1.min.js"></script>
<script type="text/javascript" src="../js/jquery/jquery.validate.js"></script>
    
<link rel="stylesheet" href="../js/jquery/themes/ui-lightness/jquery.ui.core.css">

<script type="text/javascript" src="../js/jquery/jquery.ui.core.min.js"></script>
<script type="text/javascript" src="../js/jquery/jquery.ui.widget.min.js"></script>


</head>
<%@include file="header.jsp"%>
<body>
<div id="dialog-overlay"></div>

<div class="pageSizer">

<%
    String userEmail = (String)session.getAttribute("useremail");

    if (userEmail != null) {
%>

<div class="logoutButton"><%=userEmail%> &nbsp; &nbsp;<a href="/support-portal/portal/logout.jsp">Logout</a></div>
  

<%
    }
%>
