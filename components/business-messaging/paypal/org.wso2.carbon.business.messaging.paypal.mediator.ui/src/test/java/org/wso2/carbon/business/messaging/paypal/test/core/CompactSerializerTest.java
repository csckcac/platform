/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.business.messaging.paypal.test.core;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.om.impl.llom.util.XMLComparator;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.wso2.carbon.business.messaging.paypal.mediator.ui.PaypalCompactSerializer;
import org.wso2.carbon.business.messaging.paypal.mediator.ui.PaypalMediator;
import org.wso2.carbon.business.messaging.paypal.test.util.PaypalTestUtil;

import javax.xml.stream.XMLStreamException;

public class CompactSerializerTest extends TestCase {

    public void testSerializeConfiguration() throws Exception {
        OMElement serializedElement = serialize("paypal_config1.xml");

        OMElement testElement = PaypalTestUtil.getConfiguration("paypal_config1.xml");
        assertTrue(new XMLComparator().compare(serializedElement, testElement));
    }

    public void testSerializeConfiguration2() throws Exception {
        OMElement serializedElement = serialize("paypal_config2.xml");
        OMElement testElement = PaypalTestUtil.getConfiguration("paypal_config2.xml");
        assertTrue(new XMLComparator().compare(serializedElement, testElement));
    }

    public void testSerializeConfiguration3() throws Exception {
        OMElement serializedElement = serialize("paypal_config3.xml");
        OMElement testElement = PaypalTestUtil.getConfiguration("paypal_config3.xml");
        assertTrue(new XMLComparator().compare(serializedElement, testElement));
    }

    private OMElement serialize(String config) throws XMLStreamException {
        PaypalMediator mediator = PaypalTestUtil.buildMediator("compact", PaypalTestUtil.
                getConfiguration(config));
        PaypalCompactSerializer serializer = new PaypalCompactSerializer(OMAbstractFactory.getOMFactory(),
                                                                         XMLConfigConstants.SYNAPSE_OMNAMESPACE,
                                                                         new OMNamespaceImpl(XMLConfigConstants.NULL_NAMESPACE, "nns"));
        OMElement serializedElement = serializer.serializeMediator(null, mediator);
        return serializedElement;
    }


}