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
package org.wso2.carbon.rulecep.adapters.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * The XML Object model representation of the input-output-adapters.xml
 */
public class InputOutputAdaptersConfiguration {

    private static final Log log = LogFactory.getLog(InputOutputAdaptersConfiguration.class);
    /**
     * Facts adapters
     */
    private final List<ResourceDescription> factAdapters = new ArrayList<ResourceDescription>();

    /**
     * Result adapters
     */
    private final List<ResourceDescription> resultsAdapters = new ArrayList<ResourceDescription>();

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


    private void assertResourceDescriptionNull(ResourceDescription resourceDescription) {
        if (resourceDescription == null) {
            throw new LoggedRuntimeException("Given adapter description is null", log);
        }
    }
}
