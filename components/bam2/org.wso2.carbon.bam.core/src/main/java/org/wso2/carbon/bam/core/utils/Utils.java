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
package org.wso2.carbon.bam.core.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.core.internal.ServiceHolder;
import org.wso2.carbon.bam.core.persistence.PersistencyConstants;
import org.wso2.carbon.bam.core.persistence.exceptions.ConfigurationException;
import org.wso2.carbon.bam.core.persistence.exceptions.StoreException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.HashMap;
import java.util.Map;

public class Utils {

    private static final Log log = LogFactory.getLog(Utils.class);

    public static int getTenantIdFromUserName(String userName) throws StoreException {
        String domain = MultitenantUtils.getTenantDomain(userName);

        int tenantId;
        try {
            tenantId = ServiceHolder.getRealmService().getTenantManager().getTenantId(domain);
        } catch (UserStoreException e) {
            throw new StoreException("Unable to get tenant information from user name '" +
                                     userName + "'..", e);
        }

        return tenantId;
    }

    public static boolean credentialsValid(Map<String, String> credentials) {
        if (credentials == null) {
            return false;
        }

        if (credentials.size() < 2) {
            return false;
        }

        for (Map.Entry<String, String> entry : credentials.entrySet()) {

            String key = entry.getKey();
            String value = entry.getValue();

            if (key == null || value == null) {
                return false;
            }

            if (!key.equals(PersistencyConstants.USER_NAME) &&
                !key.equals(PersistencyConstants.PASSWORD)) {
                return false;
            }
        }

        return true;

    }

    public static Map<String, String> getConnectionParameters() throws ConfigurationException {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();

        return getConnectionParameters(tenantId);

    }

    public static Map<String, String> getConnectionParameters(int tenantId) throws ConfigurationException {

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
            log.error(message, e);
            throw new ConfigurationException(message);
        }

        Map<String, String> credentials = new HashMap<String, String>();
        credentials.put(PersistencyConstants.USER_NAME, username);
        credentials.put(PersistencyConstants.PASSWORD, password);

        return credentials;

    }

}
