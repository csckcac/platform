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

import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.Entry;
import org.wso2.carbon.rule.core.RuleConstants;
import org.wso2.carbon.rule.engine.jsr94.JSR94BackendRuntimeFactory;
import org.wso2.carbon.rule.server.RuleServerConfiguration;
import org.wso2.carbon.rule.server.RuleServerManager;
import org.wso2.carbon.rulecep.adapters.impl.OMElementResourceAdapter;
import org.wso2.carbon.rulecep.adapters.impl.POJOResourceAdapter;
import org.wso2.carbon.rulecep.commons.descriptions.PropertyDescription;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.SessionDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.mediator.RuleMediatorDescription;

/**
 *
 */
public class RuleMediatorWithJSR94Test extends AbstractTestCase {

    private static final String drlRule = "<x><![CDATA[\n" +
            "package RoutingRules;" +
            "\n" +
            "import org.apache.synapse.MessageContext;\n" +
            "import org.wso2.carbon.rulecep.commons.utils.OMElementHelper;\n" +
            "import org.apache.axiom.om.OMElement;\n" +
            "import org.wso2.carbon.rule.mediator.Symbol;" +
            "global java.lang.String source\n" +
            "\n" +
            "rule InvokeIBM\n" +
            "\n" +
            "when\n" +
            "\n" +
            "$symbol : Symbol( symbol == \"IBM\" )" +
            "then\n" +
            "String symbol1 = \"WSO2\";" +
            "OMElement ele = OMElementHelper.getInstance().toOM(\"<m:symbol xmlns:m=\\\"http://services.samples/xsd/xsd\\\">\" + symbol1 + \"</m:symbol>\");" +
            "System.out.println(\"OK\");" +
            "insert(ele);" +
            "\n" +
            "end\n" +
            "]]></x>";

    public void testMediator() throws Exception {

        RuleMediatorDescription ruleMediatorDescription = new RuleMediatorDescription();
        ResourceDescription inputOne = new ResourceDescription();
        inputOne.setName("symbol");
        inputOne.setType(POJOResourceAdapter.TYPE);
        Symbol ibm = new Symbol("IBM");

        ResourceDescription childOfInputOne = new ResourceDescription();
        childOfInputOne.setName("symbol");
        childOfInputOne.setValue("IBM");
        childOfInputOne.setType(String.class.getName());
        ResourceDescription childOfInputTwo = new ResourceDescription();
        childOfInputTwo.setName("price");
        childOfInputTwo.setValue("10000.00");
        childOfInputTwo.setType(Double.class.getName());
        inputOne.addChildResource(childOfInputOne);
        inputOne.addChildResource(childOfInputTwo);
        inputOne.setType(ibm.getClass().getName());
        inputOne.setValue(ibm);

        ResourceDescription outputOne = new ResourceDescription();
        outputOne.setName("symbolCustom");
        outputOne.setType(POJOResourceAdapter.TYPE);
        outputOne.addChildResource(childOfInputOne);
        outputOne.setType(ibm.getClass().getName());

        ResourceDescription outputsTwo = new ResourceDescription();
        outputsTwo.setName("om");
        outputsTwo.setType(OMElementResourceAdapter.TYPE);
//        outputsTwo.addChildResource(childOfInputOne);

        ResourceDescription outputThree = new ResourceDescription();
        AXIOMXPath target = new AXIOMXPath("//m:request/m:symbol");
        target.addNamespace("m", "http://services.samples/xsd/xsd");
        outputThree.setName("symbol");
        outputThree.setType(String.class.getName());

        AXIOMXPath target2 = new AXIOMXPath("//m:request/m:Symbol");
        target2.addNamespace("m", "http://services.samples/xsd/xsd");

        outputOne.setExpression(target);
        outputsTwo.setExpression(target);
        outputThree.setExpression(target2);

//        PropertyDescription enrtyPoint = new PropertyDescription();
//        enrtyPoint.setName("symbol");
//        enrtyPoint.setValue("Stock Stream");
        SessionDescription sessionDescription = new SessionDescription();
        sessionDescription.setSessionType(SessionDescription.STATEFUL_SESSION);
        RuleServerManager ruleServerManager = new RuleServerManager();// TODO to get from a OSGI service
        RuleServerConfiguration configuration = new RuleServerConfiguration(new JSR94BackendRuntimeFactory());
        ruleServerManager.init(configuration); //TODO

//        sessionDescription.addSessionPropertyDescription(enrtyPoint);
        ruleMediatorDescription.setSessionDescription(sessionDescription);
        ruleMediatorDescription.addFactDescription(inputOne);
//        configuration.addOutputResourceDescription(outputThree);
//        configuration.addOutputResourceDescription(outputOne);
        ruleMediatorDescription.addResultDescription(outputsTwo);


        RuleSetDescription setDescription = new RuleSetDescription();
        setDescription.setBindURI("RoutingRules");
        setDescription.setRuleSource(createOMElement(drlRule));

        PropertyDescription creationDescription = new PropertyDescription();
        creationDescription.setName(RuleConstants.SOURCE);
        creationDescription.setValue("drl");
        setDescription.addCreationProperty(creationDescription);

        ruleMediatorDescription.setRuleSetDescription(setDescription);
        RuleMediator ruleMediator = new RuleMediator(ruleMediatorDescription);
        ruleMediator.init(null);

        for (int i = 0; i < 3; i++) {
            MessageContext mcTemp = getMessageContext();
            ruleMediator.mediate(mcTemp);
            assertEquals("WSO2",
                    mcTemp.getEnvelope().getBody().getFirstElement().
                            getFirstElement().getFirstElement().getText());
        }

    }

    private MessageContext getMessageContext() throws Exception {
        MessageContext context = TestUtils.getAxis2MessageContext(
                "        <m:getQuote xmlns:m=\"http://services.samples/xsd/xsd\">\n" +
                        "            <m:request>\n" +
                        "                <m:symbol>IBM</m:symbol>\n" +
                        "            </m:request>\n" +
                        "        </m:getQuote>\n"
                , null);
        Entry entry = new Entry("rule_script");
        entry.setValue(createOMElement(drlRule));
        context.getConfiguration().addEntry("rule_script", entry);
        return context;
    }
}