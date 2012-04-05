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

package org.wso2.carbon.broker.core.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.broker.core.*;
import org.wso2.carbon.broker.core.exception.BrokerEventProcessingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * broker service implementation.
 */
public class CarbonBrokerService implements BrokerService {

    private Map<String, BrokerType> brokerTypeMap;

    public CarbonBrokerService() {
        this.brokerTypeMap = new ConcurrentHashMap();
    }

    public void registerBrokerType(BrokerType brokerType) {
        BrokerTypeDto brokerTypeTypeDto = brokerType.getBrokerTypeDto();
        this.brokerTypeMap.put(brokerTypeTypeDto.getName(), brokerType);
    }

    public List<BrokerTypeDto> getBrokerTypes() {
        List<BrokerTypeDto> brokerTypeDtos = new ArrayList<BrokerTypeDto>();
        for (BrokerType brokerType : this.brokerTypeMap.values()) {
            brokerTypeDtos.add(brokerType.getBrokerTypeDto());
        }
        return brokerTypeDtos;
    }

    public List<String> getBrokerTypeNames() {
        List<String> brokerTypeNames = new ArrayList<String>();
        for (BrokerType brokerType : this.brokerTypeMap.values()) {
            brokerTypeNames.add(brokerType.getBrokerTypeDto().getName());
        }
        return brokerTypeNames;
    }

    public List<Property> getBrokerProperties(String brokerType) {
        return brokerTypeMap.get(brokerType).getBrokerTypeDto().getPropertyList();
    }

    public void subscribe(BrokerConfiguration brokerConfiguration,
                          String topicName,
                          BrokerListener brokerListener,
                          AxisConfiguration axisConfiguration) throws BrokerEventProcessingException {
        BrokerType brokerType = this.brokerTypeMap.get(brokerConfiguration.getType());
        brokerType.subscribe(topicName, brokerListener, brokerConfiguration, axisConfiguration);
    }

    public void publish(BrokerConfiguration brokerConfiguration,
                        String topicName, Object object) throws BrokerEventProcessingException {

        BrokerType brokerType = this.brokerTypeMap.get(brokerConfiguration.getType());
        brokerType.publish(topicName, object, brokerConfiguration);
    }

    public void unsubscribe(String topicName,
                            BrokerConfiguration brokerConfiguration,
                            AxisConfiguration axisConfiguration) throws BrokerEventProcessingException {
        BrokerType brokerType = this.brokerTypeMap.get(brokerConfiguration.getType());
        brokerType.unsubscribe(topicName, brokerConfiguration, axisConfiguration);
    }
}
