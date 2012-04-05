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
import org.jaxen.BaseXPath;

import javax.xml.namespace.QName;
import java.util.Collection;

/**
 * To create a XPath from given OMElement based on an attribute name. Xpath will be type of <code>BaseXPath</code>
 */
public interface XPathFactory {

    /**
     * @param element       OMElement Instance to be used to locate XPath expression and NameSpaces
     * @param attributeName Attribute name to get XPath expression
     * @return XPath instance as BaseXPath
     */
    BaseXPath createXPath(OMElement element, QName attributeName);

    /**
     * Create an XPath from the given xpath expression and name spaces
     *
     * @param xpath        xpath expression
     * @param omNameSpaces name spaces
     * @return XPath instance as <code>BaseXPath</code>
     */
    BaseXPath createXPath(String xpath, Collection<OMNamespace> omNameSpaces);
}
