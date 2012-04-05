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
package org.wso2.carbon.rulecep.service;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.wso2.carbon.rulecep.adapters.InputsFilter;
import org.wso2.carbon.rulecep.adapters.MessageInterceptor;
import org.wso2.carbon.rulecep.adapters.OutputsFilter;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Filters input /output based on operation name
 */
public class OpNameBasedResourceFilter implements InputsFilter, OutputsFilter {

    public List<ResourceDescription> filter(List<ResourceDescription> inputs,
                                            Object context,
                                            MessageInterceptor messageInterceptor) {

        if (!(context instanceof MessageContext)) {
            return inputs;
        }
        MessageContext messageContext = (MessageContext) context;
        AxisOperation axisOperation = messageContext.getOperationContext().getAxisOperation();
        if (axisOperation == null) {
            return inputs;
        }

        QName qName = axisOperation.getName();
        if (qName == null) {
            return inputs;
        }

        String name = qName.getLocalPart();
        if (name == null || "".equals(name)) {
            return inputs;
        }

        final List<ResourceDescription> tobeReturn = new ArrayList<ResourceDescription>();
        for (ResourceDescription description : inputs) {
            if (description == null) {
                continue;
            }
            if (name.equals(description.getName())) {
                tobeReturn.add(description);
            }
        }
        return tobeReturn;
    }

    public List<ResourceDescription> filter(List<ResourceDescription> outputs,
                                            List results,
                                            Object context,
                                            MessageInterceptor messageInterceptor) {

        return filter(outputs, context, messageInterceptor);
    }
}
