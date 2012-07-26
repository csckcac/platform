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
<%@ page import="org.apache.amber.oauth2.common.exception.OAuthSystemException" %>
<%@ page import="org.wso2.carbon.identity.oauth.ui.OAuthConstants" %>
<%@ page import="org.wso2.carbon.identity.oauth.ui.client.OAuth2AuthzClient" %>
<%
    OAuth2AuthzClient authzClient = new OAuth2AuthzClient();
    String redirectUrl;
    try {
        redirectUrl = authzClient.handleAuthorizationRequest(request, response);
    } catch (OAuthSystemException e) {
        // System Error, redirect back to the server error page.
        session.setAttribute(OAuthConstants.OAUTH_ERROR_CODE, "server_error");
        session.setAttribute(OAuthConstants.OAUTH_ERROR_MESSAGE,
                "Error when completing the user authorization.");
        redirectUrl = "../../carbon/oauth/oauth-error.jsp";
    }
%>

<script type="text/javascript">
    function redirect() {
        location.href = "<%=redirectUrl%>";
    }
</script>
<script type="text/javascript">
    redirect();
</script>

