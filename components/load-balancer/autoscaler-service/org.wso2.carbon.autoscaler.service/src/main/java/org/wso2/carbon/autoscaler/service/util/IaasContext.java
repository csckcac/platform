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
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.wso2.carbon.autoscaler.service.impl.AutoscalerServiceImpl.iaases;

/**
 * This object holds all IaaS related runtime data.
 */
public class IaasContext implements Serializable{
   
    private static final long serialVersionUID = -922284976926131383L;
    
    // name of the IaaS
    private Enum<iaases> name;
    private Map<String, Template> domainToTemplateMap = new HashMap<String, Template>();
    private ComputeService computeService;
    private Map<NodeMetadata, String> nodeToDomainMap = new LinkedHashMap<NodeMetadata, String>();
    private int scaleUpOrder, scaleDownOrder;

    public IaasContext(Enum<iaases> name, ComputeService computeService) {
        this.name = name;
        this.computeService = computeService;
    }

    public Enum<iaases> getName() {
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

    public void addNode(NodeMetadata node, String domain) {
        nodeToDomainMap.put(node, domain);
    }

    /**
     * This will return the <code>NodeMetadata</code> object which is belong to the
     * requesting domain and which is the most recently created. If it cannot find a
     * matching <code>NodeMetadata</code> object, this will return <code>null</code>.
     * @param domain service domain. 
     * @return <code>NodeMetadata</code> instance
     */
    public NodeMetadata getLastMatchingNode(String domain) {
        ListIterator<Map.Entry<NodeMetadata, String>> iter =
            new ArrayList<Entry<NodeMetadata, String>>(nodeToDomainMap.entrySet()).
                                listIterator(nodeToDomainMap.size());

        while (iter.hasPrevious()) {
            Map.Entry<NodeMetadata, String> entry = iter.previous();
            if (entry.getValue().equals(domain)) {
                return entry.getKey();
            }
        }
        
        return null;
    }

    /**
     * This will return the <code>NodeMetadata</code> object which is belong to the
     * requesting domain and which is created at first. If it cannot find a
     * matching <code>NodeMetadata</code> object, this will return <code>null</code>.
     * @param domain service domain.
     * @return <code>NodeMetadata</code> instance
     */
    public NodeMetadata getFirstMatchingNode(String domain) {
        for (Entry<NodeMetadata, String> entry : nodeToDomainMap.entrySet()) {
            if (entry.getValue().equals(domain)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * This will return the <code>NodeMetadata</code> object which has the given public IP. 
     * If it cannot find a matching <code>NodeMetadata</code> object, this will return 
     * <code>null</code>.
     * @param publicIp public IP of a node.
     * @return <code>NodeMetadata</code> instance
     */
    public NodeMetadata getNodeWithPublicIp(String publicIp) {
        for (NodeMetadata node : nodeToDomainMap.keySet()) {
            if (node.getPublicAddresses().iterator().next().equals(publicIp)) {
                return node;
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

        for (Entry<NodeMetadata, String> entry : nodeToDomainMap.entrySet()) {
            if (entry.getValue().equals(domain)) {
                nodeIds.add(entry.getKey().getId());
            }
        }

        return nodeIds;
    }

    /**
     * Removes a specific node from the {@link #nodeToDomainMap}.
     * @param node <code>NodeMetadata</code> instance to be removed.
     */
    public void removeNode(NodeMetadata node) {
        nodeToDomainMap.remove(node);
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

}
