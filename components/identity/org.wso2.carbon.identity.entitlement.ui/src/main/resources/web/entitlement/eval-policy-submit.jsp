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

    BasicRequestDTO basicRequestDTO = new BasicRequestDTO();
    String resourceNames = CharacterEncoder.getSafeText(request.getParameter("resourceNames"));
    String subjectNames = CharacterEncoder.getSafeText(request.getParameter("subjectNames"));
    String attributeId = CharacterEncoder.getSafeText(request.getParameter("attributeId"));
    String userAttributeValue = CharacterEncoder.getSafeText(request.getParameter("userAttributeValue"));
    String actionNames = CharacterEncoder.getSafeText(request.getParameter("actionNames"));
    String environmentNames = CharacterEncoder.getSafeText(request.getParameter("environmentNames"));
    String policy = CharacterEncoder.getSafeText(request.getParameter("txtPolicy"));
    String forwardTo = request.getParameter("forwardTo");

    if (resourceNames != null  && resourceNames.trim().length() > 0){
        basicRequestDTO.setResources(resourceNames);
        session.setAttribute("resourceNames",resourceNames);
    }
    if (subjectNames != null  && subjectNames.trim().length() > 0){
        basicRequestDTO.setSubjects(subjectNames);
        session.setAttribute("subjectNames",subjectNames);
    }
    if (attributeId != null  && attributeId.trim().length() > 0){
        basicRequestDTO.setUserAttributeId(attributeId);
        session.setAttribute("attributeId",attributeId);
    }
    if (userAttributeValue != null  && userAttributeValue.trim().length() > 0){
        basicRequestDTO.setUserAttributeValue(userAttributeValue);
        session.setAttribute("userAttributeValue",userAttributeValue);
    }
    if (actionNames != null  && actionNames.trim().length() > 0){
        basicRequestDTO.setActions(actionNames);
        session.setAttribute("actionNames",actionNames);
    }
    
    if (environmentNames != null  && environmentNames.trim().length() > 0){
        basicRequestDTO.setEnviornement(environmentNames);
        session.setAttribute("environmentNames",environmentNames);
    }


    EntitlementPolicyCreator entitlementPolicyCreator = new EntitlementPolicyCreator();

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	String resp = null;
	String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    try {
        String createdRequest = entitlementPolicyCreator.createBasicRequest(basicRequestDTO);

    	EntitlementServiceClient client = new EntitlementServiceClient(cookie, serverURL, configContext);
        if(policy != null && !policy.equals("")){
            resp = client.getDecision(policy);
        } else {
            if(createdRequest != null && !createdRequest.equals("")){
                resp = client.getDecision(createdRequest);
                policy = createdRequest;
            }
        }
        session.setAttribute("policyreq", policy);
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
<script
	type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
	}
</script>

<script type="text/javascript">
	forward();
</script>