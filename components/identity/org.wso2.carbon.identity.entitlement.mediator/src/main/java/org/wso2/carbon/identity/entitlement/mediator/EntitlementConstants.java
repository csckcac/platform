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

package org.wso2.carbon.identity.entitlement.mediator;

import org.apache.synapse.config.xml.XMLConfigConstants;

import javax.xml.namespace.QName;

/**
 * Constants are stored
 */
public class EntitlementConstants {


    public static final QName ELEMENT_ENTITLEMENT = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,
            "entitlementService");

    public static final QName ATTR_NAME_SERVICE_EPR = new QName("remoteServiceUrl");

    public static final QName ATTR_NAME_USER = new QName("remoteServiceUserName");

    public static final QName ATTR_NAME_PASSWORD = new QName("remoteServicePassword");

    public static final QName ATTR_CALLBACK_CLASS = new QName("callbackClass");

    public static final QName ATTR_DECISION_CACHING = new QName("decisionCaching");

    public static final QName ATTR_DECISION_CACHING_INTERVAL = new QName("decisionCachingInterval");

    public static final QName ATTR_MAX_DECISION_CACHING_ENTRIES = new QName("maxCacheEntries");

    public static final QName ATTR_BASIC_AUTH = new QName("basicAuth");

    public static final QName ATTR_THRIFT_HOST =  new QName("thriftHost");

    public static final QName ATTR_THRIFT_PORT = new QName("thriftPort");

    public static final QName ATTR_REUSE_SESSION =  new QName("reuseSession");

    public static final QName ATTR_CLIENT_CLASS =  new QName("clientClass");    

    public static final String SERVICE_EPR = "remoteServiceUrl";

    public static final String SERVER_URL = "serverUrl";

    public static final String USER = "remoteServiceUserName";

    public static final String PASSWORD = "remoteServicePassword";

    public static final String CALLBACK_CLASS = "callbackClass";

    public static final String DECISION_CACHING = "decisionCaching";

    public static final String DECISION_CACHING_INTERVAL = "decisionCachingInterval";

    public static final String DECISION_MAX_CACHING_ENTRIES = "maxCacheEntries";

    public static final String BASIC_AUTH = "basicAuth";

    public static final String CONTEXT = "context";

    public static final String THRIFT_HOST = "thriftHost";

    public static final String THRIFT_PORT = "thriftPort";

    public static final String CLIENT_CLASS= "clientClass";

    public static final String REUSE_SESSION = "reuseSession";

    public static final int THRIFT_TIME_OUT = 30000;

}
