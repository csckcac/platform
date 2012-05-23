/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.tenant.registration.email.sender.util;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.email.verification.util.EmailVerifcationSubscriber;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.stratos.common.constants.StratosConstants;
import org.wso2.carbon.stratos.common.util.ClaimsMgtUtil;
import org.wso2.carbon.stratos.common.util.CommonUtil;
import org.wso2.carbon.tenant.mgt.internal.TenantMgtServiceComponent;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.AuthenticationObserver;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Utility methods for the email sender component
 */
public class Util {

    private static RegistryService registryService;
    private static RealmService realmService;
    private static ConfigurationContextService configurationContextService;
    private static EmailVerifcationSubscriber emailVerificationService;
    private static final Log log = LogFactory.getLog(Util.class);


    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        Util.configurationContextService = configurationContextService;
    }

    public static ConfigurationContext getConfigurationContext() {
        if (configurationContextService.getServerConfigContext() == null) {
            return null;
        }
        return configurationContextService.getServerConfigContext();
    }

    public static synchronized void setRegistryService(RegistryService service) {
        if (registryService == null) {
            registryService = service;
        }
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static synchronized void setRealmService(RealmService service) {
        if (realmService == null) {
            realmService = service;
        }
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    public static TenantManager getTenantManager() {
        return realmService.getTenantManager();
    }

    public static RealmConfiguration getBootstrapRealmConfiguration() {
        return realmService.getBootstrapRealmConfiguration();
    }

    public static UserRegistry getGovernanceSystemRegistry(int tenantId) throws RegistryException {
        return registryService.getGovernanceSystemRegistry(tenantId);
    }
    
    public static void setEmailVerificationService(EmailVerifcationSubscriber emailService) {
        emailVerificationService = emailService;
    }

    /**
     * Replace the {place-holders} with the respective value provided
     * @param text string
     * @param userParameters - map of user parameters
     * @return replaced text for the email {title or body}
     */
    public static String replacePlaceHolders(String text, Map<String, String> userParameters) {
        if (userParameters != null) {
            for (Map.Entry<String, String> entry : userParameters.entrySet()) {
                String key = entry.getKey();
                text = text.replaceAll("\\{" + key + "\\}", Matcher.quoteReplacement(entry.getValue()));
            }
        }
        return text;
    }
    
    /**
     * Sends validation mail to the tenant admin upon the tenant creation
     *
     * @param tenant            - the registered tenant
     * @param originatedService - originated service of the registration request
     * @throws Exception, if the sending mail failed
     */
    public static void sendEmail(Tenant tenant, String originatedService) throws Exception {
        String firstname = ClaimsMgtUtil.getFirstName(realmService, tenant, tenant.getId());
        String adminName = ClaimsMgtUtil.getAdminUserNameFromTenantId(realmService, tenant.getId());

        String confirmationKey = generateConfirmationKey(
                tenant, originatedService, registryService.getConfigSystemRegistry(
                MultitenantConstants.SUPER_TENANT_ID), tenant.getId());

        if (CommonUtil.isTenantActivationModerated()) {
            requestSuperTenantModification(tenant, confirmationKey, firstname, adminName);
        } else {
            //request for verification
            requestUserVerification(tenant, confirmationKey, firstname, adminName);
        }
    }
    
    /**
     * generates the confirmation key for the tenant
     *
     * @param tenant            - a tenant
     * @param originatedService - originated service of the registration
     * @param superTenantConfigSystemRegistry
     *                          - super tenant config system registry.
     * @param tenantId          tenantId
     * @return confirmation key
     * @throws RegistryException if generation of the confirmation key failed.
     */
    private static String generateConfirmationKey(Tenant tenant, String originatedService,
                                                  UserRegistry superTenantConfigSystemRegistry,
                                                  int tenantId) throws RegistryException {
        // generating the confirmation key
        String confirmationKey = UUIDGenerator.generateUUID();
        UserRegistry superTenantGovernanceSystemRegistry;
        try {
            superTenantGovernanceSystemRegistry =
                registryService.getGovernanceSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
        } catch (RegistryException e) {
            String msg = "Exception in getting the governance system registry for the super tenant";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        Resource resource;
        String emailVerificationPath = StratosConstants.ADMIN_EMAIL_VERIFICATION_FLAG_PATH +
                                       RegistryConstants.PATH_SEPARATOR + tenantId;
        try {
            if (superTenantGovernanceSystemRegistry.resourceExists(emailVerificationPath)) {
                resource = superTenantGovernanceSystemRegistry.get(emailVerificationPath);
            } else {
                resource = superTenantGovernanceSystemRegistry.newResource();
            }
            resource.setContent(confirmationKey);
        } catch (RegistryException e) {
            String msg = "Error in creating the resource or getting the resource" +
                         "from the email verification path";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        // email is not validated yet, this prop is used to activate the tenant
        // later.
        resource.addProperty(StratosConstants.IS_EMAIL_VALIDATED, "false");
        resource.addProperty(StratosConstants.TENANT_ADMIN, tenant.getAdminName());
        try {
            superTenantGovernanceSystemRegistry.put(emailVerificationPath, resource);
        } catch (RegistryException e) {
            String msg = "Error in putting the resource to the super tenant registry" +
                         " for the email verification path";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

        // Used for * as a Service impl.
        // Store the cloud service from which the register req. is originated.
        if (originatedService != null) {
            String originatedServicePath =
                    StratosConstants.ORIGINATED_SERVICE_PATH +
                    StratosConstants.PATH_SEPARATOR +
                    StratosConstants.ORIGINATED_SERVICE +
                    StratosConstants.PATH_SEPARATOR + tenantId;
            try {
                Resource origServiceRes = superTenantConfigSystemRegistry.newResource();
                origServiceRes.setContent(originatedService);
                superTenantGovernanceSystemRegistry.put(originatedServicePath, origServiceRes);
            } catch (RegistryException e) {
                String msg = "Error in putting the originated service resource "
                             + "to the governance registry";
                log.error(msg, e);
                throw new RegistryException(msg, e);
            }
        }
        initializeRegistry(tenant.getId());
        if (log.isDebugEnabled()) {
            log.debug("Successfully generated the confirmation key.");
        }
        return confirmationKey;
    }
    
    /**
     * Sends mail for the super tenant for the account moderation. Once super tenant clicks the
     * link provided in the email, the tenant will be activated.
     *
     * @param tenant          the tenant who registered an account
     * @param confirmationKey confirmation key.
     * @param firstname       calling name of the tenant
     * @param adminName       the tenant admin name
     * @throws Exception if an exception is thrown from EmailVerificationSubscriber.
     */
    private static void requestSuperTenantModification(Tenant tenant, String confirmationKey,
                                                String firstname,
                                                String adminName) throws Exception {
        try {
            Map<String, String> dataToStore = new HashMap<String, String>();
            dataToStore.put("email", CommonUtil.getSuperAdminEmail());
            dataToStore.put("first-name", firstname);
            dataToStore.put("admin", adminName);
            dataToStore.put("tenantDomain", tenant.getDomain());
            dataToStore.put("confirmationKey", confirmationKey);

            emailVerificationService.requestUserVerification(
                    dataToStore, TenantMgtServiceComponent.getSuperTenantEmailVerifierConfig());
            if (log.isDebugEnabled()) {
                log.debug("Email verification for the tenant registration.");
            }
        } catch (Exception e) {
            String msg = "Error in notifying the super tenant on the account creation for " +
                         "the domain: " + tenant.getDomain();
            log.error(msg);
            throw new Exception(msg, e);
        }
    }
    
    /**
     * request email verification from the user.
     *
     * @param tenant          a tenant
     * @param confirmationKey confirmation key.
     * @param firstname       calling name
     * @throws Exception if an exception is thrown from EmailVerificationSubscriber.
     */
    private static void requestUserVerification(Tenant tenant, String confirmationKey,
                                                String firstname, String adminName) throws Exception {
        try {
            Map<String, String> dataToStore = new HashMap<String, String>();
            dataToStore.put("email", tenant.getEmail());
            dataToStore.put("first-name", firstname);
            dataToStore.put("admin", adminName);
            dataToStore.put("tenantDomain", tenant.getDomain());
            dataToStore.put("confirmationKey", confirmationKey);

            EmailVerifcationSubscriber emailVerifier =
                    TenantMgtServiceComponent.getEmailVerificationService();
            emailVerifier.requestUserVerification(
                    dataToStore, TenantMgtServiceComponent.getEmailVerifierConfig());
            if (log.isDebugEnabled()) {
                log.debug("Email verification for the tenant registration.");
            }
        } catch (Exception e) {
            String msg = "Error in notifying tenant of domain: " + tenant.getDomain();
            log.error(msg);
            throw new Exception(msg, e);
        }
    }
    
    /**
     * Initializes the registry for the tenant.
     *
     * @param tenantId tenant id.
     */
    private static void initializeRegistry(int tenantId) {
        BundleContext bundleContext = TenantMgtServiceComponent.getBundleContext();
        if (bundleContext != null) {
            ServiceTracker tracker =
                    new ServiceTracker(bundleContext,
                                       AuthenticationObserver.class.getName(),
                                       null);
            tracker.open();
            Object[] services = tracker.getServices();
            if (services != null) {
                for (Object service : services) {
                    ((AuthenticationObserver) service).startedAuthentication(tenantId);
                }
            }
            tracker.close();
        }
    }
}
