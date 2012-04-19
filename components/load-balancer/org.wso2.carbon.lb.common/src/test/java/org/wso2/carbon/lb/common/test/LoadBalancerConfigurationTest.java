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
package org.wso2.carbon.lb.common.test;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;

import org.wso2.carbon.lb.common.conf.LoadBalancerConfiguration;
import junit.framework.TestCase;

public class LoadBalancerConfigurationTest extends TestCase {

    public final void testInit() throws MalformedURLException {
        LoadBalancerConfiguration ec2LBConfig = new LoadBalancerConfiguration();
        
        File f = new File("src/test/resources/loadbalancer.conf");
        ec2LBConfig.init(f.getAbsolutePath());
        
        LoadBalancerConfiguration.LBConfiguration loadBalancerConfig = 
                ec2LBConfig.getLoadBalancerConfig();
        
        assertEquals(1, loadBalancerConfig.getInstances());
    }

    public final void testCreateLoadBalancerConfig() {
        String loadBalancerEle =
            "security_groups      stratos-appserver-lb;\n" +
            "instance_type        m1.large;\n" +
            "instances           1;\n" +
            "availability_zone    us-east-1c;";

        
        LoadBalancerConfiguration ec2LBConfig = new LoadBalancerConfiguration();
        ec2LBConfig.createLoadBalancerConfig(loadBalancerEle);
        LoadBalancerConfiguration.LBConfiguration loadBalancerConfig = 
                ec2LBConfig.getLoadBalancerConfig();
        System.err.println(loadBalancerConfig.getSecurityGroups()[0]);
        assertTrue(Arrays.equals(new String[] { "stratos-appserver-lb" },
                                 loadBalancerConfig.getSecurityGroups()));
        assertEquals("m1.large", loadBalancerConfig.getInstanceType());
        assertEquals(1, loadBalancerConfig.getInstances());
        assertEquals("us-east-1c", loadBalancerConfig.getAvailabilityZone());
    }

    public void testLoadBalancerConfigDefaultValues() throws XMLStreamException {
        String availabilityZone = "us-east-1c";
        String securityGroups = "default";
        String instanceType = "m1.large";
        String elasticIP = "${ELASTIC_IP}";
        int instances = 1;

        String loadBalancerEle = "";

        
        LoadBalancerConfiguration ec2LBConfig = new LoadBalancerConfiguration();
        ec2LBConfig.createLoadBalancerConfig(loadBalancerEle);
        LoadBalancerConfiguration.LBConfiguration loadBalancerConfig = ec2LBConfig.getLoadBalancerConfig();

        assertTrue(Arrays.equals(new String[]{securityGroups}, loadBalancerConfig.getSecurityGroups()));
        assertEquals(instanceType, loadBalancerConfig.getInstanceType());
        assertEquals(instances, loadBalancerConfig.getInstances());
        assertEquals(elasticIP, loadBalancerConfig.getElasticIP());
        assertEquals(availabilityZone, loadBalancerConfig.getAvailabilityZone());
    }
    
    public final void testCreateServicesConfig() {
        
        String servicesEle="defaults {\n" +
        		"availability_zone       us-east-1c;\n" +
        		"security_groups         default-2011-02-23;\n" +
        		"instance_type           m1.large;\n" +
        		"min_app_instances       1;\n" +
        		"max_app_instances       5;\n" +
        		"queue_length_per_node   400;\n" +
        		"rounds_to_average       10;\n" +
        		"instances_per_scale_up  1;\n" +
        		"message_expiry_time     60000;" +
        		"}\n" +
        		"appserver {\n" +
        		"hosts                   appserver.cloud-test.wso2.com,as.cloud-test.wso2.com;\n" +
        		"domains   {\n" +
        		"wso2.as1.domain {\n" +
        		"tenant_range    1-100;\n" +
        		"}\n" +
        		"wso2.as2.domain {\n" +
        		"tenant_range    101-200;\n" +
        		"}\n" +
        		"wso2.as3.domain {\n" +
        		"tenant_range    *;\n" +
        		"}\n" +
        		"}\n" +
        		"availability_zone       us-east-1z;\n" +
        		"security_groups         as,default;\n" + 
        		"}";
        
        LoadBalancerConfiguration ec2LBConfig = new LoadBalancerConfiguration();
        ec2LBConfig.createServicesConfig(servicesEle);

        LoadBalancerConfiguration.ServiceConfiguration asServiceConfig = ec2LBConfig.getServiceConfig("wso2.as1.domain");
        assertEquals(1, asServiceConfig.getInstancesPerScaleUp());
        assertEquals(5, asServiceConfig.getMaxAppInstances());
        assertEquals(1, asServiceConfig.getMinAppInstances());
        assertEquals(60000, asServiceConfig.getMessageExpiryTime());
        assertEquals(400, asServiceConfig.getQueueLengthPerNode());
        assertEquals(10, asServiceConfig.getRoundsToAverage());
        assertEquals("us-east-1z", asServiceConfig.getAvailabilityZone());
        assertEquals("m1.large", asServiceConfig.getInstanceType());
        assertTrue(Arrays.equals(new String[]{"as", "default"}, asServiceConfig.getSecurityGroups()));

     
    }

    public final void testGetServiceDomains() {
        
        String servicesEle="defaults {\n" +
                "availability_zone       us-east-1c;\n" +
                "security_groups         default-2011-02-23;\n" +
                "instance_type           m1.large;\n" +
                "min_app_instances       1;\n" +
                "max_app_instances       5;\n" +
                "queue_length_per_node   400;\n" +
                "rounds_to_average       10;\n" +
                "instances_per_scale_up  1;\n" +
                "message_expiry_time     60000;" +
                "}\n" +
                "appserver {\n" +
                "hosts                   appserver.cloud-test.wso2.com,as.cloud-test.wso2.com;\n" +
                "domains   {\n" +
                "wso2.as1.domain {\n" +
                "tenant_range    1-100;\n" +
                "}\n" +
                "wso2.as2.domain {\n" +
                "tenant_range    101-200;\n" +
                "}\n" +
                "wso2.as3.domain {\n" +
                "tenant_range    *;\n" +
                "}\n" +
                "}\n" +
                "availability_zone       us-east-1z;\n" +
                "security_groups         as,default;\n" + 
                "}";
        
        LoadBalancerConfiguration ec2LBConfig = new LoadBalancerConfiguration();
        ec2LBConfig.createServicesConfig(servicesEle);

        String[] serviceDomains = ec2LBConfig.getServiceDomains();
        assertEquals(3, serviceDomains.length);
        assertTrue("wso2.as1.domain".equals(serviceDomains[0]) 
                   || "wso2.as1.domain".equals(serviceDomains[1])
                   || "wso2.as1.domain".equals(serviceDomains[2]));
        assertTrue("wso2.as2.domain".equals(serviceDomains[0]) 
                   || "wso2.as2.domain".equals(serviceDomains[1])
                   || "wso2.as2.domain".equals(serviceDomains[2]));
        assertTrue("wso2.as3.domain".equals(serviceDomains[0]) 
                   || "wso2.as3.domain".equals(serviceDomains[1])
                   || "wso2.as3.domain".equals(serviceDomains[2]));
    }

   
}
