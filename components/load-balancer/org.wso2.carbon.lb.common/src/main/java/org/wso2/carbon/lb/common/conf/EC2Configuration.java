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

import org.apache.synapse.commons.util.PropertyHelper;

import org.wso2.carbon.lb.common.conf.structure.Node;
import org.wso2.carbon.lb.common.conf.structure.NodeBuilder;
import org.wso2.carbon.lb.common.conf.util.LoadBalancerConfigUtil;
import org.wso2.carbon.lb.common.conf.util.Constants;

import java.io.File;
import java.net.URL;
import java.util.Map;

/**
 * Data object which hold configuration data of the load ec2 configuration
 * This object looks similar to LoadBalancerConfiguration with some changes
 */
@SuppressWarnings("unused")
public class EC2Configuration extends LoadBalancerConfiguration{

    /**
     * The private key for ec2
     */
    private String ec2_access_key = "access.key";
    /**
     * The certificate for ec2
     */
    private String ec2_private_key = "private.key";
    /**
     * The key pair
     */
    private String ec2_ssh_key = "stratos - 1.0.0-keypair";

    /**
     * The EPR of the Web service which will be called for instance management
     */
    private String instance_mgt_epr = "https://ec2.amazonaws.com/";

    /**
     * Disable terminating instances via AWS API calls
     */
    private boolean disable_api_termination;

    /**
     * Enable monitoring for the instances
     */
    private boolean enable_monitoring;


    public String getEc2_access_key() {
        return ec2_access_key;
    }

    public void setEc2_access_key(String ec2_access_key) {
        this.ec2_access_key = LoadBalancerConfigUtil.replaceVariables(ec2_access_key);
    }

    public String getEc2_private_key() {
        return ec2_private_key;
    }

    public void setEc2_private_key(String ec2_private_key) {
        this.ec2_private_key = LoadBalancerConfigUtil.replaceVariables(ec2_private_key);
    }

    public String getEc2_ssh_key() {
        return ec2_ssh_key;
    }

    public void setEc2_ssh_key(String ec2_ssh_key) {
        this.ec2_ssh_key = ec2_ssh_key;
    }

    public String getInstance_mgt_epr() {
        return instance_mgt_epr;
    }

    public void setInstance_mgt_epr(String instance_mgt_epr) {
        this.instance_mgt_epr = instance_mgt_epr;
    }

    public boolean getDisable_api_termination() {
        return disable_api_termination;
    }

    public void setDisable_api_termination(boolean disable_api_termination) {
        this.disable_api_termination = disable_api_termination;
    }

    public boolean getEnable_monitoring() {
        return enable_monitoring;
    }

    public void setEnable_monitoring(boolean enable_monitoring) {
        this.enable_monitoring = enable_monitoring;
    }


