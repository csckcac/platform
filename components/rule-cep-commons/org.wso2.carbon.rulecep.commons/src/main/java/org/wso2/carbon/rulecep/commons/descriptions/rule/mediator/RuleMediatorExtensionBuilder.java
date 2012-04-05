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
import org.wso2.carbon.rulecep.commons.descriptions.XPathFactory;
import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescription;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensibleConfiguration;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensionBuilder;

/**
 * Base class for ConfigurationExtensionBuilder related to the RuleMediator
 */
public class RuleMediatorExtensionBuilder implements ExtensionBuilder {

    private static Log log = LogFactory.getLog(RuleMediatorExtensionBuilder.class);

    private final static MediatorRuleSetExtensionBuilder RULE_SET_EXTENSION_BUILDER
            = new MediatorRuleSetExtensionBuilder();

    private ExtensionBuilder getExtension(String type) {
        if (RuleSetDescription.TYPE.equals(type)) {
            return RULE_SET_EXTENSION_BUILDER;
        }
        if (log.isDebugEnabled()) {
            log.debug("There is no ConfigurationExtensionBuilder for the type " + type);
        }
        return null;
    }

    public void build(ExtensibleConfiguration extensibleConfiguration,
                      OMElement element, XPathFactory xPathFactory) {
        ExtensionBuilder configurationExtensionBuilder =
                getExtension(extensibleConfiguration.geType());
        if (configurationExtensionBuilder != null) {
            configurationExtensionBuilder.build(extensibleConfiguration, element, xPathFactory);
        }
    }
}
