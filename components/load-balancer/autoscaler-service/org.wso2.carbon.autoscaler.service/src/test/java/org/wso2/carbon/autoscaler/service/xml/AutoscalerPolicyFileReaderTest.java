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
package org.wso2.carbon.autoscaler.service.xml;

import java.io.File;

import org.wso2.carbon.autoscaler.service.util.Policy;

import junit.framework.Assert;
import junit.framework.TestCase;

public class AutoscalerPolicyFileReaderTest extends TestCase {

   

    public void testGetPolicy() throws Exception {
        
        String file = "conf/autoscaler-policy.xml";
        AutoscalerPolicyFileReader reader = new AutoscalerPolicyFileReader(file);
        
        Policy policy = reader.getPolicy();
        
        assertEquals("jvm", policy.getScaleUpOrderList().get(0));
        assertEquals("ec2", policy.getScaleUpOrderList().get(1));
        
        assertEquals("jvm", policy.getScaleDownOrderList().get(0));
        assertEquals("ec2", policy.getScaleDownOrderList().get(1));
        
        assertEquals(1, (int)policy.getScaleDownOrderIdToMinInstanceCountMap().get(0));
        assertEquals(0, (int)policy.getScaleDownOrderIdToMinInstanceCountMap().get(1));
        
        
    }

}
