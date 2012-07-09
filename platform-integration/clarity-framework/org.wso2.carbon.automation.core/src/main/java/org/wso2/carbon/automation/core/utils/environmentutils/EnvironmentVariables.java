package org.wso2.carbon.automation.core.utils.environmentutils;

import org.wso2.carbon.automation.api.clients.authenticators.AuthenticatorClient;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.frameworkutils.productvariables.ProductVariables;

public class EnvironmentVariables {


    private String sessionCookie;
    private String backEndUrl;
    private String serviceUrl;
    private UserInfo userDetails;
    private AuthenticatorClient adminServiceAuthentication;
    private ProductVariables productVariables;
    private String webAppURL;

    public String getSessionCookie() {
        return sessionCookie;
    }

    public String getBackEndUrl() {
        return backEndUrl;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public String getWebAppURL() {
        return webAppURL;
    }

    public AuthenticatorClient getAdminServiceAuthentication() {
        return adminServiceAuthentication;
    }

    public ProductVariables getProductVariables() {
        return productVariables;
    }

    public void setEnvironment
            (String _cookie, String _backendUrl, String _serviceUrl, UserInfo user,
             AuthenticatorClient authentication) {
        this.sessionCookie = _cookie;
        this.backEndUrl = _backendUrl;
        this.serviceUrl = _serviceUrl;
        this.userDetails = user;
        this.adminServiceAuthentication = authentication;
    }

    public void setEnvironment
            (String _cookie, String _backendUrl, String _serviceUrl, UserInfo user,
             AuthenticatorClient authentication, ProductVariables productVariables) {
        this.sessionCookie = _cookie;
        this.backEndUrl = _backendUrl;
        this.serviceUrl = _serviceUrl;
        this.userDetails = user;
        this.adminServiceAuthentication = authentication;
        this.productVariables = productVariables;
    }

    public void setEnvironment
            (String _cookie, String _backendUrl, String _serviceUrl, String webAppURL,
             UserInfo user,
             AuthenticatorClient authentication, ProductVariables productVariables) {
        this.sessionCookie = _cookie;
        this.backEndUrl = _backendUrl;
        this.serviceUrl = _serviceUrl;
        this.userDetails = user;
        this.webAppURL = webAppURL;
        this.adminServiceAuthentication = authentication;
        this.productVariables = productVariables;
    }

    public void setEnvironment
            (String _backendUrl, String _serviceUrl, UserInfo user,
             ProductVariables productVariables) {
        this.backEndUrl = _backendUrl;
        this.serviceUrl = _serviceUrl;
        this.userDetails = user;
        this.productVariables = productVariables;
    }
}
