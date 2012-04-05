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
<%@ page import="org.wso2.carbon.identity.sso.saml.ui.SAMLSSOProviderConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<jsp:include page="carbon/dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.identity.sso.saml.ui.i18n.Resources">
    <%
        String errorMessage = "login.fail.message";
        boolean loginFailed = false;
        if (request.getAttribute(SAMLSSOProviderConstants.AUTH_FAILURE) != null &&
            (Boolean)request.getAttribute(SAMLSSOProviderConstants.AUTH_FAILURE)) {
            loginFailed = true;
            if(request.getAttribute(SAMLSSOProviderConstants.AUTH_FAILURE_MSG) != null){
                errorMessage = (String) request.getAttribute(SAMLSSOProviderConstants.AUTH_FAILURE_MSG);
            }
        }
    %>

    <script type="text/javascript">
        function doLogin() {
            var loginForm = document.getElementById('loginForm');
            loginForm.submit();
        }
    </script>

    <link media="all" type="text/css" rel="stylesheet" href="carbon/sso-saml/css/main.css"/>
<table id="main-table" border="0" cellspacing="0">
    <tbody><tr>
        <td id="header" colspan="3">
	    	<div id="header-div">
			<div class="right-logo">Management Console</div>
			<div class="left-logo">
		    		<div  class="header-home">&nbsp;</div>
			</div>
		
			<div class="header-links">
				<div class="right-links">            
			
				</div>
			</div>
	    	</div>

        </td>
    </tr>
    <tr>
	<td colspan="3" class="content">

		    <div id="middle">
        <h2><fmt:message key='saml.sso'/></h2>

        <div id="workArea">
            <table style="width:100%">
                <tr>
                    <td style="width:50%" id="loginTable">
                        <form action="samlsso" method="post" id="loginForm">
                            <div id="loginbox" class="identity-box">
                                <strong id="loginDisplayText"><fmt:message
                                        key='enter.password.to.signin'/></strong>
                                <h2></h2>
                                <table id="loginTable1">
                                    <tr height="22">
                                        <td colspan="2"></td>
                                    </tr>
                                    <% if (loginFailed) { %>
                                    <tr>
                                        <td colspan="2" style="color: #dc143c;"><fmt:message
                                                key='<%=errorMessage%>'/></td>
                                    </tr>
                                    <% } %>
                                    <tr>
                                    <td><fmt:message key='username'/></td>
                                        <td>
                                            <input type="text" id='username' name="username"
                                                   size='30'/>
                                            <input type="hidden" name="<%= SAMLSSOProviderConstants.ASSRTN_CONSUMER_URL %>"
                                                   value="<%= request.getAttribute(SAMLSSOProviderConstants.ASSRTN_CONSUMER_URL) %>"/>
                                            <input type="hidden" name="<%= SAMLSSOProviderConstants.ISSUER %>"
                                                   value="<%= request.getAttribute(SAMLSSOProviderConstants.ISSUER) %>"/>
                                            <input type="hidden" name="<%= SAMLSSOProviderConstants.REQ_ID %>"
                                                   value="<%= request.getAttribute(SAMLSSOProviderConstants.REQ_ID) %>"/>
                                            <input type="hidden" name="<%= SAMLSSOProviderConstants.SUBJECT %>"
                                                   value="<%= request.getAttribute(SAMLSSOProviderConstants.SUBJECT) %>"/>
                                            <input type="hidden" name="<%= SAMLSSOProviderConstants.RP_SESSION_ID %>"
                                                   value="<%= request.getAttribute(SAMLSSOProviderConstants.RP_SESSION_ID) %>"/>
                                            <input type="hidden" name="<%= SAMLSSOProviderConstants.ASSERTION_STR %>"
                                                   value="<%= request.getAttribute(SAMLSSOProviderConstants.ASSERTION_STR) %>"/>
                                            <input type="hidden" name="<%= SAMLSSOProviderConstants.RELAY_STATE %>"
                                                   value="<%= request.getAttribute(SAMLSSOProviderConstants.RELAY_STATE) %>"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><fmt:message key='password'/></td>
                                        <td>
                                            <input type="password" id='password' name="password"
                                                   size='30'/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td colspan="2">
                                            <input type="submit" value="<fmt:message key='login'/>"
                                                   class="button">
                                        </td>
                                    </tr>
                                </table>
                            </div>
                        </form>
                    </td>
                </tr>
            </table>
        </div>
    </div>
	</td>		
    </tr>
              
                        


    <tr>
        <td id="footer" colspan="3">

		<div id="footer-div">
			<div class="footer-content">
				<div class="copyright">
				    © 2008 - 2011 WSO2 Inc. All Rights Reserved.
				</div>
			</div>
		</div>
                        
	</td>
    </tr>
</tbody></table>



</fmt:bundle>
