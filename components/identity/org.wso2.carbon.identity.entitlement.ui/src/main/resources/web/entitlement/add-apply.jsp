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
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.ApplyElementDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.AttributeValueElementDTO" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.AttributeDesignatorDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.AttributeSelectorDTO" %>

<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />

<%
    String currentFunctionId = "";
    String currentFunctionFunctionId = "";
    ApplyElementDTO currentApplyElementDTO = null;
    AttributeValueElementDTO currentAttributeValueElementDTO = null;
    AttributeDesignatorDTO currentAttributeDesignatorDTO = null;
    AttributeSelectorDTO currentAttributeSelectorDTO = null;
    List<AttributeValueElementDTO>  attributeValueElementDTOList = null;
    List<AttributeDesignatorDTO> attributeDesignatorDTOList = null;
    List <AttributeSelectorDTO> attributeSelectorDTOList = null;
    List<ApplyElementDTO> applyElementDTOList = entitlementPolicyBean.getApplyElementDTOs();

    String applyElementId = CharacterEncoder.getSafeText(request.getParameter("applyElementId"));
    String editApplyElement = CharacterEncoder.getSafeText(request.getParameter("editApplyElement"));
    String attributeDesignatorElementId = CharacterEncoder.getSafeText(request.
            getParameter("attributeDesignatorElementId"));
    String attributeSelectorElementId = CharacterEncoder.getSafeText(request.
            getParameter("attributeSelectorElementId"));
    String attributeValueElementId = CharacterEncoder.getSafeText(request.
            getParameter("attributeValueElementId"));



    String [] attributeDesignatorTypes = new String[] {EntitlementPolicyConstants.SUBJECT_ELEMENT,
                                                       EntitlementPolicyConstants.ACTION_ELEMENT,
                                                       EntitlementPolicyConstants.ENVIRONMENT_ELEMENT,
                                                       EntitlementPolicyConstants.RESOURCE_ELEMENT};

    String previousPageId = "";

    if("true".equals(editApplyElement)){
        if(applyElementDTOList != null && applyElementDTOList.size() > 0) {

            for (ApplyElementDTO applyElementDTO : applyElementDTOList) {
                if(applyElementDTO.getApplyElementId().equals(applyElementId)) {
                    currentApplyElementDTO = applyElementDTO;
                }
            }
        }
        if(currentApplyElementDTO != null) {

            attributeValueElementDTOList = currentApplyElementDTO.getAttributeValueElementDTOs();
            if(attributeValueElementDTOList != null) {
                for(AttributeValueElementDTO attributeValueElementDTO :
                        attributeValueElementDTOList) {
                    if(attributeValueElementDTO.getApplyElementId().equals(applyElementId)) {
                        entitlementPolicyBean.setAttributeValueElementDTOs(attributeValueElementDTO);
                    }
                }
            }

            attributeDesignatorDTOList = currentApplyElementDTO.getAttributeDesignators();
            if(attributeDesignatorDTOList != null) {
                for(AttributeDesignatorDTO attributeDesignatorDTO :
                        attributeDesignatorDTOList) {
                    if(attributeDesignatorDTO.getApplyElementId().equals(applyElementId)) {
                        entitlementPolicyBean.setAttributeDesignatorDTOs(attributeDesignatorDTO);
                    }
                }
            }

            attributeSelectorDTOList = currentApplyElementDTO.getAttributeSelectors();
            if(attributeSelectorDTOList != null) {
                for(AttributeSelectorDTO attributeSelectorDTO :
                        attributeSelectorDTOList) {
                    if(attributeSelectorDTO.getApplyElementId().equals(applyElementId)) {
                        entitlementPolicyBean.setAttributeSelectorDTOs(attributeSelectorDTO);
                    }
                }
            }


            entitlementPolicyBean.setApplyElementDTOs(currentApplyElementDTO.getApplyElements());

            currentFunctionId = currentApplyElementDTO.getFunctionId();
            if(currentFunctionId != null && !currentFunctionId.equals("")) {
                entitlementPolicyBean.functionIdMap.put(applyElementId, currentFunctionId);
            }

            currentFunctionFunctionId = currentApplyElementDTO.getFunctionFunctionId();
            if(currentFunctionFunctionId != null && !currentFunctionFunctionId.equals("")) {
                entitlementPolicyBean.functionIdElementValueMap.
                        put(applyElementId, currentFunctionFunctionId);
            }
        }
    }


    if(applyElementId != null && !applyElementId.equals("")){

        String[] pageNumberArray = applyElementId.split("/");
        String currentPageNumberString = pageNumberArray[pageNumberArray.length - 1];
        if(currentPageNumberString != null && !currentPageNumberString.equals("")){
            previousPageId = applyElementId.substring(0, applyElementId.indexOf(currentPageNumberString)-1);

        }


        attributeValueElementDTOList = entitlementPolicyBean.getAttributeValueElementDTOs(applyElementId);
        attributeDesignatorDTOList = entitlementPolicyBean.getAttributeDesignatorDTOs(applyElementId);
        attributeSelectorDTOList = entitlementPolicyBean.getAttributeSelectorDTOs(applyElementId);

        if(attributeValueElementId != null && !attributeValueElementId.trim().equals("") &&
                !attributeValueElementId.equals("null")) {
             currentAttributeValueElementDTO = entitlementPolicyBean.getAttributeValueElement(Integer.
                     parseInt(attributeValueElementId));
        }

        if(attributeSelectorElementId != null && !attributeSelectorElementId.trim().equals("") &&
                !attributeSelectorElementId.equals("null")){
             currentAttributeSelectorDTO = entitlementPolicyBean.getAttributeSelectorElement(Integer.
                     parseInt(attributeSelectorElementId));
        }

        if(attributeDesignatorElementId != null && !attributeDesignatorElementId.trim().equals("") &&
                !attributeDesignatorElementId.equals("null")){
             currentAttributeDesignatorDTO = entitlementPolicyBean.getAttributeDesignatorElement(Integer.
                     parseInt(attributeDesignatorElementId));
        }
    }
    
    if(currentFunctionId == null || currentFunctionId.trim().equals("")) {
        currentFunctionId = entitlementPolicyBean.functionIdMap.get(applyElementId);
    }

    if(currentFunctionFunctionId == null || currentFunctionFunctionId.trim().equals("")) {
        currentFunctionFunctionId = entitlementPolicyBean.functionIdElementValueMap.get(applyElementId);
    }
