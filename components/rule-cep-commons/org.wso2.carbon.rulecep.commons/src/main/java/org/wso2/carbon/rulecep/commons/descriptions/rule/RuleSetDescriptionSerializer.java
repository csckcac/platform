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
package org.wso2.carbon.rulecep.commons.descriptions.rule;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.commons.descriptions.PropertyDescription;
import org.wso2.carbon.rulecep.commons.descriptions.PropertyDescriptionSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.XPathSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensionSerializer;

import java.util.Collection;

/**
 * Serializes <code>RuleSetDescription</code> into an XML
 */
public class RuleSetDescriptionSerializer {

    private static Log log = LogFactory.getLog(RuleSetDescriptionSerializer.class);

    private static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();
    private static final OMNamespace NULL_NS
            = OM_FACTORY.createOMNamespace("", "");

    /**
     * Creates an XML for the given <code>RuleSetDescription</code>  instance
     * and adds to the parent OMElement as a child
     *
     * @param description         <code>RuleSetDescription</code>  to be mapped into XML
     * @param xPathSerializer     to be used to serialize XPaths
     * @param parent              the parent tag
     * @param extensionSerializer <code>ConfigurationExtensionSerializer</code>
     * @return parent OMElement with rule set as a child
     */
    public static OMElement serialize(RuleSetDescription description,
                                      XPathSerializer xPathSerializer,
                                      OMElement parent,
                                      ExtensionSerializer extensionSerializer) {

        OMNamespace omNamespace = parent.getNamespace();
        extensionSerializer.serialize(description, xPathSerializer, parent);
        String uri = description.getBindURI();
        if (uri != null && !"".equals(uri)) {
            parent.addAttribute(OM_FACTORY.createOMAttribute(
                    "uri", NULL_NS, uri.trim()));
        }

        addProperty(description.getCreationProperties(),
                parent, omNamespace,
                "creation", xPathSerializer);
        addProperty(description.getRegistrationProperties(),
                parent, omNamespace,
                "registration", xPathSerializer);
        addProperty(description.getDeregistrationProperties(),
                parent, omNamespace,
                "deregistration", xPathSerializer);

        return parent;

    }

    private static void addProperty(Collection<PropertyDescription> descriptions,
                                    OMElement parent,
                                    OMNamespace omNamespace,
                                    String name,
                                    XPathSerializer xPathSerializer) {

        if (!descriptions.isEmpty()) {

            OMElement creation = OM_FACTORY.createOMElement(name, omNamespace);
            for (PropertyDescription propertyDescription : descriptions) {
                if (propertyDescription != null) {
                    creation.addChild(PropertyDescriptionSerializer.serialize(
                            propertyDescription,
                            xPathSerializer, omNamespace));
                }
            }
            parent.addChild(creation);
        }
    }
}
