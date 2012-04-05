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

package org.wso2.carbon.messagebox.admin.internal.util;


import org.wso2.carbon.messagebox.MessageBoxService;
import org.wso2.carbon.registry.core.service.RegistryService;

/**
 * This class is used to keep a reference to MessageBoxService so that
 * it can be accessed from the MessageQueueSkeleton and QueueServiceSkeleton
 */
public class MessageBoxHolder {

    private MessageBoxService messageboxService;
    private RegistryService registryService;

    private static MessageBoxHolder instance = new MessageBoxHolder();

    private MessageBoxHolder() {

    }

    public static MessageBoxHolder getInstance() {
        return instance;
    }

    public MessageBoxService getMessageboxService() {
        return this.messageboxService;
    }

    public void registerMessageboxService(MessageBoxService messageboxService) {
        this.messageboxService = messageboxService;
    }

    public void unRegisterMessageboxService(MessageBoxService messageboxService) {
        this.messageboxService = null;
    }

    public void registerRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public RegistryService getRegistryService() {
        return this.registryService;
    }


}
