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

package org.wso2.carbon.apimgt.handlers.security;

public class APISecurityConstants {
    
    public static final String API_AUTH_FAILURE_HANDLER = "API_AUTH_FAILURE_HANDLER";
    
    public static final int API_AUTH_GENERAL_ERROR       = 900900;
    public static final int API_AUTH_INVALID_CREDENTIALS = 900901;
    public static final int API_AUTH_MISSING_CREDENTIALS = 900902;
    
    public static final String API_SECURITY_CONFIGURATION = "APIConsumerAuthentication.";
    public static final String API_SECURITY_AUTH_USERNAME =
            API_SECURITY_CONFIGURATION + "AdminUsername";
    public static final String API_SECURITY_AUTH_PASSWORD =
            API_SECURITY_CONFIGURATION + "AdminPassword";
    public static final String API_SECURITY_AUTHENTICATOR =
            API_SECURITY_CONFIGURATION + "Authenticator";
    public static final String API_SECURITY_OAUTH_HEADER =
            API_SECURITY_CONFIGURATION + "OAuthHeader";
    public static final String API_SECURITY_CONSUMER_KEY_HEADER_SEGMENT =
            API_SECURITY_CONFIGURATION + "KeyHeaderSegment";
    public static final String API_SECURITY_CONSUMER_KEY_SEGMENT_DELIMITER =
            API_SECURITY_CONFIGURATION + "KeySegmentDelimiter";
    public static final String API_SECURITY_OAUTH_HEADER_SPLITTER =
            API_SECURITY_CONFIGURATION + "OAuthHeaderSplitter";
    
    public static final String API_SECURITY_NS = "http://wso2.org/apimanager/security";
    public static final String API_SECURITY_NS_PREFIX = "ams";
    
    public static final int DEFAULT_MAX_VALID_KEYS = 250;
    public static final int DEFAULT_MAX_INVALID_KEYS = 100;
    
}
