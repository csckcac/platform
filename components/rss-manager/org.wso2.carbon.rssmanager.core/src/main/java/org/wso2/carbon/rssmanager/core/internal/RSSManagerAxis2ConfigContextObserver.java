/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.rssmanager.core.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.rssmanager.core.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.manager.RSSManager;
import org.wso2.carbon.rssmanager.core.internal.util.RSSConfig;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;

/**
 * This class loads the tenant specific data.
 */
public class RSSManagerAxis2ConfigContextObserver extends AbstractAxis2ConfigurationContextObserver{

    private static final Log log= LogFactory.getLog(RSSManagerAxis2ConfigContextObserver.class);

    public void createdConfigurationContext(ConfigurationContext configurationContext){
        int tid = SuperTenantCarbonContext.getCurrentContext(
                configurationContext).getTenantId();
        try {
            SuperTenantCarbonContext.startTenantFlow();
            SuperTenantCarbonContext.getCurrentContext().setTenantId(tid);

            /* Initializes tenant's RSS metadata repository */
            this.getRSSManager().initTenant(tid);
        } catch (Exception e) {
            log.error("Error occurred while loading tenant RSS configurations ", e);
        } finally {
            SuperTenantCarbonContext.endTenantFlow();
        }
    }

    public void terminatingConfigurationContext(ConfigurationContext configurationContext) {
        int tid = CarbonContext.getCurrentContext().getTenantId();
        try {
            this.getRSSManager().setMetaDataRepository(tid, null);
        } catch (RSSManagerException e) {
            log.error("Error occurred while unsetting tenant RSS metadata repository");
        }
    }

    private RSSManager getRSSManager() throws RSSManagerException {
        RSSConfig config = RSSConfig.getInstance();
        if (config == null) {
            throw new RSSManagerException("RSSConfig is not properly initialized and is null");
        }
        return config.getRssManager();
    }
    
}
