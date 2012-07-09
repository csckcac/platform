package org.wso2.carbon.automation.core.utils.frameworkutils.productvariables;

import java.util.ArrayList;
import java.util.List;

public class EnvironmentVariables {

    private String deploymentFrameworkPath;
    private List<String> productList = new ArrayList<String>();
    private int deploymentDelay;
    private String ldapUserName;
    private String ldapPasswd;
    private String keystorePath;
    private String keyStrorePassword;

    public String getDeploymentFrameworkPath() {
        return deploymentFrameworkPath;
    }

    public List<String> getProductList() {
        return productList;
    }

    public int getDeploymentDelay() {
        return deploymentDelay;
    }

    public String getLdapUserName() {
        return ldapUserName;
    }

    public String getLdapPasswd() {
        return ldapPasswd;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public String getKeyStrorePassword() {
        return keyStrorePassword;
    }

    public void setEnvironmentVariables(String deploymentFrameworkPath, List<String> productList,
                                        int deploymentDelay, String ldapUserName, String ldapPasswd,
                                        String keystorePath, String keyStrorePassword) {

        this.deploymentDelay = deploymentDelay;
        this.deploymentFrameworkPath = deploymentFrameworkPath;
        this.productList = productList;
        this.keystorePath = keystorePath;
        this.keyStrorePassword = keyStrorePassword;
        this.ldapUserName = ldapUserName;
        this.ldapPasswd = ldapPasswd;
    }
}
