/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.announcement.services;

import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.announcement.utils.Util;

public class AnnouncementService {
    private static final String instIdPath = "/repository/components/org.wso2.carbon.instance-id/uuid";
    public String retrieveRegId() throws Exception {
        RegistryService registryService = Util.getRegistryService();
        Registry systemRegistry = registryService.getGovernanceSystemRegistry();
        String uuid = null;
        systemRegistry.beginTransaction();
        boolean isSuccess = false;
        try {
            if (systemRegistry.resourceExists(instIdPath)) {
                Resource resource = systemRegistry.get(instIdPath);
                byte[] uuidBytes = (byte[])resource.getContent();
                uuid = new String(uuidBytes);
            }
            if (uuid == null) {
                uuid = UUIDGenerator.generateUUID();
                Resource resource = systemRegistry.newResource();
                resource.setContent(uuid);
                systemRegistry.put(instIdPath, resource);
            }
            isSuccess = true;
        }
        finally {
            if (isSuccess) {
                systemRegistry.commitTransaction();
            }
            else {
                systemRegistry.rollbackTransaction();
            }
        }
        return uuid;
    }
}
