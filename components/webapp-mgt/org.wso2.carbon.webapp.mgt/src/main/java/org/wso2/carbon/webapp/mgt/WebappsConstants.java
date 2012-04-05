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

    /**
     * This is to filter out custom webapp types. If a custom webapp deployer is added, it should
     * set this as a property to filer out the custom type.
     */
    public static final String WEBAPP_FILTER = "webappFilter";

    public static final class WebappState {
        public static final String STARTED = "started";
        public static final String STOPPED = "stopped";

        private WebappState() {}
    }

    private WebappsConstants() {
    }
}
