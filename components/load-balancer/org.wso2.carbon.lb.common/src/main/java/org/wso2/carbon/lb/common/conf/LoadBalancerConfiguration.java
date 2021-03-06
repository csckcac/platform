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
package org.wso2.carbon.lb.common.conf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.util.PropertyHelper;
import org.wso2.carbon.lb.common.conf.structure.Node;
import org.wso2.carbon.lb.common.conf.structure.NodeBuilder;
import org.wso2.carbon.lb.common.conf.util.Constants;
import org.wso2.carbon.lb.common.conf.util.HostContext;
import org.wso2.carbon.lb.common.conf.util.LoadBalancerConfigUtil;
import org.wso2.carbon.lb.common.conf.util.TenantDomainContext;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Data object which hold configuration data of the load analyzer task
 */
@SuppressWarnings("unused")
public class LoadBalancerConfiguration implements Serializable {

    protected static final long serialVersionUID = -5553545217542808233L;

    private static final Log log = LogFactory.getLog(LoadBalancerConfiguration.class);

    /**
     * This map is there to speed up the lookup time.
     * Key: domain
     * <p/>
     * Value: A map whose key is sub domain and value is ServiceConfiguration
     */
    private Map<String, Map<String, ServiceConfiguration>> serviceConfigurations =
            new HashMap<String, Map<String, ServiceConfiguration>>();

    /**
     * This list will be used to identify host name duplications among different services.
     * Within a service there can be duplications, but among different services you can't have duplications.
     */
    private Map<String, Set<String>> hostNamesTracker = new HashMap<String, Set<String>>();

    protected ServiceConfiguration defaultServiceConfig;
    protected LBConfiguration lbConfig;

    /**
     * LBConfig file as a String
     */
    protected String lbConfigString;

    /**
     * Root node object for loadbalancer.conf
     */
    protected Node rootNode;


