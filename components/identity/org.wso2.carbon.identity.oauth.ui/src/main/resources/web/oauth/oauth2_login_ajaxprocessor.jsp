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
<%@ page import="java.net.URLDecoder" %>
<%@ page import="java.net.URLEncoder" %>
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
%>

<link media="all" type="text/css" rel="stylesheet" href="css/registration.css"/>
<fmt:bundle basename="org.wso2.carbon.identity.oauth.ui.i18n.Resources">

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>


    <div id="middle">

        <h2><fmt:message key="oauth.login"/></h2>

        <div id="workArea">
            <table style="width:100%">
                <tr>

                    <td style="width:50%">
                        <div id="loginbox" class="identity-box" style="height:160px;">
                            <script type="text/javascript">
                                function validate() {
                                    var username = document.getElementsByName("oauth_user_name")[0].value;
                                    if (username == '') {
                                        CARBON.showWarningDialog('<fmt:message key="username.required"/>');
                                        return false;
                                    }
                                    var password = document.getElementsByName("oauth_user_password")[0].value;
                                    if (password == '') {
                                        CARBON.showWarningDialog('<fmt:message key="password.required"/>');
                                        return false;
                                    }

                                    document.oauthsign.submit();
                                }

                                function deny(){
                                    document.getElementsByName("denied")[0].value = 'true';
                                    document.oauthsign.submit();
                                }
                            </script>

                            <table style="border:none !important;width: 100%" class="styledLeft">

                                <tr>
                                    <td>
                                        <b><%=(String) oauth2Params.getApplicationName() + " "%><fmt:message
                                                key='oauth.signin.message'/><%=scopeString%>
                                        </b></td>
                                </tr>

                            </table>

                            <form method="post" name="oauthsign" action="oauth2-login-finish.jsp">
                                <%
                                    if (cssLocation != null) {
                                %>
                                <input type="hidden" name="forwardPage"
                                       value="<%=URLEncoder.encode(forwardPage,"UTF-8")%>"/>
                                <input type="hidden" name="css"
                                       value="<%=URLEncoder.encode(cssLocation,"UTF-8")%>"/>
                                <input type="hidden" name="denied" id="denied" value="false"/>
                                <%
                                    }
                                %>
                                <table class="styledLeft noBorders" style="border:none !important;">
                                    <tr>
                                        <td class="leftCol-small"><fmt:message
                                                key='user.name'/></td>
                                        <td>
                                            <input class='text-box-big' id='oauth_user_name'
                                                   name="oauth_user_name" size='30'/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="leftCol-small"><fmt:message key='password'/></td>
                                        <td>
                                            <input class='text-box-big' id='oauth_user_password'
                                                   name="oauth_user_password" size='30'
                                                   type="password"/>
                                        </td>
                                    </tr>
                                    <tr>

                                        <td class="buttonRow" colspan="2">
                                            <input name="adduser" type="button" class="button"
                                                   value="<fmt:message key='allow'/>"
                                                   onclick="validate();"/>
                                            <input type="button" class="button"
                                                   onclick="deny();"
                                                   value="<fmt:message key='deny'/>"/>
                                        </td>
                                    </tr>

                                </table>
                            </form>
                    </td>
                </tr>
            </table>

        </div>

    </div>
</fmt:bundle>
<%
    if (cssLocation != null) {
        // Closing HTML page tags.
%>
<div class="footer-content">
    <div class="copyright">&copy; 2008 - 2011 WSO2 Inc. All Rights Reserved.</div>
</div>
</body>
</html>
<%
    }
%>