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
package org.wso2.carbon.tenant.mgt.email.sender.util;

import org.wso2.carbon.email.sender.api.EmailSender;
import org.wso2.carbon.email.sender.api.EmailSenderConfiguration;
import org.wso2.carbon.email.verification.util.EmailVerifcationSubscriber;
import org.wso2.carbon.email.verification.util.EmailVerifierConfig;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.stratos.common.constants.StratosConstants;
import org.wso2.carbon.stratos.common.util.ClaimsMgtUtil;
import org.wso2.carbon.stratos.common.util.CommonUtil;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.AuthenticationObserver;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Utility methods for the email sender component
 */
public class TenantMgtEmailSenderUtil {

    private static final Log log = LogFactory.getLog(TenantMgtEmailSenderUtil.class);
    
    private static EmailSender successMsgSender;
    private static EmailSender tenantCreationNotifier;
    private static EmailSender tenantActivationNotifier;
    private static EmailVerifierConfig emailVerifierConfig = null;
    private static EmailVerifierConfig superTenantEmailVerifierConfig = null;
    
    /**
     * Sends validation mail to the tenant admin upon the tenant creation
     *
     * @param tenant            - the registered tenant
     * @param originatedService - originated service of the registration request
     * @throws Exception, if the sending mail failed
     */
    public static void sendEmail(Tenant tenant, String originatedService) throws Exception {
        String firstname = ClaimsMgtUtil.getFirstName(DataHolder.getRealmService(), 
                tenant, tenant.getId());
        String adminName = ClaimsMgtUtil.getAdminUserNameFromTenantId(DataHolder.getRealmService(), 
                tenant.getId());

        String confirmationKey = generateConfirmationKey(
                tenant, originatedService, DataHolder.getRegistryService().getConfigSystemRegistry(
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
                DataHolder.getRegistryService().getGovernanceSystemRegistry(
                        MultitenantConstants.SUPER_TENANT_ID);
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

            DataHolder.getEmailVerificationService().requestUserVerification(
                    dataToStore, superTenantEmailVerifierConfig);
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

            EmailVerifcationSubscriber emailVerifier = DataHolder.getEmailVerificationService();
            emailVerifier.requestUserVerification(dataToStore, emailVerifierConfig);
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
        BundleContext bundleContext = DataHolder.getBundleContext();
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
    
    public static void init(){
        
        // setting the success message config
        String confFilename = CarbonUtils.getCarbonConfigDirPath() + File.separator
                              +StratosConstants.EMAIL_CONFIG+
                              File.separator+"email-registration-complete.xml";
        EmailSenderConfiguration successMsgConfig =
                EmailSenderConfiguration.loadEmailSenderConfiguration(confFilename);
        successMsgSender = new EmailSender(successMsgConfig);

        loadSuperTenantNotificationEmailConfig();

        loadEmailVerificationConfig();
        
    }
    
    /**
     * loads the notification configurations for the mail to super tenant for account creations
     * and activations.
     */
    private static void loadSuperTenantNotificationEmailConfig() {
        // Tenant Registration Email Configurations
        String tenantRegistrationEmailConfFile = CarbonUtils.getCarbonConfigDirPath()+ File.separator
                                                 + StratosConstants.EMAIL_CONFIG +
                                                 File.separator+"email-new-tenant-registration.xml";
        EmailSenderConfiguration newTenantRegistrationEmailConf =
                EmailSenderConfiguration.loadEmailSenderConfiguration(
                        tenantRegistrationEmailConfFile);
        tenantCreationNotifier = new EmailSender(newTenantRegistrationEmailConf);

        // Tenant Activation Email Configurations
        String tenantActivationEmailConfFile = CarbonUtils.getCarbonConfigDirPath()+ File.separator
                                               +StratosConstants.EMAIL_CONFIG +
                                               File.separator+"email-new-tenant-activation.xml";
        EmailSenderConfiguration newTenantActivationEmailConf =
                EmailSenderConfiguration.loadEmailSenderConfiguration(
                        tenantActivationEmailConfFile);
        tenantActivationNotifier = new EmailSender(newTenantActivationEmailConf);
    }
    
    /**
     * loads the Email configuration files to be sent on the tenant registrations.
     */
    private static void loadEmailVerificationConfig() {
        String confXml = CarbonUtils.getCarbonConfigDirPath()
                         + File.separator
                         +StratosConstants.EMAIL_CONFIG+File.separator+ "email-registration.xml";
        try {
        emailVerifierConfig =
                org.wso2.carbon.email.verification.util.Util.loadeMailVerificationConfig(confXml);
        } catch(Exception e) {
            String msg = "Email Registration Configuration file not found. " +
                         "Pls check the repository/conf/email folder.";
            log.error(msg);
        }
        String superTenantConfXml = CarbonUtils.getCarbonConfigDirPath() + File.separator
                                    +StratosConstants.EMAIL_CONFIG+File.separator+
                                    "email-registration-moderation.xml";
        try {
        superTenantEmailVerifierConfig =
                org.wso2.carbon.email.verification.util.Util.loadeMailVerificationConfig(
                        superTenantConfXml);
        } catch(Exception e) {
            String msg = "Email Moderation Configuration file not found. " +
                         "Pls check the repository/conf/email folder.";
            log.error(msg);
        }
    }
    
    /**
     * Emails the tenant admin notifying the account creation.
     *
     * @param domainName tenant domain
     * @param adminName  tenant admin
     * @param email      associated tenant email address
     */
    public static void notifyTenantCreation(String domainName, String adminName, String email) {
        TenantManager tenantManager = DataHolder.getTenantManager();
        String firstName = "";
        try {
            int tenantId = tenantManager.getTenantId(domainName);
            Tenant tenant = (Tenant) tenantManager.getTenant(tenantId);
            firstName = ClaimsMgtUtil.getFirstName(DataHolder.getRealmService(), tenant, tenantId);
        } catch (Exception e) {
            String msg = "Unable to get the tenant with the tenant domain";
            log.error(msg, e);
            // just catch from here.
        }

        // load the mail configuration
        Map<String, String> userParams = new HashMap<String, String>();
        userParams.put("first-name", firstName);
        userParams.put("admin-name", adminName);
        userParams.put("domain-name", domainName);

        try {
            successMsgSender.sendEmail(email, userParams);
        } catch (Exception e) {
            // just catch from here..
            String msg = "Error in sending the notification email.";
            log.error(msg, e);
        }
    }
    
    /**
     * Emails the super admin notifying the account creation for a new tenant.
     *
     * @param domainName tenant domain
     * @param adminName  tenant admin
     * @param email      tenant's email address
     */
    public static void notifyTenantCreationToSuperAdmin(
            String domainName, String adminName, String email) {
        String notificationEmailAddress = CommonUtil.getNotificationEmailAddress();

        if (notificationEmailAddress.trim().equals("")) {
            if (log.isDebugEnabled()) {
                log.debug("No super-admin notification email address is set to notify upon a" +
                          " tenant registration");
            }
            return;
        }

        Map<String, String> userParams = initializeSuperTenantNotificationParams(
                domainName, adminName, email);

        try {
            tenantCreationNotifier.sendEmail(notificationEmailAddress, userParams);
        } catch (Exception e) {
            // just catch from here..
            String msg = "Error in sending the notification email.";
            log.error(msg, e);
        }
    }
    
    /**
     * Emails the super admin notifying the account activation for an unactivated tenant.
     *
     * @param domainName tenant domain
     * @param adminName  tenant admin
     * @param email      tenant's email address
     */
    public static void notifyTenantActivationToSuperAdmin(
            String domainName, String adminName, String email) {
        String notificationEmailAddress = CommonUtil.getNotificationEmailAddress();

        if (notificationEmailAddress.trim().equals("")) {
            if (log.isDebugEnabled()) {
                log.debug("No super-admin notification email address is set to notify upon a" +
                          " tenant activation");
            }
            return;
        }

        Map<String, String> userParams = initializeSuperTenantNotificationParams(
                domainName, adminName, email);

        try {
            tenantActivationNotifier.sendEmail(notificationEmailAddress, userParams);
        } catch (Exception e) {
            // just catch from here..
            String msg = "Error in sending the notification email.";
            log.error(msg, e);
        }
    }
    


    /**
     * Initializes the super tenant notification parameters
     *
     * @param domainName - tenant domain
     * @param adminName  - tenant admin
     * @param email      - tenant email
     * @return the parameters
     */
    private static Map<String, String> initializeSuperTenantNotificationParams(
            String domainName, String adminName, String email) {
        TenantManager tenantManager = DataHolder.getTenantManager();
        String firstName = "";
        String lastName = "";
        try {
            int tenantId = tenantManager.getTenantId(domainName);
            Tenant tenant = (Tenant) tenantManager.getTenant(tenantId);
            firstName = ClaimsMgtUtil.getFirstName(DataHolder.getRealmService(), tenant, tenantId);
            lastName = ClaimsMgtUtil.getLastName(DataHolder.getRealmService(), tenant, tenantId);

        } catch (Exception e) {
            String msg = "Unable to get the tenant with the tenant domain";
            log.error(msg, e);
            // just catch from here.
        }

        // load the mail configuration
        Map<String, String> userParams = new HashMap<String, String>();
        userParams.put("admin-name", adminName);
        userParams.put("domain-name", domainName);
        userParams.put("email-address", email);
        userParams.put("first-name", firstName);
        userParams.put("last-name", lastName);
        return userParams;
    }
}
