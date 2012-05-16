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
package org.wso2.carbon.mediator.autoscale.lbautoscale;

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.Arrays;

/**
 * EC2LoadBalancerConfigurationTest
 */
public class EC2LoadBalancerConfigurationTest extends TestCase {

    public void testLoadBalancerConfig() throws XMLStreamException {
        String xml = "<loadBalancer>\n" +
                     "                <property name=\"securityGroups\" value=\"stratos-lb\"/>\n" +
                     "                <property name=\"instanceType\" value=\"m1.large\"/>\n" +
                     "                <property name=\"instances\" value=\"11\"/>\n" +
                     "                <property name=\"elasticIP\" value=\"11.11.23.45\"/>\n" +
                     "                <property name=\"availabilityZone\" value=\"us-east-1z\"/>\n" +
                     "                <property name=\"payload\" value=\"src/test/resources/lb.zip\"/>\n" +
                     "            </loadBalancer>";

        StAXOMBuilder builder = new StAXOMBuilder(new ByteArrayInputStream(xml.getBytes()));
        OMElement loadBalancerEle = builder.getDocumentElement();
        EC2LoadBalancerConfiguration ec2LBConfig = new EC2LoadBalancerConfiguration();
        ec2LBConfig.createLoadBalancerConfig(loadBalancerEle);
        EC2LoadBalancerConfiguration.LBConfiguration loadBalancerConfig = ec2LBConfig.getLoadBalancerConfig();

        assertTrue(Arrays.equals(new String[]{"stratos-lb"}, loadBalancerConfig.getSecurityGroups()));
        assertEquals("m1.large", loadBalancerConfig.getEc2InstanceType());
        assertEquals(11, loadBalancerConfig.getInstances());
        assertEquals("11.11.23.45", loadBalancerConfig.getElasticIP());
        assertEquals(AutoscaleUtil.getUserData("src/test/resources/lb.zip"),
                     loadBalancerConfig.getUserData());
        assertEquals("us-east-1z", loadBalancerConfig.getAvailabilityZone());
    }

    public void testLoadBalancerConfigDefaultValues() throws XMLStreamException {
        String payload = "resources/cluster_node.zip";
        String availabilityZone = "us-east-1c";
        String securityGroups = "default";
        String instanceType = "m1.large";
        String elasticIP = "${ELASTIC_IP}";
        int instances = 1;

        String xml = "<loadBalancer/>";

        StAXOMBuilder builder = new StAXOMBuilder(new ByteArrayInputStream(xml.getBytes()));
        OMElement loadBalancerEle = builder.getDocumentElement();
        EC2LoadBalancerConfiguration ec2LBConfig = new EC2LoadBalancerConfiguration();
        ec2LBConfig.createLoadBalancerConfig(loadBalancerEle);
        EC2LoadBalancerConfiguration.LBConfiguration loadBalancerConfig = ec2LBConfig.getLoadBalancerConfig();

        assertTrue(Arrays.equals(new String[]{securityGroups}, loadBalancerConfig.getSecurityGroups()));
        assertEquals(instanceType, loadBalancerConfig.getEc2InstanceType());
        assertEquals(instances, loadBalancerConfig.getInstances());
        assertEquals(elasticIP, loadBalancerConfig.getElasticIP());
        assertEquals(AutoscaleUtil.getUserData(payload), loadBalancerConfig.getUserData());
        assertEquals(availabilityZone, loadBalancerConfig.getAvailabilityZone());
    }

