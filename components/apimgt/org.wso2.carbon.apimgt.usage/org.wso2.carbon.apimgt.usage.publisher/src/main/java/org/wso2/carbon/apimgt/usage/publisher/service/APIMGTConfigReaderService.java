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
