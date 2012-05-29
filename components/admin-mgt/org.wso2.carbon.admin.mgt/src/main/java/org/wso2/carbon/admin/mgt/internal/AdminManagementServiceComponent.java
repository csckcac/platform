/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.admin.mgt.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.admin.mgt.constants.AdminMgtConstants;
import org.wso2.carbon.email.verification.util.EmailVerifierConfig;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.email.verification.util.EmailVerifcationSubscriber;

import java.io.File;

/**
 * @scr.component name="org.wso2.carbon.admin.mgt.internal.AdminManagementServiceComponent"
 * immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="configuration.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="emailverification.service" interface=
 *                "org.wso2.carbon.email.verification.util.EmailVerifcationSubscriber"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setEmailVerificationService"
 *                unbind="unsetEmailVerificationService"
 */
public class AdminManagementServiceComponent {
    private static Log log = LogFactory.getLog(AdminManagementServiceComponent.class);
    private static ConfigurationContextService configurationContextService;
    private static RealmService realmService;
    private static RegistryService registryService;
    private static EmailVerifcationSubscriber emailVerificationService = null;
    private static EmailVerifierConfig emailVerifierConfig = null;

    protected void activate(ComponentContext context) {
        loadEmailVerifierConfig();
        log.debug("******* Admin Management bundle is activated ******* ");
        try {
            log.debug("******* Admin Management bundle is activated ******* ");
        } catch (Exception e) {
            log.debug("******* Failed to activate Admin Management bundle ******* ");
        }
    }

    /**
     * method to load the Email Verifier Configurations
     */
    public static void loadEmailVerifierConfig() {
        String confXml = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                AdminMgtConstants.EMAIL_CONF_DIRECTORY + File.separator +
                AdminMgtConstants.EMAIL_ADMIN_CONF_FILE;
        try {
            emailVerifierConfig =
                    org.wso2.carbon.email.verification.util.Util.
                            loadeMailVerificationConfig(confXml);
        } catch(Exception e) {
            String msg = "Email Configuration file for the password reset feature not found. " +
                    "Pls check the repository/conf/email folder for email-admin-config.xml.";
            log.error(msg);
        }
    }

    protected void deactivate(ComponentContext context) {
        log.debug("******* Admin Management bundle is deactivated ******* ");
    }

    protected void setRegistryService(RegistryService registryService) {
        AdminManagementServiceComponent.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {
        setRegistryService(null);
    }

    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        log.debug("Receiving ConfigurationContext Service");
        AdminManagementServiceComponent.configurationContextService = configurationContextService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        log.debug("Unsetting ConfigurationContext Service");
        setConfigurationContextService(null);
    }

    public static ConfigurationContext getConfigurationContext() {
        if (configurationContextService.getServerConfigContext() == null) {
            return null;
        }
        return configurationContextService.getServerConfigContext();
    }

    protected void setRealmService(RealmService realmService) {
        AdminManagementServiceComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        setRealmService(null);
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

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static UserRegistry getConfigSystemRegistry(int tenantId) throws RegistryException {
        return registryService.getConfigSystemRegistry(tenantId);
    }

    protected void setEmailVerificationService(EmailVerifcationSubscriber emailService) {
        AdminManagementServiceComponent.emailVerificationService = emailService;
    }

    protected void unsetEmailVerificationService(EmailVerifcationSubscriber emailService) {
        setEmailVerificationService(null);
    }

    public static EmailVerifcationSubscriber getEmailVerificationService() {
        return emailVerificationService;
    }

    public static EmailVerifierConfig getEmailVerifierConfig() {
        return emailVerifierConfig;
    }

}
