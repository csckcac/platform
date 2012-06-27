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
package org.wso2.carbon.autoscaler.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.jclouds.aws.ec2.compute.AWSEC2TemplateOptions;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.domain.NodeMetadata.Status;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.openstack.nova.v2_0.compute.options.NovaTemplateOptions;
import org.wso2.carbon.autoscaler.service.IAutoscalerService;
import org.wso2.carbon.autoscaler.service.jcloud.ComputeServiceBuilder;
import org.wso2.carbon.autoscaler.service.util.IaaSProvider;
import org.wso2.carbon.autoscaler.service.util.IaaSProviderComparator;
import org.wso2.carbon.autoscaler.service.xml.AutoscalerConfigFileReader;
import org.wso2.carbon.lb.common.persistence.AgentPersistenceManager;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Autoscaler Service is responsible for starting up new server instances, terminating already
 * started instances, providing pending instance count.
 * 
 */
public class AutoscalerServiceImpl implements IAutoscalerService {

    AgentPersistenceManager agentPersistenceManager =
        AgentPersistenceManager.getPersistenceManager();

    private static final Log log = LogFactory.getLog(AutoscalerServiceImpl.class);
    
    /**
     * pointer to Carbon Home directory.
     */
    private String carbonHome = CarbonUtils.getCarbonHome();

    /**
     * List of all IaaSProviders specified in the config file.
     */
    private List<IaaSProvider> iaasProviders;

    /**
     * List of all ServiceTemplate objects.
     */
    private List<org.wso2.carbon.autoscaler.service.util.ServiceTemplate> temps;

    /**
     * We keep an enum which contains all supported IaaSes.
     */
    private enum iaases {
        ec2, lxc
    };

    /**
     * Scale up order of IaaSes.
     */
    private List<Enum<iaases>> scaleUpOrder = new ArrayList<Enum<iaases>>();

    /**
     * Scale down order of IaaSes.
     */
    private List<Enum<iaases>> scaleDownOrder = new ArrayList<Enum<iaases>>();

    /**
     * We need this map to find the respective node Id assigned by JClouds for each
     * spawned instance.
     */
    private Map<String, String> nodeIdToDomainMap = new HashMap<String, String>();

    /**
     * List which keeps Iaas objects
     */
    private List<Iaas> iaasEntities = new ArrayList<Iaas>();

    /**
     * We keep track of the lastly built Iaas object.
     */
    private Map<String, Iaas> domainToLastlyBuiltIaasMap = new HashMap<String, Iaas>();

    @Override
    public boolean initAutoscaler(boolean isSpi) {

        // load configuration file
        AutoscalerConfigFileReader configReader = new AutoscalerConfigFileReader();

        // from config file, we grab the details unique to IaaS providers.
        iaasProviders = configReader.getIaasProvidersList();

        // from config file, we grab the details related to each service domain.
        temps = configReader.getTemplates();

        // we iterate through each IaaSProvider which is loaded from the config file.
        for (IaaSProvider iaas : iaasProviders) {

            // build the JClouds specific ComputeService object
            ComputeService computeService = ComputeServiceBuilder.buildComputeService(iaas);

            // build JClouds Template objects according to different IaaSes
            if (iaas.getName().equalsIgnoreCase(iaases.ec2.toString())) {

                // add to the compute service map
                // computeServiceMap.put(iaases.ec2, computeService);
                Iaas entity = new Iaas(iaases.ec2, computeService);

                // we should build the templates only if this is not SPI stuff
                if (!isSpi) {
                    // Build the Template
                    buildEC2Templates(entity, iaas.getTemplate(), isSpi);

                } else {

                    // add to data structure
                    iaasEntities.add(entity);
                }

            } else if (iaas.getName().equalsIgnoreCase(iaases.lxc.toString())) {
                // computeServiceMap.put(iaases.lxc, computeService);

                Iaas entity = new Iaas(iaases.lxc, computeService);

                // we should build the templates only if this is not SPI stuff
                if (!isSpi) {
                    // Build the Template
                    buildLXCTemplates(entity, iaas.getTemplate(), isSpi);

                } else {

                    // add to data structure
                    iaasEntities.add(entity);
                }

            } else {
                throw new RuntimeException("Unsupported IaaS! " + iaas.getName());
            }
        }

        // populate scale up order
        fillInScaleUpOrder();

        // populate scale down order
        fillInScaleDownOrder();

        return true;
    }

    

