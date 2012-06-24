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

package org.wso2.carbon.identity.sso.saml.admin;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.IdentityRegistryResources;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.core.persistence.IdentityPersistenceManager;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSOServiceProviderInfoDTO;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.WSO2Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Map;

/**
 * This class is used for managing SAML SSO providers. Adding, retrieving and removing service
 * providers are supported here.
 * In addition to that logic for generating key pairs for tenants except for tenant 0, is included
 * here.
 */
public class SAMLSSOConfigAdmin {

    private static Log log = LogFactory.getLog(SAMLSSOConfigAdmin.class);
    private UserRegistry registry;

    public SAMLSSOConfigAdmin(Registry userRegistry) {
        registry = (UserRegistry) userRegistry;
    }

    /**
     * Add a new service provider
     *
     * @param serviceProviderDTO    service Provider DTO
     * @return true if successful, false otherwise
     * @throws IdentityException    if fails to load the identity persistence manager    
     */
    public boolean addRelyingPartyServiceProvider(SAMLSSOServiceProviderDTO serviceProviderDTO) throws IdentityException {

        SAMLSSOServiceProviderDO serviceProviderDO = new SAMLSSOServiceProviderDO();
        serviceProviderDO.setIssuer(serviceProviderDTO.getIssuer());
        serviceProviderDO.setAssertionConsumerUrl(serviceProviderDTO.getAssertionConsumerUrl());
        serviceProviderDO.setCertAlias(serviceProviderDTO.getCertAlias());
        serviceProviderDO.setUseFullyQualifiedUsername(serviceProviderDTO.isUseFullyQualifiedUsername());
        serviceProviderDO.setDoSingleLogout(serviceProviderDTO.isDoSingleLogout());
        serviceProviderDO.setLogoutURL(serviceProviderDTO.getLogoutURL());
        serviceProviderDO.setDoSignAssertions(serviceProviderDTO.isDoSignAssertions());
        
        IdentityPersistenceManager persistenceManager = IdentityPersistenceManager
                .getPersistanceManager();
        try {
            return persistenceManager.addServiceProvider(registry, serviceProviderDO);
        } catch (IdentityException e) {
            log.error("Error obtaining a registry for adding a new service provider", e);
            throw new IdentityException("Error obtaining a registry for adding a new service provider", e);
        }
    }

    /**
     * Retrieve all the relying party service providers
     * 
     * @return set of RP Service Providers + file path of pub. key of generated key pair
     */
    public SAMLSSOServiceProviderInfoDTO getServiceProviders() throws IdentityException {
        SAMLSSOServiceProviderDTO[] serviceProviders = null;
        try {
            IdentityPersistenceManager persistenceManager = IdentityPersistenceManager
                    .getPersistanceManager();
            SAMLSSOServiceProviderDO[] providersSet = persistenceManager.
                    getServiceProviders(registry);
            serviceProviders = new SAMLSSOServiceProviderDTO[providersSet.length];

            for (int i = 0; i < providersSet.length; i++) {

                SAMLSSOServiceProviderDO providerDO = providersSet[i];

                SAMLSSOServiceProviderDTO providerDTO = new SAMLSSOServiceProviderDTO();
                providerDTO.setIssuer(providerDO.getIssuer());
                providerDTO.setAssertionConsumerUrl(providerDO.getAssertionConsumerUrl());
                providerDTO.setCertAlias(providerDO.getCertAlias());

                serviceProviders[i] = providerDTO;
            }
        } catch (IdentityException e) {
            log.error("Error obtaining a registry intance for reading service provider list", e);
            throw new IdentityException("Error obtaining a registry intance for reading service provider list", e);
        }

        SAMLSSOServiceProviderInfoDTO serviceProviderInfoDTO = new SAMLSSOServiceProviderInfoDTO();
        serviceProviderInfoDTO.setServiceProviders(serviceProviders);

        //if it is tenant zero
        if(registry.getTenantId() == 0){
            serviceProviderInfoDTO.setTenantZero(true);
        }
        return serviceProviderInfoDTO;
    }

