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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@page import="org.wso2.carbon.eventing.ui.EventingAdminClient"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="org.wso2.carbon.eventing.stub.service.dto.SubscriptionDTO" %>
<jsp:include page="../admin/layout/ajaxheader.jsp" />

<fmt:bundle basename="org.wso2.carbon.eventing.ui.i18n.Resources">

<carbon:breadcrumb 
		label="subscription.details"
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
		SubscriptionDTO details = null;
		String serviceName = null;
		String subscriptionId = null;

		serviceName = request.getParameter("serviceName");
		subscriptionId = request.getParameter("subscriptionId");

		backendServerURL = CarbonUIUtil.getServerURL(config
				.getServletContext(), session);
		configContext = (ConfigurationContext) config.getServletContext()
				.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

		try {
			cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
			client = new EventingAdminClient(cookie, backendServerURL,configContext);
			details = client.getSubscriptionDetails(serviceName,subscriptionId);			
		} catch (Exception e) {
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
            <jsp:include page="../admin/error.jsp"/>
<%
            return;
		}
	%>


	<div id="middle">
	<h2><fmt:message key="eventing.subscription.details"/></h2>

	<div id="workArea">

	<table class="styledLeft" width="100%" id="details">
		<thead>
			<tr>
				<th colspan="2"><fmt:message key="subscription.id"/><%=subscriptionId%></th>
			</tr>
		</thead>
		<tbody>
			<%
				if (details != null) {					
			%>
			<tr>
				<td>EPR</td>
				<td><%=details.getEpr()%></td>
			</tr>
			<tr>
				<td>End Date</td>
				<%
				 if (details.getSubscriptionEndString()!=null)
				 {
                     String expDate = details.getSubscriptionEndString();
				%>				
				<td><%=expDate%></td>
				<%
				 } else
				 {
				%>
				<td><fmt:message key="never.expires"/></td>
				<%
				 } 
				%>	
			</tr>
			<tr>
				<td><fmt:message key="delivery.mode"/></td>
				<td><%=details.getDiliveryMode()%></td>
			</tr>
                <%
                    if (details.getFilterValue()!=null){
                %>
                <tr>
                    <td>Filter</td>
                    <td><%=details.getFilterValue()%></td>
                </tr>
                <%
                    }
                    if (details.getDialect()!=null){
                %>
                <tr>
                    <td>Dialect</td>
                    <td><%=details.getDialect()%></td>
                </tr>
                <%
                    }
            } else {			
			%>
			<tr>
				<td><h4><fmt:message key="no.subscriptions.details"/></h4></td>
			</tr>
			<%
			}
			%>
        </tbody>
	</table>
	<script type="text/javascript">
    			alternateTableRows('details', 'tableEvenRow', 'tableOddRow');
	</script>

</fmt:bundle>