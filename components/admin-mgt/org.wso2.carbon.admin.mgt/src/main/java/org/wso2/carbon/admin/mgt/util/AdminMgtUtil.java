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
import org.wso2.carbon.admin.mgt.internal.AdminManagementServiceComponent;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.UserStoreException;
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
        adminMgtConfig = loadAdminManagementConfig(confXml);
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
     * Confirm that the admin management request has been sent by the user.
     *
     * @param secretKey the secret key to be sent
     * @return ConfirmationBean
     * @throws Exception if admin account management attempt failed.
     */
    public static ConfirmationBean confirmUser(String secretKey) throws Exception {
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

            String secretKeyPath = AdminMgtConstants.ADMIN_MANAGEMENT_COLLECTION + "/" + secretKey;
            if (!registry.resourceExists(secretKeyPath)) {
                String msg = "Failed Admin account management attempt.";
                log.error(msg);
                throw new Exception(msg);
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
     * @throws Exception if loading config or sending verification fail.
     */
    public static void requestUserVerification(Map<String, String> data) throws Exception {
        try {
            loadAdminManagementConfig();
        } catch (Exception e) {
            String msg = "Error in loading the admin management configurations";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
        try {
            requestUserVerification(data, adminMgtConfig);
        } catch (Exception e) {
            String msg = "Error in sending verification";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
    }


    /**
     * verifying the admin management request
     *
     * @param data          - data to include in the mail
     * @param serviceConfig - adminManagementConfig
     * @throws Exception if sending verification fails.
     */
    public static void requestUserVerification(
            Map<String, String> data, AdminManagementConfig serviceConfig) throws Exception {
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
            resource.setProperty("redirectPath", serviceConfig.getRedirectPath());
            // store the user data, redirectPath can be overwritten here.
            for (String s : data.keySet()) {
                resource.setProperty(s, data.get(s));
            }

            ((ResourceImpl) resource).setVersionableChange(false);
            String secretKeyPath = AdminMgtConstants.ADMIN_MANAGEMENT_COLLECTION + "/" + secretKey;
            registry.put(secretKeyPath, resource);
            // sending the mail
            EmailSender sender = new EmailSender(serviceConfig, emailAddress, secretKey,
                                                 SuperTenantCarbonContext.getCurrentContext().
                                                         getTenantDomain(true), userParams);
            sender.sendEmail();
        } catch (Exception e) {
            String msg = "Error in sending the email.";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
    }

    /**
     * Validate the tenant domain
     *
     * @param domainName tenant domain
     * @throws Exception , if invalid tenant domain name is given
     */
    public static void validateDomain(String domainName) throws Exception {
        if (domainName == null || domainName.equals("")) {
            String msg = "Provided domain name is empty.";
            log.error(msg);
            throw new Exception(msg);
        }
        int indexOfDot = domainName.indexOf(".");
        if (indexOfDot == 0) {
            // can't start a domain starting with ".";
            String msg = "Invalid domain, starting with '.'";
            log.error(msg);
            throw new Exception(msg);
        }
        // check the tenant domain contains any illegal characters
        if (domainName.matches(AdminMgtConstants.ILLEGAL_CHARACTERS_FOR_TENANT_DOMAIN)) {
            String msg = "The tenant domain ' " + domainName +
                         " ' contains one or more illegal characters. the valid characters are " +
                         "letters, numbers, '.', '-' and '_'";
            log.error(msg);
            throw new Exception(msg);
        }
    }

    /**
     * Gets the tenant id from the tenant domain
     * @param domain - tenant domain
     * @param tenantManager - TenantManager
     * @return - tenantId
     * @throws org.wso2.carbon.user.api.UserStoreException, catches this if the tenant doesn't exist
     */
    public static int getTenantIdFromDomain(String domain,
                                            TenantManager tenantManager) throws Exception {
        int tenantId;
        if (domain.trim().equals("")) {
            tenantId = MultitenantConstants.SUPER_TENANT_ID;
            if (log.isDebugEnabled()) {
                String msg = "Super Tenant";
                log.debug(msg);
            }
        } else {
             try {
                tenantId = tenantManager.getTenantId(domain);
                if (tenantId < 1) {
                    String msg = "Only the existing tenants can update the password";
                    log.error(msg);
                    throw new Exception(msg);
                }
            } catch (UserStoreException e) {
                String msg = "Error in retrieving tenant id of tenant domain: " + domain + ".";
                log.error(msg);
                throw new Exception(msg, e);
            }
        }
        return tenantId;
    }

    /**
     * Cleanup the used resources
     * @param superTenantSystemRegistry, SuperTenantSystemRegistry
     * @param resource, the resource
     * @throws Exception, if the cleanup failed.
     */
    public static void cleanupResources(UserRegistry superTenantSystemRegistry,
                                        Resource resource) throws Exception {
        if (resource == null) {
            String msg = "Resource doesn't exist";
            log.error(msg);
            throw new Exception(msg);
        } else if ((superTenantSystemRegistry.get(resource.getPath()) != null)) {
            try {
                superTenantSystemRegistry.delete(resource.getPath());
            } catch (Exception e) {
                String msg = "Unable to delete the resource";
                log.error(msg, e);
                throw new Exception(msg, e);
            }
        }
    }
}
