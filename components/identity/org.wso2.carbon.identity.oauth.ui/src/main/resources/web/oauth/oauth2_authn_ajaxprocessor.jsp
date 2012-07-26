<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page import="org.wso2.carbon.identity.oauth.ui.OAuth2Parameters" %>
<%@ page import="org.wso2.carbon.identity.oauth.ui.OAuthConstants" %>
<%@ page import="java.util.Set" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%

    OAuth2Parameters oauth2Params;
    String scopeString = "";
    if (session.getAttribute(OAuthConstants.OAUTH2_PARAMS) != null) {
        oauth2Params = (OAuth2Parameters) session.getAttribute(OAuthConstants.OAUTH2_PARAMS);
        // build the scope string by joining all the scope parameters sent.
        Set<String> scopes = oauth2Params.getScopes();
        for (String scope : scopes) {
            scopeString = scope + "; ";
        }
        if (scopeString.endsWith(": ")) {
            scopeString = scopeString.substring(0, scopeString.lastIndexOf(": "));
        }

    } else {
        request.getSession().setAttribute(OAuthConstants.OAUTH_ERROR_CODE, "invalid_request");
        request.getSession().setAttribute(OAuthConstants.OAUTH_ERROR_MESSAGE,
                "OAuth Authorization Request is invalid!.");
        response.sendRedirect("../../carbon/oauth/oauth-error.jsp");
        return;
    }

    String authStatus = request.getParameter("auth_status");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>OAuth2 Login</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Le styles -->
    <link href="assets/css/bootstrap.min.css" rel="stylesheet">
    <link href="css/localstyles.css" rel="stylesheet">
    <!--[if lt IE 8]>
    <link href="css/localstyles-ie7.css" rel="stylesheet">
    <![endif]-->

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="assets/js/html5.js"></script>
    <![endif]-->
    <script src="assets/js/jquery-1.7.1.min.js"></script>
    <script src="js/scripts.js"></script>


</head>

<body>

<div class="header-strip">&nbsp;</div>
<div class="header-back">
    <div class="container">
        <div class="row">
            <div class="span4 offset3">
                <a class="logo">&nbsp</a>
            </div>
        </div>
    </div>
</div>
<div class="header-text">
    <strong><%=(String) oauth2Params.getApplicationName()%></strong> requests access to <strong><%=scopeString%></strong>
</div>

<div class="container">
    <div class="row">
        <div class="span5 offset3 content-section">
            <p class="download-info">
                <a class="btn btn-primary btn-large" id="authorizeLink"><i
                        class="icon-ok icon-white"></i> Authorize</a>
                <a class="btn btn-large" id="denyLink"><i class=" icon-exclamation-sign"></i>
                    Deny</a>
            </p>

            <form class="well form-horizontal" id="loginForm"
                  <% if(!("failed".equals(authStatus))) { %>style="display:none"<% } %>
                  action="oauth2-authn-finish.jsp">

                <div class="alert alert-error"
                     id="errorMsg" <% if (!("failed".equals(authStatus))) { %>
                     style="display:none" <% } %>>
                    <% if ("failed".equals(authStatus)) { %>Authentication Failure! Please check the
                    username and password.<% } %>
                </div>
                <!--Username-->
                <div class="control-group">
                    <label class="control-label" for="oauth_user_name">Username:</label>

                    <div class="controls">
                        <input type="text" class="input-large" id='oauth_user_name'
                               name="oauth_user_name">
                    </div>
                </div>

                <!--Password-->
                <div class="control-group">
                    <label class="control-label" for="oauth_user_password">Password:</label>

                    <div class="controls">
                        <input type="password" class="input-large" id='oauth_user_password'
                               name="oauth_user_password">
                    </div>
                </div>

                <div class="form-actions">
                    <button type="button" class="btn btn-primary" id="loginBtn">Login</button>
                    <button class="btn">Cancel</button>
                </div>
            </form>
        </div>
    </div>
</div>
<!-- /container -->


</body>
</html>
