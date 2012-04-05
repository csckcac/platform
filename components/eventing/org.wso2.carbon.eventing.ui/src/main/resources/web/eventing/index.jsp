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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@page import="org.wso2.carbon.eventing.ui.EventingAdminClient"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%--<jsp:include page="../admin/layout/ajaxheader.jsp" />--%>


<carbon:breadcrumb 
		label="eventing.subscribers"
		resourceBundle="org.wso2.carbon.eventing.ui.i18n.Resources"
		topPage="false" 
		request="<%=request%>" />

	<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
	<script type="text/javascript" src="../admin/js/cookies.js"></script>
	<script type="text/javascript" src="../admin/js/main.js"></script>

	<%
		EventingAdminClient client = null;
		String backendServerURL = null;
		ConfigurationContext configContext = null;
		String cookie = null;
		String[] validSuscribers = null;
		String[] expiredSuscribers = null;
		String serviceName = null;
		int count = 0;

		serviceName = request.getParameter("serviceName");

		backendServerURL = CarbonUIUtil.getServerURL(config
				.getServletContext(), session);
		configContext = (ConfigurationContext) config.getServletContext()
				.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

		try {
			cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
			client = new EventingAdminClient(cookie, backendServerURL,
			configContext);
			validSuscribers = client.getValidSubscriptions(serviceName);
			expiredSuscribers = client.getExpiredSubscriptions(serviceName);
		} catch (Exception e) {
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
            <jsp:include page="../admin/error.jsp"/>
<%
            return;
        }
%>

<fmt:bundle basename="org.wso2.carbon.eventing.ui.i18n.Resources">
	<div id="middle">
	<h2><fmt:message key="eventing.service.subscriptions"/></h2>

	<div id="workArea">

	<table class="styledLeft" width="100%" id="validsubscriptions">
		<thead>
			<tr>
				<th><fmt:message key="valid.subscriptions"/></th>
			</tr>
		</thead>
		<tbody>
			<%
			if (validSuscribers != null && validSuscribers.length > 0) {
				for (String validSuscriber : validSuscribers) {
					if (validSuscriber != null) {
						count += 1;
			%>
			<tr>
				<td><a href="subscription_details.jsp?serviceName=<%=serviceName%>&subscriptionId=<%=validSuscriber%>"><%=validSuscriber%></a></td>
			</tr>
			<%
					}
				  }
				} if (count == 0) {
			%>
			<tr>
				<td><h4><fmt:message key="no.valid.subscriptions"/></h4></td>
			</tr>
			<%
			}
			%>
		</tbody>
	</table>
	
	<br/>
	
	<table class="styledLeft" width="100%" id="expiredsubscriptions">
		<thead>
			<tr>
				<th><fmt:message key="expired.subscriptions"/></th>
			</tr>
		</thead>
		<tbody>
			<%
			count = 0;
			if (expiredSuscribers != null && expiredSuscribers.length > 0) {
				for (String expiredSuscriber : expiredSuscribers) {
					if (expiredSuscriber != null) {
						count += 1;
			%>
			<tr><td><a href="subscription_details.jsp?serviceName=<%=serviceName%>&subscriptionId=<%=expiredSuscriber%>"><%=expiredSuscriber%></a></td></tr>
			<%
						}
				  }
			}if (count == 0){
			%>
			<tr>
				<td><h4><fmt:message key="no.expired.subscriptions"/></h4></td>
			</tr>
			<%
			}
			%>
		</tbody>
	</table>
	<script type="text/javascript">
    			alternateTableRows('expiredsubscriptions', 'tableEvenRow', 'tableOddRow');
    			alternateTableRows('validsubscriptions', 'tableEvenRow', 'tableOddRow');
	</script>

</fmt:bundle>