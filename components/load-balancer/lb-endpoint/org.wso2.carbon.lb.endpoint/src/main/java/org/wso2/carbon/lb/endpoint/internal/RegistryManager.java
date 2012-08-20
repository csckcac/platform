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
package org.wso2.carbon.lb.endpoint.internal;

import org.wso2.carbon.lb.endpoint.util.ConfigHolder;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.session.UserRegistry;

public class RegistryManager {
    UserRegistry governanceRegistry = ConfigHolder.getInstance().getGovernanceRegistry();
    /**
     *   the path in which URL mapping gets stored in registry
     */
    public static final String HOSTINFO = "hostinfo/";

    /**
     *   webapp parameter in hostinfo
     */
    public static final String WEB_APP = "web.app";

    /**
     * service parameter in hostinfo
     */
    public static final String SERVICE_EPR = "service.epr";


    /**
     * @param hostName
     *            The properties Virtual host name to be retrieved
     * @return The properties of virtual host from registry
     * @throws Exception
     */
    public String getApplicationURLFromRegistry(String hostName) throws Exception {
        Resource resource = null;
        if (governanceRegistry.resourceExists(HOSTINFO + hostName)) {
            resource = governanceRegistry.get(HOSTINFO + hostName);
        }
        String appName = null;
        if (resource != null) {
            appName = resource.getProperty(WEB_APP);
            if(appName != null) {
                return appName;
            } else {
                appName = resource.getProperty(SERVICE_EPR);
            }
        }
        return appName;
    }
}