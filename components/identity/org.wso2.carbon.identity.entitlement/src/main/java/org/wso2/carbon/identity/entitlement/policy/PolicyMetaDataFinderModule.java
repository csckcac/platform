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

import org.wso2.carbon.identity.entitlement.dto.AttributeValueTreeNodeDTO;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * When creating XACML policies from WSO2 Identity server, We can define set of pre-defined attribute
 * values, attribute ids, function and so on.  These data can be retrieved from external sources such as
 * databases,  LDAPs,  or file systems. This interface provides the flexibility for this.
 */
public interface PolicyMetaDataFinderModule {

    /**
     * initializes data retriever module
	 * @param properties properties, that need to initialize the module. These properties can be
     * defined in pip-config.xml file
     * @throws Exception throws when initialization is failed
     */
    public void init(Properties properties) throws Exception;

    /**
     * gets name of this module
     * @return name as String
     */
    public String getModuleName();

    /**
     * finds attribute values for given attribute type
     * @param category
     * @return Set of attribute values as String Set
     * @throws Exception throws if fails
     */
    public AttributeValueTreeNodeDTO getAttributeValueData(String category) throws Exception;

    /**
     * gets support attribute ids from this module
     * @param category
     * @return  support attribute ids as String Set
     * @throws Exception throws, if fails
     */
    public Set<String> getSupportedAttributeIds(String category) throws Exception;

    /**
     * gets data types of the attribute values
     * @param category
     * @return support data types as String Set
     * @throws Exception throws, if fails
     */
    public Set<String> getAttributeDataTypes(String category) throws Exception;

    /**
     * defines whether node (AttributeValueTreeNodeDTO) is defined by child node name
     * or by full path name with parent node names
     * @return true or false
     */
    public boolean isFullPathSupported();

    /**
     * defines whether nodes (AttributeValueTreeNodeDTOs) are shown in UI by as a tree or flat
     * @return  if as a tree -> true or else -> false
     */
    public boolean isHierarchicalTree();


    public Map<String, String> getSupportedCategories();

    public Map<String, String> getSupportedRuleFunctions();

    public Map<String, String> getSupportedTargetFunctions();

    public String getDefaultAttributeId(String category);

    public String getDefaultAttributeDataType(String category);

    public Set<String> getSupportedPreFunctions();




}
