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
package org.wso2.carbon.messagebox.sqs.internal.module;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.MultipleEntryHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Base64;
import org.wso2.carbon.messagebox.MessageBoxConstants;
import org.wso2.carbon.messagebox.MessageBoxException;
import org.wso2.carbon.messagebox.sqs.internal.FaultResponse;
import org.wso2.carbon.messagebox.sqs.internal.util.Utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class AuthenticationHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(AuthenticationHandler.class);

    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {
        if (messageContext.isEngaged(MessageBoxConstants.SQS_AUTHENTICATION_MODULE_NAME)) {

            MultipleEntryHashMap multipleEntryHashMap =
                    (MultipleEntryHashMap) messageContext.getProperty(Constants.REQUEST_PARAMETER_MAP);

            if (multipleEntryHashMap != null) {
                // get signature and signature calculating details from url parameters
                //  and verify signature
                try {
                    if (!verifySignatureOnRestBasedRequest(messageContext, multipleEntryHashMap)) {
                        throw new FaultResponse(new MessageBoxException("AuthFailure"),
                                                messageContext.getMessageID()).createAxisFault();
                    }
                } catch (SQSAuthenticationException e) {
                    throw new AxisFault(e.getMessage());
                }
            } else {
                // verify signature in soap header
                try {
                    if (!verifySignatureOnSOAPHeader(messageContext)) {
                        throw new FaultResponse(new MessageBoxException("AuthFailure"),
                                                messageContext.getMessageID()).createAxisFault();
                    }
                } catch (SQSAuthenticationException e) {
                    throw new AxisFault(e.getMessage());
                }
            }
            // carbon authentication handler will check for authentication
        }
        return InvocationResponse.CONTINUE;
    }

    private boolean verifySignatureOnSOAPHeader(MessageContext messageContext)
            throws SQSAuthenticationException {
        String accessKeyId = null;
        String timestamp = null;
        String signatureOnSoapMessage = null;
        SOAPEnvelope soapEnvelope = messageContext.getEnvelope();
        SOAPHeader soapHeader = soapEnvelope.getHeader();
        String actionName = messageContext.getSoapAction();
        if (soapHeader != null) {
            Iterator iterator = soapHeader.getChildrenWithName(MessageBoxConstants.ACCESS_KEY_ID_QNAME);
            if (iterator != null && iterator.hasNext()) {
                accessKeyId = ((OMElement) iterator.next()).getText().trim();
            }

            iterator = soapHeader.getChildrenWithName(MessageBoxConstants.TIMESTAMP_QNAME);
            if (iterator != null && iterator.hasNext()) {
                timestamp = ((OMElement) iterator.next()).getText().trim();
            }

            iterator = soapHeader.getChildrenWithName(MessageBoxConstants.SIGNATURE_QNAME);
            if (iterator != null && iterator.hasNext()) {
                signatureOnSoapMessage = ((OMElement) iterator.next()).getText().trim();
            }

            if (accessKeyId != null && actionName != null && timestamp != null &&
                signatureOnSoapMessage != null) {
                if (compareSignatures(messageContext, accessKeyId, timestamp, actionName,
                                      signatureOnSoapMessage,
                                      MessageBoxConstants.HMAC_SHA1_ALGORITHM, "0")) {
                    String userName = Utils.getUserName(accessKeyId, messageContext);
                    Utils.onSuccessAdminLogin(messageContext, userName);
                    log.info(userName + " is successfully authenticated for request " +
                             "with action, " + actionName);
                    return true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean verifySignatureOnRestBasedRequest(MessageContext messageContext,
                                                      MultipleEntryHashMap multipleEntryHashMap)
            throws SQSAuthenticationException {

        Object accessKeyIdProperty = multipleEntryHashMap.get(MessageBoxConstants.AWSACCESS_KEY_ID);
        Object actionNameProperty = multipleEntryHashMap.get(MessageBoxConstants.ACTION);
        Object timestampProperty = multipleEntryHashMap.get(MessageBoxConstants.TIMESTAMP);
        Object signatureProperty = multipleEntryHashMap.get(MessageBoxConstants.SIGNATURE);
        Object signatureMethod = multipleEntryHashMap.get(MessageBoxConstants.SIGNATURE_METHOD);
        Object signatureVersion = multipleEntryHashMap.get(MessageBoxConstants.SIGNATURE_VERSION);

        if (accessKeyIdProperty != null && actionNameProperty != null &&
            timestampProperty != null && signatureProperty != null && signatureVersion != null) {

            // adding all properties got from multiple hash map, when get from multiple hash map,
            //  they are removed.
            multipleEntryHashMap.put(MessageBoxConstants.AWSACCESS_KEY_ID, accessKeyIdProperty);
            multipleEntryHashMap.put(MessageBoxConstants.ACTION, actionNameProperty);
            multipleEntryHashMap.put(MessageBoxConstants.TIMESTAMP, timestampProperty);
            multipleEntryHashMap.put(MessageBoxConstants.SIGNATURE, signatureProperty);
            multipleEntryHashMap.put(MessageBoxConstants.SIGNATURE_METHOD, signatureMethod);
            multipleEntryHashMap.put(MessageBoxConstants.SIGNATURE_VERSION, signatureVersion);


            String accessKeyId = accessKeyIdProperty.toString().trim();
            String timestamp = timestampProperty.toString().trim();
            String actionName = actionNameProperty.toString().trim();
            String signatureOnSoapMessage = signatureProperty.toString().trim();
            if (signatureMethod == null) {
                throw new SQSAuthenticationException("Signature method can not be null in request!");
            }

            if (compareSignatures(messageContext, accessKeyId, timestamp, actionName,
                                  signatureOnSoapMessage, signatureMethod.toString(),
                                  signatureVersion.toString())) {
                String userName = Utils.getUserName(accessKeyId, messageContext);
                Utils.onSuccessAdminLogin(messageContext, userName);
                log.info(userName + " is successfully authenticated for request " +
                         "with action, " + actionName);
                return true;
            } else {
                log.info("Failed to authenticate request with access key id: " + accessKeyId +
                         " and action:" + actionName);
                return false;
            }
        }
        return true;
    }


    private boolean compareSignatures(MessageContext messageContext, String accessKeyId,
                                      String timestamp, String actionName,
                                      String signatureOnSoapMessage, String signatureMethod,
                                      String signatureVersion)
            throws SQSAuthenticationException {

        String dataTobeSigned;
        MultipleEntryHashMap multipleEntryHashMap =
                (MultipleEntryHashMap) messageContext.getProperty(Constants.REQUEST_PARAMETER_MAP);
        if ("0".equals(signatureVersion)) {
            dataTobeSigned = actionName + timestamp;
        } else if ("1".equals(signatureVersion)) {
            dataTobeSigned = getDataTobeSignedUsingVersion1(convertMultipleEntryHashMapToMap(multipleEntryHashMap));
        } else if ("2".equals(signatureVersion)) {
            HttpServletRequest httpServletRequest = ((HttpServletRequest) messageContext.
                    getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST));
            dataTobeSigned = getDataTobeSignedUsingVersion2(multipleEntryHashMap,
                                                            httpServletRequest.getRequestURL().toString());
        } else {
            throw new SQSAuthenticationException("Signature version " + signatureVersion +
                                                 " is not supported.");
        }
        String secretAccessKey = Utils.getUserSecretAccessKey(accessKeyId, messageContext);
        if (secretAccessKey == null) {
            throw new SQSAuthenticationException("Failed to get secretAccessKey of access key id:" + accessKeyId);
        }
        String signature = calculateRFC2104HMAC(dataTobeSigned, secretAccessKey, signatureMethod);
        return signature.equals(signatureOnSoapMessage);
    }

    private String getDataTobeSignedUsingVersion1(Map<String, String> parameters) {
        StringBuilder dataTobeSigned = new StringBuilder();
        SortedMap<String, String> sorted =
                new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        sorted.putAll(parameters);

        for (Map.Entry entry : sorted.entrySet()) {
            dataTobeSigned.append(entry.getKey());
            dataTobeSigned.append(entry.getValue());
        }

        return dataTobeSigned.toString();
    }

    private String getDataTobeSignedUsingVersion2(MultipleEntryHashMap multipleEntryHashMap,
                                                  String queueUrl)
            throws SQSAuthenticationException {

        URI endpoint;
        try {
            endpoint = new URI(queueUrl);
        } catch (URISyntaxException e) {
            throw new SQSAuthenticationException("Queue URL:" + queueUrl + " is not valid.", e);
        }

        StringBuilder dataTobeSigned = new StringBuilder();
        dataTobeSigned.append("POST").append("\n");
        dataTobeSigned.append(getCanonicalizedEndpoint(endpoint)).append("\n");
        dataTobeSigned.append(getCanonicalizedResourcePath(endpoint)).append("\n");
        Map<String, String> parameterMap = convertMultipleEntryHashMapToMap(multipleEntryHashMap);
        parameterMap.remove(MessageBoxConstants.SIGNATURE);
        dataTobeSigned.append(getCanonicalizedQueryString(parameterMap));
        return dataTobeSigned.toString();

    }


    private static String calculateRFC2104HMAC(String data, String key, String signatureMethod)
            throws SQSAuthenticationException {
        String result;
        try {
            // get an hmac_sha1 key from the raw key bytes
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), signatureMethod);
            // get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance(signatureMethod);
            mac.init(signingKey);
            // compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(data.getBytes());
            // base64-encode the hmac
            result = Base64.encode(rawHmac);
        } catch (Exception e) {
            throw new SQSAuthenticationException("Failed to generate HMAC : " + e.getMessage());
        }

        return result;

    }

    private Map<String, String> convertMultipleEntryHashMapToMap(
            MultipleEntryHashMap multipleEntryHashMap) throws SQSAuthenticationException {
        Map<String, String> map = new HashMap<String, String>();
        for (Object key : multipleEntryHashMap.keySet()) {
            if (key instanceof String) {
                try {
                    String entryKey = (String) key;
                    Object entryValue;
                    if ((entryValue = multipleEntryHashMap.get(entryKey)) != null) {
                        multipleEntryHashMap.put(entryKey, entryValue);
                        entryKey = URLDecoder.decode(entryKey, MessageBoxConstants.URL_ENCODING);
                        map.put(entryKey, URLDecoder.decode((String) entryValue,
                                                            MessageBoxConstants.URL_ENCODING));
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new SQSAuthenticationException("Failed to decode string " + e.getMessage(), e);
                }
            }
        }
        return map;
    }

    private String getCanonicalizedQueryString(Map<String, String> parameters) {
        SortedMap<String, String> sorted = new TreeMap<String, String>();
        sorted.putAll(parameters);

        StringBuilder builder = new StringBuilder();
        Iterator<Map.Entry<String, String>> pairs = sorted.entrySet().iterator();
        while (pairs.hasNext()) {
            Map.Entry<String, String> pair = pairs.next();
            String key = pair.getKey();
            String value = pair.getValue();
            builder.append(urlEncode(key, false));
            builder.append("=");
            builder.append(urlEncode(value, false));
            if (pairs.hasNext()) {
                builder.append("&");
            }
        }

        return builder.toString();
    }

    private String getCanonicalizedResourcePath(URI endpoint) {
        String uri = endpoint.getPath();
        if (uri == null || uri.length() == 0) {
            return "/";
        } else {
            return urlEncode(uri, true);
        }
    }

    private String getCanonicalizedEndpoint(URI endpoint) {
        String endpointForStringToSign = endpoint.getHost().toLowerCase();
        /*
         * Apache HttpClient will omit the port in the Host header for default
         * port values (i.e. 80 for HTTP and 443 for HTTPS) even if we
         * explicitly specify it, so we need to be careful that we use the same
         * value here when we calculate the string to sign and in the Host
         * header we send in the HTTP request.
         */
        if (isUsingNonDefaultPort(endpoint)) {
            endpointForStringToSign += ":" + endpoint.getPort();
        }

        return endpointForStringToSign;
    }

    private String urlEncode(String value, boolean path) {
        try {
            String encoded = URLEncoder.encode(value, "UTF-8")
                    .replace("+", "%20").replace("*", "%2A")
                    .replace("%7E", "~");
            if (path) {
                encoded = encoded.replace("%2F", "/");
            }

            return encoded;
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }


    private boolean isUsingNonDefaultPort(URI uri) {
        String scheme = uri.getScheme().toLowerCase();
        int port = uri.getPort();

        return port > 0 &&
               !(scheme.equals("http") && port == 80)
               && !(scheme.equals("https") && port == 443);

    }

    @Override
    public void flowComplete(MessageContext msgContext) {
        Object sqsAuthenticated = msgContext.getProperty(MessageBoxConstants.SQS_AUTHENTICATED);
        msgContext.removeProperty(MessageBoxConstants.SQS_AUTHENTICATED);
        if (sqsAuthenticated != null) {
            boolean authenticated = (Boolean)sqsAuthenticated;
            if (authenticated) {
                Object servletRequest = msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
                HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
                httpServletRequest.getSession().invalidate();
            }
        }
    }
}