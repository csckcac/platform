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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.openid4java.message.ParameterList" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.base.IdentityConstants" %>
<%@ page import="org.wso2.carbon.identity.provider.openid.stub.dto.OpenIDClaimDTO" %>


<%@page import="org.wso2.carbon.identity.provider.openid.stub.dto.OpenIDUserProfileDTO" %>
<%@page
        import="org.wso2.carbon.identity.provider.openid.ui.client.OpenIDAdminClient" %>
<%@page
        import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>

<link media="all" type="text/css"
      rel="stylesheet" href="css/registration.css"/>

<script type="text/javascript">

    function Set_Cookie(name, value, expires, path, domain, secure) {
        // set time, it's in milliseconds
        var today = new Date();
        today.setTime(today.getTime());

        /*
         if the expires variable is set, make the correct
         expires time, the current script below will set
         it for x number of days, to make it for hours,
         delete * 24, for minutes, delete * 60 * 24
         */
        if (expires) {
            expires = expires * 1000 * 60 * 60 * 24;
        }
        var expires_date = new Date(today.getTime() + (expires));

        document.cookie = name + "=" + value +
                ( ( expires ) ? ";expires=" + expires_date.toGMTString() : "" ) +
                ( ( path ) ? ";path=" + path : "" ) +
                ( ( domain ) ? ";domain=" + domain : "" ) +
                ( ( secure ) ? ";secure" : "" );
    }

    function Get_Cookie(name) {
        var start = document.cookie.indexOf(name + "=");
        var len = start + name.length + 1;
        if (( !start ) &&
                ( name != document.cookie.substring(0, name.length) )) {
            return null;
        }
        if (start == -1) return null;
        var end = document.cookie.indexOf(";", len);
        if (end == -1) end = document.cookie.length;
        return unescape(document.cookie.substring(len, end));
    }


    //this deletes the cookie when called
    function Delete_Cookie(name, path, domain) {
        if (Get_Cookie(name)) document.cookie = name + "=" +
                ( ( path ) ? ";path=" + path : "") +
                ( ( domain ) ? ";domain=" + domain : "" ) +
                ";expires=Thu, 01-Jan-1970 00:00:01 GMT";
    }

</script>

