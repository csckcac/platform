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
import org.apache.synapse.config.xml.AbstractListMediatorSerializer;
import org.wso2.carbon.rule.core.LoggedRuntimeException;
import org.wso2.carbon.rulecep.commons.descriptions.rule.mediator.RuleMediatorDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.mediator.RuleMediatorDescriptionSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.rule.mediator.RuleMediatorExtensionSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensionSerializer;

import java.util.List;

/**
 * Serializes <code>RuleMediator</code> instance into the mediator XML representation
 */
public class RuleMediatorSerializer extends AbstractListMediatorSerializer {

    private static final Log log = LogFactory.getLog(RuleMediatorSerializer.class);
    private final static SynapseXPathSerializer SYNAPSE_XPATH_SERIALIZER = new SynapseXPathSerializer();
    private final static ExtensionSerializer CONFIGURATION_EXTENSION_SERIALIZER =
            new RuleMediatorExtensionSerializer();

    /**
     * Creates the XML representation of rule mediator from the rule mediator instance
     *
     * @param m      the rule mediator instance
     * @return The XML representation of rule mediator   (OMElement)
     */
    public OMElement serializeSpecificMediator(Mediator m) {

        if (!(m instanceof RuleMediator)) {
            throw new LoggedRuntimeException("Invalid Mediator has passed to serializer", log);
        }
        RuleMediator ruleMediator = (RuleMediator) m;
        RuleMediatorDescription ruleMediatorDescription = ruleMediator.getRuleMediatorDescription();
        OMElement ruleElement =
                RuleMediatorDescriptionSerializer.serialize(ruleMediatorDescription,
                        SYNAPSE_XPATH_SERIALIZER, CONFIGURATION_EXTENSION_SERIALIZER);
        saveTracingState(ruleElement, ruleMediator);

        List<Mediator> list = ruleMediator.getList();
        if (list != null && !list.isEmpty()) {
            OMElement child = fac.createOMElement("childMediators", synNS);
            serializeChildren(child, list);
            ruleElement.addChild(child);
        }

        return ruleElement;
    }


    public String getMediatorClassName() {
        return RuleMediator.class.getName();
    }

}

