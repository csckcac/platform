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
package org.wso2.carbon.apimgt.usage.publisher.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.usage.publisher.service.APIMGTConfigReaderService;

/**
 * @scr.component name="api.mgt.usage.component" immediate="true"
 * @scr.reference name="api.manager.config.service"
 * interface="org.wso2.carbon.apimgt.impl.APIManagerConfigurationService" cardinality="1..1"
 * policy="dynamic" bind="setAPIManagerConfigurationService" unbind="unsetAPIManagerConfigurationService"
 */
public class UsageComponent {

    private static final Log log = LogFactory.getLog(UsageComponent.class);

    private static APIMGTConfigReaderService apimgtConfigReaderService;
    private static APIManagerConfigurationService amConfigService;

    protected void activate(ComponentContext ctx) {
        try {
            apimgtConfigReaderService = new APIMGTConfigReaderService(amConfigService.getAPIManagerConfiguration());
            BundleContext bundleContext = ctx.getBundleContext();
            bundleContext.registerService(APIMGTConfigReaderService.class.getName(),
                                          apimgtConfigReaderService, null);
            log.debug("API Management Usage Publisher bundle is activated ");
        } catch (Throwable e) {
            log.error("API Management Usage Publisher bundle ", e);
        }
    }

    protected void deactivate(ComponentContext ctx) {

    }

    protected void setAPIManagerConfigurationService(APIManagerConfigurationService service) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service bound to the API usage handler");
        }
        amConfigService = service;
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService service) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service unbound from the API usage handler");
        }
        amConfigService = null;
    }

    public static APIMGTConfigReaderService getApiMgtConfigReaderService() {
        return apimgtConfigReaderService;
    }
}
