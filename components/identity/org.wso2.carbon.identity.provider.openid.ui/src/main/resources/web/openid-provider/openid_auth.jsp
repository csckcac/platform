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
<jsp:include page="../dialog/display_messages.jsp"/>

<%@page import="org.openid4java.message.ParameterList" %>
<%@page import="org.wso2.carbon.identity.provider.openid.ui.handlers.OpenIDUtil"%>
<script type="text/javascript" src="../dialog/js/dialog.js"></script>

<link
        media="all" type="text/css" rel="stylesheet"
        href="css/registration.css"/>

<%
    ParameterList requestp = (ParameterList) session
            .getAttribute("parameterlist");
    String openidrealm = requestp.hasParameter("openid.realm") ? requestp
            .getParameterValue("openid.realm")
            : null;
    String openidreturnto = requestp.hasParameter("openid.return_to") ? requestp
            .getParameterValue("openid.return_to")
            : null;
    String openidclaimedid = requestp.hasParameter("openid.claimed_id") ? requestp
            .getParameterValue("openid.claimed_id")
            : null;
    String openididentity = requestp.hasParameter("openid.identity") ? requestp
            .getParameterValue("openid.identity")
            : null;
    
    String userName = null;
    if(openididentity != null){
        userName = OpenIDUtil.getUserName(openididentity);
    }          
    
    String openidrp  = openidreturnto;
    if (openidrp!=null && openidreturnto.indexOf("?")>0){
    	openidrp = openidreturnto.substring(0,openidreturnto.indexOf("?"));
    }
    
    String site = (String) (openidrealm == null ? openidreturnto
            : openidrealm);
    session.setAttribute("openId", openididentity);
 
 %>


<fmt:bundle  basename="org.wso2.carbon.identity.provider.openid.ui.i18n.Resources">

      
<script type="text/javascript">

    function submitform()
    {    	
        document.multifactorInfoCardAuthForm.submit();
    }
    function doLogin(){
        var loginForm = document.getElementById('loginForm');

        var loadingImg = document.getElementById('loadingImg');
        var loginTable = document.getElementById('loginTable');
        
        loginTable.style.display = "none";
        loadingImg.style.display = "";
        loginForm.submit();
    }

    function setRememberMe() {
        var val = document.getElementById("chkRemember").checked;
        var remMe = document.getElementById("remember");

        if (val) {
            remMe.value = "true";
        } else {
            remMe.value = "false";
        }
    }
</script>

    <div id="middle">
        <h2><fmt:message key='signin.with.openid'/></h2>

        <div id="workArea">
            <fmt:message key='signin.to.authenticate1'/><strong>"<%=openidrp%>"</strong><fmt:message key='signin.to.authenticate2'/><strong>"<%=openididentity%>"</strong>.
            <br/><br/>

            <table style="width:100%">
                <tr>
                    <td style="width:50%" id="loginTable">
                        <form action="openid_auth_submit.jsp" method="post" id="loginForm" onsubmit="doLogin()">
                            <input type="hidden" id='openid' name='openid' value="<%=openididentity%>"/>
                            <input type="hidden" id="remember" name="remember" value="false" />
                            
                            <div id="loginbox" class="identity-box">
                                <strong id="loginDisplayText"><fmt:message key='enter.password.to.signin'/></strong>

                                <h2></h2>
                                <table id="loginTable">
                                    <tr height="20">
                                        <td colspan="2"></td>
                                    </tr>
                                        <%

                                         if(userName == null || "".equals(userName.trim())){

                                        %>
                                        <tr>
                                            <td>User Name</td>
                                        <td>
                                            <input id='userName' name="userName" size='30'/>
                                        </td>
                                        </tr>

                                        <tr>
                                             <td>Password</td>
                                        <td>
                                            <input type="password" id='password' name="password" size='30'/>
                                        </td>
                                        </tr>

                                        <tr>
                                         <td>
                                            <input type="button" value="<fmt:message key='login'/>" class="button" onclick="doLogin()">
                                        </td>
                                        </tr>
                                        <%
                                            }  else {
                                        %>
                                        <tr>
                                            <td>
                                                <input type="password" id='password' name="password" size='30'/>
                                            </td>
                                             <td>
                                                <input type="button" value="<fmt:message key='login'/>" class="button" onclick="doLogin()">
                                            </td>
                                        </tr>

                                        <%
                                            }
                                        %>

                                        <tr>
                                        <td colspan="2"><input type="checkbox" id="chkRemember" onclick="setRememberMe();">Remember	me on this computer</td>
                                       </tr>                                            
                                </table>
                            </div>
                        </form>
                    </td>
                    <td style="width:50%;display:none" id="loadingImg">
                        <form action="openid_auth_submit.jsp" method="post" id="loginForm" onsubmit="doLogin()">
                            <input type="hidden" id='openid' name='openid' value="<%=openididentity%>"/>
                            
                            <div id="loginbox" class="identity-box">
                                <strong id="loginDisplayText"><fmt:message key='authenticating.please.wait'/></strong>

                                <h2></h2>
				<div style="padding-left:30px; padding-top:25px;">
                                	<img src="images/ajax-loader.gif" vspace="20" />
                            	</div>
                                
                            </div>
                        </form>
                    </td>
                    <td width="50%" style="text-align:center !important">
                        <form name="multifactorInfoCardAuthForm" action="multifactor_auth_infocard.jsp" method="POST">
                            <div id="loginbox" class="identity-box">
                                <strong><fmt:message key='signin.with.infocard'/></strong>

                                <h2></h2>
                                <img src="images/infocard-logo.png" border="0" onclick="submitform();">
                            </div>
                        </form>
                    </td>
                </tr>
            </table>


        </div>
    </div>
</fmt:bundle>