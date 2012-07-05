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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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
import org.wso2.carbon.autoscaler.service.util.IaasProvider;
import org.wso2.carbon.autoscaler.service.util.IaasContext;
import org.wso2.carbon.autoscaler.service.xml.ElasticScalerConfigFileReader;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Autoscaler Service is responsible for starting up new server instances, terminating already
 * started instances, providing pending instance count.
 * 
 */
public class AutoscalerServiceImpl implements IAutoscalerService {

    private static final Log log = LogFactory.getLog(AutoscalerServiceImpl.class);

    /**
     * pointer to Carbon Home directory.
     */
    private String carbonHome = CarbonUtils.getCarbonHome();

    /**
     * List of all IaaSProviders specified in the config file.
     */
    private List<IaasProvider> iaasProviders;

    /**
     * List of all ServiceTemplate objects.
     */
    private List<org.wso2.carbon.autoscaler.service.util.ServiceTemplate> serviceTemps;

    /**
     * We keep an enum which contains all supported IaaSes.
     */
    public enum iaases {
        ec2, openstack
    };

    /**
     * List which keeps <code>IaasContext</code> objects. TODO this should be persisted.
     */
    private List<IaasContext> iaasContextList = new ArrayList<IaasContext>();

    /**
     * We keep track of the lastly built IaasContext object. TODO this should be persisted.
     */
    private Map<String, IaasContext> domainToLastlyBuiltIaasContextMap = new HashMap<String, IaasContext>();