%>

<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
	<carbon:breadcrumb label="add.apply.element"
		resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />

<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
<script src="../entitlement/js/create-basic-policy.js" type="text/javascript"></script>
<link href="../entitlement/css/entitlement.css" rel="stylesheet" type="text/css" media="all"/>

    <script type="text/javascript">

        function doValidation() {

        }

        function submitForm(){
            document.applyElements.action = "update-apply.jsp?nextPage=add-apply&completeApply=true&applyElementId=" + '<%=applyElementId%>';
            document.applyElements.submit();
        }

        function deleteAttributeValueElement(attributeValueElementId) {
            location.href = 'delete-attribute-value.jsp?attributeValueElementId=' +
                            attributeValueElementId + '&applyElementId=' + '<%=applyElementId%>';
        }


        function editAttributeValueElement(attributeValueElementId){
            location.href = 'add-apply.jsp?attributeValueElementId=' +
                            attributeValueElementId + '&applyElementId=' + '<%=applyElementId%>';
        }

        function deleteApplyElement(elementId) {
            location.href = 'delete-apply.jsp?elementId=' + elementId + '&applyElementId=' + '<%=applyElementId%>';
        }

        function editApplyElement(applyElementId) {
            location.href = 'add-apply.jsp?applyElementId=' + applyElementId + '&editApplyElement=true';
        }

        function doApply() {
            document.applyElements.action = "update-apply.jsp?nextPage=add-apply&applyElementId=" +
                                            '<%=applyElementId%>' + "&completeApply=false";
            document.applyElements.submit();
        }

        function doCancelForm() {
            var previousPageId= '<%=previousPageId%>';
            if(previousPageId == ''){
                location.href = 'add-condition.jsp';
            } else {
                location.href = 'add-apply.jsp?applyElementId=' + previousPageId;
            }
        }

        function doAddAttributeValueElement() {
            document.applyElements.action = "update-apply.jsp?nextPage=add-apply&applyElementId=" +
                    '<%=applyElementId%>' +  "&attributeValueElementId=" + '<%=attributeValueElementId%>';
            document.applyElements.submit();
        }

        function doAddAttributeDesignatorElement(){
            document.applyElements.action = "update-apply.jsp?nextPage=add-apply&applyElementId=" +
                    '<%=applyElementId%>' +  "&attributeDesignatorElementId=" +
                                            '<%=attributeDesignatorElementId%>';
            document.applyElements.submit();
        }

        function editAttributeDesignatorElement(attributeDesignatorElementId){
            location.href = 'add-apply.jsp?attributeDesignatorElementId=' +
                            attributeDesignatorElementId + '&applyElementId=' + '<%=applyElementId%>';
        }

        function deleteAttributeDesignatorElement(attributeDesignatorElementId){
            location.href = 'delete-attribute-designator.jsp?attributeDesignatorElementId=' +
                            attributeDesignatorElementId + '&applyElementId=' + '<%=applyElementId%>';
        }

        function editAttributeSelectorSElement(attributeSelectorElementId){
            location.href = 'add-apply.jsp?attributeSelectorElementId=' +
                            attributeSelectorElementId + '&applyElementId=' + '<%=applyElementId%>';
        }

        function deleteAttributeSelectorElement(attributeSelectorElementId){
            location.href = 'delete-attribute-selector.jsp?attributeSelectorElementId=' +
                            attributeSelectorElementId + '&applyElementId=' + '<%=applyElementId%>';
        }

        function doCancel(){
            location.href = 'add-apply.jsp?applyElementId=' +  '<%=applyElementId%>';
        }

        function doAddAttributeSelectorElement(){
            document.applyElements.action = "update-apply.jsp?nextPage=add-apply&applyElementId=" +
                    '<%=applyElementId%>' +  "&attributeSelectorElementId=" +
                                            '<%=attributeSelectorElementId%>';
            document.applyElements.submit();
        }
    </script>

