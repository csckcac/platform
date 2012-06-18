<!--
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
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page
        import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.claim.mgt.ui.client.ClaimAdminClient" %>
<%@ page import="org.wso2.carbon.claim.mgt.stub.dto.ClaimDialectDTO" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.util.PolicyEditorUIUtil" %>
<%@ page import="java.util.Set" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.*" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<jsp:useBean id="entitlementPolicyBean"
             type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*"/>


<%
//    BasicRuleElementDTO basicRuleElementDTO = null;
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    // TODO
    List<BasicRuleElementDTO> basicRuleElementDTOs = entitlementPolicyBean.getBasicRuleElementDTOs();
    //
    BasicTargetElementDTO basicTargetElementDTO = entitlementPolicyBean.getBasicTargetElementDTO();

    String selectedAttributeDataType = (String) request.getParameter("selectedAttributeDataType");
    String selectedAttributeId = (String) request.getParameter("selectedAttributeId");
    String attributeType = (String) request.getParameter("attributeType");

    String ruleId = CharacterEncoder.getSafeText(request.getParameter("ruleId"));
//    if (ruleId != null && ruleId.trim().length() > 0 && !ruleId.trim().equals("null")) {
//        basicRuleElementDTO = entitlementPolicyBean.getBasicRuleElement(ruleId);
//    }

    if ("null".equals(selectedAttributeId)) {
        selectedAttributeId = null;
    }

    if ("null".equals(selectedAttributeDataType)) {
        selectedAttributeDataType = null;
    }

    String selectedSubjectType = CharacterEncoder.getSafeText(request.getParameter("selectedSubjectType"));
    if (selectedSubjectType != null && !"".equals(selectedSubjectType)) {
        if (ruleId != null && !ruleId.equals("")) {
            entitlementPolicyBean.subjectTypeMap.put(ruleId, selectedSubjectType);
        } else {
            entitlementPolicyBean.subjectTypeMap.put("Target", selectedSubjectType);
        }
    }

    String selectedAttributeNames = "";
    String selectedSubjectNames = "";
    String selectedResourceNames = "";
    String selectedActionNames = "";
    String selectedEnvironmentNames = "";
    String selectedResourceId = "";
    String selectedResourceDataType = "";
    String selectedSubjectId = "";
    String selectedSubjectDataType = "";
    String selectedActionId = "";
    String selectedActionDataType = "";
    String selectedEnvironmentId = "";
    String selectedEnvironmentDataType = "";
    String resourceNames = "";
    String environmentNames = "";
    String userAttributeValue = "";
    String functionOnResources = "";
    String subjectNames = "";
    String functionOnSubjects = "";
    String actionNames = "";
    String functionOnActions = "";
    String functionOnEnvironment = "";
    String functionOnAttributes = "";
    String ruleDescription = "";
    String ruleEffect = "";
    String resourceDataType = "";
    String subjectDataType = "";
    String actionDataType = "";
    String environmentDataType = "";
    String userAttributeValueDataType = "";
    String resourceId = "";
    String subjectId = "";
    String actionId = "";
    String environmentId = "";
    String attributeId = "";

    String resourceNamesTarget = "";
    String environmentNamesTarget = "";
    String userAttributeValueTarget = "";
    String functionOnResourcesTarget = "";
    String subjectNamesTarget = "";
    String functionOnSubjectsTarget = "";
    String actionNamesTarget = "";
    String functionOnActionsTarget = "";
    String functionOnEnvironmentTarget = "";
    String functionOnAttributesTarget = "";
    String resourceDataTypeTarget = "";
    String subjectDataTypeTarget = "";
    String actionDataTypeTarget = "";
    String environmentDataTypeTarget = "";
    String userAttributeValueDataTypeTarget = "";
    String resourceIdTarget = "";
    String subjectIdTarget = "";
    String actionIdTarget = "";
    String environmentIdTarget = "";
    String attributeIdTarget = "";
    int noOfSelectedAttributes = 1;


    /**
     *  Get posted resources from jsp pages and put then in to a String object
     */
    while (true) {
        String attributeName = request.getParameter("resourceName" + noOfSelectedAttributes);
        if (attributeName == null || attributeName.trim().length() < 1) {
            break;
        }
        if (selectedAttributeNames.equals("")) {
            selectedAttributeNames = attributeName.trim();
        } else {
            selectedAttributeNames = selectedAttributeNames + "," + attributeName.trim();
        }
        noOfSelectedAttributes++;
    }

    if (attributeType != null) {
        if (EntitlementPolicyConstants.RESOURCE_ELEMENT.equals(attributeType)) {
            selectedResourceNames = selectedAttributeNames;
            selectedResourceId = selectedAttributeId;
            selectedResourceDataType = selectedAttributeDataType;
        } else if (EntitlementPolicyConstants.SUBJECT_ELEMENT.equals(attributeType)) {
            selectedSubjectNames = selectedAttributeNames;
            selectedSubjectId = selectedAttributeId;
            selectedSubjectDataType = selectedAttributeDataType;
        } else if (EntitlementPolicyConstants.ACTION_ELEMENT.equals(attributeType)) {
            selectedActionNames = selectedAttributeNames;
            selectedActionId = selectedAttributeId;
            selectedActionDataType = selectedAttributeDataType;
        } else if (EntitlementPolicyConstants.ENVIRONMENT_ELEMENT.equals(attributeType)) {
            selectedEnvironmentNames = selectedAttributeNames;
            selectedEnvironmentId = selectedAttributeId;
            selectedEnvironmentDataType = selectedAttributeDataType;
        }
    }


    /**
     * Get posted subjects from jsp pages and put then in to a String object
     */
    String[] subjects = request.getParameterValues("subjects");
    if (subjects != null) {
        for (String subject : subjects) {
            if (subject == null || subject.trim().equals("")) {
                break;
            }
            if (selectedSubjectNames.equals("")) {
                selectedSubjectNames = subject.trim();
            } else {
                selectedSubjectNames = selectedSubjectNames + "," + subject.trim();
            }
        }
    }

    // following function ids should be retrieve from registry.  TODO
    String[] functionIds = new String[]{EntitlementPolicyConstants.EQUAL_TO,
            EntitlementPolicyConstants.IS_IN, EntitlementPolicyConstants.AT_LEAST,
            EntitlementPolicyConstants.SUBSET_OF, EntitlementPolicyConstants.REGEXP_MATCH,
            EntitlementPolicyConstants.SET_OF};


    String[] targetFunctionIds = new String[]{EntitlementPolicyConstants.EQUAL_TO,
            EntitlementPolicyConstants.AT_LEAST_ONE_MATCH,
            EntitlementPolicyConstants.AT_LEAST_ONE_MATCH_REGEXP, EntitlementPolicyConstants.REGEXP_MATCH,
            EntitlementPolicyConstants.SET_OF, EntitlementPolicyConstants.MATCH_REGEXP_SET_OF};

    String[] ruleEffects = new String[]{EntitlementPolicyConstants.RULE_EFFECT_PERMIT,
            EntitlementPolicyConstants.RULE_EFFECT_DENY};

    String[] dataTypes = new String[]{EntitlementPolicyConstants.RULE_EFFECT_PERMIT};

    /**
     * Assign current BasicRule Object Values to variables to show on UI
     */
