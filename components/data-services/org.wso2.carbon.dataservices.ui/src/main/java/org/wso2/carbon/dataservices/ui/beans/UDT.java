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

package org.wso2.carbon.dataservices.ui.beans;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;

import java.util.ArrayList;
import java.util.List;

public class UDT extends DataServiceConfigurationElement {
        private String dataSourceType;
        private String udtName;
        private String dataSourceValue;
        private String udtNamespace;

    private List<UDTAttribute> attributes;

        public UDT(String dataSourceType, String dataSourceValue, String udtName,
                String requiredRoles, String udtNamespace) {
            super(requiredRoles);
            this.dataSourceType = dataSourceType;
            this.dataSourceValue = dataSourceValue;
            this.udtName = udtName;
            this.udtNamespace = udtNamespace;
            this.attributes = new ArrayList<UDTAttribute>();
        }

        public UDT() {
        }
    
    public List<UDTAttribute> getUdtAttributes() {
		return this.attributes;
	}

    public void setUdtAttributes(List<UDTAttribute> attributes) {
		this.attributes = attributes;
	}

	public void addUdtAttribute(UDTAttribute attribute) {
		this.getUdtAttributes().add(attribute);
	}


        public OMElement buildXML() {
            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMElement elementEl = fac.createOMElement("udt", null);
            if (this.getName() != null) {
                elementEl.addAttribute("name", this.getName(), null);
            }
            if (this.getNamespace() != null && this.getNamespace().trim().length() > 0) {
                elementEl.addAttribute("namespace", this.getNamespace().trim(), null);
            }
            if (this.getDataSourceValue() != null) {
                if (this.getDataSourceType().equals("column")) {
                    elementEl.addAttribute("column", this.getDataSourceValue(), null);
                } else if (this.getDataSourceType().equals("query-param")) {
                    elementEl.addAttribute("query-param", this.getDataSourceValue(), null);
                }
            }
            if(this.getUdtAttributes() != null && this.getUdtAttributes().size() >0 ) {
                for(UDTAttribute attribute : this.getUdtAttributes()){
                    elementEl.addChild(attribute.buildXML());
                }
            }
//            if (this.getRequiredRoles() != null && this.getRequiredRoles().trim().length() > 0) {
//                elementEl.addAttribute("requiredRoles", this.getRequiredRoles().trim(), null);
//            }

            return elementEl;
        }


        public String getDataSourceType() {
            return dataSourceType;
        }


        public void setDataSourceType(String dataSourceType) {
            this.dataSourceType = dataSourceType;
        }


        public String getName() {
            return udtName;
        }


        public void setName(String name) {
            this.udtName = name;
        }


        public String getDataSourceValue() {
            return dataSourceValue;
        }


        public void setDataSourceValue(String dataSourceValue) {
            this.dataSourceValue = dataSourceValue;
        }


        public String getNamespace() {
            return udtNamespace;
        }

        public void setNamespace(String namespace) {
            this.udtNamespace = namespace;
        }

}
