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

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.commons.CommonsConstants;
import org.wso2.carbon.rulecep.commons.descriptions.PropertyDescription;
import org.wso2.carbon.rulecep.commons.descriptions.PropertyDescriptionFactory;
import org.wso2.carbon.rulecep.commons.descriptions.QNameFactory;
import org.wso2.carbon.rulecep.commons.descriptions.XPathFactory;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensionBuilder;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Factory for creating  <code>RuleSetDescription</code> from an XML configuration
 */
public class RuleSetDescriptionFactory {

    private static final Log log = LogFactory.getLog(RuleSetDescriptionFactory.class);

    /**
     * Creates a <code>RuleSetDescription</code>  instance from the given XML
     *
     * @param ruleSet          an XML representation of the <code>RuleSetDescription</code>
     * @param xPathFactory     to be used to create XPaths
     * @param extensionBuilder <code>ConfigurationExtensionBuilder</code> instance
     * @return Not null <code>RuleSetDescription</code> instance
     */
    public static RuleSetDescription create(OMElement ruleSet,
                                            XPathFactory xPathFactory,
                                            ExtensionBuilder extensionBuilder) {

        RuleSetDescription description = new RuleSetDescription();
        extensionBuilder.build(description, ruleSet, xPathFactory);
        QName parentQName = ruleSet.getQName();
        QNameFactory qNameFactory = QNameFactory.getInstance();
        QName creationQName = qNameFactory.createQName(CommonsConstants.ELE_CREATION,
                parentQName);
        QName registrationQName = qNameFactory.createQName(CommonsConstants.ELE_REGISTRATION,
                parentQName);
        QName deregistrationQName = qNameFactory.createQName(CommonsConstants.ELE_DEREGISTRATION,
                parentQName);

        String uri = ruleSet.getAttributeValue(CommonsConstants.ATT_URI_Q);
        if (uri != null) {
            description.setBindURI(uri.trim());
        }

        OMElement creation = ruleSet.getFirstChildWithName(creationQName);
        if (creation != null) {
            List<PropertyDescription> list =
                    PropertyDescriptionFactory.createPropertyDescriptionList(creation,
                            xPathFactory, null);
            for (PropertyDescription propertyDescription : list) {
                description.addCreationProperty(propertyDescription);
            }
        }

        OMElement registration = ruleSet.getFirstChildWithName(registrationQName);
        if (registration != null) {
            List<PropertyDescription> list =
                    PropertyDescriptionFactory.createPropertyDescriptionList(registration,
                            xPathFactory, null);
            for (PropertyDescription propertyDescription : list) {
                description.addRegistrationProperty(propertyDescription);
            }
        }

        OMElement deregistration = ruleSet.getFirstChildWithName(deregistrationQName);
        if (deregistration != null) {
            List<PropertyDescription> list =
                    PropertyDescriptionFactory.createPropertyDescriptionList(deregistration,
                            xPathFactory, null);
            for (PropertyDescription propertyDescription : list) {
                description.addDeregistrationProperty(propertyDescription);
            }
        }

        return description;
    }
}
