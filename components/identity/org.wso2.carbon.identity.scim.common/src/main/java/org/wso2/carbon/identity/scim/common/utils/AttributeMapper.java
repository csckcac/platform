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
package org.wso2.carbon.identity.scim.common.utils;

import org.wso2.charon.core.attributes.Attribute;
import org.wso2.charon.core.attributes.ComplexAttribute;
import org.wso2.charon.core.attributes.MultiValuedAttribute;
import org.wso2.charon.core.attributes.SimpleAttribute;
import org.wso2.charon.core.objects.AbstractSCIMObject;
import org.wso2.charon.core.schema.SCIMConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for converting SCIM attributes in a SCIM object to
 * carbon claims and vice versa
 */
public class AttributeMapper {

    /**
     * Return claims as a map of <ClaimUri (which is mapped to SCIM attribute uri),ClaimValue>
     *
     * @param scimObject
     * @return
     */
    public static Map<String, String> getClaimsMap(AbstractSCIMObject scimObject) {
        Map<String, String> claimsMap = new HashMap<String, String>();
        Map<String, Attribute> attributeList = scimObject.getAttributeList();
        for (Map.Entry<String, Attribute> attributeEntry : attributeList.entrySet()) {
            Attribute attribute = attributeEntry.getValue();
            //if the attribute is password, skip it
            if (SCIMConstants.UserSchemaConstants.PASSWORD.equals(attribute.getName())) {
                continue;
            }
            if (attribute instanceof SimpleAttribute) {
                String attributeURI = ((SimpleAttribute) attribute).getAttributeURI();
                String attributeValue = String.valueOf(((SimpleAttribute) attribute).getValue());
                //set attribute URI as the claim URI
                claimsMap.put(attributeURI, attributeValue);
            } else if (attribute instanceof MultiValuedAttribute) {
                MultiValuedAttribute multiValAttribute = (MultiValuedAttribute) attribute;
                //get the URI of root attribute
                String attributeURI = multiValAttribute.getAttributeURI();
                //check if values are set as simple attributes
                List<String> attributeValues = multiValAttribute.getValuesAsStrings();
                if (attributeValues != null && (!attributeValues.isEmpty())) {
                    String values = null;
                    for (String attributeValue : attributeValues) {
                        values = attributeValue + ",";
                    }
                    claimsMap.put(attributeURI, values);
                }
                //check if values are set as complex values
                //NOTE: in carbon, we only support storing of type and value of a multi-valued attribute
                List<Attribute> complexAttributeList = multiValAttribute.getValuesAsSubAttributes();
                for (Attribute complexAttribute : complexAttributeList) {
                    Map<String, Attribute> subAttributes = ((ComplexAttribute) complexAttribute).getSubAttributes();
                    SimpleAttribute typeAttribute = (SimpleAttribute) subAttributes.get(
                            SCIMConstants.CommonSchemaConstants.TYPE);
                    String typeValue = (String) typeAttribute.getValue();
                    //construct attribute URI
                    String valueAttriubuteURI = attributeURI + "." + typeValue;
                    SimpleAttribute valueAttribute = (SimpleAttribute) subAttributes.get(
                            SCIMConstants.CommonSchemaConstants.VALUE);
                    if (valueAttribute != null) {
                        //put it in claims
                        claimsMap.put(valueAttriubuteURI, String.valueOf(valueAttribute.getValue()));
                    }
                }
            } else if (attribute instanceof ComplexAttribute) {
                ComplexAttribute complexAttribute = (ComplexAttribute) attribute;
                Map<String, Attribute> subAttributes = complexAttribute.getSubAttributes();
                for (Attribute entry : subAttributes.values()) {
                    //we assume each entry contains a simple attribute
                    if (entry instanceof SimpleAttribute) {
                        SimpleAttribute simpleAttribute = ((SimpleAttribute) entry);
                        claimsMap.put(entry.getAttributeURI(),
                                      String.valueOf(simpleAttribute.getValue()));
                    }
                }
            }
        }
        return claimsMap;
    }


    //TODO: when constructing SCIM object from attribute values and claim uris, in multi attributes,
    //TODO: get the role list as well.
}