//    if (basicRuleElementDTO != null) {
//
//        ruleEffect = basicRuleElementDTO.getRuleEffect();
//        ruleId = basicRuleElementDTO.getRuleId();
//        ruleDescription = basicRuleElementDTO.getRuleDescription();
//
//        resourceNames = basicRuleElementDTO.getResourceList();
//        subjectNames = basicRuleElementDTO.getSubjectList();
//        actionNames = basicRuleElementDTO.getActionList();
//        environmentNames = basicRuleElementDTO.getEnvironmentList();
//        userAttributeValue = basicRuleElementDTO.getUserAttributeValue();
//
//        functionOnActions = basicRuleElementDTO.getFunctionOnActions();
//        functionOnResources = basicRuleElementDTO.getFunctionOnResources();
//        functionOnSubjects = basicRuleElementDTO.getFunctionOnSubjects();
//        functionOnEnvironment = basicRuleElementDTO.getFunctionOnEnvironment();
//        functionOnAttributes = basicRuleElementDTO.getFunctionOnAttributes();
//
//        if (selectedResourceDataType != null && selectedResourceDataType.trim().length() > 0) {
//            resourceDataType = selectedResourceDataType;
//        } else {
//            resourceDataType = basicRuleElementDTO.getResourceDataType();
//        }
//
//        if (selectedSubjectDataType != null && selectedSubjectDataType.trim().length() > 0) {
//            subjectDataType = selectedSubjectDataType;
//        } else {
//            subjectDataType = basicRuleElementDTO.getSubjectDataType();
//        }
//
//        if (selectedActionDataType != null && selectedActionDataType.trim().length() > 0) {
//            actionDataType = selectedActionDataType;
//        } else {
//            actionDataType = basicRuleElementDTO.getActionDataType();
//        }
//
//        if (selectedEnvironmentDataType != null && selectedEnvironmentDataType.trim().length() > 0) {
//            environmentDataType = selectedEnvironmentDataType;
//        } else {
//            environmentDataType = basicRuleElementDTO.getEnvironmentDataType();
//        }
//
//        userAttributeValueDataType = basicRuleElementDTO.getUserAttributeValueDataType();
//
//        if (selectedResourceId != null && selectedResourceId.trim().length() > 0) {
//            resourceId = selectedResourceId;
//        } else {
//            resourceId = basicRuleElementDTO.getResourceId();
//        }
//
//        if (selectedSubjectId != null && selectedSubjectId.trim().length() > 0) {
//            subjectId = selectedSubjectId;
//        } else {
//            subjectId = basicRuleElementDTO.getSubjectId();
//        }
//
//        if (selectedActionId != null && selectedActionId.trim().length() > 0) {
//            actionId = selectedActionId;
//        } else {
//            actionId = basicRuleElementDTO.getActionId();
//        }
//
//        if (selectedEnvironmentId != null && selectedEnvironmentId.trim().length() > 0) {
//            environmentId = selectedEnvironmentId;
//        } else {
//            environmentId = basicRuleElementDTO.getEnvironmentId();
//        }
//
//        attributeId = basicRuleElementDTO.getAttributeId();
//
//        if (!entitlementPolicyBean.subjectTypeMap.containsKey(ruleId)) {
//            entitlementPolicyBean.subjectTypeMap.put(ruleId, basicRuleElementDTO.getSubjectType());
//        }
//
//        if (selectedResourceNames != null && selectedResourceNames.trim().length() > 0) {
//            if (resourceNames != null && resourceNames.trim().length() > 0) {
//                resourceNames = resourceNames + "," + selectedResourceNames;
//            } else {
//                resourceNames = selectedResourceNames;
//            }
//        }
//
//        if (selectedSubjectNames != null && selectedSubjectNames.trim().length() > 0) {
//            if (subjectNames != null && subjectNames.trim().length() > 0) {
//                subjectNames = subjectNames + "," + selectedSubjectNames;
//            } else {
//                subjectNames = selectedSubjectNames;
//            }
//        }
//
//        if (selectedActionNames != null && selectedActionNames.trim().length() > 0) {
//            if (actionNames != null && actionNames.trim().length() > 0) {
//                actionNames = actionNames + "," + selectedActionNames;
//            } else {
//                actionNames = selectedActionNames;
//            }
//        }
//
//        if (selectedEnvironmentNames != null && selectedEnvironmentNames.trim().length() > 0) {
//            if (environmentNames != null && environmentNames.trim().length() > 0) {
//                environmentNames = environmentNames + "," + selectedEnvironmentNames;
//            } else {
//                environmentNames = selectedEnvironmentNames;
//            }
//        }
//
//    }

    /**
     * Assign current BasicTarget Object Values to variables to show on UI.
     */
    if (basicTargetElementDTO != null) {

        resourceNamesTarget = basicTargetElementDTO.getResourceList();
        subjectNamesTarget = basicTargetElementDTO.getSubjectList();
        actionNamesTarget = basicTargetElementDTO.getActionList();
        environmentNamesTarget = basicTargetElementDTO.getEnvironmentList();
        userAttributeValueTarget = basicTargetElementDTO.getUserAttributeValue();

        functionOnActionsTarget = basicTargetElementDTO.getFunctionOnActions();
        functionOnResourcesTarget = basicTargetElementDTO.getFunctionOnResources();
        functionOnSubjectsTarget = basicTargetElementDTO.getFunctionOnSubjects();
        functionOnEnvironmentTarget = basicTargetElementDTO.getFunctionOnEnvironment();
        functionOnAttributesTarget = basicTargetElementDTO.getFunctionOnAttributes();
        userAttributeValueDataTypeTarget = basicTargetElementDTO.getUserAttributeValueDataType();
        attributeIdTarget = basicTargetElementDTO.getAttributeId();

        resourceDataTypeTarget = basicTargetElementDTO.getResourceDataType();
        subjectDataTypeTarget = basicTargetElementDTO.getSubjectDataType();
        actionDataTypeTarget = basicTargetElementDTO.getActionDataType();
        environmentDataTypeTarget = basicTargetElementDTO.getEnvironmentDataType();

        resourceIdTarget = basicTargetElementDTO.getResourceId();
        subjectIdTarget = basicTargetElementDTO.getSubjectId();
        actionIdTarget = basicTargetElementDTO.getActionId();
        environmentIdTarget = basicTargetElementDTO.getEnvironmentId();

        if (!entitlementPolicyBean.subjectTypeMap.containsKey("Target")) {
            entitlementPolicyBean.subjectTypeMap.put("Target", basicTargetElementDTO.getSubjectType());
        }

//        if (basicRuleElementDTO == null) {
//            if (selectedResourceNames != null && selectedResourceNames.trim().length() > 0) {
//                if (resourceNamesTarget != null && resourceNamesTarget.trim().length() > 0) {
//                    resourceNamesTarget = resourceNamesTarget + "," + selectedResourceNames;
//                } else {
//                    resourceNamesTarget = selectedResourceNames;
//                }
//            }
//
//            if (selectedSubjectNames != null && selectedSubjectNames.trim().length() > 0) {
//                if (subjectNamesTarget != null && subjectNamesTarget.trim().length() > 0) {
//                    subjectNamesTarget = subjectNamesTarget + "," + selectedSubjectNames;
//                } else {
//                    subjectNamesTarget = selectedSubjectNames;
//                }
//            }
//
//            if (selectedActionNames != null && selectedActionNames.trim().length() > 0) {
//                if (actionNamesTarget != null && actionNamesTarget.trim().length() > 0) {
//                    actionNamesTarget = actionNamesTarget + "," + selectedActionNames;
//                } else {
//                    actionNamesTarget = selectedActionNames;
//                }
//            }
//
//            if (selectedEnvironmentNames != null && selectedEnvironmentNames.trim().length() > 0) {
//                if (environmentNamesTarget != null && environmentNamesTarget.trim().length() > 0) {
//                    environmentNamesTarget = environmentNamesTarget + "," + selectedEnvironmentNames;
//                } else {
//                    environmentNamesTarget = selectedEnvironmentNames;
//                }
//            }
//
//            if (selectedResourceDataType != null && selectedResourceDataType.trim().length() > 0) {
//                resourceDataTypeTarget = selectedResourceDataType;
//            }
//
//            if (selectedSubjectDataType != null && selectedSubjectDataType.trim().length() > 0) {
//                subjectDataTypeTarget = selectedSubjectDataType;
//            }
//
//            if (selectedActionDataType != null && selectedActionDataType.trim().length() > 0) {
//                actionDataTypeTarget = selectedActionDataType;
//            }
//
//            if (selectedEnvironmentDataType != null && selectedEnvironmentDataType.trim().length() > 0) {
//                environmentDataTypeTarget = selectedEnvironmentDataType;
//            }
//
//            if (selectedResourceId != null && selectedResourceId.trim().length() > 0) {
//                resourceIdTarget = selectedResourceId;
//            }
//
//            if (selectedSubjectId != null && selectedSubjectId.trim().length() > 0) {
//                subjectIdTarget = selectedSubjectId;
//            }
//
//            if (selectedActionId != null && selectedActionId.trim().length() > 0) {
//                actionIdTarget = selectedActionId;
//            }
//
//            if (selectedEnvironmentId != null && selectedEnvironmentId.trim().length() > 0) {
//                environmentIdTarget = selectedEnvironmentId;
//            }
//        }
    }


    /////////////////////////////////// New Imple ////////////////////////////

    String currentCategory = null;
    String currentPreFunction = null;
    String currentFunction = null;
    String currentAttributeValue =  null;
    String currentAttributeId =  null;
    String currentAttributeDataType = null;
    String currentCombineFunction = null;

    Set<String> categories = entitlementPolicyBean.getCategorySet();
    List<String> rulePreFunctions = entitlementPolicyBean.getPreFunctions();
    List<String> targetPreFunctions = entitlementPolicyBean.getPreFunctions();
    Set<String>  targetFunctions = entitlementPolicyBean.getTargetFunctionMap().keySet();
    Set<String>  ruleFunctions = entitlementPolicyBean.getRuleFunctionMap().keySet();
    String[] combineFunctions = new String[] {"END", "AND", "OR"};


    List<RuleDTO> ruleDTOs = entitlementPolicyBean.getRuleDTOs();
    TargetDTO targetDTO = entitlementPolicyBean.getTargetDTO();


    RuleDTO ruleDTO = null;

    ruleId = CharacterEncoder.getSafeText(request.getParameter("ruleId"));
    if (ruleId != null && ruleId.trim().length() > 0 && !ruleId.trim().equals("null")) {
        ruleDTO = entitlementPolicyBean.getRuleDTO(ruleId);
    }

