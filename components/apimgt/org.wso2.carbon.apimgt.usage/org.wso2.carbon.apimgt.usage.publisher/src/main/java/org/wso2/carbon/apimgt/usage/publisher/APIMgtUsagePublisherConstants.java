/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.usage.publisher;

public final class APIMgtUsagePublisherConstants {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static String _OAUTH_HEADERS_SPLITTER = ",";
    public static String _OAUTH_CONSUMER_KEY = "Bearer";
    public static  String HEADER_SEGMENT_DELIMITER = " ";
    public static  String HTTP_METHOD = "HTTP_METHOD";

    public static final String API_USAGE_TRACKING = "APIUsageTracking.";
    public static final String API_USAGE_ENABLED = API_USAGE_TRACKING + "Enabled";
    public static final String API_USAGE_THRIFT_PORT = API_USAGE_TRACKING + "ThriftPort";
    public static final String API_USAGE_BAM_SERVER_URL = API_USAGE_TRACKING + "BAMServerURL";
    public static final String API_USAGE_BAM_SERVER_USER = API_USAGE_TRACKING + "BAMUser";
    public static final String API_USAGE_BAM_SERVER_PASSWORD = API_USAGE_TRACKING + "BAMPassword";
    public static final String API_USAGE_BAM_TRUSTSTORE = API_USAGE_TRACKING + "BAMTrustStore";
    public static final String API_USAGE_BAM_TRUSTSTORE_PASSWORD = API_USAGE_TRACKING + "BAMTrustStorePassword";
    public static final String  CONSUMER_KEY = "consumerKey";
    public static final String  CONTEXT = "context";
    public static final String  API_VERSION = "api_version";
    public static final String  API = "api";
    public static final String  VERSION = "version";
    public static final String  REQUEST_TIME = "requestTime";
    public static final String  RESOURCE = "resource";

}
