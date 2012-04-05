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
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.AttributeValueElementDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.ApplyElementDTO" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.AttributeDesignatorDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.AttributeSelectorDTO" %>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />

<%

    String nextPage =  CharacterEncoder.getSafeText(request.getParameter("nextPage"));
    String applyElementId = CharacterEncoder.getSafeText(request.getParameter("applyElementId"));
    String completeApply = CharacterEncoder.getSafeText(request.getParameter("completeApply"));
    String designatorType = CharacterEncoder.getSafeText(request.getParameter("designatorType"));
    String attributeSelectorRequestContextPath = CharacterEncoder.
            getSafeText(request.getParameter("attributeSelectorRequestContextPath"));
    String attributeSelectorDataType = CharacterEncoder.
            getSafeText( request.getParameter("attributeSelectorDataType"));
    String attributeSelectorMustBePresent = CharacterEncoder.
            getSafeText(request.getParameter("attributeSelectorMustBePresent"));
    String attributeId = CharacterEncoder.getSafeText(request.getParameter("attributeId"));
    String dataType = CharacterEncoder.getSafeText(request.getParameter("dataType"));
    String issuerName = CharacterEncoder.getSafeText( request.getParameter("issuerName"));
    String attributeDesignatorMustPresent = CharacterEncoder.
            getSafeText(request.getParameter("attributeDesignatorMustPresent"));
    String subjectCategory = CharacterEncoder.getSafeText(request.getParameter("subjectCategory"));
    String variableId = CharacterEncoder.getSafeText(request.getParameter("variableId"));
    String functionFunctionId = CharacterEncoder.getSafeText(request.getParameter("functionFunctionId"));
    String functionId = CharacterEncoder.getSafeText(request.getParameter("functionId"));
    String attributeDataType = CharacterEncoder.getSafeText(request.
            getParameter("attributeDataType"));
    String attributeValue = CharacterEncoder.getSafeText(request.getParameter("attributeValue"));
    String attributeValueElementId = CharacterEncoder.
            getSafeText(request.getParameter("attributeValueElementId"));

    String attributeDesignatorElementId = CharacterEncoder.
            getSafeText(request.getParameter("attributeDesignatorElementId"));
    String attributeSelectorElementId = CharacterEncoder.
            getSafeText(request.getParameter("attributeSelectorElementId"));    

    if(attributeValue != null && !attributeValue.trim().equals("") && attributeDataType != null &&
            !attributeDataType.trim().equals("")) {

        int attributeValueElementIdInteger;
        if(attributeValueElementId != null &&
        !attributeValueElementId.equals("") && !attributeValueElementId.trim().equals("null")){
            attributeValueElementIdInteger= Integer.parseInt(attributeValueElementId);
        } else {
            attributeValueElementIdInteger = entitlementPolicyBean.getAttributeValueElementNumber();
        }
        AttributeValueElementDTO attributeValueElementDTO = new AttributeValueElementDTO();
        entitlementPolicyBean.removeAttributeValueElement(attributeValueElementIdInteger);
        attributeValueElementDTO.setElementId(attributeValueElementIdInteger);
        attributeValueElementDTO.setApplyElementId(applyElementId);
        attributeValueElementDTO.setAttributeDataType(attributeDataType);
        attributeValueElementDTO.setAttributeValue(attributeValue);
        entitlementPolicyBean.setAttributeValueElementDTOs(attributeValueElementDTO);
    }


    if(attributeId != null && !attributeId.trim().equals("") && dataType != null &&
            !dataType.trim().equals("") && designatorType != null && !designatorType.trim().equals("") &&
            attributeDesignatorElementId != null && !attributeDesignatorElementId.trim().equals("")) {


        int attributeDesignatorIdInteger;
        if(attributeDesignatorElementId != null &&
        !attributeDesignatorElementId.trim().equals("") && !attributeDesignatorElementId.trim().equals("null")){
            attributeDesignatorIdInteger= Integer.parseInt(attributeDesignatorElementId);
        } else {
            attributeDesignatorIdInteger = entitlementPolicyBean.getAttributeDesignatorElementNumber();
        }

        AttributeDesignatorDTO attributeDesignatorDTO =  new AttributeDesignatorDTO();
        entitlementPolicyBean.removeAttributeDesignatorElement(attributeDesignatorIdInteger);
        attributeDesignatorDTO.setElementId(attributeDesignatorIdInteger);
        attributeDesignatorDTO.setApplyElementId(applyElementId);
        attributeDesignatorDTO.setAttributeId(attributeId);
        attributeDesignatorDTO.setDataType(dataType);
        attributeDesignatorDTO.setElementName(designatorType);
        attributeDesignatorDTO.setIssuer(issuerName);
        if(!EntitlementPolicyConstants.COMBO_BOX_DEFAULT_VALUE.equals(attributeDesignatorMustPresent)){
            attributeDesignatorDTO.setMustBePresent(attributeDesignatorMustPresent);            
        }
        if(designatorType != null && designatorType.equals(EntitlementPolicyConstants.SUBJECT_ELEMENT)) {
            attributeDesignatorDTO.setSubjectCategory(subjectCategory);
        }
        entitlementPolicyBean.setAttributeDesignatorDTOs(attributeDesignatorDTO);
    }

    if(attributeSelectorRequestContextPath != null &&
            !attributeSelectorRequestContextPath.trim().equals("") && attributeSelectorDataType != null &&
            !attributeSelectorDataType.trim().equals("")) {

        int attributeSelectorIdInteger;
        if(attributeSelectorElementId != null &&
        !attributeSelectorElementId.trim().equals("") && !attributeSelectorElementId.trim().equals("null")){
            attributeSelectorIdInteger= Integer.parseInt(attributeSelectorElementId);
        } else {
            attributeSelectorIdInteger = entitlementPolicyBean.getAttributeSelectorElementNumber();
        }

        AttributeSelectorDTO attributeSelectorDTO = new AttributeSelectorDTO();
        entitlementPolicyBean.removeAttributeSelectorElement(attributeSelectorIdInteger);
        attributeSelectorDTO.setElementNumber(attributeSelectorIdInteger);
        attributeSelectorDTO.setApplyElementId(applyElementId);
        attributeSelectorDTO.setAttributeSelectorRequestContextPath(attributeSelectorRequestContextPath);
        attributeSelectorDTO.setAttributeSelectorDataType(attributeSelectorDataType);
        if(!EntitlementPolicyConstants.COMBO_BOX_DEFAULT_VALUE.equals(attributeSelectorMustBePresent)){
            attributeSelectorDTO.setAttributeSelectorMustBePresent(attributeSelectorMustBePresent);
        }
        entitlementPolicyBean.setAttributeSelectorDTOs(attributeSelectorDTO);
    }

    if(functionId != null && !functionId.equals("null") & !functionId.equals("")) {

        entitlementPolicyBean.functionIdMap.put(applyElementId, functionId);
        entitlementPolicyBean.functionIdElementValueMap.put(applyElementId, functionFunctionId);
        
        if("true".equals(completeApply)){

            ApplyElementDTO applyElement = new ApplyElementDTO();

            List<AttributeValueElementDTO> attributeValueElementDTOs  = entitlementPolicyBean.
                    getAttributeValueElementDTOs();
            for(AttributeValueElementDTO attributeValueElementDTO : attributeValueElementDTOs) {
                if(attributeValueElementDTO.getApplyElementId().equals(applyElementId)){
                    applyElement.setAttributeValueElementDTO(attributeValueElementDTO);
                }
            }

            List<AttributeDesignatorDTO> attributeDesignatorDTOs  = entitlementPolicyBean.
                    getAttributeDesignatorDTOs();
            for(AttributeDesignatorDTO attributeDesignatorDTO : attributeDesignatorDTOs) {
                if(attributeDesignatorDTO.getApplyElementId().equals(applyElementId)){
                    applyElement.setAttributeDesignators(attributeDesignatorDTO);
                }
            }

            List<AttributeSelectorDTO> attributeSelectorDTOs   = entitlementPolicyBean.
                    getAttributeSelectorDTOs();
            for(AttributeSelectorDTO attributeSelectorDTO  : attributeSelectorDTOs) {
                if(attributeSelectorDTO.getApplyElementId().equals(applyElementId)){
                    applyElement.setAttributeSelectors(attributeSelectorDTO);
                }
            }

            List<ApplyElementDTO> applyElementDTOs  = entitlementPolicyBean.getApplyElementDTOs(applyElementId);
            for(ApplyElementDTO applyElementDTO : applyElementDTOs) {
                if(!applyElementDTO.getApplyElementId().equals(applyElementId)) {
                    applyElement.setApplyElement(applyElementDTO);
                }
            }

            entitlementPolicyBean.removeAttributeValueElementByApplyElementNumber(applyElementId);
            entitlementPolicyBean.removeApplyElementDTOByApplyElementNumber(applyElementId);
            entitlementPolicyBean.removeAttributeDesignatorElementByApplyElementNumber(applyElementId);
            if(!EntitlementPolicyConstants.COMBO_BOX_DEFAULT_VALUE.equals(functionFunctionId)){
                applyElement.setFunctionFunctionId(functionFunctionId);
            }
            applyElement.setApplyElementId(applyElementId);
            applyElement.setFunctionId(functionId);
            entitlementPolicyBean.setApplyElementDTO(applyElement);

            if(!applyElementId.equals("/1")){
                String[] pageNumberArray = applyElementId.split("/");
                String currentPageNumberString = pageNumberArray[pageNumberArray.length - 1];
                applyElementId = applyElementId.substring(0, applyElementId.indexOf(currentPageNumberString)-1);
            } else {
                nextPage = "add-condition";
            }
        }

    }

    if("false".equals(completeApply)){
        String[] pageNumberArray = applyElementId.split("/");
        String currentPageNumberString = pageNumberArray[pageNumberArray.length - 1];
        if(currentPageNumberString != null && !currentPageNumberString.equals("")){
            applyElementId = applyElementId + "/" + entitlementPolicyBean.getApplyElementNumber();
        }
    }
      
    String forwardTo;
    
    if(nextPage != null && !nextPage.equals("")){
        forwardTo = nextPage + ".jsp?applyElementId=" + applyElementId;
    } else {
        forwardTo =  "add-apply.jsp?";
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