%>



<script type="text/javascript">

function createNewTargetRow(value) {
    if (value == "AND" || value == "OR" || value == "NEW"){
        var index = jQuery('#multipleTargetTable tr').length;
        jQuery('#multipleTargetTable > tbody:last').append('<tr><td style="padding-left:0px !important;padding-right:0px !important"><select id="targetCategory_'  + index + '" name="targetCategory_'  + index + '" class="leftCol-small"> <%for (String category : categories) { if(currentCategory != null && category.equals(currentCategory)){%> <option value="<%=currentCategory%>" selected="selected"><%=currentCategory%> </option> <%} else {%> <option value="<%=category%>"><%=category%> </option> <%} }%></select></td>' +
                '<td style="padding-left:0px !important;padding-right:0px !important"><select id="targetPreFunction_'  + index + '" name="targetPreFunction_'  + index + '" class="leftCol-small"><%for (String targetPreFunction : targetPreFunctions) {if (currentPreFunction != null && targetPreFunction.equals(currentPreFunction)) {%><option value="<%=targetPreFunction%>" selected="selected"><%=targetPreFunction%></option><%} else {%><option value="<%=targetPreFunction%>"><%=targetPreFunction%></option><%}}%></select></td>' +
                '<td style="padding-left:0px !important;padding-right:0px !important"><select id="targetFunction_'  + index + '" name="targetFunction_'  + index + '" class="leftCol-small"><%for (String targetFunction : targetFunctions) {if (currentFunction != null && targetFunction.equals(currentFunction)) {%><option value="<%=targetFunction%>" selected="selected"><%=targetFunction%></option><%} else {%><option value="<%=targetFunction%>"><%=targetFunction%></option><%}}%></select></td>' +
                '<td style="padding-left:0px !important;padding-right:0px !important"><%if (currentAttributeValue != null && !"".equals(currentAttributeValue)) {%><input type="text" size="60" name="targetAttributeValue_'  + index + '" id="targetAttributeValue_'  + index + '" value="<%=currentAttributeValue%>" class="text-box-big"/><%} else {%><input type="text" size="60" name="targetAttributeValue_'  + index + '" id="targetAttributeValue_'  + index + '" class="text-box-big"/><%}%></td>' +
                '<a title="Select Resources Names" class="icon-link" onclick="selectAttributesForTarget("<%=currentCategory%>");" style="background-image:url(images/registry.gif);"></a>' +
                '<td><input type="hidden" name="targetAttributeId_'  + index +  '" id="targetAttributeId_'  + index + '" value="<%=currentAttributeId%>"/></td>' +
                '<td><input type="hidden" name="targetAttributeTypes_'  + index + '" id="targetAttributeTypes_'  + index +  '" value="<%=currentAttributeDataType%>"/></td>' +
                '<td style="padding-left:0px !important;padding-right:0px !important"><select id="targetCombineFunctions_'  + index + '" name="targetCombineFunctions_'  + index + '" class="leftCol-small" onchange="createNewTargetRow(this.options[this.selectedIndex].value)"><%for (String combineFunction : combineFunctions) {if (currentCombineFunction != null && combineFunction.equals(currentCombineFunction)) {%><option value="<%=combineFunction%>" selected="selected"><%=combineFunction%></option><%} else {%><option value="<%=combineFunction%>"><%=combineFunction%></option><%}}%></select></td>' +
                '</tr>');
    }
}


