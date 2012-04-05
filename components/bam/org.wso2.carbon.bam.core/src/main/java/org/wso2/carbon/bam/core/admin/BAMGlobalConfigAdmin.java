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


package org.wso2.carbon.bam.core.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.core.persistence.BAMRegistryResources;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.bam.util.TimeRange;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * This class is used to store/retrieve global BAM server level parameters to the registry instance.
 */
public class BAMGlobalConfigAdmin {
    private static Log log = LogFactory.getLog(BAMGlobalConfigAdmin.class);
    protected Registry registry;

    public BAMGlobalConfigAdmin(Registry registry) {
        this.registry = registry;
    }

    public void updateDataRetentionPeriod(TimeRange timeRange) throws BAMException {
        try {
            Collection configCollection;
            if (registry.resourceExists(BAMRegistryResources.GLOBAL_CONFIG_PATH)) {
                configCollection = (Collection)registry.get(BAMRegistryResources.GLOBAL_CONFIG_PATH);
            } else {
                configCollection = registry.newCollection();
            }
            configCollection.setProperty(BAMRegistryResources.DATA_RETENTION_PROPERTY, timeRange.toString());
            registry.put(BAMRegistryResources.GLOBAL_CONFIG_PATH, configCollection);
        } catch (RegistryException e) {
            String msg = "Could not save the data retention policy in registry";
            log.error(msg);
            throw new BAMException(msg, e);
        }
    }

    public void updateDataArchivalPeriod(TimeRange timeRange) throws BAMException {
        try {
            Collection configCollection;
            if (registry.resourceExists(BAMRegistryResources.GLOBAL_CONFIG_PATH)) {
                configCollection = (Collection)registry.get(BAMRegistryResources.GLOBAL_CONFIG_PATH);
            } else {
                configCollection = registry.newCollection();
            }
            configCollection.setProperty(BAMRegistryResources.DATA_RETENTION_PROPERTY, timeRange.toString());
            configCollection.setProperty(BAMRegistryResources.DATA_ARCHIVAL_PROPERTY, timeRange.toString());
            registry.put(BAMRegistryResources.GLOBAL_CONFIG_PATH, configCollection);
        } catch (RegistryException e) {
            String msg = "Could not save the data retention policy in registry";
            log.error(msg, e);
            throw new BAMException(msg, e);
        }
    }

    public TimeRange getDataRetentionPeriod() throws BAMException {
        try {
        	if (registry.resourceExists(BAMRegistryResources.GLOBAL_CONFIG_PATH)) {
	            Collection configCollection = (Collection)registry.get(BAMRegistryResources.GLOBAL_CONFIG_PATH);
	            String period = configCollection.getProperty(BAMRegistryResources.DATA_RETENTION_PROPERTY);
	            return TimeRange.parseTimeRange(period);
        	}
        } catch (RegistryException e) {
            String msg = "Could not retrieve the data retention policy in registry";
            //log.error(msg);
            throw new BAMException(msg, e);
        }
        return null;
    }

    public TimeRange getDataArchivalPeriod() throws BAMException {
        try {
            if (registry.resourceExists(BAMRegistryResources.GLOBAL_CONFIG_PATH)) {
				Collection configCollection = (Collection) registry
						.get(BAMRegistryResources.GLOBAL_CONFIG_PATH);
				String period = configCollection
						.getProperty(BAMRegistryResources.DATA_ARCHIVAL_PROPERTY);
				return TimeRange.parseTimeRange(period);
			}
        } catch (RegistryException e) {
            String msg = "Could not retrieve the data archival policy in registry";
            //log.error(msg);
            throw new BAMException(msg, e);
        }
        return null;
    }
    
}
