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

import com.amazonaws.services.ec2.model.GroupIdentifier;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * AutoscaleUtilTest
 */
//public class AutoscaleUtilTest extends TestCase {
//
//    private static List<GroupIdentifier> groups = new ArrayList<GroupIdentifier>();
//
//    static {
//        groups.add(new GroupIdentifier().withGroupName("a"));
//        groups.add(new GroupIdentifier().withGroupName("b"));
//        groups.add(new GroupIdentifier().withGroupName("xxx"));
//    }
//
//    public void testContainGroupId() {
//        assertTrue(AutoscaleUtil.areEqual(groups, new String[]{"xxx"}));
//    }
//
//    public void testContainGroupId2() {
//        assertFalse(AutoscaleUtil.areEqual(groups, new String[]{"x"}));
//    }
//}
