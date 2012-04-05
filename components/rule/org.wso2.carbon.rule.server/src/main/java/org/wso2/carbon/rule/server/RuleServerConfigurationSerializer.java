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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rule.core.RuleConstants;
import org.wso2.carbon.rulecep.commons.descriptions.OMNamespaceFactory;
import org.wso2.carbon.rulecep.commons.descriptions.PropertyDescription;
import org.wso2.carbon.rulecep.commons.descriptions.PropertyDescriptionSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.XPathSerializer;

import javax.xml.namespace.QName;
import java.util.Collection;

/**
 * Serialize a RuleServerConfiguration instance into a XML representation
 */
public class RuleServerConfigurationSerializer {

    private static Log log = LogFactory.getLog(RuleServerConfigurationSerializer.class);
    private static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();

    /**
     * Serializes the given RuleServerConfiguration instance into the XML representation
     *
     * @param configuration   the configuration to be serialized
     * @param xPathSerializer to be used to serialize XPaths in the given configuration
     * @return The XML representation of the RuleServerConfiguration
     */
    public OMElement serialize(RuleServerConfiguration configuration,
                               XPathSerializer xPathSerializer) {

        QName tagName = configuration.getQName();
        OMNamespaceFactory omNamespaceFactory = OMNamespaceFactory.getInstance();

        OMElement parent = OM_FACTORY.createOMElement("RuleServer",
                omNamespaceFactory.createOMNamespace(tagName));

        OMNamespace omNamespace = omNamespaceFactory.createOMNamespace(tagName);
        OMElement providerElement = OM_FACTORY.createOMElement(RuleConstants.ELE_PROVIDER,
                omNamespace);

        Collection<PropertyDescription> it =
                configuration.getProviderProperties();

        if (!it.isEmpty()) {
            for (PropertyDescription propertyDescription : it) {
                if (propertyDescription != null) {
                    providerElement.addChild(PropertyDescriptionSerializer.serialize(
                            propertyDescription,
                            xPathSerializer, omNamespace));
                }
            }
            parent.addChild(providerElement);
        }
        //TODO serialize adapters 
        return parent;
    }
}
