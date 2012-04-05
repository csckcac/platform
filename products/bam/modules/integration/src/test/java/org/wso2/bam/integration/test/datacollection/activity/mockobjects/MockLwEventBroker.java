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
package org.wso2.bam.integration.test.datacollection.activity.mockobjects;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBroker;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.event.core.Message;
import org.wso2.carbon.event.core.exception.EventBrokerConfigurationException;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.subscription.EventDispatcher;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.core.topic.TopicManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.List;

public class MockLwEventBroker extends LightWeightEventBroker {

    private static final Log log = LogFactory.getLog(MockLwEventBroker.class);

    private static final String ACTIVITY_DATA_SERVICE =
            "https://localhost:9443/services/BAMActivityDataStatisticsSubscriberService";

    public MockLwEventBroker() throws AxisFault {
        super(LightWeightEventBroker.class.getPackage().getName());
    }


    public void publish(String topicName, OMElement event) throws AxisFault, RegistryException {
        if (event != null) {
            ServiceClient serviceClient = new ServiceClient();
            Options options = new Options();
            options.setTo(new EndpointReference(ACTIVITY_DATA_SERVICE));
            options.setAction("Publish");
            serviceClient.setOptions(options);
            serviceClient.fireAndForget(event);
            serviceClient.cleanupTransport();
            log.info("Fired activity statistics event to BAM server..");
        }
    }
}

