package org.wso2.esb.integration.message.store.controller.config;

public class JMSBrokerConfiguration {

	private String serverName;
    private String providerURL;
    private String initialNamingFactory;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getProviderURL() {
        return providerURL;
    }

    public void setProviderURL(String providerURL) {
        this.providerURL = providerURL;
    }

    public String getInitialNamingFactory() {
        return initialNamingFactory;
    }

    public void setInitialNamingFactory(String initialNamingFactory) {
        this.initialNamingFactory = initialNamingFactory;
    }
}
