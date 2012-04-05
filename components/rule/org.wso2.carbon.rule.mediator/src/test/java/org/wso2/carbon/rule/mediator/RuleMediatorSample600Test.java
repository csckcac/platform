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
package org.wso2.carbon.rule.mediator;

import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;

import java.util.Properties;

/**
 *
 */
public class RuleMediatorSample600Test extends AbstractTestCase {

    private static final String MEDIATOR = "<rule xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
            "                <ruleset uri=\"SimpleRoutingRules\">\n" +
            "                    <source>\n" +
            "                        <package name=\"SimpleRoutingRules\"\n" +
            "                                 xmlns=\"http://drools.org/drools-5.0\"\n" +
            "                                 xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "                                 xs:schemaLocation=\"http://drools.org/drools-5.0 drools-4.0.xsd\">\n" +
            "                            <import name=\"org.apache.synapse.MessageContext\"/>\n" +
            "                            <rule name=\"Invoke IBM \">\n" +
            "                                <lhs>\n" +
            "                                    <pattern object-type=\"MessageContext\" identifier=\"mc\">\n" +
            "                                    </pattern>\n" +
            "                                    <pattern object-type=\"String\" identifier=\"symbol\">\n" +
            "                                    </pattern>\n" +
            "                                    <eval>symbol.equals(\"IBM\")</eval>\n" +
            "                                </lhs>\n" +
            "                                <rhs>\n" +
            "                                    mc.setProperty(\"execute_children\",\"true\");\n" +
            "                                </rhs>\n" +
            "                            </rule>\n" +
            "                        </package>\n" +
            "                    </source>\n" +
            "            <creation>\n" +
            "                <property name=\"source\" value=\"xml\"/>\n" +
            "            </creation>\n" +
            "                </ruleset>\n" +
            "                <session type=\"stateless\"/>\n" +
            "                <facts><fact name=\"mc\" type=\"context\"/>\n" +
            "                <fact name=\"symbol\" type=\"java.lang.String\"\n" +
            "                       expression=\"//m0:getQuote/m0:request/m0:symbol/child::text()\"\n" +
            "                       xmlns:m0=\"http://services.samples/xsd\"/></facts>\n" +
            "            <childMediators>\n" +
            "                <property name=\"test\" value=\"executechildMediators\"/>\n" +
            "                <drop/>\n" +
            "            </childMediators>\n" +
            "        </rule>";

    public void testMediator() throws Exception {
        RuleMediatorFactory mediatorFactory = new RuleMediatorFactory();
        Properties properties = new Properties();
        Mediator ruleMediator = mediatorFactory.createMediator(createOMElement(MEDIATOR),properties);
        ((ManagedLifecycle) ruleMediator).init(null);
        MessageContext mcTemp = getMessageContext();
        ruleMediator.mediate(mcTemp);
        assertEquals("executechildMediators",
                mcTemp.getProperty("test"));

    }

    private MessageContext getMessageContext() throws Exception {

        return TestUtils.getAxis2MessageContext(
                "<m:getQuote xmlns:m=\"http://services.samples/xsd\">\n" +
                        "            <m:request>\n" +
                        "                <m:symbol>IBM</m:symbol>\n" +
                        "            </m:request>\n" +
                        "        </m:getQuote>\n"
                , null);
    }
}
