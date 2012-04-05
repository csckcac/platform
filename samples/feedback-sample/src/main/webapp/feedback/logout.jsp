<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>WSO2Con - Feedbck Application - login-error</title>
<link rel="stylesheet" href="../css/feedback.css">  
<link rel="stylesheet" href="../css/carbonFormStyles.css" type="text/css">
</head>
<body>

	<div class="pageSizer">
		<div class="header"></div>
		<img src="../images/logo.png" />
		<div class="main-message">
			<%
    				session.invalidate();
    				response.sendRedirect("index.jsp");
			%>
		</div>
		<div class="loginBox">
			
		</div>
	</div>
</body>
</html>
