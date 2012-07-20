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
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>

<%

    String policyRequest = CharacterEncoder.getSafeText(request.getParameter("txtRequest"));
    String forwardTo = request.getParameter("forwardTo");
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	String resp = null;
	String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    List<RowDTO> rowDTOs = new ArrayList<RowDTO>();
    String resourceNames = CharacterEncoder.getSafeText(request.getParameter("resourceNames"));
    String subjectNames = CharacterEncoder.getSafeText(request.getParameter("subjectNames"));
    String actionNames = CharacterEncoder.getSafeText(request.getParameter("actionNames"));
    String environmentNames = CharacterEncoder.getSafeText(request.getParameter("environmentNames"));

    if (resourceNames != null  && !resourceNames.trim().equals("")){
        RowDTO rowDTO = new RowDTO();
        rowDTO.setAttributeValue(resourceNames);
        rowDTO.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
        rowDTO.setAttributeId("urn:oasis:names:tc:xacml:1.0:resource:resource-id");
        rowDTO.setCategory("urn:oasis:names:tc:xacml:3.0:attribute-category:resource");
        rowDTOs.add(rowDTO);
        session.setAttribute("resourceNames",resourceNames);
    }
    if (subjectNames != null  && !subjectNames.trim().equals("")){
        RowDTO rowDTO = new RowDTO();
        rowDTO.setAttributeValue(subjectNames);
        rowDTO.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
        rowDTO.setAttributeId("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
        rowDTO.setCategory("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject");
        rowDTOs.add(rowDTO);
        session.setAttribute("subjectNames",subjectNames);
    }
    if (actionNames != null  && !actionNames.trim().equals("")){
        RowDTO rowDTO = new RowDTO();
        rowDTO.setAttributeValue(actionNames);
        rowDTO.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
        rowDTO.setAttributeId("urn:oasis:names:tc:xacml:1.0:action:action-id");
        rowDTO.setCategory("urn:oasis:names:tc:xacml:3.0:attribute-category:action");
        rowDTOs.add(rowDTO);
        session.setAttribute("actionNames",actionNames);
    }
    if (environmentNames != null  && !environmentNames.trim().equals("")){
        RowDTO rowDTO = new RowDTO();
        rowDTO.setAttributeValue(environmentNames);
        rowDTO.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
        rowDTO.setAttributeId("urn:oasis:names:tc:xacml:1.0:environment:environment-id");
        rowDTO.setCategory("urn:oasis:names:tc:xacml:3.0:attribute-category:environment");
        rowDTOs.add(rowDTO);
        session.setAttribute("environmentNames",environmentNames);
    }

    RequestElementDTO requestElementDTO = new RequestElementDTO();
    requestElementDTO.setRowDTOs(rowDTOs);

    EntitlementPolicyCreator entitlementPolicyCreator = new EntitlementPolicyCreator();

    try {

    	EntitlementServiceClient client = new EntitlementServiceClient(cookie, serverURL, configContext);
        if(policyRequest == null || policyRequest.trim().length() < 1){
            String createdRequest = entitlementPolicyCreator.createBasicRequest(requestElementDTO);
            if(createdRequest != null && createdRequest.trim().length() > 0){
                policyRequest = createdRequest.trim().replaceAll("><", ">\n<");
            }
        }
        resp = client.getDecision(policyRequest);
        session.setAttribute("txtRequest", policyRequest);
    	CarbonUIMessage.sendCarbonUIMessage(resp, CarbonUIMessage.INFO, request);
    	if (forwardTo == null) {
            forwardTo = "create-evaluation-request.jsp";
    	}
    } catch (Exception e) {
    	String message = resourceBundle.getString("invalid.request");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        if (forwardTo == null) {
            forwardTo = "create-evaluation-request.jsp";
     	}       
    }
%>

<%@page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementServiceClient"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="org.wso2.carbon.identity.entitlement.ui.dto.BasicRequestDTO" %>
<%@page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyCreator" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.RowDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.RequestElementDTO" %>
<script
	type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
	}
</script>

<script type="text/javascript">
	forward();
</script>