    public void testServicesConfig() throws XMLStreamException {

        String xml = "<services xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                     "                <defaults>\n" +
                     "                    <property name=\"payload\" value=\"src/test/resources/default.zip\"/>\n" +
                     "                    <property name=\"availabilityZone\" value=\"us-east-1c\"/>\n" +
                     "                    <property name=\"securityGroups\" value=\"xxx-group,yyy-group\"/>\n" +
                     "                    <property name=\"instanceType\" value=\"m1.xlarge\"/>\n" +
                     "                    <property name=\"minAppInstances\" value=\"10\"/>\n" +
                     "                    <property name=\"maxAppInstances\" value=\"15\"/>\n" +
                     "                    <property name=\"queueLengthPerNode\" value=\"431\"/>\n" +
                     "                    <property name=\"roundsToAverage\" value=\"11\"/>\n" +
                     "                    <property name=\"instancesPerScaleUp\" value=\"3\"/>\n" +
                     "                    <property name=\"messageExpiryTime\" value=\"61000\"/>\n" +
                     "                </defaults>\n" +
                     "                <service>\n" +
                     "                    <hosts>\n" +
                     "                        <host>appserver.cloud-test.wso2.com</host>\n" +
                     "                        <host>as.cloud-test.wso2.com</host>\n" +
                     "                     </hosts>" +
                     "                    <domain>wso2.as.domain</domain>\n" +
                     "                    <property name=\"payload\" value=\"src/test/resources/cluster_as.zip\"/>\n" +
                     "                    <property name=\"availabilityZone\" value=\"us-east-1x\"/>\n" +
                     "                </service>\n" +
                     "                <service>\n" +
                     "                    <hosts>\n" +
                     "                        <host>data.cloud-test.wso2.com</host>\n" +
                     "                        <host>ds.cloud-test.wso2.com</host>\n" +
                     "                     </hosts>" +
                     "                    <domain>wso2.ds.domain</domain>\n" +
                     "                </service>\n" +
                     "            </services>";

        StAXOMBuilder builder = new StAXOMBuilder(new ByteArrayInputStream(xml.getBytes()));
        OMElement servicesEle = builder.getDocumentElement();
        EC2LoadBalancerConfiguration ec2LBConfig = new EC2LoadBalancerConfiguration();
        ec2LBConfig.createServicesConfig(servicesEle);

        EC2LoadBalancerConfiguration.ServiceConfiguration asServiceConfig = ec2LBConfig.getServiceConfig("wso2.as.domain");
        assertEquals(3, asServiceConfig.getInstancesPerScaleUp());
        assertEquals(15, asServiceConfig.getMaxAppInstances());
        assertEquals(10, asServiceConfig.getMinAppInstances());
        assertEquals(61000, asServiceConfig.getMessageExpiryTime());
        assertEquals(431, asServiceConfig.getQueueLengthPerNode());
        assertEquals(11, asServiceConfig.getRoundsToAverage());
        assertEquals("us-east-1x", asServiceConfig.getAvailabilityZone());
        assertEquals("m1.xlarge", asServiceConfig.getEc2InstanceType());
        assertEquals(AutoscaleUtil.getUserData("src/test/resources/cluster_as.zip"),
                     asServiceConfig.getUserData());
        assertTrue(Arrays.equals(new String[]{"xxx-group", "yyy-group"}, asServiceConfig.getSecurityGroups()));

        EC2LoadBalancerConfiguration.ServiceConfiguration dsServiceConfig = ec2LBConfig.getServiceConfig("wso2.ds.domain");
        assertEquals(3, dsServiceConfig.getInstancesPerScaleUp());
        assertEquals(15, dsServiceConfig.getMaxAppInstances());
        assertEquals(10, dsServiceConfig.getMinAppInstances());
        assertEquals(61000, dsServiceConfig.getMessageExpiryTime());
        assertEquals(431, dsServiceConfig.getQueueLengthPerNode());
        assertEquals(11, dsServiceConfig.getRoundsToAverage());
        assertEquals("us-east-1c", dsServiceConfig.getAvailabilityZone());
        assertEquals("m1.xlarge", dsServiceConfig.getEc2InstanceType());
        assertEquals(AutoscaleUtil.getUserData("src/test/resources/default.zip"),
                     dsServiceConfig.getUserData());
        assertTrue(Arrays.equals(new String[]{"xxx-group", "yyy-group"}, dsServiceConfig.getSecurityGroups()));
    }

