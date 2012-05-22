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
package org.wso2.carbon.account.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.account.mgt.util.Util;
import org.wso2.carbon.stratos.common.TenantBillingService;
import org.wso2.carbon.stratos.common.events.StratosEventListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.email.verification.util.EmailVerifcationSubscriber;

/**
 * @scr.component name="org.wso2.carbon.account.mgt" immediate="true"
 * @scr.reference name="registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService"
 *                unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" bind="setRealmService"
 *                unbind="unsetRealmService"
 * @scr.reference name="config.context.service"
 *                interface="org.wso2.carbon.utils.ConfigurationContextService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setConfigurationContextService"
 *                unbind="unsetConfigurationContextService"
 * @scr.reference name="emailverification.service" interface=
 *                "org.wso2.carbon.email.verification.util.EmailVerifcationSubscriber"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setEmailVerificationService"
 *                unbind="unsetEmailVerificationService"
 * @scr.reference name="stratos.event.listener"
 *                interface="org.wso2.carbon.stratos.common.events.StratosEventListener"
 *                cardinality="0..1" policy="dynamic"
 *                bind="setStratosEventListener"
 *                unbind="unsetStratosEventListener"
 * @scr.reference name="default.tenant.billing.service"
 *                interface="org.wso2.carbon.stratos.common.TenantBillingService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setTenantBillingService"
 *                unbind="unsetTenantBillingService"
 */
public class AccountMgtServiceComponent {
    private static Log log = LogFactory.getLog(AccountMgtServiceComponent.class);
    private static ConfigurationContextService configContextService = null;
    private static StratosEventListener eventListener = null;
    private static TenantBillingService billingService = null;
    
    protected void activate(ComponentContext context) {
        try {
            Util.loadEmailVerificationConfig();
            Util.registerTenantMgtListenerServiceTrackers(context.getBundleContext());
            log.debug("******* Stratos account management bundle is activated ******* ");
        } catch (Exception e) {
            log.error("******* Stratos account management bundle failed activating ****", e);
        }
    }

    protected void deactivate(ComponentContext context) {
        Util.unregisterTenantMgtListenerServiceTrackers();
        log.debug("******* Stratos account managment bundle is deactivated ******* ");
    }

    protected void setRegistryService(RegistryService registryService) {
        Util.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        Util.setRegistryService(null);
    }

    protected void setRealmService(RealmService realmService) {
        Util.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        Util.setRealmService(null);
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the ConfigurationContext");
        }
        configContextService = contextService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the ConfigurationContext");
        }
    }


    protected void setEmailVerificationService(EmailVerifcationSubscriber emailService) {
        Util.setEmailVerificationService(emailService);
    }

    protected void unsetEmailVerificationService(EmailVerifcationSubscriber emailService) {
        Util.setEmailVerificationService(null);
    }

    public static StratosEventListener getStratosEventListener() {
        return eventListener;
    }

    protected void setStratosEventListener(StratosEventListener eventListener) {
        AccountMgtServiceComponent.eventListener = eventListener;
    }

    protected void unsetStratosEventListener(StratosEventListener eventListener) {
        AccountMgtServiceComponent.eventListener = null;
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
