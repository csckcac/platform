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

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.commons.descriptions.XPathSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescription;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensibleConfiguration;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensionSerializer;

/**
 * Base class for ConfigurationExtensionSerializer related to the RuleMediator
 */
public class RuleMediatorExtensionSerializer implements ExtensionSerializer {

    private static Log log = LogFactory.getLog(RuleMediatorExtensionSerializer.class);
    private final static MediatorRuleSetExtensionSerializer RULE_SET_EXTENSION_SERIALIZER
            = new MediatorRuleSetExtensionSerializer();

    private ExtensionSerializer getExtension(String type) {
        if (RuleSetDescription.TYPE.equals(type)) {
            return RULE_SET_EXTENSION_SERIALIZER;
        }
        if (log.isDebugEnabled()) {
            log.debug("There is no ConfigurationExtensionSerializer for the type " + type);
        }
        return null;
    }

    public OMElement serialize(ExtensibleConfiguration configuration,
                               XPathSerializer xPathSerializer, OMElement parent) {
        ExtensionSerializer serializerConfiguration = getExtension(configuration.geType());
        if (serializerConfiguration == null) {
            return parent;
        }
        return serializerConfiguration.serialize(configuration, xPathSerializer, parent);
    }
}

