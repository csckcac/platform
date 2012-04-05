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
package org.wso2.carbon.rulecep.commons.descriptions;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;

/**
 * Serializes <code>PropertyDescription</code> into XML
 */
public class PropertyDescriptionSerializer {

    private static Log log = LogFactory.getLog(PropertyDescriptionSerializer.class);

    private static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();
    private static final OMNamespace NULL_NS
            = OM_FACTORY.createOMNamespace("", "");

    /**
     * Creates an XML representation of the given <code>PropertyDescription</code>
     *
     * @param property        <code>PropertyDescription</code> to be serialized
     * @param xPathSerializer serializer to serialize XPaths
     * @param parent          parent Namespace to be used to create required NameSpaces for
     *                        property tags
     * @return XML representation of the given <code>PropertyDescription</code>
     */
    public static OMElement serialize(PropertyDescription property,
                                      XPathSerializer xPathSerializer,
                                      OMNamespace parent) {
        OMNamespaceFactory omNamespaceFactory = OMNamespaceFactory.getInstance();
        OMNamespace OmNamespace = omNamespaceFactory.createOMNamespace(
                parent.getNamespaceURI(), parent.getPrefix());

        OMElement prop = OM_FACTORY.createOMElement("property", OmNamespace);
        if (property.getName() != null) {
            prop.addAttribute(OM_FACTORY.createOMAttribute("name", NULL_NS, property.getName()));
        } else {
            throw new LoggedRuntimeException("Property name missing", log);
        }

        if (property.getValue() != null) {
            prop.addAttribute(OM_FACTORY.createOMAttribute("value", NULL_NS, property.getValue()));

        } else if (property.getExpression() != null) {
            xPathSerializer.serializeXPath(property.getExpression(), prop, "expression");

        } else {
            throw new LoggedRuntimeException("Property must have a literal" +
                    " value or be an expression", log);
        }
        return prop;
    }


}