    /**
     * Remove an existing service provider.
     * 
     * @param issuer issuer name
     * @return true is successful
     * @throws IdentityException
     */
    public boolean removeServiceProvider(String issuer) throws IdentityException {
        try {
            IdentityPersistenceManager persistenceManager = IdentityPersistenceManager.getPersistanceManager();
            return persistenceManager.removeServiceProvider(registry, issuer);
        } catch (IdentityException e) {
            log.error("Error removing a Service Provider");
            throw new IdentityException("Error removing a Service Provider", e);
        }
    }

    /**
     * Dumping the generated pub. cert to a file
     *
     * @param configurationContext
     * @param cert content of the certificate
     * @param uuid UUID used for the file name
     * @return file system location of the pub. cert
     */
    private String dumpPubCert(ConfigurationContext configurationContext, byte[] cert, String uuid) {
        Map fileResourcesMap = null;
        String workdir = null;
        File pubCert = null;
        OutputStream outStream = null;
        String filePath = null;

        workdir = (String) configurationContext.getProperty(ServerConstants.WORK_DIR);
        pubCert = new File(workdir + File.separator + "pub_certs");

        if (uuid == null) {
            uuid = String.valueOf(System.currentTimeMillis() + Math.random()) + ".cert";
        }

        if (!pubCert.exists()) {
            pubCert.mkdirs();
        }

        filePath = workdir + File.separator + "pub_certs" + File.separator + uuid;

        try {
            outStream = new FileOutputStream(filePath);
            outStream.write(cert);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            // TODO:
            e.printStackTrace();
            return null;
        }

        fileResourcesMap = (Map) configurationContext.getProperty(WSO2Constants.FILE_RESOURCE_MAP);
        if (fileResourcesMap == null) {
            fileResourcesMap = new Hashtable();
            configurationContext.setProperty(WSO2Constants.FILE_RESOURCE_MAP, fileResourcesMap);
        }
        fileResourcesMap.put(uuid, filePath);
        return WSO2Constants.ContextPaths.DOWNLOAD_PATH + "?id=" + uuid;
    }

    /**
     * Method used to get the file path location, if the cert is already generated.
     *
     * @return File path, if the certificate is already is generated, return null otherwise
     * @throws Exception
     */
    private String getPubKeyFilePath() throws Exception {
        Resource r;
        if (registry.resourceExists(IdentityRegistryResources.SAML_SSO_GEN_KEY)) {
            r = registry.get(IdentityRegistryResources.SAML_SSO_GEN_KEY);
            String filePath = r.getProperty(IdentityRegistryResources.PROP_SAML_SSO_PUB_KEY_FILE_PATH);
            verifyCertExistence(filePath, (byte[]) r.getContent(),
                    MessageContext.getCurrentMessageContext().getConfigurationContext());
            return filePath;
        } else {
            return null;
        }
    }

    /**
     * Check whether the certificate is availalble in the file system, if not dump it again.
     *
     * @param uuid UUID used in the file path
     * @param cert binary content of the cert.
     * @param configurationContext configuration context of the current message
     */
    private void verifyCertExistence(String uuid, byte[] cert, ConfigurationContext configurationContext) {
        uuid = uuid.substring("/filedownload?id=".length(), uuid.length());
        String workDir = (String) configurationContext.getProperty(ServerConstants.WORK_DIR);
        File pubCert = new File(workDir + File.separator + "pub_certs" + File.separator + uuid);

        //if cert is still available then exit
        if (pubCert.exists()) {
            Map fileResourcesMap = (Map) configurationContext.getProperty(WSO2Constants.FILE_RESOURCE_MAP);
            if (fileResourcesMap == null) {
                fileResourcesMap = new Hashtable();
                configurationContext.setProperty(WSO2Constants.FILE_RESOURCE_MAP, fileResourcesMap);
            }
            if (fileResourcesMap.get(uuid) == null) {
                fileResourcesMap.put(uuid, pubCert);
            }
            return;
        }

        //otherwise dump the cert
        dumpPubCert(configurationContext, cert, uuid);
    }

}
