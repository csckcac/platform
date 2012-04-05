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

import javax.xml.namespace.QName;
import java.util.Properties;

/**
 *
 */
public class RuleMediatorSample606Test extends AbstractTestCase {

    private static final String RULE_FILE = "<drl>\n" +
            "    <![CDATA[\n" +
            "package BackwardTransformation;\n" +
            "\n" +
            "import org.wso2.carbon.rule.samples.GetQuoteResponse;\n" +
            "\n" +
            "rule BackwardTransformation\n" +
            "\n" +
            "when\n" +
            "\n" +
            "symbol : String()\n" +
            "eval(symbol != null )\n" +
            "\n" +
            "then\n" +
            "\n" +
            "GetQuoteResponse request = new GetQuoteResponse();\n" +
            "\n" +
            "request.setSymbol(symbol+\".WSO2\");\n" +
            "request.setLast(\"10002.00\");\n" +
            "insert(request);\n" +
            "end\n" +
            "\n" +
            "]]>\n" +
            "</drl>";
    private static final String MEDIATOR = "<rule>\n" +
            "        <ruleset uri=\"BackwardTransformation\">\n" +
            "            <source key=\"rule-script-key\"/>\n" +
            "        </ruleset>\n" +
            "        <session type=\"stateless\"/>\n" +
            "        <facts><fact name=\"symbol\" expression=\"//m0:getQuote/m0:request/m0:symbol/child::text()\"\n" +
            "                       type=\"java.lang.String\" xmlns:m0=\"http://services.samples/xsd\"/></facts>\n" +
            "        <results><result name=\"request\" xmlns:m0=\"http://services.samples\"\n" +
            "                type=\"org.wso2.carbon.rule.samples.GetQuoteResponse\"/></results>\n" +
            "</rule>";

    public void testMediator() throws Exception {

        RuleMediatorFactory mediatorFactory = new RuleMediatorFactory();
        Properties properties = new Properties();
        Mediator ruleMediator = mediatorFactory.createMediator(createOMElement(MEDIATOR),properties);
        ((ManagedLifecycle) ruleMediator).init(null);
        MessageContext ibmMC = getMessageContext("IBM");
        ruleMediator.mediate(ibmMC);
        assertEquals("IBM.WSO2",
                ibmMC.getEnvelope().getBody().getFirstElement().getFirstChildWithName(
                        new QName("http://services.samples/xsd", "symbol", "m")).getText());
        assertEquals("10002.00",
                ibmMC.getEnvelope().getBody().getFirstElement().getFirstChildWithName(
                        new QName("http://services.samples/xsd", "last", "m")).getText());

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
