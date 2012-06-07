/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.mgt;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.RecoveryDataDTO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.identity.mgt.mail.EmailConfig;
import org.wso2.carbon.identity.mgt.mail.EmailSender;
import org.wso2.carbon.identity.mgt.mail.EmailSendingModule;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 *  process email sending
 */
public class EmailProcessor {


    private static final Log log = LogFactory.getLog(EmailProcessor.class);

    private Map<String, EmailConfig> emailConfigs = new HashMap<String, EmailConfig>();

    private boolean emailSendingInternallyManaged;

    private EmailSender emailSender;

    public EmailProcessor() {
        loadEmailConfigurations();
        emailSender = new EmailSender();
    }

    public RecoveryDataDTO processEmail(Map<String, String> data) throws IdentityMgtException {

        EmailConfig emailConfig = null;
        RecoveryDataDTO emailDataDTO  = new RecoveryDataDTO();
        String emailAddress = data.get(IdentityMgtConstants.EMAIL_ADDRESS);

                
        if(emailAddress == null){
            throw new IdentityMgtException("Can not proceed without an email address");
        }

        emailAddress = emailAddress.trim();
        String emailConfigType = data.get(IdentityMgtConstants.EMAIL_CONFIG_TYPE);
        
        if(emailConfigType != null && emailConfigType.trim().length() > 0){
            emailConfig = emailConfigs.get(emailConfigType);
        }

        if(emailConfig == null){
            emailConfig = new EmailConfig();
        }

        try {
            String confirmationKey = UUID.randomUUID().toString();

            Registry registry = IdentityMgtServiceComponent.getRegistryService().
                    getConfigSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
            Resource resource = registry.newResource();

            if(emailConfig.getRedirectPath() != null){
                resource.setProperty(IdentityMgtConstants.REDIRECT_PATH, emailConfig.getRedirectPath());
            }

            for (Map.Entry<String, String>  entry: data.entrySet()) {
                //can not store password in to registry
                if(IdentityMgtConstants.TEMPORARY_PASSWORD.equals(entry.getKey())){
                    continue;
                }
                resource.setProperty(entry.getKey(), entry.getValue());
            }

            resource.setVersionableChange(false);
            String confirmationKeyPath = IdentityMgtConstants.IDENTITY_MANAGEMENT_DATA + "/" + confirmationKey;
            registry.put(confirmationKeyPath, resource);

            emailDataDTO.setEmailSent(false);
            emailDataDTO.setConfirmation(confirmationKey);
            emailDataDTO.setEmail(emailAddress);
            emailDataDTO.setUserId(data.get(IdentityMgtConstants.USER_NAME));
            emailDataDTO.setFirstName(data.get(IdentityMgtConstants.FIRST_NAME));
            emailDataDTO.setDomainName(data.get(IdentityMgtConstants.TENANT_DOMAIN));
            emailDataDTO.setTemporaryPassword(data.get(IdentityMgtConstants.TEMPORARY_PASSWORD));
            emailDataDTO.setEmailConfig(emailConfig);

            if(emailSendingInternallyManaged){
                
                EmailSendingModule module = emailSender.getModule();
                module.setEmailDataDTO(emailDataDTO);
                emailSender.sendEmail(module);
                emailDataDTO = new RecoveryDataDTO();
                emailDataDTO.setEmailSent(true);
                emailDataDTO.setEmail(emailAddress);

            }

        } catch (Exception e) {
            String msg = "Error in processing email sending.";
            log.error(msg, e);
            throw new IdentityMgtException(msg, e);
        }

        return emailDataDTO;
    }

    public void setEmailConfig(EmailConfig emailConfig){
        emailConfigs.put(emailConfig.getConfigType(), emailConfig);
    }



    /**
     * method to load the adminManagementConfig
     */
    public  void loadEmailConfigurations() {

        String confXml = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                IdentityMgtConstants.EMAIL_CONF_DIRECTORY + File.separator +
                IdentityMgtConstants.EMAIL_ADMIN_CONF_FILE;

        File configfile = new File(confXml);
        if (!configfile.exists()) {
            log.warn("Email Configuration File is not present at: " + confXml);
        }

        XMLStreamReader parser = null;
        InputStream stream = null;

        try {
            stream = new FileInputStream(configfile);
            parser = XMLInputFactory.newInstance().createXMLStreamReader(stream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement documentElement = builder.getDocumentElement();
            Iterator iterator = documentElement.getChildElements();
            while(iterator.hasNext()){
                OMElement omElement = (OMElement) iterator.next();
                String configType = omElement.getAttributeValue(new QName("type"));
                if(configType != null && configType.trim().length() > 0){
                    emailConfigs.put(configType, loadEmailConfig(omElement));
                }

            }
        } catch (XMLStreamException e) {
            log.warn("Error while loading email config. using default configuration");
        } catch (FileNotFoundException e) {
            log.warn("Error while loading email config. using default configuration");
        } finally {
            try {
                if(parser != null){
                    parser.close();                    
                }
                if(stream != null){
                    stream.close();
                }                
            } catch (XMLStreamException e) {
                log.error("Error while closing XML stream");
            } catch (IOException e) {
                log.error("Error while closing input stream");
            }
        }

    }

    /**
     * Loading the EmailConfig details from the given config file,
     *
     * @param configElement
     * @return - admin management config
     */
    public EmailConfig loadEmailConfig(OMElement configElement) {
        EmailConfig config = new EmailConfig();
        Iterator it = configElement.getChildElements();
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
    }

    public boolean isEmailSendingInternallyManaged() {
        return emailSendingInternallyManaged;
    }

    public void setEmailSendingInternallyManaged(boolean emailSendingInternallyManaged) {
        this.emailSendingInternallyManaged = emailSendingInternallyManaged;
    }
}
