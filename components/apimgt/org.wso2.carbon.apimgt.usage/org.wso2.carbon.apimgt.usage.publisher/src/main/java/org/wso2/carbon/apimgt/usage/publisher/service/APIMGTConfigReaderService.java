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
package org.wso2.carbon.apimgt.usage.publisher.service;

import org.apache.axis2.util.JavaUtils;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsagePublisherConstants;

public class APIMGTConfigReaderService {

    private String bamServerThriftPort;
    private String bamServerURL;
    private String bamServerUser;
    private String bamServerPassword;
    private String bamAgentTrustStore;
    private String bamAgentTrustStorePassword;
    private boolean enabled;

    public APIMGTConfigReaderService(APIManagerConfiguration config) {
        String enabledStr = config.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_ENABLED);
        enabled = enabledStr != null && JavaUtils.isTrueExplicitly(enabledStr);
        bamServerThriftPort = config.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_THRIFT_PORT);
        bamServerURL = config.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_BAM_SERVER_URL);
        bamServerUser = config.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_BAM_SERVER_USER);
        bamServerPassword = config.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_BAM_SERVER_PASSWORD);
        bamAgentTrustStore = config.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_BAM_TRUSTSTORE);
        bamAgentTrustStorePassword = config.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_BAM_TRUSTSTORE_PASSWORD);
    }

    public String getBamServerThriftPort() {
        return bamServerThriftPort;
    }

    public String getBamAgentTrustStore() {
        return bamAgentTrustStore;
    }

    public String getBamServerPassword() {
        return bamServerPassword;
    }

    public String getBamServerUser() {
        return bamServerUser;
    }

    public String getBamServerURL() {
        return bamServerURL;
    }
    
    public String getBamAgentTrustStorePassword() {
        return bamAgentTrustStorePassword;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
