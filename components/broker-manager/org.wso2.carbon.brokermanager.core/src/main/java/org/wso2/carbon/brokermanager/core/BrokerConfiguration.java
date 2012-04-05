/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.brokermanager.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * represents details of a pirticular broker connection
 */
public class BrokerConfiguration {

    /**
     * logical name use to identify this configuration
     */
    private String name;

    /**
     * broker  type for this configuration
     */
    private String type;

    /**
     * property values - these properties are depends on the properties defined in the
     * broker type. there must be a property here for each property defined in broker type
     */
    private Map<String, String> properties;

    public BrokerConfiguration() {
        this.properties = new ConcurrentHashMap<String, String>();
    }

    public void addProperty(String name, String value) {
        this.properties.put(name, value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}

