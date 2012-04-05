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
package org.wso2.carbon.rulecep.adapters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.adapters.impl.ContextResourceAdapter;
import org.wso2.carbon.rulecep.adapters.impl.DOMResorceAdapter;
import org.wso2.carbon.rulecep.adapters.impl.MessageFactAdapter;
import org.wso2.carbon.rulecep.adapters.impl.OMElementResourceAdapter;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;
import org.wso2.carbon.rulecep.commons.utils.ClassHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Create, initiate and manage all input adapters registered in the rule server
 */
public class InputAdapterFactory {

    private static final Log log = LogFactory.getLog(InputAdapterFactory.class);

    public InputAdapterFactory() {
        loadAdapters();
    }

    private final Class[] adapters = {
            OMElementResourceAdapter.class,
            DOMResorceAdapter.class,
            ContextResourceAdapter.class,
            MessageFactAdapter.class,
    };


    private final Map<String, InputAdaptable> adaptersMap = new HashMap<String, InputAdaptable>();

    private void loadAdapters() {

        for (Class c : adapters) {
            if (c == null) {
                continue;
            }
            try {
                ResourceAdapter fac = (ResourceAdapter) c.newInstance();
                adaptersMap.put(fac.getType(), (InputAdaptable) fac);
            } catch (Exception e) {
                throw new LoggedRuntimeException("Error instantiating " + c.getName(), e, log);
            }
        }
    }

    /**
     * Returns a <code>InputAdaptable</code> having capable of adapting inputs for a given resource type
     *
     * @param type the adapter type
     * @return <code>InputAdaptable</code>  if the there is a adapter registered for the given type
     */
    public InputAdaptable getInputAdapter(String type) {
        InputAdaptable adapter = adaptersMap.get(type);
        return adapter;
    }

    /**
     * Registering adapters programmatically
     *
     * @param adapterDescription information about the adapter. This should contain the type and
     *                           value which is used as the class name of teh adapter
     */
    public void addInputAdapter(ResourceDescription adapterDescription) {

        if (adapterDescription == null) {
            throw new LoggedRuntimeException("Provided input adapter information is null", log);
        }

        String type = adapterDescription.getType();
        if (type == null || "".equals(type.trim())) {
            throw new LoggedRuntimeException("Cannot find the adapter type from the given " +
                    "adapter information " + adapterDescription, log);
        }

        String className = (String) adapterDescription.getValue();

        InputAdaptable resourceAdapter = (InputAdaptable) ClassHelper.createInstance(className);
        if (resourceAdapter == null) {
            throw new LoggedRuntimeException("Cannot create an adapter instance from the given " +
                    "adapter information " + adapterDescription, log);
        }
        adaptersMap.put(type, resourceAdapter);
    }

    /**
     * Check the existence of an adapter
     *
     * @param type adapter type
     * @return <code>true</code> if there is an input adapter for the given type
     */
    public boolean containsInputAdapter(String type) {
        return adaptersMap.get(type) != null;
    }

    /**
     * Returns a list of all registered adapters types
     *
     * @return A list of all registered adapters types
     */
    public Collection<String> getInputAdapters() {
        Collection<String> keys = new ArrayList<String>();
        keys.addAll(adaptersMap.keySet());
        return keys;
    }
}
