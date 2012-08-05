/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.lb.common.conf.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the basic data structure which holds a <i>Nginx</i> formatted configuration file.
 * 
 */
public class Node implements Serializable{

    private static final long serialVersionUID = 4071569903421115370L;

    /**
     * Name of this Node element
     */
    private String name;

    /**
     * Every node can have 0..n child nodes. 
     * They are kept in a List.
     */
    private List<Node> childNodes = new ArrayList<Node>();

    /**
     * Every node can have 0..n properties. 
     * They are kept in a Map, in the order they appear.
     * Key: property name
     * Value: property value
     */
    private Map<String, String> properties = new LinkedHashMap<String, String>();

    /**
     * This will convert each child Node of this Node to a String.
     * @return a string which represents child nodes of this node.
     */
    public String childNodesToString() {
        StringBuilder childNodesString = new StringBuilder();
        
        for (Node node : childNodes) {
            childNodesString.append(node.toString()+"\n");
        }
        
        return childNodesString.toString();
    }

    /**
     * This will try to find a child Node of this Node, which has the given name.
     * @param name name of the child node to find.
     * @return child Node object if found or else <code>null</code>.
     */
    public Node findChildNodeByName(String name) {
        for (Node aNode : childNodes) {
            if (aNode.getName().equals(name)) {
                return aNode;
            }
        }

        return null;
    }

    /**
     * Returns the name of this Node. 
     * @return name of the node.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns child nodes List of this Node.
     * @return List of Node
     */
    public List<Node> getChildNodes() {
        return childNodes;
    }

    /**
     * Returns properties Map of this Node.
     * @return Map whose keys and values are String.  
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Returns the value of a given property.
     * @param key name of a property.
     * @return trimmed value if the property is found in this Node, or else <code>null</code>. 
     */
    public String getProperty(String key) {
        if (properties.get(key) == null) {
            return null;
        }
        return properties.get(key).trim();
    }

    /**
     * Returns all the properties of this Node as a String.
     * Key and value of the property is separated by a tab (\t) character and
     * each property is separated by a new line character.
     * @return properties of this node as a String.
     */
    public String propertiesToString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            sb.append(entry.getKey() + "\t" + entry.getValue() + ";\n");
        }
        return sb.toString();
    }

    /**
     * Sets the name of this Node.
     * @param name String to be set as the name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Appends a child node at the end of the List of child nodes of this Node.
     * @param aNode child Node to be appended.
     */
    public void appendChild(Node aNode) {
        if (aNode != null) {
            childNodes.add(aNode);
        }
    }
    
    /**
     * Adds a new property to properties Map of this Node if and only if 
     * key is not <code>null</code>.
     * @param key name of the property to be added.
     * @param value value of the property to be added.
     */
    public void addProperty(String key, String value) {
        if (key != null) {
            properties.put(key, value);
        }
    }
    
    /**
     * Convert this Node to a String which is in <i>Nginx</i> format.
     * <br/>
     * Sample:
     * <br></br>
     * <code>
     * ij {
     * <br/>
     * klm n;
     * <br/>
     * pq {
     * <br/>
     * rst u;
     * <br/>
     * }
     * <br/>
     * }
     * <br/>
     * </code>
     */
    public String toString() {
        
        String nodeString = 
                getName()+" {\n" +
                (propertiesToString()) +
                (childNodesToString()) +
                "}";
        
        return nodeString;
    }

}
