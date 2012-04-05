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
package org.wso2.carbon.mediator.autoscale.ec2autoscale;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.util.PropertyHelper;

import javax.xml.namespace.QName;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Data object which hold configuration data of the EC2 load analyzer task
 */
@SuppressWarnings("unused")
public class EC2LoadBalancerConfiguration {

    /**
     * The private key for ec2
     */
    private String ec2AccessKey = "access.key";
    /**
     * The certificate for ec2
     */
    private String ec2PrivateKey = "private.key";
    /**
     * The key pair
     */
    private String sshKey = "stratos - 1.0.0-keypair";

    /**
     * The EPR of the Web service which will be called for instance management
     */
    private String instanceMgtEPR = "https://ec2.amazonaws.com/";

    /**
     * Disable terminating instances via AWS API calls
     */
    private boolean disableApiTermination;

    /**
     * Enable monitoring for the instances
     */
    private boolean enableMonitoring;

    /**
     * Key - service clustering domain
     */
    private Map<String, ServiceConfiguration> serviceConfigMap =
            new HashMap<String, ServiceConfiguration>();

    /**
     * Key - host, Value - service clustering domain
     */
    private Map<String, String> hostDomainMap = new HashMap<String, String>();
    private ServiceConfiguration defaultServiceConfig;
    private LBConfiguration lbConfig;

