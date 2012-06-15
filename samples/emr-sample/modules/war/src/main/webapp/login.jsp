<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!--
  Copyright 2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!DOCTYPE html>
<html><head>
<title>Login</title>
<meta charset="UTF-8">
<meta name="description" content="" />
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js"></script>
<!--[if lt IE 9]><script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script><![endif]-->
<script type="text/javascript" src="js/prettify.js"></script>                                   <!-- PRETTIFY -->
<script type="text/javascript" src="js/kickstart.js"></script>                                  <!-- KICKSTART -->
<link rel="stylesheet" type="text/css" href="css/kickstart.css" media="all" />                  <!-- KICKSTART -->
<link rel="stylesheet" type="text/css" href="css/style.css" media="all" />                          <!-- CUSTOM STYLES -->
</head><body><a id="top-of-page"></a><div id="wrap" class="clearfix">
	<div class="col_12">
	<fmt:bundle basename="org.wso2.carbon.sample.emr.i18n.Resources">
<div id="middle">
<h4><fmt:message key="title.emr"/></h4>
</div>
<form style="width: 150px;" id="login-form" action="j_security_check" method="post" >
<table class="tight">
		<tr>	
		<td>	<label for="login">Username</label> </td>
		<td>	<input type="text" id="login" name="j_username"/><td>	
		</tr>
		<tr>		
		<td>	<label for="password">Password</label></td>
		<td>	<input type="password" id="password" name="j_password"/></td>
		</tr>
		<tr>	
		<td></td>
		<td align="right">	<input type="submit" style="height: 25px; width: 60px" class="button" name="commit" value="login"/></td>
		</tr>	
	</table>
	</form>
</fmt:bundle>
	</div>
</div><!-- END WRAP -->
</body></html>


