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

package org.wso2.carbon.identity.core.dao;

import org.apache.commons.codec.binary.Base64;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.core.IdentityRegistryResources;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SAMLSSOServiceProviderDAO extends AbstractDAO<SAMLSSOServiceProviderDO> {

    private static Log log = LogFactory.getLog(SAMLSSOServiceProviderDAO.class);

    public SAMLSSOServiceProviderDAO(Registry registry) {
        this.registry = registry;
    }

    protected SAMLSSOServiceProviderDO resourceToObject(Resource resource) {
        SAMLSSOServiceProviderDO serviceProviderDO = new SAMLSSOServiceProviderDO();
        serviceProviderDO.setIssuer(resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_ISSUER));
        serviceProviderDO.setAssertionConsumerUrl(resource.getProperty(IdentityRegistryResources
                .PROP_SAML_SSO_ASSERTION_CONS_URL));
        serviceProviderDO.setCertAlias(resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_ISSUER_CERT_ALIAS));

        if (resource.getProperty(
                IdentityRegistryResources.PROP_SAML_SSO_USE_FULLY_QUALIFIED_USERNAME_AS_SUBJECT) != null) {
            serviceProviderDO.setUseFullyQualifiedUsername(new Boolean(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_USE_FULLY_QUALIFIED_USERNAME_AS_SUBJECT).trim()));
        }

        if (resource.getProperty(
                IdentityRegistryResources.PROP_SAML_SSO_DO_SINGLE_LOGOUT) != null) {
            serviceProviderDO.setDoSingleLogout(new Boolean(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_DO_SINGLE_LOGOUT).trim()));
        }

        serviceProviderDO.setLogoutURL(resource.getProperty(IdentityRegistryResources.PROP_SAML_SSO_LOGOUT_URL));

        if (resource.getProperty(
                IdentityRegistryResources.PROP_SAML_SSO_DO_SIGN_ASSERTIONS) != null) {
            serviceProviderDO.setDoSignAssertions(new Boolean(resource.getProperty(
                    IdentityRegistryResources.PROP_SAML_SSO_DO_SIGN_ASSERTIONS).trim()));
        }
        
        return serviceProviderDO;
    }

    public boolean addServiceProvider(SAMLSSOServiceProviderDO serviceProviderDO) throws IdentityException {
        String path = null;
        Resource resource;

        if (serviceProviderDO.getIssuer() != null) {
            path = IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS + encodePath(serviceProviderDO.getIssuer());
        }

        boolean isTransactionStarted = Transaction.isStarted();
        try {
            if (registry.resourceExists(path)) {
                if (log.isDebugEnabled()) {
                    log.debug("Service Provider already exists with the same issuer name" + serviceProviderDO.getIssuer());
                }
                return false;
            }

            resource = registry.newResource();
            resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_ISSUER, serviceProviderDO.getIssuer());
            resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_ASSERTION_CONS_URL,
                    serviceProviderDO.getAssertionConsumerUrl());
            resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_ISSUER_CERT_ALIAS,
                    serviceProviderDO.getCertAlias());
            resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_LOGOUT_URL, serviceProviderDO.getLogoutURL());

            String useFullyQualifiedUsername = serviceProviderDO.isUseFullyQualifiedUsername() ? "true" : "false";
            resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_USE_FULLY_QUALIFIED_USERNAME_AS_SUBJECT, useFullyQualifiedUsername);

            String doSingleLogout = serviceProviderDO.isDoSingleLogout() ? "true" : "false";
            resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_DO_SINGLE_LOGOUT, doSingleLogout);
            
            String doSignAssertions = serviceProviderDO.isDoSignAssertions() ? "true" : "false";
            resource.addProperty(IdentityRegistryResources.PROP_SAML_SSO_DO_SIGN_ASSERTIONS, doSignAssertions);

            try {
                if (!isTransactionStarted) {
                    registry.beginTransaction();
                }

                registry.put(path, resource);

                if (!isTransactionStarted) {
                    registry.commitTransaction();
                }

            } catch (RegistryException e) {
                if (!isTransactionStarted) {
                    registry.rollbackTransaction();
                }
                throw e;
            }

        } catch (RegistryException e) {
            log.error("Error While adding Service Provider", e);
            throw new IdentityException("Error while adding Service Provider", e);
        }

        if(log.isDebugEnabled()){
            log.debug("Service Provider " + serviceProviderDO.getIssuer() + " is added successfully.");
        }
        return true;
    }

    public SAMLSSOServiceProviderDO[] getServiceProviders() throws IdentityException {
        SAMLSSOServiceProviderDO[] serviceProvidersList = new SAMLSSOServiceProviderDO[0];
        try {
            if (registry.resourceExists(IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS)) {
                String[] providers = (String[])registry.get(IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS)
                        .getContent();
                if(providers != null){
                    serviceProvidersList = new SAMLSSOServiceProviderDO[providers.length];
                    for(int i = 0; i< providers.length; i++){
                        serviceProvidersList[i] = resourceToObject(registry.get(providers[i]));       
                    }
                }
            }
        } catch (RegistryException e) {
            log.error("Error reading Service Providers from Registry", e);
            throw new IdentityException("Error reading Service Providers from Registry",e);
        }
        return serviceProvidersList;
    }

    /**
     * Remove the service provider with the given name
     * @param issuer
     * @throws IdentityException
     */
    public boolean removeServiceProvider(String issuer) throws IdentityException {
        String path = IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS + encodePath(issuer);
        boolean isTransactionStarted = Transaction.isStarted();
        try {
            if (registry.resourceExists(path)) {
                try {
                    if (!isTransactionStarted) {
                        registry.beginTransaction();
                    }

                    registry.delete(path);

                    if (!isTransactionStarted) {
                        registry.commitTransaction();
                    }

                    return true;

                } catch (RegistryException e) {
                    if (!isTransactionStarted) {
                        registry.rollbackTransaction();
                    }
                    throw e;
                }
            }
        } catch (RegistryException e) {
            log.error("Error removing the service provider from the registry", e);
            throw new IdentityException("Error removing the service provider from the registry", e);
        }

        return false;
    }

    /**
     * Get the service provider
     * @param issuer
     * @return
     * @throws IdentityException
     */
    public SAMLSSOServiceProviderDO getServiceProvider(String issuer) throws IdentityException {

        SAMLSSOServiceProviderDO serviceProviderDO = null;
        String path = IdentityRegistryResources.SAML_SSO_SERVICE_PROVIDERS + encodePath(issuer);

        try {
            if (registry.resourceExists(path)) {
                serviceProviderDO = resourceToObject(registry.get(path));
            }
        } catch (RegistryException e) {
            log.error("Error reading Service Providers from Registry", e);
            throw new IdentityException("Error reading Service Providers from Registry", e);
        }

        return serviceProviderDO;
    }

    private String encodePath(String path){
        String encodedStr = new String(Base64.encodeBase64(path.getBytes()));
        return encodedStr.replace("=","");
    }

}
