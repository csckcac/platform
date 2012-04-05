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
package org.wso2.carbon.rulecep.commons.descriptions.rule.service;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.commons.CommonsConstants;
import org.wso2.carbon.rulecep.commons.descriptions.OMNamespaceFactory;
import org.wso2.carbon.rulecep.commons.descriptions.XPathSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescriptionSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensibleConfiguration;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensionSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.service.ServiceDescription;

/**
 * Base class for ConfigurationExtensionSerializer related to the RuleService
 */
public class RuleServiceExtensionSerializer implements ExtensionSerializer {

    private Log log = LogFactory.getLog(RuleServiceExtensionSerializer.class);

    private final static RuleServiceRuleSetExtensionSerializer EXTENSION_SERIALIZER_RULE
            = new RuleServiceRuleSetExtensionSerializer();
    private static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();

    private ExtensionSerializer getExtension(String type) {
        if (RuleSetDescription.TYPE.equals(type)) {
            return EXTENSION_SERIALIZER_RULE;
        }
        if (log.isDebugEnabled()) {
            log.debug("There is no ConfigurationExtensionSerializer for the type " + type);
        }
        return null;
    }

    public OMElement serialize(ExtensibleConfiguration configuration,
                               XPathSerializer xPathSerializer, OMElement parent) {

        if (ServiceDescription.TYPE.equals(configuration.geType())) {

            ServiceDescription serviceDescription = (ServiceDescription) configuration;
            RuleServiceExtensionDescription extensionDescription =
                    (RuleServiceExtensionDescription)
                            serviceDescription.getServiceExtensionDescription();
            RuleSetDescription ruleSetDescription = extensionDescription.getRuleSetDescription();
            OMNamespaceFactory omNamespaceFactory = OMNamespaceFactory.getInstance();
            OMNamespace omNamespace = omNamespaceFactory.createOMNamespace(
                    parent.getNamespace().getNamespaceURI(), "");
            if (ruleSetDescription != null) {
                OMElement ruleSetElement = OM_FACTORY.createOMElement(CommonsConstants.ELE_RULESET,
                        omNamespace);
                RuleSetDescriptionSerializer.serialize(ruleSetDescription, xPathSerializer,
                        ruleSetElement, this);
                parent.addChild(ruleSetElement);
            }
            return parent;

        } else {
            ExtensionSerializer serializer = getExtension(configuration.geType());

            if (serializer == null) {
                return parent;
            }
            return serializer.serialize(configuration, xPathSerializer, parent);
        }
    }
}

