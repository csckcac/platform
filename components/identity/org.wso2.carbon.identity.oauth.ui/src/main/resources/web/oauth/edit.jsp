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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<%@page import="org.wso2.carbon.utils.ServerConstants" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.CarbonError" %>
<%@page import="java.lang.Exception" %>

<%@page import="java.util.ResourceBundle"%>
<%@page import="org.wso2.carbon.identity.oauth.ui.client.OAuthAdminClient"%>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp"/>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<%
    String consumerkey = (String) request.getParameter("consumerkey");
    OAuthConsumerAppDTO app = null;
    String forwardTo = null;
    String[] profileConfigs = null;
	String BUNDLE = "org.wso2.carbon.identity.oauth.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
	
    try {
        String cookie = (String) session
                .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config
                .getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config
                .getServletContext().getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);
        OAuthAdminClient client = new OAuthAdminClient(cookie,
                backendServerURL, configContext);
        app = client.getOAuthApplicationData(consumerkey);
    } catch (Exception e) {
    	String message = resourceBundle.getString("error.while.loading.app.data");
        CarbonUIMessage.sendCarbonUIMessage(message,CarbonUIMessage.ERROR, request);
        forwardTo = "../admin/error.jsp";
%>

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

<script type="text/javascript">
</script>


<%@page import="org.wso2.carbon.user.core.UserCoreConstants" %>
<%@ page import="org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO" %>
<%@ page import="org.wso2.carbon.identity.oauth.ui.util.OAuthUIUtil" %>
<%@ page import="org.wso2.carbon.identity.oauth.ui.OAuthConstants" %>
<fmt:bundle basename="org.wso2.carbon.identity.oauth.ui.i18n.Resources">
    <carbon:breadcrumb label="app.settings"
                       resourceBundle="org.wso2.carbon.identity.oauth.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>

    <div id="middle">

        <h2><fmt:message key='view.application'/></h2>

        <div id="workArea">
   			<script type="text/javascript">
                function validate() {
                    var value = document.getElementsByName("callback")[0].value;
                    if (value == '') {
                        CARBON.showWarningDialog('<fmt:message key="callback.is.required"/>');
                        return false;
                    }
                    document.editAppform.submit();
                }
            </script>

            <form method="post" name="editAppform"  action="edit-finish.jsp"  target="_self">
                <table style="width: 100%" class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key='app.settings'/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
			<td class="formRow">
				<table class="normal" cellspacing="0">
                            <tr>
                                <td class="leftCol-small"><fmt:message key='oauth.version'/></td>
                                <td><%=app.getOAuthVersion()%><input id="application" name="oauthVersion"
                                                                        type="hidden" value="<%=app.getOAuthVersion()%>" /></td>
                            </tr>
				           <tr>
		                        <td class="leftCol-small"><fmt:message key='application.name'/></td>
		                        <td><%=app.getApplicationName()%><input id="application" name="application"
		                                   type="hidden" value="<%=app.getApplicationName()%>" /></td>
		                    </tr>
		                    <tr>
		                        <td class="leftCol-small"><fmt:message key='callback'/><font class="required">*</font></td>
		                        <td><input class="text-box-big" id="callback" name="callback"
		                                   type="text" value="<%=app.getCallbackUrl()%>" /></td>
		                    </tr>
		                     <tr>
		                        <td class="leftCol-small"><fmt:message key='consumerkey'/></td>
		                        <td><%=app.getOauthConsumerKey()%>
		                        <input id="consumerkey" name="consumerkey"
		                                   type="hidden" value="<%=app.getOauthConsumerKey()%>" /></td>
		                    </tr>
		                     <tr>
		                        <td class="leftCol-small"><fmt:message key='consumersecret'/></td>
		                        <td><%=app.getOauthConsumerSecret()%>
		                        <input id="consumersecret" name="consumersecret"
		                                   type="hidden" value="<%=app.getOauthConsumerSecret()%>" /></td>
		                    </tr>
		                    <tr>
		                        <td class="leftCol-small"><fmt:message key='accesstoken'/></td>
		                        <td><%=OAuthUIUtil.getAbsoluteEndpointURL(
                                        OAuthConstants.ACCESS_TOKEN_URL, app.getOAuthVersion(), request)%></td>
		                    </tr>
	                    	<tr>
		                        <td class="leftCol-small"><fmt:message key='authorizeurl'/></td>
		                        <td><%=OAuthUIUtil.getAbsoluteEndpointURL(
                                        OAuthConstants.AUTHORIZE_TOKEN_URL, app.getOAuthVersion(), request)%></td>
		                    </tr>
		                      <tr>
		                        <td class="leftCol-small"><fmt:message key='requesttokenurl'/></td>
		                        <td><%=OAuthUIUtil.getAbsoluteEndpointURL(
                                        OAuthConstants.REQUEST_TOKEN_URL, app.getOAuthVersion(), request)%></td>
		                    </tr>
				</table>
			</td>
		    </tr>
                    <tr>
                        <td class="buttonRow">
                           <input name="update"
                                   type="button" class="button" value="<fmt:message key='update'/>" onclick="validate();"/>
                           <input type="button" class="button"
                                              onclick="javascript:location.href='index.jsp?region=region5&item=userprofiles_menu&ordinal=0'"
                                          value="<fmt:message key='cancel'/>"/></td>
                    </tr>
                    </tbody>
                </table>

            </form>
        </div>
    </div>
</fmt:bundle>