function createNewRuleRow(value) {
    if (value == "AND" || value == "OR" || value == "NEW"){
        var index = jQuery('#multipleRuleTable tr').length;
        jQuery('#multipleRuleTable > tbody:last').append('<tr><td style="padding-left:0px !important;padding-right:0px !important"><select id="ruleCategory_'  + index + '" name="ruleCategory_'  + index + '" class="leftCol-small"> <%for (String category : categories) { if(currentCategory != null && category.equals(currentCategory)){%> <option value="<%=currentCategory%>" selected="selected"><%=currentCategory%> </option> <%} else {%> <option value="<%=category%>"><%=category%> </option> <%} }%></select></td>' +
                '<td style="padding-left:0px !important;padding-right:0px !important"><select id="rulePreFunction_'  + index + '" name="rulePreFunction_'  + index + '" class="leftCol-small"><%for (String rulePreFunction : rulePreFunctions) {if (currentPreFunction != null && rulePreFunction.equals(currentPreFunction)) {%><option value="<%=rulePreFunction%>" selected="selected"><%=rulePreFunction%></option><%} else {%><option value="<%=rulePreFunction%>"><%=rulePreFunction%></option><%}}%></select></td>' +
                '<td style="padding-left:0px !important;padding-right:0px !important"><select id="ruleFunction_'  + index + '" name="ruleFunction_'  + index + '" class="leftCol-small"><%for (String ruleFunction : ruleFunctions) {if (currentFunction != null && ruleFunction.equals(currentFunction)) {%><option value="<%=ruleFunction%>" selected="selected"><%=ruleFunction%></option><%} else {%><option value="<%=ruleFunction%>"><%=ruleFunction%></option><%}}%></select></td>' +
                '<td style="padding-left:0px !important;padding-right:0px !important"><%if (currentAttributeValue != null && !"".equals(currentAttributeValue)) {%><input type="text" size="60" name="ruleAttributeValue_'  + index + '" id="ruleAttributeValue_'  + index + '" value="<%=currentAttributeValue%>" class="text-box-big"/><%} else {%><input type="text" size="60" name="ruleAttributeValue_'  + index + '" id="ruleAttributeValue_'  + index + '" class="text-box-big"/><%}%></td>' +
                '<a title="Select Resources Names" class="icon-link" onclick="selectAttributesForRule("<%=currentCategory%>");" style="background-image:url(images/registry.gif);"></a>' +
                '<td><input type="hidden" name="ruleAttributeId_'  + index +  '" id="ruleAttributeId_'  + index + '" value="<%=currentAttributeId%>"/></td>' +
                '<td><input type="hidden" name="ruleAttributeTypes_'  + index + '" id="ruleAttributeTypes_'  + index +  '" value="<%=currentAttributeDataType%>"/></td>' +
                '<td style="padding-left:0px !important;padding-right:0px !important"><select id="ruleCombineFunctions_'  + index + '" name="ruleCombineFunctions_'  + index + '" class="leftCol-small" onchange="createNewRuleRow(this.options[this.selectedIndex].value)"><%for (String combineFunction : combineFunctions) {if (currentCombineFunction != null && combineFunction.equals(currentCombineFunction)) {%><option value="<%=combineFunction%>" selected="selected"><%=combineFunction%></option><%} else {%><option value="<%=combineFunction%>"><%=combineFunction%></option><%}}%></select></td>' +
                '</tr>');
    }
}



</script>







