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
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.RuleElementDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.SubElementDTO" %>

<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />
<%
    String ruleElementOrder = request.getParameter("ruleElementOrder");
    if(ruleElementOrder != null && !ruleElementOrder.equals("")){
        entitlementPolicyBean.setRuleElementOrder(ruleElementOrder.trim());
    }     
    String ruleId = CharacterEncoder.getSafeText(request.getParameter("ruleId"));
    if(ruleId != null && !ruleId.trim().equals("")) {
        entitlementPolicyBean.setCurrentRuleId(ruleId);
    }

    RuleElementDTO ruleElementDTO = null;
    if( entitlementPolicyBean.getCurrentRuleId() != null) {
        ruleElementDTO = entitlementPolicyBean.getRuleElement(entitlementPolicyBean.getCurrentRuleId());
        if(ruleElementDTO != null){
            entitlementPolicyBean.setRuleEffect(ruleElementDTO.getRuleEffect());
            entitlementPolicyBean.setRuleDescription(ruleElementDTO.getRuleDescription());
        }
    }
%>

<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
	<carbon:breadcrumb label="add.rule.element"
		resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />

	<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
	<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
	<script type="text/javascript" src="../carbon/admin/js/main.js"></script>

	<div id="middle">
	<h2><fmt:message key='entitlement.policy.creation'/></h2>
	<div id="workArea">

    <%
        if(ruleElementDTO != null){
    %>
        <h3 class="page-subtitle"><fmt:message key='edit.rule.element'/></h3>    
    <%
        } else {
    %>
        <h3 class="page-subtitle"><fmt:message key='add.new.rule.element'/></h3>
    <%
	    }
    %>
        
    <script type="text/javascript">

        function doValidation() {
            var value = document.getElementsByName("ruleId")[0].value;
            if (value == '') {
                CARBON.showWarningDialog('<fmt:message key="rule.id.is.required"/>');
                return false;
            }
            return true;
        }

        function doCancel() {
            var ruleId = "<%=entitlementPolicyBean.getCurrentRuleId()%>"
            location.href = 'add-rules.jsp?ruleId=' + ruleId;
        }

        function doAddTarget() {
            if(doValidation()) {
                document.ruleForm.action = "add-target.jsp";
                document.ruleForm.submit();
            }
        }

        function doAddCondition() {
            if(doValidation()){
                document.ruleForm.action = "add-condition.jsp";
                document.ruleForm.submit();
            }
        }

        function doAdd() {
            if (doValidation()) {
                document.ruleForm.action = "add-rules.jsp";
                document.ruleForm.submit();
            }
        }

    </script>

    <form name="ruleForm" action="index.jsp" method="post">
    	<table style="width: 100%" class="styledLeft">
		<thead>
			<tr>
                <th colspan="2"></th>
			</tr>
		</thead>
		<tbody>
        <tr>
            <td class="formRow">
            <table class="normal" cellspacing="0">
            <tr>
                <td class="leftCol-small"><fmt:message key='rule.id'/><font class="required">*</font></td>
            <%
                if( entitlementPolicyBean.getCurrentRuleId() != null) {
            %>
                    <td><input type="text" name="ruleId" id="ruleId" class="text-box-big" value="<%=entitlementPolicyBean.getCurrentRuleId()%>"/></td>
            <%
                } else {
            %>
                    <td><input type="text" name="ruleId" id="ruleId" class="text-box-big"/></td>
            <%
                }
            %>
            </tr>

            <tr>
                <td><fmt:message key="rule.effect"/></td>
                <td>
                    <select id="ruleEffect" name="ruleEffect">
            <%
                String[] ruleEffects = entitlementPolicyBean.getRuleEffectValues();
                if (ruleEffects != null) {
                    for (String ruleEffect : ruleEffects) {
                        if (entitlementPolicyBean.getRuleEffect() != null && ruleEffect.equals(entitlementPolicyBean.getRuleEffect())) {
            %>
                            <option value="<%=ruleEffect%>" selected="selected"><%=entitlementPolicyBean.getRuleEffect()%></option>
            <%
                        } else {
            %>
                            <option value="<%=ruleEffect%>"><%=ruleEffect%></option>
            <%
                        }
                    }
                }
            %>
                    </select>
                </td>
            </tr>

            <tr>
                <td class="leftCol-small"><fmt:message key='rule.description'/></td>
            <%
                if(entitlementPolicyBean.getRuleDescription() != null) {
            %>
                    <td><input type="text" name="ruleDescription" id="ruleDescription" value="<%=entitlementPolicyBean.getRuleDescription()%>" class="text-box-big"/></td>
            <%
                } else {
            %>
                    <td><input type="text" name="ruleDescription" id="ruleDescription" class="text-box-big"/></td>

            <%
                }
            %>
            </tr>


            <tr>
                <td>

                <a class="icon-link"  onclick="doAddTarget();" style="background-image:url(images/add.gif);">
            <%
                if(ruleElementDTO != null && ruleElementDTO.getTargetElementDTO() != null && ruleElementDTO.getTargetElementDTO().getSubElementDTOs().size() > 0 ) {
                    if(ruleId != null && !ruleId.trim().equals("")) {
                        for(SubElementDTO subElementDTO : ruleElementDTO.getTargetElementDTO().getSubElementDTOs()){
                            entitlementPolicyBean.setTargetElementDTO(subElementDTO);
                        }
                    }
            %>
                    <fmt:message key='edit.target.element'/></a>
            <%
                } else {
            %>
                    <fmt:message key='add.target.element'/></a>
            <%
                }
            %>
                </td>
            </tr>
                        
            <tr>
                <td>
                    <a class="icon-link"  onclick="doAddCondition();" style="background-image:url(images/add.gif);">
            <%
                if(ruleElementDTO != null  && ruleElementDTO.getConditionElementDT0() != null) {
                    if(ruleId != null && !ruleId.trim().equals("")) {
                        entitlementPolicyBean.setConditionElement(ruleElementDTO.getConditionElementDT0());
                    }
            %>
                    <fmt:message key='edit.condition.element'/></a>
            <%
                } else {
            %>
                    <fmt:message key='add.condition.element'/></a>
            <%
                }
            %>
                </td>
            </tr>

            </table>
            </td>
        </tr>
		</tbody>	
	</table>
    <table class="styledLeft" style="margin-top:20px;">
        <tbody>
        <tr>
            <td colspan="2" class="buttonRow">
                <%
                    if(ruleElementDTO != null){
                %>
                    <input class="button" type="button" value="<fmt:message key='update'/>" onclick="doAdd();"/>
                <%
                    } else {
                %>
                    <input class="button" type="button" value="<fmt:message key='add'/>" onclick="doAdd();"/>                
                <%
                    }
                %>
                <input class="button" type="button" value="<fmt:message key='cancel'/>"  onclick="doCancel();"/>
            </td>
        </tr>
        </tbody>
    </table>    
	</form>
	</div>	
	</div>
</fmt:bundle>