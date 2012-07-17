/*
 * Copyright WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.url.mapper.internal.util;

import org.wso2.carbon.utils.CarbonUtils;

public final class UrlMapperConstants {

	public final static String SERVICE_URL_PATTERN =  "/services";
    public static final class HostProperties {
        public static final String HOST_NAME = "host.name";
        public static final String HOSTINFO = "hostinfo/";
        public static final String DEFAULT_REGISTRY_HOSTINFO_PATH="/_system/config/hostinfo/";
        public static final String HOSTINFO_DIR = "/hostinfo/";
        public static final String WAR = ".war";
        public static final String FILE_SERPERATOR = "/";
        public static final String WEB_APP = "web.app";
        public static final String TENANT_DOMAIN = "tenant.domain";
        public static final String SERVICE_EPR="service.epr";
        public final static String DOMAIN_NAME_PREFIX = ".wso2.com";
        public static final String WEB_APPS = "webapps";
        public static final String HOST_DIR = "lib/tomcat/work/Catalina";
        public static final String SERVICE_IDENTIFIER="/services";
        public static final String CATALINA_HOME = CarbonUtils.getCarbonHome() + "/lib/tomcat/work/Catalina/";
    }
}
