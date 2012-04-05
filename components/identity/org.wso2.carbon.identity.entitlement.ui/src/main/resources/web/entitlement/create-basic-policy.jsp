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
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.BasicRuleElementDTO" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.claim.mgt.ui.client.ClaimAdminClient" %>
<%@ page import="org.wso2.carbon.claim.mgt.stub.dto.ClaimDialectDTO" %>
<%@ page import="org.wso2.carbon.claim.mgt.stub.dto.ClaimMappingDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.BasicTargetElementDTO" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.AttributeValueTreeNodeDTO" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />


<%
    BasicRuleElementDTO  basicRuleElementDTO = null;
    ClaimDialectDTO claimDialectDTO = null;

    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    List<BasicRuleElementDTO> basicRuleElementDTOs = entitlementPolicyBean.getBasicRuleElementDTOs();
    BasicTargetElementDTO basicTargetElementDTO = entitlementPolicyBean.getBasicTargetElementDTO();

    String selectedAttributeDataType = (String)request.getParameter("selectedAttributeDataType");
    String selectedAttributeId = (String)request.getParameter("selectedAttributeId");
    String attributeType = (String)request.getParameter("attributeType");

    String ruleId = CharacterEncoder.getSafeText(request.getParameter("ruleId"));
    if(ruleId != null && ruleId.trim().length() > 0 && !ruleId.trim().equals("null") ) {
        basicRuleElementDTO = entitlementPolicyBean.getBasicRuleElement(ruleId);
    }
        
    if("null".equals(selectedAttributeId)){
        selectedAttributeId = null;
    }

    if("null".equals(selectedAttributeDataType)){
        selectedAttributeDataType = null;
    }

    String selectedSubjectType = CharacterEncoder.getSafeText(request.getParameter("selectedSubjectType"));
    if(selectedSubjectType != null  && ! "".equals(selectedSubjectType)){
        if(ruleId != null && !ruleId.equals("")){
            entitlementPolicyBean.subjectTypeMap.put(ruleId, selectedSubjectType);          
        } else {
            entitlementPolicyBean.subjectTypeMap.put("Target", selectedSubjectType);                      
        }
    }

    String selectedAttributeNames= "";
    String selectedSubjectNames = "";
    String selectedResourceNames = "";
    String selectedActionNames = "";
    String selectedEnvironmentNames = "";
    String selectedResourceId="";
    String selectedResourceDataType="";
    String selectedSubjectId="";
    String selectedSubjectDataType="";
    String selectedActionId="";
    String selectedActionDataType="";
    String selectedEnvironmentId="";
    String selectedEnvironmentDataType="";    
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
    String resourceId= "";
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
    while(true) {
        String attributeName  = request.getParameter("resourceName" + noOfSelectedAttributes);
        if (attributeName == null || attributeName.trim().length() < 1) {
            break;
        }
        if(selectedAttributeNames.equals("")) {
            selectedAttributeNames = attributeName.trim();
        } else {
            selectedAttributeNames = selectedAttributeNames + "," + attributeName.trim();
        }
        noOfSelectedAttributes ++;
    }

    if(attributeType != null){
        if (EntitlementPolicyConstants.RESOURCE_ELEMENT.equals(attributeType)){
            selectedResourceNames = selectedAttributeNames;
            selectedResourceId = selectedAttributeId;
            selectedResourceDataType = selectedAttributeDataType;
        } else if (EntitlementPolicyConstants.SUBJECT_ELEMENT.equals(attributeType)){
            selectedSubjectNames = selectedAttributeNames;
            selectedSubjectId = selectedAttributeId;
            selectedSubjectDataType = selectedAttributeDataType;
        } else if (EntitlementPolicyConstants.ACTION_ELEMENT.equals(attributeType)){
            selectedActionNames = selectedAttributeNames;
            selectedActionId = selectedAttributeId;
            selectedActionDataType = selectedAttributeDataType;
        } else if (EntitlementPolicyConstants.ENVIRONMENT_ELEMENT.equals(attributeType)){
            selectedEnvironmentNames = selectedAttributeNames;
            selectedEnvironmentId = selectedAttributeId;
            selectedEnvironmentDataType = selectedAttributeDataType;
        }
    }



    /**
     * Get posted subjects from jsp pages and put then in to a String object
     */
    String[] subjects  = request.getParameterValues("subjects");
    if(subjects != null) {
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
    String[] functionIds = new String[] {EntitlementPolicyConstants.EQUAL_TO,
            EntitlementPolicyConstants.IS_IN, EntitlementPolicyConstants.AT_LEAST,
            EntitlementPolicyConstants.SUBSET_OF, EntitlementPolicyConstants.REGEXP_MATCH,
            EntitlementPolicyConstants.SET_OF };


    String[] targetFunctionIds = new String[] {EntitlementPolicyConstants.EQUAL_TO,
            EntitlementPolicyConstants.AT_LEAST_ONE_MATCH,
            EntitlementPolicyConstants.AT_LEAST_ONE_MATCH_REGEXP, EntitlementPolicyConstants.REGEXP_MATCH,
            EntitlementPolicyConstants.SET_OF, EntitlementPolicyConstants.MATCH_REGEXP_SET_OF };    

    String[] ruleEffects = new String[] {EntitlementPolicyConstants.RULE_EFFECT_PERMIT,
            EntitlementPolicyConstants.RULE_EFFECT_DENY};

    String[] dataTypes = new String[] {EntitlementPolicyConstants.RULE_EFFECT_PERMIT };

    /**
     * Assign current BasicRule Object Values to variables to show on UI
     */
    if(basicRuleElementDTO != null){

        ruleEffect = basicRuleElementDTO.getRuleEffect();
        ruleId = basicRuleElementDTO.getRuleId();
        ruleDescription = basicRuleElementDTO.getRuleDescription();

        resourceNames =  basicRuleElementDTO.getResourceList();
        subjectNames = basicRuleElementDTO.getSubjectList();
        actionNames = basicRuleElementDTO.getActionList();
        environmentNames = basicRuleElementDTO.getEnvironmentList();
        userAttributeValue = basicRuleElementDTO.getUserAttributeValue();

        functionOnActions = basicRuleElementDTO.getFunctionOnActions();
        functionOnResources = basicRuleElementDTO.getFunctionOnResources();
        functionOnSubjects = basicRuleElementDTO.getFunctionOnSubjects();
        functionOnEnvironment = basicRuleElementDTO.getFunctionOnEnvironment();
        functionOnAttributes = basicRuleElementDTO.getFunctionOnAttributes();

        if(selectedResourceDataType != null && selectedResourceDataType.trim().length() > 0){
            resourceDataType = selectedResourceDataType;
        } else {
            resourceDataType = basicRuleElementDTO.getResourceDataType();
        }

        if(selectedSubjectDataType != null && selectedSubjectDataType.trim().length() > 0){
            subjectDataType = selectedSubjectDataType;
        } else {
            subjectDataType = basicRuleElementDTO.getSubjectDataType();
        }

        if(selectedActionDataType != null && selectedActionDataType.trim().length() > 0){
            actionDataType = selectedActionDataType;
        } else {
            actionDataType = basicRuleElementDTO.getActionDataType();
        }

        if(selectedEnvironmentDataType != null && selectedEnvironmentDataType.trim().length() > 0){
            environmentDataType = selectedEnvironmentDataType;
        } else {
            environmentDataType = basicRuleElementDTO.getEnvironmentDataType();
        }

        userAttributeValueDataType = basicRuleElementDTO.getUserAttributeValueDataType();

        if(selectedResourceId != null && selectedResourceId.trim().length() > 0){
            resourceId = selectedResourceId;
        } else {
            resourceId = basicRuleElementDTO.getResourceId();
        }

        if(selectedSubjectId != null && selectedSubjectId.trim().length() > 0){
            subjectId = selectedSubjectId;
        } else {
            subjectId = basicRuleElementDTO.getSubjectId();
        }

        if(selectedActionId != null && selectedActionId.trim().length() > 0){
            actionId = selectedActionId;
        } else {
            actionId = basicRuleElementDTO.getActionId();
        }

        if(selectedEnvironmentId != null && selectedEnvironmentId.trim().length() > 0){
            environmentId = selectedEnvironmentId;
        } else {
            environmentId = basicRuleElementDTO.getEnvironmentId();
        }

        attributeId = basicRuleElementDTO.getAttributeId();

        if(!entitlementPolicyBean.subjectTypeMap.containsKey(ruleId)){
            entitlementPolicyBean.subjectTypeMap.put(ruleId, basicRuleElementDTO.getSubjectType());
        }

        if(selectedResourceNames != null && selectedResourceNames.trim().length() > 0){
            if(resourceNames != null && resourceNames.trim().length() > 0){
                resourceNames = resourceNames + ","  + selectedResourceNames;
            } else {
                resourceNames = selectedResourceNames;
            }
        }

        if(selectedSubjectNames != null && selectedSubjectNames.trim().length() > 0){
            if(subjectNames != null && subjectNames.trim().length() > 0){
                subjectNames = subjectNames + ","  + selectedSubjectNames;
            } else {
                subjectNames = selectedSubjectNames;
            }
        }

        if(selectedActionNames != null && selectedActionNames.trim().length() > 0){
            if(actionNames != null && actionNames.trim().length() > 0){
                actionNames = actionNames + ","  + selectedActionNames;
            } else {
                actionNames = selectedActionNames;
            }
        }

        if(selectedEnvironmentNames != null && selectedEnvironmentNames.trim().length() > 0){
            if(environmentNames != null && environmentNames.trim().length() > 0){
                environmentNames = environmentNames + ","  + selectedEnvironmentNames;
            } else {
                environmentNames = selectedEnvironmentNames;
            }
        }

    }

    /**
     * Assign current BasicTarget Object Values to variables to show on UI.
     */
    if(basicTargetElementDTO != null){
        
        resourceNamesTarget =  basicTargetElementDTO.getResourceList();
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

        resourceDataTypeTarget  = basicTargetElementDTO.getResourceDataType();
        subjectDataTypeTarget  = basicTargetElementDTO.getSubjectDataType();
        actionDataTypeTarget  = basicTargetElementDTO.getActionDataType();
        environmentDataTypeTarget  = basicTargetElementDTO.getEnvironmentDataType();

        resourceIdTarget = basicTargetElementDTO.getResourceId();
        subjectIdTarget = basicTargetElementDTO.getSubjectId();
        actionIdTarget = basicTargetElementDTO.getActionId();
        environmentIdTarget = basicTargetElementDTO.getEnvironmentId();

        if(!entitlementPolicyBean.subjectTypeMap.containsKey("Target")){
            entitlementPolicyBean.subjectTypeMap.put("Target", basicTargetElementDTO.getSubjectType());            
        }

        if(basicRuleElementDTO == null) {
            if(selectedResourceNames != null && selectedResourceNames.trim().length() > 0){
                if(resourceNamesTarget != null && resourceNamesTarget.trim().length() > 0){
                    resourceNamesTarget = resourceNamesTarget + ","  + selectedResourceNames;
                } else {
                    resourceNamesTarget = selectedResourceNames;
                }
            }

            if(selectedSubjectNames != null && selectedSubjectNames.trim().length() > 0){
                if(subjectNamesTarget != null && subjectNamesTarget.trim().length() > 0){
                    subjectNamesTarget = subjectNamesTarget + ","  + selectedSubjectNames;
                } else {
                    subjectNamesTarget = selectedSubjectNames;
                }
            }

            if(selectedActionNames != null && selectedActionNames.trim().length() > 0){
                if(actionNamesTarget != null && actionNamesTarget.trim().length() > 0){
                    actionNamesTarget = actionNamesTarget + ","  + selectedActionNames;
                } else {
                    actionNamesTarget = selectedActionNames;
                }
            }

            if(selectedEnvironmentNames != null && selectedEnvironmentNames.trim().length() > 0){
                if(environmentNamesTarget != null && environmentNamesTarget.trim().length() > 0){
                    environmentNamesTarget = environmentNamesTarget + ","  + selectedEnvironmentNames;
                } else {
                    environmentNamesTarget = selectedEnvironmentNames;
                }
            }

            if(selectedResourceDataType != null && selectedResourceDataType.trim().length() > 0){
                resourceDataTypeTarget = selectedResourceDataType;
            }

            if(selectedSubjectDataType != null && selectedSubjectDataType.trim().length() > 0){
                subjectDataTypeTarget  = selectedSubjectDataType;
            }

            if(selectedActionDataType != null && selectedActionDataType.trim().length() > 0){
                actionDataTypeTarget  = selectedActionDataType;
            }

            if(selectedEnvironmentDataType != null && selectedEnvironmentDataType.trim().length() > 0){
                environmentDataTypeTarget  = selectedEnvironmentDataType;
            }

            if(selectedResourceId != null && selectedResourceId.trim().length() > 0){
                resourceIdTarget = selectedResourceId;
            }

            if(selectedSubjectId != null && selectedSubjectId.trim().length() > 0){
                subjectIdTarget = selectedSubjectId;
            }

            if(selectedActionId != null && selectedActionId.trim().length() > 0){
                actionIdTarget = selectedActionId;
            }

            if(selectedEnvironmentId != null && selectedEnvironmentId.trim().length() > 0){
                environmentIdTarget = selectedEnvironmentId;
            }
        }
    }

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
        claimDialectDTO =claimAdminClient.getAllClaimMappingsByDialectWithRole(EntitlementPolicyConstants.DEFAULT_CARBON_DIALECT);

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
		request="<%=request%>" />
    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="resources/js/main.js"></script>
    <!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
<script src="../entitlement/js/create-basic-policy.js" type="text/javascript"></script>
<link href="../entitlement/css/entitlement.css" rel="stylesheet" type="text/css" media="all"/>

<script type="text/javascript">

    function orderRuleElement(){
        var ruleElementOrder = new Array();
        var tmp = jQuery("#dataTable tbody tr input");
        for (var i = 0 ; i < tmp.length; i++){
            ruleElementOrder.push(tmp[i].value);
        }
        return ruleElementOrder;
    }


    function submitForm() {
        if(doValidationPolicyNameOnly()){
            document.dataForm.action = "update-rule.jsp?nextPage=finish&ruleElementOrder="
                    + orderRuleElement();
            document.dataForm.submit();
        }
    }

    function doCancel() {
        location.href  = 'index.jsp';
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

    function doUpdate(){
        if(doValidation()){
            document.dataForm.action = "update-rule.jsp?nextPage=create-basic-policy&completedRule=true&updateRule=true&ruleElementOrder=" + orderRuleElement();
            document.dataForm.submit();
        }
    }
    
    function doCancelRule(){
        if(doValidation()){
            document.dataForm.action = "update-rule.jsp?nextPage=create-basic-policy&ruleId=&ruleElementOrder=" + orderRuleElement();
            document.dataForm.submit();
        }
    }

    function deleteRule(ruleId) {
        document.dataForm.action = "update-rule.jsp?nextPage=delete-rule-entry&ruleId=" + ruleId + "&ruleElementOrder=" + orderRuleElement();
        document.dataForm.submit();        
    }

    function editRule(ruleId){
        document.dataForm.action = "update-rule.jsp?nextPage=create-basic-policy&editRule=true&ruleId=" + ruleId + "&ruleElementOrder=" + orderRuleElement();
        document.dataForm.submit();
    }

    function doAdd() {
        if(doValidation()){
            document.dataForm.action = "update-rule.jsp?nextPage=create-basic-policy&completedRule=true&ruleElementOrder=" + orderRuleElement();
            document.dataForm.submit();
        }
    }

    function selectSubjects() {
        if(doValidation()){
            document.dataForm.action = "update-rule.jsp?nextPage=select-subjects&ruleElementOrder=" + orderRuleElement();
            document.dataForm.submit();
        }
    }

    function selectRegistryResources() {
        if(doValidation()){
            document.dataForm.action = "update-rule.jsp?nextPage=select-registry-resources&ruleElementOrder=" + orderRuleElement();
            document.dataForm.submit();
        }
    }

    function selectAttributes(attributeType){
        if(doValidationPolicyNameOnly()){
            document.dataForm.action = "update-rule.jsp?nextPage=select_attribute_values&updateRule=true&attributeType="
                    + attributeType + "&ruleElementOrder=" + orderRuleElement();
            document.dataForm.submit();
        }
    }

    function selectDiscoveryResources() {
        if(doValidation()){
            document.dataForm.action = "update-rule.jsp?nextPage=select-discovery-resources&ruleElementOrder=" + orderRuleElement();
            document.dataForm.submit();
        }
    }

    function selectSubjectsForTarget() {
        if(doValidationPolicyNameOnly()){
            document.dataForm.action = "update-rule.jsp?nextPage=select-subjects&ruleId=&ruleElementOrder=" + orderRuleElement();
            document.dataForm.submit();
        }
    }

    function selectRegistryResourcesForTarget() {
        if(doValidationPolicyNameOnly()){
            document.dataForm.action = "update-rule.jsp?nextPage=select-registry-resources&ruleId=&ruleElementOrder=" + orderRuleElement();
            document.dataForm.submit();
        }
    }

    function selectAttributesForTarget(attributeType){
        if(doValidationPolicyNameOnly()){
            document.dataForm.action = "update-rule.jsp?nextPage=select_attribute_values&ruleId=&attributeType="
                    + attributeType + "&ruleElementOrder=" + orderRuleElement();
            document.dataForm.submit();
        }
    }

    function selectDiscoveryResourcesForTarget() {
        if(doValidationPolicyNameOnly()){
            document.dataForm.action = "update-rule.jsp?nextPage=select-discovery-resources&ruleId=&ruleElementOrder=" + orderRuleElement();
            document.dataForm.submit();
        }
    }

    function updownthis(thislink,updown){
        var sampleTable = document.getElementById('dataTable');
        var clickedRow = thislink.parentNode.parentNode;
        var addition = -1;
        if(updown == "down"){
            addition = 1;
        }
        var otherRow = sampleTable.rows[clickedRow.rowIndex + addition];
        var numrows = jQuery("#dataTable tbody tr").length;
        if(numrows <= 1){
            return;
        }
        if(clickedRow.rowIndex == 1 && updown == "up"){
            return;
        } else if(clickedRow.rowIndex == numrows && updown == "down"){
            return;
        }
        var rowdata_clicked = new Array();
        for(var i=0;i<clickedRow.cells.length;i++){
            rowdata_clicked.push(clickedRow.cells[i].innerHTML);
            clickedRow.cells[i].innerHTML = otherRow.cells[i].innerHTML;
        }
        for(i=0;i<otherRow.cells.length;i++){
            otherRow.cells[i].innerHTML =rowdata_clicked[i];
        }
    }
</script>



<div id="middle">
    <h2><fmt:message key="create.entitlement.policy"/></h2>
    <div id="workArea">
        <div class="goToAdvance">
            <a class='icon-link' href="../entitlement/create-policy.jsp"
                       style='background-image:url(images/advanceview.png);float:none'><fmt:message key="use.advance.view"/></a>       
        </div>
        <form id="dataForm" name="dataForm" method="post" action="">
        <table class="styledLeft noBorders">
            <tr>
              <td class="leftCol-med"><fmt:message key='policy.name'/><span class="required">*</span></td>
              <%
                  if(entitlementPolicyBean.getPolicyName() != null) {
              %>
                <td><input type="text" name="policyName" id="policyName" value="<%=entitlementPolicyBean.getPolicyName()%>" class="text-box-big"/></td>
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
                      if(algorithmName.equals(entitlementPolicyBean.getAlgorithmName())){
            %>
                  <option value="<%=algorithmName%>" selected="selected"><%=entitlementPolicyBean.getAlgorithmName()%></option>
            <%
                        } else {
            %>
                  <option value="<%=algorithmName%>"><%=algorithmName%></option>
            <%
                        }
                    }
                }
            %>
            </select>
            </td>
            </tr>

            <tr>
              <td class="leftCol-small" style="vertical-align:top !important"><fmt:message key='policy.description'/></td>
              <%
                  if(entitlementPolicyBean.getPolicyDescription() != null) {
              %>
              <td><textarea name="policyDescription" id="policyDescription" value="<%=entitlementPolicyBean.getPolicyDescription()%>" class="text-box-big"><%=entitlementPolicyBean.getPolicyDescription()%></textarea></td>
              <%
                  } else {
              %>
                <td><textarea type="text" name="policyDescription" id="policyDescription" class="text-box-big"></textarea></td>
              <%
                  }
              %>
            </tr>


            <tr>
            <td colspan="2">
            <script type="text/javascript">
                jQuery(document).ready(function() {
                    <%if(basicTargetElementDTO == null){%>
                        jQuery("#newTargetLinkRow").hide();
                    <%}else{ %>
                        jQuery("#newTargetLinkRow").show();
                    <% } %>

                    <%if(basicRuleElementDTO == null){%>
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
            <h2 class="trigger  <%if(basicTargetElementDTO == null){%>active<%} %>"><a href="#"><fmt:message key="policy.apply.to"/></a></h2>

            <div class="toggle_container" style="padding:0;margin-bottom:10px;" id="newTargetLinkRow">
                 
                <table class="noBorders" cellspacing="0" style="width:100%;padding-top:5px;">
                <tr>
                    <td class="leftCol-small"><fmt:message key='resource.names'/></td>
                    <td>
                        <table class="normal" style="padding-left:0px !important">
                            <tr>
                                <td style="padding-left:0px !important;padding-right:0px !important">
                                    <select id="functionOnResourcesTarget" name="functionOnResourcesTarget"
                                            class="leftCol-small">
                                    <%
                                        for (String functionId : targetFunctionIds) {
                                            if (functionOnResourcesTarget != null && functionId.equals(functionOnResourcesTarget)) {
                                    %>
                                    <option value="<%=functionId%>"
                                            selected="selected"><%=functionOnResourcesTarget%>
                                    </option>
                                    <%
                                    } else {
                                    %>
                                    <option value="<%=functionId%>"><%=functionId%>
                                    </option>
                                    <%
                                            }
                                        }
                                    %>
                                </select></td>
                                <td style="padding-left:0px !important;padding-right:0px !important">
                                    <%
                                        if (resourceNamesTarget != null && !resourceNamesTarget.equals("")) {

                                    %>
                                    <input type="text" size="60" name="resourceNamesTarget" id="resourceNamesTarget"
                                           value="<%=resourceNamesTarget%>" class="text-box-big"/>
                                    <%
                                    } else {
                                    %>
                                    <input type="text" size="60" name="resourceNamesTarget" id="resourceNamesTarget"
                                           class="text-box-big" <%--onFocus="handleFocus(this,'Pick resource name')" onBlur="handleBlur(this,'Pick resource name');" class="defaultText text-box-big" --%>/>

                                    <%
                                        }
                                    %>
				</td>
				<td>
                                    <a title="Select Resources Names" class='icon-link' onclick='selectAttributesForTarget("Resource");'
                                       style='background-image:url(images/registry.gif);'></a>
                                </td>

                                <td>
                                    <input type="hidden" name="resourceIdTarget" id="resourceIdTarget" value="<%=resourceIdTarget%>" />
                                </td>

                                <td>
                                    <input type="hidden" name="resourceDataTypeTarget" id="resourceDataTypeTarget" value="<%=resourceDataTypeTarget%>" />
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>


                <tr>
                    <td class="leftCol-small"><fmt:message key='roles.users'/></td>
                    <td>
                        <table class="normal" style="padding-left:0px !important">
                            <tr>
                                <td style="padding-left:0px !important;padding-right:0px !important">
                            <select id="functionOnSubjectsTarget" name="functionOnSubjectsTarget" class="leftCol-small">
                            <%
                                for (String functionId : targetFunctionIds) {
                                    if (functionOnSubjectsTarget != null && functionId.equals(functionOnSubjectsTarget)) {
                            %>
                            <option value="<%=functionId%>"
                                    selected="selected"><%=functionOnSubjectsTarget%>
                            </option>
                            <%
                            } else {
                            %>
                            <option value="<%=functionId%>"><%=functionId%>
                            </option>
                            <%
                                    }
                                }
                            %>
                        </select>
                        </td>
                        <td style="padding-left:0px !important;padding-right:0px !important">

                        <%
                            if (subjectNamesTarget != null && !subjectNamesTarget.equals("")) {

                        %>
                        <input type="text" name="subjectNamesTarget" id="subjectNamesTarget"
                               value="<%=subjectNamesTarget%>" class="text-box-big"/>
                        <%
                        } else {
                        %>
                        <input type="text" name="subjectNamesTarget" id="subjectNamesTarget" class="text-box-big" <%--onFocus="handleFocus(this,'Pick role name')" onBlur="handleBlur(this,'Pick role name');" class="defaultText text-box-big"--%> />

                        <%
                            }
                        %>
			</td>
			<td>
                        <a title="Select Subject Names" class='icon-link' onclick='selectAttributesForTarget("Subject");'
                           style='background-image:url(images/user-store.gif); float:right;'></a>
                        </td>

                        <td>
                            <input type="hidden" name="subjectIdTarget" id="subjectIdTarget" value="<%=subjectIdTarget%>" />
                        </td>

                        <td>
                            <input type="hidden" name="subjectDataTypeTarget" id="subjectDataTypeTarget" value="<%=subjectDataTypeTarget%>" />
                        </td>

                        </tr>
                        </table>
                    </td>
                </tr>


                <tr>
                    <td class="leftCol-small"><fmt:message key='user.attribute'/></td>
                    <td>
                    <table class="normal" style="padding-left:0px !important">
                        <tr>
                        <td style="padding-left:0px !important;padding-right:0px !important">
                        <select id="attributeIdTarget" name="attributeIdTarget" class="leftCol-small">
                        <option value="<%=EntitlementPolicyConstants.COMBO_BOX_DEFAULT_VALUE%>" selected="selected">
                            <%=EntitlementPolicyConstants.COMBO_BOX_DEFAULT_VALUE%></option>
                            <%
                                if (claimDialectDTO != null && claimDialectDTO.getClaimMappings() != null) {

                                    for (ClaimMappingDTO claimMappingDTO : claimDialectDTO.getClaimMappings()) {
                                        String claimUri = claimMappingDTO.getClaim().getClaimUri();

                                        if (attributeIdTarget != null && claimUri.equals(attributeIdTarget)) {
                            %>
                            <option value="<%=claimUri%>" selected="selected"><%=attributeIdTarget%></option>
                            <%
                            } else {
                            %>
                            <option value="<%=claimUri%>"><%=claimUri%>
                            </option>
                            <%
                                        }
                                    }
                                }
                            %>
                        </select>
                        </td>

                        <td style="padding-left:0px !important;padding-right:0px !important">
                        <select id="functionOnAttributesTarget" name="functionOnAttributesTarget">
                        <%
                            for(String functionId : targetFunctionIds) {
                                if(functionId.equals(EntitlementPolicyConstants.EQUAL_TO) || functionId.equals(EntitlementPolicyConstants.REGEXP_MATCH)) {
                                    if(functionOnAttributesTarget != null && functionId.equals(functionOnAttributesTarget)) {
                        %>
                            <option value="<%=functionId%>" selected="selected"><%=functionOnAttributesTarget%></option>
                        <%
                                    } else {
                        %>
                            <option value="<%=functionId%>" ><%=functionId%></option>
                        <%
                                    }
                                }
                            }
                        %>
                        </select>
                        </td>

                        <td style="padding-left:0px !important;padding-right:0px !important">
                        <%
                            if (userAttributeValueTarget != null && !userAttributeValueTarget.equals("")) {

                        %>
                        <input type="text" name="userAttributeValueTarget" id="userAttributeValueTarget"
                               value="<%=userAttributeValueTarget%>" style="width: 285px" />
                        <%
                        } else {
                        %>
                        <input type="text" name="userAttributeValueTarget" id="userAttributeValueTarget" style="width: 285px" <%--onFocus="handleFocus(this,'User attribute')" onBlur="handleBlur(this,'User attribute');" class="defaultText text-box-big"--%>/>

                        <%
                            }
                        %>
                        </td>

                        <td>

                        </td>

                        </tr>
                    </table>
                </tr>


                <tr>
                    <td class="leftCol-small"><fmt:message key='action.names'/></td>
                    <td>
                        <table class="normal" style="padding-left:0px !important">
                        <tr>
                            <td style="padding-left:0px !important;padding-right:0px !important">

                            <select id="functionOnActionsTarget" name="functionOnActionsTarget" class="leftCol-small">
                            <%
                                for (String functionId : targetFunctionIds) {
                                    if (functionOnActionsTarget != null && functionId.equals(functionOnActionsTarget)) {
                            %>
                            <option value="<%=functionId%>"
                                    selected="selected"><%=functionOnActionsTarget%>
                            </option>
                            <%
                            } else {
                            %>
                            <option value="<%=functionId%>"><%=functionId%>
                            </option>
                            <%
                                    }
                                }
                            %>
                            </select>
                            </td>
                        <td style="padding-left:0px !important;padding-right:0px !important">
                        <%
                            if (actionNamesTarget != null && !actionNamesTarget.equals("")) {

                        %>
                        <input type="text" name="actionNamesTarget" id="actionNamesTarget"
                               value="<%=actionNamesTarget%>" class="text-box-big"/>
                        <%
                        } else {
                        %>
                        <input type="text" name="actionNamesTarget" id="actionNamesTarget" class="text-box-big"  <%--onFocus="handleFocus(this,'Action')" onBlur="handleBlur(this,'Action');" class="defaultText text-box-big"--%> />

                        <%
                            }
                        %>
		    </td>
		    <td>
                        <a title="Select Action Names" class='icon-link' onclick='selectAttributesForTarget("Action");'
                           style='background-image:url(images/actions.png); float:right;'></a>
                    </td>

                    <td>
                        <input type="hidden" name="actionIdTarget" id="actionIdTarget" value="<%=actionIdTarget%>" />
                    </td>

                    <td>
                        <input type="hidden" name="actionDataTypeTarget" id="actionDataTypeTarget" value="<%=actionDataTypeTarget%>" />
                    </td>
                    </tr>
                    </table>
                    </td>
                </tr>


                <tr>
                    <td class="leftCol-small"><fmt:message key='environment.names'/></td>
                    <td>
                    <table class="normal" style="padding-left:0px !important">
                        <tr>
                        <td style="padding-left:0px !important;padding-right:0px !important">
                        <select id="functionOnEnvironmentTarget" name="functionOnEnvironmentTarget" class="leftCol-small">
                            <%
                                for (String functionId : targetFunctionIds) {
                                    if (functionOnEnvironmentTarget != null && functionId.equals(functionOnEnvironmentTarget)) {
                            %>
                            <option value="<%=functionId%>"
                                    selected="selected"><%=functionOnEnvironmentTarget%>
                            </option>
                            <%
                            } else {
                            %>
                            <option value="<%=functionId%>"><%=functionId%>
                            </option>
                            <%
                                    }
                                }
                            %>
                        </select>
                        </td>
                        <td style="padding-left:0px !important;padding-right:0px !important">
                        <%
                            if (environmentNamesTarget != null && !environmentNamesTarget.equals("")) {

                        %>
                        <input type="text" name="environmentNamesTarget" id="environmentNamesTarget"
                               value="<%=environmentNamesTarget%>" class="text-box-big"/>
                        <%
                        } else {
                        %>
                        <input type="text" name="environmentNamesTarget" id="environmentNamesTarget" class="text-box-big"  <%--onFocus="handleFocus(this,'Action')" onBlur="handleBlur(this,'Action');" class="defaultText text-box-big"--%> />

                        <%
                            }
                        %>
                    </td>
                    <td>
                        <a title="Select Environment Names" class='icon-link' onclick='selectAttributesForTarget("Environment");'
                           style='background-image:url(images/calendar.jpg); float:right;'></a>
                    </td>

                    <td>
                        <input type="hidden" name="environmentIdTarget" id="environmentIdTarget" value="<%=environmentIdTarget%>" />
                    </td>

                    <td>
                        <input type="hidden" name="environmentDataTypeTarget" id="environmentDataTypeTarget" value="<%=environmentDataTypeTarget%>" />
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
                    <h2 class="trigger  <%if(basicRuleElementDTO == null){%>active<%} %>"><a href="#"><fmt:message key="add.new.entitlement.rule"/></a></h2>

                <div class="toggle_container" id="newRuleLinkRow">


                    <table class="noBorders" id="ruleTable" style="width: 100%">
                    <body>
                    <tr>
                    <td class="formRow" style="padding:0 !important">
                        <table class="normal" cellspacing="0">

                        <tr>
                            <td class="leftCol-small"><fmt:message key='rule.name'/><span class="required">*</span>
                            </td>
                            <td>
                            <%
                                if (ruleId != null && !ruleId.trim().equals("") && !ruleId.trim().equals("null")) {
                            %>
                            <input type="text" name="ruleId" id="ruleId" class="text-box-big"
                                       value="<%=basicRuleElementDTO.getRuleId()%>"/>
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
                            <td><fmt:message key='resource.names'/></td>
                            <td>
                                <table class="normal" style="padding-left:0px !important">
                                <tr>
                                    <td style="padding-left:0px !important;padding-right:0px !important">
                                <select id="functionOnResources" name="functionOnResources" class="leftCol-small">
                                    <%
                                        for (String functionId : functionIds) {
                                            if (functionId.equals(functionOnResources)) {
                                    %>
                                    <option value="<%=functionId%>" selected="selected"><%=functionOnResources%>
                                    </option>
                                    <%
                                    } else {
                                    %>
                                    <option value="<%=functionId%>"><%=functionId%>
                                    </option>
                                    <%
                                            }
                                        }
                                    %>
                                </select>
                           </td>
                           <td style="padding-left:0px !important;padding-right:0px !important">
                            <%
                                if (resourceNames != null && !resourceNames.equals("")) {

                            %>
                            <input type="text" size="60" name="resourceNames" id="resourceNames"
                                       value="<%=resourceNames%>" class="text-box-big"/>
                            <%
                            } else {
                            %>
                            <input type="text" size="60" name="resourceNames" id="resourceNames"
                                       class="text-box-big"/>

                            <%
                                }
                            %>
	                    </td>
	                    <td>
                            <a title="Select Resources Names" class='icon-link' onclick='selectAttributes("Resource");'
                               style='background-image:url(images/registry.gif); float:right;'></a>
                            </td>
                                <td>
                                    <input type="hidden" name="resourceId" id="resourceId" value="<%=resourceId%>" />
                                </td>

                                <td>
                                    <input type="hidden" name="resourceDataType" id="resourceDataType" value="<%=resourceDataType%>" />
                                </td>


                                </tr>
                                </table>
                            </td>
                        </tr>


                        <tr>
                            <td class="leftCol-small"><fmt:message key='roles.users'/></td>
                            <td>
                                <table class="normal" style="padding-left:0px !important">
                                <tr>
                                    <td style="padding-left:0px !important;padding-right:0px !important">
                                <select id="functionOnSubjects" name="functionOnSubjects" class="leftCol-small">
                                    <%
                                        for (String functionId : functionIds) {
                                            if (functionId.equals(functionOnSubjects)) {
                                    %>
                                    <option value="<%=functionId%>" selected="selected"><%=functionOnSubjects%>
                                    </option>
                                    <%
                                    } else {
                                    %>
                                    <option value="<%=functionId%>"><%=functionId%>
                                    </option>
                                    <%
                                            }
                                        }
                                    %>
                                </select>
                                </td>
                                <td style="padding-left:0px !important;padding-right:0px !important">
                            <%
                                if (subjectNames != null && !subjectNames.equals("")) {

                            %>
                            <input type="text" name="subjectNames" id="subjectNames"
                                       value="<%=subjectNames%>" class="text-box-big"/>
                            <%
                            } else {
                            %>
                            <input type="text" name="subjectNames" id="subjectNames" class="text-box-big"/>

                            <%
                                }
                            %>
	                    </td>
	                    <td>                            	
                                <a title="Select Subject Names" class='icon-link' onclick='selectAttributes("Subject");'
                                   style='background-image:url(images/user-store.gif); float:right;'></a>
                                </td>

                                <td>
                                    <input type="hidden" name="subjectId" id="subjectId" value="<%=subjectId%>" />
                                </td>

                                <td>
                                    <input type="hidden" name="subjectDataType" id="subjectDataType" value="<%=subjectDataType%>" />
                                </td>

                                </tr>
                                </table>
                            </td>
                        </tr>


                        <tr>
                            <td class="leftCol-small"><fmt:message key='user.attribute'/></td>
                            <td >
                             <table class="normal" style="padding-left:0px !important">
                                 <tr>
                                 <td style="padding-left:0px !important;padding-right:0px !important">
                                <select id="attributeId" name="attributeId" class="leftCol-small">
                                    <option value="<%=EntitlementPolicyConstants.COMBO_BOX_DEFAULT_VALUE%>" selected="selected">
                                        <%=EntitlementPolicyConstants.COMBO_BOX_DEFAULT_VALUE%></option>
                                    <%
                                        if (claimDialectDTO != null && claimDialectDTO.getClaimMappings() != null) {

                                            for (ClaimMappingDTO claimMappingDTO : claimDialectDTO.getClaimMappings()) {
                                                String claimUri = claimMappingDTO.getClaim().getClaimUri();

                                                if (claimUri.equals(attributeId)) {
                                    %>
                                    <option value="<%=claimUri%>" selected="selected"><%=attributeId%>
                                    </option>
                                    <%
                                    } else {
                                    %>
                                    <option value="<%=claimUri%>"><%=claimUri%>
                                    </option>
                                    <%
                                                }
                                            }
                                        }
                                    %>
                                </select>

                               </td>
                               <td style="padding-left:0px !important;padding-right:0px !important">
                                <select id="functionOnAttributes" name="functionOnAttributes">
                                <%
                                    for(String functionId : functionIds) {
                                        if(functionId.equals(EntitlementPolicyConstants.EQUAL_TO) || functionId.equals(EntitlementPolicyConstants.REGEXP_MATCH)) {
                                            if(functionOnAttributes != null && functionId.equals(functionOnAttributes)) {
                                %>
                                    <option value="<%=functionId%>" selected="selected"><%=functionOnAttributes%></option>
                                <%
                                            } else {
                                %>
                                    <option value="<%=functionId%>" ><%=functionId%></option>
                                <%
                                            }
                                        }
                                    }
                                %>
                                </select>

                            </td>
                            <td style="padding-left:0px !important;padding-right:0px !important">
                            <%
                                if (userAttributeValue != null && !userAttributeValue.equals("")) {

                            %>
                            <input type="text" name="userAttributeValue" id="userAttributeValue"
                                       value="<%=userAttributeValue%>"  style="width: 285px" />
                            <%
                            } else {
                            %>
                            <input type="text" name="userAttributeValue" id="userAttributeValue"
                                       style="width: 285px" />

                            <%
                                }
                            %>
                            </td>

                        </tr>
                        </table>
                        </td>
                        </tr>


                        <tr>
                            <td class="leftCol-small"><fmt:message key='action.names'/></td>
                            <td>
                            <table class="normal" style="padding-left:0px !important">
                            <tr>
                            <td style="padding-left:0px !important;padding-right:0px !important">
                                <select id="functionOnActions" name="functionOnActions" class="leftCol-small">
                                    <%
                                        for (String functionId : functionIds) {
                                            if (functionId.equals(functionOnActions)) {
                                    %>
                                    <option value="<%=functionId%>" selected="selected"><%=functionOnActions%>
                                    </option>
                                    <%
                                    } else {
                                    %>
                                    <option value="<%=functionId%>"><%=functionId%>
                                    </option>
                                    <%
                                            }
                                        }
                                    %>
                                </select>
                            </td>
                            <td style="padding-left:0px !important;padding-right:0px !important">
                            <%
                                if (actionNames != null && !actionNames.equals("")) {

                            %>
                            <input type="text" name="actionNames" id="actionNames" value="<%=actionNames%>"
                                       class="text-box-big"/>
                            <%
                            } else {
                            %>
                            <input type="text" name="actionNames" id="actionNames" class="text-box-big"/>

                            <%
                                }
                            %>
                    	</td>
                    	<td>
                            <a title="Select Action Names" class='icon-link' onclick='selectAttributes("Action");'
                               style='background-image:url(images/actions.png); float:right;'></a>
                            </td>
                            
                            <td>
                                <input type="hidden" name="actionId" id="actionId" value="<%=actionId%>" />
                            </td>

                            <td>
                                <input type="hidden" name="actionDataType" id="actionDataType" value="<%=actionDataType%>" />
                            </td>

                            </tr>
                            </table>
                            </td>
                        </tr>


                        <tr>
                            <td class="leftCol-small"><fmt:message key='environment.names'/></td>
                            <td>
                                <table class="normal" style="padding-left:0px !important">
                                <tr>
                                <td style="padding-left:0px !important;padding-right:0px !important">
                                <select id="functionOnEnvironment" name="functionOnEnvironment" class="leftCol-small">
                                    <%
                                        for (String functionId : functionIds) {
                                            if (functionId.equals(functionOnEnvironment)) {
                                    %>
                                    <option value="<%=functionId%>" selected="selected"><%=functionOnEnvironment%>
                                    </option>
                                    <%
                                    } else {
                                    %>
                                    <option value="<%=functionId%>"><%=functionId%>
                                    </option>
                                    <%
                                            }
                                        }
                                    %>
                                </select>
                            </td>
                            <td style="padding-left:0px !important;padding-right:0px !important">
                            <%
                                if (environmentNames != null && !environmentNames.equals("")) {

                            %>
                            <input type="text" name="environmentNames" id="environmentNames" value="<%=environmentNames%>"
                                       class="text-box-big"/>
                            <%
                            } else {
                            %>
                            <input type="text" name="environmentNames" id="environmentNames" class="text-box-big"/>

                            <%
                                }
                            %>
			</td>
                    	<td>
                            <a title="Select Environment Names" class='icon-link' onclick='selectAttributes("Environment");'
                               style='background-image:url(images/calendar.jpg); float:right;'></a>
                            </td>
                            <td>
                                <input type="hidden" name="environmentId" id="environmentId" value="<%=environmentId%>" />
                            </td>

                            <td>
                                <input type="hidden" name="environmentDataType" id="environmentDataType" value="<%=environmentDataType%>" />
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
                                if (basicRuleElementDTO != null && basicRuleElementDTO.isCompletedRule()) {
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
                                if (basicRuleElementDTOs != null && basicRuleElementDTOs.size() > 0) {
                                    List<BasicRuleElementDTO> orderedBasicRuleElementDTOs = new ArrayList<BasicRuleElementDTO>();
                                    String ruleElementOrder = entitlementPolicyBean.getRuleElementOrder();
                                    if(ruleElementOrder != null){
                                        String[] orderedRuleIds = ruleElementOrder.split(EntitlementPolicyConstants.ATTRIBUTE_SEPARATOR);
                                        for(String orderedRuleId : orderedRuleIds){
                                            for(BasicRuleElementDTO orderedBasicRuleElementDTO : basicRuleElementDTOs) {
                                                if(orderedRuleId.trim().equals(orderedBasicRuleElementDTO.getRuleId())){
                                                    orderedBasicRuleElementDTOs.add(orderedBasicRuleElementDTO);
                                                }
                                            }
                                        }
                                    }
                                  
                                    if(orderedBasicRuleElementDTOs.size() < 1){
                                        orderedBasicRuleElementDTOs = basicRuleElementDTOs;
                                    }
                                    for (BasicRuleElementDTO ruleElementDTO : orderedBasicRuleElementDTOs) {
                                        if(ruleElementDTO.isCompletedRule()){
                            %>
                            <tr>

                                <td>
                                    <a class="icon-link" onclick="updownthis(this,'up')" style="background-image:url(../admin/images/up-arrow.gif)"></a>
                                    <a class="icon-link" onclick="updownthis(this,'down')" style="background-image:url(../admin/images/down-arrow.gif)"></a>
                                    <input type="hidden" value="<%=ruleElementDTO.getRuleId()%>"/>
                                    <%=ruleElementDTO.getRuleId()%>
                                </td>
                                <td><%=ruleElementDTO.getRuleEffect()%></td>
                                <td>
                                    <a href="#" onclick="editRule('<%=ruleElementDTO.getRuleId()%>')"
                                       class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message
                                            key="edit"/></a>
                                    <a href="#" onclick="deleteRule('<%=ruleElementDTO.getRuleId()%>')"
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
                    <input type="button" onclick="submitForm();" value="<fmt:message key="finish"/>"  class="button"/>
                    <input type="button" onclick="doCancel();" value="<fmt:message key="cancel" />" class="button"/>
                </td>
            </tr>
        </table>
        </form>
    </div>
</div>
</fmt:bundle>
