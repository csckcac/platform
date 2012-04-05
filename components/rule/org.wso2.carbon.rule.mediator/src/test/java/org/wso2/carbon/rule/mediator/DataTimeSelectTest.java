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
 * DataTime selection from the message using an XPath
 */
public class DataTimeSelectTest extends AbstractTestCase {

    private static final String RULE_FILE = "<drl>\n" +
            "    <![CDATA[\n" +
            "package datatimetest;\n" +
            "\n" +
            "import org.wso2.carbon.rulecep.adapters.Message;\n" +
            "import java.util.Calendar;\n" +
            "\n" +
            "rule yearvalidate\n" +
            "\n" +
            "when\n" +
            "\n" +
            "$message : Message()\n" +
            "eval($message.selectDataTime(\"//n1:Sub/nc:Date/nc:DateTime/child::text()\").get(Calendar.YEAR) == 2012)" +
            "\n" +
            "then\n" +
            "\n" +
            "System.out.println(\"OK\");\n" +
            "end\n" +
            "\n" +
            "]]>\n" +
            "</drl>";
    private static final String MEDIATOR = "<rule>\n" +
            "        <ruleset>\n" +
            "            <source key=\"rule-script-key\"/>\n" +
            "        </ruleset>\n" +
            "        <session type=\"stateless\"/>\n" +
            "        <facts><fact xmlns:nc=\"http://example.4\" xmlns:n1=\"http://example.1\"  type=\"message\"/></facts>\n" +
            "</rule>";

    public void testMediator() throws Exception {

        RuleMediatorFactory mediatorFactory = new RuleMediatorFactory();
        Properties properties = new Properties();
        Mediator ruleMediator = mediatorFactory.createMediator(createOMElement(MEDIATOR),properties);
        ((ManagedLifecycle) ruleMediator).init(null);
        MessageContext ibmMC = getMessageContext("IBM");
        ruleMediator.mediate(ibmMC);

    }

    private MessageContext getMessageContext(String symbol) throws Exception {

        MessageContext context = TestUtils.getAxis2MessageContext(
                "<n1:Sub xmlns:nc=\"http://example.4\" xmlns:n1=\"http://example.1\">\n" +
                        "\t<nc:Date>\n" +
                        "\t\t<nc:DateTime>2012-04-23T13:05:34</nc:DateTime>\n" +
                        "\t</nc:Date>\t\n" +
                        "</n1:Sub>"
                , null);
        Entry entry = new Entry("rule-script-key");
        entry.setValue(createOMElement(RULE_FILE));
        context.getConfiguration().addEntry("rule-script-key", entry);
        return context;
    }
}
