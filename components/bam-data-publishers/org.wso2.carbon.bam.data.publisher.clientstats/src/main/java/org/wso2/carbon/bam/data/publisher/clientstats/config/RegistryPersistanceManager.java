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

package org.wso2.carbon.bam.data.publisher.clientstats.config;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.bam.data.publisher.clientstats.ClientStatisticsPublisherConstants;
/**
 * Registry persistence manager handles persisting of eventing configuration data to the registry as well as
 * loading the configuration from the registry.
 */
public class RegistryPersistanceManager {
	private static Registry registry;

	private static EventingConfigData eventingConfigData = new EventingConfigData();

	public RegistryPersistanceManager() {
		load();
	}

	public static void setRegistry(Registry registryParam) {
		registry = registryParam;
	}

	public String getConfigurationProperty(String propertyName) throws RegistryException {
		String resourcePath = ClientStatisticsPublisherConstants.STATISTISTICS_REG_PATH
                + propertyName;
		String value = null;
		if (registry.resourceExists(resourcePath)) {
			Resource resource = registry.get(resourcePath);
			value = resource.getProperty(propertyName);
		}
		return value;
	}

	public void updateConfigurationProperty(String propertyName, String value) throws RegistryException {
		String resourcePath = ClientStatisticsPublisherConstants.STATISTISTICS_REG_PATH
                + propertyName;
		Resource resource;
		if (!registry.resourceExists(resourcePath)) {
			resource = registry.newResource();
			resource.addProperty(propertyName, value);
			registry.put(resourcePath, resource);
		} else {
			resource = registry.get(resourcePath);
			resource.setProperty(propertyName, value);
			registry.put(resourcePath, resource);
		}
	}

	/**
	 * Loads configuration from Registry.
	 */
	private void load() {

		// First set it to defaults, but do not persist
        eventingConfigData
                .setEnableEventing(ClientStatisticsPublisherConstants.ENABLE_EVENTING_DEFAULT);
	
		// then load it from registry
		try {
			String eventingStatus = getConfigurationProperty(ClientStatisticsPublisherConstants.ENABLE_EVENTING);
			if (eventingStatus != null) { // Registry has eventing config
				eventingConfigData.setEnableEventing(eventingStatus);
			
			} else { // Registry does not have eventing config
				update(eventingConfigData);
			}
		} catch (Exception e) {
			// If something went wrong, then we have the default, or whatever loaded so far
		}
	}

	/**
	 * Updates the Registry with given config data.
	 * 
	 * @param eventingConfigData
	 *            eventing configuration data
	 * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
	 *             thrown when updating the registry properties fails.
	 */
	public void update(EventingConfigData eventingConfigData) throws RegistryException {

		updateConfigurationProperty(ClientStatisticsPublisherConstants.ENABLE_EVENTING,
                                    eventingConfigData
				.getEnableEventing());
		
		RegistryPersistanceManager.eventingConfigData = eventingConfigData;
	}

	public EventingConfigData getEventingConfigData() {
		return eventingConfigData;
	}

}
