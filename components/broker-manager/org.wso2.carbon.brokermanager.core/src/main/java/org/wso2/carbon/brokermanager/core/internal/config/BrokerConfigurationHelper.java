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

package org.wso2.carbon.brokermanager.core.internal.config;

import org.wso2.carbon.brokermanager.core.BrokerConfiguration;
import org.wso2.carbon.brokermanager.core.internal.util.BMConstants;
import org.apache.axiom.om.OMElement;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * this class is used to read the values of the broker configurations define in the carbon.xml
 */
public class BrokerConfigurationHelper {

    public static BrokerConfiguration fromOM(OMElement brokerConfigOMElement) {

        BrokerConfiguration brokerConfiguration = new BrokerConfiguration();

        brokerConfiguration.setName(brokerConfigOMElement.getAttributeValue(
                                                new QName("", BMConstants.BM_ATTR_NAME)));

        brokerConfiguration.setType(brokerConfigOMElement.getAttributeValue(
                                                new QName("", BMConstants.BM_ATTR_TYPE)));

        Iterator propertyIter = brokerConfigOMElement.getChildrenWithName(
                      new QName(BMConstants.BM_CONF_NS, BMConstants.BM_ELE_PROPERTY));
        OMElement propertyOMElement = null;

        for (; propertyIter.hasNext();) {
            propertyOMElement = (OMElement) propertyIter.next();
            String name = propertyOMElement.getAttributeValue(
                    new QName("", BMConstants.BM_ATTR_NAME));
            String value = propertyOMElement.getText();
            brokerConfiguration.addProperty(name, value);
        }

        return brokerConfiguration;

    }
}
