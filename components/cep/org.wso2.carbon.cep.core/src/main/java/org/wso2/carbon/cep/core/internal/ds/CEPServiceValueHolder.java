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

package org.wso2.carbon.cep.core.internal.ds;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.broker.core.BrokerService;
import org.wso2.carbon.brokermanager.core.BrokerManagerService;
import org.wso2.carbon.cep.core.internal.CEPService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.List;

/**
 * class to keep the other osgi services temporarily
 */
public class CEPServiceValueHolder {

    private static CEPServiceValueHolder cepServiceValueHolder = new CEPServiceValueHolder();

    private BrokerService brokerService;

    private BrokerManagerService brokerManagerService;

    private RegistryService registryService;

    private ConfigurationContextService configurationContextService;

    private CEPService cepService;

    private List<OMElement> unDeployedBuckets = new ArrayList<OMElement>();

    private CEPServiceValueHolder() {

    }

    public static CEPServiceValueHolder getInstance() {
        if (cepServiceValueHolder == null) {
            cepServiceValueHolder = new CEPServiceValueHolder();
        }
        return cepServiceValueHolder;
    }

    public BrokerService getBrokerService() {
        return brokerService;
    }

    public void registerBrokerService(BrokerService brokerService) {
        this.brokerService = brokerService;
    }

    public void unsetBrokerService() {
        this.brokerService = null;
    }

    public void setBrokerManagerService(BrokerManagerService brokerManagerService) {
        this.brokerManagerService = brokerManagerService;
    }

    public void unsetBrokerManagerService() {
        this.brokerManagerService = null;
    }

    public BrokerManagerService getBrokerManagerService() {
        return brokerManagerService;
    }

    public Registry getRegistry(int tenantId) throws RegistryException {
        return registryService.getConfigSystemRegistry(tenantId);
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public void unsetRegistryService() {
        this.registryService = null;
    }

    public void registerConfigurationContextService(ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }

    public ConfigurationContextService getConfigurationContextService() {
        return this.configurationContextService;
    }

    public CEPService getCepService() {
        return this.cepService;
    }

    public void setCepService(CEPService cepService) {
        this.cepService = cepService;
    }

    public List<OMElement> getUnDeployedBuckets() {
        return this.unDeployedBuckets;
    }

    public void setUnDeployedBuckets(List<OMElement> unDeployedBuckets) {
        this.unDeployedBuckets = unDeployedBuckets;
    }
}