    /**
     * Sample loadbalancer.conf:
     * <p/>
     * loadbalancer {
     * # minimum number of load balancer instances
     * instances           1;
     * # whether autoscaling enable or not
     * enable_autoscaler   true;
     * # End point reference of the Autoscaler Service
     * autoscaler_service_epr  https://10.100.3.81:9443/services/AutoscalerService/;
     * # interval between two task executions in milliseconds
     * autoscaler_task_interval 1000;
     * # after an instance booted up, task will wait till this much of time and let the server started up
     * server_startup_delay 15000;
     * }
     * <p/>
     * services {
     * defaults {
     * min_app_instances       1;
     * max_app_instances       5;
     * queue_length_per_node   400;
     * rounds_to_average       10;
     * instances_per_scale_up  1;
     * message_expiry_time     60000;
     * }
     * <p/>
     * appserver {
     * hosts                   appserver.cloud-test.wso2.com,as.cloud-test.wso2.com;
     * domains   {
     * wso2.as1.domain {
     * tenant_range    1-100;
     * }
     * wso2.as2.domain {
     * tenant_range    101-200;
     * }
     * wso2.as3.domain {
     * tenant_range    *;
     * }
     * }
     * }
     * }
     *
     * @param configURL URL of the load balancer config
     */
    public void init(String configURL) {

        if (configURL.startsWith("$system:")) {
            configURL = System.getProperty(configURL.substring("$system:".length()));
        }

        try {

            // get loadbalancer.conf file as a String
            if (configURL.startsWith(File.separator)) {
                lbConfigString = createLBConfigString(configURL);
            } else {
                lbConfigString = createLBConfigString(new URL(configURL).openStream());
            }

        } catch (Exception e) {
            String msg = "Cannot read configuration file from " + configURL;
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        // build a Node object for whole loadbalancer.conf
        rootNode = new Node();
        rootNode.setName("root");
        rootNode = NodeBuilder.buildNode(rootNode, lbConfigString);

        // load 'loadbalancer' node
        Node lbConfigNode = rootNode.findChildNodeByName(Constants.LOAD_BALANCER_ELEMENT);

        if (lbConfigNode == null) {
            String msg = "Mandatory " + Constants.LOAD_BALANCER_ELEMENT +
                    " element can not be" + " found in the configuration file.";
            log.error(msg);
            throw new RuntimeException(msg);
        }

        // Set load balancer configuration
        createConfiguration(lbConfig = new LBConfiguration(), lbConfigNode);

        // load services node
        Node servicesConfigNode = rootNode.findChildNodeByName(Constants.SERVICES_ELEMENT);

        if (servicesConfigNode == null) {
            String msg = "Mandatory " + Constants.SERVICES_ELEMENT +
                    " element can not be found in the configuration file.";
            log.error(msg);
            throw new RuntimeException(msg);
        }

        // Set services configuration
        createServicesConfig(servicesConfigNode);

    }


    /**
     * Given a host name and a tenant ID this will find the corresponding domain
     * @param host host name 
     * @param tenantId tenant ID
     * @return domain if exists, else empty string
     */
//    public String getDomain(String host, int tenantId) {
//        
//        if (hostDomainNodeMap.containsKey(host)) {
//            for (Node aNode : hostDomainNodeMap.get(host).getChildNodes()) {
//
//                String tenantRange = aNode.getProperty(Constants.TENANT_RANGE_ELEMENT);
//                
//                if(tenantRange== null || "".equals(tenantRange)){
//                    throw new RuntimeException("Mandatory element "+Constants.TENANT_RANGE_ELEMENT+" which " +
//                            "is a child element of "+aNode.getName()+" cannot be found.");
//                }
//                
//                //we should reach unlimited range after visiting all other ranges
//                if(tenantRange.equals(Constants.UNLIMITED_TENANT_RANGE)){
//                    return aNode.getName();
//                }
//                
//                String[] limits = tenantRange.split(Constants.TENANT_RANGE_DELIMITER);
//                
//                if(limits.length != 2 ){
//                    throw new RuntimeException("Malformed element "+Constants.TENANT_RANGE_ELEMENT+" which " +
//                    		"is a child element of "+aNode.getName());
//                }
//                
//                int lowerLimit = Integer.parseInt(limits[0]);
//                int upperLimit = Integer.parseInt(limits[1]);
//                
//                if(tenantId >=lowerLimit && tenantId <= upperLimit){
//                    return aNode.getName();
//                }
//            }
//        }
//        
//        return "";
//        
//    }

    /**
     * Given a domains Node this will return its domain entries and corresponding
     * tenant ranges Map.
     * @param domains Node
     * @return domainToTenantRangeMap
     */
//    public Map<String, String> getdomainToTenantRangeMap(Node domains) {
//        Map<String, String> domainToTenantRangeMap = new HashMap<String, String>();
//        
//        for(Node domain : domains.getChildNodes()){
//            
//            String domainName = domain.getName();
//            String tenantRange = domain.getProperty(Constants.TENANT_RANGE_ELEMENT);
//            
//            if(tenantRange== null || "".equals(tenantRange)){
//                throw new RuntimeException("Mandatory element "+Constants.TENANT_RANGE_ELEMENT+" which " +
//                        "is a child element of "+domain.getName()+" cannot be found.");
//            }
//            
//            if(tenantRange!= null && !"".equals(tenantRange) && 
//                    domainName != null && !"".equals(domainName)){
//                
//                domainToTenantRangeMap.put(domainName, tenantRange);
//            }
//        }
//        
//        return domainToTenantRangeMap;
//    }


    /**
     * Process the content of the following 'services' element
     * <p/>
     * services {
     * defaults {
     * min_app_instances       1;
     * max_app_instances       5;
     * queue_length_per_node   400;
     * rounds_to_average       10;
     * instances_per_scale_up  1;
     * message_expiry_time     60000;
     * }
     * <p/>
     * appserver {
     * hosts                   appserver.cloud-test.wso2.com,as.cloud-test.wso2.com;
     * domains   {
     * wso2.as1.domain {
     * tenant_range    1-100;
     * }
     * wso2.as2.domain {
     * tenant_range    101-200;
     * }
     * wso2.as3.domain {
     * tenant_range    *;
     * }
     * }
     * }
     * }
     *
     * @param servicesConfigNode services element's Node
     */
    public void createServicesConfig(Node servicesConfigNode) {

        // Building default configuration
        Node defaultNode = servicesConfigNode.findChildNodeByName(Constants.DEFAULTS_ELEMENT);

        if (defaultNode != null) {

            createConfiguration(defaultServiceConfig = new ServiceConfiguration(), defaultNode);
        }

        // Building custom services configuration
        for (Node serviceNode : servicesConfigNode.getChildNodes()) {
            //skip default node
            if (serviceNode != defaultNode) {

                String serviceName = serviceNode.getName();

                // reading domains

                Node domainsNode;

                if (serviceNode.getChildNodes().isEmpty() ||
                        !(domainsNode = serviceNode.getChildNodes().get(0)).getName().equals(
                                Constants.DOMAIN_ELEMENT)) {

                    String msg = "The mandatory domains element, child of the " + serviceName +
                            " element is not specified in the configuration file. \n"+
                            serviceNode.toString();
                    log.error(msg);
                    throw new RuntimeException(msg);
                }

                if (domainsNode.getChildNodes().isEmpty()) {
                    // this is probably a mistake, so we don't proceed
                    String msg = "No domain is specified under " + Constants.DOMAIN_ELEMENT +
                            " of " + serviceName + " element.";
                    log.error(msg);
                    throw new RuntimeException(msg);
                }

//                //reading hosts
//                String hosts =serviceNode.getProperty(Constants.HOSTS_ELEMENT);
//                
//                if (hosts == null) {
//                    throw new RuntimeException("The mandatory hosts element, " +
//                    		"which is a child of "+serviceName+" element is not specified");
//                }
//                
//                String[] host = hosts.split(Constants.HOSTS_DELIMITER);
//                
//                for (String aHost : host) {
//                    
//                    if (aHost.isEmpty()) {
//                        throw new RuntimeException("host cannot be empty");
//                    }
//                    if (hostDomainNodeMap.containsKey(aHost)) {
//                        throw new RuntimeException("host " + aHost + " has been duplicated in the configuration");
//                    }
//
//                    // adds the domains node to map
//                    hostDomainNodeMap.put(aHost.trim(), domainsNode);
//                    
//                }

//                serviceToDomainsMap.put(serviceName, domainsNode);

                ServiceConfiguration serviceConfig;

                // iterates through all the service domain specified in this service element. 
                for (Node domain : domainsNode.getChildNodes()) {

                    // create a new service configuration
                    serviceConfig = new ServiceConfiguration();

                    // set domain name
                    serviceConfig.setDomain(domain.getName());

                    // let's set properties common to all domains specified in this service element.
                    createConfiguration(serviceConfig, serviceNode);

                    // load properties specified under this service domain element.
                    createConfiguration(serviceConfig, domain);

                    // check host name duplication 
                    if(isDuplicatedHost(serviceNode.getName(), serviceConfig)){
                        // this is probably a mistake, so we don't proceed
                        String msg = "Duplicated host names detected for different service domains.\n" +
                                "Element: \n"+serviceNode.toString();
                        log.error(msg);
                        throw new RuntimeException(msg);
                    }

                    // compose an unique identifier for this service domain
//                    String key = serviceConfig.getSubDomain()+ Constants.SUB_DOMAIN_DELIMITER + 
//                                                                    serviceConfig.getDomain();

                    // add the built ServiceConfiguration, to the map
                    addServiceConfiguration(serviceConfig);

//                    serviceConfigMap.put(domain.getName(), serviceConfig);
                }
            }
        }

    }


    private void addServiceConfiguration(ServiceConfiguration serviceConfig) {

        Map<String, ServiceConfiguration> map;
        String domain = serviceConfig.getDomain();
        String subDomain = serviceConfig.getSubDomain();

        if (serviceConfigurations.containsKey(domain)) {
            map = serviceConfigurations.get(domain);
        } else {
            map = new HashMap<String, ServiceConfiguration>();
        }
        // put this serviceConfig
        map.put(subDomain, serviceConfig);

        // update the parent map
        serviceConfigurations.put(domain, map);

    }


    private boolean isDuplicatedHost(String name, ServiceConfiguration serviceConfig) {

        /**
         * This will be populated with host names of all other services other than the
         * service subjected to the test.
         */
        List<String> hostsOtherThanMine = new ArrayList<String>(hostNamesTracker.values().size());

        for (Map.Entry<String, Set<String>> entry : hostNamesTracker.entrySet()) {
            if (!entry.getKey().equals(name)) {
                hostsOtherThanMine.addAll(entry.getValue());
            }
        }

        for (String host : serviceConfig.getHosts()) {
            if (hostsOtherThanMine.contains(host)) {
                return true;
            }
        }

        addToHostNameTrackerMap(name, serviceConfig.getHosts());

        return false;
    }


    private void addToHostNameTrackerMap(String name, List<String> hosts) {

        Set<String> allHosts;

        if (hostNamesTracker.containsKey(name)) {
            allHosts = hostNamesTracker.get(name);
            allHosts.addAll(hosts);
        } else {
            allHosts = new HashSet<String>(hosts);
        }
        hostNamesTracker.put(name, allHosts);
    }


    public Map<String, HostContext> getHostContextMap() {

        Map<String, HostContext> hostCtxt = new HashMap<String, HostContext>();
        List<Integer> tenantIds;
        Map<String, String> URLSuffixes;

        // iterate through each service 
        for (Iterator<Set<String>> it = hostNamesTracker.values().iterator(); it.hasNext();) {

            // iterate through host names of this service
            for (String hostName : ((Set<String>) it.next())) {
                                                                  
                // building HostContext
                HostContext ctxt = new HostContext(hostName);

                // iterate through domains of this host
                for (Map.Entry<String, Map<String, ServiceConfiguration>> parentMap : serviceConfigurations.entrySet()) {

                    // iterate through sub domain of this domain
                    for (Map.Entry<String, ServiceConfiguration> childMap : parentMap.getValue()
                            .entrySet()) {
                        // iterate through hosts of this
                        for (String host : childMap.getValue().getHosts()) {
                            // if a matching Service configuration is found.
                            if (host.equals(hostName)) {
                                // get tenant ids for this range
                                tenantIds =
                                        LoadBalancerConfigUtil.getTenantIds(childMap.getValue()
                                                .getTenantRange());

                                // iterate through all tenant ids under this host
                                for (Integer tId : tenantIds) {

                                    // create a new TenantDomainContext
                                    TenantDomainContext tenantCtxt =
                                            new TenantDomainContext(
                                                    tId,
                                                    parentMap.getKey(),
                                                    childMap.getKey());
                                    // add it to this HostContext
                                    ctxt.addTenantDomainContext(tenantCtxt);
                                }

                                break;
                            }
                        }
                        //iterate through URL suffixes
                        for(Map.Entry<String, String> entry : childMap.getValue().getUrl_suffix().entrySet()) {
                            if(entry.getKey().equals(hostName)) {
                                ctxt.setUrlSuffix(entry.getValue());
                            }

                        }
                    }
                }

                // add this hostCtxt
                hostCtxt.put(hostName, ctxt);
            }

        }

//        for (Map.Entry<String, Map<String, ServiceConfiguration>> parentMap : serviceConfigurations.entrySet()) {
//            
//            for (Map.Entry<String, ServiceConfiguration> childMap : parentMap.getValue().entrySet()) {
//                
//                for (String host : childMap.getValue().getHosts()) {
//                    
//                    host = host.trim();
//                    
//                    // building HostContext
//                    HostContext ctxt = new HostContext(host);
//                    
//                    // get tenant ids for this range
//                    tenantIds = LoadBalancerConfigUtil.getTenantIds(childMap.getValue().getTenantRange());
//                    
//                    // iterate through all tenant ids under this host
//                    for (Integer tId : tenantIds) {
//                        
//                        // create a new TenantDomainContext
//                        TenantDomainContext tenantCtxt = new TenantDomainContext(tId, parentMap.getKey(), childMap.getKey());
//                        // add it to this HostContext
//                        ctxt.addTenantDomainContext(tenantCtxt);
//                    }
//                    
//                    // add this hostCtxt 
//                    hostCtxt.add(ctxt);
//                    
//                }
//                
//            }
//            
//        }
        return hostCtxt;

    }


//    public Map<String, List<TenantDomainRangeContext>> getHostDomainMap() {
//
//        /*
//        We could have same host used in multiple domains
//         */
//        Map<String, List<TenantDomainRangeContext>> map = new HashMap<String, List<TenantDomainRangeContext>>();
//
//        for (Map.Entry<String, Map<String, ServiceConfiguration>> parentMap : serviceConfigurations.entrySet()) {
//
//            TenantDomainRangeContext domainRangeContext = new TenantDomainRangeContext();
//
//            for (Map.Entry<String, ServiceConfiguration> childMap : parentMap.getValue().entrySet()) {
//
//                domainRangeContext.addTenantDomain(parentMap.getKey(), childMap.getKey(), childMap.getValue().getTenantRange());
//
//                for (String host : childMap.getValue().getHosts()) {
//                    host = host.trim();
//                    
//                    List<TenantDomainRangeContext> list;
//                    
//                    if(map.get(host) == null){
//                        list = new ArrayList<TenantDomainRangeContext>();
//                    }
//                    else{
//                        list = map.get(host);
//                    }
//                    
//                    if (!list.contains(domainRangeContext)) {
//                        list.add(domainRangeContext);
//                    }
//                    
//                    map.put(host, list);
//                }
//
//            }
//
//
//        }


//        //TODO remove! get domains elements for each service
//        for (Map.Entry<String, Node> entry : this.getServiceToDomainsMap().entrySet()) {
//            //String serviceName = entry.getKey();
//            Node domains = entry.getValue();
//            TenantDomainRangeContext domainRangeContext = new TenantDomainRangeContext();
//
//            // get domain to tenant range map for each domains element and iterate over it
//            for (Map.Entry<String, String> entry2 : this.getdomainToTenantRangeMap(domains).entrySet()) {
//
//                String domainName = entry2.getKey();
//                String tenantRange = entry2.getValue();
//                domainRangeContext.addTenantDomain(domainName, tenantRange);
//            }
//
//            // get host to domains node map and iterate over it
//            for (Map.Entry<String, Node> entry3 : this.getHostDomainNodeMap().entrySet()) {
//                String host = entry3.getKey();
//                Node domainsNode = entry3.getValue();
//
//                if (domainsNode.equals(domains)) {
//                    map.put(host, domainRangeContext);
//                }
//            }
//
//        }

//        return map;
//    }


//    /**
//     * This method will read the tenant range string and return a list of tenant ids
//     * which is derived from tenant range string.
//     *
//     * @param tenantRange
//     * @return list of tenant ids.
//     */
//    private List<Integer> getTenantIds(String tenantRange) {
//
//        List<Integer> tenantIds = new ArrayList<Integer>();
//
//        String[] parsedLine = tenantRange.trim().split("-");
//
//        if (parsedLine[0].equalsIgnoreCase("*")) {
//            tenantIds.add(0);
//        } else if (parsedLine.length == 1) {
//            try {
//                int tenantId = Integer.parseInt(tenantRange);
//                tenantIds.add(tenantId);
//
//            } catch (NumberFormatException e) {
//                String msg = "Invalid tenant range is specified " + tenantRange;
//                log.error(msg, e);
//                throw new RuntimeException(msg, e);
//            }
//        } else if (parsedLine.length == 2) {
//            try {
//
//                int startIndex = Integer.parseInt(parsedLine[0]);
//                int endIndex = Integer.parseInt(parsedLine[1]);
//
//                for (int tenantId = startIndex; tenantId < endIndex; tenantId++) {
//
//                    tenantIds.add(tenantId);
//                }
//
//            } catch (NumberFormatException e) {
//                String msg = "Invalid tenant range is specified for domain " + tenantRange;
//                log.error(msg, e);
//                throw new RuntimeException(msg, e);
//            }
//
//        } else {
//            String msg = "Invalid tenant range is specified for domain " + tenantRange;
//            log.error(msg);
//            throw new RuntimeException(msg);
//        }
//
//        return tenantIds;
//    }

    protected void createConfiguration(Configuration config, Node node) {

        if (node == null) {
            String msg = "The configuration element for " +
                    config.getClass().getName() + " is null.";
            throw new RuntimeException(msg);
        }

        try {
            // load properties
            for (Map.Entry<String, String> entry : node.getProperties().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                PropertyHelper.setInstanceProperty(key, value, config);
            }

        } catch (Exception e) {
            String msg = "Error setting values to " + config.getClass().getName();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public LBConfiguration getLoadBalancerConfig() {
        return lbConfig;
    }

    public String[] getServiceDomains() {

        Object[] objs = serviceConfigurations.keySet().toArray();

        return Arrays.copyOf(objs, objs.length, String[].class);

    }

    public String[] getServiceSubDomains(String domain) {

        if (serviceConfigurations.get(domain) != null) {
            Object[] objs = serviceConfigurations.get(domain).keySet().toArray();
            return Arrays.copyOf(objs, objs.length, String[].class);
        }

        return new String[0];
    }

    public ServiceConfiguration getServiceConfig(String domain, String subDomain) {
        if (serviceConfigurations.get(domain) != null) {
            return serviceConfigurations.get(domain).get(subDomain);
        }
        return null;
    }

    /**
     * Convert given configuration file to a single String
     *
     * @param configFileName - file name to convert
     * @return String with complete lb configuration
     * @throws FileNotFoundException
     */
    public String createLBConfigString(String configFileName) throws FileNotFoundException {
        StringBuilder lbConfigString = new StringBuilder("");

        File configFile = new File(configFileName);
        Scanner scanner;

        scanner = new Scanner(configFile);

        while (scanner.hasNextLine()) {
            lbConfigString.append(scanner.nextLine().trim() + "\n");
        }

        return lbConfigString.toString().trim();
    }

    public String createLBConfigString(InputStream configFileName) throws IOException {

        // read the stream with BufferedReader
        BufferedReader br = new BufferedReader(new InputStreamReader(configFileName));

        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line.trim() + "\n");
        }

        return sb.toString().trim();
    }

    public abstract class Configuration implements Serializable {

        private static final long serialVersionUID = -5433889427746551250L;
        protected String imageId = System.getenv("ami_id");
        protected String payload;
        protected boolean payloadSet;

        protected String availability_zone = "us-east-1c";
        protected boolean availabilityZoneSet;

        protected String[] security_groups = new String[]{"default"};
        protected boolean securityGroupsSet;

        protected String instance_type = "m1.large";
        protected boolean instanceTypeSet;

        protected String additional_info;

        public String getImageId() {
            return imageId;
        }

        public String getAdditionalInfo() {
            return additional_info;
        }

        public String getAvailability_zone() {
            if (this instanceof LBConfiguration) {
                return availability_zone;
            }
            if (availabilityZoneSet) {
                return availability_zone;
            } else if (defaultServiceConfig != null && defaultServiceConfig.availabilityZoneSet) {
                return defaultServiceConfig.availability_zone;
            }
            return availability_zone;
        }

        public String[] getSecurityGroups() {
            if (this instanceof LBConfiguration) {
                return security_groups;
            }
            if (securityGroupsSet) {
                return security_groups;
            } else if (defaultServiceConfig != null && defaultServiceConfig.securityGroupsSet) {
                return defaultServiceConfig.security_groups;
            }
            return security_groups;
        }

        public String getInstanceType() {
            if (this instanceof LBConfiguration) {
                return instance_type;
            }
            if (instanceTypeSet) {
                return instance_type;
            } else if (defaultServiceConfig != null && defaultServiceConfig.instanceTypeSet) {
                return defaultServiceConfig.instance_type;
            }
            return instance_type;
        }


        public String getUserData() {
            if (payload == null) {
                payload = LoadBalancerConfigUtil.getUserData("resources/cluster_node.zip");
            }
            if (this instanceof LBConfiguration) {
                return payload;
            }
            if (payloadSet) {
                return payload;
            } else if (defaultServiceConfig != null && defaultServiceConfig.payloadSet) {
                return defaultServiceConfig.payload;
            }
            return payload;
        }

        public void setPayload(String payload) {
            this.payload = LoadBalancerConfigUtil.getUserData(LoadBalancerConfigUtil.replaceVariables(payload));
            this.payloadSet = true;
        }

        public void setAvailability_zone(String availabilityZone) {
            this.availability_zone = LoadBalancerConfigUtil.replaceVariables(availabilityZone);
            this.availabilityZoneSet = true;
        }

        public void setSecurity_groups(String securityGroups) {
            this.security_groups = LoadBalancerConfigUtil.replaceVariables(securityGroups).split(",");
            this.securityGroupsSet = true;
        }

        public void setInstance_type(String instanceType) {
            this.instance_type = LoadBalancerConfigUtil.replaceVariables(instanceType);
            this.instanceTypeSet = true;
        }

    }

    public class LBConfiguration extends Configuration implements Serializable {

        private static final long serialVersionUID = 1357143883932816418L;
        private String elasticIP;//= LoadBalancerConfigUtil.replaceVariables("${ELASTIC_IP}");
        private int instances = 1;
        private boolean isAutoscaleEnabled;
        private int autoscalerTaskInterval = 5000;
        private String autoscalerServiceEpr;
        private int serverStartupDelay = 60000;
        private int sizeOfCache = 0 ;

        public String getElasticIP() {
            return elasticIP;
        }

        public int getInstances() {
            return instances;
        }

        public boolean isAutoscaleEnabled() {
            return isAutoscaleEnabled;
        }

        public String getAutoscalerServiceEpr() {
            return autoscalerServiceEpr;
        }

        public int getAutoscalerTaskInterval() {
            return autoscalerTaskInterval;
        }

        public int getServerStartupDelay() {
            return serverStartupDelay;
        }

        public void setElasticIP(String elasticIP) {
            this.elasticIP = LoadBalancerConfigUtil.replaceVariables(elasticIP);
        }

        public void setInstances(int instances) {
            this.instances = instances;
        }

        public void setEnable_autoscaler(String isEnabled) {
            this.isAutoscaleEnabled = Boolean.parseBoolean(isEnabled);
        }

        public void setAutoscaler_service_epr(String epr) {
            this.autoscalerServiceEpr = epr;
        }

        public void setAutoscaler_task_interval(String interval) {
            this.autoscalerTaskInterval = Integer.parseInt(interval);
        }

        public void setServer_startup_delay(String delay) {
            this.serverStartupDelay = Integer.parseInt(delay);
        }

        public int getSizeOfCache() {
            return sizeOfCache;
        }

        public void setSize_of_cache(int sizeOfCache) {
            this.sizeOfCache = sizeOfCache;
        }
    }

    public class ServiceConfiguration extends Configuration implements Serializable {

        private static final long serialVersionUID = 8707314702788040116L;
        private int minAppInstances = 1;
        private boolean minAppInstancesSet;

        private int maxAppInstances = 3;
        private boolean maxAppInstancesSet;

        private int queueLengthPerNode = 4;
        private boolean queueLengthPerNodeSet;

        private int roundsToAverage = 10;
        private boolean roundsToAverageSet;

        private int instancesPerScaleUp = 1;
        private boolean instancesPerScaleUpSet;

        private int messageExpiryTime = 60000; // milliseconds
        private boolean messageExpiryTimeSet;

        private List<String> hosts = new ArrayList<String>();
        private Map<String, String> urlSuffixes = new HashMap<String, String>();
        private boolean hostsSet;

        private String domain;

        private String tenantRange;
        private boolean tenantRangeSet;

        private String subDomain = Constants.DEFAULT_SUB_DOMAIN;
        private boolean subDomainSet;

        public String getTenantRange() {
            if (tenantRangeSet) {
                return tenantRange;
            } else if (defaultServiceConfig != null && defaultServiceConfig.tenantRangeSet) {
                return defaultServiceConfig.tenantRange;
            }
            return tenantRange;
        }

        public String getDomain() {
            return domain;
        }

        public List<String> getHosts() {
            if (hostsSet) {
                return hosts;
            } else if (defaultServiceConfig != null && defaultServiceConfig.hostsSet) {
                return defaultServiceConfig.hosts;
            }
            return hosts;
        }

        public int getMinAppInstances() {
            if (minAppInstancesSet) {
                return minAppInstances;
            } else if (defaultServiceConfig != null && defaultServiceConfig.minAppInstancesSet) {
                return defaultServiceConfig.minAppInstances;
            }
            return minAppInstances;
        }

        public int getMaxAppInstances() {
            if (maxAppInstancesSet) {
                return maxAppInstances;
            } else if (defaultServiceConfig != null && defaultServiceConfig.maxAppInstancesSet) {
                return defaultServiceConfig.maxAppInstances;
            }
            return maxAppInstances;
        }

        public int getQueueLengthPerNode() {
            if (queueLengthPerNodeSet) {
                return queueLengthPerNode;
            } else if (defaultServiceConfig != null && defaultServiceConfig.queueLengthPerNodeSet) {
                return defaultServiceConfig.queueLengthPerNode;
            }
            return queueLengthPerNode;
        }

        public int getRoundsToAverage() {
            if (roundsToAverageSet) {
                return roundsToAverage;
            } else if (defaultServiceConfig != null && defaultServiceConfig.roundsToAverageSet) {
                return defaultServiceConfig.roundsToAverage;
            }
            return roundsToAverage;
        }

        public int getInstancesPerScaleUp() {
            if (instancesPerScaleUpSet) {
                return instancesPerScaleUp;
            } else if (defaultServiceConfig != null && defaultServiceConfig.instancesPerScaleUpSet) {
                return defaultServiceConfig.instancesPerScaleUp;
            }
            return instancesPerScaleUp;
        }

        public int getMessageExpiryTime() {
            if (messageExpiryTimeSet) {
                return messageExpiryTime;
            } else if (defaultServiceConfig != null && defaultServiceConfig.messageExpiryTimeSet) {
                return defaultServiceConfig.messageExpiryTime;
            }
            return messageExpiryTime;
        }

        public String getSubDomain() {
            if (subDomainSet) {
                return subDomain;
            } else if (defaultServiceConfig != null && defaultServiceConfig.subDomainSet) {
                return defaultServiceConfig.subDomain;
            }
            return subDomain;
        }

        public void setMin_app_instances(int minAppInstances) {
            if (minAppInstances < 1) {
                LoadBalancerConfigUtil.handleException("minAppInstances in the autoscaler task configuration " +
                        "should be at least 1");
            }
            this.minAppInstances = minAppInstances;
            this.minAppInstancesSet = true;
        }

        public void setMax_app_instances(int maxAppInstances) {
            if (maxAppInstances < 1) {
                LoadBalancerConfigUtil.handleException("maxAppInstances in the autoscaler task configuration " +
                        "should be at least 1");
            }
            this.maxAppInstances = maxAppInstances;
            this.maxAppInstancesSet = true;
        }

        public void setQueue_length_per_node(int queueLengthPerNode) {
            this.queueLengthPerNode = queueLengthPerNode;
            this.queueLengthPerNodeSet = true;
        }

        public void setRounds_to_average(int roundsToAverage) {
            this.roundsToAverage = roundsToAverage;
            this.roundsToAverageSet = true;
        }

        public void setInstances_per_scale_up(int instancesPerScaleUp) {
            if (instancesPerScaleUp < 1) {
                LoadBalancerConfigUtil.handleException("instancesPerScaleUp in the autoscaler task configuration " +
                        "should be at least 1");
            }
            this.instancesPerScaleUp = instancesPerScaleUp;
            this.instancesPerScaleUpSet = true;
        }

        public void setMessage_expiry_time(int messageExpiryTime) {
            if (messageExpiryTime < 1) {
                LoadBalancerConfigUtil.handleException("messageExpiryTime in the autoscaler task configuration " +
                        "should be at least 1");
            }
            this.messageExpiryTime = messageExpiryTime;
            this.messageExpiryTimeSet = true;
        }

        public void setHosts(String hostsStr) {
            // clear all unnecessary values --> property will get overwritten
            hosts = new ArrayList<String>();
            // there can be multiple hosts, let's find out.
            String[] host = hostsStr.split(Constants.HOSTS_DELIMITER);

            for (String string : host) {
                if (!string.isEmpty()) {
                    this.hosts.add(string);
                }
            }

        }

        public void setUrl_suffix(String suffix) {
            // clear all unnecessary values --> property will get overwritten
            //hosts = new ArrayList<String>();
            // there can be multiple hosts, let's find out.
            String[] suffixes = suffix.split(Constants.HOSTS_DELIMITER);
            int count = 0;
            if(suffixes.length == this.hosts.size()) {
                for (String string : suffixes) {
                    if (!string.isEmpty()) {
                        this.urlSuffixes.put(this.hosts.get(count), string);
                        count++;
                    }
                }
            
            } else {
                //Error
            }
        }

        public Map<String, String> getUrl_suffix() {
            return this.urlSuffixes;
        }

        public void setTenant_range(String range) {
            this.tenantRange = range;
        }

        public void setSub_domain(String subDomain) {
            this.subDomain = subDomain;
            this.subDomainSet = true;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }
    }

    public Map<String, Set<String>> getHostNamesTracker() {
        return hostNamesTracker;
    }


    public Map<String, Map<String, ServiceConfiguration>> getServiceConfigurations() {
        return serviceConfigurations;
    }
}
