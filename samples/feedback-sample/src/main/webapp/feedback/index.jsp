

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Feedback - WSO2Con</title>
<link rel="stylesheet" href="../css/feedback.css">  
<link rel="stylesheet" href="../css/carbonFormStyles.css" type="text/css">

<script type="text/javascript" src="../js/modernizr-2.0.6.js"></script>
<script type="text/javascript" src="../js/jquery/jquery-1.5.1.min.js"></script>
<script type="text/javascript" src="../js/placeholder.js"></script>
<script type="text/javascript" src="../js/jquery/jquery.validate.js"></script>
    
<link rel="stylesheet" href="../js/jquery/themes/ui-lightness/jquery.ui.core.css">


<script type="text/javascript">
        jQuery(document).ready(
            function(){
                if (!Modernizr.input.autofocus) {
                    jQuery("input[autofocus]").focus(); // give focus to whichever element has the autofocus attribute
                }
                if (!Modernizr.input.placeholder) {
                    createPlaceholders();
                }
		jQuery("#loginForm").validate();
            }
            
        );
    </script> 
</head>
<body>
<div class="pageSizer">
		<div class="header"></div>
		<img src="../images/logo.png" />
<!-- 		<div class="main-message"> -->
<!-- 		 Welcome to feedback application of WSO2Con. We love to hear what you think about WSO2Con so we can make it a better experience for you next time.  -->
<!-- 		</div> -->
	<div class="main-message">
		Help us to make next year better, tell us what you think ...
		</div>
		<div class="loginBox">
			<form method="POST" action="../ProcessLogin" id="loginForm">
			<table>
				<tr>
					<td colspan="2" class="register-msg">
					Please login with <a href="https://wso2.org">Oxygen tank</a> credentials to provide feedback.<br /><br />
Not a member yet? <a href="https://wso2.org/user/register">Register here for free.</a>
					</td>
				</tr>
				<% if(request.getParameter("loginError") != null){ %>
				<tr>
					<td colspan="2">
						<label class="error">Access Denied! Please try again.</label>					
					</td>
				</tr>
				<% } %>
				<tr>
					<td class="textBox">Username:</td><td class="valueBox"><input id="name" type="text" name="name" class="required"   placeholder="Oxygentank Username" tabindex="1" /></td>
				</tr>
				<tr>
					<td class="textBox">Password:</td><td class="valueBox"><input type="password" name="password" class="required" placeholder="Oxygentank Password" tabindex="2" ></td>
				</tr>
				<tr>
					<td></td><td class="valueBox"><input  type="submit" value="Log In" class="button" tabindex="3"  ><input type="reset" value="Cancel" class="button" tabindex="4"  ></td>
				</tr>
			</table>
			</form>
		</div>
	</div>		
        
</body>
</html>
