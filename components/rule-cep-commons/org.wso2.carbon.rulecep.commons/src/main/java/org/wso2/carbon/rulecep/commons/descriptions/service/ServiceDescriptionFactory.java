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

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.rulecep.commons.CommonsConstants;
import org.wso2.carbon.rulecep.commons.descriptions.QNameFactory;
import org.wso2.carbon.rulecep.commons.descriptions.XPathFactory;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Factory to create <code>ServiceDescription</code> from XML
 */
public class ServiceDescriptionFactory {

    /**
     * Creates a <code>ServiceDescription</code> based on the given XML representing
     * <code>ServiceDescription</code> configurations
     *
     * @param configuration    Configuration XML to be used to create <code>ServiceDescription</code>
     * @param xPathFactory     to be used to create XPaths
     * @param extensionBuilder <code>ExtensionBuilderAndFactory</code> instance
     * @return <code>ServiceDescription</code> instance
     */
    public static ServiceDescription create(OMElement configuration,
                                            XPathFactory xPathFactory,
                                            ExtensionBuilder extensionBuilder) {
        // Service name
        String name = configuration.getAttributeValue(CommonsConstants.ATT_NAME_Q);
        String tns = configuration.getAttributeValue(CommonsConstants.ATT_TARGET_NAMESPACE_Q);
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setName(name);
        if (tns != null && !"".equals(tns)) {
            serviceDescription.setTargetNamespace(tns);
        }

        serviceDescription.setContainsServicesXML(
                Boolean.parseBoolean(
                        configuration.getAttributeValue(CommonsConstants.ATT_GENERATE_SERVICES_XML)));

        QName parentQName = configuration.getQName();
        QNameFactory qNameFactory = QNameFactory.getInstance();

        // description
        QName descriptionQName = qNameFactory.createQName(CommonsConstants.ELE_DESCRIPTION,
                parentQName);
        OMElement descriptionElement = configuration.getFirstChildWithName(descriptionQName);
        if (descriptionElement != null) {
            String description = descriptionElement.getText();
            if (description != null) {
                serviceDescription.setDescription(description.trim());
            }
        }
        extensionBuilder.build(serviceDescription, configuration, xPathFactory);
        List<OperationDescription> descriptions =
                OperationDescriptionListFactory.create(configuration, xPathFactory, extensionBuilder);
        for (OperationDescription description : descriptions) {
            serviceDescription.addRuleServiceOperationDescription(description);
        }
        String extension = configuration.getAttributeValue(new QName(CommonsConstants.NULL_NAMESPACE,
                CommonsConstants.ATT_EXTENSION));
        if (extension != null) {
            serviceDescription.setExtension(extension);
        }
        return serviceDescription;

    }
}
