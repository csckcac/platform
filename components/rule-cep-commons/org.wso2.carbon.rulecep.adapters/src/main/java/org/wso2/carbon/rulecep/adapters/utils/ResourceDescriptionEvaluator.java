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
package org.wso2.carbon.rulecep.adapters.utils;

import org.apache.axiom.om.OMNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.BaseXPath;
import org.wso2.carbon.rulecep.adapters.MessageInterceptor;
import org.wso2.carbon.rulecep.adapters.impl.ContextResourceAdapter;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;
import org.wso2.carbon.rulecep.commons.ReturnValue;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;

import java.util.List;

/**
 * Contains operations related to Resource Descriptions processing
 */
public class ResourceDescriptionEvaluator {

    private static final Log log = LogFactory.getLog(ResourceDescriptionEvaluator.class);

    /**
     * Extracts an object from the given message context based on the the information such as value ,
     * expression in the given resource description
     *
     * @param description        information about the resource
     * @param context            message context
     * @param messageInterceptor interceptor to access  the message context transparency
     * @return the resulted value for the provided <code>ResourceDescription</code>
     */
    public static Object evaluate(ResourceDescription description,
                                  Object context,
                                  MessageInterceptor messageInterceptor) {
        if (description == null) {
            throw new LoggedRuntimeException("Cannot find the Resource description. " +
                    "Invalid Resource!!", log);
        }

        if (ContextResourceAdapter.TYPE.equals(description.getType())) {
            return context; //TODO  remove this in a proper way
        }

        Object value = description.getValue();
        if (value != null) {
            return value;
        }
        String key = description.getKey();

        // if the key provides , then it should be a property key or entry key
        if (key != null) {
            // On the first check the availability of the key in property bag
            ReturnValue returnValue = messageInterceptor.extract(key, context, null);
            if (returnValue != null) {
                value = returnValue.getValue();
            }
        }
        if (value == null) {
            value = evaluateExpression(description, context, messageInterceptor);
            if (value == null) {
                ReturnValue returnValue = messageInterceptor.extractPayload(context);
                if (returnValue != null) {
                    value = returnValue.getValue();
                }
            }
        }
        return value;
    }

    /**
     * Extracts an data from the message based on the expression in the given resource description
     *
     * @param description        information about the resource
     * @param context            message context
     * @param messageInterceptor interceptor to access message context transparency
     * @return the resulted value for the  ResourceDescription
     */
    public static Object evaluateExpression(ResourceDescription description,
                                            Object context,
                                            MessageInterceptor messageInterceptor) {

        if (description == null) {
            throw new LoggedRuntimeException("Cannot find the Resource description. " +
                    "Invalid Resource !!", log);
        }

        BaseXPath sourceExpression = description.getExpression();
        if (sourceExpression == null) {
            return null;
        }
        // evaluates the expression against source
        ReturnValue returnValue = messageInterceptor.extract(sourceExpression, context, null);
        Object o = returnValue.getValue();
        if (o instanceof OMNode) {
            return o;

        } else if (o instanceof List && !((List) o).isEmpty()) {
            Object nodeObject = ((List) o).get(0); // Always fetches *only* the first

            if (nodeObject instanceof OMNode) {
                return nodeObject;
            } else {
                throw new LoggedRuntimeException("The evaluation of the XPath expression "
                        + sourceExpression + " must target in an OMNode", log);
            }

        } else {
            throw new LoggedRuntimeException("The evaluation of the XPath expression "
                    + sourceExpression + " must target in an OMNode", log);
        }
    }

}
