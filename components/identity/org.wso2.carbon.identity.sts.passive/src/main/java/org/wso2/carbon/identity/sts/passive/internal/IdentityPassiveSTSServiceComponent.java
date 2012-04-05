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

package org.wso2.carbon.identity.sts.passive.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="identity.passive.sts.component" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="userrealm_delegating" interface="org.wso2.carbon.user.core.UserRealm"
 * cardinality="1..1" policy="dynamic" target="(RealmGenre=Delegating)"
 * bind="setUserRealmDelegating" unbind="unsetUserRealmDelegating"
 */
public class IdentityPassiveSTSServiceComponent {
    private static Log log = LogFactory.getLog(IdentityPassiveSTSServiceComponent.class);
    private static UserRealm userRealm = null;
    private static RegistryService registryService;

    /**
     *
     */
    public IdentityPassiveSTSServiceComponent() {
    }

    /**
     * @param ctxt
     */
    protected void activate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.info("Identity Provider bundle is activated");
        }
    }

    /**
     * @param ctxt
     */
    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.info("Identity Provider bundle is deactivated");
        }
    }

    /**
     * @param userRealmDelegating
     */
    protected void setUserRealmDelegating(UserRealm userRealmDelegating) {
        if (log.isDebugEnabled()) {
            log.info("DelegatingUserRealm set in Identity Provider bundle");
        }
        userRealm = userRealmDelegating;
    }

    /**
     * @param userRealmDelegating
     */
    protected void unsetUserRealmDelegating(UserRealm userRealmDelegating) {
        if (log.isDebugEnabled()) {
            log.info("DelegatingUserRealm set in Identity Provider bundle");
        }
    }


    /**
     * @param userRealmDefault
     */
    protected void unsetUserRealmDefault(RealmService userRealmDefault) {
        if (log.isDebugEnabled()) {
            log.info("DefaultUserRealm unset in Identity Provider bundle");
        }
    }

    /**
     * @return
     */
    public static UserRealm getRealm() {
        return userRealm;
    }

    /**
     * @param registryService
     */
    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService set in Carbon STS bundle");
        }
        try {
            this.registryService = registryService;
        } catch (Throwable e) {
            log.error("Failed to get a reference to the Registry", e);
        }
    }

    /**
     * @param registryService
     */
    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService unset in Carbon STS bundle");
        }
        registryService = null;
    }

    public static Registry getGovernanceSystemRegistry() throws RegistryException {
        return registryService.getGovernanceSystemRegistry();
    }

    public static Registry getConfigSystemRegistry() throws RegistryException {
        return registryService.getConfigSystemRegistry();
    }

}