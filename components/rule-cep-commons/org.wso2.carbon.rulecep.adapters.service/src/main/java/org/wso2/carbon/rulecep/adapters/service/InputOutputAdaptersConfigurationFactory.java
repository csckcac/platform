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
package org.wso2.carbon.rulecep.adapters.service;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.adapters.AdaptersConstants;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;
import org.wso2.carbon.rulecep.commons.descriptions.QNameFactory;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescriptionFactory;
import org.wso2.carbon.rulecep.commons.descriptions.XPathFactory;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * Create <code>InputOutputAdaptersConfiguration</code> instances
 */
public class InputOutputAdaptersConfigurationFactory {

    private static final Log log = LogFactory.getLog(InputOutputAdaptersConfigurationFactory.class);

    public static InputOutputAdaptersConfiguration create(OMElement configurationXML,
                                                          XPathFactory xPathFactory) {

        if (configurationXML == null) {
            throw new LoggedRuntimeException("Invalid  configuration. " +
                    "The configuration cannot be null.", log);
        }

        InputOutputAdaptersConfiguration configuration = new InputOutputAdaptersConfiguration();
        QName tagQName = configurationXML.getQName();
        QNameFactory qNameFactory = QNameFactory.getInstance();
        // registers fact adapters
        QName factAdapterQName = qNameFactory.createQName(AdaptersConstants.INPUT_ADAPTER, tagQName);
        QName adapterQName = qNameFactory.createQName(AdaptersConstants.INPUT_ADAPTER, tagQName);
        OMElement factAdapterElement = configurationXML.getFirstChildWithName(factAdapterQName);
        if (factAdapterElement != null) {
            Iterator factAdapters = factAdapterElement.getChildrenWithName(adapterQName);
            while (factAdapters.hasNext()) {
                OMElement child = (OMElement) factAdapters.next();
                ResourceDescription factDescription =
                        ResourceDescriptionFactory.createResourceDescription(child, xPathFactory);
                if (factDescription != null) {
                    configuration.addFactAdapterDescription(factDescription);
                }
            }
        }
        // registers results adapters
        QName resultAdapterQName = qNameFactory.createQName(AdaptersConstants.OUTPUT_ADAPTER,
                tagQName);
        OMElement resultAdapterElement = configurationXML.getFirstChildWithName(resultAdapterQName);
        if (resultAdapterElement != null) {
            Iterator resultAdapters = resultAdapterElement.getChildrenWithName(adapterQName);
            while (resultAdapters.hasNext()) {
                OMElement child = (OMElement) resultAdapters.next();
                ResourceDescription resultDescription =
                        ResourceDescriptionFactory.createResourceDescription(child, xPathFactory);
                if (resultDescription != null) {
                    configuration.addResultAdapterDescription(resultDescription);
                }
            }
        }
        return configuration;
    }
}
