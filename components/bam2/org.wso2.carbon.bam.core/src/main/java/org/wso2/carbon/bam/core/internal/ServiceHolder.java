/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.core.internal;

import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

public class ServiceHolder {

    private static RealmService realmService = null;

    private static TenantRegistryLoader loader = null;

    private static RegistryService registryService = null;

    private static ConfigurationContextService configurationContextService = null;

    public static RealmService getRealmService() {
        return realmService;
    }

    public static void setRealmService(RealmService realmService) {
        ServiceHolder.realmService = realmService;
    }

    public static TenantRegistryLoader getTenantRegistryLoader() {
        return loader;
    }

    public static void setTenantRegistryLoader(TenantRegistryLoader loader) {
        ServiceHolder.loader = loader;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static void setRegistryService(RegistryService registryService) {
        ServiceHolder.registryService = registryService;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        ServiceHolder.configurationContextService = configurationContextService;
    }

}