    @Override
    public boolean initAutoscaler(boolean isSpi) {

        // load configuration file
        ElasticScalerConfigFileReader configReader = new ElasticScalerConfigFileReader();

        // from config file, we grab the details unique to IaaS providers.
        iaasProviders = configReader.getIaasProvidersList();

        // from config file, we grab the details related to each service domain.
        serviceTemps = configReader.getTemplates();

        // we iterate through each IaaSProvider which is loaded from the config file.
        for (IaasProvider iaas : iaasProviders) {

            // build the JClouds specific ComputeService object
            ComputeService computeService = ComputeServiceBuilder.buildComputeService(iaas);

            // build JClouds Template objects according to different IaaSes
            if (iaas.getType().equalsIgnoreCase(iaases.ec2.toString())) {

                // add to the compute service map
                IaasContext entity = new IaasContext(iaases.ec2, computeService);

                // we should build the templates only if this is not SPI stuff
                if (!isSpi) {
                    // Build the Template
                    buildEC2Templates(entity, iaas.getTemplate(), isSpi);

                } else {
                    // add to data structure
                    iaasContextList.add(entity);
                }

            } else if (iaas.getType().equalsIgnoreCase(iaases.openstack.toString())) {

                IaasContext entity = new IaasContext(iaases.openstack, computeService);

                // we should build the templates only if this is not SPI stuff
                if (!isSpi) {
                    // Build the Template
                    buildLXCTemplates(entity, iaas.getTemplate(), isSpi);

                } else {
                    // add to data structure
                    iaasContextList.add(entity);
                }

            } else {
                throw new RuntimeException("Unsupported IaaS! " + iaas.getType());
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

        ComputeService computeService;
        Template template;

        log.info("Starting new instance of domain : " + domainName);

        // sort the IaasContext entities according to scale up order
        Collections.sort(iaasContextList,
                         IaasContextComparator.ascending(IaasContextComparator.getComparator(IaasContextComparator.SCALE_UP_SORT)));

        // traverse through IaasContext object instances in scale up order
        for (IaasContext iaasCtxt : iaasContextList) {

            // get the ComputeService
            computeService = iaasCtxt.getComputeService();

            // from the list grab the Template corresponds to this domain
            template = iaasCtxt.getTemplate(domainName);

            // generate the group id from domain name, by replacing "." with a "-"
            String group = domainName.contains(".") ? domainName.replace('.', '-') : domainName;

            try {
                // create and start a node
                Set<? extends NodeMetadata> nodes = computeService.createNodesInGroup(group,
                                                                                      1,
                                                                                      template);

                NodeMetadata node = nodes.iterator().next();

                // add the details of the started node to maps
                iaasCtxt.addNode(node, domainName);
                replaceIaasContext(iaasCtxt);
                domainToLastlyBuiltIaasContextMap.put(domainName, iaasCtxt);

                if (log.isDebugEnabled()) {
                    log.debug("*************** Node details: \n" + node.toString() +
                              "\n***************\n");
                }

            } catch (RunNodesException e) {
                log.warn("Failed to start an instance in " + iaasCtxt.getName().toString() +
                         "" +
                         ". Hence, will try to start in another IaaS if available.", e);
                continue;
            }

            log.info("Node is successfully starting up in IaaS " + iaasCtxt.getName().toString() +
                     " ...");
            return true;
        }

        return false;

    }

    @Override
    public String startSpiInstance(String domainName, String imageId) {

        IaasContext entry;
        Enum<iaases> iaas;

        // FIXME: Build the Templates, for now we're doing a hack here. I don't know whether
        // there's a proper fix.
        if (imageId.startsWith("nova") && ((entry = findIaasContext(iaases.openstack)) != null)) {
            iaas = iaases.openstack;
            buildLXCTemplates(entry, imageId, true);
        } else if (((entry = findIaasContext(iaases.ec2)) != null)) {
            iaas = iaases.ec2;
            buildEC2Templates(entry, imageId, true);
        } else {
            throw new RuntimeException("Invalid image id!!");
        }

        NodeMetadata node = findIaasContext(iaas).getLastMatchingNode(domainName);

        if (startInstance(domainName) && node != null && node.getPublicAddresses().size() > 0) {
            return node.getPublicAddresses().iterator().next();
        }

        return "";

    }

    @Override
    public boolean terminateInstance(String domainName) {

        log.info("Starting to terminate an instance of domain : " + domainName);

        // sort the IaasContext entities according to scale down order.
        Collections.sort(iaasContextList,
                         IaasContextComparator.ascending(IaasContextComparator.getComparator(IaasContextComparator.SCALE_DOWN_SORT)));

        // traverse in scale down order
        for (IaasContext iaasTemp : iaasContextList) {

            String msg = "Failed to terminate an instance in " + iaasTemp.getName().toString() +
                         "" +
                         ". Hence, will try to terminate an instance in another IaaS if possible.";

            // grab the first node maps with the given domain
            NodeMetadata node = iaasTemp.getFirstMatchingNode(domainName);

            if (node == null) {
                log.warn(msg + " : Reason- No matching instance found for domain '" +
                         domainName +
                         "'.");
                continue;
            }

            // terminate it!
            terminate(iaasTemp, node);

            log.info("A termination request has successfully sent to terminate node : " + node.getId());

            return true;

        }

        return false;

    }

    @Override
    public boolean terminateLastlySpawnedInstance(String domainName) {

        if (domainToLastlyBuiltIaasContextMap.containsKey(domainName)) {
            IaasContext iaasTemp = domainToLastlyBuiltIaasContextMap.get(domainName);

            String msg = "Failed to terminate the lastly spawned instance of '" + domainName +
                         "' service domain.";

            if (iaasTemp == null) {
                log.error(msg + " : Reason- Iaas' data cannot be located!");
                return false;
            }

            // grab the node maps with the given public IP address
            NodeMetadata node = iaasTemp.getLastMatchingNode(domainName);

            if (node == null) {
                log.error(msg + " : Reason- No matching instance found for domain '" +
                          domainName +
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

        // sort the IaasContext entities according to scale down order.
        Collections.sort(iaasContextList,
                         IaasContextComparator.ascending(IaasContextComparator.getComparator(IaasContextComparator.SCALE_DOWN_SORT)));

        // traverse in scale down order
        for (IaasContext iaasTemp : iaasContextList) {

            String msg = "Failed to terminate an instance in " + iaasTemp.getName().toString() +
                         "" +
                         ". Hence, will try to terminate an instance in another IaaS if possible.";

            // grab the node maps with the given public IP address
            NodeMetadata node = iaasTemp.getNodeWithPublicIp(publicIp);

            if (node == null) {
                log.warn(msg + " : Reason- No matching instance found for public ip '" +
                         publicIp +
                         "'.");
                continue;
            }

            // terminate it!
            terminate(iaasTemp, node);

            return true;
        }
        return false;
    }

    private void terminate(IaasContext iaasTemp, NodeMetadata node) {
        // gets the node id
        String nodeId = node.getId();

        // destroy the node
        iaasTemp.getComputeService().destroyNode(nodeId);

        // remove the reference to the node from the data structure.
        iaasTemp.removeNode(node);

        // replace this IaasContext instance, as it reflects the new changes.
        replaceIaasContext(iaasTemp);

        log.info("Node with Id: " + nodeId + " is terminated!");
    }

    @Override
    public int getPendingInstanceCount(String domainName) {

        int pendingInstanceCount = 0;

        for (IaasContext entry : iaasContextList) {

            ComputeService computeService = entry.getComputeService();

            // get list of node Ids which are belong to the requested domain
            List<String> nodeIds = entry.getNodeIds(domainName);

            Set<? extends ComputeMetadata> set = computeService.listNodes();

            Iterator<? extends ComputeMetadata> iterator = set.iterator();

            // traverse through all nodes of this ComputeService object
            while (iterator.hasNext()) {
                org.jclouds.compute.domain.internal.NodeMetadataImpl nodeMetadata = (org.jclouds.compute.domain.internal.NodeMetadataImpl) iterator.next();

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

        log.info("Pending instance count of domain '" + domainName + "' is " + pendingInstanceCount);

        return pendingInstanceCount;

    }

    private IaasContext findIaasContext(Enum<iaases> iaas) {

        for (IaasContext entry : iaasContextList) {
            if (entry.getName().equals(iaas)) {
                return entry;
            }
        }

        return null;
    }

    private IaasContext findIaasContext(String iaasId) {

        for (IaasContext entry : iaasContextList) {
            if (entry.getName().toString().equals(iaasId)) {
                return entry;
            }
        }

        return null;
    }

    private void fillInScaleDownOrder() {

        for (IaasProvider iaas : iaasProviders) {
            if (findIaasContext(iaas.getType()) != null) {
                findIaasContext(iaas.getType()).setScaleDownOrder(iaas.getScaleDownOrder());
            }
        }

    }

    private void fillInScaleUpOrder() {

        for (IaasProvider iaas : iaasProviders) {
            if (findIaasContext(iaas.getType()) != null) {
                findIaasContext(iaas.getType()).setScaleUpOrder(iaas.getScaleUpOrder());
            }
        }

    }

    public byte[] getUserData(String payloadFileName) {

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
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
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

    public enum IaasContextComparator implements Comparator<IaasContext> {
        SCALE_UP_SORT {
            public int compare(IaasContext o1, IaasContext o2) {
                return Integer.valueOf(o1.getScaleUpOrder()).compareTo(o2.getScaleUpOrder());
            }
        },
        SCALE_DOWN_SORT {
            public int compare(IaasContext o1, IaasContext o2) {
                return Integer.valueOf(o1.getScaleDownOrder()).compareTo(o2.getScaleDownOrder());
            }
        };

        public static Comparator<IaasContext> ascending(final Comparator<IaasContext> other) {
            return new Comparator<IaasContext>() {
                public int compare(IaasContext o1, IaasContext o2) {
                    return other.compare(o1, o2);
                }
            };
        }

        public static Comparator<IaasContext>
                getComparator(final IaasContextComparator... multipleOptions) {
            return new Comparator<IaasContext>() {
                public int compare(IaasContext o1, IaasContext o2) {
                    for (IaasContextComparator option : multipleOptions) {
                        int result = option.compare(o1, o2);
                        if (result != 0) {
                            return result;
                        }
                    }
                    return 0;
                }
            };
        }
    }

    /**
     * This will replace an existing entry in iaasEntities list, if there's such.
     * If not this will add the replacement value to the list.
     * 
     * @param replacement
     *            IaasContext entry to be added.
     */
    private void replaceIaasContext(IaasContext replacement) {
        for (IaasContext entry : iaasContextList) {
            if (entry.equals(replacement)) {
                int idx = iaasContextList.indexOf(entry);
                iaasContextList.remove(idx);
                iaasContextList.add(idx, replacement);
                return;
            }
        }
        iaasContextList.add(replacement);
    }

    private void buildLXCTemplates(IaasContext entity, String imageId, boolean blockUntilRunning) {

        if (entity.getComputeService() == null) {
            throw new RuntimeException("Compute service is null for IaaS provider: " + entity.getName());
        }

        TemplateBuilder templateBuilder = entity.getComputeService().templateBuilder();
        templateBuilder.imageId(imageId);

        // to avoid creation of template objects in each and every time, we create all
        // at once!
        for (org.wso2.carbon.autoscaler.service.util.ServiceTemplate temp : serviceTemps) {

            String instanceType;

            // set instance type eg: 1. Openstack expects instance type to be a positive integer
            if (((instanceType = temp.getProperty("instanceType." + iaases.openstack.toString())) != null) && (Pattern.matches("[0-9]+",
                                                                                                                               instanceType))) {

                templateBuilder.hardwareId(instanceType);
            }

            Template template = templateBuilder.build();

            template.getOptions().as(TemplateOptions.class).blockUntilRunning(blockUntilRunning);

            template.getOptions()
                    .as(NovaTemplateOptions.class)
                    .securityGroupNames(temp.getProperty("securityGroups").split(","));

            if (temp.getProperty("payload") != null) {
                template.getOptions()
                        .as(NovaTemplateOptions.class)
                        .userData(getUserData(carbonHome + File.separator +
                                              temp.getProperty("payload")));
            }

            template.getOptions()
                    .as(NovaTemplateOptions.class)
                    .keyPairName(temp.getProperty("keyPair"));

            // add to the data structure
            entity.addToDomainToTemplateMap(temp.getDomainName(), template);

        }

        replaceIaasContext(entity);
    }

    /**
     * Builds EC2 Template object
     * 
     * @param entity
     * @param imageId
     * @param blockUntilRunning
     */
    private void buildEC2Templates(IaasContext entity, String imageId, boolean blockUntilRunning) {

        if (entity.getComputeService() == null) {
            throw new RuntimeException("Compute service is null for IaaS provider: " + entity.getName());
        }

        TemplateBuilder templateBuilder = entity.getComputeService().templateBuilder();

        // set image id specified
        templateBuilder.imageId(imageId);

        // to avoid creation of template objects in each and every time, we create all
        // at once! FIXME we could use caching and lazy loading
        for (org.wso2.carbon.autoscaler.service.util.ServiceTemplate temp : serviceTemps) {

            if (temp.getProperty("instanceType." + iaases.ec2.toString()) != null) {
                // set instance type eg: m1.large
                templateBuilder.hardwareId(temp.getProperty("instanceType." + iaases.ec2.toString()));
            }

            // build the Template
            Template template = templateBuilder.build();

            // make it non blocking
            template.getOptions().as(TemplateOptions.class).blockUntilRunning(blockUntilRunning);

            // set EC2 specific options
            template.getOptions()
                    .as(AWSEC2TemplateOptions.class)
                    .placementGroup(temp.getProperty("availabilityZone"));

            template.getOptions()
                    .as(AWSEC2TemplateOptions.class)
                    .securityGroups(temp.getProperty("securityGroups").split(","));

            if (temp.getProperty("payload") != null) {
                template.getOptions()
                        .as(AWSEC2TemplateOptions.class)
                        .userData(getUserData(carbonHome + File.separator +
                                              temp.getProperty("payload")));
            }

            template.getOptions()
                    .as(AWSEC2TemplateOptions.class)
                    .keyPair(temp.getProperty("keyPair"));

            // add to the data structure
            entity.addToDomainToTemplateMap(temp.getDomainName(), template);

        }

        replaceIaasContext(entity);

    }

}
