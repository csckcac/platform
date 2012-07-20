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
import org.wso2.carbon.identity.entitlement.dto.AttributeValueTreeNodeDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyAttributeDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;

import java.util.ArrayList;
import java.util.HashSet;
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
     * @return Set of attribute values as String Set
     */
    public Set<PolicyAttributeDTO> getPolicyAttributeValues(){

        Set<PolicyAttributeDTO> policyAttributeDTOs = new HashSet<PolicyAttributeDTO>();
        Set<String> dataTypes;
        Set<String> attributeIds;

        for(PolicyMetaDataFinderModule module : metaDataFinderModules){

            PolicyAttributeDTO policyAttributeDTO = new PolicyAttributeDTO();

            Map<String, String> categoryMap = module.getSupportedCategories();

            Set<Map.Entry<String, String>> categories = categoryMap.entrySet();

            for(Map.Entry<String, String> categoryEntry : categories){

                AttributeValueTreeNodeDTO node = null;
                String category = categoryEntry.getKey();
                try {
                    node = module.getAttributeValueData(category);
                } catch (Exception e) {
                    String message = "Error occurs while finding policy attribute value using module " + module;
                    log.error(message, e);
                }

                if(node != null){
                    node.setFullPathSupported(module.isFullPathSupported());
                    node.setHierarchicalTree(module.isHierarchicalTree());
                    node.setModuleName(module.getModuleName());
                    node.setCategoryId(category);
                    node.setCategoryUri(categoryEntry.getValue());
                    node.setDefaultAttributeDataType(module.getDefaultAttributeDataType(category));
                    node.setDefaultAttributeId(module.getDefaultAttributeId(category));
                    try {
                        if((dataTypes = module.getAttributeDataTypes(category)) != null){
                            node.setAttributeDataTypes(dataTypes.toArray(new String[dataTypes.size()]));
                        }
                    } catch (Exception e) {
                        String message = "Error occurs while finding policy attribute data types using module "
                                         + module.getModuleName();
                        log.error(message, e);
                    }

                    try {
                        if((attributeIds = module.getSupportedAttributeIds(category)) != null){
                            node.setSupportedAttributeIds(attributeIds.
                                                        toArray(new String[attributeIds.size()]));
                        }
                    } catch (Exception e) {
                        String message = "Error occurs while finding policy attribute Ids using module "
                                         + module.getModuleName();
                        log.error(message, e);
                    }

                    policyAttributeDTO.addNodeDTO(node);
                }
            }

            ArrayList<String> categoryList = new ArrayList<String>();
            for(Map.Entry<String, String> entry : categoryMap.entrySet()){
                categoryList.add(entry.getKey());
                categoryList.add(entry.getValue());
            }
            policyAttributeDTO.addSupportedCategories(categoryList);

            Map<String, String> targetFunctionMap = module.getSupportedTargetFunctions();
            if(targetFunctionMap != null){
                ArrayList<String> targetFunctionList = new ArrayList<String>();
                for(Map.Entry<String, String> entry : targetFunctionMap.entrySet()){
                    targetFunctionList.add(entry.getKey());
                    targetFunctionList.add(entry.getValue());
                }
                policyAttributeDTO.addSupportedTargetFunctions(targetFunctionList);
            }

            Map<String, String> ruleFunctionMap = module.getSupportedRuleFunctions();

            if(ruleFunctionMap != null){
                ArrayList<String> ruleFunctionList = new ArrayList<String>();
                for(Map.Entry<String, String> entry : ruleFunctionMap.entrySet()){
                    ruleFunctionList.add(entry.getKey());
                    ruleFunctionList.add(entry.getValue());
                }
                policyAttributeDTO.addSupportedRuleFunctions(ruleFunctionList);
            }

            Set<String> preFunctions = module.getSupportedPreFunctions();
            if(preFunctions != null){
                policyAttributeDTO.setSupportedPreFunctions(preFunctions.
                                                        toArray(new String[preFunctions.size()]));                
            }
            policyAttributeDTOs.add(policyAttributeDTO);
        }
        
        return policyAttributeDTOs;
    }

}
