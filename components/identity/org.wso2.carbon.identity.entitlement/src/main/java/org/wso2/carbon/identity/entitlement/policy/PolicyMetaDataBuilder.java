/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement.policy;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.rampart.util.Axis2Util;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.entitlement.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.AttributeValueDTO;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;


/**
 * This class phrase the xml representation of policy and build the policy meta data such as
 * resource names, subject names action names and environment names, attribute ids and data types.
 */
public class PolicyMetaDataBuilder {

    /**
     * This creates properties object which contains the policy meta data.
     * @param policy policy as a String
     * @return properties object which contains the policy meta data
     * @throws IdentityException throws
     */
    public Properties getPolicyMetaDataFromPolicy(String policy) throws IdentityException {

        List<AttributeValueDTO> attributeValueDTOs = new ArrayList<AttributeValueDTO>();
        try {
            attributeValueDTOs = createPolicyMetaData(policy, attributeValueDTOs);
        } catch (IdentityException e) {
            throw new IdentityException("Can not create Policy MetaData for given policy");
        }

        int attributeElementNo = 0;
        Properties properties = new Properties();

        if(attributeValueDTOs != null){
            for(AttributeValueDTO attributeValueDTO : attributeValueDTOs){
                properties.setProperty(EntitlementConstants.POLICY_META_DATA + attributeElementNo,
                       attributeValueDTO.getAttributeType() + EntitlementConstants.ATTRIBUTE_SEPARATOR + 
                       attributeValueDTO.getAttribute() + EntitlementConstants.ATTRIBUTE_SEPARATOR +
                       attributeValueDTO.getAttributeId() + EntitlementConstants.ATTRIBUTE_SEPARATOR +
                       attributeValueDTO.getAttributeDataType());
                attributeElementNo ++;
            }
        }
        return properties;
    }

    /**
     * This creates the attributes from registry property values
     * @param properties Properties object read from registry resource
     * @return  attributes as AttributeValueDTO[] object
     */
    public AttributeValueDTO[] getPolicyMetaDataFromRegistryProperties(Properties properties){

        List<AttributeValueDTO> attributeValueDTOs = new ArrayList<AttributeValueDTO>();
        if(properties != null && !properties.isEmpty()){
            for (int attributeElementNo = 0; attributeElementNo < properties.size();){
                List attributeList = (ArrayList) properties.get(EntitlementConstants.POLICY_META_DATA +
                                                                attributeElementNo);
                if(attributeList != null && attributeList.get(0) != null){
                    String[] attributeData = attributeList.get(0).toString().
                            split(EntitlementConstants.ATTRIBUTE_SEPARATOR);
                    if(attributeData.length == EntitlementConstants.POLICY_META_DATA_ARRAY_LENGTH){
                        AttributeValueDTO attributeValueDTO = new AttributeValueDTO();
                        attributeValueDTO.setAttributeType(attributeData[0]);
                        attributeValueDTO.setAttribute(attributeData[1]);
                        attributeValueDTO.setAttributeId(attributeData[2]);                        
                        attributeValueDTO.setAttributeDataType(attributeData[3]);
                        attributeValueDTOs.add(attributeValueDTO);
                    }
                }
                attributeElementNo ++;
            }
        } else {
            return null;
        }

        return attributeValueDTOs.toArray(new AttributeValueDTO[attributeValueDTOs.size()]);
    }

