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
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

public class IaaSProviderComparatorTest extends TestCase {
    
    List<IaaSProvider> iaasProviders = new ArrayList<IaaSProvider>();
    
    @Override
    protected void setUp() throws Exception {
        IaaSProvider a = new IaaSProvider();
        a.setName("ec2");
        a.setScaleUpOrder(1);
        a.setScaleDownOrder(5);
        
        IaaSProvider b = new IaaSProvider();
        b.setName("lxc");
        b.setScaleUpOrder(3);
        b.setScaleDownOrder(0);
        
        iaasProviders.add(a);
        iaasProviders.add(b);
        
        super.setUp();
    }

    public void testSort() {
        
        // scale up order sort test
        Collections.sort(iaasProviders,
                         IaaSProviderComparator.ascending(
                         IaaSProviderComparator.getComparator(
                         IaaSProviderComparator.SCALE_UP_SORT)));
        
        assertEquals("ec2", iaasProviders.get(0).getName());
        assertEquals("lxc", iaasProviders.get(1).getName());
        
        // scale down order sort test
        Collections.sort(iaasProviders,
                         IaaSProviderComparator.ascending(
                         IaaSProviderComparator.getComparator(
                         IaaSProviderComparator.SCALE_DOWN_SORT)));
        
        assertEquals("lxc", iaasProviders.get(0).getName());
        assertEquals("ec2", iaasProviders.get(1).getName());
        
        IaaSProvider c = new IaaSProvider();
        c.setName("vm");
        c.setScaleUpOrder(0);
        c.setScaleDownOrder(4);
        
        iaasProviders.add(c);
        
        // scale up order sort test
        Collections.sort(iaasProviders,
                         IaaSProviderComparator.ascending(
                         IaaSProviderComparator.getComparator(
                         IaaSProviderComparator.SCALE_UP_SORT)));
        
        assertEquals("vm", iaasProviders.get(0).getName());
        assertEquals("ec2", iaasProviders.get(1).getName());
        
        // scale down order sort test
        Collections.sort(iaasProviders,
                         IaaSProviderComparator.ascending(
                         IaaSProviderComparator.getComparator(
                         IaaSProviderComparator.SCALE_DOWN_SORT)));
        
        assertEquals("lxc", iaasProviders.get(0).getName());
        assertEquals("vm", iaasProviders.get(1).getName());
        
        
    }

}