    public void testServicesConfigNoDefaults() throws XMLStreamException {

        String xml = "<services xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                     "                <service>\n" +
                     "                    <domain>wso2.as.domain</domain>\n" +
                     "                    <hosts>\n" +
                     "                        <host>appserver.cloud-test.wso2.com</host>\n" +
                     "                        <host>as.cloud-test.wso2.com</host>\n" +
                     "                     </hosts>" +
                     "                    <property name=\"payload\" value=\"src/test/resources/cluster_as.zip\"/>\n" +
                     "                    <property name=\"availabilityZone\" value=\"us-east-1x\"/>\n" +
                     "                </service>\n" +
                     "                <service>\n" +
                     "                    <hosts>\n" +
                     "                        <host>ds.cloud-test.wso2.com</host>\n" +
                     "                        <host>data.cloud-test.wso2.com</host>\n" +
                     "                     </hosts>" +
                     "                    <domain>wso2.ds.domain</domain>\n" +
                     "                </service>\n" +
                     "            </services>";

        StAXOMBuilder builder = new StAXOMBuilder(new ByteArrayInputStream(xml.getBytes()));
        OMElement servicesEle = builder.getDocumentElement();
        EC2LoadBalancerConfiguration ec2LBConfig = new EC2LoadBalancerConfiguration();
        ec2LBConfig.createServicesConfig(servicesEle);

        EC2LoadBalancerConfiguration.ServiceConfiguration asServiceConfig = ec2LBConfig.getServiceConfig("wso2.as.domain");
        assertEquals(1, asServiceConfig.getInstancesPerScaleUp());
        assertEquals(3, asServiceConfig.getMaxAppInstances());
        assertEquals(1, asServiceConfig.getMinAppInstances());
        assertEquals(60000, asServiceConfig.getMessageExpiryTime());
        assertEquals(400, asServiceConfig.getQueueLengthPerNode());
        assertEquals(10, asServiceConfig.getRoundsToAverage());
        assertEquals("us-east-1x", asServiceConfig.getAvailabilityZone());
        assertEquals("m1.large", asServiceConfig.getEc2InstanceType());
        assertEquals(AutoscaleUtil.getUserData("src/test/resources/cluster_as.zip"),
                     asServiceConfig.getUserData());
        assertTrue(Arrays.equals(new String[]{"default"}, asServiceConfig.getSecurityGroups()));

        EC2LoadBalancerConfiguration.ServiceConfiguration dsServiceConfig = ec2LBConfig.getServiceConfig("wso2.ds.domain");
        assertEquals(1, dsServiceConfig.getInstancesPerScaleUp());
        assertEquals(3, dsServiceConfig.getMaxAppInstances());
        assertEquals(1, dsServiceConfig.getMinAppInstances());
        assertEquals(60000, dsServiceConfig.getMessageExpiryTime());
        assertEquals(400, dsServiceConfig.getQueueLengthPerNode());
        assertEquals(10, dsServiceConfig.getRoundsToAverage());
        assertEquals("us-east-1c", dsServiceConfig.getAvailabilityZone());
        assertEquals("m1.large", dsServiceConfig.getEc2InstanceType());
        assertEquals(AutoscaleUtil.getUserData("resources/cluster_node.zip"),
                     dsServiceConfig.getUserData());
        assertTrue(Arrays.equals(new String[]{"default"}, dsServiceConfig.getSecurityGroups()));
    }

    public void testServiceDomains() throws XMLStreamException {

        String xml = "<services xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                     "                <service>\n" +
                     "                    <hosts>\n" +
                     "                        <host>appserver.cloud-test.wso2.com</host>\n" +
                     "                        <host>as.cloud-test.wso2.com</host>\n" +
                     "                     </hosts>" +
                     "                    <domain>wso2.as.domain</domain>\n" +
                     "                    <property name=\"payload\" value=\"src/test/resources/cluster_as.zip\"/>\n" +
                     "                    <property name=\"availabilityZone\" value=\"us-east-1x\"/>\n" +
                     "                </service>\n" +
                     "                <service>\n" +
                     "                    <hosts>\n" +
                     "                        <host>ds.cloud-test.wso2.com</host>\n" +
                     "                        <host>data.cloud-test.wso2.com</host>\n" +
                     "                     </hosts>" +
                     "                    <domain>wso2.ds.domain</domain>\n" +
                     "                </service>\n" +
                     "            </services>";

        StAXOMBuilder builder = new StAXOMBuilder(new ByteArrayInputStream(xml.getBytes()));
        OMElement servicesEle = builder.getDocumentElement();
        EC2LoadBalancerConfiguration ec2LBConfig = new EC2LoadBalancerConfiguration();
        ec2LBConfig.createServicesConfig(servicesEle);

        String[] serviceDomains = ec2LBConfig.getServiceDomains();
        assertEquals(2, serviceDomains.length);
        assertTrue("wso2.as.domain".equals(serviceDomains[0]) || "wso2.as.domain".equals(serviceDomains[1]));
        assertTrue("wso2.ds.domain".equals(serviceDomains[0]) || "wso2.ds.domain".equals(serviceDomains[1]));
        assertEquals("wso2.as.domain", ec2LBConfig.getDomain("foo.appserver.cloud-test.wso2.com"));
        assertEquals("wso2.as.domain", ec2LBConfig.getDomain("bar.appserver.cloud-test.wso2.com"));
        assertEquals("wso2.as.domain", ec2LBConfig.getDomain("appserver.cloud-test.wso2.com"));
    }