<%
    OpenIDUserProfileDTO[] profiles = null;
    String BUNDLE = "org.wso2.carbon.identity.provider.openid.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    if (session.getAttribute("profiles") == null) {
        ParameterList requestedClaims = null;

        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String forwardTo = null;

        String isPhishingResistance = (String) session.getAttribute("papePhishingResistance");
        String isMultiFactorAuthEnabled = (String) session.getAttribute("multiFactorAuth");


        String infoCardAuthStatus = (String) session.getAttribute("infoCardAuthenticated");
        boolean infoCardAuthenticated = false;
        if (infoCardAuthStatus != null && infoCardAuthStatus.equals("true")) {
            infoCardAuthenticated = true;
        }


        String openid = (request.getParameter("openid") != null) ? request.getParameter("openid") : (String) session.getAttribute("openId");
   
        try {
            OpenIDAdminClient client = new OpenIDAdminClient(configContext, serverURL,null);
            if (infoCardAuthenticated || session.getAttribute("isOpenIDAuthenticated")!=null) {
                requestedClaims = (ParameterList) session.getAttribute(IdentityConstants.OpenId.PARAM_LIST);
                profiles = client.getUserProfiles(openid,requestedClaims);
                session.setAttribute("profiles", profiles);
                session.setAttribute("selectedProfile","default");
                session.setAttribute("_action", "complete");
                session.removeAttribute("isOpenIDAuthenticated");
                if (isPhishingResistance != null && isPhishingResistance.equals("true")) {
                    session.setAttribute(IdentityConstants.PHISHING_RESISTANCE, IdentityConstants.TRUE);
                }

                if (isMultiFactorAuthEnabled != null && isMultiFactorAuthEnabled.equals("true")) {
                    session.setAttribute(IdentityConstants.MULTI_FACTOR_AUTH, IdentityConstants.TRUE);
                }
             } else {
            	String message = resourceBundle.getString("invalid.credentials");
                CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, new Exception(message));
%>
<script type="text/javascript">
    location.href = "openid_auth.jsp";
</script>
<% return;
}
} catch (Exception e) {
%>
<script type="text/javascript">
    location.href = "../admin/login.jsp";
</script>
<% return;
}
finally {
    session.removeAttribute("papePhishingResistance");
    session.removeAttribute("multiFactorAuth");
    session.removeAttribute("infoCardBasedMultiFacotrAuth");
    session.removeAttribute("xmppBasedMultiFacotrAuth");
    session.removeAttribute("infoCardAuthenticated");
	session.removeAttribute("isOpenIDAuthenticated");
}
}
    else{
       profiles = (OpenIDUserProfileDTO[])session.getAttribute("profiles");
}
%>
<fmt:bundle
        basename="org.wso2.carbon.identity.provider.openid.ui.i18n.Resources">
    <link media="all" type="text/css" rel="stylesheet" href="css/.css">
    <link media="all" type="text/css" rel="stylesheet"
          href="css/openid-provider.css">
    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>

    <script type="text/javascript">
        function submitProfileSelection() {
            document.profileSelection.submit();
        }
    </script>


    <div id="middle">
        <h2>OpenID User Profile</h2>

        <div id="workArea">
            <script type="text/javascript">
                function approved() {
                <%
                    session.setAttribute("profile", request.getParameter("selectedProfile"));
                    session.setAttribute("userApproved","true");
                 %>
                    document.profile.submit();
                }
            </script>


            <form name="profileSelection" action="openid_profile_view.jsp" method="POST">
                <table style="margin-bottom:10px;">
                    <tr>
                        <td style="height:25px; vertical-align:middle;"><fmt:message key='profile'/></td>
                        <td style="height:25px; vertical-align:middle;"><select name="selectedProfile" onchange="submitProfileSelection();">
                            <% for (int i = 0; i < profiles.length; i++) {
                                OpenIDUserProfileDTO profileDTO = profiles[i];
                            if(request.getParameter("selectedProfile") == null){
                                if(profileDTO.getProfileName().equals("default")){
                            %>
                                 <option value="<%=profileDTO.getProfileName()%>" selected="selected"><%=profileDTO.getProfileName()%>
                                 </option>
                                <%}else{ %>
                                 <option value="<%=profileDTO.getProfileName()%>"><%=profileDTO.getProfileName()%></option> <%  
                                }
                            } else{
                             if(profileDTO.getProfileName().equals(request.getParameter("selectedProfile"))){
                            %>
                            <option value="<%=profileDTO.getProfileName()%>" selected="selected"><%=profileDTO.getProfileName()%>
                            </option>
                            <%}
                            else{
                              %><option value="<%=profileDTO.getProfileName()%>"><%=profileDTO.getProfileName()%></option><%
                            }
                            }
                            }%>
                        </select>
                        </td>
                    </tr>
                </table>
            </form>


            <form action="../../openidserver" id="profile" name="profile">

                <table cellpadding="0" cellspacing="0" class="card-box">
                    <tr>
                        <td><img src="images/card-box01.jpg"/></td>
                        <td class="card-box-top"></td>
                        <td><img src="images/card-box03.jpg"/></td>
                    </tr>
                    <tr>
                        <td class="card-box-left"></td>
                        <td class="card-box-mid">
                            <div class="user-pic"><img src="images/profile-picture.gif"
                                                       align="bottom"/></div>                       
                            <%
                                OpenIDUserProfileDTO[] profileSet = (OpenIDUserProfileDTO[]) session.getAttribute("profiles");
                                String selectedProfile = (request.getParameter("selectedProfile") != null)?request
                                        .getParameter("selectedProfile"):(String)session.getAttribute("selectedProfile");
                                for (int i = 0; i < profileSet.length; i++) {
                                    OpenIDUserProfileDTO profile = profileSet[i];
                                    if (profile.getProfileName().equals(selectedProfile)) {
                                        OpenIDClaimDTO[] claimSet = profile.getClaimSet();
                                        if(claimSet != null){
                                        for (int j = 0; j < claimSet.length; j++) {
                                            OpenIDClaimDTO claimDto = claimSet[j];
                            %>
                            <div><strong><%=claimDto.getDisplayTag()%>
                            </strong></div>
                            <div><%=claimDto.getClaimValue()%>
                            </div>
                            <br/><%
                                        }
                                        }
                                        break;
                                    }
                                }
                            %>
                        </td>
                        <td class="card-box-right"></td>
                    </tr>
                    <tr>
                        <td><img src="images/card-box07.jpg"/></td>
                        <td class="card-box-bottom"></td>
                        <td><img src="images/card-box05.jpg"/></td>
                    </tr>
                </table>
                <br/>
                <table width="100%" class="styledLeft">
                    <tbody>
                    <tr>
                        <td class="buttonRow" colspan="2"><input type="button"
                                                                 class="button" id="approve" name="approve"
                                                                 onclick="javascript: approved(); return false;"
                                                                 value="<fmt:message key='approve'/>"/>
                            <input class="button" type="reset" value="<fmt:message key='cancel'/>"
                                   onclick="javascript:document.location.href='../admin/login.jsp'"/>
                       </td>
                    </tr>
                    </tbody>
                </table>
            </form>

        </div>
    </div>
</fmt:bundle>
