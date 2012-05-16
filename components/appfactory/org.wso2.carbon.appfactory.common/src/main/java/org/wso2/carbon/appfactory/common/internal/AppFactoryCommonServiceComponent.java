/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.wso2.carbon.appfactory.common.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;

/**
 * @scr.component name="appfactory.common" immediate="true"
 */
public class AppFactoryCommonServiceComponent {

    private static final Log log = LogFactory.getLog(AppFactoryCommonServiceComponent.class);

    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        AppFactoryConfiguration configuration;
        try {
            configuration = AppFactoryUtil.loadAppFactoryConfiguration();
            AppFactoryUtil.setAppFactoryConfiguration(configuration);
            bundleContext.registerService(AppFactoryConfiguration.class.getName(), configuration, null);
        } catch (AppFactoryException e) {
            log.error("Error in loading " + AppFactoryConstants.APPFACTORY_CONFIG_FILE_NAME);
        }
        if (log.isDebugEnabled()) {
            log.debug("Appfactory common bundle is activated");
        }
    }

    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Appfactory common bundle is deactivated");
        }
    }
}
