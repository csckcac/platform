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
package org.wso2.carbon.rule.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rule.core.LoggedRuntimeException;
import org.wso2.carbon.rule.core.RuleBackendRuntimeFactory;
import org.wso2.carbon.rulecep.commons.descriptions.PropertyDescription;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds configuration information related with the rule server
 */
public class RuleServerConfiguration {

    private static final Log log = LogFactory.getLog(RuleServerConfiguration.class);

    /**
     * The  factory for creating the rule engine provider
     */
    private RuleBackendRuntimeFactory ruleBackendRuntimeFactory;

    /**
     * QName of the server configuration
     */
    private QName qName;

    /**
     * Rule engine provider provider properties
     */
    private final List<PropertyDescription> providerProperties =
            new ArrayList<PropertyDescription>();
    private final Map<String, PropertyDescription> propertiesMap =
            new HashMap<String, PropertyDescription>();

    /**
     * Facts adapters
     */
    private final List<ResourceDescription> factAdapters = new ArrayList<ResourceDescription>();

    /**
     * Result adapters
     */
    private final List<ResourceDescription> resultsAdapters = new ArrayList<ResourceDescription>();

    /**
     * Creates a RuleServerConfiguration instance. RuleBackendRuntimeFactory should not be null.
     *
     * @param ruleBackendRuntimeFactory a RuleBackendRuntimeFactory implementation
     */
    public RuleServerConfiguration(RuleBackendRuntimeFactory ruleBackendRuntimeFactory) {
        assertRuleBackendRuntimeFactoryNull(ruleBackendRuntimeFactory);

        this.ruleBackendRuntimeFactory = ruleBackendRuntimeFactory;
    }

    public void addProviderPropertyDescription(PropertyDescription propertyDescription) {
        assertPropertyDescriptionCorrect(propertyDescription);

        providerProperties.add(propertyDescription);
        propertiesMap.put(propertyDescription.getName(), propertyDescription);
    }

    public List<PropertyDescription> getProviderProperties() {
        List<PropertyDescription> view = new ArrayList<PropertyDescription>();
        view.addAll(providerProperties);
        return view;
    }

    public Map<String, PropertyDescription> getProviderPropertiesAsMap() {
        return propertiesMap;
    }

    public RuleBackendRuntimeFactory getRuleBackendRuntimeFactory() {
        return ruleBackendRuntimeFactory;
    }

    public QName getQName() {
        return qName;
    }

    public void setQName(QName qName) {
        this.qName = qName;
    }

    public void addFactAdapterDescription(ResourceDescription adapterDescription) {
        assertResourceDescriptionNull(adapterDescription);

        factAdapters.add(adapterDescription);
    }

    public void addResultAdapterDescription(ResourceDescription adapterDescription) {
        assertResourceDescriptionNull(adapterDescription);

        resultsAdapters.add(adapterDescription);
    }

    public List<ResourceDescription> getFactAdapterDescriptionAsList() {
        List<ResourceDescription> view = new ArrayList<ResourceDescription>();
        view.addAll(factAdapters);
        return view;
    }

    public List<ResourceDescription> getResultAdapterDescriptionAsList() {
        List<ResourceDescription> view = new ArrayList<ResourceDescription>();
        view.addAll(resultsAdapters);
        return view;
    }

    private void assertRuleBackendRuntimeFactoryNull(RuleBackendRuntimeFactory factory) {
        if (factory == null) {
            throw new LoggedRuntimeException("Given RuleBackendRuntimeFactory is null", log);
        }
    }

    private void assertPropertyDescriptionCorrect(PropertyDescription propertyDescription) {
        if (propertyDescription == null) {
            throw new LoggedRuntimeException("Given provider property is null", log);
        }
        String name = propertyDescription.getName();
        if (name == null || "".equals(name.trim())) {
            throw new LoggedRuntimeException("Given provider property name is null or empty", log);
        }
    }

    private void assertResourceDescriptionNull(ResourceDescription resourceDescription) {
        if (resourceDescription == null) {
            throw new LoggedRuntimeException("Given adapter description is null", log);
        }
    }

}
