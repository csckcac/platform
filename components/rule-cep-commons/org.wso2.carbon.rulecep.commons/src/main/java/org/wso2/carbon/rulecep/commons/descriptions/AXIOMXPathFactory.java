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
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;
import org.wso2.carbon.rulecep.commons.utils.XPathHelper;

import javax.xml.namespace.QName;
import java.util.Collection;

/**
 * Creates <code>AXIOMXPath</code>s
 */
public class AXIOMXPathFactory implements XPathFactory {

    private static Log log = LogFactory.getLog(AXIOMXPathFactory.class);

    /**
     * Creates <code>AXIOMXPath</code> based on the expression located from the given OMElement
     * using the given attribute QName and the NameSpaces declared in the OMElement.
     *
     * @param element       OMElement Instance
     * @param attributeName Attribute name to get XPath expression
     * @return <code>AXIOMXPath</code> if there is a non-empty attribute in the provided OMElement
     *         for the provided attribute QName
     */
    public BaseXPath createXPath(OMElement element, QName attributeName) {
        OMAttribute omAttribute = element.getAttribute(attributeName);

        if (omAttribute != null && omAttribute.getAttributeValue() != null) {

            try {
                AXIOMXPath xpath = new AXIOMXPath(omAttribute.getAttributeValue());
                XPathHelper.addNameSpaces(xpath, element);
                return xpath;
            } catch (JaxenException e) {
                throw new LoggedRuntimeException("Invalid XPapth expression : " +
                        omAttribute.getAttributeValue(), e, log);
            }

        } else {
            throw new LoggedRuntimeException("Couldn't find the XPath attribute with the QName : "
                    + attributeName.toString() + " in the element : " + element.toString(), log);
        }
    }

    /**
     * Creates an <code>AXIOMXPath </code> instance from the given xpath expression and name spaces
     *
     * @param xpath        xpath expression
     * @param omNameSpaces name spaces
     * @return <code>AXIOMXPath</code> if the expression is valid
     */
    public BaseXPath createXPath(String xpath, Collection<OMNamespace> omNameSpaces) {

        if (xpath == null || "".equals(xpath)) {
            throw new LoggedRuntimeException("XPath expression is null or empty", log);
        }
        try {
            AXIOMXPath axiomxPath = new AXIOMXPath(xpath.trim());
            for (OMNamespace omNamespace : omNameSpaces) {
                if (omNamespace != null) {
                    axiomxPath.addNamespace(omNamespace.getPrefix(), omNamespace.getNamespaceURI());
                }
            }
            return axiomxPath;
        } catch (JaxenException e) {
            throw new LoggedRuntimeException("Invalid XPapth expression : " +
                    xpath, e, log);
        }
    }
}
