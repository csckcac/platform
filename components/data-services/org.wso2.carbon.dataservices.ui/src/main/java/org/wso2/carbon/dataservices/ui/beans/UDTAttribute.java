/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.dataservices.ui.beans;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;

public class UDTAttribute extends DataServiceConfigurationElement {

    private String attributeName;

    private String attributeValue;

    private String schemaType;

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public String getSchemaType() {
        return schemaType;
    }

    public void setSchemaType(String schemaType) {
        this.schemaType = schemaType;
    }


    @Override
    public OMElement buildXML() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
            OMElement elementEl = fac.createOMElement("udtAttribute", null);
            if (this.getAttributeName() != null) {
                elementEl.addAttribute("name", this.getAttributeName(), null);
            }
            if(this.getSchemaType() != null){
                elementEl.addAttribute("schemaType", this.getSchemaType(), null);
            }
            if (this.getRequiredRoles() != null && this.getRequiredRoles().trim().length() > 0) {
                elementEl.addAttribute("requiredRoles", this.getRequiredRoles().trim(), null);
            }

            return elementEl;
    }
}
