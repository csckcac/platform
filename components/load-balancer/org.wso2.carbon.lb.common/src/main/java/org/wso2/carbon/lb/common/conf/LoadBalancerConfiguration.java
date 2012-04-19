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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.commons.util.PropertyHelper;

import javax.xml.namespace.QName;
import org.wso2.carbon.lb.common.LBConfigParser;
import org.wso2.carbon.lb.common.conf.structure.Node;
import org.wso2.carbon.lb.common.conf.structure.NodeBuilder;
import org.wso2.carbon.lb.common.conf.util.LoadBalancerConfigUtil;
import org.wso2.carbon.lb.common.conf.util.Constants;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Data object which hold configuration data of the load analyzer task
 */
@SuppressWarnings("unused")
public class LoadBalancerConfiguration {


    /**
     * Key - service clustering domain
     */
    private Map<String, ServiceConfiguration> serviceConfigMap =
            new HashMap<String, ServiceConfiguration>();

    /**
     * Key - host, Value - Node with service clustering domains of this host
     */
    private Map<String, Node> hostDomainMap = new HashMap<String, Node>();
    private ServiceConfiguration defaultServiceConfig;
    private LBConfiguration lbConfig;
    
    private LBConfigParser lbConfParser;
    
    /**
     * LBConfig file as a String
     */
    private String lbConfigString;
    
    /**
     * LBConfig file's loadbalancer part in a Node object
     */
    private Node lbConfigNode = new Node();
    
    /**
     * LBConfig file's services part in a Node object
     */
    private Node servicesConfigNode = new Node();

    /**
     * Sample loadbalancer.conf:
     * 
     * loadbalancer {
     * securityGroups      stratos-appserver-lb;
     * instanceType        m1.large;
     * instances           1;
     * availabilityZone    us-east-1c;
     * payload             /mnt/payload.zip;
     * }
     * 
     * services {
     *      defaults {
     *          payload                 resources/cluster_node.zip;
     *          availability_zone       us-east-1c;
     *          security_groups         default-2011-02-23;
     *          instance_type           m1.large;
     *          min_app_instances       1;
     *          max_app_instances       5;
     *          queue_length_per_node   400;
     *          rounds_to_average       10;
     *          instances_per_scale_up  1;
     *          message_expiry_time     60000;
     *      }
     *          
     *      appserver {
     *          hosts                   appserver.cloud-test.wso2.com,as.cloud-test.wso2.com;
     *          domains   {
     *              wso2.as1.domain {
     *                  tenant_range    1-100;
     *              }
     *              wso2.as2.domain {
     *                  tenant_range    101-200;
     *              }
     *              wso2.as3.domain {
     *                  tenant_range    *;
     *              }
     *          }
     *          payload                 resources/cluster_node.zip;
     *          availability_zone       us-east-1c;
     *      }
     * }
     *
     * @param configURL URL of the load balancer config
     */
    public void init(String configURL) {

        if (configURL.startsWith("$system:")) {
            configURL = System.getProperty(configURL.substring("$system:".length()));
        }

        try {

            lbConfParser = new LBConfigParser();
            
            if(configURL.startsWith(File.separator)){
                lbConfigString = lbConfParser.createLBConfigString(configURL);
            }
            else{
                lbConfigString = lbConfParser.createLBConfigString(new URL(configURL).openStream());
            }
            

        } catch (Exception e) {
            throw new RuntimeException("Cannot read configuration file from URL " + configURL);
        }

        // Set load balancer config
        String loadBalancerEle =
            lbConfParser.getConfigElementFromString(lbConfigString, Constants.LOAD_BALANCER_ELEMENT);

        createLoadBalancerConfig(loadBalancerEle);

        // Set services config
        String servicesEle =
            lbConfParser.getConfigElementFromString(lbConfigString, Constants.SERVICES_ELEMENT);

        createServicesConfig(servicesEle);
    }


    public String getDomain(String host, int tenantId) {
        
        if (hostDomainMap.containsKey(host)) {
            for (Node aNode : hostDomainMap.get(host).getNodes()) {

                String tenantRange = aNode.getProperty(Constants.TENANT_RANGE_ELEMENT);
                
                //we should reach unlimited range after visiting all other ranges
                if(tenantRange.equals(Constants.UNLIMITED_TENANT_RANGE)){
                    return aNode.getName();
                }
                
                String[] limits = tenantRange.split(Constants.TENANT_RANGE_DELIMITER);
                
                if(limits.length != 2 ){
                    throw new RuntimeException("Malformed element "+Constants.TENANT_RANGE_ELEMENT+" which " +
                    		"is a child element of "+aNode.getName());
                }
                
                int lowerLimit = Integer.parseInt(limits[0]);
                int upperLimit = Integer.parseInt(limits[1]);
                
                if(tenantId >=lowerLimit && tenantId <= upperLimit){
                    return aNode.getName();
                }
            }
        }
        
        return "";
        
    }

