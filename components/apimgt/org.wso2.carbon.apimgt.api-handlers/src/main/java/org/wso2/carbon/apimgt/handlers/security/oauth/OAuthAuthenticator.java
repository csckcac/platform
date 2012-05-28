/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.handlers.security.oauth;

import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.handlers.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.handlers.security.*;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.xsd.APIKeyValidationInfoDTO;

import java.util.Map;

/**
 * An API consumer authenticator which authenticates user requests using
 * the OAuth protocol. This implementation uses some default token/delimiter
 * values to parse OAuth headers, but if needed these settings can be overridden
 * through the APIManagerConfiguration.
 */
public class OAuthAuthenticator implements Authenticator {
    
    protected APIKeyValidator keyValidator;
    
    private String securityHeader = HttpHeaders.AUTHORIZATION;
    private String consumerKeyHeaderSegment = "Bearer";
    private String oauthHeaderSplitter = ",";
    private String consumerKeySegmentDelimiter = " ";

    public OAuthAuthenticator() {
        this.keyValidator = new APIKeyValidator();
        initOAuthParams();
    }

    public boolean authenticate(MessageContext synCtx) throws APISecurityException {
        Map headers = (Map) ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String apiKey = null;
        if (headers != null) {
            apiKey = extractCustomerKeyFromAuthHeader(headers);
        }
        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);

        if (apiKey == null || apiContext == null || apiVersion == null) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_MISSING_CREDENTIALS,
                    "Required OAuth credentials not provided");
        }

        APIKeyValidationInfoDTO info = keyValidator.getKeyValidationInfo(apiContext, apiKey, apiVersion);
        if (info.getAuthorized()) {
            AuthenticationContext authContext = new AuthenticationContext();
            authContext.setAuthenticated(true);
            authContext.setTier(info.getTier());
            authContext.setApiKey(apiKey);
            authContext.setKeyType(info.getType());
            authContext.setUsername(info.getUsername());
            APISecurityUtils.setAuthenticationContext(synCtx, authContext);
            return true;
        } else {
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                    "Authentication failure for API: " + apiContext + ", version: " + apiVersion +
                            " with key: " + apiKey);
        }
    }

    /**
     * Extracts the customer API key from the OAuth Authentication header. If the required
     * security header is present in the provided map, it will be removed from the map
     * after processing.
     * 
     * @param headersMap Map of HTTP headers
     * @return extracted customer key value or null if the required header is not present
     */
    public String extractCustomerKeyFromAuthHeader(Map headersMap) {
        // Remove the OAuth authorization header from the message
        // It shouldn't go beyond this point
        String authHeader = (String) headersMap.remove(securityHeader);
        if (authHeader == null) {
            return null;
        }
        
        if (authHeader.startsWith("OAuth ") || authHeader.startsWith("oauth ")) {
            authHeader = authHeader.substring(authHeader.indexOf("o"));
        }

        String[] headers = authHeader.split(oauthHeaderSplitter);
        if (headers != null) {
            for (int i = 0; i < headers.length; i++) {
                String[] elements = headers[i].split(consumerKeySegmentDelimiter);
                if (elements != null && elements.length > 1) {
                    int j = 0;
                    for (String element : elements) {
                        if (!"".equals(element.trim()) &&
                                consumerKeyHeaderSegment.equals(elements[j].trim())){
                            return removeLeadingAndTrailing(elements[j + 1].trim());
                        }
                        j++;
                    }
                }
            }
        }

        return null;
    }

    private String removeLeadingAndTrailing(String base) {
        String result = base;

        if (base.startsWith("\"") || base.endsWith("\"")) {
            result = base.replace("\"", "");
        }
        return result.trim();
    }
    
    protected void initOAuthParams() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        String value = config.getFirstProperty(
                APISecurityConstants.API_SECURITY_OAUTH_HEADER);
        if (value != null) {
            securityHeader = value;
        }

        value = config.getFirstProperty(
                APISecurityConstants.API_SECURITY_CONSUMER_KEY_HEADER_SEGMENT);
        if (value != null) {
            consumerKeyHeaderSegment = value;
        }

        value = config.getFirstProperty(
                APISecurityConstants.API_SECURITY_OAUTH_HEADER_SPLITTER);
        if (value != null) {
            oauthHeaderSplitter = value;
        }

        value = config.getFirstProperty(
                APISecurityConstants.API_SECURITY_CONSUMER_KEY_SEGMENT_DELIMITER);
        if (value != null) {
            consumerKeySegmentDelimiter = value;
        }
    }

    public String getChallengeString() {
        return "OAuth2 realm=\"WSO2 API Manager\"";
    }
}
