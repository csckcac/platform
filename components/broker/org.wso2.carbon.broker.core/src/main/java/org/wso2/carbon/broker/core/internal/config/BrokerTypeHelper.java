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

package org.wso2.carbon.broker.core.internal.config;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.broker.core.Property;
import org.wso2.carbon.broker.core.BrokerTypeDto;
import org.wso2.carbon.broker.core.internal.util.BrokerConstants;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * helper class to parse the broker type details
 */
public final class BrokerTypeHelper {

    private BrokerTypeHelper(){}

    public static BrokerTypeDto fromOM(OMElement omElement) {

        BrokerTypeDto brokerTypeDto = new BrokerTypeDto();
        brokerTypeDto.setName(omElement.getAttributeValue(
                new QName("", BrokerConstants.BROKER_CONF_ATTR_NAME)));


        Iterator propertyIter = omElement.getChildrenWithName(
                new QName(BrokerConstants.BROKER_CONF_NS,
                          BrokerConstants.BROKER_CONF_ELE_PROPERTY));
        OMElement propertyOMElement = null;

        for (; propertyIter.hasNext();) {
            propertyOMElement = (OMElement) propertyIter.next();
            String name = propertyOMElement.getAttributeValue(
                    new QName("", BrokerConstants.BROKER_CONF_ATTR_NAME));
            // need to correct this
            brokerTypeDto.addProperty(new Property(name));
        }

        return brokerTypeDto;
    }
}
