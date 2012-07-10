/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.entitlement.filter;

public class EntitlementConstants {

    public static final String USER = "remoteServiceUserName";

    public static final String PASSWORD = "remoteServicePassword";

    public static final String HOST = "remoteServiceHost";

    public static final String PORT = "remoteServicePort";

    public static final String CONTEXT = "context";

    public static final String CLIENT_CLASS = "clientClass";

    public static final String SUBJECT_SCOPE = "subjectScope";

    public static final String SUBJECT_ATTRIBUTE_NAME = "subjectAttributeName";

    public static final String DECISION_CACHING = "decisionCaching";

    public static final String MAX_CACHE_ENTRIES = "maxCacheEntries";

    public static final String CACHE_INVALIDATION_INTERVAL = "cacheInvalidationInterval";

    public static final String AUTH_REDIRECT_URL = "authRedirectUrl";

    public final static String DECISION_CACHE = "DECISION_CACHE";

    public static final String THRIFT_HOST = "thriftHost";

    public static final String THRIFT_PORT = "thriftPort";

    public final static int SIMPLE_CACHE_MAX_ENTRIES = 10000;

    public static final int DEFAULT_THRIFT_PORT = 10500;

    public static final int THRIFT_TIME_OUT = 30000;

    public final static String DEFAULT = "default";

    public final static String ENABLE = "enable";

    public final static String DISABLE = "disable";

    public final static String WSO2_IS = "wso2is";

    public final static String WSO2_AS = "wso2as";

    public final static String WEB_APP = "webapp";

    public final static String PERMIT = "PERMIT";

    public final static String DENY = "DENY";

    public final static String NOT_APPLICABLE = "NotApplicable";

    public final static String REQUEST_PARAM = "request-param";

    public final static String REQUEST_ATTIBUTE = "request-attribute";

    public final static String SESSION = "session";

    public final static String HTTPS_PORT = "httpsPort";

    public final static String AUTHENTICATION = "authentication";

    public final static String AUTHENTICATION_PAGE = "authenticationPage";

    public final static String AUTHENTICATION_PAGE_URL = "authenticationPageUrl";

    public final static String TRUST_STORE = "javax.net.ssl.trustStore";

    public final static String TRUST_STORE_PASSWORD = "javax.net.ssl.trustStorePassword";

}
