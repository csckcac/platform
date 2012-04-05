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

<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.*" %>

<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />

<%

    String[] matchIds = entitlementPolicyBean.getMatchIds();
    String[] dataTypes = entitlementPolicyBean.getDataTypes();
    String[] mustPresents = entitlementPolicyBean.getMustBePresentValues();
    MatchElementDTO currentMatchElement = null;
    AttributeValueElementDTO attributeValueElementDTO = null;
    AttributeDesignatorDTO attributeDesignatorDTO = null;
    AttributeSelectorDTO attributeSelectorDTO = null;

    String editSubElement = CharacterEncoder.getSafeText(request.getParameter("editSubElement"));
    String matchElementId = CharacterEncoder.getSafeText(request.getParameter("matchElementId"));
    String editMatchElement = CharacterEncoder.getSafeText(request.getParameter("editMatchElement"));

    if(matchElementId !=null && !matchElementId.trim().equals("") && !matchElementId.trim().
            equals("null") && editMatchElement != null && "true".equals(editMatchElement.trim())){
        currentMatchElement = entitlementPolicyBean.getMatchElement(Integer.parseInt(matchElementId));
        if(currentMatchElement != null){
            attributeValueElementDTO = currentMatchElement.
                    getAttributeValueElementDTO();
            attributeDesignatorDTO = currentMatchElement.
                    getAttributeDesignatorDTO();
            attributeSelectorDTO = currentMatchElement.
                    getAttributeSelectorDTO();
        }
    }


    String subElementId = CharacterEncoder.getSafeText(request.getParameter("subElementId"));
    String matchId = CharacterEncoder.getSafeText(request.getParameter("matchId"));
    String attributeValueDataType = CharacterEncoder.getSafeText(request.getParameter("dataType"));
    String attributeValue = CharacterEncoder.getSafeText(request.getParameter("attributeValue"));
    String attributeDesignatorDataType = CharacterEncoder.getSafeText(request.
            getParameter("attributeDesignatorDataType"));
    String attributeId = CharacterEncoder.getSafeText(request.getParameter("attributeId"));
    String issuerName = CharacterEncoder.getSafeText(request.getParameter("issuerName"));
    String attributeDesignatorMustPresent = CharacterEncoder.getSafeText(request.
            getParameter("attributeDesignatorMustPresent"));
    String attributeSelectorDataType = CharacterEncoder.getSafeText(request.
            getParameter("attributeSelectorDataType"));
    String requestContextPath = CharacterEncoder.getSafeText(request.getParameter("requestContextPath"));
    String attributeSelectorMustPresent = CharacterEncoder.getSafeText(request.
            getParameter("attributeSelectorMustPresent"));

    if(subElementId != null && !subElementId.trim().equals("") && !subElementId.trim().equals("null")) {
       entitlementPolicyBean.setCurrentSubElementNumber(Integer.parseInt(subElementId));
        SubElementDTO subElementDTO = entitlementPolicyBean.getTargetElementDTO(Integer.parseInt(subElementId));
        if(subElementDTO != null) {
            List<MatchElementDTO> matchElementList = subElementDTO.getMatchElementDTOs();
            for(MatchElementDTO matchElement : matchElementList ) {
                entitlementPolicyBean.setMatchElement(matchElement);
            }
        }
    }

    if(matchId != null && !matchId.trim().equals("") && attributeValue != null &&
            !attributeValue.trim().equals("") && attributeValueDataType !=null &&
            !attributeValueDataType.trim().equals("")) {

        MatchElementDTO matchElementDTO = new MatchElementDTO();
        AttributeValueElementDTO valueElementDTO = new AttributeValueElementDTO();
        AttributeDesignatorDTO designatorDTO = new AttributeDesignatorDTO();
        AttributeSelectorDTO selectorDTO = new AttributeSelectorDTO();

        matchElementDTO.setMatchElementName(EntitlementPolicyConstants.RESOURCE_ELEMENT);

        matchElementDTO.setMatchId(matchId);
        if(editMatchElement == null || !"done".equals(editMatchElement.trim())) {
            matchElementDTO.setElementId(entitlementPolicyBean.getMatchElementNumber());
        } else if(matchElementId !=null && !matchElementId.trim().equals("")){
            int matchElementIdInteger = Integer.parseInt(matchElementId);
            entitlementPolicyBean.removeMatchElement(matchElementIdInteger);
            matchElementDTO.setElementId(matchElementIdInteger);
        }

        if(attributeId != null && !attributeId.trim().equals("") &&
           attributeDesignatorDataType != null && !attributeDesignatorDataType.trim().equals("") &&
           !attributeDesignatorDataType.trim().
                   equals(EntitlementPolicyConstants.COMBO_BOX_DEFAULT_VALUE)) {

            designatorDTO.setElementName(EntitlementPolicyConstants.RESOURCE_ELEMENT);
            designatorDTO.setAttributeId(attributeId);
            if(!EntitlementPolicyConstants.COMBO_BOX_DEFAULT_VALUE.
                    equals(attributeDesignatorMustPresent)){
                designatorDTO.setMustBePresent(attributeDesignatorMustPresent);
            }
            designatorDTO.setIssuer(issuerName);
            designatorDTO.setDataType(attributeDesignatorDataType);
            matchElementDTO.setAttributeDesignatorDTO(designatorDTO);
        }

        if(attributeSelectorDataType != null && !attributeSelectorDataType.trim().equals("") &&
                !attributeSelectorDataType.trim().equals(EntitlementPolicyConstants.
                        COMBO_BOX_DEFAULT_VALUE) && requestContextPath != null &&
                !requestContextPath.trim().equals("") ) {

            selectorDTO.setAttributeSelectorDataType(attributeSelectorDataType);
            selectorDTO.setAttributeSelectorRequestContextPath(requestContextPath);
            if(EntitlementPolicyConstants.COMBO_BOX_DEFAULT_VALUE.equals(attributeSelectorMustPresent)){
                selectorDTO.setAttributeSelectorMustBePresent(attributeSelectorMustPresent);
            }
            matchElementDTO.setAttributeSelectorDTO(selectorDTO);
        }

        if(entitlementPolicyBean.getCurrentRuleId() != null &&
                !entitlementPolicyBean.getCurrentRuleId().trim().equals("")) {
            matchElementDTO.setRuleElementName(entitlementPolicyBean.getCurrentRuleId());
        }

        valueElementDTO.setAttributeValue(attributeValue);
        valueElementDTO.setAttributeDataType(attributeValueDataType);
        matchElementDTO.setAttributeValueElementDTO(valueElementDTO);
        entitlementPolicyBean.setMatchElement(matchElementDTO);
    }

