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

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractListMediatorFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.wso2.carbon.rulecep.commons.descriptions.XPathFactory;
import org.wso2.carbon.rulecep.commons.descriptions.rule.mediator.RuleMediatorDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.mediator.RuleMediatorDescriptionFactory;
import org.wso2.carbon.rulecep.commons.descriptions.rule.mediator.RuleMediatorExtensionBuilder;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensionBuilder;

import javax.xml.namespace.QName;
import java.util.Properties;

/**
 * Factory for creating a <code>RuleMediator</code>.
 */
public class RuleMediatorFactory extends AbstractListMediatorFactory {

    private static final Log log = LogFactory.getLog(RuleMediatorFactory.class);

    private static final QName TAG_NAME
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "rule");

    private static final QName ELE_CHILD_MEDIATORS_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "childMediators");
    private final static XPathFactory XPATH_FACTORY = new SynapseXPathFactory();
    private final static ExtensionBuilder CONFIGURATION_EXTENSION_BUILDER = new RuleMediatorExtensionBuilder();

    /**
     * Creates a rule mediator instance from the XML representation of rule mediator
     * First using OSGIServiceLocator,try to find the RuleServerManagerService,
     * if it is found uses it. Otherwise , create a default  RuleServerManagerService implementation
     *
     * @param confElement The OMElement -  The XML representation of rule mediator
     * @return Returns an instance of rule mediator
     */
    public Mediator createSpecificMediator(OMElement confElement , Properties properties) {

        RuleMediatorDescription ruleMediatorDescription =
                RuleMediatorDescriptionFactory.create(confElement, XPATH_FACTORY, CONFIGURATION_EXTENSION_BUILDER);
        RuleMediator ruleMediator = new RuleMediator(ruleMediatorDescription);

        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        super.processAuditStatus(ruleMediator, confElement);

        OMElement child = confElement.getFirstChildWithName(ELE_CHILD_MEDIATORS_Q);
        if (child != null) {
            addChildren(child, ruleMediator,properties);
        }

        return ruleMediator;
    }

    public QName getTagQName() {
        return TAG_NAME;
    }
}

