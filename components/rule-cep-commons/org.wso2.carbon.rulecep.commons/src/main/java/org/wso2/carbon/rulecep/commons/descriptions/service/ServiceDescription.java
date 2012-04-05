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

import java.util.*;

/**
 * Required information to use the rule engine by the Rule Services
 */
public class ServiceDescription extends ExtensibleConfiguration {

    public static final String TYPE = "service";

    /**
     * Service name
     */
    private String name;
    /**
     * Service description
     */
    private String description;

    private ServiceExtensionDescription serviceExtensionDescription;
    /**
     * Target namespace for this service *
     */
    private String targetNamespace;

    private String targetNSPrefix;

    /**
     * Operations
     */
    private final List<OperationDescription> operationDescriptions =
            new ArrayList<OperationDescription>();
    private final Map<String, OperationDescription> descriptionMap =
            new HashMap<String, OperationDescription>();

    private String extension;

    /**
     * Whether the name of this service can be edited
     */
    private boolean editable;

    private boolean containsServicesXML;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addRuleServiceOperationDescription(
            OperationDescription operationDescription) {
        operationDescriptions.add(operationDescription);
        descriptionMap.put(operationDescription.getName().getLocalPart(), operationDescription);
    }

    public Iterator<OperationDescription> getOperationDescriptions() {
        return operationDescriptions.iterator();
    }

    public boolean containsOperationDescriptions() {
        return !operationDescriptions.isEmpty();
    }

    public OperationDescription getRuleServiceOperationDescription(String name) {
        return descriptionMap.get(name);
    }

    public boolean isEditable() {
        return editable || (name == null || "".equals(name.trim()));
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void removeRuleServiceOperationDescription(String name) {
        if (descriptionMap.containsKey(name)) {
            OperationDescription description = descriptionMap.get(name);
            descriptionMap.remove(name);
            operationDescriptions.remove(description);
        }
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    public String getTargetNSPrefix() {
        return targetNSPrefix;
    }

    public void setTargetNSPrefix(String targetNSPrefix) {
        this.targetNSPrefix = targetNSPrefix;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public boolean isContainsServicesXML() {
        return containsServicesXML;
    }

    public void setContainsServicesXML(boolean containsServicesXML) {
        this.containsServicesXML = containsServicesXML;
    }

    public ServiceExtensionDescription getServiceExtensionDescription() {
        return serviceExtensionDescription;
    }

    public void setServiceExtensionDescription(ServiceExtensionDescription serviceExtensionDescription) {
        this.serviceExtensionDescription = serviceExtensionDescription;
    }

    @Override
    public String geType() {
        return TYPE;
    }
}
