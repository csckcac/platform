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
package org.wso2.bam.integration.test.datacollection.mediation.mockobjects;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBroker;
import org.wso2.carbon.event.core.Message;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class MockLwEventBroker extends LightWeightEventBroker {

    private static final Log log = LogFactory.getLog(MockLwEventBroker.class);

    private static final String SERVER_USER_DEFINED_DATA_SERVICE =
            "https://localhost:9443/services/BAMServerUserDefinedDataSubscriberService";


    public MockLwEventBroker() throws AxisFault {
        super(LightWeightEventBroker.class.getPackage().getName());
    }

    public void publish(String topicName, OMElement event) throws AxisFault, RegistryException {
        if (event != null) {
            ServiceClient serviceClient = new ServiceClient();
            Options options = new Options();
            options.setTo(new EndpointReference(SERVER_USER_DEFINED_DATA_SERVICE));
            options.setAction("Publish");
            serviceClient.setOptions(options);

            serviceClient.fireAndForget(event);
            log.info("Fired mediation statistics event to BAM server..");
        }
    }

//    @Override
//    public void publish(Message message, String s, int i) throws EventBrokerException {
//
//    }
//
//    public void publishRobust(Message message, String s) throws EventBrokerException {
//
//    }
//
//    @Override
//    public void publishRobust(Message message, String s, int i) throws EventBrokerException {
//
//    }
//
//    public String subscribe(Subscription subscription) throws EventBrokerException {
//        return null;
//    }
//
//    public void unsubscribe(String s) throws EventBrokerException {
//
//    }
//
//    public Subscription getSubscription(String s) throws EventBrokerException {
//        return null;
//    }
//
//    public void renewSubscription(Subscription subscription) throws EventBrokerException {
//
//    }
//
//    public List<Subscription> getAllSubscriptions(String s) throws EventBrokerException {
//        return null;
//    }
//
//    public void registerEventDispatcher(String s, EventDispatcher eventDispatcher) {
//
//    }
//
//    public TopicManager getTopicManager() throws EventBrokerException {
//        return null;
//    }
//
//    @Override
//    public void initializeTenant() throws EventBrokerException {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    public void loadExistingSubscriptions() throws EventBrokerConfigurationException {
//
//    }

}
