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
<%@page import="org.wso2.carbon.identity.user.profile.ui.client.UserProfileCient"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.lang.Exception"%>
<%@page import="org.wso2.carbon.user.core.UserCoreConstants"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="org.wso2.carbon.ui.util.CharacterEncoder"%><script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp" />
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO" %>
<%@ page import="org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO" %>
<%@ page import="java.text.MessageFormat" %>

<%
	String profile = CharacterEncoder.getSafeText(request.getParameter("profile"));
    String username = CharacterEncoder.getSafeText(request.getParameter("username"));
    String profileConfiguration = CharacterEncoder.getSafeText(request.getParameter("profileConfiguration"));
	UserFieldDTO[] fieldDTOs = null;
	UserProfileDTO profileDTO = null;
	String forwardTo = null;
    String BUNDLE = "org.wso2.carbon.identity.user.profile.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
	String fromUserMgt = (String) request.getParameter("fromUserMgt");
	
    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        UserProfileCient client = new UserProfileCient(cookie, backendServerURL, configContext);    
        
        try {
        	profileDTO = client.getUserProfile(username,profile);
        	if (UserCoreConstants.DEFAULT_PROFILE.equals(profile)||profileDTO!=null && profileDTO.getProfileName()!=null) {  
        		 String message = resourceBundle.getString("user.profile.with.given.name.exists");
        		 CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
        	     forwardTo ="add.jsp?username="+username;
%>
<script type="text/javascript">
        location.href = "<%=forwardTo%>";
</script>
<%			return;
        	}
        }catch(Exception e) {
        	// ignore exception
        }
        
    	fieldDTOs = client.getProfileFieldsForInternalStore().getFieldValues();
    	
        if (fieldDTOs!=null)
        {
          for (UserFieldDTO field : fieldDTOs) {
            String value = request.getParameter(field.getClaimUri());
            field.setFieldValue(value);   
          }
        }
        
        UserProfileDTO userprofile = new UserProfileDTO();
        userprofile.setProfileName(profile);
        userprofile.setFieldValues(fieldDTOs); 
        userprofile.setProfileConifuration(profileConfiguration);
        client.setUserProfile(username, userprofile);
        String message = resourceBundle.getString("user.profile.added.successfully");
        CarbonUIMessage.sendCarbonUIMessage(message,CarbonUIMessage.INFO, request);
        if ("true".equals(fromUserMgt)) {
        	forwardTo ="index.jsp?username="+username;
        }else{
        	forwardTo ="index.jsp?region=region5&item=userprofiles_menu&ordinal=0";        	
        }
    } catch (Exception e) {
        String message = MessageFormat.format(resourceBundle.getString(
            "error.while.updating.user.profile"), username, e.getMessage());
    	CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "edit.jsp?username=" + username + "&profile=" + profile + "&fromUserMgt=true";
    }
%>

<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>