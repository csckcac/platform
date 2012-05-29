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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.mgt.beans.ConfirmationBean;
import org.wso2.carbon.admin.mgt.constants.AdminMgtConstants;
import org.wso2.carbon.admin.mgt.exception.AdminManagementException;
import org.wso2.carbon.admin.mgt.internal.AdminManagementServiceComponent;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * Utility methods for the admin management component - password reset feature.
 */
public class AdminMgtUtil {
    private static final Log log = LogFactory.getLog(AdminMgtUtil.class);

    private static AdminManagementConfig adminMgtConfig;

    /**
     * method to load the adminManagementConfig
     */
    public static void loadAdminManagementConfig() {
        String confXml = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                AdminMgtConstants.EMAIL_CONF_DIRECTORY + File.separator +
                AdminMgtConstants.EMAIL_ADMIN_CONF_FILE;
        try {
            adminMgtConfig = loadAdminManagementConfig(confXml);
        } catch (Exception e) {
            String msg = "Error in loading the admin management configuration file.";
            log.error(msg, e);
        }
    }

    /**
     * Loading the AdminManagementConfig details from the given config file,
     *
     * @param configFilename - configuration file
     * @return - admin management config
     */
    public static AdminManagementConfig loadAdminManagementConfig(String configFilename) {
        AdminManagementConfig config = new AdminManagementConfig();
        File configfile = new File(configFilename);
        if (!configfile.exists()) {
            log.error("Email Configuration File is not present at: " + configFilename);
            return null;
        }
        try {
            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(
                    new FileInputStream(configfile));
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement documentElement = builder.getDocumentElement();
            Iterator it = documentElement.getChildElements();
            while (it.hasNext()) {
                OMElement element = (OMElement) it.next();
                if ("subject".equals(element.getLocalName())) {
                    config.setSubject(element.getText());
                } else if ("body".equals(element.getLocalName())) {
                    config.setEmailBody(element.getText());
                } else if ("footer".equals(element.getLocalName())) {
                    config.setEmailFooter(element.getText());
                } else if ("targetEpr".equals(element.getLocalName())) {
                    config.setTargetEpr(element.getText());
                } else if ("redirectPath".equals(element.getLocalName())) {
                    config.setRedirectPath(element.getText());
                }
            }
            return config;
        } catch (Exception e) {
            String msg = "Error in loading configuration for configuring the admin user: " +
                    configFilename + ".";
            log.error(msg, e);
            return null;
        }
    }

    /**
     * Confirms that the password reset request has been sent by the user.
     *
     * @param secretKey the secret key that was sent in the email
     * @return ConfirmationBean, the bean with the data and redirect path.
     * @throws AdminManagementException if the attempt failed -
     *                                  mostly due to the password reset link already been used or expired.
     * @throws org.wso2.carbon.registry.api.RegistryException
     *                                  Registry exception.
     */
    public static ConfirmationBean confirmUser(String secretKey) throws RegistryException,
            AdminManagementException {
        ConfirmationBean confirmationBean = new ConfirmationBean();
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement data = fac.createOMElement("configuration", null);

        // The password reset request hasn't been verified by the tenant yet.
        // Hence using the super tenant registry instance
        Registry registry = AdminManagementServiceComponent.
                getConfigSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
        boolean success = false;
        try {
            registry.beginTransaction();

            String secretKeyPath = AdminMgtConstants.ADMIN_MANAGEMENT_COLLECTION +
                    RegistryConstants.PATH_SEPARATOR + secretKey;
            if (!registry.resourceExists(secretKeyPath)) {
                String msg = "Password reset attempt failed, since the link was already clicked.";
                log.error(msg);
                throw new AdminManagementException(msg);
            }
            Resource resource = registry.get(secretKeyPath);

            // just get the properties of that
            Properties props = resource.getProperties();
            for (Object o : props.keySet()) {
                String key = (String) o;
                OMElement internal = fac.createOMElement(key, null);
                internal.setText(resource.getProperty(key));
                data.addChild(internal);
                if (key.equals("redirectPath")) {
                    confirmationBean.setRedirectPath(resource.getProperty(key));
                }
            }

            // removing the temporarily stored data from the registry
            registry.delete(resource.getPath());
            confirmationBean.setData(data.toString());
            success = true;

        } finally {
            if (success) {
                registry.commitTransaction();
            } else {
                registry.rollbackTransaction();
            }
        }
        return confirmationBean;
    }

    /**
     * verifying the admin management request from the user
     *
     * @param data - data to include in the mail
     * @throws AdminManagementException if loading config or sending verification fail.
     */
    public static void requestUserVerification(Map<String, String> data) throws
            AdminManagementException {
        String emailAddress = data.get("email");

        Map<String, String> userParams = new HashMap<String, String>();
        userParams.put("admin-name", data.get("admin"));
        userParams.put("domain-name", data.get("tenantDomain"));
        userParams.put("first-name", data.get("first-name"));

        emailAddress = emailAddress.trim();
        try {
            String secretKey = UUID.randomUUID().toString();

            // The password reset request hasn't been verified by the tenant yet.
            // Hence using the super tenant registry instance
            Registry registry = AdminManagementServiceComponent.
                    getConfigSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
            Resource resource = registry.newResource();
            // store the redirector url
            resource.setProperty("redirectPath", adminMgtConfig.getRedirectPath());
            // store the user data, redirectPath can be overwritten here.
            for (String s : data.keySet()) {
                resource.setProperty(s, data.get(s));
            }

            resource.setVersionableChange(false);
            String secretKeyPath = AdminMgtConstants.ADMIN_MANAGEMENT_COLLECTION +
                    RegistryConstants.PATH_SEPARATOR + secretKey;
            // resource put into the registry, with the secretKeyPath
            registry.put(secretKeyPath, resource);
            // sending the mail
            EmailSender sender = new EmailSender(adminMgtConfig, emailAddress, secretKey,
                    SuperTenantCarbonContext.getCurrentContext().getTenantDomain(true), userParams);
            sender.sendEmail();
        } catch (RegistryException e) {
            String msg = "Error in sending the password reset verification email.";
            log.error(msg, e);
            throw new AdminManagementException(msg, e);
        }
    }

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
     * @param adminName, adminName
     * @param domain,    the tenant domain.
     * @return admin management path
     * @throws AdminManagementException, if the user doesn't exist, or couldn't retrieve the path.
     */
    public static String getAdminManagementPath(String adminName, String domain) throws
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
                    RegistryConstants.PATH_SEPARATOR + adminName;
        } else {
            adminManagementPath = AdminMgtConstants.ADMIN_MANAGEMENT_FLAG_PATH +
                    RegistryConstants.PATH_SEPARATOR + domain + RegistryConstants.PATH_SEPARATOR +
                    adminName;
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
