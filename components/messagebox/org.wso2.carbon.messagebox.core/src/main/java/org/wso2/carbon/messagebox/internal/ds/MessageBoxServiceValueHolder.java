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

package org.wso2.carbon.messagebox.internal.ds;

import org.wso2.carbon.qpid.service.QpidService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

public class MessageBoxServiceValueHolder {

    private QpidService qpidService;
    private RegistryService registryService;
    private RealmService realmService;
    private ConfigurationContextService configurationContextService;

    private static MessageBoxServiceValueHolder instance = new MessageBoxServiceValueHolder();

    public static MessageBoxServiceValueHolder getInstance() {
        return instance;
    }

    public void registerQpidService(QpidService qpidService) {
        this.qpidService = qpidService;
    }

    public QpidService getQpidService() {
        return this.qpidService;
    }

    public void registerRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public RegistryService getRegistryService() {
        return this.registryService;
    }

    public void registerRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public RealmService getRealmService() {
        return this.realmService;
    }

    public ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public void registerConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }
}
