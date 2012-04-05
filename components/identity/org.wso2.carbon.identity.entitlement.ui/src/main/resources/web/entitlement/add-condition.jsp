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

<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.ApplyElementDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.ConditionElementDT0" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>

<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />
<%
    String ruleId = CharacterEncoder.getSafeText(request.getParameter("ruleId"));
    String ruleEffect = CharacterEncoder.getSafeText(request.getParameter("ruleEffect"));
    String ruleDescription = CharacterEncoder.getSafeText(request.getParameter("ruleDescription"));
    String applyElementId = CharacterEncoder.getSafeText(request.getParameter("applyElementId"));
    String forwardTo = "";

    if(ruleId != null && !ruleId.trim().equals("")) {
        entitlementPolicyBean.setCurrentRuleId(ruleId);
        if (ruleEffect != null && !ruleEffect.trim().equals("")) {
            entitlementPolicyBean.setRuleEffect(ruleEffect);
        }
        if (ruleDescription != null && !ruleDescription.trim().equals("")) {
            entitlementPolicyBean.setRuleDescription(ruleDescription);
        }
        ConditionElementDT0 currentConditionElementDT0  = entitlementPolicyBean.getConditionElement();
        if(currentConditionElementDT0 != null && currentConditionElementDT0.getApplyElement() != null) {
            entitlementPolicyBean.setApplyElementDTO(currentConditionElementDT0.getApplyElement());
        }

        forwardTo = "add-apply.jsp?applyElementId=/1&editApplyElement=true";
    } else{
        forwardTo = "add-rule.jsp";
    }

    ConditionElementDT0 conditionElementDT0 = new ConditionElementDT0();
    if(applyElementId != null && !applyElementId.equals("null") & !applyElementId.equals("") ) {

        ApplyElementDTO applyElementDTO = entitlementPolicyBean.getApplyElementDTO(applyElementId);
        conditionElementDT0.setApplyElement(applyElementDTO);
        entitlementPolicyBean.setConditionElement(conditionElementDT0);
    }

    entitlementPolicyBean.removeAttributeValueElements();
    entitlementPolicyBean.removeApplyElementDTOs();
    entitlementPolicyBean.removeAttributeDesignatorElements();
    entitlementPolicyBean.removeAttributeSelectorElements();
%>


<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
	<carbon:breadcrumb label="add.condition.element"
		resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />

	<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
	<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
	<script type="text/javascript" src="../carbon/admin/js/main.js"></script>

	<div id="middle">
	<h2><fmt:message key='entitlement.policy.creation'/></h2>
	<div id="workArea">
	
	   <script type="text/javascript">

        function doCancel() {
            location.href = 'add-rule.jsp';
        }
        
        function doAdd() {
            document.conditionElement.submit();
        }

        function doAddApply() {
            location.href = 'add-apply.jsp?applyElementId=/1&editApplyElement=true';
        }

        function doAddAttribute() {
            location.href = 'add-attribute-value.jsp?attributeValueElementId=-1';            
        }

  	   </script>

    <form name="conditionElement" action="add-rule.jsp" method="post">
    	<table style="width: 100%" class="styledLeft">
		<thead>
			<tr>
				<th colspan="2"><fmt:message key='add.expression'/></th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td class="formRow">
					<table class="normal" cellspacing="0">
                        <%
                            ConditionElementDT0 currentConditionElementDT0  = entitlementPolicyBean.getConditionElement();

                            if(currentConditionElementDT0 != null && currentConditionElementDT0.getApplyElement() != null) {
                                entitlementPolicyBean.setApplyElementDTO(currentConditionElementDT0.getApplyElement());

                        %>
                            <tr>
                                <td>
                                    <div style="height:30px;">
                                        <a class="icon-link"  onclick="doAddApply();" style="background-image:url(images/add.gif);">
                                        <fmt:message key='edit.apply.element'/></a>
                                    </div>
                                </td>
                            </tr>

                        <%
                            } else {

                        %>

                            <tr>
                                <td>
                                    <div style="height:30px;">
                                        <a class="icon-link"  onclick="doAddApply();" style="background-image:url(images/add.gif);">
                                        <fmt:message key='add.apply.element'/></a>
                                    </div>
                                </td>
                            </tr>
                        <%
                            }
                        %>
					</table>
				</td>
			</tr>
			<tr>
			    <td colspan="2" class="buttonRow">
	                      <input class="button" type="button" value="<fmt:message key='add'/>" onclick="doAdd();"/>
	                      <input class="button" type="button" value="<fmt:message key='cancel'/>"  onclick="doCancel();"/>
                </td>
			</tr>
		</tbody>	
	</table>
	</form>
	</div>	
	</div>
</fmt:bundle>