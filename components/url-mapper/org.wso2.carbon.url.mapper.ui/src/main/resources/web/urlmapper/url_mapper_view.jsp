<!--
 ~ Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page
	import="org.wso2.carbon.url.mapper.stub.types.carbon.MappingData"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.utils.multitenancy.CarbonContextHolder"%>
<%@ page
	import="org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<!--         <script type="text/javascript" src="../admin/dialog/js/dialog.js"></script> -->
<%@ page import="org.wso2.carbon.utils.CarbonUtils"%>
<%@ page import="org.wso2.carbon.url.mapper.ui.UrlMapperServiceClient"%>
<%
	String backendServerURL = CarbonUIUtil
			.getServerURL(config.getServletContext(), session);
	ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
			.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
	String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

	UrlMapperServiceClient hostAdmin = new UrlMapperServiceClient(cookie, backendServerURL,
			configContext);
	MappingData hosts[] = null;
	String referance = "";
	try {

		hosts = hostAdmin.getAllMappings();

	} catch (Exception e) {
		CarbonUIMessage.sendCarbonUIMessage(e.getLocalizedMessage(), CarbonUIMessage.ERROR,
				request, e);
%>
<script type="text/javascript">
	location.href = "../admin/error.jsp";
</script>
<%
	return;
	}
%>

<fmt:bundle basename="org.wso2.carbon.url.mapper.ui.i18n.Resources">
	<carbon:breadcrumb label="url.mapping"
		resourceBundle="org.wso2.carbon.url.mapper.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />
	<div id="middle">


		<h2>
			<fmt:message key="url.mapping" />
		</h2>
		<div id="workArea">
		
			<br />

			<%
				if (hosts == null || hosts.length == 0) {
			%>
			<fmt:message key="no.mappings.found" />
			<%
				} else {
			%>
			<table class="styledLeft">
				<thead>
					<tr>
						<th><b><fmt:message key="host.name" /> </b></th>
						<th><b><fmt:message key="url" /> </b></th>
						<th><b><fmt:message key="tenant.domain" /> </b></th>

					</tr>
				</thead>
				<%
					int index = -1;
							for (MappingData host : hosts) {
								++index;
								if (index % 2 != 0) {
				%>
				<tr>
					<%
						} else {
					%>
				
				<tr bgcolor="#eeeffb">
					<%
						}
									if (hosts == null || hosts[0] == null ) {
					%>
					<td colspan="3"><fmt:message key="no.mappings.found" /></td>
					<%
						} else {
					%>
					<td><%=host.getMappingName()%></td>
					<td><%=host.getUrl()%></td>
					<td><%=host.getTenantDomain()%></td>
					<%
						}
					%>
				</tr>
				<%
					}
						}
				%>



			</table>

		</div>
	</div>
</fmt:bundle>