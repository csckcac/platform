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
package org.wso2.carbon.rule.server;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rule.core.LoggedRuntimeException;
import org.wso2.carbon.rule.core.RuleBackendRuntimeFactory;
import org.wso2.carbon.rule.core.RuleConstants;
import org.wso2.carbon.rulecep.commons.descriptions.*;
import org.wso2.carbon.rulecep.commons.utils.ClassHelper;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;

/**
 * Factory to  create a RuleServerConfiguration from a XML object
 */
public class RuleServerConfigurationFactory {

    private static final Log log = LogFactory.getLog(RuleServerConfigurationFactory.class);

    /**
     * Creates a RuleServerConfiguration instance from the given XML. if the given XML is invalid ,
     * LoggedRuntimeExceptions are thrown.
     *
     * @param configurationXML XML representation of the rule server configuration
     * @param xPathFactory     Factory to create XPaths defined in the configuration
     * @return <code>RuleServerConfiguration</code>  if the provided XML is correct.
     *         Otherwise LoggedRuntimeExceptions are thrown
     */
    public static RuleServerConfiguration create(OMElement configurationXML, XPathFactory xPathFactory) {

        if (configurationXML == null) {
            throw new LoggedRuntimeException("Invalid  configuration. " +
                    "The configuration cannot be null.", log);
        }

        QName tagQName = configurationXML.getQName();
        QNameFactory qNameFactory = QNameFactory.getInstance();

        QName providerQName = qNameFactory.createQName(RuleConstants.ELE_PROVIDER, tagQName);
        OMElement providerElement = configurationXML.getFirstChildWithName(providerQName);
        String ruleProviderClass = null;
        if (providerElement != null) {
            ruleProviderClass = providerElement.getAttributeValue(new QName("class"));
        }

        if (ruleProviderClass == null || "".equals(ruleProviderClass.trim())) {
            ruleProviderClass = "org.wso2.carbon.rule.engine.jsr94.JSR94BackendRuntimeFactory";
            log.info("A Rule provider has not been specified. Using default one : " +
                    ruleProviderClass);
        }
        // create the rule engine provider
        RuleBackendRuntimeFactory runtimeFactory =
                (RuleBackendRuntimeFactory) ClassHelper.createInstance(ruleProviderClass);
        RuleServerConfiguration serverConfiguration = new RuleServerConfiguration(runtimeFactory);
        serverConfiguration.setQName(tagQName);

        if (providerElement != null) {
            final List<PropertyDescription> propertyList =
                    PropertyDescriptionFactory.createPropertyDescriptionList(providerElement,
                            xPathFactory, PropertyDescription.PROPERTY_CAP_Q);
            for (PropertyDescription propertyDescription : propertyList) {
                if (propertyDescription != null) {
                    serverConfiguration.addProviderPropertyDescription(propertyDescription);
                }
            }
        }
        // registers fact adapters
        QName factAdapterQName = qNameFactory.createQName(RuleConstants.ELE_FACT_ADAPTER, tagQName);
        QName adapterQName = qNameFactory.createQName(RuleConstants.ELE_ADAPTER, tagQName);
        OMElement factAdapterElement = configurationXML.getFirstChildWithName(factAdapterQName);
        if (factAdapterElement != null) {
            Iterator factAdapters = factAdapterElement.getChildrenWithName(adapterQName);
            while (factAdapters.hasNext()) {
                OMElement child = (OMElement) factAdapters.next();
                ResourceDescription factDescription =
                        ResourceDescriptionFactory.createResourceDescription(child, xPathFactory);
                if (factDescription != null) {
                    serverConfiguration.addFactAdapterDescription(factDescription);
                }
            }
        }
        // registers results adapters
        QName resultAdapterQName = qNameFactory.createQName(RuleConstants.ELE_RESULT_ADAPTER, tagQName);
        OMElement resultAdapterElement = configurationXML.getFirstChildWithName(resultAdapterQName);
        if (resultAdapterElement != null) {
            Iterator resultAdapters = resultAdapterElement.getChildrenWithName(adapterQName);
            while (resultAdapters.hasNext()) {
                OMElement child = (OMElement) resultAdapters.next();
                ResourceDescription resultDescription =
                        ResourceDescriptionFactory.createResourceDescription(child, xPathFactory);
                if (resultDescription != null) {
                    serverConfiguration.addResultAdapterDescription(resultDescription);
                }
            }
        }
        return serverConfiguration;
    }
}