%>

<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
    <carbon:breadcrumb
            label="add.resource.element"
            resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>

<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
<script src="../entitlement/js/create-basic-policy.js" type="text/javascript"></script>
<link href="../entitlement/css/entitlement.css" rel="stylesheet" type="text/css" media="all"/>

<script type="text/javascript">

    function doValidation(){
        var value1 = document.getElementsByName("attributeValue")[0].value;
        var value2 = document.getElementsByName("attributeId")[0].value;
        var value3 = document.getElementsByName("requestContextPath")[0].value;
        var value4 = document.getElementsByName("attributeDesignatorDataType")[0].value;
        var value5 = document.getElementsByName("attributeSelectorDataType")[0].value;

        if (value1 == '') {
            CARBON.showWarningDialog('<fmt:message key="attribute.value.is.required"/>');
            return false;
        }

        if((value2 == '' || value4 == 'Select') && (value3 == '' || value5 == 'Select')){
            CARBON.showWarningDialog('<fmt:message key="attribute.designator.selector.element.is.required"/>');
            return false;
        }

        return true;
    }

    function deleteMatchElement(matchElementId) {
        location.href = 'delete-match.jsp?subElementName=Resource&matchElementId=' + matchElementId +
                        '&editSubElement=' + <%=editSubElement%> ;
    }

    function doCancel() {
        location.href = 'cancel-match.jsp?subElementName=Resource';
    }

    function doCancelMatch() {
        location.href = 'add-resources.jsp?editSubElement=' + <%=editSubElement%>;
    }

    function doAdd () {
        var subElementId = "<%=entitlementPolicyBean.getCurrentSubElementNumber()%>";
        location.href = 'add-target.jsp?subElementName=Resource&subElementId=' + subElementId +
                        '&editSubElement=' + <%=editSubElement%>;
    }

    function doAddMatch () {

        var editMatchElement = "<%=editMatchElement%>";
        if(editMatchElement == 'true') {
            editMatchElement = "done";
        } else {
            editMatchElement = "notDone"
        }

        if(doValidation()){
            document.matchElementForm.action = "add-resources.jsp?matchElementId=" + '<%=matchElementId%>'
                + "&editSubElement=" + '<%=editSubElement%>' +  "&editMatchElement=" + editMatchElement;
            document.matchElementForm.submit();
        }
    }

