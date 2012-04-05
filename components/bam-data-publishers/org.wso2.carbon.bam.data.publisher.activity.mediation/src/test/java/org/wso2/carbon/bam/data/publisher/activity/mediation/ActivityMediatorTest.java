/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.bam.data.publisher.activity.mediation;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.bam.data.publisher.activity.mediation.services.ActivityPublisherAdmin;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActivityMediatorTest extends AbstractActivityTest {

    public void testBasicMediation() {
        MessageContext synCtx = getTestContext();
        TestActivityProcessor activityProcessor = new TestActivityProcessor();

        // init queue
        ActivityQueue queue = new ActivityQueue(activityProcessor);
        ActivityPublisherUtils.setActivityQueue(queue);

        ActivityPublisherAdmin admin = new ActivityPublisherAdmin();
        ActivityPublisherUtils.setActivityPublisherAdmin(admin);

        Mediator mediator = new MessageActivityMediator();
        mediator.mediate(synCtx);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {

        }

        List<MessageActivity> list = activityProcessor.getActivityList();
        assertEquals(1, list.size());
        MessageActivity activity = list.get(0);
        assertEquals(ACTIVITY_ID, activity.getActivityId());
        assertEquals(SERVICE, activity.getService());
        assertEquals(OPERATION, activity.getOperation());

/*        try {
            OMElement payload = AXIOMUtil.stringToOM(activity.getPayload());
            assertEquals(PAYLOAD, payload.getFirstOMChild().toString());
        } catch (XMLStreamException e) {
            fail("Error while converting payload to OM");
        }*/

        assertNotNull(activity.getMessageId());
        assertNotNull(activity.getTimestamp());
        //assertNotNull(activity.getPayload());
        assertEquals(ActivityPublisherConstants.DIRECTION_IN, activity.getDirection());

        assertEquals(3, activity.getPropertyKeys().size());
        assertEquals("", activity.getProperty(ActivityPublisherConstants.PROP_APPLICATION_FAILURE));
        assertEquals("", activity.getProperty(ActivityPublisherConstants.PROP_TECHNICAL_FAILURE));
        assertEquals("", activity.getProperty(ActivityPublisherConstants.PROP_ARC_KEY));

        assertNull(activity.getActivityName());
        assertNull(activity.getDescription());
        assertNull(activity.getUserAgent());
        assertNull(activity.getSenderHost());
    }

    private static class TestActivityProcessor implements ActivityProcessor {

        private List<MessageActivity> activityList = new ArrayList<MessageActivity>();

        public void process(MessageActivity[] activities) {
            activityList.addAll(Arrays.asList(activities));
        }

        public void destroy() {

        }

        public List<MessageActivity> getActivityList() {
            return activityList;
        }

    }
}