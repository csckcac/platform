/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.broker.core.internal.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.broker.core.BrokerConfiguration;
import org.wso2.carbon.broker.core.BrokerListener;
import org.wso2.carbon.broker.core.internal.brokers.ws.SubscriptionMessageReceiver;

import javax.xml.namespace.QName;

public class Axis2Util {

    public static AxisService registerAxis2Service(String topicName,
                                                   BrokerListener brokerListener,
                                                   BrokerConfiguration brokerConfiguration,
                                                   AxisConfiguration axisConfiguration)
            throws AxisFault {
        //first create an Axis2 service to receive the messages to this broker
        //operation name can not have
        topicName = topicName.replaceAll("/","");
        String axisServiceName = brokerConfiguration.getName() + "Service";
        AxisService axisService = axisConfiguration.getService(axisServiceName);
        if (axisService == null) {
            // create a new axis service
            axisService = new AxisService(axisServiceName);

            axisConfiguration.addService(axisService);
            axisService.getAxisServiceGroup().addParameter(CarbonConstants.DYNAMIC_SERVICE_PARAM_NAME, "true");
        }

        AxisOperation axisOperation = axisService.getOperation(new QName("", topicName));
        if (axisOperation == null) {
            axisOperation = new InOnlyAxisOperation(new QName("", topicName));
            axisOperation.setMessageReceiver(new SubscriptionMessageReceiver());
            axisOperation.setSoapAction("urn:" + topicName);


            axisConfiguration.getPhasesInfo().setOperationPhases(axisOperation);
            axisService.addOperation(axisOperation);
        }

        SubscriptionMessageReceiver messageReceiver =
                (SubscriptionMessageReceiver) axisOperation.getMessageReceiver();
        messageReceiver.addBrokerListener(brokerListener);
        return axisService;
    }

    /**
     * removes the operation from the Axis service. 
     * @param topicName
     * @param brokerConfiguration
     * @param axisConfiguration
     * @throws AxisFault
     */
    public static void removeOperation(String topicName,
                                       BrokerConfiguration brokerConfiguration,
                                       AxisConfiguration axisConfiguration) throws AxisFault {

        topicName = topicName.replaceAll("/", "");
        String axisServiceName = brokerConfiguration.getName() + "Service";
        AxisService axisService = axisConfiguration.getService(axisServiceName);

        if (axisService == null){
            throw new AxisFault("There is not service with the name ==> " + axisServiceName);
        }

        axisService.removeOperation(new QName("", topicName));
    }


}
