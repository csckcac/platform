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
import org.apache.synapse.config.Entry;

import java.util.Properties;

/**
 *
 */
public class RuleMediatorSample604Test extends AbstractTestCase {

    private static final String RULE_FILE = "<package name=\"SimpleMessageTransformationRules\"\n" +
            "         xmlns=\"http://drools.org/drools-5.0\"\n" +
            "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "         xs:schemaLocation=\"http://drools.org/drools-5.0 drools-4.0.xsd\">\n" +
            "    <rule name=\"Invoke Always IBM \">\n" +
            "        <lhs>\n" +
            "            <pattern object-type=\"String\" identifier=\"symbol\">\n" +
            "            </pattern>\n" +
            "            <eval>symbol.equals(\"MSFT\") || symbol.equals(\"SUN\")</eval>\n" +
            "        </lhs>\n" +
            "        <rhs>\n" +
            "            update(drools.getWorkingMemory().getFactHandle(symbol),\"IBM\");\n" +
            "        </rhs>\n" +
            "    </rule>\n" +
            "\n" +
            "</package>";
    private static final String MEDIATOR = " <rule>\n" +
            "                <ruleset uri=\"SimpleMessageTransformationRules\">\n" +
            "                    <source key=\"rule-script-key\"/>\n" +
            "            <creation>\n" +
            "                <property name=\"source\" value=\"xml\"/>\n" +
            "            </creation>\n" +
            "                </ruleset>\n" +
            "                <session type=\"stateless\"/>\n" +
            "                <facts><fact name=\"symbol\" expression=\"//m0:getQuote/m0:request/m0:symbol/child::text()\"\n" +
            "                       type=\"java.lang.String\" xmlns:m0=\"http://services.samples/xsd\"/></facts>\n" +
            "                <results><result name=\"symbol\" expression=\"//m0:getQuote/m0:request/m0:symbol\"\n" +
            "                        type=\"java.lang.String\" xmlns:m0=\"http://services.samples/xsd\"/></results>\n" +
            "\n" +
            "        </rule>";

    public void testMediator() throws Exception {

        RuleMediatorFactory mediatorFactory = new RuleMediatorFactory();
        Properties properties = new Properties();
        Mediator ruleMediator = mediatorFactory.createMediator(createOMElement(MEDIATOR),properties);
        ((ManagedLifecycle) ruleMediator).init(null);
        MessageContext ibmMC = getMessageContext("IBM");
        ruleMediator.mediate(ibmMC);
        assertEquals("IBM",
                ibmMC.getEnvelope().getBody().getFirstElement().
                        getFirstElement().getFirstElement().getText());

        MessageContext msfcMC = getMessageContext("MSFT");
        ruleMediator.mediate(msfcMC);
        assertEquals("IBM",
                msfcMC.getEnvelope().getBody().getFirstElement().
                        getFirstElement().getFirstElement().getText());
        MessageContext sunMC = getMessageContext("SUN");
        ruleMediator.mediate(sunMC);
        assertEquals("IBM",
                sunMC.getEnvelope().getBody().getFirstElement().
                        getFirstElement().getFirstElement().getText());

    }

    private MessageContext getMessageContext(String symbol) throws Exception {

        MessageContext context = TestUtils.getAxis2MessageContext(
                "<m:getQuote xmlns:m=\"http://services.samples/xsd\">\n" +
                        "            <m:request>\n" +
                        "                <m:symbol>" + symbol + "</m:symbol>\n" +
                        "            </m:request>\n" +
                        "        </m:getQuote>\n"
                , null);
        Entry entry = new Entry("rule-script-key");
        entry.setValue(createOMElement(RULE_FILE));
        context.getConfiguration().addEntry("rule-script-key", entry);
        return context;
    }
}

