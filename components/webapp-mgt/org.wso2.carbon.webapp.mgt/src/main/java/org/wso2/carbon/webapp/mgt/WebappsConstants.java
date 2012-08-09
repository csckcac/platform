/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
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
package org.wso2.carbon.webapp.mgt;

/**
 * Web Application Constants
 */
public final class WebappsConstants {
    public static final String WEBAPP_PREFIX = "webapps";
    public static final String WEBAPP_DEPLOYMENT_FOLDER = "webapps";
    public static final String WEBAPP_EXTENSION = "war";
    public static final String WEBAPP_INFO_JSP_PAGE = "/webapp-mgt/webapp_info.jsp";
    public static final int MAX_DEPTH = 10;
    public static final String ALL_WEBAPP_FILTER_PROP = "all";
    public static final String WEBAPP_FILTER_PROP = "webapp";
    public static final String JAX_WEBAPP_FILTER_PROP = "jaxWebapp";
    public static final String JAGGERY_WEBAPP_FILTER_PROP = "jaggeryWebapp";
    public static final String JAX_WEBAPP_REPO = "jaxwebapps";
    public static final String JAGGERY_WEBAPP_REPO = "jaggeryapps";
    public static final int VALVE_INDEX = 0;
    public static final String JAGGERY_APPS_PREFIX = "jaggeryapps";
    public static final String JAX_WEBAPPS_PREFIX = "jaxwebapps";

    /**
     * This is to filter out custom webapp types. If a custom webapp deployer is added, it should
     * set this as a property to filer out the custom type.
     */
    public static final String WEBAPP_FILTER = "webappFilter";

    public static final class WebappState {
        public static final String STARTED = "started";
        public static final String STOPPED = "stopped";
        public static final String ALL = "all";

        private WebappState() {}
    }

    private WebappsConstants() {
    }
}
