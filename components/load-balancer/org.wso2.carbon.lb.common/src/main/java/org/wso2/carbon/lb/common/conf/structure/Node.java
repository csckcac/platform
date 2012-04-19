/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.lb.common.conf.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the basic data structure which holds a Nginx formatted configuration file.
 * TODO: Introduce a DOM based API
 */
public class Node{
    
    private String name;
    
    /**
     * To mark the end of construction of this {@link #Node}.
     */
    private boolean fullyConstructed = false;

    /**
     * Every node can have 0..n nodes. Following keep them tracked.
     */
    private List<Node> nodes = new ArrayList<Node>();
    
    /**
     * Every node can have 0..n properties. Following Map keeps them.
     * Key: property name
     * Value: property value
     */
    private Map<String, String> properties = new HashMap<String, String>();

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(Node aNode) {
        if (aNode != null) {
            nodes.add(aNode);
        }
    }

    public Map<String, String> getProperties() {
        return properties;
    }
    
    public String getProperty(String key) {
        if(properties.get(key) == null){
            return null;
        }
        return properties.get(key).trim();
    }

    public void setProperties(String key, String value) {
        properties.put(key, value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Node findChildNodeByName(String name) {
        for (Node aNode : nodes) {
            if(aNode.getName().equals(name)){
                return aNode;
            }
        }
        
        return null;
    }

    public boolean isFullyConstructed() {
        return fullyConstructed;
    }

    public void setFullyConstructed(boolean fullyConstructed) {
        this.fullyConstructed = fullyConstructed;
    }
    
    public String getPropertiesString() {
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, String> entry : properties.entrySet()){
            sb.append(entry.getKey()+"\t"+entry.getValue()+"\n");
        }
        return sb.toString();
    }
 
    
}
