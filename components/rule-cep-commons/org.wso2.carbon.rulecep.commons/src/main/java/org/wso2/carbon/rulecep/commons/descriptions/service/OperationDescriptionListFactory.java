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
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescriptionFactory;
import org.wso2.carbon.rulecep.commons.descriptions.XPathFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Factory to create <code>OperationDescription</code> instances from XML
 * that represents <code>OperationDescription</code> configuration
 */
public class OperationDescriptionListFactory {

    /**
     * Creates a list of <code>OperationDescription</code> instances based on the given
     * XML representing <code>OperationDescription</code> configurations
     *
     * @param configuration    Configuration XML to be used to create
     *                         <code>OperationDescription</code>
     * @param xPathFactory     to be used to create XPaths
     * @param extensionBuilder <code>ExtensionBuilder</code>
     * @return a list of <code>OperationDescription</code> instances
     */
    public static List<OperationDescription> create(OMElement configuration,
                                                    XPathFactory xPathFactory,
                                                    ExtensionBuilder extensionBuilder) {

        final List<OperationDescription> operationList =
                new ArrayList<OperationDescription>();
        QName parentQName = configuration.getQName();
        QNameFactory qNameFactory = QNameFactory.getInstance();
        // operations
        QName operationQName = qNameFactory.createQName(CommonsConstants.ELE_OPERATION,
                parentQName);
        Iterator operations = configuration.getChildrenWithName(operationQName);
        while (operations.hasNext()) {
            OMElement operation = (OMElement) operations.next();
            if (operation == null) {
                continue;
            }
            OperationDescription operationDescription =
                    create(operation, xPathFactory, parentQName);
            extensionBuilder.build(operationDescription, operation, xPathFactory);
            if (operationDescription != null) {
                operationList.add(operationDescription);
            }
        }
        return operationList;
    }

    /**
     * Helper method to create a OperationDescription from a <code>AxisOperation</code>
     *
     * @param operation    <code>AxisOperation </code>
     * @param xPathFactory to be used to create XPaths
     * @param parentQName  QName of the parent element
     * @return <code>OperationDescription</code>  instance
     */
    private static OperationDescription create(OMElement operation,
                                               XPathFactory xPathFactory,
                                               QName parentQName) {
        QNameFactory qNameFactory = QNameFactory.getInstance();
        OperationDescription operationDescription =
                new OperationDescription();
        String name = operation.getAttributeValue(CommonsConstants.ATT_NAME_Q);
        if (name != null && !"".equals(name)) {
            operationDescription.setName(qNameFactory.createQName(name, parentQName));
        }


        QName inputQName = qNameFactory.createQName(CommonsConstants.ELE_WITH_PARAM,
                parentQName);
        Iterator inputs = operation.getChildrenWithName(inputQName);
        while (inputs.hasNext()) {
            OMElement inputElem = (OMElement) inputs.next();
            ResourceDescription input =
                    ResourceDescriptionFactory.createResourceDescription(inputElem,
                            xPathFactory);
            if (input != null) {
                operationDescription.addFactDescription(input);
            }
        }

        QName resultsQName = qNameFactory.createQName("result", parentQName);
        OMElement resultsElement = operation.getFirstChildWithName(resultsQName);
        if (resultsElement != null) {
            ResourceDescription resultWrapper = new ResourceDescription();
            resultWrapper.setType("omelement");//todo
            String wrapperName = resultsElement.getAttributeValue(CommonsConstants.ATT_NAME_Q);
            if (wrapperName != null && !"".equals(wrapperName)) {
                resultWrapper.setName(wrapperName);// TODO setting QName
            } else {
                resultWrapper.setName(CommonsConstants.DEFAULT_WRAPPER_NAME);
            }
            QName outputQName = qNameFactory.createQName(CommonsConstants.ELE_ELEMENT,
                    parentQName);
            Iterator outputs = resultsElement.getChildrenWithName(outputQName);
            while (outputs.hasNext()) {
                OMElement outputElem = (OMElement) outputs.next();
                ResourceDescription output =
                        ResourceDescriptionFactory.createResourceDescription(outputElem,
                                xPathFactory);
                if (output != null) {
                    resultWrapper.addChildResource(output);
                }
            }
            if (resultWrapper.hasChildren()) {
                operationDescription.addResultDescription(resultWrapper);
            }
        }
        return operationDescription;
    }
}