    @Override
    public boolean startInstance(String domainName) {

        // FIXME the instanceId param can be removed, given that we pass domain to terminate method!

        ComputeService computeService;
        Template template;
        // List<Template> templates;

        log.info("Starting new instance of domain : " + domainName);

        // traverse through scale up order
        for (Enum<iaases> iaas : scaleUpOrder) {

            // find the matching data holder
            Iaas iaasTemp = findIaas(iaas);

            if (iaasTemp == null) {
                log.warn("Failed to start an instance in " + iaas + "" +
                    ". Hence, will try to start in another IaaS if available.");
                continue;
            }

            // get the ComputeService
            computeService = iaasTemp.getComputeService();// computeServiceMap.get(iaas);

            // get the list of Templates for this IaaS
            // templates = templatesMap.get(iaas);

            // from the list grab the Template corresponds to this domain
            template = iaasTemp.getTemplate(domainName);// templates.get(domainsMap.get(iaas).indexOf(domainName));

            // generate the group id from domain name, by replacing "." with a "-"
            String group = domainName.contains(".") ? domainName.replace('.', '-') : domainName;

            try {
                // create and start a node
                Set<? extends NodeMetadata> nodes =
                    computeService.createNodesInGroup(group, 1, template);

                NodeMetadata node = nodes.iterator().next();

                // get the id of the started node
                // String id = node.getId();

                // add the details of the started node to maps
                iaasTemp.addNode(node, domainName);
                replaceIaas(iaasTemp);
                domainToLastlyBuiltIaasMap.put(domainName, iaasTemp);
                // nodeIdsMap.put(iaasTemp, id);
                // nodeIdToDomainMap.put(id, domainName);
                // addToIaasToNodeIdsMap(iaas, id);

                // FIXME remove later
                log.info("*************** getProviderId = " + node.getProviderId());
                log.info("*************** getType = " + node.getType().toString());

            } catch (RunNodesException e) {
                log.warn("Failed to start an instance in " + iaas + "" +
                    ". Hence, will try to start in another IaaS if available.", e);
                continue;
            }

            log.info("Done.... Started...");
            return true;
        }

        return false;

    }

    @Override
    public String startSpiInstance(String domainName, String imageId) {

        Iaas entry;
        Enum<iaases> iaas;

        // FIXME: Build the Templates, for now we're doing a hack here. I don't know whether
        // there's a proper fix.
        if (imageId.startsWith("nova") && ((entry = findIaas(iaases.lxc)) != null)) {
            iaas = iaases.lxc;
            buildLXCTemplates(entry, imageId, true);
        } else if (((entry = findIaas(iaases.ec2)) != null)) {
            iaas = iaases.ec2;
            buildEC2Templates(entry, imageId, true);
        } else {
            throw new RuntimeException("Invalid image id!!");
        }

        NodeMetadata node = findIaas(iaas).getLastMatchingNode(domainName);

        if (startInstance(domainName) && node != null && node.getPublicAddresses().size() > 0) {
            return node.getPublicAddresses().iterator().next();
        }

        return "";

    }

