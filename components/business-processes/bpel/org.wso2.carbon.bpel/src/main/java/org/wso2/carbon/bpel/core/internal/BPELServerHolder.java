/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.bpel.core.internal;

import org.wso2.carbon.bpel.core.ode.integration.BPELServerImpl;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;

/**
 * Data holder for the BPELServiceComponent
 */
public final class BPELServerHolder {
    private static BPELServerHolder instance;

    private RegistryService registryService;
    private BPELServerImpl bpelServer;

    private TenantRegistryLoader registryLoader;

    private BPELServerHolder() {
    }

    public static BPELServerHolder getInstance() {
        if (instance == null) {
            instance = new BPELServerHolder();
        }
        return instance;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public BPELServerImpl getBpelServer() {
        return bpelServer;
    }

    public void setBpelServer(BPELServerImpl bpelServer) {
        this.bpelServer = bpelServer;
    }

    public TenantRegistryLoader getRegistryLoader() {
        return registryLoader;
    }

    public void setRegistryLoader(TenantRegistryLoader registryLoader) {
        this.registryLoader = registryLoader;
    }
}
