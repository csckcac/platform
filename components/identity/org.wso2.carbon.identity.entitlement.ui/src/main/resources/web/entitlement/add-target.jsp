
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
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.MatchElementDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.SubElementDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>

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
    String editSubElement = CharacterEncoder.getSafeText(request.getParameter("editSubElement"));
    String subElementName = CharacterEncoder.getSafeText(request.getParameter("subElementName"));
    String subElementId = CharacterEncoder.getSafeText(request.getParameter("subElementId"));
    
    if (ruleId != null && !ruleId.trim().equals("")) {
        entitlementPolicyBean.setCurrentRuleId(ruleId);
    }
    if (ruleEffect != null && !ruleEffect.trim().equals("")) {
        entitlementPolicyBean.setRuleEffect(ruleEffect);
    }
    if (ruleDescription != null && !ruleDescription.trim().equals("")) {
        entitlementPolicyBean.setRuleDescription(ruleDescription);
    }

    List<MatchElementDTO> matchElementDTOs = entitlementPolicyBean.getMatchElements();

    if(matchElementDTOs != null && matchElementDTOs.size() > 0 && subElementId != null &&
            !subElementId.trim().equals("") && !subElementId.trim().equals("null") &&
            subElementName !=null && !subElementName.trim().equals("")) {

        SubElementDTO elementDTO = new SubElementDTO();

        int subElementIdInteger = Integer.parseInt(subElementId);
        elementDTO.setElementName(subElementName);
        for(MatchElementDTO matchElementDTO : matchElementDTOs) {
            elementDTO.setMatchElementDTOs(matchElementDTO);
        }
        
        if(entitlementPolicyBean.getCurrentRuleId() != null &&
                !entitlementPolicyBean.getCurrentRuleId().trim().equals("")) {
            elementDTO.setRuleId(entitlementPolicyBean.getCurrentRuleId());
        }

        if(!"true".equals(editSubElement)) {
            entitlementPolicyBean.setSubElementNumber(subElementIdInteger + 1);
            elementDTO.setElementId(subElementIdInteger);
        } else {
            entitlementPolicyBean.removeTargetElementDTO(subElementIdInteger);
            elementDTO.setElementId(subElementIdInteger);
        }
        
        entitlementPolicyBean.setTargetElementDTO(elementDTO);
        entitlementPolicyBean.removeMatchElements();
    }
    
    List<SubElementDTO> existingElements = entitlementPolicyBean.getTargetElementDTOs();

%>