    @Override
    public boolean terminateInstance(String domainName) {

        // traverse in scale down order
        for (Enum<iaases> iaas : scaleDownOrder) {

            String msg =
                "Failed to terminate an instance in " + iaas.toString() + "" +
                    ". Hence, will try to terminate an instance in another IaaS if possible.";

            // find the matching data holder
            Iaas iaasTemp = findIaas(iaas);

            if (iaasTemp == null) {
                log.warn(msg + " : Reason- Iaas' data cannot be located!");
                continue;
            }

            // grab the first node maps with the given domain
            NodeMetadata node = iaasTemp.getFirstMatchingNode(domainName);

            if (node == null) {
                log.warn(msg + " : Reason- No matching instance found for domain '" + domainName +
                    "'.");
                continue;
            }

            // terminate it!
            terminate(iaasTemp, node);

            return true;

            // if (iaasToNodeIdsMap.containsKey(iaas) && computeServiceMap.containsKey(iaas)) {
            // // find a node which is spawned in this IaaS
            // for (String nodeId : iaasToNodeIdsMap.get(iaas)) {
            //
            // // if it matches the correct domain
            // if (domainName.equals(nodeIdToDomainMap.get(nodeId))) {
            // // found the correct node to destroy
            // computeServiceMap.get(iaas).destroyNode(nodeId);
            //
            // // remove from the maps
            // nodeIdToDomainMap.remove(nodeId);
            // iaasToNodeIdsMap.get(iaas).remove(nodeId);
            //
            // log.info("******** Terminated! Node Id: " + nodeId);
            // return true;
            // }
            // }
            // }
        }

        return false;

    }

    @Override
    public boolean terminateLastlySpawnedInstance(String domainName) {

        if (domainToLastlyBuiltIaasMap.containsKey(domainName)) {
            Iaas iaasTemp = domainToLastlyBuiltIaasMap.get(domainName);

            String msg =
                "Failed to terminate the lastly spawned instance of '" + domainName +
                    "' service domain.";

            if (iaasTemp == null) {
                log.error(msg + " : Reason- Iaas' data cannot be located!");
                return false;
            }

            // grab the node maps with the given public IP address
            NodeMetadata node = iaasTemp.getLastMatchingNode(domainName);

            if (node == null) {
                log.error(msg + " : Reason- No matching instance found for domain '" + domainName +
                    "'.");
                return false;
            }

            // terminate it!
            terminate(iaasTemp, node);

            return true;

        }

        return false;
    }

    @Override
    public boolean terminateSpiInstance(String publicIp) {

        // traverse in scale down order
        for (Enum<iaases> iaas : scaleDownOrder) {

            String msg =
                "Failed to terminate an instance in " + iaas.toString() + "" +
                    ". Hence, will try to terminate an instance in another IaaS if possible.";

            // find the matching data holder
            Iaas iaasTemp = findIaas(iaas);

            if (iaasTemp == null) {
                log.warn(msg + " : Reason- Iaas' data cannot be located!");
                continue;
            }

            // grab the node maps with the given public IP address
            NodeMetadata node = iaasTemp.getNodeWithPublicIp(publicIp);

            if (node == null) {
                log.warn(msg + " : Reason- No matching instance found for public ip '" + publicIp +
                    "'.");
                continue;
            }

            // terminate it!
            terminate(iaasTemp, node);

            return true;
        }
        return false;
    }

    private void terminate(Iaas iaasTemp, NodeMetadata node) {
        // gets the node id
        String nodeId = node.getId();

        // destroy the node
        iaasTemp.getComputeService().destroyNode(nodeId);

        // remove the reference to the node from the data structure.
        iaasTemp.removeNode(node);

        // replace this Iaas instance, as it reflects the new changes.
        replaceIaas(iaasTemp);

        log.info("******** Terminated! Node Id: " + nodeId);
    }

