/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.entitlement.ui.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants;
import org.wso2.carbon.identity.entitlement.ui.PolicyEditorConstants;
import org.wso2.carbon.identity.entitlement.ui.PolicyEditorException;
import org.wso2.carbon.identity.entitlement.ui.dto.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class PolicyEditorUtil {

    public static void createTestPolicy(List<RuleDTO>  ruleDTOs,  TargetDTO targetDTO){

    }


    public static Element createRule(RuleDTO ruleDTO, Document doc) throws PolicyEditorException {

        RuleElementDTO ruleElementDTO = new RuleElementDTO();

        ruleElementDTO.setRuleId(ruleDTO.getRuleId());
        ruleElementDTO.setRuleEffect(ruleDTO.getRuleEffect());
        ConditionElementDT0 conditionElementDT0 = createConditionDTO(ruleDTO.getRowDTOList());
        NewTargetElementDTO targetElementDTO = createTargetDTO(ruleDTO.getTargetDTO());

        if(conditionElementDT0 != null){
            ruleElementDTO.setConditionElementDT0(conditionElementDT0);            
        }

        if(targetElementDTO != null){
            ruleElementDTO.setNewTargetElementDTO(targetElementDTO);
        }

        return PolicyCreatorUtil.createRuleElement(ruleElementDTO, doc);
    }


    public static Element createTarget(TargetDTO targetDTO, Document doc) throws PolicyEditorException {

        Element targetElement = null;

        NewTargetElementDTO targetElementDTO = createTargetDTO(targetDTO);

        if(targetElementDTO != null){
            targetElement = createTargetElement(targetElementDTO, doc);
        }

        return targetElement;
    }


    public static ConditionElementDT0 createConditionDTO(List<RowDTO> rowDTOs) throws PolicyEditorException {

        ConditionElementDT0 rootApplyDTO = new ConditionElementDT0();

        ArrayList<RowDTO> temp = new ArrayList<RowDTO>();
        Set<ArrayList<RowDTO>> listSet = new HashSet <ArrayList<RowDTO>>();

        for(int i = 0; i < rowDTOs.size(); i ++){

            if(i == 0){
                temp.add(rowDTOs.get(0));
                continue;
            }

            String combineFunction = rowDTOs.get(i-1).getCombineFunction();

            if(PolicyEditorConstants.COMBINE_FUNCTION_AND.equals(combineFunction)){
                temp.add(rowDTOs.get(i));
            }

            if(PolicyEditorConstants.COMBINE_FUNCTION_OR.equals(combineFunction)){
                listSet.add(temp);
                temp = new ArrayList<RowDTO>();
                temp.add(rowDTOs.get(i)) ;
            }
        }

        listSet.add(temp);

        if(listSet.size() > 1){
            for(ArrayList<RowDTO> rowDTOArrayList : listSet){
                ApplyElementDTO orApplyDTO = new ApplyElementDTO();
                orApplyDTO.setFunctionId(processFunction("or"));

                if(rowDTOArrayList.size() > 1){
                     ApplyElementDTO andApplyDTO = new ApplyElementDTO();
                     andApplyDTO.setFunctionId(processFunction("and"));
                    for(RowDTO rowDTO : rowDTOArrayList){
                        ApplyElementDTO applyElementDTO = createApplyElement(rowDTO);
                        andApplyDTO.setApplyElement(applyElementDTO);
                    }
                    orApplyDTO.setApplyElement(andApplyDTO);
                } else if (rowDTOArrayList.size() == 1) {
                    RowDTO rowDTO = rowDTOArrayList.get(0);
                    ApplyElementDTO andApplyDTO = createApplyElement(rowDTO);
                    orApplyDTO.setApplyElement(andApplyDTO);
                }
            }
        } else if(listSet.size() == 1) {
            ArrayList<RowDTO> rowDTOArrayList = listSet.iterator().next();
            if(rowDTOArrayList.size() > 1){
                 ApplyElementDTO andApplyDTO = new ApplyElementDTO();
                 andApplyDTO.setFunctionId(processFunction("and"));
                for(RowDTO rowDTO : rowDTOArrayList){
                    ApplyElementDTO applyElementDTO = createApplyElement(rowDTO);
                    andApplyDTO.setApplyElement(applyElementDTO);
                }
            } else if (rowDTOArrayList.size() == 1) {
                RowDTO rowDTO = rowDTOArrayList.get(0);
                ApplyElementDTO andApplyDTO = createApplyElement(rowDTO);
                rootApplyDTO.setApplyElement(andApplyDTO);                                                                            }
        }


        for(RowDTO rowDTO : rowDTOs){
            if(rowDTO.getFunction().contains("<" ) || rowDTO.getFunction().contains(">" )){
                processGreaterLessThanFunctions(rowDTO);
            } else if(rowDTO.getFunction().equals("=")){
                processEqualFunctions(rowDTO);
            }
        }

        return rootApplyDTO;
    }


    public static ApplyElementDTO createApplyElement(RowDTO rowDTO) throws PolicyEditorException {

        ApplyElementDTO applyElementDTO;
        if(rowDTO.getFunction().contains("<" ) || rowDTO.getFunction().contains(">" )){
            applyElementDTO = processGreaterLessThanFunctions(rowDTO);
        } else if(rowDTO.getFunction().equals("=")){
            applyElementDTO = processEqualFunctions(rowDTO);
        } else {
            throw new PolicyEditorException("");
        }

        return applyElementDTO;
    }


    public static NewTargetElementDTO createTargetDTO(TargetDTO targetDTO) {

        AllOfElementDTO allOfElementDTO = new AllOfElementDTO();
        AnyOfElementDTO anyOfElementDTO = new AnyOfElementDTO();
        NewTargetElementDTO targetElementDTO = new NewTargetElementDTO();

        ArrayList<RowDTO> rowDTOs = targetDTO.getRowDTOList();
        ArrayList<RowDTO> tempRowDTOs = new ArrayList<RowDTO>();

        // pre function processing
        for(RowDTO rowDTO : rowDTOs){
            if(PolicyEditorConstants.PRE_FUNCTION_ARE.equals(rowDTO.getPreFunction())){
                String[] attributeValues = rowDTO.getAttributeValue().split(",");
                allOfElementDTO =  new AllOfElementDTO();
                for(int j = 0; j < attributeValues.length; j ++){
                    RowDTO newDto = new RowDTO(rowDTO);
                    newDto.setAttributeValue(attributeValues[j]);
                    if(j != attributeValues.length - 1){
                        newDto.setCombineFunction(PolicyEditorConstants.COMBINE_FUNCTION_AND);
                    }
                    tempRowDTOs.add(newDto);
                }
            } else {
                tempRowDTOs.add(rowDTO);
            }
        }

        for(int i = 0; i < tempRowDTOs.size(); i ++){
            if(i == 0){
                MatchElementDTO matchElementDTO = createTargetMatch(tempRowDTOs.get(0));
                if(matchElementDTO != null){
                    allOfElementDTO.addMatchElementDTO(matchElementDTO);
                }
                continue;
            }

            String combineFunction = tempRowDTOs.get(i-1).getCombineFunction();

            if(PolicyEditorConstants.COMBINE_FUNCTION_AND.equals(combineFunction)){
                MatchElementDTO matchElementDTO = createTargetMatch(tempRowDTOs.get(i));
                if(matchElementDTO != null){
                    allOfElementDTO.addMatchElementDTO(matchElementDTO);
                }

            }

            if(PolicyEditorConstants.COMBINE_FUNCTION_OR.equals(combineFunction)){
                anyOfElementDTO.addAllOfElementDTO(allOfElementDTO);
                allOfElementDTO =  new AllOfElementDTO();
                MatchElementDTO matchElementDTO = createTargetMatch(tempRowDTOs.get(i));
                if(matchElementDTO != null){
                    allOfElementDTO.addMatchElementDTO(matchElementDTO);
                }
            }
        }
        anyOfElementDTO.addAllOfElementDTO(allOfElementDTO);
        targetElementDTO.addAnyOfElementDTO(anyOfElementDTO);

        return targetElementDTO;
    }


    public static Element createTargetElement(NewTargetElementDTO targetDTO, Document doc) {

        Element targetElement = doc.createElement(PolicyEditorConstants.TARGET_ELEMENT);
        List<AnyOfElementDTO> anyOfElementDTOs = targetDTO.getAnyOfElementDTOs();

        for(AnyOfElementDTO anyOfElementDTO : anyOfElementDTOs){
            Element anyOfElement = doc.createElement(PolicyEditorConstants.ANY_OF_ELEMENT);
            List<AllOfElementDTO> allOfElementDTOs = anyOfElementDTO.getAllOfElementDTOs();

            for(AllOfElementDTO allOfElementDTO : allOfElementDTOs){
                Element allOfElement = doc.createElement(PolicyEditorConstants.ALL_OF_ELEMENT);
                List<MatchElementDTO> matchElementDTOs =  allOfElementDTO.getMatchElementDTOs();

                for(MatchElementDTO matchElementDTO : matchElementDTOs){
                    Element matchElement = PolicyCreatorUtil.createMatchElement(matchElementDTO, doc);

                    allOfElement.appendChild(matchElement);
                }

                anyOfElement.appendChild(allOfElement);
            }

            targetElement.appendChild(anyOfElement);
        }

        return targetElement;

    }

    public static Element processArithmeticFunctions(RowDTO rowDTO) throws PolicyEditorException {
        return null;
    }

    public static ApplyElementDTO processEqualFunctions(RowDTO rowDTO) {

        
        return null;


    }



    public static ApplyElementDTO processGreaterLessThanFunctions(RowDTO rowDTO)
                                                                    throws PolicyEditorException {
        //  <= X <=
        //  < X <
        //  <= X <
        //  < X <=
        //  < X
        //  <= X
        String function =  rowDTO.getFunction();
        String dataType = rowDTO.getAttributeDataType();
        String attributeValue = rowDTO.getAttributeValue();
        String leftValue;
        String rightValue;

        String[] values = attributeValue.split(PolicyEditorConstants.ATTRIBUTE_SEPARATOR);
        if(values.length == 2){
            leftValue = values[0];
            rightValue = values[1];
        } else {
            throw new PolicyEditorException("Can not create Apply element:" +
                    " Required Attribute value is missing");
        }

        AttributeDesignatorDTO designatorDTO = new AttributeDesignatorDTO();
        designatorDTO.setCategory(rowDTO.getCategory());
        designatorDTO.setAttributeId(rowDTO.getAttributeId());
        designatorDTO.setDataType(dataType);
        designatorDTO.setMustBePresent("true");

        if(function == null){
            throw new PolicyEditorException("Can not create Apply element:" +
                    " Required Function Id is missing");
        }

        if(PolicyEditorConstants.FUNCTION_GREATER_EQUAL_AND_LESS_EQUAL.equals(function)){

            ApplyElementDTO andApplyElement = new ApplyElementDTO();

            andApplyElement.setFunctionId(processFunction("and", dataType));

            ApplyElementDTO greaterThanApplyElement = new ApplyElementDTO();
            greaterThanApplyElement.setFunctionId(processFunction("greater-than-or-equal", dataType));

            ApplyElementDTO lessThanApplyElement = new ApplyElementDTO();
            lessThanApplyElement.setFunctionId(processFunction("less-than-or-equal", dataType));

            ApplyElementDTO oneAndOnlyApplyElement = new ApplyElementDTO();
            oneAndOnlyApplyElement.setFunctionId(processFunction("one-and-only", dataType));
            oneAndOnlyApplyElement.setAttributeDesignators(designatorDTO);

            AttributeValueElementDTO leftValueElementDTO = new AttributeValueElementDTO();
            leftValueElementDTO.setAttributeDataType(dataType);
            leftValueElementDTO.setAttributeValue(leftValue);

            AttributeValueElementDTO rightValueElementDTO = new AttributeValueElementDTO();
            rightValueElementDTO.setAttributeDataType(dataType);
            rightValueElementDTO.setAttributeValue(rightValue);

            greaterThanApplyElement.setApplyElement(oneAndOnlyApplyElement);
            greaterThanApplyElement.setAttributeValueElementDTO(leftValueElementDTO);

            lessThanApplyElement.setApplyElement(oneAndOnlyApplyElement);
            lessThanApplyElement.setAttributeValueElementDTO(rightValueElementDTO);

            andApplyElement.setApplyElement(greaterThanApplyElement);
            andApplyElement.setApplyElement(lessThanApplyElement);

            return andApplyElement;
        }

        return null;

    }

    private static String processFunction(String function, String type, int version){
        return  "urn:oasis:names:tc:xacml:" + version + ":function:" + getDataTypePrefix(type) +
                                                                                "-" + function;
    }

    private static String processFunction(String function){
        return "urn:oasis:names:tc:xacml:1.0:function:" + function;
    }

    private static String processFunction(String function, String type){
        return  "urn:oasis:names:tc:xacml:1.0:function:" + getDataTypePrefix(type) + "-" + function;
    }


    private static String getDataTypePrefix(String dataTypeUri){

        if(dataTypeUri != null){
            if(dataTypeUri.contains("#")){
                return dataTypeUri.substring(dataTypeUri.indexOf("#") + 1);
            } else if(dataTypeUri.contains(":")){
                String[] stringArray = dataTypeUri.split(":");
                if(stringArray != null && stringArray.length > 0){
                    return stringArray[stringArray.length];
                }
            }
        }
        return dataTypeUri;
    }

    
    /////////////////////////////////////// copied for other classes//////////////////////////////////


    public static MatchElementDTO createTargetMatch(RowDTO rowDTO) {


        String category = rowDTO.getCategory();
        String functionId = rowDTO.getFunction();
        String attributeValue = rowDTO.getAttributeValue();
        String attributeId = rowDTO.getAttributeId();
        String dataType = rowDTO.getAttributeDataType();
        MatchElementDTO matchElementDTO;

        if (functionId != null && functionId.trim().length() > 0 && attributeValue != null &&
                attributeValue.trim().length() > 0 && category != null &&
                category.trim().length() > 0 && attributeId != null &&
                attributeId.trim().length() > 0 && dataType != null &&
                dataType.trim().length() > 0) {

            functionId = processFunction(functionId, dataType);

            matchElementDTO = new MatchElementDTO();

            AttributeValueElementDTO attributeValueElementDTO = new AttributeValueElementDTO();
            attributeValueElementDTO.setAttributeDataType(dataType);
            attributeValueElementDTO.setAttributeValue(attributeValue.trim());

            AttributeDesignatorDTO attributeDesignatorDTO = new AttributeDesignatorDTO();
            attributeDesignatorDTO.setDataType(dataType);
            attributeDesignatorDTO.setAttributeId(attributeId);
            attributeDesignatorDTO.setElementName(category);

            matchElementDTO.setMatchElementName(category);
            matchElementDTO.setMatchId(functionId);
            matchElementDTO.setAttributeValueElementDTO(attributeValueElementDTO);
            matchElementDTO.setAttributeDesignatorDTO(attributeDesignatorDTO);
        } else {
            return null; // TODO
        }

        return matchElementDTO;
    }


    /**
     * This method creates a match element (such as subject,action,resource or environment) of the XACML policy
     * @param matchElementDTO match element data object
     * @param doc XML document
     * @return match Element
     * @throws PolicyEditorException if any error occurs
     */
    public static Element createMatchElement(MatchElementDTO matchElementDTO, Document doc)
                                                                    throws PolicyEditorException {

        Element matchElement;

        if(matchElementDTO.getMatchId() != null && matchElementDTO.getMatchId().trim().length() > 0) {

            matchElement = doc.createElement(PolicyEditorConstants.MATCH_ELEMENT);

            matchElement.setAttribute(PolicyEditorConstants.MATCH_ID,
                    matchElementDTO.getMatchId());

            if(matchElementDTO.getAttributeValueElementDTO() != null) {
                Element attributeValueElement = createAttributeValueElement(matchElementDTO.
                        getAttributeValueElementDTO(), doc);
                matchElement.appendChild(attributeValueElement);
            }

            if(matchElementDTO.getAttributeDesignatorDTO() != null ) {
                Element attributeDesignatorElement = createAttributeDesignatorElement(matchElementDTO.
                        getAttributeDesignatorDTO(), doc);
                matchElement.appendChild(attributeDesignatorElement);
            } else if(matchElementDTO.getAttributeSelectorDTO() != null ) {
                Element attributeSelectorElement = createAttributeSelectorElement(matchElementDTO.
                        getAttributeSelectorDTO(), doc);
                matchElement.appendChild(attributeSelectorElement);
            }
        } else {
            throw new PolicyEditorException("Can not create Match element:" +
                    " Required Attributes are missing");
        }
        return matchElement;
    }

    /**
     * This method creates attribute value DOM element
     * @param attributeValueElementDTO attribute value element data object
     * @param doc XML document
     * @return attribute value element as DOM
     */
    public static Element createAttributeValueElement(AttributeValueElementDTO
            attributeValueElementDTO, Document doc) {

        Element attributeValueElement = doc.createElement(EntitlementPolicyConstants.ATTRIBUTE_VALUE);

        if(attributeValueElementDTO.getAttributeValue() != null && attributeValueElementDTO.
                getAttributeValue().trim().length() > 0) {

            attributeValueElement.setTextContent(attributeValueElementDTO.getAttributeValue().trim());

            if(attributeValueElementDTO.getAttributeDataType()!= null && attributeValueElementDTO.
                    getAttributeDataType().trim().length() > 0){
                attributeValueElement.setAttribute(EntitlementPolicyConstants.DATA_TYPE,
                                attributeValueElementDTO.getAttributeDataType());
            } else {
                attributeValueElement.setAttribute(EntitlementPolicyConstants.DATA_TYPE,
                                EntitlementPolicyConstants.STRING_DATA_TYPE);
            }

        }

        return attributeValueElement;
    }

    /**
     * This method creates attribute designator DOM element
     * @param attributeDesignatorDTO  attribute designator data object
     * @param doc  XML document
     * @return attribute designator element as DOM
     * @throws PolicyEditorException throws if missing required data
     */
    public static Element createAttributeDesignatorElement(AttributeDesignatorDTO
            attributeDesignatorDTO, Document doc) throws PolicyEditorException {

        Element attributeDesignatorElement;

        if(attributeDesignatorDTO != null && doc != null){

            String category = attributeDesignatorDTO.getCategory();
            String attributeId = attributeDesignatorDTO.getAttributeId();
            String dataType = attributeDesignatorDTO.getDataType();
            String mustBe = attributeDesignatorDTO.getMustBePresent();

            if(category != null && category.trim().length() > 0 && attributeId != null &&
                    attributeId.trim().length() > 0 && dataType != null && dataType.trim().length() > 0 &&
                    mustBe != null && mustBe.trim().length() > 0){

                attributeDesignatorElement = doc.
                        createElement(PolicyEditorConstants.ATTRIBUTE_DESIGNATOR);

                attributeDesignatorElement.setAttribute(PolicyEditorConstants.ATTRIBUTE_ID,
                        attributeId);

                attributeDesignatorElement.setAttribute(PolicyEditorConstants.CATEGORY, category);

                attributeDesignatorElement.setAttribute(PolicyEditorConstants.DATA_TYPE, dataType);

                attributeDesignatorElement.setAttribute(PolicyEditorConstants.MUST_BE_PRESENT, mustBe);

                if(attributeDesignatorDTO.getIssuer() != null && attributeDesignatorDTO.getIssuer().
                        trim().length() > 0) {
                    attributeDesignatorElement.setAttribute(EntitlementPolicyConstants.ISSUER,
                            attributeDesignatorDTO.getIssuer());
                }
            } else {
                throw new PolicyEditorException("Can not create AttributeDesignator element:" +
                        " Required Attributes are missing");
            }
        } else {
            throw new PolicyEditorException("Can not create AttributeDesignator element:" +
                    " A Null object is received");
        }
        return attributeDesignatorElement;
    }

    /**
     * This method creates attribute selector DOM element
     * @param attributeSelectorDTO  attribute selector data object
     * @param doc xML document
     * @return attribute selector element as DOM
     */
    public static Element createAttributeSelectorElement(AttributeSelectorDTO attributeSelectorDTO,
                                                         Document doc)  {

        Element attributeSelectorElement = doc.createElement(EntitlementPolicyConstants.
                ATTRIBUTE_SELECTOR);

        if(attributeSelectorDTO.getAttributeSelectorRequestContextPath() != null &&
                attributeSelectorDTO.getAttributeSelectorRequestContextPath().trim().length() > 0) {

            attributeSelectorElement.setAttribute(EntitlementPolicyConstants.REQUEST_CONTEXT_PATH,
                    EntitlementPolicyConstants.ATTRIBUTE_NAMESPACE + attributeSelectorDTO.
                            getAttributeSelectorRequestContextPath());

            if(attributeSelectorDTO.getAttributeSelectorDataType() != null &&
                    attributeSelectorDTO.getAttributeSelectorDataType().trim().length() > 0) {
                attributeSelectorElement.setAttribute(EntitlementPolicyConstants.DATA_TYPE,
                        attributeSelectorDTO.getAttributeSelectorDataType());
            } else {
                attributeSelectorElement.setAttribute(EntitlementPolicyConstants.DATA_TYPE,
                        EntitlementPolicyConstants.STRING_DATA_TYPE);
            }

            if(attributeSelectorDTO.getAttributeSelectorMustBePresent() != null &&
                    attributeSelectorDTO.getAttributeSelectorMustBePresent().trim().length() > 0) {
                attributeSelectorElement.setAttribute(EntitlementPolicyConstants.MUST_BE_PRESENT,
                        attributeSelectorDTO.getAttributeSelectorMustBePresent());
            }

        }

        return attributeSelectorElement;
    }

}
