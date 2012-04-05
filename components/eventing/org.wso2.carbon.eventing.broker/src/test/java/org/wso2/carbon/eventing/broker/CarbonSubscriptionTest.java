/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.eventing.broker;

import junit.framework.TestCase;
import org.wso2.carbon.eventing.broker.builders.CommandBuilderConstants;

public class CarbonSubscriptionTest extends TestCase {

    protected final String DELIVERY_MODE = "http://tempuri.org/2009/11/eventing/DeliveryModes/Test";

    public void testConstruction() throws Exception {
        CarbonSubscription subscription = new CarbonSubscription();

        assertNotNull("The subscription object is null", subscription);
        assertNotNull("The subscription id is null", subscription.getId());
        assertNotNull("The delivery mode is null", subscription.getDeliveryMode());

        assertEquals("Invalid default delivery mode",
                CommandBuilderConstants.WSE_DEFAULT_DELIVERY_MODE, subscription.getDeliveryMode());

        assertNotNull("No subscription data found", subscription.getSubscriptionData());
    }

    public void testConstructionWithDeliveryMode() throws Exception {
        CarbonSubscription subscription = new CarbonSubscription(DELIVERY_MODE);

        assertNotNull("The subscription object is null", subscription);
        assertNotNull("The subscription id is null", subscription.getId());
        assertNotNull("The delivery mode is null", subscription.getDeliveryMode());

        assertFalse("Invalid delivery mode",
                CommandBuilderConstants.WSE_DEFAULT_DELIVERY_MODE.equals(
                        subscription.getDeliveryMode()));
        assertTrue("Invalid delivery mode",
                DELIVERY_MODE.equals(subscription.getDeliveryMode()));

        assertNotNull("No subscription data found", subscription.getSubscriptionData());
    }
}