    /**
     * This creates the OMElement from the policy xml and create the the meta data for hole policy
     * @param policy   policy as a String
     * @param attributeValueDTOs object which holds the policy meta data in String format
     * @return  list of AttributeValueDTO object which holds the policy meta data in String format
     * @throws IdentityException throws if OMElement can not be created
     */
    public List<AttributeValueDTO> createPolicyMetaData(String policy,
                            List<AttributeValueDTO> attributeValueDTOs) throws IdentityException {

        OMElement omElement;

        try {
            omElement = AXIOMUtil.stringToOM(policy);
        } catch (XMLStreamException e) {
            throw new IdentityException("Policy xml can not be converted to OMElement");
        }

        if (omElement != null) {
            Iterator iterator1 = omElement.getChildrenWithLocalName(EntitlementConstants.
                    TARGET_ELEMENT);
            while(iterator1.hasNext()){
                OMElement targetElement = (OMElement)iterator1.next();
                createMetaDataFromTargetElement(targetElement, attributeValueDTOs);
            }

            Iterator iterator2 = omElement.getChildrenWithLocalName(EntitlementConstants.
                    RULE_ELEMENT);
            while(iterator2.hasNext()){
                OMElement targetElement = (OMElement)iterator2.next();
                createMetaDataFromRuleElement(targetElement, attributeValueDTOs);
            }

            Iterator iterator3 = omElement.getChildrenWithLocalName(EntitlementConstants.
                    POLICY_ELEMENT);
            while(iterator3.hasNext()){
                OMElement targetElement = (OMElement)iterator3.next();
                createPolicyMetaData(targetElement.toString(), attributeValueDTOs);
            }
        }

        return attributeValueDTOs;
    }

    /**
     * This extract policy meta data from target element in the policy
     * @param omElement  target element as an OMElement
     * @param attributeValueDTOs  list of AttributeValueDTO object which holds the policy meta data
     * in String format
     * @return list of AttributeValueDTO object which holds the policy meta data in String format
     */
    public List<AttributeValueDTO> createMetaDataFromTargetElement(OMElement omElement,
                                                        List<AttributeValueDTO> attributeValueDTOs){

        if(omElement != null){

            Iterator iterator1 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.RESOURCE_ELEMENT + "s");
            while(iterator1.hasNext()){
                OMElement resourceElements = (OMElement) iterator1.next();

                Iterator iterator2 = resourceElements.
                        getChildrenWithLocalName(EntitlementConstants.RESOURCE_ELEMENT);
                while(iterator2.hasNext()){
                    OMElement resourceElement = (OMElement) iterator2.next();

                    Iterator iterator3 = resourceElement.
                            getChildrenWithLocalName(EntitlementConstants.RESOURCE_ELEMENT +
                                                     EntitlementConstants.MATCH_ELEMENT);

                    while(iterator3.hasNext()){
                        OMElement resourceMatch = (OMElement) iterator3.next();
                        List<AttributeValueDTO> attributeValueDTOList =
                                createMetaDataFromMatchElement(resourceMatch,
                                                               EntitlementConstants.RESOURCE_ELEMENT);
                        for(AttributeValueDTO attributeValueDTO : attributeValueDTOList){
                            attributeValueDTOs.add(attributeValueDTO);
                        }
                    }
                }
            }

            Iterator iterator4 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.SUBJECT_ELEMENT + "s");
            while(iterator4.hasNext()){
                OMElement resourceElements = (OMElement) iterator4.next();

                Iterator iterator2 = resourceElements.
                        getChildrenWithLocalName(EntitlementConstants.SUBJECT_ELEMENT);
                while(iterator2.hasNext()){
                    OMElement resourceElement = (OMElement) iterator2.next();

                    Iterator iterator3 = resourceElement.
                            getChildrenWithLocalName(EntitlementConstants.SUBJECT_ELEMENT +
                                                     EntitlementConstants.MATCH_ELEMENT);

                    while(iterator3.hasNext()){
                        OMElement resourceMatch = (OMElement) iterator3.next();
                        List<AttributeValueDTO> attributeValueDTOList =
                                createMetaDataFromMatchElement(resourceMatch,
                                                               EntitlementConstants.SUBJECT_ELEMENT);
                        for(AttributeValueDTO attributeValueDTO : attributeValueDTOList){
                            attributeValueDTOs.add(attributeValueDTO);
                        }
                    }
                }

            }

