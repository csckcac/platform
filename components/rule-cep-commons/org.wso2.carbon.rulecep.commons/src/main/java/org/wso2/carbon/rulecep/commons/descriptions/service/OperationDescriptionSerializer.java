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
package org.wso2.carbon.rulecep.commons.descriptions.service;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.commons.CommonsConstants;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;
import org.wso2.carbon.rulecep.commons.descriptions.*;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.List;

/**
 * Serializes <code>OperationDescription</code> into different XML representations
 */
public class OperationDescriptionSerializer {

    private static Log log = LogFactory.getLog(OperationDescriptionSerializer.class);

    private static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();
    private static final OMNamespace NULL_NS
            = OM_FACTORY.createOMNamespace("", "");

    /**
     * Serialize an instance of <code>OperationDescription</code> into an OMElement representing
     * operations in the axis2 service.xml
     *
     * @param description     <code>OperationDescription</code>  to be used to generate XML
     * @param parent          <code>OMElement </code> parent OM Element
     * @param xPathSerializer to be used to serialize XPaths
     * @return An OMElement representing operations in the axis2 service.xml
     */
    public static OMElement serializeToServiceXML(OMElement parent,
                                                  OperationDescription description,
                                                  XPathSerializer xPathSerializer) {
        OMElement opElement;
        QNameFactory qNameFactory = QNameFactory.getInstance();
        if (parent != null) {
            opElement = parent;
        } else {
            QName name = description.getName();
            if (name != null) {
                OMNamespaceFactory omNamespaceFactory = OMNamespaceFactory.getInstance();
                opElement = OM_FACTORY.createOMElement(CommonsConstants.ELE_OPERATION, NULL_NS);
                opElement.addAttribute(OM_FACTORY.createOMAttribute("name",
                        omNamespaceFactory.createOMNamespace(name), name.getLocalPart()));
            } else {
                throw new LoggedRuntimeException("Operation name missing", log);
            }
        }

        if (description.isForceInOnly()) {
            opElement.addAttribute(OM_FACTORY.createOMAttribute("mep",
                    NULL_NS, WSDL2Constants.MEP_URI_IN_ONLY));
        } else {
            if (!description.isContainsResult()) {
                opElement.addAttribute(OM_FACTORY.createOMAttribute("mep",
                        NULL_NS, WSDL2Constants.MEP_URI_IN_ONLY));
            } else if (description.isContainsFact()) {
                opElement.addAttribute(OM_FACTORY.createOMAttribute("mep",
                        NULL_NS, WSDL2Constants.MEP_URI_IN_OUT));
            }
        }

        return opElement;
    }

    /**
     * Serialize an instance of <code>OperationDescription</code> into an OMElement representing
     * operations in the rule-service.conf
     *
     * @param description         <code>OperationDescription</code>  to be used to generate XML
     * @param parent              <code>OmNamespace</code> providing namespaces and prefix
     * @param extensionSerializer <code>ExtensionSerializer</code>
     * @param xPathSerializer     to be used to serialize XPaths
     * @return An OMElement representing operations in the rule-service.conf
     */
    public static OMElement serializeToRuleServiceConfiguration(OperationDescription description,
                                                                XPathSerializer xPathSerializer,
                                                                OMNamespace parent,
                                                                ExtensionSerializer extensionSerializer) {

        OMNamespaceFactory omNamespaceFactory = OMNamespaceFactory.getInstance();
        OMNamespace OmNamespace = omNamespaceFactory.createOMNamespace(
                parent.getNamespaceURI(), "");

        OMElement opElement = OM_FACTORY.createOMElement(CommonsConstants.ELE_OPERATION, OmNamespace);
        QName name = description.getName();
        if (name != null) {
            opElement.addAttribute(OM_FACTORY.createOMAttribute("name",
                    omNamespaceFactory.createOMNamespace(name), name.getLocalPart()));
        } else {
            throw new LoggedRuntimeException("Operation name missing", log);
        }
        extensionSerializer.serialize(description, xPathSerializer, opElement);
        serialize(description, xPathSerializer, opElement);

        return opElement;
    }

    /**
     * Serialize facts and results of an <code>OperationDescription</code> into an OMElement
     *
     * @param description     <code>OperationDescription</code>  to be used to generate XML
     * @param parent          <code>OMElement </code> parent OM Element
     * @param xPathSerializer to be used to serialize XPaths
     */
    private static void serialize(OperationDescription description,
                                  XPathSerializer xPathSerializer,
                                  OMElement parent) {

        OMNamespaceFactory omNamespaceFactory = OMNamespaceFactory.getInstance();
        OMNamespace omNamespace = omNamespaceFactory.createOMNamespace(parent.getQName());

        QNameFactory qNameFactory = QNameFactory.getInstance();
        QName factQName = qNameFactory.createQName(CommonsConstants.ELE_WITH_PARAM, omNamespace);

        if (description.isContainsFact()) {
            List<ResourceDescription> facts = description.getFactDescriptions();
            for (ResourceDescription resourceDescription : facts) {
                OMElement resourceElement =
                        ResourceDescriptionSerializer.serialize(
                                resourceDescription, factQName, xPathSerializer);
                if (resourceElement != null) {
                    parent.addChild(resourceElement);
                }
            }
        }

        QName elementQName = qNameFactory.createQName(CommonsConstants.ELE_ELEMENT, omNamespace);
        if (description.isContainsResult()) {
            ResourceDescription firstDescription = description.getResultDescriptions().get(0);
            if (!"omelement".equals(firstDescription.getType())) { //todo
                throw new LoggedRuntimeException("Invalid result !! result type is invalid", log);
            }
            OMElement resultElement = OM_FACTORY.createOMElement(CommonsConstants.ELE_RESULT,
                    omNamespace);
            String name = firstDescription.getName();
            if (name != null && !"".equals(name)) {
                resultElement.addAttribute(OM_FACTORY.createOMAttribute("name",
                        omNamespaceFactory.createOMNamespace(new QName(name)), name));
            }
            Collection<ResourceDescription> results = firstDescription.getChildResources();
            for (ResourceDescription resourceDescription : results) {
                OMElement resourceElement =
                        ResourceDescriptionSerializer.serialize(
                                resourceDescription, elementQName, xPathSerializer);
                if (resourceElement != null) {
                    resultElement.addChild(resourceElement);
                }
            }
            parent.addChild(resultElement);
        }
    }
}
