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
package org.wso2.carbon.rule.core;

import javax.xml.namespace.QName;

/**
 * Keeps constants related to the rule component
 */
public class RuleConstants {

    public final static String FORMAT_DRL = "drl";
    /* Rule Service Provider Uri for Drools implementation */
    public final static String DROOLS_RULE_SERVICE_PROVIDER_URI = "http://drools.org/";
    /* Drools Rule Service Provider implementation which is registered to 'http://drools.org/' */
    public final static String DROOLS_RULE_SERVICE_PROVIDER =
            "org.drools.jsr94.rules.RuleServiceProviderImpl";
    /* A Drools specific property key . Uses for setting language of the rule script */
    public final static String SOURCE = "source";
    /* Null namespace*/
    public static final String NULL_NAMESPACE = "";
    /**
     * The names of attributes used in the configuration
     */
    public static final QName ATT_GENERATE_SERVICES_XML = new QName(NULL_NAMESPACE,
            "generateServicesXML");
    public static final String ATT_EXTENSION = "extension";
    /**
     * The names of elements used in the configuration
     */
    public static final String ELE_FACT_ADAPTER = "FactAdapters";
    public static final String ELE_RESULT_ADAPTER = "ResultAdapters";
    public static final String ELE_ADAPTER = "Adapter";
    public static final String ELE_PROVIDER = "RuleEngineProvider";

    public static final String RULE_FILE_EXTENSION = "rsl";
    public static final String RULE_SERVICE_ARCHIVE_EXTENSION = "aar";
    public static final String RULE_SERVICE_TYPE = "rule_service";
    public static final String RULE_SERVICE_PATH = "rule_service_path";
    public static final String PROP_DEFAULT_PROPERTIES_PROVIDER = "default.properties.provider";
    public final static String RULE_SESSION = "rule_session";
    public static final String DEFAULT_TARGET_NAMESPACE = "http://brs.carbon.wso2.org";
    public static final String DEFAULT_TARGET_NAMESPACE_PREFIX = "brs";
    public static final String DEFAULT_WRAPPER_NAME = "result";
    public static final String RULE_SERVICE_TEMP_DIR = "ruleservices_temp";
    public static final String RULE_SERVICE_REPOSITORY_NAME = "ruleservices";
    public static final String RULE_SERVICE_ARCHIVE_GENERATABLE = "service_archive_generatable";
}
