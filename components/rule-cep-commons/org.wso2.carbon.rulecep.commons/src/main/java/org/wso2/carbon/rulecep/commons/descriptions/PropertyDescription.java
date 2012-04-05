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
package org.wso2.carbon.rulecep.commons.descriptions;

import org.jaxen.BaseXPath;

import javax.xml.namespace.QName;

/**
 * Property Mata-data
 */
public class PropertyDescription {

    public static final java.lang.String NULL_NAMESPACE = "";
    public static final QName PROPERTY_Q = new QName(NULL_NAMESPACE, "property");
    public static final QName PROPERTY_CAP_Q = new QName(NULL_NAMESPACE, "Property");
    public static final QName ATT_NAME_Q = new QName(NULL_NAMESPACE, "name");
    public static final QName ATT_VALUE_Q = new QName(NULL_NAMESPACE, "value");
    public static final QName ATT_EXPR_Q = new QName(NULL_NAMESPACE, "expression");

    private String name;
    private String value;
    private BaseXPath expression;

    public PropertyDescription() {
    }

    public PropertyDescription(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public PropertyDescription(String name, BaseXPath expression) {
        this.expression = expression;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public BaseXPath getExpression() {
        return expression;
    }

    public void setExpression(BaseXPath expression) {
        this.expression = expression;
    }
}
