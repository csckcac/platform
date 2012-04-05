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
package org.wso2.carbon.rulecep.commons.utils;

import org.apache.axiom.om.*;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Helper class to provide XPath evaluation needed within rule scripts.
 */
@SuppressWarnings("unused")
public class XPathHelper {

    private static Log log = LogFactory.getLog(XPathHelper.class);

    /**
     * Creates an <code>AXIOMXPath</code> and evaluates it on the given source
     *
     * @param source the source on which the XPath expression will evaluate
     * @param xPath  string representation of an XPath Expression
     * @return the resulted value from the XPath evaluation
     */
    public static Object evaluateAsAXIOMXPath(Object source, String xPath) {
        try {
            return evaluate(source, new AXIOMXPath(xPath));
        } catch (JaxenException e) {
            throw new LoggedRuntimeException("Error creating XPath " + xPath, log);
        }
    }

    /**
     * Return the object to be used for the variable value
     *
     * @param source     The source on which will be evaluated the XPath expression
     * @param expression XPath Expression
     * @return Return the OMNode to be used for the variable value
     */
    public static Object evaluate(Object source, BaseXPath expression) {

        try {
            Object result = expression.evaluate(source);
            if (result instanceof List && !((List) result).isEmpty()) {
                result = ((List) result).get(0);  // Always fetches *only* the first
            }
            if (result instanceof OMNode) {

                int nodeType = ((OMNode) result).getType();
                if (nodeType == OMNode.TEXT_NODE) {
                    return ((OMText) result).getText();
                }
                return result;
            } else {
                return result;
            }
        } catch (JaxenException e) {
            throw new LoggedRuntimeException("Error evaluating XPath " + expression +
                    " on message" + source, log);
        }
    }

    /**
     * Adds NameSpaces declared in the OMElement to the XPath
     *
     * @param xpath   Xpath which needs NameSpaces from the provided OMelement
     * @param element OMelement to be used to extract NameSpaces
     */
    public static void addNameSpaces(BaseXPath xpath, OMElement element) {

        Collection<OMNamespace> nameSpaces = extractNameSpaces(element);
        addNameSpaces(xpath, nameSpaces);
    }

    /**
     * Gets the NameSpaces in the element and it's all ancestors
     *
     * @param element an XML tag as OMElement
     * @return A collection of NameSpaces (<code>OMNamespace</code>)
     */
    public static Collection<OMNamespace> extractAllNameSpaces(OMElement element) {

        final Collection<OMNamespace> nameSpaces = new ArrayList<OMNamespace>();
        OMElement currentElem = element;
        while (currentElem != null) {
            Iterator it = currentElem.getAllDeclaredNamespaces();
            while (it.hasNext()) {
                Object ns = it.next();
                if (ns instanceof OMNamespace) {
                    nameSpaces.add((OMNamespace) ns);
                }
            }
            OMContainer parent = currentElem.getParent();
            //if the parent is a document element or parent is null ,then return
            if (parent == null || parent instanceof OMDocument) {
                return nameSpaces;
            }
            if (parent instanceof OMElement) {
                currentElem = (OMElement) parent;
            }
        }
        return nameSpaces;
    }

    /**
     * Gets the NameSpaces in the given element
     *
     * @param element an XML tag as OMElement
     * @return A collection of NameSpaces (<code>OMNamespace</code>)
     */
    public static Collection<OMNamespace> extractNameSpaces(OMElement element) {

        final Collection<OMNamespace> nameSpaces = new ArrayList<OMNamespace>();
        if (element == null) {
            return nameSpaces;
        }
        Iterator it = element.getAllDeclaredNamespaces();
        while (it.hasNext()) {
            Object ns = it.next();
            if (ns instanceof OMNamespace) {
                nameSpaces.add((OMNamespace) ns);
            }
        }
        return nameSpaces;
    }

    /**
     * Adds the NameSpaces of the provided collection of <code>OMNamespace</code>to the XPath
     *
     * @param xpath        XPath expression
     * @param omNameSpaces A collection of <code>OMNamespace</code>
     */
    public static void addNameSpaces(BaseXPath xpath, Collection<OMNamespace> omNameSpaces) {
        for (OMNamespace ns : omNameSpaces) {
            addNameSpaces(xpath, ns);
        }
    }

    /**
     * Adds the NameSpaces of the provided <code>OMNamespace</code>to the XPath
     *
     * @param xpath     an XPath expression as <code>BaseXPath</code>
     * @param namespace a <code>OMNamespace</code>
     */
    public static void addNameSpaces(BaseXPath xpath, OMNamespace namespace) {
        // Exclude the default namespace as explained in the Javadoc above
        if (namespace != null && !"".equals(namespace.getPrefix())) {

            try {
                xpath.addNamespace(namespace.getPrefix(), namespace.getNamespaceURI());
            } catch (JaxenException je) {
                throw new LoggedRuntimeException(
                        "Error adding declared name space with prefix : "
                                + namespace.getPrefix() + "and uri : " + namespace.getNamespaceURI()
                                + " to the XPath : " + xpath, je, log);
            }
        }
    }
}
