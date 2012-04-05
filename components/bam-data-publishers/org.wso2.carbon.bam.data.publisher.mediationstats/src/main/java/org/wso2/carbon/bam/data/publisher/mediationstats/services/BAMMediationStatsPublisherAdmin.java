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
package org.wso2.carbon.bam.data.publisher.mediationstats.services;

import org.wso2.carbon.bam.data.publisher.mediationstats.config.MediationStatConfig;
import org.wso2.carbon.bam.data.publisher.mediationstats.config.RegistryPersistenceManager;
import org.wso2.carbon.core.AbstractAdmin;

/**
 * Admin service class to get user params
 *TODO removethis
 */
public class BAMMediationStatsPublisherAdmin extends AbstractAdmin {

    private RegistryPersistenceManager registryPersistenceManager;

    public BAMMediationStatsPublisherAdmin() {
        registryPersistenceManager = new RegistryPersistenceManager();
    }

    public void configureEventing(MediationStatConfig mediationStatConfig) {
        registryPersistenceManager.update(mediationStatConfig);
    }

    public MediationStatConfig getEventingConfigData() {
        return registryPersistenceManager.getEventingConfigData();
    }

}