    /**
     * <pre>
     * <loadBalancerConfig xmlns="http://ws.apache.org/ns/synapse">
     * <property name="ec2PrivateKey" value="/mnt/payload/pk.pem"/>
     * <property name="ec2Cert" value="/mnt/payload/cert.pem"/>
     * <property name="sshKey" value="stratos-1.0.0-keypair"/>
     * <property name="instanceMgtEPR" value="https://ec2.amazonaws.com/"/>
     * <property name="disableApiTermination" value="true"/>
     * <property name="enableMonitoring" value="true"/>
     * <loadBalancer>
     * <property name="securityGroups" value="stratos-appserver-lb"/>
     * <property name="instanceType" value="m1.large"/>
     * <property name="instances" value="1"/>
     * <property name="elasticIP" value="${ELASTIC_IP}"/>
     * <property name="availabilityZone" value="us-east-1c"/>
     * <property name="payload" value="/mnt/payload.zip"/>
     * </loadBalancer>
     * <p/>
     * <services>
     * <defaults>
     * <property name="payload" value="resources/cluster_node.zip"/>
     * <property name="availabilityZone" value="us-east-1c"/>
     * <property name="securityGroups" value="default-2011-02-23"/>
     * <property name="instanceType" value="m1.large"/>
     * <property name="minAppInstances" value="1"/>
     * <property name="maxAppInstances" value="5"/>
     * <property name="queueLengthPerNode" value="400"/>
     * <property name="roundsToAverage" value="10"/>
     * <property name="instancesPerScaleUp" value="1"/>
     * <property name="messageExpiryTime" value="60000"/>
     * <property name="preserveOldestInstance" value="true"/>
     * </defaults>
     * <service>
     * <hosts>
     * <host>cloud-test.wso2.com</host>
     * </hosts>
     * <domain>wso2.manager.domain</domain>
     * </service>
     * <service>
     * <hosts>
     * <host>appserver.cloud-test.wso2.com</host>
     * <host>as.cloud-test.wso2.com</host>
     * </hosts>
     * <domain>wso2.as.domain</domain>
     * <p/>
     * <property name="payload" value="resources/cluster_node.zip"/>
     * <property name="availabilityZone" value="us-east-1c"/>
     * </service>
     * <service>
     * <hosts>
     * <host>esb.cloud-test.wso2.com</host>
     * </hosts>
     * <domain>wso2.esb.domain</domain>
     * <p/>
     * <property name="payload" value="resources/cluster_node.zip"/>
     * <property name="minAppInstances" value="1"/>
     * <property name="maxAppInstances" value="5"/>
     * <property name="queueLengthPerNode" value="400"/>
     * <property name="roundsToAverage" value="10"/>
     * <property name="instancesPerScaleUp" value="1"/>
     * <property name="availabilityZone" value="us-east-1c"/>
     * <property name="securityGroups" value="ds-2011-02-23"/>
     * </service>
     * <service>
     * <hosts>
     * <host>governance.cloud-test.wso2.com</host>
     * </hosts>
     * <domain>wso2.governance.domain</domain>
     * </service>
     * <service>
     * <hosts>
     * <host>identity.cloud-test.wso2.com</host>
     * </hosts>
     * <domain>wso2.is.domain</domain>
     * </service>
     * </services>
     * </loadBalancerConfig>
     * </pre>
     *
     * @param configURL URL of the load balancer config
     */
    public void init(String configURL) {
        if (configURL.startsWith("$system:")) {
            configURL = System.getProperty(configURL.substring("$system:".length()));
        }
        StAXOMBuilder builder;
        try {
            builder = new StAXOMBuilder(new URL(configURL).openStream());
        } catch (Exception e) {
            throw new RuntimeException("Cannot read configuration file from URL " + configURL);
        }
        OMElement loadBalancerConfigEle = builder.getDocumentElement();

        // Set all properties
        try {
            for (Iterator<OMElement> iter = loadBalancerConfigEle.getChildrenWithLocalName("property");
                 iter.hasNext();) {
                setProperty(this, iter.next());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error setting values to " + this.getClass().getName(), e);
        }

        // Set load balancer config
        OMElement loadBalancerEle =
                loadBalancerConfigEle.getFirstChildWithName(new QName(SynapseConstants.SYNAPSE_NAMESPACE,
                                                                      "loadBalancer"));
        createLoadBalancerConfig(loadBalancerEle);

        // Set services config
        OMElement servicesEle =
                loadBalancerConfigEle.getFirstChildWithName(new QName(SynapseConstants.SYNAPSE_NAMESPACE,
                                                                      "services"));
        createServicesConfig(servicesEle);
    }

    public String getEc2AccessKey() {
        return ec2AccessKey;
    }

    public void setEc2AccessKey(String ec2AccessKey) {
        this.ec2AccessKey = AutoscaleUtil.replaceVariables(ec2AccessKey);
    }

    public String getEc2PrivateKey() {
        return ec2PrivateKey;
    }

    public void setEc2PrivateKey(String ec2PrivateKey) {
        this.ec2PrivateKey = AutoscaleUtil.replaceVariables(ec2PrivateKey);
    }

    public String getSshKey() {
        return sshKey;
    }

    public void setSshKey(String sshKey) {
        this.sshKey = sshKey;
    }

    public String getInstanceMgtEPR() {
        return instanceMgtEPR;
    }

    public void setInstanceMgtEPR(String instanceMgtEPR) {
        this.instanceMgtEPR = instanceMgtEPR;
    }

    public boolean getDisableApiTermination() {
        return disableApiTermination;
    }

    public void setDisableApiTermination(boolean disableApiTermination) {
        this.disableApiTermination = disableApiTermination;
    }

    public boolean getEnableMonitoring() {
        return enableMonitoring;
    }

    public void setEnableMonitoring(boolean enableMonitoring) {
        this.enableMonitoring = enableMonitoring;
    }

    public String getDomain(String host) {
        String domain = hostDomainMap.get(host);
        if(domain == null){
            int indexOfDot;
            if ((indexOfDot = host.indexOf(".")) != -1) {
                domain = getDomain(host.substring(indexOfDot + 1));
            }
        }
        return domain;
    }

    /**
     * Process the following element
     * <loadBalancer>
     * <property name="securityGroups" value="stratos-appserver-lb,cloud-mysql,default"/>
     * <property name="instanceType" value="m1.large"/>
     * <property name="instances" value="1"/>
     * <property name="elasticIP" value="${ELASTIC_IP}"/>
     * <property name="availabilityZone" value="us-east-1c"/>
     * <property name="payload" value="/mnt/payload.zip"/>
     * </loadBalancer>
     *
     * @param loadBalancerEle The loadBalancer element
     */
    void createLoadBalancerConfig(OMElement loadBalancerEle) {
        createConfiguration(loadBalancerEle, lbConfig = new LBConfiguration());
    }

    /**
     * Process the following element
     *
     * @param servicesEle The services element
     */
    void createServicesConfig(OMElement servicesEle) {
        OMElement defaultsEle = servicesEle.getFirstChildWithName(new QName(SynapseConstants.SYNAPSE_NAMESPACE,
                                                                            "defaults"));
        if (defaultsEle != null) {
            createConfiguration(defaultsEle, defaultServiceConfig = new ServiceConfiguration(null));
        }
        for (Iterator<OMElement> iter = servicesEle.getChildrenWithLocalName("service");
             iter.hasNext();) {
            OMElement serviceEle = iter.next();
            OMElement domainEle = serviceEle.getFirstChildWithName(new QName(SynapseConstants.SYNAPSE_NAMESPACE,
                                                                             "domain"));
            String domain;
            if (domainEle == null || (domain = domainEle.getText()).isEmpty()) {
                throw new RuntimeException("The mandatory domain element child of the service element is not specified");
            }
            OMElement hostsEle = serviceEle.getFirstChildWithName(new QName(SynapseConstants.SYNAPSE_NAMESPACE,
                                                                            "hosts"));
            if (hostsEle == null) {
                throw new RuntimeException("The mandatory hosts element child of the service element is not specified");
            }
            for (Iterator<OMElement> hostsIter = hostsEle.getChildrenWithLocalName("host");
                 hostsIter.hasNext();) {
                OMElement hostEle = hostsIter.next();
                String host;
                if ((host = hostEle.getText()).isEmpty()) {
                    throw new RuntimeException("host cannot be empty");
                }
                if (hostDomainMap.containsKey(host)) {
                    throw new RuntimeException("host " + host + " has been duplicated in the configuration");
                }
                hostDomainMap.put(host, domain);
            }
            ServiceConfiguration serviceConfig = new ServiceConfiguration(domain);
            createConfiguration(serviceEle, serviceConfig);
            serviceConfigMap.put(domain, serviceConfig);
        }
    }

    private void createConfiguration(OMElement configEle, Configuration config) {
        if (configEle == null) {
            throw new RuntimeException("The configuration element for " + config.getClass().getName() + " is null");
        }
        try {
            for (Iterator<OMElement> iter = configEle.getChildrenWithLocalName("property");
                 iter.hasNext();) {
                OMElement propEle = iter.next();
                setProperty(config, propEle);
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
        protected String imageId = System.getenv("ami_id");

        protected String payload;
        protected boolean payloadSet;

        protected String availabilityZone = "us-east-1c";
        protected boolean availabilityZoneSet;

        protected String[] securityGroups = new String[]{"default"};
        protected boolean securityGroupsSet;

        protected String instanceType = "m1.large";
        protected boolean instanceTypeSet;

        protected String additionalInfo = "WSO2 Autoscaled setup";

        public String getImageId() {
            return imageId;
        }

        public String getAdditionalInfo() {
            return additionalInfo;
        }

        public String getUserData() {
            if (payload == null) {
                payload = AutoscaleUtil.getUserData("resources/cluster_node.zip");
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
                return securityGroups;
            }
            if (securityGroupsSet) {
                return securityGroups;
            } else if (defaultServiceConfig != null && defaultServiceConfig.securityGroupsSet) {
                return defaultServiceConfig.securityGroups;
            }
            return securityGroups;
        }

        public String getEc2InstanceType() {
            if (this instanceof LBConfiguration) {
                return instanceType;
            }
            if (instanceTypeSet) {
                return instanceType;
            } else if (defaultServiceConfig != null && defaultServiceConfig.instanceTypeSet) {
                return defaultServiceConfig.instanceType;
            }
            return instanceType;
        }

        public void setPayload(String payload) {
            this.payload = AutoscaleUtil.getUserData(AutoscaleUtil.replaceVariables(payload));
            this.payloadSet = true;
        }

        public void setAvailabilityZone(String availabilityZone) {
            this.availabilityZone = AutoscaleUtil.replaceVariables(availabilityZone);
            this.availabilityZoneSet = true;
        }

        public void setSecurityGroups(String securityGroups) {
            this.securityGroups = AutoscaleUtil.replaceVariables(securityGroups).split(",");
            this.securityGroupsSet = true;
        }

        public void setInstanceType(String instanceType) {
            this.instanceType = AutoscaleUtil.replaceVariables(instanceType);
            this.instanceTypeSet = true;
        }

        public void setImageId(String imageId) {
            this.imageId = AutoscaleUtil.replaceVariables(imageId);
        }
    }

    @SuppressWarnings("unused")
    public class LBConfiguration extends Configuration {
        private String elasticIP = AutoscaleUtil.replaceVariables("${ELASTIC_IP}");
        private int instances = 1;

        public String getElasticIP() {
            return elasticIP;
        }

        public int getInstances() {
            return instances;
        }

        public void setElasticIP(String elasticIP) {
            this.elasticIP = AutoscaleUtil.replaceVariables(elasticIP);
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

        private int queueLengthPerNode = 400;
        private boolean queueLengthPerNodeSet;

        private int roundsToAverage = 10;
        private boolean roundsToAverageSet;

        private int instancesPerScaleUp = 1;
        private boolean instancesPerScaleUpSet;

        private int messageExpiryTime = 60000; // milliseconds
        private boolean messageExpiryTimeSet;

        private double loadAverageLowerLimit = 2.0;
        private boolean loadAverageLowerLimitSet;

        private double loadAverageHigherLimit = 5.0;
        private boolean loadAverageHigherLimitSet;

        private int serviceHttpPort = 80;
        private boolean serviceHttpPortSet;

        private int serviceHttpsPort = 443;
        private boolean serviceHttpsPortSet;

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

        public void setMinAppInstances(int minAppInstances) {
            if (minAppInstances < 1) {
                AutoscaleUtil.handleException("minAppInstances in the autoscaler task configuration " +
                                              "should be at least 1");
            }
            this.minAppInstances = minAppInstances;
            this.minAppInstancesSet = true;
        }

        public void setMaxAppInstances(int maxAppInstances) {
            if (maxAppInstances < 1) {
                AutoscaleUtil.handleException("maxAppInstances in the autoscaler task configuration " +
                                              "should be at least 1");
            }
            this.maxAppInstances = maxAppInstances;
            this.maxAppInstancesSet = true;
        }

        public void setQueueLengthPerNode(int queueLengthPerNode) {
            this.queueLengthPerNode = queueLengthPerNode;
            this.queueLengthPerNodeSet = true;
        }

        public void setRoundsToAverage(int roundsToAverage) {
            this.roundsToAverage = roundsToAverage;
            this.roundsToAverageSet = true;
        }

        public void setInstancesPerScaleUp(int instancesPerScaleUp) {
            if (instancesPerScaleUp < 1) {
                AutoscaleUtil.handleException("instancesPerScaleUp in the autoscaler task configuration " +
                                              "should be at least 1");
            }
            this.instancesPerScaleUp = instancesPerScaleUp;
            this.instancesPerScaleUpSet = true;
        }

        public void setMessageExpiryTime(int messageExpiryTime) {
            if (messageExpiryTime < 1) {
                AutoscaleUtil.handleException("messageExpiryTime in the autoscaler task configuration " +
                                              "should be at least 1");
            }
            this.messageExpiryTime = messageExpiryTime;
            this.messageExpiryTimeSet = true;
        }

        public double getLoadAverageLowerLimit() {
            if (loadAverageLowerLimitSet) {
                return loadAverageLowerLimit;
            } else if (defaultServiceConfig != null &&
                    defaultServiceConfig.loadAverageLowerLimitSet) {
                return defaultServiceConfig.loadAverageLowerLimit;
            }
            return loadAverageLowerLimit;
        }

        public void setLoadAverageLowerLimit(double loadAverageLowerLimit) {
            if (loadAverageLowerLimit <= 0) {
                AutoscaleUtil.handleException("The lower limit of the load average - " +
                        "LoadAverageLowerLimit should be a positive value");
            }
            this.loadAverageLowerLimit = loadAverageLowerLimit;
            this.loadAverageLowerLimitSet = true;
        }

        public double getLoadAverageHigherLimit() {
            if (loadAverageHigherLimitSet) {
                return loadAverageHigherLimit;
            } else if (defaultServiceConfig != null &&
                    defaultServiceConfig.loadAverageHigherLimitSet) {
                return defaultServiceConfig.loadAverageHigherLimit;
            }
            return loadAverageHigherLimit;
        }

        public void setLoadAverageHigherLimit(double loadAverageHigherLimit) {
            if (loadAverageHigherLimit <= getLoadAverageLowerLimit()) {
                AutoscaleUtil.handleException("The higher limit of the load average " +
                        "LoadAverageHigherLimit should be greater than the lower limit - " +
                        "LoadAverageLowerLimit");
            }
            this.loadAverageHigherLimit = loadAverageHigherLimit;
            this.loadAverageHigherLimitSet = true;
        }

        public int getServiceHttpsPort() {
            if (serviceHttpsPortSet) {
                return serviceHttpsPort;
            } else if (defaultServiceConfig != null &&
                    defaultServiceConfig.serviceHttpsPortSet) {
                return defaultServiceConfig.serviceHttpsPort;
            }
            return serviceHttpsPort;
        }

        public void setServiceHttpsPort(int serviceHttpsPort) {
            this.serviceHttpsPort = serviceHttpsPort;
            this.serviceHttpsPortSet = true;
        }

        public int getServiceHttpPort() {
            if (serviceHttpPortSet) {
                return serviceHttpPort;
            } else if (defaultServiceConfig != null &&
                    defaultServiceConfig.serviceHttpPortSet) {
                return defaultServiceConfig.serviceHttpPort;
            }
            return serviceHttpPort;
        }

        public void setServiceHttpPort(int serviceHttpPort) {
            this.serviceHttpPort = serviceHttpPort;
            this.serviceHttpPortSet = true;
        }
    }
}
