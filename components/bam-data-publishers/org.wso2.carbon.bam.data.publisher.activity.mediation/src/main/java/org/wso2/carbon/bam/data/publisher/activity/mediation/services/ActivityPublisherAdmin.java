/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.data.publisher.activity.mediation.services;

import org.wso2.carbon.bam.data.publisher.activity.mediation.ActivityPublisherUtils;
import org.wso2.carbon.bam.data.publisher.activity.mediation.config.EventingConfigData;
import org.wso2.carbon.bam.data.publisher.activity.mediation.config.RegistryPersistanceManager;
import org.wso2.carbon.bam.data.publisher.activity.mediation.config.XPathConfigData;
import org.wso2.carbon.core.AbstractAdmin;

public class ActivityPublisherAdmin extends AbstractAdmin {
	private RegistryPersistanceManager registryPersistanceManager;

	public ActivityPublisherAdmin() {
		registryPersistanceManager = new RegistryPersistanceManager();
	}

	public void configureEventing(EventingConfigData eventingConfigData) throws Exception {
		registryPersistanceManager.update(eventingConfigData);
        ActivityPublisherUtils.getActivityQueue().setThreshold(eventingConfigData.getMessageThreshold());
	}

	public EventingConfigData getEventingConfigData() {
        return registryPersistanceManager.getEventingConfigData();
    }

    /**
     * Stores xpath configuration data in to the registry. Upon successful storage fires an event to BAM
     * notifying the changes to xpath configuration.
     *
     * @param xpathConfigData
     * @throws Exception
     */
    public void configureXPathData(XPathConfigData xpathConfigData) throws Exception {
        registryPersistanceManager.update(xpathConfigData);
/*        try {
            PublisherUtils.publishXPathConfigurations(xpathConfigData);
        } catch (Exception e) {
            registryPersistanceManager.rollback(xpathConfigData);
            throw e;
        }*/

    }

    public XPathConfigData[] getXPathData() throws Exception {
        return registryPersistanceManager.getXPathConfigData();
    }

    public void deleteXPathData(XPathConfigData xpathConfigData) throws Exception {
        registryPersistanceManager.rollback(xpathConfigData);
    }

}