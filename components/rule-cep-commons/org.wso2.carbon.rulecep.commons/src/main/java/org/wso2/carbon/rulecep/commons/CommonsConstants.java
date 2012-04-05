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
package org.wso2.carbon.rulecep.commons;

import javax.xml.namespace.QName;

/**
 * Constants belong to rule cep commons
 */
public final class CommonsConstants {

    /* XMl as the language of the rule source */
    public final static String FORMAT_XML = "xml";
    /* A Drools specific property key . Uses for setting language of the rule script */
    public final static String SOURCE = "source";
    /* Null namespace*/
    public static final String NULL_NAMESPACE = "";
    /**
     * The names of attributes used in the configuration
     */
    public static final QName ATT_KEY_Q = new QName(NULL_NAMESPACE, "key");
    public static final QName ATT_PATH_Q = new QName(NULL_NAMESPACE, "path");
    public static final QName ATT_TOPIC_Q = new QName(NULL_NAMESPACE, "topic");
    public static final QName ATT_NAME_Q = new QName(NULL_NAMESPACE, "name");
    public static final QName ATT_TARGET_NAMESPACE_Q = new QName(NULL_NAMESPACE, "tns");
    public static final QName ATT_TYPE_Q = new QName(NULL_NAMESPACE, "type");
    public static final QName ATT_VALUE_Q = new QName(NULL_NAMESPACE, "value");
    public static final QName ATT_EXPR_Q = new QName(NULL_NAMESPACE, "expression");
    public static final QName ATT_URI_Q = new QName(NULL_NAMESPACE, "uri");
    public static final QName ATT_USER_NAME_Q = new QName(NULL_NAMESPACE, "username");
    public static final QName ATT_PASSWORD_Q = new QName(NULL_NAMESPACE, "password");
    public static final QName ATT_GENERATE_SERVICES_XML = new QName(NULL_NAMESPACE,
            "generateServicesXML");
    public static final String ATT_EXTENSION = "extension";

    /**
     * The names of elements used in the configuration
     */

    public static final String ELE_DESCRIPTION = "description";
    public static final String ELE_OPERATION = "operation";
    public static final String ELE_SERVICE = "service";
    public static final String ELE_RULE_SERVICE = "ruleService";
    public static final String ELE_RULESET = "ruleset";
    public static final String ELE_QUERY = "query";
    public static final String ELE_INPUT_EVENT_STREAM = "inputStream";
    public static final String ELE_OUTPUT_EVENT_STREAM = "outputStream";
    public static final String ELE_CREATION = "creation";
    public static final String ELE_REGISTRATION = "registration";
    public static final String ELE_DEREGISTRATION = "deregistration";
    public static final String ELE_SOURCE = "source";
    public static final String ELE_SESSION = "session";
    public static final String ELE_FACT = "fact";
    public static final String ELE_WITH_PARAM = "with-param";
    public static final String ELE_RESULT = "result";
    public static final String ELE_ELEMENT = "element";
    public static final String ELE_PARAMETER = "parameter";

    public static final String DEFAULT_WRAPPER_NAME = "result";
}
