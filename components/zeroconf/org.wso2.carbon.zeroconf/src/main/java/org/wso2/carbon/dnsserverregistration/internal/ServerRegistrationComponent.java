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
package org.wso2.carbon.dnsserverregistration.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.engine.ListenerManager;
import org.osgi.service.component.ComponentContext;
import org.osgi.framework.ServiceRegistration;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.dnsserverregistration.ServerRegistration;
import org.wso2.carbon.dnsserverregistration.ServerRegistrationConstants;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="registration.server.dscomponent" immediate="true"
 * @scr.reference name="registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService"
 *                unbind="unsetRegistryService"
 * @scr.reference name="config.context.service"
 *                interface="org.wso2.carbon.utils.ConfigurationContextService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setConfigurationContextService"
 *                unbind="unsetConfigurationContextService"
 * @scr.reference name="server.configuration"
 *                interface="org.wso2.carbon.base.ServerConfiguration"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setServerConfiguration"
 *                unbind="unsetServerConfiguration"
 * @scr.reference name="listener.manager.service"
 *                interface="org.apache.axis2.engine.ListenerManager"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setListenerManager"
 *                unbind="unsetListenerManager"
 */

public class ServerRegistrationComponent {

	private static Log log = LogFactory
			.getLog(ServerRegistrationComponent.class);
	private static ServerConfiguration serverConfiguration;
	private static RegistryService registryServiceInstance;
	private static ConfigurationContextService ccServiceInstance;
    private ServerRegistration serverRegistration = null;
    private ServiceRegistration serviceRegistration = null;

	protected void activate(ComponentContext ctxt) {
		try {
			serverRegistration = new ServerRegistration();
            serverRegistration.registerServer();
			log.info("Registered Server as: "
					+ serverRegistration.getServerConfigurationProperty() + "."
					+ ServerRegistrationConstants.SERVICE_TYPE);
            serviceRegistration = ctxt.getBundleContext().registerService(
					ServerRegistration.class.getName(), serverRegistration,
					null);
		} catch (Throwable e) {
			log.error("Failed to register the server with jmdns. ", e);
		}
    }

	protected void deactivate(ComponentContext ctxt) {
        serviceRegistration.unregister();
        serviceRegistration = null;
        try {
            serverRegistration.unregisterServer();
        } catch (Exception e) {
            // This has no side effects, if occurred at a shutdown.
            if (log.isDebugEnabled()) {
                log.debug("Exception occured while unregistering the server from jmdns", e);
            }
        }
        serverRegistration = null;
		log.debug("Server Registration bundle is deactivated ");
	}

	public static ServerConfiguration getServerConfiguration() throws Exception {
		if (serverConfiguration == null) {
			String msg = "Before activating Registration bundle, an instance of "
					+ "ServerConfiguration Service should be in existance";
			log.error(msg);
			throw new Exception(msg);
		}
		return serverConfiguration;
	}

	protected void setServerConfiguration(
			ServerConfiguration serverConfiguration) {
		ServerRegistrationComponent.serverConfiguration = serverConfiguration;
	}

	protected void unsetServerConfiguration(
			ServerConfiguration serverConfiguration) {
		ServerRegistrationComponent.serverConfiguration = null;
	}

	public static ConfigurationContextService getConfigurationContextService() {
		if (ccServiceInstance == null) {
			String msg = "Before activating Registration bundle, an instance of "
					+ "UserRealm service should be in existance";
			log.error(msg);
			throw new RuntimeException(msg);
		}
		return ccServiceInstance;
	}

	protected void unsetConfigurationContextService(
			ConfigurationContextService contextService) {
		ccServiceInstance = null;
	}

	protected void setConfigurationContextService(
			ConfigurationContextService contextService) {
		ccServiceInstance = contextService;
	}

	protected void setRegistryService(RegistryService registryService) {
		registryServiceInstance = registryService;
	}

	protected void unsetRegistryService(RegistryService registryService) {
		registryServiceInstance = null;
	}

    protected void setListenerManager(ListenerManager listenerManager) {
    }

    protected void unsetListenerManager(ListenerManager listenerManager) {
    }

	public static RegistryService getRegistryService() throws Exception {
		if (registryServiceInstance == null) {
			String msg = "Before activating Carbon UI bundle, an instance of "
					+ "RegistryService should be in existance";
			log.error(msg);
			throw new Exception(msg);
		}
		return registryServiceInstance;
	}
}
