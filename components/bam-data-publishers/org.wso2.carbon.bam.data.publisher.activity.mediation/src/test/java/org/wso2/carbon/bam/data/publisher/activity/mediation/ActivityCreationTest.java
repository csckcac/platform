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
import org.apache.synapse.MessageContext;
import org.wso2.carbon.bam.data.publisher.activity.mediation.config.EventingConfigData;
import org.wso2.carbon.bam.data.publisher.activity.mediation.services.ActivityPublisherAdmin;

import javax.xml.stream.XMLStreamException;


public class ActivityCreationTest extends AbstractActivityTest {

    public void testActivityCreation1() {
        MessageContext synCtx = getTestContext();

        ActivityPublisherAdmin admin = new ActivityPublisherAdmin();
        ActivityPublisherUtils.setActivityPublisherAdmin(admin);

        MessageActivity activity = ActivityPublisherUtils.newActivity(synCtx, true);
        activity.setPayload(synCtx.getEnvelope().getBody().toString());

        assertEquals(ACTIVITY_ID, activity.getActivityId());
        assertEquals(SERVICE, activity.getService());
        assertEquals(OPERATION, activity.getOperation());

        try {
            OMElement payload = AXIOMUtil.stringToOM(activity.getPayload());
            assertEquals(PAYLOAD, payload.getFirstOMChild().toString());
        } catch (XMLStreamException e) {
            fail("Error while converting payload to OM");
        }

        assertNotNull(activity.getMessageId());
        assertNotNull(activity.getTimestamp());
        assertNotNull(activity.getPayload());
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

    public void testActivityCreation2() {
        MessageContext synCtx = getTestContext();
        synCtx.setProperty(ActivityPublisherConstants.PROP_TECHNICAL_FAILURE, "true");
        synCtx.setProperty(ActivityPublisherConstants.PROP_TECHNICAL_FAILURE_DETAIL,
                           "Connection refused or failed for the test.wso2.com");
        synCtx.setProperty(ActivityPublisherConstants.PROP_REMOTE_ADDRESS, "10.8.0.1");
        synCtx.setProperty(ActivityPublisherConstants.PROP_MESSAGE_FORMAT, "soap");

        ActivityPublisherAdmin admin = new ActivityPublisherAdmin();
        ActivityPublisherUtils.setActivityPublisherAdmin(admin);

        MessageActivity activity = ActivityPublisherUtils.newActivity(synCtx, false);
        activity.setPayload(synCtx.getEnvelope().getBody().toString());

        assertEquals(ACTIVITY_ID, activity.getActivityId());
        assertEquals(SERVICE, activity.getService());
        assertEquals(OPERATION, activity.getOperation());
        try {
            OMElement payload = AXIOMUtil.stringToOM(activity.getPayload());
            assertEquals(PAYLOAD, payload.getFirstOMChild().toString());
        } catch (XMLStreamException e) {
            fail("Error while converting payload to OM");
        }

        assertNotNull(activity.getMessageId());
        assertNotNull(activity.getTimestamp());
        assertNotNull(activity.getPayload());
        assertEquals(ActivityPublisherConstants.DIRECTION_OUT, activity.getDirection());

        assertEquals(4, activity.getPropertyKeys().size());
        assertEquals("", activity.getProperty(ActivityPublisherConstants.PROP_APPLICATION_FAILURE));
        assertEquals("Connection refused or failed for the test.wso2.com",
                     activity.getProperty(ActivityPublisherConstants.PROP_TECHNICAL_FAILURE));
        assertEquals("", activity.getProperty(ActivityPublisherConstants.PROP_ARC_KEY));
        assertEquals("soap", activity.getProperty(ActivityPublisherConstants.PROP_MESSAGE_FORMAT));

        assertNull(activity.getActivityName());
        assertNull(activity.getDescription());
        assertNull(activity.getUserAgent());

        assertEquals("10.8.0.1", activity.getSenderHost());

        initSerialization();
        System.out.println(ActivityPublisherUtils.serialize(activity));
    }

    public void testActivityCreation3() {
        MessageContext synCtx = getTestContext();
        synCtx.setProperty(ActivityPublisherConstants.PROP_APPLICATION_FAILURE, "true");
        synCtx.setProperty(ActivityPublisherConstants.PROP_APPLICATION_FAILURE_DETAIL,
                           "NumberFormatException in the quantity field");
        synCtx.setProperty(ActivityPublisherConstants.PROP_REMOTE_ADDRESS, "10.8.0.1");
        synCtx.setProperty(ActivityPublisherConstants.PROP_MESSAGE_FORMAT, "IDOC");

        ActivityPublisherAdmin admin = new ActivityPublisherAdmin();    
        ActivityPublisherUtils.setActivityPublisherAdmin(admin);

        MessageActivity activity = ActivityPublisherUtils.newActivity(synCtx, false);
        activity.setPayload(synCtx.getEnvelope().getBody().toString());

        assertEquals(ACTIVITY_ID, activity.getActivityId());
        assertEquals(SERVICE, activity.getService());
        assertEquals(OPERATION, activity.getOperation());
        try {
            OMElement payload = AXIOMUtil.stringToOM(activity.getPayload());
            assertEquals(PAYLOAD, payload.getFirstOMChild().toString());
        } catch (XMLStreamException e) {
            fail("Error while converting payload to OM");
        }

        assertNotNull(activity.getMessageId());
        assertNotNull(activity.getTimestamp());
        assertNotNull(activity.getPayload());
        assertEquals(ActivityPublisherConstants.DIRECTION_OUT, activity.getDirection());

        assertEquals(4, activity.getPropertyKeys().size());
        assertEquals("", activity.getProperty(ActivityPublisherConstants.PROP_TECHNICAL_FAILURE));
        assertEquals("NumberFormatException in the quantity field",
                     activity.getProperty(ActivityPublisherConstants.PROP_APPLICATION_FAILURE));
        assertEquals("", activity.getProperty(ActivityPublisherConstants.PROP_ARC_KEY));
        assertEquals("IDOC", activity.getProperty(ActivityPublisherConstants.PROP_MESSAGE_FORMAT));

        assertNull(activity.getActivityName());
        assertNull(activity.getDescription());
        assertNull(activity.getUserAgent());

        assertEquals("10.8.0.1", activity.getSenderHost());

        initSerialization();
        System.out.println(ActivityPublisherUtils.serialize(activity));
    }

}