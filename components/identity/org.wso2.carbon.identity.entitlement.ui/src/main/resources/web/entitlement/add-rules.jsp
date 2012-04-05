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
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.RuleElementDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.ConditionElementDT0" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.SubElementDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.TargetElementDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />

<%
    entitlementPolicyBean.setCurrentRuleId(null);
    entitlementPolicyBean.setRuleEffect(null);
    entitlementPolicyBean.setRuleDescription(null);
    String ruleId = CharacterEncoder.getSafeText(request.getParameter("ruleId"));
    String ruleEffect = CharacterEncoder.getSafeText(request.getParameter("ruleEffect"));
    String ruleDescription = CharacterEncoder.getSafeText(request.getParameter("ruleDescription"));


    if(ruleId != null && !ruleId.trim().equals("") && !ruleId.equals("null") ) {
        List<SubElementDTO> subElementDTOs = entitlementPolicyBean.getTargetElementDTO(ruleId);

        if(ruleEffect != null &&!ruleEffect.trim().equals("")){

            RuleElementDTO ruleElement = new RuleElementDTO();
            ConditionElementDT0 conditionElement = entitlementPolicyBean.getConditionElement();

            if(entitlementPolicyBean.isExistingRuleElement(ruleId)) {
                entitlementPolicyBean.removeRuleElement(ruleId);
            } else {
                entitlementPolicyBean.setRuleElementOrder(entitlementPolicyBean.getRuleElementOrder() +
                                                          "," + ruleId);                
            }

            ruleElement.setRuleId(ruleId);
            ruleElement.setRuleEffect(ruleEffect);

            if(ruleDescription != null && !ruleDescription.trim().equals("")){
                ruleElement.setRuleDescription(ruleDescription);
            }
            if(conditionElement != null) {
                ruleElement.setConditionElementDT0(conditionElement);
                entitlementPolicyBean.setConditionElement(null);

            }
            if(subElementDTOs != null){
                TargetElementDTO targetElementDTO = new TargetElementDTO();
                targetElementDTO.setSubElementDTOs(subElementDTOs);
                ruleElement.setTargetElementDTO(targetElementDTO);
                for(SubElementDTO subElementDTO : subElementDTOs){
                    entitlementPolicyBean.removeTargetElementDTO(subElementDTO.getElementId());
                }
            }
            entitlementPolicyBean.setRuleElement(ruleElement);
        } else {            
            for(SubElementDTO subElementDTO : subElementDTOs){
                entitlementPolicyBean.removeTargetElementDTO(subElementDTO.getElementId());
            }
        }
    }

%>

<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
    <carbon:breadcrumb
            label="add.rule.elements"
            resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>

   <script type="text/javascript">

    function deleteRuleElement(ruleId) {
        location.href = 'delete-rule.jsp?ruleId=' + ruleId + '&ruleElementOrder=' + orderRuleElement();
    }

    function editRuleElement(ruleId){
        location.href = 'add-rule.jsp?ruleId=' + ruleId + '&ruleElementOrder=' + orderRuleElement();
    }

    function addRuleElement(){
        location.href = 'add-rule.jsp?ruleElementOrder=' + orderRuleElement();
    }

    function doCancel() {
        location.href = 'index.jsp?';
    }

    function doBack() {
        location.href = 'add-target.jsp';
    }

    function doFinish() {
        location.href = 'finish.jsp?ruleElementOrder=' + orderRuleElement();
    }

    function orderRuleElement(){
        var ruleElementOrder = new Array();
        var tmp = jQuery("#dataTable tbody tr input");
        for (var i = 0 ; i < tmp.length; i++){
            ruleElementOrder.push(tmp[i].value);
        }
        return ruleElementOrder;
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
        <h2><fmt:message key='entitlement.policy.creation'/></h2>
        <div id="workArea">
        <%
            if(entitlementPolicyBean.isEditPolicy()){
        %>
            <h3 class="page-subtitle"><fmt:message key='edit.rule.elements'/></h3>
        <%
            } else {
        %>
            <h3 class="page-subtitle"><fmt:message key='add.rule.element'/></h3>            
        <%
            }
        %>
            <table class="styledLeft" border="1">
                <thead>
                    <tr>
                        <th colspan="2"></th>
                    </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        <div style="height:30px;">
                            <a  href="#" onclick="addRuleElement()"  class="icon-link"
                               style="background-image:url(images/add.gif);"><fmt:message key='add.new.rule.element'/></a>
                        </div>

                        <table class="styledLeft" id="dataTable" border="1">
                        <thead>
                            <th><fmt:message key="rule.id"/></th>
                            <th><fmt:message key="action"/></th>
                        </thead>
                        <tbody>
                        <%
                            List<RuleElementDTO> existingRuleElements = entitlementPolicyBean.getRuleElements();
                            if(existingRuleElements != null && existingRuleElements.size() > 0) {

                                List<RuleElementDTO> orderedRuleElementDTOs = new ArrayList<RuleElementDTO >();
                                List<RuleElementDTO> nonOrderedRuleElementDTOs = new ArrayList<RuleElementDTO >();
                                String ruleElementOrder = entitlementPolicyBean.getRuleElementOrder();
                                if(ruleElementOrder != null){
                                    String[] orderedRuleIds = ruleElementOrder.split(EntitlementPolicyConstants.ATTRIBUTE_SEPARATOR);
                                    for(String orderedRuleId : orderedRuleIds){
                                        for(RuleElementDTO orderedBasicRuleElementDTO : existingRuleElements) {
                                            if(orderedRuleId.trim().equals(orderedBasicRuleElementDTO.getRuleId())){
                                                orderedRuleElementDTOs.add(orderedBasicRuleElementDTO);
                                            }
                                        }
                                    }
                                }

                                if(orderedRuleElementDTOs.size() < 1){
                                    orderedRuleElementDTOs = existingRuleElements;
                                }
                                for (RuleElementDTO existingRuleElement : orderedRuleElementDTOs) {
                        %>

                        <tr>
                            <td>
                                <a class="icon-link" onclick="updownthis(this,'up')" style="background-image:url(../admin/images/up-arrow.gif)"></a>
                                <a class="icon-link" onclick="updownthis(this,'down')" style="background-image:url(../admin/images/down-arrow.gif)"></a>
                                <input type="hidden" value="<%=existingRuleElement.getRuleId()%>"/>
                                Rule [id - <%=existingRuleElement.getRuleId()%> ]
                            </td>
                            <td>
                                <a href="#" onclick="editRuleElement('<%=existingRuleElement.getRuleId()%>')" class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message key="edit"/></a>
                                <a href="#" onclick="deleteRuleElement('<%=existingRuleElement.getRuleId()%>')" class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message key="delete"/></a
                            </td>
                        </tr>

                        <%
                                }
                            } else {
                        %>
                            <tr class="noDataBox">
                                <td colspan="3"><fmt:message key="no.rule.element.define"/><br/></td>
                            </tr>
                        <%
                            }
                        %>
                        </tbody>
                        </table>
                    </td>
                </tr>
                <tbody>
            </table>
            <table class="styledLeft" style="margin-top:20px;">
            <tbody>
            <tr>
                <td colspan="2" class="buttonRow">
                          <input class="button" type="button" value="<fmt:message key='back'/>" onclick="doBack();"/>
                          <input class="button" type="button" value="<fmt:message key='finish'/>" onclick="doFinish();"/>
                          <input class="button" type="button" value="<fmt:message key='cancel'/>"  onclick="doCancel();"/>
                 </td>
            </tr>
            </tbody>
            </table>
        </div>
    </div>
</fmt:bundle>
