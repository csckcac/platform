<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.user.registration.ui.client.UserRegistrationClient" %>
<%@ page import="org.wso2.carbon.identity.user.registration.ui.util.UserRegistrationUtils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<%
    String cssLocation = request.getParameter("css");
    if ("null".equals(cssLocation)) {
        cssLocation = null;
    }

    if (cssLocation != null) {
        cssLocation = URLDecoder.decode(cssLocation, "UTF-8");
    }

    String pageTitle = request.getParameter("title");

    String forwardPage = request.getParameter("forwardPage");
    if (forwardPage != null) {
        forwardPage = URLDecoder.decode(forwardPage, "UTF-8");
    }

    String required = null;
    String optional = null;
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);

    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String forwardTo = null;
    String BUNDLE = "org.wso2.carbon.identity.user.registration.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    try {
        UserRegistrationClient client =
                new UserRegistrationClient(cookie, serverURL, configContext);
        required = client.getRequiredFieldsForRegistration();
        optional = client.getOptionalFieldsForRegistration();
    } catch (Exception e) {
        String message = resourceBundle.getString("error.while.loading.user.registration.fields");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "index.jsp?region=region1&item=user_registration_menu&ordinal=0";
%>

<%@page import="java.util.ResourceBundle" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="java.net.URLEncoder" %>
<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
<%
    }
%>

<form name="frm" id="frm" method="post" action="infocard_accept.jsp"><input
        type="hidden" name="InfoCardSignin" value="Log in"/><br/>
    <input type="hidden" name="FromIdentityProvider" value="true"/><br/>
    <input type="hidden" name="AuthenticationMethod" value="infocard"/><br/>

    <%
        if (cssLocation != null) {
    %>
    <input type="hidden" name="forwardPage" value="<%=URLEncoder.encode(forwardPage,"UTF-8")%>"/>
    <input type="hidden" name="css" value="<%=URLEncoder.encode(cssLocation,"UTF-8")%>"/>
    <input type="hidden" name="title" value="<%=URLEncoder.encode(pageTitle,"UTF-8")%>"/>
    <%
        }
    %>

    <OBJECT type="application/x-informationCard" name="xmlToken">
        <PARAM Name="tokenType"
               Value="http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1">

        <PARAM Name="requiredClaims" Value='<%=required%>'>
        <%if (optional != null) { %>
        <PARAM Name='optionalClaims' value='<%=optional%>'>
        <%}%>
        <PARAM Name="issuer"
               value="http://schemas.xmlsoap.org/ws/2005/05/identity/issuer/self">
    </OBJECT>
</form>
<script language="JavaScript" type="text/JavaScript">
    <!--
    document.frm.submit();
    -->
</script>
