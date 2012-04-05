/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.messagebox.sqs.internal.util;

import org.apache.axiom.util.base64.Base64Utils;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.core.services.util.CarbonAuthenticationUtil;
import org.wso2.carbon.messagebox.MessageBoxConstants;
import org.wso2.carbon.messagebox.MessageBoxService;
import org.wso2.carbon.messagebox.sqs.internal.module.SQSAuthenticationException;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.RegistryConstants;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class Utils {
    private static final Log log = LogFactory.getLog(Utils.class);


    public static String getMessageRequestId() {
        return UUID.randomUUID().toString();
        //Todo: should return the actual message requested id rather uuid,
        // Todo: otherwise user can not identify the message
    }

    public static String getQueueNameFromRequestURI() {
        try {
            String queueUrl = MessageContext.getCurrentMessageContext().getTo().getAddress();
            return queueUrl.split("MessageQueue/")[1];
        } catch (Exception e) {
            log.error("Failed to get Queue Name");
            return null;
        }
    }

    public static MessageBoxService getMessageBoxService() {
        return MessageBoxHolder.getInstance().getMessageboxService();
    }

    public static String getMD5OfMessage(String messageBody) {
        try {
            URLEncoder.encode(messageBody, MessageBoxConstants.URL_ENCODING);
        } catch (UnsupportedEncodingException e) {
            log.error("Failed to URL encode the message " + messageBody);
        }
        String MD5OfMessage = null;
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(messageBody.getBytes());
            byte[] hash = digest.digest();
            MD5OfMessage = Base64Utils.encode(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to get MD5 hash in message " + messageBody);
        }
        return MD5OfMessage;
    }

    public static URI constructResponseURL(String queueName) {
        HttpServletRequest httpServletRequest = ((HttpServletRequest) MessageContext.
                getCurrentMessageContext().getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST));
        String finalResponseUrl = httpServletRequest.getRequestURL().toString();
        // in order to access queues users have to use this address
        finalResponseUrl = finalResponseUrl.substring(0, finalResponseUrl.indexOf("QueueService"));
        finalResponseUrl = finalResponseUrl + "MessageQueue/" + queueName;
        return ConverterUtil.convertToURI(finalResponseUrl);
    }

    public static boolean validQueueName(String queueName) {
        if (queueName != null) {
            String queueNameRegex = "([a-zA-Z0-9_\\-])+";
            return Pattern.matches(queueNameRegex, queueName);
        } else {
            return false;
        }

    }

    public static boolean validMessageBody(String messageBody) {
        return true;
        //ToDo: write a regular expression and validate message body
    }

    public static Map<String, String> getSQSErrorCodeDescriptionMap() {
        Map<String, String> sqsErrorCodes = new HashMap<String, String>();
        sqsErrorCodes.put("AccessDenied", "Access to the resource is denied.");
        sqsErrorCodes.put("AuthFailure", "A value used for authentication could not be validated, " +
                                         "such as Signature.");
        sqsErrorCodes.put("InternalError", "There is an internal problem with SQS, " +
                                           "which you cannot resolve.");
        sqsErrorCodes.put("AWS.SimpleQueueService.NonExistentQueue", "Queue does not exist.");
        sqsErrorCodes.put("InvalidAccessKeyId", "AWS was not able to validate the \n" +
                                                "provided access credentials. ");
        sqsErrorCodes.put("InvalidAction", "The action specified was invalid. ");
        sqsErrorCodes.put("InvalidParameterValue", "One or more parameters cannot be \n" +
                                                   "validated. ");
        sqsErrorCodes.put("AWS.SimpleQueueService.QueueNameExists",
                          " Queue already exists. SQS returns this error only if the request " +
                          "includes a DefaultVisibilityTimeout value that differs from the value " +
                          " for the existing queue.");
        return sqsErrorCodes;
    }

    public static String getUserSecretAccessKey(String accessKeyId, MessageContext messageContext)
            throws SQSAuthenticationException {
        try {
            org.wso2.carbon.registry.api.Registry registry =
                    MessageBoxHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(
                            SuperTenantCarbonContext.getCurrentContext(messageContext).getTenantId());

            String userName = getUserName(accessKeyId, messageContext);
            if (registry.resourceExists(RegistryConstants.PROFILES_PATH + userName)) {
                org.wso2.carbon.registry.api.Collection userCollection =
                        (org.wso2.carbon.registry.api.Collection) registry.
                                get(RegistryConstants.PROFILES_PATH + userName);
                if (userCollection != null) {
                    return userCollection.getProperty(MessageBoxConstants.SECRET_ACCESS_KEY_ID);
                }
            }

            return null;
        } catch (RegistryException e) {
            throw new SQSAuthenticationException("Failed to get secret id of user " + accessKeyId);
        }
    }

    public static String getUserName(String accessKeyId, MessageContext messageContext)
            throws SQSAuthenticationException {
        try {
            org.wso2.carbon.registry.api.Registry registry =
                    MessageBoxHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(
                            SuperTenantCarbonContext.getCurrentContext(messageContext).getTenantId());

            if (registry.resourceExists(MessageBoxConstants.REGISTRY_ACCESS_KEY_INDEX_PATH)) {
                org.wso2.carbon.registry.api.Collection userCollection =
                        (org.wso2.carbon.registry.api.Collection) registry.
                                get(MessageBoxConstants.REGISTRY_ACCESS_KEY_INDEX_PATH);
                if (userCollection != null) {
                    return userCollection.getProperty(accessKeyId);
                }
            }
            return null;
        } catch (RegistryException e) {
            throw new SQSAuthenticationException("Failed to get secret id of user " + accessKeyId);
        }
    }

    public static void onSuccessAdminLogin(MessageContext messageContext, String accessKeyId)
            throws SQSAuthenticationException {
        Object servletRequest = messageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        messageContext.setProperty(MessageBoxConstants.SQS_AUTHENTICATED, Boolean.TRUE);
        if (servletRequest != null) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

            try {
                SuperTenantCarbonContext carbonContext =
                        SuperTenantCarbonContext.getCurrentContext(messageContext);
                CarbonAuthenticationUtil.onSuccessAdminLogin(httpServletRequest.getSession(),
                                                             accessKeyId,
                                                             carbonContext.getTenantId(),
                                                             carbonContext.getTenantDomain(),
                                                             null);
            } catch (Exception e) {
                throw new SQSAuthenticationException("Failed to set on success admin " +
                                                     "log in parameters" + e.getMessage());
            }

        }
    }
}
