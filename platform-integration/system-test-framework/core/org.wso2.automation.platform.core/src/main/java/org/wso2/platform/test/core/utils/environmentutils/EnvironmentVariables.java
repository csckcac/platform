package org.wso2.platform.test.core.utils.environmentutils;

import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.frameworkutils.productvariables.ProductVariables;

public class EnvironmentVariables {


    private String sessionCookie;
    private String backEndUrl;
    private String serviceUrl;
    private UserInfo userDetails;
    private AdminServiceAuthentication adminServiceAuthentication;
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

    public AdminServiceAuthentication getAdminServiceAuthentication() {
        return adminServiceAuthentication;
    }

    public ProductVariables getProductVariables()
    {
        return productVariables;
    }

    public void setEnvironment
            (String _cookie, String _backendUrl, String _serviceUrl, UserInfo user,
             AdminServiceAuthentication authentication) {
        this.sessionCookie = _cookie;
        this.backEndUrl = _backendUrl;
        this.serviceUrl = _serviceUrl;
        this.userDetails = user;
        this.adminServiceAuthentication = authentication;
    }
    public void setEnvironment
            (String _cookie, String _backendUrl, String _serviceUrl, UserInfo user,
             AdminServiceAuthentication authentication,ProductVariables productVariables) {
        this.sessionCookie = _cookie;
        this.backEndUrl = _backendUrl;
        this.serviceUrl = _serviceUrl;
        this.userDetails = user;
        this.adminServiceAuthentication = authentication;
        this.productVariables=productVariables;
    }

    public void setEnvironment
            (String _cookie, String _backendUrl, String _serviceUrl,String  webAppURL, UserInfo user,
             AdminServiceAuthentication authentication,ProductVariables productVariables) {
        this.sessionCookie = _cookie;
        this.backEndUrl = _backendUrl;
        this.serviceUrl = _serviceUrl;
        this.userDetails = user;
        this.webAppURL = webAppURL;
        this.adminServiceAuthentication = authentication;
        this.productVariables=productVariables;
    }
}
