/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.identity.sso.saml.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.identity.sso.saml.SSOServiceProviderConfigManager;
import org.wso2.carbon.identity.sso.saml.util.SAMLSSOUtil;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="identity.sso.saml.component" immediate="true"
 * @scr.reference name="registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService"
 *                unbind="unsetRegistryService"
 * @scr.reference name="config.context.service"
 *                interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 *                policy="dynamic" bind="setConfigurationContextService"
 *                unbind="unsetConfigurationContextService"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" bind="setRealmService"
 *                unbind="unsetRealmService"
 */
public class IdentitySAMLSSOServiceComponent{

    private static Log log = LogFactory.getLog(IdentitySAMLSSOServiceComponent.class);

    protected void activate(ComponentContext ctxt) {
        SAMLSSOUtil.setBundleContext(ctxt.getBundleContext());
        // Register a SSOServiceProviderConfigManager object as an OSGi Service
        ctxt.getBundleContext().registerService(SSOServiceProviderConfigManager.class.getName(),
                                                SSOServiceProviderConfigManager.getInstance(), null);
        if (log.isDebugEnabled()) {
            log.info("Identity SAML SSO bundle is activated");
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        SAMLSSOUtil.setBundleContext(null);
        if (log.isDebugEnabled()) {
            log.info("Identity SAML SSO bundle is deactivated");
        }
    }

    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService set in Identity SAML SSO bundle");
        }
        try {
            SAMLSSOUtil.setRegistryService(registryService);
        } catch (Throwable e) {
            log.error("Failed to get a reference to the Registry in SAML SSO bundle", e);
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService unset in SAML SSO bundle");
        }
        SAMLSSOUtil.setRegistryService(null);
    }

    protected void setRealmService(RealmService realmService){
        if(log.isDebugEnabled()){
            log.debug("Realm Service is set in the SAML SSO bundle");
        }
        SAMLSSOUtil.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService){
        if(log.isDebugEnabled()){
            log.debug("Realm Service is set in the SAML SSO bundle");
        }
        SAMLSSOUtil.setRegistryService(null);
    }

    protected void setConfigurationContextService(ConfigurationContextService configCtxService){
        if(log.isDebugEnabled()){
            log.debug("Configuration Context Service is set in the SAML SSO bundle");
        }
        SAMLSSOUtil.setConfigCtxService(configCtxService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configCtxService){
        if(log.isDebugEnabled()){
            log.debug("Configuration Context Service is unset in the SAML SSO bundle");
        }
        SAMLSSOUtil.setConfigCtxService(null);
    }

}