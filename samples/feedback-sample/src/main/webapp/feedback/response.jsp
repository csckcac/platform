<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<fmt:bundle basename="org.wso2con.feedback.form.i18n.Resources">

	<html>
<head>
<title>WSO2Con - Feedbck Application - Error</title>
<link rel="stylesheet" href="../css/feedback.css">  
<link rel="stylesheet" href="../css/carbonFormStyles.css" type="text/css">
</head>
<%  String message=request.getParameter("message");;
%>

<body>
<div class="pageSizer">
		<div class="header"></div>
		<img src="../images/logo.png" />
		<div class="main-message">
			<% if (message.equals("success")) {%>
			<h1>
				<b><font color="black">Feedback Form successfully submitted!!! </font> </b>
			</h1>
			<%}%>
			<% if (message.equals("error")) {%>
			<h1>
				<b><font color="red">Feedback process failed ... Please try again later. </font> </b>
			</h1>
			<%}%>
		</div>
		<div class="loginBox">
			
		</div>
	</div>


	
	
</body>
	</html>
</fmt:bundle>
