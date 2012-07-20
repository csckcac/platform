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

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.AttributeValueTreeNodeDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreManager;

import java.util.*;

/**
 * this is default implementation of the policy meta data finder module which finds the resource in the
 * under-line registry
 */
public class CarbonPolicyMetaDataFinder extends AbstractPolicyMetaDataFinder {

    public static final String MODULE_NAME =  "Carbon Attribute Finder Module";

    private Registry registry;

    private String[] defaultActions = new String[]{"read", "write", "delete", "edit"};
    
    @Override
    public void init(Properties properties) throws Exception {

    }

    @Override
    public String getModuleName() {
        return  MODULE_NAME;
    }

    @Override
    public AttributeValueTreeNodeDTO getAttributeValueData(String categoryId) throws Exception {

        registry = EntitlementServiceComponent.getRegistryService().getSystemRegistry(CarbonContext.
                getCurrentContext().getTenantId());        
        if(EntitlementConstants.RESOURCE_CATEGORY_ID.equals(categoryId)){
            AttributeValueTreeNodeDTO  nodeDTO = new AttributeValueTreeNodeDTO("/");
            getChildResources(nodeDTO, "_system");
            return nodeDTO;
        } else if(EntitlementConstants.ACTION_CATEGORY_ID.equals(categoryId)){
            AttributeValueTreeNodeDTO nodeDTO = new AttributeValueTreeNodeDTO("");
            for (String action : defaultActions){
                AttributeValueTreeNodeDTO childNode = new AttributeValueTreeNodeDTO(action);
                nodeDTO.addChildNode(childNode);
            }
            return nodeDTO;
        } else if(EntitlementConstants.SUBJECT_CATEGORY_ID.equals(categoryId)){
            AttributeValueTreeNodeDTO nodeDTO = new AttributeValueTreeNodeDTO("");
            int tenantId =  CarbonContext.getCurrentContext().getTenantId();
            UserStoreManager userStoreManager = EntitlementServiceComponent.getRealmservice().
                    getTenantUserRealm(tenantId).getUserStoreManager();
            for(String role : userStoreManager.getRoleNames()){
                if(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME.equals(role)){
                    continue;
                }
                AttributeValueTreeNodeDTO childNode = new AttributeValueTreeNodeDTO(role);
                nodeDTO.addChildNode(childNode);
            }
            return nodeDTO;
        }

        return null;
    }

    /**
     * This helps to find resources un a recursive manner
     * @param node attribute value node
     * @param parentResource  parent resource Name
     * @return child resource set
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException throws
     */
    private AttributeValueTreeNodeDTO getChildResources(AttributeValueTreeNodeDTO node,
                                                String parentResource)  throws RegistryException {

        if(registry.resourceExists(parentResource)){
            String[] resourcePath = parentResource.split("/");
            AttributeValueTreeNodeDTO childNode =
                                new AttributeValueTreeNodeDTO(resourcePath[resourcePath.length-1]);
            node.addChildNode(childNode);
            Resource root = registry.get(parentResource);
            if(root instanceof Collection){
                Collection collection = (Collection) root;
                String[] resources = collection.getChildren();
                for(String resource : resources){
                    getChildResources(childNode, resource);
                }
            }
        }

        return node;
    }


    @Override
    public boolean isHierarchicalTree() {
        return true;
    }

    @Override
    public Set<String> getSupportedAttributeIds(String category)  throws Exception {
        Set<String> values = new HashSet<String>();        
        if(EntitlementConstants.SUBJECT_CATEGORY_ID.equals(category)){
            values.add("http://wso2.org/claims/role");
        }
        return values;
    }

    @Override
    public Map<String, String> getSupportedRuleFunctions() {
        Map<String, String> newMap = new HashMap<String, String>();
        newMap.put("greater-than-or-equal-and-less-than-or-equal to" , "greater-than-or-equal-and-less-than-or-equal");
        newMap.put("greater-than-and-less-than-or-equal to" , "greater-than-and-less-than-or-equal");
        newMap.put("greater-than-or-equal-and-less-than to" , "greater-than-or-equal-and-less-than");
        newMap.put("greater-than-and-less-than to" , "greater-than-and-less-than");
        newMap.put("greater-than to" , "greater-than");
        newMap.put("greater-than-or-equal to" , "greater-than-or-equal");
        newMap.put("less-than to" , "less-than");
        newMap.put("less-than-or-equal to" , "less-than-or-equal");
        newMap.put("equal to" , "equal");
        newMap.put("regexp equal to" , "regexp-match");
        newMap.put("equal with at least one of", "at-least-one-member-of");
        newMap.put("in", "is-in");
        newMap.put("equal with all of", "set-equals");
        return newMap;
    }

    @Override
    public Map<String, String> getSupportedTargetFunctions() {
        Map<String, String> newMap = new HashMap<String, String>();
        newMap.put("matching to" , "equal");
        newMap.put("regexp matching to" , "regexp-match");
        return newMap;
    }

    @Override
    public Set<String> getSupportedPreFunctions() {
        Set<String> values = new HashSet<String>();
        values.add("is");
        values.add("is not");
        return values;

    }

    @Override
    public Set<String> getAttributeDataTypes(String attributeType) throws Exception {
        
        Set<String> values = new HashSet<String>();
        values.add("http://www.w3.org/2001/XMLSchema#string");
        values.add("http://www.w3.org/2001/XMLSchema#boolean");
        values.add("http://www.w3.org/2001/XMLSchema#integer");
        values.add("http://www.w3.org/2001/XMLSchema#double");
        values.add("http://www.w3.org/2001/XMLSchema#time");
        values.add("http://www.w3.org/2001/XMLSchema#date");
        values.add("urn:oasis:names:tc:xacml:1.0:data-type:x500Name");
        values.add("urn:oasis:names:tc:xacml:2.0:data-type:ipAddress");
        return values;
    }
}