<%

    if(targetDTO != null){

        ArrayList<RowDTO> rowDTOs = targetDTO.getRowDTOList();

        for(RowDTO rowDTO : rowDTOs){
            currentCategory = rowDTO.getCategory();
            currentPreFunction = rowDTO.getPreFunction();
            currentFunction = rowDTO.getFunction();
            currentAttributeValue =  rowDTO.getAttributeValue();
            currentAttributeId =  rowDTO.getAttributeId();
            currentAttributeDataType = rowDTO.getAttributeDataType();
            currentCombineFunction =  rowDTO.getCombineFunction();
        %>
            <script type="text/javascript">
                createNewTargetRow("NEW");
            </script>
        <%
        }
    } else {
        System.out.println("Target is NULL ");
    }

    if(ruleDTO != null){
        ArrayList<RowDTO> ruleRowDTOs = ruleDTO.getRowDTOList();

        for(RowDTO rowDTO : ruleRowDTOs){
            currentCategory = rowDTO.getCategory();
            currentPreFunction = rowDTO.getPreFunction();
            currentFunction = rowDTO.getFunction();
            currentAttributeValue =  rowDTO.getAttributeValue();
            currentAttributeId =  rowDTO.getAttributeId();
            currentAttributeDataType = rowDTO.getAttributeDataType();
            currentCombineFunction =  rowDTO.getCombineFunction();
        %>
            <script type="text/javascript">
                createNewRuleRow("NEW");
            </script>
        <%
        }
    } else {
        System.out.println("Rule is NULL ");            
    }

    ////////////////////////////////////////////////////////////////////////////


    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.
                    CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String backEndServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    String forwardTo = null;
    String[] algorithmNames = null;

    try {
        EntitlementPolicyAdminServiceClient client = new EntitlementPolicyAdminServiceClient(cookie,
                serverURL, configContext);
        algorithmNames = client.getEntitlementPolicyDataFromRegistry("ruleCombiningAlgorithms");
        ClaimAdminClient claimAdminClient = new ClaimAdminClient(cookie, backEndServerURL,
                configContext);
        claimAdminClient.getAllClaimMappingsByDialectWithRole(EntitlementPolicyConstants.DEFAULT_CARBON_DIALECT);

    } catch (Exception e) {
        String message = resourceBundle.getString("error.while.loading.policy.resource");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "../admin/error.jsp";

%>
<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
<%
    }
%>


<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
<carbon:breadcrumb
        label="create.basic.policy"
        resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="resources/js/main.js"></script>
<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
<script src="../entitlement/js/policy-editor.js" type="text/javascript"></script>
<link href="../entitlement/css/entitlement.css" rel="stylesheet" type="text/css" media="all"/>

<script type="text/javascript">

function orderRuleElement() {
    var ruleElementOrder = new Array();
    var tmp = jQuery("#dataTable tbody tr input");
    for (var i = 0; i < tmp.length; i++) {
        ruleElementOrder.push(tmp[i].value);
    }
    return ruleElementOrder;
}


function submitForm() {
    if (doValidationPolicyNameOnly()) {
        document.dataForm.action = "update-rule.jsp?nextPage=finish&ruleElementOrder="
                + orderRuleElement();
        document.dataForm.submit();
    }
}

function doCancel() {
    location.href = 'index.jsp';
}

function doValidation() {

    var value = document.getElementsByName("policyName")[0].value;
    if (value == '') {
        CARBON.showWarningDialog('<fmt:message key="policy.name.is.required"/>');
        return false;
    }

    value = document.getElementsByName("ruleId")[0].value;
    if (value == '') {
        CARBON.showWarningDialog('<fmt:message key="rule.id.is.required"/>');
        return false;
    }

    return true;
}

function doValidationPolicyNameOnly() {

    var value = document.getElementsByName("policyName")[0].value;
    if (value == '') {
        CARBON.showWarningDialog('<fmt:message key="policy.name.is.required"/>');
        return false;
    }

    return true;
}

function doUpdate() {
    if (doValidation()) {
        document.dataForm.action = "update-rule.jsp?nextPage=policy-editor&completedRule=true&updateRule=true&ruleElementOrder=" + orderRuleElement();
        document.dataForm.submit();
    }
}

function doCancelRule() {
    if (doValidation()) {
        document.dataForm.action = "update-rule.jsp?nextPage=policy-editor&ruleId=&ruleElementOrder=" + orderRuleElement();
        document.dataForm.submit();
    }
}

function deleteRule(ruleId) {
    document.dataForm.action = "update-rule.jsp?nextPage=delete-rule-entry&ruleId=" + ruleId + "&ruleElementOrder=" + orderRuleElement();
    document.dataForm.submit();
}

function editRule(ruleId) {
    document.dataForm.action = "update-rule.jsp?nextPage=policy-editor&editRule=true&ruleId=" + ruleId + "&ruleElementOrder=" + orderRuleElement();
    document.dataForm.submit();
}

function doAdd() {
    if (doValidation()) {
        document.dataForm.action = "update-rule.jsp?nextPage=policy-editor&completedRule=true&ruleElementOrder=" + orderRuleElement();
        document.dataForm.submit();
    }
}

function selectSubjects() {
    if (doValidation()) {
        document.dataForm.action = "update-rule.jsp?nextPage=select-subjects&ruleElementOrder=" + orderRuleElement();
        document.dataForm.submit();
    }
}

function selectRegistryResources() {
    if (doValidation()) {
        document.dataForm.action = "update-rule.jsp?nextPage=select-registry-resources&ruleElementOrder=" + orderRuleElement();
        document.dataForm.submit();
    }
}

function selectAttributes(attributeType) {
    if (doValidationPolicyNameOnly()) {
        document.dataForm.action = "update-rule.jsp?nextPage=select_attribute_values&updateRule=true&attributeType="
                + attributeType + "&ruleElementOrder=" + orderRuleElement();
        document.dataForm.submit();
    }
}

function selectDiscoveryResources() {
    if (doValidation()) {
        document.dataForm.action = "update-rule.jsp?nextPage=select-discovery-resources&ruleElementOrder=" + orderRuleElement();
        document.dataForm.submit();
    }
}

function selectSubjectsForTarget() {
    if (doValidationPolicyNameOnly()) {
        document.dataForm.action = "update-rule.jsp?nextPage=select-subjects&ruleId=&ruleElementOrder=" + orderRuleElement();
        document.dataForm.submit();
    }
}

function selectRegistryResourcesForTarget() {
    if (doValidationPolicyNameOnly()) {
        document.dataForm.action = "update-rule.jsp?nextPage=select-registry-resources&ruleId=&ruleElementOrder=" + orderRuleElement();
        document.dataForm.submit();
    }
}

function selectAttributesForTarget(category) {
    if (doValidationPolicyNameOnly()) {
        document.dataForm.action = "update-rule.jsp?nextPage=select_attribute_values&ruleId=&attributeType="
                + category + "&ruleElementOrder=" + orderRuleElement();
        document.dataForm.submit();
    }
}

function selectDiscoveryResourcesForTarget() {
    if (doValidationPolicyNameOnly()) {
        document.dataForm.action = "update-rule.jsp?nextPage=select-discovery-resources&ruleId=&ruleElementOrder=" + orderRuleElement();
        document.dataForm.submit();
    }
}

