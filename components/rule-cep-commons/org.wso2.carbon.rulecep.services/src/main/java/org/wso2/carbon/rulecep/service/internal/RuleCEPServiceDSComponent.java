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
package org.wso2.carbon.rulecep.service.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.rulecep.adapters.service.InputOutputAdaptersService;
import org.wso2.carbon.rulecep.service.RuleCEPDeployerService;
import org.wso2.carbon.rulecep.service.ServiceManger;
import org.wso2.carbon.utils.Utils;

/**
 * @scr.component name="rulecep.service.component" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"*
 * @scr.reference name="inputoutputadapters.component"
 * interface="org.wso2.carbon.rulecep.adapters.service.InputOutputAdaptersService" cardinality="1..1"
 * policy="dynamic" bind="setInputOutputAdaptersService" unbind="unsetInputOutputAdaptersService"
 */
public class RuleCEPServiceDSComponent {

    private static Log log = LogFactory.getLog(RuleCEPServiceDSComponent.class);

    private ServiceManger ruleServiceManger = ServiceManger.getInstance();
    private RegistryService registryService;
    private InputOutputAdaptersService adaptersService;
    private ServiceRegistration serviceRegistration;

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Rule CEP ServiceDeployer Services bundle is activated ");
        }
        try {
            ruleServiceManger.setRegistryService(registryService);
            ruleServiceManger.setInputOutputAdaptersService(adaptersService);
            serviceRegistration = componentContext.getBundleContext().registerService(
                    RuleCEPDeployerService.class.getName(),
                    new RuleCEPDeployerService(),
                    null);
            Utils.registerDeployerServices(componentContext.getBundleContext());

        } catch (Exception e) {
            String msg = "Failed to register ServiceDeployer as an OSGi service.";
            log.error(msg, e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        componentContext.getBundleContext().ungetService(serviceRegistration.getReference());
        if (log.isDebugEnabled()) {
            log.debug("Rule CEP ServiceDeployer bundle is deactivated ");
        }
    }

    protected void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {
        this.registryService = null;
    }

    protected void setInputOutputAdaptersService(InputOutputAdaptersService adaptersService) {
        this.adaptersService = adaptersService;
    }

    protected void unsetInputOutputAdaptersService(InputOutputAdaptersService adaptersService) {
        this.adaptersService = null;
    }
}
