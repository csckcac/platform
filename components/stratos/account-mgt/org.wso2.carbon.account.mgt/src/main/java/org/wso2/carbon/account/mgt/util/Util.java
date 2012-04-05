/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.account.mgt.util;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.email.verification.util.EmailVerifcationSubscriber;
import org.wso2.carbon.email.verification.util.EmailVerifierConfig;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.stratos.common.constants.StratosConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;

/**
 * Util methods for AccountMgt
 */
public class Util {

    private static final Log log = LogFactory.getLog(Util.class);

    private static RegistryService registryService;
    private static RealmService realmService;
    private static EmailVerifcationSubscriber emailVerificationService = null;
    private static EmailVerifierConfig emailVerfierConfig = null;
    private static ServiceTracker tenantMgtListenerServiceTracker = null;

    public static synchronized void setRegistryService(RegistryService service) {
        if (registryService == null) {
            registryService = service;
        }
    }

    
    public static RealmService getRealmService() {
        return realmService;
    }


    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static synchronized void setEmailVerificationService(EmailVerifcationSubscriber service) {
        if (emailVerificationService == null) {
            emailVerificationService = service;
        }
    }

    public static EmailVerifcationSubscriber getEmailVerificationService() {
        return emailVerificationService;
    }


    public static synchronized void setRealmService(RealmService service) {
        if (realmService == null) {
            realmService = service;
        }
    }


    public static TenantManager getTenantManager() {
        return realmService.getTenantManager();
    }

    public static UserRegistry getGovernanceSystemRegistry(int tenantId) throws RegistryException {
        return registryService.getGovernanceSystemRegistry(tenantId);
    }

    public static HttpSession getRequestSession() throws RegistryException {
        MessageContext messageContext = MessageContext.getCurrentMessageContext();
        if (messageContext == null) {
            String msg = "Could not get the user's session. Message context not found.";
            log.error(msg);
            throw new RegistryException(msg);
        }

        HttpServletRequest request =
                (HttpServletRequest) messageContext.getProperty("transport.http.servletRequest");

        return request.getSession();
    }

    public static void loadEmailVerificationConfig() {
        String configXml = CarbonUtils.getCarbonConfigDirPath()+ File.separator
                           + StratosConstants.EMAIL_CONFIG +File.separator +"email-update.xml";
        emailVerfierConfig = org.wso2.carbon.email.verification.util.Util.loadeMailVerificationConfig(configXml);
    }

    public static EmailVerifierConfig getEmailVerifierConfig() {
        return emailVerfierConfig;
    }

    // related to service trackers on tenant renaming
    public static void registerTenantMgtListenerServiceTrackers(BundleContext bundleContext) {
        tenantMgtListenerServiceTracker = new ServiceTracker(bundleContext,
                TenantMgtListener.class.getName(), null);
        tenantMgtListenerServiceTracker.open();

    }

    public static void unregisterTenantMgtListenerServiceTrackers() {
        tenantMgtListenerServiceTracker.close();
    }

    public static void alertTenantRenames(int tenantId, String oldName, String newName)
            throws UserStoreException {
        Object[] tenantMgtListeners = tenantMgtListenerServiceTracker.getServices();

        for (Object tenantMgtListenerObj : tenantMgtListeners) {
            TenantMgtListener tenantMgtLister = (TenantMgtListener) tenantMgtListenerObj;
            tenantMgtLister.renameTenant(tenantId, oldName, newName);
        }
    }
}
