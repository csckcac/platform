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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<%@page import="org.wso2.carbon.utils.ServerConstants"%>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@page import="org.wso2.carbon.CarbonConstants"%>
<%@page import="org.wso2.carbon.CarbonError"%>
<%@page import="org.wso2.carbon.identity.user.profile.ui.client.UserProfileCient"%>
<%@page import="java.lang.Exception"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="org.wso2.carbon.ui.util.CharacterEncoder"%>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp" />
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@page import="org.wso2.carbon.user.core.UserCoreConstants"%>
<%@ page import="org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO" %>

<%
    boolean readOnlyUserStore = false;
    String username = CharacterEncoder.getSafeText(request.getParameter("username"));
    String forwardTo = null;
    String fromUserMgt = "true";
    UserProfileCient client = null;

    if (username == null) {
        username = (String) request.getSession().getAttribute("logged-user");
    }

    String addAction = "add.jsp?username="+username+"&fromUserMgt=false";

    if ("true".equals(fromUserMgt)) {
        addAction = "add.jsp?username=" + username+"&fromUserMgt=true";
    }

    UserProfileDTO[] profiles = new UserProfileDTO[0];
    String BUNDLE = "org.wso2.carbon.identity.user.profile.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    try {
        String cookie = (String) session
				.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config
				.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config
				.getServletContext().getAttribute(
						CarbonConstants.CONFIGURATION_CONTEXT);
        client = new UserProfileCient(cookie,
                backendServerURL, configContext);
        readOnlyUserStore = client.isReadOnlyUserStore();
     	profiles = client.getUserProfiles(username);
	} catch (Exception e) {
		String message = resourceBundle.getString("error.while.loading.user.profile.data");
		CarbonUIMessage.sendCarbonUIMessage(message,CarbonUIMessage.ERROR, request);
		forwardTo = "../admin/error.jsp";
	}
%>

<%
    if ( forwardTo != null) {
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
    return;
	}
%>

<fmt:bundle basename="org.wso2.carbon.identity.user.profile.ui.i18n.Resources">
    <carbon:breadcrumb
            label="user.profiles"
            resourceBundle="org.wso2.carbon.identity.user.profile.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
            
            <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
			<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
			<script type="text/javascript" src="../carbon/admin/js/main.js"></script>
			
    <div id="middle">
        <%
        	if ("true".equals(fromUserMgt)) {
        %>
       		<h2><fmt:message key='user.profiles1'/><%=username%></h2>
        <%
        	} else {
        %>
        <h2><fmt:message key='my.profiles'/></h2>
        <%
        	}
        %>
        <div id="workArea">   
            <script type="text/javascript">
              function remove(username,profile) {
                 if(profile == "default"){
                	 CARBON.showWarningDialog("<fmt:message key='cannot.remove.default.profile'/>", null, null);
                	 return;
                 }
        	     CARBON.showConfirmationDialog("<fmt:message key='remove.message1'/>"+ profile +"<fmt:message key='remove.message2'/>",
                    function() {
              	       location.href ="remove-profile.jsp?username="+username+"&profile="+profile+"&fromUserMgt=<%=fromUserMgt%>";
                     }, null);
                 }
            </script>
            <% if(!readOnlyUserStore) {%>
            <div style="height:30px;">
                <%if (client.isAddProfileEnabled()) {%>
                <a href="javascript:document.location.href='<%=addAction%>'" class="icon-link"
                   style="background-image:url(../admin/images/add.gif);"><fmt:message
                        key='add.new.profiles'/></a>
                <%}%>
            </div>
             <% } %>      
           	<table style="width: 100%" class="styledLeft">
			<thead>
				<tr>
					<th colspan="2"><fmt:message key='available.profiles'/></th>
				</tr>
			</thead>
				<tbody>
           <%
           	if (profiles != null && profiles.length > 0) {
           			for (int i = 0; i < profiles.length; i++) { 
           				String profileName = CharacterEncoder.getSafeText(profiles[i].getProfileName());
           %>		
			<tr>
			    <%
        	    if ("true".equals(fromUserMgt)) {
                %>
				<td width="50%"><a href="edit.jsp?username=<%=username%>&profile=<%=profileName%>&fromUserMgt=true"><%=profileName%></a></td>
				<%}else{ %>
				<td width="50%"><a href="edit.jsp?username=<%=username%>&profile=<%=profileName%>&fromUserMgt=false"><%=profileName%></a></td>
				<%} %>
				<td width="50%">
				<%
                    if (readOnlyUserStore == false) {
                %>
				<a title="<fmt:message key='remove.profile'/>"
                                   onclick="remove('<%=username%>','<%=profileName%>');return false;"
                                   href="#" style="background-image: url(../userprofile/images/delete.gif);" class="icon-link">
                                    <fmt:message key='delete'/></a>
                <%
                    }
                %>                    
               </td>			
			</tr>
		  <%
		  	}
		  		} else {
		  %>
		    <tr>
				<td width="100%" colspan="2"><i><fmt:message key='no.profiles'/></i></td>
			 </tr>
		  <%
		  	}
		  %>
		  </tbody>
		  </table>		
          </div>
    </div>
</fmt:bundle>