function updownthis(thislink, updown) {
    var sampleTable = document.getElementById('dataTable');
    var clickedRow = thislink.parentNode.parentNode;
    var addition = -1;
    if (updown == "down") {
        addition = 1;
    }
    var otherRow = sampleTable.rows[clickedRow.rowIndex + addition];
    var numrows = jQuery("#dataTable tbody tr").length;
    if (numrows <= 1) {
        return;
    }
    if (clickedRow.rowIndex == 1 && updown == "up") {
        return;
    } else if (clickedRow.rowIndex == numrows && updown == "down") {
        return;
    }
    var rowdata_clicked = new Array();
    for (var i = 0; i < clickedRow.cells.length; i++) {
        rowdata_clicked.push(clickedRow.cells[i].innerHTML);
        clickedRow.cells[i].innerHTML = otherRow.cells[i].innerHTML;
    }
    for (i = 0; i < otherRow.cells.length; i++) {
        otherRow.cells[i].innerHTML = rowdata_clicked[i];
    }
}


<%--function selectRightList(serviceUri) {--%>
    <%--var rightList = "";--%>
    <%--var operationNameElement = document.getElementById('userAttribute');--%>
    <%--if ('<%=selectUserAttributes%>' == serviceUri) {--%>
        <%--for (var i = 0; i < operationList.length; i++) {--%>

            <%--rightList = operationList[i];--%>
            <%--operationNameElement.innerHTML += "<option value='" + rightList + "'>" + rightList + "</option>";--%>
        <%--}--%>
    <%--}--%>
<%--}--%>


//jQuery(document).ready(function() {
//    jQuery('#multipleTargetTable tr td select[name^=targetCombineFunctions]').change(function() {
//        console.info(jQuery(this).val());
//        if (jQuery(this).val() == "AND" || jQuery(this).val() == "OR") {
//            createNewTargetRow();
//        }
//    });
//});


</script>


<div id="middle">
<h2><fmt:message key="create.entitlement.policy"/></h2>

<div id="workArea">
<div class="goToAdvance">
    <a class='icon-link' href="../entitlement/create-policy.jsp"
       style='background-image:url(images/advanceview.png);float:none'><fmt:message
            key="use.advance.view"/></a>
</div>
<form id="dataForm" name="dataForm" method="post" action="">
<table class="styledLeft noBorders">
<tr>
    <td class="leftCol-med"><fmt:message key='policy.name'/><span class="required">*</span></td>
    <%
        if (entitlementPolicyBean.getPolicyName() != null) {
    %>
    <td><input type="text" name="policyName" id="policyName"
               value="<%=entitlementPolicyBean.getPolicyName()%>" class="text-box-big"/></td>
    <%
    } else {
    %>
    <td><input type="text" name="policyName" id="policyName" class="text-box-big"/></td>
    <%
        }
    %>
</tr>

<tr>
    <td><fmt:message key="rule.combining.algorithm"/></td>
    <td>
        <select id="algorithmName" name="algorithmName" class="text-box-big">
            <%
                if (algorithmNames != null && algorithmNames.length > 0) {
                    for (String algorithmName : algorithmNames) {
                        if (algorithmName.equals(entitlementPolicyBean.getAlgorithmName())) {
            %>
            <option value="<%=algorithmName%>"
                    selected="selected"><%=entitlementPolicyBean.getAlgorithmName()%>
            </option>
            <%
            } else {
            %>
            <option value="<%=algorithmName%>"><%=algorithmName%>
            </option>
            <%
                        }
                    }
                }
            %>
        </select>
    </td>
</tr>

<tr>
    <td class="leftCol-small" style="vertical-align:top !important"><fmt:message
            key='policy.description'/></td>
    <%
        if (entitlementPolicyBean.getPolicyDescription() != null) {
    %>
    <td><textarea name="policyDescription" id="policyDescription"
                  value="<%=entitlementPolicyBean.getPolicyDescription()%>"
                  class="text-box-big"><%=entitlementPolicyBean.getPolicyDescription()%>
    </textarea></td>
    <%
    } else {
    %>
    <td><textarea type="text" name="policyDescription" id="policyDescription"
                  class="text-box-big"></textarea></td>
    <%
        }
    %>
</tr>


