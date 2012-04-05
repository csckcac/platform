/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.bam.data.publisher.servicestats.config;

import org.wso2.carbon.bam.data.publisher.servicestats.ServiceStatisticsPublisherConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * Registry persistence manager handles persisting of eventing configuration data to the registry as well as
 * loading the configuration from the registry.
 */
public class RegistryPersistenceManager {
	private static Registry registry;

	private static EventingConfigData eventingConfigData = new EventingConfigData();

	public RegistryPersistenceManager() {
		load();
	}

	public static void setRegistry(Registry registryParam) {
		registry = registryParam;
	}

    /**
     * Fetches the value of the property with propertyName from registry. Returns null if no property
     * exists with the given name.
     * @param propertyName  Name of the property to be fetched.
     * @return
     * @throws RegistryException
     */
	public String getConfigurationProperty(String propertyName) throws RegistryException {
		String resourcePath = ServiceStatisticsPublisherConstants.STATISTISTICS_REG_PATH + propertyName;
		String value = null;
		if (registry.resourceExists(resourcePath)) {
			Resource resource = registry.get(resourcePath);
			value = resource.getProperty(propertyName);
		}
		return value;
	}

    /**
     * Updates configuration property to a new value.
     * @param propertyName Name of the property to be updated.
     * @param value New value of the property
     * @throws RegistryException
     */
	public void updateConfigurationProperty(String propertyName, String value) throws RegistryException {
		String resourcePath = ServiceStatisticsPublisherConstants.STATISTISTICS_REG_PATH + propertyName;
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
		eventingConfigData.setEnableEventing(ServiceStatisticsPublisherConstants.ENABLE_EVENTING_DEFAULT);
		eventingConfigData
				.setSystemRequestCountThreshold(
                        ServiceStatisticsPublisherConstants.SYSTEM_REQUEST_COUNT_THRESHOLD_DEFAULT);

		// then load it from registry
		try {
			String eventingStatus = getConfigurationProperty(
                    ServiceStatisticsPublisherConstants.ENABLE_EVENTING);
			if (eventingStatus != null) { // Registry has eventing config
				eventingConfigData.setEnableEventing(eventingStatus);
				eventingConfigData
						.setSystemRequestCountThreshold(Integer.parseInt(getConfigurationProperty(
                                ServiceStatisticsPublisherConstants.SYSTEM_REQUEST_COUNT_THRESHOLD)));
			} else { // Registry does not have eventing config. Set to defaults.
				update(eventingConfigData);
			}
		} catch (Exception ignored) {
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

		updateConfigurationProperty(ServiceStatisticsPublisherConstants.ENABLE_EVENTING, eventingConfigData
				.getEnableEventing());
		updateConfigurationProperty(ServiceStatisticsPublisherConstants.SYSTEM_REQUEST_COUNT_THRESHOLD,
				Integer.toString(eventingConfigData.getSystemRequestCountThreshold()));

		RegistryPersistenceManager.eventingConfigData = eventingConfigData;
	}

	public EventingConfigData getEventingConfigData() {
		return eventingConfigData;
	}

}
