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
package org.wso2.carbon.rulecep.commons.descriptions.rule;

import org.wso2.carbon.rulecep.commons.descriptions.PropertyDescription;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensibleConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * RuleSet's Meta-data
 */
public class RuleSetDescription extends ExtensibleConfiguration {

    public static final String TYPE = "ruleset";
    /**
     * Rule set
     */
    private Object ruleSource;

    /**
     * URI of the ruleset received from the ruleset registration with the rule engine
     */
    private String bindURI;

    /**
     * The key to look up ruleset from a system such as registry , fileSystem , classpath
     */
    private String key;
    /*The properties which is used when performing the registration of a ExecutionSet */
    private final List<PropertyDescription> registrationProperties =
            new ArrayList<PropertyDescription>();

    /*The properties which is used when performing the deregistration of a ExecutionSet */
    private final List<PropertyDescription> deregistrationProperties =
            new ArrayList<PropertyDescription>();

    /*The properties which is used when creating the RuleExecutionSet implementation.
      This can be null */
    private final List<PropertyDescription> creationProperties =
            new ArrayList<PropertyDescription>();
    /**
     * The path to load the rule set
     */
    private String path;

    private String registryType;

    public Object getRuleSource() {
        return ruleSource;
    }

    public void setRuleSource(Object ruleSource) {
        this.ruleSource = ruleSource;
    }

    public String getBindURI() {
        return bindURI;
    }

    public void setBindURI(String bindURI) {
        this.bindURI = bindURI;
    }

    public void addDeregistrationProperty(PropertyDescription propertyDescription) {
        deregistrationProperties.add(propertyDescription);
    }

    public void addRegistrationProperty(PropertyDescription propertyDescription) {
        registrationProperties.add(propertyDescription);
    }

    public void addCreationProperty(PropertyDescription propertyDescription) {
        creationProperties.add(propertyDescription);
    }

    public List<PropertyDescription> getDeregistrationProperties() {
        List<PropertyDescription> view = new ArrayList<PropertyDescription>();
        view.addAll(deregistrationProperties);
        return view;
    }

    public List<PropertyDescription> getCreationProperties() {
        List<PropertyDescription> view = new ArrayList<PropertyDescription>();
        view.addAll(creationProperties);
        return view;
    }

    public List<PropertyDescription> getRegistrationProperties() {
        List<PropertyDescription> view = new ArrayList<PropertyDescription>();
        view.addAll(registrationProperties);
        return view;
    }

    public void clearCreationProperties() {
        creationProperties.clear();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRegistryType() {
        return registryType;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

    @Override
    public String geType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "RuleSetDescription{" +
                "key='" + key + '\'' +
                ", path='" + path + '\'' +
                ", ruleSource=" + ruleSource +
                '}';
    }
}
