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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.commons.CommonsConstants;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;
import org.wso2.carbon.rulecep.commons.descriptions.OMNamespaceFactory;
import org.wso2.carbon.rulecep.commons.descriptions.QNameFactory;
import org.wso2.carbon.rulecep.commons.descriptions.XPathSerializer;
import org.wso2.carbon.rulecep.commons.utils.OMElementHelper;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Serializes <code>ServiceDescription</code> into different XML representions
 */
public class ServiceDescriptionSerializer {

    private static Log log = LogFactory.getLog(ServiceDescriptionSerializer.class);

    private static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();
    private static final OMNamespace NULL_NS
            = OM_FACTORY.createOMNamespace("", "");

    /**
     * Creates an XML representation of the axis2 service XML from the given <code>ServiceDescription</code>
     *
     * @param description     <code>ServiceDescription</code> to be used to generate XML
     * @param parent          <code>OmNamespace</code> providing namespaces and prefix
     * @param xPathSerializer to be used to serialize XPaths
     * @return An OMElement representing axis2 service XML
     */
    public static OMElement serializeToServiceXML(ServiceDescription description,
                                                  OMElement parent, XPathSerializer xPathSerializer) {

        OMElement serviceElement;
        QNameFactory qNameFactory = QNameFactory.getInstance();
        OMNamespaceFactory omNamespaceFactory = OMNamespaceFactory.getInstance();
        OMElementHelper omElementHelper = OMElementHelper.getInstance();
        if (parent != null) {
            // removes existing rule config information in the service.xml
            serviceElement = parent;
            Iterator iterator = serviceElement.getChildElements();
            while (iterator.hasNext()) {
                Object o = iterator.next();
                if (!(o instanceof OMElement)) {
                    continue;
                }
                OMElement child = (OMElement) o;
                if (CommonsConstants.ELE_DESCRIPTION.equals(child.getLocalName())) {
                    omElementHelper.detachChildren(child);
                    String des = description.getDescription();
                    if (des != null && !"".equals(des.trim())) {
                        child.setText(des);
                    }
                }
            }
        } else {
            String name = description.getName();
            if (name != null && !"".equals(name.trim())) {
                serviceElement = OM_FACTORY.createOMElement(CommonsConstants.ELE_SERVICE,
                        omNamespaceFactory.createOMNamespace(new QName(name)));
                serviceElement.addAttribute(OM_FACTORY.createOMAttribute("name", NULL_NS,
                        name.trim()));
            } else {
                throw new LoggedRuntimeException("Service name missing", log);
            }
            // set description
            OMNamespace omNamespace = omNamespaceFactory.createOMNamespace(
                    serviceElement.getQName());

            String des = description.getDescription();
            if (des != null && !"".equals(des.trim())) {
                OMElement desElement = OM_FACTORY.createOMElement(CommonsConstants.ELE_DESCRIPTION,
                        omNamespace);
                desElement.setText(des.trim());
                serviceElement.addChild(desElement);
            }
        }

        // updating existing operations
        final List<String> addedOpNames = new ArrayList<String>();
        Iterator iterator = serviceElement.getChildrenWithName(
                qNameFactory.createQName(CommonsConstants.ELE_OPERATION, serviceElement.getQName()));
        while (iterator.hasNext()) {
            Object o = iterator.next();
            if (!(o instanceof OMElement)) {
                continue;
            }
            OMElement opElement = (OMElement) o;
            String name = opElement.getAttributeValue(new QName("name"));
            OperationDescription operationDescription =
                    description.getRuleServiceOperationDescription(name);
            if (operationDescription != null) {
                OperationDescriptionSerializer.serializeToServiceXML(
                        opElement, operationDescription, xPathSerializer);
                addedOpNames.add(name);
            } else {
                opElement.detach();
            }
        }
        // adding new operations
        Iterator<OperationDescription> operations = description.getOperationDescriptions();
        while (operations.hasNext()) {
            OperationDescription operationDescription = operations.next();
            if (operationDescription == null) {
                continue;
            }
            if (!addedOpNames.contains(operationDescription.getName().getLocalPart())) {
                OMElement opElement =
                        OperationDescriptionSerializer.serializeToServiceXML(
                                null, operationDescription, xPathSerializer);
                if (opElement != null) {
                    serviceElement.addChild(opElement);
                }
            }
        }

        return serviceElement;
    }

    /**
     * Serialize an instance of <code>ServiceDescription</code> into an OMElement representing
     * rule-service.conf
     *
     * @param description         <code>ServiceDescription</code> to be used to generate XML
     * @param parent              <code>OmNamespace</code> providing namespaces and prefix
     * @param xPathSerializer     to be used to serialize XPaths
     * @param extensionSerializer <code>ExtensionSerializer</code>
     * @return An OMElement representing rule-service.conf
     */
    public static OMElement serializeToRuleServiceConfiguration(ServiceDescription description,
                                                                OMNamespace parent,
                                                                XPathSerializer xPathSerializer,
                                                                ExtensionSerializer extensionSerializer) {

        OMNamespaceFactory omNamespaceFactory = OMNamespaceFactory.getInstance();
        OMNamespace omNamespace = omNamespaceFactory.createOMNamespace(
                parent.getNamespaceURI(), "");

        OMElement serviceElement = OM_FACTORY.createOMElement(CommonsConstants.ELE_RULE_SERVICE,
                omNamespace);
        String name = description.getName();
        if (name != null && !"".equals(name.trim())) {
            serviceElement.addAttribute(OM_FACTORY.createOMAttribute("name", NULL_NS, name.trim()));
        } else {
            throw new LoggedRuntimeException("Service name missing", log);
        }

        String tns = description.getTargetNamespace();
        if (tns != null && !"".equals(tns)) {
            serviceElement.addAttribute(OM_FACTORY.createOMAttribute(
                    CommonsConstants.ATT_TARGET_NAMESPACE_Q.getLocalPart(), NULL_NS, tns.trim()));
        }
        String des = description.getDescription();
        if (des != null && !"".equals(des.trim())) {
            OMElement desElement = OM_FACTORY.createOMElement(CommonsConstants.ELE_DESCRIPTION,
                    omNamespace);
            desElement.setText(des.trim());
            serviceElement.addChild(desElement);
        }
        extensionSerializer.serialize(description, xPathSerializer, serviceElement);
        Iterator<OperationDescription> iterator = description.getOperationDescriptions();
        while (iterator.hasNext()) {
            OperationDescription operationDescription = iterator.next();
            if (operationDescription != null) {
                OMElement opElement =
                        OperationDescriptionSerializer.
                                serializeToRuleServiceConfiguration(
                                        operationDescription,
                                        xPathSerializer,
                                        parent,
                                        extensionSerializer);
                if (opElement != null) {
                    serviceElement.addChild(opElement);
                }
            }
        }
        return serviceElement;
    }
}
