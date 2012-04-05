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
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.util.ResourceBundle" %>
<%@page import="java.lang.Exception"%>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.PolicySetDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="org.wso2.carbon.claim.mgt.ui.client.ClaimAdminClient" %>
<%@ page import="org.wso2.carbon.claim.mgt.stub.dto.ClaimDialectDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.BasicTargetElementDTO" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.claim.mgt.stub.dto.ClaimMappingDTO" %>

<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />
<%
    String forwardTo = null;
    String[] algorithmNames = null;
    String[] policyIds = null;

    PolicySetDTO policySetDTO = entitlementPolicyBean.getPolicySetDTO();
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.
                    CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String backEndServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);      
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    ClaimDialectDTO claimDialectDTO = null;
    BasicTargetElementDTO basicTargetElementDTO = entitlementPolicyBean.getBasicTargetElementDTO();

    String selectedSubjectNames = "";
    String selectedResourceNames = "";
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
    int noOfSelectedResources = 1;

    /**
     *  Get posted resources from jsp pages and put then in to a String object
     */
    while(true) {
        String resourceName  = request.getParameter("resourceName" + noOfSelectedResources);
        if (resourceName == null || resourceName.trim().equals("")) {
            break;
        }
        if(selectedResourceNames.equals("")) {
            selectedResourceNames = resourceName.trim();
        } else {
            selectedResourceNames = selectedResourceNames + "," + resourceName.trim();
        }
        noOfSelectedResources ++;
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

    String[] targetFunctionIds = new String[] {EntitlementPolicyConstants.EQUAL_TO,
            EntitlementPolicyConstants.AT_LEAST_ONE_MATCH,
            EntitlementPolicyConstants.AT_LEAST_ONE_MATCH_REGEXP, EntitlementPolicyConstants.REGEXP_MATCH,
            EntitlementPolicyConstants.SET_OF, EntitlementPolicyConstants.MATCH_REGEXP_SET_OF };

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

        resourceDataTypeTarget = basicTargetElementDTO.getResourceDataType();
        actionDataTypeTarget = basicTargetElementDTO.getActionDataType();
        subjectDataTypeTarget = basicTargetElementDTO.getSubjectDataType();
        environmentDataTypeTarget = basicTargetElementDTO.getEnvironmentDataType();
        userAttributeValueDataTypeTarget = basicTargetElementDTO.getUserAttributeValueDataType();

        resourceIdTarget = basicTargetElementDTO.getResourceId();
        actionIdTarget = basicTargetElementDTO.getActionId();
        subjectIdTarget = basicTargetElementDTO.getSubjectId();
        environmentIdTarget = basicTargetElementDTO.getEnvironmentId();
        attributeIdTarget = basicTargetElementDTO.getAttributeId();

        if(!entitlementPolicyBean.subjectTypeMap.containsKey("Target")){
            entitlementPolicyBean.subjectTypeMap.put("Target", basicTargetElementDTO.getSubjectType());
        }
    }



    try {

        EntitlementPolicyAdminServiceClient client = new EntitlementPolicyAdminServiceClient(cookie,
                serverURL, configContext);
        algorithmNames = client.getEntitlementPolicyDataFromRegistry("policyCombiningAlgorithms");
        entitlementPolicyBean.setPolicyCombiningAlgorithms(algorithmNames);
        policyIds = client.getAllPolicyIds();
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
	<carbon:breadcrumb label="create.policy.set"
		resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />
<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../resources/js/registry-browser.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
<script src="../entitlement/js/create-basic-policy.js" type="text/javascript"></script>
<link href="../entitlement/css/entitlement.css" rel="stylesheet" type="text/css" media="all"/>

    <script type="text/javascript">

    function removeRow(link){
        link.parentNode.parentNode.parentNode.removeChild(link.parentNode.parentNode);
    }

    function submitForm() {

    }

    function cancelForm() {
       location.href = 'index.jsp';
    }

    function addNewRow(){

        var comboBox = document.getElementById("policyIds");
        var policySetId = comboBox[comboBox.selectedIndex].value;
        var mainTable = document.getElementById('mainTable');
        var newTr = mainTable.insertRow(mainTable.rows.length);
        var cell1 = newTr.insertCell(0);
        var rowValue = mainTable.rows.length - 1;
        cell1.innerHTML = policySetId + '<input type="hidden" readonly="readonly"   value="' + policySetId +'" name="policyIds'+ rowValue +'" id="policyIds'+ rowValue +'"/>' ;

        var cell2 = newTr.insertCell(1);
        cell2.innerHTML = '<a onclick="removeRow(this)" style="background-image:url(images/delete.gif);" type="button" class="icon-link"></a>' ;
    }

    function doValidation() {

        var value = document.getElementsByName("policySetName")[0].value;
        if (value == '') {
            CARBON.showWarningDialog('<fmt:message key="policy.name.is.required"/>');
            return false;
        }
        return true;
    }

    function doCancel() {
        location.href = 'index.jsp?';
    }

    function doFinish() {
        document.createPolicySet.action = "update-policy-set.jsp?";
        if (doValidation()) {
            document.createPolicySet.submit();
        }
    }

    function doAddTarget() {

        if(doValidation()) {
            document.createPolicySet.action = "update-policy-set.jsp?";
            document.createPolicySet.submit();
        }
    }

</script>

	<div id="middle">
	<h2><fmt:message key='create.entitlement.policy.set'/></h2>
	<div id="workArea">
    <form name="createPolicySet" action="index.jsp" method="post">
    	<table class="styledLeft noBorders">
		<tbody>
            <tr>
                <td class="leftCol-med"><fmt:message key='policy.set.name'/><font class="required">*</font></td>
                <%
                    if(policySetDTO != null && policySetDTO.getPolicySetId() != null){
                %>
                    <td><input type="text" name="policySetName" id="policySetName" value="<%=policySetDTO.getPolicySetId()%>" class="text-box-big"/></td>
                <%
                    } else {
                %>
                    <td><input type="text" name="policySetName" id="policySetName" class="text-box-big"/></td>
                <%
                    }
                %>
            </tr>
            <tr>
                <td><fmt:message key="policy.combining.algorithm"/></td>
                <td>
                <select id="policyAlgorithmName" name="policyAlgorithmName" class="text-box-big">
                <%
                  if (algorithmNames != null && algorithmNames.length > 0) {
                      for (String algorithmName : algorithmNames) {
                          if(policySetDTO != null && algorithmName.equals(policySetDTO.getPolicyCombiningAlgId())){
                %>
                      <option value="<%=algorithmName%>" selected="selected"><%=policySetDTO.getPolicyCombiningAlgId()%></option>
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
                <td  class="leftCol-small" style="vertical-align:top !important"><fmt:message key='policy.set.description'/></td>
                <%
                    if(policySetDTO != null && policySetDTO.getDescription() != null){
                %>
                    <td><input type="text" name="policySetDescription" id="policySetDescription" value="<%=policySetDTO.getDescription()%>" class="text-box-big"/></td>
                <%
                    } else {
                %>
                    <td><input type="text" name="policySetDescription" id="policySetDescription" class="text-box-big"/></td>
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

                    <%if(policyIds == null){%>
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
            <h2 class="trigger  <%if(basicTargetElementDTO == null){%>active<%} %>"><a href="#"><fmt:message key="policy.set.apply.to"/></a></h2>

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
                        </tr></table>
                    </td>
                </tr>


                <tr>
                    <td class="leftCol-small"><fmt:message key='user.attribute'/></td>


                    <td>
                        <select id="attributeIdTarget" name="attributeIdTarget" class="leftCol-small">
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


                        <%
                            if (userAttributeValueTarget != null && !userAttributeValueTarget.equals("")) {

                        %>
                        <input type="text" name="userAttributeValueTarget" id="userAttributeValueTarget"
                               value="<%=userAttributeValueTarget%>" class="text-box-big"/>
                        <%
                        } else {
                        %>
                        <input type="text" name="userAttributeValueTarget" id="userAttributeValueTarget" class="text-box-big" <%--onFocus="handleFocus(this,'User attribute')" onBlur="handleBlur(this,'User attribute');" class="defaultText text-box-big"--%>/>

                        <%
                            }
                        %>
                    </td>
                </tr>


                <tr>
                    <td class="leftCol-small"><fmt:message key='action.names'/></td>
                    <td>
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
                </tr>
                </table>
            </div>

            </td>
            </tr>


            <tr>
                <td colspan="2" style="margin-top:10px;">
                    <h2 class="trigger  <%if(policyIds == null){%>active<%} %>"><a href="#"><fmt:message key="define.policy.policy.sets"/></a></h2>

                <div class="toggle_container" id="newRuleLinkRow">
                <table class="noBorders" cellspacing="0" style="width:100%;padding-top:5px;">
                    <tr>
                        <td class="leftCol-med" style="vertical-align:top"><fmt:message
                                 key="select.policies.policySets"/></td>
                        <td>
                         <select id="policyIds" name="policyIds" class="text-box-big">
                         <%
                             if (policyIds != null && policyIds.length > 0) {
                                 for (String policyId : policyIds) {
                         %>
                             <option value="<%=policyId%>"><%=policyId%></option>
                         <%
                                 }
                             }
                         %>
                         </select>

                         <a onclick="addNewRow();" style='background-image:url(images/add.gif);float:none' type="button"
                            class="icon-link"><fmt:message key="add.to.policy.set"/></a>
                        </td>                        
                    </tr>
                </table>
                </div>
                </td>
            </tr>

            <tr>
                <td colspan="2">
                    <table id="mainTable" class="styledLeft" style="width: 100%">
                    <thead>
                    <tr>
                        <th><fmt:message key="selected.policies"/></th>
                        <th><fmt:message key="action"/></th>
                    </tr>
                    </thead>
                            <tbody>

                            <%
                                if(policySetDTO != null && policySetDTO.getPolicyIds().size() > 0) {
                                    int i = 1;
                                    for(String policyId : policySetDTO.getPolicyIds()){
                            %>
                            <tr>
                                <td>
                                    <input type="hidden" value="<%=policyId%>" id="policyIds<%=i%>" name="policyIds<%=i%>"/>
                                    <%=policyId%>
                                </td>
                                <td><a onclick="removeRow(this)" style="background-image:url(images/delete.gif);" type="button" class="icon-link"></a></td>
                            </tr>

                            <%
                                        i++;
                                    }
                                }
                            %>
                        </tbody>
                    </table>
                </td>
            </tr>

            <tr>
                <td colspan="2" class="buttonRow">
                    <input class="button" type="button" value="<fmt:message key='finish'/>" onclick="doFinish();"/>
                    <input class="button" type="button" value="<fmt:message key='cancel'/>"  onclick="doCancel();"/>
                </td>
            </tr>
        </tbody>
    </table>    
	</form>
	</div>	
	</div>
</fmt:bundle>                      