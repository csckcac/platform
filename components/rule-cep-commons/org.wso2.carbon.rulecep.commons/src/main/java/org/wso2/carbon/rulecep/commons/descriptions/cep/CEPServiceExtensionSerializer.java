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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.commons.CommonsConstants;
import org.wso2.carbon.rulecep.commons.descriptions.OMNamespaceFactory;
import org.wso2.carbon.rulecep.commons.descriptions.XPathSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensibleConfiguration;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensionSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.service.OperationDescription;
import org.wso2.carbon.rulecep.commons.descriptions.service.ServiceDescription;

/**
 *  Serialize extensions of the CEP services(i.e service , operation configuration extensions) into XML
 */
public class CEPServiceExtensionSerializer implements ExtensionSerializer {

    private Log log = LogFactory.getLog(CEPServiceExtensionSerializer.class);

    private static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();

    public OMElement serialize(ExtensibleConfiguration configuration,
                               XPathSerializer xPathSerializer, OMElement parent) {

        if (ServiceDescription.TYPE.equals(configuration.geType())) {

            ServiceDescription serviceDescription = (ServiceDescription) configuration;
            QueryDescription extensionDescription =
                    (QueryDescription)
                            serviceDescription.getServiceExtensionDescription();

            if (extensionDescription != null) {
                OMNamespaceFactory omNamespaceFactory = OMNamespaceFactory.getInstance();
                OMNamespace omNamespace = omNamespaceFactory.createOMNamespace(
                        parent.getNamespace().getNamespaceURI(), "");
                OMElement ruleSetElement = OM_FACTORY.createOMElement(CommonsConstants.ELE_QUERY,
                        omNamespace);
                QueryDescriptionSerializer.serialize(extensionDescription, xPathSerializer,
                        ruleSetElement);
                parent.addChild(ruleSetElement);
            }
            return parent;
        } else if (OperationDescription.TYPE.equals(configuration.geType())) {

            OperationDescription operationDescription = (OperationDescription) configuration;
            CEPOperationExtensionDescription extensionDescription =
                    (CEPOperationExtensionDescription)
                            operationDescription.getExtensionDescription();

            if (extensionDescription != null) {
                OMNamespaceFactory omNamespaceFactory = OMNamespaceFactory.getInstance();
                OMNamespace omNamespace = omNamespaceFactory.createOMNamespace(
                        parent.getNamespace().getNamespaceURI(), "");
                EventStreamDescription input = extensionDescription.getInputEventStream();
                if (input != null) {
                    OMElement inEventStream = OM_FACTORY.createOMElement(
                            CommonsConstants.ELE_INPUT_EVENT_STREAM,
                            omNamespace);
                    EventStreamDescriptionSerializer.serialize(input, xPathSerializer, inEventStream);
                    parent.addChild(inEventStream);
                }

                EventStreamDescription output = extensionDescription.getInputEventStream();
                if (output != null) {
                    OMElement outStream = OM_FACTORY.createOMElement(
                            CommonsConstants.ELE_OUTPUT_EVENT_STREAM,
                            omNamespace);
                    EventStreamDescriptionSerializer.serialize(output, xPathSerializer, outStream);
                }
            }
            return parent;
        }
        return null;
    }
}


