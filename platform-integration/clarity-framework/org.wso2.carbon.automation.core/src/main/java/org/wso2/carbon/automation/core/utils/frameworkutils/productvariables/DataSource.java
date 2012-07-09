package org.wso2.carbon.automation.core.utils.frameworkutils.productvariables;

public class DataSource {
    private String _dbDriverName;
    private String _dbUrl;
    private String _dbUser;
    private String _dbPassword;
    private String _dbName;

    private String _rssDbUser;
    private String _rssDbPassword;

    public String getM_dbDriverName() {
        return _dbDriverName;
    }

    public String getDbUrl() {
        return _dbUrl;
    }

    public String getDbUser() {
        return _dbUser;
    }

    public String getDbPassword() {
        return _dbPassword;
    }

    public String getDbName() {
        return _dbName;
    }

    public String getRssDbUser() {
        return _rssDbUser;
    }

    public String getRssDbPassword() {
        return _rssDbPassword;
    }

    public void setDatasource(String dbDriverName, String dbUrl, String dbUser, String dbPassword,
                              String dbName, String rssDbUser, String rssDbPassword) {
        _dbDriverName = dbDriverName;
        _dbUrl = dbUrl;
        _dbUser = dbUser;
        _dbPassword = dbPassword;
        _dbName = dbName;
        _rssDbUser = rssDbUser;
        _rssDbPassword = rssDbPassword;
    }
}
