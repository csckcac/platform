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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.adapters.MessageInterceptor;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;
import org.wso2.carbon.rulecep.commons.ReturnValue;
import org.wso2.carbon.rulecep.commons.descriptions.PropertyDescription;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains operations related to property descriptions processing
 */
public class PropertyDescriptionEvaluator {

    private static final Log log = LogFactory.getLog(PropertyDescriptionEvaluator.class);

    /**
     * Extracts data from the message context based on the information such as value ,
     * expression in the given property description
     *
     * @param propertyDescription information about the property
     * @param context             message context object
     * @param messageInterceptor  interceptor to access the message context transparency
     * @return Extracted data from the message context.
     */
    public static Object evaluate(PropertyDescription propertyDescription,
                                  Object context, MessageInterceptor messageInterceptor) {

        String name = propertyDescription.getName();
        if (name == null || "".equals(name)) {
            throw new LoggedRuntimeException("Invalid PropertyDescription - a name cannot " +
                    "be found", log);
        }

        Object value = propertyDescription.getValue();
        if (value == null || "".equals(value)) {

            if (context == null) {
                throw new LoggedRuntimeException("Invalid property - a value cannot be found" +
                        " for property : " + name, log);
            }
            ReturnValue returnValue = messageInterceptor.extract(
                    propertyDescription.getExpression(),
                    context, value);
            value = returnValue.getValue();
        }

        if (value == null || "".equals(value)) {
            throw new LoggedRuntimeException("Invalid property - a value cannot be found " +
                    "for property : " + name, log);
        }

        return value;
    }

    /**
     * Converts a list of properties into a map of properties
     *
     * @param properties a list of properties to be mapped into a map
     * @return a resulted map of properties
     */
    public static Map<String, Object> evaluate(Collection<PropertyDescription> properties) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (PropertyDescription description : properties) {
            if (description != null) {
                Object value = description.getValue();
                if (value != null) {
                    map.put(description.getName(), value);
                }
            }
        }
        return map;
    }

}
