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
import org.wso2.carbon.rulecep.commons.CommonsConstants;
import org.wso2.carbon.rulecep.commons.descriptions.OMNamespaceFactory;
import org.wso2.carbon.rulecep.commons.descriptions.PropertyDescription;
import org.wso2.carbon.rulecep.commons.descriptions.PropertyDescriptionSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.XPathSerializer;

import javax.xml.namespace.QName;
import java.util.Collection;

/**
 * Serializes <code>SessionDescription</code> into XML
 */
public class SessionDescriptionSerializer {

    private static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();

    private static final OMNamespace NULL_NS
            = OM_FACTORY.createOMNamespace("", "");

    /**
     * Creates an XML representation of the given <code>SessionDescription</code>
     *
     * @param description     <code>SessionDescription</code> to be serialized into XML
     * @param xPathSerializer to be used to serialize XPaths
     * @param parent          QName of the parent to be used to extractPayload name spaces and prefix
     * @return an XML representation of the given <code>SessionDescription</code>
     */
    public static OMElement serialize(SessionDescription description,
                                      XPathSerializer xPathSerializer,
                                      QName parent) {
        OMNamespaceFactory omNamespaceFactory = OMNamespaceFactory.getInstance();

        OMNamespace omNamespace = omNamespaceFactory.createOMNamespace(
                parent.getNamespaceURI(), parent.getPrefix());

        String sessionType = description.getSessionType();
        OMElement sessionElement = OM_FACTORY.createOMElement(CommonsConstants.ELE_SESSION,
                omNamespace);
        if (sessionType != null && !"".equals(sessionType)) {
            sessionElement.addAttribute(OM_FACTORY.createOMAttribute(
                    "type", NULL_NS, sessionType.trim()));

        }

        Collection<PropertyDescription> descriptions =
                description.getSessionProperties();

        if (!descriptions.isEmpty()) {
            for (PropertyDescription propertyDescription : descriptions) {
                if (propertyDescription != null) {
                    sessionElement.addChild(PropertyDescriptionSerializer.serialize(
                            propertyDescription,
                            xPathSerializer, omNamespace));
                }
            }
        }

        return sessionElement;
    }
}
