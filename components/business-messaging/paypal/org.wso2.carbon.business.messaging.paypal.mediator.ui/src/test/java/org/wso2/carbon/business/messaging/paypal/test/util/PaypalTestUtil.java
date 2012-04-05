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
package org.wso2.carbon.business.messaging.paypal.test.util;

import junit.framework.Assert;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.wso2.carbon.business.messaging.paypal.mediator.ui.Input;
import org.wso2.carbon.business.messaging.paypal.mediator.ui.PaypalCompactBuilder;
import org.wso2.carbon.business.messaging.paypal.mediator.ui.PaypalMediator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PaypalTestUtil {
    public static void validateCredentialInputs(HashMap map, List<Input> inputs) {
        validateSimpleInputs(map, inputs);
    }

    public static void validateSimpleInputs(HashMap map, List<Input> inputs) {
        for (Input input : inputs) {
            Assert.assertTrue(map.containsKey(input.getName()));
            Input testInput = (Input) map.get(input.getName());
            validateInputProperties(input, testInput);
        }
    }

    public static void validateInputProperties(Input input, Input testInput) {
        Assert.assertEquals(testInput.getName(), input.getName());
        Assert.assertEquals(testInput.isRequired(), input.isRequired());
        if (testInput.getSourceXPath() != null && input.getSourceXPath() != null) {
            SynapseXPath inputXpath = input.getSourceXPath();
            SynapseXPath testXpath = testInput.getSourceXPath();
            validateXpathExpressions(inputXpath, testXpath);
        } else {
            Assert.assertEquals(testInput.getSourceXPath(), input.getSourceXPath());
        }
        Assert.assertEquals(testInput.getSourceValue(), input.getSourceValue());
    }

    private static void validateXpathExpressions(SynapseXPath inputXpath, SynapseXPath testXpath) {
        String normInputXpath = createNormalizedXpath(inputXpath);
        String normTestXpath = createNormalizedXpath(testXpath);
        Assert.assertEquals(normInputXpath, normTestXpath);
    }

    private static String createNormalizedXpath(SynapseXPath inputXpath) {
        //TODO this xpath normalizing implementation may not be complete  find a better way?
        String inputXpathString = inputXpath.toString();
        Map namespaceMap = inputXpath.getNamespaces();
        Set keys = inputXpath.getNamespaces().keySet();
        String normalizedXpathExpr = inputXpathString;
        for (Object key : keys) {
            String namespace = (String) namespaceMap.get(key);
            String prefix = (String) key;
            normalizedXpathExpr = normalizedXpathExpr.replaceAll(prefix + ":", "{" + namespace + "}");
        }
        return normalizedXpathExpr;
    }

    public static void validateComplexInputs(HashMap map, List<Input> inputs) {
        for (Input input : inputs) {
            String key = input.getName() == null ? input.getType() : input.getName();
            Assert.assertTrue(map.containsKey(key));
            Object inline = map.get(key);

            if (inline instanceof Input) {
                Input testInput = (Input) inline;
                validateInputProperties(input, testInput);
            } else if (inline instanceof HashMap) {
                validateComplexInputs((HashMap) inline, input.getSubInputs());
            } else {
                //this can't happen ie:-null
                Assert.assertTrue(false);
            }
        }
    }

    public static OMElement getConfiguration(String configurationFile) throws XMLStreamException {
        OMElement rootElem = new StAXOMBuilder(Thread.currentThread().getContextClassLoader().
                getResourceAsStream(configurationFile)).getDocumentElement();
        return rootElem.getFirstChildWithName(new QName(XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(),
                                                        "paypal"));
    }

    public static PaypalMediator buildMediator(String builderType ,OMElement config) {
        PaypalMediator mediator = null;
        if ("compact".equals(builderType)) {
            mediator = new PaypalMediator();
            new PaypalCompactBuilder().buildMediator(config, mediator);
        }
        return mediator;
    }
}
