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
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.BasicRuleElementDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.BasicTargetElementDTO" %>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />

<%
    BasicRuleElementDTO basicRuleElementDTO = new BasicRuleElementDTO();
    BasicTargetElementDTO basicTargetElementDTO = new BasicTargetElementDTO();
    entitlementPolicyBean.setRuleElementOrder(null);
    
    String ruleElementOrder = request.getParameter("ruleElementOrder");
    String updateRule = request.getParameter("updateRule");
    String nextPage = request.getParameter("nextPage");
    String ruleId = request.getParameter("ruleId");
    String ruleEffect = request.getParameter("ruleEffect");
    String ruleDescription = request.getParameter("ruleDescription");
    String resourceNames = request.getParameter("resourceNames");
    String functionOnResources = request.getParameter("functionOnResources");
    String resourceDataType = request.getParameter("resourceDataType");
    String resourceId = request.getParameter("resourceId");
    String subjectNames = request.getParameter("subjectNames");
    String functionOnSubjects = request.getParameter("functionOnSubjects");
    String subjectDataType = request.getParameter("subjectDataType");
    String subjectId = request.getParameter("subjectId");
    String actionNames = request.getParameter("actionNames");
    String functionOnActions = request.getParameter("functionOnActions");
    String actionDataType = request.getParameter("actionDataType");
    String actionId = request.getParameter("actionId");
    String environmentNames = request.getParameter("environmentNames");
    String functionOnEnvironment = request.getParameter("functionOnEnvironment");
    String environmentDataType = request.getParameter("environmentDataType");
    String environmentId = request.getParameter("environmentId");
    String attributeId = request.getParameter("attributeId");
    String functionOnAttributes = request.getParameter("functionOnAttributes");
    String userAttributeValue = request.getParameter("userAttributeValue");
    String attributeType = request.getParameter("attributeType");

    String policyName = request.getParameter("policyName");
    String algorithmName = request.getParameter("algorithmName");
    String policyDescription = request.getParameter("policyDescription");
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
    String completedRule = request.getParameter("completedRule");
    String editRule = request.getParameter("editRule");

    if(ruleId != null && !ruleId.trim().equals("") && !ruleId.trim().equals("null") && editRule == null ) {

        basicRuleElementDTO.setRuleId(ruleId);
        basicRuleElementDTO.setRuleEffect(ruleEffect);

        if(ruleDescription != null && ruleDescription.trim().length() > 0 ){
            basicRuleElementDTO.setRuleDescription(ruleDescription);
        }

        if(resourceNames != null && !resourceNames.equals("")){
            basicRuleElementDTO.setResourceList(resourceNames);
        }

        if(functionOnResources != null && !functionOnResources.equals("")){
            basicRuleElementDTO.setFunctionOnResources(functionOnResources);
        }

        if(resourceDataType != null && resourceDataType.trim().length() > 0 &&
                                        !resourceDataType.trim().equals("null")){
            basicRuleElementDTO.setResourceDataType(resourceDataType);
        }

        if(resourceId != null && resourceId.trim().length() > 0 && !resourceId.trim().equals("null")){
            basicRuleElementDTO.setResourceId(resourceId);
        }

        if(subjectNames != null && !subjectNames.equals("")){
            basicRuleElementDTO.setSubjectList(subjectNames);
        }

        if(subjectNames != null && !functionOnSubjects.equals("")){
            basicRuleElementDTO.setFunctionOnSubjects(functionOnSubjects);
        }

        if(subjectDataType != null && subjectDataType.trim().length() > 0 &&
                                                            !subjectDataType.trim().equals("null")) {
            basicRuleElementDTO.setSubjectDataType(subjectDataType);
        }

        if(subjectId != null && subjectId.trim().length() > 0 && !subjectId.trim().equals("null")){
            basicRuleElementDTO.setSubjectId(subjectId);
        }

        if(attributeId != null && !attributeId.equals("")){
            basicRuleElementDTO.setAttributeId(attributeId);
        }

        if(functionOnAttributes != null && !functionOnAttributes.equals("")){
            basicRuleElementDTO.setFunctionOnAttributes(functionOnAttributes);
        }

        if(userAttributeValue != null && !userAttributeValue.equals("")){
            basicRuleElementDTO.setUserAttributeValue(userAttributeValue);
        }

        if(actionNames != null && !actionNames.equals("")){
            basicRuleElementDTO.setActionList(actionNames);
        }

        if(functionOnActions != null && !functionOnActions.equals("")){
            basicRuleElementDTO.setFunctionOnActions(functionOnActions);
        }

        if(actionDataType != null && actionDataType.trim().length() > 0 &&
                                            !actionDataType.trim().equals("null")){
            basicRuleElementDTO.setActionDataType(actionDataType);
        }

        if(actionId != null && actionId.trim().length() > 0 && !actionId.trim().equals("null")){
            basicRuleElementDTO.setActionId(actionId);
        }

        if(environmentNames != null && !environmentNames.equals("")){
            basicRuleElementDTO.setEnvironmentList(environmentNames);
        }

        if(functionOnEnvironment != null && !functionOnEnvironment.equals("")){
            basicRuleElementDTO.setFunctionOnEnvironment(functionOnEnvironment);
        }

        if(environmentDataType != null && environmentDataType.trim().length() > 0 && 
                                                !environmentDataType.trim().equals("null")){
            basicRuleElementDTO.setEnvironmentDataType(environmentDataType);
        }

        if(environmentId != null && environmentId.trim().length() > 0 &&
                                                !environmentId.trim().equals("null")){
            basicRuleElementDTO.setEnvironmentId(environmentId);
        }

        if(completedRule != null && completedRule.equals("true")){
            basicRuleElementDTO.setCompletedRule(true);
        }

        if(entitlementPolicyBean.subjectTypeMap.get(ruleId) != null ) {
            basicRuleElementDTO.setSubjectType(entitlementPolicyBean.subjectTypeMap.get(ruleId));            
        }
        entitlementPolicyBean.setBasicRuleElementDTOs(basicRuleElementDTO);
    } else {
        if(entitlementPolicyBean.subjectTypeMap.get("Target") != null ) {
            basicTargetElementDTO.setSubjectType(entitlementPolicyBean.subjectTypeMap.get("Target"));
        }
    }

    if(resourceNamesTarget != null && !resourceNamesTarget.equals("")){
        basicTargetElementDTO.setResourceList(resourceNamesTarget);
    }

    if(functionOnResourcesTarget != null && !functionOnResourcesTarget.equals("")){
        basicTargetElementDTO.setFunctionOnResources(functionOnResourcesTarget);
    }

    if(resourceDataTypeTarget != null && resourceDataTypeTarget.trim().length() > 0 &&
                                                    !resourceDataTypeTarget.trim().equals("null")){
        basicTargetElementDTO.setResourceDataType(resourceDataTypeTarget);
    }

    if(resourceIdTarget != null && resourceIdTarget.trim().length() > 0 &&
                                            !resourceIdTarget.trim().equals("null")){
        basicTargetElementDTO.setResourceId(resourceIdTarget);
    }

    if(subjectNamesTarget != null && !subjectNamesTarget.equals("")){
        basicTargetElementDTO.setSubjectList(subjectNamesTarget);
    }

    if(functionOnSubjectsTarget != null && !functionOnSubjectsTarget.equals("")){
        basicTargetElementDTO.setFunctionOnSubjects(functionOnSubjectsTarget);
    }

    if(subjectDataTypeTarget != null && subjectDataTypeTarget.trim().length() > 0 &&
                                                    !subjectDataTypeTarget.trim().equals("null")){
        basicTargetElementDTO.setSubjectDataType(subjectDataTypeTarget);
    }

    if(subjectIdTarget != null && subjectIdTarget.trim().length() > 0 &&
                                                    !subjectIdTarget.trim().equals("null")){
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

    if(actionDataTypeTarget != null && actionDataTypeTarget.trim().length() > 0 &&
                                                !actionDataTypeTarget.trim().equals("null")){
        basicTargetElementDTO.setActionDataType(actionDataTypeTarget);
    }

    if(actionIdTarget != null && actionIdTarget.trim().length() > 0 &&
                                                !actionIdTarget.trim().equals("null")){
        basicTargetElementDTO.setActionId(actionIdTarget);
    }

    if(environmentNamesTarget != null && !environmentNamesTarget.equals("")){
        basicTargetElementDTO.setEnvironmentList(environmentNamesTarget);
    }

    if(functionOnEnvironmentTarget != null && !functionOnEnvironmentTarget.equals("")){
        basicTargetElementDTO.setFunctionOnEnvironment(functionOnEnvironmentTarget);
    }

    if(environmentDataTypeTarget != null && environmentDataTypeTarget.trim().length() > 0 &&
                                               !environmentDataTypeTarget.trim().equals("null")){
        basicTargetElementDTO.setEnvironmentDataType(environmentDataTypeTarget);
    }

    if(environmentIdTarget != null && environmentIdTarget.trim().length() > 0 &&
                                                !environmentIdTarget.trim().equals("null")){
        basicTargetElementDTO.setEnvironmentId(environmentIdTarget);
    }

    entitlementPolicyBean.setBasicTargetElementDTO(basicTargetElementDTO);
    
    String forwardTo;

    if(ruleElementOrder != null && !ruleElementOrder.equals("")){
        if(basicRuleElementDTO.isCompletedRule() && !"true".equals(updateRule)){
            entitlementPolicyBean.setRuleElementOrder(ruleElementOrder.trim() + ", " +
                                                      basicRuleElementDTO.getRuleId());
        } else{
            entitlementPolicyBean.setRuleElementOrder(ruleElementOrder.trim());
        }
    }

    if(completedRule != null && completedRule.equals("true")){
        forwardTo = nextPage + ".jsp?";
    } else {
        forwardTo = nextPage + ".jsp?ruleId=" + ruleId;
        if(attributeType != null && attributeType.trim().length() > 0){
            forwardTo = forwardTo + "&attributeType=" + attributeType;     
        }
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