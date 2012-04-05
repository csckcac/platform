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
package org.wso2.carbon.rulecep.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;

import java.io.InputStream;

/**
 * Find and provide resources from classpath, registry, etc
 */
public class ResourceLoader {

    private static Log log = LogFactory.getLog(ResourceLoader.class);

    private RegistryService registryService;

    public ResourceLoader(RegistryService registryService) {
        this.registryService = registryService;
    }

    /**
     * Finds the resource corresponded to the given key from the given class loader
     * Checks the META-INF and conf sub directories
     *
     * @param key key to look up resource
     * @param cl  Service class loader
     * @return <code>InputStream</code> if there is a resource at the given key , otherwise null
     */
    public InputStream loadResourceFromLocal(String key, ClassLoader cl) {

        InputStream inputStream = cl.getResourceAsStream(key);

        if (inputStream == null) {
            inputStream = cl.getResourceAsStream("META-INF/" + key);
            if (inputStream == null) {
                inputStream = cl.getResourceAsStream("conf/" + key);
            }
        }
        return inputStream;
    }

    /**
     * Loads the resource from the registry with the provided key
     *
     * @param key key to look up resource
     * @return <code>InputStream</code> if there is a resource at the given key , otherwise null
     */
    public InputStream loadResourceFromRegistry(String key,int tenantId) {
        try {
            if (key.startsWith(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)) {
                key = key.substring(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH.length());
                Registry registry =
                        registryService.getGovernanceSystemRegistry(tenantId);
                if (registry.resourceExists(key)) {
                    Resource resource = registry.get(key);
                    if (resource != null) {
                        return resource.getContentStream();
                    }
                }
            }else if(key.startsWith(RegistryConstants.CONFIG_REGISTRY_BASE_PATH)){
                key = key.substring(RegistryConstants.CONFIG_REGISTRY_BASE_PATH.length());
                Registry registry =
                        registryService.getConfigSystemRegistry(tenantId);
                if (registry.resourceExists(key)) {
                    Resource resource = registry.get(key);
                    if (resource != null) {
                        return resource.getContentStream();
                    }
                }
            }else if (key.startsWith(RegistryConstants.LOCAL_REPOSITORY_BASE_PATH)){
                key = key.substring(RegistryConstants.LOCAL_REPOSITORY_BASE_PATH.length());
                Registry registry =
                        registryService.getLocalRepository(tenantId);
                if (registry.resourceExists(key)) {
                    Resource resource = registry.get(key);
                    if (resource != null) {
                        return resource.getContentStream();
                    }
                }
            }
        } catch (RegistryException e) {
            throw new LoggedRuntimeException("Error accessing ConfigUserRegistry", e, log);
        }
        return null;
    }

}
