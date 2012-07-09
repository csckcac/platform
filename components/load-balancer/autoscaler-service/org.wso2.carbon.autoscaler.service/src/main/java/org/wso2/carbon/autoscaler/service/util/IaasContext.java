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
package org.wso2.carbon.autoscaler.service.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.Template;
import org.wso2.carbon.autoscaler.service.impl.AutoscalerServiceImpl.Iaases;

/**
 * This object holds all IaaS related runtime data.
 */
public class IaasContext implements Serializable{
   
    private static final long serialVersionUID = -922284976926131383L;
    
    // name of the IaaS
    private Enum<Iaases> name;
    private transient Map<String, Template> domainToTemplateMap;
    private transient ComputeService computeService;
    
    // Since Jclouds' NodeMetadata object contains unserializable objects, I had to use 3 maps.
    private Map<String, String> nodeIdToDomainMap = new LinkedHashMap<String, String>();
    private Map<String, String> publicIpToDomainMap = new LinkedHashMap<String, String>();
    private Map<String, String> publicIpToNodeIdMap = new LinkedHashMap<String, String>();
    
    private int scaleUpOrder, scaleDownOrder;

    public IaasContext(Enum<Iaases> name, ComputeService computeService) {
        this.name = name;
        this.computeService = computeService;
        domainToTemplateMap = new HashMap<String, Template>();
    }

    public Enum<Iaases> getName() {
        return name;
    }

    public void addToDomainToTemplateMap(String key, Template value) {
        domainToTemplateMap.put(key, value);
    }

    public Template getTemplate(String key) {
        return domainToTemplateMap.get(key);
    }

    public ComputeService getComputeService() {
        return computeService;
    }
    
    public void setComputeService(ComputeService computeService) {
        this.computeService = computeService;
    }

    public void addNodeIdToDomainMap(String nodeId, String domain) {
        nodeIdToDomainMap.put(nodeId, domain);
    }
    
    public void addPublicIpToDomainMap(String ip, String domain) {
        publicIpToDomainMap.put(ip, domain);
    }
    
    public void addPublicIpToNodeIdMap(String ip, String nodeId) {
        publicIpToNodeIdMap.put(ip, nodeId);
    }

    /**
     * This will return the node id of the node which is belong to the
     * requesting domain and which is the most recently created. If it cannot find a
     * matching node id, this will return <code>null</code>.
     * @param domain service domain. 
     * @return the node Id of the node
     */
    public String getLastMatchingNode(String domain) {
        ListIterator<Map.Entry<String, String>> iter =
            new ArrayList<Entry<String, String>>(nodeIdToDomainMap.entrySet()).
                                listIterator(nodeIdToDomainMap.size());

        while (iter.hasPrevious()) {
            Map.Entry<String, String> entry = iter.previous();
            if (entry.getValue().equals(domain)) {
                return entry.getKey();
            }
        }
        
        return null;
    }
    
    /**
     * This will return the public IP of the node which is belong to the
     * requesting domain and which is the most recently created. If it cannot find a
     * matching public IP, this will return <code>null</code>.
     * @param domain service domain. 
     * @return the public IP of the node
     */
    public String getLastMatchingPublicIp(String domain) {
        // traverse from the last entry of the map
        ListIterator<Map.Entry<String, String>> iter =
            new ArrayList<Entry<String, String>>(publicIpToDomainMap.entrySet()).
                                listIterator(publicIpToDomainMap.size());

        while (iter.hasPrevious()) {
            Map.Entry<String, String> entry = iter.previous();
            if (entry.getValue().equals(domain)) {
                return entry.getKey();
            }
        }
        
        return null;
    }

    /**
     * This will return the node id of the node which is belong to the
     * requesting domain and which is created at first. If it cannot find a
     * matching node id, this will return <code>null</code>.
     * @param domain service domain.
     * @return node id of the node
     */
    public String getFirstMatchingNode(String domain) {
        for (Entry<String, String> entry : nodeIdToDomainMap.entrySet()) {
            if (entry.getValue().equals(domain)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * This will return the node id of the node which has the given public IP. 
     * If it cannot find a matching node id, this will return 
     * <code>null</code>.
     * @param publicIp public IP of a node.
     * @return node id of the matching node.
     */
    public String getNodeWithPublicIp(String publicIp) {
        for (String node : publicIpToNodeIdMap.keySet()) {
            if (node.equals(publicIp)) {
                return publicIpToNodeIdMap.get(node);
            }
        }

        return null;
    }

    /**
     * This will return a list of node Ids that are started in this IaaS and that are 
     * belong to the given domain.
     * @param domain service domain.
     * @return List of node Ids.
     */
    public List<String> getNodeIds(String domain) {

        List<String> nodeIds = new ArrayList<String>();

        for (Entry<String, String> entry : nodeIdToDomainMap.entrySet()) {
            if (entry.getValue().equals(domain)) {
                nodeIds.add(entry.getKey());
            }
        }

        return nodeIds;
    }

    /**
     * Removes a specific node id from the {@link #nodeIdToDomainMap}.
     * @param node id of the node to be removed.
     */
    public void removeNodeId(String nodeId) {
        nodeIdToDomainMap.remove(nodeId);
    }

    public boolean equals(Object obj) {

        if (obj instanceof IaasContext) {
            return new EqualsBuilder().append(getName(), ((IaasContext) obj).getName()).isEquals();
        }
        return false;

    }
    
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
        append(name).
        toHashCode();
    }

    public int getScaleDownOrder() {
        return scaleDownOrder;
    }

    public void setScaleDownOrder(int scaleDownOrder) {
        this.scaleDownOrder = scaleDownOrder;
    }

    public int getScaleUpOrder() {
        return scaleUpOrder;
    }

    public void setScaleUpOrder(int scaleUpOrder) {
        this.scaleUpOrder = scaleUpOrder;
    }
    
    public void setDomainToTemplateMap(Map<String, Template> map) {
        domainToTemplateMap = map;
    }
    
    public Map<String, Template> getDomainToTemplateMap() {
        return domainToTemplateMap;
    }

}
