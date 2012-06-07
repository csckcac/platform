/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.mgt.mail;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.mail.MailConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * default email sending  implementation
 */
public class DefaultEmailSendingModule extends EmailSendingModule {

    public static final String CONF_STRING = "confirmation";
    private static Log log = LogFactory.getLog(DefaultEmailSendingModule.class);


    public void processSendingEmail() {

        Map<String, String> userParameters = new HashMap<String, String>();
        Map<String, String> headerMap = new HashMap<String, String>();

        String tenantDomain = emailDataDTO.getDomainName();
        String emailAddress = emailDataDTO.getEmail();
        EmailConfig config = emailDataDTO.getEmailConfig();
        
        userParameters.put("user-id", emailDataDTO.getUserId());
        userParameters.put("domain-name", emailDataDTO.getDomainName());
        userParameters.put("first-name", emailDataDTO.getFirstName());
        userParameters.put("temporary-password", emailDataDTO.getTemporaryPassword());

        try {
            SuperTenantCarbonContext.startTenantFlow();
            SuperTenantCarbonContext.getCurrentContext().setTenantDomain(tenantDomain, true);
            if (config.getSubject().length() == 0) {
                headerMap.put(MailConstants.MAIL_HEADER_SUBJECT, EmailConfig.DEFAULT_VALUE_SUBJECT);
            } else {
                headerMap.put(MailConstants.MAIL_HEADER_SUBJECT, replacePlaceHolders(
                        config.getSubject(), userParameters));
            }
            
            String requestMessage = replacePlaceHolders(getRequestMessage(), userParameters);

            OMElement payload = OMAbstractFactory.getOMFactory().createOMElement(
                    BaseConstants.DEFAULT_TEXT_WRAPPER, null);
            payload.setText(requestMessage);
            ServiceClient serviceClient;
            ConfigurationContext configContext = IdentityMgtServiceComponent.
                                            getConfigurationContextService().getServerConfigContext();
            if (configContext != null) {
                serviceClient = new ServiceClient(configContext, null);
            } else {
                serviceClient = new ServiceClient();
            }
            Options options = new Options();
            options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
            options.setProperty(MessageContext.TRANSPORT_HEADERS, headerMap);
            options.setProperty(MailConstants.TRANSPORT_MAIL_FORMAT,
                    MailConstants.TRANSPORT_FORMAT_TEXT);
            options.setTo(new EndpointReference("mailto:" + emailAddress));
            serviceClient.setOptions(options);
            serviceClient.fireAndForget(payload);
            log.debug("Sending " +
                    "user credentials configuration mail to " + emailAddress);
            log.debug("Verification url : " + requestMessage);
            // Send the message

            log.info("User credentials configuration mail has been sent to " + emailAddress);
        } catch (AxisFault e) {
            log.error("Failed Sending Email", e);
        } finally {
            SuperTenantCarbonContext.endTenantFlow();
        }
    }

    private String getRequestMessage() {
        
        String msg;
        EmailConfig config = emailDataDTO.getEmailConfig();
        
        String targetEpr = config.getTargetEpr();
        String tenantDomain = emailDataDTO.getDomainName();
        if (tenantDomain == null) {
            SuperTenantCarbonContext.getCurrentContext().getTenantDomain(true);
        }
        if (tenantDomain != null && targetEpr.indexOf("/carbon") > 0 &&
                MultitenantUtils.getTenantDomainFromRequestURL(targetEpr) == null) {
            targetEpr = targetEpr.replace("/carbon", "/" +
                    MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/" + tenantDomain + "/carbon");
        }
        if (config.getEmailBody().length() == 0) {
            msg = EmailConfig.DEFAULT_VALUE_MESSAGE + "\n" + targetEpr + "?"
                    + CONF_STRING + "=" + emailDataDTO.getConfirmation() + "m\n";
        } else {
            msg = config.getEmailBody() + "\n" + targetEpr + "?" + CONF_STRING + "="
                    + emailDataDTO.getConfirmation() + "\n";
        }
        if (config.getEmailFooter() != null) {
            msg = msg + "\n" + config.getEmailFooter();
        }
        return msg;
    }

    /**
     * Replace the {user-parameters} in the config file with the respective values
     * @param text the initial text
     * @param userParameters mapping of the key and its value
     * @return the final text to be sent in the email
     */
    public static String replacePlaceHolders(String text, Map<String, String> userParameters) {
        if (userParameters != null) {
            for (Map.Entry<String, String> entry : userParameters.entrySet()) {
                String key = entry.getKey();
                text = text.replaceAll("\\{" + key + "\\}", entry.getValue());
            }
        }
        return text;
    }
}
