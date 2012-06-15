package org.wso2.carbon.eventbridge.commons;

public class Credentials {
    private final String username;
    private final String password;
    private final String domainName;

    public Credentials(String username, String password) {
        this.username = username;
        this.password = password;
        String[] userNameParts = username.split("@");
        if (userNameParts.length == 2) {
            domainName = userNameParts[1];
        } else {
            domainName = null;
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDomainName() {
        return domainName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Credentials)) {
            return false;
        }

        Credentials that = (Credentials) o;

        if (!password.equals(that.password)) {
            return false;
        }
        if (!username.equals(that.username)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + password.hashCode();
        return result;
    }
}
