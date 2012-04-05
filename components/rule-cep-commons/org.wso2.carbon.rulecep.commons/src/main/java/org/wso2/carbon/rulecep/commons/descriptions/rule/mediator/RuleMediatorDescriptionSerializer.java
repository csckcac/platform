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
package org.wso2.carbon.rulecep.commons.descriptions.rule.mediator;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.wso2.carbon.rulecep.commons.CommonsConstants;
import org.wso2.carbon.rulecep.commons.descriptions.QNameFactory;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescriptionSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.XPathSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescriptionSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.rule.SessionDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.SessionDescriptionSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensionSerializer;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Serializes <code>RuleMediatorDescription</code>  into an XML
 */
public class RuleMediatorDescriptionSerializer {

    private static final OMFactory fac = OMAbstractFactory.getOMFactory();
    private static final OMNamespace synNS
            = fac.createOMNamespace("http://ws.apache.org/ns/synapse", "syn");
    private static final QNameFactory qNameFactory = QNameFactory.getInstance();


    /**
     * Creates an XML representation of the <code>RuleMediatorDescription</code>
     *
     * @param ruleMediatorDescription <code>RuleMediatorDescription</code>  to be serialized
     *                                into an XML
     * @param xPathSerializer         serializes XPaths
     * @param configurationExtensionSerializer
     *                                <code>ConfigurationExtensionSerializer </code> instance
     * @return an XML representation of the  <code>RuleMediatorDescription</code>
     */
    public static OMElement serialize(RuleMediatorDescription ruleMediatorDescription,
                                      XPathSerializer xPathSerializer,
                                      ExtensionSerializer configurationExtensionSerializer) {

        OMElement ruleElement = fac.createOMElement("rule", synNS);

        RuleSetDescription ruleSetDescription = ruleMediatorDescription.getRuleSetDescription();
        if (ruleSetDescription != null) {
            OMElement ruleSetElement = fac.createOMElement(CommonsConstants.ELE_RULESET, synNS);
            RuleSetDescriptionSerializer.serialize(ruleSetDescription, xPathSerializer,
                    ruleSetElement, configurationExtensionSerializer);
            ruleElement.addChild(ruleSetElement);
        }

        SessionDescription sessionDescription = ruleMediatorDescription.getSessionDescription();
        if (sessionDescription != null) {
            OMElement sessionElement =
                    SessionDescriptionSerializer.serialize(sessionDescription, xPathSerializer,
                            ruleElement.getQName());
            ruleElement.addChild(sessionElement);
        }

        List<ResourceDescription> facts = ruleMediatorDescription.getFacts();
        if (!facts.isEmpty()) {
            OMElement factElement = fac.createOMElement("facts",
                    synNS);
            QName factQName = qNameFactory.createQName(CommonsConstants.ELE_FACT, synNS);
            for (ResourceDescription resourceDescription : facts) {
                OMElement resourceElement =
                        ResourceDescriptionSerializer.serialize(
                                resourceDescription, factQName, xPathSerializer);
                if (resourceElement != null) {
                    factElement.addChild(resourceElement);
                }
            }
            ruleElement.addChild(factElement);
        }

        List<ResourceDescription> results = ruleMediatorDescription.getResults();
        if (!results.isEmpty()) {
            OMElement resultElement = fac.createOMElement("results",
                    synNS);
            QName resultQName = qNameFactory.createQName(CommonsConstants.ELE_RESULT, synNS);

            for (ResourceDescription resourceDescription : results) {
                OMElement resourceElement =
                        ResourceDescriptionSerializer.serialize(
                                resourceDescription, resultQName, xPathSerializer);
                if (resourceElement != null) {
                    resultElement.addChild(resourceElement);
                }
            }
            ruleElement.addChild(resultElement);
        }
        return ruleElement;
    }
}
