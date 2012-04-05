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
package org.wso2.carbon.tenant.mgt.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.core.multitenancy.persistence.TenantPersistor;
import org.wso2.carbon.email.sender.api.EmailSender;
import org.wso2.carbon.email.sender.api.EmailSenderConfiguration;
import org.wso2.carbon.email.verification.util.EmailVerifcationSubscriber;
import org.wso2.carbon.email.verification.util.EmailVerifierConfig;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.stratos.common.TenantBillingService;
import org.wso2.carbon.stratos.common.constants.StratosConstants;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.stratos.common.util.CommonUtil;
import org.wso2.carbon.stratos.common.util.StratosConfiguration;
import org.wso2.carbon.tenant.mgt.internal.util.PasswordUtil;
import org.wso2.carbon.tenant.mgt.internal.util.TenantMgtRampartUtil;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @scr.component name="org.wso2.carbon.tenant.mgt" immediate="true"
 * @scr.reference name="registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService"
 *                unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" bind="setRealmService"
 *                unbind="unsetRealmService"
 * @scr.reference name="emailverification.service" interface=
 *                "org.wso2.carbon.email.verification.util.EmailVerifcationSubscriber"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setEmailVerificationService"
 *                unbind="unsetEmailVerificationService"
 * @scr.reference name="configuration.context.service"
 *                interface="org.wso2.carbon.utils.ConfigurationContextService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setConfigurationContextService"
 *                unbind="unsetConfigurationContextService"
 * @scr.reference name="org.wso2.carbon.tenant.mgt.listener.service"
 *                interface="org.wso2.carbon.stratos.common.listeners.TenantMgtListener"
 *                cardinality="0..n" policy="dynamic"
 *                bind="setTenantMgtListenerService"
 *                unbind="unsetTenantMgtListenerService"
 * @scr.reference name="default.tenant.billing.service"
 *                interface="org.wso2.carbon.stratos.common.TenantBillingService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setTenantBillingService"
 *                unbind="unsetTenantBillingService"
 * @scr.reference name="default.tenant.persistor"
 *                interface="org.wso2.carbon.core.multitenancy.persistence.TenantPersistor"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setTenantPersistor"
 *                unbind="unsetTenantPersistor"
 */
public class TenantMgtServiceComponent {
    private static Log log = LogFactory.getLog(TenantMgtServiceComponent.class);

    private static final String GAPP_TENANT_REG_SERVICE_NAME = "GAppTenantRegistrationService";
    
    private static BundleContext bundleContext;
    private static RealmService realmService;
    private static RegistryService registryService;
    private static EmailVerifcationSubscriber emailVerificationService = null;
    private static ConfigurationContextService configurationContextService;
    private static EmailSender successMsgSender;
    private static EmailSender tenantCreationNotifier;
    private static EmailSender tenantActivationNotifier;
    private static EmailVerifierConfig emailVerifierConfig = null;
    private static EmailVerifierConfig superTenantEmailVerifierConfig = null;
    private static List<TenantMgtListener> tenantMgtListeners = new ArrayList<TenantMgtListener>();
    private static TenantPersistor tenantPersistor = null;
    private static TenantBillingService billingService = null;

    protected void activate(ComponentContext context) {
        try {
            bundleContext = context.getBundleContext();
            // setting the success message config
            String confFilename = CarbonUtils.getCarbonConfigDirPath() + File.separator
                                  +StratosConstants.EMAIL_CONFIG+
                                  File.separator+"email-registration-complete.xml";
            EmailSenderConfiguration successMsgConfig =
                    EmailSenderConfiguration.loadEmailSenderConfiguration(confFilename);
            successMsgSender = new EmailSender(successMsgConfig);

            loadSuperTenantNotificationEmailConfig();

            loadEmailVerificationConfig();

            // Setting the password reset email config.
            initPasswordResetEmailSender();

            // Loading the stratos configurations from Stratos.xml
            if (CommonUtil.getStratosConfig() == null) {
                StratosConfiguration stratosConfig = CommonUtil.loadStratosConfiguration();
                CommonUtil.setStratosConfig(stratosConfig);
            }

            // Loading the EULA
            if (CommonUtil.getEula() == null) {
                String eula = CommonUtil.loadTermsOfUsage();
                CommonUtil.setEula(eula);
            }
            
			populateRampartConfig(configurationContextService.
					              getServerConfigContext().getAxisConfiguration());

            log.debug("******* Governance Tenant Config bundle is activated ******* ");
        } catch (Exception e) {
            log.error("******* Governance Tenant Config bundle failed activating ****", e);
        }
    }

