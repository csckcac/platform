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

import org.apache.axiom.om.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.BaseXPath;
import org.wso2.carbon.rulecep.commons.CommonsConstants;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;

import javax.xml.namespace.QName;
import java.util.Collection;

/**
 * Serializes <code>ResourceDescription</code> into an XML
 */
public class ResourceDescriptionSerializer {

    private static final Log log = LogFactory.getLog(ResourceDescriptionSerializer.class);

    private static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();

    private static final OMNamespace NULL_NS
            = OM_FACTORY.createOMNamespace(CommonsConstants.NULL_NAMESPACE, "");

    /**
     * Takes a ResourceDescription and return the XML representation of it
     *
     * @param description     The ResourceDescription instance
     * @param root            The root tag name
     * @param xPathSerializer XPathSerializer implementation
     * @return The OMElement of Resource Description
     */
    public static OMElement serialize(ResourceDescription description,
                                      QName root,
                                      XPathSerializer xPathSerializer) {

        if (description == null) {
            throw new LoggedRuntimeException("Invalid Resource description !! ." +
                    " The Resource description is null.", log);
        }

        String id = description.getName();
        OMElement resourceElem = OM_FACTORY.createOMElement(root.getLocalPart(),
                OM_FACTORY.createOMNamespace(root.getNamespaceURI(), root.getPrefix()));
        if (id != null) {
            resourceElem.addAttribute(OM_FACTORY.createOMAttribute(
                    "name", NULL_NS, id));
        }

        Object value = description.getValue();
        String key = description.getKey();

        if (value != null && !"".equals(value)) {

            if (value instanceof String) {
                resourceElem.addAttribute(OM_FACTORY.createOMAttribute(
                        "value", NULL_NS, (String) value));
            } else if (value instanceof OMElement) {
                resourceElem.addChild((OMNode) value);
            }
        } else {
            if (key != null && !"".equals(key)) {
                resourceElem.addAttribute(OM_FACTORY.createOMAttribute(
                        "key", NULL_NS, key));
            } else {
                BaseXPath expression = description.getExpression();
                if (expression != null && !"".equals(expression.toString())) {
                    xPathSerializer.serializeXPath(expression, resourceElem, "expression");
                }
            }
        }

        String type = description.getType();
        if (type != null && !"".equals(type)) {
            resourceElem.addAttribute(OM_FACTORY.createOMAttribute(
                    "type", NULL_NS, type));
        }

        Collection<OMNamespace> namespaces = description.getNameSpaces();
        for (OMNamespace omNamespace : namespaces) {
            if (omNamespace == null) {
                continue;
            }
            resourceElem.declareNamespace(omNamespace);
        }

        return resourceElem;
    }
}
