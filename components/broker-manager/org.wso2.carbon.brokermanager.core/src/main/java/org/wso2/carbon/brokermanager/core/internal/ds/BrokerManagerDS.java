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

package org.wso2.carbon.brokermanager.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.brokermanager.core.BrokerManagerService;
import org.wso2.carbon.brokermanager.core.internal.build.Axis2ConfigurationContextObserverImpl;
import org.wso2.carbon.brokermanager.core.internal.build.BrokerManagerServiceBuilder;
import org.wso2.carbon.brokermanager.core.internal.util.RegistryHolder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

/**
 * @scr.component name="brokermanager.component" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
public class BrokerManagerDS {

    private static final Log log = LogFactory.getLog(BrokerManagerDS.class);

    /**
     * initialize the cep service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            BrokerManagerService brokerProxyService =
                    BrokerManagerServiceBuilder.createBrokerManagerService();
            context.getBundleContext().registerService(BrokerManagerService.class.getName(),
                                                       brokerProxyService, null);
            registerAxis2ConfigurationContextObserver(context);
            log.info("Successfully deployed the broker manager service");
        } catch (Throwable e) {
            log.error("Can not create the broker manager service ", e);
        }
    }

    protected void setRegistryService(RegistryService registryService) throws RegistryException {
        RegistryHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        RegistryHolder.getInstance().unSetRegistryService();
    }

    private void registerAxis2ConfigurationContextObserver(ComponentContext context){
        context.getBundleContext().registerService(Axis2ConfigurationContextObserver.class.getName(),
                new Axis2ConfigurationContextObserverImpl(),
                null);
    }

}
