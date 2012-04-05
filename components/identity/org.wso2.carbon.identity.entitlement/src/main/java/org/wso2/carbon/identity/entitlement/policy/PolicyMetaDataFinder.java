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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.AttributeValueTreeNodeDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * When creating XACML policies from WSO2 Identity server, We can define set of pre-defined attribute
 * values, attribute ids, function and so on.  These data can be retrieved from external sources such as
 * databases,  LDAPs,  or file systems. we can register, set of data retriever modules with this class.
 */
public class PolicyMetaDataFinder {

	private static Log log = LogFactory.getLog(PolicyMetaDataFinder.class);
    /**
     * List of meta data finder modules
     */
    Set<PolicyMetaDataFinderModule> metaDataFinderModules = new HashSet<PolicyMetaDataFinderModule>();

    /**
     * tenant id
     */
    int tenantId;

    public PolicyMetaDataFinder(int tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * init PolicyMetaDataFinder
     */
    public void init(){
		Map<PolicyMetaDataFinderModule, Properties> metaDataFinderConfigs = EntitlementServiceComponent.
                getEntitlementConfig().getPolicyMetaDataFinderModules();
        if(metaDataFinderConfigs != null && !metaDataFinderConfigs.isEmpty()){
            metaDataFinderModules = metaDataFinderConfigs.keySet();
        }
    }

    /**
     * finds attribute values for given attribute type
     * @param attributeType  subject, action, resource or environment
     * @return Set of attribute values as String Set
     */
    public Set<AttributeValueTreeNodeDTO> getAttributeValues(String attributeType){

        Set<AttributeValueTreeNodeDTO> policyMetaDataSet = new HashSet<AttributeValueTreeNodeDTO>();
        Set<String> dataTypes;
        Set<String> attributeIds;

        for(PolicyMetaDataFinderModule module : metaDataFinderModules){

            if(EntitlementConstants.RESOURCE_ELEMENT.equals(attributeType)){
                if(!module.isResourceAttributeSupported()){
                    String message = "Resource Attribute Values are not supported by  module "
                                     + module.getModuleName();
                    if(log.isDebugEnabled()){
                        log.debug(message);
                    }
                    continue;
                }
            } else if(EntitlementConstants.ACTION_ELEMENT.equals(attributeType)){
                if(!module.isActionAttributeSupported()){
                    String message = "Action Attribute Values are not supported by  module "
                                     + module.getModuleName();
                    if(log.isDebugEnabled()){
                        log.debug(message);
                    }
                    continue;
                }
            } else if(EntitlementConstants.SUBJECT_ELEMENT.equals(attributeType)){
                if(!module.isSubjectAttributeSupported()){
                    String message = "Subject Attribute Values are not supported by  module "
                                     + module.getModuleName();
                    if(log.isDebugEnabled()){
                        log.debug(message);
                    }
                    continue;
                }
            } else if(EntitlementConstants.ENVIRONMENT_ELEMENT.equals(attributeType)){
                if(!module.isEnvironmentAttributeSupported()){
                    String message = "Environment Attribute Values are not supported by module "
                                     + module.getModuleName();
                    if(log.isDebugEnabled()){
                        log.debug(message);
                    }
                    continue;
                }
            } else {
                String message = "Unknown Attribute Value Type is tried to retrieve from module :"
                                 + module.getModuleName();
                if(log.isDebugEnabled()){
                    log.debug(message);
                }
                break;
            }

            AttributeValueTreeNodeDTO node = null;
            try {
                node = module.getAttributeValueData(attributeType);
            } catch (Exception e) {
                String message = "Error occurs while finding attribute value using module " + module;
                log.error(message, e);
            }
            if(node != null){
                node.setFullPathSupported(module.isFullPathSupported());
                node.setHierarchicalTree(module.isHierarchicalTree());
                node.setModuleName(module.getModuleName());
                try {
                    if((dataTypes = module.getAttributeDataTypes(attributeType)) != null){
                        node.setAttributeDataTypes(dataTypes.toArray(new String[dataTypes.size()]));    
                    }
                } catch (Exception e) {
                    String message = "Error occurs while finding attribute data types using module "
                                     + module.getModuleName();
                    log.error(message, e);
                }

                try {
                    if((attributeIds = module.getSupportedAttributeIds(attributeType)) != null){
                        node.setSupportedAttributeIds(attributeIds.toArray(new String[attributeIds.size()]));
                    }
                } catch (Exception e) {
                    String message = "Error occurs while finding attribute Ids using module "
                                     + module.getModuleName();
                    log.error(message, e);
                }

                policyMetaDataSet.add(node);
            }
        }
        return policyMetaDataSet;
    }

}
