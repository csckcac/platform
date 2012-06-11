/*
 * Copyright 2012 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.bam.eventreceiver.service;

import org.wso2.carbon.bam.eventreceiver.BAMEventReceiverComponentManager;
import org.wso2.carbon.bam.eventreceiver.exception.ConfigurationException;
import org.wso2.carbon.bam.eventreceiver.internal.util.EventReceiverConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.session.UserRegistry;

/**
 *  Cassandra stream definition store credential store service
 */
public class CassandraCredentialMgtService extends AbstractAdmin {

    public CassandraCredential getCassandraCredentials() throws ConfigurationException {

        int tenantId = CarbonContext.getCurrentContext().getTenantId();

        String username;
        String password;

        try {
            UserRegistry configSystemRegistry = BAMEventReceiverComponentManager.getInstance().
                    getRegistryService().getConfigSystemRegistry(tenantId);

            String cassandraStreamDefAuthPath = EventReceiverConstants.CASSANDRA_STREAM_DEF_AUTH_PATH;
            Resource cassandraAuthResource;
            if (configSystemRegistry.resourceExists(cassandraStreamDefAuthPath)) {
                cassandraAuthResource = configSystemRegistry.get(cassandraStreamDefAuthPath);
            } else {
                cassandraAuthResource = configSystemRegistry.newResource();
                configSystemRegistry.put(cassandraStreamDefAuthPath, cassandraAuthResource);
                cassandraAuthResource = configSystemRegistry.get(cassandraStreamDefAuthPath);
            }

            username = cassandraAuthResource.
                    getProperty(EventReceiverConstants.USERNAME_PROPERTY);

            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
            try {
                String cipherTextPassword = cassandraAuthResource.
                        getProperty(EventReceiverConstants.PASSWORD_PROPERTY);
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

        } catch (org.wso2.carbon.registry.core.exceptions.RegistryException e) {
            String message = "Failed to fetch connection parameters for tenant : " +
                    tenantId;
            throw new ConfigurationException(message);
        }

        return new CassandraCredential(username, password);
    }

    public void setCassandraCredentials(String userName, String userPassword) throws ConfigurationException {

        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        try {

            UserRegistry configSystemRegistry = BAMEventReceiverComponentManager.getInstance().getRegistryService().
                    getConfigSystemRegistry(tenantId);

            String cassandraStreamDefAuthPath = EventReceiverConstants.CASSANDRA_STREAM_DEF_AUTH_PATH;
            Resource cassandraAuthResource;
            if (configSystemRegistry.resourceExists(cassandraStreamDefAuthPath)) {
                cassandraAuthResource = configSystemRegistry.get(cassandraStreamDefAuthPath);
            } else {
                cassandraAuthResource = configSystemRegistry.newResource();
                configSystemRegistry.put(cassandraStreamDefAuthPath, cassandraAuthResource);
                cassandraAuthResource = configSystemRegistry.get(cassandraStreamDefAuthPath);
            }

            cassandraAuthResource.setProperty(EventReceiverConstants.USERNAME_PROPERTY, userName);

            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
            try {
                String cipherTextPassword = cryptoUtil.
                        encryptAndBase64Encode(userPassword.getBytes());
                cassandraAuthResource.setProperty(EventReceiverConstants.PASSWORD_PROPERTY,
                        cipherTextPassword);

            } catch (CryptoException e) {
                throw new ConfigurationException("Failed to store connection parameters ", e);
            }

            configSystemRegistry.put(cassandraStreamDefAuthPath, cassandraAuthResource);

        } catch (RegistryException e) {
            throw new ConfigurationException("Failed to store connection parameters for tenant : " +
                    tenantId);
        }
    }
}
