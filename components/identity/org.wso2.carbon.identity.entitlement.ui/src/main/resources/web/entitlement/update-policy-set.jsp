,<!--
/*
* Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
-->
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.PolicySetDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyCreator" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyCreationException" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page
        import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.BasicTargetElementDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.util.PolicyCreatorUtil" %>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />

<%

    PolicySetDTO policySetDTO = new PolicySetDTO();
    BasicTargetElementDTO basicTargetElementDTO = new BasicTargetElementDTO();
    String policySetName =  CharacterEncoder.getSafeText(request.getParameter("policySetName"));
    String policyAlgorithmName = CharacterEncoder.getSafeText(request.getParameter("policyAlgorithmName"));
    String policySetDescription = CharacterEncoder.getSafeText(request.getParameter("policySetDescription"));

    String resourceNamesTarget = request.getParameter("resourceNamesTarget");
    String functionOnResourcesTarget = request.getParameter("functionOnResourcesTarget");
    String resourceDataTypeTarget = request.getParameter("resourceDataTypeTarget");
    String resourceIdTarget = request.getParameter("resourceIdTarget");
    String subjectNamesTarget = request.getParameter("subjectNamesTarget");
    String functionOnSubjectsTarget = request.getParameter("functionOnSubjectsTarget");
    String subjectDataTypeTarget = request.getParameter("subjectDataTypeTarget");
    String subjectIdTarget = request.getParameter("subjectIdTarget");
    String actionNamesTarget = request.getParameter("actionNamesTarget");
    String functionOnActionsTarget = request.getParameter("functionOnActionsTarget");
    String actionDataTypeTarget = request.getParameter("actionDataTypeTarget");
    String actionIdTarget = request.getParameter("actionIdTarget");
    String environmentNamesTarget = request.getParameter("environmentNamesTarget");
    String functionOnEnvironmentTarget = request.getParameter("functionOnEnvironmentTarget");
    String environmentDataTypeTarget = request.getParameter("environmentDataTypeTarget");
    String environmentIdTarget = request.getParameter("environmentIdTarget");
    String attributeIdTarget = request.getParameter("attributeIdTarget");
    String functionOnAttributesTarget = request.getParameter("functionOnAttributesTarget");
    String userAttributeValueTarget = request.getParameter("userAttributeValueTarget");

    if(resourceNamesTarget != null && !resourceNamesTarget.equals("")){
        basicTargetElementDTO.setResourceList(resourceNamesTarget);
    }

    if(functionOnResourcesTarget != null && !functionOnResourcesTarget.equals("")){
        basicTargetElementDTO.setFunctionOnResources(functionOnResourcesTarget);
    }

    if(resourceDataTypeTarget != null && !resourceDataTypeTarget.equals("")){
        basicTargetElementDTO.setResourceDataType(resourceDataTypeTarget);
    }

    if(resourceIdTarget != null && !resourceIdTarget.equals("")){
        basicTargetElementDTO.setResourceId(resourceIdTarget);
    }

    if(subjectNamesTarget != null && !subjectNamesTarget.equals("")){
        basicTargetElementDTO.setSubjectList(subjectNamesTarget);
    }

    if(functionOnSubjectsTarget != null && !functionOnSubjectsTarget.equals("")){
        basicTargetElementDTO.setFunctionOnSubjects(functionOnSubjectsTarget);
    }

    if(subjectDataTypeTarget != null && !subjectDataTypeTarget.equals("")){
        basicTargetElementDTO.setSubjectDataType(subjectDataTypeTarget);
    }

    if(subjectIdTarget != null && !subjectIdTarget.equals("")){
        basicTargetElementDTO.setSubjectId(subjectIdTarget);
    }

    if(attributeIdTarget != null && !attributeIdTarget.equals("")){
        basicTargetElementDTO.setAttributeId(attributeIdTarget);
    }

    if(functionOnAttributesTarget != null && !functionOnAttributesTarget.equals("")){
        basicTargetElementDTO.setFunctionOnAttributes(functionOnAttributesTarget);
    }

    if(userAttributeValueTarget != null && !userAttributeValueTarget.equals("")){
        basicTargetElementDTO.setUserAttributeValue(userAttributeValueTarget);
    }

    if(actionNamesTarget != null && !actionNamesTarget.equals("")){
        basicTargetElementDTO.setActionList(actionNamesTarget);
    }

    if(functionOnActionsTarget != null && !functionOnActionsTarget.equals("")){
        basicTargetElementDTO.setFunctionOnActions(functionOnActionsTarget);
    }

    if(actionDataTypeTarget != null && !actionDataTypeTarget.equals("")){
        basicTargetElementDTO.setActionDataType(actionDataTypeTarget);
    }

    if(actionIdTarget != null && !actionIdTarget.equals("")){
        basicTargetElementDTO.setActionId(actionIdTarget);
    }

    if(environmentNamesTarget != null && !environmentNamesTarget.equals("")){
        basicTargetElementDTO.setEnvironmentList(environmentNamesTarget);
    }

    if(functionOnEnvironmentTarget != null && !functionOnEnvironmentTarget.equals("")){
        basicTargetElementDTO.setFunctionOnEnvironment(functionOnEnvironmentTarget);
    }

    if(environmentDataTypeTarget != null && !environmentDataTypeTarget.equals("")){
        basicTargetElementDTO.setEnvironmentDataType(environmentDataTypeTarget);
    }

    if(environmentIdTarget != null && !environmentIdTarget.equals("")){
        basicTargetElementDTO.setEnvironmentId(environmentIdTarget);
    }

    entitlementPolicyBean.setBasicTargetElementDTO(basicTargetElementDTO);


    String forwardTo = "index.jsp";
    List<String> selectedPolicyIds = new ArrayList<String>();
    int noOfSelectedPolicies = 1;

    while(true) {
        String policyId  = request.getParameter("policyIds" + noOfSelectedPolicies);
        if (policyId != null && !policyId.trim().equals("")) {
            selectedPolicyIds.add(policyId);
        } else {
            if(noOfSelectedPolicies > 20){
                break;
            }
        }
        noOfSelectedPolicies ++;
    }

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.
                    CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    
    try {
        EntitlementPolicyAdminServiceClient client = new EntitlementPolicyAdminServiceClient(cookie,
                serverURL, configContext);

        if(policySetName != null && !policySetName.equals("")){

            policySetDTO.setPolicySetId(policySetName);

            policySetDTO.setBasicTargetElementDTO(basicTargetElementDTO);

            if(policyAlgorithmName != null && !policyAlgorithmName.equals("")){
                policySetDTO.setPolicyCombiningAlgId(policyAlgorithmName);
            }
            if(policySetDescription != null && !policySetDescription.equals("")){
                policySetDTO.setDescription(policySetDescription);
            }

//            for(String policyId : selectedPolicyIds){
//                PolicyDTO policyDTO = client.getPolicy(policyId);
//                if(policyDTO  != null){
//                    policySetDTO.setPolicyIds(policyId);
//                    policySetDTO.setPolicy(policyDTO.getPolicy());
//                }
//
//            }

            List<String> policyIdReferences = new ArrayList<String>();
            List<String> policySetIdReferences = new ArrayList<String>();

            for(String policyId : selectedPolicyIds){
                PolicyDTO policyDTO = client.getLightPolicy(policyId);
                if(policyDTO  != null){
                    if("PolicySet".equals(policyDTO.getPolicyType())){
                        policySetIdReferences.add(policyId);
                    } else if("Policy".equals(policyDTO.getPolicyType())){
                        policyIdReferences.add(policyId);
                    }
                }
            }

            policySetDTO.setPolicySetIdReferences(policySetIdReferences);
            policySetDTO.setPolicyIdReferences(policyIdReferences);

            EntitlementPolicyCreator entitlementPolicyCreator = new EntitlementPolicyCreator();
            String policySet = entitlementPolicyCreator.createPolicySet(policySetDTO);
            String[] policyMetaData = PolicyCreatorUtil.createPolicyMetaData(basicTargetElementDTO, null, null);
            PolicyDTO policyDTO = new PolicyDTO();
            policyDTO.setPolicy(policySet);
            policyDTO.setBasicPolicyEditorMetaData(policyMetaData);
            policyDTO.setPolicyType("PolicySet");
            if(entitlementPolicyBean.isEditPolicy()){
                client.updatePolicy(policyDTO);
            } else {
                client.addPolicy(policyDTO);
            }
            String message = resourceBundle.getString("ent.policy.added.successfully");
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
            forwardTo="index.jsp";
        }
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage( e.getMessage(), CarbonUIMessage.ERROR, request);
        forwardTo = "index.jsp?";
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
