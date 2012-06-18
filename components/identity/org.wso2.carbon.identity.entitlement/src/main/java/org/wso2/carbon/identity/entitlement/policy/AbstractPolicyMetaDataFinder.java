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

import org.wso2.carbon.identity.entitlement.EntitlementConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Abstract implementation of PolicyMetaDataFinderModule
 */
public abstract class AbstractPolicyMetaDataFinder implements PolicyMetaDataFinderModule{

    @Override
    public Set<String> getSupportedAttributeIds(String attributeType) throws Exception {
        return null;
    }

    @Override
    public boolean isFullPathSupported() {
        return true;
    }

    @Override
    public boolean isHierarchicalTree() {
        return true; 
    }

    @Override
    public Set<String> getAttributeDataTypes(String attributeType) throws Exception {
        return null;
    }

    @Override
    public Map<String, String> getSupportedCategories() {
        Map<String, String> newMap = new HashMap<String, String>();
        newMap.put(EntitlementConstants.RESOURCE_CATEGORY_ID, EntitlementConstants.RESOURCE_CATEGORY_URI);
        newMap.put(EntitlementConstants.SUBJECT_CATEGORY_ID, EntitlementConstants.SUBJECT_CATEGORY_URI);
        newMap.put(EntitlementConstants.ACTION_CATEGORY_ID, EntitlementConstants.ACTION_CATEGORY_URI);
        newMap.put(EntitlementConstants.ENVIRONMENT_CATEGORY_ID, EntitlementConstants.ENVIRONMENT_CATEGORY_URI);
        return newMap;
    }

    
    @Override
    public String getDefaultAttributeId(String category) {
        if(EntitlementConstants.RESOURCE_CATEGORY_ID.equals(category)){
            return EntitlementConstants.RESOURCE_ID_DEFAULT;
        } else if(EntitlementConstants.ACTION_CATEGORY_ID.equals(category)){
            return EntitlementConstants.ACTION_ID_DEFAULT;
        } else if(EntitlementConstants.SUBJECT_CATEGORY_ID.equals(category)){
            return EntitlementConstants.SUBJECT_ID_DEFAULT;
        } else if(EntitlementConstants.ENVIRONMENT_CATEGORY_ID.equals(category)){
            return EntitlementConstants.ENVIRONMENT_ID_DEFAULT;
        }

        return null;
    }

    @Override
    public String getDefaultAttributeDataType(String category) {
        return EntitlementConstants.STRING_DATA_TYPE;
    }
}
