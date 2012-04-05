/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.apimgt.keymgt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;

import java.sql.Connection;

public class APIKeyMgtUtil {

    private static final Log log = LogFactory.getLog(APIKeyMgtUtil.class);

    public static String getTenantDomainFromTenantId(int tenantId) throws APIKeyMgtException {
        try {
            TenantManager tenantManager = APIKeyMgtDataHolder.getRealmService().getTenantManager();
            return tenantManager.getDomain(tenantId);
        } catch (UserStoreException e) {
            String errorMsg = "Error when getting the Tenant domain name for the given Tenant Id";
            log.error(errorMsg, e);
            throw new APIKeyMgtException(errorMsg, e);
        }
    }

    /**
     * Get a database connection instance from the Identity Persistence Manager
     * @return Database Connection
     * @throws APIKeyMgtException Error when getting an instance of the identity Persistence Manager
     */
    public static Connection getDBConnection() throws APIKeyMgtException {
        try {
            return IdentityDatabaseUtil.getDBConnection();
        } catch (IdentityException e) {
            String errMsg = "Error when getting a database connection from the Identity Persistence Manager";
            log.error(errMsg, e);
            throw new APIKeyMgtException(errMsg, e);
        }
    }

}
