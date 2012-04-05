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
package org.wso2.carbon.rulecep.commons.descriptions.cep;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.commons.CommonsConstants;
import org.wso2.carbon.rulecep.commons.descriptions.QNameFactory;
import org.wso2.carbon.rulecep.commons.descriptions.XPathFactory;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensibleConfiguration;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensionBuilder;
import org.wso2.carbon.rulecep.commons.descriptions.service.OperationDescription;
import org.wso2.carbon.rulecep.commons.descriptions.service.ServiceDescription;

import javax.xml.namespace.QName;

/**
 * Build extensions of the CEP services(i.e service , operation configuration extensions)
 */
public class CEPServiceExtensionBuilder implements ExtensionBuilder {

    public static final String FILE_EXTENSION = "csl";
    public static final String TARGET_NAMESPACE = "http://es.carbon.wso2.org";
    public static final String TARGET_NAMESPACE_PREFIX = "es";

    private static Log log = LogFactory.getLog(CEPServiceExtensionBuilder.class);

    public void build(ExtensibleConfiguration extensibleConfiguration,
                      OMElement element, XPathFactory xPathFactory) {

        if (ServiceDescription.TYPE.equals(extensibleConfiguration.geType())) {

            ServiceDescription serviceDescription = (ServiceDescription) extensibleConfiguration;
            QName parentQName = element.getQName();
            QNameFactory qNameFactory = QNameFactory.getInstance();

            // rule set
            QName parameterQName = qNameFactory.createQName(CommonsConstants.ELE_QUERY,
                    parentQName);
            OMElement parameterElem = element.getFirstChildWithName(parameterQName);
            if (parameterElem == null) {
                return;
            }
            QueryDescription description =
                    QueryDescriptionFactory.create(parameterElem, xPathFactory);
            serviceDescription.setServiceExtensionDescription(description);
            if (serviceDescription.getExtension() == null) {
                serviceDescription.setExtension(FILE_EXTENSION);
            }
            if (serviceDescription.getTargetNamespace() == null) {
                serviceDescription.setTargetNamespace(TARGET_NAMESPACE);
            }
            if (serviceDescription.getTargetNSPrefix() == null) {
                serviceDescription.setTargetNSPrefix(TARGET_NAMESPACE_PREFIX);
            }
        } else if (OperationDescription.TYPE.equals(extensibleConfiguration.geType())) {

            OperationDescription operationDescription =
                    (OperationDescription) extensibleConfiguration;
            operationDescription.setForceInOnly(true);
            CEPOperationExtensionDescription extensionDescription =
                    new CEPOperationExtensionDescription();

            QName parentQName = element.getQName();
            QNameFactory qNameFactory = QNameFactory.getInstance();

            QName inStreamQName = qNameFactory.createQName(CommonsConstants.ELE_INPUT_EVENT_STREAM,
                    parentQName);
            OMElement inStreamElement = element.getFirstChildWithName(inStreamQName);
            if (inStreamElement != null) {
                EventStreamDescription streamDescription =
                        EventStreamDescriptionFactory.create(inStreamElement, xPathFactory);
                extensionDescription.setInputEventStream(streamDescription);
            }

            QName outStreamQName = qNameFactory.createQName(CommonsConstants.ELE_OUTPUT_EVENT_STREAM,
                    parentQName);
            OMElement outStreamElement = element.getFirstChildWithName(outStreamQName);
            if (outStreamElement != null) {
                EventStreamDescription streamDescription =
                        EventStreamDescriptionFactory.create(outStreamElement, xPathFactory);
                extensionDescription.setOutputEventStream(streamDescription);
            }
            operationDescription.setExtensionDescription(extensionDescription);
        }
    }
}
