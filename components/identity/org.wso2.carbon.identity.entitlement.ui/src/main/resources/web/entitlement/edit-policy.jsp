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
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />
<%
    entitlementPolicyBean.cleanEntitlementPolicyBean();
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	String forwardTo = null;
	String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    try {
    	EntitlementPolicyAdminServiceClient client = new EntitlementPolicyAdminServiceClient(cookie, serverURL, configContext);
        String policyId = request.getParameter("policyid");
        PolicyDTO policyDTO = client.getPolicy(policyId);
        String policy = policyDTO.getPolicy();

        if(EntitlementPolicyConstants.POLICY_SET_ELEMENT.equals(policyDTO.getPolicyType())){
            PolicySetDTO policySetDTO = PolicyCreatorUtil.createPolicySetDTO(policyDTO.getPolicy());
            entitlementPolicyBean.setPolicySetDTO(policySetDTO);
            String[] policyMetaData = policyDTO.getBasicPolicyEditorMetaData();            
            BasicTargetElementDTO basicTargetElementDTO = PolicyCreatorUtil.
                    createBasicTargetElementDTO(policyMetaData);
            entitlementPolicyBean.setBasicTargetElementDTO(basicTargetElementDTO);
            forwardTo="create-policy-set.jsp";
        } else {
            PolicyElementDTO elementDTO = PolicyCreatorUtil.createPolicyElementDTO(policy);
            entitlementPolicyBean.setPolicyName(elementDTO.getPolicyName());
            entitlementPolicyBean.setAlgorithmName(elementDTO.getRuleCombiningAlgorithms());
            entitlementPolicyBean.setPolicyDescription(elementDTO.getPolicyDescription());

            TargetElementDTO targetElementDTO = PolicyCreatorUtil.createTargetElementDTOs(policy);
            List<RuleElementDTO> ruleElementDTOs = PolicyCreatorUtil.createRuleElementDTOs(policy);
            String[] policyMetaData = policyDTO.getBasicPolicyEditorMetaData();

            if(EntitlementPolicyConstants.BASIC_POLICY_EDITOR.equals(policyDTO.getPolicyEditor())){

                try{
                    BasicTargetElementDTO basicTargetElementDTO = PolicyCreatorUtil.
                            createBasicTargetElementDTO(policyMetaData);

                    List<BasicRuleElementDTO> basicRuleElementDTOs = PolicyCreatorUtil.
                            createBasicRuleElementDTOs(ruleElementDTOs, policyMetaData);

                    entitlementPolicyBean.setBasicTargetElementDTO(basicTargetElementDTO);
                    entitlementPolicyBean.setBasicRuleElementDTOs(basicRuleElementDTOs);
                    forwardTo="create-basic-policy.jsp";
                }catch (Exception e) {
                    String message = resourceBundle.getString("policy.could.not.be.edited.with.basic");
                    CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
                }
            } else {
                forwardTo="create-policy.jsp";
            }
        
            int attributeDesignatorElementNumber = 0;
            int attributeSelectorElementNumber = 0;
            int attributeValueElementNumber = 0;
            int matchElementNumber = 0;
            int subElementNumber = 0;

            if(ruleElementDTOs != null){
                for(RuleElementDTO ruleElementDTO : ruleElementDTOs){
                    if(ruleElementDTO.getConditionElementDT0() != null &&
                       ruleElementDTO.getConditionElementDT0().getApplyElement() != null){
                        ApplyElementDTO applyElementDTO =
                                ruleElementDTO.getConditionElementDT0().getApplyElement();

                        attributeValueElementNumber = PolicyCreatorUtil.
                                getAttributeValueElementCount(applyElementDTO, attributeValueElementNumber);
                        attributeDesignatorElementNumber = PolicyCreatorUtil.
                                getAttributeDesignatorElementCount(applyElementDTO, attributeDesignatorElementNumber);
                        attributeSelectorElementNumber = PolicyCreatorUtil.
                                getAttributeSelectorElementCount(applyElementDTO, attributeSelectorElementNumber);
                    }

                    if(ruleElementDTO.getTargetElementDTO() != null &&
                                    ruleElementDTO.getTargetElementDTO().getSubElementDTOs() != null) {
                        for(SubElementDTO subElementDTO : ruleElementDTO.getTargetElementDTO().
                                getSubElementDTOs()){
                            matchElementNumber = matchElementNumber + subElementDTO.getMatchElementCount();
                        }
                        subElementNumber = subElementNumber + ruleElementDTO.getTargetElementDTO()
                                .getSubElementCount();
                    }
                }
                entitlementPolicyBean.setRuleElements(ruleElementDTOs);
            }
            if(targetElementDTO != null && targetElementDTO.getSubElementDTOs() != null){
                for(SubElementDTO subElementDTO : targetElementDTO.getSubElementDTOs()){
                    matchElementNumber = matchElementNumber + subElementDTO.getMatchElementCount();
                }
                subElementNumber = subElementNumber + targetElementDTO.getSubElementCount();
                entitlementPolicyBean.setTargetElementDTOs(targetElementDTO.getSubElementDTOs());
            }

            entitlementPolicyBean.setSubElementNumber(subElementNumber);
            entitlementPolicyBean.setMatchElementNumber(matchElementNumber);
            entitlementPolicyBean.setAttributeDesignatorElementNumber(attributeDesignatorElementNumber);
            entitlementPolicyBean.setAttributeValueElementNumber(attributeValueElementNumber);
            entitlementPolicyBean.setAttributeSelectorElementNumber(attributeSelectorElementNumber);
        }
        entitlementPolicyBean.setEditPolicy(true);

    } catch (Exception e) {
        forwardTo ="index.jsp";
    	String message = resourceBundle.getString("policy.could.not.be.edited");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "index.jsp?region=region1&item=policy_menu";
    }
%>

<%@page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="org.wso2.carbon.identity.entitlement.ui.util.PolicyCreatorUtil" %>
<%@page import="org.wso2.carbon.identity.entitlement.ui.dto.PolicyElementDTO" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.SubElementDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.RuleElementDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.ApplyElementDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.TargetElementDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.BasicTargetElementDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.BasicRuleElementDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.PolicySetDTO" %>

<script
	type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
	}
</script>

<script type="text/javascript">
	forward();
</script>