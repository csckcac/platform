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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.BaseXPath;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Factory for creating PropertyDescriptions
 */
public class PropertyDescriptionFactory {

    private static final Log log = LogFactory.getLog(PropertyDescriptionFactory.class);

    /**
     * Creates a list of <code>PropertyDescription</code> from the provided OMElement
     *
     * @param element       source OMElement containing properties as configurations
     * @param xPathFactory  the factory to create XPaths
     * @param propertyQName The QName of the property tag. If this is null ,
     *                      the default QName is used. (i.e property)
     * @return a list of <code>PropertyDescription</code>
     */
    public static List<PropertyDescription> createPropertyDescriptionList(OMElement element,
                                                                          XPathFactory xPathFactory,
                                                                          QName propertyQName) {

        final List<PropertyDescription> propertyList = new ArrayList<PropertyDescription>();
        if (propertyQName == null) {
            propertyQName = PropertyDescription.PROPERTY_Q;
        }
        Iterator iterator = element.getChildrenWithName(propertyQName);

        while (iterator.hasNext()) {

            OMElement propEle = (OMElement) iterator.next();
            OMAttribute attName = propEle.getAttribute(PropertyDescription.ATT_NAME_Q);
            OMAttribute attValue = propEle.getAttribute(PropertyDescription.ATT_VALUE_Q);
            OMAttribute attExpr = propEle.getAttribute(PropertyDescription.ATT_EXPR_Q);

            PropertyDescription prop = new PropertyDescription();

            if (attName == null || attName.getAttributeValue() == null ||
                    attName.getAttributeValue().trim().length() == 0) {
                throw new LoggedRuntimeException("Name is a required attribute for a " +
                        "property", log);
            } else {
                prop.setName(attName.getAttributeValue());
            }

            // if a value is specified, use it, else look for an expression
            if (attValue != null) {

                if (attValue.getAttributeValue() == null ||
                        attValue.getAttributeValue().trim().length() == 0) {

                    throw new LoggedRuntimeException("Attribute value (if specified) " +
                            "is required for a property", log);

                } else {
                    prop.setValue(attValue.getAttributeValue());
                }

            } else if (attExpr != null) {

                if (attExpr.getAttributeValue() == null ||
                        attExpr.getAttributeValue().trim().length() == 0) {

                    throw new LoggedRuntimeException("Attribute expression (if specified) " +
                            "is required for a property", log);

                } else {
                    BaseXPath xpath = xPathFactory.createXPath(propEle,
                            PropertyDescription.ATT_EXPR_Q);
                    prop.setExpression(xpath);
                }

            } else if (propEle.getFirstElement() != null) {
                prop.setValue(propEle.getFirstElement().toString());
            } else {
                throw new LoggedRuntimeException(
                        "Attribute value OR expression must " +
                                "be specified for a property", log);
            }

            propertyList.add(prop);
        }

        return propertyList;
    }
}