<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
    <carbon:breadcrumb
            label="add.target.element"
            resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>

	   <script type="text/javascript">


        function deleteSubElement(subElementId) {
             location.href = 'delete-subelement.jsp?subElementId=' + subElementId ;
        }

        function doCancel() {
            location.href = 'index.jsp';
        }

        function doBackToRule() {
            location.href = 'add-rule.jsp';
        }

        function doNext() {
            location.href = 'add-rules.jsp';
        }

        function doFinish() {
            location.href = 'finish.jsp';
        }

        function doAdd() {
            location.href = 'add-rule.jsp';
        }

        function doBack(){
            location.href = 'create-policy.jsp';            
        }

  	   </script>


    <div id="middle">
        <h2><fmt:message key='entitlement.policy.creation'/></h2>
        <div id="workArea">
        <%
            if(existingElements != null && existingElements.size() > 0){
        %>
            <h3 class="page-subtitle"><fmt:message key='edit.target.element'/></h3>    
        <%
            } else {
        %>
            <h3 class="page-subtitle"><fmt:message key='add.target.element'/></h3>
        <%
            }
        %>
        <table style="width: 100%" class="styledLeft noBorders">
            <thead>
                <tr>
                    <th colspan="2"></th>
                </tr>
            </thead>
        <tbody>
        <tr>
            <td>
                <div style="height:30px;">
                    <a href="javascript:document.location.href='add-subjects.jsp?subElementId=<%=entitlementPolicyBean.getSubElementNumber()%>&matchElementId=0'" class="icon-link"
                       style="background-image:url(images/add.gif);"><fmt:message key='add.new.subject.element'/></a>
                </div>
                <table class="styledLeft" border="1">
                <thead>
                    <th><fmt:message key="subject.element.name"/></th>
                    <th><fmt:message key="action"/></th>
                </thead>
                <tbody>

                <%
                int subjectElementCount = 0;
                if(existingElements != null && existingElements.size() > 0) {
                    for (SubElementDTO existingElement : existingElements) {
                        if(existingElement.getElementName().equals(EntitlementPolicyConstants.SUBJECT_ELEMENT)) {
                            subjectElementCount ++;
                            if(entitlementPolicyBean.getCurrentRuleId() == null && existingElement.getRuleId() == null ) {
                %>

                <tr>
                    <td><fmt:message key="subject"/> <%=existingElement.getElementId()%></td>
                    <td>
	                    <a href="add-subjects.jsp?subElementId=<%=existingElement.getElementId()%>&editSubElement=true" class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message key="edit"/></a>
                        <a href="#" onclick="deleteSubElement('<%=existingElement.getElementId()%>')" class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message key="delete"/></a
                    </td>
                </tr>

                <%

                            } else if(entitlementPolicyBean.getCurrentRuleId().equals(existingElement.getRuleId())) {
                %>

                <tr>
                    <td>Subject <%=existingElement.getElementId()%></td>
                    <td>
	                    <a href="add-subjects.jsp?subElementId=<%=existingElement.getElementId()%>&editSubElement=true" class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message key="edit"/></a>
                        <a href="#" onclick="deleteSubElement('<%=existingElement.getElementId()%>')" class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message key="delete"/></a
                    </td>
                </tr>

                <%
                            }
                        }
                    }
                }
                if(subjectElementCount == 0){
                %>
                <tr class="noDataBox">
                    <td colspan="3"><fmt:message key="no.subject.define"/><br/></td>
                </tr>
                <%
                    }
                %>
                </tbody>    
                </table>

                <div style="height:30px;">
                    <a href="javascript:document.location.href='add-actions.jsp?subElementId=<%=entitlementPolicyBean.getSubElementNumber()%>'" class="icon-link"
                       style="background-image:url(images/add.gif);"><fmt:message key='add.new.action.element'/></a>
                </div>

               <table class="styledLeft" border="1">
                <thead>
                    <th><fmt:message key="action.element.name"/></th>
                    <th><fmt:message key="action"/></th>
                </thead>
                <tbody>
                <%
                int actionElementCount = 0;
                if(existingElements != null && existingElements.size() > 0) {                    
                    for (SubElementDTO existingElement : existingElements) {
                        if(existingElement.getElementName().equals(EntitlementPolicyConstants.ACTION_ELEMENT)) {
                            actionElementCount ++;
                            if(entitlementPolicyBean.getCurrentRuleId() == null && existingElement.getRuleId() == null ) {
                %>

                <tr>
                    <td><fmt:message key="action"/> <%=existingElement.getElementId()%></td>
                    <td>
	                    <a href="add-actions.jsp?subElementId=<%=existingElement.getElementId()%>&editSubElement=true" class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message key="edit"/></a>
                        <a href="#" onclick="deleteSubElement('<%=existingElement.getElementId()%>')" class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message key="delete"/></a
                    </td>
                </tr>

                <%

                        } else if(entitlementPolicyBean.getCurrentRuleId().equals(existingElement.getRuleId())) {
                %>

                <tr>
                    <td>Action <%=existingElement.getElementId()%></td>
                    <td>
	                    <a href="add-actions.jsp?subElementId=<%=existingElement.getElementId()%>&editSubElement=true" class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message key="edit"/></a>
                        <a href="#" onclick="deleteSubElement('<%=existingElement.getElementId()%>')" class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message key="delete"/></a
                    </td>
                </tr>

                <%
                            }
                        }
                    }
                }

                if(actionElementCount == 0){
                %>
                <tr class="noDataBox">
                    <td colspan="3"><fmt:message key="no.action.define"/><br/></td>
                </tr>
                <%
                    }
                %>
                </tbody>
                </table>

                <div style="height:30px;">
                    <a href="javascript:document.location.href='add-resources.jsp?subElementId=<%=entitlementPolicyBean.getSubElementNumber()%>'" class="icon-link"
                       style="background-image:url(images/add.gif);"><fmt:message key='add.new.resource.element'/></a>
                </div>

               <table class="styledLeft" border="1">
                <thead>
                    <th><fmt:message key="resource.element.name"/></th>
                    <th><fmt:message key="action"/></th>
                </thead>
                <tbody>                      
                <%
                int resourceElementCount = 0;
                if(existingElements != null && existingElements.size() > 0) {
                    for (SubElementDTO existingElement : existingElements) {
                        if(existingElement.getElementName().equals(EntitlementPolicyConstants.RESOURCE_ELEMENT)) {
                            resourceElementCount ++;
                            if(entitlementPolicyBean.getCurrentRuleId() == null && existingElement.getRuleId() == null ) {
                %>

                <tr>
                    <td>Resource <%=existingElement.getElementId()%></td>
                    <td>
	                    <a href="add-resources.jsp?subElementId=<%=existingElement.getElementId()%>&editSubElement=true" class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message key="edit"/></a>
                        <a href="#" onclick="deleteSubElement('<%=existingElement.getElementId()%>')" class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message key="delete"/></a
                    </td>
                </tr>

                <%

                            } else if(entitlementPolicyBean.getCurrentRuleId().equals(existingElement.getRuleId())) {
                %>

                <tr>
                    <td><fmt:message key="resource"/> <%=existingElement.getElementId()%></td>
                    <td>
	                    <a href="add-resources.jsp?subElementId=<%=existingElement.getElementId()%>&editSubElement=true" class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message key="edit"/></a>
                        <a href="#" onclick="deleteSubElement('<%=existingElement.getElementId()%>')" class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message key="delete"/></a
                    </td>
                </tr>

                <%
                            }
                        }
                    }
                }

                if(resourceElementCount == 0){
                %>
                <tr class="noDataBox">
                    <td colspan="3"><fmt:message key="no.resource.define"/><br/></td>
                </tr>
                <%
                    }
                %>
                </tbody>
                </table>

                <div style="height:30px;">
                    <a href="javascript:document.location.href='add-environments.jsp?subElementId=<%=entitlementPolicyBean.getSubElementNumber()%>'" class="icon-link"
                       style="background-image:url(images/add.gif);"><fmt:message key='add.new.environment.element'/></a>
                </div>

               <table class="styledLeft" border="1">
                <thead>
                    <th><fmt:message key="environment.element.name"/></th>
                    <th><fmt:message key="action"/></th>
                </thead>
                <tbody>                     
                <%
                int environmentElementCount = 0;
                if(existingElements != null && existingElements.size() > 0) {
                    for (SubElementDTO existingElement : existingElements) {
                        if(existingElement.getElementName().equals(EntitlementPolicyConstants.ENVIRONMENT_ELEMENT)) {
                            environmentElementCount ++;
                            if(entitlementPolicyBean.getCurrentRuleId() == null && existingElement.getRuleId() == null ) {
                %>

                <tr>
                    <td>Environment <%=existingElement.getElementId()%></td>
                    <td>
	                    <a href="add-environments.jsp?subElementId=<%=existingElement.getElementId()%>&editSubElement=true" class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message key="edit"/></a>
                        <a href="#" onclick="deleteSubElement('<%=existingElement.getElementId()%>')" class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message key="delete"/></a
                    </td>
                </tr>

                <%

                            } else if(entitlementPolicyBean.getCurrentRuleId().equals(existingElement.getRuleId())) {
                %>

                <tr>
                    <td><fmt:message key="environment"/> <%=existingElement.getElementId()%></td>
                    <td>
	                    <a href="add-environments.jsp?subElementId=<%=existingElement.getElementId()%>&editSubElement=true" class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message key="edit"/></a>
                        <a href="#" onclick="deleteSubElement('<%=existingElement.getElementId()%>')" class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message key="delete"/></a
                    </td>
                </tr>

                <%
                            }
                        }
                    }
                }

                if(environmentElementCount == 0){
                %>
                <tr class="noDataBox">
                    <td colspan="3"><fmt:message key="no.environment.define"/><br/></td>
                </tr>
                <%
                    }
                %>
                </tbody>
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
                            if(entitlementPolicyBean.getCurrentRuleId() != null) {
                                if (existingElements != null && existingElements.size() > 0){
                        %>
                                    <input class="button" type="button" value="<fmt:message key='update'/>" onclick="doAdd();"/>
                        <%
                                } else {
                        %>
                                    <input class="button" type="button" value="<fmt:message key='add'/>" onclick="doAdd();"/>                         
                        <%
                                }
                        %>
                                <input class="button" type="button" value="<fmt:message key='cancel'/>"  onclick="doBackToRule();"/>
                        <%
                            } else {
                        %>
                                <input class="button" type="button" value="<fmt:message key='back'/>" onclick="doBack();"/>
                                <input class="button" type="button" value="<fmt:message key='next'/>" onclick="doNext();"/>
                                <input class="button" type="button" value="<fmt:message key='finish'/>" onclick="doFinish();"/>
                                <input class="button" type="button" value="<fmt:message key='cancel'/>"  onclick="doCancel();"/>
                        <%
                            }
                        %>
                     </td>
                </tr>
            </tbody>
            </table>
        </div>
    </div>
</fmt:bundle>
