package org.wso2.carbon.automation.core.utils.frameworkutils.productvariables;

public class Selenium {

    private String _browserName;
    private String _chromrDriverPath;
    private boolean _remoteWebDriver;
    private String _remoteWebDriverUrl;

    public String getBrowserName() {
        return _browserName;
    }

    public String getChromrDriverPath() {
        return _chromrDriverPath;
    }

    public boolean getRemoteWebDriver() {
        return _remoteWebDriver;
    }

    public String getRemoteWebDriverUrl() {
        return _remoteWebDriverUrl;
    }

    public void setSelenium(String browserName, String chromrDriverPath, boolean remoteWebDriver,
                            String remoteWebDriverUrl) {
        _browserName = browserName;
        _chromrDriverPath = chromrDriverPath;
        _remoteWebDriver = remoteWebDriver;
        _remoteWebDriverUrl = remoteWebDriverUrl;
    }
}
