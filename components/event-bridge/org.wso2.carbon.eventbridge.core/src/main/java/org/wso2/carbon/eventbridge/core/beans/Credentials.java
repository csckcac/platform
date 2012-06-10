package org.wso2.carbon.eventbridge.core.beans;

public class Credentials {
    private final String domainName;
    private final String username;
    private final String password;

    public Credentials(String domainName, String username, String password) {
        this.domainName = domainName;
        this.username = username;
        this.password = password;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