    /**
     * loads the notification configurations for the mail to super tenant for account creations
     * and activations.
     */
    private void loadSuperTenantNotificationEmailConfig() {
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


    private void initPasswordResetEmailSender() {
        String passwordResetConfigFileName = CarbonUtils.getCarbonConfigDirPath()+ File.separator
                                             +StratosConstants.EMAIL_CONFIG +
                                             File.separator+"email-password-reset.xml";
        EmailSenderConfiguration passwordResetMsgConfig =
                EmailSenderConfiguration.loadEmailSenderConfiguration(passwordResetConfigFileName);
        PasswordUtil.setPasswordResetMsgSender(new EmailSender(passwordResetMsgConfig));
    }

    protected void setTenantMgtListenerService(TenantMgtListener tenantMgtListener) {
        addTenantMgtListener(tenantMgtListener);
    }

    protected void unsetTenantMgtListenerService(TenantMgtListener tenantMgtListener) {
        removeTenantMgtListener(tenantMgtListener);
    }

    protected void deactivate(ComponentContext context) {
        log.debug("******* Governance Tenant Config bundle is deactivated ******* ");
    }

    protected void setRegistryService(RegistryService registryService) {
        TenantMgtServiceComponent.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {
        setRegistryService(null);
    }

    protected void setRealmService(RealmService realmService) {
        TenantMgtServiceComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        setRealmService(null);
    }

    protected void setEmailVerificationService(EmailVerifcationSubscriber emailService) {
        TenantMgtServiceComponent.emailVerificationService = emailService;
    }

    protected void unsetEmailVerificationService(EmailVerifcationSubscriber emailService) {
        setEmailVerificationService(null);
    }

    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        log.debug("Receiving ConfigurationContext Service");
        TenantMgtServiceComponent.configurationContextService = configurationContextService;

    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        log.debug("Unsetting ConfigurationContext Service");
        setConfigurationContextService(null);
    }

    public static BundleContext getBundleContext() {
        return bundleContext;
    }

    public static void addTenantMgtListener(TenantMgtListener tenantMgtListener) {
        tenantMgtListeners.add(tenantMgtListener);
        sortTenantMgtListeners();
    }

    public static void removeTenantMgtListener(TenantMgtListener tenantMgtListener) {
        tenantMgtListeners.remove(tenantMgtListener);
        sortTenantMgtListeners();
    }

    public static void sortTenantMgtListeners() {
        Collections.sort(tenantMgtListeners, new Comparator<TenantMgtListener>() {
            public int compare(TenantMgtListener o1, TenantMgtListener o2) {
                return o1.getListenerOrder() - o2.getListenerOrder();
            }
        });
    }

    public static List<TenantMgtListener> getTenantMgtListeners() {
        return tenantMgtListeners;
    }

    public static EmailSender getSuccessMsgSender() {
        return successMsgSender;
    }

    public static EmailSender getTenantCreationNotifier() {
        return tenantCreationNotifier;
    }

    public static EmailSender getTenantActivationNotifier() {
        return tenantActivationNotifier;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static ConfigurationContext getConfigurationContext() {
        if (configurationContextService.getServerConfigContext() == null) {
            return null;
        }
        return configurationContextService.getServerConfigContext();
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }


    public static RealmService getRealmService() {
        return realmService;
    }


    public static EmailVerifcationSubscriber getEmailVerificationService() {
        return emailVerificationService;
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

    public static UserRegistry getConfigSystemRegistry(int tenantId) throws RegistryException {
        return registryService.getConfigSystemRegistry(tenantId);
    }

    /**
     * loads the Email configuration files to be sent on the tenant registrations.
     */
    public static void loadEmailVerificationConfig() {
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

    public static EmailVerifierConfig getSuperTenantEmailVerifierConfig() {
        return superTenantEmailVerifierConfig;
    }

    public static EmailVerifierConfig getEmailVerifierConfig() {
        return emailVerifierConfig;
    }

    public static TenantPersistor getTenantPersistor() {
        return tenantPersistor;
    }

    protected void setTenantPersistor(TenantPersistor defaultTenantPersistor) {
        tenantPersistor = defaultTenantPersistor;
        System.out.println("TenantMgtServiceComponent setTenantPersistor");
    }

    public void unsetTenantPersistor(TenantPersistor defaultTenantPersistor) {
        tenantPersistor = null;
    }

    
   /** Updates RelyingPartyService with Crypto information
    *
    * @param config AxisConfiguration
    * @throws Exception
    */
   private void populateRampartConfig(AxisConfiguration config) throws Exception {
       AxisService service;
       // Get the RelyingParty Service to update security policy with keystore information
       service = config.getService(GAPP_TENANT_REG_SERVICE_NAME);
       if (service == null) {
           String msg = GAPP_TENANT_REG_SERVICE_NAME + " is not available in the Configuration Context";
           log.error(msg);
           throw new Exception(msg);
       }
       // Create a Rampart Config with default crypto information
       Policy rampartConfig = TenantMgtRampartUtil.getDefaultRampartConfig();
       // Add the RampartConfig to service policy
       service.getPolicySubject().attachPolicy(rampartConfig);

   }
   
   protected void setTenantBillingService(TenantBillingService tenantBillingService) {
       billingService = tenantBillingService;
   }
   
   protected void unsetTenantBillingService(TenantBillingService tenantBilling) {
       setTenantBillingService(null);
   }
   
   public static TenantBillingService getBillingService() {
       return billingService;
   }
   
   
}
