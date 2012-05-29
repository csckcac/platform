/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.email.verification.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;


public class Util {
    private static final Log log = LogFactory.getLog(Util.class);

    private static RegistryService registryService;
    public static Map<String, EmailVerifierConfig> serviceConfigMap =
            new HashMap<String, EmailVerifierConfig>();
    private static final String EMAIL_VERIFICATION_COLLECTION =
            "/repository/components/org.wso2.carbon.email-verification/email-verifications-map";
    private static final String VERIFIED_EMAIL_RESOURCE_PATH =
            "/repository/components/org.wso2.carbon.email-verification/emailIndex";

    public static synchronized void setRegistryService(RegistryService service) {
        if (registryService == null) {
            registryService = service;
        }
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static UserRegistry getConfigSystemRegistry(int tenantId) throws RegistryException {
        return registryService.getConfigSystemRegistry(tenantId);
    }


    public static EmailVerifierConfig loadeMailVerificationConfig(String configFilename) {
        EmailVerifierConfig config = new EmailVerifierConfig();
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
            String msg = "Error in loading configuration for email verification: " +
                    configFilename + ".";
            log.error(msg, e);
            return null;
        }
    }

    public static ConfirmationBean confirmUser(String secretKey) throws Exception {
        ConfirmationBean confirmationBean = new ConfirmationBean();
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement data = fac.createOMElement("configuration", null);

        Registry registry = Util.getConfigSystemRegistry(0);
        boolean success = false;
        try {
            registry.beginTransaction();

            String secretKeyPath = EMAIL_VERIFICATION_COLLECTION + "/" + secretKey;
            if (!registry.resourceExists(secretKeyPath)) {
                String msg = "Email verification failed.";
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
            registry.delete(resource.getPath());// remove the temporaraly store resource from registry
            confirmationBean.setData(data.toString());
            success = true;

            //when verifying the user ,email address is being persisted in order to be recognized for one time verification
            if (success) {
                Resource tempResource;
                if (registry.resourceExists(VERIFIED_EMAIL_RESOURCE_PATH)) {
                    String verifyingEmail = data.getFirstChildWithName(new QName("email")).getText();
                    String key = UUIDGenerator.generateUUID();
                    tempResource = registry.get(VERIFIED_EMAIL_RESOURCE_PATH);
                    if (tempResource != null) {
                        tempResource.setProperty(key, verifyingEmail);
                    }
                    registry.put(VERIFIED_EMAIL_RESOURCE_PATH, tempResource);
                }
            }

        } finally {
            if (success) {
                registry.commitTransaction();
            } else {
                registry.rollbackTransaction();
            }
        }
        return confirmationBean;
    }

    /*
    public static void requestUserVerification(Map<String,String> data, String configFilename) throws Exception {
        EmailVerifierConfig serviceConfig = serviceConfigMap.get(configFilename);
        if (serviceConfig == null) {
            // hm, we have to load the stuff
            serviceConfig = Util.loadeMailVerificationConfig(configFilename);
        }
    } */

    public static void requestUserVerification(Map<String, String> data,
                                               EmailVerifierConfig serviceConfig) throws Exception {
        String emailAddress = data.get("email");

        emailAddress = emailAddress.trim();
        try {
            String secretKey = UUID.randomUUID().toString();

            // User is supposed to give where he wants to store the intermediate data.
            // But, here there is no tenant signing in happened yet.
            // So get the super tenant registry instance.
            Registry registry = Util.getConfigSystemRegistry(0);
            Resource resource = registry.newResource();
            // store the redirector url
            resource.setProperty("redirectPath", serviceConfig.getRedirectPath());
            // store the user data, redirectPath can be overwritten here.
            for (String s : data.keySet()) {
                resource.setProperty(s, data.get(s));
            }

            ((ResourceImpl) resource).setVersionableChange(false);
            String secretKeyPath = EMAIL_VERIFICATION_COLLECTION +
                    RegistryConstants.PATH_SEPARATOR + secretKey;
            registry.put(secretKeyPath, resource);
            // sending the mail
            EmailSender sender = new EmailSender(serviceConfig, emailAddress, secretKey,
                    SuperTenantCarbonContext.getCurrentContext().getTenantDomain(true), data);
            sender.sendEmail();
        } catch (Exception e) {
            String msg = "Error in sending the email to validation.";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
    }
}
