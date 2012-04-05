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

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.commons.descriptions.XPathFactory;
import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescription;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensibleConfiguration;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensionBuilder;
import org.wso2.carbon.rulecep.commons.descriptions.service.ServiceDescription;

/**
 * Base class for ConfigurationExtensionBuilder related to the RuleService
 */
public class RuleServiceExtensionBuilder implements ExtensionBuilder {

    public static final String FILE_EXTENSION = "rsl";
    public static final String TARGET_NAMESPACE = "http://brs.carbon.wso2.org";
    public static final String TARGET_NAMESPACE_PREFIX = "brs";

    private static Log log = LogFactory.getLog(RuleServiceExtensionBuilder.class);

    private final static RuleServiceRuleSetExtensionBuilder RULE_SET_EXTENSION_BUILDER_RULE =
            new RuleServiceRuleSetExtensionBuilder();

    private ExtensionBuilder getExtension(String type) {
        if (RuleSetDescription.TYPE.equals(type)) {
            return RULE_SET_EXTENSION_BUILDER_RULE;
        }
        if (log.isDebugEnabled()) {
            log.debug("There is no ConfigurationExtensionBuilder for the type " + type);
        }
        return null;
    }

    public void build(ExtensibleConfiguration extensibleConfiguration,
                      OMElement element, XPathFactory xPathFactory) {

        if (ServiceDescription.TYPE.equals(extensibleConfiguration.geType())) {
            ServiceDescription serviceDescription = (ServiceDescription) extensibleConfiguration;
            RuleServiceExtensionDescription description =
                    RuleServiceExtensionDescriptionFactory.create(element, xPathFactory, this);
            serviceDescription.setServiceExtensionDescription(description);
            if (serviceDescription.getExtension() == null) {
                serviceDescription.setExtension(FILE_EXTENSION);
            }
            if (serviceDescription.getTargetNamespace() == null) {
                serviceDescription.setTargetNamespace(TARGET_NAMESPACE);
            }
            if (serviceDescription.getTargetNSPrefix() == null) {
                serviceDescription.setTargetNSPrefix(TARGET_NAMESPACE_PREFIX);
            }
        } else {
            // go for children
            ExtensionBuilder builder = getExtension(
                    extensibleConfiguration.geType());
            if (builder != null) {
                builder.build(extensibleConfiguration, element, xPathFactory);
            }
        }
    }
}
