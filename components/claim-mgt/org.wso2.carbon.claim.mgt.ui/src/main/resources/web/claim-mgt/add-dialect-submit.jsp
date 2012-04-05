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
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>


<%@page import="java.lang.Exception"%>
<%@page import="org.wso2.carbon.claim.mgt.ui.client.ClaimAdminClient"%>

<%
	String serverURL = CarbonUIUtil.getServerURL(config
			.getServletContext(), session);
	ConfigurationContext configContext = (ConfigurationContext) config
			.getServletContext().getAttribute(
					CarbonConstants.CONFIGURATION_CONTEXT);
	String cookie = (String) session
			.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	String forwardTo = null;
	ClaimMappingDTO mapping = null;
	ClaimDTO claim = null;
	String displayName = request.getParameter("displayName");
	String description = request.getParameter("description");
	String claimUri = request.getParameter("claimUri");
	String dialect = request.getParameter("dialect");
	String attribute = request.getParameter("attribute");
	String regex = request.getParameter("regex");
	String supported = request.getParameter("supportedhidden");
	String required = request.getParameter("requiredhidden");
	String store = request.getParameter("store");
	String displayOrder = request.getParameter("displayOrder");
	ClaimDialectDTO dto = null;
	String BUNDLE = "org.wso2.carbon.claim.mgt.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

	try {
		ClaimAdminClient client = new ClaimAdminClient(cookie,
				serverURL, configContext);
		mapping = new ClaimMappingDTO();
		claim = new ClaimDTO();
		claim.setClaimUri(claimUri);
		claim.setDisplayTag(displayName);
		claim.setDescription(description);
		claim.setDialectURI(dialect);
		claim.setRegEx(regex);
		if (displayOrder.trim().length()==0){
			displayOrder="0";
		}
		claim.setDisplayOrder(Integer.parseInt(displayOrder));
		
		if ("true".equals(required)) {
			claim.setRequired(true);
		}

		if ("true".equals(supported)) {
			claim.setSupportedByDefault(true);
		}
		
		mapping.setClaim(claim);
		mapping.setMappedAttribute(attribute);
		
		dto = new ClaimDialectDTO();
		dto.addClaimMappings(mapping);
		dto.setDialectURI(dialect);
		dto.setUserStore(store);

		client.addNewClaimDialect(dto);
		forwardTo = "index.jsp?region=region1&item=claim_mgt_menu&ordinal=0";
	} catch (Exception e) {
		String message = resourceBundle.getString("error.adding.dialect");
		CarbonUIMessage.sendCarbonUIMessage(message,CarbonUIMessage.ERROR, request);
		forwardTo = "../admin/error.jsp";
	}
%>

<%@page import="org.wso2.carbon.claim.mgt.stub.dto.ClaimDialectDTO"%>
<%@page import="java.util.ResourceBundle"%>
<%@ page import="org.wso2.carbon.claim.mgt.stub.dto.ClaimMappingDTO" %>
<%@ page import="org.wso2.carbon.claim.mgt.stub.dto.ClaimDTO" %>
<script
	type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
