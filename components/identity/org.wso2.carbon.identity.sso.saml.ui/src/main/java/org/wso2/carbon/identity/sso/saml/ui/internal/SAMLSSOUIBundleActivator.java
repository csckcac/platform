/*
* Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.identity.sso.saml.ui.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.sso.saml.ui.util.SAMLSSOUIUtil;


public class SAMLSSOUIBundleActivator implements BundleActivator {
    private static Log log = LogFactory.getLog(SAMLSSOUIBundleActivator.class);
    private static int defaultSingleLogoutRetryCount = 5;
    private static long defaultSingleLogoutRetryInterval = 60000;

    public void start(BundleContext bundleContext) {
        try {
            IdentityUtil.populateProperties();
            SAMLSSOUIUtil.setSingleLogoutRetryCount(Integer.parseInt(
                    IdentityUtil.getProperty(IdentityConstants.ServerConfig.SINGLE_LOGOUT_RETRY_COUNT)));
            SAMLSSOUIUtil.setSingleLogoutRetryInterval(Long.parseLong(IdentityUtil.getProperty(
                    IdentityConstants.ServerConfig.SINGLE_LOGOUT_RETRY_INTERVAL)));
            log.debug("Single logout retry count is set to " + SAMLSSOUIUtil.getSingleLogoutRetryCount());
            log.debug("Single logout retry interval is set to " +
                      SAMLSSOUIUtil.getSingleLogoutRetryInterval() + " in seconds.");
        } catch (Exception e) {
            SAMLSSOUIUtil.setSingleLogoutRetryCount(defaultSingleLogoutRetryCount);
            SAMLSSOUIUtil.setSingleLogoutRetryInterval(defaultSingleLogoutRetryInterval);
            if (log.isDebugEnabled()) {
                log.debug("Failed to activate SAMLSSOUIBundle Activator," +
                        " which load the single logout retry count and interval values." +
                        " And default values for retry count: " + defaultSingleLogoutRetryCount +
                        " and interval: " + defaultSingleLogoutRetryInterval + " will be used.");
            }
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {

    }
}

