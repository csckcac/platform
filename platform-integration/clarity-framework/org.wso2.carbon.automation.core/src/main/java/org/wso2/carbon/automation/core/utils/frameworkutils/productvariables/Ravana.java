package org.wso2.carbon.automation.core.utils.frameworkutils.productvariables;

public class Ravana {
    private String _jdbc_Url;
    private String _dbUser;
    private String _dbPassword;
    private String _frameworkPath;
    private String _testStatus;

    public String getJdbc_Url() {
        return _jdbc_Url;
    }

    public String getDbUser() {
        return _dbUser;
    }

    public String getDBpassword() {
        return _dbPassword;
    }

    public String getFrameworkPath() {
        return _frameworkPath;
    }

    public String getTestStatus() {
        return _testStatus;
    }

    public void setRavana(String jdbc_Url, String dbUser, String dbPassword, String frameworkPath,
                          String testStatus) {
        _jdbc_Url = jdbc_Url;
        _dbUser = dbUser;
        _dbPassword = dbPassword;
        _frameworkPath = frameworkPath;
        _testStatus = testStatus;
    }

}
