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

import org.apache.axiom.om.OMNode;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.XMLToObjectMapper;
import org.apache.synapse.config.xml.MediatorFactoryFinder;

import java.util.Properties;

/**
 *
 */
public class RuleMediatorSample603Test extends AbstractTestCase {

    private static final String RULE_FILE = "<package name=\"AdvancedRoutingRules\"\n" +
            "         xmlns=\"http://drools.org/drools-5.0\"\n" +
            "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "         xs:schemaLocation=\"http://drools.org/drools-5.0 drools-4.0.xsd\">\n" +
            "\n" +
            "    <import name=\"org.apache.synapse.MessageContext\"/>\n" +
            "    <import name=\"org.apache.synapse.Mediator\"/>\n" +
            "    <import name=\"org.apache.synapse.mediators.base.SequenceMediator\"/>\n" +
            "\n" +
            "    <rule name=\"Invoke IBM \">\n" +
            "        <lhs>\n" +
            "            <pattern object-type=\"MessageContext\" identifier=\"mc\">\n" +
            "            </pattern>\n" +
            "            <pattern object-type=\"String\" identifier=\"symbol\">\n" +
            "            </pattern>\n" +
            "            <pattern identifier=\"seq\" object-type=\"SequenceMediator\">\n" +
            "                <field-binding field-name=\"name\" identifier=\"a4\"/>\n" +
            "                <field-constraint field-name=\"name\">\n" +
            "                    <literal-restriction evaluator=\"==\" value=\"ibmSequence\"/>\n" +
            "                </field-constraint>\n" +
            "            </pattern>\n" +
            "            <eval>symbol.equals(\"IBM\")</eval>\n" +
            "        </lhs>\n" +
            "\n" +
            "        <rhs>\n" +
            "            ((Mediator)seq).mediate(mc);\n" +
            "        </rhs>\n" +
            "    </rule>\n" +
            "\n" +
            "    <rule name=\"Invoke SUN \">\n" +
            "        <lhs>\n" +
            "            <pattern object-type=\"MessageContext\" identifier=\"mc\">\n" +
            "            </pattern>\n" +
            "            <pattern object-type=\"String\" identifier=\"symbol\">\n" +
            "            </pattern>\n" +
            "            <pattern identifier=\"seq\" object-type=\"SequenceMediator\">\n" +
            "                <field-binding field-name=\"name\" identifier=\"a4\"/>\n" +
            "                <field-constraint field-name=\"name\">\n" +
            "                    <literal-restriction evaluator=\"==\" value=\"sunSequence\"/>\n" +
            "                </field-constraint>\n" +
            "            </pattern>\n" +
            "            <eval>symbol.equals(\"SUN\")</eval>\n" +
            "        </lhs>\n" +
            "\n" +
            "        <rhs>\n" +
            "            ((Mediator)seq).mediate(mc);\n" +
            "        </rhs>\n" +
            "    </rule>\n" +
            "\n" +
            "    <rule name=\"Invoke MFST \">\n" +
            "        <lhs>\n" +
            "            <pattern object-type=\"MessageContext\" identifier=\"mc\">\n" +
            "            </pattern>\n" +
            "            <pattern object-type=\"String\" identifier=\"symbol\">\n" +
            "            </pattern>\n" +
            "            <pattern identifier=\"seq\" object-type=\"SequenceMediator\">\n" +
            "                <field-binding field-name=\"name\" identifier=\"a4\"/>\n" +
            "                <field-constraint field-name=\"name\">\n" +
            "                    <literal-restriction evaluator=\"==\" value=\"msftSequence\"/>\n" +
            "                </field-constraint>\n" +
            "            </pattern>\n" +
            "            <eval>symbol.equals(\"MSFT\")</eval>\n" +
            "        </lhs>\n" +
            "\n" +
            "        <rhs>\n" +
            "            ((Mediator)seq).mediate(mc);\n" +
            "        </rhs>\n" +
            "    </rule>\n" +
            "\n" +
            "</package>";
    private static final String MEDIATOR = "<rule>\n" +
            "                <ruleset uri=\"SimpleRoutingRules\">\n" +
            "                    <source key=\"rule-script-key\"/>\n" +
            "            <creation>\n" +
            "                <property name=\"source\" value=\"xml\"/>\n" +
            "            </creation>\n" +
            "                </ruleset>\n" +
            "                <session type=\"stateless\"/>\n" +
            "                <facts><fact name=\"mc\" type=\"context\"/>\n" +
            "                <fact name=\"ibmSeq\" key=\"ibmSequence\" type=\"mediator\"/>\n" +
            "                <fact name=\"sunSeq\" key=\"sunSequence\" type=\"mediator\"/>\n" +
            "                <fact name=\"msftSeq\" key=\"msftSequence\" type=\"mediator\"/>\n" +
            "                <fact name=\"symbol\" type=\"java.lang.String\"\n" +
            "                       expression=\"//m0:getQuote/m0:request/m0:symbol/child::text()\"\n" +
            "                       xmlns:m0=\"http://services.samples/xsd\"/></facts>\n" +
            "        </rule>";

    public void testMediator() throws Exception {

        RuleMediatorFactory mediatorFactory = new RuleMediatorFactory();
        Properties properties = new Properties();
        Mediator ruleMediator = mediatorFactory.createMediator(createOMElement(MEDIATOR),properties);
        ((ManagedLifecycle) ruleMediator).init(null);
        MessageContext ibmMC = getMessageContext("IBM");
        ruleMediator.mediate(ibmMC);
        assertEquals("IBMSEQ",
                ibmMC.getProperty("sequence"));

        MessageContext msfcMC = getMessageContext("MSFT");
        ruleMediator.mediate(msfcMC);
        assertEquals("MSFTSEQ",
                msfcMC.getProperty("sequence"));
        MessageContext sunMC = getMessageContext("SUN");
        ruleMediator.mediate(sunMC);
        assertEquals("SUNSEQ",
                sunMC.getProperty("sequence"));

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
        context.getConfiguration().addSequence("ibmSequence", getMediator(
                createOMElement(" <sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"ibmSequence\">\n" +
                        "        <property name=\"sequence\" value=\"IBMSEQ\"/>\n" +
                        "    </sequence>")));
        context.getConfiguration().addSequence("msftSequence", getMediator(
                createOMElement("<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"msftSequence\">\n" +
                        "        <property name=\"sequence\" value=\"MSFTSEQ\"/>\n" +
                        "    </sequence>")));
        context.getConfiguration().addSequence("sunSequence", getMediator(
                createOMElement(" <sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"sunSequence\">\n" +
                        "        <property name=\"sequence\" value=\"SUNSEQ\"/>\n" +
                        "    </sequence>")));
        return context;
    }

    private Mediator getMediator(OMNode omNode) {
        XMLToObjectMapper mapper = MediatorFactoryFinder.getInstance();
        Properties properties = new Properties();
        return (Mediator) mapper.getObjectFromOMNode(omNode,properties);
    }


}