    /**
     * Process the following element
     * 
     * loadbalancer {
     * securityGroups      stratos-appserver-lb;
     * instanceType        m1.large;
     * instances           1;
     * availabilityZone    us-east-1c;
     * }
     *
     * @param loadBalancerEle The loadBalancer element
     */
    public void createLoadBalancerConfig(String loadBalancerEle) {
        lbConfigNode.setName(Constants.LOAD_BALANCER_ELEMENT);
        createConfiguration(loadBalancerEle, lbConfig = new LBConfiguration(),
                lbConfigNode);
    }

    /**
     * Process the content of the following 'services' element
     * 
     * services {
     *      defaults {
     *          payload                 resources/cluster_node.zip;
     *          availability_zone       us-east-1c;
     *          security_groups         default-2011-02-23;
     *          instance_type           m1.large;
     *          min_app_instances       1;
     *          max_app_instances       5;
     *          queue_length_per_node   400;
     *          rounds_to_average       10;
     *          instances_per_scale_up  1;
     *          message_expiry_time     60000;
     *      }
     *          
     *      appserver {
     *          hosts                   appserver.cloud-test.wso2.com,as.cloud-test.wso2.com;
     *          domains   {
     *              wso2.as1.domain {
     *                  tenant_range    1-100;
     *              }
     *              wso2.as2.domain {
     *                  tenant_range    101-200;
     *              }
     *              wso2.as3.domain {
     *                  tenant_range    *;
     *              }
     *          }
     *          payload                 resources/cluster_node.zip;
     *          availability_zone       us-east-1c;
     *      }
     * }
     *
     * @param servicesEle The services element eg:
     * 
     * defaults {
     *          payload                 resources/cluster_node.zip;
     *          availability_zone       us-east-1c;
     *          security_groups         default-2011-02-23;
     *          instance_type           m1.large;
     *          min_app_instances       1;
     *          max_app_instances       5;
     *          queue_length_per_node   400;
     *          rounds_to_average       10;
     *          instances_per_scale_up  1;
     *          message_expiry_time     60000;
     *      }
     *          
     *      appserver {
     *          hosts                   appserver.cloud-test.wso2.com,as.cloud-test.wso2.com;
     *          domains   {
     *              wso2.as1.domain {
     *                  tenant_range    1-100;
     *              }
     *              wso2.as2.domain {
     *                  tenant_range    101-200;
     *              }
     *              wso2.as3.domain {
     *                  tenant_range    *;
     *              }
     *          }
     *          payload                 resources/cluster_node.zip;
     *          availability_zone       us-east-1c;
     *      }
     * 
     */
    public void createServicesConfig(String servicesEle) {
        
        servicesConfigNode.setName(Constants.SERVICES_ELEMENT);
        
        servicesConfigNode = NodeBuilder.buildNode(servicesConfigNode, servicesEle);
        
        // Building default configuration

        Node defaultNode = servicesConfigNode.findChildNodeByName(Constants.DEFAULTS_ELEMENT);

        createConfiguration(null, defaultServiceConfig = new ServiceConfiguration(null),
                            defaultNode);
        
        // Building custom services configuration
        
        for (Node serviceNode : servicesConfigNode.getNodes()) {
            //skip default node
            if(serviceNode != defaultNode){
                
                String serviceName = serviceNode.getName();
                
                // reading domains
                
                Node domainsNode;
                
                if (serviceNode.getNodes().isEmpty() || 
                  !(domainsNode = serviceNode.getNodes().get(0)).getName().equals(
                                                             Constants.DOMAIN_ELEMENT)) {
                    throw new RuntimeException("The mandatory domains element child of the "+serviceName+
                                           " element is not specified");
                }
                
                if(domainsNode.getNodes().isEmpty()){
                    throw new RuntimeException("No domain is specified under "+Constants.DOMAIN_ELEMENT+
                            " of "+serviceName+" element.");
                }
                
                //reading hosts
                String hosts =serviceNode.getProperty(Constants.HOSTS_ELEMENT);
                
                if (hosts == null) {
                    throw new RuntimeException("The mandatory hosts element, " +
                    		"which is a child of "+serviceName+" element is not specified");
                }
                
                String[] host = hosts.split(Constants.HOSTS_DELIMITER);
                
                for (String aHost : host) {
                    
                    if (aHost.isEmpty()) {
                        throw new RuntimeException("host cannot be empty");
                    }
                    if (hostDomainMap.containsKey(aHost)) {
                        throw new RuntimeException("host " + aHost + " has been duplicated in the configuration");
                    }
                    
                    // adds the domains node to map
                    hostDomainMap.put(aHost, domainsNode);
                    
                }
                
                for (Node domain : domainsNode.getNodes()) {
                    ServiceConfiguration serviceConfig = new ServiceConfiguration(domain.getName());
                    
                    //serviceNode is fully constructed hence we're sending null as the first argument
                    createConfiguration(null, serviceConfig, serviceNode);
                    serviceConfigMap.put(domain.getName(), serviceConfig);
                }
            }
        }
        
    }