    @Override
    public int getPendingInstanceCount(String domainName) {

        int pendingInstanceCount = 0;

        for (Iaas entry : iaasEntities) {

            ComputeService computeService = entry.getComputeService();

            // get list of node Ids which are belong to the requested domain
            List<String> nodeIds = entry.getNodeIds(domainName);

            Set<? extends ComputeMetadata> set = computeService.listNodes();

            Iterator<? extends ComputeMetadata> iterator = set.iterator();

            // traverse through all nodes of this ComputeService object
            while (iterator.hasNext()) {
                org.jclouds.compute.domain.internal.NodeMetadataImpl nodeMetadata =
                    (org.jclouds.compute.domain.internal.NodeMetadataImpl) iterator.next();

                // if this node belongs to the requested domain
                if (nodeIds.contains(nodeMetadata.getId())) {
                    // org.jclouds.compute.domain. nodeState = nodeMetadata.gets.getState();
                    Status nodeStatus = nodeMetadata.getStatus();

                    // count nodes that are in pending state
                    if (nodeStatus.toString().equalsIgnoreCase("PENDING")) {
                        pendingInstanceCount++;
                    }
                }

            }
        }

        // // get list of node Ids which are belong to the requested domain
        // List<String> nodeIds = getNodeIds(domainName);
        //
        // // traverse through all compute service objects in the map
        // for (ComputeService computeService : computeServiceMap.values()) {
        //
        // Set<? extends ComputeMetadata> set = computeService.listNodes();
        //
        // Iterator<? extends ComputeMetadata> iterator = set.iterator();
        //
        // // traverse through all nodes of this ComputeService object
        // while (iterator.hasNext()) {
        // org.jclouds.compute.domain.internal.NodeMetadataImpl nodeMetadata =
        // (org.jclouds.compute.domain.internal.NodeMetadataImpl) iterator.next();
        //
        // // if this node belongs to the requested domain
        // if (nodeIds.contains(nodeMetadata.getId())) {
        // // org.jclouds.compute.domain. nodeState = nodeMetadata.gets.getState();
        // Status nodeStatus = nodeMetadata.getStatus();
        //
        // // count nodes that are in pending state
        // if (nodeStatus.toString().equalsIgnoreCase("PENDING")) {
        // pendingInstanceCount++;
        // }
        // }
        //
        // }
        // }

        log.info("*********** pending instance count " + pendingInstanceCount);

        return pendingInstanceCount;

    }

    private Iaas findIaas(Enum<iaases> iaas) {

        for (Iaas entry : iaasEntities) {
            if (entry.getName().equals(iaas)) {
                return entry;
            }
        }

        return null;
    }

    // private List<String> getNodeIds(String domainName) {
    //
    // List<String> nodeIds = new ArrayList<String>();
    //
    // // traverse through all the entries
    // for (Entry<String, String> entry : nodeIdToDomainMap.entrySet()) {
    // // find entries with matching domain name
    // if (entry.getValue().equals(domainName)) {
    // // add them to the list
    // nodeIds.add(entry.getKey());
    // }
    // }
    //
    // return nodeIds;
    // }

    // private void addToIaasToNodeIdsMap(Enum<iaases> iaas, String id) {
    //
    // List<String> nodeIds;
    //
    // if (iaasToNodeIdsMap.containsKey(iaas)) {
    // nodeIds = iaasToNodeIdsMap.get(iaas);
    // } else {
    // nodeIds = new ArrayList<String>();
    // }
    //
    // nodeIds.add(id);
    // iaasToNodeIdsMap.put(iaas, nodeIds);
    // }

    // private void addToTemplatesMap(iaases key, Template temp) {
    //
    // List<Template> tempList;
    //
    // if (!templatesMap.containsKey(key)) {
    // tempList = new ArrayList<Template>();
    //
    // } else {
    // tempList = templatesMap.get(key);
    // }
    //
    // tempList.add(temp);
    //
    // templatesMap.put(key, tempList);
    //
    // }
    //
    // private void addToDomainsMap(iaases key, String domain) {
    //
    // List<String> domainList;
    //
    // if (!domainsMap.containsKey(key)) {
    // domainList = new ArrayList<String>();
    //
    // } else {
    // domainList = domainsMap.get(key);
    // }
    //
    // if (!domainList.contains(domain)) {
    // domainList.add(domain);
    // }
    //
    // domainsMap.put(key, domainList);
    //
    // }

    private void fillInScaleDownOrder() {
        Collections.sort(iaasProviders,
                         IaaSProviderComparator.ascending(IaaSProviderComparator.getComparator(IaaSProviderComparator.SCALE_DOWN_SORT)));

        for (IaaSProvider iaas : iaasProviders) {
            scaleDownOrder.add(iaases.valueOf(iaas.getName()));
        }
    }

    private void fillInScaleUpOrder() {

        Collections.sort(iaasProviders,
                         IaaSProviderComparator.ascending(IaaSProviderComparator.getComparator(IaaSProviderComparator.SCALE_UP_SORT)));

        for (IaaSProvider iaas : iaasProviders) {
            scaleUpOrder.add(iaases.valueOf(iaas.getName()));
        }
    }