    public void testDuplicateServiceHosts() throws XMLStreamException {

        String xml = "<services xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                     "                <service>\n" +
                     "                    <hosts>\n" +
                     "                        <host>appserver.cloud-test.wso2.com</host>\n" +
                     "                        <host>as.cloud-test.wso2.com</host>\n" +
                     "                     </hosts>" +
                     "                    <domain>wso2.as.domain</domain>\n" +
                     "                    <property name=\"payload\" value=\"src/test/resources/cluster_as.zip\"/>\n" +
                     "                    <property name=\"availabilityZone\" value=\"us-east-1x\"/>\n" +
                     "                </service>\n" +
                     "                <service>\n" +
                     "                    <hosts>\n" +
                     "                        <host>as.cloud-test.wso2.com</host>\n" +
                     "                        <host>data.cloud-test.wso2.com</host>\n" +
                     "                     </hosts>" +
                     "                    <domain>wso2.ds.domain</domain>\n" +
                     "                </service>\n" +
                     "            </services>";

        StAXOMBuilder builder = new StAXOMBuilder(new ByteArrayInputStream(xml.getBytes()));
        OMElement servicesEle = builder.getDocumentElement();
        EC2LoadBalancerConfiguration ec2LBConfig = new EC2LoadBalancerConfiguration();
        try {
            ec2LBConfig.createServicesConfig(servicesEle);
            fail("Expected exception due to duplicate host was not thrown");
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testFullConfig() throws XMLStreamException {
        String lbXml = "<loadBalancer>\n" +
                       "                <property name=\"securityGroups\" value=\"stratos-lb\"/>\n" +
                       "                <property name=\"instanceType\" value=\"m1.large\"/>\n" +
                       "                <property name=\"instances\" value=\"11\"/>\n" +
                       "                <property name=\"elasticIP\" value=\"11.11.23.45\"/>\n" +
                       "                <property name=\"availabilityZone\" value=\"us-east-1z\"/>\n" +
                       "                <property name=\"payload\" value=\"src/test/resources/lb.zip\"/>\n" +
                       "            </loadBalancer>";

        String servicesXml = "<services xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                             "                <defaults>\n" +
                             "                    <property name=\"payload\" value=\"src/test/resources/default.zip\"/>\n" +
                             "                    <property name=\"securityGroups\" value=\"strat-default\"/>\n" +
                             "                    <property name=\"availabilityZone\" value=\"us-east-1c\"/>\n" +
                             "                    <property name=\"securityGroups\" value=\"xxx-group\"/>\n" +
                             "                    <property name=\"instanceType\" value=\"m1.xlarge\"/>\n" +
                             "                    <property name=\"minAppInstances\" value=\"10\"/>\n" +
                             "                    <property name=\"maxAppInstances\" value=\"15\"/>\n" +
                             "                    <property name=\"queueLengthPerNode\" value=\"431\"/>\n" +
                             "                    <property name=\"roundsToAverage\" value=\"11\"/>\n" +
                             "                    <property name=\"instancesPerScaleUp\" value=\"3\"/>\n" +
                             "                    <property name=\"messageExpiryTime\" value=\"61000\"/>\n" +
                             "                </defaults>\n" +
                             "                <service>\n" +
                             "                    <hosts>\n" +
                             "                        <host>appserver.cloud-test.wso2.com</host>\n" +
                             "                        <host>as.cloud-test.wso2.com</host>\n" +
                             "                     </hosts>" +
                             "                    <domain>wso2.as.domain</domain>\n" +
                             "                    <property name=\"payload\" value=\"src/test/resources/cluster_as.zip\"/>\n" +
                             "                    <property name=\"availabilityZone\" value=\"us-east-1x\"/>\n" +
                             "                </service>\n" +
                             "                <service>\n" +
                             "                    <hosts>\n" +
                             "                        <host>governace.cloud-test.wso2.com</host>\n" +
                             "                        <host>greg.cloud-test.wso2.com</host>\n" +
                             "                     </hosts>" +
                             "                    <domain>wso2.ds.domain</domain>\n" +
                             "                </service>\n" +
                             "            </services>";

        StAXOMBuilder builder = new StAXOMBuilder(new ByteArrayInputStream(lbXml.getBytes()));
        OMElement loadBalancerEle = builder.getDocumentElement();
        EC2LoadBalancerConfiguration ec2LBConfig = new EC2LoadBalancerConfiguration();
        ec2LBConfig.createLoadBalancerConfig(loadBalancerEle);
        EC2LoadBalancerConfiguration.LBConfiguration loadBalancerConfig = ec2LBConfig.getLoadBalancerConfig();
        StAXOMBuilder builder2 = new StAXOMBuilder(new ByteArrayInputStream(servicesXml.getBytes()));
        OMElement servicesEle = builder2.getDocumentElement();
        ec2LBConfig.createServicesConfig(servicesEle);

        assertTrue(Arrays.equals(new String[]{"stratos-lb"}, loadBalancerConfig.getSecurityGroups()));
        assertEquals("m1.large", loadBalancerConfig.getEc2InstanceType());
        assertEquals(11, loadBalancerConfig.getInstances());
        assertEquals("11.11.23.45", loadBalancerConfig.getElasticIP());
        assertEquals(AutoscaleUtil.getUserData("src/test/resources/lb.zip"),
                     loadBalancerConfig.getUserData());
        assertEquals("us-east-1z", loadBalancerConfig.getAvailabilityZone());
    }
}
