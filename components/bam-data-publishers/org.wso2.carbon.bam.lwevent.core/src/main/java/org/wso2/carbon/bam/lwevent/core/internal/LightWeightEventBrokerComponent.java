/*
 * Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.wso2.carbon.bam.lwevent.core.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBroker;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.bam.lwevent.core" immediate="true"
 *
 * @scr.reference name="realm.service"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" bind="setRealmService"
 *                unbind="unsetRealmService"
 * 
 * @scr.reference name="config.context.service"
 *                interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 *                policy="dynamic" bind="setConfigurationContextService"
 *                unbind="unsetConfigurationContextService"
 * @scr.reference name="server.configuration" interface="org.wso2.carbon.base.ServerConfiguration"
 *                cardinality="1..1" policy="dynamic" bind="setServerConfiguration"
 *                unbind="unsetServerConfiguration"
 * @scr.reference name="org.wso2.carbon.registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService"
 *                unbind="unsetRegistryService"
 */

public class LightWeightEventBrokerComponent {

	private static Log log = LogFactory.getLog(LightWeightEventBrokerComponent.class);

    private static ConfigurationContext configurationContext;
    private static ServerConfiguration serverConfiguration;
    private static RealmService realmService;
    private static RegistryService registryService;

		
	protected void activate(ComponentContext context) {
		try {

            BundleContext bundleContext = context.getBundleContext();
            bundleContext.registerService(LightWeightEventBroker.class.getName(),
                                          LightWeightEventBroker.getInstance(), null);

		} catch (Throwable e) {
			if (log.isErrorEnabled()) {
				log.error("Failed to activate light weight eventing bundle", e);
			}
        }
	}

	protected void deactivate(ComponentContext context) {
		if (log.isDebugEnabled()) {
			log.debug("BAM service statistics data publisher bundle is deactivated");
		}
	}

	protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        configurationContext = configurationContextService.getServerConfigContext();

	}

	protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        configurationContext = null;
	}

    public static ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

//	protected void setEventBroker(EventBroker broker) {
//        eventBroker = broker;
//	}
//
//	protected void unsetEventBroker(EventBroker broker) {
//		eventBroker = null;
//	}
//
//    public static EventBroker getEventBroker() {
//        return eventBroker;
//    }

	public static ServerConfiguration getServerConfiguration() {
		return serverConfiguration;
	}

	public void setServerConfiguration(ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
	}

	public void unsetServerConfiguration(ServerConfiguration serverConfiguration) {
		serverConfiguration = null;
	}

	protected void setRegistryService(RegistryService registryService) {
		try {
            this.registryService = registryService;
		} catch (Exception e) {
			log.error("Cannot retrieve System Registry", e);
		}
	}

    protected void setRealmService(RealmService realmService){
         this.realmService=realmService;
    }
    protected void unsetRealmService(RealmService realmService){
         this.realmService=null;
    }

	protected void unsetRegistryService(RegistryService registryService) {
        this.registryService = null;
	}

    public static RegistryService getRegistryService() {
        return registryService;
    }

}
