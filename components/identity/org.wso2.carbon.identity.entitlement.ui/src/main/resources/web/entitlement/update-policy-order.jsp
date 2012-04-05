<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page
        import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO" %>
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


<%
    String forwardTo = null;
    String policyOrder = request.getParameter("policyOrder");

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    try {
        EntitlementPolicyAdminServiceClient client =
                new EntitlementPolicyAdminServiceClient(cookie, serverURL, configContext);
            if(policyOrder != null && !policyOrder.equals("")){
                String[] policyIds = policyOrder.split(EntitlementPolicyConstants.ATTRIBUTE_SEPARATOR);
                boolean authorize = true;
                for (String policyId : policyIds) {
                    PolicyDTO policyDTO = client.getLightPolicy(policyId);
                    if (!policyDTO.getPolicyEditable()) {
                        authorize = false;
                        break;
                    }
                }
                if(authorize){
                    for(int i = 0; i < policyIds.length; i ++){
                        PolicyDTO policyDTO = client.getMetaDataPolicy(policyIds[i]);
                        policyDTO.setPolicyOrder(policyIds.length - i);
                        client.updatePolicy(policyDTO);
                    }
                } else {
                    String message = resourceBundle.getString("cannot.order.policies");
                    CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.WARNING, request);
                }
            }
        forwardTo = "index.jsp";
    } catch (Exception e) {
    	String message = resourceBundle.getString("error.while.ordering.policy");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "index.jsp?region=region1&item=policy_menu";
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
