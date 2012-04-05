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
<%@page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@page import="org.wso2.carbon.eventing.eventsource.ui.EventingSourceAdminClient"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.eventing.eventsource.stub.types.carbon.EventSourceDTO" %>

<%
		EventingSourceAdminClient client = null;
		String backendServerURL = null;
		ConfigurationContext configContext = null;
		String cookie = null;
	    EventSourceDTO eventSource = null;
		String name = null;
		String eventSourceType = null;
		String registryUrl = null;
		String namespace = null;
		String headerName =null;
		String password = null;
		String username = null;
		
       
		name = request.getParameter("name");
		eventSourceType = request.getParameter("eventsourcetype");
		
		if(eventSourceType==null){
			eventSourceType="DefaultInMemory";
		}
		
		registryUrl = request.getParameter("registryUrl");
		namespace = request.getParameter("namespace");
		headerName = request.getParameter("headerName");
		
		password = request.getParameter("pwd");
		username = request.getParameter("user");
		
		eventSource = new EventSourceDTO();
		eventSource.setType(eventSourceType);
		eventSource.setTopicHeaderNS(namespace);
		eventSource.setTopicHeaderName(headerName);
		eventSource.setName(name);
		eventSource.setRegistryUrl(registryUrl);
		eventSource.setUsername(username);
		eventSource.setPassword(password);

		backendServerURL = CarbonUIUtil.getServerURL(config
				.getServletContext(), session);
		configContext = (ConfigurationContext) config.getServletContext()
				.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

		try {
			cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
			client = new EventingSourceAdminClient(cookie, backendServerURL,configContext);
			client.saveEventSource(eventSource);
	%>
	 <script type="text/javascript">
			  location.href = "../event-source/index.jsp";
	 </script>
	<%
	        return;
		} catch (Exception e) {
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
            <jsp:include page="../admin/error.jsp"/>
<%
            return;
		}	
	%>