    public byte[] getUserData(String payloadFileName) {
        // String userData = null;
        byte[] bytes = null;
        try {
            File file = new File(payloadFileName);
            if (!file.exists()) {
                handleException("Payload file " + payloadFileName + " does not exist");
            }
            if (!file.canRead()) {
                handleException("Payload file " + payloadFileName + " does cannot be read");
            }
            bytes = getBytesFromFile(file);

        } catch (IOException e) {
            handleException("Cannot read data from payload file " + payloadFileName, e);
        }
        return bytes;
    }

    /**
     * Returns the contents of the file in a byte array
     * 
     * @param file
     *            - Input File
     * @return Bytes from the file
     * @throws java.io.IOException
     *             , if retrieving the file contents failed.
     */
    public byte[] getBytesFromFile(File file) throws IOException {
        if (!file.exists()) {
            log.error("Payload file " + file.getAbsolutePath() + " does not exist");
            return null;
        }
        InputStream is = new FileInputStream(file);
        byte[] bytes;

        try {
            // Get the size of the file
            long length = file.length();

            // You cannot create an array using a long type.
            // It needs to be an int type.
            // Before converting to an int type, check
            // to ensure that file is not larger than Integer.MAX_VALUE.
            if (length > Integer.MAX_VALUE) {
                if (log.isDebugEnabled()) {
                    log.debug("File is too large");
                }
            }

            // Create the byte array to hold the data
            bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead;
            while (offset < bytes.length &&
                (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }
        } finally {
            // Close the input stream and return bytes
            is.close();
        }

        return bytes;
    }

    /**
     * handles the exception
     * 
     * @param msg
     *            exception message
     */
    public void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    /**
     * handles the exception
     * 
     * @param msg
     *            exception message
     * @param e
     *            exception
     */
    public static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

    /**
     * This object holds all IaaS related runtime data.
     */
    private class Iaas {
        // name of the IaaS
        private Enum<iaases> name;
        Map<String, Template> domainToTemplateMap = new HashMap<String, Template>();
        ComputeService computeService;
        Map<NodeMetadata, String> nodeToDomainMap = new LinkedHashMap<NodeMetadata, String>();

        public Iaas(Enum<iaases> name, ComputeService computeService) {
            this.name = name;
            this.computeService = computeService;
        }

        public Enum<iaases> getName() {
            return name;
        }

        // public void setName(Enum<iaases> name) {
        // this.name = name;
        // }
        //
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

        public NodeMetadata getLastMatchingNode(String domain) {
            ListIterator<Map.Entry<NodeMetadata, String>> iter =
                new ArrayList(nodeToDomainMap.entrySet()).listIterator(nodeToDomainMap.size());

            while (iter.hasPrevious()) {
                Map.Entry<NodeMetadata, String> entry = iter.previous();
                if (entry.getValue().equals(domain)) {
                    return entry.getKey();
                }
            }

            // for (Entry<NodeMetadata, String> entry : nodeToDomainMap.entrySet()) {
            // if(entry.getValue().equals(domain)){
            // return entry.getKey();
            // }
            // }
            return null;
        }

        public NodeMetadata getFirstMatchingNode(String domain) {
            for (Entry<NodeMetadata, String> entry : nodeToDomainMap.entrySet()) {
                if (entry.getValue().equals(domain)) {
                    return entry.getKey();
                }
            }
            return null;
        }

        public NodeMetadata getNodeWithPublicIp(String publicIp) {
            for (NodeMetadata node : nodeToDomainMap.keySet()) {
                if (node.getPublicAddresses().iterator().next().equals(publicIp)) {
                    return node;
                }
            }

            return null;
        }

        public List<String> getNodeIds(String domain) {

            List<String> nodeIds = new ArrayList<String>();

            for (Entry<NodeMetadata, String> entry : nodeToDomainMap.entrySet()) {
                if (entry.getValue().equals(domain)) {
                    nodeIds.add(entry.getKey().getId());
                }
            }

            return nodeIds;
        }

        public void removeNode(NodeMetadata node) {
            nodeIdToDomainMap.remove(node);
        }

        public boolean equals(Object obj) {

            if (obj instanceof Iaas) {
                return this.getName().equals(((Iaas) obj).getName());
            }
            return false;

        }

    }

    /**
     * This will replace an existing entry in iaasEntities list, if there's such.
     * If not this will add the replacement value to the list.
     * 
     * @param replacement
     *            Iaas entry to be added.
     */
    private void replaceIaas(Iaas replacement) {
        for (Iaas entry : iaasEntities) {
            if (entry.equals(replacement)) {
                int idx = iaasEntities.indexOf(entry);
                iaasEntities.remove(idx);
                iaasEntities.add(idx, replacement);
                return;
            }
        }
        iaasEntities.add(replacement);
    }
    
    private void buildLXCTemplates(Iaas entity, String imageId, boolean blockUntilRunning) {

        if (entity.getComputeService() == null) {
            throw new RuntimeException("Compute service is null for IaaS provider: " +
                entity.getName());
        }

        TemplateBuilder templateBuilder = entity.getComputeService().templateBuilder();
        templateBuilder.imageId(imageId);

        // to avoid creation of template objects in each and every time, we create all
        // at once!
        for (org.wso2.carbon.autoscaler.service.util.ServiceTemplate temp : temps) {

            // templateBuilder.hardwareId(temp.getProperty("instanceType"));
            Template template = templateBuilder.build();

            template.getOptions().as(TemplateOptions.class).blockUntilRunning(blockUntilRunning);
            // template.getOptions().as(NovaTemplateOptions.class)
            // .placementGroup(temp.getProperty("availabilityZone"));

            template.getOptions().as(NovaTemplateOptions.class)
                    .securityGroupNames(temp.getProperty("securityGroups").split(","));

            template.getOptions().as(NovaTemplateOptions.class).userData(
             getUserData(carbonHome + File.separator + temp.getProperty("payload")));

            template.getOptions().as(NovaTemplateOptions.class)
                    .keyPairName(temp.getProperty("keyPair"));

            // add to data structure
            // add to the data structure
            entity.addToDomainToTemplateMap(temp.getDomainName(), template);

            // add to domains map
            // addToDomainsMap(iaases.lxc, temp.getDomainName());

            // adds to template map
            // addToTemplatesMap(iaases.lxc, template);
        }

        replaceIaas(entity);
    }

    private void buildEC2Templates(Iaas entity, String imageId, boolean blockUntilRunning) {

        if (entity.getComputeService() == null) {
            throw new RuntimeException("Compute service is null for IaaS provider: " +
                entity.getName());
        }

        TemplateBuilder templateBuilder = entity.getComputeService().templateBuilder();

        // set image id specified
        templateBuilder.imageId(imageId);

        // to avoid creation of template objects in each and every time, we create all
        // at once! FIXME we could use caching and lazy loading
        for (org.wso2.carbon.autoscaler.service.util.ServiceTemplate temp : temps) {

            // set instance type eg: m1.large
            templateBuilder.hardwareId(temp.getProperty("instanceType"));

            // build the Template
            Template template = templateBuilder.build();

            // make it non blocking
            template.getOptions().as(TemplateOptions.class).blockUntilRunning(blockUntilRunning);

            // set EC2 specific options
            template.getOptions().as(AWSEC2TemplateOptions.class)
                    .placementGroup(temp.getProperty("availabilityZone"));

            template.getOptions().as(AWSEC2TemplateOptions.class)
                    .securityGroups(temp.getProperty("securityGroups").split(","));

             template.getOptions().as(AWSEC2TemplateOptions.class).userData(
             getUserData(carbonHome + File.separator + temp.getProperty("payload")));

            template.getOptions().as(AWSEC2TemplateOptions.class)
                    .keyPair(temp.getProperty("keyPair"));

            // add to the data structure
            entity.addToDomainToTemplateMap(temp.getDomainName(), template);

            // // add to domains map
            // addToDomainsMap(iaases.ec2, temp.getDomainName());
            //
            // // adds to template map
            // addToTemplatesMap(iaases.ec2, template);
        }

        replaceIaas(entity);

    }

}
