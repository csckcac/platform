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
package org.wso2.carbon.lb.common.test;

import java.io.File;
import org.wso2.carbon.lb.common.conf.LoadBalancerConfiguration;
import junit.framework.TestCase;

public class LoadBalancerConfigurationTest extends TestCase {
    
    private LoadBalancerConfiguration lbConfig;
    
    @Override
    protected void setUp() throws Exception {
        lbConfig = new LoadBalancerConfiguration();

        File f = new File("src/test/resources/loadbalancer.conf");
        lbConfig.init(f.getAbsolutePath());
    }

    public final void testCreateLoadBalancerConfig() {

        LoadBalancerConfiguration.LBConfiguration loadBalancerConfig =
            lbConfig.getLoadBalancerConfig();
        
        assertEquals(1, loadBalancerConfig.getInstances());
        assertEquals(5000, loadBalancerConfig.getAutoscalerTaskInterval());
    }

    public final void testCreateServicesConfig() {

        LoadBalancerConfiguration.ServiceConfiguration asServiceConfig =
            lbConfig.getServiceConfig("wso2.as1.domain");
        
        assertEquals(1, asServiceConfig.getInstancesPerScaleUp());
        assertEquals(5, asServiceConfig.getMaxAppInstances());
        assertEquals(1, asServiceConfig.getMinAppInstances());
        assertEquals(60000, asServiceConfig.getMessageExpiryTime());
        assertEquals(400, asServiceConfig.getQueueLengthPerNode());
        assertEquals(10, asServiceConfig.getRoundsToAverage());

    }

    public final void testGetServiceDomains() {

        String[] serviceDomains = lbConfig.getServiceDomains();
        assertEquals(3, serviceDomains.length);
        
        assertTrue("wso2.as1.domain".equals(serviceDomains[0]) ||
            "wso2.as1.domain".equals(serviceDomains[1]) ||
            "wso2.as1.domain".equals(serviceDomains[2]));
        assertTrue("wso2.as2.domain".equals(serviceDomains[0]) ||
            "wso2.as2.domain".equals(serviceDomains[1]) ||
            "wso2.as2.domain".equals(serviceDomains[2]));
        assertTrue("wso2.as3.domain".equals(serviceDomains[0]) ||
            "wso2.as3.domain".equals(serviceDomains[1]) ||
            "wso2.as3.domain".equals(serviceDomains[2]));
        
        assertEquals("wso2.as3.domain", lbConfig.getDomain("appserver.cloud-test.wso2.com", 300));
        assertEquals("wso2.as3.domain", lbConfig.getDomain("as.cloud-test.wso2.com", 300));
        assertEquals("wso2.as1.domain", lbConfig.getDomain("as.cloud-test.wso2.com", 1));
        assertEquals("wso2.as2.domain", lbConfig.getDomain("as.cloud-test.wso2.com", 200));
    }

}
