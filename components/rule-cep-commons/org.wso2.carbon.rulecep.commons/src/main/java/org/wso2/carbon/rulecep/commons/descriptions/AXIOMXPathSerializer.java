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
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.BaseXPath;
import org.wso2.carbon.rulecep.commons.CommonsConstants;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;

/**
 * Serializes <code>AXIOMXPath</coce>s
 */
public class AXIOMXPathSerializer implements XPathSerializer {

    private static Log log = LogFactory.getLog(AXIOMXPathSerializer.class);

    /**
     * Serializes the XPath string and NameSpaces on the provided target OMElement
     *
     * @param xpath         XPath to be serialized
     * @param element       Target OMElement
     * @param attributeName Target attribute name
     */
    public void serializeXPath(BaseXPath xpath, OMElement element, String attributeName) {
        OMNamespace nullNS = element.getOMFactory()
                .createOMNamespace(CommonsConstants.NULL_NAMESPACE, "");

        if (xpath != null) {

            element.addAttribute(element.getOMFactory().createOMAttribute(
                    attributeName, nullNS, xpath.toString()));

            serializeNameSpaces(element, xpath);

        } else {
            throw new LoggedRuntimeException("Couldn't find the xpath in the AXIOMXPath", log);
        }
    }

    private void serializeNameSpaces(OMElement elem, BaseXPath xpath) {
        if (xpath instanceof AXIOMXPath) {
            for (Object o : ((AXIOMXPath) xpath).getNamespaces().keySet()) {
                String prefix = (String) o;
                String uri = xpath.getNamespaceContext().translateNamespacePrefixToUri(prefix);
                elem.declareNamespace(uri, prefix);
            }
        }
    }
}
