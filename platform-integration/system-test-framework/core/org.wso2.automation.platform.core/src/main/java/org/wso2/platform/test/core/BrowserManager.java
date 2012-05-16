/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.platform.test.core;

import com.opera.core.systems.OperaDriver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class BrowserManager {
    private static final Log log = LogFactory.getLog(BrowserManager.class);
    public static WebDriver driver;
    static EnvironmentBuilder env = new EnvironmentBuilder();

    public static WebDriver getWebDriver() throws MalformedURLException {
        String driverSelection = env.getFrameworkSettings().getSelenium().getBrowserName();
        if (env.getFrameworkSettings().getSelenium().getRemoteWebDriver()) {
            log.info("Test runs on remote browser");
            getRemoteWebDriver();
            driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
            return driver;
        } else {
            log.info("Test runs on " + driverSelection + "browser");
            getDriver(driverSelection);
            driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
            return driver;
        }
    }

    private static void getDriver(String driverSelection) {
        if (driverSelection.equalsIgnoreCase(ProductConstant.FIREFOX_BROWSER)) {
            driver = new FirefoxDriver();
        } else if (driverSelection.equalsIgnoreCase(ProductConstant.CHROME_BROWSER)) {
            driver = new ChromeDriver();
            System.setProperty("webdriver.chrome.driver", env.getFrameworkSettings().getSelenium().getChromrDriverPath());
        } else if (driverSelection.equalsIgnoreCase(ProductConstant.IE_BROWSER)) {
            driver = new InternetExplorerDriver();
        } else if (driverSelection.equalsIgnoreCase(ProductConstant.HTML_UNIT_DRIVER)) {
            driver = new HtmlUnitDriver(true);
            System.setProperty("webdriver.chrome.driver", env.getFrameworkSettings().getSelenium().getChromrDriverPath());
        } else {
            driver = new OperaDriver();
        }
    }

    private static void getRemoteWebDriver() throws MalformedURLException {
        URL url;
        String browserName = env.getFrameworkSettings().getSelenium().getBrowserName();
        String remoteWebDriverURL = env.getFrameworkSettings().getSelenium().getRemoteWebDriverUrl();
        if (log.isDebugEnabled()) {
            log.debug("Browser selection " + browserName);
            log.debug("Remote WebDriverURL " + remoteWebDriverURL);
        }
        try {
            url = new URL(remoteWebDriverURL);
        } catch (MalformedURLException e) {
            log.error("Malformed URL " + e.getMessage());
            throw new MalformedURLException("Malformed URL " + e.getMessage());
        }
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setJavascriptEnabled(true);
        capabilities.setBrowserName(browserName);
        driver = new RemoteWebDriver(url, capabilities);
    }
}
