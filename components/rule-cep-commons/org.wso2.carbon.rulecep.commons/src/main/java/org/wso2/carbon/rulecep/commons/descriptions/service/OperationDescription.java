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


import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Information about the rule service operations
 */
public class OperationDescription extends ExtensibleConfiguration {

    public static final String TYPE = "operation";

    /**
     * The name of the rule service
     */
    private QName name;

    /**
     * Information  about Facts
     */
    private final List<ResourceDescription> inputs = new ArrayList<ResourceDescription>();

    /**
     * Information  about Results
     */
    private final List<ResourceDescription> outputs = new ArrayList<ResourceDescription>();

    private OperationExtensionDescription extensionDescription;

    private boolean forceInOnly;

    public void addFactDescription(ResourceDescription description) {
        inputs.add(description);
    }

    public List<ResourceDescription> getFactDescriptions() {
        return inputs;
    }

    public void addResultDescription(ResourceDescription description) {
        outputs.add(description);
    }

    public List<ResourceDescription> getResultDescriptions() {
        return outputs;
    }

    public boolean isContainsFact() {
        return !inputs.isEmpty();
    }

    public boolean isContainsResult() {
        return !outputs.isEmpty();
    }

    public QName getName() {
        return name;
    }

    public void setName(QName name) {
        this.name = name;
    }

    public void clearFacts() {
        inputs.clear();
    }

    public void clearResults() {
        outputs.clear();
    }

    public OperationExtensionDescription getExtensionDescription() {
        return extensionDescription;
    }

    public void setExtensionDescription(OperationExtensionDescription extensionDescription) {
        this.extensionDescription = extensionDescription;
    }

    @Override
    public String geType() {
        return TYPE;
    }

    public boolean isForceInOnly() {
        return forceInOnly;
    }

    public void setForceInOnly(boolean forceInOnly) {
        this.forceInOnly = forceInOnly;
    }
}