<tr>
    <td colspan="2">
        <script type="text/javascript">
            jQuery(document).ready(function() {
            <%if(targetDTO == null){%>
                jQuery("#newTargetLinkRow").hide();
            <%}else{ %>
                jQuery("#newTargetLinkRow").show();
            <% } %>

            <%if(ruleDTO == null){%>
                jQuery("#newRuleLinkRow").hide();
            <%}else{ %>
                jQuery("#newRuleLinkRow").show();
            <% } %>
                /*Hide (Collapse) the toggle containers on load use show() insted of hide() 	in the 			above code if you want to keep the content section expanded. */

                jQuery("h2.trigger").click(function() {
                    if (jQuery(this).next().is(":visible")) {
                        this.className = "active trigger";
                    } else {
                        this.className = "trigger";
                    }

                    jQuery(this).next().slideToggle("fast");
                    return false; //Prevent the browser jump to the link anchor
                });
            });
        </script>
        <h2 class="trigger  <%if(targetDTO == null){%>active<%} %>"><a
                href="#"><fmt:message key="policy.apply.to"/></a></h2>

        <div class="toggle_container" style="padding:0;margin-bottom:10px;" id="newTargetLinkRow">

            <table class="noBorders" cellspacing="0" style="width:100%;padding-top:5px;">
                <tr>
                    <td>
                        <table id="multipleTargetTable" name="multipleTargetTable" class="normal"
                               style="padding-left:0px !important">
                            <tr>

                                <td style="padding-left:0px !important;padding-right:0px !important">
                                    <select id="targetCategory_0" name="targetCategory_0" class="leftCol-small">
                                        <%
                                            for (String category : categories) {
                                                if (currentCategory != null && category.equals(currentCategory)) {
                                        %>
                                        <option value="<%=category%>" selected="selected"><%=category%></option>
                                        <%
                                                } else {
                                        %>
                                        <option value="<%=category%>"><%=category%></option>
                                        <%
                                                    }
                                                }
                                        %>
                                    </select>
                                </td>


                                <td style="padding-left:0px !important;padding-right:0px !important">
                                    <select id="targetPreFunction_0" name="targetPreFunction_0"
                                            class="leftCol-small">
                                        <%
                                            for (String targetPreFunction : targetPreFunctions) {
                                                if (currentPreFunction != null && targetPreFunction.equals(currentPreFunction)) {
                                        %>
                                        <option value="<%=targetPreFunction%>"
                                                selected="selected"><%=targetPreFunction%>
                                        </option>
                                        <%
                                        } else {
                                        %>
                                        <option value="<%=targetPreFunction%>"><%=targetPreFunction%>
                                        </option>
                                        <%
                                                }
                                            }
                                        %>
                                    </select>
                                </td>


                                <td style="padding-left:0px !important;padding-right:0px !important">
                                    <select id="targetFunction_0" name="targetFunction_0"
                                            class="leftCol-small">
                                        <%
                                            for (String targetFunction : targetFunctions) {
                                                if (currentFunction != null && targetFunction.equals(currentFunction)) {
                                        %>
                                        <option value="<%=targetFunction%>"
                                                selected="selected"><%=targetFunction%>
                                        </option>
                                        <%
                                        } else {
                                        %>
                                        <option value="<%=targetFunction%>"><%=targetFunction%>
                                        </option>
                                        <%
                                                }
                                            }
                                        %>
                                    </select>
                                </td>


                                <td style="padding-left:0px !important;padding-right:0px !important">
                                    <%
                                        if (currentAttributeValue != null && !"".equals(currentAttributeValue)) {

                                    %>
                                    <input type="text" size="60" name="targetAttributeValue_0"
                                           id="targetAttributeValue_0"
                                           value="<%=currentAttributeValue%>" class="text-box-big"/>
                                    <%
                                    } else {
                                    %>
                                    <input type="text" size="60" name="targetAttributeValue_0"
                                           id="targetAttributeValue_0"
                                           class="text-box-big" <%--onFocus="handleFocus(this,'Pick resource name')" onBlur="handleBlur(this,'Pick resource name');" class="defaultText text-box-big" --%>/>

                                    <%
                                        }
                                    %>
                                </td>
                                <td>
                                    <a title="Select Resources Names" class='icon-link'
                                       onclick='selectAttributesForTarget("Resource");'
                                       style='background-image:url(images/registry.gif);'></a>
                                </td>


                                <td>
                                    <input type="hidden" name="targetAttributeId_0"
                                           id="targetAttributeId_0" value="<%=currentAttributeId%>"/>
                                </td>

                                <td>
                                    <input type="hidden" name="targetAttributeTypes_0"
                                           id="targetAttributeTypes_0"
                                           value="<%=currentAttributeDataType%>"/>
                                </td>

                                <td style="padding-left:0px !important;padding-right:0px !important">
                                    <select id="targetCombineFunctions_0" name="targetCombineFunctions_0"
                                            class="leftCol-small" onchange="createNewTargetRow(this.options[this.selectedIndex].value)">
                                        <%
                                            for (String combineFunction : combineFunctions) {
                                                if (currentCombineFunction != null && combineFunction.equals(currentCombineFunction)) {
                                        %>
                                        <option value="<%=combineFunction%>"
                                                selected="selected"><%=combineFunction%>
                                        </option>
                                        <%
                                        } else {
                                        %>
                                        <option value="<%=combineFunction%>"><%=combineFunction%>
                                        </option>
                                        <%
                                                }
                                            }
                                        %>
                                    </select>
                                </td>

                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </div>

    </td>
</tr>
<tr>
<td colspan="2" style="margin-top:10px;">
<h2 class="trigger  <%if(ruleDTO == null){%>active<%} %>"><a href="#"><fmt:message
        key="add.new.entitlement.rule"/></a></h2>

