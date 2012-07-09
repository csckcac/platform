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
package org.wso2.carbon.autoscaler.service.util;

import java.util.ArrayList;
import java.util.Arrays;

import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeMetadataBuilder;
import org.wso2.carbon.autoscaler.service.impl.AutoscalerServiceImpl.Iaases;

import junit.framework.TestCase;

public class IaasContextTest extends TestCase {

    IaasContext ctx;
    NodeMetadata node1, node2, node3, node4;

    public IaasContextTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();

        node1 = new NodeMetadataBuilder().id("1")
                                         .status(org.jclouds.compute.domain.NodeMetadata.Status.RUNNING)
                                         .publicAddresses(new ArrayList<String>(Arrays.asList("192.168.1.2")))
                                         .build();
        node2 = new NodeMetadataBuilder().id("2")
                                         .status(org.jclouds.compute.domain.NodeMetadata.Status.RUNNING)
                                         .build();
        node3 = new NodeMetadataBuilder().id("3")
                                         .status(org.jclouds.compute.domain.NodeMetadata.Status.RUNNING)
                                         .build();
        node4 = new NodeMetadataBuilder().id("4")
                                         .status(org.jclouds.compute.domain.NodeMetadata.Status.RUNNING)
                                         .build();

        ctx = new IaasContext(Iaases.ec2, null);
        ctx.addNodeIdToDomainMap(node1.getId(), "wso2.a");
        ctx.addPublicIpToDomainMap("192.168.1.2", "wso2.a");
        ctx.addPublicIpToNodeIdMap("192.168.1.2", node1.getId());
        ctx.addNodeIdToDomainMap(node2.getId(), "wso2.b");
        ctx.addNodeIdToDomainMap(node3.getId(), "wso2.a");
        ctx.addPublicIpToDomainMap("192.168.1.3", "wso2.a");
        ctx.addPublicIpToNodeIdMap("192.168.1.3", node3.getId());
        ctx.addNodeIdToDomainMap(node4.getId(), "wso2.c");
    }

    public final void testGetLastMatchingNode() {

        assertEquals(node3.getId(), ctx.getLastMatchingNode("wso2.a"));
        ctx.removeNodeId(node3.getId());
        assertEquals(node1.getId(), ctx.getLastMatchingNode("wso2.a"));
        ctx.addNodeIdToDomainMap(node3.getId(), "wso2.a");
    }

    public final void testGetFirstMatchingNode() {
        assertEquals(node1.getId(), ctx.getFirstMatchingNode("wso2.a"));
    }
    
    public final void testGetFirstMatchingPublicIp() {
        assertEquals("192.168.1.3", ctx.getLastMatchingPublicIp("wso2.a"));
    }

    public final void testGetNodeWithPublicIp() {
        assertEquals(node1.getId(), ctx.getNodeWithPublicIp("192.168.1.2"));
    }

    public final void testGetNodeIds() {
        assertEquals(new ArrayList<String>(Arrays.asList("1", "3")), ctx.getNodeIds("wso2.a"));
        assertEquals(new ArrayList<String>(Arrays.asList("2")), ctx.getNodeIds("wso2.b"));
    }

}
