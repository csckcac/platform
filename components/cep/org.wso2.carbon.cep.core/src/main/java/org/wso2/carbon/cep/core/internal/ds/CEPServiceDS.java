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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.broker.core.BrokerService;
import org.wso2.carbon.brokermanager.core.BrokerManagerService;
import org.wso2.carbon.cep.core.CEPServiceInterface;
import org.wso2.carbon.cep.core.internal.builder.Axis2ConfigurationContextObserverImpl;
import org.wso2.carbon.cep.core.internal.builder.CEPServiceBuilder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="cepservice.component" immediate="true"
 * @scr.reference name="configurationcontext.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="broker.service"
 * interface="org.wso2.carbon.broker.core.BrokerService" cardinality="1..1"
 * policy="dynamic" bind="setBrokerService" unbind="unsetBrokerService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="brokermanager.service"
 * interface="org.wso2.carbon.brokermanager.core.BrokerManagerService" cardinality="1..1"
 * policy="dynamic" bind="setBrokerManagerService" unbind="unsetBrokerManagerService"
 */
public class CEPServiceDS {
    private static final Log log = LogFactory.getLog(CEPServiceDS.class);

    /**
     * initialize the cep service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            CEPServiceInterface cepServiceInterface = CEPServiceBuilder.createCEPService();
            context.getBundleContext().registerService(CEPServiceInterface.class.getName(),
                    cepServiceInterface, null);
            registerAxis2ConfigurationContextObserver(context);
            log.info("Successfully deployed the cep service");
        } catch (Throwable e) {
            log.error("Can not create the cep service ", e);
        }
    }

    protected void setBrokerService(BrokerService brokerService) {
        CEPServiceValueHolder.getInstance().registerBrokerService(brokerService);
    }

    protected void unsetBrokerService(BrokerService brokerService) {

    }

    protected void setRegistryService(RegistryService registryService) throws RegistryException {
        CEPServiceValueHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        CEPServiceValueHolder.getInstance().unsetRegistryService();
    }

    protected void setBrokerManagerService(BrokerManagerService brokerManagerService) {
        CEPServiceValueHolder.getInstance().setBrokerManagerService(brokerManagerService);
    }

    protected void unsetBrokerManagerService(BrokerManagerService brokerManagerService) {
        CEPServiceValueHolder.getInstance().unsetBrokerManagerService();
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        CEPServiceValueHolder.getInstance().registerConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {

    }

    private void registerAxis2ConfigurationContextObserver(ComponentContext context) {
        context.getBundleContext().registerService(Axis2ConfigurationContextObserver.class.getName(),
                new Axis2ConfigurationContextObserverImpl(),
                null);
    }
}
