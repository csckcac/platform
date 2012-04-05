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
<%@page import="java.util.Map"%>
<%@page import="java.util.Iterator"%>

<%@page import="org.wso2.carbon.profiles.mgt.stub.dto.ClaimConfigurationDTO"%>
<%@page import="org.wso2.carbon.profiles.mgt.ui.client.ProfileMgtClient"%>
<%@page import="java.util.ResourceBundle"%><script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp" />
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>

<%
    String profile = request.getParameter("profile");
    String dialect = request.getParameter("dialect");
    ClaimConfigurationDTO[] configuration = (ClaimConfigurationDTO[])session.getAttribute("profileconfigurations");
    session.removeAttribute("profileconfigurations");
    String forwardTo=null;
    
    if (configuration==null) {
    	configuration = new ClaimConfigurationDTO[0] ;
    }
    
    for (int i=0;i<configuration.length;i++){    	
    	configuration[i].setBehavior(request.getParameter(configuration[i].getClaimUri()));
    }                         
    
    String BUNDLE = "org.wso2.carbon.profiles.mgt.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    
    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        ProfileMgtClient client = new ProfileMgtClient(cookie, backendServerURL, configContext);
        client.addProfile(profile, dialect, configuration);
        String message = resourceBundle.getString("profile.configuration.added.successfully");
        CarbonUIMessage.sendCarbonUIMessage(message,CarbonUIMessage.INFO, request);
        forwardTo = "index.jsp?region=region1&item=profile_mgt_menu&ordinal=0";
    } catch (Exception e) {
    	String message = resourceBundle.getString("error.while.adding.profile.configuration");
    	CarbonUIMessage.sendCarbonUIMessage(message,CarbonUIMessage.ERROR, request,e);
    	forwardTo = "index.jsp?region=region1&item=profile_mgt_menu&ordinal=0";
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