<div id="middle">
<h2><fmt:message key='entitlement.policy.creation'/></h2>
<div id="workArea">

    <%
        if(entitlementPolicyBean.isEditPolicy()){
    %>
        <h3 class="page-subtitle"><fmt:message key='edit.apply.match.element'/></h3>
    <%
        } else {
    %>
        <h3 class="page-subtitle"><fmt:message key='add.apply.match.element'/></h3>
    <%
    }
    %>
<form name="applyElements"  action="create-policy.jsp" method="post">
    <table style="width: 100%" class="styledLeft noBorders">
        <thead>
            <tr>
                <th colspan="2"></th>
            </tr>
        </thead>
    <tbody>
    <tr>
        <td>
        <table class="styledLeft noBorders">
            <tr>
            <td class="leftCol-small"><fmt:message key='function.id'/><font class="required">*</font></td>
            <td>
                <select id="functionId" name="functionId">
            <%
            String[] functionIds = entitlementPolicyBean.getFunctionIds();
            if (functionIds != null) {
                for (String functionIdName : functionIds) {
                    if(currentFunctionId != null && functionIdName.equals(currentFunctionId)) {
            %>
                    <option value="<%=functionIdName%>" selected="selected"><%=currentFunctionId%></option>

            <%
                    } else {
            %>
                    <option value="<%=functionIdName%>"><%=functionIdName%></option>

            <%
                    }
                }
            }
            %>
                </select>
            </td>
            </tr>
        </table>            
        </td>
    </tr>


    <tr>
        <td colspan="2">
        <table cellspacing="0" class="styledInner">
            <thead>
            <tr>
                <th>
                    <a onclick="showHideRow(this)"
                       class="icon-link arrowUp" id="applyElementLink"><fmt:message key="apply.element"/></a>
                </th>
            </tr>
            </thead>
        </table>
        <div id="applyElementLinkRow" style="margin-left:2px;">
        <table class="noBorders" id="applyElement" style="width: 100%">
        <tr>
            <td>
            <a class="icon-link"  onclick="doApply();" style="background-image:url(images/add.gif);">
            <fmt:message key='add.apply.element'/></a>
            </td>
        </tr>
        </table>
        </div>
        </td>
    </tr>

    <tr>
        <td>
            <table class="styledLeft" border="1">
            <thead>
            <tr>
                <th><fmt:message key="apply.element.id"/></th>
                <th><fmt:message key="function.id"/></th>
                <th><fmt:message key="action"/></th>
            </tr>
            </thead>
            <body>
            <%
            List<ApplyElementDTO> existingApplyElementDTOs = entitlementPolicyBean.getApplyElementDTOs(applyElementId);
            if(existingApplyElementDTOs != null && existingApplyElementDTOs.size() > 0) {
                for (ApplyElementDTO existingApplyElementDTO : existingApplyElementDTOs) {
                    if(!existingApplyElementDTO.getApplyElementId().equals(applyElementId)) {
            %>
            <tr>
                <td><fmt:message key="apply.element"/> <%=existingApplyElementDTO.getApplyElementId()%></td>
                <td><%=existingApplyElementDTO.getFunctionId()%></td>
                <td>
                    <a href="#" onclick="editApplyElement('<%=existingApplyElementDTO.getApplyElementId()%>')" class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message key="edit"/></a>
                    <a href="#" onclick="deleteApplyElement('<%=existingApplyElementDTO.getApplyElementId()%>')" class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message key="delete"/></a
                </td>
            </tr>
            <%
                    }
                }
            } else {
            %>
            <tr class="noRuleBox">
                <td colspan="3"><fmt:message key="no.apply.element.define"/> <br/>
                    <div style="clear:both"> </div>
                </td>
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
        <table cellspacing="0" class="styledInner">
            <thead>
            <tr>
                <th>
                    <a onclick="showHideRow(this)"
                       class="icon-link <%if(currentFunctionFunctionId != null){%>arrowUp<%} else {%>arrowDown<% } %> " id="functionElementLink"><fmt:message key="function.element"/></a>
                </th>
            </tr>
            </thead>
        </table>
        <div id="functionElementLinkRow" style="margin-left:2px; <%if(currentFunctionFunctionId == null) {%>display:none<% } %>">
         <table class="noBorders" id="functionElement" style="width: 100%">
            <tr>
                <td class="formRow" style="padding:0 !important">
                <td><fmt:message key="function.id"/></td>
                <td>
                <select id="functionFunctionId" name="functionFunctionId">
                        <option value="Select" selected="selected">Select</option>
                <%
                functionIds = entitlementPolicyBean.getFunctionIds();
                if (functionIds != null) {
                    for (String functionIdName : functionIds) {
                        if(currentFunctionFunctionId != null && functionIdName.equals(currentFunctionFunctionId)) {
                %>
                        <option value="<%=functionIdName%>" selected="selected"><%=currentFunctionFunctionId%></option>
                <%
                        } else {
                %>
                        <option value="<%=functionIdName%>"><%=functionIdName%></option>
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
                           class="icon-link <%if(currentAttributeValueElementDTO != null){%>arrowUp<%} else {%>arrowDown<% } %> " id="attributeValueLink"><fmt:message key="attribute.value.element"/></a>
                    </th>
                </tr>
                </thead>
            </table>
            <div id="attributeValueLinkRow" style="margin-left:2px; <%if(currentAttributeValueElementDTO == null) {%>display:none<% } %>">
            <table class="noBorders" id="attributeValueElement" style="width: 100%">
            <tr>
            <td class="formRow" style="padding:0 !important">
                <table class="normal" cellspacing="0">

                    <td><fmt:message key="attribute.data.type"/></td>
                    <td>
                        <select id="attributeDataType" name="attributeDataType">
                    <%
                        if (entitlementPolicyBean.getDataTypes() != null) {
                            for (String attributeDataType : entitlementPolicyBean.getDataTypes()) {
                                if(currentAttributeValueElementDTO != null && attributeDataType.equals(currentAttributeValueElementDTO.getAttributeDataType())) {
                    %>
                                <option value="<%=attributeDataType%>" selected="selected"><%=currentAttributeValueElementDTO.getAttributeDataType()%></option>

                    <%
                                } else {
                    %>
                                <option value="<%=attributeDataType%>"><%=attributeDataType%></option>

                    <%
                                }
                            }
                        }
                    %>
                        </select>
                    </td>

                    <tr>
                            <td class="leftCol-small"><fmt:message key='attribute.value'/><font class="required">*</font></td>
                    <%
                        if(currentAttributeValueElementDTO != null && currentAttributeValueElementDTO.getAttributeValue() != null) {
                    %>
                            <td><input type="text" name="attributeValue" id="attributeValue" class="text-box-big" value="<%=currentAttributeValueElementDTO.getAttributeValue()%>"/></td>
                    <%
                        } else {
                    %>
                            <td><input type="text" name="attributeValue" id="attributeValue" class="text-box-big"/></td>
                    <%
                        }
                    %>
                    </tr>

                </table>
                </td>
            </tr>
            <tr>
                <td colspan="2" class="buttonRow">
                    <%
                        if(currentAttributeValueElementDTO != null){
                    %>
                        <input class="button" type="button" value="<fmt:message key='update'/>"
                               onclick="doAddAttributeValueElement();"/>
                    <%
                        } else {
                    %>
                        <input class="button" type="button" value="<fmt:message key='add'/>"
                               onclick="doAddAttributeValueElement();"/>
                    <%
                        }
                    %>
                    <input class="button" type="button" value="<fmt:message key='cancel'/>"
                           onclick="doCancel();"/>
                </td>
            </tr>
            </table>
            </div>
        </td>
    </tr>

    <tr>
        <td>
            <table class="styledLeft" id="attributeValueTables" style="width: 100%;margin-top:10px;">
                <thead>
                <tr>
                    <th><fmt:message key="attributeValue.element.id"/></th>
                    <th><fmt:message key="action"/></th>
                </tr>
                </thead>
                <body>
                <%
                if (attributeValueElementDTOList != null && attributeValueElementDTOList.size() > 0) {
                    for (AttributeValueElementDTO attributeValueElementDTO : attributeValueElementDTOList) {
                %>
                <tr>
                    <td>Attribute Value  <%=attributeValueElementDTO.getElementId()%></td>
                    <td>
                        <a href="#" onclick="editAttributeValueElement('<%=attributeValueElementDTO.getElementId()%>')"
                           class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message
                                key="edit"/></a>
                        <a href="#" onclick="deleteAttributeValueElement('<%=attributeValueElementDTO.getElementId()%>')"
                           class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message
                                key="delete"/></a>
                    </td>
                </tr>
                <%
                    }

                } else {
                %>
                <tr class="noRuleBox">
                    <td colspan="3"><fmt:message key="not.attribute.value.element.defined"/><br/>
                        <div style="clear:both"> </div>
                    </td>
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
         <table cellspacing="0" class="styledInner">
             <thead>
             <tr>
                 <th>
                     <a onclick="showHideRow(this)"
                        class="icon-link <%if(currentAttributeDesignatorDTO != null){%>arrowUp<%} else {%>arrowDown<% } %> " id="attributeDesignatorLink"><fmt:message key="attribute.designator.element"/></a>
                 </th>
             </tr>
             </thead>
         </table>
         <div id="attributeDesignatorLinkRow" style="margin-left:2px; <%if(currentAttributeDesignatorDTO == null) {%>display:none<% } %>">
            <table class="noBorders" id="attributeDesignatorElement" style="width: 100%">
            <tr>
                <td><fmt:message key="select.attribute.designator.type"/></td>
                <td>
                    <select id="designatorType" name="designatorType">
                <%
                    for (String designatorType : attributeDesignatorTypes) {
                        if(currentAttributeDesignatorDTO != null && designatorType.equals(currentAttributeDesignatorDTO.getElementName())) {
                %>
                            <option value="<%=designatorType%>" selected="selected"><%=currentAttributeDesignatorDTO.getElementName()%></option>
                <%
                        } else {
                %>
                            <option value="<%=designatorType%>"><%=designatorType%></option>
                <%
                        }
                    }
                %>
                    </select>
                </td>
                </tr>


                <tr>
                <td><fmt:message key="attribute.designator.data.type"/></td>
                <td>
                    <select id="dataType" name="dataType">
                <%
                if (entitlementPolicyBean.getDataTypes() != null) {
                    for (String attributeDataType : entitlementPolicyBean.getDataTypes()) {
                        if(currentAttributeDesignatorDTO != null && attributeDataType.equals(currentAttributeDesignatorDTO.getDataType())) {
                %>
                            <option value="<%=attributeDataType%>" selected="selected"><%=currentAttributeDesignatorDTO.getDataType()%></option>
                <%
                            } else {
                %>
                            <option value="<%=attributeDataType%>"><%=attributeDataType%></option>
                <%
                        }
                    }
                }
                %>
                    </select>
                </td>
                </tr>

                <tr>
                <td class="leftCol-small"><fmt:message key='attribute.id'/><font class="required">*</font></td>
                <%
                    if(currentAttributeDesignatorDTO != null && currentAttributeDesignatorDTO.getAttributeId() != null) {
                %>
                    <td><input type="text" name="attributeId" id="attributeId" class="text-box-big" value="<%=currentAttributeDesignatorDTO.getAttributeId()%>"/></td>
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
                    if(currentAttributeDesignatorDTO != null && currentAttributeDesignatorDTO.getIssuer() != null) {
                %>
                    <td><input id="issuerName" name="issuerName" class="text-box-big" type="text" value="<%=currentAttributeDesignatorDTO.getIssuer()%>"></td>
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
                    if (entitlementPolicyBean.getMustBePresentValues() != null) {
                        for (String mustPresent : entitlementPolicyBean.getMustBePresentValues()) {
                            if(currentAttributeDesignatorDTO != null && mustPresent.equals(currentAttributeDesignatorDTO.getMustBePresent())) {
                %>
                            <option value="<%=mustPresent%>" selected="selected"><%=currentAttributeDesignatorDTO.getMustBePresent()%></option>
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

                <%--<%--%>
                    <%--if(elementName != null && elementName.equals(EntitlementPolicyConstants.SUBJECT_ELEMENT)) {--%>
                <%--%>--%>

                    <%--<tr>--%>
                    <%--<td class="leftCol-small"><fmt:message key='subject.category'/></td>--%>

                    <%--<%--%>
                        <%--if(attributeDesignatorDTO != null && attributeDesignatorDTO.getSubjectCategory() != null) {--%>
                    <%--%>--%>
                        <%--<td><input id="subjectCategory" name="subjectCategory" class="text-box-big" type="text" value="<%=attributeDesignatorDTO.getSubjectCategory()%>"></td>--%>
                    <%--<%--%>
                        <%--} else {--%>
                    <%--%>--%>
                        <%--<td><input type="text" name="subjectCategory" id="subjectCategory" class="text-box-big"/></td>--%>

                    <%--<%--%>
                        <%--}--%>
                    <%--%>--%>
                    <%--</tr>--%>

                <%--<%--%>
                    <%--}--%>
                <%--%>--%>
            <tr>
                <td colspan="2" class="buttonRow">
                    <%
                        if(currentAttributeDesignatorDTO != null){
                    %>
                        <input class="button" type="button" value="<fmt:message key='update'/>"
                               onclick="doAddAttributeDesignatorElement();"/>
                    <%
                        } else {
                    %>
                        <input class="button" type="button" value="<fmt:message key='add'/>"
                               onclick="doAddAttributeDesignatorElement();"/>
                    <%
                        }
                    %>
                    <input class="button" type="button" value="<fmt:message key='cancel'/>"
                           onclick="doCancel();"/>
                </td>
            </tr>
            </table>
             </div>
        </td>
    </tr>
    <tr>
        <td>
            <table class="styledLeft" id="attributeDesignatorTable" style="width: 100%;margin-top:10px;">
                <thead>
                <tr>
                    <th><fmt:message key="attribute.designator.element.id"/></th>
                    <th><fmt:message key="action"/></th>
                </tr>
                </thead>
                <body>
                <%
                if (attributeDesignatorDTOList != null && attributeDesignatorDTOList.size() > 0) {
                    for (AttributeDesignatorDTO attributeDesignatorDTO : attributeDesignatorDTOList) {
                %>
                <tr>
                    <td><fmt:message key="attribute.designator.element"/>  <%=attributeDesignatorDTO.getElementId()%></td>
                    <td>
                        <a href="#" onclick="editAttributeDesignatorElement('<%=attributeDesignatorDTO.getElementId()%>')"
                           class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message
                                key="edit"/></a>
                        <a href="#" onclick="deleteAttributeDesignatorElement('<%=attributeDesignatorDTO.getElementId()%>')"
                           class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message
                                key="delete"/></a>
                    </td>
                </tr>
                <%
                    }

                } else {
                %>
                <tr class="noRuleBox">
                    <td colspan="3"><fmt:message key="not.attribute.designator.element.defined"/><br/>
                        <div style="clear:both"> </div>
                    </td>
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
         <table cellspacing="0" class="styledInner">
             <thead>
             <tr>
                 <th>
                     <a onclick="showHideRow(this)"
                        class="icon-link <%if(currentAttributeSelectorDTO != null){%>arrowUp<%} else {%>arrowDown<% } %> " id="attributeSelectorLink"><fmt:message key="attribute.selector.element"/></a>
                 </th>
             </tr>
             </thead>
         </table>
         <div id="attributeSelectorLinkRow" style="margin-left:2px; <%if(currentAttributeSelectorDTO == null) {%>display:none<% } %>">

            <table class="noBorders" id="attributeSelectorElement" style="width: 100%">
            <tr>
                <td><fmt:message key="attribute.selector.data.type"/></td>
                <td>
                    <select id="attributeSelectorDataType" name="attributeSelectorDataType">
                <%
                if (entitlementPolicyBean.getDataTypes() != null) {
                    for (String attributeSelectorData : entitlementPolicyBean.getDataTypes()) {
                        if(currentAttributeSelectorDTO != null && attributeSelectorData.equals(currentAttributeSelectorDTO.getAttributeSelectorDataType())) {
                %>
                        <option value="<%=attributeSelectorData%>" selected="selected"><%=currentAttributeSelectorDTO.getAttributeSelectorDataType()%></option>

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
                    if(currentAttributeSelectorDTO != null && currentAttributeSelectorDTO.getAttributeSelectorRequestContextPath() != null) {
                %>
                    <td><input id="attributeSelectorRequestContextPath" name="attributeSelectorRequestContextPath" class="text-box-big" type="text" value="<%=currentAttributeSelectorDTO.getAttributeSelectorRequestContextPath()%>"></td>
                <%
                    } else {
                %>
                    <td><input type="text" name="attributeSelectorRequestContextPath" id="attributeSelectorRequestContextPath" class="text-box-big"/></td>
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
                if (entitlementPolicyBean.getMustBePresentValues() != null) {
                    for (String mustPresent : entitlementPolicyBean.getMustBePresentValues()) {
                        if(currentAttributeSelectorDTO != null && mustPresent.equals(currentAttributeSelectorDTO.getAttributeSelectorMustBePresent())) {
                %>
                        <option value="<%=mustPresent%>" selected="selected"><%=currentAttributeSelectorDTO.getAttributeSelectorMustBePresent()%></option>
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

            <tr>
                <td colspan="2" class="buttonRow">
                    <%
                        if(currentAttributeSelectorDTO != null){
                    %>
                        <input class="button" type="button" value="<fmt:message key='update'/>"
                               onclick="doAddAttributeSelectorElement();"/>
                    <%
                        } else {
                    %>
                        <input class="button" type="button" value="<fmt:message key='add'/>"
                               onclick="doAddAttributeSelectorElement();"/>
                    <%
                        }
                    %>

                    <input class="button" type="button" value="<fmt:message key='cancel'/>"
                           onclick="doCancel();"/>
                </td>
            </tr>
            </table>
            </div>
        </td>
    </tr>
    <tr>
        <td>
            <table class="styledLeft" id="dataTable" style="width: 100%;margin-top:10px;">
                <thead>
                <tr>
                    <th><fmt:message key="attribute.selector.element.id"/></th>
                    <th><fmt:message key="action"/></th>
                </tr>
                </thead>
                <body>
                <%
                if (attributeSelectorDTOList != null && attributeSelectorDTOList.size() > 0) {
                    for (AttributeSelectorDTO attributeSelectorDTO : attributeSelectorDTOList) {
                %>
                <tr>
                    <td><fmt:message key="attribute.selector.element"/>  <%=attributeSelectorDTO.getElementNumber()%></td>
                    <td>
                        <a href="#" onclick="editAttributeSelectorSElement('<%=attributeSelectorDTO.getElementNumber()%>')"
                           class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message
                                key="edit"/></a>
                        <a href="#" onclick="deleteAttributeSelectorElement('<%=attributeSelectorDTO.getElementNumber()%>')"
                           class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message
                                key="delete"/></a>
                    </td>
                </tr>
                <%
                    }

                } else {
                %>
                <tr class="noRuleBox">
                    <td colspan="3"><fmt:message key="not.attribute.selector.element.defined"/> <br/>
                        <div style="clear:both"> </div>
                    </td>
                </tr>
                <%
                }
                %>
                </body>
            </table>
        </td>
    </tr>
    </tbody>
</table>
<table class="styledLeft" style="margin-top:20px;">
    <tbody>
    <tr>
        <td class="buttonRow" colspan="2">
            <%
                if(entitlementPolicyBean.isEditPolicy()) {
            %>
                <input type="button" onclick="submitForm();" value="<fmt:message key="update"/>"  class="button"/>
            <%
                } else {
            %>
                <input type="button" onclick="submitForm();" value="<fmt:message key="add"/>"  class="button"/>
            <%
                }   
            %>
            <input type="button" onclick="doCancelForm();" value="<fmt:message key="cancel" />" class="button"/>
        </td>
    </tr>
    <tbody>
</table>    
</form>

</div>
</div>
</fmt:bundle>
