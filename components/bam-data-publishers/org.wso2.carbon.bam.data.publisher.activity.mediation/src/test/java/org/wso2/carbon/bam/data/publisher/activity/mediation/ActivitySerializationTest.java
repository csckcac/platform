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

import javax.xml.namespace.QName;

public class ActivitySerializationTest extends AbstractActivityTest {

    public void testBasicSerialization() {
        MessageActivity activity = new MessageActivity();
        activity.setActivityId("urn:uuid:1234567890");
        activity.setActivityName("TestActivity");
        activity.setService(SERVICE);
        activity.setOperation(OPERATION);
        activity.setMessageId("urn:uuid:0987654321");
        activity.setPayload(PAYLOAD);
        activity.setDirection(ActivityPublisherConstants.DIRECTION_IN);
        activity.setTimestamp(BAMCalendar.getInstance());

        initSerialization();
        OMElement om = ActivityPublisherUtils.serialize(activity);
        System.out.println(om);

        assertEquals("urn:uuid:1234567890", om.getFirstChildWithName(
                new QName(ActivityPublisherConstants.ACTIVITY_DATA_NS_URI,
                        ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_ACTIVITY_ID)).getText());
        assertEquals("TestActivity", om.getFirstChildWithName(
                new QName(ActivityPublisherConstants.ACTIVITY_DATA_NS_URI,
                        ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_ACTIVITY_NAME)).getText());
        assertEquals("urn:uuid:0987654321", om.getFirstChildWithName(
                new QName(ActivityPublisherConstants.ACTIVITY_DATA_NS_URI,
                        ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_MESSAGE_ID)).getText());
        assertEquals(SERVICE, om.getFirstChildWithName(
                new QName(ActivityPublisherConstants.ACTIVITY_DATA_NS_URI,
                        ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_SERVICE_NAME)).getText());
        assertEquals(OPERATION, om.getFirstChildWithName(
                new QName(ActivityPublisherConstants.ACTIVITY_DATA_NS_URI,
                        ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_OPERATION_NAME)).getText());
        assertEquals(PAYLOAD, om.getFirstChildWithName(
                new QName(ActivityPublisherConstants.ACTIVITY_DATA_NS_URI,
                        ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_MESSAGE_BODY)).getText());
        assertEquals("Request", om.getFirstChildWithName(
                new QName(ActivityPublisherConstants.ACTIVITY_DATA_NS_URI,
                        ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT_MESSAGE_DIRECTION)).getText());
    }
}