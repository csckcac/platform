/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.extensions.jmx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Properties implements PropertiesMBean {

    private static final Log log = LogFactory.getLog(Activities.class);

    private Registry registry;

    public Properties(Registry registry) {
        this.registry = registry;
    }

    public String[] getProperties(String path) {
        List<String> output = new LinkedList<String>();
        try {
            java.util.Properties properties = registry.get(path).getProperties();
            for (Map.Entry<Object, Object> e : properties.entrySet()) {
                output.add(e.getKey() + ":" + e.getValue());
            }
        } catch (RegistryException e) {
            log.error("Unable to fetch all properties", e);
        }
        return output.toArray(new String[output.size()]);
    }

    public String getProperty(String path, String key) {
        try {
            return registry.get(path).getProperty(key);
        } catch (RegistryException e) {
            log.error("Unable to fetch property value", e);
            return "";
        }
    }

    public void setProperty(String path, String key, String value) {
        try {
            Resource resource = registry.get(path);
            resource.setProperty(key, value);
            registry.put(path, resource);
        } catch (RegistryException e) {
            log.error("Unable to fetch property value", e);
        }
    }

    public void removeProperty(String path, String key) {
        try {
            Resource resource = registry.get(path);
            resource.removeProperty(key);
            registry.put(path, resource);
        } catch (RegistryException e) {
            log.error("Unable to fetch property value", e);
        }
    }
}