</script>

<div id="middle">
    <h2><fmt:message key='entitlement.policy.creation'/></h2>
    <div id="workArea">
    <%
        if(editSubElement != null && "true".equals(editSubElement)){
    %>
        <h3 class="page-subtitle"><fmt:message key='edit.resource.element'/></h3>
    <%
        } else {
    %>
        <h3 class="page-subtitle"><fmt:message key='add.resource.element'/></h3>
    <%
	    }
    %>
    <form name="matchElementForm"  action="create-policy.jsp" method="post">
    <div class="tabTen">
        <table style="width: 100%" class="styledLeft noBorders">
        <thead>
            <tr>
                <th colspan="2"></th>
            </tr>
        </thead>
        <tbody>
        <tr>
            <td class="leftCol-med"><fmt:message key="match.id"/></td>
            <td>
                <select id="matchId" name="matchId">
            <%
            if (matchIds != null) {
                for (String id : matchIds) {
                    if(currentMatchElement != null && id.equals(currentMatchElement.getMatchId())) {
            %>
                    <option value="<%=id%>" selected="selected"><%=currentMatchElement.getMatchId()%></option>
            <%
                    } else {
            %>
                    <option value="<%=id%>"><%=id%></option>
            <%
                    }
                }
            }
            %>
                </select>
            </td>
        </tr>

        <tr>
            <td colspan="2">
            <table cellspacing="0" class="styledInner">
                <thead>
                <tr>
                    <th>
                        <a onclick="showHideRow(this)"
                           class="icon-link <%if(attributeValueElementDTO != null){%>arrowUp<%} else {%>arrowDown<% } %> " id="attributeValueLink"><fmt:message key="attribute.value.element"/></a>
                    </th>
                </tr>
                </thead>
            </table>
            <div id="attributeValueLinkRow" style="margin-left:2px; <%if(attributeValueElementDTO == null) {%>display:none<% } %>">
            <table class="noBorders" cellspacing="0" style="width:100%;padding-top:5px;">
                <tr>
                    <td><fmt:message key="attribute.data.type"/></td>
                    <td>
                        <select id="dataType" name="dataType">
                    <%
                    if (dataTypes != null) {
                        for (String dataType : dataTypes) {
                            if(attributeValueElementDTO != null && dataType.equals(attributeValueElementDTO.getAttributeDataType())) {
                    %>
                            <option value="<%=dataType%>" selected="selected"><%=attributeValueElementDTO.getAttributeDataType()%></option>
                    <%
                            } else {
                    %>
                            <option value="<%=dataType%>"><%=dataType%></option>
                    <%
                            }
                        }
                    }
                    %>
                        </select>
                    </td>
                </tr>

                <tr>
                    <td ><fmt:message key='attribute.value'/><font class="required">*</font></td>
                    <%
                    if(attributeValueElementDTO != null && attributeValueElementDTO.getAttributeValue() != null) {
                    %>
                        <td><input id="attributeValue" name="attributeValue" class="text-box-big" type="text" value="<%=attributeValueElementDTO.getAttributeValue()%>"></td>
                    <%
                    } else {
                    %>
                        <td><input type="text" name="attributeValue" id="attributeValue" class="text-box-big"/></td>
                    <%
                    }
                    %>
                </tr>
            </table>
            </div>
            </td>
        </tr>

        <tr>
            <td colspan="2">
            <table cellspacing="0" class="styledInner">
                <thead>
                <tr>
                    <th>
                        <a onclick="showHideRow(this)"
                           class="icon-link <%if(attributeDesignatorDTO != null){%>arrowUp<%} else {%>arrowDown<% } %> " id="attributeDesignatorLink"><fmt:message key="attribute.designator.element"/></a>
                    </th>
                </tr>
                </thead>
            </table>
            <div id="attributeDesignatorLinkRow" style="margin-left:2px; <%if(attributeDesignatorDTO == null) {%>display:none<% } %>">
                <table class="noBorders" cellspacing="0" style="width:100%;padding-top:5px;">
                    <tr>
                        <td class="leftCol-med"><fmt:message key="attribute.designator.data.type"/></td>
                        <td>
                            <select id="attributeDesignatorDataType" name="attributeDesignatorDataType">
                                <option value="Select" selected="selected">Select</option>
                                <%
                                    if (dataTypes != null) {
                                        for (String attributeDesignatorData : dataTypes) {
                                            if (attributeDesignatorDTO != null && attributeDesignatorData.equals(attributeDesignatorDTO.getDataType())) {
                                %>
                                <option value="<%=attributeDesignatorData%>"
                                        selected="selected"><%=attributeDesignatorDTO.getDataType()%>
                                </option>

                                <%
                                } else {
                                %>
                                <option value="<%=attributeDesignatorData%>"><%=attributeDesignatorData%>
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
                        <td class="leftCol-small"><fmt:message key='attribute.id'/></td>
                        <%
                            if (attributeDesignatorDTO != null && attributeDesignatorDTO.getAttributeId() != null) {
                        %>
                        <td><input id="attributeId" name="attributeId" class="text-box-big" type="text"
                                   value="<%=attributeDesignatorDTO.getAttributeId()%>"></td>
                        <%
                        } else {
                        %>
                        <td><input type="text" name="attributeId" id="attributeId" class="text-box-big"/></td>
                        <%
                            }
                        %>
                    </tr>

                    <tr>
                        <td class="leftCol-small"><fmt:message key='issuer'/></td>
                        <%
                            if (attributeDesignatorDTO != null && attributeDesignatorDTO.getIssuer() != null) {
                        %>
                        <td><input id="issuerName" name="issuerName" class="text-box-big" type="text"
                                   value="<%=attributeDesignatorDTO.getIssuer()%>"></td>
                        <%
                        } else {
                        %>
                        <td><input type="text" name="issuerName" id="issuerName" class="text-box-big"/></td>
                        <%
                            }
                        %>
                    </tr>

                    <tr>
                        <td><fmt:message key="must.present"/></td>
                        <td>
                            <select id="attributeDesignatorMustPresent" name="attributeDesignatorMustPresent">
                                <option value="Select" selected="selected">Select</option>
                                <%
                                    if (mustPresents != null) {
                                        for (String mustPresent : mustPresents) {
                                            if (attributeDesignatorDTO != null && mustPresent.equals(attributeDesignatorDTO.getMustBePresent())) {
                                %>
                                <option value="<%=mustPresent%>"
                                        selected="selected"><%=attributeDesignatorDTO.getMustBePresent()%>
                                </option>

                                <%
                                } else {
                                %>
                                <option value="<%=mustPresent%>"><%=mustPresent%>
                                </option>

                                <%
                                            }
                                        }
                                    }
                                %>
                            </select>
                        </td>
                    </tr>
                </table>
            </div>
            </td>
        </tr>

        <tr>
            <td colspan="2">
            <table cellspacing="0" class="styledInner">
                <thead>
                <tr>
                    <th>
                        <a onclick="showHideRow(this)"
                           class="icon-link <%if(attributeSelectorDTO != null){%>arrowUp<%} else {%>arrowDown<% } %> " id="attributeSelectorLink"><fmt:message key="attribute.selector.element"/></a>
                    </th>
                </tr>
                </thead>
            </table>
            <div id="attributeSelectorLinkRow" style="margin-left:2px; <%if(attributeSelectorDTO == null) {%>display:none<% } %>">
            <table class="noBorders" cellspacing="0" style="width:100%;padding-top:5px;">
                <tr>
                <td class="leftCol-med"><fmt:message key="attribute.selector.data.type"/></td>
                <td>
                    <select id="attributeSelectorDataType" name="attributeSelectorDataType">
                    <option value="Select" selected="selected">Select</option>
                <%
                if (dataTypes != null) {
                    for (String attributeSelectorData : dataTypes) {
                        if(attributeSelectorDTO != null && attributeSelectorData.equals(attributeSelectorDTO.getAttributeSelectorDataType())) {
                %>
                        <option value="<%=attributeSelectorData%>" selected="selected"><%=attributeSelectorDTO.getAttributeSelectorDataType()%></option>
                <%
                        } else {
                %>
                        <option value="<%=attributeSelectorData%>"><%=attributeSelectorData%></option>
                <%
                        }
                    }
                }
                %>
                    </select>
                </td>
            </tr>


            <tr>
                    <td class="leftCol-small"><fmt:message key='request.context.path'/></td>
                <%
                    if(attributeSelectorDTO != null && attributeSelectorDTO.getAttributeSelectorRequestContextPath() != null) {
                %>
                    <td><input id="requestContextPath" name="requestContextPath" class="text-box-big" type="text" value="<%=attributeSelectorDTO.getAttributeSelectorRequestContextPath()%>"></td>
                <%
                    } else {
                %>
                    <td><input type="text" name="requestContextPath" id="requestContextPath" class="text-box-big"/></td>
                <%
                    }
                %>
            </tr>

            <tr>
                <td><fmt:message key="must.present"/></td>
                <td>
                    <select id="attributeSelectorMustPresent" name="attributeSelectorMustPresent">
                    <option value="Select" selected="selected">Select</option>
                <%
                if (mustPresents != null) {
                    for (String mustPresent : mustPresents) {
                        if(attributeSelectorDTO != null && mustPresent.equals(attributeSelectorDTO.getAttributeSelectorMustBePresent())) {
                %>
                        <option value="<%=mustPresent%>" selected="selected"><%=attributeSelectorDTO.getAttributeSelectorMustBePresent()%></option>
                <%
                        } else {
                %>
                        <option value="<%=mustPresent%>"><%=mustPresent%></option>
                <%
                        }
                    }
                }
                %>
                    </select>
                </td>
            </tr>
            </table>
            </div>
            </td>
        </tr>

        <tr>
            <td class="buttonRow" colspan="2">
                <%
                    if("true".equals(editMatchElement)){
                %>
                    <input class="button" type="button" value="<fmt:message key='update'/>" onclick="doAddMatch();"/>
                <%
                    } else {
                %>
                    <input class="button" type="button" value="<fmt:message key='add'/>" onclick="doAddMatch();"/>
                <%
                    }
                %>
                <input class="button" type="button" value="<fmt:message key='cancel'/>" onclick="doCancelMatch();"/>
            <td>
        <tr>
        </tbody>
        </table>
    </div>
    </form>

    <div class="tabTen">
        <table class="styledLeft" border="1">
        <thead>
            <th><fmt:message key="match.element.id"/></th>
            <th><fmt:message key="match.id"/></th>
            <th><fmt:message key="action"/></th>
        </thead>
        <tbody>
        <%
        List<MatchElementDTO> existingMatchElements = entitlementPolicyBean.getMatchElements();
        if(existingMatchElements != null && existingMatchElements.size() > 0) {
            for (MatchElementDTO existingMatchElement : existingMatchElements) {
                if(entitlementPolicyBean.getCurrentRuleId() == null && existingMatchElement.getRuleElementName() == null ) {
        %>

        <tr>
            <td><fmt:message key="resource.match"/>  <%=existingMatchElement.getElementId()%></td>
            <td><%=existingMatchElement.getMatchId()%></td>
            <td>
                <a href="add-resources.jsp?subElementName=Resource&matchElementId=<%=existingMatchElement.getElementId()%>&editMatchElement=true&editSubElement=<%=editSubElement%>" class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message key="edit"/></a>
                <a href="#" onclick="deleteMatchElement('<%=existingMatchElement.getElementId()%>')" class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message key="delete"/></a
            </td>
        </tr>

        <%
                }else if(entitlementPolicyBean.getCurrentRuleId().equals(existingMatchElement.getRuleElementName())) {
        %>

        <tr>
            <td><fmt:message key="resource.match"/> <%=existingMatchElement.getElementId()%></td>
            <td><%=existingMatchElement.getMatchId()%></td>            
            <td>
                <a href="add-resources.jsp?subElementName=Resource&matchElementId=<%=existingMatchElement.getElementId()%>&editMatchElement=true&editSubElement=<%=editSubElement%>" class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message key="edit"/></a>
                <a href="#" onclick="deleteMatchElement('<%=existingMatchElement.getElementId()%>')" class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message key="delete"/></a
            </td>
        </tr>

        <%
                }
            }
        } else {
        %>
        <tr class="noDataBox">
            <td colspan="3"><fmt:message key="no.resource.match.define"/><br/></td>
        </tr>
        <%
            }
        %>
        </tbody>
    </table>

    <table class="styledLeft" style="margin-top:20px;">
        <tbody>
        <tr>
            <td class="buttonRow">
                <%
                    if(editSubElement != null && "true".equals(editSubElement)){
                %>
                    <input type="submit" class="button" value="<fmt:message key="update"/>" onclick="doAdd();"/>
                <%
                    } else {
                %>
                    <input type="submit" class="button" value="<fmt:message key="add"/>" onclick="doAdd();"/>                
                <%
                    }
                %>
                <input class="button" type="button" value="<fmt:message key='cancel'/>" onclick="doCancel();"/>
            </td>
        </tr>
        </tbody>
    </table>
    </div>
</div>
</fmt:bundle>
