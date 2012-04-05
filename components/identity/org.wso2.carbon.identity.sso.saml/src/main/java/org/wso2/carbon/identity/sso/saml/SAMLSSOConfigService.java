/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.sso.saml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.util.KeyStoreUtil;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.sso.saml.admin.SAMLSSOConfigAdmin;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSOServiceProviderInfoDTO;
import org.wso2.carbon.identity.sso.saml.util.SAMLSSOUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.security.SecurityConfigException;
import org.wso2.carbon.security.keystore.KeyStoreAdmin;
import org.wso2.carbon.security.keystore.service.KeyStoreData;

public class SAMLSSOConfigService extends AbstractAdmin{

    private static Log log = LogFactory.getLog(SAMLSSOConfigService.class);

    public boolean addRPServiceProvider(SAMLSSOServiceProviderDTO spDto) throws IdentityException {
        SAMLSSOConfigAdmin configAdmin = new SAMLSSOConfigAdmin(getConfigSystemRegistry());
        return configAdmin.addRelyingPartyServiceProvider(spDto);
    }

    public SAMLSSOServiceProviderInfoDTO getServiceProviders() throws IdentityException {
        SAMLSSOConfigAdmin configAdmin = new SAMLSSOConfigAdmin(getConfigSystemRegistry());
         return configAdmin.getServiceProviders();
    }


    private KeyStoreData[] getKeyStores() throws IdentityException {
        KeyStoreAdmin admin = null;
        try {
            admin = new KeyStoreAdmin(getGovernanceRegistry());
            return admin.getKeyStores();
        } catch (SecurityConfigException e) {
            log.error("Error when loading the key stores from registry", e);
            throw new IdentityException("Error when loading the key stores from registry", e);
        }
    }

    public String[] getCertAliasOfPrimaryKeyStore() throws IdentityException {
        KeyStoreData[] keyStores = getKeyStores();
        KeyStoreData primaryKeyStore = null;
        for (int i = 0; i < keyStores.length; i++) {
            if (getTenantDomain() == null && KeyStoreUtil.isPrimaryStore(keyStores[i].getKeyStoreName())) {
                primaryKeyStore = keyStores[i];
                break;
            }
            else if (getTenantDomain() != null && SAMLSSOUtil.generateKSNameFromDomainName(getTenantDomain()).equals(
                    keyStores[i].getKeyStoreName())){
                primaryKeyStore = keyStores[i];
                break;
            }
        }
        if (primaryKeyStore != null) {
                return getStoreEntries(primaryKeyStore.getKeyStoreName());
        }
        throw new IdentityException("Primary Keystore cannot be found.");
    }

    public boolean removeServiceProvider(String issuer) throws IdentityException {
        SAMLSSOConfigAdmin ssoConfigAdmin = new SAMLSSOConfigAdmin(getConfigSystemRegistry());
        return ssoConfigAdmin.removeServiceProvider(issuer);
    }

    private String[] getStoreEntries(String keyStoreName) throws IdentityException {
        KeyStoreAdmin admin = null;
        try {
            admin = new KeyStoreAdmin(getGovernanceRegistry());
            return admin.getStoreEntries(keyStoreName);
        } catch (SecurityConfigException e) {
            log.error("Error reading entries from the key store : " + keyStoreName);
            throw new IdentityException("Error reading entries from the keystore" + e);
        }
    }
}
