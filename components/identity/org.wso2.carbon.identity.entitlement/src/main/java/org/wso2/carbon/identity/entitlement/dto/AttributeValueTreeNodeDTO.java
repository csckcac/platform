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

package org.wso2.carbon.identity.entitlement.dto;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * encapsulate attribute value node of a attribute tree which shows in the XACML policy editor UI
 */
public class AttributeValueTreeNodeDTO {

    /**
     * Node name
     */
    private String name;

    /**
     * Attribute type of this node
     */
    private String categoryId;

    /**
     * Attribute type of this node
     */
    private String categoryUri;

    /**
     * data type of the attribute
     */
    private String[] attributeDataTypes = new String[]{};

    /**
     * supported attribute ids for this module 
     */
    private String[] supportedAttributeIds = new String[]{};


    /**
     * whether node is defined by child node name or by full path name with parent node names
     */
    private boolean fullPathSupported;


    private boolean hierarchicalTree;

    /**
     * module name which node has been created.
     */
    private String moduleName;

    /**
     * children of the Node
     */
    private AttributeValueTreeNodeDTO[] childNodes = new AttributeValueTreeNodeDTO[]{};

    /**
     *
     */
    private String defaultAttributeDataType;

    /**
     * 
     */
    private String defaultAttributeId;

    public AttributeValueTreeNodeDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryUri() {
        return categoryUri;
    }

    public void setCategoryUri(String categoryUri) {
        this.categoryUri = categoryUri;
    }

    public AttributeValueTreeNodeDTO[] getChildNodes() {
        return  Arrays.copyOf(childNodes, childNodes.length);
    }

    public boolean isFullPathSupported() {
        return fullPathSupported;
    }

    public void setFullPathSupported(boolean fullPathSupported) {
        this.fullPathSupported = fullPathSupported;
    }

    public boolean isHierarchicalTree() {
        return hierarchicalTree;
    }

    public void setHierarchicalTree(boolean hierarchicalTree) {
        this.hierarchicalTree = hierarchicalTree;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String[] getAttributeDataTypes() {
        return  Arrays.copyOf(attributeDataTypes, attributeDataTypes.length);
    }

    public void setAttributeDataTypes(String[] attributeDataTypes) {
        this.attributeDataTypes = Arrays.copyOf(attributeDataTypes, attributeDataTypes.length);
    }

    public void setSupportedAttributeIds(String[] supportedAttributeIds) {
        this.supportedAttributeIds = Arrays.copyOf(supportedAttributeIds, supportedAttributeIds.length);
    }

    public void addAttributeDataType(String attributeDataType) {
        Set<String> attributeDataTypes = new HashSet<String>(Arrays.asList(this.attributeDataTypes));
        attributeDataTypes.add(attributeDataType);
        this.attributeDataTypes = attributeDataTypes.toArray(new String[attributeDataTypes.size()]);
    }

    public void setChildNodes(AttributeValueTreeNodeDTO[] childNodes) {
        this.childNodes = Arrays.copyOf(childNodes, childNodes.length);
    }

    public void addChildNode(AttributeValueTreeNodeDTO node){
        Set<AttributeValueTreeNodeDTO> valueNodes = new HashSet<AttributeValueTreeNodeDTO>(Arrays.asList(this.childNodes));
        valueNodes.add(node);
        this.childNodes = valueNodes.toArray(new AttributeValueTreeNodeDTO[valueNodes.size()]);
    }

    public String[] getSupportedAttributeIds() {
        return  Arrays.copyOf(supportedAttributeIds, supportedAttributeIds.length);
    }

    public void addSupportedAttributeId(String supportedAttributeId) {
        Set<String> attributeIds = new HashSet<String>(Arrays.asList(this.supportedAttributeIds));
        attributeIds.add(supportedAttributeId);
        this.supportedAttributeIds = attributeIds.toArray(new String[attributeIds.size()]);
    }

    public String getDefaultAttributeDataType() {
        return defaultAttributeDataType;
    }

    public void setDefaultAttributeDataType(String defaultAttributeDataType) {
        this.defaultAttributeDataType = defaultAttributeDataType;
    }

    public String getDefaultAttributeId() {
        return defaultAttributeId;
    }

    public void setDefaultAttributeId(String defaultAttributeId) {
        this.defaultAttributeId = defaultAttributeId;
    }


    
}
