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
import org.jclouds.compute.domain.internal.NodeMetadataImpl;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.openstack.nova.v2_0.compute.options.NovaTemplateOptions;
import org.wso2.carbon.autoscaler.service.IAutoscalerService;
import org.wso2.carbon.autoscaler.service.jcloud.ComputeServiceBuilder;
import org.wso2.carbon.autoscaler.service.util.AutoscalerConstant;
import org.wso2.carbon.autoscaler.service.util.IaasContext;
import org.wso2.carbon.autoscaler.service.util.IaasProvider;
import org.wso2.carbon.autoscaler.service.util.ServiceTemplate;
import org.wso2.carbon.autoscaler.service.xml.ElasticScalerConfigFileReader;
import org.wso2.carbon.autoscaler.service.exception.AutoscalerServiceException;
import org.wso2.carbon.autoscaler.service.exception.DeserializationException;
import org.wso2.carbon.autoscaler.service.exception.SerializationException;
import org.wso2.carbon.autoscaler.service.io.Deserializer;
import org.wso2.carbon.autoscaler.service.io.Serializer;
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
    private static final String CARBON_HOME = CarbonUtils.getCarbonHome();

    /**
     * pointer to Carbon Temp directory.
     */
    private static final String CARBON_TEMP = CarbonUtils.getTmpDir();

    /**
     * This directory will be used to store serialized objects.
     */
    private String serializationDir;

    /**
     * List of all <code>IaaSProviders</code> specified in the config file.
     */
    private List<IaasProvider> iaasProviders;

    /**
     * List of all <code>ServiceTemplate</code> objects.
     */
    private List<ServiceTemplate> serviceTemps;

    /**
     * We keep an enum which contains all supported IaaSes.
     */
    public enum Iaases {
        ec2, openstack
    };

    /**
     * List which keeps <code>IaasContext</code> objects.
     */
    private List<IaasContext> iaasContextList = new ArrayList<IaasContext>();

    /**
     * We keep track of the IaaS where the last instance of a domain is spawned.
     * This is required when there are multiple <code>IaasProvider</code>s defined.
     */
    private Map<String, String> domainToLastlyUsedIaasMap = new HashMap<String, String>();

    /**
     * To track whether the {@link #initAutoscaler(boolean)} method has been called.
     */
    boolean isInitialized = false;

    @Override
    public boolean initAutoscaler(boolean isSpi) {

        log.debug("InitAutoscaler has started ...  IsSPI : " + isSpi);

        // load configuration file
        ElasticScalerConfigFileReader configReader = new ElasticScalerConfigFileReader();

        // read serialization directory from config file if specified, else will use the default.
        if ("".equals(serializationDir = configReader.getSerializationDir())) {
            serializationDir = CARBON_TEMP;

            log.debug("Directory to be used to serialize objects: " + serializationDir);
        }

        // let's deserialize and load the serialized objects.
        deserialize();

        // from config file, we grab the details unique to IaaS providers.
        iaasProviders = configReader.getIaasProvidersList();

        // from config file, we grab the details related to each service domain.
        serviceTemps = configReader.getTemplates();

        // we iterate through each IaaSProvider which is loaded from the config file.
        for (IaasProvider iaas : iaasProviders) {

            // build the JClouds specific ComputeService object
            ComputeService computeService = ComputeServiceBuilder.buildComputeService(iaas);
            IaasContext entity;

            // let's see whether there's a serialized entity
            entity = findIaasContext(iaas.getType());

            if (entity != null) {

                log.debug("Serializable object is loaded for IaaS " + iaas.getType());

                // ComputeService isn't serializable, hence we need to set it in the deserialized
                // object.
                entity.setComputeService(computeService);
            }

            // build JClouds Template objects according to different IaaSes
            if (iaas.getType().equalsIgnoreCase(Iaases.ec2.toString())) {

                // initiate the IaasContext object, if it is null.
                entity = (entity == null) ? (entity = new IaasContext(Iaases.ec2, computeService))
                                         : entity;

                // we should build the templates only if this is not SPI stuff
                if (!isSpi) {
                    // Build the Template
                    buildEC2Templates(entity, iaas.getTemplate(), isSpi);

                } else {
                    // add to data structure
                    iaasContextList.add(entity);
                }

            } else if (iaas.getType().equalsIgnoreCase(Iaases.openstack.toString())) {

                // initiate the IaasContext object, if it is null.
                entity = (entity == null) ? (entity = new IaasContext(Iaases.openstack,
                                                                      computeService)) : entity;

                // we should build the templates only if this is not SPI stuff
                if (!isSpi) {
                    // Build the Template
                    buildLXCTemplates(entity, iaas.getTemplate(), isSpi);

                } else {
                    // add to data structure
                    iaasContextList.add(entity);
                }

            } else {
                // unsupported IaaS detected. We only complain, since there could be other IaaSes.
                String msg = "Unsupported IaasProvider is specified in the config file: " + iaas.getType() +
                             ". Supported IaasProviders are " +
                             print(Iaases.values());
                log.warn(msg);
                continue;
            }

            // populate scale up order
            fillInScaleUpOrder();

            // populate scale down order
            fillInScaleDownOrder();

            // serialize the objects
            serialize();
        }

        // we couldn't locate any valid IaaS providers from config file, thus shouldn't proceed.
        if (iaasContextList.size() == 0) {
            String msg = "No valid IaaS provider specified in the config file!";
            log.error(msg);
            throw new AutoscalerServiceException(msg);
        }

        // initialization completed.
        isInitialized = true;

        log.info("Autoscaler service initialized successfully!!");

        return true;
    }

    
    @Override
    public boolean startInstance(String domainName) {

        // initialize the service, if it's not already initialized.
        initialize(false);

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
                iaasCtxt.addNodeIdToDomainMap(node.getId(), domainName);

                // set public Ip, if it's available
                if (node.getPublicAddresses().size() > 0) {
                    String publicIp = node.getPublicAddresses().iterator().next();
                    iaasCtxt.addPublicIpToDomainMap(publicIp, domainName);
                    iaasCtxt.addPublicIpToNodeIdMap(publicIp, node.getId());
                }

                // since we modified the IaasContext instance, let's replace it.
                replaceIaasContext(iaasCtxt);

                // update the domainToLastlyUsedIaasMap
                domainToLastlyUsedIaasMap.put(domainName, iaasCtxt.getName().toString());

                if (log.isDebugEnabled()) {
                    log.debug("Node details: \n" + node.toString() +
                              "\n***************\n");
                }

            } catch (RunNodesException e) {
                log.warn("Failed to start an instance in " + iaasCtxt.getName().toString() +
                         ". Hence, will try to start in another IaaS if available.", e);
                continue;
            }

            log.info("Instance is successfully starting up in IaaS " + iaasCtxt.getName()
                                                                               .toString() + " ...");

            // serialize the objects
            serialize();

            return true;
        }

        log.info("Failed to start instance, in any available IaaS.");

        return false;

    }

    @Override
    public String startSpiInstance(String domainName, String imageId) {

        log.debug("Starting an SPI instance ... | domain: " + domainName + " | ImageId: " + imageId);

        // initialize the service, if it's not already initialized.
        initialize(true);

        IaasContext entry;

        // FIXME: Build the Templates, for now we're doing a hack here. I don't know whether
        // there's a proper fix.
        // handle openstack case
        if (imageId.startsWith("nova") && ((entry = findIaasContext(Iaases.openstack)) != null)) {

            buildLXCTemplates(entry, imageId, true);

        } else if (((entry = findIaasContext(Iaases.ec2)) != null)) {

            buildEC2Templates(entry, imageId, true);

        } else {
            String msg = "Invalid image id: " + imageId;
            log.error(msg);
            throw new AutoscalerServiceException(msg);
        }

        // let's start the instance
        if (startInstance(domainName)) {

            // if it's successful, get the public IP of the started instance.
            // FIXME remove --> String publicIP =
            // findIaasContext(iaas).getLastMatchingPublicIp(domainName);
            String publicIP = entry.getLastMatchingPublicIp(domainName);

            // if public IP is null, return an empty string, else return public IP.
            return (publicIP == null) ? "" : publicIP;

        }

        return "";

    }

    @Override
    public boolean terminateInstance(String domainName) {

        // initialize the service, if it's not already initialized.
        initialize(false);

        log.info("Starting to terminate an instance of domain : " + domainName);

        // sort the IaasContext entities according to scale down order.
        Collections.sort(iaasContextList,
                         IaasContextComparator.ascending(IaasContextComparator.getComparator(IaasContextComparator.SCALE_DOWN_SORT)));

        // traverse in scale down order
        for (IaasContext iaasTemp : iaasContextList) {

            String msg = "Failed to terminate an instance in " + iaasTemp.getName().toString() +
                         ". Hence, will try to terminate an instance in another IaaS if possible.";

            String nodeId = null;

            // grab the node ids related to the given domain and traverse
            for (String id : iaasTemp.getNodeIds(domainName)) {
                if (id != null) {
                    nodeId = id;
                    break;
                }
            }

            // if no matching node id can be found.
            if (nodeId == null) {

                log.warn(msg + " : Reason- No matching instance found for domain '" +
                         domainName +
                         "'.");
                continue;
            }

            // terminate it!
            terminate(iaasTemp, nodeId);

            return true;

        }

        log.info("Termination of an instance which is belong to domain '" + domainName +
                 "', failed!");

        return false;

    }

    @Override
    public boolean terminateLastlySpawnedInstance(String domainName) {

        // initialize the service, if it's not already initialized.
        initialize(false);

        // see whether there is a matching IaaS, where we spawn an instance belongs to given domain.
        if (domainToLastlyUsedIaasMap.containsKey(domainName)) {

            // grab the name of the IaaS
            String iaas = domainToLastlyUsedIaasMap.get(domainName);

            // find the corresponding IaasContext
            IaasContext iaasTemp = findIaasContext(iaas);

            String msg = "Failed to terminate the lastly spawned instance of '" + domainName +
                         "' service domain.";

            if (iaasTemp == null) {
                log.error(msg + " : Reason- Iaas' data cannot be located!");
                return false;
            }

            // find the instance spawned at last of this IaasContext
            String nodeId = iaasTemp.getLastMatchingNode(domainName);

            if (nodeId == null) {
                log.error(msg + " : Reason- No matching instance found for domain '" +
                          domainName +
                          "'.");
                return false;
            }

            // terminate it!
            terminate(iaasTemp, nodeId);

            return true;

        }

        log.info("Termination of an instance which is belong to domain '" + domainName +
                 "', failed!");

        return false;
    }

    @Override
    public boolean terminateSpiInstance(String publicIp) {

        // initialize the service, if it's not already initialized.
        initialize(true);

        // sort the IaasContext entities according to scale down order.
        Collections.sort(iaasContextList,
                         IaasContextComparator.ascending(IaasContextComparator.getComparator(IaasContextComparator.SCALE_DOWN_SORT)));

        // traverse in scale down order
        for (IaasContext iaasTemp : iaasContextList) {

            String msg = "Failed to terminate an instance in " + iaasTemp.getName().toString() +
                         "" +
                         ". Hence, will try to terminate an instance in another IaaS if possible.";

            // grab the node maps with the given public IP address
            String nodeId = iaasTemp.getNodeWithPublicIp(publicIp);

            if (nodeId == null) {
                log.warn(msg + " : Reason- No matching instance found for public ip '" +
                         publicIp +
                         "'.");
                continue;
            }

            // terminate it!
            terminate(iaasTemp, nodeId);

            return true;
        }

        log.info("Termination of an instance which has the public IP '" + publicIp + "', failed!");

        return false;
    }

    @Override
    public int getPendingInstanceCount(String domainName) {

        // initialize the service, if it's not already initialized.
        initialize(false);

        int pendingInstanceCount = 0;

        // traverse through IaasContexts
        for (IaasContext entry : iaasContextList) {

            ComputeService computeService = entry.getComputeService();

            // get list of node Ids which are belong to the requested domain
            List<String> nodeIds = entry.getNodeIds(domainName);

            // get all the nodes spawned by this IaasContext
            Set<? extends ComputeMetadata> set = computeService.listNodes();

            Iterator<? extends ComputeMetadata> iterator = set.iterator();

            // traverse through all nodes of this ComputeService object
            while (iterator.hasNext()) {
                NodeMetadataImpl nodeMetadata = (NodeMetadataImpl) iterator.next();

                // if this node belongs to the requested domain
                if (nodeIds.contains(nodeMetadata.getId())) {

                    // get the status of the node
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

    /**
     * Returns matching IaasContext for the given {@link Iaases} entry.
     */
    private IaasContext findIaasContext(Enum<Iaases> iaas) {

        for (IaasContext entry : iaasContextList) {
            if (entry.getName().equals(iaas)) {
                return entry;
            }
        }

        return null;
    }

    /**
     * Returns matching IaasContext for the given iaas type.
     */
    private IaasContext findIaasContext(String iaasType) {

        for (IaasContext entry : iaasContextList) {
            if (entry.getName().toString().equals(iaasType)) {
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

    private byte[] getUserData(String payloadFileName) {

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
    private byte[] getBytesFromFile(File file) throws IOException {
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

    /**
     * Comparator to compare IaasContexts on different attributes.
     */
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

    /**
     * Builds the LXC Template object.
     */
    private void buildLXCTemplates(IaasContext entity, String imageId, boolean blockUntilRunning) {

        if (entity.getComputeService() == null) {
            throw new AutoscalerServiceException("Compute service is null for IaaS provider: " + entity.getName());
        }

        // if domain to template map is null
        if (entity.getDomainToTemplateMap() == null) {
            // we initialize it
            entity.setDomainToTemplateMap(new HashMap<String, Template>());
        }

        TemplateBuilder templateBuilder = entity.getComputeService().templateBuilder();
        templateBuilder.imageId(imageId);

        // to avoid creation of template objects in each and every time, we create all
        // at once!
        for (org.wso2.carbon.autoscaler.service.util.ServiceTemplate temp : serviceTemps) {

            String instanceType;

            // set instance type 
            if (((instanceType = temp.getProperty("instanceType." + Iaases.openstack.toString())) != null) ) {

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
                        .userData(getUserData(CARBON_HOME + File.separator +
                                              temp.getProperty("payload")));
            }

            template.getOptions()
                    .as(NovaTemplateOptions.class)
                    .keyPairName(temp.getProperty("keyPair"));

            // add to the data structure
            entity.addToDomainToTemplateMap(temp.getDomainName(), template);

        }

        // since we modified the Context, we need to replace
        replaceIaasContext(entity);
    }

    /**
     * Builds EC2 Template object
     * 
     */
    private void buildEC2Templates(IaasContext entity, String imageId, boolean blockUntilRunning) {

        if (entity.getComputeService() == null) {
            throw new AutoscalerServiceException("Compute service is null for IaaS provider: " + entity.getName());
        }

        // if domain to template map is null
        if (entity.getDomainToTemplateMap() == null) {
            // we initialize it
            entity.setDomainToTemplateMap(new HashMap<String, Template>());
        }

        TemplateBuilder templateBuilder = entity.getComputeService().templateBuilder();

        // set image id specified
        templateBuilder.imageId(imageId);

        // to avoid creation of template objects in each and every time, we create all
        // at once! FIXME we could use caching and lazy loading
        for (org.wso2.carbon.autoscaler.service.util.ServiceTemplate temp : serviceTemps) {

            if (temp.getProperty("instanceType." + Iaases.ec2.toString()) != null) {
                // set instance type eg: m1.large
                templateBuilder.hardwareId(temp.getProperty("instanceType." + Iaases.ec2.toString()));
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
                        .userData(getUserData(CARBON_HOME + File.separator +
                                              temp.getProperty("payload")));
            }

            template.getOptions()
                    .as(AWSEC2TemplateOptions.class)
                    .keyPair(temp.getProperty("keyPair"));

            // add to the data structure
            entity.addToDomainToTemplateMap(temp.getDomainName(), template);

        }

        // since we modified the Context, we need to replace
        replaceIaasContext(entity);

    }

    /**
     * Call {@link #initAutoscaler(boolean)} method, in case autoscaler service is not initialized.
     * 
     * @param isSpi
     *            whether this is a SPI call
     */
    private void initialize(boolean isSpi) {
        if (!isInitialized) {
            initAutoscaler(isSpi);
        }
    }

    private String print(Iaases[] values) {
        String str = "";
        for (Iaases iaases : values) {
            str = iaases.name() + ", ";
        }
        str = str.trim();
        return str.endsWith(",") ? str.substring(0, str.length() - 1) : str;
    }

    @SuppressWarnings("unchecked")
    private void deserialize() {

        String path;

        try {
            path = serializationDir + File.separator +
                   AutoscalerConstant.IAAS_CONTEXT_LIST_SERIALIZING_FILE;

            Object obj = Deserializer.deserialize(path);
            if (obj != null) {
                iaasContextList = (List<IaasContext>) obj;
                log.debug("Deserialization was successful from file: " + path);
            }

            path = serializationDir + File.separator +
                   AutoscalerConstant.DOMAIN_TO_LASTLY_USED_IAAS_MAP_SERIALIZING_FILE;

            obj = Deserializer.deserialize(path);

            if (obj != null) {
                domainToLastlyUsedIaasMap = (Map<String, String>) obj;
                log.debug("Deserialization was successful from file: " + path);
            }

        } catch (Exception e) {
            String msg = "Deserialization of objects failed!";
            log.fatal(msg, e);
            throw new DeserializationException(msg, e);
        }

    }

    /**
     * Does all the serialization stuff!
     */
    private void serialize() {

        try {
            Serializer.serialize(iaasContextList,
                                 serializationDir + File.separator +
                                         AutoscalerConstant.IAAS_CONTEXT_LIST_SERIALIZING_FILE);

            Serializer.serialize(domainToLastlyUsedIaasMap,
                                 serializationDir + File.separator +
                                         AutoscalerConstant.DOMAIN_TO_LASTLY_USED_IAAS_MAP_SERIALIZING_FILE);

        } catch (IOException e) {
            String msg = "Serialization of objects failed!";
            log.fatal(msg, e);
            throw new SerializationException(msg, e);
        }
    }

    /**
     * A helper method to terminate an instance.
     */
    private void terminate(IaasContext iaasTemp, String nodeId) {

        // this is just to be safe
        if (iaasTemp.getComputeService() == null) {
            String msg = "Unexpeced error occured! IaasContext's ComputeService is null!";
            log.error(msg);
            throw new AutoscalerServiceException(msg);
        }

        // destroy the node
        iaasTemp.getComputeService().destroyNode(nodeId);

        // remove the node id from the Context
        iaasTemp.removeNodeId(nodeId);

        // replace this IaasContext instance, as it reflects the new changes.
        replaceIaasContext(iaasTemp);

        // serialize the objects
        serialize();

        log.info("Node with Id: '" + nodeId + "' is terminated!");
    }

}