    private void createConfiguration(String configEle, Configuration config, Node node) {
        
        // if the node is fully constructed, we avoid building it again.
        if (!node.isFullyConstructed()) {
            if (configEle == null) {
                throw new RuntimeException("The configuration element for " +
                    config.getClass().getName() + " is null");
            }

            node = NodeBuilder.buildNode(node, configEle);
        }
        
        try {
            
            for (Map.Entry<String, String> entry : node.getProperties().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                PropertyHelper.setInstanceProperty(key, value, config);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error setting values to " + config.getClass().getName(), e);
        }
    }

    private void setProperty(Object obj, OMElement propEle) throws IllegalAccessException,
                                                                   InvocationTargetException,
                                                                   NoSuchMethodException {
        String name = propEle.getAttributeValue(new QName("name"));
        String value = propEle.getAttributeValue(new QName("value"));
        PropertyHelper.setInstanceProperty(name, value, obj);
    }

    public LBConfiguration getLoadBalancerConfig() {
        return lbConfig;
    }

    public String[] getServiceDomains() {
        Set<String> domains = serviceConfigMap.keySet();
        return domains.toArray(new String[domains.size()]);
    }

    public ServiceConfiguration getServiceConfig(String domain) {
        return serviceConfigMap.get(domain);
    }

    @SuppressWarnings("unused")
    public abstract class Configuration {

        protected String availabilityZone = "us-east-1c";
        protected boolean availabilityZoneSet;

        protected String[] security_groups = new String[]{"default"};
        protected boolean securityGroupsSet;

        protected String instance_type = "m1.large";
        protected boolean instanceTypeSet;

        protected String additional_info = "WSO2 Autoscaled setup";

        public String getAdditionalInfo() {
            return additional_info;
        }

        public String getAvailabilityZone() {
            if (this instanceof LBConfiguration) {
                return availabilityZone;
            }
            if (availabilityZoneSet) {
                return availabilityZone;
            } else if (defaultServiceConfig != null && defaultServiceConfig.availabilityZoneSet) {
                return defaultServiceConfig.availabilityZone;
            }
            return availabilityZone;
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

        public void setAvailability_zone(String availabilityZone) {
            this.availabilityZone = LoadBalancerConfigUtil.replaceVariables(availabilityZone);
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

    @SuppressWarnings("unused")
    public class LBConfiguration extends Configuration {
        private String elasticIP = LoadBalancerConfigUtil.replaceVariables("${ELASTIC_IP}");
        private int instances = 1;

        public String getElasticIP() {
            return elasticIP;
        }

        public int getInstances() {
            return instances;
        }

        public void setElastic_IP(String elasticIP) {
            this.elasticIP = LoadBalancerConfigUtil.replaceVariables(elasticIP);
        }

        public void setInstances(int instances) {
            this.instances = instances;
        }
    }

    @SuppressWarnings("unused")
    public class ServiceConfiguration extends Configuration {
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

        private String hosts;
        private boolean hostsSet;
        
        private String domain;

        public ServiceConfiguration(String domain) {
            this.domain = domain;
        }

        public String getDomain() {
            return domain;
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
        
        public void setHosts(String hosts) {
            this.hosts = hosts;
            this.hostsSet = true;
        }
    }
}