            Iterator iterator5 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ACTION_ELEMENT + "s");

            while(iterator5.hasNext()){
                OMElement resourceElements = (OMElement) iterator5.next();

                Iterator iterator2 = resourceElements.
                        getChildrenWithLocalName(EntitlementConstants.ACTION_ELEMENT);
                while(iterator2.hasNext()){
                    OMElement resourceElement = (OMElement) iterator2.next();

                    Iterator iterator3 = resourceElement.
                            getChildrenWithLocalName(EntitlementConstants.ACTION_ELEMENT +
                                                     EntitlementConstants.MATCH_ELEMENT);

                    while(iterator3.hasNext()){
                        OMElement resourceMatch = (OMElement) iterator3.next();
                        List<AttributeValueDTO> attributeValueDTOList =
                                createMetaDataFromMatchElement(resourceMatch,
                                                               EntitlementConstants.ACTION_ELEMENT);
                        for(AttributeValueDTO attributeValueDTO : attributeValueDTOList){
                            attributeValueDTOs.add(attributeValueDTO);
                        }
                    }
                }

            }

            Iterator iterator6 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ENVIRONMENT_ELEMENT + "s");

            while(iterator6.hasNext()){
                OMElement resourceElements = (OMElement) iterator6.next();

                Iterator iterator2 = resourceElements.
                        getChildrenWithLocalName(EntitlementConstants.ENVIRONMENT_ELEMENT);
                while(iterator2.hasNext()){
                    OMElement resourceElement = (OMElement) iterator2.next();

                    Iterator iterator3 = resourceElement.
                            getChildrenWithLocalName(EntitlementConstants.ENVIRONMENT_ELEMENT +
                                                     EntitlementConstants.MATCH_ELEMENT);

                    while(iterator3.hasNext()){
                        OMElement resourceMatch = (OMElement) iterator3.next();

                        List<AttributeValueDTO> attributeValueDTOList =
                                createMetaDataFromMatchElement(resourceMatch,
                                                           EntitlementConstants.ENVIRONMENT_ELEMENT);
                        for(AttributeValueDTO attributeValueDTO : attributeValueDTOList){
                            attributeValueDTOs.add(attributeValueDTO);
                        }
                    }
                }

            }

        }

        return attributeValueDTOs;

    }

    /**
     * This extract policy meta data from match element in the policy
     * @param omElement  match element  as an OMElement
     * @param subElementName  match element name
     * @return AttributeValueDTO object which holds the policy meta data in String format
     */
    public List<AttributeValueDTO> createMetaDataFromMatchElement(OMElement omElement,
                                                                  String subElementName){

        List<AttributeValueDTO> attributeValueDTOs = new ArrayList<AttributeValueDTO>();
        String attributeId = null;
        String dataType = null;

        if(omElement != null){
            Iterator iterator1 = omElement.
                    getChildrenWithLocalName(subElementName + EntitlementConstants.ATTRIBUTE_DESIGNATOR);
            while(iterator1.hasNext()){
                OMElement attributeDesignator = (OMElement)iterator1.next();
                if(attributeDesignator != null){
                    attributeId = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.ATTRIBUTE_ID));
                    dataType = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                }
            }

            Iterator iterator2 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ATTRIBUTE_SELECTOR);
            while(iterator2.hasNext()){
                OMElement attributeDesignator = (OMElement)iterator2.next();
                if(attributeDesignator != null){
                    attributeId = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.REQUEST_CONTEXT_PATH));
                    dataType = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                }
            }

            Iterator iterator3 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ATTRIBUTE_VALUE);
            while(iterator3.hasNext()){
                OMElement attributeElement = (OMElement)iterator3.next();
                if(attributeElement != null){
                    AttributeValueDTO attributeValueDTO = new AttributeValueDTO();
                    attributeValueDTO.setAttribute(attributeElement.getText());
                    attributeValueDTO.setAttributeId(attributeId);
                    attributeValueDTO.setAttributeDataType(dataType);
                    attributeValueDTO.setAttributeType(subElementName);
                    attributeValueDTOs.add(attributeValueDTO);
                }
            }

        }
        return attributeValueDTOs;

    }

    /**
     * This extract policy meta data from apply element in the policy
     * @param omElement apply element as an OMElement
     * @param attributeValueDTOs  list of AttributeValueDTO object which holds the policy meta data
     * in String format
     * @return list of AttributeValueDTO object which holds the policy meta data in String format
     */
    public List<AttributeValueDTO> createMetaDataFromApplyElement(OMElement omElement,
                                                      List<AttributeValueDTO> attributeValueDTOs) {

        //TODO check with function id and decide whether search can be done or not
        if(omElement != null){
            Iterator iterator1 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.RESOURCE_ELEMENT +
                                             EntitlementConstants.ATTRIBUTE_DESIGNATOR);
            while(iterator1.hasNext()){
                OMElement attributeDesignator = (OMElement)iterator1.next();
                if(attributeDesignator != null){
                    String attributeId = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.ATTRIBUTE_ID));
                    String dataType = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                    List<String> attributeValues = searchAttributeValues(omElement,
                                                                         new ArrayList<String>(), true);
                    if(attributeValues == null){
                        AttributeValueDTO attributeValueDTO = new AttributeValueDTO();
                        attributeValueDTO.setAttributeType(EntitlementConstants.UNKNOWN);
                        attributeValueDTO.setAttribute(EntitlementConstants.SEARCH_WARNING_MESSAGE1 +
                                                    " for " + EntitlementConstants.RESOURCE_ELEMENT +
                                                    " Designator Element ");
                        attributeValueDTOs.add(attributeValueDTO);
                    } else if(attributeValues.isEmpty()){
                        AttributeValueDTO attributeValueDTO = new AttributeValueDTO();
                        attributeValueDTO.setAttributeType(EntitlementConstants.UNKNOWN);
                        attributeValueDTO.setAttribute(EntitlementConstants.SEARCH_WARNING_MESSAGE2 +
                                                    " for " + EntitlementConstants.RESOURCE_ELEMENT +
                                                    " Designator Element ");

                    } else {
                        for(String value : attributeValues){
                            AttributeValueDTO attributeValueDTO = new AttributeValueDTO();
                            attributeValueDTO.setAttribute(value);
                            attributeValueDTO.setAttributeDataType(dataType);
                            attributeValueDTO.setAttributeType(EntitlementConstants.RESOURCE_ELEMENT);
                            attributeValueDTO.setAttributeId(attributeId);
                            attributeValueDTOs.add(attributeValueDTO);
                        }
                    }
                }
            }

            Iterator iterator2 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.SUBJECT_ELEMENT +
                                             EntitlementConstants.ATTRIBUTE_DESIGNATOR);
            while(iterator2.hasNext()){
                OMElement attributeDesignator = (OMElement)iterator2.next();
                if(attributeDesignator != null){
                    String attributeId = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.ATTRIBUTE_ID));
                    String dataType = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                    List<String> attributeValues = searchAttributeValues(omElement,
                                                                         new ArrayList<String>(), true);
                    if(attributeValues == null){
                        AttributeValueDTO attributeValueDTO = new AttributeValueDTO();
                        attributeValueDTO.setAttributeType(EntitlementConstants.UNKNOWN);
                        attributeValueDTO.setAttribute(EntitlementConstants.SEARCH_WARNING_MESSAGE1 +
                                                    " for " + EntitlementConstants.RESOURCE_ELEMENT +
                                                    " Designator Element ");

                        attributeValueDTOs.add(attributeValueDTO);
                    } else if(attributeValues.isEmpty()){
                        AttributeValueDTO attributeValueDTO = new AttributeValueDTO();
                        attributeValueDTO.setAttributeType(EntitlementConstants.UNKNOWN);
                        attributeValueDTO.setAttribute(EntitlementConstants.SEARCH_WARNING_MESSAGE2 +
                                                    " for " + EntitlementConstants.RESOURCE_ELEMENT +
                                                    " Designator Element ");

                    } else {
                        for(String value : attributeValues){
                            AttributeValueDTO attributeValueDTO = new AttributeValueDTO();
                            attributeValueDTO.setAttribute(value);
                            attributeValueDTO.setAttributeDataType(dataType);
                            attributeValueDTO.setAttributeType(EntitlementConstants.SUBJECT_ELEMENT);
                            attributeValueDTO.setAttributeId(attributeId);
                            attributeValueDTOs.add(attributeValueDTO);
                        }
                    }
                }
            }

            Iterator iterator3 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ACTION_ELEMENT +
                                             EntitlementConstants.ATTRIBUTE_DESIGNATOR);
            while(iterator3.hasNext()){
                OMElement attributeDesignator = (OMElement)iterator3.next();
                if(attributeDesignator != null){
                    String attributeId = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.ATTRIBUTE_ID));
                    String dataType = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                    List<String> attributeValues = searchAttributeValues(omElement,
                                                                         new ArrayList<String>(), true);
                    if(attributeValues == null){
                        AttributeValueDTO attributeValueDTO = new AttributeValueDTO();
                        attributeValueDTO.setAttributeType(EntitlementConstants.UNKNOWN);
                        attributeValueDTO.setAttribute(EntitlementConstants.SEARCH_WARNING_MESSAGE1 +
                                                    " for " + EntitlementConstants.RESOURCE_ELEMENT +
                                                    " Designator Element ");

                        attributeValueDTOs.add(attributeValueDTO);
                    } else if(attributeValues.isEmpty()){
                        AttributeValueDTO attributeValueDTO = new AttributeValueDTO();
                        attributeValueDTO.setAttributeType(EntitlementConstants.UNKNOWN);
                        attributeValueDTO.setAttribute(EntitlementConstants.SEARCH_WARNING_MESSAGE2 +
                                                    " for " + EntitlementConstants.RESOURCE_ELEMENT +
                                                    " Designator Element ");

                    } else {
                        for(String value : attributeValues){
                            AttributeValueDTO attributeValueDTO = new AttributeValueDTO();
                            attributeValueDTO.setAttribute(value);
                            attributeValueDTO.setAttributeDataType(dataType);
                            attributeValueDTO.setAttributeType(EntitlementConstants.ACTION_ELEMENT);
                            attributeValueDTO.setAttributeId(attributeId);
                            attributeValueDTOs.add(attributeValueDTO);
                        }
                    }
                }
            }

            Iterator iterator4 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ENVIRONMENT_ELEMENT +
                                             EntitlementConstants.ATTRIBUTE_DESIGNATOR);
            while(iterator4.hasNext()){
                OMElement attributeDesignator = (OMElement)iterator4.next();
                if(attributeDesignator != null){
                    String attributeId = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.ATTRIBUTE_ID));
                    String dataType = attributeDesignator.
                            getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                    List<String> attributeValues = searchAttributeValues(omElement,
                                                                         new ArrayList<String>(), true);
                    if(attributeValues == null){
                        AttributeValueDTO attributeValueDTO = new AttributeValueDTO();
                        attributeValueDTO.setAttributeType(EntitlementConstants.UNKNOWN);
                        attributeValueDTO.setAttribute(EntitlementConstants.SEARCH_WARNING_MESSAGE1 +
                                                    " for " + EntitlementConstants.RESOURCE_ELEMENT +
                                                    " Designator Element ");

                        attributeValueDTOs.add(attributeValueDTO);
                    } else if(attributeValues.isEmpty()){
                        AttributeValueDTO attributeValueDTO = new AttributeValueDTO();
                        attributeValueDTO.setAttributeType(EntitlementConstants.UNKNOWN);
                        attributeValueDTO.setAttribute(EntitlementConstants.SEARCH_WARNING_MESSAGE2 +
                                                    " for " + EntitlementConstants.RESOURCE_ELEMENT +
                                                    " Designator Element ");

                    } else {
                        for(String value : attributeValues){
                            AttributeValueDTO attributeValueDTO = new AttributeValueDTO();
                            attributeValueDTO.setAttribute(value);
                            attributeValueDTO.setAttributeDataType(dataType);
                            attributeValueDTO.setAttributeType(EntitlementConstants.ENVIRONMENT_ELEMENT);
                            attributeValueDTO.setAttributeId(attributeId);
                            attributeValueDTOs.add(attributeValueDTO);
                        }
                    }
                }
            }

            Iterator iterator5 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ATTRIBUTE_SELECTOR);
            while(iterator5.hasNext()){
                OMElement attributeSelector = (OMElement)iterator5.next();
                if(attributeSelector != null){
                    String attributeId = attributeSelector.
                            getAttributeValue(new QName(EntitlementConstants.REQUEST_CONTEXT_PATH));
                    String subElementName = attributeId;  //TODO  Fix finding element name from Xpath 
                    String dataType = attributeSelector.
                            getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                    List<String> attributeValues = searchAttributeValues(omElement,
                                                                         new ArrayList<String>(), true);
                    if(attributeValues == null){
                        AttributeValueDTO attributeValueDTO = new AttributeValueDTO();
                        attributeValueDTO.setAttributeType(EntitlementConstants.UNKNOWN);
                        attributeValueDTO.setAttribute(EntitlementConstants.SEARCH_WARNING_MESSAGE3);

                        attributeValueDTOs.add(attributeValueDTO);
                    } else if(attributeValues.isEmpty()){
                        AttributeValueDTO attributeValueDTO = new AttributeValueDTO();
                        attributeValueDTO.setAttributeType(EntitlementConstants.UNKNOWN);
                        attributeValueDTO.setAttribute(EntitlementConstants.SEARCH_WARNING_MESSAGE3);

                    } else {
                        for(String value : attributeValues){
                            AttributeValueDTO attributeValueDTO = new AttributeValueDTO();
                            attributeValueDTO.setAttribute(value);
                            attributeValueDTO.setAttributeDataType(dataType);
                            attributeValueDTO.setAttributeType(subElementName);
                            attributeValueDTO.setAttributeId(attributeId);
                            attributeValueDTOs.add(attributeValueDTO);
                            // Remove following after fixing XPath issues
                            attributeValueDTO.setAttributeType(EntitlementConstants.UNKNOWN);
                            attributeValueDTO.setAttribute(EntitlementConstants.SEARCH_WARNING_MESSAGE3);
                        }
                    }
                }
            }

            Iterator iterator6 = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ATTRIBUTE_VALUE);
            if(iterator6.hasNext()) {
                List<String> attributeValues = searchAttributeValues(omElement, new ArrayList<String>(), false);
                if(attributeValues == null){
                    AttributeValueDTO attributeValueDTO = new AttributeValueDTO();
                    attributeValueDTO.setAttributeType(EntitlementConstants.UNKNOWN);
                        attributeValueDTO.setAttribute(EntitlementConstants.SEARCH_WARNING_MESSAGE1 +
                                                    " for " + EntitlementConstants.RESOURCE_ELEMENT +
                                                    " Designator Element ");
                    attributeValueDTOs.add(attributeValueDTO);
                } else if(attributeValues.isEmpty()){
                    AttributeValueDTO attributeValueDTO = new AttributeValueDTO();
                    attributeValueDTO.setAttributeType(EntitlementConstants.UNKNOWN);
                        attributeValueDTO.setAttribute(EntitlementConstants.SEARCH_WARNING_MESSAGE2 +
                                                    " for " + EntitlementConstants.RESOURCE_ELEMENT +
                                                    " Designator Element ");
                } else {
                    for(String values : attributeValues){
                        AttributeValueDTO attributeValueDTO = new AttributeValueDTO();
                        attributeValueDTO.setAttribute(values);
                        Iterator iterator8 = omElement.
                                getChildrenWithLocalName(EntitlementConstants.APPLY_ELEMENT);
                        while(iterator8.hasNext()){
                            OMElement applyElement = (OMElement)iterator8.next();
                            searchDesignatorOrSelector(applyElement, attributeValueDTO);
                        }
                        if(attributeValueDTO.getAttributeType() != null ||
                                                        "".equals(attributeValueDTO.getAttributeType())) {
                            attributeValueDTOs.add(attributeValueDTO);
                        }
                    }
                }
            }

            Iterator iterator7 = omElement.getChildrenWithLocalName(EntitlementConstants.APPLY_ELEMENT);
            while(iterator7.hasNext()){
                OMElement applyElement = (OMElement)iterator7.next();
                createMetaDataFromApplyElement(applyElement,attributeValueDTOs);
            }                    
        }
        return attributeValueDTOs;
    }

    /**
     * This searches through  attribute values in the attribute elements to extract the policy meta data
     * @param omElement apply element as an OMElement
     * @param values set of String objects
     * @param searchDesignators states where,  to find designators which are involved in creating
     *          attribute values
     * @return   AttributeValueDTO object which holds the policy meta data in String format
     */
    public List<String> searchAttributeValues(OMElement omElement, List<String> values,
                                              boolean searchDesignators){

        if(values != null){
            Iterator iterator = omElement.
                    getChildrenWithLocalName(EntitlementConstants.ATTRIBUTE_VALUE);
            while(iterator.hasNext()){
                OMElement attributeElement = (OMElement)iterator.next();
                if(attributeElement != null){
                    values.add(attributeElement.getText());
                }
            }
        }

        Iterator iterator1 = omElement.getChildrenWithLocalName(EntitlementConstants.APPLY_ELEMENT);
        while(iterator1.hasNext()){
            OMElement applyElement = (OMElement)iterator1.next();
            searchAttributeValues(applyElement, values, searchDesignators);

            AttributeValueDTO  attributeValueDTO = new AttributeValueDTO();
            if(searchDesignators){
                searchDesignatorOrSelector(applyElement, attributeValueDTO);
            }
            if(attributeValueDTO.getAttributeType() != null || attributeValueDTO.getAttributeId() != null ||
                    attributeValueDTO.getAttributeDataType() != null){
                values = null;
            }
        }

        return values;
    }

    /**
     * This searches through  designator and selector values in the attribute elements to extract
     * the policy meta data
     * @param omElement apply element as an OMElement
     * @param attributeValueDTO AttributeValueDTO object which holds the policy meta data in String format
     * @return   AttributeValueDTO object which holds the policy meta data in String format
     */
    public AttributeValueDTO searchDesignatorOrSelector(OMElement omElement,
                                                        AttributeValueDTO attributeValueDTO) {


        Iterator iterator1 = omElement.
                getChildrenWithLocalName(EntitlementConstants.RESOURCE_ELEMENT +
                                         EntitlementConstants.ATTRIBUTE_DESIGNATOR);
        while(iterator1.hasNext()){
            OMElement attributeDesignator = (OMElement)iterator1.next();
            if(attributeDesignator != null){
                String attributeId = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.ATTRIBUTE_ID));
                String dataType = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                attributeValueDTO.setAttributeDataType(dataType);
                attributeValueDTO.setAttributeType(EntitlementConstants.RESOURCE_ELEMENT);
                attributeValueDTO.setAttributeId(attributeId);
            }
        }

        Iterator iterator2 = omElement.
                getChildrenWithLocalName(EntitlementConstants.SUBJECT_ELEMENT +
                                         EntitlementConstants.ATTRIBUTE_DESIGNATOR);
        while(iterator2.hasNext()){
            OMElement attributeDesignator = (OMElement)iterator2.next();
            if(attributeDesignator != null){
                String attributeId = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.ATTRIBUTE_ID));
                String dataType = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                attributeValueDTO.setAttributeDataType(dataType);
                attributeValueDTO.setAttributeType(EntitlementConstants.SUBJECT_ELEMENT);
                attributeValueDTO.setAttributeId(attributeId);
            }
        }

        Iterator iterator3 = omElement.
                getChildrenWithLocalName(EntitlementConstants.ACTION_ELEMENT +
                                         EntitlementConstants.ATTRIBUTE_DESIGNATOR);
        while(iterator3.hasNext()){
            OMElement attributeDesignator = (OMElement)iterator3.next();
            if(attributeDesignator != null){
                String attributeId = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.ATTRIBUTE_ID));
                String dataType = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                attributeValueDTO.setAttributeDataType(dataType);
                attributeValueDTO.setAttributeType(EntitlementConstants.ACTION_ELEMENT);
                attributeValueDTO.setAttributeId(attributeId);
            }
        }

        Iterator iterator4 = omElement.
                getChildrenWithLocalName(EntitlementConstants.ENVIRONMENT_ELEMENT +
                                         EntitlementConstants.ATTRIBUTE_DESIGNATOR);
        while(iterator4.hasNext()){
            OMElement attributeDesignator = (OMElement)iterator4.next();
            if(attributeDesignator != null){
                String attributeId = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.ATTRIBUTE_ID));
                String dataType = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                attributeValueDTO.setAttributeDataType(dataType);
                attributeValueDTO.setAttributeType(EntitlementConstants.ENVIRONMENT_ELEMENT);
                attributeValueDTO.setAttributeId(attributeId);
            }
        }

        Iterator iterator5 = omElement.
                getChildrenWithLocalName(EntitlementConstants.ATTRIBUTE_SELECTOR);
        while(iterator5.hasNext()){
            OMElement attributeDesignator = (OMElement)iterator5.next();
            if(attributeDesignator != null){
                String attributeId = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.REQUEST_CONTEXT_PATH));
                String subElementName = attributeId;  //TODO  Fix finding element name from Xpath
                String dataType = attributeDesignator.
                        getAttributeValue(new QName(EntitlementConstants.DATA_TYPE));
                attributeValueDTO.setAttributeDataType(dataType);
                attributeValueDTO.setAttributeType(EntitlementConstants.UNKNOWN);
                attributeValueDTO.setAttribute(EntitlementConstants.SEARCH_WARNING_MESSAGE3);
                attributeValueDTO.setAttributeId(attributeId);
            }
        }

        return attributeValueDTO;
    }

    /**
     * This extract policy meta data from condition element in the policy
     * @param omElement condition element as an OMElement
     * @param attributeValueDTOs  list of AttributeValueDTO object which holds the policy meta data
     * in String format
     * @return list of AttributeValueDTO object which holds the policy meta data in String format
     */
    public List<AttributeValueDTO> createMetaDataFromConditionElement(OMElement omElement,
                                                   List<AttributeValueDTO> attributeValueDTOs){

        Iterator iterator = omElement.getChildrenWithLocalName(EntitlementConstants.APPLY_ELEMENT);
        if(iterator.hasNext()){
            createMetaDataFromApplyElement(omElement, attributeValueDTOs);
        } else {
            AttributeValueDTO attributeValueDTO = new AttributeValueDTO();
            attributeValueDTO.setAttributeType(EntitlementConstants.UNKNOWN);
            attributeValueDTO.setAttribute(EntitlementConstants.SEARCH_WARNING_MESSAGE4);
        }

        // TODO currently only search meta data on Apply Element, support for other elements 
        return attributeValueDTOs;
    }

    /**
     * This extract policy meta data from each rule element in the policy
     * @param omElement rule element as an OMElement
     * @param attributeValueDTOs  list of AttributeValueDTO object which holds the policy meta data
     * in String format
     * @return list of AttributeValueDTO object which holds the policy meta data in String format
     */
    public List<AttributeValueDTO> createMetaDataFromRuleElement(OMElement omElement,
                                              List<AttributeValueDTO> attributeValueDTOs){

        if (omElement != null) {

            Iterator iterator1 = omElement.getChildrenWithLocalName(EntitlementConstants.
                    TARGET_ELEMENT);
            while(iterator1.hasNext()){
                OMElement targetElement = (OMElement)iterator1.next();
                createMetaDataFromTargetElement(targetElement, attributeValueDTOs);
            }

            Iterator iterator2 = omElement.getChildrenWithLocalName(EntitlementConstants.
                    CONDITION_ELEMENT);
            while(iterator2.hasNext()){
                OMElement conditionElement = (OMElement)iterator2.next();
                createMetaDataFromConditionElement(conditionElement, attributeValueDTOs );
            }
        }

        return attributeValueDTOs;
    }
}
