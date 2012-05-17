/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl;

/**
 * This class represents the constants that are used for APIManager implementation
 */
public final class APIConstants {

    //key value of the provider rxt
    public static final String PROVIDER_KEY = "provider";

    //key value of the APIImpl rxt
    public static final String API_KEY = "api";
    
    public static final String API_CONTEXT_ID = "api.context.id";
    //This is the resource name of API
    public static final String API_RESOURCE_NAME ="/api";

    //Association between documentation and its content
    public static final String DOCUMENTATION_CONTENT_ASSOCIATION = "hasContent";

    public static final String DOCUMENTATION_KEY = "document";

    //association type between provider and APIImpl
    public static final String PROVIDER_ASSOCIATION = "provides";

    //association type between API and Documentation
    public static final String DOCUMENTATION_ASSOCIATION = "document";

    //registry location of providers
    public static final String PROVIDERS_PATH = "/providers";

    //registry location of API
    public static final String API_LOCATION = "/apimgt/applicationdata/provider";

    //registry location for consumer
    public static final String API_ROOT_LOCATION = "/apimgt/applicationdata/provider";
    
    public static final String API_ICON_IMAGE = "icon";

    // Those constance are used in API artifact.
    public static final String API_OVERVIEW_NAME = "overview_name";
    public static final String API_OVERVIEW_VERSION = "overview_version";
    public static final String API_OVERVIEW_CONTEXT = "overview_context";
    public static final String API_OVERVIEW_DESCRIPTION = "overview_description";
    public static final String API_OVERVIEW_ENDPOINT_URL = "overview_endpointURL";
    public static final String API_OVERVIEW_SANDBOX_URL = "overview_sandboxURL";
    public static final String API_OVERVIEW_WSDL = "overview_WSDL";
    public static final String API_OVERVIEW_PROVIDER = "overview_provider";
    public static final String API_OVERVIEW_THUMBNAIL_URL="overview_thumbnail";
    public static final String API_OVERVIEW_STATUS="overview_status";
    public static final String API_OVERVIEW_TIER="overview_tier";
    public static final String API_OVERVIEW_IS_LATEST ="overview_isLatest";
    public static final String API_URI_TEMPLATES ="uriTemplates_entry";


    //Those constance are used in Provider artifact.
    public static final String PROVIDER_OVERVIEW_NAME= "overview_name";
    public static final String PROVIDER_OVERVIEW_EMAIL = "overview_email";
    public static final String PROVIDER_OVERVIEW_DESCRIPTION = "overview_description";

    //database columns for Subscriber
    public static final String SUBSCRIBER_FIELD_EMAIL_ADDRESS = "EMAIL_ADDRESS";
    public static final String SUBSCRIBER_FIELD_USER_ID = "USER_ID";
    public static final String SUBSCRIBER_FIELD_DATE_SUBSCRIBED = "DATE_SUBSCRIBED";

    //tables columns for subscription
    public static final String SUBSCRIPTION_FIELD_SUBSCRIPTION_ID = "SUBSCRIPTION_ID";
    public static final String SUBSCRIPTION_FIELD_TIER_ID = "TIER_ID";
    public static final String SUBSCRIPTION_FIELD_API_ID = "API_ID";
    public static final String SUBSCRIPTION_FIELD_ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String SUBSCRIPTION_FIELD_LAST_ACCESS = "LAST_ACCESSED";

    public static final String SUBSCRIPTION_KEY_TYPE = "KEY_TYPE";

    //IDENTITY OAUTH2 table
    public static final String IDENTITY_OAUTH2_FIELD_TOKEN_STATE="TOKEN_STATE";

    //documentation rxt

    public static final String DOC_NAME= "overview_name";
    public static final String DOC_SUMMARY = "overview_summary";
    public static final String DOC_TYPE = "overview_type";
    public static final String DOC_DIR = "documentation";
    public static final String INLINE_DOCUMENT_CONTENT_DIR = "contents";
    public static final String DOC_API_BASE_PATH="overview_apiBasePath";
    public static final String DOC_SOURCE_URL = "overview_sourceURL";
    public static final String DOC_SOURCE_TYPE = "overview_sourceType";
    public static final String PUBLISHED = "PUBLISHED";
    public static final String CREATED = "CREATED";


    public static class TokenStatus {
        public static final String ACTIVE = "ACTIVE";
        public static final String BLOCKED = "BLOCKED";
        public static final String REVOKED = "REVOKED";
    }

    public static final String RXT_MEDIA_TYPE = "application/vnd.wso2.registry-ext-type+xml";
    public static final int TOP_TATE_MARGIN = 4;
    
    public static final class Permissions {
        public static final String API_CREATE = "/permission/admin/manage/api/create";
        public static final String API_PUBLISH = "/permission/admin/manage/api/publish";
        public static final String API_SUBSCRIBE = "/permission/admin/manage/api/subscribe";
    }
    
    public static final String API_GATEWAY = "APIGateway.";
    public static final String API_GATEWAY_SERVER_URL = API_GATEWAY + "ServerURL";
    public static final String API_GATEWAY_USERNAME = API_GATEWAY + "Username";
    public static final String API_GATEWAY_PASSWORD = API_GATEWAY + "Password";
    public static final String API_GATEWAY_API_ENDPOINT = API_GATEWAY + "APIEndpointURL";
    
    public static final String API_KEY_MANAGER = "APIKeyManager.";
    public static final String API_KEY_MANAGER_URL = API_KEY_MANAGER + "ServerURL";
    public static final String API_KEY_MANAGER_USERNAME = API_KEY_MANAGER + "Username";
    public static final String API_KEY_MANAGER_PASSWORD = API_KEY_MANAGER + "Password";
    
    public static final String API_KEY_TYPE = "AM_KEY_TYPE";
    public static final String API_KEY_TYPE_PRODUCTION = "PRODUCTION";
    public static final String API_KEY_TYPE_SANDBOX = "SANDBOX";
    
    public static final String DEFAULT_APPLICATION_NAME = "DefaultApplication";

}
