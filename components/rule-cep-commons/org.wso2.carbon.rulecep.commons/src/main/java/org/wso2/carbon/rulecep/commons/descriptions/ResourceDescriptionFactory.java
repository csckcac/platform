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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.BaseXPath;
import org.wso2.carbon.rulecep.commons.CommonsConstants;
import org.wso2.carbon.rulecep.commons.utils.XPathHelper;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Iterator;

/**
 * Factory for creating  <code>ResourceDescription</code>
 */
public class ResourceDescriptionFactory {

    private final static Log log = LogFactory.getLog(ResourceDescriptionFactory.class);

    /**
     * Takes an OMElement and returns an instance of the ResourceDescription
     *
     * @param element      The OMElement to be used as source for locating
     *                     <code>ResourceDescription</code>
     * @param xPathFactory XPathFactory implementation
     * @return The instance of ResourceDescription
     */
    public static ResourceDescription createResourceDescription(OMElement element,
                                                                XPathFactory xPathFactory) {

        String name = element.getAttributeValue(CommonsConstants.ATT_NAME_Q);
        ResourceDescription description = new ResourceDescription();
        description.setName(name);

        String type = element.getAttributeValue(CommonsConstants.ATT_TYPE_Q);
        if (type != null) {
            description.setType(type);
        }

        String key = element.getAttributeValue(CommonsConstants.ATT_KEY_Q);
        String value = element.getAttributeValue(CommonsConstants.ATT_VALUE_Q);

        if (value != null && !"".equals(value)) {
            description.setValue(value);
        } else {
            if (key != null && !"".equals(key)) {
                description.setKey(key);
            } else {
                String expression = element.getAttributeValue(CommonsConstants.ATT_EXPR_Q);
                if (expression != null && !"".equals(expression)) {
                    BaseXPath xp = xPathFactory.createXPath(element, CommonsConstants.ATT_EXPR_Q);
                    description.setExpression(xp);
                } else {
                    if ("message".equals(type)) {// TODO fix - this is the type message fact adapter
                        Collection<OMNamespace> namespaces = XPathHelper.extractNameSpaces(element);
                        description.addNameSpaces(namespaces);
                    }
                }
            }
        }

        if (description.getValue() == null &&
                description.getKey() == null &&
                description.getExpression() == null) {
            OMElement child = element.getFirstElement();
            if (child != null) {
                description.setValue(child);
            }
        }
        description.setXPathFactory(xPathFactory);
        QName parentQName = element.getQName();
        QNameFactory qNameFactory = QNameFactory.getInstance();

        QName parameterQName = qNameFactory.createQName(CommonsConstants.ELE_PARAMETER,
                parentQName);
        Iterator children = element.getChildrenWithName(parameterQName);
        while (children.hasNext()) {
            OMElement childElement = (OMElement) children.next();
            if (childElement == null) {
                continue;
            }
            ResourceDescription parameter =
                    ResourceDescriptionFactory.createResourceDescription(childElement,
                            xPathFactory);
            if (parameter != null) {
                description.addChildResource(parameter);
            }
        }
        return description;
    }

}
