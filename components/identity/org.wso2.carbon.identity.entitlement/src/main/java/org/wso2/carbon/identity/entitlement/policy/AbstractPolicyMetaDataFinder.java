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
    public boolean isEnvironmentAttributeSupported() {
        return true;
    }

    @Override
    public boolean isActionAttributeSupported() {
        return true;
    }

    @Override
    public boolean isResourceAttributeSupported() {
        return true;
    }

    @Override
    public boolean isSubjectAttributeSupported() {
        return true;
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

    protected final String getResourceAttributeTypeId(){
        return "Resource";
    }

    protected final String getActionAttributeTypeId(){
        return "Action";
    }

    protected final String getSubjectAttributeTypeId(){
        return "Subject";
    }

    protected final String getEnvironmentAttributeTypeId(){
        return "Environment";
    }
}