<div class="toggle_container" id="newRuleLinkRow">


    <table class="noBorders" id="ruleTable" style="width: 100%">
        <body>
        <tr>
            <td class="formRow" style="padding:0 !important">
                <table class="normal" cellspacing="0">

                    <tr>
                        <td class="leftCol-small"><fmt:message key='rule.name'/><span
                                class="required">*</span>
                        </td>
                        <td>
                            <%
                                if (ruleId != null && !ruleId.trim().equals("") && !ruleId.trim().equals("null")) {
                            %>
                            <input type="text" name="ruleId" id="ruleId" class="text-box-big"
                                   value="<%=ruleDTO.getRuleId()%>"/>
                            <%
                            } else {
                            %>
                            <input type="text" name="ruleId" id="ruleId" class="text-box-big"/>
                            <%
                                }
                            %>
                        </td>
                    </tr>

                    <tr>
                        <td><fmt:message key="rule.effect"/></td>
                        <td>
                            <select id="ruleEffect" name="ruleEffect" class="leftCol-small">
                                <%
                                    if (ruleEffects != null) {
                                        for (String effect : ruleEffects) {
                                            if (effect.equals(ruleEffect)) {

                                %>
                                <option value="<%=effect%>" selected="selected"><%=ruleEffect%>
                                </option>
                                <%
                                } else {

                                %>
                                <option value="<%=effect%>"><%=effect%>
                                </option>
                                <%
                                            }
                                        }
                                    }
                                %>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td>
                        <table id="multipleRuleTable" name="multipleRuleTable" class="normal"
                               style="padding-left:0px !important">
                            <tr>

                                <td style="padding-left:0px !important;padding-right:0px !important">
                                    <select id="ruleCategory_0" name="ruleCategory_0"
                                            class="leftCol-small">
                                        <%
                                            for (String category : categories) {
                                                if (functionOnResourcesTarget != null && category.equals(functionOnResourcesTarget)) {
                                        %>
                                        <option value="<%=category%>"
                                                selected="selected"><%=category%>
                                        </option>
                                        <%
                                        } else {
                                        %>
                                        <option value="<%=category%>"><%=category%>
                                        </option>
                                        <%
                                                }
                                            }
                                        %>
                                    </select>
                                </td>


                                <%--<td style="padding-left:0px !important;padding-right:0px !important">--%>
                                    <%--<select id="userAttribute" name="userAttribute">--%>
                                      <%--onchange="selectRightList(this.options[this.selectedIndex].value)"--%>

                                    <%--</select>--%>
                                <%--</td>--%>


                                <td style="padding-left:0px !important;padding-right:0px !important">
                                    <select id="rulePreFunction_0" name="rulePreFunction_0"
                                            class="leftCol-small">
                                        <%
                                            for (String rulePreFunction : rulePreFunctions) {
                                                if (functionOnResourcesTarget != null && rulePreFunction.equals(functionOnResourcesTarget)) {
                                        %>
                                        <option value="<%=rulePreFunction%>"
                                                selected="selected"><%=rulePreFunction%>
                                        </option>
                                        <%
                                        } else {
                                        %>
                                        <option value="<%=rulePreFunction%>"><%=rulePreFunction%>
                                        </option>
                                        <%
                                                }
                                            }
                                        %>
                                    </select>
                                </td>


                                <td style="padding-left:0px !important;padding-right:0px !important">
                                    <select id="ruleFunction_0" name="ruleFunction_0"
                                            class="leftCol-small">
                                        <%
                                            for (String ruleFunction : ruleFunctions) {
                                                if (functionOnResourcesTarget != null && ruleFunction.equals(functionOnResourcesTarget)) {
                                        %>
                                        <option value="<%=ruleFunction%>"
                                                selected="selected"><%=ruleFunction%>
                                        </option>
                                        <%
                                        } else {
                                        %>
                                        <option value="<%=ruleFunction%>"><%=ruleFunction%>
                                        </option>
                                        <%
                                                }
                                            }
                                        %>
                                    </select>
                                </td>


                                <td style="padding-left:0px !important;padding-right:0px !important">
                                    <%
                                        if (resourceNamesTarget != null && !resourceNamesTarget.equals("")) {

                                    %>
                                    <input type="text" size="60" name="ruleAttributeValue_0"
                                           id="ruleAttributeValue_0"
                                           value="<%=resourceNamesTarget%>" class="text-box-big"/>
                                    <%
                                    } else {
                                    %>
                                    <input type="text" size="60" name="ruleAttributeValue_0"
                                           id="ruleAttributeValue_0"
                                           class="text-box-big" <%--onFocus="handleFocus(this,'Pick resource name')" onBlur="handleBlur(this,'Pick resource name');" class="defaultText text-box-big" --%>/>

                                    <%
                                        }
                                    %>
                                </td>
                                <td>
                                    <a title="Select Resources Names" class='icon-link'
                                       onclick='selectAttributesForTarget("Resource");'
                                       style='background-image:url(images/registry.gif);'></a>
                                </td>


                                <td>
                                    <input type="hidden" name="ruleAttributeId_0"
                                           id="ruleAttributeId_0" value="<%=resourceIdTarget%>"/>
                                </td>

                                <td>
                                    <input type="hidden" name="ruleAttributeTypes_0"
                                           id="ruleAttributeTypes_0"
                                           value="<%=resourceDataTypeTarget%>"/>
                                </td>

                                <td style="padding-left:0px !important;padding-right:0px !important">
                                    <select id="ruleCombineFunctions_0" name="ruleCombineFunctions_0"
                                            class="leftCol-small" onchange="createNewRuleRow(this.options[this.selectedIndex].value)">
                                        <%
                                            for (String combineFunction : combineFunctions) {
                                                if (functionOnResourcesTarget != null && combineFunction.equals(functionOnResourcesTarget)) {
                                        %>
                                        <option value="<%=combineFunction%>"
                                                selected="selected"><%=combineFunction%>
                                        </option>
                                        <%
                                        } else {
                                        %>
                                        <option value="<%=combineFunction%>"><%=combineFunction%>
                                        </option>
                                        <%
                                                }
                                            }
                                        %>
                                    </select>
                                </td>

                            </tr>
                        </table>

                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td colspan="2" class="buttonRow">
                <%
                    if (ruleDTO != null && ruleDTO.isCompletedRule()) {
                %>
                <input class="button" type="button" value="<fmt:message key='update'/>"
                       onclick="doUpdate();"/>

                <input class="button" type="button" value="<fmt:message key='cancel'/>"
                       onclick="doCancelRule();"/>

                <%
                } else {
                %>

                <input class="button" type="button" value="<fmt:message key='add'/>"
                       onclick="doAdd();"/>
                <%
                    }
                %>
            </td>
        </tr>
        </body>
    </table>
</div>

<table class="styledLeft" id="dataTable" style="width: 100%;margin-top:10px;">
    <thead>
    <tr>
        <th><fmt:message key="rule.id"/></th>
        <th><fmt:message key="rule.effect"/></th>
        <th><fmt:message key="action"/></th>
    </tr>
    </thead>
    <body>
    <%
        if (ruleDTOs != null && ruleDTOs.size() > 0) {
            List<RuleDTO> orderedRuleDTOs = new ArrayList<RuleDTO>();
            String ruleElementOrder = entitlementPolicyBean.getRuleElementOrder();
            if (ruleElementOrder != null) {
                String[] orderedRuleIds = ruleElementOrder.split(EntitlementPolicyConstants.ATTRIBUTE_SEPARATOR);
                for (String orderedRuleId : orderedRuleIds) {
                    for (RuleDTO dto : ruleDTOs) {
                        if (orderedRuleId.trim().equals(dto.getRuleId())) {
                            orderedRuleDTOs.add(dto);
                        }
                    }
                }
            }

            if (orderedRuleDTOs.size() < 1) {
                orderedRuleDTOs = ruleDTOs;
            }
            for (RuleDTO orderedRuleDTO : orderedRuleDTOs) {
                if (orderedRuleDTO.isCompletedRule()) {
    %>
    <tr>

        <td>
            <a class="icon-link" onclick="updownthis(this,'up')"
               style="background-image:url(../admin/images/up-arrow.gif)"></a>
            <a class="icon-link" onclick="updownthis(this,'down')"
               style="background-image:url(../admin/images/down-arrow.gif)"></a>
            <input type="hidden" value="<%=orderedRuleDTO.getRuleId()%>"/>
            <%=orderedRuleDTO.getRuleId()%>
        </td>
        <td><%=orderedRuleDTO.getRuleEffect()%>
        </td>
        <td>
            <a href="#" onclick="editRule('<%=orderedRuleDTO.getRuleId()%>')"
               class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message
                    key="edit"/></a>
            <a href="#" onclick="deleteRule('<%=orderedRuleDTO.getRuleId()%>')"
               class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message
                    key="delete"/></a>
        </td>
    </tr>
    <%
            }
        }
    } else {
    %>
    <tr class="noRuleBox">
        <td colspan="3"><fmt:message key="no.rule.defined"/><br/></td>
    </tr>
    <%
        }
    %>
    </body>
</table>
</td>
</tr>
<tr>
    <td colspan="2">

    </td>
</tr>
<tr>
    <td class="buttonRow" colspan="2">
        <input type="button" onclick="submitForm();" value="<fmt:message key="finish"/>"
               class="button"/>
        <input type="button" onclick="doCancel();" value="<fmt:message key="cancel" />"
               class="button"/>
    </td>
</tr>
</table>
</form>
</div>
</div>
</fmt:bundle>
