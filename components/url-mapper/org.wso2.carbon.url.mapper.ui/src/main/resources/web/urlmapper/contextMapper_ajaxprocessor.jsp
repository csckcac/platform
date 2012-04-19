
<!--
        ~ Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.utils.multitenancy.CarbonContextHolder"%>
<%@ page
	import="org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<script type="text/javascript" src="../admin/dialog/js/dialog.js"></script>
<%@ page import="org.wso2.carbon.utils.CarbonUtils"%>
<%@ page import="org.wso2.carbon.url.mapper.ui.UrlMapperServiceClient"%>

<%
	String carbonEndpoint = request.getParameter("carbonEndpoint");
	String requestType = request.getParameter("type");
	String backendServerURL = CarbonUIUtil
			.getServerURL(config.getServletContext(), session);
	ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
			.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
	String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

	UrlMapperServiceClient hostAdmin = new UrlMapperServiceClient(cookie, backendServerURL,
			configContext);
	String usergivenEndpoint = request.getParameter("userEndpoint");
	String oldHost = request.getParameter("oldHost");
	if (requestType.equalsIgnoreCase("edit")) {
		try {
			if (carbonEndpoint.contains("services")) {
				hostAdmin.editServiceDomain(usergivenEndpoint,oldHost);
			} else {
				hostAdmin.editHost(usergivenEndpoint,oldHost);
			}
			%>URL Mapping successfully edited.<%
	} catch (Exception e) {
			%>Failed to edit URL Mapping<%
	}
	} else if (requestType.equalsIgnoreCase("delete")) {
		try {
			if (carbonEndpoint.contains("services")) {
				hostAdmin.removeServiceDomain(usergivenEndpoint);
			} else {
				hostAdmin.deleteHost(usergivenEndpoint);
			}
			%>URL Mapping successfully deleted<%
	} catch (Exception e) {
			%>Failed to delete URL Mapping<%
	}
	}
	if (requestType.equalsIgnoreCase("add")) {
		
		String endpointType = request.getParameter("endpointType");

		if (carbonEndpoint.contains("services")) {
			if (carbonEndpoint != null && usergivenEndpoint != null && endpointType != null) {
				try {
					if (hostAdmin.isMappingExist(usergivenEndpoint)) {
					 %>Failed to add URL Mapping. Mapping already exist.<%
					} else {
							hostAdmin.addServiceDomain(usergivenEndpoint, carbonEndpoint);
							%>URL Mapping successfully inserted<%
					}

				} catch (Exception e) {
				  %>Failed to add URL Mapping. Mapping already exist.<%
				}
			} else {

			}

		} else if (requestType.contains("webapps") || !requestType.contains("services")) {

			if (carbonEndpoint != null && usergivenEndpoint != null && endpointType != null) {
				try {
					if (hostAdmin.isMappingExist(usergivenEndpoint)) {
						%>Failed to add URL Mapping. Mapping already exist.<%
						} else {
							hostAdmin.addWebAppToHost(usergivenEndpoint, carbonEndpoint);
							%>URL Mapping successfully inserted<%
						}

				} catch (Exception e) {
					%>Failed to add URL Mapping. Mapping already exist.<%
				}
			} else {

			}

		}

	}
%>