    /**
     * Initialize ec2 configuration
     *
     * Sample ec2.conf
     *
     * ec2_access_key          ${AWS_ACCESS_KEY};
     * ec2_private_key         ${AWS_PRIVATE_KEY};
     * ec2_ssh_key             stratos-1.0.0-keypair;
     * instance_mgt_epr        https://ec2.amazonaws.com/;
     * disable_api_termination true;
     * enable_monitoring       false;

     * loadbalancer {
     *    securityGroups      stratos-appserver-lb;
     *    instanceType        m1.large;
     *    elasticIP           ${ELASTIC_IP};
     *    availability_zone    us-east-1c;
     *    payload             /mnt/payload.zip;
     * }

     * services {
     *     defaults {
     *         availability_zone       us-east-1c;
     *         security_groups         default-2011-02-23;
     *         instance_type           m1.large;
     *     }

     *     appserver {
     *         payload                 resources/cluster_node.zip;
     *         availability_zone       us-east-1c;
     *         securityGroups          stratos-appserver;
     *         domains   {
     *             wso2.as1.domain {
     *                 payload                 resources/cluster_node.zip;
     *             	  availability_zone       us-east-1c;
     *             }
     *             wso2.as2.domain {
     *                 payload                 resources/cluster_node.zip;
     *             	  availability_zone       us-east-1c;
     *             }
     *             wso2.as3.domain {
     *             }
     *         }
     *     }
     * }

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
            throw new RuntimeException("Cannot read configuration file from " + configURL);
        }

        // build a Node object for whole loadbalancer.conf
        rootNode = new Node();
        rootNode.setName("root");
        rootNode = NodeBuilder.buildNode(rootNode, lbConfigString);

        //Set all the properties under root node (ec2_access_key , ec2_private_key  etc... )
        Map<String, String> rootProperties= rootNode.getProperties();

        for (Map.Entry<String, String> entry : rootProperties.entrySet()) {
            PropertyHelper.setInstanceProperty(entry.getKey(), entry.getValue(), this);
        }

        // load loadbalancer node
        Node lbConfigNode = rootNode.findChildNodeByName(Constants.LOAD_BALANCER_ELEMENT);

        if (lbConfigNode == null) {
            throw new RuntimeException("Mandatory " + Constants.LOAD_BALANCER_ELEMENT +
                    " element can not be" + " found in configuration file.");
        }


        // Set load balancer configuration
        createConfiguration(lbConfig = new LBConfiguration(), lbConfigNode);

        // load services node
        Node servicesConfigNode = rootNode.findChildNodeByName(Constants.SERVICES_ELEMENT);

        if (servicesConfigNode == null) {
            throw new RuntimeException("Mandatory " + Constants.SERVICES_ELEMENT +
                " element can not be found in configuration file.");
        }

        // Set services configuration
        createServicesConfig(servicesConfigNode);
    }

    public void createServicesConfig(Node servicesConfigNode) {

        // Building default configuration

        Node defaultNode = servicesConfigNode.findChildNodeByName(Constants.DEFAULTS_ELEMENT);

        createConfiguration( defaultServiceConfig = new ServiceConfiguration(null),
                defaultNode);

        // Building custom services configuration

        for (Node serviceNode : servicesConfigNode.getChildNodes()) {
            //skip default node
            if(serviceNode != defaultNode){

                String serviceName = serviceNode.getName();

                // reading domains

                Node domainsNode;

                if (serviceNode.getChildNodes().isEmpty() ||
                  !(domainsNode = serviceNode.getChildNodes().get(0)).getName().equals(
                                                             Constants.DOMAIN_ELEMENT)) {
                    throw new RuntimeException("The mandatory domains element child of the "+serviceName+
                                           " element is not specified");
                }

                if(domainsNode.getChildNodes().isEmpty()){
                    throw new RuntimeException("No domain is specified under "+Constants.DOMAIN_ELEMENT+
                            " of "+serviceName+" element.");
                }

                for (Node domain : domainsNode.getChildNodes()) {
                    ServiceConfiguration serviceConfig = new ServiceConfiguration(domain.getName());

                    createConfiguration(serviceConfig, domain, serviceNode );
                    serviceConfigMap.put(domain.getName(), serviceConfig);
                }
            }
        }

    }

    /**
     *  Create configuration for the given node
     * @param config: Service configuration
     * @param node : Configuration node for this service
     * @param parentNode: Configuration parent node for this service (This is used to drag default properties).
     */
    protected void createConfiguration(Configuration config, Node node, Node parentNode) {

        if(node == null){
            throw new RuntimeException("The configuration element for " +
                  config.getClass().getName() + " is null");
        }

        // First set parentNode related property values for Configuration (these are the defaults)
        try {
            for (Map.Entry<String, String> entry : parentNode.getProperties().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                PropertyHelper.setInstanceProperty(key, value, config);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error setting values to " + config.getClass().getName(), e);
        }

        // Then add node specified properties to configuration (override defaults if exists)
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



}
