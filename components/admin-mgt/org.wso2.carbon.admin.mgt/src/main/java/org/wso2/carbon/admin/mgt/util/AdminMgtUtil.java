/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.admin.mgt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.mgt.constants.AdminMgtConstants;
import org.wso2.carbon.admin.mgt.exception.AdminManagementException;
import org.wso2.carbon.admin.mgt.internal.AdminManagementServiceComponent;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * Utility methods for the admin management component - password reset feature.
 */
public class AdminMgtUtil {
    private static final Log log = LogFactory.getLog(AdminMgtUtil.class);

    /**
     * Is the given tenant domain valid
     *
     * @param domainName tenant domain
     * @throws AdminManagementException , if invalid tenant domain name is given
     */
    public static void checkIsDomainValid(String domainName) throws AdminManagementException {
        if (domainName == null || domainName.equals("")) {
            String msg = "Provided domain name is empty.";
            log.error(msg);
            throw new AdminManagementException(msg);
        }
        int indexOfDot = domainName.indexOf(".");
        if (indexOfDot == 0) {
            // can't start a domain starting with ".";
            String msg = "Invalid domain, starting with '.'";
            log.error(msg);
            throw new AdminManagementException(msg);
        }
        // check the tenant domain contains any illegal characters
        if (domainName.matches(AdminMgtConstants.ILLEGAL_CHARACTERS_FOR_TENANT_DOMAIN)) {
            String msg = "The tenant domain ' " + domainName +
                    " ' contains one or more illegal characters. the valid characters are " +
                    "letters, numbers, '.', '-' and '_'";
            log.error(msg);
            throw new AdminManagementException(msg);
        }
    }

    /**
     * Gets the tenant id from the tenant domain
     *
     * @param domain - tenant domain
     * @return - tenantId
     * @throws AdminManagementException, if getting tenant id failed.
     */
    public static int getTenantIdFromDomain(String domain) throws AdminManagementException {
        TenantManager tenantManager = AdminManagementServiceComponent.getTenantManager();
        int tenantId;
        if (domain.trim().equals("")) {
            tenantId = MultitenantConstants.SUPER_TENANT_ID;
            if (log.isDebugEnabled()) {
                String msg = "Password reset attempt on Super Tenant";
                log.debug(msg);
            }
        } else {
            try {
                tenantId = tenantManager.getTenantId(domain);
                if (tenantId < 1) {
                    String msg = "Only the existing tenants can update the password";
                    log.error(msg);
                    throw new AdminManagementException(msg);
                }
            } catch (UserStoreException e) {
                String msg = "Error in retrieving tenant id of tenant domain: " + domain + ".";
                log.error(msg);
                throw new AdminManagementException(msg, e);
            }
        }
        return tenantId;
    }

    /**
     * Gets the admin management path of the tenant
     *
     * @param tenantLessUserName, the user name without the tenant part.
     * @param domain,    the tenant domain.
     * @return admin management path
     * @throws AdminManagementException, if the user doesn't exist, or couldn't retrieve the path.
     */
    public static String getAdminManagementPath(String tenantLessUserName, String domain) throws
            AdminManagementException {
        int tenantId;
        String adminManagementPath;
        try {
            tenantId = getTenantIdFromDomain(domain);
        } catch (AdminManagementException e) {
            String msg = "Error in getting tenant, tenant domain: " + domain + ".";
            log.error(msg, e);
            throw new AdminManagementException(msg, e);
        }
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            adminManagementPath = AdminMgtConstants.ADMIN_MANAGEMENT_FLAG_PATH +
                    RegistryConstants.PATH_SEPARATOR + tenantLessUserName;
        } else {
            adminManagementPath = AdminMgtConstants.ADMIN_MANAGEMENT_FLAG_PATH +
                    RegistryConstants.PATH_SEPARATOR + domain + RegistryConstants.PATH_SEPARATOR +
                    tenantLessUserName;
        }
        return adminManagementPath;
    }

    /**
     * Cleanup the used resources
     *
     * @param adminName, admin name
     * @param domain,    The tenant domain
     * @throws AdminManagementException, if the cleanup failed.
     */
    public static void cleanupResources(
            String adminName, String domain) throws AdminManagementException {
        String adminManagementPath = getAdminManagementPath(adminName, domain);
        UserRegistry superTenantSystemRegistry;
        Resource resource;
        try {
            superTenantSystemRegistry = AdminManagementServiceComponent.
                    getGovernanceSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
            if (superTenantSystemRegistry.resourceExists(adminManagementPath)) {
                resource = superTenantSystemRegistry.get(adminManagementPath);
                Resource tempResource = superTenantSystemRegistry.get(resource.getPath());
                if (tempResource != null) {
                    superTenantSystemRegistry.delete(resource.getPath());
                }
            }
        } catch (RegistryException e) {
            String msg = "Registry resource doesn't exist at the path, " + adminManagementPath;
            log.error(msg, e);
            throw new AdminManagementException(msg, e);
        }
    }

    /**
     * Gets the userName from the tenantLess userName and Domain
     *
     * @param adminName, userName without domain
     * @param domain,    domainName
     * @return complete userName
     */
    public static String getUserNameWithDomain(String adminName, String domain) {
        String userName = adminName;
        if (!domain.trim().equals("")) {
            // get the userName with tenant domain.
            userName = adminName + "@" + domain;
        }
        return userName;
    }
}
