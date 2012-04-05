/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.core.service;

import org.wso2.carbon.bam.core.internal.ServiceHolder;
import org.wso2.carbon.bam.core.persistence.PersistencyConstants;
import org.wso2.carbon.bam.core.persistence.exceptions.ConfigurationException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

public class ConnectionMgtService extends AbstractAdmin {

    public boolean configureConnectionParameters(String userName, String password)
            throws ConfigurationException {
        
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        try {
            UserRegistry configSystemRegistry = ServiceHolder.getRegistryService().
                    getConfigSystemRegistry(tenantId);

            String connectionResourcePath = PersistencyConstants.CONNECTION_PATH;
            Resource connectionResource;
            if (configSystemRegistry.resourceExists(connectionResourcePath)) {
                connectionResource = configSystemRegistry.get(connectionResourcePath);
            } else {
                connectionResource = configSystemRegistry.newResource();
                configSystemRegistry.put(connectionResourcePath, connectionResource);
                connectionResource = configSystemRegistry.get(connectionResourcePath);
            }

            connectionResource.setProperty(PersistencyConstants.USERNAME_PROPERTY, userName);

            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
            try {
                String cipherTextPassword = cryptoUtil.
                        encryptAndBase64Encode(password.getBytes());
                connectionResource.setProperty(PersistencyConstants.PASSWORD_PROPERTY,
                                               cipherTextPassword);

            } catch (CryptoException e) {
                throw new ConfigurationException("Failed to store connection parameters ", e);
            }

            configSystemRegistry.put(connectionResourcePath, connectionResource);

        } catch (RegistryException e) {
            throw new ConfigurationException("Failed to store connection parameters for tenant : " +
                                        tenantId);
        }

        return true;
    }

    public ConnectionDTO getConnectionParameters() throws ConfigurationException {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();

        String username;
        String password;

        try {
            UserRegistry configSystemRegistry = ServiceHolder.getRegistryService().
                    getConfigSystemRegistry(tenantId);

            String connectionResourcePath = PersistencyConstants.CONNECTION_PATH;
            Resource connectionResource;
            if (configSystemRegistry.resourceExists(connectionResourcePath)) {
                connectionResource = configSystemRegistry.get(connectionResourcePath);
            } else {
                connectionResource = configSystemRegistry.newResource();
                configSystemRegistry.put(connectionResourcePath, connectionResource);
                connectionResource = configSystemRegistry.get(connectionResourcePath);
            }

            username = connectionResource.
                    getProperty(PersistencyConstants.USERNAME_PROPERTY);

            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
            try {
                String cipherTextPassword = connectionResource.
                        getProperty(PersistencyConstants.PASSWORD_PROPERTY);
                if (cipherTextPassword != null) {
                    byte[] passwordBytes = cryptoUtil.
                            base64DecodeAndDecrypt(cipherTextPassword);
                    password = new String(passwordBytes);
                } else {
                    password = null;
                }
            } catch (CryptoException e) {
                throw new ConfigurationException("Failed to fetch connection parameters ", e);
            }

        } catch (RegistryException e) {
            String message = "Failed to fetch connection parameters for tenant : " +
                             tenantId;
            throw new ConfigurationException(message);
        }

        return new ConnectionDTO(username, password);

    }
    
}
