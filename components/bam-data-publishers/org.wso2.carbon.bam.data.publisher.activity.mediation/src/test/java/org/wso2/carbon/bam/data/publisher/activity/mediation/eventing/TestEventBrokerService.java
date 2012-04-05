/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.data.publisher.activity.mediation.eventing;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.wso2.carbon.bam.data.publisher.activity.mediation.ActivityPublisherConstants;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.event.core.Message;
import org.wso2.carbon.event.core.exception.EventBrokerConfigurationException;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.topic.TopicManager;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBroker;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;

public class TestEventBrokerService extends LightWeightEventBroker {

    private int eventCount = 0;
    private int activityCount = 0;

    public TestEventBrokerService(String packageName) throws AxisFault {
        super(packageName);
    }

    public String subscribe(org.wso2.carbon.event.core.subscription.Subscription subscription) {
        return null;
    }

    public void unsubscribe(org.wso2.carbon.event.core.subscription.Subscription subscription) {

    }

    public void publish(String topicName, OMElement event) {
        eventCount++;
        //System.out.println(msgCtx.getEnvelope());
        Iterator iter = event.getChildrenWithName(
                new QName(ActivityPublisherConstants.ACTIVITY_DATA_NS_URI,
                          ActivityPublisherConstants.ACTIVITY_DATA_ELEMENT));
        int localActivityCount = 0;
        while (iter.hasNext()) {
            iter.next();
            activityCount++;
            localActivityCount++;
        }
        System.out.println("Received a burst of " + localActivityCount + " activities");

        String threshold = System.getProperty("bam.activity.threshold");
        if (threshold != null && Integer.parseInt(threshold) > localActivityCount) {
            System.err.println("Runtime error: Threshold is greater than the local " +
                               "activity count - " + threshold + " > " + localActivityCount);
        }

    }

    public int getTotalEventCount() {
        return eventCount;
    }

    public int getActivityCount() {
        return activityCount;
    }
}